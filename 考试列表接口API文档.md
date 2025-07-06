# 考试列表接口API文档

## 接口概述

获取考试列表，用于前端下拉选择框展示。

## 接口详情

### 获取考试列表

**请求方式：** GET  
**接口路径：** `/api/exams/list`

**请求参数：**
- 无参数：返回所有可用考试列表

**请求示例：**
```
GET /api/exams/list
```

**响应格式：**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "2024年春季期中考试"
    },
    {
      "id": 2,
      "name": "2024年春季期末考试"
    }
  ]
}
```

## 字段说明

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 考试唯一标识 |
| name | String | 考试名称（用于前端显示） |

## 错误响应

```json
{
  "code": 500,
  "message": "获取考试列表失败: 具体错误信息",
  "data": null
}
```

## 使用场景

1. **前端下拉选择框**：在教师分析界面中，用于选择要分析的考试
2. **考试管理**：在考试管理页面展示考试列表

## 注意事项

1. 接口返回的是可用的考试列表（status为PUBLISHED的考试）
2. 返回数据按考试创建时间倒序排列（最新的考试在前）

## 前端集成示例

```javascript
// 获取所有考试列表
fetch('/api/exams/list')
  .then(response => response.json())
  .then(data => {
    if (data.code === 200) {
      // 填充下拉选择框
      const examSelect = document.getElementById('examSelect');
      data.data.forEach(exam => {
        const option = document.createElement('option');
        option.value = exam.id;
        option.textContent = exam.name;
        examSelect.appendChild(option);
      });
    }
  });
``` 