package hibiscvs.modbone.db;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import hibiscvs.modbone.Main;
import hibiscvs.modbone.db.Database.DownloadNumberPair;
import hibiscvs.modbone.mod.Mod;

public class MonitorEngine {

    public static List<Mod> loadModList() throws FileNotFoundException {
        Gson gson = new Gson();
        FileReader reader = new FileReader(Main.getModDefPath());
        Mod[] mods = gson.fromJson(reader, Mod[].class);
        return Arrays.asList(mods);
    }

    public static void performForAllMods(List<Mod> mods) throws SQLException {
        for (Mod mod : mods) {
            MonitorEngine.getAndStoreMetrics(mod);
        }
    }

    public static void getAndStoreMetrics(Mod mod) throws SQLException {
        Database db = Database.getDatabase();
        Map<String,DownloadNumberPair> downloads = MonitorEngine.getDownloadsForMod(mod);
        db.storeTodaysRecords(mod, downloads);
    }

    public static Map<String,DownloadNumberPair> getDownloadsForMod(Mod mod) {
        Map<String,Integer> modrinth = mod.getModrinthData().getDownloadNumbers();
        Map<String,Integer> curseforge = mod.getCurseforgeData().getDownloadNumbers();
        return combineMrCf(modrinth, curseforge);
    }

    public static Map<String,DownloadNumberPair> combineMrCf(Map<String,Integer> modrinth, Map<String,Integer> curseforge) {
        Map<String,DownloadNumberPair> combined = new HashMap<>();
        modrinth.forEach((key, value) -> {
            combined.put(key, new DownloadNumberPair(value, null));
        });
        curseforge.forEach((key, value) -> {
            DownloadNumberPair pair = combined.getOrDefault(key, DownloadNumberPair.DEFAULT);
            pair = pair.combineCurseForge(value);
            combined.put(key, pair);
        });
        return combined;
    }
}
