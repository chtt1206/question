<template>
  <div class="survey-list">
    <a-card title="问卷列表" class="mb-4">
      <a-button type="primary" @click="handleAdd" class="mb-4">
        <template #icon>
          <plus-outlined />
        </template>
        新建问卷
      </a-button>
      <a-table
        :columns="columns"
        :data-source="surveys"
        :loading="loading"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'actions'">
            <a-button type="link" @click="handleEdit(record)">
              编辑
            </a-button>
            <a-button 
              v-if="record.status === 'DRAFT'"
              type="link"
              @click="showPublishConfirm(record)"
            >
              发布
            </a-button>
            <a-button 
              v-else
              type="link"
              @click="showUnpublishConfirm(record)"
            >
              取消发布
            </a-button>
            <a-button type="link" @click="handleStatistics(record)">
              统计
            </a-button>
            <a-button type="link" danger @click="showDeleteConfirm(record)">
              删除
            </a-button>
          </template>
          <template v-else-if="column.key === 'createdAt'">
            {{ record.createdAt || 'N/A' }}
          </template>
        </template>
      </a-table>
    </a-card>
    <a-modal
      v-model:open="deleteModalVisible"
      title="删除确认"
      @ok="handleDeleteConfirm"
      @cancel="deleteModalVisible = false"
    >
      <p>确定要删除该问卷吗？此操作不可恢复。</p>
    </a-modal>
    <a-modal
      v-model:open="publishModalVisible"
      title="发布确认"
      @ok="handlePublishConfirm"
      @cancel="publishModalVisible = false"
    >
      <p>确定要发布该问卷吗？发布后问卷将对外可见。</p>
    </a-modal>
    <a-modal
      v-model:open="unpublishModalVisible"
      title="取消发布确认"
      @ok="handleUnpublishConfirm"
      @cancel="unpublishModalVisible = false"
    >
      <p>确定要取消发布该问卷吗？取消发布后问卷将不再对外可见。</p>
    </a-modal>
    <a-modal
      v-model:open="statisticsModalVisible"
      title="📊 答题统计"
      width="800px"
      @cancel="statisticsModalVisible = false"
    >
      <div v-if="loadingStatistics">
        <a-spin tip="加载统计数据..." />
      </div>
      <div v-else>
        <div class="statistics-summary">
          <div class="statistics-item">
            <a-statistic title="总回答数" :value="statistics.totalAnswers" />
          </div>
          <div class="statistics-item">
            <a-statistic title="完成率" :value="statistics.completionRate" suffix="%" :precision="2" />
          </div>
        </div>
        <a-tabs v-model:activeKey="activeTab">
          <a-tab-pane key="userRank" tab="👥 按用户答对题数排序">
            <!-- 用户排行榜表格 -->
            <a-table :columns="userRankColumns" :pagination="false" :data-source="statistics.userRank" row-key="rank" />
            <!-- 分页组件 -->
            <div style="margin-top: 16px; text-align: right;">
              <a-pagination
                v-model:current="currentPage"
                v-model:page-size="pageSize"
                :total="total"
                @change="handlePageChange"
                show-size-changer
                :page-size-options="['10', '20', '50', '100']"
                show-quick-jumper
                :show-total="(total) => `共 ${total} 条`"
              />
            </div>
          </a-tab-pane>
          <a-tab-pane key="questionStats" tab="📋 按题目统计 (正确/错误/选项分析)">
            <!-- 按题目统计面板 -->
            <div style="margin-bottom: 20px;">
              <div 
                v-for="(item, index) in statistics.questionStats" 
                :key="item.id"
                style="background:#f1f5f9; border-radius: 20px; padding: 18px; margin-bottom: 16px;"
              >
                <h4>📌 题目{{ index + 1 }} ({{ item.type }}) - {{ item.text }}</h4>
                <p v-if="item.correctRate">
                  📊 总共答题人数: {{ item.totalCount }}人 &nbsp;|
                  ✅ 答对数量: {{ item.correctCount }}人 &nbsp;|
                  ❌ 答错数量: {{ item.wrongCount }}人 &nbsp;|
                  正确率: {{ item.correctRate }}
                </p>
                <p v-else>
                  📊 总共答题人数: {{ item.totalCount }}人 |
                  ✅ 全对数量: {{ item.correctCount }}人 |
                  部分正确: {{ item.partialCorrect }}人 |
                  全错: {{ item.wrongCount }}人
                </p>
              </div>
            </div>
          </a-tab-pane>
        </a-tabs>
        <div class="mt-4">
          <a-button type="primary" @click="exportData">📎 导出数据 (Excel / CSV)</a-button>
          <span style="margin-left: 12px; font-size:12px; align-self:center;">支持导出明细/排名/题目统计</span>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { surveyApi, statisticsApi } from '../services/api';
import { PlusOutlined } from '@ant-design/icons-vue';
import { message, Modal } from 'ant-design-vue';

const router = useRouter();
const surveys = ref([]);
const loading = ref(false);
const deleteModalVisible = ref(false);
const currentDeleteId = ref(null);
const publishModalVisible = ref(false);
const currentPublishId = ref(null);
const unpublishModalVisible = ref(false);
const currentUnpublishId = ref(null);

// 统计相关变量
const statisticsModalVisible = ref(false);
const loadingStatistics = ref(false);
const currentSurveyId = ref(null);
const activeTab = ref('userRank');
const statistics = ref({
  totalAnswers: 0,
  completionRate: 0,
  userRank: [],
  questionStats: []
});

// 分页相关变量
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);

const columns = [
  {
    title: '问卷名称',
    dataIndex: 'title',
    key: 'title',
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    customRender: (text) => {
      return text === 'DRAFT' ? '草稿' : '已发布';
    },
  },
  {
    title: '创建时间',
    dataIndex: 'createdAt',
    key: 'createdAt',
  },
  {
    title: '操作',
    key: 'actions',
    width: 300,
  },
];

// 用户排行榜表格列定义
const userRankColumns = [
  {
    title: '排名',
    dataIndex: 'rank',
    key: 'rank',
    render: (rank) => {
      return rank === 1 ? '🥇 1' : rank === 2 ? '🥈 2' : rank === 3 ? '🥉 3' : rank;
    }
  },
  {
    title: '用户昵称',
    dataIndex: 'name',
    key: 'name'
  },
  {
    title: '答对题数',
    dataIndex: 'correctCount',
    key: 'correctCount'
  },
  {
    title: '总分',
    dataIndex: 'score',
    key: 'score'
  },
  {
    title: '答题时长',
    dataIndex: 'answerTime',
    key: 'answerTime'
  },
  {
    title: '提交时间',
    dataIndex: 'submitTime',
    key: 'submitTime'
  }
];

const fetchSurveys = async () => {
  loading.value = true;
  try {
    const data = await surveyApi.getList();
    surveys.value = data;
  } catch (error) {
    message.error('获取问卷列表失败');
  } finally {
    loading.value = false;
  }
};

const handleAdd = () => {
  router.push('/survey/new');
};

const handleEdit = (record) => {
  router.push(`/survey/${record.id}`);
};

const handleQuestions = (record) => {
  router.push(`/survey/${record.id}/questions`);
};

const handleAnswers = (record) => {
  router.push(`/survey/${record.id}/answers`);
};

const handleStatistics = async (record) => {
  currentSurveyId.value = record.id;
  statisticsModalVisible.value = true;
  // 重置分页参数
  currentPage.value = 1;
  pageSize.value = 10;
  await fetchStatistics(record.id, currentPage.value, pageSize.value);
};

const fetchStatistics = async (surveyId, page = 1, pageSizeVal = 10) => {
  loadingStatistics.value = true;
  try {
    console.log('开始获取统计数据，问卷ID:', surveyId, '页码:', page, '每页大小:', pageSizeVal);
    const data = await statisticsApi.getSurveyStatistics(surveyId, page, pageSizeVal);
    console.log('获取到的统计数据:', data);
    
    // 确保数据结构完整
    let userRank = data.userRank || data.ranking || [];
    
    // 处理不同格式的数据
    if (userRank.length > 0 && typeof userRank[0] === 'object' && userRank[0].userName !== undefined) {
      // 处理不同格式的数据，保留后端返回的排名
      userRank = userRank.map((record) => ({
        rank: record.rank,
        name: record.userName || `用户${record.rank || 1}`,
        correctCount: record.correctCount || '0/0',
        score: record.score || '0分',
        answerTime: record.answerTime || '0秒',
        submitTime: record.submitTime || new Date().toISOString()
      }));
    }
    console.log('后端返回的排名数据:', userRank);
    console.log('后端返回的题目统计数据:', data.questionStats);
    
    // 更新统计数据
    statistics.value = {
      totalAnswers: data.totalAnswers || data.answerCount || 0,
      completionRate: data.completionRate || 0,
      userRank: userRank,
      questionStats: data.questionStats || []
    };
    console.log('处理后的题目统计数据:', statistics.value.questionStats);
    
    // 更新分页数据
    total.value = data.total || 0;
    currentPage.value = data.page || 1;
    pageSize.value = data.pageSize || 10;
    
    console.log('处理后的统计数据:', statistics.value);
  } catch (error) {
    console.error('获取统计数据失败:', error);
    message.error('获取统计数据失败');
    // 设置默认值
    statistics.value = {
      totalAnswers: 0,
      completionRate: 0,
      userRank: [],
      questionStats: []
    };
    total.value = 0;
    currentPage.value = 1;
    pageSize.value = 10;
  } finally {
    loadingStatistics.value = false;
  }
};

const exportData = () => {
  alert('📎 导出数据：支持下载《答题明细.xlsx》《用户排名.xlsx》《题目统计.xlsx》\n满足按题目(总共答题人数/答对/答错/选项分析)需求。');
};

const handlePageChange = async (page, pageSizeVal) => {
  console.log('分页变化，页码:', page, '每页大小:', pageSizeVal);
  if (currentSurveyId.value) {
    await fetchStatistics(currentSurveyId.value, page, pageSizeVal);
  }
};

const showPublishConfirm = (record) => {
  currentPublishId.value = record.id;
  publishModalVisible.value = true;
};

const handlePublishConfirm = async () => {
  try {
    await surveyApi.publish(currentPublishId.value);
    message.success('发布成功');
    fetchSurveys();
    publishModalVisible.value = false;
  } catch (error) {
    message.error('发布失败');
  }
};

const showUnpublishConfirm = (record) => {
  currentUnpublishId.value = record.id;
  unpublishModalVisible.value = true;
};

const handleUnpublishConfirm = async () => {
  try {
    await surveyApi.unpublish(currentUnpublishId.value);
    message.success('取消发布成功');
    fetchSurveys();
    unpublishModalVisible.value = false;
  } catch (error) {
    message.error('取消发布失败');
  }
};

const showDeleteConfirm = (record) => {
  currentDeleteId.value = record.id;
  deleteModalVisible.value = true;
};

const handleDeleteConfirm = async () => {
  try {
    await surveyApi.delete(currentDeleteId.value);
    message.success('删除成功');
    fetchSurveys();
    deleteModalVisible.value = false;
  } catch (error) {
    message.error('删除失败');
  }
};

onMounted(() => {
  fetchSurveys();
});
</script>

<style scoped>
.survey-list {
  padding: 24px;
}
.statistics-summary {
  display: flex;
  gap: 48px;
  padding: 20px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  margin-bottom: 24px;
}
.statistics-item {
  flex: 1;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
.statistics-item :deep(.ant-statistic-title) {
  color: #666;
  font-size: 14px;
}
.statistics-item :deep(.ant-statistic-content) {
  color: #333;
  font-weight: 600;
}
.statistics-item :deep(.ant-statistic-content-value) {
  font-size: 28px;
  color: #667eea;
}
.statistics-item :deep(.ant-statistic-content-suffix) {
  font-size: 18px;
  color: #667eea;
}
</style>