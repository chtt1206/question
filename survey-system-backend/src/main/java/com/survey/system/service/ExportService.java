package com.survey.system.service;

import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public interface ExportService {
    StreamingResponseBody exportSurveyData(Long surveyId);
    StreamingResponseBody exportAnswerData(Long surveyId);
}