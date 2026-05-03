package com.dormitory.dormitoryserver.mapper;

import com.dormitory.dormitoryserver.entity.Student;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 学生表数据访问层接口
 */
@Mapper
public interface StudentMapper {

    /**
     * 根据学号查询学生
     * 用于：1. 登录时校验账号密码；2. 注册时检查学号是否被占用
     *
     * @param studentNo 学号
     * @return 学生实体对象
     */
    Student getByStudentNo(String studentNo);

    /**
     * 插入新的学生记录
     * 用于：学生注册
     *
     * @param student 学生实体对象
     */
    void insert(Student student);

    /**
     * 根据主键 ID 查询学生
     */
    Student getById(Long id);

    /**
     * 批量插入学生数据
     */
    void insertBatch(@org.apache.ibatis.annotations.Param("students") java.util.List<com.dormitory.dormitoryserver.entity.Student> students);

    /**
     * 动态更新学生信息（密码等）
     */
    void update(Student student);
}