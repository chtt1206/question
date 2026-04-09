<script setup>
import { ref, reactive, watch } from 'vue';
import dayjs from 'dayjs';

// 问卷数据
const surveyTitle = ref('2024年员工安全培训考试');
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
    correctOption: 1,
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
    correctOptions: [0, 2],
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
    correctOption: 1,
    score: 1
  }
]);

// 监听题目正确答案变化，同步更新isCorrect字段
for (const question of questions) {
  if (question.type === 'multiple') {
    // 监听correctOptions变化
    Object.defineProperty(question, 'correctOptions', {
      get() {
        return this._correctOptions || [];
      },
      set(value) {
        this._correctOptions = value;
        // 同步更新options的isCorrect字段
        this.options.forEach((option, index) => {
          option.isCorrect = value.includes(index);
        });
      },
      enumerable: true,
      configurable: true
    });
    // 初始化_correctOptions
    question._correctOptions = question.correctOptions;
  } else {
    // 监听correctOption变化
    Object.defineProperty(question, 'correctOption', {
      get() {
        return this._correctOption;
      },
      set(value) {
        this._correctOption = value;
        // 同步更新options的isCorrect字段
        this.options.forEach((option, index) => {
          option.isCorrect = index === value;
        });
      },
      enumerable: true,
      configurable: true
    });
    // 初始化_correctOption
    question._correctOption = question.correctOption;
  }
}

// 发布设置
const settings = reactive({
  startTime: dayjs('2026-04-10T08:00'),
  endTime: dayjs('2026-04-20T23:59'),
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

// 表格列定义
const columns = [
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

// 添加题目相关
const showAddQuestionModal = ref(false);
const newQuestion = reactive({
  type: 'single',
  text: '',
  options: [
    { text: '', isCorrect: false },
    { text: '', isCorrect: false },
    { text: '', isCorrect: false }
  ],
  correctOption: 0,
  correctOptions: [],
  score: 2
});

// 监听新题目类型变化
watch(() => newQuestion.type, (newType) => {
  if (newType === 'multiple') {
    newQuestion.correctOptions = [];
  } else {
    newQuestion.correctOption = 0;
  }
  // 同步更新options的isCorrect字段
  newQuestion.options.forEach((option, index) => {
    if (newType === 'multiple') {
      option.isCorrect = newQuestion.correctOptions.includes(index);
    } else {
      option.isCorrect = index === newQuestion.correctOption;
    }
  });
});

// 监听新题目正确答案变化
Object.defineProperty(newQuestion, 'correctOption', {
  get() {
    return this._correctOption || 0;
  },
  set(value) {
    this._correctOption = value;
    // 同步更新options的isCorrect字段
    this.options.forEach((option, index) => {
      option.isCorrect = index === value;
    });
  },
  enumerable: true,
  configurable: true
});

Object.defineProperty(newQuestion, 'correctOptions', {
  get() {
    return this._correctOptions || [];
  },
  set(value) {
    this._correctOptions = value;
    // 同步更新options的isCorrect字段
    this.options.forEach((option, index) => {
      option.isCorrect = value.includes(index);
    });
  },
  enumerable: true,
  configurable: true
});

// 初始化新题目
newQuestion._correctOption = newQuestion.correctOption;
newQuestion._correctOptions = newQuestion.correctOptions;

// 添加题目
const addQuestion = () => {
  // 重置新题目数据
  newQuestion.type = 'single';
  newQuestion.text = '';
  newQuestion.options = [
    { text: '', isCorrect: false },
    { text: '', isCorrect: false },
    { text: '', isCorrect: false }
  ];
  newQuestion.correctOption = 0;
  newQuestion.correctOptions = [];
  newQuestion.score = 2;
  // 初始化内部字段
  newQuestion._correctOption = newQuestion.correctOption;
  newQuestion._correctOptions = newQuestion.correctOptions;
  showAddQuestionModal.value = true;
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

// 获取正确选项索引
const getCorrectOptionIndex = (question) => {
  return question.options.findIndex(option => option.isCorrect);
};

// 处理单选框变更
const handleRadioChange = (question, index) => {
  question.options.forEach((option, i) => {
    option.isCorrect = i === index;
  });
};

// 为现有题目添加选项
const addOptionToExisting = (question) => {
  question.options.push({ text: '', isCorrect: false });
};

// 删除题目
const deleteQuestion = (id) => {
  const index = questions.findIndex(q => q.id === id);
  if (index !== -1) {
    questions.splice(index, 1);
  }
};

// 删除选项
const removeOptionFromExisting = (question, index) => {
  if (question.options.length > 2) {
    question.options.splice(index, 1);
    // 更新正确答案
    if (question.type === 'multiple') {
      question.correctOptions = question.correctOptions.filter(i => i !== index);
    } else if (question.correctOption > index) {
      question.correctOption--;
    }
  } else {
    alert('至少需要2个选项');
  }
};

// 处理题目类型变更
const handleQuestionTypeChange = (question) => {
  if (question.type === 'multiple') {
    question.correctOptions = [];
    delete question.correctOption;
    delete question._correctOption;
    // 为多选题添加属性监听
    Object.defineProperty(question, 'correctOptions', {
      get() {
        return this._correctOptions || [];
      },
      set(value) {
        this._correctOptions = value;
        this.options.forEach((option, index) => {
          option.isCorrect = value.includes(index);
        });
      },
      enumerable: true,
      configurable: true
    });
    question._correctOptions = question.correctOptions;
  } else {
    question.correctOption = 0;
    delete question.correctOptions;
    delete question._correctOptions;
    // 为单选题或判断题添加属性监听
    Object.defineProperty(question, 'correctOption', {
      get() {
        return this._correctOption;
      },
      set(value) {
        this._correctOption = value;
        this.options.forEach((option, index) => {
          option.isCorrect = index === value;
        });
      },
      enumerable: true,
      configurable: true
    });
    question._correctOption = question.correctOption;
  }
  // 同步更新options的isCorrect字段
  question.options.forEach((option, index) => {
    if (question.type === 'multiple') {
      option.isCorrect = question.correctOptions?.includes(index) || false;
    } else {
      option.isCorrect = index === question.correctOption;
    }
  });
};

// 保存新题目
const saveNewQuestion = () => {
  // 验证题目信息
  if (!newQuestion.text.trim()) {
    alert('请输入题目');
    return;
  }
  
  // 验证选项
  const validOptions = newQuestion.options.filter(option => option.text.trim());
  if (validOptions.length < 2) {
    alert('至少需要2个有效选项');
    return;
  }
  
  // 验证正确答案
  const correctOptions = newQuestion.options.filter(option => option.isCorrect);
  if (correctOptions.length === 0) {
    alert('请设置正确答案');
    return;
  }
  
  if (newQuestion.type === 'single' && correctOptions.length > 1) {
    alert('单选题只能有一个正确答案');
    return;
  }
  
  // 生成新题目ID
  const newId = questions.length > 0 ? Math.max(...questions.map(q => q.id)) + 1 : 1;
  
  // 创建新题目对象
  const questionToAdd = {
    id: newId,
    type: newQuestion.type,
    text: newQuestion.text,
    options: [...newQuestion.options.filter(option => option.text.trim())],
    score: newQuestion.score
  };
  
  // 添加正确答案字段
  if (newQuestion.type === 'multiple') {
    questionToAdd.correctOptions = newQuestion.correctOptions;
    questionToAdd._correctOptions = newQuestion.correctOptions;
  } else {
    questionToAdd.correctOption = newQuestion.correctOption;
    questionToAdd._correctOption = newQuestion.correctOption;
  }
  
  // 为新题目添加属性监听
  if (questionToAdd.type === 'multiple') {
    Object.defineProperty(questionToAdd, 'correctOptions', {
      get() {
        return this._correctOptions || [];
      },
      set(value) {
        this._correctOptions = value;
        this.options.forEach((option, index) => {
          option.isCorrect = value.includes(index);
        });
      },
      enumerable: true,
      configurable: true
    });
  } else {
    Object.defineProperty(questionToAdd, 'correctOption', {
      get() {
        return this._correctOption;
      },
      set(value) {
        this._correctOption = value;
        this.options.forEach((option, index) => {
          option.isCorrect = index === value;
        });
      },
      enumerable: true,
      configurable: true
    });
  }
  
  // 添加到题目列表
  questions.push(questionToAdd);
  
  // 关闭模态框
  showAddQuestionModal.value = false;
  
  alert('题目添加成功！');
};

// 添加选项
const addOption = () => {
  newQuestion.options.push({ text: '', isCorrect: false });
};

// 删除选项
const removeOption = (index) => {
  if (newQuestion.options.length > 2) {
    newQuestion.options.splice(index, 1);
  } else {
    alert('至少需要2个选项');
  }
};


</script>

<template>
  <div class="app-container">
    <div class="main-content">
      <div class="toolbar">
        <div class="btn-group">
          <a-button @click="showPreview = true">👁️ 题目预览</a-button>
          <a-button @click="importTemplate">📚 模板导入</a-button>
          <a-button type="primary" @click="saveDraft">💾 保存草稿</a-button>
          <a-button type="primary" @click="publishSurvey">🚀 发布</a-button>
        </div>
      </div>

      <div class="editor-area">
        <!-- 问卷标题 -->
        <div class="survey-title-card">
          <a-input v-model:value="surveyTitle" placeholder="问卷标题" class="survey-title-input" />
        </div>

        <!-- 题目列表 -->
        <div v-for="question in questions" :key="question.id" class="question-card">
          <div class="question-header">
            <div class="question-type">
              <a-radio-group v-model:value="question.type" size="small" @change="handleQuestionTypeChange(question)">
                <a-radio value="single">🔘 单选题</a-radio>
                <a-radio value="multiple">☑️ 多选题</a-radio>
                <a-radio value="judgment">⚖️ 判断题</a-radio>
              </a-radio-group>
            </div>
            <div class="question-actions">
              <a-button size="small" @click="addOptionToExisting(question)">+ 选项</a-button>
              <span style="margin: 0 8px;">⋮⋮</span>
              <a-button size="small" danger @click="deleteQuestion(question.id)">🗑️ 删除</a-button>
            </div>
          </div>
          <div class="question-text">
            <a-input v-model:value="question.text" placeholder="请输入题目内容" style="width: 100%;" />
          </div>
          <div class="options-area">
            <template v-if="question.type === 'multiple'">
              <a-checkbox-group v-model:value="question.correctOptions" style="display:inline-grid">
                    <a-checkbox 
                      v-for="(option, index) in question.options" 
                      :key="index" 
                      :value="index"
                      style=" margin-bottom: 8px; display: flex; align-items: center; gap: 8px;"
                    >
                      <a-input v-model:value="option.text" placeholder="选项内容" style="flex: 1;" />
                      <span class="correct-badge" v-if="option.isCorrect">✅ 正确答案</span>
                      <span class="correct-badge" v-else>❌</span>
                      <a-button 
                        size="small" 
                        danger 
                        @click="removeOptionFromExisting(question, index)"
                      >
                        删除
                      </a-button>
                    </a-checkbox>
              </a-checkbox-group>
            </template>
            <template v-else>
              <a-radio-group v-model:value="question.correctOption"  style="display:inline-grid">
                <a-radio 
                  v-for="(option, index) in question.options" 
                  :key="index" 
                  :value="index"
                  style="margin-bottom: 8px; display: flex; align-items: center; gap: 8px;"
                >
                  <div style="display: inline-flex;align-items: center;">
                    <a-input v-model:value="option.text" placeholder="选项内容" style="flex: 1;" />
                    <span class="correct-badge" v-if="option.isCorrect">✅ 正确答案</span>
                    <span class="correct-badge" v-else>❌</span>
                    <a-button 
                      size="small" 
                      danger 
                      @click="removeOptionFromExisting(question, index)"
                    >
                      删除
                    </a-button>
                  </div>
                </a-radio>
              </a-radio-group>
            </template>
          </div>
          <div class="flex-between">
            <span>分值: <a-input-number v-model:value="question.score" :step="1" class="score-input" size="small" /> 分</span>
            <span v-if="question.type === 'multiple'" style="color:#6c757d; font-size:12px;">全对得分</span>
          </div>
        </div>

        <!-- 添加题目按钮 -->
        <div class="add-question-btn" @click="addQuestion">+ 添加题目 (单选/多选)</div>

        <!-- 发布设置区域 -->
        <div class="settings-panel">
          <h3 style="margin-bottom: 18px; font-size: 18px;">⚙️ 答题规则设置</h3>
          <div class="settings-grid">
            <div class="setting-field">
              <label>📅 答题开始时间</label>
              <a-date-picker v-model:value="settings.startTime" format="YYYY-MM-DD HH:mm" show-time />
            </div>
            <div class="setting-field">
              <label>⏰ 答题截止时间</label>
              <a-date-picker v-model:value="settings.endTime" format="YYYY-MM-DD HH:mm" show-time />
            </div>
            <div class="setting-field">
              <label>🔄 是否允许重复答题</label>
              <a-switch v-model:checked="settings.allowRepeat" />
              <span style="margin-left: 8px; font-size: 12px;">{{ settings.allowRepeat ? '允许重复答题' : '不允许 (每人仅一次)' }}</span>
            </div>
            <div class="setting-field">
              <label>⏱️ 答题时间要求 (分钟)</label>
              <a-input-number v-model:value="settings.timeLimit" placeholder="0表示不限时" />
            </div>
          </div>
          <div class="flex-between">
            <div class="btn-group">
              <a-button @click="saveSettings">保存设置</a-button>
            </div>
          </div>
        </div>

        <!-- 答题统计模块 -->
        <div style="margin-top: 40px; border-top: 2px solid #eef2ff; padding-top: 24px;">
          <h2 style="font-size: 20px; margin-bottom: 8px;">📊 答题统计面板</h2>
          <div class="stats-container">
            <a-tabs v-model:activeKey="activeTab">
              <a-tab-pane key="userRank" tab="👥 按用户答对题数排序">
                <!-- 用户排行榜表格 -->
                <a-table :columns="columns" :data-source="statistics.userRank" row-key="rank" />
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
            
            <!-- 导出按钮 -->
            <div class="export-btn">
              <a-button type="primary" @click="exportData">📎 导出数据 (Excel / CSV)</a-button>
              <span style="margin-left: 12px; font-size:12px; align-self:center;">支持导出明细/排名/题目统计</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 预览弹窗 -->
    <a-modal
      v-model:open="showPreview"
      title="📱 问卷预览 (答题者视角)"
      width="700px"
    >
      <h4>{{ surveyTitle }}</h4>
      <div style="margin-top: 20px;">
        <div v-for="(question, index) in questions" :key="question.id" style="margin-bottom: 20px;">
          <p><strong>{{ question.text }}</strong></p>
          <div style="margin-top: 10px; margin-left: 20px;">
            <template v-if="question.type === 'single'">
              <a-radio-group v-model:value="previewAnswers[question.id]" style="display:inline-grid">
                <a-radio 
                  v-for="(option, optIndex) in question.options" 
                  :key="optIndex" 
                  :value="optIndex"
                  style=" margin-bottom: 8px;"
                >
                  {{ option.text }}
                </a-radio>
              </a-radio-group>
            </template>
            <template v-else-if="question.type === 'multiple'">
              <a-checkbox-group v-model:value="previewAnswers[question.id]" style="display:inline-grid">
                    <a-checkbox 
                      v-for="(option, optIndex) in question.options" 
                      :key="optIndex" 
                      :value="optIndex"
                      style="margin-bottom: 8px;"
                    >
                      {{ option.text }}
                    </a-checkbox>
              </a-checkbox-group>
            </template>
            <template v-else>
              <a-radio-group v-model:value="previewAnswers[question.id]">
                <a-radio :value="0" style="display: block; margin-bottom: 8px;">正确</a-radio>
                <a-radio :value="1" style="display: block; margin-bottom: 8px;">错误</a-radio>
              </a-radio-group>
            </template>
          </div>
        </div>
      </div>
      <template #footer>
        <a-button type="primary" @click="submitAnswers">提交答卷</a-button>
      </template>
      <p style="font-size:12px; color:#6c757d; margin-top: 16px;">
        * 限时{{ settings.timeLimit }}分钟，开始时间/截止时间已配置
      </p>
    </a-modal>

    <!-- 添加题目模态框 -->
    <a-modal
      v-model:open="showAddQuestionModal"
      title="📝 添加题目"
      width="700px"
    >
      <!-- 题目类型选择 -->
      <div style="margin-bottom: 20px;">
        <label style="display: block; font-weight: 600; margin-bottom: 8px;">题目类型</label>
        <a-radio-group v-model:value="newQuestion.type">
          <a-radio value="single">🔘 单选题</a-radio>
          <a-radio value="multiple">☑️ 多选题</a-radio>
        </a-radio-group>
      </div>
      
      <!-- 题干输入 -->
      <div style="margin-bottom: 20px;">
        <label style="display: block; font-weight: 600; margin-bottom: 8px;">题目</label>
        <textarea 
          v-model="newQuestion.text" 
          placeholder="请输入题目内容" 
          style="width: 100%; padding: 12px; border: 1px solid #cbd5e1; border-radius: 12px; resize: vertical; min-height: 80px;"
        ></textarea>
      </div>
      
      <!-- 选项编辑 -->
      <div style="margin-bottom: 20px;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
          <label style="font-weight: 600;">选项</label>
          <a-button size="small" @click="addOption">+ 添加选项</a-button>
        </div>
        
        <template v-if="newQuestion.type === 'multiple'">
          <a-checkbox-group v-model:value="newQuestion.correctOptions">
            <div v-for="(option, index) in newQuestion.options" :key="index" style="margin-bottom: 12px; display: flex; align-items: center; gap: 12px;">
              <a-checkbox :value="index"></a-checkbox>
              <a-input 
                v-model:value="option.text" 
                placeholder="选项内容" 
                style="flex: 1;"
              >
              </a-input>
              <a-button 
                size="small" 
                danger 
                @click="removeOption(index)"
              >
                删除
              </a-button>
            </div>
          </a-checkbox-group>
        </template>
        <template v-else>
          <a-radio-group v-model:value="newQuestion.correctOption">
            <div v-for="(option, index) in newQuestion.options" :key="index" style="margin-bottom: 12px; display: flex; align-items: center; gap: 12px;">
              <a-radio :value="index"></a-radio>
              <a-input 
                v-model:value="option.text" 
                placeholder="选项内容" 
                style="flex: 1;"
              >
              </a-input>
              <a-button 
                size="small" 
                danger 
                @click="removeOption(index)"
              >
                删除
              </a-button>
            </div>
          </a-radio-group>
        </template>
      </div>
      
      <!-- 分值设置 -->
      <div style="margin-bottom: 20px;">
        <label style="display: block; font-weight: 600; margin-bottom: 8px;">分值</label>
        <a-input-number 
          v-model:value="newQuestion.score" 
          :step="1" 
          :min="1" 
          style="width: 100px;"
        ></a-input-number>
        <span style="margin-left: 8px; color: #64748b;">分</span>
      </div>
      
      <template #footer>
        <a-button @click="showAddQuestionModal = false">取消</a-button>
        <a-button type="primary" @click="saveNewQuestion">保存题目</a-button>
      </template>
    </a-modal>
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
  /* background: #dcfce7; */
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





.export-btn {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}



.flex-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>