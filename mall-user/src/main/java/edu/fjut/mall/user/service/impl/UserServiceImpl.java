package edu.fjut.mall.user.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import edu.fjut.mall.common.result.ResultCode;
import edu.fjut.mall.common.exception.BusinessException;
import edu.fjut.mall.common.util.JwtUtil;
import edu.fjut.mall.common.util.SnowflakeIdGenerator;
import edu.fjut.mall.user.dto.*;
import edu.fjut.mall.user.entity.User;
import edu.fjut.mall.user.mapper.UserMapper;
import edu.fjut.mall.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        // 1. 检查用户名是否已存在
        User existUser = userMapper.selectByUsername(request.getUsername());
        if (existUser != null) {
            throw new BusinessException(ResultCode.USERNAME_EXIST);
        }

        // 2. 构建用户实体
        User user = new User();
        user.setId(snowflakeIdGenerator.nextId());
        user.setUsername(request.getUsername());
        user.setPassword(BCrypt.hashpw(request.getPassword()));
        user.setNickname(request.getUsername());        // 默认昵称 = 用户名
        user.setPhone(request.getPhone());
        user.setRole(0);                                // 默认买家
        user.setStatus(1);                               // 启用
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        // 3. 入库
        userMapper.insert(user);
        log.info("新用户注册成功: username={}, id={}", request.getUsername(), user.getId());
    }

    @Override
    public LoginVO login(LoginRequest request) {
        // 1. 查用户
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 2. 验密码
        if (!BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        // 3. 检查状态
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "账号已被禁用");
        }

        // 4. 生成 Token
        String token = JwtUtil.generateToken(user.getId(), user.getRole());

        // 5. 构建返回
        return LoginVO.builder()
                .token(token)
                .user(toUserVO(user))
                .build();

    }

    @Override
    public UserVO getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return toUserVO(user);
    }

    @Override
    @Transactional
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        user.setNickname(request.getNickname());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setAvatar(request.getAvatar());
        user.setUpdateTime(LocalDateTime.now());

        userMapper.updateById(user);
        log.info("用户信息更新成功: userId={}", userId);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 验旧密码
        if (!BCrypt.checkpw(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR.getCode(), "原密码错误");
        }

        // 更新新密码
        user.setPassword(BCrypt.hashpw(request.getNewPassword()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        log.info("密码修改成功: userId={}", userId);
    }

    // ==================== 工具方法 ====================

    /**
     * Entity → VO（脱敏）
     */
    private UserVO toUserVO(User user) {
        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .phone(user.getPhone())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .status(user.getStatus())
                .build();
    }
}
