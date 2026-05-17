import java.sql.*;
import java.util.Properties;

public class TestConnection {
    public static void main(String[] args) {
        System.out.println("=== Test: Pooler ap-south-1 with Properties (avoids URL encoding issues) ===");
        
        String url = "jdbc:postgresql://aws-0-ap-south-1.pooler.supabase.com:6543/postgres?sslmode=require";
        String user = "postgres.zqjlrctwksoxbamjzrzk";
        String pass = "25062004@Patil";
        
        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", pass);
        props.setProperty("sslmode", "require");
        props.setProperty("connectTimeout", "15");
        props.setProperty("socketTimeout", "15");
        
        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("Connecting to: " + url);
            System.out.println("User: " + user);
            System.out.println("Password length: " + pass.length());
            
            Connection conn = DriverManager.getConnection(url, props);
            System.out.println("SUCCESS! Connected.");
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as cnt FROM employees");
            if (rs.next()) {
                System.out.println("Employees in table: " + rs.getInt("cnt"));
            }
            
            // List all employees
            rs = stmt.executeQuery("SELECT id, name, email, role FROM employees");
            while (rs.next()) {
                System.out.println("  Employee: " + rs.getInt("id") + " | " + rs.getString("name") + " | " + rs.getString("email") + " | " + rs.getString("role"));
            }
            
            conn.close();
        } catch (Exception e) {
            System.err.println("FAILED: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Caused by: " + e.getCause().getMessage());
            }
        }

        // Also try session mode
        System.out.println("\n=== Test: Pooler ap-south-1 Session Mode (port 5432) ===");
        url = "jdbc:postgresql://aws-0-ap-south-1.pooler.supabase.com:5432/postgres";
        props.setProperty("sslmode", "require");
        
        try {
            Connection conn = DriverManager.getConnection(url, props);
            System.out.println("SUCCESS! Connected via session mode.");
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as cnt FROM employees");
            if (rs.next()) {
                System.out.println("Employees in table: " + rs.getInt("cnt"));
            }
            conn.close();
        } catch (Exception e) {
            System.err.println("FAILED: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Caused by: " + e.getCause().getMessage());
            }
        }
    }
}
