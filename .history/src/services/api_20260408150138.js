import axios from 'axios';

const apiClient = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
});

export const surveyApi = {
  create: (surveyDTO) => apiClient.post('/survey', surveyDTO),
  update: (surveyDTO) => apiClient.put('/survey', surveyDTO),
  delete: (id) => apiClient.delete(`/survey/${id}`),
  getById: (id) => apiClient.get(`/survey/${id}`),
  getList: () => apiClient.get('/survey'),
  publish: (id) => apiClient.put(`/survey/${id}/publish`)
};

export const questionApi = {
  create: (questionDTO) => apiClient.post('/question', questionDTO),
  update: (questionDTO) => apiClient.put('/question', questionDTO),
  delete: (id) => apiClient.delete(`/question/${id}`),
  getById: (id) => apiClient.get(`/question/${id}`),
  getBySurveyId: (surveyId) => apiClient.get(`/question/survey/${surveyId}`)
};

export const answerApi = {
  submit: (answerSubmitDTO) => apiClient.post('/answer', answerSubmitDTO),
  getBySurveyId: (surveyId) => apiClient.get(`/answer/survey/${surveyId}`),
  getCountBySurveyId: (surveyId) => apiClient.get(`/answer/count/${surveyId}`)
};