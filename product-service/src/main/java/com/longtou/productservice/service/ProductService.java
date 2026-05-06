package com.longtou.productservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.longtou.productservice.domain.dto.ProductDTO;
import com.longtou.productservice.domain.entity.Product;
import com.longtou.productservice.domain.vo.ProductVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ProductService extends IService<Product> {
    ProductVO addProduct(ProductDTO dto);
    ProductVO updateProduct(ProductDTO dto);
    ProductVO getProductById(Long id);
    void deleteProduct(Long id);

    List<ProductVO> getProductByIds(List<Long> ids);
}