package edu.fjut.mall.inventory.service.impl;

import edu.fjut.mall.common.exception.BusinessException;
import edu.fjut.mall.common.result.ResultCode;
import edu.fjut.mall.common.util.SnowflakeIdGenerator;
import edu.fjut.mall.inventory.dto.*;
import edu.fjut.mall.inventory.entity.Inventory;
import edu.fjut.mall.inventory.entity.InventoryLog;
import edu.fjut.mall.inventory.mapper.InventoryLogMapper;
import edu.fjut.mall.inventory.mapper.InventoryMapper;
import edu.fjut.mall.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryMapper inventoryMapper;
    private final InventoryLogMapper inventoryLogMapper;
    private final SnowflakeIdGenerator idGen;

    @Override
    public InventoryVO query(Long skuId) {
        Inventory inv = inventoryMapper.selectBySkuId(skuId);
        if (inv == null) throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "库存记录不存在");
        return toVO(inv);
    }

    @Override
    public List<InventoryVO> batchQuery(List<Long> skuIds) {
        List<Inventory> list = inventoryMapper.selectBySkuIds(skuIds);
        return list.stream().map(this::toVO).toList();
    }

    @Override
    @Transactional
    public void lock(Long skuId, Integer quantity, String orderNo) {
        // 使用 FOR UPDATE 行锁防止并发超卖
        Inventory inv = inventoryMapper.selectBySkuIdForUpdate(skuId);
        if (inv == null) throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "库存记录不存在");
        if (inv.getAvailableStock() < quantity)
            throw new BusinessException(ResultCode.STOCK_INSUFFICIENT.getCode(),
                "SKU " + skuId + " 可用库存不足，当前可用: " + inv.getAvailableStock());

        int beforeStock = inv.getAvailableStock();
        inv.setAvailableStock(inv.getAvailableStock() - quantity);
        inv.setLockedStock(inv.getLockedStock() + quantity);
        inventoryMapper.updateStock(skuId, inv.getTotalStock(), inv.getLockedStock(), inv.getAvailableStock());

        saveLog(skuId, orderNo, "LOCK", quantity, beforeStock, inv.getAvailableStock());
        log.info("库存预占: skuId={}, quantity={}, orderNo={}, available={}", skuId, quantity, orderNo, inv.getAvailableStock());
    }

    @Override
    @Transactional
    public void deduct(Long skuId, Integer quantity, String orderNo) {
        Inventory inv = inventoryMapper.selectBySkuIdForUpdate(skuId);
        if (inv == null) throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "库存记录不存在");
        if (inv.getLockedStock() < quantity)
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "锁定库存不足");

        int beforeStock = inv.getTotalStock();
        inv.setTotalStock(inv.getTotalStock() - quantity);
        inv.setLockedStock(inv.getLockedStock() - quantity);
        inventoryMapper.updateStock(skuId, inv.getTotalStock(), inv.getLockedStock(), inv.getAvailableStock());

        saveLog(skuId, orderNo, "DEDUCT", quantity, beforeStock, inv.getTotalStock());
        log.info("库存扣减: skuId={}, quantity={}, orderNo={}, total={}", skuId, quantity, orderNo, inv.getTotalStock());
    }

    @Override
    @Transactional
    public void release(Long skuId, Integer quantity, String orderNo) {
        Inventory inv = inventoryMapper.selectBySkuIdForUpdate(skuId);
        if (inv == null) throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "库存记录不存在");
        if (inv.getLockedStock() < quantity)
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "锁定库存不足，无法释放");

        int beforeStock = inv.getAvailableStock();
        inv.setAvailableStock(inv.getAvailableStock() + quantity);
        inv.setLockedStock(inv.getLockedStock() - quantity);
        inventoryMapper.updateStock(skuId, inv.getTotalStock(), inv.getLockedStock(), inv.getAvailableStock());

        saveLog(skuId, orderNo, "RELEASE", quantity, beforeStock, inv.getAvailableStock());
        log.info("库存释放: skuId={}, quantity={}, orderNo={}, available={}", skuId, quantity, orderNo, inv.getAvailableStock());
    }

    @Override
    @Transactional
    public void add(Long skuId, Integer quantity) {
        Inventory inv = inventoryMapper.selectBySkuId(skuId);
        if (inv == null) throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "库存记录不存在");

        int beforeStock = inv.getTotalStock();
        inv.setTotalStock(inv.getTotalStock() + quantity);
        inv.setAvailableStock(inv.getAvailableStock() + quantity);
        inventoryMapper.updateStock(skuId, inv.getTotalStock(), inv.getLockedStock(), inv.getAvailableStock());

        saveLog(skuId, null, "ADD", quantity, beforeStock, inv.getTotalStock());
        log.info("库存补货: skuId={}, quantity={}, total={}", skuId, quantity, inv.getTotalStock());
    }

    @Override
    @Transactional
    public void init(InventoryInitRequest request) {
        Inventory existing = inventoryMapper.selectBySkuId(request.getSkuId());
        if (existing != null) throw new BusinessException(ResultCode.CONFLICT.getCode(), "该SKU库存已初始化");

        Inventory inv = new Inventory();
        inv.setId(idGen.nextId());
        inv.setSkuId(request.getSkuId());
        inv.setTotalStock(request.getTotalStock());
        inv.setLockedStock(0);
        inv.setAvailableStock(request.getTotalStock());
        inv.setSafetyStock(request.getSafetyStock() != null ? request.getSafetyStock() : 10);
        inv.setCreateTime(LocalDateTime.now());
        inv.setUpdateTime(LocalDateTime.now());
        inventoryMapper.insert(inv);

        saveLog(request.getSkuId(), null, "INIT", request.getTotalStock(), 0, request.getTotalStock());
        log.info("库存初始化: skuId={}, totalStock={}", request.getSkuId(), request.getTotalStock());
    }

    @Override
    public List<InventoryLogVO> queryLog(Long skuId) {
        List<InventoryLog> logs = inventoryLogMapper.selectBySkuId(skuId);
        return logs.stream().map(l -> InventoryLogVO.builder()
            .id(l.getId()).skuId(l.getSkuId()).orderNo(l.getOrderNo())
            .changeType(l.getChangeType()).changeCount(l.getChangeCount())
            .beforeStock(l.getBeforeStock()).afterStock(l.getAfterStock())
            .createTime(l.getCreateTime())
            .build()).toList();
    }

    private void saveLog(Long skuId, String orderNo, String changeType, Integer changeCount,
                         Integer beforeStock, Integer afterStock) {
        InventoryLog logEntry = new InventoryLog();
        logEntry.setId(idGen.nextId());
        logEntry.setSkuId(skuId);
        logEntry.setOrderNo(orderNo);
        logEntry.setChangeType(changeType);
        logEntry.setChangeCount(changeCount);
        logEntry.setBeforeStock(beforeStock);
        logEntry.setAfterStock(afterStock);
        logEntry.setCreateTime(LocalDateTime.now());
        inventoryLogMapper.insert(logEntry);
    }

    private InventoryVO toVO(Inventory inv) {
        return InventoryVO.builder()
            .id(inv.getId()).skuId(inv.getSkuId())
            .totalStock(inv.getTotalStock()).lockedStock(inv.getLockedStock())
            .availableStock(inv.getAvailableStock()).safetyStock(inv.getSafetyStock())
            .build();
    }
}
