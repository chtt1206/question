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
        int totalAnswers = answerRecordMapper.countBySurveyId(surveyId);
        statistics.put("totalAnswers", totalAnswers);

        // 计算完成率（假设完成率为100%，实际项目中可能需要根据具体逻辑计算）
        double completionRate = 100.0;
        statistics.put("completionRate", completionRate);

        // 获取用户排名
        List<Map<String, Object>> userRank = new ArrayList<>();
        List<AnswerRecord> answerRecords = answerRecordMapper.selectBySurveyId(surveyId);
        
        // 按排序规则排序：先按总分降序，再按答对题数降序，再按答题时长升序
        List<AnswerRecord> sortedRecords = answerRecords.stream()
                .sorted((a, b) -> {
                    // 1. 按总分降序
                    if (b.getScore() != a.getScore()) {
                        return Double.compare(b.getScore(), a.getScore());
                    }
                    // 2. 按答对题数降序
                    if (b.getCorrectCount() != a.getCorrectCount()) {
                        return Integer.compare(b.getCorrectCount(), a.getCorrectCount());
                    }
                    // 3. 按答题时长升序
                    return Integer.compare(a.getAnswerTime(), b.getAnswerTime());
                })
                .collect(Collectors.toList());
        
        // 构建用户排名数据
        for (int i = 0; i < sortedRecords.size(); i++) {
            AnswerRecord record = sortedRecords.get(i);
            Map<String, Object> userRankItem = new HashMap<>();
            userRankItem.put("rank", i + 1);
            userRankItem.put("name", record.getUserName());
            userRankItem.put("correctCount", record.getCorrectCount() + "/" + 10); // 假设总题数为10，实际项目中需要根据具体问卷计算
            userRankItem.put("score", record.getScore() + "分");
            userRankItem.put("answerTime", record.getAnswerTime() + "秒");
            userRankItem.put("submitTime", record.getSubmitTime().toString());
            userRank.add(userRankItem);
        }
        statistics.put("userRank", userRank);

        // 获取题目统计
        List<Map<String, Object>> questionStats = new ArrayList<>();
        List<Question> questions = questionMapper.selectBySurveyId(surveyId);
        
        for (Question question : questions) {
            Map<String, Object> questionStat = new HashMap<>();
            questionStat.put("id", question.getId());
            questionStat.put("type", question.getType());
            questionStat.put("text", question.getText());
            
            // 计算题目统计数据（实际项目中需要根据具体逻辑计算）
            questionStat.put("totalCount", totalAnswers);
            questionStat.put("correctCount", totalAnswers / 2); // 示例数据
            questionStat.put("wrongCount", totalAnswers / 2); // 示例数据
            questionStat.put("correctRate", "50.0%"); // 示例数据
            questionStat.put("optionAnalysis", "选项A(10次), 选项B(5次), 选项C(15次)"); // 示例数据
            
            questionStats.add(questionStat);
        }
        statistics.put("questionStats", questionStats);

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