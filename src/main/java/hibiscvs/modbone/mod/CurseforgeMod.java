package hibiscvs.modbone.mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import hibiscvs.modbone.Main;
import hibiscvs.modbone.net.Request;

public class CurseforgeMod {

    private String modId;
    @SerializedName("fileIds")
    private HashMap<String,String> names2Ids;

    public CurseforgeMod(String modId, HashMap<String,String> names2Ids) {
        this.modId = modId;
        this.names2Ids = names2Ids;
    }

    public Map<String,Integer> getDownloadNumbers() {
        Map<String,Integer> names2Downloads = new HashMap<>();
        Map<String,Integer> ids2Downloads = new HashMap<>();
        List<CurseforgeFile> files = this.collectFiles();
        for (CurseforgeFile file : files) {
            ids2Downloads.put(file.id, file.downloadCount);
        }
        for (Map.Entry<String,String> entry : this.names2Ids.entrySet()) {
            String name = entry.getKey();
            String id = entry.getValue();
            names2Downloads.put(name, ids2Downloads.get(id));
        }
        return names2Downloads;
    }
    
    public HashMap<String, String> getNames2Ids() {
        return names2Ids;
    }

    private List<CurseforgeFile> collectFiles() {
        List<CurseforgeFile> files = new ArrayList<>();
        Gson gson = new Gson();
        int seenFiles = 0;
        int totalFiles = Integer.MAX_VALUE;
        while (seenFiles < totalFiles) {
            CurseforgeResponse response = getFilesPage(gson, seenFiles);
            for (CurseforgeFile file : response.data) {
                files.add(file);
            }
            seenFiles += response.pagination.resultCount;
            totalFiles = response.pagination.totalCount;
        }
        return files;
    }

    private CurseforgeResponse getFilesPage(Gson gson, int index) {
        String response = new Request("https://api.curseforge.com/v1/mods/%s/files?index=%d".formatted(this.modId, index)).header("x-api-key", Main.CURSEFORGE_API_KEY).get();
        return gson.fromJson(response, CurseforgeResponse.class);
    }

    private class CurseforgeFile {
        public String id;
        public int downloadCount;
    }

    private class CurseforgePagination {
        public int resultCount;
        public int totalCount;
    }

    private class CurseforgeResponse {
        public CurseforgeFile[] data;
        public CurseforgePagination pagination;
    }
}
