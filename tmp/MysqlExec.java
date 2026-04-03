import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class MysqlExec {
    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            throw new IllegalArgumentException("Usage: MysqlExec <url> <user> <password> <sql>");
        }

        Class.forName("com.mysql.cj.jdbc.Driver");

        String url = args[0];
        String user = args[1];
        String password = args[2];
        String sql = args[3];

        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement()) {
            boolean hasResultSet = statement.execute(sql);
            if (!hasResultSet) {
                System.out.println("UPDATE_COUNT=" + statement.getUpdateCount());
                return;
            }

            try (ResultSet resultSet = statement.getResultSet()) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();
                StringBuilder header = new StringBuilder();
                for (int i = 1; i <= columnCount; i++) {
                    if (i > 1) {
                        header.append('\t');
                    }
                    header.append(metaData.getColumnLabel(i));
                }
                System.out.println(header);

                while (resultSet.next()) {
                    StringBuilder row = new StringBuilder();
                    for (int i = 1; i <= columnCount; i++) {
                        if (i > 1) {
                            row.append('\t');
                        }
                        Object value = resultSet.getObject(i);
                        row.append(value == null ? "NULL" : value);
                    }
                    System.out.println(row);
                }
            }
        }
    }
}
