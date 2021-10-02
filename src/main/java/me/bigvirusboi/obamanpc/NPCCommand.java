package me.bigvirusboi.obamanpc;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.network.protocol.game.PacketPlayOutEntityHeadRotation;
import net.minecraft.network.protocol.game.PacketPlayOutNamedEntitySpawn;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class NPCCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // KEEP IN MIND THAT THIS ONLY DISPLAYS TO THE SENDER
        if (sender instanceof Player player) {
            MinecraftServer server = MinecraftServer.getServer();
            WorldServer world = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();

            UUID uuid = UUID.randomUUID();
            EntityPlayer npc = new EntityPlayer(server, world, new GameProfile(uuid, "Hamburger"));
            Location loc = player.getLocation();
            npc.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());

            PacketPlayOutEntityHeadRotation rotation = new PacketPlayOutEntityHeadRotation(npc, (byte) ((loc.getYaw() * 256.0F) / 360.0F));
            // MOJANG MAPPINGS - EntityPlayer.playerConnection
            PlayerConnection connection = ((CraftPlayer) player).getHandle().b;

            String texture = "eyJ0aW1lc3RhbXAiOjE1NTc5MTczNjMzMDUsInByb2ZpbGVJZCI6IjA2OWE3OWY0NDRlOTQ3MjZhNWJlZmNhOTBlMzhhYWY1IiwicHJvZmlsZU5hbWUiOiJOb3RjaCIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjkyMDA5YTQ5MjViNThmMDJjNzdkYWRjM2VjZWYwN2VhNGM3NDcyZjY0ZTBmZGMzMmNlNTUyMjQ4OTM2MjY4MCJ9fX0=";
            String signature = "c/E/YYTTc4DglcI8c/2mPm3E2v+zCYZX+XfS0cYaWcFEeMmyGvTSr0pdRxC2domDR1j2oCecYW+ZYeS8fLi6m8M0gbiFnOWJEF1n9zyNFsQbiTX+kZDMTG0j8SDMsgEzBOYqPjOjiWXmYRAVek9Pa4oiJJ2wrmMvOKCD1X0ZnCKDXbrJ91FYESLzVqabhwypZ5vxArhA5U8aLJHr9SFjxwKoF/JjY3HEncfPfaxO9HJgkweTRu1KqAitI2FnW473hwTzt8w/saYrOVfwARHKXLv5EZE9Qo+OmEgeUaxYIj//TtyH7pBj8gJ4OCAIiZY87zOPHfK7pIalN+2HPT/ylIOXS5nBOiCc48+BmcwYrCD9mlNDLFCNV22PNaVe0yDRFK95ubkCawTaGohxsj+UIJMuCLA8feevw1wzxiNblK7AaT2T/LXKAlphm3Esz0AXFklcVA/y13ZkmKfxbU6oPflV1oW5W6iJALLwDclnqE1Smo5ao6wome+/ZjPjGDzYzlMhBy6Og4Jp+cGLQ7ZeN8SSQeRI+sgMbAd4C/FFTeojMjxvUrGfLLUxhN577gaog8Q4kk6syxb71vkD6a6sBN008Ym6rpMK5mjcvmHbb6V4VbszgvOmYr4IcTDV44G2NwlNr+zLRz61SVHKDSZjbAJJO06iawLeQy4UZ+MeLjc=";
            npc.getProfile().getProperties().put("textures", new Property("textures", texture, signature));

            // MOJANG MAPPINGS - PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER
            connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a, npc));
            connection.sendPacket(new PacketPlayOutNamedEntitySpawn(npc));
            connection.sendPacket(rotation);
        }
        return true;
    }
}
