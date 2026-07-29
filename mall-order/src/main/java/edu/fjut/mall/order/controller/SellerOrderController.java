package edu.fjut.mall.order.controller;

import edu.fjut.mall.common.exception.BusinessException;
import edu.fjut.mall.common.page.PageResult;
import edu.fjut.mall.common.result.Result;
import edu.fjut.mall.common.result.ResultCode;
import edu.fjut.mall.order.dto.SellerOrderPageQuery;
import edu.fjut.mall.order.dto.SellerOrderVO;
import edu.fjut.mall.order.dto.ShipOrderRequest;
import edu.fjut.mall.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 商家订单接口。商家仅能操作自己店铺对应的子订单。 */
@RestController
@RequestMapping("/api/seller/orders")
@RequiredArgsConstructor
public class SellerOrderController {

    private final OrderService orderService;

    @GetMapping("/page")
    public Result<PageResult<SellerOrderVO>> page(@ModelAttribute SellerOrderPageQuery query,
                                                   @RequestHeader("X-User-Id") Long sellerId,
                                                   @RequestHeader("X-User-Role") Integer role) {
        requireSeller(role);
        return Result.success(orderService.pageForSeller(query, sellerId));
    }

    @GetMapping("/{id}")
    public Result<SellerOrderVO> detail(@PathVariable Long id,
                                         @RequestHeader("X-User-Id") Long sellerId,
                                         @RequestHeader("X-User-Role") Integer role) {
        requireSeller(role);
        return Result.success(orderService.getByIdForSeller(id, sellerId));
    }

    @PutMapping("/{id}/ship")
    public Result<Void> ship(@PathVariable Long id,
                             @Valid @RequestBody ShipOrderRequest request,
                             @RequestHeader("X-User-Id") Long sellerId,
                             @RequestHeader("X-User-Role") Integer role) {
        requireSeller(role);
        orderService.shipForSeller(id, request, sellerId);
        return Result.success("订单已发货", null);
    }

    private void requireSeller(Integer role) {
        if (role == null || role != 1) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "仅商家可以访问商家订单数据");
        }
    }
}
