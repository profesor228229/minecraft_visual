package dev.nebula.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class NumberSetting extends Setting<Double> {
    private final double min, max, step;
    private final String suffix;

    public NumberSetting(String name, double value, double min, double max, double step) {
        this(name, value, min, max, step, "");
    }

    public NumberSetting(String name, double value, double min, double max, double step, String suffix) {
        super(name, value);
        this.min = min; this.max = max; this.step = step; this.suffix = suffix;
    }

    @Override
    public void set(Double v) {
        double s = Math.round(v / step) * step;
        this.value = Math.max(min, Math.min(max, s));
    }

    public float getFloat() { return value.floatValue(); }
    public int getInt() { return value.intValue(); }
    public double getMin() { return min; }
    public double getMax() { return max; }
    public double getStep() { return step; }

    public String display() {
        String n = step >= 1 ? String.valueOf((long) Math.round(value)) : String.format(java.util.Locale.US, step >= 0.1 ? "%.1f" : "%.2f", value);
        return n + suffix;
    }

    @Override public JsonElement toJson() { return new JsonPrimitive(value); }
    @Override public void fromJson(JsonElement e) { set(e.getAsDouble()); }
}
