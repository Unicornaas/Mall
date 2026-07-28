package edu.fjut.mall.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ShipOrderRequest {

    @NotBlank(message = "物流公司不能为空")
    private String shippingCompany;

    @NotBlank(message = "运单号不能为空")
    private String trackingNo;
}
