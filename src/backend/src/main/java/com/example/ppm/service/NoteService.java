package com.example.ppm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.ppm.common.BusinessException;
import com.example.ppm.dto.note.NoteResponse;
import com.example.ppm.entity.Task;
import com.example.ppm.entity.TaskNote;
import com.example.ppm.mapper.TaskMapper;
import com.example.ppm.mapper.TaskNoteMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoteService {
    private final TaskNoteMapper mapper;
    private final TaskMapper taskMapper;
    public NoteService(TaskNoteMapper mapper, TaskMapper taskMapper) { this.mapper = mapper; this.taskMapper = taskMapper; }

    /** 备注归属于任务，任务必须属于当前用户 */
    private void requireOwnTask(Long uid, Long taskId) {
        Task t = taskMapper.selectById(taskId);
        if (t == null) throw new BusinessException(404, "任务不存在");
        if (!uid.equals(t.getOwnerId())) throw new BusinessException(403, "无权限");
    }

    public List<NoteResponse> list(Long uid, Long taskId) {
        requireOwnTask(uid, taskId);
        return mapper.selectList(new LambdaQueryWrapper<TaskNote>().eq(TaskNote::getTaskId, taskId).orderByDesc(TaskNote::getId))
                .stream().map(this::out).toList();
    }

    public NoteResponse add(Long uid, String username, Long taskId, String content) {
        requireOwnTask(uid, taskId);
        TaskNote n = new TaskNote();
        n.setTaskId(taskId); n.setAuthorId(uid); n.setAuthorName(username); n.setContent(content);
        mapper.insert(n);
        return out(mapper.selectById(n.getId()));
    }

    public NoteResponse update(Long uid, Long id, String content) {
        TaskNote n = mapper.selectById(id);
        if (n == null) throw new BusinessException(404, "备注不存在");
        requireOwnTask(uid, n.getTaskId());
        n.setContent(content);
        mapper.updateById(n);
        return out(mapper.selectById(id));
    }

    public void remove(Long uid, Long id) {
        TaskNote n = mapper.selectById(id);
        if (n == null) throw new BusinessException(404, "备注不存在");
        requireOwnTask(uid, n.getTaskId());
        mapper.deleteById(id);
    }

    private NoteResponse out(TaskNote n) {
        return new NoteResponse(n.getId(), n.getTaskId(), n.getAuthorName(), n.getContent(), n.getCreatedAt());
    }
}
