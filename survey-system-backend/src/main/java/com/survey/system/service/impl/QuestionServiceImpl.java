package com.survey.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.survey.system.dao.QuestionMapper;
import com.survey.system.dao.OptionMapper;
import com.survey.system.dto.QuestionDTO;
import com.survey.system.dto.OptionDTO;
import com.survey.system.model.Question;
import com.survey.system.model.Option;
import com.survey.system.service.QuestionService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class QuestionServiceImpl implements QuestionService {

    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private OptionMapper optionMapper;

    @Override
    @Transactional
    public QuestionDTO createQuestion(QuestionDTO questionDTO) {
        Question question = new Question();
        BeanUtils.copyProperties(questionDTO, question);
        // 确保content字段与text字段一致
        if (questionDTO.getText() != null) {
            question.setContent(questionDTO.getText());
        }
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

        return getQuestionById(question.getId());
    }

    @Override
    @Transactional
    public QuestionDTO updateQuestion(QuestionDTO questionDTO) {
        Question question = new Question();
        BeanUtils.copyProperties(questionDTO, question);
        // 确保content字段与text字段一致
        if (questionDTO.getText() != null) {
            question.setContent(questionDTO.getText());
        }
        question.setUpdatedAt(LocalDateTime.now());
        questionMapper.updateById(question);

        // 删除旧的选项
        optionMapper.delete(
            new LambdaQueryWrapper<Option>()
                .eq(Option::getQuestionId, question.getId())
        );

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

        return getQuestionById(question.getId());
    }

    @Override
    @Transactional
    public void deleteQuestion(Long id) {
        // 删除选项
        optionMapper.delete(
            new LambdaQueryWrapper<Option>()
                .eq(Option::getQuestionId, id)
        );
        // 删除问题
        questionMapper.deleteById(id);
    }

    @Override
    public QuestionDTO getQuestionById(Long id) {
        Question question = questionMapper.selectById(id);
        if (question == null) {
            return null;
        }

        QuestionDTO questionDTO = new QuestionDTO();
        BeanUtils.copyProperties(question, questionDTO);

        List<Option> options = optionMapper.selectList(
            new LambdaQueryWrapper<Option>()
                .eq(Option::getQuestionId, id)
        );
        List<OptionDTO> optionDTOs = new ArrayList<>();
        for (Option option : options) {
            OptionDTO optionDTO = new OptionDTO();
            BeanUtils.copyProperties(option, optionDTO);
            optionDTOs.add(optionDTO);
        }
        questionDTO.setOptions(optionDTOs);

        return questionDTO;
    }

    @Override
    public List<Question> getQuestionsBySurveyId(Long surveyId) {
        return questionMapper.selectList(
            new LambdaQueryWrapper<Question>()
                .eq(Question::getSurveyId, surveyId)
        );
    }
}