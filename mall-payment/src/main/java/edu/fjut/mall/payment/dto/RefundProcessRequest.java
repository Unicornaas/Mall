package edu.fjut.mall.payment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RefundProcessRequest {

    @NotNull(message = "处理结果不能为空")
    private Integer refundStatus;

    @Size(max = 500, message = "处理备注不能超过500个字符")
    private String processRemark;
}
