package me.cookiedeath;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import java.io.File;

import java.sql.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

public class Main extends JavaPlugin
{

    public ArrayList<String> cookieLore = new ArrayList<String>();
    public JSONObject upgradesJSON;
    public JSONArray upgradesArray;

    @Override
    public void onEnable()
    {
        Bukkit.getLogger().info("cookie clicker is ready");
        Bukkit.getPluginManager().registerEvents(new CookieMenuListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this );
        getCommand(getConfig().getString("commandPrefix")).setExecutor(new CookieMenuCommand(this));

        cookieLore.add("Click me to get");
        cookieLore.add("more cookies!");

        File dataLocation = new File(getConfig().getString("dataLocation"));
        if (!dataLocation.isDirectory())
        {
            dataLocation.mkdir();
        }

        if(!Files.exists(Paths.get(getConfig().getString("dataLocation") + getConfig().getString("dbName"))))
        {
            createNewDatabase();
        }

        try
        {
            if (!Files.exists(Paths.get(getConfig().getString("dataLocation") + getConfig().getString("upgradesJSON"))))
            {
                upgradesJSON = new JSONObject();
                upgradesArray = new JSONArray();

                CookieUpgrade exampleUpgrade1 = new CookieUpgrade("Yeet", 31, 0, 12, 10, "10 cookies.", 10, 10, this);
                CookieUpgrade exampleUpgrade2 = new CookieUpgrade("Yoot", 69, 0, 13, 10, "10 cookies.", 10, 10, this);
                CookieUpgrade exampleUpgrade3 = new CookieUpgrade("Yootunheim", 420, 0, 14, 10, "10 cookies.", 10, 10, this);
                exampleUpgrade1.addUpgradeToJSONArray(upgradesArray);
                exampleUpgrade2.addUpgradeToJSONArray(upgradesArray);
                exampleUpgrade3.addUpgradeToJSONArray(upgradesArray);

                upgradesJSON.put("Upgrades", upgradesArray);

                Files.write(Paths.get(getConfig().getString("dataLocation") + getConfig().getString("upgradesJSON")), upgradesJSON.toJSONString().getBytes());
            }
            else
            {
                FileReader tempReader = new FileReader(getConfig().getString("dataLocation") + getConfig().getString("upgradesJSON"));
                JSONParser tempParser = new JSONParser();
                upgradesJSON = (JSONObject) tempParser.parse(tempReader);
                upgradesArray = (JSONArray) upgradesJSON.get("Upgrades");
            }
        }
        catch (IOException | ParseException ex) { System.out.println(ex.getMessage()); }

    }

    @Override
    public void onDisable()
    {
        Bukkit.getLogger().info("cokkie clicker is dead");
    }

    public ItemStack createGUIItem(String itemName, ArrayList<String> description, Material material, int amount, int metadata)
    {
        ItemStack item = new ItemStack(material, amount, (byte) metadata);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(itemName);
        itemMeta.setLore(description);
        item.setItemMeta(itemMeta);
        return item;
    }

    private void createNewDatabase() {

        String url = "jdbc:sqlite:" + getConfig().getString("dataLocation") + getConfig().getString("dbName");

        String sql = "CREATE TABLE IF NOT EXISTS players (\n"
                + "	uuid TEXT PRIMARY KEY UNIQUE,\n"
                + "	username TEXT NOT NULL,\n"
                + "	cookies REAL,\n"
                + "	upgrades TEXT\n"
                + ");";

        Connection conn = null;
        Statement stmt = null;
        try {
            conn = connectToDatabase();
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                stmt = conn.createStatement();
                stmt.execute(sql);
                sql = "PRAGMA JOURNAL_MODE=WAL";
                stmt.execute(sql);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        finally {
            try { stmt.close(); } catch(Exception ex) {}
            try { conn.close(); } catch(Exception ex) {}
        }
    }

    public void insertNewPlayerData(String uuid, String username, String upgrades) {

        String sql = "INSERT INTO players(uuid, username, cookies, upgrades) VALUES(?,?,?,?)";

        Connection conn = null;
        PreparedStatement pstmt = null;
        try
        {
            conn = connectToDatabase();
            if (conn != null)
            {
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, uuid);
                pstmt.setString(2, username);
                pstmt.setLong(3, 0);
                pstmt.setString(4, upgrades);
                pstmt.executeUpdate();
                conn.close();
            }
        }
        catch (SQLException ex)
        {
            System.out.println(ex.getMessage());
        }
        finally {
            try { pstmt.close(); } catch(Exception ex) {}
            try { conn.close(); } catch(Exception ex) {}
        }
    }

    public void updatePlayerCookies(String uuid, Float cookies)
    {

        String sql = "UPDATE players SET cookies = ? WHERE uuid = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        try
        {
            conn = connectToDatabase();
            if (conn != null)
            {
                pstmt = conn.prepareStatement(sql);
                pstmt.setFloat(1, cookies);
                pstmt.setString(2, uuid);
                pstmt.executeUpdate();
            }
        }
        catch (SQLException ex)
        {
            System.out.println(ex.getMessage());
        }
        finally {
            try { pstmt.close(); } catch(Exception ex) {}
            try { conn.close(); } catch(Exception ex) {}
        }
    }
    public void updatePlayerName(String uuid, String username)
    {

        String sql = "UPDATE players SET username = ? WHERE uuid = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        try
        {
            conn = connectToDatabase();
            if (conn != null)
            {
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, username);
                pstmt.setString(2, uuid);
                pstmt.executeUpdate();
            }
        }
        catch (SQLException ex)
        {
            System.out.println(ex.getMessage());
        }
        finally {
            try { pstmt.close(); } catch(Exception ex) {}
            try { conn.close(); } catch(Exception ex) {}
        }
    }
    public void updatePlayerUpgrade(String uuid, String upgradeName)
    {
        String sql = "SELECT upgrades FROM players WHERE uuid = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        try
        {
            conn = connectToDatabase();
            if (conn != null)
            {
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, uuid);
                JSONObject newUpgradeJSON = (JSONObject) new JSONParser().parse(pstmt.executeQuery().getString("upgrades"));
                newUpgradeJSON.replace(upgradeName, Long.valueOf(String.valueOf(newUpgradeJSON.get(upgradeName))) + 1L);

                sql = "UPDATE players SET upgrades = ? WHERE uuid = ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, newUpgradeJSON.toString());
                pstmt.setString(2, uuid);
                pstmt.executeUpdate();
            }
        }
        catch (SQLException | ParseException ex)
        {
            System.out.println(ex.getMessage());
        }
        finally {
            try { pstmt.close(); } catch(Exception ex) {}
            try { conn.close(); } catch(Exception ex) {}
        }
    }



    public Float getPlayerCookies(String uuid)
    {
        String sql = "SELECT cookies FROM players WHERE uuid = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        Float cookies = 0F;
        try
        {
            conn = connectToDatabase();
            if (conn != null) {
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, uuid);
                cookies = pstmt.executeQuery().getFloat("cookies");
            }
        }
        catch (SQLException ex)
        {
            System.out.println(ex.getMessage());
        }
        finally {
            try { pstmt.close(); } catch(Exception ex) {}
            try { conn.close(); } catch(Exception ex) {}
            return cookies;
        }
    }

    public Connection connectToDatabase()
    {
        Connection conn = null;
        try
        {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + getConfig().getString("dataLocation") + getConfig().getString("dbName"));
        }
        catch (SQLException | ClassNotFoundException ex)
        {
            System.out.println(ex.getMessage());
        }
        return conn;
    }

}
