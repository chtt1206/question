package com.survey.system.controller;

import com.survey.system.dto.SurveyDTO;
import com.survey.system.model.Survey;
import com.survey.system.service.SurveyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/survey")
public class SurveyController {

    @Autowired
    private SurveyService surveyService;

    @PostMapping
    public SurveyDTO createSurvey(@RequestBody SurveyDTO surveyDTO) {
        return surveyService.createSurvey(surveyDTO);
    }

    @PutMapping
    public SurveyDTO updateSurvey(@RequestBody SurveyDTO surveyDTO) {
        return surveyService.updateSurvey(surveyDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteSurvey(@PathVariable Long id) {
        surveyService.deleteSurvey(id);
    }

    @GetMapping("/{id}")
    public SurveyDTO getSurveyById(@PathVariable Long id) {
        return surveyService.getSurveyById(id);
    }

    @GetMapping
    public List<SurveyDTO> getSurveyList() {
        return surveyService.getSurveyList();
    }

    @PutMapping("/{id}/publish")
    public void publishSurvey(@PathVariable Long id) {
        surveyService.publishSurvey(id);
    }

    @PutMapping("/{id}/unpublish")
    public void unpublishSurvey(@PathVariable Long id) {
        surveyService.unpublishSurvey(id);
    }

    @PutMapping("/{id}/settings")
    public void updateSettings(@PathVariable Long id, @RequestBody SurveyDTO settingsDTO) {
        surveyService.updateSettings(id, settingsDTO);
    }
}