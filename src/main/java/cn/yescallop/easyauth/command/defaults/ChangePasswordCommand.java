package cn.yescallop.easyauth.command.defaults;

import cn.nukkit.command.CommandSender;
import cn.yescallop.easyauth.command.CommandBase;

public class ChangePasswordCommand extends CommandBase {

    public ChangePasswordCommand() {
        super("changepassword");
        this.setAliases(new String[]{"changepasswd", "changepwd", "changepw"});
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        return false;
    }
}
