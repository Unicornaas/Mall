package edu.fjut.mall.user.service;

import edu.fjut.mall.user.dto.AddressRequest;
import edu.fjut.mall.user.entity.UserAddress;

import java.util.List;

/**
 * 收货地址服务接口
 */
public interface UserAddressService {

    /**
     * 查询用户所有地址
     */
    List<UserAddress> list(Long userId);

    /**
     * 根据ID查询地址
     */
    UserAddress getById(Long id, Long userId);

    /**
     * 新增地址
     */
    void add(Long userId, AddressRequest request);

    /**
     * 修改地址
     */
    void update(Long id, Long userId, AddressRequest request);

    /**
     * 删除地址
     */
    void delete(Long id, Long userId);
}
