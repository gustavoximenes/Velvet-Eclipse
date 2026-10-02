package com.gx.velveteclipse.exploration;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.*;
import java.util.UUID;

/** One transient, server-authoritative bonus. Never replaces sprinting or equipment attributes. */
public final class ExplorationSprint {
    public static final double BONUS=.20;
    private static final UUID ID=UUID.fromString("9e9c7295-2e08-443e-90fb-62961e2bfdea");
    private static final AttributeModifier MODIFIER=new AttributeModifier(ID,"Velvet exploration sprint",BONUS,AttributeModifier.Operation.MULTIPLY_TOTAL);
    public static boolean eligible(ExplorationRules.Mode mode,boolean sprinting){return mode==ExplorationRules.Mode.EXPLORATION&&sprinting;}
    public static void update(ServerPlayer p,boolean enabled){
        update(p.m_21051_(Attributes.f_22279_),enabled);
    }
    public static void update(AttributeInstance attribute,boolean enabled){
        if(attribute==null)return;
        if(enabled){if(attribute.m_22111_(ID)==null)attribute.m_22118_(MODIFIER);}
        else attribute.m_22120_(ID);
    }
}
