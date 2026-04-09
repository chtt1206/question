# 问卷系统后端

## 项目简介

这是一个基于Spring Boot的问卷系统后端，提供问卷管理、题目管理、答题、统计和导出等功能。

## 技术栈

- Java 17
- Spring Boot 3.2.0
- MyBatis-Plus 3.5.5
- MySQL 8.0
- Redis 7.0+
- JWT
- Swagger 3.0.0
- EasyExcel 3.3.4

## 项目结构

```
survey-system-backend/
├── src/
│   ├── main/
│   │   ├── java/com/survey/system/
│   │   │   ├── controller/       # 控制器层
│   │   │   ├── service/          # 服务层
│   │   │   ├── dao/              # 数据访问层
│   │   │   ├── model/            # 数据模型
│   │   │   ├── dto/              # 数据传输对象
│   │   │   ├── config/           # 配置类
│   │   │   └── utils/            # 工具类
│   │   └── resources/
│   │       ├── mapper/           # MyBatis映射文件
│   │       ├── application.yml   # 应用配置
│   │       └── init.sql          # 数据库初始化脚本
│   └── test/                     # 测试
├── pom.xml                       # Maven配置
└── README.md                     # 项目说明
```

## 数据库配置

1. 确保MySQL服务已启动
2. 执行 `src/main/resources/init.sql` 脚本创建数据库和表结构
3. 修改 `application.yml` 中的数据库连接配置

## 运行项目

### 开发环境

```bash
# 进入项目目录
cd survey-system-backend

# 编译项目
mvn clean compile

# 运行项目
mvn spring-boot:run
```

### 生产环境

```bash
# 打包项目
mvn clean package

# 运行项目
java -jar target/survey-system-backend-1.0.0.jar
```

## API文档

项目启动后，可通过以下地址访问Swagger API文档：

```
http://localhost:8080/api/swagger-ui.html
```

## 主要功能

### 问卷管理
- 创建问卷
- 更新问卷
- 获取问卷列表
- 获取问卷详情
- 删除问卷
- 发布问卷

### 题目管理
- 添加题目
- 更新题目
- 获取题目详情
- 删除题目

### 答题
- 提交答卷

### 统计
- 获取用户排名
- 获取题目统计

### 导出
- 导出答题明细
- 导出用户排名
- 导出题目统计

## 注意事项

1. 确保Redis服务已启动，用于缓存和Session管理
2. 数据库连接配置需要根据实际环境修改
3. JWT密钥需要在生产环境中修改为安全的密钥
4. 本项目仅供学习和测试使用，生产环境需要进行更多的安全配置
