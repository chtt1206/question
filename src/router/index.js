import { createRouter, createWebHashHistory } from 'vue-router';
import SurveyList from '../views/SurveyList.vue';
import SurveyDetail from '../views/SurveyDetail.vue';
import QuestionList from '../views/QuestionList.vue';
import AnswerStatistics from '../views/AnswerStatistics.vue';

const routes = [
  {
    path: '/',
    name: 'SurveyList',
    component: SurveyList
  },
  {
    path: '/survey/:id',
    name: 'SurveyDetail',
    component: SurveyDetail
  },
  {
    path: '/survey/:surveyId/questions',
    name: 'QuestionList',
    component: QuestionList
  },
  {
    path: '/survey/:surveyId/answers',
    name: 'AnswerStatistics',
    component: AnswerStatistics
  }
];

const router = createRouter({
  history: createWebHashHistory(),
  routes
});

export default router;