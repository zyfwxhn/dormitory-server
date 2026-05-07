package com.dormitory.dormitoryserver.mapper;

import com.dormitory.dormitoryserver.entity.Worker;
import com.dormitory.dormitoryserver.vo.WorkerRankVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 维修员 Mapper 接口
 */
@Mapper
public interface WorkerMapper {

    /**
     * 根据账号查询维修员
     * @param username 维修员账号
     * @return 维修员实体对象
     */
    @Select("SELECT * FROM worker WHERE username = #{username}")
    Worker getByUsername(String username);

    /**
     * 根据ID查询维修员
     * @param id 维修员ID
     * @return 维修员实体对象
     */
    @Select("SELECT * FROM worker WHERE id = #{id}")
    Worker getById(Long id);

    /**
     * 智能调度: 获取当前未完成订单最少 (最空闲)的可用维修员 ID
     * @return 维修员 ID
     */
    Long getIdleWorkerId();

    /**
     * 获取维修员历史完单量排行榜 Top5
     */
    List<WorkerRankVO> getWorkerRanking();

    /**
     * 智能匹配算法: 寻找擅长该领域且当前接单量最少的空闲师傅
     * @param repairType 故障类型 (如: 水管、电器)
     * @return 匹配的师傅ID
     */
    Long getSmartDispatchWorkerId(@org.apache.ibatis.annotations.Param("repairType") String repairType);

    /**
     * 分页查询维修员列表 (管理员端)
     */
    java.util.List<com.dormitory.dormitoryserver.entity.Worker> pageQuery(com.dormitory.dormitoryserver.dto.WorkerSaveDTO dto);

    /**
     * 新增维修员
     */
    void insert(com.dormitory.dormitoryserver.entity.Worker worker);

    /**
     * 更新维修员信息
     */
    void update(com.dormitory.dormitoryserver.entity.Worker worker);

    /**
     * 批量新增维修员 (Excel 导入)
     */
    void insertBatch(List<com.dormitory.dormitoryserver.entity.Worker> list);
}