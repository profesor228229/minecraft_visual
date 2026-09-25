package dev.nebula.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.Arrays;
import java.util.List;

public class ModeSetting extends Setting<String> {
    private final List<String> modes;

    public ModeSetting(String name, String value, String... modes) {
        super(name, value);
        this.modes = Arrays.asList(modes);
    }

    public boolean is(String mode) { return value.equalsIgnoreCase(mode); }
    public List<String> getModes() { return modes; }
    public int index() { return Math.max(0, modes.indexOf(value)); }

    public void cycle(boolean forward) {
        int i = index() + (forward ? 1 : -1);
        if (i >= modes.size()) i = 0;
        if (i < 0) i = modes.size() - 1;
        value = modes.get(i);
    }

    @Override public JsonElement toJson() { return new JsonPrimitive(value); }
    @Override public void fromJson(JsonElement e) {
        String s = e.getAsString();
        if (modes.contains(s)) value = s;
    }
}
