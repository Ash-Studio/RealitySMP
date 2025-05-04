package me.ash.realitySMP.jobs;

import me.ash.realitySMP.RealitySMP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TaxiDriverJob implements Listener {

    private final RealitySMP plugin;
    private final Map<UUID, Double> fareRates = new HashMap<>();
    private final Map<UUID, Map<UUID, Double>> activePassengers = new HashMap<>();
    private final Map<UUID, Long> tripStartTimes = new HashMap<>();

    public TaxiDriverJob(RealitySMP plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Called when a player enters a vehicle
     */
    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (!(event.getEntered() instanceof Player)) return;

        Player passenger = (Player) event.getEntered();
        Entity vehicle = event.getVehicle();

        // Check if the vehicle has a driver
        if (vehicle.getPassengers().isEmpty()) return;

        Entity possibleDriver = vehicle.getPassengers().get(0);
        if (!(possibleDriver instanceof Player)) return;

        Player driver = (Player) possibleDriver;
        UUID driverId = driver.getUniqueId();

        // Check if driver is a taxi driver
        if (!plugin.getJobManager().hasJob(driverId, "taxi")) return;

        // Start tracking this passenger
        Map<UUID, Double> passengers = activePassengers.computeIfAbsent(
                driverId, k -> new HashMap<>());
        passengers.put(passenger.getUniqueId(), 0.0);

        // Record start time
        tripStartTimes.put(passenger.getUniqueId(), System.currentTimeMillis());

        // Notify both parties
        driver.sendMessage(ChatColor.YELLOW + passenger.getName() + " has entered your taxi!");

        double rate = fareRates.getOrDefault(driverId, 10.0);
        passenger.sendMessage(ChatColor.YELLOW + "You've entered " + driver.getName() + "'s taxi. The fare is $" + rate + " per minute.");
    }

    /**
     * Called when a player exits a vehicle
     */
    @EventHandler
    public void onVehicleExit(VehicleExitEvent event) {
        if (!(event.getExited() instanceof Player)) return;

        Player exitingPlayer = (Player) event.getExited();
        UUID exitingId = exitingPlayer.getUniqueId();

        // If player is a driver with passengers
        if (plugin.getJobManager().hasJob(exitingId, "taxi") && activePassengers.containsKey(exitingId)) {

            // Notify all passengers the ride is over
            Map<UUID, Double> passengers = activePassengers.get(exitingId);
            for (UUID passengerId : passengers.keySet()) {
                Player passenger = Bukkit.getPlayer(passengerId);
                if (passenger != null && passenger.isOnline()) {
                    passenger.sendMessage(ChatColor.YELLOW + "The taxi driver has ended the ride.");
                }
            }

            // Clear passengers
            activePassengers.remove(exitingId);
            return;
        }

        // If player is a passenger
        for (Map.Entry<UUID, Map<UUID, Double>> entry : activePassengers.entrySet()) {
            UUID driverId = entry.getKey();
            Map<UUID, Double> passengers = entry.getValue();

            if (passengers.containsKey(exitingId)) {
                // Calculate fare
                long startTime = tripStartTimes.getOrDefault(exitingId, System.currentTimeMillis());
                long duration = System.currentTimeMillis() - startTime;

                // Convert to minutes
                double minutes = duration / 60000.0;
                double rate = fareRates.getOrDefault(driverId, 10.0);
                double fare = rate * minutes;

                // Round to 2 decimal places
                fare = Math.round(fare * 100.0) / 100.0;

                // Update the fare
                passengers.put(exitingId, fare);

                // Notify driver
                Player driver = Bukkit.getPlayer(driverId);
                if (driver != null && driver.isOnline()) {
                    driver.sendMessage(ChatColor.YELLOW + exitingPlayer.getName() + " has left your taxi. " +
                            "Use /taxi charge " + exitingPlayer.getName() + " to charge $" + fare);
                }

                // Notify passenger
                exitingPlayer.sendMessage(ChatColor.YELLOW + "You've left the taxi. Your fare is $" + fare);
                break;
            }
        }
    }

    /**
     * Process taxi commands
     */
    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage().toLowerCase();
        if (!message.startsWith("/taxi ")) return;

        event.setCancelled(true);
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Check if player is a taxi driver
        if (!plugin.getJobManager().hasJob(playerId, "taxi")) {
            player.sendMessage(ChatColor.RED + "You must be a taxi driver to use this command!");
            return;
        }

        String[] args = message.substring(6).trim().split(" ");
        if (args.length == 0) {
            sendTaxiHelp(player);
            return;
        }

        switch (args[0]) {
            case "rate":
                handleRateCommand(player, args);
                break;
            case "charge":
                handleChargeCommand(player, args);
                break;
            default:
                sendTaxiHelp(player);
                break;
        }
    }

    /**
     * Handle the /taxi rate command
     */
    private void handleRateCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /taxi rate <amount>");
            return;
        }

        try {
            double rate = Double.parseDouble(args[1]);
            if (rate <= 0) {
                player.sendMessage(ChatColor.RED + "Rate must be greater than zero!");
                return;
            }

            fareRates.put(player.getUniqueId(), rate);
            player.sendMessage(ChatColor.GREEN + "Taxi fare rate set to $" + rate + " per minute.");
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid rate amount!");
        }
    }

    /**
     * Handle the /taxi charge command
     */
    private void handleChargeCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /taxi charge <player>");
            return;
        }

        String passengerName = args[1];
        Player passenger = Bukkit.getPlayer(passengerName);

        if (passenger == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }

        UUID passengerId = passenger.getUniqueId();

        // Check if this was a passenger
        Map<UUID, Double> passengers = activePassengers.get(player.getUniqueId());
        if (passengers == null || !passengers.containsKey(passengerId)) {
            player.sendMessage(ChatColor.RED + "This player hasn't been in your taxi recently!");
            return;
        }

        double fare = passengers.get(passengerId);

        // Check if player can afford it
        if (!plugin.getEconomy().has(passenger, fare)) {
            player.sendMessage(ChatColor.RED + passenger.getName() + " cannot afford the fare of $" + fare);
            passenger.sendMessage(ChatColor.RED + "You cannot afford the taxi fare of $" + fare);
            return;
        }

        // Process payment
        plugin.getEconomy().withdrawPlayer(passenger, fare);
        plugin.getEconomy().depositPlayer(player, fare);

        // Notify both parties
        player.sendMessage(ChatColor.GREEN + "You charged " + passenger.getName() + " $" + fare + " for the taxi ride.");
        passenger.sendMessage(ChatColor.GREEN + "You paid " + player.getName() + " $" + fare + " for the taxi ride.");

        // Remove passenger from active list
        passengers.remove(passengerId);
    }

    /**
     * Send taxi help to a player
     */
    private void sendTaxiHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Taxi Driver Commands ===");
        player.sendMessage(ChatColor.YELLOW + "/taxi rate <amount> - Set your fare rate per minute");
        player.sendMessage(ChatColor.YELLOW + "/taxi charge <player> - Charge a passenger for their ride");
    }
}