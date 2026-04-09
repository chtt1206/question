<template>
  <div class="question-list">
    <a-card title="问题管理" class="mb-4">
      <a-button type="primary" @click="handleAdd" class="mb-4">
        <template #icon>
          <plus-outlined />
        </template>
        新增问题
      </a-button>
      <a-table
        :columns="columns"
        :data-source="questions"
        :loading="loading"
        row-key="id"
      >
        <template #bodyCell="{ record }">
          <template v-if="colKey === 'type'">
            {{ getQuestionTypeText(record.type) }}
          </template>
          <template v-if="colKey === 'actions'">
            <a-button type="link" @click="handleEdit(record)">
              编辑
            </a-button>
            <a-button type="link" danger @click="handleDelete(record)">
              删除
            </a-button>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { questionApi } from '../services/api';
import { PlusOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';

const router = useRouter();
const route = useRoute();
const questions = ref([]);
const loading = ref(false);
const surveyId = ref(route.params.surveyId);

const columns = [
  {
    title: '问题内容',
    dataIndex: 'content',
    key: 'content',
  },
  {
    title: '问题类型',
    dataIndex: 'type',
    key: 'type',
  },
  {
    title: '操作',
    key: 'actions',
    width: 150,
  },
];

const getQuestionTypeText = (type) => {
  const typeMap = {
    'SINGLE': '单选题',
    'MULTIPLE': '多选题',
    'TEXT': '填空题'
  };
  return typeMap[type] || type;
};

const fetchQuestions = async () => {
  loading.value = true;
  try {
    const response = await questionApi.getBySurveyId(surveyId.value);
    questions.value = response.data;
  } catch (error) {
    message.error('获取问题列表失败');
  } finally {
    loading.value = false;
  }
};

const handleAdd = () => {
  // 这里可以跳转到问题编辑页面，暂时先简单处理
  message.info('功能开发中');
};

const handleEdit = (record) => {
  message.info('功能开发中');
};

const handleDelete = async (record) => {
  try {
    await questionApi.delete(record.id);
    message.success('删除成功');
    fetchQuestions();
  } catch (error) {
    message.error('删除失败');
  }
};

onMounted(() => {
  fetchQuestions();
});
</script>

<style scoped>
.question-list {
  padding: 24px;
}
</style>