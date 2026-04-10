package com.survey.system.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("survey")
public class Survey {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean allowRepeat;
    private Integer timeLimit;
    private Integer passingScore;
    private String status; // DRAFT, PUBLISHED, EXPIRED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}