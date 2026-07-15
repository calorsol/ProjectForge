package com.example.ppm.entity;
import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;
@TableName("task_history") public class TaskHistory {
 @TableId(type=IdType.AUTO) private Long id; @TableField("task_id") private Long taskId; @TableField("user_id") private Long userId;
 @TableField("user_name") private String userName; private String action; private String detail; @TableField("created_at") private LocalDateTime createdAt;
 public Long getId(){return id;} public void setId(Long v){id=v;}
 public Long getTaskId(){return taskId;} public void setTaskId(Long v){taskId=v;}
 public Long getUserId(){return userId;} public void setUserId(Long v){userId=v;}
 public String getUserName(){return userName;} public void setUserName(String v){userName=v;}
 public String getAction(){return action;} public void setAction(String v){action=v;}
 public String getDetail(){return detail;} public void setDetail(String v){detail=v;}
 public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
}
