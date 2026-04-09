package com.survey.system.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("answer_record")
public class AnswerRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long surveyId;
    private String userId;
    private String userName;
    private String answers; // JSON格式存储
    private Double score;
    private Integer correctCount;
    private Integer answerTime;
    private LocalDateTime submitTime;
}