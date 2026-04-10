package com.survey.system.dto;

import lombok.Data;
import java.util.List;

@Data
public class AnswerSubmitDTO {
    private Long surveyId;
    private String userId;
    private String userName;
    private Integer answerTime; // 答题时长（秒）
    private List<AnswerItemDTO> answers;

    @Data
    public static class AnswerItemDTO {
        private Long questionId;
        private List<Long> selectedOptions;
        private String textAnswer;
    }
}