package com.dormitory.dormitoryserver.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.dormitory.dormitoryserver.dto.StudentExcelDTO;
import com.dormitory.dormitoryserver.service.StudentService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Excel 解析监听器 (流式读取，防 OOM)
 * 注意：Listener 不能被 Spring 托管（不能加 @Component），每次读取都要 new，
 * 所以 Service 需要通过构造方法传进来。
 */
@Slf4j
public class StudentExcelListener implements ReadListener<StudentExcelDTO> {

    // 每隔 100 条存一次数据库，清理 list 防止内存溢出
    private static final int BATCH_COUNT = 100;

    // 缓存读取的数据
    private List<StudentExcelDTO> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

    // 业务逻辑 Service
    private StudentService studentService;

    /**
     * 构造方法，传入 Spring 容器中的 studentService
     */
    public StudentExcelListener(StudentService studentService) {
        this.studentService = studentService;
    }

    /**
     * 每一条数据解析都会调用这个方法
     */
    @Override
    public void invoke(StudentExcelDTO data, AnalysisContext context) {
        cachedDataList.add(data);
        // 达到 BATCH_COUNT 阈值，就去存储一次数据库
        if (cachedDataList.size() >= BATCH_COUNT) {
            saveData();
            // 存储完成清理 list
            cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        }
    }

    /**
     * 所有数据解析完成以后会调用这个方法
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 确保最后遗留的数据也存储到数据库
        saveData();
        log.info("Excel 所有数据解析并导入完成！");
    }

    /**
     * 执行保存到数据库
     */
    private void saveData() {
        if (!cachedDataList.isEmpty()) {
            log.info("{} 条数据，开始存储数据库！", cachedDataList.size());
            // 【注意】这里会飘红报错，因为我们还没在 StudentService 写这个方法，下一步就写！
            studentService.importStudentList(cachedDataList);
        }
    }
}