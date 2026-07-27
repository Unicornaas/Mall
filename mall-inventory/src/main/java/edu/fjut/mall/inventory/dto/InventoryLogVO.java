package edu.fjut.mall.inventory.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class InventoryLogVO {
    private Long id;
    private Long skuId;
    private String orderNo;
    private String changeType;
    private Integer changeCount;
    private Integer beforeStock;
    private Integer afterStock;
    private LocalDateTime createTime;
}
