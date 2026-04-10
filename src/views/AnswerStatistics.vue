<template>
  <div class="answer-statistics">
    <a-card title="回答统计" class="mb-4">
      <div class="mb-4">
        <a-statistic title="总回答数" :value="statistics.totalAnswers" />
        <a-statistic title="完成率" :value="statistics.completionRate" suffix="%" style="margin-left: 32px" />
      </div>
      <a-card title="问题统计" class="mb-4">
        <a-table
          :columns="questionColumns"
          :data-source="statistics.questionStatistics"
          row-key="questionId"
        >
          <template #bodyCell="{ record }">
            <template v-if="record.questionType === 'SINGLE' || record.questionType === 'MULTIPLE'">
              <a-progress 
                v-for="option in record.options" 
                :key="option.optionId"
                :percent="option.percentage"
                :format="() => `${option.text}: ${option.count} (${option.percentage}%)`"
                style="margin-bottom: 8px"
              />
            </template>
            <template v-else>
              <span>{{ record.answerCount }} 个回答</span>
            </template>
          </template>
        </a-table>
      </a-card>
    </a-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { statisticsApi } from '../services/api';
import { message } from 'ant-design-vue';

const route = useRoute();
const statistics = ref({
  totalAnswers: 0,
  completionRate: 0,
  questionStatistics: []
});
const loading = ref(false);
const surveyId = ref(route.params.surveyId);

const questionColumns = [
  {
    title: '问题',
    dataIndex: 'questionText',
    key: 'questionText',
  },
  {
    title: '回答统计',
    dataIndex: 'statistics',
    key: 'statistics',
  },
];

const fetchStatistics = async () => {
  loading.value = true;
  try {
    const data = await statisticsApi.getSurveyStatistics(surveyId.value);
    statistics.value = data;
  } catch (error) {
    message.error('获取统计数据失败');
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchStatistics();
});
</script>

<style scoped>
.answer-statistics {
  padding: 24px;
}
</style>