package edu.fjut.mall.inventory.service;

import edu.fjut.mall.inventory.dto.*;

import java.util.List;

public interface InventoryService {
    InventoryVO query(Long skuId);
    List<InventoryVO> batchQuery(List<Long> skuIds);
    void lock(Long skuId, Integer quantity, String orderNo);
    void deduct(Long skuId, Integer quantity, String orderNo);
    void release(Long skuId, Integer quantity, String orderNo);
    void add(Long skuId, Integer quantity);
    void init(InventoryInitRequest request);
    List<InventoryLogVO> queryLog(Long skuId);
}
