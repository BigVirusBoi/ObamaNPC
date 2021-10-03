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
        String texture = "ewogICJ0aW1lc3RhbXAiIDogMTYzMzI2Mzg5NjIyNSwKICAicHJvZmlsZUlkIiA6ICIwNjlhNzlmNDQ0ZTk0NzI2YTViZWZjYTkwZTM4YWFmNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJOb3RjaCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8yOTIwMDlhNDkyNWI1OGYwMmM3N2RhZGMzZWNlZjA3ZWE0Yzc0NzJmNjRlMGZkYzMyY2U1NTIyNDg5MzYyNjgwIgogICAgfQogIH0KfQ==";
        String signature = "ArwoD4sGhthC32Qaq1oSwNOWPciJN54mLj+Tq0tZBUMCaw7Gnpj6W9HJhLrax6gVs8X3O5cWUrgLbAIF8uelb5jLdUpm9ZFsAFUo/MtE3oqCXBjoXw8+Wn8y8WR1UAXwv0ts+C6OSyOfLGk0tR7Jmkac6G7bUKYOAMFtCGcppdmoxvhALHPkcsPmdlE8SsHhOVDBp+SE9SBA0V5Z2YDTua34bLdCh4jHibb9x6D8yLxos5ksqcUzsLW9HZ6gqt29GqRD3+M2q1VyXyOjQCR1MD/5A0WfFAFBtExWPRn4V8Fl8a6+814a84H6apaoIN0e6rZHC9ArLEbfSStS54YbjFZ5jfUHx4jkyg0n16B14Z7KLVRmWJjUPtICWaW7zlOOzzq+ZkV1fckVmXEA0Ri349DnWMSGU44nkgPsjD5PL9PLdDqhWqXQGL9f3C+XmUC+5WWdE1cA2W+ZrTN0mZajlkmcwYL0priAZZfzubhVV6PqWAaM9phgaoK7s5oQc6ruaXObauGZvxZ2p+LDx8A+AKnpxSPvjE+fVoOZUAvzVIhwXkFo8Y7+lJi29GjNS8f+fZctPivnABnK2oHXVapvdWlOfpTg/Y8cgc+GHhsvY82f9p7tyFAjV59Ps2G3TDjNbxm7iRaNs4MBUf2e8+mQFt/MbbblCfDBMUOprV0vjks=";
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
