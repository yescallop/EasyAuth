package cn.yescallop.easyauth;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.utils.Binary;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.Utils;
import cn.yescallop.easyauth.lang.BaseLang;
import cn.yescallop.easyauth.util.PasswordUtil;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class EasyAuthAPI {

    private static EasyAuthAPI instance;
    private final EasyAuth plugin;
    private final BaseLang lang;
    private final File playersFolder;
    private final Set<Player> authedPlayers = new HashSet<>();
    private byte[] salt;

    protected EasyAuthAPI(EasyAuth plugin) {
        instance = this;
        this.plugin = plugin;
        this.lang = new BaseLang(Server.getInstance().getLanguage().getLang());
        initSalt();
        playersFolder = new File(plugin.getDataFolder(), "players");
        playersFolder.mkdirs();
    }

    public static EasyAuthAPI getInstance() {
        return instance;
    }

    protected BaseLang getLanguage() {
        return lang;
    }

    private void initSalt() {
        File saltFile = new File(plugin.getDataFolder(), "salt.txt");
        try {
            if (!saltFile.exists()) {
                saltFile.createNewFile();
                Utils.writeFile(saltFile, PasswordUtil.randomSaltString());
            }
            String salt = Utils.readFile(saltFile);
            this.salt = Binary.hexStringToBytes(salt);
        } catch (IOException e) {
            this.salt = new byte[0];
        }
    }

    public void authenticatePlayers() {
        for (Player player : Server.getInstance().getOnlinePlayers().values()) {
            if (isPlayerUsingLastClientId(player)) {
                authenticatePlayer(player);
            }
        }
    }

    public void authenticatePlayer(Player player) {
        authedPlayers.add(player);
        updatePlayerLastClientId(player);
    }

    public void deauthenticatePlayer(Player player) {
        authedPlayers.remove(player);
    }

    public boolean isPlayerAuthenticated(Player player) {
        return authedPlayers.contains(player);
    }

    public boolean checkPlayerPassword(Player player, String password) {
        Config config = getPlayerConfig(player);
        String realPasswordStr = config.getString("password");
        if (realPasswordStr == null) return false;
        byte[] realPassword = Binary.hexStringToBytes(realPasswordStr);
        byte[] digest = PasswordUtil.digestPassword(password, salt);
        return Arrays.equals(realPassword, digest);
    }

    public void setPlayerPassword(Player player, String password) {
        Config config = getPlayerConfig(player);
        config.set("password", PasswordUtil.digestPasswordToString(password, salt));
        config.save();
    }

    public void registerPlayer(Player player, String password) {
        setPlayerPassword(player, password);
        authenticatePlayer(player);
    }

    public void unregisterPlayer(Player player) {
        deauthenticatePlayer(player);
        getPlayerConfigFile(player).delete();
    }

    public boolean isPlayerRegistered(Player player) {
        return getPlayerConfigFile(player).exists();
    }

    public void updatePlayerLastClientId(Player player) {
        setPlayerLastClientId(player, player.getClientId());
    }

    public void setPlayerLastClientId(Player player, Long clientId) {
        Config config = getPlayerConfig(player);
        config.set("lastClientId", clientId);
        config.save();
    }

    public Long getPlayerLastClientId(Player player) {
        if (isPlayerRegistered(player)) {
            Config config = getPlayerConfig(player);
            return config.getLong("lastClientId");
        } else {
            return null;
        }
    }

    public boolean isPlayerUsingLastClientId(Player player) {
        Long clientId = getPlayerLastClientId(player);
        return clientId != null && player.getClientId().equals(clientId);
    }

    protected File getPlayerConfigFile(Player player) {
        return new File(playersFolder, player.getName().toLowerCase() + ".yml");
    }

    protected Config getPlayerConfig(Player player) {
        return new Config(getPlayerConfigFile(player), Config.YAML);
    }
}
