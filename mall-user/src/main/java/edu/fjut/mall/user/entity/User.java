package edu.fjut.mall.user.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 用户实体（对应 user_t 表）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class User extends edu.fjut.mall.common.entity.BaseEntity {

    /** 用户名 */
    private String username;

    /** 加密后的密码 */
    private String password;

    /** 昵称 */
    private String nickname;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 头像URL */
    private String avatar;

    /** 角色: 0-买家 1-卖家 2-管理员 */
    private Integer role;

    /** 状态: 0-禁用 1-启用 */
    private Integer status;

    @Override
    public void setCreateTime(LocalDateTime createTime) {
        super.setCreateTime(createTime);
    }

    @Override
    public void setUpdateTime(LocalDateTime updateTime) {
        super.setUpdateTime(updateTime);
    }
}
