package com.survey.system.service.impl;

import com.survey.system.dao.AnswerRecordMapper;
import com.survey.system.dao.QuestionMapper;
import com.survey.system.dao.OptionMapper;
import com.survey.system.dao.QuestionStatisticsMapper;
import com.survey.system.model.AnswerRecord;
import com.survey.system.model.Question;
import com.survey.system.model.Option;
import com.survey.system.model.QuestionStatistics;
import com.survey.system.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private AnswerRecordMapper answerRecordMapper;
    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private OptionMapper optionMapper;
    @Autowired
    private QuestionStatisticsMapper questionStatisticsMapper;

    @Override
    public Map<String, Object> getSurveyStatistics(Long surveyId) {
        Map<String, Object> statistics = new HashMap<>();

        // 获取答题总数
        int answerCount = answerRecordMapper.countBySurveyId(surveyId);
        statistics.put("answerCount", answerCount);

        // 获取平均分
        List<AnswerRecord> answerRecords = answerRecordMapper.selectBySurveyId(surveyId);
        double averageScore = 0;
        if (!answerRecords.isEmpty()) {
            double totalScore = answerRecords.stream().mapToDouble(AnswerRecord::getScore).sum();
            averageScore = totalScore / answerRecords.size();
        }
        statistics.put("averageScore", averageScore);

        // 获取最高分
        double maxScore = answerRecords.stream().mapToDouble(AnswerRecord::getScore).max().orElse(0);
        statistics.put("maxScore", maxScore);

        // 获取最低分
        double minScore = answerRecords.stream().mapToDouble(AnswerRecord::getScore).min().orElse(0);
        statistics.put("minScore", minScore);

        return statistics;
    }

    @Override
    public Map<String, Object> getQuestionStatistics(Long questionId) {
        Map<String, Object> statistics = new HashMap<>();

        // 获取问题信息
        Question question = questionMapper.selectById(questionId);
        statistics.put("question", question);

        // 获取选项信息
        List<Option> options = optionMapper.selectByQuestionId(questionId);
        statistics.put("options", options);

        // 计算每个选项的选择次数
        List<AnswerRecord> answerRecords = answerRecordMapper.selectList(null);
        Map<Long, Integer> optionCount = new HashMap<>();
        int totalAnswers = 0;

        for (AnswerRecord record : answerRecords) {
            // 解析答案（实际项目中需要解析JSON）
            // 这里简化处理，实际需要根据存储的JSON格式解析
            totalAnswers++;
        }

        // 计算选择率
        List<Map<String, Object>> optionStatistics = new ArrayList<>();
        for (Option option : options) {
            Map<String, Object> optionStat = new HashMap<>();
            optionStat.put("option", option);
            int count = optionCount.getOrDefault(option.getId(), 0);
            optionStat.put("count", count);
            double rate = totalAnswers > 0 ? (double) count / totalAnswers * 100 : 0;
            optionStat.put("rate", rate);
            optionStatistics.add(optionStat);
        }

        statistics.put("optionStatistics", optionStatistics);

        return statistics;
    }

    @Override
    public Map<String, Object> getUserRanking(Long surveyId) {
        Map<String, Object> ranking = new HashMap<>();

        // 获取所有答题记录
        List<AnswerRecord> answerRecords = answerRecordMapper.selectBySurveyId(surveyId);

        // 按分数排序
        List<AnswerRecord> sortedRecords = answerRecords.stream()
                .sorted(Comparator.comparingDouble(AnswerRecord::getScore).reversed())
                .collect(Collectors.toList());

        ranking.put("ranking", sortedRecords);

        return ranking;
    }
}