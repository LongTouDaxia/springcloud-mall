package com.longtou.productservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.longtou.commonapi.domain.dto.SeckillOrderMessage;
import com.longtou.commoncore.utils.UserContext;
import com.longtou.commonweb.exception.BusinessException;
import com.longtou.commoncore.constant.ErrorCode;
import com.longtou.productservice.domain.dto.SeckillProductDTO;
import com.longtou.productservice.domain.dto.SeckillStockDTO;
import com.longtou.productservice.domain.entity.Product;
import com.longtou.productservice.domain.entity.SeckillProduct;
import com.longtou.productservice.domain.vo.SeckillProductVO;
import com.longtou.productservice.mapper.ProductMapper;
import com.longtou.productservice.mapper.SeckillProductMapper;
import com.longtou.productservice.service.ProductService;
import com.longtou.productservice.service.SeckillProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.support.MessageBuilder;

import lombok.extern.slf4j.Slf4j;

import org.apache.rocketmq.spring.core.RocketMQTemplate;


import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillProductServiceImpl extends ServiceImpl<SeckillProductMapper, SeckillProduct> implements SeckillProductService {

    private final ProductService productService;
    private final ProductMapper productMapper;
    private final SeckillProductMapper seckillProductMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RocketMQTemplate rocketMQTemplate;


    //信息预热  存入秒杀商品库存
    private DefaultRedisScript<Long> stockLuaScript;
    //注解  表示bean生成后即执行这个方法
    @PostConstruct
    public void init(){

        LuaInit();
        log.info("Lua初始化成功");


        initSeckillStockToRedis();
        log.info("秒杀商品预热成功");

    }

    private void initSeckillStockToRedis() {
        List<SeckillProduct> seckillProducts = seckillProductMapper.selectList(null);

        for(SeckillProduct seckillProduct:seckillProducts){
            String stockKey = "seckill:stock:" + seckillProduct.getId();
            stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(seckillProduct.getStock()));
        }

    }
    private void LuaInit() {

        stockLuaScript = new DefaultRedisScript<>();

        stockLuaScript.setScriptText(
                "local stock_key = KEYS[1]\n" +
                        "local user_key = KEYS[2]\n" +
                        "local user_id = ARGV[1]\n" +
                        "local stock = redis.call('get', stock_key)\n" +
                        "if tonumber(stock) <= 0 then return 0 end\n" +
                        "local bought = redis.call('sismember', user_key, user_id)\n" +
                        "if bought == 1 then return 1 end\n" +
                        "redis.call('decr', stock_key)\n" +
                        "redis.call('sadd', user_key, user_id)\n" +
                        "return 2\n"
        );
        stockLuaScript.setResultType(Long.class);
    }
    @Override
    @Transactional
    public SeckillProductVO addSeckillProduct(SeckillProductDTO dto) {
        // 校验关联的普通商品是否存在
        Product product = productService.getById(dto.getProductId());
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_EXIST);
        }

        // 校验时间段是否重叠（简单起见不实现）
        SeckillProduct seckillProduct = new SeckillProduct();
        BeanUtils.copyProperties(dto, seckillProduct);
        this.save(seckillProduct);
        return convertToVO(seckillProduct, product);
    }

    @Override
    @Transactional
    public SeckillProductVO updateSeckillProduct(SeckillProductDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        SeckillProduct exist = this.getById(dto.getId());
        if (exist == null) {
            throw new BusinessException(ErrorCode.SECKILL_PRODUCT_NOT_EXIST);
        }
        BeanUtils.copyProperties(dto, exist);
        this.updateById(exist);
        Product product = productService.getById(exist.getProductId());
        return convertToVO(exist, product);
    }

    @Override
    public SeckillProductVO getSeckillProductById(Long id) {
        SeckillProduct sp = this.getById(id);
        if (sp == null) {
            throw new BusinessException(ErrorCode.SECKILL_PRODUCT_NOT_EXIST);
        }
        Product product = productService.getById(sp.getProductId());
        return convertToVO(sp, product);
    }

    @Override
    @Transactional
    public void deleteSeckillProduct(Long id) {
        if (!this.removeById(id)) {
            throw new BusinessException(ErrorCode.SECKILL_PRODUCT_DELETE_FAIL);
        }
    }

    @Override
    public List<SeckillProductVO> listOngoingSeckillProducts() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<SeckillProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(SeckillProduct::getStartTime, now)
               .ge(SeckillProduct::getEndTime, now);
        List<SeckillProduct> seckillList = this.list(wrapper);
        if (seckillList.isEmpty()) {
            return Collections.emptyList();
        }
        // 批量查询秒杀活动关联商品

        List<Long> productIds = seckillList.stream()
                .map(SeckillProduct::getProductId)
                .collect(Collectors.toList());
        List<Product> products = productService.listByIds(productIds);
        var productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        return seckillList.stream()
                .map(sp -> convertToVO(sp, productMap.get(sp.getProductId())))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, String> doSeckill(@Valid SeckillStockDTO seckillStockDTO) {
        Long userId = UserContext.getCurrentUserId();
        Long seckillId = seckillStockDTO.getSeckillId();
        Integer quantity = seckillStockDTO.getQuantity();

        Map<String, String> map = new HashMap<>();
        // 1. 参数校验
        if (userId == null || seckillId == null || quantity == null || quantity <= 0) {
            throw new BusinessException(500,"参数异常");
        }
        //资格校验
        String stockKey = "seckill:stock:" + seckillId;
        String userKey = "seckill:user:" + seckillId;

        //获取用户状态  0表示库存不足 1表示用户已购买 2表示下单成功
        //redis之星lua教本
        Long result = stringRedisTemplate.execute(stockLuaScript,
                Arrays.asList(stockKey, userKey),
                userId.toString());

        if (result == 0) {
            throw new BusinessException(500,"库存不足");
        }
        if (result == 1) {
            log.info("用户买过了");
            map.put("message","不能重复下单");
            return map;
        }
        //创建订单
        //校验成功  生成订单唯一id
        String orderToken = UUID.randomUUID().toString().replaceAll("-", "");

        // 3. 发送消息到 RabbitMQ（异步创建订单）
       // SeckillOrderMessage message = new SeckillOrderMessage(userId, seckillId, quantity,orderToken);

       //改用rocketmq发送消息并实现事务一致性
        // rabbitTemplate.convertAndSend("cloud_seckill.exchange", "seckill.order", message);

        SeckillOrderMessage message = new SeckillOrderMessage(userId, seckillId, quantity, orderToken);

        rocketMQTemplate.sendMessageInTransaction("seckill-topic",
                MessageBuilder.withPayload(message).build(),
                message);
        log.info("已发送 RocketMQ 事务消息至 seckill_topic: {}", orderToken);


        map.put("tokenId",orderToken);
        map.put("wsUrl","/ws/seckill/"+orderToken);//告诉前端完整路径
        return map;

    }

    @Override
    public void decreaseStock(Long userId, Long quantity, Long seckillId) {
        // 1. 查询秒杀商品信息
        SeckillProduct seckillProduct = query().eq("id", seckillId).one();

        // 2. 校验库存是否充足
        if (seckillProduct == null || seckillProduct.getStock() < quantity) {
            throw new BusinessException(500, "库存不足或商品不存在");
        }

        Integer version = seckillProduct.getVersion();
        // 3. 扣减库存（乐观锁方式，防止超卖）

        // 乐观锁扣减库存
        boolean updateResult = lambdaUpdate()
                .setSql("stock = stock - " + quantity)   // 注意这里拼接 quantity，小心 SQL 注入？但 quantity 是 Long 类型且来自内部，问题不大；更安全可用 set("stock", "stock - " + quantity) 但 MyBatis-Plus 的 set 不支持表达式，所以用 setSql
                .eq(SeckillProduct::getId, seckillId)
                .eq(SeckillProduct::getVersion, version)  // 乐观锁条件
                .update();  // 返回 boolean，实际上影响行数>0 则 true

        if (!updateResult) {
            // 更新失败说明库存被其他线程抢先扣减导致版本号/库存条件不满足
            throw new BusinessException(500, "扣减库存失败，请重试");
        }

        // 4. 可选：记录用户秒杀明细、日志等
        log.info("用户 {} 秒杀商品 {}，扣减库存 {}", userId, seckillId, quantity);
    }

 /*   @Override
    @Transactional
    public void decreaseStock(SeckillStockDTO dto) {
        // 先查询秒杀商品（带乐观锁 version）
        SeckillProduct sp = this.getById(dto.getSeckillId());
        if (sp == null) {
            throw new BusinessException(ErrorCode.SECKILL_PRODUCT_NOT_EXIST);
        }
        // 校验秒杀是否进行中
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(sp.getStartTime()) || now.isAfter(sp.getEndTime())) {
            throw new BusinessException(ErrorCode.SECKILL_NOT_STARTED_OR_ENDED);
        }
        if (sp.getStock() < dto.getQuantity()) {
            throw new BusinessException(ErrorCode.SECKILL_STOCK_INSUFFICIENT);
        }

        // 使用乐观锁更新
        int updated = baseMapper.decreaseStockWithVersion(
                dto.getSeckillId(),
                dto.getQuantity(),
                sp.getVersion()
        );
        if (updated == 0) {
            throw new BusinessException(ErrorCode.SECKILL_CONFLICT, "库存扣减失败，请重试");
        }
    }

  */

    private SeckillProductVO convertToVO(SeckillProduct sp, Product product) {
        SeckillProductVO vo = new SeckillProductVO();
        BeanUtils.copyProperties(sp, vo);
        if (product != null) {
            vo.setProductName(product.getName());
            vo.setProductDesc(product.getDescription());
            vo.setNormalPrice(product.getNormalPrice());
        }
        return vo;
    }
}