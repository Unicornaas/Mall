package edu.fjut.mall.user.controller;

import edu.fjut.mall.common.result.Result;
import edu.fjut.mall.user.dto.*;
import edu.fjut.mall.user.interceptor.LoginInterceptor;
import edu.fjut.mall.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 注册
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return Result.success("注册成功", null);
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        LoginVO loginVO = userService.login(request);
        return Result.success(loginVO);
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/current")
    public Result<UserVO> currentUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(LoginInterceptor.USER_ID);
        return Result.success(userService.getCurrentUser(userId));
    }

    /**
     * 修改个人信息
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody UpdateProfileRequest request,
                                       HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute(LoginInterceptor.USER_ID);
        userService.updateProfile(userId, request);
        return Result.success("修改成功", null);
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                        HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute(LoginInterceptor.USER_ID);
        userService.changePassword(userId, request);
        return Result.success("密码修改成功", null);
    }
}
