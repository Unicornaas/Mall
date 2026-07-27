package edu.fjut.mall.user.mapper;

import edu.fjut.mall.user.entity.UserAddress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 收货地址 Mapper
 */
@Mapper
public interface UserAddressMapper {

    /** 根据用户ID查询所有地址 */
    List<UserAddress> selectByUserId(@Param("userId") Long userId);

    /** 根据ID查询 */
    UserAddress selectById(@Param("id") Long id);

    /** 插入地址 */
    int insert(UserAddress userAddress);

    /** 更新地址 */
    int updateById(UserAddress userAddress);

    /** 删除地址 */
    int deleteById(@Param("id") Long id);

    /** 将该用户的其他地址取消默认 */
    int clearDefault(@Param("userId") Long userId);
}
