import java.security.MessageDigest;
import java.sql.*;

public class UpdatePassword {
    static String md5(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hash = md.digest(input.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02x", b));
        return sb.toString();
    }
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://172.16.0.40:3306/system?useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true";
        String user = "root";
        String pass = "root@";
        String newPassword = "admin123";
        String salt = "abc123";
        String hash = md5(newPassword + salt);
        System.out.println("New hash: " + hash + " (password=" + newPassword + ", salt=" + salt + ")");
        
        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            // First show current users
            try (PreparedStatement ps = conn.prepareStatement("SELECT sys_user_id, sys_user_username, sys_user_password, sys_user_salt FROM sys_user LIMIT 10")) {
                ResultSet rs = ps.executeQuery();
                System.out.println("\nCurrent users:");
                while (rs.next()) {
                    System.out.println("  id=" + rs.getInt(1) + " username=" + rs.getString(2) + " pwd=" + rs.getString(3) + " salt=" + rs.getString(4));
                }
            }
            // Update sa user password
            try (PreparedStatement ps = conn.prepareStatement("UPDATE sys_user SET sys_user_password=?, sys_user_salt=? WHERE sys_user_username='sa'")) {
                ps.setString(1, hash);
                ps.setString(2, salt);
                int rows = ps.executeUpdate();
                System.out.println("\nUpdated " + rows + " rows for user 'sa' with password '" + newPassword + "'");
            }
        }
    }
}
