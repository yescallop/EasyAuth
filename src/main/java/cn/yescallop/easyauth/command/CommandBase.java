package cn.yescallop.easyauth.command;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.lang.TranslationContainer;
import cn.nukkit.utils.TextFormat;
import cn.yescallop.easyauth.EasyAuthAPI;
import cn.yescallop.easyauth.lang.BaseLang;

public abstract class CommandBase extends Command {

    protected EasyAuthAPI api;
    protected BaseLang lang;

    public CommandBase(String name) {
        super(name);
        this.api = EasyAuthAPI.getInstance();
        this.lang = api.getLanguage();
        this.description = lang.translateString("commands." + name + ".description");
        String usageMessage = lang.translateString("commands." + name + ".usage");
        this.usageMessage = usageMessage.equals("commands." + name + ".usage") ? "/" + name : usageMessage;
        this.setPermission("easyauth." + name);
    }

    protected void sendUsage(CommandSender sender) {
        sender.sendMessage(new TranslationContainer("commands.generic.usage", this.usageMessage));
    }

    protected boolean testIngame(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(TextFormat.RED + lang.translateString("commands.generic.ingame"));
            return false;
        }
        return true;
    }

    protected void sendPermissionMessage(CommandSender sender) {
        sender.sendMessage(new TranslationContainer(TextFormat.RED + "%commands.generic.permission"));
    }

    public boolean testPermission(CommandSender target, String s) {
        return api.isPermissionConsoleOnly(s) ? !target.isPlayer() : target.hasPermission(s);
    }
}
