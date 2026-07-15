package com.example.ppm.entity;
import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;
@TableName("task_note") public class TaskNote {
 @TableId(type=IdType.AUTO) private Long id; @TableField("task_id") private Long taskId; @TableField("author_id") private Long authorId;
 @TableField("author_name") private String authorName; private String content; @TableField("created_at") private LocalDateTime createdAt; @TableLogic private Integer deleted;
 public Long getId(){return id;} public void setId(Long v){id=v;}
 public Long getTaskId(){return taskId;} public void setTaskId(Long v){taskId=v;}
 public Long getAuthorId(){return authorId;} public void setAuthorId(Long v){authorId=v;}
 public String getAuthorName(){return authorName;} public void setAuthorName(String v){authorName=v;}
 public String getContent(){return content;} public void setContent(String v){content=v;}
 public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
}
