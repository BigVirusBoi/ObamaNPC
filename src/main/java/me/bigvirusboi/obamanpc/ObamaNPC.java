package me.bigvirusboi.obamanpc;

import org.bukkit.plugin.java.JavaPlugin;

public final class ObamaNPC extends JavaPlugin {
    @Override
    public void onEnable() {
        getCommand("npc").setExecutor(new NPCCommand());
    }
}
