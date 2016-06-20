package cn.yescallop.easyauth;

import cn.nukkit.Player;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.Utils;
import cn.yescallop.easyauth.lang.BaseLang;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class EasyAuth extends PluginBase {

    private BaseLang lang;
    private byte[] salt;
    private File playersFolder;
    private ArrayList<Player> authedPlayers = new ArrayList<>();

    @Override
    public void onEnable() {
        this.getDataFolder().mkdirs();
        File saltFile = new File(this.getDataFolder(), "salt.txt");
        try {
            if (!saltFile.exists()) {
                saltFile.createNewFile();
                Utils.writeFile(saltFile, getRandomSalt());
            }
            String salt = Utils.readFile(saltFile);
            this.salt = new BASE64Decoder().decodeBuffer(salt);
        } catch (IOException e) {
            this.salt = new byte[0];
        }
        playersFolder = new File(this.getDataFolder(), "players");
        playersFolder.mkdirs();
        lang = new BaseLang(this.getServer().getLanguage().getLang());
        this.getServer().getPluginManager().registerEvents(new EventListener(this), this);
        for (Player player : this.getServer().getOnlinePlayers().values()) {
            if (isPlayerLastUUID(player)) {
                authenticatePlayer(player);
            }
        }
        this.getLogger().info(lang.translateString("easyauth.loaded"));
    }
    
    private String getRandomSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return new BASE64Encoder().encode(salt);
    }
    
    public BaseLang getLanguage() {
        return lang;
    }
    
    public void authenticatePlayer(Player player) {
        authedPlayers.add(player);
        setPlayerLastUUID(player);
    }
    
    public void deauthenticatePlayer(Player player) {
        authedPlayers.remove(player);
    }
    
    public boolean isPlayerAuthenticated(Player player) {
        return authedPlayers.contains(player);
    }
    
    private byte[] getEncryptedPassword(String password) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.update(salt);
            digest.update(password.getBytes("UTF-8"));
        } catch (Exception e) {
            return new byte[0];
        }
        return digest.digest();
    }
    
    private String getEncryptedPasswordString(String password) {
        return new BASE64Encoder().encode(getEncryptedPassword(password));
    }
    
    public boolean checkPlayerPassword(Player player, String password) {
        Config config = getPlayerConfig(player);
        String realPasswordStr = config.getString("password");
        if (realPasswordStr == null) return false;
        byte[] realPassword;
        try {
            realPassword = new BASE64Decoder().decodeBuffer(realPasswordStr);
        } catch (IOException e) {
            return false;
        }
        byte [] encryptedPassword = getEncryptedPassword(password);
        return Arrays.equals(realPassword, encryptedPassword);
    }
    
    public void setPlayerPassword(Player player, String password) {
        Config config = getPlayerConfig(player);
        String encryptedPassword = getEncryptedPasswordString(password);
        config.set("password", encryptedPassword);
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
    
    public void setPlayerLastUUID(Player player) {
        Config config = getPlayerConfig(player);
        config.set("lastUUID", player.getUniqueId().toString());
        config.save();
    }
    
    public UUID getPlayerLastUUID(Player player) {
        if (isPlayerRegistered(player)) {
            Config config = getPlayerConfig(player);
            String uuid = config.getString("lastUUID");
            return UUID.fromString(uuid);
        } else {
            return null;
        }
    }
    
    public boolean isPlayerLastUUID(Player player) {
        UUID uuid = getPlayerLastUUID(player);
        if (uuid == null) return false;
        return player.getUniqueId().equals(uuid);
    }
    
    public File getPlayerConfigFile(Player player) {
        return new File(playersFolder, player.getName().toLowerCase() + ".yml");
    }
    
    public Config getPlayerConfig(Player player) {
        return new Config(getPlayerConfigFile(player), Config.YAML);
    }
}