package com.dormitory.dormitoryserver.service.impl;

import com.dormitory.dormitoryserver.context.BaseContext;
import com.dormitory.dormitoryserver.dto.PasswordChangeDTO;
import com.dormitory.dormitoryserver.dto.StudentExcelDTO;
import com.dormitory.dormitoryserver.dto.StudentLoginDTO;
import com.dormitory.dormitoryserver.dto.StudentProfileDTO;
import com.dormitory.dormitoryserver.entity.Student;
import com.dormitory.dormitoryserver.exception.BaseException;
import com.dormitory.dormitoryserver.mapper.StudentMapper;
import com.dormitory.dormitoryserver.service.StudentService;
import com.dormitory.dormitoryserver.vo.StudentInfoVO;
import com.dormitory.dormitoryserver.utils.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        Student student = studentMapper.getByStudentNo(studentNo);
        if (student == null) {
            throw new BaseException("账号不存在");
        }
        if (!PasswordUtil.matches(password, student.getPassword())) {
            throw new BaseException("密码错误");
        }
        return student;
    }

    @Override
    public StudentInfoVO getStudentInfo() {
        Long currentId = BaseContext.getCurrentId();
        Student student = studentMapper.getById(currentId);
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
        String defaultPassword = PasswordUtil.encode("123456");
        LocalDateTime now = LocalDateTime.now();
        List<Student> students = dtoList.stream().map(dto -> {
            Student student = new Student();
            BeanUtils.copyProperties(dto, student);
            student.setPassword(defaultPassword);
            student.setCreateTime(now);
            student.setUpdateTime(now);
            return student;
        }).collect(Collectors.toList());
        studentMapper.insertBatch(students);
        log.info("批量导入 {} 条学生数据", students.size());
    }
}
