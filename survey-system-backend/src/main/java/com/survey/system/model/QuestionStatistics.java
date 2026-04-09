package com.survey.system.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("question_statistics")
public class QuestionStatistics {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long questionId;
    private String optionText;
    private Integer selectCount;
    private Double selectRate;
    private LocalDateTime updatedAt;
}