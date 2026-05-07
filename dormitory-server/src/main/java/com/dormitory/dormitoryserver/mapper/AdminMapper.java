package com.dormitory.dormitoryserver.mapper;

import com.dormitory.dormitoryserver.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 管理员 Mapper 接口
 */
@Mapper
public interface AdminMapper {

    /**
     * 根据账号查询管理员
     * @param username 管理员账号
     * @return 管理员实体对象
     */
    @Select("SELECT * FROM admin WHERE username = #{username}")
    Admin getByUsername(String username);

    /**
     * 根据ID查询管理员
     * @param id 管理员ID
     * @return 管理员实体对象
     */
    @Select("SELECT * FROM admin WHERE id = #{id}")
    Admin getById(Long id);

    /**
     * 修改管理员密码
     * @param id 管理员ID
     * @param newPassword 新密码
     */
    @Update("UPDATE admin SET password = #{newPassword} WHERE id = #{id}")
    void updatePassword(Long id, String newPassword);
}