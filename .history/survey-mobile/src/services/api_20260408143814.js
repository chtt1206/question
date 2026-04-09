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
            timeLimit: 30