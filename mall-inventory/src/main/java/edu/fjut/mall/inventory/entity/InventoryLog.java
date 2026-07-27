package edu.fjut.mall.inventory.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InventoryLog {
    private Long id;
    private Long skuId;
    private String orderNo;
    private String changeType;   // LOCK-预占 DEDUCT-扣减 RELEASE-释放 ADD-补货 INIT-初始化
    private Integer changeCount;
    private Integer beforeStock;
    private Integer afterStock;
    private LocalDateTime createTime;
}
