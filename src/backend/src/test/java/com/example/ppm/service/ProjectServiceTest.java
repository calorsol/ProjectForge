package com.example.ppm.service;

import com.example.ppm.dto.project.ProjectRequest;
import com.example.ppm.entity.Project;
import com.example.ppm.mapper.ProjectMapper;
import com.example.ppm.mapper.TaskMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ProjectServiceTest {
    @Test
    void createAssignsCurrentUserAsOwner() {
        ProjectMapper mapper = mock(ProjectMapper.class);
        doAnswer(i -> { ((Project) i.getArgument(0)).setId(3L); return 1; }).when(mapper).insert(any(Project.class));
        ProjectService service = new ProjectService(mapper, mock(TaskMapper.class));

        var result = service.create(9L, new ProjectRequest("网站重构", "web_refactor", 30089, "<p>说明</p>"));

        assertEquals(3L, result.id());
        ArgumentCaptor<Project> captor = ArgumentCaptor.forClass(Project.class);
        verify(mapper).insert(captor.capture());
        assertEquals(9L, captor.getValue().getOwnerId());
    }
}
