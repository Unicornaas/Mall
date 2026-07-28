package edu.fjut.mall.user.controller;

import edu.fjut.mall.common.exception.BusinessException;
import edu.fjut.mall.common.page.PageResult;
import edu.fjut.mall.common.result.Result;
import edu.fjut.mall.common.result.ResultCode;
import edu.fjut.mall.user.dto.AdminUserPageQuery;
import edu.fjut.mall.user.dto.AdminUserStatusRequest;
import edu.fjut.mall.user.dto.UserVO;
import edu.fjut.mall.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping("/page")
    public Result<PageResult<UserVO>> page(@ModelAttribute AdminUserPageQuery query,
                                           @RequestHeader("X-User-Role") Integer role) {
        requireAdmin(role);
        return Result.success(userService.pageForAdmin(query));
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @Valid @RequestBody AdminUserStatusRequest request,
                                     @RequestHeader("X-User-Id") Long operatorId,
                                     @RequestHeader("X-User-Role") Integer role) {
        requireAdmin(role);
        userService.updateStatusForAdmin(id, request.getStatus(), operatorId);
        return Result.success("用户状态已更新", null);
    }

    private void requireAdmin(Integer role) {
        if (role == null || role != 2) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "仅管理员可以操作用户管理功能");
        }
    }
}
