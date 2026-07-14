package com.example.ppm.entity;
import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;
@TableName("project") public class Project {
 @TableId(type=IdType.AUTO) private Long id; private String name; private String code; @TableField("owner_id") private Long ownerId; @TableField("sort_no") private Integer sortNo; private String description; private String status; @TableField("created_at") private LocalDateTime createdAt; @TableLogic private Integer deleted;
 public Long getId(){return id;} public void setId(Long v){id=v;} public String getName(){return name;} public void setName(String v){name=v;} public String getCode(){return code;} public void setCode(String v){code=v;} public Long getOwnerId(){return ownerId;} public void setOwnerId(Long v){ownerId=v;} public Integer getSortNo(){return sortNo;} public void setSortNo(Integer v){sortNo=v;} public String getDescription(){return description;} public void setDescription(String v){description=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;} public LocalDateTime getCreatedAt(){return createdAt;}
}
