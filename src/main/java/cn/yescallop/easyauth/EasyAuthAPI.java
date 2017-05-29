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
import java.util.*;

public class EasyAuthAPI {

    private static EasyAuthAPI instance;
    private final EasyAuth plugin;
    private final BaseLang lang;
    private final File playersFolder;
    private final Set<Player> authedPlayers = new HashSet<>();
    private final Map<String, Config> playerConfigs = new HashMap<>();
    private final Config config;
    private byte[] salt = new byte[0];

    protected EasyAuthAPI(EasyAuth plugin) throws IOException {
        instance = this;
        this.plugin = plugin;
        this.lang = new BaseLang(this.getServer().getLanguage().getLang());
        this.config = plugin.getConfig();
        initSalt();
        playersFolder = new File(plugin.getDataFolder(), "players");
        playersFolder.mkdirs();
    }

    public static EasyAuthAPI getInstance() {
        return instance;
    }

    public Server getServer() {
        return plugin.getServer();
    }

    public BaseLang getLanguage() {
        return lang;
    }

    public boolean isPermissionConsoleOnly(String s) {
        return (boolean) config.getSection("permissions-console-only").getOrDefault(s, false);
    }

    private void initSalt() throws IOException {
        File saltFile = new File(plugin.getDataFolder(), "salt.txt");
        String salt;
        if (!saltFile.exists()) {
            saltFile.createNewFile();
            Utils.writeFile(saltFile, salt = PasswordUtil.randomSaltString());
        } else {
            salt = Utils.readFile(saltFile);
        }
        this.salt = Binary.hexStringToBytes(salt);
    }

    public void authenticateOnlinePlayers() {
        for (Player player : this.getServer().getOnlinePlayers().values()) {
            if (isPlayerUsingLastClientId(player)) {
                authenticatePlayer(player);
            }
        }
    }

    public boolean authenticatePlayer(Player player) {
        updatePlayerLastClientId(player);
        return authedPlayers.add(player);
    }

    public boolean deauthenticatePlayer(Player player) {
        return authedPlayers.remove(player);
    }

    public boolean isPlayerAuthenticated(Player player) {
        return authedPlayers.contains(player);
    }

    private byte[] getPlayerPassword(Player player) {
        return getPlayerPassword(player.getName());
    }

    private byte[] getPlayerPassword(String name) {
        if (!isPlayerRegistered(name)) return null;
        Config config = getPlayerConfig(name);
        String password = config.getString("password");
        return Binary.hexStringToBytes(password);
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

    public boolean setPlayerPassword(Player player, String password) {
        return setPlayerPassword(player.getName(), password);
    }

    public boolean setPlayerPassword(String name, String password) {
        Config config = getPlayerConfig(name);
        String digestStr = PasswordUtil.digestPasswordToString(password, salt);
        if (digestStr.equals(config.get("password"))) {
            return false;
        }
        config.set("password", digestStr);
        config.save();
        return true;
    }

    public boolean registerPlayer(Player player, String password) {
        return registerPlayer(player.getName(), password);
    }

    public boolean registerPlayer(String name, String password) {
        if (isPlayerRegistered(name)) return false;
        setPlayerPassword(name, password);
        return true;
    }

    public boolean unregisterPlayer(Player player) {
        return unregisterPlayer(player.getName());
    }

    public boolean unregisterPlayer(String name) {
        if (!isPlayerRegistered(name)) return false;
        getPlayerConfigFile(name).delete();
        return true;
    }

    public boolean isPlayerRegistered(Player player) {
        return isPlayerRegistered(player.getName());
    }

    public boolean isPlayerRegistered(String name) {
        return getPlayerConfigFile(name).exists() && getPlayerConfig(name).exists("password");
    }

    public boolean updatePlayerLastClientId(Player player) {
        return setPlayerLastClientId(player, player.getClientId());
    }

    public boolean setPlayerLastClientId(Player player, Long clientId) {
        return setPlayerLastClientId(player.getName(), clientId);
    }

    public boolean setPlayerLastClientId(String name, Long clientId) {
        Config config = getPlayerConfig(name);
        if (config.get("lastClientId").equals(clientId)) {
            return false;
        }
        config.set("lastClientId", clientId);
        config.save();
        return true;
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
        return playerConfigs.compute(name, (n, c) -> {
            if (c == null) {
                return new Config(getPlayerConfigFile(n), Config.YAML);
            }
            return c;
        });
    }
}
