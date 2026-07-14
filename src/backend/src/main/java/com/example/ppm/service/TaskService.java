package com.example.ppm.service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.ppm.common.BusinessException;
import com.example.ppm.dto.task.*;
import com.example.ppm.entity.Task;
import com.example.ppm.entity.Project;
import com.example.ppm.mapper.TaskMapper;
import com.example.ppm.mapper.ProjectMapper;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service public class TaskService {
 private final TaskMapper mapper; private final AttachmentService attachments; private final ProjectMapper projectMapper; public TaskService(TaskMapper mapper,AttachmentService attachments,ProjectMapper projectMapper){this.mapper=mapper;this.attachments=attachments;this.projectMapper=projectMapper;}
 public List<TaskResponse> list(Long uid,String keyword,Long projectId,String type,String status,LocalDateTime from,LocalDateTime to){return mapper.selectList(new LambdaQueryWrapper<Task>().eq(Task::getOwnerId,uid).like(keyword!=null&&!keyword.isBlank(),Task::getTitle,keyword).eq(projectId!=null,Task::getProjectId,projectId).eq(type!=null&&!type.isBlank(),Task::getType,type).eq(status!=null&&!status.isBlank(),Task::getStatus,status).ge(from!=null,Task::getPlanStartAt,from).le(to!=null,Task::getPlanStartAt,to).orderByDesc(Task::getId)).stream().map(this::out).toList();}
 public List<TaskResponse> workbench(Long uid,String tab){List<Task> all=mapper.selectList(new LambdaQueryWrapper<Task>().eq(Task::getOwnerId,uid).orderByDesc(Task::getId));return all.stream().filter(t->switch(tab==null?"todo":tab){case "todo"->Set.of("TODO","DOING").contains(t.getStatus());case "paused"->"PAUSED".equals(t.getStatus());case "done"->"DONE".equals(t.getStatus());case "all"->true;default->false;}).map(this::out).toList();}
 public TaskResponse create(Long uid,TaskRequest r){Task t=new Task();t.setOwnerId(uid);t.setStatus("TODO");apply(t,r);mapper.insert(t);attachments.bind(r.attachmentIds(),t.getId());return out(t);}
 public TaskResponse get(Long uid,Long id){return out(require(uid,id));}
 public TaskResponse update(Long uid,Long id,TaskRequest r){Task t=require(uid,id);apply(t,r);mapper.updateById(t);attachments.bind(r.attachmentIds(),t.getId());return out(t);}
 public void delete(Long uid,Long id){mapper.deleteById(require(uid,id).getId());}
 public TaskResponse status(Long uid,Long id,String action){Task t=require(uid,id);String next=switch(t.getStatus()+":"+action){case "TODO:start"->"DOING";case "DOING:pause"->"PAUSED";case "PAUSED:resume"->"DOING";case "DOING:finish","PAUSED:finish"->"DONE";case "TODO:cancel","DOING:cancel","PAUSED:cancel"->"CANCELED";case "DONE:reopen"->"DOING";default->throw new BusinessException(400,"非法的状态操作");};t.setStatus(next);t.setFinishedAt("DONE".equals(next)?LocalDateTime.now():"DOING".equals(next)&&"reopen".equals(action)?null:t.getFinishedAt());mapper.updateById(t);return out(t);}
 private Task require(Long uid,Long id){Task t=mapper.selectById(id);if(t==null)throw new BusinessException(404,"任务不存在");if(!uid.equals(t.getOwnerId()))throw new BusinessException(403,"无权限");return t;}
 private void apply(Task t,TaskRequest r){t.setTitle(r.title());t.setProjectId(r.projectId());t.setType(r.type());t.setPriority(r.priority()==null||r.priority().isBlank()?"MEDIUM":r.priority());t.setPlanStartAt(r.planStartAt());t.setDueAt(r.dueAt());t.setDescription(r.description());}
 private TaskResponse out(Task t){boolean overdue=t.getDueAt()!=null&&t.getDueAt().isBefore(LocalDateTime.now())&&Set.of("TODO","DOING","PAUSED").contains(t.getStatus());String projectName=t.getProjectId()==null?"默认项目":Optional.ofNullable(projectMapper.selectById(t.getProjectId())).map(Project::getName).orElse("默认项目");return new TaskResponse(t.getId(),t.getTitle(),t.getProjectId(),projectName,t.getType(),t.getPriority(),t.getStatus(),t.getPlanStartAt(),t.getDueAt(),overdue,t.getCreatedAt(),t.getFinishedAt(),t.getDescription());}
}
