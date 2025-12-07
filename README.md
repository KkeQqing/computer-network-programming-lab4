实验
三 使用 线程 池 模式 设计 “石头、剪刀、布”游戏
一、实验目的
（1）理解并掌握线程池 概念、原理及线程调度方法。
（2）理解并掌握 ExecutorService创建线程池的几种方法。
（3）使用线程池模式实现“石头、剪刀、布”游戏服务器和客户端设计。
二、实验内容
日常生活中存在大量有形无形的排队或拥挤现象，线程池所体现的技术思想与生活中的排队现象一脉相承，根据服务器资源负载能力，预先设定一个比较有效率的客户线程规模，当客户机请求总数超过规模限制时采取排队等候的方法，只有当线程池中的某个或某些线程结束，排队的客户请求才能依次进入线程池得到即时服务。线程池工作原理如下图。
三、实验方法与步骤
（
（11）编写客户端程序：）编写客户端程序：
1
1、、设计客户端游戏界面。设计客户端游戏界面。
2
2、、创建客户端创建客户端SocketSocket，连接服务器端主机和端口。，连接服务器端主机和端口。
3
3、、发送客户端选择给服务器发送客户端选择给服务器。。
4
4、接收服务器处理并返回结果。、接收服务器处理并返回结果。
5
5、显示游戏结果。、显示游戏结果。
（
（22）编写服务器端程序：）编写服务器端程序：
1
1、创建服务器端、创建服务器端ServerSocketServerSocket。。
2
2、、创建线程池并指定池大小创建线程池并指定池大小。。
3
3、、接受客户端链接并执行线程池调度管理接受客户端链接并执行线程池调度管理。。
4
4、服务器根据游戏协议计算游戏结果。、服务器根据游戏协议计算游戏结果。
5
5、返回结果给客、返回结果给客户端。户端。
（
（33）编译客户端和服务器端程序，并生成相应的类文件。）编译客户端和服务器端程序，并生成相应的类文件。
（
（44）先运行服务端程序，再运行客户端程序，观察运行结果。）先运行服务端程序，再运行客户端程序，观察运行结果。
四、边实验边思考
（
（11））在什么情况下使用线程池？在什么情况下使用线程池？
（
（22））使用线程池有什么使用线程池有什么好处好处？？
五、实验结论及心得体会
根据实验情况，撰写实验报告
根据实验情况，撰写实验报告,,简明扼要记录实验过程、实验结果，提出实简明扼要记录实验过程、实验结果，提出实验问题，做出实验分析。根据实验情况认真回答上述实验思考题。验问题，做出实验分析。根据实验情况认真回答上述实验思考题。


后续需要修改：
将两秒一刷新改为有新增时再刷新（咕咕咕）


数据库创建
-- 创建数据库（如果不存在），并指定字符集与排序规则
CREATE DATABASE IF NOT EXISTS UDP_test
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- 使用该数据库
USE UDP_test;

-- 用户表：存储系统中的所有用户信息
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '用户唯一ID，自增主键',
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名，唯一且不能为空',
    password VARCHAR(100) NOT NULL COMMENT '用户密码',
    status ENUM('online', 'offline') DEFAULT 'offline' COMMENT '用户在线状态：online（在线）或 offline（离线）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '用户注册时间'
) COMMENT = '用户信息表';

-- 聊天室表：记录所有群聊房间的基本信息
CREATE TABLE chat_rooms (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '聊天室唯一ID，自增主键',
    name VARCHAR(100) UNIQUE NOT NULL COMMENT '聊天室名称，全局唯一',
    creator_id INT COMMENT '创建该聊天室的用户ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '聊天室创建时间',
    FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE SET NULL
) COMMENT = '群聊聊天室表';

-- 聊天室成员表：记录哪些用户加入了哪些聊天室
CREATE TABLE room_members (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '成员关系ID，自增主键',
    room_id INT COMMENT '所属聊天室ID',
    user_id INT COMMENT '用户ID',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '用户加入聊天室的时间',
    FOREIGN KEY (room_id) REFERENCES chat_rooms(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_room_user (room_id, user_id) COMMENT '确保同一用户不能重复加入同一聊天室'
) COMMENT = '聊天室成员关联表';

-- 群聊消息表：存储所有群聊消息
CREATE TABLE group_messages (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '消息唯一ID，自增主键',
    room_id INT COMMENT '消息所属的聊天室ID',
    sender_id INT COMMENT '发送消息的用户ID',
    message TEXT COMMENT '消息内容（支持长文本）',
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '消息发送时间',
    FOREIGN KEY (room_id) REFERENCES chat_rooms(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE
) COMMENT = '群聊消息记录表';

-- 私聊消息表：存储用户之间的私聊消息
CREATE TABLE private_messages (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '私聊消息唯一ID，自增主键',
    sender_id INT COMMENT '发送者用户ID',
    receiver_id INT COMMENT '接收者用户ID',
    message TEXT COMMENT '私聊消息内容',
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '消息发送时间',
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE
) COMMENT = '私聊消息记录表';

数据输入
-- 使用数据库
USE UDP_test;

-- 1. 插入用户数据（3个用户）
INSERT INTO users (username, password, status) VALUES
('admin', '123456', 'online'),
('bob',   '123456', 'online'),
('alice', '123456', 'offline');


-- 2. 插入聊天室数据（2个聊天室）
INSERT INTO chat_rooms (name, creator_id) VALUES
('room1', 1),   -- 由 admin 创建
('romm2', 2);     -- 由 bob 创建

-- 3. 插入聊天室成员（每个房间加入 2-3 人）
INSERT INTO room_members (room_id, user_id) VALUES
(1, 1),  -- admin in 项目讨论组
(1, 2),  -- bob in 项目讨论组
(1, 3),  -- alice in 项目讨论组
(2, 2),  -- bob in 闲聊天地
(2, 1);  -- admin in 闲聊天地

-- 4. 插入群聊消息（3条）
INSERT INTO group_messages (room_id, sender_id, message) VALUES
(1, 1, '大家好，今天的任务进度如何？'),
(1, 2, '我已完成前端部分！'),
(2, 2, '周末有什么计划？');

-- 5. 插入私聊消息（2条：alice → bob，bob → alice）
INSERT INTO private_messages (sender_id, receiver_id, message) VALUES
(1, 2, 'Hey Bob，能帮忙看下这个 bug 吗？'),
(2, 1, '当然可以，发我链接吧！');