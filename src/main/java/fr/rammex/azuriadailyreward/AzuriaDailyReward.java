package fr.rammex.azuriadailyreward;

import fr.rammex.azuriadailyreward.commands.DailyRewardCommand;
import fr.rammex.azuriadailyreward.database.SQliteManager;
import fr.rammex.azuriadailyreward.events.GUIListener;
import fr.rammex.azuriadailyreward.messages.MessageManager;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class AzuriaDailyReward extends JavaPlugin {

    public static AzuriaDailyReward instance;

    @Override
    public void onEnable() {
        instance = this;

        // SQLiteManager
        SQliteManager sqLiteManager = new SQliteManager("azuriadailyreward", new File(getDataFolder(), "data.db"));
        sqLiteManager.load();

        // CONFIG.YML
        saveDefaultConfig();

        // MESSAGES.YML
        MessageManager messageManager = new MessageManager();
        messageManager.saveDefaultMessages();

        loadCommands();
        loadEvents();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void loadCommands() {
        getCommand("dailyrewards").setExecutor(new DailyRewardCommand(this));
    }

    private void loadEvents() {
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
    }
}
