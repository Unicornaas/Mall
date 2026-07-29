package edu.fjut.mall.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ShipOrderRequest {

    @NotBlank(message = "物流公司不能为空")
    private String shippingCompany;

    @NotBlank(message = "运单号不能为空")
    private String trackingNo;

    /** Null means all currently shippable items, preserving the old API. */
    private List<ShipItemRequest> items;
}
