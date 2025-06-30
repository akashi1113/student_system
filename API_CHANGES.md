# API 变更说明

## 概述
本次更新将成绩管理模块和学习效果分析模块从查询ID的方式改为获取当前登录用户的信息。

## 变更内容

### 1. 新增用户上下文工具类
- 文件：`src/main/java/com/csu/sms/util/UserContext.java`
- 功能：提供获取当前登录用户ID的工具方法
- 支持从请求头 `X-User-Id` 或请求参数 `userId` 获取用户ID

### 2. 成绩分析模块API变更

#### 原有接口（已废弃，建议使用新接口）
- `GET /api/grade-analysis/exam-records/{userId}` - 获取指定用户的考试记录
- `GET /api/grade-analysis/study-records/{userId}` - 获取指定用户的学习记录
- `GET /api/grade-analysis/analysis/{userId}` - 获取指定用户的综合分析
- `GET /api/grade-analysis/chart/{userId}` - 获取指定用户的图表数据

#### 新接口（推荐使用）
- `GET /api/grade-analysis/exam-records` - 获取当前用户的考试记录
- `GET /api/grade-analysis/study-records` - 获取当前用户的学习记录
- `GET /api/grade-analysis/analysis` - 获取当前用户的综合分析
- `GET /api/grade-analysis/charts/{chartType}` - 获取当前用户的图表数据

#### 管理员接口（保留）
- `GET /api/grade-analysis/admin/exam-records/{userId}` - 获取指定用户的考试记录
- `GET /api/grade-analysis/admin/study-records/{userId}` - 获取指定用户的学习记录
- `GET /api/grade-analysis/admin/analysis/{userId}` - 获取指定用户的综合分析
- `GET /api/grade-analysis/admin/charts/{userId}/{chartType}` - 获取指定用户的图表数据

### 3. AI学习建议模块API变更

#### 新增接口
- `GET /api/ai/learning-suggestions` - 获取当前用户的个性化学习建议

#### 保留接口
- `POST /api/ai/learning-suggestions` - 获取个性化学习建议（原有接口）

### 4. 全局异常处理
- 文件：`src/main/java/com/csu/sms/controller/GlobalExceptionHandler.java`
- 功能：统一处理用户上下文相关异常

## 使用方式

### 前端调用示例

#### 方式1：通过请求头传递用户ID
```javascript
// 获取当前用户的考试记录
fetch('/api/grade-analysis/exam-records?pageNum=1&pageSize=10', {
  headers: {
    'X-User-Id': '123', // 当前登录用户的ID
    'Content-Type': 'application/json'
  }
})
.then(response => response.json())
.then(data => console.log(data));
```

#### 方式2：通过请求参数传递用户ID
```javascript
// 获取当前用户的学习记录
fetch('/api/grade-analysis/study-records?pageNum=1&pageSize=10&userId=123', {
  headers: {
    'Content-Type': 'application/json'
  }
})
.then(response => response.json())
.then(data => console.log(data));
```

#### 获取当前用户的AI学习建议
```javascript
// 获取当前用户的AI学习建议
fetch('/api/ai/learning-suggestions?startDate=2024-01-01 00:00:00&endDate=2024-12-31 23:59:59', {
  headers: {
    'X-User-Id': '123', // 当前登录用户的ID
    'Content-Type': 'application/json'
  }
})
.then(response => response.json())
.then(data => console.log(data));
```

## 注意事项

1. **用户ID传递**：前端需要在请求中传递当前登录用户的ID，可以通过请求头 `X-User-Id` 或请求参数 `userId` 传递。

2. **错误处理**：如果未传递用户ID或用户ID无效，API会返回401错误，提示"请先登录"。

3. **向后兼容**：原有的管理员接口仍然保留，可以用于查看指定用户的数据。

4. **权限控制**：建议在实际部署时添加适当的权限验证机制，确保用户只能访问自己的数据。

## 迁移建议

1. **前端修改**：将原有的通过路径参数传递用户ID的方式改为通过请求头或请求参数传递。

2. **接口调用**：使用新的API接口，不再需要在URL中包含用户ID。

3. **错误处理**：添加对401错误的处理，当用户未登录时引导用户到登录页面。

4. **测试验证**：确保所有相关功能在修改后正常工作。 