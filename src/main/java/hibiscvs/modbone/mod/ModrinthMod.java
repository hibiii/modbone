package hibiscvs.modbone.mod;

import java.util.HashMap;

public class ModrinthMod {

    private String projectId;
    private HashMap<String,String> versions;

    public ModrinthMod(String projectId, HashMap<String,String> versions) {
        this.projectId = projectId;
        this.versions = versions;
    }
}
