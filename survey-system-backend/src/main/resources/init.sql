-- 创建数据库
CREATE DATABASE IF NOT EXISTS survey_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE survey_system;

-- 创建问卷表
CREATE TABLE IF NOT EXISTS survey (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    start_time DATETIME,
    end_time DATETIME,
    allow_repeat BOOLEAN DEFAULT FALSE,
    time_limit INT,
    status VARCHAR(20) DEFAULT 'DRAFT',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建问题表
CREATE TABLE IF NOT EXISTS question (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    survey_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    text TEXT NOT NULL,
    required BOOLEAN DEFAULT FALSE,
    correct_option INT,
    correct_answer TEXT,
    score DOUBLE DEFAULT 0,
    question_type VARCHAR(20) DEFAULT 'QUESTION',
    sort_order INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (survey_id) REFERENCES survey(id) ON DELETE CASCADE
);

-- 创建选项表
CREATE TABLE IF NOT EXISTS survey_option (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_id BIGINT NOT NULL,
    text VARCHAR(255) NOT NULL,
    is_correct BOOLEAN DEFAULT FALSE,
    sort_order INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (question_id) REFERENCES question(id) ON DELETE CASCADE
);

-- 创建答题记录表
CREATE TABLE IF NOT EXISTS answer_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    survey_id BIGINT NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    user_name VARCHAR(100) NOT NULL,
    answers TEXT NOT NULL,
    score DOUBLE DEFAULT 0,
    correct_count INT DEFAULT 0,
    answer_time INT DEFAULT 0,
    submit_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (survey_id) REFERENCES survey(id) ON DELETE CASCADE
);

-- 创建问题统计表
CREATE TABLE IF NOT EXISTS question_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_id BIGINT NOT NULL,
    option_text VARCHAR(255) NOT NULL,
    select_count INT DEFAULT 0,
    select_rate DOUBLE DEFAULT 0,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (question_id) REFERENCES question(id) ON DELETE CASCADE
);

-- 创建索引
CREATE INDEX idx_survey_status ON survey(status);
CREATE INDEX idx_question_survey_id ON question(survey_id);
CREATE INDEX idx_survey_option_question_id ON survey_option(question_id);
CREATE INDEX idx_answer_record_survey_id ON answer_record(survey_id);
CREATE INDEX idx_question_statistics_question_id ON question_statistics(question_id);
