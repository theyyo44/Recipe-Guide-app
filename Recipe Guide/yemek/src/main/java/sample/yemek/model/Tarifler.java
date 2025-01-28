package sample.yemek.model;

public class Tarifler {

    private int tarifID;

    private String tarifAd;

    private String kategori;
    private  int MalzemeSayisi;
    private int hazirlanmaSure;


    private String talimatlar;

    private String imgpath ;

    private double maliyet;


    public int getMalzemeSayisi() { return MalzemeSayisi; }

    public void setMalzemeSayisi(int malzemeSayisi) { MalzemeSayisi = malzemeSayisi; }

    public double getMaliyet() { return maliyet; }

    public void setMaliyet(double maliyet) { this.maliyet = maliyet; }

    public String getImgpath() {
        return imgpath;
    }

    public void setImgpath(String imgpath) {
        this.imgpath = imgpath;
    }

    public int getTarifID() {
        return tarifID;
    }

    public void setTarifID(int tarifID) {
        this.tarifID = tarifID;
    }

    public String getTarifAd() {
        return tarifAd;
    }

    public void setTarifAd(String tarifAd) {
        this.tarifAd = tarifAd;
    }

    public String getKategori() {
        return kategori;
    }

    public void setKategori(String kategori) {
        this.kategori = kategori;
    }

    public int getHazirlanmaSure() {
        return hazirlanmaSure;
    }

    public void setHazirlanmaSure(int hazirlanmaSure) {
        this.hazirlanmaSure = hazirlanmaSure;
    }

    public String getTalimatlar() {
        return talimatlar;
    }

    public void setTalimatlar(String talimatlar) {
        this.talimatlar = talimatlar;
    }
}
