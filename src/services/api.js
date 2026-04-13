import axios from 'axios';

const apiClient = axios.create({
  baseURL: 'http://localhost:8082/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
});

export const surveyApi = {
  create: async (surveyDTO) => {
    const response = await apiClient.post('/survey', surveyDTO);
    return response.data;
  },
  update: async (surveyDTO) => {
    const response = await apiClient.put('/survey', surveyDTO);
    return response.data;
  },
  delete: async (id) => {
    const response = await apiClient.delete(`/survey/${id}`);
    return response.data;
  },
  getById: async (id) => {
    const response = await apiClient.get(`/survey/${id}`);
    return response.data;
  },
  getList: async () => {
    const response = await apiClient.get('/survey');
    return response.data;
  },
  publish: async (id) => {
    const response = await apiClient.put(`/survey/${id}/publish`);
    return response.data;
  },
  unpublish: async (id) => {
    const response = await apiClient.put(`/survey/${id}/unpublish`);
    return response.data;
  },
  updateSettings: async (id, settingsDTO) => {
    const response = await apiClient.put(`/survey/${id}/settings`, settingsDTO);
    return response.data;
  }
};

export const questionApi = {
  create: async (questionDTO) => {
    const response = await apiClient.post('/question', questionDTO);
    return response.data;
  },
  update: async (questionDTO) => {
    const response = await apiClient.put('/question', questionDTO);
    return response.data;
  },
  delete: async (id) => {
    const response = await apiClient.delete(`/question/${id}`);
    return response.data;
  },
  getById: async (id) => {
    const response = await apiClient.get(`/question/${id}`);
    return response.data;
  },
  getBySurveyId: async (surveyId) => {
    const response = await apiClient.get(`/question/survey/${surveyId}`);
    return response.data;
  }
};

export const answerApi = {
  submit: async (answerSubmitDTO) => {
    const response = await apiClient.post('/answer', answerSubmitDTO);
    return response.data;
  },
  getBySurveyId: async (surveyId) => {
    const response = await apiClient.get(`/answer/survey/${surveyId}`);
    return response.data;
  },
  getCountBySurveyId: async (surveyId) => {
    const response = await apiClient.get(`/answer/count/${surveyId}`);
    return response.data;
  }
};

export const statisticsApi = {
  getSurveyStatistics: async (surveyId, page = 1, pageSize = 10) => {
    const response = await apiClient.get(`/statistics/survey/${surveyId}`, {
      params: { page, pageSize }
    });
    return response.data;
  },
  getQuestionStatistics: async (questionId) => {
    const response = await apiClient.get(`/statistics/question/${questionId}`);
    return response.data;
  },
  getUserRanking: async (surveyId) => {
    const response = await apiClient.get(`/statistics/ranking/${surveyId}`);
    return response.data;
  }
};