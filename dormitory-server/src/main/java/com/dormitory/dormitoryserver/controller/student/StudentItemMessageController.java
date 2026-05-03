package com.dormitory.dormitoryserver.controller.student;

import com.dormitory.dormitoryserver.context.BaseContext;
import com.dormitory.dormitoryserver.entity.ItemMessage;
import com.dormitory.dormitoryserver.result.Result;
import com.dormitory.dormitoryserver.service.ItemMessageService;
import com.dormitory.dormitoryserver.vo.ItemMessageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/student/item-message")
public class StudentItemMessageController {

    @Autowired
    private ItemMessageService itemMessageService;

    @PostMapping
    public Result publishMessage(@RequestBody ItemMessage itemMessage) {
        Long currentStudentId = BaseContext.getCurrentId();
        itemMessage.setFromStudentId(currentStudentId);
        itemMessageService.publishMessage(itemMessage);
        return Result.success();
    }

    @GetMapping("/{itemId}")
    public Result<List<ItemMessageVO>> getMessagesByItemId(@PathVariable Long itemId) {
        List<ItemMessageVO> messages = itemMessageService.getMessagesByItemId(itemId);
        return Result.success(messages);
    }
}