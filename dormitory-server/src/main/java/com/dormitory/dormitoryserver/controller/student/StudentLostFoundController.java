package com.dormitory.dormitoryserver.controller.student;

import com.dormitory.dormitoryserver.dto.LostFoundPageQueryDTO;
import com.dormitory.dormitoryserver.dto.LostFoundSubmitDTO;
import com.dormitory.dormitoryserver.dto.LostFoundUpdateStatusDTO;
import com.dormitory.dormitoryserver.entity.LostFound;
import com.dormitory.dormitoryserver.result.PageResult;
import com.dormitory.dormitoryserver.result.Result;
import com.dormitory.dormitoryserver.service.LostFoundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

/**
 * 学生端 - 失物招领接口
 */
@RestController
@RequestMapping("/student/lost-found")
@Validated
@Slf4j
public class StudentLostFoundController {
     @Autowired
     private LostFoundService lostFoundService;

    /**
     * 发布寻物启事 / 失物招领
     * @param dto 前端提交的表单数据
     * @return 统一返回结果
     */
    @PostMapping("/publish")
    public Result<String> publish(@RequestBody @Validated LostFoundSubmitDTO dto) {
        //  Service 层进行保存
        lostFoundService.publish(dto);
        return Result.success("发布成功");
    }

    /**
     * 浏览失物招领大厅 (分页条件查询)
     * 注意: GET 请求不要加 @RequestBody
     * @param lostFoundPageQueryDTO 查询条件
     * @return 包含列表和总数的分页结果
     */
    @GetMapping("/page")
    public Result<PageResult> pageQuery(LostFoundPageQueryDTO lostFoundPageQueryDTO) {
        
        PageResult pageResult = lostFoundService.pageQuery(lostFoundPageQueryDTO);
        return Result.success(pageResult);

    }

    /**
     * 修改发布信息状态
     */
    @PutMapping("/status")
    public Result<String> updateStatus(@RequestBody @Validated LostFoundUpdateStatusDTO dto) {
        lostFoundService.updateStatus(dto);
        return Result.success("状态修改成功");
    }

    /**
     * 根据ID查询详情
     * @param id URL路径中的ID
     * @return 统一返回结果
     */
    @GetMapping("/{id}")
    public Result<LostFound> getById(@PathVariable Long id) {
        LostFound lostFound = lostFoundService.getById(id);
        return Result.success(lostFound);
    }

    /**
     * 学生发起认领: 通过平台通知发布者, 不暴露手机号
     */
    @PostMapping("/claim/{id}")
    public Result claim(@PathVariable Long id) {
        lostFoundService.claim(id);
        return Result.success("认领请求已发送, 请等待发布者联系你");
    }

    /**
     * 学生编辑自己的发布信息
     */
    @PutMapping("/{id}")
    public Result edit(@PathVariable Long id, @RequestBody LostFoundSubmitDTO dto) {
        log.info("学生编辑失物招领信息: id={}, {}", id, dto.getTitle());
        lostFoundService.edit(dto, id);
        return Result.success("修改成功");
    }

}