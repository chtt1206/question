package com.survey.system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.survey.system.model.Question;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
    List<Question> selectBySurveyId(Long surveyId);
}