package cn.yescallop.easyauth;

import cn.nukkit.plugin.PluginBase;

import java.io.IOException;

public class EasyAuth extends PluginBase {

    private EasyAuthAPI api;

    @Override
    public void onEnable() {
        this.getDataFolder().mkdirs();
        try {
            this.api = new EasyAuthAPI(this);
        } catch (IOException e) {
            this.getLogger().error("Error in salt initialization", e);
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.getServer().getPluginManager().registerEvents(new EventListener(), this);
        api.authenticateOnlinePlayers();
        this.getLogger().info(api.getLanguage().translateString("easyauth.loaded"));
    }
}