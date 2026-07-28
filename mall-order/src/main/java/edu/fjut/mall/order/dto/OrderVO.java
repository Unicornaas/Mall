package edu.fjut.mall.order.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderVO {
    private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal totalAmount;
    private Integer status;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String remark;
    private String shippingCompany;
    private String trackingNo;
    private LocalDateTime shipTime;
    private LocalDateTime receiveTime;
    private LocalDateTime createTime;
    private List<OrderItemVO> items;

    @Data
    @Builder
    public static class OrderItemVO {
        private Long id;
        private Long spuId;
        private Long skuId;
        private String productName;
        private String productImage;
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal totalPrice;
    }
}
