<script setup>
import { ref, reactive } from 'vue';

// 问卷数据
const surveyTitle = ref('2024年员工安全培训考试 (含单选/多选/判断)');
const questions = reactive([
  {
    id: 1,
    type: 'single',
    text: '1. 以下哪个是正确的手部卫生步骤？',
    options: [
      { text: '使用冷水冲洗', isCorrect: false },
      { text: '使用肥皂搓洗至少20秒', isCorrect: true },
      { text: '仅使用免洗洗手液', isCorrect: false }
    ],
    score: 2
  },
  {
    id: 2,
    type: 'multiple',
    text: '2. 以下哪些属于火灾逃生的正确做法？',
    options: [
      { text: '用湿毛巾捂住口鼻', isCorrect: true },
      { text: '乘坐电梯快速下楼', isCorrect: false },
      { text: '弯腰低姿前进', isCorrect: true },
      { text: '从窗户直接跳下', isCorrect: false }
    ],
    score: 3
  },
  {
    id: 3,
    type: 'judgment',
    text: '3. 电器着火时，应首先使用水基灭火器扑救。',
    options: [
      { text: '正确', isCorrect: false },
      { text: '错误', isCorrect: true }
    ],
    score: 1
  }
]);

// 发布设置
const settings = reactive({
  startTime: '2026-04-10T08:00',
  endTime: '2026-04-20T23:59',
  allowRepeat: false,
  timeLimit: 30
});

// 统计数据
const statistics = {
  userRank: [
    { rank: 1, name: '张明', correctCount: '3/3', score: '6分', submitTime: '2026-04-07 14:23' },
    { rank: 2, name: '李芳', correctCount: '2/3', score: '4分', submitTime: '2026-04-07 13:15' },
    { rank: 3, name: '王强', correctCount: '2/3', score: '3.5分', submitTime: '2026-04-06 09:47' },
    { rank: 4, name: '赵敏', correctCount: '1/3', score: '2分', submitTime: '2026-04-05 20:12' }
  ],
  questionStats: [
    {
      id: 1,
      type: '单选题',
      text: '手部卫生步骤',
      totalCount: 32,
      correctCount: 26,
      wrongCount: 6,
      correctRate: '81.2%',
      optionAnalysis: 'A(3人) B(26人 ✅) C(3人)'
    },
    {
      id: 2,
      type: '多选题',
      text: '火灾逃生',
      totalCount: 32,
      correctCount: 18,
      partialCorrect: 10,
      wrongCount: 4,
      optionAnalysis: '选项A(28次), 选项B(5次), 选项C(24次), 选项D(2次)'
    },
    {
      id: 3,
      type: '判断题',
      text: '电器灭火',
      totalCount: 32,
      correctCount: 24,
      wrongCount: 8,
      correctRate: '75%',
      optionAnalysis: '正确(8人) ❌ 错误(24人) ✅'
    }
  ]
};

// 预览模态框
const showPreview = ref(false);

// 预览答案
const previewAnswers = reactive({
  1: null, // 单选题答案
  2: [],   // 多选题答案
  3: null  // 判断题答案
});

// 统计选项卡
const activeTab = ref('userRank');

// 添加题目
const addQuestion = () => {
  alert('新建题目：可选择单选题/多选题/判断题，支持设置题干、选项、正确答案及分值');
};

// 模板导入
const importTemplate = () => {
  alert('📚 模板导入功能\n支持官方模板库 / 我的模板 / Excel批量导入（.xlsx）\n可快速填充单选/多选/判断题');
};

// 保存草稿
const saveDraft = () => {
  alert('✅ 草稿已保存，当前所有题目及设置已存储');
};

// 发布问卷
const publishSurvey = () => {
  alert('🚀 问卷已发布！已生效答题开始/截止时间、重复答题限制及限时要求。\n可分享链接/二维码给答题者。');
};

// 导出数据
const exportData = () => {
  alert('📎 导出数据：支持下载《答题明细.xlsx》《用户排名.xlsx》《题目统计.xlsx》\n满足按题目(总共答题人数/答对/答错/选项分析)需求。');
};

// 保存设置
const saveSettings = () => {
  alert('✅ 设置已保存');
};

// 提交答卷
const submitAnswers = () => {
  alert('✅ 答卷已提交！');
};
</script>

<template>
  <div class="app-container">
    <div class="main-content">
      <div class="toolbar">
        <span class="page-title">📌 2024员工安全培训考试 (编辑中)</span>
        <div class="btn-group">
          <button class="btn btn-outline" @click="showPreview = true">👁️ 题目预览</button>
          <button class="btn btn-outline" @click="importTemplate">📚 模板导入</button>
          <button class="btn btn-primary" @click="saveDraft">💾 保存草稿</button>
          <button class="btn btn-primary" @click="publishSurvey">🚀 发布</button>
        </div>
      </div>

      <div class="editor-area">
        <!-- 问卷标题 -->
        <div class="survey-title-card">
          <input type="text" class="survey-title-input" v-model="surveyTitle" placeholder="问卷标题">
        </div>

        <!-- 题目列表 -->
        <div v-for="question in questions" :key="question.id" class="question-card">
          <div class="question-header">
            <span class="question-type">
              {{ question.type === 'single' ? '🔘 单选题' : question.type === 'multiple' ? '☑️ 多选题' : '⚖️ 判断题' }}
            </span>
            <div class="question-actions">
              <span>⋮⋮</span>
              <span>🗑️</span>
            </div>
          </div>
          <div class="question-text">{{ question.text }}</div>
总时长          <div class="options-area">
            <template v-if="question.type === 'multiple'">
              <a-checkbox-group :disabled="true">
                <a-checkbox 
                  v-for="(option, index) in question.options" 
                  :key="index" 
                  :checked="option.isCorrect"
                  style="margin-right: 20px; margin-bottom: 8px;"
                >
                  {{ option.text }}
                  <span class="correct-badge" v-if="option.isCorrect">✅ 正确答案</span>
                  <span class="correct-badge" v-else>❌</span>
                </a-checkbox>
              </a-checkbox-group>
            </template>
            <template v-else>
              <a-radio-group :disabled="true">
                <a-radio 
                  v-for="(option, index) in question.options" 
                  :key="index" 
                  :checked="option.isCorrect"
                  style="margin-right: 20px; margin-bottom: 8px;"
                >
                  {{ option.text }}
                  <span class="correct-badge" v-if="option.isCorrect">✅ 正确答案</span>
                  <span class="correct-badge" v-else>❌</span>
                </a-radio>
              </a-radio-group>
            </template>
          </div>
          <div class="flex-between">
            <span>分值: <input type="number" class="score-input" v-model.number="question.score" step="1"> 分</span>
            <span v-if="question.type === 'multiple'" style="color:#6c757d; font-size:12px;">全对得分</span>
          </div>
        </div>

        <!-- 添加题目按钮 -->
        <div class="add-question-btn" @click="addQuestion">+ 添加题目 (单选/多选/判断)</div>

        <!-- 发布设置区域 -->
        <div class="settings-panel">
          <h3 style="margin-bottom: 18px; font-size: 18px;">⚙️ 答题规则设置</h3>
          <div class="settings-grid">
            <div class="setting-field">
              <label>📅 答题开始时间</label>
              <input type="datetime-local" v-model="settings.startTime">
            </div>
            <div class="setting-field">
              <label>⏰ 答题截止时间</label>
              <input type="datetime-local" v-model="settings.endTime">
            </div>
            <div class="setting-field">
              <label>🔄 是否允许重复答题</label>
              <select v-model="settings.allowRepeat">
                <option :value="false">不允许 (每人仅一次)</option>
                <option :value="true">允许重复答题</option>
              </select>
            </div>
            <div class="setting-field">
              <label>⏱️ 答题时间要求 (分钟)</label>
              <input type="number" v-model.number="settings.timeLimit" placeholder="0表示不限时"> 
              <span style="font-size:12px;">分钟</span>
            </div>
          </div>
          <div class="flex-between">
            <span style="color:#4b5563; font-size:13px;">* 支持限制同一微信/设备仅填一次，基于OpenID识别</span>
            <div class="btn-group">
              <button class="btn btn-outline" style="border-color:#2563eb;" @click="saveSettings">保存设置</button>
            </div>
          </div>
        </div>

        <!-- 答题统计模块 -->
        <div style="margin-top: 40px; border-top: 2px solid #eef2ff; padding-top: 24px;">
          <h2 style="font-size: 20px; margin-bottom: 8px;">📊 答题统计面板</h2>
          <div class="stats-container">
            <div class="stats-tabs">
              <div 
                class="stat-tab" 
                :class="{ active: activeTab === 'userRank' }" 
                @click="activeTab = 'userRank'"
              >
                👥 按用户答对题数排序
              </div>
              <div 
                class="stat-tab" 
                :class="{ active: activeTab === 'questionStats' }" 
                @click="activeTab = 'questionStats'"
              >
                📋 按题目统计 (正确/错误/选项分析)
              </div>
            </div>
            
            <!-- 用户排行榜表格 -->
            <div v-if="activeTab === 'userRank'" id="userRankPanel">
              <table class="ranking-table">
                <thead>
                  <tr><th>排名</th><th>用户昵称</th><th>答对题数</th><th>总分</th><th>提交时间</th></tr>
                </thead>
                <tbody>
                  <tr v-for="item in statistics.userRank" :key="item.rank">
                    <td>{{ item.rank === 1 ? '🥇 1' : item.rank === 2 ? '🥈 2' : item.rank === 3 ? '🥉 3' : item.rank }}</td>
                    <td>{{ item.name }}</td>
                    <td>{{ item.correctCount }}</td>
                    <td>{{ item.score }}</td>
                    <td>{{ item.submitTime }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
            
            <!-- 按题目统计面板 -->
            <div v-else id="questionStatsPanel">
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
            </div>
            
            <!-- 导出按钮 -->
            <div class="export-btn">
              <button class="btn btn-primary" @click="exportData">📎 导出数据 (Excel / CSV)</button>
              <span style="margin-left: 12px; font-size:12px; align-self:center;">支持导出明细/排名/题目统计</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 预览弹窗 -->
    <div id="previewModal" class="preview-modal" v-if="showPreview" @click="showPreview = false">
      <div class="preview-card" @click.stop>
        <div style="display:flex; justify-content: space-between;">
          <h3>📱 问卷预览 (答题者视角)</h3>
          <span style="cursor:pointer; font-size:24px;" @click="showPreview = false">&times;</span>
        </div>
        <hr style="margin: 12px 0;">
        <h4>{{ surveyTitle }}</h4>
        <div style="margin-top: 20px;">
          <div v-for="(question, index) in questions" :key="question.id" style="margin-bottom: 20px;">
            <p><strong>{{ question.text }}</strong></p>
            <div style="margin-top: 10px; margin-left: 20px;">
              <template v-if="question.type === 'single'">
                <a-radio-group v-model:value="previewAnswers[question.id]">
                  <a-radio 
                    v-for="(option, optIndex) in question.options" 
                    :key="optIndex" 
                    :value="optIndex"
                  >
                    {{ option.text }}
                  </a-radio>
                </a-radio-group>
              </template>
              <template v-else-if="question.type === 'multiple'">
                <a-checkbox-group v-model:value="previewAnswers[question.id]">
                  <a-checkbox 
                    v-for="(option, optIndex) in question.options" 
                    :key="optIndex" 
                    :value="optIndex"
                  >
                    {{ option.text }}
                  </a-checkbox>
                </a-checkbox-group>
              </template>
              <template v-else>
                <a-radio-group v-model:value="previewAnswers[question.id]">
                  <a-radio :value="0">正确</a-radio>
                  <a-radio :value="1">错误</a-radio>
                </a-radio-group>
              </template>
            </div>
          </div>
        </div>
        <div style="margin-top: 24px; text-align:right;">
          <a-button type="primary" @click="submitAnswers">提交答卷</a-button>
        </div>
        <p style="font-size:12px; color:#6c757d; margin-top: 16px;">
          * 限时{{ settings.timeLimit }}分钟，开始时间/截止时间已配置
        </p>
      </div>
    </div>
  </div>
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  background: #f0f2f6;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  color: #1e293b;
  padding: 32px 40px;
}

/* 整体容器 */
.app-container {
  max-width: 1440px;
  margin: 0 auto;
}

/* 主内容区 */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #ffffff;
  overflow-y: auto;
  border-radius: 28px;
  box-shadow: 0 20px 35px -12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  min-height: 720px;
}

/* 顶部工具栏 */
.toolbar {
  padding: 20px 28px;
  border-bottom: 1px solid #eef2ff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
}

.btn-group {
  display: flex;
  gap: 12px;
}

.btn {
  padding: 8px 20px;
  border-radius: 40px;
  font-weight: 500;
  font-size: 14px;
  border: none;
  cursor: pointer;
  background: white;
  transition: 0.2s;
}

.btn-primary {
  background: #2563eb;
  color: white;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.btn-primary:hover {
  background: #1d4ed8;
}

.btn-outline {
  border: 1px solid #cbd5e1;
  background: white;
}

.btn-outline:hover {
  background: #f8fafc;
}

/* 问卷编辑器区域 */
.editor-area {
  padding: 24px 32px;
  background: #ffffff;
}

/* 问卷标题卡片 */
.survey-title-card {
  background: #fefce8;
  border-left: 5px solid #eab308;
  padding: 16px 24px;
  margin-bottom: 28px;
  border-radius: 20px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.03);
}

.survey-title-input {
  font-size: 20px;
  font-weight: 600;
  border: none;
  background: transparent;
  width: 100%;
  outline: none;
  padding: 6px 0;
  border-bottom: 2px dashed #e2e8f0;
}

/* 题目卡片 */
.question-card {
  background: #ffffff;
  border: 1px solid #e9edf2;
  border-radius: 24px;
  padding: 20px 24px;
  margin-bottom: 20px;
  transition: 0.2s;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.question-type {
  background: #eef2ff;
  padding: 4px 12px;
  border-radius: 30px;
  font-size: 12px;
  font-weight: 600;
  color: #2563eb;
}

.question-actions {
  display: flex;
  gap: 12px;
  color: #94a3b8;
}

.question-text {
  font-weight: 500;
  margin-bottom: 20px;
  font-size: 16px;
  background: #f9fafb;
  padding: 12px 16px;
  border-radius: 16px;
}

.options-area {
  margin-bottom: 20px;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  padding: 6px 0;
}

.correct-badge {
  background: #dcfce7;
  color: #15803d;
  font-size: 12px;
  padding: 2px 10px;
  border-radius: 30px;
  margin-left: 12px;
}

.score-input {
  width: 70px;
  padding: 6px 10px;
  border: 1px solid #cbd5e1;
  border-radius: 28px;
  text-align: center;
}

.add-question-btn {
  border: 2px dashed #cbd5e6;
  background: #fafcff;
  border-radius: 28px;
  padding: 14px;
  text-align: center;
  font-weight: 500;
  color: #3b82f6;
  margin-top: 20px;
  cursor: pointer;
  transition: 0.2s;
}

.add-question-btn:hover {
  background: #f0f9ff;
}

/* 发布设置面板 */
.settings-panel {
  background: #f9f9fc;
  border-radius: 24px;
  padding: 24px;
  margin-top: 28px;
  border: 1px solid #eef2ff;
}

.settings-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
  margin-bottom: 20px;
}

.setting-field label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #334155;
}

.setting-field input, .setting-field select {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #cfdee9;
  border-radius: 16px;
}

/* 统计区域 */
.stats-container {
  margin-top: 20px;
}

.stats-tabs {
  display: flex;
  gap: 20px;
  border-bottom: 1px solid #e2e8f0;
  margin-bottom: 24px;
}

.stat-tab {
  padding: 10px 0;
  font-weight: 600;
  cursor: pointer;
  color: #64748b;
  border-bottom: 2px solid transparent;
}

.stat-tab.active {
  color: #2563eb;
  border-bottom-color: #2563eb;
}

.ranking-table {
  width: 100%;
  border-collapse: collapse;
}

.ranking-table th {
  text-align: left;
  padding: 12px 8px;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
}

.ranking-table td {
  padding: 12px 8px;
  border-bottom: 1px solid #f1f5f9;
}

.export-btn {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

/* 预览模态框 */
.preview-modal {
  position: fixed;
  top:0; left:0; width:100%; height:100%;
  background: rgba(0,0,0,0.5);
  justify-content: center;
  align-items: center;
  z-index: 1000;
  display: flex;
}

.preview-card {
  background: white;
  width: 700px;
  max-height: 80vh;
  overflow-y: auto;
  border-radius: 32px;
  padding: 28px;
}

.flex-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>