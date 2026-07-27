package edu.fjut.mall.user.controller;

import edu.fjut.mall.common.result.Result;
import edu.fjut.mall.user.dto.AddressRequest;
import edu.fjut.mall.user.entity.UserAddress;
import edu.fjut.mall.user.interceptor.LoginInterceptor;
import edu.fjut.mall.user.service.UserAddressService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收货地址接口
 */
@RestController
@RequestMapping("/api/user/addresses")
@RequiredArgsConstructor
public class UserAddressController {

    private final UserAddressService userAddressService;

    /**
     * 查询当前用户所有地址
     */
    @GetMapping
    public Result<List<UserAddress>> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(LoginInterceptor.USER_ID);
        return Result.success(userAddressService.list(userId));
    }

    /**
     * 查询单个地址
     */
    @GetMapping("/{id}")
    public Result<UserAddress> getById(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(LoginInterceptor.USER_ID);
        return Result.success(userAddressService.getById(id, userId));
    }

    /**
     * 新增地址
     */
    @PostMapping
    public Result<Void> add(@Valid @RequestBody AddressRequest addressRequest,
                             HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(LoginInterceptor.USER_ID);
        userAddressService.add(userId, addressRequest);
        return Result.success("新增成功", null);
    }

    /**
     * 修改地址
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                                @Valid @RequestBody AddressRequest addressRequest,
                                HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(LoginInterceptor.USER_ID);
        userAddressService.update(id, userId, addressRequest);
        return Result.success("修改成功", null);
    }

    /**
     * 删除地址
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(LoginInterceptor.USER_ID);
        userAddressService.delete(id, userId);
        return Result.success("删除成功", null);
    }
}
