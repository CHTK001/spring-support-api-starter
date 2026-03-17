import java.sql.*;
public class UnlockUser {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://172.16.0.40:3306/system?useSSL=false&allowPublicKeyRetrieval=true";
        try (Connection c = DriverManager.getConnection(url, "root", "root@");
             Statement s = c.createStatement()) {
            // 先查字段
            ResultSet rs = s.executeQuery("SHOW COLUMNS FROM sys_user LIKE '%lock%'");
            System.out.println("=== lock columns ===");
            while (rs.next()) System.out.println(rs.getString(1));
            rs = s.executeQuery("SHOW COLUMNS FROM sys_user LIKE '%fail%'");
            System.out.println("=== fail columns ===");
            while (rs.next()) System.out.println(rs.getString(1));
            rs = s.executeQuery("SELECT * FROM sys_user WHERE sys_user_username='sa' LIMIT 1");
            ResultSetMetaData md = rs.getMetaData();
            System.out.println("=== all columns ===");
            for (int i = 1; i <= md.getColumnCount(); i++) System.out.println(md.getColumnName(i));
        }
    }
}
