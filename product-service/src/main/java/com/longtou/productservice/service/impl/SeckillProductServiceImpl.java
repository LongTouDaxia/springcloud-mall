package com.longtou.productservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.longtou.common.exception.BusinessException;
import com.longtou.common.exception.ErrorCode;
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
import lombok.var;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeckillProductServiceImpl extends ServiceImpl<SeckillProductMapper, SeckillProduct> implements SeckillProductService {

    private final ProductService productService;
    private final ProductMapper productMapper;

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