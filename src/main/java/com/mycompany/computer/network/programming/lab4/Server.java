package com.mycompany.computer.network.programming.lab4;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

public class Server {
    private static final int PORT = 9000;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/UDP_test";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "your_password";

    private Map<String, Integer> onlineUsers = new HashMap<>(); // username -> socket port
    private List<String> rooms = Arrays.asList("room1", "romm2"); // 初始房间名
    private Map<String, Set<String>> roomMembers = new HashMap<>();

    public void start() throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("服务器启动在端口：" + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        }
    }

    private class ClientHandler implements Runnable {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

                while (true) {
                    String request = (String) in.readObject();
                    System.out.println("收到请求：" + request);

                    JSONObject json = new JSONObject(request);
                    String type = json.getString("type");

                    switch (type) {
                        case "login":
                            login(json, out);
                            break;
                        case "register":
                            register(json, out);
                            break;
                        case "logout":
                            logout(json, out);
                            break;
                        case "get_room_list":
                            sendRoomList(out);
                            break;
                        case "create_room":
                            createRoom(json, out);
                            break;
                        case "join_room":
                            joinRoom(json, out);
                            break;
                        case "get_user_list":
                            getUserList(json, out);
                            break;
                        case "send_group_msg":
                            sendGroupMessage(json, out);
                            break;
                        case "send_private_msg":
                            sendPrivateMessage(json, out);
                            break;
                        default:
                            out.writeObject("{\"error\": \"未知命令\"}");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void login(JSONObject json, ObjectOutputStream out) throws SQLException {
            String username = json.getString("username");
            String password = json.getString("password");

            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT id FROM users WHERE username = ? AND password = ?");
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                onlineUsers.put(username, socket.getPort());
                out.writeObject("{\"status\": \"success\", \"msg\": \"登录成功\"}");
            } else {
                out.writeObject("{\"status\": \"fail\", \"msg\": \"用户名或密码错误\"}");
            }
            conn.close();
        }

        private void register(JSONObject json, ObjectOutputStream out) throws SQLException {
            String username = json.getString("username");
            String password = json.getString("password");

            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO users (username, password) VALUES (?, ?)");
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();
            conn.close();

            out.writeObject("{\"status\": \"success\", \"msg\": \"注册成功\"}");
        }

        private void logout(JSONObject json, ObjectOutputStream out) {
            String username = json.getString("username");
            onlineUsers.remove(username);
            out.writeObject("{\"status\": \"success\", \"msg\": \"已注销\"}");
        }

        private void sendRoomList(ObjectOutputStream out) {
            JSONObject response = new JSONObject();
            response.put("rooms", rooms);
            try {
                out.writeObject(response.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void createRoom(JSONObject json, ObjectOutputStream out) {
            String roomName = json.getString("name");
            rooms.add(roomName);
            roomMembers.put(roomName, new HashSet<>());
            out.writeObject("{\"status\": \"success\", \"msg\": \"创建成功\"}");
        }

        private void joinRoom(JSONObject json, ObjectOutputStream out) {
            String username = json.getString("username");
            String roomName = json.getString("room");
            roomMembers.get(roomName).add(username);
            out.writeObject("{\"status\": \"success\", \"msg\": \"加入成功\"}");
        }

        private void getUserList(JSONObject json, ObjectOutputStream out) {
            String roomName = json.getString("room");
            Set<String> users = roomMembers.get(roomName);
            JSONObject response = new JSONObject();
            response.put("users", new ArrayList<>(users));
            try {
                out.writeObject(response.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void sendGroupMessage(JSONObject json, ObjectOutputStream out) {
            String roomName = json.getString("room");
            String sender = json.getString("sender");
            String msg = json.getString("message");

            for (String user : roomMembers.get(roomName)) {
                if (!user.equals(sender)) {
                    // 广播给其他成员
                    broadcastToUser(user, "{\"type\": \"group_message\", \"sender\": \"" + sender + "\", \"msg\": \"" + msg + "\"}");
                }
            }
        }

        private void sendPrivateMessage(JSONObject json, ObjectOutputStream out) {
            String sender = json.getString("sender");
            String receiver = json.getString("receiver");
            String msg = json.getString("message");

            broadcastToUser(receiver, "{\"type\": \"private_message\", \"sender\": \"" + sender + "\", \"msg\": \"" + msg + "\"}");
        }

        private void broadcastToUser(String username, String message) {
            // 这里需要维护每个用户的 socket，实际中应保存 socket 或使用广播机制
            // 简化版本仅打印日志
            System.out.println("向 " + username + " 发送：" + message);
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