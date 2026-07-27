package edu.fjut.mall.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 地址请求
 */
@Data
public class AddressRequest {

    @NotBlank(message = "收货人姓名不能为空")
    private String receiverName;

    @NotBlank(message = "收货人电话不能为空")
    private String receiverPhone;

    @NotBlank(message = "省份不能为空")
    private String province;

    @NotBlank(message = "城市不能为空")
    private String city;

    @NotBlank(message = "区县不能为空")
    private String district;

    @NotBlank(message = "详细地址不能为空")
    private String detail;

    /** 是否默认: 0-否 1-是 */
    private Integer isDefault;
}
