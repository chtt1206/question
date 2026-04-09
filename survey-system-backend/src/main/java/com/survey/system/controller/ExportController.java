package com.survey.system.controller;

import com.survey.system.service.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/export")
public class ExportController {

    @Autowired
    private ExportService exportService;

    @GetMapping("/survey/{surveyId}")
    public StreamingResponseBody exportSurveyData(@PathVariable Long surveyId) {
        return exportService.exportSurveyData(surveyId);
    }

    @GetMapping("/answer/{surveyId}")
    public StreamingResponseBody exportAnswerData(@PathVariable Long surveyId) {
        return exportService.exportAnswerData(surveyId);
    }
}