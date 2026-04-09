package com.survey.system.controller;

import com.survey.system.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/survey/{surveyId}")
    public Map<String, Object> getSurveyStatistics(@PathVariable Long surveyId) {
        return statisticsService.getSurveyStatistics(surveyId);
    }

    @GetMapping("/question/{questionId}")
    public Map<String, Object> getQuestionStatistics(@PathVariable Long questionId) {
        return statisticsService.getQuestionStatistics(questionId);
    }

    @GetMapping("/ranking/{surveyId}")
    public Map<String, Object> getUserRanking(@PathVariable Long surveyId) {
        return statisticsService.getUserRanking(surveyId);
    }
}