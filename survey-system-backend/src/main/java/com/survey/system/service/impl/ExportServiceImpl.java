package com.survey.system.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.survey.system.dao.SurveyMapper;
import com.survey.system.dao.QuestionMapper;
import com.survey.system.dao.OptionMapper;
import com.survey.system.dao.AnswerRecordMapper;
import com.survey.system.model.Survey;
import com.survey.system.model.Question;
import com.survey.system.model.Option;
import com.survey.system.model.AnswerRecord;
import com.survey.system.service.ExportService;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@Service
public class ExportServiceImpl implements ExportService {

    @Autowired
    private SurveyMapper surveyMapper;
    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private OptionMapper optionMapper;
    @Autowired
    private AnswerRecordMapper answerRecordMapper;

    @Override
    public StreamingResponseBody exportSurveyData(Long surveyId) {
        return outputStream -> {
            // 创建Excel样式
            WriteCellStyle headStyle = new WriteCellStyle();
            WriteFont headFont = new WriteFont();
            headFont.setBold(true);
            headStyle.setWriteFont(headFont);
            headStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
            headStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            WriteCellStyle contentStyle = new WriteCellStyle();
            contentStyle.setHorizontalAlignment(HorizontalAlignment.LEFT);
            contentStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            HorizontalCellStyleStrategy styleStrategy = new HorizontalCellStyleStrategy(headStyle, contentStyle);

            // 导出问卷基本信息
            EasyExcel.write(outputStream, Survey.class)
                    .sheet("问卷信息")
                    .registerWriteHandler(styleStrategy)
                    .doWrite(List.of(surveyMapper.selectById(surveyId)));

            // 导出问题信息
            List<Question> questions = questionMapper.selectBySurveyId(surveyId);
            EasyExcel.write(outputStream, Question.class)
                    .sheet("问题信息")
                    .registerWriteHandler(styleStrategy)
                    .doWrite(questions);

            // 导出选项信息
            for (Question question : questions) {
                List<Option> options = optionMapper.selectByQuestionId(question.getId());
                if (!options.isEmpty()) {
                    EasyExcel.write(outputStream, Option.class)
                            .sheet("选项信息-问题" + question.getId())
                            .registerWriteHandler(styleStrategy)
                            .doWrite(options);
                }
            }
        };
    }

    @Override
    public StreamingResponseBody exportAnswerData(Long surveyId) {
        return outputStream -> {
            // 创建Excel样式
            WriteCellStyle headStyle = new WriteCellStyle();
            WriteFont headFont = new WriteFont();
            headFont.setBold(true);
            headStyle.setWriteFont(headFont);
            headStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
            headStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            WriteCellStyle contentStyle = new WriteCellStyle();
            contentStyle.setHorizontalAlignment(HorizontalAlignment.LEFT);
            contentStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            HorizontalCellStyleStrategy styleStrategy = new HorizontalCellStyleStrategy(headStyle, contentStyle);

            // 导出答题记录
            List<AnswerRecord> answerRecords = answerRecordMapper.selectBySurveyId(surveyId);
            EasyExcel.write(outputStream, AnswerRecord.class)
                    .sheet("答题记录")
                    .registerWriteHandler(styleStrategy)
                    .doWrite(answerRecords);
        };
    }
}