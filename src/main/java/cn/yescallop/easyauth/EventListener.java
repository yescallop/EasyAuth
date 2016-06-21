package cn.yescallop.easyauth;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.inventory.InventoryPickupItemEvent;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;
import cn.nukkit.event.player.PlayerDropItemEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerItemConsumeEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.inventory.InventoryHolder;
import cn.yescallop.easyauth.lang.BaseLang;

import java.util.HashMap;

public class EventListener implements Listener {

    EasyAuth plugin;
    BaseLang lang;
    HashMap<Player, String> confirmWaiting = new HashMap<>();

    public EventListener(EasyAuth plugin) {
        this.plugin = plugin;
        lang = plugin.getLanguage();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (plugin.isPlayerLastUUID(player)) {
            plugin.authenticatePlayer(player);
            player.sendMessage(lang.translateString("login.auto"));
        } else {
            player.sendMessage(plugin.isPlayerRegistered(player) ? lang.translateString("login.input") : lang.translateString("register.input"));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        if (!plugin.isPlayerAuthenticated(player)) {
            event.setCancelled();
            if (!plugin.isPlayerRegistered(player)) {
                if (!confirmWaiting.containsKey(player)) {
                    player.sendMessage(lang.translateString("register.confirm", message));
                    confirmWaiting.put(player, message);
                } else {
                    if (!message.equals("back")) {
                        if (message.equals(confirmWaiting.get(player))) {
                            plugin.registerPlayer(player, message);
                            player.sendMessage(lang.translateString("register.success"));
                            confirmWaiting.remove(player);
                        } else {
                            player.sendMessage(lang.translateString("register.notmatching"));
                        }
                    } else {
                        player.sendMessage(lang.translateString("register.reinput"));
                        confirmWaiting.remove(player);
                    }
                }
            } else {
                if (plugin.checkPlayerPassword(player, message)) {
                    plugin.authenticatePlayer(player);
                    player.sendMessage(lang.translateString("login.success"));
                } else {
                    player.kick(lang.translateString("login.fail"));
                    Server.getInstance().getNetwork().blockAddress(player.getAddress(), 30);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.isPlayerAuthenticated(event.getPlayer())) {
            event.setCancelled();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!plugin.isPlayerAuthenticated(event.getPlayer())) {
            event.setCancelled();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryPickupItem(InventoryPickupItemEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof Player && !plugin.isPlayerAuthenticated((Player) holder)) {
            event.setCancelled();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (!plugin.isPlayerAuthenticated(event.getPlayer()) && event.getMessage().startsWith("/")) {
            event.setCancelled();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!plugin.isPlayerAuthenticated(event.getPlayer())) {
            event.setCancelled();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!plugin.isPlayerAuthenticated(event.getPlayer())) {
            event.setCancelled();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        if (!plugin.isPlayerAuthenticated(event.getPlayer())) {
            event.setCancelled();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!plugin.isPlayerAuthenticated(event.getPlayer())) {
            event.setCancelled();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.deauthenticatePlayer(player);
        confirmWaiting.remove(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player && !plugin.isPlayerAuthenticated((Player) entity)) {
            event.setCancelled();
        }
    }
}