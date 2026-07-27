package edu.fjut.mall.product.service.impl;

import edu.fjut.mall.common.exception.BusinessException;
import edu.fjut.mall.common.result.ResultCode;
import edu.fjut.mall.common.util.SnowflakeIdGenerator;
import edu.fjut.mall.product.dto.SpuRequest;
import edu.fjut.mall.product.entity.ProductSpu;
import edu.fjut.mall.product.mapper.ProductSpuMapper;
import edu.fjut.mall.product.service.ProductSpuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品 SPU 服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSpuServiceImpl implements ProductSpuService {

    private final ProductSpuMapper spuMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    public List<ProductSpu> listByCategoryId(Long categoryId) {
        return spuMapper.selectByCategoryId(categoryId);
    }

    @Override
    public ProductSpu getById(Long id) {
        ProductSpu spu = spuMapper.selectById(id);
        if (spu == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "SPU不存在");
        }
        return spu;
    }

    @Override
    @Transactional
    public void add(SpuRequest request) {
        ProductSpu spu = new ProductSpu();
        spu.setId(snowflakeIdGenerator.nextId());
        spu.setCategoryId(request.getCategoryId());
        spu.setName(request.getName());
        spu.setDescription(request.getDescription());
        spu.setBrand(request.getBrand());
        spu.setMainImage(request.getMainImage());
        spu.setImages(request.getImages());
        spu.setStatus(1); // 默认上架
        spu.setCreateTime(LocalDateTime.now());
        spu.setUpdateTime(LocalDateTime.now());

        spuMapper.insert(spu);
        log.info("新增SPU: id={}, name={}", spu.getId(), spu.getName());
    }

    @Override
    @Transactional
    public void update(Long id, SpuRequest request) {
        getById(id);

        ProductSpu spu = new ProductSpu();
        spu.setId(id);
        spu.setName(request.getName());
        spu.setDescription(request.getDescription());
        spu.setBrand(request.getBrand());
        spu.setMainImage(request.getMainImage());
        spu.setImages(request.getImages());
        spu.setUpdateTime(LocalDateTime.now());

        spuMapper.updateById(spu);
        log.info("更新SPU: id={}", id);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        getById(id);
        spuMapper.updateStatus(id, status);
        log.info("SPU上下架: id={}, status={}", id, status);
    }
}
