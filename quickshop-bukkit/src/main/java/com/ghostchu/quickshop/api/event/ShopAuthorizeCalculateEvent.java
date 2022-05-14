package com.ghostchu.quickshop.api.event;

import com.ghostchu.quickshop.api.shop.Shop;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class ShopAuthorizeCalculateEvent extends AbstractQSEvent {

    @NotNull
    private final Shop shop;

    private final Plugin namespace;

    private final String permission;

    private boolean result;

    /**
     * Call when shop was clicked.
     *
     * @param shop The shop bought from
     */
    public ShopAuthorizeCalculateEvent(@NotNull Shop shop, @NotNull Plugin namespace, @NotNull String permission, boolean result) {
        this.shop = shop;
        this.namespace = namespace;
        this.permission = permission;
        this.result = result;
    }


    /**
     * Getting the shops that checking for
     *
     * @return Shop
     */
    public @NotNull Shop getShop() {
        return this.shop;
    }

    /**
     * Gets the namespace of the permission
     *
     * @return namespace
     */
    @NotNull
    public Plugin getNamespace() {
        return namespace;
    }

    /**
     * Getting the permission
     *
     * @return permission
     */
    @NotNull
    public String getPermission() {
        return permission;
    }

    /**
     * Gets the final result of permission check
     *
     * @return true if permission is granted, false if not
     */
    public boolean getResult() {
        return result;
    }

    /**
     * Sets the final result of permission check
     *
     * @param result true if permission is granted, false if not
     */
    public void setResult(boolean result) {
        this.result = result;
    }
}
