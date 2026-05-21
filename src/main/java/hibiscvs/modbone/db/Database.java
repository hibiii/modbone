package hibiscvs.modbone.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hibiscvs.modbone.Main;
import hibiscvs.modbone.mod.Mod;

public final class Database {

    private Connection connection = null;

    public static Database getDatabase() throws SQLException {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    public void verifyModsList(List<Mod> mods) throws SQLException {
        Set<String> definedMods = new HashSet<>();
        for (Mod mod : mods) {
            PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO mod_id (name) VALUES (?) ON CONFLICT (name) DO NOTHING"
            );
            statement.setString(1, mod.getName());
            statement.executeUpdate();
            definedMods.add(mod.getName());
        }
        Statement statement = connection.createStatement();
        ResultSet results = statement.executeQuery("SELECT name FROM mod_id");
        while (results.next()) {
            String name = results.getString("name");
            if (!definedMods.contains(name)) {
                System.err.println("WARN: mod \"%s\" is recorded in the database but is not defined, keeping".formatted(name));
            }
        }
    }

    public void storeTodaysRecords(Mod mod, Map<String,DownloadNumberPair> downloads) throws SQLException {
        String todaysDate = todaysDate();
        int modId = 0;
        try (PreparedStatement statement = connection.prepareStatement("SELECT id FROM mod_id WHERE name = ?")) {
            statement.setString(1, mod.getName());
            ResultSet results = statement.executeQuery();
            modId = results.getInt("id");
        }
        try {
            this.connection.setAutoCommit(false);
            PreparedStatement statement  = this.connection.prepareStatement(
                "INSERT OR IGNORE INTO records (date, mod_id, mod_version, modrinth_downloads, curseforge_downloads) VALUES (?, ?, ?, ?, ?)"
            );
            for (Map.Entry<String,DownloadNumberPair> entry : downloads.entrySet()) {
                statement.setString(1, todaysDate);
                statement.setInt(2, modId);
                statement.setString(3, entry.getKey());
                DownloadNumberPair dls = entry.getValue();
                statement.setInt(4, dls.modrinth());
                statement.setInt(5, dls.curseforge());
                statement.addBatch();
            }
            statement.executeBatch();
            this.connection.commit();
        } catch (SQLException e) {
            this.connection.rollback();
            throw e;
        }
    }

    public boolean hasTodaysRecords() throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT date FROM records WHERE date = ?");
        statement.setString(1, todaysDate());
        ResultSet results = statement.executeQuery();
        return results.next();
    }

    private static String todaysDate() {
        LocalDate today = LocalDate.now();
        return "%04d-%02d-%02d".formatted(today.getYear(), today.getMonthValue(), today.getDayOfMonth());
    }

    private Database() throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:sqlite:%s".formatted(Main.getDatabasePath()));
    }

    private static Database instance = null;

    public static record DownloadNumberPair(Integer modrinth, Integer curseforge) {
        public DownloadNumberPair combineCurseForge(Integer curseforge) {
            Integer newCf = null;
            if (this.curseforge == null) {
                newCf = curseforge;
            }
            if (curseforge == null) {
                newCf = this.curseforge;
            }
            return new DownloadNumberPair(this.modrinth, newCf);
        }
        public DownloadNumberPair combineModrinth(Integer modrinth) {
            Integer newMr = null;
            if (this.modrinth == null) {
                newMr = modrinth;
            }
            if (modrinth == null) {
                newMr = this.modrinth;
            }
            return new DownloadNumberPair(newMr, this.curseforge);
        }
        public static final DownloadNumberPair DEFAULT = new DownloadNumberPair(null, null);
    }
}
