package hibiscvs.modbone.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
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

    private Database(String filePath) throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:sqlite:%s".formatted(filePath));
    }

    private static Database instance = null;
}
