package hibiscvs.modbone;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import hibiscvs.modbone.db.Database;
import hibiscvs.modbone.db.MonitorEngine;
import hibiscvs.modbone.mod.Mod;

public class Main {

    public static final String CURSEFORGE_API_KEY = System.getenv("CURSEFORGE_API_KEY");
    public static final String USER_AGENT = "hibiii/modbone/1.0a1 (hibiscus.pet)";
    public static final String VERSION_INFORMATION = "modbone 1.0a1, made by hibi, licensed under MIT";

    public static void main(String[] args) throws Exception {
        parseArgs(args);
        for (Task task : tasks) {
            task.execute();
        }
        if (tasks.isEmpty()) {
            System.err.println("No tasks requested.");
            System.err.println(USAGE_TEXT);
            System.exit(0);
        }
    }

    public static String getModDefPath() {
        return Main.modDefPath;
    }
    public static String getDatabasePath() {
        return Main.dbPath;
    }

    private static String modDefPath = "./mods.json";
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
                | argumentMatches(arg, "--mods-file:", (text) -> modDefPath = text)
                | argumentMatches(arg, "--database-file:", (text) -> dbPath = text)
                | argumentMatches(arg, "monitor", (_) -> addTask(Task.MONITOR));
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
    The following tasks are available and at least one must be present:
    monitor
        Get current download metrics for the configured mods.
    
    The following options are available to use from the command line:
    --help
        Shows this help text and exits.
    --version
        Prints modbone version information and exits.
    --mods-file:<mod definition list JSON>
        Specify a different path to load the mod definition list from. Defaults to `./mods.json`.
    --database-file:<SQLite database>
        Specify a different path to load the database in which to store download
        records. DefaultsR)  to `./records.db`.
    """;

    private static List<Mod> mods = null;
    private static List<Task> tasks = new ArrayList<>(1);

    private static void addTask(Task task) {
        if (tasks.contains(task)) {
            return;
        }
        tasks.addLast(task);
    }

    private static enum Task {
        NONE,
        MONITOR { @Override public void execute() throws SQLException {
            if (Main.mods == null) {
                try {Main.mods = MonitorEngine.loadModList();}
                catch(FileNotFoundException e) {
                    Logger.error("Could not load the mod definitions list: %s".formatted(e.getMessage()));
                    System.exit(1);
                }
            }
            Database db = null;
            try {
                db = Database.getDatabase();
            } catch (SQLException e) {
                Logger.error("Could not load the database: %s".formatted(e.getMessage()));
                System.exit(5);
            }
            if (db.hasTodaysRecords()) {
                Logger.info("There's already records for today, ignoring");
                return;
            }
            MonitorEngine.performForAllMods(Main.mods);
        }},
        ;

        public void execute() throws Exception {}
    }
}
