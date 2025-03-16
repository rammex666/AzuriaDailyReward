package fr.rammex.azuriadailyreward.database;

import fr.rammex.azuriadailyreward.AzuriaDailyReward;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

import static fr.rammex.azuriadailyreward.database.SQliteManager.getSQLConnection;

public class CooldownManager {

    public static void addCooldown(UUID playerUUID, int cooldown) {
        String query = "INSERT INTO cooldowns (player_uuid, cooldown) VALUES (?, ?)";
        try (Connection connection = getSQLConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, playerUUID.toString());
            ps.setInt(2, cooldown);
            ps.executeUpdate();
        } catch (SQLException ex) {
            AzuriaDailyReward.instance.getLogger().log(Level.SEVERE, "Unable to add cooldown to player", ex);
        }
    }

    public static void updateCooldown(UUID playerUUID, int cooldown) {
        String query = "UPDATE cooldowns SET cooldown = ? WHERE player_uuid = ?";
        try (Connection connection = getSQLConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, cooldown);
            ps.setString(2, playerUUID.toString());
            ps.executeUpdate();
        } catch (SQLException ex) {
            AzuriaDailyReward.instance.getLogger().log(Level.SEVERE, "Unable to update cooldown for player", ex);
        }
    }

    public static int getCooldown(UUID playerUUID) {
        String query = "SELECT cooldown FROM cooldowns WHERE player_uuid = ?";
        try (Connection connection = getSQLConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, playerUUID.toString());
            return ps.executeQuery().getInt("cooldown");
        } catch (SQLException ex) {
            AzuriaDailyReward.instance.getLogger().log(Level.SEVERE, "Unable to get cooldown for player", ex);
        }
        return 0;
    }

    public static boolean hasCooldown(UUID playerUUID) {
        String query = "SELECT cooldown FROM cooldowns WHERE player_uuid = ?";
        try (Connection connection = getSQLConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, playerUUID.toString());
            return ps.executeQuery().next();
        } catch (SQLException ex) {
            AzuriaDailyReward.instance.getLogger().log(Level.SEVERE, "Unable to check if player has cooldown", ex);
        }
        return false;
    }

    public static void removeCooldown(UUID playerUUID) {
        String query = "DELETE FROM cooldowns WHERE player_uuid = ?";
        try (Connection connection = getSQLConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, playerUUID.toString());
            ps.executeUpdate();
        } catch (SQLException ex) {
            AzuriaDailyReward.instance.getLogger().log(Level.SEVERE, "Unable to remove 1s player time", ex);
        }
    }
}
