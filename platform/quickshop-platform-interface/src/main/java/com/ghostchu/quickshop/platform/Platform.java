/*
 *  This file is a part of project QuickShop, the name is Platform.java
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

package com.ghostchu.quickshop.platform;

import de.tr7zw.nbtapi.NBTItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.command.PluginCommand;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Platform {
    void setLine(@NotNull Sign sign, int line, @NotNull Component component);
    @NotNull
    Component getLine(@NotNull Sign sign, int line);
    @NotNull
    TranslatableComponent getItemTranslationKey(@NotNull Material material);
    @NotNull
    HoverEvent<HoverEvent.ShowItem> getItemStackHoverEvent(@NotNull ItemStack stack);
    void registerCommand(@NotNull String prefix, @NotNull PluginCommand command);
    boolean isServerStopping();
    @NotNull
    String getMinecraftVersion();
    @Nullable
    default String getItemShopId(@NotNull ItemStack stack) {
        if(!Bukkit.getPluginManager().isPluginEnabled("NBTAPI")) {
            return null;
        }
        NBTItem nbtItem = new NBTItem(stack);
        String shopId = nbtItem.getString("shopId");
        if(shopId == null || shopId.isEmpty() || shopId.isBlank()) {
            return null;
        }
        return shopId;
    }
}
