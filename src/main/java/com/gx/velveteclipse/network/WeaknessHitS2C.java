package com.gx.velveteclipse.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

/** Cosmetic event sent only after the server resolves a player's weakness hit. */
public record WeaknessHitS2C(int id) {
    public static void encode(WeaknessHitS2C p,FriendlyByteBuf b){b.writeInt(p.id);}
    public static WeaknessHitS2C decode(FriendlyByteBuf b){return new WeaknessHitS2C(b.readInt());}
    public static void handle(WeaknessHitS2C p,Supplier<NetworkEvent.Context> supplier){
        var context=supplier.get();
        context.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->com.gx.velveteclipse.client.ClientPersonaCutinOverlay.trigger(p.id)));
        context.setPacketHandled(true);
    }
}
