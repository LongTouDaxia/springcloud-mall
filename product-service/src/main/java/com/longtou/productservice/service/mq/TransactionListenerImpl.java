package com.longtou.productservice.service.mq;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.longtou.commonapi.domain.dto.SeckillOrderMessage;
import com.longtou.productservice.domain.entity.SeckillProduct;
import com.longtou.productservice.mapper.SeckillProductMapper;
import com.longtou.productservice.service.SeckillProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.transaction.Transaction;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
@RocketMQTransactionListener
public class TransactionListenerImpl implements RocketMQLocalTransactionListener
{
    private final SeckillProductMapper seckillProductMapper;
    private final SeckillProductService seckillProductService;

    private final Map<String,RocketMQLocalTransactionState>  transactionStateCache = new ConcurrentHashMap<>();

    //执行本地事务 比如扣减库存
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object o) {
        SeckillOrderMessage orderMessage = (SeckillOrderMessage) o;

        Long seckillId = orderMessage.getSeckillId();
        Long userId = orderMessage.getUserId();
        Integer quantity = orderMessage.getQuantity();
        String orderToken = orderMessage.getOrderToken();

        //查询秒杀商品

        try {
            SeckillProduct seckillProduct = seckillProductMapper.selectOne(new LambdaQueryWrapper<>(SeckillProduct.class)
                    .eq(SeckillProduct::getId, seckillId));

            if (seckillProduct == null || seckillProduct.getStock() < quantity) {
                log.info("商品不存在或库存不足");

                return RocketMQLocalTransactionState.ROLLBACK;//事务回滚
            }
            Integer version = seckillProduct.getVersion();

            //乐观锁口库存
            boolean update = seckillProductService.update().
                    setSql("stock = stock -" + quantity).
                    eq("id", seckillId).
                    eq("version", version).
                    gt("stock", 0)
                    .setSql("version = version +1").update();
            if (!update) {
                log.warn("乐观锁冲突，扣库存失败，事务回滚: seckillId={}", seckillId);
                return RocketMQLocalTransactionState.ROLLBACK;
            }
            // 这里也可以顺便记录一条“订单预创建”日志，用于回查
            log.info("本地事务成功，提交消息: orderToken={}", orderToken);
            //成功后把是无状态放进map
            transactionStateCache.put(orderToken, RocketMQLocalTransactionState.COMMIT);
            return RocketMQLocalTransactionState.COMMIT;
        }catch (Exception e){
            log.error("本地事务异常，回滚", e);
            return RocketMQLocalTransactionState.ROLLBACK;
        }

    }

    //事务回查（RocketMQ 问：这个订单到底有没有扣库存成功？）
    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {
        String orderToken = message.getHeaders().get("orderToken").toString();
        log.info("收到事务回查");
        //到数据库查库存是否扣减成功  这里先返回不知道
        RocketMQLocalTransactionState statue = transactionStateCache.get("orderToken");
        log.info("事务当前状态："+statue);
        return statue != null ? statue: RocketMQLocalTransactionState.UNKNOWN;

    }
}
