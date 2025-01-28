package sample.yemek;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHelper {

    private static final String URL = "jdbc:mysql://localhost:3306/yemek_tarifleri";  // Veritabanı URL'si
    private static final String USER = "root";  // Veritabanı kullanıcı adı
    private static final String PASSWORD = "";  // Veritabanı şifresi

    // Veritabanına bağlanma
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Tabloları oluşturma metodu
    public static void createTables() {
        String createTariflerTable = "CREATE TABLE IF NOT EXISTS Tarifler (" +
                "TarifID INT AUTO_INCREMENT PRIMARY KEY, " +
                "TarifAdi VARCHAR(255) NOT NULL, " +
                "Kategori VARCHAR(100), " +
                "HazirlamaSuresi INT, " +
                "Talimatlar TEXT);";

        String createMalzemelerTable = "CREATE TABLE IF NOT EXISTS Malzemeler (" +
                "MalzemeID INT AUTO_INCREMENT PRIMARY KEY, " +
                "MalzemeAdi VARCHAR(255) NOT NULL, " +
                "ToplamMiktar VARCHAR(100), " +
                "MalzemeBirim VARCHAR(50), " +
                "BirimFiyat FLOAT);";

        String createTarifMalzemeTable = "CREATE TABLE IF NOT EXISTS TarifMalzeme (" +
                "TarifID INT, " +
                "MalzemeID INT, " +
                "MalzemeMiktar FLOAT, " +
                "FOREIGN KEY (TarifID) REFERENCES Tarifler(TarifID), " +
                "FOREIGN KEY (MalzemeID) REFERENCES Malzemeler(MalzemeID), " +
                "PRIMARY KEY (TarifID, MalzemeID));";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Tabloları oluştur
            stmt.execute(createTariflerTable);
            stmt.execute(createMalzemelerTable);
            stmt.execute(createTarifMalzemeTable);

            System.out.println("Tablolar başarıyla oluşturuldu.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
