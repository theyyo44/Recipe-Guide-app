package sample.yemek;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import sample.yemek.model.Tarifler;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class YemeklerController {

    @FXML
    private Label KategoriLabel;

    @FXML
    private Label MaliyetLabel;

    @FXML
    private Label hazırlanmaSureLabel;

    @FXML
    private Label tarifAdLabel;

    @FXML
    private AnchorPane anchorpane;
    @FXML
    private ImageView img;

    private Tarifler tarif;
    private HelloController helloController;  // Ana sayfayı güncellemek için ana kontrolcü

    // Ana kontrolcüyü ayarla (ana sayfayı güncellemek için kullanacağız)
    public void setHelloController(HelloController helloController) {
        this.helloController = helloController;
        // Kontrol noktası
        if (helloController != null) {
            System.out.println("setHelloController(Y): HelloController referansı ayarlandı.");
        } else {
            System.out.println("setHelloController(Y): HelloController referansı null.");
        }
    }

    // Tarife ait bilgileri ve resmi ayarlama
    public void setData(Tarifler tarif, double eslesmeYuzdesi) {
        int l=0;
        this.tarif = tarif;


        hazırlanmaSureLabel.setText(tarif.getHazirlanmaSure() + " dk");
        tarifAdLabel.setText(tarif.getTarifAd());
        KategoriLabel.setText(String.format("%%%.2f", eslesmeYuzdesi));

        // Resim yükleme
        String imgPath = tarif.getImgpath();
        if (imgPath == null || imgPath.isEmpty()) {
            imgPath = "/images/mercimek.png";
        }
        Image image = new Image(getClass().getResource(imgPath).toExternalForm());
        img.setImage(image);
        img.setFitWidth(225);
        img.setFitHeight(131);
        img.setPreserveRatio(true);

        // Malzeme kontrolü ve maliyet hesaplama
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT TM.MalzemeMiktar, M.ToplamMiktar, M.BirimFiyat " +
                             "FROM TarifMalzeme TM " +
                             "JOIN Malzemeler M ON TM.MalzemeID = M.MalzemeID " +
                             "WHERE TM.TarifID = ?")) {

            stmt.setInt(1, tarif.getTarifID());
            ResultSet rs = stmt.executeQuery();

            boolean malzemelerYeterli = true;  // Başlangıçta yeterli varsayılır
            double eksikMaliyet = 0;

            while (rs.next()) {
                int gerekenMiktar = rs.getInt("MalzemeMiktar");
                int mevcutMiktar = rs.getInt("ToplamMiktar");
                double birimFiyat = rs.getDouble("BirimFiyat");

                if (mevcutMiktar < gerekenMiktar) {
                    malzemelerYeterli = false;  // Eğer stokta yeterli değilse kırmızı yapılacak
                    int eksikMiktar = gerekenMiktar - mevcutMiktar;
                    eksikMaliyet += eksikMiktar * birimFiyat;  // Eksik miktar kadar maliyet hesapla
                }
            }

            // Malzemeler yeterliyse panel yeşil ve maliyet sıfır, eksikse kırmızı ve eksik maliyet hesaplanacak
            if (malzemelerYeterli) {
                anchorpane.setStyle("-fx-background-color:  #005500;");
                MaliyetLabel.setText("Maliyet: 0.00 TL");// Malzeme tam ise maliyet sıfır
                tarif.setMaliyet(0);
            } else {
                anchorpane.setStyle("-fx-background-color: #AA0000;");
                MaliyetLabel.setText(String.format("Eksik Maliyet: %.2f TL", eksikMaliyet));  // Eksik malzemelerin maliyeti
                tarif.setMaliyet(eksikMaliyet);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Tarife tıklandığında yeni bir pencere açma (talimatları göstermek için)
        img.setOnMouseClicked(event -> openTalimatPanel());
    }
    public void setData(Tarifler tarif) {
        int l=0;
        this.tarif = tarif;

        KategoriLabel.setText(tarif.getKategori());
        hazırlanmaSureLabel.setText(tarif.getHazirlanmaSure() + " dk");
        tarifAdLabel.setText(tarif.getTarifAd());

        // Resim yükleme
        String imgPath = tarif.getImgpath();
        if (imgPath == null || imgPath.isEmpty()) {
            imgPath = "/images/mercimek.png";
        }
        Image image = new Image(getClass().getResource(imgPath).toExternalForm());
        img.setImage(image);
        img.setFitWidth(225);
        img.setFitHeight(131);
        img.setPreserveRatio(true);

        // Malzeme kontrolü ve maliyet hesaplama
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT TM.MalzemeMiktar, M.ToplamMiktar, M.BirimFiyat " +
                             "FROM TarifMalzeme TM " +
                             "JOIN Malzemeler M ON TM.MalzemeID = M.MalzemeID " +
                             "WHERE TM.TarifID = ?")) {

            stmt.setInt(1, tarif.getTarifID());
            ResultSet rs = stmt.executeQuery();

            boolean malzemelerYeterli = true;  // Başlangıçta yeterli varsayılır
            double eksikMaliyet = 0;

            while (rs.next()) {
                int gerekenMiktar = rs.getInt("MalzemeMiktar");
                int mevcutMiktar = rs.getInt("ToplamMiktar");
                double birimFiyat = rs.getDouble("BirimFiyat");

                if (mevcutMiktar < gerekenMiktar) {
                    malzemelerYeterli = false;  // Eğer stokta yeterli değilse kırmızı yapılacak
                    int eksikMiktar = gerekenMiktar - mevcutMiktar;
                    eksikMaliyet += eksikMiktar * birimFiyat;  // Eksik miktar kadar maliyet hesapla
                }
            }

            // Malzemeler yeterliyse panel yeşil ve maliyet sıfır, eksikse kırmızı ve eksik maliyet hesaplanacak
            if (malzemelerYeterli) {
                anchorpane.setStyle("-fx-background-color: #005500;");
                MaliyetLabel.setText("Maliyet: 0.00 TL");  // Malzeme tam ise maliyet sıfır
            } else {
                anchorpane.setStyle("-fx-background-color: #AA0000;");
                MaliyetLabel.setText(String.format("Eksik Maliyet: %.2f TL", eksikMaliyet));  // Eksik malzemelerin maliyeti
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Tarife tıklandığında yeni bir pencere açma (talimatları göstermek için)
        img.setOnMouseClicked(event -> openTalimatPanel());
    }

    // Yeni panel açarak tarifin talimatlarını gösteren metot
    private void openTalimatPanel() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/sample/yemek/talimat-view.fxml"));
            AnchorPane talimatPane = fxmlLoader.load();

            // Yeni bir Stage (pencere) oluşturma
            Stage stage = new Stage();
            stage.setTitle(tarif.getTarifAd() + " - Talimatlar");

            // TalimatController'a talimatları geçme
            TalimatController talimatController = fxmlLoader.getController();
            talimatController.setTalimat(tarif);
            talimatController.setHelloController(helloController);
            if (helloController != null) {
                System.out.println("HelloController referansı doğru aktarıldı.");
            } else {
                System.out.println("HelloController referansı null (Y), aktarım başarısız.");
            }
             // Ana kontrolcüyü aktar


            // Yeni sahne oluşturma ve gösterme
            Scene scene = new Scene(talimatPane);
            stage.setScene(scene);

            // Yeni pencereyi modal olarak ayarla (önceki pencereyle etkileşim olmaz)
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
