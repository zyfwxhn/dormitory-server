package com.dormitory.dormitoryserver.controller.admin;

import com.alibaba.excel.EasyExcel;
import com.dormitory.dormitoryserver.dto.StudentExcelDTO;
import com.dormitory.dormitoryserver.listener.StudentExcelListener;
import com.dormitory.dormitoryserver.result.Result;
import com.dormitory.dormitoryserver.service.StudentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

/**
 * 管理员端 - 学生信息管理接口
 */
@RestController
@RequestMapping("/admin/student")
@Slf4j
public class StudentAdminController {

    @Autowired
    private StudentService studentService;

    /**
     * 通过 Excel 批量导入学生
     * @param file 前端上传的 Excel 文件
     * @return 统一响应结果
     */
    @PostMapping("/import")
    public Result importStudents(@RequestParam("file") MultipartFile file) {
        log.info("管理员请求导入学生数据: {}", file.getOriginalFilename());
        try {
            
            EasyExcel.read(file.getInputStream(), StudentExcelDTO.class, new StudentExcelListener(studentService)).sheet().doRead();
            return Result.success("批量导入学生成功！");
        } catch (IOException e) {
            log.error("Excel读取失败", e);
            return Result.error("导入失败: " + e.getMessage());
        }
    }
}