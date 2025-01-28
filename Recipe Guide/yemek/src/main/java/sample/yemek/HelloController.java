package sample.yemek;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import sample.yemek.model.Tarifler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;



public class HelloController {
    @FXML
    private ComboBox<String> filtrelemeSecim;
    @FXML
    private ComboBox<String> siralamaSecim;
    @FXML
    private ComboBox<String> kategoriSecim;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField arama;

    @FXML
    private Button aramabuton;

    @FXML
    private Button tarifekle;

    @FXML
    private Button malzemeEkle;

    @FXML
    private GridPane grid;

    @FXML
    private ScrollPane scroll;

    public static List <Tarifler> tarifler = new ArrayList<>();
    private List<Tarifler> filteredTarifler = new ArrayList<>();

    private String createPlaceholders(int count) {
        return String.join(",", Collections.nCopies(count, "?"));
    }

    private List <Tarifler> getData(){
        List<Tarifler> tariflerListesi = new ArrayList<>();

        // SQL sorgusu
        String sql = "SELECT * FROM Tarifler";  // 'Tarifler' tablosundan tüm verileri alıyoruz

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            // Sonuçları işleme
            while (rs.next()) {
                Tarifler tarif = new Tarifler();
                tarif.setTarifID(rs.getInt("TarifID"));
                tarif.setTarifAd(rs.getString("TarifAdi"));
                tarif.setKategori(rs.getString("Kategori"));
                tarif.setHazirlanmaSure(rs.getInt("HazirlamaSuresi"));
                tarif.setTalimatlar(rs.getString("Talimatlar"));
                tarif.setImgpath(rs.getString("imgpath"));

                // Listeye ekle
                tariflerListesi.add(tarif);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tariflerListesi;
    }


    @FXML
    void initialize() {

        // ComboBox'a seçenekleri ekleme
        filtrelemeSecim.getItems().addAll("Ada Göre", "Malzemeye Göre");
        siralamaSecim.getItems().addAll("Malzeme Sayısı (Artan)","Malzeme Sayısı (Azalan)","Hazırlama Süresi (Artan)","Hazırlama Süresi (Azalan)");
        kategoriSecim.getItems().addAll("Çorbalar", "Ana Yemekler", "Tatlılar", "Salatalar", "Atıştırmalıklar");
        tarifler.addAll(getData());

        grid.setHgap(10);  // Sütunlar arası boşluk 20 piksel
        grid.setVgap(10);  // Satırlar arası boşluk 20 piksel


        int column = 0;
        int row = 1;
        try {
            for (int i = 0; i < tarifler.size(); i++) {
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("/sample/yemek/yemek.fxml"));

                AnchorPane anchorPane = fxmlLoader.load();

                YemeklerController yemeklerController = fxmlLoader.getController();
                yemeklerController.setData(tarifler.get(i));
                yemeklerController.setHelloController(this);

                if(column == 3){
                    column = 0;
                    row++;
                }
                grid.add(anchorPane,column++,row);
                // grid width ayarı
                grid.setMinWidth(Region.USE_COMPUTED_SIZE);
                grid.setPrefWidth(Region.USE_COMPUTED_SIZE);
                grid.setMaxWidth(Region.USE_COMPUTED_SIZE);
                // grid heigh ayarı
                grid.setMinHeight(Region.USE_COMPUTED_SIZE);
                grid.setPrefHeight(Region.USE_COMPUTED_SIZE);
                grid.setMaxHeight(Region.USE_COMPUTED_SIZE);

                GridPane.setMargin(anchorPane, new Insets(10));



            }

        }catch (IOException e){
            e.printStackTrace();
        }


    }

    @FXML
    public void tarifAra() {
        System.out.println("deneme");
        String secim = filtrelemeSecim.getValue();  // ComboBox'tan seçilen değer
        String aramaMetni = arama.getText();  // Arama çubuğundaki metin
        String sql = "";  // Dinamik olarak oluşturacağımız SQL sorgusu

        // Kullanıcının girdiği malzemeleri virgülle ayırarak alalım (örneğin: "tuz, un, su")
        String[] malzemeler = aramaMetni.split(",");

        if (secim != null && !secim.isEmpty()) {
            if (secim.equals("Ada Göre")) {
                // Ada göre arama sorgusu
                sql = "SELECT * FROM Tarifler WHERE TarifAdi LIKE ?";
            } else if (secim.equals("Malzemeye Göre")) {

                // Dinamik olarak IN sorgusuna yer tutucu ekle
                String placeholders = String.join(",", Collections.nCopies(malzemeler.length, "?"));


                // SQL sorgusunu güncelliyoruz
                sql = "SELECT T.TarifID, T.TarifAdi, T.Kategori, T.HazirlamaSuresi, T.Talimatlar, T.imgpath, " +
                        "(SELECT COUNT(*) FROM Malzemeler M JOIN TarifMalzeme TM ON M.MalzemeID = TM.MalzemeID " +
                        "WHERE TM.TarifID = T.TarifID AND M.MalzemeAdi IN (" + placeholders + ")) AS EslestirmeSayisi, " +
                        "(SELECT COUNT(*) FROM TarifMalzeme TM WHERE TM.TarifID = T.TarifID) AS ToplamMalzemeSayisi " +
                        "FROM Tarifler T " +
                        "GROUP BY T.TarifID " +
                        "HAVING EslestirmeSayisi > 0 " +
                        "ORDER BY (EslestirmeSayisi / ToplamMalzemeSayisi) DESC";
            }

            // Veritabanında sorguyu çalıştır ve sonuçları al
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {


                if (secim.equals("Ada Göre")) {
                    stmt.setString(1, "%" + aramaMetni + "%");  // Arama metniyle sorgu çalıştırma
                } else if (secim.equals("Malzemeye Göre")) {
                    // Malzemeleri yer tutuculara ekle
                    for (int i = 0; i < malzemeler.length; i++) {
                        stmt.setString(i + 1, malzemeler[i].trim());
                    }
                }


                ResultSet rs = stmt.executeQuery();

                // GridPane'i temizleme
                grid.getChildren().clear();
                int column = 0;
                int row = 1;
                int l = 0;
                // Sonuçları işleme ve GridPane'e ekleme
                while (rs.next()) {

                    // Yeni bir FXMLLoader ile sonuçları dinamik olarak yükle
                    FXMLLoader fxmlLoader = new FXMLLoader();
                    fxmlLoader.setLocation(getClass().getResource("/sample/yemek/yemek.fxml"));
                    AnchorPane anchorPane = fxmlLoader.load();

                    YemeklerController yemeklerController = fxmlLoader.getController();
                    yemeklerController.setHelloController(this);

                    // Sorgudan gelen verileri YemeklerController'a aktar
                    Tarifler tarif = new Tarifler();
                    tarif.setTarifID(rs.getInt("TarifID"));
                    tarif.setTarifAd(rs.getString("TarifAdi"));
                    tarif.setKategori(rs.getString("Kategori"));
                    tarif.setHazirlanmaSure(rs.getInt("HazirlamaSuresi"));
                    tarif.setTalimatlar(rs.getString("Talimatlar"));
                    tarif.setImgpath(rs.getString("imgpath"));


                    if (secim.equals("Ada Göre")){
                        yemeklerController.setData(tarif);
                    }else if(secim.equals("Malzemeye Göre")){
                        // Eşleşme yüzdesi hesaplama
                        int eslestirmeSayisi = rs.getInt("EslestirmeSayisi");

                        int toplamMalzemeSayisi = rs.getInt("ToplamMalzemeSayisi");
                        double eslesmeYuzdesi = (double) eslestirmeSayisi / toplamMalzemeSayisi * 100;

                        yemeklerController.setData(tarif,eslesmeYuzdesi);
                    }


                    // GridPane'de sütun ve satır kontrolü
                    if (column == 3) {
                        column = 0;
                        row++;
                    }

                    // GridPane'e AnchorPane ekle
                    l++;
                    System.out.println(l);
                    grid.add(anchorPane, column++, row);
                    GridPane.setMargin(anchorPane, new Insets(10));



                }

            } catch (SQLException | IOException e) {
                e.printStackTrace();
            }
        }
    }


    @FXML
    public void tarifEkle() {
        try {
            // Yeni pencereyi yükle
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/sample/yemek/tarif-ekle-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            TarifEkleController tarifEkleController = fxmlLoader.getController();
            tarifEkleController.setHelloController(this);

            // Yeni bir Stage (pencere) oluştur
            Stage stage = new Stage();
            stage.setTitle("Yeni Tarif Ekle");
            stage.setScene(scene);

            // Yeni pencereyi modal olarak ayarla (önceki pencereyle etkileşim olmaz)
            stage.initModality(Modality.APPLICATION_MODAL);

            // Pencereyi göster
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void refreshTarifler(){
        tarifler.clear();
        tarifler.addAll(getData());


        grid.setHgap(10);  // Sütunlar arası boşluk 20 piksel
        grid.setVgap(10);  // Satırlar arası boşluk 20 piksel
        grid.getChildren().clear();
        int column = 0;
        int row = 1;
        System.out.println(tarifler.size());
        try {
            for (int i = 0; i < tarifler.size(); i++) {
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("/sample/yemek/yemek.fxml"));

                AnchorPane anchorPane = fxmlLoader.load();

                YemeklerController yemeklerController = fxmlLoader.getController();
                yemeklerController.setData(tarifler.get(i));
                yemeklerController.setHelloController(this);

                if(column == 3){
                    column = 0;
                    row++;
                }
                grid.add(anchorPane,column++,row);
                // grid width ayarı
                grid.setMinWidth(Region.USE_COMPUTED_SIZE);
                grid.setPrefWidth(Region.USE_COMPUTED_SIZE);
                grid.setMaxWidth(Region.USE_COMPUTED_SIZE);
                // grid heigh ayarı
                grid.setMinHeight(Region.USE_COMPUTED_SIZE);
                grid.setPrefHeight(Region.USE_COMPUTED_SIZE);
                grid.setMaxHeight(Region.USE_COMPUTED_SIZE);

                GridPane.setMargin(anchorPane, new Insets(10));

            }

        }catch (IOException e){
            e.printStackTrace();
        }

    }
    // Kategoriye göre tarifleri filtreleme
    @FXML
    public void kategoriFiltrele() {
        String secilenKategori = kategoriSecim.getValue(); // Seçilen kategori
        String sql = "SELECT * FROM Tarifler WHERE Kategori = ?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, secilenKategori); // SQL sorgusunda seçilen kategoriye göre filtreleme

            ResultSet rs = stmt.executeQuery();

            // GridPane'i temizleme ve yeni tarifleri listeye ekleme
            grid.getChildren().clear();
            tarifler.clear();

            int column = 0;
            int row = 1;
            while (rs.next()) {
                Tarifler tarif = new Tarifler();
                tarif.setTarifID(rs.getInt("TarifID"));
                tarif.setTarifAd(rs.getString("TarifAdi"));
                tarif.setKategori(rs.getString("Kategori"));
                tarif.setHazirlanmaSure(rs.getInt("HazirlamaSuresi"));
                tarif.setTalimatlar(rs.getString("Talimatlar"));
                tarif.setImgpath(rs.getString("imgpath"));

                tarifler.add(tarif); // Listeye ekle

                // Ekrana ekle
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("/sample/yemek/yemek.fxml"));
                AnchorPane anchorPane = fxmlLoader.load();

                YemeklerController yemeklerController = fxmlLoader.getController();
                yemeklerController.setData(tarif);

                if (column == 3) {
                    column = 0;
                    row++;
                }
                grid.add(anchorPane, column++, row);
                GridPane.setMargin(anchorPane, new Insets(10));
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void sirala() {
        String siralamaKriteri = siralamaSecim.getValue();  // Kullanıcının seçtiği sıralama kriteri
        String sql = "SELECT T.TarifID, T.TarifAdi, T.Talimatlar,T.Kategori, T.imgpath, COUNT(TM.MalzemeID) AS MalzemeSayisi, T.HazirlamaSuresi " +
                "FROM Tarifler T " +
                "JOIN TarifMalzeme TM ON T.TarifID = TM.TarifID " +
                "GROUP BY T.TarifID, T.TarifAdi, T.HazirlamaSuresi";

        // Kullanıcı seçimine göre SQL sorgusunu güncelliyoruz
        if ("Malzeme Sayısı (Artan)".equals(siralamaKriteri)) {
            sql += " ORDER BY MalzemeSayisi ASC";
        } else if ("Malzeme Sayısı (Azalan)".equals(siralamaKriteri)) {
            sql += " ORDER BY MalzemeSayisi DESC";
        } else if ("Hazırlama Süresi (Artan)".equals(siralamaKriteri)) {
            sql += " ORDER BY T.HazirlamaSuresi ASC";
        } else if ("Hazırlama Süresi (Azalan)".equals(siralamaKriteri)) {
            sql += " ORDER BY T.HazirlamaSuresi DESC";
        }

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            // GridPane'i temizleme
            grid.getChildren().clear();
            tarifler.clear();

            // Sonuçları işleme ve GridPane'e ekleme
            int column = 0;
            int row = 1;
            while (rs.next()) {
                Tarifler tarif = new Tarifler();
                tarif.setTarifID(rs.getInt("TarifID"));
                tarif.setTarifAd(rs.getString("TarifAdi"));
                tarif.setHazirlanmaSure(rs.getInt("HazirlamaSuresi"));
                tarif.setKategori(rs.getString("Kategori"));
                tarif.setTalimatlar(rs.getString("Talimatlar"));
                tarif.setImgpath(rs.getString("imgpath"));

                // Malzeme sayısını setleme
                int malzemeSayisi = rs.getInt("MalzemeSayisi");
                tarif.setMalzemeSayisi(malzemeSayisi); // Malzeme sayısını tarif nesnesine ekliyoruz

                tarifler.add(tarif);  // Listeye ekle

                // Ekrana ekle
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("/sample/yemek/yemek.fxml"));
                AnchorPane anchorPane = fxmlLoader.load();

                YemeklerController yemeklerController = fxmlLoader.getController();
                yemeklerController.setData(tarif);

                if (column == 3) {
                    column = 0;
                    row++;
                }
                grid.add(anchorPane, column++, row);
                GridPane.setMargin(anchorPane, new Insets(10));
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void MalzemeEkle(ActionEvent event){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/sample/yemek/Malzeme-Ekle-Wiew.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            MalzemeEkleController malzemeEkleController = fxmlLoader.getController();
            malzemeEkleController.setHelloController(this);

            Stage stage = new Stage();
            stage.setTitle("Yeni Malzeme Ekle");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}