package sample.yemek;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MalzemeEkleController {

    @FXML
    private TextField BirimFiyat;

    @FXML
    private TextField MalzemeAdi;

    @FXML
    private TextField MalzemeBririm;

    @FXML
    private Button MalzemeEkle;

    @FXML
    private TextField MalzemeMiktar;

    private TarifEkleController tarifEkleController;  // TarifEkleController referansı
    private HelloController helloController;

    // TarifEkleController referansını set etme metodu
    public void setTarifEkleController(TarifEkleController controller) {
        this.tarifEkleController = controller;
    }
    public void setHelloController(HelloController controller) {
        this.helloController = controller;
    }

    @FXML
    private void MalzemeEkle(ActionEvent event) {
        // Kullanıcıdan alınan verileri kontrol et
        String malzemeAdi = MalzemeAdi.getText();
        String malzemeBirim = MalzemeBririm.getText();
        String miktarText = MalzemeMiktar.getText();
        String fiyatText = BirimFiyat.getText();

        // Boş alan kontrolü
        if (malzemeAdi.isEmpty() || malzemeBirim.isEmpty() || miktarText.isEmpty() || fiyatText.isEmpty()) {
            alertGoster("Hata", "Tüm alanları doldurun.", Alert.AlertType.ERROR);
            return;
        }

        // Tip kontrolü
        int miktar;
        float fiyat;
        try {
            miktar = Integer.parseInt(miktarText);
        } catch (NumberFormatException e) {
            alertGoster("Hata", "Malzeme miktarı geçerli bir tam sayı olmalıdır.", Alert.AlertType.ERROR);
            return;
        }

        try {
            fiyat = Float.parseFloat(fiyatText);
        } catch (NumberFormatException e) {
            alertGoster("Hata", "Birim fiyat geçerli bir sayı olmalıdır.", Alert.AlertType.ERROR);
            return;
        }

        // Duplicate kontrolü (Malzeme adı zaten var mı?)
        if (malzemeZatenVarMi(malzemeAdi)) {
            alertGoster("Hata", "Bu malzeme zaten veritabanında mevcut.", Alert.AlertType.ERROR);
            return;
        }

        // Veritabanına ekleme işlemi
        malzemeEkle(malzemeAdi, miktar, malzemeBirim, fiyat);
    }

    // Veritabanında bu malzemenin var olup olmadığını kontrol eder
    private boolean malzemeZatenVarMi(String malzemeAdi) {
        String sql = "SELECT * FROM Malzemeler WHERE MalzemeAdi = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, malzemeAdi);
            ResultSet rs = stmt.executeQuery();
            return rs.next();  // Eğer sonuç dönerse, malzeme zaten var demektir
        } catch (SQLException e) {
            e.printStackTrace();
            alertGoster("Hata", "Veritabanı hatası: " + e.getMessage(), Alert.AlertType.ERROR);
            return false;
        }
    }

    // Veritabanına yeni malzeme ekler
    private void malzemeEkle(String malzemeAdi, int miktar, String malzemeBirim, float fiyat) {
        String sql = "INSERT INTO Malzemeler (MalzemeAdi, ToplamMiktar, MalzemeBirim, BirimFiyat) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, malzemeAdi);
            stmt.setInt(2, miktar);
            stmt.setString(3, malzemeBirim);
            stmt.setFloat(4, fiyat);

            stmt.executeUpdate();
            alertGoster("Başarılı", "Malzeme başarıyla eklendi.", Alert.AlertType.INFORMATION);

            // TarifEkleController panelini güncelle
            if (tarifEkleController != null) {
                tarifEkleController.refreshMalzemeler();  // Malzemeleri güncelle
            }

            // Pencereyi kapat
            Stage stage = (Stage) MalzemeEkle.getScene().getWindow();
            stage.close();

        } catch (SQLException e) {
            e.printStackTrace();
            alertGoster("Hata", "Veritabanı hatası: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Uyarı mesajlarını göstermek için kullanılan metot
    private void alertGoster(String baslik, String mesaj, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(baslik);
        alert.setContentText(mesaj);
        alert.showAndWait();
    }

}
