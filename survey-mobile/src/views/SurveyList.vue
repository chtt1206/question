<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, List, Cell, Badge, Empty } from 'vant'
import { showToast } from 'vant'
import dayjs from 'dayjs'
import { surveyApi } from '../services/api'

const router = useRouter()
const searchText = ref('')
const surveys = ref([])
const loading = ref(true)

// 过滤后的问卷列表
const filteredSurveys = ref([])

// 搜索功能
const handleSearch = (value) => {
  searchText.value = value
  filterSurveys()
}

// 过滤问卷
const filterSurveys = () => {
  if (searchText.value) {
    filteredSurveys.value = surveys.value.filter(survey => 
      survey.title.toLowerCase().includes(searchText.value.toLowerCase())
    )
  } else {
    filteredSurveys.value = [...surveys.value]
  }
}

// 进入问卷详情
const goToSurvey = (survey) => {
  if (survey.status === 'PUBLISHED') {
    router.push(`/survey/${survey.id}`)
  } else if (survey.status === 'DRAFT') {
    showToast('问卷尚未发布')
  } else {
    showToast('问卷不可用')
  }
}

// 获取问卷状态文本和样式
const getStatusInfo = (status) => {
  switch (status) {
    case 'PUBLISHED':
      return { text: '进行中', color: '#07c160' }
    case 'DRAFT':
      return { text: '未发布', color: '#ee0a24' }
    default:
      return { text: '未知', color: '#ff976a' }
  }
}

// 格式化时间
const formatTime = (time) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

// 加载数据
onMounted(async () => {
  try {
    const response = await surveyApi.getSurveyList()
    // 后端直接返回数据，没有 code 字段
    if (Array.isArray(response)) {
      // 确保每个问卷都有 questions 字段
      surveys.value = response.map(survey => ({
        ...survey,
        questions: survey.questions || []
      }))
      filteredSurveys.value = [...surveys.value]
    }
  } catch (error) {
    showToast('加载失败，请重试')
    console.error('加载问卷列表失败:', error)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="survey-list">
    <!-- 搜索栏 -->
    <div class="search-container">
      <van-search 
        v-model="searchText" 
        placeholder="搜索问卷" 
        @search="handleSearch"
        @submit="handleSearch"
        shape="round"
        background="#fff"
      />
    </div>

    <!-- 问卷列表 -->
    <div class="list-container">
      <div v-if="!loading && filteredSurveys.length > 0">
        <van-cell-group>
          <van-cell 
            v-for="survey in filteredSurveys" 
            :key="survey.id"
            @click="goToSurvey(survey)"
            class="survey-item"
          >
            <template #default>
              <div class="survey-info">
                <h3 class="survey-title">{{ survey.title }}</h3>
                <p class="survey-meta">
                  <span class="question-count">{{ survey.questionCount || (survey.questions || []).filter(q => q.questionType !== 'BASIC').length }}题</span>
                  <span class="start-time">{{ formatTime(survey.startTime) }}</span>
                </p>
                <p class="survey-end-time">截止时间：{{ formatTime(survey.endTime) }}</p>
                <div class="survey-status">
                  <div :class="['status-tag', survey.status.toLowerCase()]">{{ getStatusInfo(survey.status).text }}</div>
                </div>
              </div>
            </template>
          </van-cell>
        </van-cell-group>
      </div>

      <!-- 空状态 -->
      <van-empty 
        v-if="!loading && filteredSurveys.length === 0"
        description="暂无问卷"
      />

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.survey-list {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: var(--spacing-lg);
}

.search-container {
  background-color: #ffffff;
  padding: var(--spacing-sm);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 10;
  transition: box-shadow var(--transition-normal);
}

.search-container.scrolled {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.list-container {
  margin-top: var(--spacing-sm);
  padding: 0 var(--spacing-sm);
}

.survey-item {
  margin-bottom: var(--spacing-sm);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  background-color: #ffffff;
  border: 1px solid #f0f0f0;
}

.survey-item:active {
  transform: scale(0.98);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

.survey-item:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
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
  border: 3px solid #f3f3f3;
  border-top: 3px solid var(--primary-color);
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

/* 自定义Vant组件样式 */
:deep(.van-search__content) {
  border-radius: var(--radius-full);
  background-color: #f5f5f5;
  height: 40px;
}

:deep(.van-search__action) {
  color: var(--primary-color);
  font-size: var(--font-size-sm);
}

:deep(.van-cell) {
  border-radius: var(--radius-lg);
  overflow: hidden;
  padding: var(--spacing-md);
  margin-bottom: var(--spacing-sm);
}

:deep(.van-empty__description) {
  color: var(--text-tertiary);
  font-size: var(--font-size-sm);
}

:deep(.van-empty__image) {
  width: 120px;
  height: 120px;
}

/* 问卷信息样式 */
.survey-info {
  position: relative;
  width: 100%;
  text-align: left;
}

.survey-title {
  font-weight: 600;
  color: var(--text-primary);
  font-size: var(--font-size-base);
  line-height: 1.4;
  margin-bottom: var(--spacing-sm);
  padding-right: 80px;
}

.survey-meta {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-xs);
}

.question-count {
  color: var(--primary-color);
  font-size: var(--font-size-sm);
  font-weight: 500;
  background-color: rgba(59, 130, 246, 0.1);
  padding: 2px 8px;
  border-radius: var(--radius-full);
}

.start-time {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
}

.survey-end-time {
  color: var(--text-tertiary);
  font-size: var(--font-size-xs);
  margin-bottom: 0;
}

.survey-status {
  position: absolute;
  top: 0;
  right: 0;
}

/* 状态标签样式 */
.status-tag {
  font-size: var(--font-size-xs);
  padding: 4px 12px;
  border-radius: var(--radius-full);
  text-align: center;
  font-weight: 500;
  transition: all 0.3s ease;
}

.status-tag.published {
  background-color: #e6f7ee;
  color: #07c160;
}

.status-tag.draft {
  background-color: #fff1f0;
  color: #ee0a24;
}

.status-tag.unknown {
  background-color: #fff7e6;
  color: #ff976a;
}

/* 已作答标签 */
.answered-badge {
  background-color: #e6f7ff;
  color: #1890ff;
  font-size: var(--font-size-xs);
  padding: 4px 12px;
  border-radius: var(--radius-full);
  text-align: center;
  font-weight: 500;
  transition: all 0.3s ease;
  margin-top: var(--spacing-xs);
}

/* 响应式调整 */
@media (max-width: 375px) {
  .search-container {
    padding: var(--spacing-xs);
  }
  
  .list-container {
    padding: 0 var(--spacing-xs);
  }
  
  .survey-item {
    margin-bottom: var(--spacing-xs);
  }
  
  :deep(.van-cell) {
    padding: var(--spacing-sm);
  }
  
  .survey-title {
    font-size: var(--font-size-sm);
    padding-right: 70px;
  }
  
  .survey-meta {
    gap: var(--spacing-sm);
  }
  
  .question-count {
    font-size: 10px;
    padding: 2px 6px;
  }
  
  .start-time {
    font-size: 10px;
  }
  
  .survey-end-time {
    font-size: 9px;
  }
  
  .status-tag {
    font-size: 10px;
    padding: 3px 8px;
  }
  
  :deep(.van-search__content) {
    height: 36px;
  }
}
</style>