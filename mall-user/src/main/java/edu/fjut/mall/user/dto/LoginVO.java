package edu.fjut.mall.user.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 登录返回结果（含 Token + 用户信息）
 */
@Data
@Builder
public class LoginVO {

    /** JWT Token */
    private String token;

    /** 用户信息 */
    private UserVO user;
}
