package me.bigvirusboi.obamanpc;

import net.minecraft.server.level.EntityPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class ObamaNPC extends JavaPlugin implements CommandExecutor, Listener {
    private static ObamaNPC instance;

    @Override
    public void onEnable() {
        instance = this;

        getCommand("npc").setExecutor(this);
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            String name = "§6§lCheeseburger";
            if (args.length > 0) {
                if (args[0] != null && !args[0].equals("")) {
                    name = "§6§l" + args[0];
                }
            }
            NPCManager.createNPC(player, name);
            player.sendMessage("§eCreated NPC with name: §6§l" + name);
        }
        return true;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!NPCManager.getNPCList().isEmpty()) {
            NPCManager.sendJoinPackets(e.getPlayer());
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!NPCManager.getNPCList().isEmpty()) {
            NPCManager.sendLookPacket(e.getPlayer());
        }
    }

    public static ObamaNPC getInstance() {
        return instance;
    }
}
