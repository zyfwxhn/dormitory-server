package com.dormitory.dormitoryserver.mapper;

import com.dormitory.dormitoryserver.dto.SecondhandItemPageQueryDTO;
import com.dormitory.dormitoryserver.entity.SecondhandItem;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SecondhandItemMapper {

    /**
     * 插入一条二手商品记录
     * @param secondhandItem 实体对象
     */
    @Insert("INSERT INTO secondhand_item (student_id, name, description, category, price, condition_level, images, status, create_time, update_time) " +
            "VALUES (#{studentId}, #{name}, #{description}, #{category}, #{price}, #{conditionLevel}, #{images}, #{status}, #{createTime}, #{updateTime})")
    void insert(SecondhandItem secondhandItem);

    /**
     * 分页多条件动态查询
     * @param dto 查询条件
     * @return Page对象
     */
    Page<SecondhandItem> pageQuery(SecondhandItemPageQueryDTO dto);

    /**
     * 根据主键查询单条商品信息
     */
    @Select("SELECT si.*, s.name AS studentName, s.student_no AS studentNo, s.avatar AS studentAvatar FROM secondhand_item si LEFT JOIN student s ON si.student_id = s.id WHERE si.id = #{id}")
    SecondhandItem getById(Long id);

    /**
     * 动态更新二手商品信息
     */
    void update(SecondhandItem secondhandItem);
}