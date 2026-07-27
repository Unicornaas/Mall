package edu.fjut.mall.user.service.impl;

import edu.fjut.mall.common.exception.BusinessException;
import edu.fjut.mall.common.result.ResultCode;
import edu.fjut.mall.common.util.SnowflakeIdGenerator;
import edu.fjut.mall.user.dto.AddressRequest;
import edu.fjut.mall.user.entity.UserAddress;
import edu.fjut.mall.user.mapper.UserAddressMapper;
import edu.fjut.mall.user.service.UserAddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 收货地址服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAddressServiceImpl implements UserAddressService {

    private final UserAddressMapper userAddressMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    public List<UserAddress> list(Long userId) {
        return userAddressMapper.selectByUserId(userId);
    }

    @Override
    public UserAddress getById(Long id, Long userId) {
        UserAddress address = userAddressMapper.selectById(id);
        if (address == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "地址不存在");
        }
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权访问该地址");
        }
        return address;
    }

    @Override
    @Transactional
    public void add(Long userId, AddressRequest request) {
        // 1. 如果设为默认，先取消其他默认
        if (request.getIsDefault() != null && request.getIsDefault() == 1) {
            userAddressMapper.clearDefault(userId);
        }

        // 2. 构建实体
        UserAddress address = buildEntity(userId, request);
        address.setId(snowflakeIdGenerator.nextId());
        address.setCreateTime(LocalDateTime.now());
        address.setUpdateTime(LocalDateTime.now());

        // 3. 入库
        userAddressMapper.insert(address);
        log.info("收货地址新增成功: userId={}, addressId={}", userId, address.getId());
    }

    @Override
    @Transactional
    public void update(Long id, Long userId, AddressRequest request) {
        // 1. 校验地址存在且属于该用户
        UserAddress address = getById(id, userId);

        // 2. 如果设为默认，先取消其他默认
        if (request.getIsDefault() != null && request.getIsDefault() == 1) {
            userAddressMapper.clearDefault(userId);
        }

        // 3. 更新字段
        address.setReceiverName(request.getReceiverName());
        address.setReceiverPhone(request.getReceiverPhone());
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setDetail(request.getDetail());
        address.setIsDefault(request.getIsDefault());
        address.setUpdateTime(LocalDateTime.now());

        userAddressMapper.updateById(address);
        log.info("收货地址更新成功: addressId={}", id);
    }

    @Override
    public void delete(Long id, Long userId) {
        // 校验地址存在且属于该用户
        getById(id, userId);
        userAddressMapper.deleteById(id);
        log.info("收货地址删除成功: addressId={}", id);
    }

    // ==================== 工具方法 ====================

    /**
     * 将请求 DTO 转为实体（不含 ID 和时间）
     */
    private UserAddress buildEntity(Long userId, AddressRequest request) {
        UserAddress address = new UserAddress();
        address.setUserId(userId);
        address.setReceiverName(request.getReceiverName());
        address.setReceiverPhone(request.getReceiverPhone());
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setDetail(request.getDetail());
        address.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : 0);
        return address;
    }
}
