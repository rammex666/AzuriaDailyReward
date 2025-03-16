package fr.rammex.azuriadailyreward.database;

import fr.rammex.azuriadailyreward.AzuriaDailyReward;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.logging.Level;

public class SQliteManager {
    private static String dbname;
    private static File dataFolder;

    public SQliteManager(String databaseName, File folder) {
        dbname = databaseName;
        dataFolder = folder;
    }

    public void initialize() {
        try (Connection connection = getSQLConnection()) {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM head_data");
            ResultSet rs = ps.executeQuery();
            close(ps, rs);
        } catch (SQLException ex) {
            AzuriaDailyReward.instance.getLogger().log(Level.SEVERE, "Unable to retrieve connection", ex);
        }
    }

    public static Connection getSQLConnection() {
        File folder = new File(dataFolder, dbname + ".db");
        if (!folder.getParentFile().exists()) {
            folder.getParentFile().mkdirs();
        }
        if (!folder.exists()) {
            try {
                folder.createNewFile();
            } catch (IOException e) {
                AzuriaDailyReward.instance.getLogger().log(Level.SEVERE, "File write error: " + dbname + ".db");
            }
        }
        try {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection("jdbc:sqlite:" + folder);
        } catch (SQLException | ClassNotFoundException ex) {
            AzuriaDailyReward.instance.getLogger().log(Level.SEVERE, "SQLite exception on initialize", ex);
        }
        return null;
    }

    public void load() {
        try (Connection connection = getSQLConnection()) {
            Statement s = connection.createStatement();

            // Table for heads
            String cooldowns = "CREATE TABLE IF NOT EXISTS cooldowns (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "`uuid` TEXT," +
                    "`cooldown` TEXT," +
                    ");";



            s.executeUpdate(cooldowns);

            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        initialize();
    }

    public static void close(PreparedStatement ps, ResultSet rs) {
        try {
            if (ps != null) {
                ps.close();
            }
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException ingored) {
        }
    }
}
