# 化工安全标准顾问 Agent

私有化部署的化工安全标准顾问 Agent，采用 Java 主系统 + Python AI 服务的分层架构。

## 目录结构

- `docs/`：架构、API 契约和示例数据
- `backend-java/`：业务主系统
- `ai-service-python/`：AI、知识检索、RAG、文档解析服务
- `scripts/`：演示导入脚本
- `docker-compose.yml`：本地依赖服务

## 快速启动

### 方式一：Docker Compose

```bash
docker compose up --build
```

### 方式二：分别启动

- Java 后端：`backend-java/`
- Python 服务：`ai-service-python/`

打开浏览器访问：

- `http://localhost:8080/`

## 当前页面能力

- 标准问答
- 场景分析
- 清单生成
- 票证预审
- 文档上传入库
- 服务状态检查
- 标准库浏览页面：`/standards.html`
- 标准库导入页面：`/standard-import.html`
- 标准库版本历史页面：`/standard-versions.html`
- 标准条目编辑、删除、版本查看与恢复，并可一键同步到知识库

说明：标准库现在是数据库持久化的，首次启动会自动写入默认种子，也可以通过导入接口继续追加和更新。

## 默认端口

- Java 后端：`8080`
- Python AI 服务：`8000`
- PostgreSQL：`5432`
- Qdrant：`6333`
- Redis：`6379`
- MinIO：`9000` / `9001`

## 容器内服务地址

- Java -> Python：`AI_SERVICE_URL=http://ai-service-python:8000`
- Python -> Qdrant：`QDRANT_URL=http://qdrant:6333`

## 已有接口

- `POST /api/advisor/ask`
- `POST /api/advisor/analyze-scenario`
- `POST /api/advisor/generate-checklist`
- `POST /api/advisor/precheck-ticket`
- `GET /api/standards/library`
- `GET /api/standards/library/search?q=...`
- `GET /api/standards/library/detail?code=...`
- `POST /api/standards/library/import`
- `POST /api/standards/library/batch/import`
- `POST /api/standards/library/preview?code=...`
- `GET /api/standards/library/compare?code=...&versionNo=...`
- `GET /api/standards/library/export?format=json|markdown|csv`
- `GET /api/standards/library/template`
- `PUT /api/standards/library/detail?code=...`
- `DELETE /api/standards/library/detail?code=...`
- `POST /api/standards/library/batch/archive`
- `POST /api/standards/library/batch/restore`
- `POST /api/standards/library/sync`
- `GET /api/standards/library/versions?code=...`
- `POST /api/standards/library/restore?code=...&versionNo=...`
- `POST /api/standards/library/seed`
- `POST /api/documents/ingest`
- `POST /api/documents/upload`
- `POST /ask`
- `POST /ingest-document`
- `POST /ingest-file`

## 目前支持的入库文件

- `txt`
- `md`
- `pdf`
- `docx`

说明：

- `pdf` 和 `docx` 会先在 Python 服务里抽取文本，再自动切分入库
- `txt` 和 `md` 会按 UTF-8 文本直接入库
- 扫描版 PDF 当前先做文本抽取，后续可以继续增强 OCR
- 图片文件会尽量做 OCR 识别

## OCR 说明

- 图片文件会尝试 OCR 识别
- 扫描版 PDF 先尝试文本抽取，后续可以继续增强 OCR

## 演示导入

启动 Python 服务后执行：

```bash
python scripts/seed_demo.py
```

这会把 [`docs/sample_standard_hot_work.md`](docs/sample_standard_hot_work.md) 导入知识库，并立刻发起一次标准问答演示。

PowerShell 一键版本：

```powershell
.\scripts\run_demo.ps1
```

国标初始化：

```bash
python scripts/seed_standards.py
```

这会把 [`docs/seed_national_standards.md`](docs/seed_national_standards.md) 里的几条标准摘要初始化到知识库里。

PowerShell 版本：

```powershell
python scripts\seed_standards.py
```

标准库一键初始化：

```bash
python scripts/seed_standard_library.py
```

标准库导入接口：

```bash
POST /api/standards/library/import
```

标准库模板和导出：

```bash
GET /api/standards/library/template
GET /api/standards/library/export?format=json|markdown|csv
```

标准库批量操作：

```bash
POST /api/standards/library/batch/import
POST /api/standards/library/batch/archive
POST /api/standards/library/batch/restore
POST /api/standards/library/sync
```

标准库单条管理：

```bash
POST /api/standards/library/preview?code=...
GET /api/standards/library/compare?code=...&versionNo=...
PUT /api/standards/library/detail?code=...
DELETE /api/standards/library/detail?code=...
GET /api/standards/library/versions?code=...
POST /api/standards/library/restore?code=...&versionNo=...
```

`preview` 会返回校验问题和字段差异，适合在保存前做确认。

标准库重建种子：

```bash
POST /api/standards/library/seed
```

## 目标

- 标准问答
- 场景顾问
- 检查清单生成
- 票证预审
- 私有化部署

## 本地启动依赖

- PostgreSQL
- Qdrant
- Redis
- MinIO

## 下一步

1. 完成 Java 后端的数据库模型和接口
2. 完成 Python AI 服务的检索与问答流程
3. 接入文档解析和知识库入库
