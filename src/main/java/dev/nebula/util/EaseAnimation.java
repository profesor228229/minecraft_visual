package dev.nebula.util;

/** Duration based animation with easing that can run forwards and backwards. */
public class EaseAnimation {
    private final long duration;
    private final Easing easing;
    private boolean forward;
    private long start;
    private double from;

    public EaseAnimation(long durationMs, Easing easing) {
        this.duration = durationMs;
        this.easing = easing;
        this.start = System.currentTimeMillis() - durationMs;
    }

    public void setDirection(boolean forward) {
        if (this.forward == forward) return;
        this.from = linear();
        this.forward = forward;
        this.start = System.currentTimeMillis();
    }

    public void reset(boolean forward) {
        this.forward = forward;
        this.from = forward ? 0 : 1;
        this.start = System.currentTimeMillis();
    }

    private double linear() {
        double t = Math.min(1.0, (System.currentTimeMillis() - start) / (double) duration);
        double to = forward ? 1 : 0;
        return from + (to - from) * t;
    }

    public double get() {
        double l = linear();
        return forward ? easing.apply(l) : 1.0 - easing.apply(1.0 - l);
    }

    public double getLinear() { return linear(); }
    public boolean isForward() { return forward; }
    public boolean isDone() { return System.currentTimeMillis() - start >= duration; }
}
