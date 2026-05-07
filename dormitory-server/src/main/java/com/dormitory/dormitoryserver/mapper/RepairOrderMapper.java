package com.dormitory.dormitoryserver.mapper;

import com.dormitory.dormitoryserver.dto.RepairOrderAdminPageQueryDTO;
import com.dormitory.dormitoryserver.dto.RepairOrderPageQueryDTO;
import com.dormitory.dormitoryserver.entity.RepairOrder;
import com.dormitory.dormitoryserver.vo.StatusCountVO;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RepairOrderMapper {

    /**
     * 插入报修订单数据
     */
    void insert(RepairOrder repairOrder);

    /**
     * 分页查询历史报修记录 (带动态条件和数据隔离)
     * * @param queryDTO 分页和状态查询条件
     * @param studentId 当前登录学生ID
     * @return Page<RepairOrder>
     */
    Page<RepairOrder> pageQuery(@Param("queryDTO") RepairOrderPageQueryDTO queryDTO, @Param("studentId") Long studentId);

    /**
     * 根据主键 ID 和 学生 ID 查询报修单详情
     * @param id 订单ID
     * @param studentId 当前登录学生ID
     * @return RepairOrder
     */

    RepairOrder getByIdAndStudentId(@Param("id") Long id, @Param("studentId") Long studentId);

    /**
     * 管理端: 多条件动态分页查询全校报修单
     * @param queryDTO 管理端查询条件
     * @return Page<RepairOrder>
     */
    Page<RepairOrder> adminPageQuery(RepairOrderAdminPageQueryDTO queryDTO);

    /**
     * 管理端: 根据主键查询报修单 (无需过滤 student_id)
     */
    @Select("SELECT r.*, s.name AS studentName, s.student_no AS studentNo, s.phone AS studentPhone FROM repair_order r LEFT JOIN student s ON r.student_id = s.id WHERE r.id = #{id}")
    RepairOrder getById(Long id);

    /**
     * 通用动态更新报修单
     */
    void update(RepairOrder repairOrder);

    /**
     * 统计今日新增的报修单数量
     */
    @org.apache.ibatis.annotations.Select("SELECT COUNT(id) FROM repair_order WHERE DATE(create_time) = CURDATE()")
    Integer getTodayNewRepairCount();

    /**
     * 统计今日已完成的报修单数量
     */
    @org.apache.ibatis.annotations.Select("SELECT COUNT(id) FROM repair_order WHERE DATE(update_time) = CURDATE() AND status = 3")
    Integer getTodayFinishedRepairCount();

    /**
     * 统计各状态的报修单数量分布
     */
    List<StatusCountVO> getStatusCount();

    /**
     * 维修员专属动态分页查询
     * @param dto 查询条件
     * @param workerId 当前维修员ID
     * @return
     */
    Page<RepairOrder> workerPageQuery(@org.apache.ibatis.annotations.Param("dto") RepairOrderPageQueryDTO dto,
                                      @org.apache.ibatis.annotations.Param("workerId") Long workerId);
}