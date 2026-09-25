package dev.nebula.util;

/** Frame-rate independent exponential smoothing. */
public class Animation {
    private double value;
    private double target;
    private double speed;
    private long last = System.nanoTime();

    public Animation(double initial, double speed) {
        this.value = initial;
        this.target = initial;
        this.speed = speed;
    }

    public double update() {
        long now = System.nanoTime();
        double dt = Math.min((now - last) / 1_000_000_000.0, 0.1);
        last = now;
        value += (target - value) * (1.0 - Math.exp(-speed * dt));
        if (Math.abs(target - value) < 0.0005) value = target;
        return value;
    }

    public double animate(double target) {
        this.target = target;
        return update();
    }

    public double get() { return value; }
    public double getTarget() { return target; }
    public void setTarget(double t) { this.target = t; }
    public void set(double v) { this.value = v; this.target = v; }
    public boolean isDone() { return value == target; }
}
