package com.gx.velveteclipse.client.exploration;

/** Floor decal coordinates. Texture top faces away from the viewing camera. */
public final class EntryBannerGeometry {
    public record Vertex(double x, double y, double z, float u, float v) {}

    public static Vertex[] quad(double x, double y, double z,
                                double eyeX, double eyeZ, double fallbackYawDegrees) {
        double awayX = x - eyeX, awayZ = z - eyeZ;
        double distance = Math.hypot(awayX, awayZ);
        if (distance < 1e-4) {
            double yaw = Math.toRadians(fallbackYawDegrees);
            awayX = -Math.sin(yaw);
            awayZ = Math.cos(yaw);
        } else {
            awayX /= distance;
            awayZ /= distance;
        }
        // Camera's horizontal right, not the player's right. Both axes must change
        // together: flipping only V would turn the upside-down text into a mirror.
        double rightX = -awayZ, rightZ = awayX;
        double[][] corners = {{-3.4, .85, 0, 0}, {-3.4, -.85, 0, 1},
                              {3.4, -.85, 1, 1}, {3.4, .85, 1, 0}};
        Vertex[] vertices = new Vertex[4];
        for (int i = 0; i < corners.length; i++) {
            double[] c = corners[i];
            vertices[i] = new Vertex(x + rightX*c[0] + awayX*c[1], y,
                    z + rightZ*c[0] + awayZ*c[1], (float)c[2], (float)c[3]);
        }
        return vertices;
    }

    private EntryBannerGeometry() {}
}
