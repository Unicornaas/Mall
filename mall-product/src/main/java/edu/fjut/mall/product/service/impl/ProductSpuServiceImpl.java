package edu.fjut.mall.product.service.impl;

import edu.fjut.mall.common.exception.BusinessException;
import edu.fjut.mall.common.result.ResultCode;
import edu.fjut.mall.common.util.SnowflakeIdGenerator;
import edu.fjut.mall.product.dto.SpuRequest;
import edu.fjut.mall.product.dto.AdminProductPageQuery;
import edu.fjut.mall.product.dto.SellerProductPageQuery;
import edu.fjut.mall.common.page.PageResult;
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
    public void add(SpuRequest request, Long sellerId) {
        ProductSpu spu = new ProductSpu();
        spu.setId(snowflakeIdGenerator.nextId());
        spu.setSellerId(sellerId);
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
        log.info("新增SPU: id={}, sellerId={}, name={}", spu.getId(), sellerId, spu.getName());
    }

    @Override
    @Transactional
    public void update(Long id, SpuRequest request) {
        getById(id);

        ProductSpu spu = new ProductSpu();
        spu.setId(id);
        spu.setCategoryId(request.getCategoryId());
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

    @Override
    public PageResult<ProductSpu> pageForAdmin(AdminProductPageQuery query) {
        List<ProductSpu> records = spuMapper.selectPageForAdmin(query);
        long total = spuMapper.countForAdmin(query);
        return new PageResult<>(records, total, query.getPageNum(), query.getPageSize());
    }

    @Override
    public PageResult<ProductSpu> pageForSeller(SellerProductPageQuery query, Long sellerId) {
        List<ProductSpu> records = spuMapper.selectPageForSeller(sellerId, query);
        long total = spuMapper.countForSeller(sellerId, query);
        return new PageResult<>(records, total, query.getPageNum(), query.getPageSize());
    }

    @Override
    public ProductSpu getByIdForSeller(Long id, Long sellerId) {
        ProductSpu spu = spuMapper.selectByIdAndSellerId(id, sellerId);
        if (spu == null) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权访问该商品");
        }
        return spu;
    }

    @Override
    @Transactional
    public void addForSeller(SpuRequest request, Long sellerId) {
        ProductSpu spu = new ProductSpu();
        spu.setId(snowflakeIdGenerator.nextId());
        spu.setSellerId(sellerId);
        spu.setCategoryId(request.getCategoryId());
        spu.setName(request.getName());
        spu.setDescription(request.getDescription());
        spu.setBrand(request.getBrand());
        spu.setMainImage(request.getMainImage());
        spu.setImages(request.getImages());
        spu.setStatus(0);
        spu.setCreateTime(LocalDateTime.now());
        spu.setUpdateTime(LocalDateTime.now());

        spuMapper.insert(spu);
        log.info("商家新增SPU: id={}, sellerId={}, name={}", spu.getId(), sellerId, spu.getName());
    }

    @Override
    @Transactional
    public void updateForSeller(Long id, SpuRequest request, Long sellerId) {
        getByIdForSeller(id, sellerId);

        ProductSpu spu = new ProductSpu();
        spu.setId(id);
        spu.setCategoryId(request.getCategoryId());
        spu.setName(request.getName());
        spu.setDescription(request.getDescription());
        spu.setBrand(request.getBrand());
        spu.setMainImage(request.getMainImage());
        spu.setImages(request.getImages());
        spu.setUpdateTime(LocalDateTime.now());

        spuMapper.updateById(spu);
        log.info("商家更新SPU: id={}, sellerId={}", id, sellerId);
    }

    @Override
    @Transactional
    public void updateStatusForSeller(Long id, Integer status, Long sellerId) {
        validateStatus(status);
        getByIdForSeller(id, sellerId);
        spuMapper.updateStatus(id, status);
        log.info("商家更新SPU状态: id={}, sellerId={}, status={}", id, sellerId, status);
    }

    private void validateStatus(Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "商品状态只能为0或1");
        }
    }
}
