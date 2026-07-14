package com.example.ppm.controller;
import com.example.ppm.common.R; import com.example.ppm.dto.project.*; import com.example.ppm.security.CurrentUserContext; import com.example.ppm.service.ProjectService; import jakarta.validation.Valid; import org.springframework.web.bind.annotation.*; import java.util.List;
@RestController @RequestMapping("/api/projects") public class ProjectController { private final ProjectService service; public ProjectController(ProjectService s){service=s;} private Long uid(){return CurrentUserContext.require().userId();}
 @GetMapping public R<List<ProjectResponse>> list(@RequestParam(required=false)String keyword,@RequestParam(required=false)String status){return R.ok(service.list(uid(),keyword,status));}
 @PostMapping public R<ProjectResponse> create(@Valid @RequestBody ProjectRequest r){return R.ok(service.create(uid(),r));}
 @GetMapping("/{id}") public R<ProjectResponse> get(@PathVariable Long id){return R.ok(service.get(uid(),id));}
 @PutMapping("/{id}") public R<ProjectResponse> update(@PathVariable Long id,@Valid @RequestBody ProjectRequest r){return R.ok(service.update(uid(),id,r));}
 @DeleteMapping("/{id}") public R<Void> delete(@PathVariable Long id){service.delete(uid(),id);return R.ok();}}
