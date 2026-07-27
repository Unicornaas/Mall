package edu.fjut.mall.user.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 收货地址实体（对应 user_address 表）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserAddress extends edu.fjut.mall.common.entity.BaseEntity {

    /** 用户ID */
    private Long userId;

    /** 收货人姓名 */
    private String receiverName;

    /** 收货人电话 */
    private String receiverPhone;

    /** 省份 */
    private String province;

    /** 城市 */
    private String city;

    /** 区/县 */
    private String district;

    /** 详细地址 */
    private String detail;

    /** 是否默认: 0-否 1-是 */
    private Integer isDefault;

    @Override
    public void setCreateTime(LocalDateTime createTime) {
        super.setCreateTime(createTime);
    }

    @Override
    public void setUpdateTime(LocalDateTime updateTime) {
        super.setUpdateTime(updateTime);
    }
}
