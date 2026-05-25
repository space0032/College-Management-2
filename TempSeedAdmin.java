import com.college.utils.PasswordUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class TempSeedAdmin {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:h2:file:./data/college_fallback;MODE=PostgreSQL;AUTO_SERVER=TRUE;DATABASE_TO_LOWER=TRUE";
        String passwordHash = PasswordUtils.hashPassword("admin123");

        try (Connection conn = DriverManager.getConnection(url, "sa", "")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM users WHERE username='admin'");
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO users (username, password, role, is_active, created_at) VALUES (?, ?, ?, TRUE, CURRENT_TIMESTAMP)")) {
                ps.setString(1, "admin");
                ps.setString(2, passwordHash);
                ps.setString(3, "ADMIN");
                ps.executeUpdate();
            }

            System.out.println(passwordHash);
            System.out.println("Seeded admin user: admin / admin123");
        }
    }
}

