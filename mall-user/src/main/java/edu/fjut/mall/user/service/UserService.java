package edu.fjut.mall.user.service;

import edu.fjut.mall.user.dto.*;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     */
    void register(RegisterRequest request);

    /**
     * 用户登录，返回 Token + 用户信息
     */
    LoginVO login(LoginRequest request);

    /**
     * 获取当前用户信息
     */
    UserVO getCurrentUser(Long userId);

    /**
     * 修改个人信息
     */
    void updateProfile(Long userId, UpdateProfileRequest request);

    /**
     * 修改密码
     */
    void changePassword(Long userId, ChangePasswordRequest request);
}
