package com.survey.system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.survey.system.model.Survey;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SurveyMapper extends BaseMapper<Survey> {
}