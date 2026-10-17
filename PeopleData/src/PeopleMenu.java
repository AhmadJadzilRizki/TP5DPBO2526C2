import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.Year;

/**
 * File: PeopleMenu.java
 * Logic untuk form PeopleMenu.form (Java Swing GUI) yang terhubung ke
 * database MySQL (db_people, tabel person).
 *
 * Perbedaan dari versi sebelumnya:
 * 1. Semua operasi CRUD memakai database, bukan data di memori.
 * 2. Tidak ada lagi list di memori: tabel diisi langsung dari hasil query,
 *    dan data baris yang diklik dibaca langsung dari model tabel.
 * 3. Dialog error jika ada kolom input yang kosong (insert dan update).
 * 4. Dialog error jika ID yang dimasukkan sudah ada (insert).
 */
public class PeopleMenu extends JFrame {

    // ===== Component hasil binding dari PeopleMenu.form =====
    private JPanel mainPanel;
    private JLabel titleLabel;
    private JTextField idField;
    private JTextField namaField;
    private JTextField tahunLahirField;
    private JComboBox<String> kategoriComboBox;
    private JSlider keaktifanSlider;
    private JButton addUpdateButton;
    private JButton cancelButton;
    private JButton deleteButton;
    private JTable peopleTable;

    // koneksi ke database
    private Database database;

    // ID baris yang sedang dipilih di tabel (null = tidak ada yang dipilih).
    // ID dipakai sebagai kunci pada query UPDATE dan DELETE.
    private String selectedId = null;

    // ================= main =================
    public static void main(String[] args) {
        PeopleMenu menu = new PeopleMenu();
        menu.setSize(700, 600);
        menu.setLocationRelativeTo(null);
        menu.setContentPane(menu.mainPanel);
        menu.getContentPane().setBackground(Color.WHITE);
        menu.setVisible(true);
        menu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    // ================= constructor =================
    public PeopleMenu() {
        // buka koneksi ke database; jika gagal, tampilkan pesan lalu keluar
        try {
            database = new Database();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null,
                    "Tidak dapat terhubung ke database:\n" + ex.getMessage()
                            + "\n\nPastikan MySQL sudah berjalan, database db_people sudah diimport,"
                            + "\ndan mysql-connector-j sudah ditambahkan ke project.",
                    "Koneksi Database Gagal", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // isi tabel dari database
        peopleTable.setModel(setTable());

        // ubah styling title
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 20f));

        // atur isi combo box ("???" = placeholder, belum memilih kategori)
        String[] kategoriData = {"???", "Mahasiswa", "Dosen", "Tenaga Kependidikan",
                "Asisten Praktikum", "Alumni"};
        kategoriComboBox.setModel(new DefaultComboBoxModel<>(kategoriData));

        // sembunyikan button delete (baru muncul saat ada data yang dipilih)
        deleteButton.setVisible(false);

        // tutup koneksi database saat window ditutup
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                database.close();
            }
        });

        // ----- Listener: tombol Add / Update -----
        addUpdateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedId == null) {
                    insertData();
                } else {
                    updateData();
                }
            }
        });

        // ----- Listener: tombol Delete -----
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteData();
            }
        });

        // ----- Listener: tombol Cancel -----
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearForm();
            }
        });

        // ----- Listener: klik baris tabel -> isi form -----
        // nilai dibaca langsung dari model tabel (tanpa list di memori)
        peopleTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = peopleTable.getSelectedRow();
                if (row < 0) {
                    return;
                }
                TableModel model = peopleTable.getModel();

                // kolom tabel: 0=ID, 1=Nama, 2=Tahun Lahir, 3=Kategori, 4=Keaktifan
                selectedId = model.getValueAt(row, 0).toString();

                idField.setText(selectedId);
                namaField.setText(model.getValueAt(row, 1).toString());
                tahunLahirField.setText(model.getValueAt(row, 2).toString());
                kategoriComboBox.setSelectedItem(model.getValueAt(row, 3).toString());
                keaktifanSlider.setValue(Integer.parseInt(model.getValueAt(row, 4).toString()));

                // ID adalah kunci data, jadi tidak boleh diubah saat mode ubah
                idField.setEditable(false);

                addUpdateButton.setText("Update");
                deleteButton.setVisible(true);
            }
        });
    }

    // ================= setTable =================
    // membuat model tabel langsung dari hasil query SELECT
    private DefaultTableModel setTable() {
        String[] columnNames = {"ID", "Nama", "Tahun Lahir", "Kategori", "Keaktifan (%)"};

        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        try {
            ResultSet rs = database.selectQuery(
                    "SELECT id, nama, tahun_lahir, kategori, keaktifan FROM person ORDER BY id");
            while (rs.next()) {
                Object[] rowData = {
                        rs.getString("id"),
                        rs.getString("nama"),
                        rs.getInt("tahun_lahir"),
                        rs.getString("kategori"),
                        rs.getInt("keaktifan")
                };
                model.addRow(rowData);
            }
        } catch (SQLException ex) {
            tampilkanErrorDatabase(ex);
        }
        return model;
    }

    // ================= ambilDataForm =================
    // membaca & memvalidasi isi form. Return object Person, atau null kalau
    // ada input yang tidak valid (dialog error langsung ditampilkan).
    // modeTambah = true untuk insert (ID juga dicek duplikat), false untuk update.
    private Person ambilDataForm(boolean modeTambah) {
        String id = idField.getText().trim();
        String nama = namaField.getText().trim();
        String tahunText = tahunLahirField.getText().trim();

        // 1. cek kolom yang masih kosong
        String[] labelKolom = {"ID", "Nama", "Tahun Lahir", "Kategori"};
        boolean[] kosong = {
                id.isEmpty(),
                nama.isEmpty(),
                tahunText.isEmpty(),
                kategoriComboBox.getSelectedIndex() <= 0   // masih "???"
        };
        StringBuilder daftarKosong = new StringBuilder();
        for (int i = 0; i < labelKolom.length; i++) {
            if (kosong[i]) {
                daftarKosong.append("\n- ").append(labelKolom[i]);
            }
        }
        if (daftarKosong.length() > 0) {
            JOptionPane.showMessageDialog(this,
                    "Kolom berikut belum diisi:" + daftarKosong,
                    "Input Kosong", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        // 2. tahun lahir harus angka
        int tahunLahir;
        try {
            tahunLahir = Integer.parseInt(tahunText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Tahun Lahir harus berupa angka!",
                    "Input Tidak Valid", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        // 3. tahun lahir harus masuk akal
        int tahunSekarang = Year.now().getValue();
        if (tahunLahir < 1900 || tahunLahir > tahunSekarang) {
            JOptionPane.showMessageDialog(this,
                    "Tahun Lahir harus antara 1900 dan " + tahunSekarang + "!",
                    "Input Tidak Valid", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        // 4. saat insert, ID tidak boleh sudah ada di database
        if (modeTambah) {
            try {
                if (idSudahAda(id)) {
                    tampilkanErrorIdDuplikat(id);
                    return null;
                }
            } catch (SQLException ex) {
                tampilkanErrorDatabase(ex);
                return null;
            }
        }

        String kategori = (String) kategoriComboBox.getSelectedItem();
        int keaktifan = keaktifanSlider.getValue();
        return new Person(id, nama, tahunLahir, kategori, keaktifan);
    }

    // mengecek apakah ID sudah ada di tabel person
    private boolean idSudahAda(String id) throws SQLException {
        ResultSet rs = database.selectQuery("SELECT COUNT(*) FROM person WHERE id = ?", id);
        rs.next();
        return rs.getInt(1) > 0;
    }

    private void tampilkanErrorIdDuplikat(String id) {
        JOptionPane.showMessageDialog(this,
                "ID '" + id + "' sudah ada di database, gunakan ID lain!",
                "ID Duplikat", JOptionPane.ERROR_MESSAGE);
    }

    private void tampilkanErrorDatabase(SQLException ex) {
        JOptionPane.showMessageDialog(this,
                "Operasi database gagal:\n" + ex.getMessage(),
                "Error Database", JOptionPane.ERROR_MESSAGE);
    }

    // ================= insertData (Create) =================
    private void insertData() {
        Person personBaru = ambilDataForm(true);
        if (personBaru == null) {
            return;
        }

        try {
            database.insertUpdateDeleteQuery(
                    "INSERT INTO person (id, nama, tahun_lahir, kategori, keaktifan) "
                            + "VALUES (?, ?, ?, ?, ?)",
                    personBaru.getId(), personBaru.getNama(), personBaru.getTahunLahir(),
                    personBaru.getKategori(), personBaru.getKeaktifan());

            // refresh tabel dari database, lalu bersihkan form
            peopleTable.setModel(setTable());
            clearForm();
            System.out.println("Insert berhasil");
            JOptionPane.showMessageDialog(this, "Data berhasil ditambahkan!");
        } catch (SQLIntegrityConstraintViolationException ex) {
            // pengaman tambahan: database menolak ID kembar (primary key)
            tampilkanErrorIdDuplikat(personBaru.getId());
        } catch (SQLException ex) {
            tampilkanErrorDatabase(ex);
        }
    }

    // ================= updateData (Update) =================
    private void updateData() {
        Person personBaru = ambilDataForm(false);
        if (personBaru == null) {
            return;
        }

        try {
            database.insertUpdateDeleteQuery(
                    "UPDATE person SET nama = ?, tahun_lahir = ?, kategori = ?, keaktifan = ? "
                            + "WHERE id = ?",
                    personBaru.getNama(), personBaru.getTahunLahir(),
                    personBaru.getKategori(), personBaru.getKeaktifan(), selectedId);

            peopleTable.setModel(setTable());
            clearForm();
            System.out.println("Update berhasil");
            JOptionPane.showMessageDialog(this, "Data berhasil diubah!");
        } catch (SQLException ex) {
            tampilkanErrorDatabase(ex);
        }
    }

    // ================= deleteData (Delete) =================
    private void deleteData() {
        if (selectedId == null) {
            return;
        }

        // confirmation prompt sebelum data dihapus
        int konfirmasi = JOptionPane.showConfirmDialog(this,
                "Yakin ingin menghapus data dengan ID \"" + selectedId + "\"?",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (konfirmasi != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            database.insertUpdateDeleteQuery("DELETE FROM person WHERE id = ?", selectedId);

            peopleTable.setModel(setTable());
            clearForm();
            System.out.println("Delete berhasil");
            JOptionPane.showMessageDialog(this, "Data berhasil dihapus!");
        } catch (SQLException ex) {
            tampilkanErrorDatabase(ex);
        }
    }

    // ================= clearForm =================
    // mengosongkan form & mengembalikan tampilan ke mode "tambah data"
    private void clearForm() {
        idField.setText("");
        namaField.setText("");
        tahunLahirField.setText("");
        kategoriComboBox.setSelectedIndex(0);
        keaktifanSlider.setValue(50);

        idField.setEditable(true);
        addUpdateButton.setText("Add");
        deleteButton.setVisible(false);
        selectedId = null;
        peopleTable.clearSelection();
    }
}
