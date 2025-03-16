package fr.rammex.azuriadailyreward.events;

import fr.rammex.azuriadailyreward.AzuriaDailyReward;
import fr.rammex.azuriadailyreward.messages.MessageManager;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GUIListener implements Listener {

    private AzuriaDailyReward plugin;
    private Random random = new Random();
    private MessageManager messageManager = new MessageManager();

    public GUIListener(AzuriaDailyReward plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String guiTitle = messageManager.getMessages().getString("gui.title", "§6Daily Rewards");
        if (!event.getView().getTitle().equals(guiTitle))
            return;

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR)
            return;

        int rawSlot = event.getRawSlot();
        Material type = clicked.getType();

        if (type == Material.BARRIER) {
            player.closeInventory();
            return;
        }

        if (rawSlot == 11 && type == Material.RED_WOOL) {
            String uuid = player.getUniqueId().toString();
            long currentTime = System.currentTimeMillis();
            try {
                Connection conn = plugin.getDatabaseManager().getConnection();
                PreparedStatement ps = conn.prepareStatement("SELECT cooldown FROM cooldowns WHERE uuid = ?");
                ps.setString(1, uuid);
                ResultSet rs = ps.executeQuery();
                long lastCooldown = 0;
                if (rs.next()) {
                    lastCooldown = rs.getLong("cooldown");
                }
                rs.close();
                ps.close();

                if (currentTime < lastCooldown) {
                    long remainingMillis = lastCooldown - currentTime;
                    long remainingSeconds = remainingMillis / 1000;
                    long hours = remainingSeconds / 3600;
                    long minutes = (remainingSeconds % 3600) / 60;
                    long seconds = remainingSeconds % 60;

                    String waitMsg = messageManager.getMessages().getString("message.waitTimeFormatted",
                                    "Vous devez attendre {hours}h {minutes}m {seconds}s avant de récupérer votre récompense.")
                            .replace("{hours}", String.valueOf(hours))
                            .replace("{minutes}", String.valueOf(minutes))
                            .replace("{seconds}", String.valueOf(seconds));
                    player.sendMessage(waitMsg);
                }
            } catch (SQLException e) {
                player.sendMessage(messageManager.getMessages().getString("message.dbError",
                        "Erreur lors de la connexion à la base de données."));
                e.printStackTrace();
            }
            player.closeInventory();
            return;
        }

        if (rawSlot == 11 && type == Material.GREEN_WOOL) {
            rewardPlayer(player);
            player.closeInventory();
            return;
        }
    }

    private void rewardPlayer(Player player) {
        FileConfiguration config = plugin.getConfig();
        long delayMillis = config.getInt("reward-delay") * 1000L;
        String uuid = player.getUniqueId().toString();
        long currentTime = System.currentTimeMillis();

        try {
            Connection conn = plugin.getDatabaseManager().getConnection();

            PreparedStatement ps = conn.prepareStatement("SELECT cooldown FROM cooldowns WHERE uuid = ?");
            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();
            long lastCooldown = 0;
            if (rs.next()) {
                lastCooldown = rs.getLong("cooldown");
            }
            rs.close();
            ps.close();

            if (currentTime < lastCooldown) {
                long remainingSeconds = (lastCooldown - currentTime) / 1000;
                String waitMsg = messageManager.getMessages().getString("message.waitTime",
                                "Veuillez attendre {remainingSeconds} secondes avant de récupérer votre récompense.")
                        .replace("{remainingSeconds}", String.valueOf(remainingSeconds));
                player.sendMessage(waitMsg);
                return;
            }

            List<Map<?, ?>> rewardsList = config.getMapList("rewards");
            if (rewardsList == null || rewardsList.isEmpty()) {
                player.sendMessage(messageManager.getMessages().getString("message.noRewardConfigured",
                        "Aucune récompense n'est configurée."));
                return;
            }

            int randomValue = random.nextInt(100) + 1;
            int cumulative = 0;
            Material rewardMaterial = null;
            String rewardCommand = null;
            int amount = 1;
            for (Map<?, ?> reward : rewardsList) {
                int chance = (int) reward.get("chance");
                cumulative += chance;
                if (randomValue <= cumulative) {
                    if (reward.containsKey("item")) {
                        String itemName = (String) reward.get("item");
                        try {
                            rewardMaterial = Material.valueOf(itemName);
                        } catch (IllegalArgumentException e) {
                            String errorMsg = messageManager.getMessages().getString("message.rewardNotExist",
                                            "L'item {itemName} n'existe pas.")
                                    .replace("{itemName}", itemName);
                            player.sendMessage(errorMsg);
                        }
                    }
                    if (reward.containsKey("commands")) {
                        rewardCommand = (String) reward.get("commands");
                    }
                    if (reward.containsKey("amount")) {
                        try {
                            amount = Integer.parseInt(reward.get("amount").toString());
                        } catch (NumberFormatException e) {
                            amount = 1;
                        }
                    }
                    break;
                }
            }

            boolean awardedItem = false;
            boolean executedCommand = false;
            if (rewardMaterial != null) {
                player.getInventory().addItem(new ItemStack(rewardMaterial, amount));
                awardedItem = true;
            }
            if (rewardCommand != null) {
                rewardCommand = rewardCommand.replace("{player}", player.getName());
                player.getServer().dispatchCommand(player.getServer().getConsoleSender(), rewardCommand);
                executedCommand = true;
            }

            if (awardedItem && executedCommand) {
                String combinedMsg = messageManager.getMessages().getString("message.receivedRewardCombined",
                        "§b[DailyRewads] §aVous avez reçu : {rewardMaterial} (x{amount}) et une récompense supplémentaire !");
                combinedMsg = combinedMsg.replace("{rewardMaterial}", rewardMaterial.name())
                        .replace("{amount}", String.valueOf(amount));
                player.sendMessage(combinedMsg);
            } else if (awardedItem) {
                String receivedMsg = messageManager.getMessages().getString("message.receivedReward",
                                "Vous avez reçu : {rewardMaterial} (x{amount})")
                        .replace("{rewardMaterial}", rewardMaterial.name())
                        .replace("{amount}", String.valueOf(amount));
                player.sendMessage(receivedMsg);
            } else if (executedCommand) {
                String receivedMsg = messageManager.getMessages().getString("message.receivedRewardCommands",
                        "§b[DailyRewads] §aVous avez reçu une récompense !");
                player.sendMessage(receivedMsg);
            } else {
                player.sendMessage(messageManager.getMessages().getString("message.rewardError",
                        "Une erreur est survenue lors de la récupération de la récompense."));
                return;
            }

            long nextAvailable = currentTime + delayMillis;
            PreparedStatement update = conn.prepareStatement("REPLACE INTO cooldowns (uuid, cooldown) VALUES (?, ?)");
            update.setString(1, uuid);
            update.setLong(2, nextAvailable);
            update.executeUpdate();
            update.close();
        } catch (SQLException e) {
            player.sendMessage(messageManager.getMessages().getString("message.dbError",
                    "Erreur lors de la connexion à la base de données."));
            e.printStackTrace();
        }
    }
}
