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

    public BaseLang getLanguage() {
        return lang;
    }

    private void initSalt() {
        File saltFile = new File(plugin.getDataFolder(), "salt.txt");
        try {
            String salt;
            if (!saltFile.exists()) {
                saltFile.createNewFile();
                Utils.writeFile(saltFile, salt = PasswordUtil.randomSaltString());
            } else {
                salt = Utils.readFile(saltFile);
            }
            this.salt = Binary.hexStringToBytes(salt);
        } catch (IOException e) {
            this.salt = new byte[0];
        }
    }

    public void authenticateOnlinePlayers() {
        for (Player player : Server.getInstance().getOnlinePlayers().values()) {
            if (isPlayerUsingLastClientId(player)) {
                authenticatePlayer(player);
            }
        }
    }

    public void authenticatePlayer(String name) {
        Player player = Server.getInstance().getPlayerExact(name);
        if (player != null) authenticatePlayer(player);
    }

    public void authenticatePlayer(Player player) {
        authedPlayers.add(player);
        updatePlayerLastClientId(player);
    }

    public void deauthenticatePlayer(String name) {
        deauthenticatePlayer(Server.getInstance().getPlayerExact(name));
    }

    public void deauthenticatePlayer(Player player) {
        authedPlayers.remove(player);
    }

    public boolean isPlayerAuthenticated(String name) {
        Player player = Server.getInstance().getPlayerExact(name);
        return player == null ? null : isPlayerAuthenticated(player);
    }

    public boolean isPlayerAuthenticated(Player player) {
        return authedPlayers.contains(player);
    }

    private byte[] getPlayerPassword(Player player) {
        return getPlayerPassword(player.getName());
    }

    private byte[] getPlayerPassword(String name) {
        if (isPlayerRegistered(name)) {
            Config config = getPlayerConfig(name);
            String password = config.getString("password");
            return Binary.hexStringToBytes(password);
        } else {
            return null;
        }
    }

    public boolean checkPlayerPassword(Player player, String password) {
        return checkPlayerPassword(player.getName(), password);
    }

    public boolean checkPlayerPassword(String name, String password) {
        byte[] realPassword = getPlayerPassword(name);
        if (realPassword == null) return false;
        byte[] digest = PasswordUtil.digestPassword(password, salt);
        return Arrays.equals(realPassword, digest);
    }

    public void setPlayerPassword(Player player, String password) {
        setPlayerPassword(player.getName(), password);
    }

    public void setPlayerPassword(String name, String password) {
        Config config = getPlayerConfig(name);
        config.set("password", PasswordUtil.digestPasswordToString(password, salt));
        config.save();
    }

    public boolean registerPlayer(Player player, String password) {
        return registerPlayer(player.getName(), password);
    }

    public boolean registerPlayer(String name, String password) {
        if (isPlayerRegistered(name)) return false;
        setPlayerPassword(name, password);
        authenticatePlayer(name);
        return true;
    }

    public boolean unregisterPlayer(Player player) {
        return unregisterPlayer(player.getName());
    }

    public boolean unregisterPlayer(String name) {
        if (!isPlayerRegistered(name)) return false;
        getPlayerConfigFile(name).delete();
        deauthenticatePlayer(name);
        return true;
    }

    public boolean isPlayerRegistered(Player player) {
        return isPlayerRegistered(player.getName());
    }

    public boolean isPlayerRegistered(String name) {
        return getPlayerConfigFile(name).exists() && getPlayerConfig(name).exists("password");
    }

    public void updatePlayerLastClientId(Player player) {
        setPlayerLastClientId(player, player.getClientId());
    }

    public void setPlayerLastClientId(Player player, Long clientId) {
        setPlayerLastClientId(player.getName(), clientId);
    }

    public void setPlayerLastClientId(String name, Long clientId) {
        Config config = getPlayerConfig(name);
        config.set("lastClientId", clientId);
        config.save();
    }

    public Long getPlayerLastClientId(Player player) {
        return getPlayerLastClientId(player.getName());
    }

    public Long getPlayerLastClientId(String name) {
        if (playerConfigExists(name)) {
            Config config = getPlayerConfig(name);
            return config.getLong("lastClientId");
        } else {
            return null;
        }
    }

    public boolean isPlayerUsingLastClientId(Player player) {
        if (!playerConfigExists(player)) return false;
        Long clientId = getPlayerLastClientId(player);
        return clientId != null && player.getClientId().equals(clientId);
    }

    private File getPlayerConfigFile(Player player) {
        return getPlayerConfigFile(player.getName());
    }

    private File getPlayerConfigFile(String name) {
        return new File(playersFolder, name.toLowerCase() + ".yml");
    }

    private boolean playerConfigExists(Player player) {
        return playerConfigExists(player.getName());
    }

    private boolean playerConfigExists(String name) {
        return getPlayerConfigFile(name).exists();
    }

    private Config getPlayerConfig(Player player) {
        return getPlayerConfig(player.getName());
    }

    private Config getPlayerConfig(String name) {
        return new Config(getPlayerConfigFile(name.toLowerCase()), Config.YAML);
    }
}
