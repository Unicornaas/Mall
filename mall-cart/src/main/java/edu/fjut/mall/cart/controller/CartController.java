package edu.fjut.mall.cart.controller;

import edu.fjut.mall.cart.dto.*;
import edu.fjut.mall.cart.service.CartService;
import edu.fjut.mall.common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * CART-01: 加入购物车
     */
    @PostMapping("/add")
    public Result<CartVO> add(@Valid @RequestBody CartAddRequest request,
                              @RequestHeader("X-User-Id") Long userId) {
        request.setUserId(userId);
        return Result.success(cartService.add(request));
    }

    /**
     * CART-02: 购物车列表
     */
    @GetMapping("/list")
    public Result<List<CartVO>> list(@RequestParam(required = false) Long ignoredUserId,
                                     @RequestHeader("X-User-Id") Long userId) {
        return Result.success(cartService.list(userId));
    }

    /**
     * CART-03: 修改数量
     */
    @PutMapping("/{id}/quantity")
    public Result<CartVO> updateQuantity(@PathVariable Long id,
                                         @Valid @RequestBody CartQuantityRequest request,
                                         @RequestParam(required = false) Long ignoredUserId,
                                         @RequestHeader("X-User-Id") Long userId) {
        return Result.success(cartService.updateQuantity(id, request.getQuantity(), userId));
    }

    /**
     * CART-04: 选中/取消选中
     */
    @PutMapping("/{id}/selected")
    public Result<CartVO> updateSelected(@PathVariable Long id,
                                         @Valid @RequestBody CartSelectedRequest request,
                                         @RequestParam(required = false) Long ignoredUserId,
                                         @RequestHeader("X-User-Id") Long userId) {
        return Result.success(cartService.updateSelected(id, request.getSelected(), userId));
    }

    /**
     * CART-05: 全选/取消全选
     */
    @PutMapping("/select-all")
    public Result<Void> selectAll(@RequestParam(required = false) Long ignoredUserId,
                                  @RequestHeader("X-User-Id") Long userId,
                                  @RequestParam Integer selected) {
        cartService.selectAll(userId, selected);
        return Result.success();
    }

    /**
     * CART-06: 删除购物车项
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id,
                               @RequestParam(required = false) Long ignoredUserId,
                               @RequestHeader("X-User-Id") Long userId) {
        cartService.delete(id, userId);
        return Result.success();
    }

    /**
     * CART-07: 批量删除
     */
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@Valid @RequestBody CartBatchDeleteRequest request,
                                    @RequestHeader("X-User-Id") Long userId) {
        cartService.batchDelete(userId, request.getIds());
        return Result.success();
    }

    /**
     * CART-08: 购物车商品数量
     */
    @GetMapping("/count")
    public Result<Integer> count(@RequestParam(required = false) Long ignoredUserId,
                                 @RequestHeader("X-User-Id") Long userId) {
        return Result.success(cartService.count(userId));
    }
}
