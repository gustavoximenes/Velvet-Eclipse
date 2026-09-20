package com.gx.velveteclipse.darkhour;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.network.chat.Component;
/** Creative placement of the same authored entities used by the dungeon. */
public final class TartarusPropItem extends Item {
 public static final String PLACED="tartarus_placed_prop";
 private final String kind;
 public TartarusPropItem(String kind){super(new Item.Properties().m_41487_(64));this.kind=kind;}
 @Override public InteractionResult m_6225_(UseOnContext c){
  var p=c.m_43723_();var l=c.m_43725_();if(p==null)return InteractionResult.FAIL;
  if(TartarusDungeonService.inside(p)||com.gx.velveteclipse.forge.BattleService.current(p)!=null){if(!l.f_46443_)p.m_5661_(Component.m_237113_("Coloque estas peças fora do Tártaro e de batalhas."),true);return InteractionResult.FAIL;}
  if(c.m_43719_()!=Direction.UP)return InteractionResult.FAIL;
  var hit=c.m_43720_();double x=Math.floor(hit.f_82479_)+.5,y=Math.ceil(hit.f_82480_-1e-5),z=Math.floor(hit.f_82481_)+.5;
  float yaw=Math.round((p.m_146908_()+180)/90)*90;
  double w=kind.equals("terminal")?3.3:kind.equals("monad")?3.4:1.5,h=kind.equals("terminal")?4:kind.equals("monad")?5.1:1.8,d=kind.equals("monad")?.8:w;
  if(Math.floorMod(Math.round(yaw/90),2)==1){double t=w;w=d;d=t;}
  var bounds=new AABB(x-w/2,y,z-d/2,x+w/2,y+h,z+d/2);
  if(!l.m_45756_(null,bounds)||!l.m_45976_(TartarusPropEntity.class,bounds).isEmpty())return InteractionResult.FAIL;
  if(l instanceof ServerLevel server){var e=TartarusPropRegistry.type(kind).m_20615_(l);if(e==null)return InteractionResult.FAIL;e.m_6034_(x,y,z);e.m_146922_(yaw);e.getPersistentData().m_128379_(PLACED,true);if(!server.m_7967_(e))return InteractionResult.FAIL;if(!p.m_150110_().f_35937_)c.m_43722_().m_41774_(1);}
  return InteractionResult.SUCCESS;
 }
}
