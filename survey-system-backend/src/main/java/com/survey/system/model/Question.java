package com.survey.system.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("question")
public class Question {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long surveyId;
    private String type; // SINGLE, MULTIPLE, TEXT
    private String text;
    private String content; // 与text字段保持一致，用于前端显示
    private Boolean required;
    private Integer correctOption;
    private String correctAnswer;
    private Double score;
    private String questionType; // BASIC, QUESTION
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}