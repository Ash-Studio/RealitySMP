package me.ash.realitySMP.utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtils {
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("\\{#([A-Fa-f0-9]{6})}(.*?)\\{#([A-Fa-f0-9]{6})}");
    private static final String PREFIX = "&8[&bReailtySMP&8] &r";
    private static final String ERROR_PREFIX = "&8[&cReailtySMP&8] &c";
    private static final String SUCCESS_PREFIX = "&8[&aReailtySMP&8] &a";
    private static final String INFO_PREFIX = "&8[&eReailtySMP&8] &e";

    /**
     * Colorize a message string with color codes
     *
     * @param message The message to colorize
     * @return The colorized message
     */
    public static String colorize(String message) {
        if (message == null) return "";

        // Replace hex colors (&#RRGGBB)
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.of("#" + hex).toString());
        }

        matcher.appendTail(buffer);
        String processed = buffer.toString();

        // Process standard color codes (&a, &b, etc.)
        return ChatColor.translateAlternateColorCodes('&', processed);
    }

    /**
     * Apply a gradient between two hex colors to a text
     *
     * @param message The message containing gradient syntax
     * @return The colorized message with gradient colors
     */
    public static String gradient(String message) {
        if (message == null) return "";

        // First process normal color codes
        String processed = colorize(message);

        // Process gradients {#RRGGBB}text{#RRGGBB}
        Matcher matcher = GRADIENT_PATTERN.matcher(processed);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String startHex = matcher.group(1);
            String text = matcher.group(2);
            String endHex = matcher.group(3);

            matcher.appendReplacement(buffer, applyGradient(text, startHex, endHex));
        }

        matcher.appendTail(buffer);
        return buffer.toString();
    }

    /**
     * Apply a gradient to text between two colors
     *
     * @param text The text to apply gradient to
     * @param startHex The starting hex color
     * @param endHex The ending hex color
     * @return The text with gradient colors applied
     */
    private static String applyGradient(String text, String startHex, String endHex) {
        // Parse hex colors
        java.awt.Color start = java.awt.Color.decode("#" + startHex);
        java.awt.Color end = java.awt.Color.decode("#" + endHex);

        // Calculate color steps
        int length = text.length();
        if (length == 0) return "";
        if (length == 1) return ChatColor.of("#" + startHex) + text;

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < length; i++) {
            float ratio = (float) i / (length - 1);

            // Interpolate color
            int red = (int) (start.getRed() * (1 - ratio) + end.getRed() * ratio);
            int green = (int) (start.getGreen() * (1 - ratio) + end.getGreen() * ratio);
            int blue = (int) (start.getBlue() * (1 - ratio) + end.getBlue() * ratio);

            // Format as hex
            String hex = String.format("#%02x%02x%02x", red, green, blue);
            result.append(ChatColor.of(hex)).append(text.charAt(i));
        }

        return result.toString();
    }

    /**
     * Send a regular message to a command sender
     *
     * @param sender The command sender
     * @param message The message to send
     */
    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(colorize(PREFIX + message));
    }

    /**
     * Send an error message to a command sender
     *
     * @param sender The command sender
     * @param message The error message to send
     */
    public static void sendErrorMessage(CommandSender sender, String message) {
        sender.sendMessage(colorize(ERROR_PREFIX + message));

        if (sender instanceof Player) {
            Player player = (Player) sender;
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
        }
    }

    /**
     * Send a success message to a command sender
     *
     * @param sender The command sender
     * @param message The success message to send
     */
    public static void sendSuccessMessage(CommandSender sender, String message) {
        sender.sendMessage(colorize(SUCCESS_PREFIX + message));

        if (sender instanceof Player) {
            Player player = (Player) sender;
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 2.0F);
        }
    }

    /**
     * Send an info message to a command sender
     *
     * @param sender The command sender
     * @param message The info message to send
     */
    public static void sendInfoMessage(CommandSender sender, String message) {
        sender.sendMessage(colorize(INFO_PREFIX + message));
    }

    /**
     * Broadcast a message to all players on the server
     *
     * @param message The message to broadcast
     */
    public static void broadcast(String message) {
        Bukkit.broadcastMessage(colorize(PREFIX + message));
    }

    /**
     * Broadcast a message with sound to all players on the server
     *
     * @param message The message to broadcast
     * @param sound The sound to play
     * @param volume The volume of the sound
     * @param pitch The pitch of the sound
     */
    public static void broadcastWithSound(String message, Sound sound, float volume, float pitch) {
        String coloredMessage = colorize(PREFIX + message);
        Bukkit.broadcastMessage(coloredMessage);

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    /**
     * Create an interactive text component
     *
     * @param text The text to display
     * @param hoverText The text to show on hover
     * @param command The command to execute on click (can be null)
     * @param url The URL to open on click (can be null)
     * @return The interactive text component
     */
    public static TextComponent createInteractiveText(String text, String hoverText, String command, String url) {
        TextComponent component = new TextComponent(colorize(text));

        if (hoverText != null && !hoverText.isEmpty()) {
            BaseComponent[] hoverComponents = new ComponentBuilder(colorize(hoverText)).create();
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverComponents));
        }

        if (command != null && !command.isEmpty()) {
            component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        } else if (url != null && !url.isEmpty()) {
            component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        }

        return component;
    }

    /**
     * Send a message with interactive components to a player
     *
     * @param player The player to send the message to
     * @param components The components to send
     */
    public static void sendInteractiveMessage(Player player, BaseComponent... components) {
        player.spigot().sendMessage(components);
    }

    /**
     * Center a text message based on chat width
     *
     * @param message The message to center
     * @return The centered message
     */
    public static String centerText(String message) {
        if (message == null || message.isEmpty()) return "";

        message = colorize(message);
        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for (char c : message.toCharArray()) {
            if (c == '§') {
                previousCode = true;
            } else if (previousCode) {
                previousCode = false;
                isBold = c == 'l' || c == 'L';
            } else {
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = 154 - halvedMessageSize; // 154 = chat window width (in px)
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;
        StringBuilder sb = new StringBuilder();

        while (compensated < toCompensate) {
            sb.append(" ");
            compensated += spaceLength;
        }

        return sb.toString() + message;
    }

    /**
     * Create a list of text lines with proper formatting
     *
     * @param header The header text (can be null)
     * @param lines The lines to add
     * @return A formatted list of text lines
     */
    public static List<String> createTextList(String header, List<String> lines) {
        List<String> result = new ArrayList<>();

        if (header != null && !header.isEmpty()) {
            result.add(colorize("&7&m----------------------"));
            result.add(colorize(header));
            result.add(colorize("&7&m----------------------"));
        }

        for (String line : lines) {
            result.add(colorize(line));
        }

        if (header != null && !header.isEmpty()) {
            result.add(colorize("&7&m----------------------"));
        }

        return result;
    }

    /**
     * Send a title to a player
     *
     * @param player The player to send the title to
     * @param title The title text
     * @param subtitle The subtitle text
     * @param fadeIn The fade in time in ticks
     * @param stay The stay time in ticks
     * @param fadeOut The fade out time in ticks
     */
    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        player.sendTitle(
                title != null ? colorize(title) : "",
                subtitle != null ? colorize(subtitle) : "",
                fadeIn,
                stay,
                fadeOut
        );
    }

    /**
     * Send an action bar message to a player
     *
     * @param player The player to send the action bar to
     * @param message The message to send
     */
    public static void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(colorize(message)));
    }

    /**
     * Send a message to a player with a customizable prefix
     *
     * @param player The player to send the message to
     * @param prefix The prefix to use
     * @param message The message to send
     */
    public static void sendPrefixedMessage(Player player, String prefix, String message) {
        player.sendMessage(colorize(prefix + " " + message));
    }

    /**
     * Font information for centered text
     */
    public enum DefaultFontInfo {
        A('A', 5),
        a('a', 5),
        B('B', 5),
        b('b', 5),
        C('C', 5),
        c('c', 5),
        D('D', 5),
        d('d', 5),
        E('E', 5),
        e('e', 5),
        F('F', 5),
        f('f', 4),
        G('G', 5),
        g('g', 5),
        H('H', 5),
        h('h', 5),
        I('I', 3),
        i('i', 1),
        J('J', 5),
        j('j', 5),
        K('K', 5),
        k('k', 4),
        L('L', 5),
        l('l', 1),
        M('M', 5),
        m('m', 5),
        N('N', 5),
        n('n', 5),
        O('O', 5),
        o('o', 5),
        P('P', 5),
        p('p', 5),
        Q('Q', 5),
        q('q', 5),
        R('R', 5),
        r('r', 5),
        S('S', 5),
        s('s', 5),
        T('T', 5),
        t('t', 4),
        U('U', 5),
        u('u', 5),
        V('V', 5),
        v('v', 5),
        W('W', 5),
        w('w', 5),
        X('X', 5),
        x('x', 5),
        Y('Y', 5),
        y('y', 5),
        Z('Z', 5),
        z('z', 5),
        NUM_1('1', 5),
        NUM_2('2', 5),
        NUM_3('3', 5),
        NUM_4('4', 5),
        NUM_5('5', 5),
        NUM_6('6', 5),
        NUM_7('7', 5),
        NUM_8('8', 5),
        NUM_9('9', 5),
        NUM_0('0', 5),
        EXCLAMATION_POINT('!', 1),
        AT_SYMBOL('@', 6),
        NUM_SIGN('#', 5),
        DOLLAR_SIGN('$', 5),
        PERCENT('%', 5),
        UP_ARROW('^', 5),
        AMPERSAND('&', 5),
        ASTERISK('*', 5),
        LEFT_PARENTHESIS('(', 4),
        RIGHT_PARENTHESIS(')', 4),
        MINUS('-', 5),
        UNDERSCORE('_', 5),
        PLUS_SIGN('+', 5),
        EQUALS_SIGN('=', 5),
        LEFT_CURL_BRACE('{', 4),
        RIGHT_CURL_BRACE('}', 4),
        LEFT_BRACKET('[', 3),
        RIGHT_BRACKET(']', 3),
        COLON(':', 1),
        SEMI_COLON(';', 1),
        DOUBLE_QUOTE('"', 3),
        SINGLE_QUOTE('\'', 1),
        LEFT_ARROW('<', 4),
        RIGHT_ARROW('>', 4),
        QUESTION_MARK('?', 5),
        SLASH('/', 5),
        BACK_SLASH('\\', 5),
        LINE('|', 1),
        TILDE('~', 5),
        TICK('`', 2),
        PERIOD('.', 1),
        COMMA(',', 1),
        SPACE(' ', 3),
        DEFAULT('a', 4);

        private final char character;
        private final int length;

        DefaultFontInfo(char character, int length) {
            this.character = character;
            this.length = length;
        }

        public char getCharacter() {
            return character;
        }

        public int getLength() {
            return length;
        }

        public int getBoldLength() {
            if (this == DefaultFontInfo.SPACE) return length;
            return length + 1;
        }

        public static DefaultFontInfo getDefaultFontInfo(char c) {
            for (DefaultFontInfo dFI : DefaultFontInfo.values()) {
                if (dFI.getCharacter() == c) return dFI;
            }
            return DefaultFontInfo.DEFAULT;
        }
    }

    /**
     * Format a placeholder in a message using provided values
     *
     * @param message The message containing placeholders
     * @param placeholders The placeholder keys
     * @param values The placeholder values
     * @return The formatted message
     */
    public static String formatPlaceholders(String message, String[] placeholders, String[] values) {
        if (message == null) return "";
        if (placeholders == null || values == null) return message;
        if (placeholders.length != values.length) return message;

        String formatted = message;
        for (int i = 0; i < placeholders.length; i++) {
            formatted = formatted.replace(placeholders[i], values[i]);
        }

        return colorize(formatted);
    }

    /**
     * Send a private message between players
     *
     * @param sender The sending player
     * @param receiver The receiving player
     * @param message The message to send
     */
    public static void sendPrivateMessage(Player sender, Player receiver, String message) {
        String formattedMessage = colorize("&7[&dPM&7] &d" + sender.getName() + " &7-> &d" + receiver.getName() + "&7: &f" + message);
        sender.sendMessage(formattedMessage);
        receiver.sendMessage(formattedMessage);
        receiver.playSound(receiver.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5F, 1.0F);
    }

    /**
     * Create a line separator
     *
     * @param color The color code for the separator
     * @param character The character to use
     * @param length The length of the separator
     * @return The formatted separator
     */
    public static String createSeparator(String color, char character, int length) {
        StringBuilder sb = new StringBuilder();
        sb.append(color);
        for (int i = 0; i < length; i++) {
            sb.append(character);
        }
        return colorize(sb.toString());
    }
}