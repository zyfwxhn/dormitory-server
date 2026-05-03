package com.dormitory.dormitoryserver.controller.student;

import com.dormitory.dormitoryserver.dto.SecondhandItemPageQueryDTO;
import com.dormitory.dormitoryserver.dto.SecondhandItemSubmitDTO;
import com.dormitory.dormitoryserver.dto.SecondhandItemUpdateStatusDTO;
import com.dormitory.dormitoryserver.entity.SecondhandItem;
import com.dormitory.dormitoryserver.result.PageResult;
import com.dormitory.dormitoryserver.result.Result;
import com.dormitory.dormitoryserver.service.SecondhandItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 学生端 - 二手交易商品接口
 */
@RestController
@RequestMapping("/student/secondhand-item")
@Validated
public class StudentSecondhandItemController {


     @Autowired
     private SecondhandItemService secondhandItemService;

    /**
     * 发布二手商品
     * @param dto 前端提交的表单数据
     * @return 统一返回结果
     */
    @PostMapping("/publish")
    public Result<String> publish(@RequestBody @Validated SecondhandItemSubmitDTO dto) {
        //调用 Service 层进行保存
        secondhandItemService.publish(dto);
        return Result.success("发布成功");
    }

    /**
     * 浏览二手市场大厅（分页与多条件动态查询）
     * @param dto 查询条件
     * @return 包含列表和总数的分页结果
     */
    @GetMapping("/page")
    public Result<PageResult> pageQuery(SecondhandItemPageQueryDTO dto) {
        //调用 Service 层进行分页查询
        PageResult pageResult = secondhandItemService.pageQuery(dto);
        return Result.success(pageResult);

    }

    /**
     * 修改二手商品状态（如：标记售出、下架）
     */
    @PutMapping("/status")
    public Result<String> updateStatus(@RequestBody @Validated SecondhandItemUpdateStatusDTO dto) {
        secondhandItemService.updateStatus(dto);
        return Result.success("商品状态更新成功");
    }

    /**
     * 根据ID查询商品详情
     * @param id URL路径中的商品ID
     * @return 统一返回结果（包含商品完整信息）
     */
    @GetMapping("/{id}")
    public Result<SecondhandItem> getById(@PathVariable Long id) {
        SecondhandItem secondhandItem = secondhandItemService.getById(id);
        return Result.success(secondhandItem);
    }
}