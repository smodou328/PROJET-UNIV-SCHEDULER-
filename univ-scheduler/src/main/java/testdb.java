import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class testdb {


        public static void main(String[] args) {
            String url = "jdbc:mysql://localhost:3306/univ_scheduler"; // nom de ta base
            String user = "root"; // ton utilisateur MySQL
            String password = "modoulo328"; // ton mot de passe MySQL

            try {
                Connection conn = DriverManager.getConnection(url, user, password);
                System.out.println("✅ Connexion réussie à MySQL !");
                conn.close();
            } catch (SQLException e) {
                System.out.println("❌ Erreur de connexion : " + e.getMessage());
            }
        }
    }

