package me.ash.realitySMP.commands;

import me.ash.realitySMP.RealitySMP;
import me.ash.realitySMP.gui.JobGUI;
import me.ash.realitySMP.jobs.Job;
import me.ash.realitySMP.jobs.JobManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JobCommand implements CommandExecutor, TabCompleter {
    private final RealitySMP plugin;
    private final JobGUI jobGUI;

    public JobCommand(RealitySMP plugin) {
        this.plugin = plugin;
        this.jobGUI = new JobGUI(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        JobManager jobManager = plugin.getJobManager();

        if (args.length == 0) {
            // Open job selection GUI
            jobGUI.openJobSelectionGUI(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "join":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /job join <job>");
                    return true;
                }

                String jobName = args[1];
                boolean success = jobManager.joinJob(player, jobName);
                if (!success) {
                    // Error message already sent by jobManager
                }
                return true;

            case "leave":
                jobManager.leaveJob(player);
                return true;

            case "stats":
            case "info":
                Job job = jobManager.getPlayerJob(player.getUniqueId());
                if (job == null) {
                    player.sendMessage("§cYou don't have a job! Use /job to select one.");
                    return true;
                }

                jobGUI.openJobStatsGUI(player);
                return true;

            case "list":
                player.sendMessage("§e------------- §6Available Jobs §e-------------");
                for (Job availableJob : jobManager.getAvailableJobs().values()) {
                    player.sendMessage("§6• §e" + availableJob.getName() + " §7- Base salary: §a$" + availableJob.getBaseSalary());
                }
                return true;

            case "heal":
                job = jobManager.getPlayerJob(player.getUniqueId());
                if (job == null || !job.getName().equalsIgnoreCase("Doctor")) {
                    player.sendMessage("§cThis command is only available to Doctors!");
                    return true;
                }

                // Trigger the heal action
                jobManager.handleJobAction(player, "heal");
                return true;

            case "arrest":
                job = jobManager.getPlayerJob(player.getUniqueId());
                if (job == null || !job.getName().equalsIgnoreCase("Police Officer")) {
                    player.sendMessage("§cThis command is only available to Police Officers!");
                    return true;
                }

                if (args.length < 2) {
                    player.sendMessage("§cUsage: /job arrest <player>");
                    return true;
                }

                // Get the target player
                String targetName = args[1];
                Player target = plugin.getServer().getPlayer(targetName);

                if (target == null) {
                    player.sendMessage("§cPlayer " + targetName + " is not online!");
                    return true;
                }

                // Implement arrest mechanics here
                // For example: teleport the player to jail, set a timer, etc.
                target.sendMessage("§cYou have been arrested by Officer " + player.getName() + "!");
                player.sendMessage("§9You have arrested " + target.getName() + "!");

                // Trigger the arrest action for XP
                jobManager.handleJobAction(player, "arrest");
                return true;

            case "repair":
                job = jobManager.getPlayerJob(player.getUniqueId());
                if (job == null || !job.getName().equalsIgnoreCase("Mechanic")) {
                    player.sendMessage("§cThis command is only available to Mechanics!");
                    return true;
                }

                // Implement repair mechanics
                // For now, just trigger the action
                jobManager.handleJobAction(player, "repair");
                return true;

            case "help":
                sendHelpMessage(player);
                return true;

            default:
                player.sendMessage("§cUnknown sub-command. Use /job help for a list of commands.");
                return true;
        }
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage("§e------------- §6Job Commands §e-------------");
        player.sendMessage("§6/job §7- Open the job selection GUI");
        player.sendMessage("§6/job join <job> §7- Join a specific job");
        player.sendMessage("§6/job leave §7- Leave your current job");
        player.sendMessage("§6/job stats §7- View your job statistics");
        player.sendMessage("§6/job list §7- List all available jobs");
        player.sendMessage("§6/job help §7- Show this help message");

        // Job-specific commands
        player.sendMessage("");
        player.sendMessage("§e------------- §6Job-Specific Commands §e-------------");
        player.sendMessage("§6/job heal §7- (Doctor) Heal nearby players");
        player.sendMessage("§6/job arrest <player> §7- (Police) Arrest a player");
        player.sendMessage("§6/job repair §7- (Mechanic) Repair items");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // First argument - subcommands
            List<String> subCommands = List.of("join", "leave", "stats", "info", "list", "heal", "arrest", "repair", "help");

            return filterCompletions(subCommands, args[0]);
        } else if (args.length == 2) {
            // Second argument - depends on the first argument
            if (args[0].equalsIgnoreCase("join")) {
                // If joining a job, suggest available jobs
                return filterCompletions(
                        plugin.getJobManager().getAvailableJobs().values().stream()
                                .map(Job::getName)
                                .collect(Collectors.toList()),
                        args[1]
                );
            } else if (args[0].equalsIgnoreCase("arrest")) {
                // If arresting, suggest online players
                return filterCompletions(
                        plugin.getServer().getOnlinePlayers().stream()
                                .map(Player::getName)
                                .collect(Collectors.toList()),
                        args[1]
                );
            }
        }

        return completions;
    }

    private List<String> filterCompletions(List<String> options, String input) {
        if (input.isEmpty()) {
            return options;
        }

        String lowerInput = input.toLowerCase();
        return options.stream()
                .filter(option -> option.toLowerCase().startsWith(lowerInput))
                .collect(Collectors.toList());
    }
}