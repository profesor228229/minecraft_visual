package dev.nebula.util;

public interface Easing {
    double apply(double t);

    Easing LINEAR = t -> t;
    Easing OUT_CUBIC = t -> 1 - Math.pow(1 - t, 3);
    Easing OUT_QUINT = t -> 1 - Math.pow(1 - t, 5);
    Easing IN_OUT_CUBIC = t -> t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
    Easing OUT_BACK = t -> {
        double c1 = 1.70158, c3 = c1 + 1;
        return 1 + c3 * Math.pow(t - 1, 3) + c1 * Math.pow(t - 1, 2);
    };
    Easing OUT_EXPO = t -> t >= 1 ? 1 : 1 - Math.pow(2, -10 * t);
}
