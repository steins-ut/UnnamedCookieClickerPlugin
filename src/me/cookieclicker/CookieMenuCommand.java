package me.cookiedeath;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.json.simple.JSONObject;

public class CookieMenuCommand implements CommandExecutor
{

    private Main main;

    public CookieMenuCommand(Main main)
    {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender cmdSender, Command cmd, String label, String[] args) {

        if(cmdSender instanceof Player)
        {
            Player cmdPlayer = (Player) cmdSender;
            if (args.length == 0 || args[0].equals("click")) { cookieInvCreator(cmdPlayer); return true; }
            else if (args[0].equals("shop")) { cookieShopCreator(cmdPlayer); return true; }
        }
        else
        {
            Bukkit.getLogger().info("Console can't use this command!");
        }
        return false;
    }

    public void cookieInvCreator(Player player)
    {
        Inventory inv = Bukkit.createInventory(null, 45, ChatColor.YELLOW + "Cookie clicker");
        for(int i = 0; i < inv.getSize(); i++)
        {
            inv.setItem(i, main.createGUIItem(null, null, Material.STAINED_GLASS_PANE, 1, 0));
        }
        inv.setItem(22, main.createGUIItem("Cookies: " + main.getPlayerCookies(player.getUniqueId().toString()), main.cookieLore, Material.COOKIE, 1, 0));
        inv.setItem(41, main.createGUIItem("Close menu.", null, Material.BARRIER, 1, 0));
        inv.setItem(39, main.createGUIItem("Go to the shop", null, Material.SIGN, 1, 0));

        player.openInventory(inv);
    }

    public void cookieShopCreator(Player player)
    {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.RED + "Cookie shop");
        for(int i = 0; i < inv.getSize(); i++)
        {
            inv.setItem(i, main.createGUIItem(null, null, Material.STAINED_GLASS_PANE, 1, 0));
        }
        inv.setItem(41, main.createGUIItem("Close menu.", null, Material.BARRIER, 1, 0));
        inv.setItem(39, main.createGUIItem("Back to clicking.", null, Material.COOKIE, 1, 0));

        try
        {
            for (int i = 0; i < main.upgradesArray.size(); i++)
            {
                JSONObject tempJSONObject = (JSONObject) main.upgradesArray.get(i);

                CookieUpgrade tempUpgrade = new CookieUpgrade(tempJSONObject, this.main);

                inv.setItem(tempUpgrade.upgradePosition, tempUpgrade.prepareUpgradeItem());
            }

            player.openInventory(inv);
        }
        catch (NullPointerException ex) { ex.printStackTrace(); }
    }

}
