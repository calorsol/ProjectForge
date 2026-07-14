package com.example.ppm.service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.ppm.common.BusinessException;
import com.example.ppm.dto.project.*;
import com.example.ppm.entity.Project;
import com.example.ppm.mapper.ProjectMapper;
import com.example.ppm.mapper.TaskMapper;
import com.example.ppm.entity.Task;
import org.springframework.stereotype.Service;
import java.util.List;
@Service public class ProjectService {
 private final ProjectMapper mapper; private final TaskMapper taskMapper; public ProjectService(ProjectMapper mapper,TaskMapper taskMapper){this.mapper=mapper;this.taskMapper=taskMapper;}
 public ProjectResponse create(Long ownerId, ProjectRequest r){Project p=new Project();p.setName(r.name());p.setCode(r.code());p.setSortNo(r.sortNo()==null?0:r.sortNo());p.setDescription(r.description());p.setStatus("ACTIVE");p.setOwnerId(ownerId);mapper.insert(p);return response(p);}
 public List<ProjectResponse> list(Long ownerId,String keyword,String status){return mapper.selectList(new LambdaQueryWrapper<Project>().eq(Project::getOwnerId,ownerId).like(keyword!=null&&!keyword.isBlank(),Project::getName,keyword).eq(status!=null&&!status.isBlank(),Project::getStatus,status).orderByAsc(Project::getSortNo)).stream().map(this::response).toList();}
 public ProjectResponse get(Long ownerId,Long id){return response(require(ownerId,id));}
 public ProjectResponse update(Long ownerId,Long id,ProjectRequest r){Project p=require(ownerId,id);p.setName(r.name());p.setCode(r.code());p.setSortNo(r.sortNo()==null?0:r.sortNo());p.setDescription(r.description());mapper.updateById(p);return response(p);}
 public void delete(Long ownerId,Long id){Project p=require(ownerId,id);mapper.deleteById(p.getId());}
 private Project require(Long ownerId,Long id){Project p=mapper.selectById(id);if(p==null)throw new BusinessException(404,"项目不存在");if(!ownerId.equals(p.getOwnerId()))throw new BusinessException(403,"无权限");return p;}
 private ProjectResponse response(Project p){long total=taskMapper.selectCount(new LambdaQueryWrapper<Task>().eq(Task::getProjectId,p.getId()));long open=taskMapper.selectCount(new LambdaQueryWrapper<Task>().eq(Task::getProjectId,p.getId()).notIn(Task::getStatus,List.of("DONE","CANCELED")));return new ProjectResponse(p.getId(),p.getName(),p.getCode(),p.getSortNo(),p.getDescription(),p.getStatus(),p.getCreatedAt(),total,open);}
}
