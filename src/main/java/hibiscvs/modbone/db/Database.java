package hibiscvs.modbone.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hibiscvs.modbone.mod.Mod;

public final class Database {

    private Connection connection = null;

    public static Database getDatabase(String filePath) throws SQLException {
        if (instance == null) {
            instance = new Database(filePath);
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

    public void storeTodaysRecords(Mod mod, Map<String,Integer> modrinth, Map<String,Integer> curseforge) throws SQLException {
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
                "INSERT INTO records (date, mod_id, mod_version, modrinth_downloads, curseforge_downloads) VALUES (?, ?, ?, ?, ?)"
            );
            Map<String,VersionDownloadCounts> combined = new HashMap<>();
            modrinth.forEach((key, value) -> combined.put(key, new VersionDownloadCounts(value, 0)));
            curseforge.forEach((key, value) -> combined.put(key, combined.getOrDefault(key, VersionDownloadCounts.DEFAULT).withCurseforge(value)));
            for (Map.Entry<String,VersionDownloadCounts> entry : combined.entrySet()) {
                statement.setString(1, todaysDate);
                statement.setInt(2, modId);
                statement.setString(3, entry.getKey());
                VersionDownloadCounts dls = entry.getValue();
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

    private Database(String filePath) throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:sqlite:%s".formatted(filePath));
    }

    private static Database instance = null;

    private record VersionDownloadCounts(int modrinth, int curseforge) {
        public VersionDownloadCounts withCurseforge(int dl) {
            return new VersionDownloadCounts(this.modrinth, dl);
        }
        public static final VersionDownloadCounts DEFAULT = new VersionDownloadCounts(0, 0);
    }
}
