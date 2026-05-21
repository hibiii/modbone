package hibiscvs.modbone;

import java.util.function.Consumer;

public class Main {

    public static final String CURSEFORGE_API_KEY = System.getenv("CURSEFORGE_API_KEY");
    public static final String USER_AGENT = "hibiii/modbone/1.0a1 (hibiscus.pet)";
    public static final String VERSION_INFORMATION = "modbone 1.0a1, made by hibi, licensed under MITj";

    public static void main(String[] args) {
        parseArgs(args);
    }

    private static String filePath = "./mods.json";

    private static void parseArgs(final String[] args) {
        if (args.length == 0) {
            System.err.println("No arguments passed.");
            System.err.println(USAGE_TEXT);
            System.exit(1);
        }
        for (String arg : args) {
            boolean hasMatched = false
                | argumentMatches(arg, "--version", (_) -> {
                    System.out.println(VERSION_INFORMATION);
                    System.exit(0); })
                | argumentMatches(arg, "--help", (_) -> {
                    System.out.println(USAGE_TEXT);
                    System.exit(0); })
                | argumentMatches(arg, "--mods-file:", (text) -> filePath = text);
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
    """;;
}
