/*
 *  This file is a part of project QuickShop, the name is Lands.java
 *  Copyright (C) Ghost_chu and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.ghostchu.quickshop.compatibility.lands;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.ShopAuthorizeCalculateEvent;
import com.ghostchu.quickshop.api.event.ShopCreateEvent;
import com.ghostchu.quickshop.api.event.ShopPreCreateEvent;
import com.ghostchu.quickshop.api.event.ShopPurchaseEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopChunk;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import com.ghostchu.quickshop.util.Util;
import me.angeschossen.lands.api.events.LandUntrustPlayerEvent;
import me.angeschossen.lands.api.events.PlayerLeaveLandEvent;
import me.angeschossen.lands.api.integration.LandsIntegration;
import me.angeschossen.lands.api.land.Land;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.UUID;

public final class Main extends CompatibilityModule implements Listener {
    private boolean ignoreDisabledWorlds;
    private boolean whitelist;
    private LandsIntegration landsIntegration;
    private boolean deleteWhenLosePermission;

    public void init() {
        landsIntegration = new me.angeschossen.lands.api.integration.LandsIntegration(this);
        ignoreDisabledWorlds = getConfig().getBoolean("ignore-disabled-worlds");
        whitelist = getConfig().getBoolean("whitelist-mode");
        deleteWhenLosePermission = getConfig().getBoolean("delete-on-lose-permission");
    }

    @EventHandler(ignoreCancelled = true)
    public void permissionOverride(ShopAuthorizeCalculateEvent event) {
        Location shopLoc = event.getShop().getLocation();
        Land land = landsIntegration.getLand(shopLoc);
        if (land == null) return;
        if (land.getOwnerUID().equals(event.getAuthorizer())) {
            if (event.getNamespace().equals(QuickShop.getInstance()) && event.getPermission().equals(BuiltInShopPermission.DELETE.getRawNode())) {
                event.setResult(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPreCreation(ShopPreCreateEvent event) {
        if (landsIntegration.getLandWorld(event.getLocation().getWorld()) == null) {
            if (!ignoreDisabledWorlds) {
                event.setCancelled(true, "Lands: this world not in list and ignore-disabled-worlds is disabled");
                return;
            }
        }
        Land land = landsIntegration.getLand(event.getLocation());
        if (land != null) {
            if (land.getOwnerUID().equals(event.getPlayer().getUniqueId()) || land.isTrusted(event.getPlayer().getUniqueId())) {
                return;
            }
            event.setCancelled(true, "Lands: you don't have permission to create shop here");
        } else {
            if (whitelist) {
                event.setCancelled(true, "Lands: you don't have permission to create shop here (whitelist)");
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreation(ShopCreateEvent event) {
        if (landsIntegration.getLandWorld(event.getShop().getLocation().getWorld()) == null) {
            if (!ignoreDisabledWorlds) {
                event.setCancelled(true, "Lands: this world not in list and ignore-disabled-worlds is disabled");
                return;
            }
        }
        Land land = landsIntegration.getLand(event.getShop().getLocation());
        if (land != null) {
            if (land.getOwnerUID().equals(event.getPlayer().getUniqueId()) || land.isTrusted(event.getPlayer().getUniqueId())) {
                return;
            }
            event.setCancelled(true, "Lands: you don't have permission to create shop here");
        } else {
            if (whitelist) {
                event.setCancelled(true, "Lands: you don't have permission to create shop here (whitelist)");
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTrading(ShopPurchaseEvent event) {
        if (landsIntegration.getLandWorld(event.getShop().getLocation().getWorld()) == null) {
            if (ignoreDisabledWorlds) {
                return;
            }
            event.setCancelled(true, "Lands: this world not in list and ignore-disabled-worlds is disabled");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLandsPermissionChanges(LandUntrustPlayerEvent event) {
        if (!deleteWhenLosePermission) {
            return;
        }
        deleteShopInLand(event.getLand(), event.getTarget());
    }

    private void deleteShopInLand(Land land, UUID target) {
        //Getting all shop with world-chunk-shop mapping
        for (Map.Entry<String, Map<ShopChunk, Map<Location, Shop>>> entry : getApi().getShopManager().getShops().entrySet()) {
            //Matching world
            World world = getServer().getWorld(entry.getKey());
            if (world != null) {
                //Matching chunk
                for (Map.Entry<ShopChunk, Map<Location, Shop>> chunkedShopEntry : entry.getValue().entrySet()) {
                    ShopChunk shopChunk = chunkedShopEntry.getKey();
                    if (land.hasChunk(world, shopChunk.getX(), shopChunk.getZ())) {
                        //Matching Owner and delete it
                        Map<Location, Shop> shops = chunkedShopEntry.getValue();
                        for (Shop shop : shops.values()) {
                            if (target.equals(shop.getOwner())) {
                                recordDeletion(Util.getNilUniqueId(), shop, "Lands: shop deleted because owner lost permission");
                                Util.mainThreadRun(shop::delete);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLandsMember(PlayerLeaveLandEvent event) {
        if (!deleteWhenLosePermission) {
            return;
        }
        deleteShopInLand(event.getLand(), event.getLandPlayer().getUID());
    }

}
