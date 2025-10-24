package com.guardianuncraft;

import com.guardianuncraft.config.ConfigManager;
import com.guardianuncraft.gui.GuiListener;
import com.guardianuncraft.gui.GuiManager;
import com.guardianuncraft.listeners.RightClickListener;
import com.guardianuncraft.commands.UncraftCommand;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;

public final class GuardianUncraft extends JavaPlugin {

    private static GuardianUncraft instance;
    private MiniMessage miniMessage;
    private ConfigManager configManager;
    private GuiManager guiManager;
    private String prefix;

    @Override
    public void onEnable() {
        instance = this;
        miniMessage = MiniMessage.miniMessage();

        // Configs
        configManager = new ConfigManager(this);
        configManager.reload();

        // GUI manager
        guiManager = new GuiManager(this);

        // Command
        getCommand("uncraft").setExecutor(new UncraftCommand(this));

        // Listeners
        getServer().getPluginManager().registerEvents(new RightClickListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);

        getLogger().info("✅ GuardianUncraft enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("GuardianUncraft disabled.");
    }

    public static GuardianUncraft getInstance() {
        return instance;
    }

    public MiniMessage miniMessage() {
        return miniMessage;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }

    public String getPrefix() {
        return configManager.getConfig().getString("prefix");
    }
}
