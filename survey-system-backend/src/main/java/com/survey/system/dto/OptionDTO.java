package com.survey.system.dto;

import lombok.Data;

@Data
public class OptionDTO {
    private Long id;
    private Long questionId;
    private String text;
    private Boolean isCorrect;
    private Integer sortOrder;
}