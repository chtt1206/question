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
            <a-button type="link" @click="handleQuestions(record)">
              问题管理
            </a-button>
            <a-button type="link" @click="handleAnswers(record)">
              查看回答
            </a-button>
            <a-button 
              v-if="record.status === 'DRAFT'"
              type="link" 
              type="primary"
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
            <a-button type="link" danger @click="showDeleteConfirm(record)">
              删除
            </a-button>
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { surveyApi } from '../services/api';
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