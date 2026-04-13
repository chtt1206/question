package com.survey.system.dto;

import lombok.Data;
import java.util.List;

@Data
public class QuestionDTO {
    private Long id;
    private Long surveyId;
    private String type;
    private String text;
    private String content; // 与text字段保持一致，用于前端显示
    private Boolean required;
    private Integer correctOption;
    private String correctAnswer;
    private Double score;
    private String questionType;
    private Integer sortOrder;
    private String answerExplanation; // 答案解析
    private List<OptionDTO> options;
}