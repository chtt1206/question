package com.survey.system.controller;

import com.survey.system.dto.QuestionDTO;
import com.survey.system.model.Question;
import com.survey.system.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/question")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @PostMapping
    public QuestionDTO createQuestion(@RequestBody QuestionDTO questionDTO) {
        return questionService.createQuestion(questionDTO);
    }

    @PutMapping
    public QuestionDTO updateQuestion(@RequestBody QuestionDTO questionDTO) {
        return questionService.updateQuestion(questionDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
    }

    @GetMapping("/{id}")
    public QuestionDTO getQuestionById(@PathVariable Long id) {
        return questionService.getQuestionById(id);
    }

    @GetMapping("/survey/{surveyId}")
    public List<Question> getQuestionsBySurveyId(@PathVariable Long surveyId) {
        return questionService.getQuestionsBySurveyId(surveyId);
    }
}