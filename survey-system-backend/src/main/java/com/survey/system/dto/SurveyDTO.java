package com.survey.system.dto;

import lombok.Data;
import java.util.List;

@Data
public class SurveyDTO {
    private Long id;
    private String title;
    private String description;
    private String startTime;
    private String endTime;
    private Boolean allowRepeat;
    private Double timeLimit;
    private Double passingScore;
    private String status;
    private List<QuestionDTO> questions;
    private Integer questionCount;
    private String createdAt;
}