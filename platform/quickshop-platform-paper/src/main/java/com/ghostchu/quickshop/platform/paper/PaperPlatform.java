/*
 *  This file is a part of project QuickShop, the name is PaperPlatform.java
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

package com.ghostchu.quickshop.platform.paper;

import com.ghostchu.quickshop.platform.Platform;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.command.PluginCommand;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class PaperPlatform implements Platform {

    private Map<String, String> translationMapping;

    public PaperPlatform(Map<String, String> mapping){
        this.translationMapping = mapping;
    }


    @Override
    public void setLine(@NotNull Sign sign, int line, @NotNull Component component) {
        sign.line(line, component);
    }

    @Override
    public @NotNull Component getLine(@NotNull Sign sign, int line) {
        return sign.line(line);
    }

    @Override
    public @NotNull TranslatableComponent getItemTranslationKey(@NotNull Material material) {
        try {
            return Component.translatable(material.translationKey());
        } catch (Error e) {
            return Component.translatable(material.getTranslationKey());
        }
    }

    @Override
    public @NotNull HoverEvent<HoverEvent.ShowItem> getItemStackHoverEvent(@NotNull ItemStack stack) {
        return stack.asHoverEvent();
    }

    @Override
    public void registerCommand(@NotNull String prefix, @NotNull PluginCommand command) {
        Bukkit.getCommandMap().register(prefix, command);
        Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
    }

    @Override
    public boolean isServerStopping() {
        return Bukkit.isStopping();
    }

    @Override
    public @NotNull String getMinecraftVersion() {
        return Bukkit.getMinecraftVersion();
    }

    @Override
    public @NotNull String getTranslationKey(@NotNull Material material) {
        return postProcessingTranslationKey(material.translationKey());
    }

    private String postProcessingTranslationKey(String key){
        return this.translationMapping.getOrDefault(key,key);
    }

    @Override
    public @NotNull String getTranslationKey(@NotNull EntityType type) {
        return postProcessingTranslationKey(type.translationKey());
    }

    @Override
    public @NotNull String getTranslationKey(@NotNull PotionEffectType potionEffectType) {
        return postProcessingTranslationKey(potionEffectType.translationKey());
    }

    @Override
    public @NotNull String getTranslationKey(@NotNull Enchantment enchantment) {
        return postProcessingTranslationKey(enchantment.translationKey());
    }

    @Override
    public @NotNull Component getTranslation(@NotNull Material material) {
        return Component.translatable(getTranslationKey(material));
    }

    @Override
    public @NotNull Component getTranslation(@NotNull EntityType entity) {
        return Component.translatable(getTranslationKey(entity));
    }

    @Override
    public @NotNull Component getTranslation(@NotNull PotionEffectType potionEffectType) {
        return Component.translatable(getTranslationKey(potionEffectType));
    }

    @Override
    public @NotNull Component getTranslation(@NotNull Enchantment enchantment) {
        return Component.translatable(getTranslationKey(enchantment));
    }

    @Override
    public @NotNull Component getDisplayName(@NotNull ItemStack stack) {
        return stack.displayName();
    }

    @Override
    public void setDisplayName(@NotNull ItemStack stack, @NotNull Component component) {
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(component);
        stack.setItemMeta(meta);
    }

    @Override
    public void setDisplayName(@NotNull Item stack, @NotNull Component component) {
        stack.customName(component);
    }

    @Override
    public void updateTranslationMappingSection(@NotNull Map<String, String> mapping) {
        this.translationMapping = mapping;
    }
}
