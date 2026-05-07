package com.dormitory.dormitoryserver.service.impl;

import com.dormitory.dormitoryserver.context.BaseContext;
import com.dormitory.dormitoryserver.dto.AdminLoginDTO;
import com.dormitory.dormitoryserver.entity.Admin;
import com.dormitory.dormitoryserver.exception.BaseException;
import com.dormitory.dormitoryserver.mapper.AdminMapper;
import com.dormitory.dormitoryserver.service.AdminService;
import com.dormitory.dormitoryserver.vo.AdminInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dormitory.dormitoryserver.utils.PasswordUtil;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Override
    public Admin login(AdminLoginDTO adminLoginDTO) {
        String username = adminLoginDTO.getUsername();
        String password = adminLoginDTO.getPassword();
        Admin admin = adminMapper.getByUsername(username);
        if (admin == null) {
            throw new BaseException("管理员账号不存在");
        }
        if (!PasswordUtil.matches(password, admin.getPassword())) {
            throw new BaseException("密码错误");
        }

        return admin;
    }

    @Override
    public AdminInfoVO getAdminInfo() {
        Long currentId = BaseContext.getCurrentId();
        Admin admin = adminMapper.getById(currentId);
        return AdminInfoVO.builder()
                .id(admin.getId())
                .username(admin.getUsername())
                .name(admin.getName())
                .build();
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        Long currentId = BaseContext.getCurrentId();
        Admin admin = adminMapper.getById(currentId);

        if (!PasswordUtil.matches(oldPassword, admin.getPassword())) {
            throw new BaseException("原密码错误");
        }

        adminMapper.updatePassword(currentId, PasswordUtil.encode(newPassword));
    }
}