package edu.fjut.mall.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度3-50位")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度6-100位")
    private String password;

    private String phone;

    /** 注册角色：0-买家，1-商家；管理员不可通过公开注册创建 */
    @Min(value = 0, message = "注册角色不合法")
    @Max(value = 1, message = "注册角色不合法")
    private Integer role = 0;
}
