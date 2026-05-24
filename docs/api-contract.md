# API 契约

## Java 业务系统

### 1. 健康检查

- `GET /api/health`

### 2. 标准问答

- `POST /api/advisor/ask`

请求示例：

```json
{
  "question": "动火作业前需要确认哪些内容？"
}
```

响应示例：

```json
{
  "question": "动火作业前需要确认哪些内容？",
  "answer": "...",
  "sources": [],
  "risk_level": "HIGH"
}
```

### 3. 场景分析

- `POST /api/advisor/analyze-scenario`

请求示例：

```json
{
  "scenario": "车间检修时进行受限空间作业，需要审批、通风和监护。",
  "tenantId": 1
}
```

### 4. 清单生成

- `POST /api/advisor/generate-checklist`

请求示例：

```json
{
  "scenario": "临时用电作业",
  "tenantId": 1
}
```

### 5. 票证预审

- `POST /api/advisor/precheck-ticket`

请求示例：

```json
{
  "ticketType": "动火票",
  "content": {
    "monitoring": true,
    "gas_detection": true,
    "approval": true
  },
  "tenantId": 1
}
```

### 6. 文档入库

- `POST /api/documents/ingest`

请求示例：

```json
{
  "document_code": "GB-XXXX",
  "document_name": "示例标准",
  "doc_type": "GB",
  "version": "1.0",
  "source_uri": "file:///sample.pdf",
  "raw_text": "第一条......",
  "chunks": []
}
```

### 7. 文档上传

- `POST /api/documents/upload`
- `multipart/form-data`

字段：

- `file`
- `document_name`
- `doc_type`
- `document_code`，可选
- `version`，可选
- `source_uri`，可选
- `tenant_id`，可选

### 8. 标准库浏览

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

说明：

- `library` 返回当前初始化的标准目录
- `search` 按标准号、名称、标签、场景和摘要检索
- `detail` 按 `code` 获取单条标准详情
- `import` 用于批量导入或更新标准条目
- `batch/import` 用于批量导入当前 JSON 清单
- `preview` 用于在保存前校验条目并输出字段差异
- `compare` 用于对比当前版本与历史版本的字段差异
- `export` 用于导出整个标准库
- `template` 用于获取标准条目导入模板
- `update` 用于按 `code` 单条修改标准条目
- `delete` 用于按 `code` 软删除条目
- `batch/archive` 用于批量归档条目
- `batch/restore` 用于批量恢复条目
- `sync` 用于将标准库同步到 AI 知识库
- `versions` 用于查看某条标准的版本历史
- `restore` 用于把某条标准恢复到指定版本
- `seed` 用于重建默认标准种子

`sync` 响应包含：
- `total`：待同步条目数
- `synced`：成功同步条目数
- `failed`：失败条目数
- `syncedCodes`：成功条目标号列表
- `failedCodes`：失败条目标号列表

`preview` 响应包含：

- `issues`：校验问题列表
- `diffs`：字段差异列表

### 9. 标准版本历史页面

- `/standard-versions.html`

说明：

- 可按标准号查看版本历史
- 可查看快照、对比当前版本并恢复历史版本

导入请求示例：

```json
{
  "items": [
    {
      "code": "GB 30871-2022",
      "name": "危险化学品企业特殊作业安全规范",
      "status": "现行",
      "publishDate": "2022-03-15",
      "implementDate": "2022-10-01",
      "scope": "危险化学品企业的特殊作业管理",
      "summary": "重点面向特殊作业许可、审批、现场条件确认、监护、检测、隔离和应急准备。",
      "tags": ["特殊作业", "作业票"],
      "scenarios": ["动火作业", "受限空间"],
      "sourceUrl": "https://std.samr.gov.cn/..."
    }
  ]
}
```

## Python AI 服务

### 1. 健康检查

- `GET /health`

### 2. 标准问答

- `POST /ask`

### 3. 场景分析

- `POST /analyze-scenario`

### 4. 清单生成

- `POST /generate-checklist`

### 5. 票证预审

- `POST /precheck-ticket`

### 6. 文档入库

- `POST /ingest-document`

请求示例：

```json
{
  "document_code": "GB-XXXX",
  "document_name": "示例标准",
  "doc_type": "GB",
  "version": "1.0",
  "source_uri": "file:///sample.pdf",
  "raw_text": "第一条......",
  "chunks": []
}
```

### 7. 文件入库

- `POST /ingest-file`
- `multipart/form-data`

字段：

- `file`
- `document_name`
- `doc_type`
- `document_code`，可选
- `version`，可选
- `source_uri`，可选
- `tenant_id`，可选

## 响应约定

- 所有答案都应返回来源列表
- 风险等级建议使用：
  - `LOW`
  - `MEDIUM`
  - `HIGH`
  - `UNKNOWN`
