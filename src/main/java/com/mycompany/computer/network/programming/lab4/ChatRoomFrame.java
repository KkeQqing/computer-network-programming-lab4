/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.computer.network.programming.lab4;

import dao.*;
import model.ChatRoom;
import model.GroupMessage;
import model.PrivateMessage;
import model.User;
import utils.DBUtil;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author KQ
 */
public class ChatRoomFrame extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ChatRoomFrame.class.getName());

    private String currentUsername; // 当前登录用户名
    private Integer currentUserId;  // 当前登录用户ID
    private Integer selectedRoomId = null; // 当前选中的聊天室ID
    private Integer selectedPrivateUserId = null; // 当前私聊对象ID
    private javax.swing.Timer autoRefreshTimer; // 自动刷新定时器

    /**
     * Creates new form ChatRoomFrame
     */
    public ChatRoomFrame(String username) {
        this.currentUsername = username;
        User user = new UserDAO().findByUsername(username);
        if (user != null) {
            this.currentUserId = user.getId();
            // 设置用户为 online
            new UserDAO().updateStatus(currentUserId, "online");
        } else {
            JOptionPane.showMessageDialog(this, "用户不存在！");
            System.exit(0);
        }
        initComponents();

        jButton1.addActionListener(this::jButton1ActionPerformed);
        jButton2.addActionListener(this::jButton2ActionPerformed);
        jButton3.addActionListener(this::jButton3ActionPerformed);
        jButton4.addActionListener(this::jButton4ActionPerformed);
        jButton7.addActionListener(this::jButton7ActionPerformed);

        loadInitialData();

        startAutoRefresh();
    }

    // 初始化数据
    private void loadInitialData() {
        setupRoomListListener();
        setupUserListListener();

        // 加载初始数据
        refreshRoomListWithoutListener();
        refreshUserListWithoutListener();
        updateRoomLabel();
        updatePrivateUserLabel();
    }

    // 刷新房间列表
    private void refreshRoomList() {
        List<ChatRoom> rooms = new ChatRoomDAO().findAll();
        DefaultListModel<String> model = new DefaultListModel<>();
        for (ChatRoom room : rooms) {
            model.addElement(room.getName()); // 显示名称
        }
        jList1.setModel(model);
        jList1.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedName = jList1.getSelectedValue();
                if (selectedName != null) {
                    ChatRoom room = new ChatRoomDAO().findByName(selectedName);
                    if (room != null) {
                        selectedRoomId = room.getId();
                        updateRoomLabel();
                        loadGroupMessages();
                    }
                }
            }
        });
    }

    // 刷新群聊消息
    private void loadGroupMessages() {
        if (selectedRoomId == null) {
            jTextArea1.setText("请先选择一个聊天室");
            return;
        }
        List<GroupMessage> messages = new GroupMessageDAO().findByRoomId(selectedRoomId);
        StringBuilder sb = new StringBuilder();
        for (GroupMessage msg : messages) {
            User sender = new UserDAO().findById(msg.getSenderId());
            String senderName = sender != null ? sender.getUsername() : "未知";
            sb.append("[").append(msg.getSentAt()).append("] ")
                    .append(senderName).append(": ")
                    .append(msg.getMessage()).append("\n");
        }
        jTextArea1.setText(sb.toString());
        jTextArea1.setCaretPosition(jTextArea1.getDocument().getLength()); // 滚动到底部
    }

    // 刷新用户列表
    private void refreshUserList() {
        List<User> users = new UserDAO().findAll();
        DefaultListModel<String> model = new DefaultListModel<>();
        for (User u : users) {
            model.addElement(u.getUsername() + " (" + u.getStatus() + ")");
        }
        jList2.setModel(model);
        jList2.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String val = jList2.getSelectedValue();
                if (val != null) {
                    String username = val.split(" ")[0];
                    User user = new UserDAO().findByUsername(username);
                    if (user != null && !user.getId().equals(currentUserId)) {
                        selectedPrivateUserId = user.getId();
                        updatePrivateUserLabel();
                        loadPrivateMessages();
                    } else {
                        selectedPrivateUserId = null;
                        jLabel5.setText("私聊用户：");
                        jTextArea2.setText("不能与自己私聊");
                    }
                }
            }
        });
    }

    // 更新私聊标签
    private void updatePrivateUserLabel() {
        if (selectedPrivateUserId != null) {
            User u = new UserDAO().findById(selectedPrivateUserId);
            jLabel5.setText("私聊用户：" + (u != null ? u.getUsername() : "未知"));
        } else {
            jLabel5.setText("私聊用户：");
        }
    }

    // 刷新私聊消息
    private void loadPrivateMessages() {
        if (selectedPrivateUserId == null) {
            jTextArea2.setText("请选择一个私聊对象");
            return;
        }
        List<PrivateMessage> msgs = new PrivateMessageDAO().findConversation(currentUserId, selectedPrivateUserId);
        StringBuilder sb = new StringBuilder();
        for (PrivateMessage msg : msgs) {
            String senderName = msg.getSenderId().equals(currentUserId) ? "我" :
                    new UserDAO().findById(msg.getSenderId()).getUsername();
            sb.append("[").append(msg.getSentAt()).append("] ")
                    .append(senderName).append(": ")
                    .append(msg.getMessage()).append("\n");
        }
        jTextArea2.setText(sb.toString());
        jTextArea2.setCaretPosition(jTextArea2.getDocument().getLength());
    }

    // 更新房间标签
    private void updateRoomLabel() {
        String roomName = jList1.getSelectedValue();
        jLabel4.setText("所在房间：" + (roomName != null ? roomName : "未选择"));
    }

    // 启动自动刷新
    private void startAutoRefresh() {
        autoRefreshTimer = new javax.swing.Timer(2000, e -> {
            // 刷新房间列表（如果当前没选中房间，可能不需要重载消息）
            refreshRoomListWithoutListener(); // 避免重复添加监听器
            refreshUserListWithoutListener(); // 同理

            // 如果已选择房间，刷新群聊消息
            if (selectedRoomId != null) {
                loadGroupMessages();
            }

            // 如果已选择私聊对象，刷新私聊消息
            if (selectedPrivateUserId != null) {
                loadPrivateMessages();
            }
        });
        autoRefreshTimer.start();
    }

    // 停止自动刷新
    private void stopAutoRefresh() {
        if (autoRefreshTimer != null && autoRefreshTimer.isRunning()) {
            autoRefreshTimer.stop();
        }
    }

    // 刷新房间列表（不添加监听器）
    private void refreshRoomListWithoutListener() {
        List<ChatRoom> rooms = new ChatRoomDAO().findAll();
        DefaultListModel<String> model = new DefaultListModel<>();
        for (ChatRoom room : rooms) {
            model.addElement(room.getName());
        }
        jList1.setModel(model); // 替换模型，原有监听器仍然有效
    }

    // 刷新用户列表（不添加监听器）
    private void refreshUserListWithoutListener() {
        List<User> users = new UserDAO().findAll();
        DefaultListModel<String> model = new DefaultListModel<>();
        for (User u : users) {
            model.addElement(u.getUsername() + " (" + u.getStatus() + ")");
        }
        jList2.setModel(model);
    }

    // 设置房间列表监听器
    private void setupRoomListListener() {
        jList1.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedName = jList1.getSelectedValue();
                if (selectedName != null) {
                    ChatRoom room = new ChatRoomDAO().findByName(selectedName);
                    if (room != null) {
                        selectedRoomId = room.getId();
                        updateRoomLabel();
                        loadGroupMessages();
                    }
                }
            }
        });
    }

    // 设置用户列表监听器
    private void setupUserListListener() {
        jList2.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String val = jList2.getSelectedValue();
                if (val != null) {
                    String username = val.split(" ")[0];
                    User user = new UserDAO().findByUsername(username);
                    if (user != null && !user.getId().equals(currentUserId)) {
                        selectedPrivateUserId = user.getId();
                        updatePrivateUserLabel();
                        loadPrivateMessages();
                    } else {
                        selectedPrivateUserId = null;
                        jLabel5.setText("私聊用户：");
                        jTextArea2.setText("不能与自己私聊");
                    }
                }
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();
        jLabel1 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jTextField1 = new javax.swing.JTextField();
        jButton4 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jList2 = new javax.swing.JList<>();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();
        jTextField2 = new javax.swing.JTextField();
        jButton7 = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(153, 153, 153));

        jScrollPane1.setViewportView(jList1);

        jLabel1.setText("房间列表：");

        jButton2.setText("创建");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("删除");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE))
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(3, 3, 3)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton3)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane2.setViewportView(jTextArea1);

        jTextField1.setText("");
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jButton4.setText("发送");
        jButton4.setToolTipText("");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Microsoft YaHei UI", 0, 18)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("所在房间：");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(397, 397, 397)
                                .addComponent(jButton4))
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 469, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 385, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(113, 113, 113)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 232, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        jButton1.setForeground(new java.awt.Color(255, 0, 51));
        jButton1.setText("账号注销");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jPanel2.setBackground(new java.awt.Color(153, 153, 153));

        jScrollPane3.setViewportView(jList2);

        jLabel3.setText("用户列表：");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addGap(3, 3, 3)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTextArea2.setColumns(20);
        jTextArea2.setRows(5);
        jScrollPane4.setViewportView(jTextArea2);

        jTextField2.setText("");

        jButton7.setText("发送");

        jLabel5.setFont(new java.awt.Font("Microsoft YaHei UI", 0, 18)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("私聊用户：");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 373, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 463, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(119, 119, 119)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 249, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(33, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jTextField2, javax.swing.GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE)
                            .addComponent(jButton7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        jLabel2.setFont(new java.awt.Font("Microsoft YaHei UI", 0, 24)); // NOI18N
        jLabel2.setText("聊天室");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(197, 197, 197)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(23, 23, 23))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton1)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(59, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        String name = JOptionPane.showInputDialog(this, "请输入聊天室名称：");
        if (name != null && !name.trim().isEmpty()) {
            if (new ChatRoomDAO().findByName(name) != null) {
                JOptionPane.showMessageDialog(this, "聊天室名称已存在！");
                return;
            }
            boolean success = new ChatRoomDAO().insert(name, currentUserId);
            if (success) {
                // 自动加入该房间
                ChatRoom room = new ChatRoomDAO().findByName(name);
                new RoomMemberDAO().addMember(room.getId(), currentUserId);
                refreshRoomList();
                JOptionPane.showMessageDialog(this, "聊天室创建成功！");
            } else {
                JOptionPane.showMessageDialog(this, "创建失败！");
            }
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        String selectedName = jList1.getSelectedValue();
        if (selectedName == null) {
            JOptionPane.showMessageDialog(this, "请先选择一个聊天室！");
            return;
        }

        ChatRoom room = new ChatRoomDAO().findByName(selectedName);
        if (room == null) return;

        // 检查是否是创建者
        if (!Objects.equals(room.getCreatorId(), currentUserId)) {
            JOptionPane.showMessageDialog(this, "只有创建者才能删除该聊天室！");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "确定删除聊天室 [" + selectedName + "]？所有消息和成员将被清除！",
                "确认删除", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            // 删除聊天室（ON DELETE CASCADE 会自动删 messages 和 members）
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM chat_rooms WHERE id = ?")) {
                stmt.setInt(1, room.getId());
                if (stmt.executeUpdate() > 0) {
                    selectedRoomId = null;
                    refreshRoomList();
                    jTextArea1.setText("");
                    jLabel4.setText("所在房间：");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "删除失败：" + e.getMessage());
            }
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        if (selectedRoomId == null) {
            JOptionPane.showMessageDialog(this, "请先选择一个聊天室！");
            return;
        }
        String content = jTextField1.getText().trim();
        if (content.isEmpty()) return;

        boolean sent = new GroupMessageDAO().insert(selectedRoomId, currentUserId, content);
        if (sent) {
            jTextField1.setText("");
            loadGroupMessages(); // 刷新
        } else {
            JOptionPane.showMessageDialog(this, "发送失败！");
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        if (selectedPrivateUserId == null) {
            JOptionPane.showMessageDialog(this, "请先选择一个私聊用户！");
            return;
        }
        String content = jTextField2.getText().trim();
        if (content.isEmpty()) return;

        boolean sent = new PrivateMessageDAO().insert(currentUserId, selectedPrivateUserId, content);
        if (sent) {
            jTextField2.setText("");
            loadPrivateMessages();
        } else {
            JOptionPane.showMessageDialog(this, "私聊发送失败！");
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要永久删除账号 [" + currentUsername + "] 吗？此操作不可恢复！",
                "确认删除", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            // 删除用户（级联删除 room_members, private_messages, group_messages 中相关记录）
            try (Connection conn = DBUtil.getConnection()) {
                conn.setAutoCommit(false);
                // 先删除群聊消息、私聊消息、房间成员、房间（若为创建者）、最后用户
                String sql1 = "DELETE FROM group_messages WHERE sender_id = ?";
                String sql2 = "DELETE FROM private_messages WHERE sender_id = ? OR receiver_id = ?";
                String sql3 = "DELETE FROM room_members WHERE user_id = ?";
                String sql4 = "DELETE FROM chat_rooms WHERE creator_id = ?"; // 可选：是否允许删除房间？
                String sql5 = "DELETE FROM users WHERE id = ?";

                try (PreparedStatement ps1 = conn.prepareStatement(sql1);
                     PreparedStatement ps2 = conn.prepareStatement(sql2);
                     PreparedStatement ps3 = conn.prepareStatement(sql3);
                     PreparedStatement ps4 = conn.prepareStatement(sql4);
                     PreparedStatement ps5 = conn.prepareStatement(sql5)) {

                    ps1.setInt(1, currentUserId);
                    ps2.setInt(1, currentUserId);
                    ps2.setInt(2, currentUserId);
                    ps3.setInt(1, currentUserId);
                    ps4.setInt(1, currentUserId);
                    ps5.setInt(1, currentUserId);

                    ps1.executeUpdate();
                    ps2.executeUpdate();
                    ps3.executeUpdate();
                    ps4.executeUpdate(); // 删除其创建的房间（可选策略）
                    ps5.executeUpdate();

                    conn.commit();
                    JOptionPane.showMessageDialog(this, "账号已成功删除！");
                    System.exit(0);
                } catch (SQLException e) {
                    conn.rollback();
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "删除失败：" + e.getMessage());
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "数据库连接失败");
            }
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton7;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JList<String> jList1;
    private javax.swing.JList<String> jList2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    // End of variables declaration//GEN-END:variables
}
