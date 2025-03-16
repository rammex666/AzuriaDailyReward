package fr.rammex.azuriadailyreward.messages;

import fr.rammex.azuriadailyreward.AzuriaDailyReward;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class MessageManager {
    private FileConfiguration messages;
    private File messagesFile;
    public void reloadMessages() {
        if (messagesFile == null) {
            messagesFile = new File(AzuriaDailyReward.instance.getDataFolder(), "messages.yml");
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public FileConfiguration getMessages() {
        if (messages == null) {
            reloadMessages();
        }
        return messages;
    }

    public void saveDefaultMessages() {
        if (messagesFile == null) {
            messagesFile = new File(AzuriaDailyReward.instance.getDataFolder(), "messages.yml");
        }
        if (!messagesFile.exists()) {
            AzuriaDailyReward.instance.saveResource("messages.yml", false);
        }
    }
}
