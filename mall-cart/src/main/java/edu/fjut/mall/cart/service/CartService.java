package edu.fjut.mall.cart.service;

import edu.fjut.mall.cart.dto.CartAddRequest;
import edu.fjut.mall.cart.dto.CartVO;

import java.util.List;

public interface CartService {
    CartVO add(CartAddRequest request);
    List<CartVO> list(Long userId);
    CartVO updateQuantity(Long id, Integer quantity, Long userId);
    CartVO updateSelected(Long id, Integer selected, Long userId);
    void selectAll(Long userId, Integer selected);
    void delete(Long id, Long userId);
    void batchDelete(Long userId, List<Long> ids);
    int count(Long userId);
}
