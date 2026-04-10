<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Button, RadioGroup, Radio, CheckboxGroup, Checkbox, Field, Progress, CountDown, showToast } from 'vant'
import { surveyApi } from '../services/api'

const router = useRouter()
const route = useRoute()
const surveyId = computed(() => route.params.id)

const survey = ref(null)
const loading = ref(true)
const currentQuestionIndex = ref(0)
const answers = ref({})
const timeRemaining = ref(30 * 60) // 30分钟
const timer = ref(null)
const showBasicInfo = ref(true) // 是否显示基础信息题
const startTime = ref(null) // 开始答题时间

// 计算基础信息题
const basicInfoQuestions = computed(() => {
  if (!survey.value) return []
  return survey.value.questions.filter(q => q.questionType === 'BASIC')
})

// 计算普通问卷题
const surveyQuestions = computed(() => {
  if (!survey.value) return []
  return survey.value.questions.filter(q => q.questionType !== 'BASIC')
})

// 计算当前题目
const currentQuestion = computed(() => {
  if (!survey.value || showBasicInfo.value) return null
  return surveyQuestions.value[currentQuestionIndex.value]
})

// 计算进度
const progress = computed(() => {
  if (!survey.value || showBasicInfo.value) return 0
  const questionCount = survey.value.questionCount || (survey.value.questions || []).filter(q => q.questionType !== 'BASIC').length
  return parseFloat(((currentQuestionIndex.value + 1) / (questionCount || 1) * 100).toFixed(2))
})

// 计算是否可以提交
const canSubmit = computed(() => {
  if (!survey.value) return false
  // 检查所有必答题是否已回答
  for (const question of survey.value.questions) {
    if (question.required && !answers.value[question.id]) {
      return false
    }
  }
  return true
})

// 计算是否可以进入问卷答题
const canStartSurvey = computed(() => {
  if (!survey.value) return false
  // 检查所有基础信息必答题是否已回答
  for (const question of basicInfoQuestions.value) {
    if (question.required && !answers.value[question.id]) {
      return false
    }
  }
  return true
})

// 计算是否可以进入下一题
const canNextQuestion = computed(() => {
  if (!currentQuestion.value) return false
  // 检查当前题目是否已回答
  if (currentQuestion.value.required && !answers.value[currentQuestion.value.id]) {
    return false
  }
  // 检查多选题是否至少选择了一个选项
  if (currentQuestion.value.type === 'multiple' && (!answers.value[currentQuestion.value.id] || answers.value[currentQuestion.value.id].length === 0)) {
    return false
  }
  return true
})

// 处理单选题答案
const handleSingleChoice = (questionId, optionId) => {
  answers.value[questionId] = optionId
}

// 处理多选题答案
const handleMultipleChoice = (questionId, optionIds) => {
  answers.value[questionId] = optionIds
}

// 处理输入题答案
const handleInputAnswer = (questionId, value) => {
  // 处理事件对象的情况
  if (value && typeof value === 'object' && 'target' in value) {
    answers.value[questionId] = value.target.value
  } else {
    answers.value[questionId] = value
  }
}

// 处理基础信息题答案
const handleBasicInfoAnswer = (questionId, value) => {
  // 处理事件对象的情况
  if (value && typeof value === 'object' && 'target' in value) {
    answers.value[questionId] = value.target.value
  } else {
    answers.value[questionId] = value
  }
}

// 下一题
const nextQuestion = () => {
  if (!survey.value) return
  const questionCount = survey.value.questionCount || (survey.value.questions || []).filter(q => q.questionType !== 'BASIC').length
  if (canNextQuestion.value && currentQuestionIndex.value < questionCount - 1) {
    currentQuestionIndex.value++
  } else if (!canNextQuestion.value) {
    showToast('请完成当前题目')
  }
}

// 上一题
const prevQuestion = () => {
  if (currentQuestionIndex.value > 0) {
    currentQuestionIndex.value--
  }
}

// 开始问卷答题
const startSurvey = () => {
  if (canStartSurvey.value) {
    showBasicInfo.value = false
    currentQuestionIndex.value = 0
    // 记录开始答题时间
    startTime.value = Date.now()
    // 开始倒计时
    if (survey.value && survey.value.timeLimit && survey.value.timeLimit > 0) {
      timeRemaining.value = survey.value.timeLimit * 60
      startCountdown()
    }
  } else {
    showToast('请完成所有基础信息题')
  }
}

// 提交问卷
const submitSurvey = async () => {
  if (!canSubmit.value) {
    showToast('请完成所有必答题')
    return
  }

  try {
    showToast({
      message: '提交中...',
      forbidClick: true,
      loading: true
    })
    // 计算答题时长（秒）
            const answerTime = startTime.value ? Math.round((Date.now() - startTime.value) / 1000) : 0
    const result = await surveyApi.submitAnswer(surveyId.value, answers.value, answerTime)
    // 保存 userId 到本地存储
    if (result && result.userId) {
      localStorage.setItem('survey_user_id_' + surveyId.value, result.userId)
    }
    showToast('提交成功！')
    router.push(`/result/${surveyId.value}`)
  } catch (error) {
    showToast('提交失败，请重试')
    console.error('提交答案失败:', error)
  } finally {
    // 清理
  }
}

// 倒计时
const startCountdown = () => {
  timer.value = setInterval(() => {
    if (timeRemaining.value > 0) {
      timeRemaining.value--
    } else {
      clearInterval(timer.value)
      showToast('时间到！自动提交')
      submitSurvey()
    }
  }, 1000)
}

// 格式化时间
const formatTime = (seconds) => {
  const minutes = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

// 加载数据
onMounted(async () => {
  try {
    const response = await surveyApi.getSurveyDetail(surveyId.value)
    // 后端直接返回数据，没有 code 字段
    if (response) {
      survey.value = response
      timeRemaining.value = response.timeLimit ? response.timeLimit * 60 : 0
      // 检查是否有基础信息题
      if (basicInfoQuestions.value.length === 0) {
        showBasicInfo.value = false
        // 如果没有基础信息题，直接开始倒计时
        if (response.timeLimit && response.timeLimit > 0) {
          startCountdown()
        }
      }
    } else {
      showToast('加载失败，请重试')
    }
  } catch (error) {
    showToast('加载失败，请重试')
    console.error('加载问卷详情失败:', error)
  } finally {
    loading.value = false
  }
})

// 清理定时器
onUnmounted(() => {
  if (timer.value) {
    clearInterval(timer.value)
  }
})
</script>

<template>
  <div class="survey-detail">
    <!-- 顶部导航栏 -->
    <div class="header">
      <div class="header-left" @click="router.back()">
        ← 返回
      </div>
      <div class="header-center">
        {{ showBasicInfo ? '基础信息' : '问卷答题' }}
      </div>
      <div class="header-right" v-if="!showBasicInfo && survey && survey.timeLimit && survey.timeLimit > 0">
        <van-count-down :time="timeRemaining * 1000" format="mm:ss" />
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 问卷内容 -->
    <div v-else-if="survey" class="survey-content">
      <!-- 问卷标题 -->
      <div class="survey-header">
        <h1 class="survey-title">{{ survey.title }}</h1>
        <p class="survey-description">{{ survey.description }}</p>
      </div>

      <!-- 基础信息题区域 -->
      <div v-if="showBasicInfo" class="basic-info-container">
        <h2 class="section-title">基础信息</h2>
        <div class="basic-info-form">
          <div 
            v-for="question in basicInfoQuestions" 
            :key="question.id"
            class="basic-info-item"
          >
            <div class="basic-info-label">
              {{ question.text }}
              <span v-if="question.required" class="required-mark">*</span>
            </div>
            <van-field
              v-model="answers[question.id]"
              :placeholder="question.placeholder"
              @change="(value) => handleBasicInfoAnswer(question.id, value)"
              class="basic-info-input"
            />
          </div>
        </div>

        <!-- 进入问卷答题按钮 -->
        <div class="start-survey-button">
          <van-button 
            type="primary"
            @click="startSurvey"
            :disabled="!canStartSurvey"
            class="start-button"
          >
            去问卷答题
          </van-button>
        </div>
      </div>

      <!-- 问卷答题区域 -->
      <div v-else class="survey-questions-container">
        <!-- 进度条 -->
        <div class="progress-container">
          <van-progress :percentage="progress" :stroke-width="8" />
          <div class="progress-text">
            {{ currentQuestionIndex + 1 }} / {{ survey ? (survey.questionCount || (survey.questions || []).filter(q => q.questionType !== 'BASIC').length) : 0 }}
          </div>
        </div>

        <!-- 题目区域 -->
        <div class="question-container">
          <div class="question-header">
            <span class="question-number">第 {{ currentQuestionIndex + 1 }} 题</span>
            <span v-if="currentQuestion.required" class="required-mark">*</span>
          </div>
          <h3 class="question-text">{{ currentQuestion.text }}</h3>

          <!-- 选项区域 -->
          <div class="options-container">
            <!-- 单选题 -->
            <van-radio-group
              v-if="currentQuestion.type === 'single'"
              v-model="answers[currentQuestion.id]"
              @change="(value) => handleSingleChoice(currentQuestion.id, value)"
            >
              <van-radio 
                v-for="option in currentQuestion.options" 
                :key="option.id" 
                :name="option.id"
                class="option-item"
              >
                {{ option.text }}
              </van-radio>
            </van-radio-group>

            <!-- 多选题 -->
            <van-checkbox-group
              v-else-if="currentQuestion.type === 'multiple'"
              v-model="answers[currentQuestion.id]"
              @change="(value) => handleMultipleChoice(currentQuestion.id, value)"
            >
              <van-checkbox 
                v-for="option in currentQuestion.options" 
                :key="option.id" 
                :name="option.id"
                class="option-item"
              >
                {{ option.text }}
              </van-checkbox>
            </van-checkbox-group>

            <!-- 输入题 -->
            <van-field
              v-else-if="currentQuestion.type === 'input'"
              v-model="answers[currentQuestion.id]"
              placeholder="请输入答案"
              @change="(value) => handleInputAnswer(currentQuestion.id, value)"
              class="input-answer"
            />
          </div>
        </div>

        <!-- 导航按钮 -->
        <div class="navigation-buttons">
          <van-button 
            @click="prevQuestion"
            :disabled="currentQuestionIndex === 0"
            class="nav-button"
          >
            上一题
          </van-button>
          <van-button 
            @click="nextQuestion"
            :disabled="currentQuestionIndex === (survey ? (survey.questionCount || (survey.questions || []).filter(q => q.questionType !== 'BASIC').length) : 0) - 1 || !canNextQuestion"
            class="nav-button"
          >
            下一题
          </van-button>
          <van-button 
            type="primary"
            @click="submitSurvey"
            :disabled="!canSubmit"
            class="submit-button"
          >
            提交
          </van-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.survey-detail {
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

.survey-content {
  padding: var(--spacing-lg);
}

.survey-header {
  margin-bottom: var(--spacing-xl);
  text-align: center;
}

.survey-title {
  font-size: var(--font-size-2xl);
  font-weight: 600;
  margin-bottom: var(--spacing-sm);
  color: var(--text-primary);
  line-height: 1.3;
  padding: 0 var(--spacing-md);
}

.survey-description {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  line-height: 1.5;
  padding: 0 var(--spacing-lg);
}

/* 基础信息题样式 */
.basic-info-container {
  background-color: var(--card-background);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
  box-shadow: var(--shadow-sm);
  margin-bottom: var(--spacing-xl);
}

.section-title {
  font-size: var(--font-size-xl);
  font-weight: 600;
  margin-bottom: var(--spacing-lg);
  color: var(--text-primary);
  text-align: center;
  padding-bottom: var(--spacing-md);
  border-bottom: 1px solid var(--border-color);
}

.basic-info-form {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.basic-info-item {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.basic-info-label {
  font-size: var(--font-size-base);
  font-weight: 500;
  color: var(--text-primary);
}

.basic-info-input {
  width: 100%;
  padding: var(--spacing-md);
  border: 2px solid var(--border-color);
  border-radius: var(--radius-md);
  font-size: var(--font-size-base);
  touch-action: manipulation;
  transition: all var(--transition-normal);
}

.basic-info-input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px var(--primary-light);
}

.start-survey-button {
  margin-top: var(--spacing-xl);
  display: flex;
  justify-content: center;
}

.start-button {
  width: 100%;
  max-width: 300px;
  height: 48px;
  border-radius: var(--radius-md);
  touch-action: manipulation;
  font-size: var(--font-size-base);
  font-weight: 500;
  transition: all var(--transition-normal);
}

/* 问卷答题区域样式 */
.survey-questions-container {
  width: 100%;
}

.progress-container {
  margin-bottom: var(--spacing-xl);
}

.progress-text {
  text-align: right;
  font-size: var(--font-size-xs);
  color: var(--text-secondary);
  margin-top: var(--spacing-xs);
  font-weight: 500;
}

.question-container {
  background-color: var(--card-background);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
  box-shadow: var(--shadow-sm);
  margin-bottom: var(--spacing-xl);
  transition: box-shadow var(--transition-normal);
}

.question-container:hover {
  box-shadow: var(--shadow-md);
}

.question-header {
  display: flex;
  align-items: center;
  margin-bottom: var(--spacing-md);
  padding-bottom: var(--spacing-sm);
  border-bottom: 1px solid var(--border-color);
}

.question-number {
  font-size: var(--font-size-sm);
  color: var(--primary-color);
  font-weight: 600;
  background-color: var(--primary-light);
  padding: 2px 8px;
  border-radius: var(--radius-full);
}

.required-mark {
  color: var(--danger-color);
  margin-left: var(--spacing-xs);
  font-size: var(--font-size-sm);
  font-weight: 600;
}

.question-text {
  font-size: var(--font-size-lg);
  font-weight: 700;
  margin-bottom: var(--spacing-lg);
  line-height: 1.6;
  color: var(--text-primary);
}

.options-container {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.option-item {
  padding: var(--spacing-md);
  border: none;
  border-radius: var(--radius-md);
  transition: all var(--transition-normal);
  touch-action: manipulation;
  background-color: var(--card-background);
}

.option-item:hover {
  background-color: var(--primary-light);
}

.option-item:active {
  transform: scale(0.98);
  background-color: var(--primary-light);
}

.input-answer {
  width: 100%;
  padding: var(--spacing-md);
  border: 2px solid var(--border-color);
  border-radius: var(--radius-md);
  font-size: var(--font-size-base);
  touch-action: manipulation;
  transition: all var(--transition-normal);
}

.input-answer:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px var(--primary-light);
}

.navigation-buttons {
  display: flex;
  gap: var(--spacing-sm);
  margin-top: var(--spacing-lg);
  padding-bottom: var(--spacing-lg);
  align-items: center;
  justify-content: space-between;
}

.nav-button {
  flex: 1;
  height: 48px;
  border-radius: var(--radius-md);
  touch-action: manipulation;
  font-size: var(--font-size-base);
  font-weight: 500;
  transition: all var(--transition-normal);
  min-width: 80px;
}

.submit-button {
  flex: 1.2;
  height: 48px;
  border-radius: var(--radius-md);
  touch-action: manipulation;
  font-size: var(--font-size-base);
  font-weight: 500;
  transition: all var(--transition-normal);
  min-width: 100px;
}

/* 自定义Vant组件样式 */
:deep(.van-progress__bar) {
  background-color: var(--primary-light);
}

:deep(.van-progress__portion) {
  background-color: var(--primary-color);
  border-radius: var(--radius-sm);
}

:deep(.van-radio__label) {
  font-size: var(--font-size-base);
  color: var(--text-primary);
}

:deep(.van-checkbox__label) {
  font-size: var(--font-size-base);
  color: var(--text-primary);
}

/* 确保 checkbox 显示为方形 */
:deep(.van-checkbox__icon .van-icon) {
  border-radius: 0 !important;
}

:deep(.van-checkbox__icon--checked .van-icon) {
  border-radius: 0 !important;
}

:deep(.van-field__control) {
  font-size: var(--font-size-base);
  color: var(--text-primary);
}

:deep(.van-field__placeholder) {
  color: var(--text-tertiary);
}

:deep(.van-button--primary) {
  background-color: var(--primary-color);
  border-color: var(--primary-color);
  border-radius: var(--radius-md);
}

:deep(.van-button--primary:hover) {
  background-color: var(--primary-dark);
  border-color: var(--primary-dark);
}

:deep(.van-button--default) {
  border-radius: var(--radius-md);
  border-color: var(--border-color);
}

:deep(.van-button--default:hover) {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .survey-content {
    padding: var(--spacing-md);
  }
  
  .basic-info-container,
  .question-container {
    padding: var(--spacing-md);
    margin-bottom: var(--spacing-lg);
  }
  
  .survey-title {
    font-size: var(--font-size-xl);
  }
  
  .section-title {
    font-size: var(--font-size-lg);
  }
  
  .question-text {
    font-size: var(--font-size-base);
  }
  
  .navigation-buttons {
    gap: var(--spacing-xs);
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
  
  .survey-content {
    padding: var(--spacing-sm);
  }
  
  .basic-info-container,
  .question-container {
    padding: var(--spacing-sm);
    margin-bottom: var(--spacing-md);
  }
  
  .navigation-buttons {
    flex-direction: row;
    gap: var(--spacing-xs);
    flex-wrap: nowrap;
  }
  
  .nav-button {
    flex: 1;
    height: 44px;
    font-size: var(--font-size-sm);
    min-width: 70px;
  }
  
  .submit-button {
    flex: 1.2;
    height: 44px;
    font-size: var(--font-size-sm);
    min-width: 90px;
  }
  
  .start-button {
    width: 100%;
    height: 44px;
  }
  
  .survey-title {
    font-size: var(--font-size-lg);
  }
  
  .section-title {
    font-size: var(--font-size-base);
  }
  
  .question-text {
    font-size: var(--font-size-sm);
  }
  
  .option-item {
    padding: var(--spacing-sm);
  }
  
  .input-answer,
  .basic-info-input {
    padding: var(--spacing-sm);
  }
}
</style>