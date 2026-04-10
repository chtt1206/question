package com.survey.system.service;

import com.survey.system.dto.AnswerSubmitDTO;
import com.survey.system.model.AnswerRecord;
import java.util.List;

public interface AnswerService {
    void submitAnswer(AnswerSubmitDTO answerSubmitDTO);
    List<AnswerRecord> getAnswersBySurveyId(Long surveyId);
    Integer getAnswerCountBySurveyId(Long surveyId);
    AnswerRecord getAnswerBySurveyIdAndUserId(Long surveyId, String userId);
}