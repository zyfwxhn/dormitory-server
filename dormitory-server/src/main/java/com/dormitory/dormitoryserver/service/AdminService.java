package com.dormitory.dormitoryserver.service;

import com.dormitory.dormitoryserver.dto.AdminLoginDTO;
import com.dormitory.dormitoryserver.entity.Admin;

import com.dormitory.dormitoryserver.vo.AdminInfoVO;

public interface AdminService {
    Admin login(AdminLoginDTO adminLoginDTO);

    /**
     * 获取当前管理员信息
     * @return
     */
    AdminInfoVO getAdminInfo();

    /**
     * 管理员修改密码
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void changePassword(String oldPassword, String newPassword);
}