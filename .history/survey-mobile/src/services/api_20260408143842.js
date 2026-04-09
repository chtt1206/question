import axios from 'axios'

// 创建axios实例
const api = axios.create({
  baseURL: 'http://localhost:8080', // 后端API地址
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
    // 模拟数据
    return new Promise(resolve => {
      setTimeout(() => {
        resolve({
          code: 200,
          data: [
            {
              id: 1,
              title: '2024年员工安全培训考试',
              status: 'ongoing',
              startTime: '2026-04-10T08:00',
              endTime: '2026-04-20T23:59',
              answered: false,
              questions: 3
            },
            {
              id: 2,
              title: '客户满意度调查',
              status: 'ongoing',
              startTime: '2026-04-01T00:00',
              endTime: '2026-04-30T23:59',
              answered: true,
              questions: 5
            }
          ]
        })
      }, 300)
    })
  },

  // 获取问卷详情
  getSurveyDetail: (id) => {
    // 模拟数据
    return new Promise(resolve => {
      setTimeout(() => {
        resolve({
          code: 200,
          data: {
            id: 1,
            title: '2024年员工安全培训考试',
            description: '请认真完成以下安全培训考试题目',
            timeLimit: 30,
            questions: [
              {
                id: 1,
                type: 'single',
                text: '以下哪个是正确的手部卫生步骤？',
                required: true,
                options: [
                  { id: 1, text: '使用冷水冲洗' },
                  { id: 2, text: '使用肥皂搓洗至少20秒' },
                  { id: 3, text: '仅使用免洗洗手液' }
                ]
              },
              {
                id: 2,
                type: 'multiple',
                text: '以下哪些属于火灾逃生的正确做法？',
                required: true,
                options: [
                  { id: 1, text: '用湿毛巾捂住口鼻' },
                  { id: 2, text: '乘坐电梯快速下楼' },
                  { id: 3, text: '弯腰低姿前进' },
                  { id: 4, text: '从窗户直接跳下' }
                ]
              },
              {
                id: 3,
                type: 'single',
                text: '电器着火时，应首先使用水基灭火器扑救。',
                required: true,
                options: [
                  { id: 1, text: '正确' },
                  { id: 2, text: '错误' }
                ]
              }
            ]
          }
        })
      }, 300)
    })
  },

  // 提交答案
  submitAnswer: (surveyId, answers) => {
    // 模拟数据
    return new Promise(resolve => {
      setTimeout(() => {
        resolve({
          code: 200,
          data: {
            success: true,
            message: '提交成功'
          }
        })
      }, 800)
    })
  },

  // 获取答题结果
  getAnswerResult: (surveyId) => {
    // 模拟数据
    return new Promise(resolve => {
      setTimeout(() => {
        resolve({
          code: 200,
          data: {
            surveyId: 1,
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