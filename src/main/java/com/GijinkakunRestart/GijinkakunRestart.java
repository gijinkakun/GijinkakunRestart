package com.GijinkakunRestart;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GijinkakunRestart extends JavaPlugin {

    private LocalTime restartTime;
    private final Set<String> validTimes = new HashSet<>(Arrays.asList("30", "15", "10", "5", "1"));

    @Override
    public void onEnable() {
        // Load configuration and initialize restart time
        saveDefaultConfig();
        try {
            restartTime = LocalTime.parse(getConfig().getString("restart-time", "00:00"));
            scheduleWarningsAndRestart();
            logToConsole("Gijinkakun Restart has been enabled!", ChatColor.GREEN);
        } catch (DateTimeParseException e) {
            logToConsole("Invalid time format in config.yml: " + e.getParsedString(), ChatColor.RED);
            getServer().getPluginManager().disablePlugin(this);
        }
        getCommand("gijinkakunrestart").setExecutor(this);
        getCommand("gijinkakunrestart").setTabCompleter(this);
    }

    @Override
    public void onDisable() {
        // Log a message when the plugin is disabled
        logToConsole("Gijinkakun Restart has been disabled!", ChatColor.RED);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("gijinkakunrestart")) {
            if (!sender.hasPermission("gijinkakunrestart.use")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                return true;
            }
            if (args.length != 1 || !validTimes.contains(args[0])) {
                sender.sendMessage(ChatColor.RED + "Invalid argument! Use /gijinkakunrestart [30|15|10|5|1]");
                return false;
            }
            int minutes = Integer.parseInt(args[0]);
            scheduleForcedRestart(minutes);
            String forcedRestartMessage = getConfig().getString("messages.forced_restart", "Forced restart scheduled in %minutes% minutes.");
            forcedRestartMessage = forcedRestartMessage.replace("%minutes%", String.valueOf(minutes));
            if (minutes == 1) {
                forcedRestartMessage = forcedRestartMessage.replace("minutes", "minute");
            }
            sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + forcedRestartMessage);
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("gijinkakunrestart")) {
            if (args.length == 1) {
                return validTimes.stream().filter(time -> time.startsWith(args[0])).collect(Collectors.toList());
            }
        }
        return null;
    }

    /**
     * Schedules a forced restart based on the specified number of minutes.
     *
     * @param minutes The number of minutes until the forced restart
     */
    private void scheduleForcedRestart(int minutes) {
        long seconds = minutes * 60;
        long delay = seconds * 20; // Convert to ticks

        String warningMessage = getConfig().getString("messages.warning_" + minutes, minutes + " mins Until Restart");
        if (minutes == 1) {
            warningMessage = getConfig().getString("messages.warning_1", "1 min Until Restart").replace("mins", "min");
        }

        scheduleWarning(warningMessage, delay);
        scheduleCountdown(delay - 10 * 20); // Start countdown 10 seconds before restart
    }

    /**
     * Schedules the warnings and restart tasks based on the configured restart time.
     */
    private void scheduleWarningsAndRestart() {
        LocalTime now = LocalTime.now();

        // Define warning times in seconds
        long thirtyMinutesInSeconds = 30 * 60;
        long fifteenMinutesInSeconds = 15 * 60;
        long tenMinutesInSeconds = 10 * 60;
        long fiveMinutesInSeconds = 5 * 60;
        long oneMinuteInSeconds = 1 * 60;

        // Schedule warnings and countdown
        scheduleWarning(getConfig().getString("messages.warning_30", "30 mins Until Restart"), computeDelay(now, restartTime, thirtyMinutesInSeconds));
        scheduleWarning(getConfig().getString("messages.warning_15", "15 mins Until Restart"), computeDelay(now, restartTime, fifteenMinutesInSeconds));
        scheduleWarning(getConfig().getString("messages.warning_10", "10 mins Until Restart"), computeDelay(now, restartTime, tenMinutesInSeconds));
        scheduleWarning(getConfig().getString("messages.warning_5", "5 mins Until Restart"), computeDelay(now, restartTime, fiveMinutesInSeconds));
        scheduleWarning(getConfig().getString("messages.warning_1", "1 min Until Restart").replace("mins", "min"), computeDelay(now, restartTime, oneMinuteInSeconds));
        scheduleCountdown(computeDelay(now, restartTime, 10)); // Schedule countdown to start 10 seconds before restart
    }

    /**
     * Computes the delay in ticks until a warning should be issued.
     *
     * @param now                The current time
     * @param targetTime         The target restart time
     * @param warningSecondsBefore The number of seconds before the restart time to issue the warning
     * @return The delay in ticks
     */
    private long computeDelay(LocalTime now, LocalTime targetTime, long warningSecondsBefore) {
        long secondsUntilRestart = now.until(targetTime, ChronoUnit.SECONDS);
        if (secondsUntilRestart < 0) {
            secondsUntilRestart += 24 * 60 * 60;
        }
        long delay = (secondsUntilRestart - warningSecondsBefore) * 20;
        getLogger().info("Computed delay: " + delay + " ticks for warning " + warningSecondsBefore + " seconds before restart");
        return Math.max(delay, 0);
    }

    /**
     * Schedules a warning message to be broadcasted at a specific delay.
     *
     * @param message The warning message
     * @param delay   The delay in ticks before the message is broadcasted
     */
    private void scheduleWarning(String message, long delay) {
        if (delay > 0) {
            logToConsole("Scheduling warning: " + message + " with delay " + delay + " ticks", ChatColor.RED);
            new BukkitRunnable() {
                @Override
                public void run() {
                    Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + message);
                    playAnvilSound();
                    logToConsole("[Gijinkakun Restart] " + message, ChatColor.YELLOW);
                }
            }.runTaskLater(this, delay);
        }
    }

    /**
     * Plays the anvil sound to all online players.
     */
    private void playAnvilSound() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 1.0f);
        }
    }

    /**
     * Schedules a countdown from 10 seconds to the restart time.
     *
     * @param initialDelay The initial delay in ticks before the countdown starts
     */
    private void scheduleCountdown(long initialDelay) {
        if (initialDelay > 0) {
            new BukkitRunnable() {
                int countdown = 10;

                @Override
                public void run() {
                    if (countdown > 0) {
                        String formattedMessage = ChatColor.RED + "" + ChatColor.BOLD + getConfig().getString("messages.countdown", "%seconds% seconds until restart.").replace("%seconds%", String.valueOf(countdown));
                        Bukkit.broadcastMessage(formattedMessage);
                        playAnvilSound();
                        getLogger().info(formattedMessage);
                        countdown--;
                    } else {
                        String formattedMessage = ChatColor.RED + "" + ChatColor.BOLD + getConfig().getString("messages.restart", "Server Restarting...");
                        Bukkit.broadcastMessage(formattedMessage);
                        playAnvilSound();
                        getLogger().info(formattedMessage);
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");
                        cancel();
                    }
                }
            }.runTaskTimer(this, initialDelay, 20);
        }
    }

    /**
     * Logs a message to the console with a specified color.
     *
     * @param message The message to log
     * @param color   The color of the message
     */
    public void logToConsole(String message, ChatColor color) {
        Bukkit.getConsoleSender().sendMessage(color + message);
    }
}