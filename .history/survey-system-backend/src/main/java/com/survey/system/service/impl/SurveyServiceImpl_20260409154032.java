package com.survey.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.survey.system.dao.SurveyMapper;
import com.survey.system.dao.QuestionMapper;
import com.survey.system.dao.OptionMapper;
import com.survey.system.dto.SurveyDTO;
import com.survey.system.dto.QuestionDTO;
import com.survey.system.dto.OptionDTO;
import com.survey.system.model.Survey;
import com.survey.system.model.Question;
import com.survey.system.model.Option;
import com.survey.system.service.SurveyService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class SurveyServiceImpl implements SurveyService {

    @Autowired
    private SurveyMapper surveyMapper;
    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private OptionMapper optionMapper;

    @Override
    @Transactional
    public SurveyDTO createSurvey(SurveyDTO surveyDTO) {
        Survey survey = new Survey();
        BeanUtils.copyProperties(surveyDTO, survey);
        // 转换时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (surveyDTO.getStartTime() != null) {
            survey.setStartTime(LocalDateTime.parse(surveyDTO.getStartTime(), formatter));
        }
        if (surveyDTO.getEndTime() != null) {
            survey.setEndTime(LocalDateTime.parse(surveyDTO.getEndTime(), formatter));
        }
        survey.setStatus("DRAFT");
        survey.setCreatedAt(LocalDateTime.now());
        survey.setUpdatedAt(LocalDateTime.now());
        surveyMapper.insert(survey);

        if (surveyDTO.getQuestions() != null) {
            for (QuestionDTO questionDTO : surveyDTO.getQuestions()) {
                Question question = new Question();
                BeanUtils.copyProperties(questionDTO, question);
                question.setSurveyId(survey.getId());
                question.setCreatedAt(LocalDateTime.now());
                question.setUpdatedAt(LocalDateTime.now());
                questionMapper.insert(question);

                if (questionDTO.getOptions() != null) {
                    for (OptionDTO optionDTO : questionDTO.getOptions()) {
                        Option option = new Option();
                        BeanUtils.copyProperties(optionDTO, option);
                        option.setQuestionId(question.getId());
                        option.setCreatedAt(LocalDateTime.now());
                        option.setUpdatedAt(LocalDateTime.now());
                        optionMapper.insert(option);
                    }
                }
            }
        }

        return getSurveyById(survey.getId());
    }

    @Override
    @Transactional
    public SurveyDTO updateSurvey(SurveyDTO surveyDTO) {
        Survey survey = new Survey();
        BeanUtils.copyProperties(surveyDTO, survey);
        // 转换时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (surveyDTO.getStartTime() != null) {
            survey.setStartTime(LocalDateTime.parse(surveyDTO.getStartTime(), formatter));
        }
        if (surveyDTO.getEndTime() != null) {
            survey.setEndTime(LocalDateTime.parse(surveyDTO.getEndTime(), formatter));
        }
        survey.setUpdatedAt(LocalDateTime.now());
        surveyMapper.updateById(survey);

        // 删除旧的问题和选项
        questionMapper.delete(new QueryWrapper<Question>().eq("survey_id", survey.getId()));

        if (surveyDTO.getQuestions() != null) {
            for (QuestionDTO questionDTO : surveyDTO.getQuestions()) {
                Question question = new Question();
                BeanUtils.copyProperties(questionDTO, question);
                question.setSurveyId(survey.getId());
                question.setCreatedAt(LocalDateTime.now());
                question.setUpdatedAt(LocalDateTime.now());
                questionMapper.insert(question);

                if (questionDTO.getOptions() != null) {
                    for (OptionDTO optionDTO : questionDTO.getOptions()) {
                        Option option = new Option();
                        BeanUtils.copyProperties(optionDTO, option);
                        option.setQuestionId(question.getId());
                        option.setCreatedAt(LocalDateTime.now());
                        option.setUpdatedAt(LocalDateTime.now());
                        optionMapper.insert(option);
                    }
                }
            }
        }

        return getSurveyById(survey.getId());
    }

    @Override
    @Transactional
    public void deleteSurvey(Long id) {
        // 删除选项
        List<Question> questions = questionMapper.selectList(new QueryWrapper<Question>().eq("survey_id", id));
        for (Question question : questions) {
            optionMapper.delete(new QueryWrapper<Option>().eq("question_id", question.getId()));
        }
        // 删除问题
        questionMapper.delete(new QueryWrapper<Question>().eq("survey_id", id));
        // 删除问卷
        surveyMapper.deleteById(id);
    }

    @Override
    public SurveyDTO getSurveyById(Long id) {
        Survey survey = surveyMapper.selectById(id);
        if (survey == null) {
            return null;
        }

        SurveyDTO surveyDTO = new SurveyDTO();
        BeanUtils.copyProperties(survey, surveyDTO);
        
        // 手动格式化时间字段
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (survey.getStartTime() != null) {
            surveyDTO.setStartTime(survey.getStartTime().format(formatter));
        }
        if (survey.getEndTime() != null) {
            surveyDTO.setEndTime(survey.getEndTime().format(formatter));
        }

        List<Question> questions = questionMapper.selectList(new QueryWrapper<Question>().eq("survey_id", id));
        List<QuestionDTO> questionDTOs = new ArrayList<>();
        for (Question question : questions) {
            QuestionDTO questionDTO = new QuestionDTO();
            BeanUtils.copyProperties(question, questionDTO);

            List<Option> options = optionMapper.selectList(new QueryWrapper<Option>().eq("question_id", question.getId()));
            List<OptionDTO> optionDTOs = new ArrayList<>();
            for (Option option : options) {
                OptionDTO optionDTO = new OptionDTO();
                BeanUtils.copyProperties(option, optionDTO);
                optionDTOs.add(optionDTO);
            }
            questionDTO.setOptions(optionDTOs);
            questionDTOs.add(questionDTO);
        }
        surveyDTO.setQuestions(questionDTOs);

        return surveyDTO;
    }

    @Override
    public List<SurveyDTO> getSurveyList() {
        List<Survey> surveys = surveyMapper.selectList(null);
        List<SurveyDTO> surveyDTOs = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        for (Survey survey : surveys) {
            SurveyDTO surveyDTO = new SurveyDTO();
            BeanUtils.copyProperties(survey, surveyDTO);
            
            // 手动格式化时间字段
            if (survey.getStartTime() != null) {
                surveyDTO.setStartTime(survey.getStartTime().format(formatter));
            }
            if (survey.getEndTime() != null) {
                surveyDTO.setEndTime(survey.getEndTime().format(formatter));
            }
            if (survey.getCreatedAt() != null) {
                surveyDTO.setCreatedAt(survey.getCreatedAt().format(formatter));
            }
            
            surveyDTOs.add(surveyDTO);
        }
        
        return surveyDTOs;
    }

    @Override
    public void publishSurvey(Long id) {
        Survey survey = surveyMapper.selectById(id);
        if (survey != null) {
            survey.setStatus("PUBLISHED");
            survey.setUpdatedAt(LocalDateTime.now());
            surveyMapper.updateById(survey);
        }
    }

    @Override
    public void unpublishSurvey(Long id) {
        Survey survey = surveyMapper.selectById(id);
        if (survey != null) {
            survey.setStatus("DRAFT");
            survey.setUpdatedAt(LocalDateTime.now());
            surveyMapper.updateById(survey);
        }
    }

    @Override
    public void updateSettings(Long id, SurveyDTO settingsDTO) {
        Survey survey = surveyMapper.selectById(id);
        if (survey != null) {
            // 转换时间格式
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            if (settingsDTO.getStartTime() != null) {
                survey.setStartTime(LocalDateTime.parse(settingsDTO.getStartTime(), formatter));
            }
            if (settingsDTO.getEndTime() != null) {
                survey.setEndTime(LocalDateTime.parse(settingsDTO.getEndTime(), formatter));
            }
            survey.setAllowRepeat(settingsDTO.getAllowRepeat());
            survey.setTimeLimit(settingsDTO.getTimeLimit());
            survey.setUpdatedAt(LocalDateTime.now());
            surveyMapper.updateById(survey);
        }
    }
}