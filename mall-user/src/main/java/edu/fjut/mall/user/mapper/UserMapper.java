package edu.fjut.mall.user.mapper;

import edu.fjut.mall.user.entity.User;
import edu.fjut.mall.user.dto.AdminUserPageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户 Mapper
 */
@Mapper
public interface UserMapper {

    /** 根据用户名查询 */
    User selectByUsername(@Param("username") String username);

    /** 根据ID查询 */
    User selectById(@Param("id") Long id);

    /** 插入用户 */
    int insert(User user);

    /** 更新用户 */
    int updateById(User user);

    List<User> selectPageForAdmin(AdminUserPageQuery query);

    long countForAdmin(AdminUserPageQuery query);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
