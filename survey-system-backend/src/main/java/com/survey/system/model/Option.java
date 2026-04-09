package com.survey.system.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("survey_option")
public class Option {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long questionId;
    private String text;
    private Boolean isCorrect;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}