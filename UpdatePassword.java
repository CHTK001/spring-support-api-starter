import java.sql.*;

public class UpdatePassword {
    public static void main(String[] args) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");

        String url = "jdbc:mysql://172.16.0.40:3306/system?useSSL=false&allowPublicKeyRetrieval=true";
        String user = "root";
        String pass = "root@";

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("Connected to database!");

            // Update sa user password to admin123 (MD5: 0192023a7bbd73250516f069df18b500)
            System.out.println("\nUpdating sa user password...");
            String newPassword = "0192023a7bbd73250516f069df18b500";
            String updateSql = "UPDATE sys_user SET sys_user_password = ? WHERE sys_user_username = 'sa'";

            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setString(1, newPassword);
                int rows = pstmt.executeUpdate();
                System.out.println("Updated " + rows + " row(s)");
                System.out.println("Password changed to: admin123");
            }

            // Verify
            System.out.println("\nVerification:");
            String selectSql = "SELECT sys_user_username, sys_user_password FROM sys_user WHERE sys_user_username = 'sa'";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(selectSql)) {
                if (rs.next()) {
                    System.out.println("  Username: " + rs.getString("sys_user_username"));
                    System.out.println("  Password (MD5): " + rs.getString("sys_user_password"));
                    System.out.println("\n✅ Password updated successfully!");
                    System.out.println("You can now login with:");
                    System.out.println("  Username: sa");
                    System.out.println("  Password: admin123");
                }
            }
        }
    }
}
