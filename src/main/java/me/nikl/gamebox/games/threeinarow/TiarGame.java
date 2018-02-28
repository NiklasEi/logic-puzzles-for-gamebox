package me.nikl.gamebox.games.threeinarow;

import me.nikl.gamebox.utility.ItemStackUtility;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nikl on 25.02.18.
 */
public class TiarGame {
    private Inventory inventory;
    private TiarLanguage language;
    private ThreeInARow game;
    private Map<Integer, ItemStack> helpItems = new HashMap<>();
    private Integer[] grid = new Integer[36];
    private List<Integer> tipSlots = new ArrayList<>();
    private ItemStack backGround = ItemStackUtility.getItemStack("160:7");
    
    public TiarGame(ThreeInARow threeInARow, TiarRules rules, Player player, String game){
        language = (TiarLanguage) threeInARow.getGameLang();
        this.game = threeInARow;
        this.inventory = threeInARow.createInventory(54 + 9, language.GAME_TITLE);
        prepareInventory(game);
        player.openInventory(inventory);
    }

    private void prepareInventory(String game) {
        placeBackGround();
        String tips = game.split(";")[0];
        int gridSlot = -1;
        for (char character : tips.toCharArray()) {
            gridSlot ++;
            if (character == '0'){
                grid[gridSlot] = 0;
                continue;
            }
            if (character == '1') {
                grid[gridSlot] = 1;
                inventory.setItem(gridToInventory(gridSlot), this.game.getWhiteTileTip());
                tipSlots.add(gridToInventory(gridSlot));
                continue;
            }
            if (character == '2') {
                grid[gridSlot] = 2;
                inventory.setItem(gridToInventory(gridSlot), this.game.getBlueTileTip());
                tipSlots.add(gridToInventory(gridSlot));
            }
        }
        placeHelpItems();
    }

    private void placeBackGround() {
        for (int i = 0; i < 7; i++) {
            inventory.setItem(i*9, backGround);
            inventory.setItem(i*9 + 8, backGround);
        }
        inventory.setItem(7, backGround);
    }

    private void placeHelpItems() {
        for (int i = 0; i < 6; i++) {
            helpItems.put(gridToInventory(i) - 9, game.getWrongHelpItem());
            helpItems.put(gridToInventory(i*6 + 5) + 1, game.getWrongHelpItem());
            updateHelpItem(i*7);
        }
    }

    private void updateHelpItem(int gridSlot) {
        int row = gridSlot / 6;
        int column = gridSlot % 6;
        updateRowHelpItem(row);
        updateColumnHelpItem(column);
    }

    private void updateColumnHelpItem(int column) {
        int blue = 0;
        int white = 0;
        for (int row = 0; row < 6; row++) {
            if (grid[row*6 + column] == 1) white++;
            if (grid[row*6 + column] == 2) blue++;
        }
        if (blue == 3 && white == 3) {
            helpItems.put(column + 1, game.getCorrectHelpItem());
            inventory.setItem(column + 1, game.getCorrectHelpItem());
            return;
        }
        helpItems.put(column + 1, createHelpItem(white, blue));
        inventory.setItem(column + 1, helpItems.get(column + 1));
    }

    private ItemStack createHelpItem(int white, int blue) {
        ItemStack helpItem = game.getWrongHelpItem();
        ItemMeta meta = helpItem.getItemMeta();
        List<String> lore = meta.getLore();
        for (int i = 0; i<lore.size(); i++) {
            lore.set(i, lore.get(i)
                    .replace("%blue_count%", String.valueOf(blue))
                    .replace("%white_count%", String.valueOf(white)));
        }
        meta.setLore(lore);
        helpItem.setItemMeta(meta);
        return helpItem;
    }

    private void updateRowHelpItem(int row) {
        int blue = 0;
        int white = 0;
        for (int column = 0; column < 6; column++) {
            if (grid[row*6 + column] == 1) white++;
            if (grid[row*6 + column] == 2) blue++;
        }
        if (blue == 3 && white == 3) {
            helpItems.put(gridToInventory(row*6 + 5) + 1, game.getCorrectHelpItem());
            inventory.setItem(gridToInventory(row*6 + 5) + 1, game.getCorrectHelpItem());
            return;
        }
        helpItems.put(gridToInventory(row*6 + 5) + 1, createHelpItem(white, blue));
        inventory.setItem(gridToInventory(row*6 + 5) + 1, helpItems.get(gridToInventory(row*6 + 5) + 1));
    }

    private int gridToInventory(int gridSlot) {
        int row = gridSlot / 6;
        int column = gridSlot % 6;
        return 9 + row * 9 + column + 1;
    }

    private int inventoryToGrid (int inventorySlot) {
        int row = inventorySlot / 9;
        int column = inventorySlot % 9;
        if (row == 0 || column == 0 || column > 6 || row > 6) return -1;
        return (row - 1)*6 + column - 1;
    }

    private void clickSlot(int inventorySlot) {
        int gridSlot = inventoryToGrid(inventorySlot);
        if (gridSlot < 0) return;
        if (tipSlots.contains(inventorySlot)) return;
        grid[gridSlot] = (grid[gridSlot] + 1) % 3;
        updateGridSlot(gridSlot);
    }

    private void updateGridSlot(int gridSlot) {
        switch (grid[gridSlot]) {
            case 0:
                inventory.setItem(gridToInventory(gridSlot), null);
                break;
            case 1:
                inventory.setItem(gridToInventory(gridSlot), game.getWhiteTile());
                break;
            case 2:
                inventory.setItem(gridToInventory(gridSlot), game.getBlueTile());
                break;
        }
        updateHelpItem(gridSlot);
    }

    public boolean onClick(InventoryClickEvent inventoryClickEvent) {
        if (inventoryClickEvent.getClick() == ClickType.DOUBLE_CLICK) return true;
        clickSlot(inventoryClickEvent.getSlot());
        return true;
    }

    public void onClose() {
    }
}
