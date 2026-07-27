package edu.fjut.mall.user.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 用户信息返回（脱敏，不含密码）
 */
@Data
@Builder
public class UserVO {

    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String email;
    private String avatar;
    private Integer role;
    private Integer status;
}
