package com.survey.system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.survey.system.model.Option;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OptionMapper extends BaseMapper<Option> {
    List<Option> selectByQuestionId(Long questionId);
}