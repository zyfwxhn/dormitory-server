package com.dormitory.dormitoryserver.service.impl;

import com.dormitory.dormitoryserver.context.BaseContext;
import com.dormitory.dormitoryserver.dto.StudentExcelDTO;
import com.dormitory.dormitoryserver.dto.StudentLoginDTO;
import com.dormitory.dormitoryserver.dto.StudentProfileDTO;
import com.dormitory.dormitoryserver.entity.Student;
import com.dormitory.dormitoryserver.exception.BaseException;
import com.dormitory.dormitoryserver.mapper.StudentMapper;
import com.dormitory.dormitoryserver.service.StudentService;
import com.dormitory.dormitoryserver.vo.StudentInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dormitory.dormitoryserver.utils.PasswordUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StudentServiceImpl implements StudentService {

    @Autowired
    private StudentMapper studentMapper;

    @Override
    public Student login(StudentLoginDTO studentLoginDTO) {
        String studentNo = studentLoginDTO.getStudentNo();
        String password = studentLoginDTO.getPassword();

        // 1. 查库
        Student student = studentMapper.getByStudentNo(studentNo);

        // 2. 校验账号
        if (student == null) {
            // 替换成 BaseException
            throw new BaseException("账号不存在");
        }

        // 密码比对
        if (!PasswordUtil.matches(password, student.getPassword())) {
            throw new BaseException("密码错误");
        }

        // 3. 直接返回学生对象，生成令牌的事交给 Controller
        return student;
    }

    @Override
    public StudentInfoVO getStudentInfo() {
        // 1. 神奇的魔法：直接从 ThreadLocal 上下文中“白嫖”当前登录人的 ID
        Long currentId = BaseContext.getCurrentId();

        // 2. 去数据库查询详细信息
        Student student = studentMapper.getById(currentId);

        // 3. 数据脱敏：把实体类转成安全的 VO 给前端（密码被完美隐藏了）
        return StudentInfoVO.builder()
                .id(student.getId())
                .studentNo(student.getStudentNo())
                .name(student.getName())
                .gender(student.getGender())
                .grade(student.getGrade())
                .phone(student.getPhone())
                .buildingNo(student.getBuildingNo())
                .roomNo(student.getRoomNo())
                .avatar(student.getAvatar())
                .build();
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        Long currentId = BaseContext.getCurrentId();
        Student student = studentMapper.getById(currentId);

        if (!PasswordUtil.matches(oldPassword, student.getPassword())) {
            throw new BaseException("旧密码不正确");
        }

        Student update = new Student();
        update.setId(currentId);
        update.setPassword(PasswordUtil.encode(newPassword));
        update.setUpdateTime(LocalDateTime.now());
        studentMapper.update(update);
    }

    @Override
    public void updateAvatar(String avatarUrl) {
        Long currentId = BaseContext.getCurrentId();
        Student update = new Student();
        update.setId(currentId);
        update.setAvatar(avatarUrl);
        update.setUpdateTime(LocalDateTime.now());
        studentMapper.update(update);
        log.info("学生 {} 更新头像", currentId);
    }

    @Override
    public void updateProfile(StudentProfileDTO dto) {
        Long currentId = BaseContext.getCurrentId();
        Student update = new Student();
        update.setId(currentId);
        update.setName(dto.getName());
        update.setGender(dto.getGender());
        update.setGrade(dto.getGrade());
        update.setPhone(dto.getPhone());
        update.setUpdateTime(LocalDateTime.now());
        studentMapper.update(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importStudentList(List<StudentExcelDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return;
        }

        // 1. 设置系统统一的默认密码：123456，并进行 MD5 加密
        String defaultPassword = PasswordUtil.encode("123456");
        LocalDateTime now = LocalDateTime.now();

        // 2. 将 DTO 列表转换为 Entity 列表，并补全默认属性
        List<Student> students = dtoList.stream().map(dto -> {
            Student student = new Student();
            BeanUtils.copyProperties(dto, student);

            // 设置加密后的密码和系统时间
            student.setPassword(defaultPassword);
            student.setCreateTime(now);
            student.setUpdateTime(now);
            return student;
        }).collect(Collectors.toList());

        // 3. 调用 Mapper 层执行【真·批量插入】
        studentMapper.insertBatch(students);

        log.info("成功批量插入 {} 条学生数据到数据库", students.size());
    }
}