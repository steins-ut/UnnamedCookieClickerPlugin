package me.cookiedeath;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.*;
import org.json.simple.JSONObject;

import java.sql.*;
import java.util.HashMap;

public class CookieMenuListener implements Listener
{
    private Main main;
    private HashMap<Player, Long> cookieMap = new HashMap<>();

    public CookieMenuListener(Main main) { this.main = main; }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event)
    {
        cookieMap.put((Player) event.getPlayer(), 0L);
    }

    @EventHandler
    public void onItemClick(InventoryClickEvent event)
    {
        Player player = (Player) event.getWhoClicked();

        if(ChatColor.translateAlternateColorCodes('&', event.getClickedInventory().getTitle()).equals(ChatColor.YELLOW + "Cookie clicker"))
        {
            if (event.getCurrentItem() != null)
            {
                event.setCancelled(true);
                switch (event.getCurrentItem().getType())
                {
                    case COOKIE:

                        cookieMap.replace(player, cookieMap.get(player) + 1);
                        changeItemName(event.getCurrentItem(),"Cookies: " + (cookieMap.get(player) + main.getPlayerCookies(player.getUniqueId().toString())));
                        return;

                    case BARRIER:
                        player.closeInventory();
                        return;

                    case SIGN:
                        player.performCommand("cookie shop");
                        return;

                    default:
                        return;

                }
            }
        }
        else if(ChatColor.translateAlternateColorCodes('&', event.getClickedInventory().getTitle()).equals(ChatColor.RED + "Cookie shop"))
        {
            if (event.getCurrentItem() != null)
            {
                event.setCancelled(true);
                switch (event.getCurrentItem().getType())
                {
                    case COOKIE:
                        player.closeInventory();
                        player.performCommand("cookie");
                        return;

                    case BARRIER:
                        player.closeInventory();
                        return;

                    default:

                        CookieUpgrade tempUpgrade;

                        for(int i = 0; i < main.upgradesArray.size(); i++)
                        {
                            tempUpgrade = new CookieUpgrade((JSONObject) main.upgradesArray.get(i), main);
                            if (tempUpgrade.upgradeName == event.getCurrentItem().getItemMeta().getDisplayName())
                            {
                                if(main.getPlayerCookies(player.getUniqueId().toString()) >= tempUpgrade.upgradePrice)
                                {
                                    main.updatePlayerUpgrade(player.getUniqueId().toString(), tempUpgrade.upgradeName);
                                    main.updatePlayerCookies(player.getUniqueId().toString(), main.getPlayerCookies(player.getUniqueId().toString()) - tempUpgrade.upgradePrice);
                                    return;
                                }
                                player.sendMessage(ChatColor.RED + "You do not have enough cookies to buy this upgrade.");
                                return;
                            }
                        }
                        return;

                }
            }
        }
        return;
    }

    @EventHandler
    private void onCookieMenuClose(InventoryCloseEvent event)
    {
        if(ChatColor.translateAlternateColorCodes('&', event.getInventory().getTitle()).equals(ChatColor.YELLOW + "Cookie clicker"))
        {
            main.updatePlayerCookies(event.getPlayer().getUniqueId().toString(), cookieMap.get(event.getPlayer()) + main.getPlayerCookies(event.getPlayer().getUniqueId().toString()));
            cookieMap.remove(event.getPlayer());
            if (main.getConfig().getBoolean("internalLeaderboard"))
            {
                updateLeaderboard((Player) event.getPlayer());
            }
        }
    }

    private void changeItemName(ItemStack item, String itemName)
    {
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(itemName);
        item.setItemMeta(itemMeta);
    }

    private void updateLeaderboard(Player player)
    {
        Connection conn = null;
        Statement stmt = null;
        ResultSet top5Set = null;
        try
        {
            conn = main.connectToDatabase();
            if (conn != null)
            {

                String sql = "SELECT username, cookies FROM players ORDER BY cookies DESC LIMIT 5";

                stmt = conn.createStatement();
                top5Set = stmt.executeQuery(sql);

                ScoreboardManager boardManager = Bukkit.getScoreboardManager();

                Scoreboard leaderboard = boardManager.getNewScoreboard();

                Objective objective = leaderboard.registerNewObjective("COOKIE TOP", "dummy");
                objective.setDisplayName("TOP COOKIE MAKERS");
                objective.setDisplaySlot(DisplaySlot.SIDEBAR);

                Score[] topScoreArray = new Score[5];

                int i = 0;

                while(top5Set.next())
                {
                    topScoreArray[i] = objective.getScore( ChatColor.GOLD + String.valueOf(i + 1) + "." + top5Set.getString("username"));
                    topScoreArray[i].setScore(top5Set.getInt("cookies"));
                    i++;
                }

                player.setScoreboard(leaderboard);

            }
        }
        catch (SQLException ex) { System.out.println(ex.getMessage()); }
        finally {
            try { top5Set.close(); } catch (Exception ex) {}
            try { stmt.close(); } catch(Exception ex) {}
            try { conn.close(); } catch(Exception ex) {}
        }

    }

}
