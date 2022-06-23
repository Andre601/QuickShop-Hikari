/*
 *  This file is a part of project QuickShop, the name is SubCommand_Debug.java
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

package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.external.com.ti.ems.jacky.ResultSetToJson;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class SubCommand_Debug implements CommandHandler<CommandSender> {
    private final Cache<UUID, String> sqlCachePool = CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.SECONDS).build();

    public SubCommand_Debug(QuickShop plugin) {
        this.plugin = plugin;
    }

    private final QuickShop plugin;

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (cmdArg.length < 1) {
            switchDebug(sender);
            return;
        }

        switch (cmdArg[0]) {
            case "debug", "dev", "devmode" -> switchDebug(sender);
            case "handlerlist" -> handleHandlerList(sender, ArrayUtils.remove(cmdArg, 0));
            case "signs" -> handleSigns(sender);
            case "database" -> handleDatabase(sender, ArrayUtils.remove(cmdArg, 0));
            default -> MsgUtil.sendDirectMessage(sender, Component.text("Error! No correct arguments were entered!."));
        }
    }

    private void handleSigns(@NotNull CommandSender sender) {
        final BlockIterator bIt = new BlockIterator((LivingEntity) sender, 10);
        if (!bIt.hasNext()) {
            plugin.text().of(sender, "not-looking-at-shop").send();
            return;
        }
        while (bIt.hasNext()) {
            final Block b = bIt.next();
            final Shop shop = plugin.getShopManager().getShop(b.getLocation());
            if (shop != null) {
                shop.getSigns().forEach(sign -> MsgUtil.sendDirectMessage(sender, Component.text("Sign located at: " + sign.getLocation()).color(NamedTextColor.GREEN)));
                break;
            }
        }
    }

    private void handleDatabase(@NotNull CommandSender sender, @NotNull String[] cmdArg) {
        if (cmdArg.length < 1) {
            MsgUtil.sendDirectMessage(sender, Component.text("You must specific a operation!"));
            return;
        }
        switch (cmdArg[0]) {
            case "sql" -> handleDatabaseSQL(sender, ArrayUtils.remove(cmdArg, 0));
        }
    }


    private void handleDatabaseSQL(@NotNull CommandSender sender, @NotNull String[] cmdArg) {
        if (cmdArg.length < 1) {
            MsgUtil.sendDirectMessage(sender, Component.text("You must enter an valid Base64 encoded SQL!"));
            return;
        }
        if (cmdArg.length == 1) {
            try {
                byte[] b = Base64.getDecoder().decode(cmdArg[0]);
                String sql = new String(b, StandardCharsets.UTF_8);
                UUID uuid = UUID.randomUUID();
                sqlCachePool.put(uuid, sql);
                plugin.getLogger().warning("An SQL query pending for confirm executed by " + sender.getName() + " with UUID " + uuid + " and content " + sql);
                MsgUtil.sendDirectMessage(sender, Component.text("Warning: You're running a SQL query, please make sure you know what you're doing!").color(NamedTextColor.RED));
                MsgUtil.sendDirectMessage(sender, Component.text("SQL Content: " + sql));
                MsgUtil.sendDirectMessage(sender, Component.text("Type /qs debug sql confirm " + uuid + " in 60 seconds to confirm the query.")
                        .color(NamedTextColor.YELLOW)
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/qs debug database sql confirm " + uuid)));
                MsgUtil.sendDirectMessage(sender, Component.text("Don't and confirm unless you trust it.")
                        .color(NamedTextColor.RED)
                        .decorate(TextDecoration.BOLD)
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/qs debug database sql confirm " + uuid)));
            } catch (Exception e) {
                MsgUtil.sendDirectMessage(sender, Component.text("You must enter an valid Base64 encoded SQL!"));
                e.printStackTrace();
            }
        }
        if (cmdArg.length == 2) {
            if (cmdArg[0].equals("confirm")) {
                if (Util.isUUID(cmdArg[1])) {
                    UUID uuid = UUID.fromString(cmdArg[1]);
                    String sql = sqlCachePool.getIfPresent(uuid);
                    if (sql == null) {
                        MsgUtil.sendDirectMessage(sender, Component.text("No pending SQL query found with UUID " + uuid));
                        return;
                    }
                    sqlCachePool.invalidate(uuid);
                    plugin.getLogger().warning("An SQL query executed by " + sender.getName() + " with UUID " + uuid + " and content " + sql);
                    MsgUtil.sendDirectMessage(sender, Component.text("SQL Content: " + sql + ", executing..."));
                    try (Connection connection = plugin.getSqlManager().getConnection()) {
                        Statement statement = connection.createStatement();
                        boolean success = statement.execute(sql);
                        if (!success) {
                            MsgUtil.sendDirectMessage(sender, Component.text("An error occurred while executing the SQL query!"));
                        } else {
                            ResultSet set = statement.getResultSet();
                            MsgUtil.sendDirectMessage(sender, Component.text(ResultSetToJson.resultSetToJsonString(set)));
                            MsgUtil.sendDirectMessage(sender, Component.text("Completed, " + statement.getLargeUpdateCount() + ".").color(NamedTextColor.GREEN));
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        MsgUtil.sendDirectMessage(sender, Component.text("An error occurred while executing the SQL query, check the Console!").color(NamedTextColor.RED));
                    }
                } else {
                    MsgUtil.sendDirectMessage(sender, Component.text("You must enter an valid index UUID that already registered into caches!"));
                }
            }
        }
    }

    private void handleHandlerList(@NotNull CommandSender sender, @NotNull String[] cmdArg) {
        if (cmdArg.length < 1) {
            MsgUtil.sendDirectMessage(sender, Component.text("You must enter an Bukkit Event class"));
            return;
        }
        printHandlerList(sender, cmdArg[1]);
    }

    public void switchDebug(@NotNull CommandSender sender) {
        final boolean debug = plugin.getConfig().getBoolean("dev-mode");

        if (debug) {
            plugin.reloadConfig();
            plugin.getConfig().set("dev-mode", false);
            plugin.saveConfig();
            plugin.getReloadManager().reload();
            plugin.text().of(sender, "command.now-nolonger-debuging").send();
            return;
        }

        plugin.reloadConfig();
        plugin.getConfig().set("dev-mode", true);
        plugin.saveConfig();
        plugin.getReloadManager().reload();
        plugin.text().of(sender, "command.now-debuging").send();
    }

    public void printHandlerList(@NotNull CommandSender sender, String event) {
        try {
            final Class<?> clazz = Class.forName(event);
            final Method method = clazz.getMethod("getHandlerList");
            final Object[] obj = new Object[0];
            final HandlerList list = (HandlerList) method.invoke(null, obj);

            for (RegisteredListener listener1 : list.getRegisteredListeners()) {
                MsgUtil.sendDirectMessage(sender,
                        LegacyComponentSerializer.legacySection().deserialize(ChatColor.AQUA
                                + listener1.getPlugin().getName()
                                + ChatColor.YELLOW
                                + " # "
                                + ChatColor.GREEN
                                + listener1.getListener().getClass().getCanonicalName()));
            }
        } catch (Exception th) {
            MsgUtil.sendDirectMessage(sender, Component.text("ERR " + th.getMessage()).color(NamedTextColor.RED));
            plugin.getLogger().log(Level.WARNING, "An error has occurred while getting the HandlerList", th);
        }
    }

    @NotNull
    @Override
    public List<String> onTabComplete(
            @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return Collections.emptyList();
    }

}
