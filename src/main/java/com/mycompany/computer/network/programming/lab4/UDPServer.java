package com.mycompany.computer.network.programming.lab4;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dao.ChatRoomDAO;
import dao.GroupMessageDAO;
import dao.PrivateMessageDAO;
import dao.UserDAO;
import model.User;

public class UDPServer {
    private static final int PORT = 9999;
    private static final Gson gson = new Gson();
    private static final ExecutorService threadPool = Executors.newCachedThreadPool(); // 线程池

    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            System.out.println("UDP 聊天服务器启动，监听端口: " + PORT);

            while (true) {
                byte[] buffer = new byte[65536];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                threadPool.submit(() -> handleRequest(socket, packet));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 处理请求
    private static void handleRequest(DatagramSocket socket, DatagramPacket packet) {
        String jsonStr = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
        try {
            JsonObject request = JsonParser.parseString(jsonStr).getAsJsonObject();
            String action = request.get("action").getAsString();

            JsonObject response = new JsonObject();
            response.addProperty("action", action);

            switch (action) {
                case "login":
                    handleLogin(request, response);
                    break;
                case "register":
                    handleRegister(request, response);
                    break;
                case "sendGroupMessage":
                    handleSendGroupMessage(request, response);
                    break;
                case "sendPrivateMessage":
                    handleSendPrivateMessage(request, response);
                    break;
                case "createRoom":
                    handleCreateRoom(request, response);
                    break;
                // 可扩展更多 action
                default:
                    response.addProperty("success", false);
                    response.addProperty("message", "未知操作");
            }

            // 发送响应
            sendResponse(socket, packet.getAddress(), packet.getPort(), response);

        } catch (Exception e) {
            e.printStackTrace();
            JsonObject error = new JsonObject();
            error.addProperty("success", false);
            error.addProperty("message", "服务器内部错误");
            sendResponse(socket, packet.getAddress(), packet.getPort(), error);
        }
    }

    // --- 具体处理方法 ---
    private static void handleLogin(JsonObject req, JsonObject res) {
        String username = req.get("username").getAsString();
        String password = req.get("password").getAsString();
        UserDAO userDAO = new UserDAO();
        User user = userDAO.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            userDAO.updateStatus(user.getId(), "online");
            res.addProperty("success", true);
            res.addProperty("userId", user.getId());
            res.addProperty("message", "登录成功");
        } else {
            res.addProperty("success", false);
            res.addProperty("message", "用户名或密码错误");
        }
    }

    private static void handleRegister(JsonObject req, JsonObject res) {
        String username = req.get("username").getAsString();
        String password = req.get("password").getAsString();
        UserDAO userDAO = new UserDAO();
        if (userDAO.findByUsername(username) != null) {
            res.addProperty("success", false);
            res.addProperty("message", "用户名已存在");
        } else {
            boolean ok = userDAO.insert(username, password);
            res.addProperty("success", ok);
            res.addProperty("message", ok ? "注册成功" : "注册失败");
        }
    }

    private static void handleSendGroupMessage(JsonObject req, JsonObject res) {
        int roomId = req.get("roomId").getAsInt();
        int senderId = req.get("senderId").getAsInt();
        String message = req.get("message").getAsString();
        boolean ok = new GroupMessageDAO().insert(roomId, senderId, message);
        res.addProperty("success", ok);
        res.addProperty("message", ok ? "消息发送成功" : "发送失败");

        // TODO: 可广播给该房间所有在线成员（需维护在线用户表）
        if (ok) {
            broadcastToRoom(roomId, req); // 异步推送
        }
    }

    private static void handleSendPrivateMessage(JsonObject req, JsonObject res) {
        int senderId = req.get("senderId").getAsInt();
        int receiverId = req.get("receiverId").getAsInt();
        String message = req.get("message").getAsString();
        boolean ok = new PrivateMessageDAO().insert(senderId, receiverId, message);
        res.addProperty("success", ok);
        res.addProperty("message", ok ? "私聊发送成功" : "发送失败");

        // TODO: 若 receiver 在线，直接 UDP 推送
    }

    private static void handleCreateRoom(JsonObject req, JsonObject res) {
        String name = req.get("name").getAsString();
        int creatorId = req.get("creatorId").getAsInt();
        ChatRoomDAO roomDAO = new ChatRoomDAO();
        if (roomDAO.findByName(name) != null) {
            res.addProperty("success", false);
            res.addProperty("message", "房间名已存在");
        } else {
            boolean ok = roomDAO.insert(name, creatorId);
            res.addProperty("success", ok);
            res.addProperty("message", ok ? "创建成功" : "创建失败");
        }
    }

    // --- 工具方法 ---
    private static void sendResponse(DatagramSocket socket, InetAddress addr, int port, JsonObject response) {
        try {
            byte[] data = gson.toJson(response).getBytes(StandardCharsets.UTF_8);
            DatagramPacket outPacket = new DatagramPacket(data, data.length, addr, port);
            socket.send(outPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 扩展：广播给房间内所有在线用户（需额外维护 <userId, InetAddress+port> 映射）
    private static void broadcastToRoom(int roomId, JsonObject messageJson) {
        // 需要一个全局 Map<Integer, InetSocketAddress> onlineUsers;
        // 此处简化：暂不实现推送，客户端定时拉取
    }
}