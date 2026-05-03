package com.dormitory.dormitoryserver.controller.worker;

import com.dormitory.dormitoryserver.dto.PasswordChangeDTO;
import com.dormitory.dormitoryserver.dto.WorkerLoginDTO;
import com.dormitory.dormitoryserver.dto.WorkerProfileDTO;
import com.dormitory.dormitoryserver.entity.Worker;
import com.dormitory.dormitoryserver.properties.JwtProperties;
import com.dormitory.dormitoryserver.result.Result;
import com.dormitory.dormitoryserver.service.WorkerService;
import com.dormitory.dormitoryserver.utils.JwtUtil;
import com.dormitory.dormitoryserver.vo.WorkerInfoVO;
import com.dormitory.dormitoryserver.vo.WorkerLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/worker")
@Slf4j
public class WorkerController {

    @Autowired
    private WorkerService workerService;
    @Autowired
    private JwtProperties jwtProperties;

    @PostMapping("/login")
    public Result<WorkerLoginVO> login(@RequestBody @Validated WorkerLoginDTO workerLoginDTO) {
        log.info("维修员登录：{}", workerLoginDTO.getUsername());

        Worker worker = workerService.login(workerLoginDTO);

        Map<String, Object> claims = new HashMap<>();
        claims.put("workerId", worker.getId());

        String token = JwtUtil.createJWT(
                jwtProperties.getWorkerSecretKey(),
                jwtProperties.getWorkerTtl(),
                claims);

        WorkerLoginVO vo = WorkerLoginVO.builder()
                .id(worker.getId())
                .name(worker.getName())
                .token(token)
                .build();

        return Result.success(vo);
    }

    @GetMapping("/info")
    public Result<WorkerInfoVO> getInfo() {
        log.info("获取当前登录维修员信息");
        WorkerInfoVO workerInfoVO = workerService.getWorkerInfo();
        return Result.success(workerInfoVO);
    }

    /**
     * 维修员修改密码
     */
    @PutMapping("/password")
    public Result changePassword(@RequestBody @Validated PasswordChangeDTO dto) {
        log.info("维修员修改密码");
        workerService.changePassword(dto.getOldPassword(), dto.getNewPassword());
        return Result.success();
    }

    /**
     * 维修员更新自己的技能
     */
    @PutMapping("/skills")
    public Result updateSkills(@RequestParam String skills) {
        log.info("维修员更新技能：{}", skills);
        workerService.updateSkills(skills);
        return Result.success();
    }

    /**
     * 维修员修改个人信息
     */
    @PutMapping("/profile")
    public Result updateProfile(@RequestBody WorkerProfileDTO dto) {
        log.info("维修员修改个人信息：{}", dto);
        workerService.updateProfile(dto);
        return Result.success();
    }
}