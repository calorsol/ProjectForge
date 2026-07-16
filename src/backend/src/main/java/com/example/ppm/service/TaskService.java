package com.example.ppm.service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.ppm.common.BusinessException;
import com.example.ppm.dto.history.HistoryResponse;
import com.example.ppm.dto.task.*;
import com.example.ppm.entity.Task;
import com.example.ppm.entity.Project;
import com.example.ppm.mapper.TaskMapper;
import com.example.ppm.mapper.ProjectMapper;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service public class TaskService {
 private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
 private static final Map<String,String> STATUS_LABEL = Map.of("TODO","未开始","DOING","进行中","PAUSED","已暂停","DONE","已完成","CANCELED","已取消");
 private static final Map<String,String> PRIORITY_LABEL = Map.of("HIGH","高","MEDIUM","中","LOW","低");
 private static final Map<String,String> ACTION_LABEL = Map.of("start","启动任务","pause","暂停任务","resume","继续任务","finish","完成任务","cancel","取消任务","reopen","重开任务");

 private final TaskMapper mapper; private final AttachmentService attachments; private final ProjectMapper projectMapper; private final HistoryService historyService;
 public TaskService(TaskMapper mapper,AttachmentService attachments,ProjectMapper projectMapper,HistoryService historyService){this.mapper=mapper;this.attachments=attachments;this.projectMapper=projectMapper;this.historyService=historyService;}

 public List<TaskResponse> list(Long uid,String keyword,Long projectId,String type,String status,LocalDateTime from,LocalDateTime to){return outList(mapper.selectList(new LambdaQueryWrapper<Task>().eq(Task::getOwnerId,uid).like(keyword!=null&&!keyword.isBlank(),Task::getTitle,keyword).eq(projectId!=null,Task::getProjectId,projectId).eq(type!=null&&!type.isBlank(),Task::getType,type).eq(status!=null&&!status.isBlank(),Task::getStatus,status).ge(from!=null,Task::getPlanStartAt,from).le(to!=null,Task::getPlanStartAt,to).orderByDesc(Task::getId)));}
 public List<TaskResponse> workbench(Long uid,String tab){List<Task> all=mapper.selectList(new LambdaQueryWrapper<Task>().eq(Task::getOwnerId,uid).orderByDesc(Task::getId));List<Task> filtered=all.stream().filter(t->switch(tab==null?"todo":tab){case "todo"->Set.of("TODO","DOING").contains(t.getStatus());case "paused"->"PAUSED".equals(t.getStatus());case "done"->"DONE".equals(t.getStatus());case "all"->true;default->false;}).toList();return outList(filtered);}

 /** 批量输出：一次查出所有涉及的项目名，避免逐条 selectById 的 N+1 查询 */
 private List<TaskResponse> outList(List<Task> tasks){
  Set<Long> pids=tasks.stream().map(Task::getProjectId).filter(Objects::nonNull).collect(java.util.stream.Collectors.toSet());
  Map<Long,String> names=pids.isEmpty()?Map.of():projectMapper.selectBatchIds(pids).stream().collect(java.util.stream.Collectors.toMap(Project::getId,Project::getName,(a,b)->a));
  return tasks.stream().map(t->out(t,t.getProjectId()==null?"默认项目":names.getOrDefault(t.getProjectId(),"默认项目"))).toList();
 }

 public TaskResponse create(Long uid,TaskRequest r){
  Task t=new Task();t.setOwnerId(uid);t.setStatus("TODO");apply(t,r);mapper.insert(t);
  attachments.bind(r.attachmentIds(),t.getId());
  historyService.record(t.getId(),"创建任务",null);
  return out(t);
 }

 public TaskResponse get(Long uid,Long id){return out(require(uid,id));}

 public TaskResponse update(Long uid,Long id,TaskRequest r){
  Task t=require(uid,id);
  String detail=diff(t,r);              // 必须在 apply 之前算，此时 t 还是旧值
  apply(t,r);mapper.updateById(t);
  attachments.bind(r.attachmentIds(),t.getId());
  if(!detail.isBlank()) historyService.record(id,"编辑任务",detail);
  return out(t);
 }

 public void delete(Long uid,Long id){mapper.deleteById(require(uid,id).getId());}

 public TaskResponse status(Long uid,Long id,String action){
  Task t=require(uid,id);String prev=t.getStatus();
  String next=switch(prev+":"+action){case "TODO:start"->"DOING";case "DOING:pause"->"PAUSED";case "PAUSED:resume"->"DOING";case "DOING:finish","PAUSED:finish"->"DONE";case "TODO:cancel","DOING:cancel","PAUSED:cancel"->"CANCELED";case "DONE:reopen"->"DOING";default->throw new BusinessException(400,"非法的状态操作");};
  t.setStatus(next);
  t.setFinishedAt("DONE".equals(next)?LocalDateTime.now():"DOING".equals(next)&&"reopen".equals(action)?null:t.getFinishedAt());
  mapper.updateById(t);
  historyService.record(id,ACTION_LABEL.getOrDefault(action,action),"状态："+label(STATUS_LABEL,prev)+" → "+label(STATUS_LABEL,next));
  return out(t);
 }

 public List<HistoryResponse> history(Long uid,Long id){require(uid,id);return historyService.list(id);}

 private Task require(Long uid,Long id){Task t=mapper.selectById(id);if(t==null)throw new BusinessException(404,"任务不存在");if(!uid.equals(t.getOwnerId()))throw new BusinessException(403,"无权限");return t;}
 private void apply(Task t,TaskRequest r){t.setTitle(r.title());t.setProjectId(r.projectId());t.setType(r.type());t.setPriority(r.priority()==null||r.priority().isBlank()?"MEDIUM":r.priority());t.setPlanStartAt(r.planStartAt());t.setDueAt(r.dueAt());t.setDescription(r.description());}

 /** 对比新旧值，生成「谁改了什么」的可读描述 */
 private String diff(Task old,TaskRequest r){
  List<String> c=new ArrayList<>();
  change(c,"任务名称",old.getTitle(),r.title());
  if(!Objects.equals(old.getProjectId(),r.projectId())) c.add("所属项目："+projectName(old.getProjectId())+" → "+projectName(r.projectId()));
  change(c,"任务类型",old.getType(),r.type());
  String np=r.priority()==null||r.priority().isBlank()?"MEDIUM":r.priority();
  if(!Objects.equals(old.getPriority(),np)) c.add("优先级："+label(PRIORITY_LABEL,old.getPriority())+" → "+label(PRIORITY_LABEL,np));
  change(c,"计划启动时间",fmt(old.getPlanStartAt()),fmt(r.planStartAt()));
  change(c,"截止时间",fmt(old.getDueAt()),fmt(r.dueAt()));
  if(!Objects.equals(nz(old.getDescription()),nz(r.description()))) c.add("修改了任务描述");
  return String.join("；",c);
 }
 private void change(List<String> c,String field,String oldV,String newV){if(!Objects.equals(nz(oldV),nz(newV))) c.add(field+"："+show(oldV)+" → "+show(newV));}
 private String nz(String s){return s==null?"":s;}
 private String show(String s){return s==null||s.isBlank()?"空":s;}
 private String fmt(LocalDateTime t){return t==null?null:t.format(FMT);}
 private String label(Map<String,String> m,String k){return k==null?"空":m.getOrDefault(k,k);}
 private String projectName(Long id){return id==null?"默认项目":Optional.ofNullable(projectMapper.selectById(id)).map(Project::getName).orElse("默认项目");}

 private TaskResponse out(Task t){return out(t,projectName(t.getProjectId()));}
 private TaskResponse out(Task t,String projectName){boolean overdue=t.getDueAt()!=null&&t.getDueAt().isBefore(LocalDateTime.now())&&Set.of("TODO","DOING","PAUSED").contains(t.getStatus());return new TaskResponse(t.getId(),t.getTitle(),t.getProjectId(),projectName,t.getType(),t.getPriority(),t.getStatus(),t.getPlanStartAt(),t.getDueAt(),overdue,t.getCreatedAt(),t.getFinishedAt(),t.getDescription());}
}
