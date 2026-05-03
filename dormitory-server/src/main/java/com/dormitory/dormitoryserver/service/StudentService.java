package com.dormitory.dormitoryserver.service;

import com.dormitory.dormitoryserver.dto.StudentExcelDTO;
import com.dormitory.dormitoryserver.dto.StudentLoginDTO;
import com.dormitory.dormitoryserver.entity.Student;
import com.dormitory.dormitoryserver.vo.StudentInfoVO;

import java.util.List;

public interface StudentService {
    Student login(StudentLoginDTO studentLoginDTO);
    StudentInfoVO getStudentInfo();
    void importStudentList(List<StudentExcelDTO> studentList);

    void changePassword(String oldPassword, String newPassword);

    /**
     * 学生修改个人信息
     */
    void updateProfile(com.dormitory.dormitoryserver.dto.StudentProfileDTO dto);
}