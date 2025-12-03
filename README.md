# 点词·短语智慧记忆系统

一个基于Java后端和HTML前端的英语学习工具，支持英文文章分词、单词和短语识别，以及详细信息展示功能。

## 项目简介

该系统实现了一个智能的英语学习助手，具有以下特点：
- 支持粘贴英文文章并进行智能分词
- 优先识别常用短语（最长匹配算法）
- 点击单词或短语显示详细信息卡片
- 包含读音、派生词和记忆口诀
- 提供在线和离线两种使用方式

## 系统架构

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  前端页面       │     │  后端API        │     │  本地数据库     │
│  (HTML/CSS/JS)  │────▶│  (Spring Boot)  │────▶│  (H2 Database)  │
└─────────────────┘     └─────────────────┘     └─────────────────┘
```

## 技术栈

### 后端
- Java 11
- Spring Boot 2.7.0
- Spring Data JPA
- H2 Database（嵌入式数据库）
- Maven（构建工具）

### 前端
- HTML5
- CSS3
- JavaScript (ES6+)
- 原生AJAX（fetch API）

## 快速开始

### 环境要求
- JDK 11 或更高版本
- Maven 3.6.0 或更高版本（可选，已包含在项目中）
- 现代浏览器（Chrome、Firefox、Edge等）

### 1. 运行方式一：使用在线版本（推荐）

#### 启动后端服务

```bash
# 进入后端目录
cd c:\Trae\English\backend

# 使用已配置的Maven启动服务
# Windows系统
$env:PATH += ";D:\apache-maven-3.9.11\bin"; mvn spring-boot:run
# 或直接使用项目内的Maven
.\apache-maven-3.9.8-bin\apache-maven-3.9.8\bin\mvn spring-boot:run
```

服务将在 `http://localhost:8080/api` 启动

#### 打开前端页面

直接在浏览器中打开：
```
c:\Trae\English\frontend\index.html
```

### 2. 运行方式二：使用离线版本

无需启动后端服务，直接在浏览器中打开：
```
c:\Trae\English\frontend\offline.html
```

离线版本包含完整的本地功能，使用内置的词库和短语库。

## 功能说明

### 1. 文章输入与处理
- 在文本框中粘贴英文文章
- 点击"处理文章"按钮进行分词
- 系统会自动识别单词和短语

### 2. 单词/短语信息查看
- 点击文章中的单词或短语
- 显示包含以下信息的卡片：
  - 读音
  - 派生词
  - 记忆口诀

### 3. 交互功能
- 单词和短语有不同的视觉标识
- 点击其他区域或关闭按钮隐藏信息卡片

## 项目结构

```
English/
├── backend/              # 后端服务
│   ├── pom.xml          # Maven配置文件
│   ├── src/             # 源代码
│   │   └── main/
│   │       ├── java/    # Java代码
│   │       └── resources/# 资源文件
│   └── target/          # 编译输出
├── data/                # 数据文件
│   ├── words.csv        # 单词库
│   └── phrases.csv      # 短语库
├── frontend/            # 前端页面
│   ├── index.html       # 在线版本
│   ├── offline.html     # 离线版本
│   └── test.html        # 测试页面
└── README.md            # 项目说明文档
```

## 部署说明

### 本地部署（开发环境）

1. 确保已安装JDK 11
2. 克隆或下载项目到本地
3. 启动后端服务（参考快速开始部分）
4. 打开前端页面

### 生产环境部署

#### 1. 后端部署

```bash
# 构建可执行JAR文件
cd c:\Trae\English\backend
mvn clean package -DskipTests

# 运行JAR文件
java -jar target/word-phrase-memory-0.0.1-SNAPSHOT.jar
```

#### 2. 前端部署

将 `frontend` 目录下的文件部署到任何Web服务器（如Nginx、Apache等）：

```nginx
# Nginx配置示例
server {
    listen 80;
    server_name your-domain.com;
    
    location / {
        root /path/to/frontend;
        index index.html;
    }
    
    location /api {
        proxy_pass http://localhost:8080/api;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 词库说明

### 单词库（data/words.csv）
包含29个高频英语单词，字段说明：
- id: 单词ID
- word: 单词
- lemma: 原型
- pronunciation: 读音
- derivation: 派生词
- tip: 记忆口诀

### 短语库（data/phrases.csv）
包含25个常用英语短语，字段说明：
- phrase: 短语
- len: 短语长度
- main_idx: 主词索引
- pronunciation: 读音
- derivation: 派生词
- tip: 记忆口诀

## 注意事项

1. **关于API**：该系统完全使用本地API，不依赖任何需要购买的第三方API
2. **浏览器兼容性**：建议使用Chrome或Firefox浏览器以获得最佳体验
3. **离线版本**：离线版本功能完整，但仅包含内置的词库和短语库
4. **性能优化**：系统采用缓存机制提高查询速度，首次加载可能稍慢
5. **数据安全**：所有数据存储在本地，不会上传到任何服务器

## 常见问题

### Q: 为什么页面显示黑屏？
A: 可能是浏览器兼容性问题或JavaScript错误，请尝试以下解决方案：
- 使用Chrome或Firefox浏览器
- 检查浏览器控制台是否有错误信息
- 使用离线版本（offline.html）

### Q: 为什么点击单词没有反应？
A: 请确保：
- 后端服务正在运行
- 网络连接正常
- 已点击"处理文章"按钮进行分词

### Q: 如何添加新的单词或短语？
A: 可以直接编辑 `data/words.csv` 和 `data/phrases.csv` 文件，添加新的条目

## 更新日志

### v1.0.0
- 完成基本功能开发
- 实现单词和短语识别
- 提供在线和离线版本
- 添加详细的README文档

## 许可证

本项目仅供学习和研究使用。

## 联系方式

如有问题或建议，请联系项目维护者。