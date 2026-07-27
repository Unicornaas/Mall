package edu.fjut.mall.order.service;

import edu.fjut.mall.order.dto.CreateOrderRequest;
import edu.fjut.mall.order.dto.OrderVO;

import java.util.List;

public interface OrderService {
    OrderVO create(CreateOrderRequest request);
    OrderVO getById(Long id);
    List<OrderVO> listByUserId(Long userId);
    void cancel(Long id, Long userId);
}
