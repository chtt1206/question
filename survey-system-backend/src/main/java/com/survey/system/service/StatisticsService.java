package com.survey.system.service;

import java.util.Map;

public interface StatisticsService {
    Map<String, Object> getSurveyStatistics(Long surveyId);
    Map<String, Object> getSurveyStatisticsWithPagination(Long surveyId, int page, int pageSize);
    Map<String, Object> getQuestionStatistics(Long questionId);
    Map<String, Object> getUserRanking(Long surveyId);
}