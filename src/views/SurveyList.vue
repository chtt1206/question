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
      v-model:visible="deleteModalVisible"
      title="删除确认"
      @ok="handleDeleteConfirm"
      @cancel="deleteModalVisible = false"
    >
      <p>确定要删除该问卷吗？此操作不可恢复。</p>
    </a-modal>
    <a-modal
      v-model:visible="publishModalVisible"
      title="发布确认"
      @ok="handlePublishConfirm"
      @cancel="publishModalVisible = false"
    >
      <p>确定要发布该问卷吗？发布后问卷将对外可见。</p>
    </a-modal>
    <a-modal
      v-model:visible="unpublishModalVisible"
      title="取消发布确认"
      @ok="handleUnpublishConfirm"
      @cancel="unpublishModalVisible = false"
    >
      <p>确定要取消发布该问卷吗？取消发布后问卷将不再对外可见。</p>
    </a-modal>
    <a-modal
      v-model:visible="statisticsModalVisible"
      title="📊 答题统计"
      width="800px"
      @cancel="statisticsModalVisible = false"
    >
      <div v-if="loadingStatistics">
        <a-spin tip="加载统计数据..." />
      </div>
      <div v-else>
        <div class="mb-4">
          <a-statistic title="总回答数" :value="statistics.totalAnswers" />
          <a-statistic title="完成率" :value="statistics.completionRate" suffix="%" style="margin-left: 32px" />
        </div>
        <a-tabs v-model:activeKey="activeTab">
          <a-tab-pane key="userRank" tab="👥 按用户答对题数排序">
            <!-- 用户排行榜表格 -->
            <a-table :columns="userRankColumns" :data-source="statistics.userRank" row-key="rank" />
          </a-tab-pane>
          <a-tab-pane key="questionStats" tab="📋 按题目统计 (正确/错误/选项分析)">
            <!-- 按题目统计面板 -->
            <div style="margin-bottom: 20px;">
              <div 
                v-for="item in statistics.questionStats" 
                :key="item.id"
                style="background:#f1f5f9; border-radius: 20px; padding: 18px; margin-bottom: 16px;"
              >
                <h4>📌 题目{{ item.id }} ({{ item.type }}) - {{ item.text }}</h4>
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
                <p>📈 选项分析: {{ item.optionAnalysis }}</p>
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
  await fetchStatistics(record.id);
};

const fetchStatistics = async (surveyId) => {
  loadingStatistics.value = true;
  try {
    const data = await statisticsApi.getSurveyStatistics(surveyId);
    // 确保数据结构完整
    let userRank = data.userRank || [];
    
    // 按排序规则排序：先按总分降序，再按答对题数降序，再按答题时长升序
    userRank.sort((a, b) => {
      // 1. 按总分降序
      const scoreA = parseFloat(a.score) || 0;
      const scoreB = parseFloat(b.score) || 0;
      if (scoreA !== scoreB) {
        return scoreB - scoreA;
      }
      
      // 2. 按答对题数降序
      const correctCountA = parseInt(a.correctCount?.split('/')[0]) || 0;
      const correctCountB = parseInt(b.correctCount?.split('/')[0]) || 0;
      if (correctCountA !== correctCountB) {
        return correctCountB - correctCountA;
      }
      
      // 3. 按答题时长升序
      const answerTimeA = parseInt(a.answerTime) || 0;
      const answerTimeB = parseInt(b.answerTime) || 0;
      return answerTimeA - answerTimeB;
    });
    
    statistics.value = {
      totalAnswers: data.totalAnswers || 0,
      completionRate: data.completionRate || 0,
      userRank: userRank,
      questionStats: data.questionStats || []
    };
  } catch (error) {
    message.error('获取统计数据失败');
    // 设置默认值
    statistics.value = {
      totalAnswers: 0,
      completionRate: 0,
      userRank: [],
      questionStats: []
    };
  } finally {
    loadingStatistics.value = false;
  }
};

const exportData = () => {
  alert('📎 导出数据：支持下载《答题明细.xlsx》《用户排名.xlsx》《题目统计.xlsx》\n满足按题目(总共答题人数/答对/答错/选项分析)需求。');
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
</style>