package hibiscvs.modbone.mod;

public class Mod {

    private String name;
    private ModrinthMod modrinth;
    private CurseforgeMod curseforge;

    public Mod(String name) {
        this.name = name;
    }

    public ModrinthMod getModrinthData() {
        return this.modrinth;
    }

    public CurseforgeMod getCurseforgeData() {
        return this.curseforge;
    }

    public String getName() {
        return this.name;
    }
}
