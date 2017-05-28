package cn.yescallop.easyauth.command;

import cn.nukkit.Server;
import cn.nukkit.command.CommandMap;
import cn.yescallop.easyauth.command.defaults.ChangePasswordCommand;

public class CommandManager {

    public static void registerAll() {
        CommandMap map = Server.getInstance().getCommandMap();

        map.register("EasyAuth", new ChangePasswordCommand());
    }
}
