package edu.fjut.mall.product.service.impl;

import edu.fjut.mall.common.exception.BusinessException;
import edu.fjut.mall.common.result.ResultCode;
import edu.fjut.mall.common.util.SnowflakeIdGenerator;
import edu.fjut.mall.product.dto.SkuRequest;
import edu.fjut.mall.product.entity.ProductSku;
import edu.fjut.mall.product.mapper.ProductSkuMapper;
import edu.fjut.mall.product.service.ProductSkuService;
import edu.fjut.mall.product.service.ProductSpuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品 SKU 服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSkuServiceImpl implements ProductSkuService {

    private final ProductSkuMapper skuMapper;
    private final ProductSpuService spuService;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    public List<ProductSku> listBySpuId(Long spuId) {
        return skuMapper.selectBySpuId(spuId);
    }

    @Override
    public List<ProductSku> listEnabledBySpuId(Long spuId) {
        return skuMapper.selectEnabledBySpuId(spuId);
    }

    @Override
    public ProductSku getById(Long id) {
        ProductSku sku = skuMapper.selectById(id);
        if (sku == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "SKU不存在");
        }
        return sku;
    }

    @Override
    public ProductSku getEnabledById(Long id) {
        ProductSku sku = skuMapper.selectEnabledById(id);
        if (sku == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "SKU不存在或已禁用");
        }
        return sku;
    }

    @Override
    @Transactional
    public void add(SkuRequest request) {
        ProductSku sku = new ProductSku();
        sku.setId(snowflakeIdGenerator.nextId());
        sku.setSpuId(request.getSpuId());
        sku.setName(request.getName());
        sku.setSkuCode(request.getSkuCode());
        sku.setPrice(request.getPrice());
        sku.setStock(request.getStock());
        sku.setImages(request.getImages());
        sku.setSpecs(request.getSpecs());
        sku.setStatus(1);
        sku.setCreateTime(LocalDateTime.now());
        sku.setUpdateTime(LocalDateTime.now());

        skuMapper.insert(sku);
        log.info("新增SKU: id={}, name={}", sku.getId(), sku.getName());
    }

    @Override
    @Transactional
    public void update(Long id, SkuRequest request) {
        getById(id);

        ProductSku sku = new ProductSku();
        sku.setId(id);
        sku.setName(request.getName());
        sku.setSkuCode(request.getSkuCode());
        sku.setPrice(request.getPrice());
        sku.setStock(request.getStock());
        sku.setImages(request.getImages());
        sku.setSpecs(request.getSpecs());
        sku.setUpdateTime(LocalDateTime.now());

        skuMapper.updateById(sku);
        log.info("更新SKU: id={}", id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getById(id);
        skuMapper.deleteById(id);
        log.info("删除SKU: id={}", id);
    }

    @Override
    @Transactional
    public void addForSeller(SkuRequest request, Long sellerId) {
        spuService.getByIdForSeller(request.getSpuId(), sellerId);
        add(request);
        log.info("商家新增SKU: sellerId={}, spuId={}", sellerId, request.getSpuId());
    }

    @Override
    @Transactional
    public void updateForSeller(Long id, SkuRequest request, Long sellerId) {
        ProductSku existing = getById(id);
        spuService.getByIdForSeller(existing.getSpuId(), sellerId);
        if (!existing.getSpuId().equals(request.getSpuId())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "不能修改SKU所属商品");
        }
        update(id, request);
        log.info("商家更新SKU: id={}, sellerId={}", id, sellerId);
    }

    @Override
    @Transactional
    public void updateStatusForSeller(Long id, Integer status, Long sellerId) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "SKU状态只能为0或1");
        }
        ProductSku existing = getById(id);
        spuService.getByIdForSeller(existing.getSpuId(), sellerId);
        skuMapper.updateStatus(id, status);
        log.info("商家更新SKU状态: id={}, sellerId={}, status={}", id, sellerId, status);
    }
}
