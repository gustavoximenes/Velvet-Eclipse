package com.gx.velveteclipse.darkhour;

import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Stops movement inside the trigger cell, so entityInside can still request a transfer. */
public final class TartarusPortalSafety {
    private TartarusPortalSafety() {}
    public static final long COOLDOWN_MILLIS = 2000;
    // Every generated Tartarus portal lies in an X/Y plane. Recessing the barrier
    // lets the player's box enter the trigger cell before collision stops it.
    public static final VoxelShape BARRIER = Shapes.m_83048_(0, 0, 7.0 / 16, 1, 1, 9.0 / 16);

    public static boolean coolingDown(long now, long lastTransfer) {
        return lastTransfer > 0 && now - lastTransfer < COOLDOWN_MILLIS;
    }
}
