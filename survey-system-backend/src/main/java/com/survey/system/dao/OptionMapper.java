package com.survey.system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.survey.system.model.Option;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OptionMapper extends BaseMapper<Option> {
    @Select("SELECT * FROM survey_option WHERE question_id = #{questionId}")
    List<Option> selectByQuestionId(Long questionId);
}