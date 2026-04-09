<template>
  <div class="survey-detail">
    <a-card :title="isNew ? '新建问卷' : '编辑问卷'" class="mb-4">
      <a-form
        :model="surveyForm"
        :rules="rules"
        layout="vertical"
      >
        <a-form-item label="问卷名称" name="title">
          <a-input v-model:value="surveyForm.title" placeholder="请输入问卷名称" />
        </a-form-item>
        <a-form-item label="问卷描述" name="description">
          <a-textarea v-model:value="surveyForm.description" placeholder="请输入问卷描述" rows="4" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="handleSubmit">
            保存
          </a-button>
          <a-button @click="handleCancel">
            取消
          </a-button>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { surveyApi } from '../services/api';
import { message } from 'ant-design-vue';

const router = useRouter();
const route = useRoute();
const surveyForm = ref({
  id: null,
  title: '',
  description: ''
});
const loading = ref(false);

const isNew = computed(() => route.params.id === 'new');

const rules = {
  title: [
    {
      required: true,
      message: '请输入问卷名称',
      trigger: 'blur'
    }
  ]
};

const fetchSurvey = async () => {
  if (!isNew.value) {
    loading.value = true;
    try {
      const response = await surveyApi.getById(route.params.id);
      surveyForm.value = response;
    } catch (error) {
      message.error('获取问卷详情失败');
    } finally {
      loading.value = false;
    }
  }
};

const handleSubmit = async () => {
  try {
    if (isNew.value) {
      const survey = await surveyApi.create(surveyForm.value);
      message.success('创建成功');
      // 跳转到问卷编辑页面（问题列表）
      router.push(`/survey/${survey.id}/questions`);
    } else {
      await surveyApi.update(surveyForm.value);
      message.success('更新成功');
      router.push('/');
    }
  } catch (error) {
    message.error('保存失败');
  }
};

const handleCancel = () => {
  router.push('/');
};

onMounted(() => {
  fetchSurvey();
});
</script>

<style scoped>
.survey-detail {
  padding: 24px;
  max-width: 800px;
  margin: 0 auto;
}
</style>