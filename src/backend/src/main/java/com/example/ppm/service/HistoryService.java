package com.example.ppm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.ppm.dto.history.HistoryResponse;
import com.example.ppm.entity.TaskHistory;
import com.example.ppm.mapper.TaskHistoryMapper;
import com.example.ppm.security.CurrentUserContext;
import com.example.ppm.security.JwtUser;
import org.springframework.stereotype.Service;

import java.util.List;

/** 记录任务的操作历史（谁创建、谁编辑、谁改了时间/状态） */
@Service
public class HistoryService {
    private final TaskHistoryMapper mapper;
    public HistoryService(TaskHistoryMapper mapper) { this.mapper = mapper; }

    public void record(Long taskId, String action, String detail) {
        TaskHistory h = new TaskHistory();
        h.setTaskId(taskId); h.setAction(action); h.setDetail(detail);
        JwtUser u = CurrentUserContext.get();
        if (u != null) { h.setUserId(u.userId()); h.setUserName(u.username()); }
        mapper.insert(h);
    }

    public List<HistoryResponse> list(Long taskId) {
        return mapper.selectList(new LambdaQueryWrapper<TaskHistory>().eq(TaskHistory::getTaskId, taskId).orderByAsc(TaskHistory::getId))
                .stream().map(h -> new HistoryResponse(h.getId(), h.getUserName(), h.getAction(), h.getDetail(), h.getCreatedAt())).toList();
    }
}
