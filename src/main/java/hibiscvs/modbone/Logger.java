package hibiscvs.modbone;

public class Logger {
    public static void setLogLevel(int newLevel) {
        Logger.level = newLevel;
    }
    public static void error(String message) {
        System.err.println("[ERR] " + message);
    }
    public static void warning(String message) {
        if(level >= LEVEL_WARN)
            System.err.println("[WARN] " + message);
    }
    public static void info(String message) {
        if(level >= LEVEL_INFO)
            System.err.println("[INFO] " + message);
    }

    public static final int LEVEL_INFO = 0, LEVEL_WARN = 1;

    private static int level;
}
