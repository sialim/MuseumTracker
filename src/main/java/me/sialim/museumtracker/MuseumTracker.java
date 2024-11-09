package me.sialim.museumtracker;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class MuseumTracker extends JavaPlugin implements Listener {
    public Map<UUID, VisitData> playerVisits = new HashMap<>();
    private int globalVisitNumber = 1;
    private final String FILE_PATH = "visit_data.txt";

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        loadVisitData();

        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new VisitPlaceholder(this).register();
        }
    }

    @Override
    public void onDisable() {
        saveVisitData();
    }

    @EventHandler public void onPlayerJoin(PlayerJoinEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        VisitData visitData = playerVisits.get(uuid);

        LocalDateTime now = LocalDateTime.now();

        if (visitData == null || Duration.between(visitData.visitStart, now).toHours() >= 24) {
            visitData = new VisitData(globalVisitNumber++, uuid, now);
            playerVisits.put(uuid, visitData);
        }

        visitData.joinTime = now;
    }

    @EventHandler public void onPlayerQuit(PlayerQuitEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        VisitData visitData = playerVisits.get(uuid);

        if (visitData != null && visitData.joinTime != null) {
            LocalDateTime now = LocalDateTime.now();
            Duration sessionTime = Duration.between(visitData.joinTime, now);
            visitData.totalPlaytime = visitData.totalPlaytime.plus(sessionTime);
        }
    }

    private void loadVisitData() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(":");
                int visitNumber = Integer.parseInt(data[0]);
                UUID uuid = UUID.fromString(data[1]);
                Duration playtime = Duration.parse(data[2]);
                LocalDateTime visitStart = LocalDateTime.parse(data[3]);

                VisitData visitData = new VisitData(visitNumber, uuid, visitStart);
                visitData.totalPlaytime = playtime;
                playerVisits.put(uuid, visitData);

                globalVisitNumber = Math.max(globalVisitNumber, visitNumber + 1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveVisitData() {
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (VisitData visitData : playerVisits.values()) {
                writer.write(visitData.toFileFormat());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public VisitData getVisitData(Player player) {
        return playerVisits.get(player.getUniqueId());
    }

    public int getVisitNumber(Player player) {
        VisitData visitData = playerVisits.get(player.getUniqueId());
        return visitData != null ? visitData.visitNumber : 0;
    }

    public static class VisitData {
        int visitNumber;
        UUID uuid;
        LocalDateTime visitStart;
        LocalDateTime joinTime;
        Duration totalPlaytime = Duration.ZERO;

        VisitData(int visitNumber, UUID uuid, LocalDateTime visitStart) {
            this.visitNumber = visitNumber;
            this.uuid = uuid;
            this.visitStart = visitStart;
        }

        String toFileFormat() {
            return visitNumber + ":" + uuid + ":" + totalPlaytime + ":" + visitStart;
        }
    }
}