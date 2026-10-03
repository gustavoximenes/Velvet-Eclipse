package com.gx.velveteclipse.darkhour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.*;

/** Opaque black surface, with no collision; server-authoritative routing. */
public final class TartarusPortalBlock extends TartarusScenicBlock {
 private final String route;
 public TartarusPortalBlock(String route){super(BlockBehaviour.Properties.m_284310_().m_60913_(-1,3600000).m_60910_().m_60955_());this.route=route;}
 @Override public void m_7892_(BlockState state,Level level,BlockPos pos,Entity entity){if(entity instanceof ServerPlayer p)TartarusDungeonService.cross(p,route);}
}
