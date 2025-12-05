// Server.java (UDP 版本)
package com.mycompany.computer.network.programming.lab4;

import org.json.JSONArray;
import org.json.JSONObject;
import utils.DBUtil;
import dao.*;
import model.*;

import java.net.*;
import java.sql.*;
import java.util.*;
import java.io.IOException;

public class Server {
    private static final int PORT = 9000;
    private Map<String, InetSocketAddress> onlineUsers = new HashMap<>(); // username -> (IP, port)
    private List<ChatRoom> roomsCache = new ArrayList<>();

    // 初始化房间缓存
    private void loadRoomsFromDB() {
        ChatRoomDAO roomDAO = new ChatRoomDAO();
        roomsCache = roomDAO.findAll();
    }

    public void start() throws Exception {
        loadRoomsFromDB();
        DatagramSocket socket = new DatagramSocket(PORT);
        System.out.println("UDP 服务器启动在端口: " + PORT);

        byte[] buffer = new byte[2048];
        while (true) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);

            String requestStr = new String(packet.getData(), 0, packet.getLength());
            InetAddress clientAddress = packet.getAddress();
            int clientPort = packet.getPort();

            try {
                JSONObject req = new JSONObject(requestStr);
                String type = req.getString("type");

                JSONObject response = handleRequest(req, clientAddress, clientPort);

                byte[] responseData = response.toString().getBytes();
                DatagramPacket responsePacket = new DatagramPacket(
                        responseData, responseData.length, clientAddress, clientPort
                );
                socket.send(responsePacket);

            } catch (Exception e) {
                e.printStackTrace();
                JSONObject error = new JSONObject()
                        .put("status", "error")
                        .put("msg", "服务器处理异常");
                socket.send(new DatagramPacket(error.toString().getBytes(), error.toString().length(), clientAddress, clientPort));
            }
        }
    }

    private JSONObject handleRequest(JSONObject req, InetAddress addr, int port) {
        String type = req.getString("type");

        switch (type) {
            case "register":
                return handleRegister(req);
            case "login":
                String username = req.getString("username");
                onlineUsers.put(username, new InetSocketAddress(addr, port));
                return handleLogin(req);
            case "logout":
                onlineUsers.remove(req.getString("username"));
                return new JSONObject().put("status", "success").put("msg", "已注销");
            case "get_room_list":
                return handleGetRoomList();
            case "create_room":
                JSONObject res = handleCreateRoom(req);
                if ("success".equals(res.getString("status"))) {
                    loadRoomsFromDB(); // 刷新缓存
                }
                return res;
            case "join_room":
                return handleJoinRoom(req);
            case "get_user_list":
                return handleGetUserList(req);
            case "send_group_msg":
                handleSendGroupMessage(req);
                return new JSONObject().put("status", "success");
            case "send_private_msg":
                handleSendPrivateMessage(req);
                return new JSONObject().put("status", "success");
            default:
                return new JSONObject().put("status", "error").put("msg", "未知操作类型");
        }
    }

    // ===== 以下是各操作的具体实现 =====

    private JSONObject handleRegister(JSONObject req) {
        String username = req.getString("username");
        String password = req.getString("password");
        UserDAO userDAO = new UserDAO();
        if (userDAO.findByUsername(username) != null) {
            return new JSONObject().put("status", "fail").put("msg", "用户名已存在");
        }
        // 插入数据库（需补充 User 表插入逻辑）
        String sql = "INSERT INTO users (username, password, status) VALUES (?, ?, 'offline')";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();
            return new JSONObject().put("status", "success").put("msg", "注册成功");
        } catch (SQLException e) {
            e.printStackTrace();
            return new JSONObject().put("status", "fail").put("msg", "数据库错误");
        }
    }

    private JSONObject handleLogin(JSONObject req) {
        String username = req.getString("username");
        String password = req.getString("password");
        UserDAO userDAO = new UserDAO();
        User user = userDAO.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            // 更新状态为 online
            userDAO.updateStatus(user.getId(), "online");
            return new JSONObject().put("status", "success").put("msg", "登录成功");
        }
        return new JSONObject().put("status", "fail").put("msg", "用户名或密码错误");
    }

    private JSONObject handleGetRoomList() {
        JSONArray arr = new JSONArray();
        for (ChatRoom room : roomsCache) {
            arr.put(room.getName());
        }
        return new JSONObject().put("rooms", arr);
    }

    private JSONObject handleCreateRoom(JSONObject req) {
        String name = req.getString("name");
        // 检查是否重名
        for (ChatRoom r : roomsCache) {
            if (r.getName().equals(name)) {
                return new JSONObject().put("status", "fail").put("msg", "房间已存在");
            }
        }
        ChatRoomDAO roomDAO = new ChatRoomDAO();
        boolean ok = roomDAO.insert(name, null); // creatorId 暂设为 null
        if (ok) {
            return new JSONObject().put("status", "success").put("msg", "创建成功");
        }
        return new JSONObject().put("status", "fail").put("msg", "数据库错误");
    }

    private JSONObject handleJoinRoom(JSONObject req) {
        // 可选：记录用户加入关系（需 room_members 表）
        // 这里简化：只要房间存在即可加入
        String room = req.getString("room");
        boolean exists = roomsCache.stream().anyMatch(r -> r.getName().equals(room));
        if (!exists) {
            return new JSONObject().put("status", "fail").put("msg", "房间不存在");
        }
        return new JSONObject().put("status", "success").put("msg", "加入成功");
    }

    private JSONObject handleGetUserList(JSONObject req) {
        String room = req.getString("room");
        // 简化：返回所有在线用户（实际应根据 room_members 关联）
        JSONArray users = new JSONArray();
        for (String user : onlineUsers.keySet()) {
            users.put(user);
        }
        return new JSONObject().put("users", users);
    }

    private void handleSendGroupMessage(JSONObject req) {
        String room = req.getString("room");
        String sender = req.getString("sender");
        String msg = req.getString("message");

        // 广播给所有在线用户（实际应只发给该房间成员）
        for (Map.Entry<String, InetSocketAddress> entry : onlineUsers.entrySet()) {
            String user = entry.getKey();
            if (!user.equals(sender)) { // 不发给自己
                sendToUser(user, new JSONObject()
                        .put("type", "group_message")
                        .put("room", room)
                        .put("sender", sender)
                        .put("message", msg)
                );
            }
        }
    }

    private void handleSendPrivateMessage(JSONObject req) {
        String receiver = req.getString("receiver");
        sendToUser(receiver, req); // 直接转发私聊消息
    }

    private void sendToUser(String username, JSONObject msg) {
        InetSocketAddress addr = onlineUsers.get(username);
        if (addr != null) {
            try {
                DatagramSocket socket = new DatagramSocket(); // 临时 socket 发送
                byte[] data = msg.toString().getBytes();
                DatagramPacket packet = new DatagramPacket(data, data.length, addr.getAddress(), addr.getPort());
                socket.send(packet);
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        try {
            new Server().start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}