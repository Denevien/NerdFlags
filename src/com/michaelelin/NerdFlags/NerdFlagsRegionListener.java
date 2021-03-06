package com.michaelelin.NerdFlags;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

import com.sk89q.worldedit.Vector;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.mewin.WGRegionEvents.events.RegionEnteredEvent;
import com.mewin.WGRegionEvents.events.RegionLeftEvent;
import com.sk89q.worldguard.protection.flags.StateFlag;

public class NerdFlagsRegionListener implements Listener {

    private NerdFlagsPlugin plugin;

    public NerdFlagsRegionListener(NerdFlagsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerEnteredRegion(RegionEnteredEvent event) {
        if (event.getRegion().getFlag(plugin.WEATHER) == StateFlag.State.ALLOW) {
            PacketContainer weatherPacket = plugin.protocolManager.createPacket(PacketType.Play.Server.GAME_STATE_CHANGE);
            weatherPacket.getIntegers().write(0, 2);
            weatherPacket.getFloat().write(0, 0F);
            try {
                plugin.protocolManager.sendServerPacket(event.getPlayer(), weatherPacket);
            } catch (InvocationTargetException e) {
            }
        }
        String entryCommands = event.getRegion().getFlag(plugin.ENTRY_COMMANDS);
        com.sk89q.worldedit.Location warp = event.getRegion().getFlag(plugin.WARP);
        if (entryCommands != null) {
            for (String command : parseCommands(entryCommands)) {
                plugin.getServer().dispatchCommand(event.getPlayer(), command);
            }
        }
        if (warp != null) {
            Player player = event.getPlayer();
            Vector vec = warp.getPosition();
            World world = Bukkit.getWorld(warp.getWorld().getName());
            Location loc = new Location(world, vec.getX(), vec.getY(), vec.getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());
            player.teleport(loc);
            player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1f, 1f);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLeftRegion(RegionLeftEvent event) {
        if (event.getRegion().getFlag(plugin.WEATHER) == StateFlag.State.ALLOW && !event.getPlayer().getWorld().hasStorm()) {
            PacketContainer weatherPacket = plugin.protocolManager.createPacket(PacketType.Play.Server.GAME_STATE_CHANGE);
            weatherPacket.getIntegers().write(0, 1);
            weatherPacket.getFloat().write(0, 0F);
            try {
                plugin.protocolManager.sendServerPacket(event.getPlayer(), weatherPacket);
            } catch (InvocationTargetException e) {
            }
        }
    }

    private static List<String> parseCommands(String commands) {
        List<String> commandList = new LinkedList<String>();
        StringBuilder curr = new StringBuilder();
        boolean escape = false;
        for (char c : commands.toCharArray()) {
            if (c == '|' && !escape) {
                commandList.add(curr.toString());
                curr = new StringBuilder();
                escape = false;
            } else if (c == '\\') {
                if (escape) {
                    curr.append('\\');
                }
                escape = !escape;
            } else {
                curr.append(c);
                escape = false;
            }
        }
        commandList.add(curr.toString());
        return commandList;
    }

}
