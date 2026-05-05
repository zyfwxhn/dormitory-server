package com.dormitory.dormitoryserver.mapper;

import com.dormitory.dormitoryserver.dto.LostFoundPageQueryDTO;
import com.dormitory.dormitoryserver.entity.LostFound;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LostFoundMapper {

    /**
     * 插入一条失物招领/寻物启事记录
     * @param lostFound 实体对象
     */
    @Insert("INSERT INTO lost_found (student_id, type, title, description, category, location, contact_info, images, status, create_time, update_time) " +
            "VALUES (#{studentId}, #{type}, #{title}, #{description}, #{category}, #{location}, #{contactInfo}, #{images}, #{status}, #{createTime}, #{updateTime})")
    void insert(LostFound lostFound);

    /**
     * 分页条件查询
     * @param dto 查询条件
     * @return Page对象
     */
    Page<LostFound> pageQuery(LostFoundPageQueryDTO dto);

    /**
     * 根据主键查询单条信息
     */
    @Select("SELECT lf.*, s.name AS studentName, s.student_no AS studentNo, s.avatar AS studentAvatar FROM lost_found lf LEFT JOIN student s ON lf.student_id = s.id WHERE lf.id = #{id}")
    LostFound getById(Long id);

    /**
     * 动态更新失物招领信息
     */
    void update(LostFound lostFound);
}