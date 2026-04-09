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
  submitAnswer: (surveyId, answers) => {
    // 构建提交数据
    const submitData = {
      surveyId: parseInt(surveyId),
      userId: 'user_' + Date.now(), // 临时用户ID
      userName: answers[1] || '匿名用户', // 使用姓名作为userName
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
    
    return api.post('/answer', submitData)
  },

  // 获取答题结果
  getAnswerResult: (surveyId) => {
    // 模拟数据，实际项目中需要根据后端接口调整
    return new Promise(resolve => {
      setTimeout(() => {
        resolve({
          code: 200,
          data: {
            surveyId: parseInt(surveyId),
            surveyTitle: '2024年员工安全培训考试',
            score: 80,
            totalScore: 100,
            correctCount: 4,
            totalQuestions: 5,
            timeUsed: 15,
            rank: 5,
            totalParticipants: 120,
            answers: [
              {
                questionId: 1,
                questionText: '以下哪个是正确的手部卫生步骤？',
                userAnswer: '使用肥皂搓洗至少20秒',
                correctAnswer: '使用肥皂搓洗至少20秒',
                isCorrect: true
              },
              {
                questionId: 2,
                questionText: '以下哪些属于火灾逃生的正确做法？',
                userAnswer: '用湿毛巾捂住口鼻, 弯腰低姿前进',
                correctAnswer: '用湿毛巾捂住口鼻, 弯腰低姿前进',
                isCorrect: true
              },
              {
                questionId: 3,
                questionText: '电器着火时，应首先使用水基灭火器扑救。',
                userAnswer: '错误',
                correctAnswer: '错误',
                isCorrect: true
              },
              {
                questionId: 4,
                questionText: '以下哪种灭火器适用于电器火灾？',
                userAnswer: '干粉灭火器',
                correctAnswer: '干粉灭火器',
                isCorrect: true
              },
              {
                questionId: 5,
                questionText: '安全出口标志的颜色是？',
                userAnswer: '红色',
                correctAnswer: '绿色',
                isCorrect: false
              }
            ]
          }
        })
      }, 300)
    })
  }
}

export default api