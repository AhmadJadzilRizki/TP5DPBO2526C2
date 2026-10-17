/**
 * Class Person: model data untuk satu orang (civitas kampus).
 * Semua atribut private (encapsulation), diakses lewat getter & setter.
 *
 * Atribut awal : id, nama, tahunLahir, kategori
 * Atribut baru : keaktifan (0-100), diisi lewat komponen JSlider di form
 */
public class Person {
    private String id;
    private String nama;
    private int tahunLahir;
    private String kategori;
    private int keaktifan; // atribut baru -> tingkat keaktifan dalam persen (0-100)

    public Person(String id, String nama, int tahunLahir, String kategori, int keaktifan) {
        this.id = id;
        this.nama = nama;
        this.tahunLahir = tahunLahir;
        this.kategori = kategori;
        this.keaktifan = keaktifan;
    }

    // ===== Getter =====
    public String getId() {
        return id;
    }

    public String getNama() {
        return nama;
    }

    public int getTahunLahir() {
        return tahunLahir;
    }

    public String getKategori() {
        return kategori;
    }

    public int getKeaktifan() {
        return keaktifan;
    }

    // ===== Setter =====
    public void setId(String id) {
        this.id = id;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public void setTahunLahir(int tahunLahir) {
        this.tahunLahir = tahunLahir;
    }

    public void setKategori(String kategori) {
        this.kategori = kategori;
    }

    public void setKeaktifan(int keaktifan) {
        this.keaktifan = keaktifan;
    }
}
