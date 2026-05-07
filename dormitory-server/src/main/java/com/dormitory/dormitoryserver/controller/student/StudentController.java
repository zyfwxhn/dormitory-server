package com.dormitory.dormitoryserver.controller.student;

import com.dormitory.dormitoryserver.dto.PasswordChangeDTO;
import com.dormitory.dormitoryserver.dto.StudentLoginDTO;
import com.dormitory.dormitoryserver.dto.StudentProfileDTO;
import com.dormitory.dormitoryserver.entity.Student;
import com.dormitory.dormitoryserver.properties.JwtProperties;
import com.dormitory.dormitoryserver.result.Result;
import com.dormitory.dormitoryserver.service.StudentService;
import com.dormitory.dormitoryserver.utils.JwtUtil;
import com.dormitory.dormitoryserver.vo.StudentInfoVO;
import com.dormitory.dormitoryserver.vo.StudentLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Map;

@RestController
@RequestMapping("/student")
@Slf4j
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private JwtProperties jwtProperties;

    @PostMapping("/login")
    public Result<StudentLoginVO> login(@RequestBody @Validated StudentLoginDTO studentLoginDTO) {
        log.info("学生登录: {}", studentLoginDTO.getStudentNo());

        Student student = studentService.login(studentLoginDTO);

        Map<String, Object> claims = new HashMap<>();
        claims.put("studentId", student.getId());

        String token = JwtUtil.createJWT(
                jwtProperties.getStudentSecretKey(),
                jwtProperties.getStudentTtl(),
                claims);

        StudentLoginVO studentLoginVO = StudentLoginVO.builder()
                .id(student.getId())
                .name(student.getName())
                .token(token)
                .build();

        return Result.success(studentLoginVO);
    }

    @GetMapping("/info")
    public Result<StudentInfoVO> getInfo() {
        log.info("获取当前登录学生信息");
        StudentInfoVO studentInfoVO = studentService.getStudentInfo();
        return Result.success(studentInfoVO);
    }

    /**
     * 学生修改密码
     */
    @PutMapping("/password")
    public Result changePassword(@RequestBody @Validated PasswordChangeDTO dto) {
        log.info("学生修改密码");
        studentService.changePassword(dto.getOldPassword(), dto.getNewPassword());
        return Result.success();
    }

    /**
     * 学生修改个人信息
     */
    @PutMapping("/profile")
    public Result updateProfile(@RequestBody StudentProfileDTO dto) {
        log.info("学生修改个人信息: {}", dto);
        studentService.updateProfile(dto);
        return Result.success();
    }

    /**
     * 学生修改头像
     */
    @PutMapping("/avatar")
    public Result updateAvatar(@RequestBody Map<String, String> body) {
        String avatarUrl = body.get("avatar");
        log.info("学生修改头像: {}", avatarUrl);
        studentService.updateAvatar(avatarUrl);
        return Result.success();
    }
}