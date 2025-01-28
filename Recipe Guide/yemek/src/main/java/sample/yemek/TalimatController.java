package sample.yemek;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import sample.yemek.model.Tarifler;
import javafx.scene.image.ImageView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TalimatController {

    @FXML
    private TextArea talimatlar;

    @FXML
    private TextField tarifKategori;

    @FXML
    private TextField tarifad;

    @FXML
    private TextField hazirlanmaSure;

    @FXML
    private ImageView img;

    @FXML
    private Button delete;

    @FXML
    private Button update;

    private int tarifID;

    private HelloController helloController;  // Ana sayfayı güncellemek için ana kontrolcü

    // Ana kontrolcüyü ayarla (ana sayfayı güncellemek için kullanacağız)
    public void setHelloController(HelloController helloController) {
        this.helloController = helloController;
        // Kontrol noktası
        if (helloController != null) {
            System.out.println("setHelloController(t): HelloController referansı ayarlandı.");
        } else {
            System.out.println("setHelloController(T): HelloController referansı null.");
        }
    }

    public void setTalimat(Tarifler tarif) {
        tarifID = tarif.getTarifID();  // Tarif ID'sini saklıyoruz
        talimatlar.setText(tarif.getTalimatlar());
        tarifad.setText(tarif.getTarifAd());
        tarifKategori.setText(tarif.getKategori());
        hazirlanmaSure.setText(tarif.getHazirlanmaSure() + "dk");
        String imgPath = tarif.getImgpath();
        if (imgPath == null || imgPath.isEmpty()) {
            imgPath = "/images/mercimek.png";
        }
        Image image = new Image(getClass().getResource(imgPath).toExternalForm());
        img.setImage(image);
        img.setFitWidth(240);
        img.setFitHeight(190);
        img.setPreserveRatio(true);

        // İlgili malzemeleri ekle
        loadMalzemeler();
    }


    private void loadMalzemeler() {
        StringBuilder malzemelerListesi = new StringBuilder();
        String sql = "SELECT M.MalzemeAdi, TM.MalzemeMiktar, M.MalzemeBirim " +
                     "FROM TarifMalzeme TM " +
                     "JOIN Malzemeler M ON TM.MalzemeID = M.MalzemeID " +
                     "WHERE TM.TarifID = ?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, tarifID);
            ResultSet rs = stmt.executeQuery();

            // Malzeme Listesi
            malzemelerListesi.append("Malzemeler:\n");
            while (rs.next()) {
                String malzemeAdi = rs.getString("MalzemeAdi");
                double miktar = rs.getDouble("MalzemeMiktar");
                String birim = rs.getString("MalzemeBirim");

                malzemelerListesi.append("- ")
                        .append(malzemeAdi)
                        .append(": ")
                        .append(miktar)
                        .append(" ")
                        .append(birim)
                        .append("\n");
            }

            // Mevcut talimatlara malzemeleri ekle
            String talimatVeMalzemeler = malzemelerListesi.toString() + "\n" + talimatlar.getText();
            talimatlar.setText(talimatVeMalzemeler);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Silme işlemi
    @FXML
    void delete(ActionEvent event) {
        String sql1 = "DELETE FROM TarifMalzeme WHERE TarifID = ?";
        String sql2 = "DELETE FROM Tarifler WHERE TarifID = ?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt1 = conn.prepareStatement(sql1);
             PreparedStatement stmt2 = conn.prepareStatement(sql2)) {

            // İlk olarak TarifMalzeme tablosundaki ilişkili malzemeleri sil
            stmt1.setInt(1, tarifID);
            stmt1.executeUpdate();

            // Ardından Tarifler tablosundan tarifi sil
            stmt2.setInt(1, tarifID);
            int affectedRows = stmt2.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Tarif başarıyla silindi.");
                closeWindowAndRefresh(event);
            } else {
                System.out.println("Tarif silinemedi.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Güncelleme işlemi
    @FXML
    void update(ActionEvent event) {
        String sql = "UPDATE Tarifler SET TarifAdi = ?, Kategori = ?, HazirlamaSuresi = ?, Talimatlar = ? WHERE TarifID = ?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tarifad.getText());  // Kullanıcıdan alınan yeni tarif adı
            stmt.setString(2, tarifKategori.getText());  // Kullanıcıdan alınan yeni kategori


            String hazirlamaSuresiText = hazirlanmaSure.getText().replace(" dk", "").trim();
            stmt.setInt(3, Integer.parseInt(hazirlamaSuresiText));

            stmt.setString(4, talimatlar.getText());
            stmt.setInt(5, tarifID);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Tarif başarıyla güncellendi.");
                closeWindowAndRefresh(event);
            } else {
                System.out.println("Tarif güncellenemedi.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.out.println("Hazırlama süresi geçerli bir sayı değil: " + e.getMessage());
        }
    }

    private void closeWindowAndRefresh(ActionEvent event) {
        // Ana ekranı güncelle
        if (helloController != null) {
            System.out.println("refreshTarifler() çağrıldı");
            helloController.refreshTarifler();  // Ana kontrolü günceller
        }
        if (helloController != null) {
            System.out.println("HelloController referansı mevcut. refreshTarifler() çağrılıyor.");
            helloController.refreshTarifler();
        } else {
            System.out.println("HelloController referansı null, refreshTarifler() çağrılmadı.");
        }
        // Pencereyi kapatma
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
