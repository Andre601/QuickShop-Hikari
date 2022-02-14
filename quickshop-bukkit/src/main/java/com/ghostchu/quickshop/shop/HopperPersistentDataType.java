/*
 *  This file is a part of project QuickShop, the name is HopperPersistentDataType.java
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
package com.ghostchu.quickshop.shop;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import com.ghostchu.quickshop.util.MsgUtil;

import java.util.UUID;

public class HopperPersistentDataType implements PersistentDataType<String, HopperPersistentData> {
    public static final HopperPersistentDataType INSTANCE = new HopperPersistentDataType();

    private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    @Override
    public @NotNull Class<String> getPrimitiveType() {
        return String.class;
    }

    @Override
    public @NotNull Class<HopperPersistentData> getComplexType() {
        return HopperPersistentData.class;
    }

    @NotNull
    @Override
    public String toPrimitive(@NotNull HopperPersistentData complex, @NotNull PersistentDataAdapterContext context) {
        try {
            return GSON.toJson(complex);
        } catch (Exception th) {
            MsgUtil.debugStackTrace(th.getStackTrace());
            return "";
        }
    }

    @NotNull
    @Override
    public HopperPersistentData fromPrimitive(
            @NotNull String primitive, @NotNull PersistentDataAdapterContext context) {
        try {
            return GSON.fromJson(primitive, HopperPersistentData.class);
        } catch (Exception th) {
            MsgUtil.debugStackTrace(th.getStackTrace());
            return new HopperPersistentData(new UUID(0,0));
        }
    }
}
