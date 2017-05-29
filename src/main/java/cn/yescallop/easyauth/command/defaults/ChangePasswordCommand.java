package cn.yescallop.easyauth.command.defaults;

import cn.nukkit.command.CommandSender;
import cn.yescallop.easyauth.command.CommandBase;

public class ChangePasswordCommand extends CommandBase {

    public ChangePasswordCommand() {
        super("changepassword");
        this.setAliases(new String[]{"changepasswd", "changepwd", "changepw", "cpw"});
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!this.testPermission(sender)) {
            return false;
        }
        if (args.length == 0 || args.length > 2) {
            this.sendUsage(sender);
            return false;
        }
        String name;
        String password;
        if (args.length == 1) {
            if (!this.testIngame(sender)) {
                return false;
            }
            name = sender.getName();
            password = args[0];
        } else {
            if (!this.testPermission(sender, "easyauth.changepassword.others")) {
                this.sendPermissionMessage(sender);
                return false;
            }
            name = args[0];
            password = args[1];
        }
        if (api.setPlayerPassword(name, password)) {
            sender.sendMessage(lang.translateString("commands.changepassword.success.own", password));
            if (!name.equalsIgnoreCase(sender.getName())) {
                sender.sendMessage(lang.translateString("commands.changepassword.success", name, password));
            }
        } else {
            sender.sendMessage(lang.translateString("commands.changepassword.same"));
            return false;
        }
        return true;
    }
}
