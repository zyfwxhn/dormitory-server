package com.dormitory.dormitoryserver.service.impl;

import com.dormitory.dormitoryserver.context.BaseContext;
import com.dormitory.dormitoryserver.dto.WorkerExcelDTO;
import com.dormitory.dormitoryserver.dto.WorkerLoginDTO;
import com.dormitory.dormitoryserver.dto.WorkerSaveDTO;
import com.dormitory.dormitoryserver.entity.Worker;
import com.dormitory.dormitoryserver.exception.BaseException;
import com.dormitory.dormitoryserver.mapper.WorkerMapper;
import com.dormitory.dormitoryserver.result.PageResult;
import com.dormitory.dormitoryserver.service.WorkerService;
import com.dormitory.dormitoryserver.vo.WorkerInfoVO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dormitory.dormitoryserver.utils.PasswordUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkerServiceImpl implements WorkerService {

    @Autowired
    private WorkerMapper workerMapper;

    @Override
    public Worker login(WorkerLoginDTO workerLoginDTO) {
        String username = workerLoginDTO.getUsername();
        String password = workerLoginDTO.getPassword();

        // 1. 调用 Mapper，仅根据账号查出该人
        Worker worker = workerMapper.getByUsername(username);

        // 2. 校验账号是否存在
        if (worker == null) {
            throw new BaseException("维修员账号不存在");
        }

        // 3. 【业务亮点】校验账号是否被禁用 (isAvailable == 0)
        if (worker.getIsAvailable() != null && worker.getIsAvailable() == 0) {
            throw new BaseException("该账号已被停用，请联系管理员");
        }

        if (!PasswordUtil.matches(password, worker.getPassword())) {
            throw new BaseException("密码错误");
        }

        // 6. 校验全数通过，返回完整的实体对象（供 Controller 签发 JWT）
        return worker;
    }

    @Override
    public WorkerInfoVO getWorkerInfo() {
        Long currentId = BaseContext.getCurrentId();
        Worker worker = workerMapper.getById(currentId);
        return WorkerInfoVO.builder()
                .id(worker.getId())
                .username(worker.getUsername())
                .name(worker.getName())
                .phone(worker.getPhone())
                .skills(worker.getSkills())
                .isAvailable(worker.getIsAvailable())
                .build();
    }

    @Override
    public PageResult pageQuery(WorkerSaveDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<Worker> page = (Page<Worker>) workerMapper.pageQuery(dto);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void save(WorkerSaveDTO dto) {
        // 校验账号是否已存在
        Worker exist = workerMapper.getByUsername(dto.getUsername());
        if (exist != null) {
            throw new BaseException("该工号已存在");
        }
        Worker worker = new Worker();
        BeanUtils.copyProperties(dto, worker);
        worker.setPassword(PasswordUtil.encode(dto.getPassword()));
        worker.setIsAvailable(1);
        worker.setCreateTime(LocalDateTime.now());
        worker.setUpdateTime(LocalDateTime.now());
        workerMapper.insert(worker);
    }

    @Override
    public void update(WorkerSaveDTO dto) {
        Worker worker = new Worker();
        BeanUtils.copyProperties(dto, worker);
        // 如果传了密码则加密
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            worker.setPassword(PasswordUtil.encode(dto.getPassword()));
        }
        worker.setUpdateTime(LocalDateTime.now());
        workerMapper.update(worker);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Worker worker = Worker.builder()
                .id(id)
                .isAvailable(status)
                .updateTime(LocalDateTime.now())
                .build();
        workerMapper.update(worker);
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        Long currentId = BaseContext.getCurrentId();
        Worker worker = workerMapper.getById(currentId);

        if (!PasswordUtil.matches(oldPassword, worker.getPassword())) {
            throw new BaseException("旧密码不正确");
        }

        Worker update = Worker.builder()
                .id(currentId)
                .password(PasswordUtil.encode(newPassword))
                .updateTime(LocalDateTime.now())
                .build();
        workerMapper.update(update);
    }

    @Override
    public void updateProfile(com.dormitory.dormitoryserver.dto.WorkerProfileDTO dto) {
        Long currentId = BaseContext.getCurrentId();
        Worker update = Worker.builder()
                .id(currentId)
                .name(dto.getName())
                .phone(dto.getPhone())
                .updateTime(LocalDateTime.now())
                .build();
        workerMapper.update(update);
    }

    @Override
    public void updateSkills(String skills) {
        Long currentId = BaseContext.getCurrentId();
        Worker worker = Worker.builder()
                .id(currentId)
                .skills(skills)
                .updateTime(LocalDateTime.now())
                .build();
        workerMapper.update(worker);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importWorkerList(List<WorkerExcelDTO> list) {
        if (list == null || list.isEmpty()) return;
        List<Worker> workers = list.stream().map(dto -> {
            Worker w = new Worker();
            w.setUsername(dto.getUsername());
            w.setPassword(PasswordUtil.encode(dto.getPassword()));
            w.setName(dto.getName());
            w.setPhone(dto.getPhone());
            w.setSkills(dto.getSkills());
            w.setIsAvailable(1);
            w.setCreateTime(LocalDateTime.now());
            w.setUpdateTime(LocalDateTime.now());
            return w;
        }).collect(Collectors.toList());
        workerMapper.insertBatch(workers);
        log.info("批量导入 {} 条维修员成功", workers.size());
    }
}