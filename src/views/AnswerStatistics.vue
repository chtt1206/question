<template>
  <div class="answer-statistics">
    <a-card title="回答统计" class="mb-4">
      <div class="mb-4">
        <a-statistic title="总回答数" :value="answerCount" />
      </div>
      <a-table
        :columns="columns"
        :data-source="answers"
        :loading="loading"
        row-key="id"
      >
        <template #bodyCell="{ record }">
          <template v-if="colKey === 'answers'">
            <a-tag v-for="(answer, index) in record.answers" :key="index" class="mr-2 mb-2">
              {{ answer }}
            </a-tag>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { answerApi } from '../services/api';
import { message } from 'ant-design-vue';

const route = useRoute();
const answers = ref([]);
const answerCount = ref(0);
const loading = ref(false);
const surveyId = ref(route.params.surveyId);

const columns = [
  {
    title: '回答ID',
    dataIndex: 'id',
    key: 'id',
  },
  {
    title: '回答内容',
    dataIndex: 'answers',
    key: 'answers',
  },
  {
    title: '回答时间',
    dataIndex: 'createdAt',
    key: 'createdAt',
  },
];

const fetchAnswers = async () => {
  loading.value = true;
  try {
    const [answersResponse, countResponse] = await Promise.all([
      answerApi.getBySurveyId(surveyId.value),
      answerApi.getCountBySurveyId(surveyId.value)
    ]);
    answers.value = answersResponse.data;
    answerCount.value = countResponse.data;
  } catch (error) {
    message.error('获取回答数据失败');
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchAnswers();
});
</script>

<style scoped>
.answer-statistics {
  padding: 24px;
}
</style>