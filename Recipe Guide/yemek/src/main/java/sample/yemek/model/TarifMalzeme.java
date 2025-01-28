package sample.yemek.model;

public class TarifMalzeme {

    private String malzemeAdi;
    private int malzemeMiktari;

    public TarifMalzeme(int malzemeMiktari, String malzemeAdi) {
        this.malzemeMiktari = malzemeMiktari;
        this.malzemeAdi = malzemeAdi;
    }

    public String getMalzemeAdi() {
        return malzemeAdi;
    }

    public void setMalzemeAdi(String malzemeAdi) {
        this.malzemeAdi = malzemeAdi;
    }

    public int getMalzemeMiktari() {
        return malzemeMiktari;
    }

    public void setMalzemeMiktari(int malzemeMiktari) {
        this.malzemeMiktari = malzemeMiktari;
    }
}
