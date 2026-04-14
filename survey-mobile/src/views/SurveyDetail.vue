<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Button, RadioGroup, Radio, CheckboxGroup, Checkbox, Field, showToast } from 'vant'
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
const handleMultipleChoice = (questionId, optionId) => {
  if (!answers.value[questionId]) {
    answers.value[questionId] = []
  }
  const index = answers.value[questionId].indexOf(optionId)
  if (index === -1) {
    answers.value[questionId].push(optionId)
  } else {
    answers.value[questionId].splice(index, 1)
  }
}

// 处理输入题答案
const handleInputAnswer = (questionId, e) => {
  answers.value[questionId] = e.target.value
}

// 处理基础信息题答案
const handleBasicInfoAnswer = (questionId, e) => {
  answers.value[questionId] = e.target.value
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
const submitSurvey = async (isAutoSubmit = false) => {
  if (!isAutoSubmit && !canSubmit.value) {
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
      submitSurvey(true) // 自动提交，不需要校验
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
      timeRemaining.value = response.timeLimit ? Math.round(response.timeLimit * 60) : 0
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

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 问卷内容 -->
    <div v-else-if="survey" class="survey-content">
      <!-- 基础信息题区域 -->
      <div v-if="showBasicInfo" class="basic-info-container">
        <div class="survey-header">
          <div class="survey-title">{{ survey.title }}</div>
          <div class="survey-meta">
            <span>基础信息填写</span>
            <span>⏱️ 预计1分钟</span>
          </div>
        </div>
        <div class="basic-info-form">
          <div 
            v-for="question in basicInfoQuestions" 
            :key="question.id"
            class="basic-info-item"
          >
            <div class="basic-info-label">
              {{ question.text }}
              <span v-if="question.required" class="required-badge">*</span>
            </div>
            <textarea 
              v-model="answers[question.id]"
              :placeholder="question.placeholder || '请输入'"
              @input="(e) => handleBasicInfoAnswer(question.id, e)"
              class="text-input"
              rows="3"
            ></textarea>
          </div>
        </div>

        <!-- 进入问卷答题按钮 -->
        <div class="start-survey-button">
          <button 
            @click="startSurvey"
            :disabled="!canStartSurvey"
            :class="['btn', 'btn-primary', { 'btn-disabled': !canStartSurvey }]"
          >
            去问卷答题
          </button>
        </div>
      </div>

      <!-- 问卷答题区域 -->
      <div v-else class="survey-questions-container">
        <!-- 问卷头部 -->
        <div class="survey-header">
          <div class="survey-title">{{ survey.title }}</div>
          <div class="survey-meta">
            <span>第 {{ currentQuestionIndex + 1 }} / {{ survey ? (survey.questionCount || (survey.questions || []).filter(q => q.questionType !== 'BASIC').length) : 0 }} 题</span>
            <span>⏱️ 预计2分钟</span>
          </div>
          <div class="progress-section">
            <div class="progress-bar-container">
              <div class="progress-fill" :style="{ width: progress + '%' }"></div>
            </div>
            <div class="progress-text">已完成 {{ Math.round(progress) }}%</div>
          </div>
        </div>

        <!-- 题目区域 -->
        <div class="question-area">
          <div class="question-card">
            <div class="question-text">
              {{ currentQuestion.text }}
              <span v-if="currentQuestion.required" class="required-badge">*</span>
            </div>

            <!-- 选项区域 -->
            <div class="options-container">
              <!-- 单选题 -->
              <div 
                v-if="currentQuestion.type === 'single'"
                class="options-group"
              >
                <div 
                  v-for="option in currentQuestion.options" 
                  :key="option.id"
                  :class="['option-item', { 'active': answers[currentQuestion.id] === option.id }]"
                  @click="handleSingleChoice(currentQuestion.id, option.id)"
                >
                  <div class="radio-custom"></div>
                  <div class="option-label">{{ option.text }}</div>
                </div>
              </div>

              <!-- 多选题 -->
              <div 
                v-else-if="currentQuestion.type === 'multiple'"
                class="options-group"
              >
                <div 
                  v-for="option in currentQuestion.options" 
                  :key="option.id"
                  :class="['option-item', { 'active': answers[currentQuestion.id] && answers[currentQuestion.id].includes(option.id) }]"
                  @click="handleMultipleChoice(currentQuestion.id, option.id)"
                >
                  <div class="checkbox-custom"></div>
                  <div class="option-label">{{ option.text }}</div>
                </div>
              </div>

              <!-- 输入题 -->
              <textarea
                v-else-if="currentQuestion.type === 'input'"
                v-model="answers[currentQuestion.id]"
                placeholder="请输入答案"
                @input="(e) => handleInputAnswer(currentQuestion.id, e)"
                class="text-input"
                rows="3"
              ></textarea>
            </div>
          </div>
        </div>

        <!-- 导航按钮 -->
        <div class="footer-buttons">
          <button 
            @click="prevQuestion"
            :disabled="currentQuestionIndex === 0"
            :class="['btn', 'btn-secondary', { 'btn-disabled': currentQuestionIndex === 0 }]"
          >
            上一题
          </button>
          <button 
            @click="currentQuestionIndex === (survey ? (survey.questionCount || (survey.questions || []).filter(q => q.questionType !== 'BASIC').length) : 0) - 1 ? submitSurvey() : nextQuestion()"
            :disabled="!canNextQuestion"
            :class="['btn', 'btn-primary', { 'btn-disabled': !canNextQuestion }]"
          >
            {{ currentQuestionIndex === (survey ? (survey.questionCount || (survey.questions || []).filter(q => q.questionType !== 'BASIC').length) : 0) - 1 ? '提交问卷' : '下一题' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.survey-detail {
  min-height: 100vh;
  background-color: #F5F7FB;
  display: flex;
  flex-direction: column;
}


.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  min-height: 60vh;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #1A73E8;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

.loading-container p {
  color: #6C7A8E;
  font-size: 14px;
  font-weight: 500;
  margin: 0;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.survey-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

/* 问卷头部 */
.survey-header {
  padding: 12px 20px 8px 20px;
  background: white;
  border-bottom: 1px solid #EFF2F6;
}

.survey-title {
  font-size: 20px;
  font-weight: 700;
  color: #0B2B4A;
  margin-bottom: 4px;
}

.survey-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #6C7A8E;
  margin-top: 6px;
}

/* 进度条 */
.progress-section {
  margin-top: 12px;
  margin-bottom: 4px;
}

.progress-bar-container {
  background-color: #EDF0F4;
  border-radius: 12px;
  height: 6px;
  width: 100%;
  overflow: hidden;
}

.progress-fill {
  background-color: #1A73E8;
  height: 100%;
  width: 0%;
  border-radius: 12px;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 12px;
  color: #5C6A7E;
  margin-top: 6px;
  text-align: right;
}

/* 基础信息题样式 */
.basic-info-container {
  flex: 1;
  padding: 20px;
  background: white;
}

.basic-info-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-top: 20px;
}

.basic-info-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.basic-info-label {
  font-size: 16px;
  font-weight: 500;
  color: #1F2A3E;
}

/* 题目区域 */
.question-area {
  flex: 1;
  overflow-y: auto;
  padding: 20px 20px 16px 20px;
  scroll-behavior: smooth;
}

.question-area::-webkit-scrollbar {
  width: 4px;
}

.question-area::-webkit-scrollbar-track {
  background: #F0F2F5;
}

.question-area::-webkit-scrollbar-thumb {
  background: #C1C9D2;
  border-radius: 4px;
}

/* 题目卡片 */
.question-card {
  background: #FFFFFF;
  border-radius: 24px;
  padding: 20px;
  margin-bottom: 24px;
  animation: fadeSlideUp 0.3s ease;
}

@keyframes fadeSlideUp {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.question-text {
  font-size: 18px;
  font-weight: 600;
  color: #1F2A3E;
  line-height: 1.4;
  margin-bottom: 20px;
  padding-right: 8px;
}

.required-badge {
  color: #E54545;
  font-size: 16px;
  margin-left: 4px;
}

/* 选项样式 */
.options-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.options-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.option-item {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  background-color: #F8F9FC;
  border-radius: 16px;
  border: 1px solid #EDF0F4;
  transition: all 0.2s;
  cursor: pointer;
}

.option-item.active {
  background-color: #E9F4FF;
  border-color: #1A73E8;
}

.radio-custom, .checkbox-custom {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  border: 2px solid #C5CED9;
  margin-right: 14px;
  position: relative;
  background: white;
  transition: all 0.1s;
  flex-shrink: 0;
}

.checkbox-custom {
  border-radius: 6px;
}

.option-item.active .radio-custom {
  border-color: #1A73E8;
  background-color: #1A73E8;
  box-shadow: inset 0 0 0 4px white;
}

.option-item.active .checkbox-custom {
  border-color: #1A73E8;
  background-color: #1A73E8;
  position: relative;
}

.option-item.active .checkbox-custom::after {
  content: "✓";
  color: white;
  font-size: 14px;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-weight: bold;
}

.option-label {
  font-size: 16px;
  color: #1F2A3E;
  line-height: 1.3;
  flex: 1;
}

/* 文本输入框 */
.text-input {
  width: 100%;
  padding: 14px 16px;
  font-size: 16px;
  font-family: inherit;
  border: 1px solid #E2E8F0;
  border-radius: 16px;
  background-color: #F8F9FC;
  transition: all 0.2s;
  resize: vertical;
  outline: none;
}

.text-input:focus {
  border-color: #1A73E8;
  background-color: white;
  box-shadow: 0 0 0 3px rgba(26,115,232,0.1);
}

/* 按钮样式 */
.start-survey-button {
  margin-top: 32px;
  display: flex;
  justify-content: center;
}

.footer-buttons {
  padding: 16px 20px 24px;
  background-color: white;
  border-top: 0.5px solid #EFF2F6;
  display: flex;
  gap: 12px;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.02);
}

.btn {
  flex: 1;
  text-align: center;
  padding: 14px 0;
  border-radius: 40px;
  font-weight: 600;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
  background: none;
}

.btn-primary {
  background-color: #1A73E8;
  color: white;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.btn-primary:active {
  background-color: #0F5BB5;
  transform: scale(0.97);
}

.btn-secondary {
  background-color: #F0F2F5;
  color: #2C3E50;
}

.btn-secondary:active {
  background-color: #E4E8ED;
  transform: scale(0.97);
}

.btn-disabled {
  opacity: 0.5;
  pointer-events: none;
}

/* 响应式调整 */
@media (max-width: 375px) {
  .survey-header {
    padding: 10px 16px 8px 16px;
  }
  
  .survey-title {
    font-size: 18px;
  }
  
  .survey-meta {
    font-size: 12px;
  }
  
  .basic-info-container {
    padding: 16px;
  }
  
  .basic-info-form {
    gap: 16px;
    margin-top: 16px;
  }
  
  .question-area {
    padding: 16px 16px 12px 16px;
  }
  
  .question-card {
    padding: 16px;
    margin-bottom: 20px;
  }
  
  .question-text {
    font-size: 16px;
    margin-bottom: 16px;
  }
  
  .option-item {
    padding: 12px 14px;
  }
  
  .option-label {
    font-size: 15px;
  }
  
  .text-input {
    padding: 12px 14px;
    font-size: 15px;
  }
  
  .footer-buttons {
    padding: 14px 16px 20px;
    gap: 10px;
  }
  
  .btn {
    padding: 12px 0;
    font-size: 15px;
  }
  
  .start-survey-button {
    margin-top: 24px;
  }
}
</style>