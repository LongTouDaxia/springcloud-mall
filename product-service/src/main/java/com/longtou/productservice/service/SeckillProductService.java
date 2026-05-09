package com.longtou.productservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.longtou.productservice.domain.dto.SeckillProductDTO;
import com.longtou.productservice.domain.dto.SeckillStockDTO;
import com.longtou.productservice.domain.entity.SeckillProduct;
import com.longtou.productservice.domain.vo.SeckillProductVO;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Service
public interface SeckillProductService extends IService<SeckillProduct> {
    SeckillProductVO addSeckillProduct(SeckillProductDTO dto);
    SeckillProductVO updateSeckillProduct(SeckillProductDTO dto);
    SeckillProductVO getSeckillProductById(Long id);
    void deleteSeckillProduct(Long id);
    List<SeckillProductVO> listOngoingSeckillProducts();      // 当前正在进行的秒杀

    Map<String, String> doSeckill(@Valid SeckillStockDTO seckillStockDTO);

    void decreaseStock(@Valid Long userId, Long quantity, Long seckillId);
    // void decreaseStock(SeckillStockDTO dto);                  // 扣减库存（乐观锁）
}