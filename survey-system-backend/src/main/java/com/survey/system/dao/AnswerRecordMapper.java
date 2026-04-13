package com.survey.system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.survey.system.model.AnswerRecord;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AnswerRecordMapper extends BaseMapper<AnswerRecord> {
    @Select("SELECT * FROM answer_record WHERE survey_id = #{surveyId}")
    List<AnswerRecord> selectBySurveyId(Long surveyId);
    
    @Select("SELECT COUNT(*) FROM answer_record WHERE survey_id = #{surveyId}")
    Integer countBySurveyId(Long surveyId);
}