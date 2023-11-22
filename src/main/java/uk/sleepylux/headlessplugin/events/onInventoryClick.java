package uk.sleepylux.headlessplugin.events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.json.simple.JSONObject;
import uk.sleepylux.headlessplugin.HeadlessPlugin;
import uk.sleepylux.headlessplugin.utility.HeadManager;
import uk.sleepylux.headlessplugin.utility.MessageManager;
import static uk.sleepylux.headlessplugin.HeadlessPlugin.PlayerManager;

import java.util.concurrent.atomic.AtomicBoolean;

import static uk.sleepylux.headlessplugin.commands.revive.inv;

public class onInventoryClick implements Listener {
    HeadlessPlugin plugin;
    public onInventoryClick(HeadlessPlugin plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void OnInventoryClick(InventoryClickEvent e) {
        if (!e.getInventory().equals(inv)) return;
        e.setCancelled(true);

        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir()) return;

        OfflinePlayer revivee = ((SkullMeta) clickedItem.getItemMeta()).getOwningPlayer();
        Player player = (Player) e.getWhoClicked();

        if (revivee == null) {
            MessageManager.sendMessage(player, Component.text("Could not find user you are trying to revive"));
            return;
        }

        JSONObject reviveeJson = PlayerManager.parse(PlayerManager.get(revivee.getUniqueId().toString()));
        ItemStack skull = HeadManager.Create(plugin, revivee);
        skull.setAmount(1);

        AtomicBoolean hasHeads = new AtomicBoolean(false);
        player.getInventory().forEach(item -> {
            if (item != null && item.isSimilar(clickedItem) && item.getAmount() >= 1) {
                hasHeads.set(true);
                player.getInventory().removeItem(item);
            }
        });
        if (!hasHeads.get()) {
            MessageManager.sendMessage(player, Component.text("You must have 4 of " + revivee.getName() + "'s heads in 1 slot to revive them"));
            return;
        }

        reviveeJson.put("dead", false);
        reviveeJson.put("lives", 1);

        inv.close();
        PlayerManager.set(revivee.getUniqueId().toString(), reviveeJson);
        MessageManager.broadcastMessage(plugin.getServer(), Component.text(revivee.getName() + " has been revived!")
                .color(TextColor.fromCSSHexString("#FF55FF")));
        plugin.getServer().getBanList(BanList.Type.NAME).pardon(revivee.getName());
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1F, 1F);
        }
    }
}
