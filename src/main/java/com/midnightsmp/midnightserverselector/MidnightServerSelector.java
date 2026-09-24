package com.midnightsmp.midnightserverselector;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;

public class MidnightServerSelector extends JavaPlugin implements Listener, CommandExecutor {

    private final String MAIN_MENU_TITLE = ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Midnight SMP Navigation";
    private final String LIFESTEAL_MENU_TITLE = ChatColor.RED + "" + ChatColor.BOLD + "Select Lifesteal Mode";

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        getCommand("selector").setExecutor(this);
        getCommand("menu").setExecutor(this);

        getLogger().info("MidnightServerSelector enabled successfully!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player player) {
            openMainMenu(player);
            return true;
        }
        return false;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item != null && item.getType() == Material.COMPASS && item.hasItemMeta()) {
            if (item.getItemMeta().getDisplayName().contains("Server Selector")) {
                openMainMenu(event.getPlayer());
                event.setCancelled(true);
            }
        }
    }

    // --- MAIN MENU ---
    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, MAIN_MENU_TITLE);

        inv.setItem(10, createMenuItem(Material.REDSTONE_BLOCK, ChatColor.RED + "" + ChatColor.BOLD + "Lifesteal", "Click to choose Lifesteal modes"));
        inv.setItem(12, createMenuItem(Material.GRASS_BLOCK, ChatColor.GREEN + "" + ChatColor.BOLD + "Survival", "Click to view Survival modes"));
        inv.setItem(14, createMenuItem(Material.DIAMOND_SWORD, ChatColor.AQUA + "" + ChatColor.BOLD + "Practice", "Click to join Practice arena"));
        inv.setItem(15, createMenuItem(Material.TNT, ChatColor.YELLOW + "" + ChatColor.BOLD + "MiniGames", "Click to view MiniGames"));
        inv.setItem(16, createMenuItem(Material.NETHER_STAR, ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Events", "Click to join ongoing Events"));

        player.openInventory(inv);
    }

    // --- LIFESTEAL SUB-MENU ---
    public void openLifestealMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, LIFESTEAL_MENU_TITLE);

        inv.setItem(11, createMenuItem(Material.HEART_OF_THE_SEA, ChatColor.RED + "Simple Lifesteal", "Pure Lifesteal (No Economy) via Lobby"));
        inv.setItem(15, createMenuItem(Material.NETHERITE_SWORD, ChatColor.DARK_RED + "Special SMP Lifesteal", "Custom abilities & community features"));

        player.openInventory(inv);
    }

    private ItemStack createMenuItem(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Collections.singletonList(ChatColor.GRAY + lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    // --- INVENTORY CLICK HANDLING ---
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.equals(MAIN_MENU_TITLE) && !title.equals(LIFESTEAL_MENU_TITLE)) return;

        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        Player player = (Player) event.getWhoClicked();
        Material clickedType = event.getCurrentItem().getType();

        if (title.equals(MAIN_MENU_TITLE)) {
            if (clickedType == Material.REDSTONE_BLOCK) {
                openLifestealMenu(player);
            } else if (clickedType == Material.GRASS_BLOCK) {
                sendPlayerToServer(player, getConfig().getString("servers.survival-lobby", "survival-lobby"));
            } else if (clickedType == Material.DIAMOND_SWORD) {
                sendPlayerToServer(player, getConfig().getString("servers.practice", "practice"));
            } else if (clickedType == Material.TNT) {
                sendPlayerToServer(player, getConfig().getString("servers.minigames", "minigames"));
            } else if (clickedType == Material.NETHER_STAR) {
                sendPlayerToServer(player, getConfig().getString("servers.event", "event"));
            }
        } else if (title.equals(LIFESTEAL_MENU_TITLE)) {
            if (clickedType == Material.HEART_OF_THE_SEA) {
                sendPlayerToServer(player, getConfig().getString("servers.lifesteal-lobby", "lifesteal-lobby"));
            } else if (clickedType == Material.NETHERITE_SWORD) {
                sendPlayerToServer(player, getConfig().getString("servers.special-lifesteal", "special-lifesteal"));
            }
        }
    }

    // --- VELOCITY / BUNGEECORD REDIRECTION ---
    private void sendPlayerToServer(Player player, String serverName) {
        player.sendMessage(ChatColor.GREEN + "Connecting to " + serverName + "...");
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(serverName);
        player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
    }
}
