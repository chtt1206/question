import axios from 'axios'

// 创建axios实例
const api = axios.create({
  baseURL: 'http://localhost:8081/api', // 后端API地址
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    // 可以在这里添加token等认证信息
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    console.error('API请求错误:', error)
    return Promise.reject(error)
  }
)

// 问卷相关API
export const surveyApi = {
  // 获取问卷列表
  getSurveyList: () => {
    return api.get('/survey')
  },

  // 获取问卷详情
  getSurveyDetail: (id) => {
    return api.get(`/survey/${id}`)
  },

  // 提交答案
  submitAnswer: (surveyId, answers, answerTime = 0) => {
    // 生成用户ID
    const userId = 'user_' + Date.now()
    // 构建提交数据
    const submitData = {
      surveyId: parseInt(surveyId),
      userId: userId,
      userName: answers[1] || '匿名用户', // 使用姓名作为userName
      answerTime: answerTime, // 答题时长（分钟）
      answers: Object.entries(answers).map(([questionId, answer]) => {
        const item = {
          questionId: parseInt(questionId)
        }

        // 根据题目类型处理答案
        if (typeof answer === 'string') {
          // 基础信息题或输入题
          item.textAnswer = answer
        } else if (Array.isArray(answer)) {
          // 多选题
          item.selectedOptions = answer.map(opt => parseInt(opt))
        } else {
          // 单选题
          item.selectedOptions = [parseInt(answer)]
        }

        return item
      })
    }

    return api.post('/answer', submitData).then(() => {
      // 返回 userId 供后续使用
      return { userId: userId }
    })
  },

  // 获取答题结果
  getAnswerResult: (surveyId, userId) => {
    return Promise.all([
      api.get(`/answer/result/${surveyId}/${userId}`),
      api.get(`/survey/${surveyId}`),
      api.get(`/answer/survey/${surveyId}`)
    ]).then(([record, survey, allRecords]) => {
      if (record) {
        // 计算总题数（不包含基础信息题）
        const totalQuestions = survey.questions ? survey.questions.filter(q => q.questionType !== 'BASIC').length : 0
        // 计算总分（所有非基础信息题的分数之和）
        const totalScore = survey.questions ? survey.questions.filter(q => q.questionType !== 'BASIC').reduce((sum, q) => sum + (q.score || 0), 0) : 0
        // 计算参与人数
        const totalParticipants = allRecords ? allRecords.length : 0
        // 计算排名（按得分降序，答对题数降序，答题时长升序）
        let rank = 1
        if (allRecords && allRecords.length > 0) {
          allRecords.sort((a, b) => {
            // 按得分降序
            if (b.score !== a.score) {
              return b.score - a.score
            }
            // 按答对题数降序
            if (b.correctCount !== a.correctCount) {
              return b.correctCount - a.correctCount
            }
            // 按答题时长升序
            return (a.answerTime || 0) - (b.answerTime || 0)
          })
          // 找到当前用户的排名
          for (let i = 0; i < allRecords.length; i++) {
            if (allRecords[i].userId === userId) {
              rank = i + 1
              break
            }
          }
        }
        // 构建结果数据结构
        return {
          surveyId: record.surveyId,
          score: record.score,
          totalScore: totalScore,
          correctCount: record.correctCount,
          totalQuestions: totalQuestions,
          timeUsed: record.answerTime || 0,
          rank: rank,
          totalParticipants: totalParticipants,
          answers: [] // 暂时返回空数组，实际项目中需要解析answers字段
        }
      } else {
        // 如果没有答案记录，返回默认数据
        return {
          surveyId: parseInt(surveyId),
          score: 0,
          totalScore: 0,
          correctCount: 0,
          totalQuestions: 0,
          timeUsed: 0,
          rank: 0,
          totalParticipants: 0,
          answers: []
        }
      }
    })
  }
}

export default api