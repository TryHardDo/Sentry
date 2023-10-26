package dev.tryharddo.sentry.utils;

import net.minecraft.world.phys.Vec3;

public class MathUtils {
    public static double computePitch(double sX, double sY, double sZ, double tX, double tY, double tZ) {
        double dX = tX - sX;
        double dY = tY - sY;
        double dZ = tZ - sZ;
        double dis = Math.sqrt(dX * dX + dY * dY + dZ * dZ);
        return -Math.asin(dY / dis);
    }

    public static double computeYaw(double sX, double sZ, double tX, double tZ) {
        double dX = tX - sX;
        double dZ = tZ - sZ;
        double dis = Math.sqrt(dX * dX + dZ * dZ);
        double rYaw = Math.atan2(dX / dis, dZ / dis);

        if (rYaw < 0) {
            rYaw += 2 * Math.PI;
        }

        return -rYaw;
    }
}
