package sample.yemek;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sample.yemek.model.Malzeme;
import sample.yemek.model.TarifMalzeme;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TarifEkleController {
    private HelloController helloController;

    @FXML
    private TableColumn<Malzeme, Float> BirimFiyatı;

    @FXML
    private TextField HazırlanmaSüresi;

    @FXML
    private TableColumn<Malzeme, String> MalzemeBirimi;  // Yeni sütun tanımlandı

    @FXML
    private ComboBox<String> KategoriComboBox;

    @FXML
    private TableColumn<Malzeme, String> MalzemeAdı;

    @FXML
    private TextField MalzemeAdıText;

    @FXML
    private Button MalzemeEkleButonu;

    @FXML
    private TableColumn<Malzeme, Integer> MalzemeMiktarı;

    @FXML
    private TableView<Malzeme> MalzemelerTablosu;

    @FXML
    private TextField ResimURL;

    @FXML
    private Button TablodanSil;

    @FXML
    private TextArea Talimatlar;

    @FXML
    private TextField TarifAdı;

    @FXML
    private Button TarifEkleButon;

    @FXML
    private TableColumn<TarifMalzeme, String> TarifMalzeme;

    @FXML
    private TableView<TarifMalzeme> TarifMalzemeTablosu;

    @FXML
    private TableColumn<TarifMalzeme, Integer> TarifMiktar;

    @FXML
    private Button YeniMalzemEkle;

    @FXML
    private TextField tarifiçinMiktar;

    // Kategori combo box için kategoriler listesi
    private final ObservableList<String> kategoriListesi = FXCollections.observableArrayList("Tatlılar", "Ana Yemekler", "Atıştırmalıklar", "Salatalar", "Çorbalar");

    public void setHelloController(HelloController helloController) {
        this.helloController = helloController;
    }

    private ObservableList<TarifMalzeme> tarifMalzemeListesi = FXCollections.observableArrayList();

    public void initialize() {
        // ComboBox'a kategorileri ekliyoruz
        KategoriComboBox.setItems(kategoriListesi);

        ObservableList<Malzeme> malzemeListesi = FXCollections.observableArrayList();

        // Tablo kolonlarına veri bağlama
        MalzemeAdı.setCellValueFactory(new PropertyValueFactory<>("malzemeAdi"));
        MalzemeMiktarı.setCellValueFactory(new PropertyValueFactory<>("toplamMiktar"));
        MalzemeBirimi.setCellValueFactory(new PropertyValueFactory<>("malzemeBirim"));
        BirimFiyatı.setCellValueFactory(new PropertyValueFactory<>("birimFiyat"));

        // Tabloya listeyi bağlama
        MalzemelerTablosu.setItems(malzemeListesi);
        malzemeListesi.addAll(getMalzemelerData());

        MalzemelerTablosu.getSelectionModel().selectedItemProperty().addListener((obs, eskiSecim, yeniSecim) -> {
            if (yeniSecim != null) {
                Malzeme secilenMalzeme = MalzemelerTablosu.getSelectionModel().getSelectedItem();
                MalzemeAdıText.setText(secilenMalzeme.getMalzemeAdi());
            }
        });

        // Tarif malzeme tablosunun kolonlarını ayarla
        TarifMalzeme.setCellValueFactory(new PropertyValueFactory<>("malzemeAdi"));
        TarifMiktar.setCellValueFactory(new PropertyValueFactory<>("malzemeMiktari"));
        TarifMalzemeTablosu.setItems(tarifMalzemeListesi);
    }

    public void refreshMalzemeler() {
        ObservableList<Malzeme> malzemeListesi = FXCollections.observableArrayList();
        malzemeListesi.addAll(getMalzemelerData());
        MalzemelerTablosu.setItems(malzemeListesi);
        MalzemelerTablosu.refresh();  // Tablonun güncellenmesini sağla
    }

    private ObservableList<Malzeme> getMalzemelerData() {
        ObservableList<Malzeme> malzemelerListesi = FXCollections.observableArrayList();

        // SQL sorgusu: 'Malzemeler' tablosundaki tüm verileri alıyoruz
        String sql = "SELECT * FROM Malzemeler";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int malzemeID = rs.getInt("MalzemeID");
                String malzemeAdi = rs.getString("MalzemeAdi");
                int toplamMiktar = rs.getInt("ToplamMiktar");
                String malzemeBirim = rs.getString("MalzemeBirim");
                float birimFiyat = rs.getFloat("BirimFiyat");

                Malzeme malzeme = new Malzeme(malzemeID, malzemeAdi, toplamMiktar, malzemeBirim, birimFiyat);
                malzemelerListesi.add(malzeme);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return malzemelerListesi;
    }

    @FXML
    private void MalzemeEkleButonu(ActionEvent event) {
        String malzemeAdi = MalzemeAdıText.getText();
        String miktarText = tarifiçinMiktar.getText();

        if (malzemeAdi != null && !miktarText.isEmpty()) {
            try {
                int miktar = Integer.parseInt(miktarText);
                TarifMalzeme yeniMalzeme = new TarifMalzeme(miktar, malzemeAdi);
                tarifMalzemeListesi.add(yeniMalzeme);
                TarifMalzemeTablosu.refresh();
            } catch (NumberFormatException e) {
                alertGoster("Hata", "Geçerli bir miktar girin.", Alert.AlertType.ERROR);
            }
        } else {
            alertGoster("Hata", "Bir malzeme seçin ve miktar girin.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void TarifEkleButon(ActionEvent event) {
        if (TarifAdı.getText().isEmpty() || KategoriComboBox.getSelectionModel().isEmpty() || HazırlanmaSüresi.getText().isEmpty() || Talimatlar.getText().isEmpty()) {
            alertGoster("Hata", "Tüm alanları doldurun.", Alert.AlertType.ERROR);
            return;
        }

        try (Connection conn = DatabaseHelper.getConnection();

             PreparedStatement duplicateCheckStmt = conn.prepareStatement("SELECT COUNT(*) FROM Tarifler WHERE TarifAdi = ?")) {
            duplicateCheckStmt.setString(1, TarifAdı.getText());
            ResultSet rs = duplicateCheckStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                // Aynı isimde bir tarif var, uyarı göster
                alertGoster("Hata", "Bu isimde bir tarif zaten mevcut.", Alert.AlertType.ERROR);
                return;
            }

            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO Tarifler (TarifAdi, Kategori, HazirlamaSuresi, Talimatlar, imgpath) VALUES (?, ?, ?, ?, ?)",
                    PreparedStatement.RETURN_GENERATED_KEYS);

            stmt.setString(1, TarifAdı.getText());
            stmt.setString(2, KategoriComboBox.getValue());
            stmt.setInt(3, Integer.parseInt(HazırlanmaSüresi.getText()));
            stmt.setString(4, Talimatlar.getText());
            stmt.setString(5, ResimURL.getText());

            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            int tarifID = -1;
            if (generatedKeys.next()) {
                tarifID = generatedKeys.getInt(1);
            }

            // TarifMalzeme tablosuna malzemeleri ekleme işlemi
            String sqlMalzeme = "INSERT INTO TarifMalzeme (TarifID, MalzemeID, MalzemeMiktar) VALUES (?, ?, ?)";
            for (TarifMalzeme malzeme : tarifMalzemeListesi) {
                String malzemeSql = "SELECT MalzemeID FROM Malzemeler WHERE MalzemeAdi = ?";
                PreparedStatement stmtMalzeme = conn.prepareStatement(malzemeSql);
                stmtMalzeme.setString(1, malzeme.getMalzemeAdi());
                ResultSet malzemeRs = stmtMalzeme.executeQuery();

                if (malzemeRs.next()) {
                    int MalzemeID = malzemeRs.getInt("MalzemeID");

                    PreparedStatement stmtTarifMalzeme = conn.prepareStatement(sqlMalzeme);
                    stmtTarifMalzeme.setInt(1, tarifID);
                    stmtTarifMalzeme.setInt(2, MalzemeID);
                    stmtTarifMalzeme.setInt(3, malzeme.getMalzemeMiktari());
                    stmtTarifMalzeme.executeUpdate();
                }
            }

            alertGoster("Başarılı", "Tarif başarıyla eklendi.", Alert.AlertType.INFORMATION);
            if (helloController != null) {
                helloController.refreshTarifler();
            }
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.close();
        } catch (SQLException e) {
            e.printStackTrace();
            alertGoster("Hata", "Tarif eklenirken bir hata oluştu.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void YeniMalzemeEkle(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/sample/yemek/Malzeme-Ekle-Wiew.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            MalzemeEkleController malzemeEkleController = fxmlLoader.getController();
            malzemeEkleController.setTarifEkleController(this);

            Stage stage = new Stage();
            stage.setTitle("Yeni Malzeme Ekle");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void alertGoster(String baslik, String mesaj, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(baslik);
        alert.setContentText(mesaj);
        alert.showAndWait();
    }
}
