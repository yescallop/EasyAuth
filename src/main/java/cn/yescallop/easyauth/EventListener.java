package cn.yescallop.easyauth;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.inventory.InventoryPickupItemEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.inventory.InventoryHolder;
import cn.yescallop.easyauth.lang.BaseLang;

import java.util.HashMap;
import java.util.Map;

public class EventListener implements Listener {

    private final EasyAuthAPI api;
    private final BaseLang lang;
    private final Map<Player, String> confirmWaiting = new HashMap<>();

    protected EventListener() {
        this.api = EasyAuthAPI.getInstance();
        this.lang = api.getLanguage();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (api.isPlayerUsingLastClientId(player)) {
            api.authenticatePlayer(player);
            player.sendMessage(lang.translateString("login.auto"));
        } else {
            player.sendMessage(api.isPlayerRegistered(player) ? lang.translateString("login.input") : lang.translateString("register.input"));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        if (!api.isPlayerAuthenticated(player)) {
            event.setCancelled();
            if (!api.isPlayerRegistered(player)) {
                if (!confirmWaiting.containsKey(player)) {
                    player.sendMessage(lang.translateString("register.confirm", message));
                    confirmWaiting.put(player, message);
                } else {
                    if (message.equals(confirmWaiting.get(player))) {
                        api.registerPlayer(player, message);
                        api.authenticatePlayer(player);
                        player.sendMessage(lang.translateString("register.success"));
                        confirmWaiting.remove(player);
                    } else {
                        player.sendMessage(lang.translateString("register.notmatch"));
                    }
                }
            } else {
                if (api.checkPlayerPassword(player, message)) {
                    api.authenticatePlayer(player);
                    player.sendMessage(lang.translateString("login.success"));
                } else {
                    player.kick(lang.translateString("login.fail"));
                    api.getServer().getNetwork().blockAddress(player.getAddress(), 30);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!api.isPlayerAuthenticated(event.getPlayer())) {
            event.setCancelled();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!api.isPlayerAuthenticated(event.getPlayer())) {
            event.setCancelled();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryPickupItem(InventoryPickupItemEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof Player && !api.isPlayerAuthenticated((Player) holder)) {
            event.setCancelled();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (!api.isPlayerAuthenticated(event.getPlayer())) {
            event.setCancelled();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!api.isPlayerAuthenticated(event.getPlayer())) {
            event.setCancelled();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!api.isPlayerAuthenticated(event.getPlayer())) {
            event.setCancelled();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        if (!api.isPlayerAuthenticated(event.getPlayer())) {
            event.setCancelled();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!api.isPlayerAuthenticated(event.getPlayer())) {
            event.setCancelled();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        api.deauthenticatePlayer(player);
        confirmWaiting.remove(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player && !api.isPlayerAuthenticated((Player) entity)) {
            event.setCancelled();
        }
    }
}