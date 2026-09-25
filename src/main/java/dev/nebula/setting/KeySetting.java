package dev.nebula.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeySetting extends Setting<Integer> {
    public KeySetting(String name, int key) { super(name, key); }

    public String keyName() {
        int k = value;
        if (k == GLFW.GLFW_KEY_UNKNOWN || k == 0) return "None";
        try {
            String s = InputUtil.Type.KEYSYM.createFromCode(k).getLocalizedText().getString();
            return s.length() > 1 ? s.substring(0, 1).toUpperCase() + s.substring(1) : s.toUpperCase();
        } catch (Exception e) {
            return "Key " + k;
        }
    }

    @Override public JsonElement toJson() { return new JsonPrimitive(value); }
    @Override public void fromJson(JsonElement e) { value = e.getAsInt(); }
}
