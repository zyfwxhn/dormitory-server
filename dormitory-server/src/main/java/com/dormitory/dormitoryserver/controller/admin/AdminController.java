package com.dormitory.dormitoryserver.controller.admin;

import com.dormitory.dormitoryserver.dto.AdminLoginDTO;
import com.dormitory.dormitoryserver.dto.PasswordChangeDTO;
import com.dormitory.dormitoryserver.entity.Admin;
import com.dormitory.dormitoryserver.properties.JwtProperties;
import com.dormitory.dormitoryserver.result.Result;
import com.dormitory.dormitoryserver.service.AdminService;
import com.dormitory.dormitoryserver.utils.JwtUtil;
import com.dormitory.dormitoryserver.vo.AdminInfoVO;
import com.dormitory.dormitoryserver.vo.AdminLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@Slf4j
public class AdminController {

    @Autowired
    private AdminService adminService;
    @Autowired
    private JwtProperties jwtProperties;

    @PostMapping("/login")
    public Result<AdminLoginVO> login(@RequestBody @Validated AdminLoginDTO adminLoginDTO) {
        log.info("管理员登录: {}", adminLoginDTO.getUsername());

        Admin admin = adminService.login(adminLoginDTO);
        Map<String, Object> claims = new HashMap<>();
        claims.put("adminId", admin.getId());

        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        AdminLoginVO vo = AdminLoginVO.builder()
                .id(admin.getId())
                .name(admin.getName())
                .token(token)
                .build();

        return Result.success(vo);
    }

    /**
     * 获取当前登录管理员信息
     */
    @GetMapping("/info")
    public Result<AdminInfoVO> getInfo() {
        log.info("获取当前登录管理员信息");
        AdminInfoVO adminInfoVO = adminService.getAdminInfo();
        return Result.success(adminInfoVO);
    }

    /**
     * 管理员修改密码
     */
    @PutMapping("/password")
    public Result changePassword(@RequestBody @Validated PasswordChangeDTO dto) {
        log.info("管理员修改密码");
        adminService.changePassword(dto.getOldPassword(), dto.getNewPassword());
        return Result.success();
    }
}