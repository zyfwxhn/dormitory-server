package com.dormitory.dormitoryserver.service;

import com.dormitory.dormitoryserver.dto.WorkerExcelDTO;
import com.dormitory.dormitoryserver.dto.WorkerLoginDTO;
import com.dormitory.dormitoryserver.dto.WorkerSaveDTO;
import com.dormitory.dormitoryserver.entity.Worker;
import com.dormitory.dormitoryserver.result.PageResult;

import com.dormitory.dormitoryserver.vo.WorkerInfoVO;
import java.util.List;

public interface WorkerService {
    Worker login(WorkerLoginDTO workerLoginDTO);

    WorkerInfoVO getWorkerInfo();

    PageResult pageQuery(WorkerSaveDTO dto);

    void save(WorkerSaveDTO dto);

    void update(WorkerSaveDTO dto);

    void startOrStop(Integer status, Long id);

    void changePassword(String oldPassword, String newPassword);

    void updateSkills(String skills);

    void updateProfile(com.dormitory.dormitoryserver.dto.WorkerProfileDTO dto);

    /**
     * 批量导入维修员 (Excel)
     */
    void importWorkerList(List<WorkerExcelDTO> list);
}