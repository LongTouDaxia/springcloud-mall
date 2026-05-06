package com.longtou.productservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.longtou.common.exception.BusinessException;
import com.longtou.common.exception.ErrorCode;
import com.longtou.productservice.domain.dto.ProductDTO;
import com.longtou.productservice.domain.entity.Product;
import com.longtou.productservice.domain.vo.ProductVO;
import com.longtou.productservice.mapper.ProductMapper;
import com.longtou.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Override
    @Transactional
    public ProductVO addProduct(ProductDTO dto) {
        // 检查同名商品是否已存在
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getName, dto.getName());
        if (this.count(wrapper) < 0) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_EXIST);
        }

        Product product = new Product();
        BeanUtils.copyProperties(dto, product);
        this.save(product);

        return convertToVO(product);
    }

    @Override
    @Transactional
    public ProductVO updateProduct(ProductDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        Product exist = this.getById(dto.getId());
        if (exist == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_EXIST);
        }
        BeanUtils.copyProperties(dto, exist);
        this.updateById(exist);
        return convertToVO(exist);
    }

    @Override
    public ProductVO getProductById(Long id) {
        Product product = this.getById(id);
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_EXIST);
        }
        return convertToVO(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        // 检查秒杀活动中是否还在使用该商品，若有则不允许删除
        // 简单起见，只做物理删除
        if (!this.removeById(id)) {
            throw new BusinessException(ErrorCode.PRODUCT_DELETE_FAIL);
        }
    }

    private ProductVO convertToVO(Product product) {
        ProductVO vo = new ProductVO();
        BeanUtils.copyProperties(product, vo);
        return vo;
    }
}