package com.gx.velveteclipse.client.menu;

/** Framing for the approved rigs, including ribbons, tail and the complete attack turn. */
public final class ApprovedPersonaFraming {
    public static float sheetScale(String id, float fallback) {
        return switch (id) {
            case "apsaras" -> 110;
            case "jack_frost" -> 185;
            case "forneus" -> 95;
            case "chimera" -> 170;
            default -> fallback;
        };
    }
    public static float cardScale(String id, double width, double height, float fallback) {
        double[] bounds = switch (id) {
            case "apsaras" -> new double[]{1.9, 3.1};
            case "jack_frost" -> new double[]{1.35, 1.7};
            case "forneus" -> new double[]{4.1, 2.3};
            case "chimera" -> new double[]{2.4, 2.1};
            default -> null;
        };
        return bounds == null ? fallback : (float)Math.max(1, Math.min((width-20)/bounds[0], (height-20)/bounds[1]));
    }
    public static double baselineOffset(String id, float scale) {
        return id.equals("forneus") ? -.28 * scale : 0;
    }
    private ApprovedPersonaFraming() {}
}
