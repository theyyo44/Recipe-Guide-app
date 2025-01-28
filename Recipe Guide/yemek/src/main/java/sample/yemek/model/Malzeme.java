package sample.yemek.model;

public class Malzeme {
    private int malzemeID;
    private String malzemeAdi;
    private int toplamMiktar;
    private String malzemeBirim;
    private float birimFiyat; // float olarak güncellendi

    // Constructor
    public Malzeme(int malzemeID, String malzemeAdi, int toplamMiktar, String malzemeBirim, float birimFiyat) {
        this.malzemeID = malzemeID;
        this.malzemeAdi = malzemeAdi;
        this.toplamMiktar = toplamMiktar;
        this.malzemeBirim = malzemeBirim;
        this.birimFiyat = birimFiyat;
    }

    // Getter ve Setter metotları
    public int getMalzemeID() {
        return malzemeID;
    }

    public void setMalzemeID(int malzemeID) {
        this.malzemeID = malzemeID;
    }

    public String getMalzemeAdi() {
        return malzemeAdi;
    }

    public void setMalzemeAdi(String malzemeAdi) {
        this.malzemeAdi = malzemeAdi;
    }

    public int getToplamMiktar() {
        return toplamMiktar;
    }

    public void setToplamMiktar(int toplamMiktar) {
        this.toplamMiktar = toplamMiktar;
    }

    public String getMalzemeBirim() {
        return malzemeBirim;
    }

    public void setMalzemeBirim(String malzemeBirim) {
        this.malzemeBirim = malzemeBirim;
    }

    public float getBirimFiyat() {
        return birimFiyat;
    }

    public void setBirimFiyat(float birimFiyat) {
        this.birimFiyat = birimFiyat;
    }
}
