<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Search, List, Cell, Badge, Empty, showToast } from 'vant'
import dayjs from 'dayjs'
import { surveyApi } from '../services/api'

const router = useRouter()
const searchText = ref('')
const surveys = ref([])
const loading = ref(true)
const currentTab = ref('all') // 'all', 'active', 'completed'

// 过滤后的问卷列表
const filteredSurveys = computed(() => {
  let result = surveys.value
  
  // 按状态过滤
  if (currentTab.value === 'active') {
    result = result.filter(survey => {
      const now = new Date().getTime()
      const start = new Date(survey.startTime).getTime()
      const end = new Date(survey.endTime).getTime()
      return survey.status === 'PUBLISHED' && now >= start && now <= end
    })
  } else if (currentTab.value === 'completed') {
    // 这里需要根据实际情况判断已完成的问卷，暂时使用一个简单的判断
    result = result.filter(survey => {
      const now = new Date().getTime()
      const end = new Date(survey.endTime).getTime()
      return survey.status === 'PUBLISHED' && now > end
    })
  }
  
  // 按搜索词过滤
  if (searchText.value) {
    const keyword = searchText.value.toLowerCase()
    result = result.filter(survey => 
      survey.title.toLowerCase().includes(keyword) || 
      (survey.description && survey.description.toLowerCase().includes(keyword))
    )
  }
  
  return result
})

// 搜索功能
const handleSearch = (value) => {
  searchText.value = value
}

// 切换Tab
const switchTab = (tab) => {
  currentTab.value = tab
}

// 进入问卷详情
const goToSurvey = (survey) => {
  if (survey.status === 'PUBLISHED') {
    const now = new Date().getTime()
    const start = new Date(survey.startTime).getTime()
    const end = new Date(survey.endTime).getTime()
    
    if (now < start) {
      showToast('问卷尚未开始')
    } else if (now > end) {
      showToast('问卷已结束')
    } else {
      router.push(`/survey/${survey.id}`)
    }
  } else if (survey.status === 'DRAFT') {
    showToast('问卷尚未发布')
  } else {
    showToast('问卷不可用')
  }
}

// 获取状态信息
const getStatusInfo = (status, startTime, endTime) => {
  if (status === 'DRAFT') {
    return { text: '未发布', className: 'draft' }
  } else if (status === 'PUBLISHED') {
    const now = new Date().getTime()
    const start = new Date(startTime).getTime()
    const end = new Date(endTime).getTime()
    
    if (now < start) {
      return { text: '未开始', className: 'not-started' }
    } else if (now >= start && now <= end) {
      return { text: '进行中', className: 'active' }
    } else {
      return { text: '已结束', className: 'expired' }
    }
  } else {
    return { text: '未知', className: '' }
  }
}

// 格式化时间
const formatTime = (time) => {
  return dayjs(time).format('YYYY-MM-DD')
}



// 获取答题截止时间
const getEndTime = (survey) => {
  return formatTime(survey.endTime)
}

// 处理操作按钮点击
const handleAction = (survey, action) => {
  if (action === 'start') {
    goToSurvey(survey)
  } else if (action === 'view') {
    // 查看结果
    showToast('查看结果功能待实现')
  }
}

// 处理筛选按钮点击
const handleFilter = () => {
  showToast('筛选功能待实现')
}

// 加载数据
onMounted(async () => {
  try {
    const response = await surveyApi.getSurveyList()
    // 后端直接返回数据，没有 code 字段
    if (Array.isArray(response)) {
      // 确保每个问卷都有必要的字段
      surveys.value = response.map(survey => ({
        ...survey,
        questions: survey.questions || [],
        description: survey.description || '暂无描述'
      }))
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
    <!-- 顶部导航栏 -->
    <div class="nav-header">
      <div class="title-section">
        <h1>问卷广场</h1>
        <p>参与调研，影响产品决策</p>
      </div>
      <div class="filter-icon" @click="handleFilter" role="button" aria-label="筛选">
        <span>⚡</span>
      </div>
    </div>

    <!-- 搜索框 -->
    <div class="search-container">
      <div class="search-box">
        <span>🔍</span>
        <input 
          type="text" 
          v-model="searchText"
          @input="handleSearch"
          placeholder="搜索问卷标题或关键词"
          autocomplete="off"
        />
      </div>
    </div>

    <!-- Tab 切换 -->
    <div class="tabs">
      <div 
        class="tab-item" 
        :class="{ active: currentTab === 'all' }"
        @click="switchTab('all')"
      >
        全部
      </div>
      <div 
        class="tab-item" 
        :class="{ active: currentTab === 'active' }"
        @click="switchTab('active')"
      >
        进行中
      </div>
      <div 
        class="tab-item" 
        :class="{ active: currentTab === 'completed' }"
        @click="switchTab('completed')"
      >
        已完成
      </div>
    </div>

    <!-- 问卷列表 -->
    <div class="questionnaire-list">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <!-- 空状态 -->
      <div v-else-if="filteredSurveys.length === 0" class="empty-state">
        <div class="empty-icon">📭</div>
        <div>暂无相关问卷</div>
        <div style="font-size:12px; margin-top:8px;">试试切换分类或关键词</div>
      </div>

      <!-- 问卷列表 -->
      <div v-else>
        <div 
          v-for="(survey, index) in filteredSurveys" 
          :key="survey.id"
          class="survey-card"
          :data-id="survey.id"
        >
          <div class="card-header">
            <div class="survey-title">{{ survey.title }}</div>
            <div :class="['status-badge', getStatusInfo(survey.status, survey.startTime, survey.endTime).className]">
              {{ getStatusInfo(survey.status, survey.startTime, survey.endTime).text }}
            </div>
          </div>
          <div class="survey-desc">{{ survey.description }}</div>
          <div class="meta-info">
            <div class="question-count">
              <span>📋</span> {{ survey.questionCount || (survey.questions || []).filter(q => q.questionType !== 'BASIC').length }} 个问题
            </div>
            <div class="end-time-info">
              <span class="reward-icon">⏰</span> 截止时间：{{ getEndTime(survey) }}
            </div>
          </div>
          
          <!-- 操作按钮 -->
          <div class="action-btn">
            <div 
              v-if="getStatusInfo(survey.status, survey.startTime, survey.endTime).text === '进行中'"
              class="btn-outline"
              @click="handleAction(survey, 'start')"
            >
              开始填写
            </div>
            <div 
              v-else-if="getStatusInfo(survey.status, survey.startTime, survey.endTime).text === '已结束'"
              class="btn-outline"
              style="color:#5C6A7E; border-color:#E0E5EA;"
              @click="handleAction(survey, 'view')"
            >
              查看结果
            </div>
            <div 
              v-else
              class="btn-outline"
              style="color:#9AA6B5; border-color:#E9ECF0;"
            >
              {{ getStatusInfo(survey.status, survey.startTime, survey.endTime).text }}
            </div>
          </div>
        </div>
        
        <!-- 列表底部 -->
        <div class="list-end">—— 已经到底了 ——</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.survey-list {
  min-height: 100vh;
  background-color: #F5F7FB;
  display: flex;
  flex-direction: column;
}

/* 导航栏 */
.nav-header {
  padding: 12px 20px;
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  background-color: #FFFFFF;
  border-bottom: 0.5px solid #E9ECF0;
}

.title-section h1 {
  font-size: 28px;
  font-weight: 700;
  color: #0B2B4A;
  letter-spacing: -0.3px;
  margin: 0;
}

.title-section p {
  font-size: 14px;
  color: #6C7A8E;
  margin-top: 4px;
  margin-bottom: 0;
}

.filter-icon {
  background-color: #F0F2F5;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 30px;
  cursor: pointer;
  transition: background 0.2s;
}

.filter-icon:hover {
  background-color: #E4E8ED;
}

/* 搜索框 */
.search-container {
  padding: 8px 20px 16px 20px;
  background-color: #FFFFFF;
}

.search-box {
  background-color: #F5F7FB;
  border-radius: 30px;
  padding: 12px 18px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #8E9AAB;
  font-size: 16px;
  border: 0.5px solid #E9ECF0;
}

.search-box span:first-child {
  font-size: 18px;
}

.search-box input {
  background: transparent;
  border: none;
  flex: 1;
  font-size: 16px;
  outline: none;
  font-family: inherit;
}

.search-box input::placeholder {
  color: #B2BBCA;
}

/* Tab 切换 */
.tabs {
  display: flex;
  padding: 0 20px 12px 20px;
  gap: 24px;
  background-color: #FFFFFF;
  border-bottom: 0.5px solid #EFF2F6;
}

.tab-item {
  font-size: 16px;
  font-weight: 500;
  color: #8E9AAB;
  padding-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.tab-item.active {
  color: #1A73E8;
  font-weight: 600;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 100%;
  height: 2.5px;
  background-color: #1A73E8;
  border-radius: 2px;
}

/* 问卷列表 */
.questionnaire-list {
  flex: 1;
  overflow-y: auto;
  padding: 4px 16px 20px 16px;
  scroll-behavior: smooth;
}

/* 自定义滚动条 */
.questionnaire-list::-webkit-scrollbar {
  width: 4px;
}

.questionnaire-list::-webkit-scrollbar-track {
  background: #F0F2F5;
  border-radius: 4px;
}

.questionnaire-list::-webkit-scrollbar-thumb {
  background: #C1C9D2;
  border-radius: 4px;
}

/* 问卷卡片 */
.survey-card {
  background-color: #FFFFFF;
  border-radius: 20px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02), 0 1px 2px rgba(0, 0, 0, 0.03);
  border: 1px solid #EDF0F4;
  transition: transform 0.1s ease, box-shadow 0.2s;
  cursor: pointer;
  animation: fadeIn 0.25s ease forwards;
}

.survey-card:active {
  transform: scale(0.99);
  background-color: #FEFEFE;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}

.survey-title {
  font-size: 17px;
  font-weight: 600;
  color: #1F2A3E;
  line-height: 1.3;
  flex: 1;
  padding-right: 8px;
}

.status-badge {
  font-size: 11px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 30px;
  background-color: #F0F2F5;
  color: #5C6A7E;
  white-space: nowrap;
}

.status-badge.active {
  background-color: #E9F4FF;
  color: #1A73E8;
}

.status-badge.completed {
  background-color: #E6F7EC;
  color: #2E7D32;
}

.status-badge.expired {
  background-color: #FFEFED;
  color: #D32F2F;
}

.status-badge.draft {
  background-color: #FFEFED;
  color: #D32F2F;
}

.status-badge.not-started {
  background-color: #FFF7E6;
  color: #FF976A;
}

.survey-desc {
  font-size: 13px;
  color: #6F7D95;
  margin-bottom: 14px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.meta-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
  font-size: 12px;
  color: #8C9AA8;
}

.end-time-info {
  display: flex;
  align-items: center;
  gap: 6px;
  background-color: #E6F7FF;
  padding: 4px 10px;
  border-radius: 20px;
  color: #1A73E8;
  font-weight: 500;
}

.reward-icon {
  font-size: 14px;
}

.question-count {
  display: flex;
  align-items: center;
  gap: 4px;
}



.action-btn {
  text-align: right;
}

.btn-outline {
  background-color: transparent;
  border: 1px solid #D0D8E2;
  padding: 8px 16px;
  border-radius: 40px;
  font-size: 13px;
  font-weight: 500;
  color: #1A73E8;
  display: inline-block;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-outline:active {
  background-color: #F0F7FF;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #9AA6B5;
}

.empty-icon {
  font-size: 56px;
  margin-bottom: 16px;
  opacity: 0.6;
}

/* 列表底部 */
.list-end {
  text-align: center;
  padding: 20px 0 30px;
  font-size: 12px;
  color: #A3B1C2;
}

/* 加载状态 */
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
  border: 3px solid rgba(59, 130, 246, 0.2);
  border-top: 3px solid #1A73E8;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: var(--spacing-md);
}

.loading-container p {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
  font-weight: 500;
  margin: 0;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(6px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式调整 */
@media (max-width: 375px) {
  .nav-header {
    padding: 10px 16px;
  }
  
  .title-section h1 {
    font-size: 24px;
  }
  
  .title-section p {
    font-size: 12px;
  }
  
  .filter-icon {
    width: 36px;
    height: 36px;
  }
  
  .search-container {
    padding: 8px 16px 12px 16px;
  }
  
  .search-box {
    padding: 10px 16px;
  }
  
  .tabs {
    padding: 0 16px 10px 16px;
    gap: 20px;
  }
  
  .tab-item {
    font-size: 14px;
  }
  
  .questionnaire-list {
    padding: 4px 12px 16px 12px;
  }
  
  .survey-card {
    padding: 14px;
    margin-bottom: 12px;
  }
  
  .survey-title {
    font-size: 16px;
  }
  
  .survey-desc {
    font-size: 12px;
    margin-bottom: 12px;
  }
  
  .meta-info {
    margin-bottom: 12px;
  }
  
  .end-time-info {
    padding: 3px 8px;
    font-size: 11px;
  }
  
  .question-count {
    font-size: 11px;
  }
  

  
  .btn-outline {
    padding: 6px 14px;
    font-size: 12px;
  }
  
  .empty-state {
    padding: 40px 16px;
  }
  
  .empty-icon {
    font-size: 48px;
  }
  
  .list-end {
    padding: 16px 0 24px;
  }
}
</style>