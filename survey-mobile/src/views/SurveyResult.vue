<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Button, Empty, List, Cell, Badge, showToast } from 'vant'
import { surveyApi } from '../services/api'

const router = useRouter()
const route = useRoute()
const surveyId = route.params.id

const result = ref(null)
const loading = ref(true)

// 加载结果数据
onMounted(async () => {
  try {
    // 从本地存储获取 userId
    const userId = localStorage.getItem('survey_user_id_' + surveyId)
    if (!userId) {
      showToast('未找到答题记录')
      loading.value = false
      return
    }
    const response = await surveyApi.getAnswerResult(surveyId, userId)
    // 后端直接返回数据，没有 code 字段
    if (response) {
      console.log('加载到的结果:', response)

      result.value = response
    } else {
      showToast('加载失败，请重试')
    }
  } catch (error) {
    showToast('加载失败，请重试')
    console.error('加载答题结果失败:', error)
  } finally {
    loading.value = false
  }
})

// 返回问卷列表
const goBackToList = () => {
  router.push('/')
}

// 重新答题
const reTakeSurvey = () => {
  router.push(`/survey/${surveyId}`)
}
</script>

<template>
  <div class="survey-result">
    <!-- 顶部导航栏 -->
    <div class="header">
      <div class="header-left" @click="goBackToList">
        ← 返回
      </div>
      <div class="header-center">
        答题结果
      </div>
      <div class="header-right">
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 结果内容 -->
    <div v-else-if="result" class="result-content">
      <!-- 结果概览 -->
      <div class="result-overview">
        <div class="result-content">
          <div v-if="result.passingScore">
            <div class="result-icon" :class="{ 'success': result.score >= result.passingScore, 'error': result.score < result.passingScore }">
              {{ result.score >= result.passingScore ? '✓' : '✗' }}
            </div>
            <h2 class="result-title">{{ result.score >= result.passingScore ? '恭喜你通过了考试！' : '很遗憾，未能通过考试' }}</h2>
            <p class="result-description">通过分数：{{ result.passingScore }}分</p>
            <p class="result-description">得分：{{ result.score }}/{{ result.totalScore }}分</p>
          </div>
          <div class="result-stats">
            <div class="stat-item">
              <span class="stat-value">{{ result.correctCount }}/{{ result.totalQuestions }}</span>
              <span class="stat-label">答对题数</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ Math.floor(result.timeUsed / 60) }}:{{ (result.timeUsed % 60).toString().padStart(2, '0') }}</span>
              <span class="stat-label">用时</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ result.rank }}/{{ result.totalParticipants }}</span>
              <span class="stat-label">排名</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 答题详情 -->
      <div class="answer-details">
        <h3 class="section-title">答题详情</h3>
        <van-cell-group>
          <template v-for="(answer, index) in result.answers" :key="answer.questionId">
          <van-cell  class="answer-item" >
            <template #title>
              <div class="question-title">
                <span class="question-text">第{{ index + 1 }}题：{{ answer.questionText }}</span>
                <span
                  :class="['status-tag', answer.isCorrect ? 'correct' : 'wrong']"
                >
                  {{ answer.isCorrect ? '正确' : '错误' }}
                </span>
              </div>
            </template>
            </van-cell>
            <van-cell >
            <template #default>
              <div class="answer-content">
                <p v-if="!answer.isCorrect" class="user-answer">你的答案：{{ answer.userAnswer }}</p>
                <p v-if="!answer.isCorrect && answer.correctAnswer" class="correct-answer">
                  正确答案：{{ answer.correctAnswer }}
                </p>
                <p v-if="!answer.isCorrect && answer.answerExplanation" class="answer-explanation">
                  💡 答案解析：{{ answer.answerExplanation }}
                </p>
              </div>
            </template>
          </van-cell>
            </template>
        </van-cell-group>
      </div>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <van-button
          @click="goBackToList"
          class="action-button"
        >
          返回问卷列表
        </van-button>
        <van-button
          type="primary"
          @click="reTakeSurvey"
          class="action-button"
        >
          重新答题
        </van-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.survey-result {
  min-height: 100vh;
  background-color: var(--background-color);
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: var(--primary-color);
  color: white;
  padding: var(--spacing-md);
  font-size: var(--font-size-base);
  font-weight: 600;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: var(--shadow-md);
  transition: all var(--transition-normal);
}

.header.scrolled {
  box-shadow: var(--shadow-lg);
}

.header-left {
  cursor: pointer;
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--radius-md);
  transition: background-color var(--transition-fast);
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
}

.header-left:active {
  background-color: rgba(255, 255, 255, 0.2);
}

.header-center {
  flex: 1;
  text-align: center;
  font-size: var(--font-size-lg);
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-2xl) 0;
  min-height: 60vh;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid var(--primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: var(--spacing-md);
}

.loading-container p {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.result-content {
  padding: var(--spacing-lg);
}

.result-overview {
  background-color: var(--card-background);
  border-radius: var(--radius-lg);
  padding: var(--spacing-xl) var(--spacing-lg);
  box-shadow: var(--shadow-sm);
  margin-bottom: var(--spacing-lg);
  text-align: center;
  transition: box-shadow var(--transition-normal);
  animation: slideUp 0.5s ease-out;
}

.result-overview:hover {
  box-shadow: var(--shadow-md);
}

.result-icon {
  width: 90px;
  height: 90px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  font-weight: bold;
  margin: 0 auto var(--spacing-lg);
  transition: all var(--transition-normal);
  box-shadow: var(--shadow-sm);
}

.result-icon:hover {
  transform: scale(1.05);
  box-shadow: var(--shadow-md);
}

.result-icon.success {
  background-color: var(--success-light);
  color: var(--success-color);
}

.result-icon.error {
  background-color: var(--danger-light);
  color: var(--danger-color);
}

.result-title {
  font-size: var(--font-size-2xl);
  font-weight: 600;
  margin-bottom: var(--spacing-sm);
  color: var(--text-primary);
  animation: fadeIn 0.6s ease-out;
}

.result-description {
  font-size: var(--font-size-base);
  color: var(--text-secondary);
  margin-bottom: var(--spacing-lg);
  font-weight: 500;
  animation: fadeIn 0.8s ease-out;
}

.result-stats {
  display: flex;
  justify-content: space-around;
  margin-top: var(--spacing-lg);
  padding-top: var(--spacing-lg);
  border-top: 1px solid var(--border-color);
  animation: slideUp 0.7s ease-out;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--spacing-sm);
  border-radius: var(--radius-md);
  transition: all var(--transition-normal);
}

.stat-item:hover {
  background-color: var(--primary-light);
  transform: translateY(-2px);
}

.stat-value {
  font-size: var(--font-size-xl);
  font-weight: 600;
  color: var(--primary-color);
  margin-bottom: var(--spacing-xs);
  font-family: 'Arial', sans-serif;
}

.stat-label {
  font-size: var(--font-size-xs);
  color: var(--text-secondary);
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.answer-details {
  background-color: var(--card-background);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
  box-shadow: var(--shadow-sm);
  margin-bottom: var(--spacing-lg);
  transition: box-shadow var(--transition-normal);
  animation: slideUp 0.9s ease-out;
}

.answer-details:hover {
  box-shadow: var(--shadow-md);
}

.section-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  margin-bottom: var(--spacing-md);
  color: var(--text-primary);
  padding-bottom: var(--spacing-sm);
  border-bottom: 2px solid var(--primary-light);
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.section-title::before {
  content: '';
  width: 4px;
  height: 20px;
  background-color: var(--primary-color);
  border-radius: var(--radius-sm);
}

.answer-item {
  padding: var(--spacing-md) 16px;
  transition: all var(--transition-normal);
  border-left: 4px solid transparent;
}
.answer-item:first-child {
    border-top: none;
  }
  .van-cell:after{
    display: none;
  }
.answer-item:hover {
  background-color: #f9f9f9;
  border-left-color: var(--primary-light);
  padding-left: var(--spacing-sm);
}

.answer-item:active {
  background-color: #f0f0f0;
}

.answer-item:last-child {
  border-bottom: none;
}

.answer-content {
  margin-top: var(--spacing-xs);
}

.user-answer {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin-bottom: var(--spacing-xs);
  line-height: 1.3;
}

.correct-answer {
  font-size: var(--font-size-sm);
  color: var(--success-color);
  font-weight: 500;
  line-height: 1.3;
}

.answer-explanation {
  font-size: var(--font-size-sm);
  color: var(--primary-color);
  font-weight: 500;
  line-height: 1.4;
  margin-top: var(--spacing-sm);
  padding: var(--spacing-sm);
  background-color: var(--primary-light);
  border-radius: var(--radius-sm);
}

.question-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: nowrap;
  gap: var(--spacing-xs);
}

.question-text {
  flex: 1;
  min-width: 0;
  word-break: break-word;
}

.status-tag {
  padding: 2px 8px;
  border-radius: var(--radius-full);
  font-size: var(--font-size-xs);
  font-weight: 600;
  white-space: nowrap;
}

.status-tag.correct {
  background-color: var(--success-light);
  color: var(--success-color);
}

.status-tag.wrong {
  background-color: var(--danger-light);
  color: var(--danger-color);
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
  margin-top: var(--spacing-lg);
  padding-bottom: var(--spacing-lg);
  animation: slideUp 1s ease-out;
}

.action-button {
  height: 48px;
  border-radius: var(--radius-md);
  font-size: var(--font-size-base);
  font-weight: 500;
  touch-action: manipulation;
  transition: all var(--transition-normal);
}

.action-button:active {
  transform: scale(0.98);
}

/* 自定义Vant组件样式 */
:deep(.van-cell) {
  border-radius: var(--radius-md);
  overflow: hidden;
}

:deep(.van-cell__title) {
  font-weight: 500;
  color: var(--text-primary);
  font-size: var(--font-size-base);
  line-height: 1.4;
}

:deep(.van-cell__label) {
  color: var(--text-tertiary);
  font-size: var(--font-size-sm);
  margin-top: 4px;
  line-height: 1.3;
}

:deep(.van-button--primary) {
  background-color: var(--primary-color);
  border-color: var(--primary-color);
  border-radius: var(--radius-md);
  font-weight: 500;
}

:deep(.van-button--primary:hover) {
  background-color: var(--primary-dark);
  border-color: var(--primary-dark);
}

:deep(.van-button--default) {
  border-radius: var(--radius-md);
  border-color: var(--border-color);
  font-weight: 500;
}

:deep(.van-button--default:hover) {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

/* 动画 */
@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* 响应式调整 */
@media (max-width: 768px) {
  .result-content {
    padding: var(--spacing-md);
  }

  .result-overview {
    padding: var(--spacing-lg) var(--spacing-md);
    margin-bottom: var(--spacing-md);
  }

  .answer-details {
    padding: var(--spacing-md);
    margin-bottom: var(--spacing-md);
  }

  .stat-value {
    font-size: var(--font-size-lg);
  }

  .result-icon {
    width: 80px;
    height: 80px;
    font-size: 40px;
  }
}

@media (max-width: 375px) {
  .header {
    padding: var(--spacing-sm);
    font-size: var(--font-size-sm);
  }

  .header-center {
    font-size: var(--font-size-base);
  }

  .result-content {
    padding: var(--spacing-sm);
  }

  .result-overview {
    padding: var(--spacing-md) var(--spacing-sm);
    margin-bottom: var(--spacing-sm);
  }

  .answer-details {
    padding: var(--spacing-sm);
    margin-bottom: var(--spacing-sm);
  }

  .action-buttons {
    gap: var(--spacing-xs);
  }

  .action-button {
    height: 44px;
    font-size: var(--font-size-sm);
  }

  .result-stats {
    margin-top: var(--spacing-md);
    padding-top: var(--spacing-md);
  }

  .stat-value {
    font-size: var(--font-size-base);
  }

  .stat-label {
    font-size: var(--font-size-xs);
  }

  .section-title {
    font-size: var(--font-size-base);
    margin-bottom: var(--spacing-sm);
  }

  .answer-item {
    border-top:1px solid var(--van-cell-border-color);
    padding: var(--spacing-sm) 16px;
  }
  .answer-item:first-child {
    border-top: none;
  }
  .van-cell:after{
    display: none;
  }
  .result-icon {
    width: 70px;
    height: 70px;
    font-size: 36px;
  }

  .result-title {
    font-size: var(--font-size-xl);
  }

  .result-description {
    font-size: var(--font-size-sm);
  }
}
</style>