-- 操作日志表
CREATE TABLE IF NOT EXISTS operation_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT COMMENT '用户ID',
    username VARCHAR(50) COMMENT '用户名',
    module VARCHAR(50) COMMENT '操作模块',
    operation VARCHAR(50) COMMENT '操作类型',
    description VARCHAR(200) COMMENT '操作描述',
    request_url VARCHAR(500) COMMENT '请求URL',
    request_method VARCHAR(10) COMMENT '请求方法',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '用户代理',
    request_params TEXT COMMENT '请求参数',
    response_result TEXT COMMENT '响应结果',
    execution_time BIGINT COMMENT '执行时间（毫秒）',
    status VARCHAR(20) COMMENT '操作状态',
    error_message TEXT COMMENT '错误信息',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_module (module),
    INDEX idx_operation (operation),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 系统日志表
CREATE TABLE IF NOT EXISTS system_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    level VARCHAR(20) COMMENT '日志级别',
    type VARCHAR(50) COMMENT '日志类型',
    title VARCHAR(200) COMMENT '日志标题',
    content TEXT COMMENT '日志内容',
    source VARCHAR(100) COMMENT '来源模块',
    stack_trace TEXT COMMENT '堆栈信息',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_level (level),
    INDEX idx_type (type),
    INDEX idx_source (source),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统日志表';

-- 插入示例数据
INSERT INTO `operation_log` (`user_id`, `username`, `module`, `operation`, `method`, `ip_address`, `status`, `create_time`) VALUES
(1, 'admin', '用户管理', '用户登录', 'UserController.login', '127.0.0.1', 1, NOW()),
(1, 'admin', '课程管理', '查看课程列表', 'CourseController.list', '127.0.0.1', 1, NOW()),
(2, 'student1', '考试管理', '提交考试答案', 'ExamController.submit', '127.0.0.1', 1, NOW());

INSERT INTO `system_log` (`level`, `module`, `message`, `create_time`) VALUES
('INFO', '系统启动', '学生管理系统启动成功', NOW()),
('INFO', '数据库连接', '数据库连接池初始化完成', NOW()),
('INFO', 'Redis连接', 'Redis连接成功', NOW()); 