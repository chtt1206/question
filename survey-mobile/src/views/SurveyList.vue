<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, List, Cell, Badge, Empty, Toast } from 'vant'
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
  if (survey.status === 'ongoing') {
    router.push(`/survey/${survey.id}`)
  } else if (survey.status === 'upcoming') {
    Toast.info('问卷尚未开始')
  } else if (survey.status === 'ended') {
    Toast.info('问卷已结束')
  }
}

// 获取问卷状态文本和样式
const getStatusInfo = (status) => {
  switch (status) {
    case 'ongoing':
      return { text: '进行中', type: 'success' }
    case 'upcoming':
      return { text: '未开始', type: 'warning' }
    case 'ended':
      return { text: '已结束', type: 'default' }
    default:
      return { text: '未知', type: 'default' }
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
    if (response.code === 200) {
      surveys.value = response.data
      filteredSurveys.value = [...response.data]
    }
  } catch (error) {
    Toast.info('加载失败，请重试')
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
            :title="survey.title"
            :value="`${survey.questions}题 | ${formatTime(survey.startTime)}`"
            :label="`截止时间：${formatTime(survey.endTime)}`"
            @click="goToSurvey(survey)"
            class="survey-item"
          >
            <template #right>
              <div class="cell-extra">
                <van-badge 
                  :type="getStatusInfo(survey.status).type"
                  :text="getStatusInfo(survey.status).text"
                />
                <div v-if="survey.answered" class="answered-tag">已作答</div>
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
  background-color: var(--background-color);
  padding-bottom: var(--spacing-lg);
}

.search-container {
  background-color: var(--card-background);
  padding: var(--spacing-sm);
  box-shadow: var(--shadow-sm);
  position: sticky;
  top: 0;
  z-index: 10;
  transition: box-shadow var(--transition-normal);
}

.search-container.scrolled {
  box-shadow: var(--shadow-md);
}

.list-container {
  margin-top: var(--spacing-sm);
  padding: 0 var(--spacing-sm);
}

.survey-item {
  margin-bottom: var(--spacing-sm);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-normal);
  background-color: var(--card-background);
}

.survey-item:active {
  transform: scale(0.98);
  box-shadow: var(--shadow-sm);
}

.survey-item:hover {
  box-shadow: var(--shadow-md);
}

.cell-extra {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: var(--spacing-xs);
}

.answered-tag {
  background-color: var(--primary-light);
  color: var(--primary-color);
  font-size: var(--font-size-xs);
  padding: 2px 8px;
  border-radius: var(--radius-full);
  text-align: center;
  font-weight: 500;
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
}

:deep(.van-cell) {
  border-radius: var(--radius-lg);
  overflow: hidden;
}

:deep(.van-cell__title) {
  font-weight: 600;
  color: var(--text-primary);
  font-size: var(--font-size-base);
}

:deep(.van-cell__value) {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
}

:deep(.van-cell__label) {
  color: var(--text-tertiary);
  font-size: var(--font-size-xs);
  margin-top: 4px;
}

:deep(.van-empty__description) {
  color: var(--text-tertiary);
  font-size: var(--font-size-sm);
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
  
  :deep(.van-cell__title) {
    font-size: var(--font-size-sm);
  }
  
  :deep(.van-cell__value) {
    font-size: var(--font-size-xs);
  }
}
</style>