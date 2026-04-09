package com.survey.system.service;

import com.survey.system.dto.QuestionDTO;
import com.survey.system.model.Question;
import java.util.List;

public interface QuestionService {
    QuestionDTO createQuestion(QuestionDTO questionDTO);
    QuestionDTO updateQuestion(QuestionDTO questionDTO);
    void deleteQuestion(Long id);
    QuestionDTO getQuestionById(Long id);
    List<Question> getQuestionsBySurveyId(Long surveyId);
}