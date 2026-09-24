package com.gx.velveteclipse.network;

import java.util.*;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

/** Server-selected impact positions. Cosmetic only; the server alone applies damage. */
public record BattleSpellS2C(String skill,List<BattleLightningS2C.Strike> targets) {
    public BattleSpellS2C {
        if(!com.gx.velveteclipse.core.persona.SpellVisuals.supported(skill))throw new IllegalArgumentException("Unknown spell visual");
        if(targets.size()>5)throw new IllegalArgumentException("Too many spell targets");
        targets=List.copyOf(targets);
        if(targets.stream().anyMatch(p->!Double.isFinite(p.x())||!Double.isFinite(p.y())||!Double.isFinite(p.z())))throw new IllegalArgumentException("Invalid spell position");
    }
    public static void encode(BattleSpellS2C p,FriendlyByteBuf b){b.m_130070_(p.skill);BattleLightningS2C.encode(new BattleLightningS2C(p.targets),b);}
    public static BattleSpellS2C decode(FriendlyByteBuf b){return new BattleSpellS2C(b.m_130277_(),BattleLightningS2C.decode(b).strikes());}
    public static void handle(BattleSpellS2C p,Supplier<NetworkEvent.Context> supplier){var ctx=supplier.get();ctx.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->com.gx.velveteclipse.client.SpellImpactRenderer.accept(p)));ctx.setPacketHandled(true);}
}
