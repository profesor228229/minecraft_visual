package dev.nebula.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.awt.Color;

public class ColorSetting extends Setting<Integer> {
    public ColorSetting(String name, int argb) { super(name, argb); }

    public float[] hsb() {
        int c = value;
        return Color.RGBtoHSB((c >> 16) & 0xFF, (c >> 8) & 0xFF, c & 0xFF, null);
    }

    public void setHsb(float h, float s, float b) {
        value = (value & 0xFF000000) | (Color.HSBtoRGB(h, s, b) & 0x00FFFFFF);
    }

    @Override public JsonElement toJson() { return new JsonPrimitive(value); }
    @Override public void fromJson(JsonElement e) { value = e.getAsInt(); }
}
