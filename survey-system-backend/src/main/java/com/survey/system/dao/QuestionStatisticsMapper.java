package com.survey.system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.survey.system.model.QuestionStatistics;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuestionStatisticsMapper extends BaseMapper<QuestionStatistics> {
    List<QuestionStatistics> selectByQuestionId(Long questionId);
}