package com.survey.system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.survey.system.model.AnswerRecord;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AnswerRecordMapper extends BaseMapper<AnswerRecord> {
    List<AnswerRecord> selectBySurveyId(Long surveyId);
    Integer countBySurveyId(Long surveyId);
}