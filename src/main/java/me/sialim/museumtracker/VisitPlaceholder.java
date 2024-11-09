package me.sialim.museumtracker;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.LocalDateTime;

public class VisitPlaceholder extends PlaceholderExpansion {
    private final MuseumTracker plugin;

    public VisitPlaceholder(MuseumTracker plugin) { this.plugin = plugin;}

    @Override public boolean persist() { return true; }

    @Override public boolean canRegister() { return true; }

    @Override
    public @NotNull String getIdentifier() { return "museum"; }

    @Override
    public @NotNull String getAuthor() { return "sialim"; }

    @Override
    public @NotNull String getVersion() { return "1.0"; }

    @Override public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) return "";
        if (identifier.equals("visit_number")) {
            return String.valueOf(plugin.getVisitNumber(player));
        }
        if (identifier.equals("current_visit_time")) {
            return getCurrentVisitTime(player);
        }

        return null;
    }

    public String getCurrentVisitTime(Player player) {
        MuseumTracker.VisitData visitData = plugin.playerVisits.get(player.getUniqueId());
        if (visitData != null && visitData.joinTime != null) {
            Duration visitDuration = Duration.between(visitData.joinTime, LocalDateTime.now());

            long hours = visitDuration.toHours();
            long minutes = visitDuration.toMinutesPart();
            long seconds = visitDuration.toSecondsPart();

            return String.format("%d hours %d minutes %d seconds", hours, minutes, seconds);
        }
        return "0 hours 0 minutes 0 seconds";
    }
}
