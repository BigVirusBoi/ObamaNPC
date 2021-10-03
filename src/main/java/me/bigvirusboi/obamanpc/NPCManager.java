package me.bigvirusboi.obamanpc;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NPCManager {
    private static final List<EntityPlayer> npcList = new ArrayList<>();

    public static void createNPC(Player player, String name) {
        MinecraftServer server = MinecraftServer.getServer();
        Location loc = player.getLocation();
        WorldServer world = ((CraftWorld) loc.getWorld()).getHandle();

        UUID uuid = UUID.randomUUID();
        EntityPlayer npc = new EntityPlayer(server, world, new GameProfile(uuid, name));
        npc.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());

        /* Retrieving skins:
         *  - First thing to understand is that skins have to be "signed" by Mojang. Each skin has two parts: texture and signature
         *  - You can retrieve this data from:
         *    https://sessionserver.mojang.com/session/minecraft/profile/UUID?unsigned=false
         *  - UUID = the uuid of a player without the dashes
         *  - UUID data can only be retrieved every 60 seconds (if same uuid)
         */
        String texture = "ewogICJ0aW1lc3RhbXAiIDogMTYzMzI1ODYyNzkwNCwKICAicHJvZmlsZUlkIiA6ICI4ZDU5NTFhMzkwN2I0OWMxOTZiNDA0YWRjYjU5ZjU4YyIsCiAgInByb2ZpbGVOYW1lIiA6ICJzdXMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzY3NGY5OTQzNWEzOGQ3Y2Q3NDY5OTE1OTJkM2IzOGEwZDQ0OTc4OGQ0MGVhOTBkMzI5MDczNjNmYWIxZWU1MCIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9";
        String signature = "UJnkh+AWLhwTZwc4pSwM91aJWMZTOSwAFF2CEBl2uo6yL47IkDN3AtdA7xbCPIjG3qDhtCuwocPFRVpR70eB6s04VFpQnOsyF5+yiqI+spbfrhM3ZfLE6k94oTZdHFqNvCqqNGU5sz49ewRAR5x4nKbsEkmPRr6tvXioZqmggW0umeipSJY27DHNEJOXsC4c1AwCf+vY+/ew45JAbmwQ8QAo4QNjeXk4tt5w5rPQo26KSaFGOOURR89VA4mpSnRPCTkXD8DeZIgfEoYWjydYgQHTAo5tYUHsfve5W8X/v+xzBwIj/yHORB8RxAvbjLk4e5OIX2W5jJZnxWrx0HUUjVgmr2XfKVEZGOsd+5QVurHrDbgv3008Hzi5VpiFZRQPNXnJ8PkDeNKzhWbGC3jO5lkaKycKa0QtEpz2+GVVCwmeKYZKDtO/wYLG6eHrgn/FEnjQPNMtYVwgpGEldGG4D34Ir9ac4Mv0QJUmNc6+Nq5MPGs67Mb6uPZRHSEZfl0o9Hvq9TJCxf4itHeSB2UpP1epGMvD/KJE72HdYrGjDEtsr/8KJ36WOIxPv6zbP8KiKgb9n7fwkVdlfjSMcS7d8StToMiKnqJyMzIquIGn7S6W6VCC1qQKfpS/c5C4yuwzB7IRHZgXGIBbVhKh8GF/YIz4JBLQYrgQEipskUFgOco=";
        npc.getProfile().getProperties().put("textures", new Property("textures", texture, signature));
        // Activate skin layers - NOT WORKING
        //npc.getDataWatcher().set(DataWatcherRegistry.a.a(16), (byte) 127);

        sendAddPackets(npc);
        npcList.add(npc);
    }

    public static void sendAddPackets(EntityPlayer npc) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendPackets(player, npc);
        }
    }

    public static void sendJoinPackets(Player player) {
        for (EntityPlayer npc : npcList) {
            sendPackets(player, npc);
        }
    }

    public static void sendPackets(Player player, EntityPlayer npc) {
        // MOJANG MAPPINGS - getHandle().playerConnection
        PlayerConnection connection = ((CraftPlayer) player).getHandle().b;
        // MOJANG MAPPINGS - PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER
        connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a, npc));
        connection.sendPacket(new PacketPlayOutNamedEntitySpawn(npc));
        // Activate skin layers - NOT WORKING
        //connection.sendPacket(new PacketPlayOutEntityMetadata(npc.getId(), npc.getDataWatcher(), true));

        // This will remove the npc from the tab list IMPORTANT TO MAKE DELAYED
        Bukkit.getScheduler().runTaskLater(ObamaNPC.getInstance(), () ->
                // MOJANG MAPPINGS - PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER
                connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.e, npc)), 20);

        sendLookPacket(player);
    }

    public static void sendLookPacket(Player player) {
        for (EntityPlayer npc : npcList) {
            Location loc = npc.getBukkitEntity().getLocation();
            loc.setDirection(player.getLocation().subtract(loc).toVector());
            float yaw = loc.getYaw();
            float pitch = loc.getPitch();

            // MOJANG MAPPINGS - getHandle().playerConnection
            PlayerConnection connection = ((CraftPlayer) player).getHandle().b;
            connection.sendPacket(new PacketPlayOutEntity.PacketPlayOutEntityLook(npc.getId(), (byte) ((yaw%360.)*256/360), (byte) ((pitch%360.)*256/360), false));
            connection.sendPacket(new PacketPlayOutEntityHeadRotation(npc, (byte) ((yaw%360)*256/360)));
        }
    }

    public static List<EntityPlayer> getNPCList() {
        return npcList;
    }
}
