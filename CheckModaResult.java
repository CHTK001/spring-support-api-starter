import java.sql.*;
public class CheckModaResult {
    public static void main(String[] args) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://172.16.0.40:3306/system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&characterEncoding=utf8",
                "root", "root@")) {
            String target = "spider-exec-ab2a1995-c502-4a69-9f96-5a89f51e0ee2";
            // Execution status
            String sql = "SELECT spider_execution_status, spider_started_at, spider_finished_at FROM spider_execution WHERE spider_execution_no = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, target);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) System.out.println("状态: " + rs.getString("spider_execution_status") + " | 开始: " + rs.getTimestamp("spider_started_at") + " | 结束: " + rs.getTimestamp("spider_finished_at"));
                    else System.out.println("未找到执行记录");
                }
            }
            // Steps
            String steps = "SELECT spider_node_code, spider_node_name, spider_step_status, spider_error_message FROM spider_execution_step WHERE spider_execution_no = ? ORDER BY spider_id";
            try (PreparedStatement ps = conn.prepareStatement(steps)) {
                ps.setString(1, target);
                try (ResultSet rs = ps.executeQuery()) {
                    System.out.println("\n步骤:");
                    while (rs.next()) {
                        String err = rs.getString("spider_error_message");
                        System.out.println("  " + rs.getString("spider_node_code") + " [" + rs.getString("spider_step_status") + "] " + (err != null ? err.substring(0, Math.min(200, err.length())) : ""));
                    }
                }
            }
            // Data records
            String cnt = "SELECT COUNT(*) AS cnt FROM spider_data_record WHERE spider_execution_no = ?";
            try (PreparedStatement ps = conn.prepareStatement(cnt)) {
                ps.setString(1, target);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) System.out.println("\n数据记录: " + rs.getInt("cnt") + " 条");
                }
            }
            // Gitee table
            String gc = "SELECT COUNT(*) AS cnt FROM gitee_explore_project WHERE spider_execution_no = ? AND name IS NOT NULL AND name != ''";
            try (PreparedStatement ps = conn.prepareStatement(gc)) {
                ps.setString(1, target);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) System.out.println("Gitee项目数: " + rs.getInt("cnt") + " 条");
                }
            }
        }
    }
}
