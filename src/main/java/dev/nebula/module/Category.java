package dev.nebula.module;

public enum Category {
    HUD("Hud", "\u25A3"),
    RENDER("Render", "\u2726"),
    WORLD("World", "\u2600"),
    PLAYER("Player", "\u263A"),
    CLIENT("Client", "\u2699");

    public final String display;
    public final String icon;

    Category(String display, String icon) {
        this.display = display;
        this.icon = icon;
    }
}
