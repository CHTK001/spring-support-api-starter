import java.sql.*;

public class ClearLock {
    public static void main(String[] args) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");

        String url = "jdbc:mysql://172.16.0.40:3306/system?useSSL=false&allowPublicKeyRetrieval=true";
        String user = "root";
        String pass = "root@";

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("Connected to database!");

            // Check if there's a lock table
            String checkTableSql = "SHOW TABLES LIKE '%lock%'";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(checkTableSql)) {
                System.out.println("\nTables with 'lock' in name:");
                while (rs.next()) {
                    System.out.println("  - " + rs.getString(1));
                }
            }

            // Check sys_user for lock-related columns
            String descSql = "SHOW COLUMNS FROM sys_user LIKE '%lock%'";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(descSql)) {
                System.out.println("\nColumns with 'lock' in sys_user:");
                while (rs.next()) {
                    System.out.println("  - " + rs.getString("Field"));
                }
            }

            // Check sys_user for error count columns
            String descSql2 = "SHOW COLUMNS FROM sys_user LIKE '%error%'";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(descSql2)) {
                System.out.println("\nColumns with 'error' in sys_user:");
                while (rs.next()) {
                    System.out.println("  - " + rs.getString("Field"));
                }
            }

            System.out.println("\n✅ Check complete!");
        }
    }
}
