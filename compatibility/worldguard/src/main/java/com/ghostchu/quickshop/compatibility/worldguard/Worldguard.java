/*
 *  This file is a part of project QuickShop, the name is Worldguard.java
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

package com.ghostchu.quickshop.compatibility.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import com.ghostchu.quickshop.api.event.ShopCreateEvent;
import com.ghostchu.quickshop.api.event.ShopPreCreateEvent;
import com.ghostchu.quickshop.api.event.ShopPurchaseEvent;

import java.util.logging.Level;

public final class Worldguard extends JavaPlugin implements Listener {
    private StateFlag createFlag;
    private StateFlag tradeFlag;
    @Override
    public void onLoad() {
        saveDefaultConfig();
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            // create a flag with the name "my-custom-flag", defaulting to true
            StateFlag createFlag = new StateFlag("quickshophikari-create", getConfig().getBoolean("create.default-allow",false));
            StateFlag tradeFlag = new StateFlag("quickshophikari-trade", getConfig().getBoolean("trade.default-allow",true));
            registry.register(createFlag);
            registry.register(tradeFlag);
            this.createFlag = createFlag;
            this.tradeFlag = tradeFlag;
        } catch (FlagConflictException e) {
            // some other plugin registered a flag by the same name already.
            // you can use the existing flag, but this may cause conflicts - be sure to check type
            Flag<?> existing = registry.get("quickshophikari-create");
            if (existing instanceof StateFlag createFlag) {
               this.createFlag = createFlag;
            } else {
               getLogger().log(Level.WARNING,"Could not register flags! CONFLICT!",e);
               Bukkit.getPluginManager().disablePlugin(this);
               return;
            }
            existing = registry.get("quickshophikari-reade");
            if (existing instanceof StateFlag tradeFlag) {
                this.tradeFlag = tradeFlag;
            } else {
                getLogger().log(Level.WARNING,"Could not register flags! CONFLICT!",e);
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
        }
        getLogger().info("QuickShop Compatibility Module - WorldGuard loaded");
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getPluginManager().registerEvents(this,this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler(ignoreCancelled = true)
    public void preCreation(ShopPreCreateEvent event){
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(event.getPlayer());
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        if (!query.testState(BukkitAdapter.adapt(event.getLocation()), localPlayer, this.createFlag)) {
            event.setCancelled(true,"WorldGuard CreationFlag Test Failed");
        }
    }
    @EventHandler(ignoreCancelled = true)
    public void preCreation(ShopCreateEvent event){
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(event.getPlayer());
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        if (!query.testState(BukkitAdapter.adapt(event.getShop().getLocation()), localPlayer, this.createFlag)) {
            event.setCancelled(true,"WorldGuard CreationFlag Test Failed");
        }
    }
    @EventHandler(ignoreCancelled = true)
    public void preCreation(ShopPurchaseEvent event){
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(event.getPlayer());
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        if (!query.testState(BukkitAdapter.adapt(event.getShop().getLocation()), localPlayer, this.tradeFlag)) {
            event.setCancelled(true,"WorldGuard TradeFlag Test Failed");
        }
    }
}
