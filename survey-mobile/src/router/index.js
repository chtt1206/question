import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'SurveyList',
      component: () => import('../views/SurveyList.vue')
    },
    {
      path: '/survey/:id',
      name: 'SurveyDetail',
      component: () => import('../views/SurveyDetail.vue')
    },
    {
      path: '/result/:id',
      name: 'SurveyResult',
      component: () => import('../views/SurveyResult.vue')
    }
  ]
})

export default router