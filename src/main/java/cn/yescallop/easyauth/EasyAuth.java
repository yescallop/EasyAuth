package cn.yescallop.easyauth;

import cn.nukkit.plugin.PluginBase;

public class EasyAuth extends PluginBase {

    private EasyAuthAPI api;

    @Override
    public void onEnable() {
        this.getDataFolder().mkdirs();
        this.api = new EasyAuthAPI(this);
        this.getServer().getPluginManager().registerEvents(new EventListener(), this);
        api.authenticatePlayers();
        this.getLogger().info(api.getLanguage().translateString("easyauth.loaded"));
    }
}