package edu.fjut.mall.inventory.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminInventoryVO {

    private Long skuId;
    private String skuCode;
    private String skuName;
    private String productName;
    private String image;
    private Integer totalStock;
    private Integer lockedStock;
    private Integer availableStock;
    private Integer safetyStock;
    private Boolean warning;
    private LocalDateTime updateTime;
}
