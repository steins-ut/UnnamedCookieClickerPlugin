package me.cookiedeath;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.sql.*;
import java.util.Set;

public class PlayerJoinListener implements Listener
{
    private Main main;

    public PlayerJoinListener(Main main) { this.main = main; }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event)
    {
        createPlayerEntryInDatabase(event.getPlayer());
        if (main.getConfig().getBoolean("internalLeaderboard"))
        {
            createLeaderboard(event.getPlayer());
        }
    }

    private void createPlayerEntryInDatabase(Player player)
    {
        String sql = "SELECT * FROM players WHERE uuid=?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet result = null;
        try
        {
            conn = main.connectToDatabase();
            if (conn != null)
            {
                JSONObject allUpgrades = new JSONObject();
                for (int i = 0; i < main.upgradesArray.size(); i++)
                {
                    allUpgrades.put(((JSONObject) main.upgradesArray.get(i)).get("upgradeName"), 0);
                }
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, player.getUniqueId().toString());
                result = pstmt.executeQuery();
                if (!result.next())
                {
                    main.insertNewPlayerData(player.getUniqueId().toString(), player.getPlayer().getPlayerListName(), allUpgrades.toString());
                    conn.close();
                    return;
                }
                main.updatePlayerName(player.getUniqueId().toString(), player.getPlayerListName());
                if (result.next() && !result.isClosed() && ((JSONObject) new JSONParser().parse(result.getString("upgrades"))).size() < allUpgrades.size())
                {
                    String tempJsonString = result.getString("upgrades");
                    JSONObject currentAvailableUpgrades = (JSONObject) new JSONParser().parse(tempJsonString);
                    String[] upgradeNames = (String[]) allUpgrades.keySet().toArray();
                    for(int i = 0; i < allUpgrades.size(); i++)
                    {
                        if (!currentAvailableUpgrades.containsKey(upgradeNames[i]))
                        {
                            currentAvailableUpgrades.put(upgradeNames[i], 0);
                        }
                    }
                    sql = "UPDATE players SET upgrades = ? WHERE uuid = ?";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, currentAvailableUpgrades.toString());
                    pstmt.setString(2, player.getUniqueId().toString());
                    pstmt.executeUpdate();
                    result.close();
                    conn.close();
                    return;
                }
                conn.close();
            }
        }
        catch (SQLException | ParseException ex)
        {
            System.out.println(ex.getMessage());
        }
        finally {
            try { result.close(); } catch (Exception ex) {}
            try { pstmt.close(); } catch(Exception ex) {}
            try { conn.close(); } catch(Exception ex) {}
        }
    }

    private void createLeaderboard(Player player) {

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
