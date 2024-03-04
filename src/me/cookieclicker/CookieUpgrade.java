package me.cookiedeath;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;

public class CookieUpgrade
{

    public String upgradeName;
    public int upgradeMaterialID;
    public int upgradeMaterialMetadata;
    public int upgradePosition;
    public long upgradePrice;
    public String priceDescription;
    public float upgradeCPS;
    public float upgradeCPC;
    private Main main;

    public CookieUpgrade(String upgradeName, int upgradeMaterialID, int upgradeMaterialMetadata, int upgradePosition, long upgradePrice, String priceDescription, float upgradeCPS, float upgradeCPC, Main main)
    {
        this.upgradeName = upgradeName;
        this.upgradeMaterialID = upgradeMaterialID;
        this.upgradeMaterialMetadata = upgradeMaterialMetadata;
        this.upgradePosition = upgradePosition;
        this.upgradePrice = upgradePrice;
        this.priceDescription = priceDescription;
        this.upgradeCPS = upgradeCPS;
        this.upgradeCPC = upgradeCPC;
        this.main = main;
    }

    @SuppressWarnings("deprecation")
    public CookieUpgrade(String upgradeName, String upgradeMaterialID, int upgradeMaterialMetadata, int upgradePosition, long upgradePrice, String priceDescription, float upgradeCPS, float upgradeCPC, Main main)
    {
        this.upgradeName = upgradeName;
        this.upgradeMaterialID = Material.getMaterial(upgradeMaterialID).getId();
        this.upgradeMaterialMetadata = upgradeMaterialMetadata;
        this.upgradePosition = upgradePosition;
        this.upgradePrice = upgradePrice;
        this.priceDescription = priceDescription;
        this.upgradeCPS = upgradeCPS;
        this.upgradeCPC = upgradeCPC;
        this.main = main;
    }

    public CookieUpgrade(JSONObject jsonObject, Main main)
    {
        this.upgradeName = jsonObject.get("upgradeName").toString();
        this.upgradeMaterialID = Integer.valueOf(jsonObject.get("upgradeMaterialID").toString());
        this.upgradeMaterialMetadata = Integer.valueOf(jsonObject.get("upgradeMaterialMetadata").toString());
        this.upgradePrice = Long.valueOf(jsonObject.get("upgradePrice").toString());
        this.priceDescription = jsonObject.get("priceDescription").toString();
        this.upgradePosition = Integer.valueOf(jsonObject.get("upgradePosition").toString());
        this.upgradeCPS = Float.valueOf(jsonObject.get("upgradeCPS").toString());
        this.upgradeCPC = Float.valueOf(jsonObject.get("upgradeCPC").toString());
        this.main = main;
    }

    public void addUpgradeToJSONArray(JSONArray jsonArray)
    {
        JSONObject upgrade = new JSONObject();
        upgrade.put("upgradeName", upgradeName);
        upgrade.put("upgradeMaterialID", upgradeMaterialID);
        upgrade.put("upgradeMaterialMetadata", upgradeMaterialMetadata);
        upgrade.put("upgradePosition", upgradePosition);
        upgrade.put("upgradePrice", upgradePrice);
        upgrade.put("priceDescription", priceDescription);
        upgrade.put("upgradeCPS", upgradeCPS);
        upgrade.put("upgradeCPC", upgradeCPC);

        jsonArray.add(upgrade);
    }

    @SuppressWarnings("deprecation")
    public ItemStack prepareUpgradeItem()
    {
        ArrayList<String> upgradeDescription = new ArrayList<>();
        upgradeDescription.add(ChatColor.GRAY + "This upgrade increases your:");
        upgradeDescription.add(ChatColor.BLUE + "CPS(Cookies per second) by: " + upgradeCPS);
        upgradeDescription.add(ChatColor.BLUE + "CPC(Cookies per click) by: " + upgradeCPC);
        upgradeDescription.add(ChatColor.YELLOW + "Click to buy this upgrade for " + priceDescription);
        ItemStack upgradeItemStack = main.createGUIItem(upgradeName, upgradeDescription, Material.getMaterial(upgradeMaterialID), 1, upgradeMaterialMetadata);
        return upgradeItemStack;
    }

}
