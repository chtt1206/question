package com.survey.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.survey.system.dao.AnswerRecordMapper;
import com.survey.system.dao.QuestionMapper;
import com.survey.system.dao.OptionMapper;
import com.survey.system.dto.AnswerSubmitDTO;
import com.survey.system.model.AnswerRecord;
import com.survey.system.model.Question;
import com.survey.system.model.Option;
import com.survey.system.service.AnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnswerServiceImpl implements AnswerService {

    @Autowired
    private AnswerRecordMapper answerRecordMapper;
    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private OptionMapper optionMapper;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional
    public void submitAnswer(AnswerSubmitDTO answerSubmitDTO) {
        try {
            // 计算分数和正确题目数（不包含基础信息题）
            double score = 0;
            int correctCount = 0;

            // 使用MyBatis-Plus查询问卷的所有问题
            List<Question> questions = questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                    .eq(Question::getSurveyId, answerSubmitDTO.getSurveyId())
            );

            for (Question question : questions) {
                // 跳过基础信息题
                if ("BASIC".equals(question.getQuestionType())) {
                    continue;
                }

                // 查找对应的答案
                for (AnswerSubmitDTO.AnswerItemDTO answerItem : answerSubmitDTO.getAnswers()) {
                    if (answerItem.getQuestionId().equals(question.getId())) {
                        // 检查是否正确
                        if ("single".equals(question.getType()) || "multiple".equals(question.getType())) {
                            // 选择题
                            List<Option> options = optionMapper.selectList(
                                new LambdaQueryWrapper<Option>()
                                    .eq(Option::getQuestionId, question.getId())
                            );
                            boolean isCorrect = true;
                            for (Option option : options) {
                                boolean selected = answerItem.getSelectedOptions() != null && answerItem.getSelectedOptions().contains(option.getId());
                                if (option.getIsCorrect() != selected) {
                                    isCorrect = false;
                                    break;
                                }
                            }
                            if (isCorrect) {
                                score += question.getScore();
                                correctCount++;
                            }
                        } else if ("input".equals(question.getType())) {
                            // 填空题
                            if (question.getCorrectAnswer() != null && question.getCorrectAnswer().equals(answerItem.getTextAnswer())) {
                                score += question.getScore();
                                correctCount++;
                            }
                        }
                        break;
                    }
                }
            }

            // 保存答案记录
            AnswerRecord answerRecord = new AnswerRecord();
            answerRecord.setSurveyId(answerSubmitDTO.getSurveyId());
            answerRecord.setUserId(answerSubmitDTO.getUserId());
            answerRecord.setUserName(answerSubmitDTO.getUserName());
            answerRecord.setAnswers(objectMapper.writeValueAsString(answerSubmitDTO.getAnswers()));
            answerRecord.setScore(score);
            answerRecord.setCorrectCount(correctCount);
            answerRecord.setAnswerTime(answerSubmitDTO.getAnswerTime() != null ? answerSubmitDTO.getAnswerTime() : 0);
            answerRecord.setSubmitTime(LocalDateTime.now());
            answerRecordMapper.insert(answerRecord);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException("答案提交失败：" + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("答案提交失败：" + e.getMessage());
        }
    }

    @Override
    public List<AnswerRecord> getAnswersBySurveyId(Long surveyId) {
        return answerRecordMapper.selectList(
            new LambdaQueryWrapper<AnswerRecord>()
                .eq(AnswerRecord::getSurveyId, surveyId)
        );
    }

    @Override
    public Integer getAnswerCountBySurveyId(Long surveyId) {
        return Math.toIntExact(answerRecordMapper.selectCount(
            new LambdaQueryWrapper<AnswerRecord>()
                .eq(AnswerRecord::getSurveyId, surveyId)
        ));
    }

    @Override
    public AnswerRecord getAnswerBySurveyIdAndUserId(Long surveyId, String userId) {
        return answerRecordMapper.selectOne(
            new LambdaQueryWrapper<AnswerRecord>()
                .eq(AnswerRecord::getSurveyId, surveyId)
                .eq(AnswerRecord::getUserId, userId)
                .orderByDesc(AnswerRecord::getSubmitTime)
                .last("LIMIT 1")
        );
    }
}