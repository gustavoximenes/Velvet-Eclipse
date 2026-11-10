package com.gx.velveteclipse.darkhour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.shapes.*;

/** Opaque portal; collision holds visitors until server-authoritative routing completes. */
public final class TartarusPortalBlock extends TartarusScenicBlock {
 private final String route;
 public TartarusPortalBlock(String route){super(BlockBehaviour.Properties.m_284310_().m_60913_(-1,3600000).m_60955_());this.route=route;}
 @Override public VoxelShape m_5939_(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return TartarusPortalSafety.BARRIER;}
 @Override public void m_7892_(BlockState state,Level level,BlockPos pos,Entity entity){if(entity instanceof ServerPlayer p){if(route.equals("entrance")&&com.gx.velveteclipse.city.CityService.inside(p))com.gx.velveteclipse.city.CityService.enterTartarus(p);else TartarusDungeonService.cross(p,route);}}
}
