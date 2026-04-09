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
    private Integer timeLimit;
    private String status;
    private List<QuestionDTO> questions;
    private String createdAt;
}