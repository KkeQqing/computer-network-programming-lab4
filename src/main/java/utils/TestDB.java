package utils;

import java.sql.Connection;
import java.sql.SQLException;

public class TestDB {
    public static void main(String[] args) {
        try (Connection conn = DBUtil.getConnection()) {
            if (conn != null) {
                System.out.println("✅ 成功连接到数据库！");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}