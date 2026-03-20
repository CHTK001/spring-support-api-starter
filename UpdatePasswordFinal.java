import java.sql.*;

public class UpdatePasswordFinal {
    public static void main(String[] args) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");

        String url = "jdbc:mysql://172.16.0.40:3306/system?useSSL=false&allowPublicKeyRetrieval=true";
        String user = "root";
        String pass = "root@";

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("Connected to database!");

            // Update password: MD5(MD5("admin123") + "abc123")
            String newPassword = "d4b9ef56c7fb834fa1eb3e0114cc95f3";
            String updateSql = "UPDATE sys_user SET sys_user_password = ? WHERE sys_user_username = 'sa'";

            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setString(1, newPassword);
                int rows = pstmt.executeUpdate();
                System.out.println("Updated " + rows + " row(s)");
                System.out.println("\n✅ Password updated!");
                System.out.println("Now you can login with:");
                System.out.println("  Username: sa");
                System.out.println("  Password: admin123");
            }

            // Verify
            String selectSql = "SELECT sys_user_username, sys_user_password FROM sys_user WHERE sys_user_username = 'sa'";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(selectSql)) {
                if (rs.next()) {
                    System.out.println("\nVerification:");
                    System.out.println("  Username: " + rs.getString("sys_user_username"));
                    System.out.println("  Password (MD5): " + rs.getString("sys_user_password"));
                }
            }
        }
    }
}
