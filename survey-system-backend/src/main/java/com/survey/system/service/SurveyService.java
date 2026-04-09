package com.survey.system.service;

import com.survey.system.dto.SurveyDTO;
import com.survey.system.model.Survey;
import java.util.List;

public interface SurveyService {
    SurveyDTO createSurvey(SurveyDTO surveyDTO);
    SurveyDTO updateSurvey(SurveyDTO surveyDTO);
    void deleteSurvey(Long id);
    SurveyDTO getSurveyById(Long id);
    List<SurveyDTO> getSurveyList();
    void publishSurvey(Long id);
    void unpublishSurvey(Long id);
    void updateSettings(Long id, SurveyDTO settingsDTO);
}