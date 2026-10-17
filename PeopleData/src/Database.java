import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * File: Database.java
 * Class perantara antara program Java dan database MySQL (lewat JDBC).
 * Semua query dijalankan dengan PreparedStatement: nilai input dikirim
 * sebagai parameter (tanda ?), bukan digabung langsung ke teks SQL. Dengan
 * begitu nama yang mengandung tanda petik (mis. O'Brien) tidak merusak
 * query, dan program aman dari SQL injection.
 */
public class Database {

    // ===== Pengaturan koneksi =====
    // Sesuaikan dengan MySQL di komputer masing-masing
    // (default XAMPP / Laragon: user "root" dan password kosong).
    private static final String DB_NAME = "db_people";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/" + DB_NAME;
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    private Connection connection;

    // statement SELECT yang terakhir dijalankan; ditutup sebelum SELECT
    // berikutnya supaya tidak menumpuk
    private PreparedStatement selectStatement;

    // ================= constructor =================
    // membuka koneksi ke database; melempar SQLException jika gagal
    public Database() throws SQLException {
        // Driver MySQL (mysql-connector-j) terdaftar otomatis oleh JDBC selama
        // file .jar-nya sudah ditambahkan ke Dependencies project. Jika belum,
        // getConnection() melempar "No suitable driver found".
        connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // ================= selectQuery =================
    // menjalankan query SELECT dan mengembalikan hasilnya (ResultSet).
    // params = nilai yang menggantikan tanda ? pada query, urut dari kiri.
    public ResultSet selectQuery(String sql, Object... params) throws SQLException {
        if (selectStatement != null) {
            selectStatement.close();
        }
        selectStatement = connection.prepareStatement(sql);
        isiParameter(selectStatement, params);
        return selectStatement.executeQuery();
    }

    // ================= insertUpdateDeleteQuery =================
    // menjalankan query INSERT / UPDATE / DELETE.
    // mengembalikan jumlah baris yang terpengaruh.
    public int insertUpdateDeleteQuery(String sql, Object... params) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            isiParameter(statement, params);
            return statement.executeUpdate();
        }
    }

    // mengisi tanda ? pada query dengan nilai-nilai dari params
    private void isiParameter(PreparedStatement statement, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
    }

    // menutup koneksi saat program berakhir
    public void close() {
        try {
            if (selectStatement != null) {
                selectStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            System.out.println("Gagal menutup koneksi: " + e.getMessage());
        }
    }
}
