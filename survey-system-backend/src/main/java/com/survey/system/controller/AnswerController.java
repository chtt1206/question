package com.survey.system.controller;

import com.survey.system.dto.AnswerSubmitDTO;
import com.survey.system.model.AnswerRecord;
import com.survey.system.service.AnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/answer")
public class AnswerController {

    @Autowired
    private AnswerService answerService;

    @PostMapping
    public void submitAnswer(@RequestBody AnswerSubmitDTO answerSubmitDTO) {
        answerService.submitAnswer(answerSubmitDTO);
    }

    @GetMapping("/survey/{surveyId}")
    public List<AnswerRecord> getAnswersBySurveyId(@PathVariable Long surveyId) {
        return answerService.getAnswersBySurveyId(surveyId);
    }

    @GetMapping("/count/{surveyId}")
    public Integer getAnswerCountBySurveyId(@PathVariable Long surveyId) {
        return answerService.getAnswerCountBySurveyId(surveyId);
    }

    @GetMapping("/result/{surveyId}/{userId}")
    public AnswerRecord getAnswerResult(@PathVariable Long surveyId, @PathVariable String userId) {
        return answerService.getAnswerBySurveyIdAndUserId(surveyId, userId);
    }
}