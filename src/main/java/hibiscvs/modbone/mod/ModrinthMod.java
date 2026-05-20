package hibiscvs.modbone.mod;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import hibiscvs.modbone.net.Request;
import hibiscvs.modbone.net.json.ModrinthResponse;

public class ModrinthMod {

    private String projectId;
    private HashMap<String,String> names2Ids;

    public ModrinthMod(String projectId, HashMap<String,String> names2Ids) {
        this.projectId = projectId;
        this.names2Ids = names2Ids;
    }

    public Map<String,Integer> getDownloadNumbers() {
        Map<String,Integer> names2Downloads = new HashMap<>();
        Map<String,Integer> ids2Downloads = new HashMap<>();
        String response = new Request("https://api.modrinth.com/v2/project/%s/version".formatted(this.projectId)).get();
        Gson gson = new Gson();
        ModrinthResponse[] versions = gson.fromJson(response, ModrinthResponse[].class);
        for (ModrinthResponse version : versions) {
            ids2Downloads.put(version.id, version.downloads);
        }
        for (Map.Entry<String,String> entry : this.names2Ids.entrySet()) {
            String name = entry.getKey();
            String id = entry.getValue();
            names2Downloads.put(name, ids2Downloads.get(id));
        }
        return names2Downloads;
    }
}
