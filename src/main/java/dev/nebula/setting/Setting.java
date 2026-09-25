package dev.nebula.setting;

import com.google.gson.JsonElement;

import java.util.function.BooleanSupplier;

public abstract class Setting<T> {
    private final String name;
    protected T value;
    private final T defaultValue;
    private BooleanSupplier visibility = () -> true;

    protected Setting(String name, T value) {
        this.name = name;
        this.value = value;
        this.defaultValue = value;
    }

    public String getName() { return name; }
    public T get() { return value; }
    public void set(T value) { this.value = value; }
    public void reset() { this.value = defaultValue; }
    public boolean isVisible() { return visibility.getAsBoolean(); }

    @SuppressWarnings("unchecked")
    public <S extends Setting<T>> S visibleIf(BooleanSupplier s) {
        this.visibility = s;
        return (S) this;
    }

    public abstract JsonElement toJson();
    public abstract void fromJson(JsonElement e);
}
