package hibiscvs.modbone;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import com.google.gson.Gson;

import hibiscvs.modbone.db.Database;
import hibiscvs.modbone.mod.Mod;

public class Main {

    public static final String CURSEFORGE_API_KEY = System.getenv("CURSEFORGE_API_KEY");
    public static final String USER_AGENT = "hibiii/modbone/1.0a1 (hibiscus.pet)";
    public static final String VERSION_INFORMATION = "modbone 1.0a1, made by hibi, licensed under MIT";

    public static void main(String[] args) throws SQLException {
        parseArgs(args);
        List<Mod> mods = null;
        try {mods = loadMods();}
        catch(FileNotFoundException e) {
            System.err.println("Could not load the mod definitions list: %s".formatted(e.getMessage()));
            System.exit(5);
        }
        Database db = null;
        try {
            db = Database.getDatabase(dbPath);
        } catch (SQLException e) {
            System.err.println("Could not load the database: %s".formatted(e.getMessage()));
            System.exit(5);
        }
        if(db.hasTodaysRecords()) {
            System.err.println("Metrics for today have already been recorded, exiting");
            System.exit(0);
        }
        db.verifyModsList(mods);
        System.out.println("OK");
    }

    private static String filePath = "./mods.json";
    private static String dbPath = "./records.db";

    private static void parseArgs(final String[] args) {
        for (String arg : args) {
            boolean hasMatched = false
                | argumentMatches(arg, "--version", (_) -> {
                    System.out.println(VERSION_INFORMATION);
                    System.exit(0); })
                | argumentMatches(arg, "--help", (_) -> {
                    System.out.println(USAGE_TEXT);
                    System.exit(0); })
                | argumentMatches(arg, "--mods-file:", (text) -> filePath = text)
                | argumentMatches(arg, "--database-file:", (text) -> dbPath = text);
            if (hasMatched == false) {
                System.err.println("Unknown argument \"%s\".".formatted(arg));
                System.exit(1);
            }
        }
    };

    private static boolean argumentMatches(String current, String param, Consumer<String> onMatch) {
        if (!current.startsWith(param)) {
            return false;
        }
        onMatch.accept(current.substring(param.length()));
        return true;
    }

    private static final String USAGE_TEXT =
    """
    The following parameters are available to use from the command line:
    --help
        Shows this help text and exits.
    --version
        Prints modbone version information and exits.
    --mods-file:<mod definition list JSON>
        Specify a different path to load the mod definition list from. Defaults to `./mods.json`.
    --database-file:<SQLite database>
        Specify a different path to load the database in which to store download
        records. Defaults to `./records.db`.
    """;;

    private static List<Mod> loadMods() throws FileNotFoundException {
        Gson gson = new Gson();
        FileReader reader = new FileReader(filePath);
        Mod[] mods = gson.fromJson(reader, Mod[].class);
        return Arrays.asList(mods);
    }
}
