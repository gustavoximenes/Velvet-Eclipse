package com.gx.velveteclipse.shadow;
import com.gx.velveteclipse.darkhour.*;import com.gx.velveteclipse.forge.*;import com.gx.velveteclipse.exploration.ExplorationService;
import net.minecraft.world.entity.*;import net.minecraft.world.level.Level;import net.minecraft.server.level.ServerPlayer;import net.minecraft.nbt.CompoundTag;import net.minecraft.network.syncher.*;import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;import software.bernie.geckolib.core.animation.*;import software.bernie.geckolib.util.GeckoLibUtil;import java.util.*;
/** The roaming silhouette is separate from every revealed battle actor. */
public final class FieldShadowEntity extends PathfinderMob implements GeoEntity {
 private static final EntityDataAccessor<Boolean> RARE=SynchedEntityData.m_135353_(FieldShadowEntity.class,EntityDataSerializers.f_135035_);
 private static final EntityDataAccessor<Integer> MOOD=SynchedEntityData.m_135353_(FieldShadowEntity.class,EntityDataSerializers.f_135028_);
 private final AnimatableInstanceCache cache=GeckoLibUtil.createInstanceCache(this);private final ShadowAwareness brain=new ShadowAwareness();
 private UUID target;private long seed;private boolean initialized,home;private double homeX,homeZ;private float searchYaw,noticeYaw;private int wanderDelay;
 public FieldShadowEntity(EntityType<? extends FieldShadowEntity> type,Level level){super(type,level);m_21530_();m_20331_(true);m_20049_("velvet_encounter_shadow");}
 @Override protected void m_8099_(){} // No vanilla melee, random jump or automatic targeting goals.
 @Override protected void m_8097_(){super.m_8097_();f_19804_.m_135372_(RARE,false);f_19804_.m_135372_(MOOD,0);}
 public boolean rare(){return f_19804_.m_135370_(RARE);}public void rare(boolean v){f_19804_.m_135381_(RARE,v);}
 public int mood(){return f_19804_.m_135370_(MOOD);}public long encounterSeed(){return seed;}
 public void initialize(long value){seed=value;initialized=true;rare(ShadowEncounterRules.rare(seed));}
 @Override public void m_7380_(CompoundTag t){super.m_7380_(t);t.m_128356_("encounter_seed",seed);t.m_128379_("seeded",initialized);t.m_128379_("rare",rare());t.m_128379_("home",home);t.m_128347_("home_x",homeX);t.m_128347_("home_z",homeZ);}
 @Override public void m_7378_(CompoundTag t){super.m_7378_(t);seed=t.m_128454_("encounter_seed");initialized=t.m_128471_("seeded");rare(t.m_128471_("rare"));home=t.m_128471_("home");homeX=t.m_128459_("home_x");homeZ=t.m_128459_("home_z");}
 private boolean available(ServerPlayer p){return p!=null&&!com.gx.velveteclipse.awakening.AwakeningService.active(p)&&p.m_6084_()&&!p.m_5833_()&&p.m_9236_()==m_9236_()&&BattleService.current(p)==null&&!ExplorationService.preparing(p)&&!ShadowWorldService.escaping(p)&&PlayerPersonaData.load(p).hp()>0;}
 public static boolean sameRoom(Entity a,Entity b){
  if(a.m_9236_()!=b.m_9236_())return false;if(!TartarusDungeonService.inside(a))return Math.abs(a.m_20186_()-b.m_20186_())<3;
  int f=TartarusDungeonService.floor(a);if(f!=TartarusDungeonService.floor(b)||TartarusInteriorLayout.fixedFloor(f))return false;int ox=TartarusInteriorLayout.origin(f);
  return room(a.m_20185_()-ox)==room(b.m_20185_()-ox)&&room(a.m_20189_())==room(b.m_20189_())&&Math.abs(a.m_20186_()-b.m_20186_())<3;
 }
 private static int room(double coordinate){return (int)Math.floor((coordinate+48)/24);}
 private boolean sees(ServerPlayer p){return available(p)&&sameRoom(this,p)&&m_20280_(p)<=18*18&&m_142582_(p)&&ShadowAwareness.front(m_146908_(),p.m_20185_()-m_20185_(),p.m_20189_()-m_20189_());}
 private void face(float yaw){m_146922_(yaw);m_5616_(yaw);m_5618_(yaw);}
 @Override public void m_8119_(){
  super.m_8119_();if(m_9236_().f_46443_)return;if(!initialized)initialize(m_20148_().getMostSignificantBits());if(!home){home=true;homeX=m_20185_();homeZ=m_20189_();}
  if(ShadowWorldService.held(this)||ExplorationService.reserved(this)||m_21525_()){m_21573_().m_26573_();m_20256_(Vec3.f_82478_);return;}
  if(TartarusDungeonService.inside(this)&&TartarusInteriorLayout.fixedFloor(TartarusDungeonService.floor(this))){m_21573_().m_26573_();return;}
  var level=(net.minecraft.server.level.ServerLevel)m_9236_();ServerPlayer p=target==null?null:level.m_7654_().m_6846_().m_11259_(target);
  if(target!=null&&!available(p)){p=null;target=null;brain.reset();}
  if(brain.phase!=ShadowAwareness.Phase.CHASE&&brain.phase!=ShadowAwareness.Phase.ATTACK){
   final ServerPlayer current=p;var candidate=level.m_6907_().stream().filter(this::available).filter(q->sameRoom(this,q)&&m_20280_(q)<18*18).filter(q->sees(q)||m_20280_(q)<9).min(Comparator.comparingDouble(q->m_20280_(q))).orElse(null);
   if(candidate!=null){if(current!=candidate)brain.seen=0;p=candidate;target=p.m_20148_();}
  }
  boolean close=p!=null&&m_20280_(p)<9,visible=p!=null&&sees(p),valid=p!=null&&available(p)&&m_20280_(p)<24*24&&(!TartarusDungeonService.inside(this)||sameRoom(this,p));
  boolean wasQuestion=brain.questioning();var before=brain.phase;brain.update(visible,close,valid);if(!wasQuestion&&brain.questioning())noticeYaw=m_146908_();if(before!=ShadowAwareness.Phase.SEARCH&&brain.phase==ShadowAwareness.Phase.SEARCH)searchYaw=m_146908_();
  if(brain.phase==ShadowAwareness.Phase.SEARCH||brain.phase==ShadowAwareness.Phase.NOTICE){m_21573_().m_26573_();m_20256_(new Vec3(0,m_20184_().f_82480_,0));if(!brain.canTurn())face(noticeYaw);else if(valid)facePlayer(p);else turn(searchYaw+180*Math.min(1,(brain.alertTicks-ShadowAwareness.TURN_DELAY)/40f));}
  else if(brain.phase==ShadowAwareness.Phase.IDLE)wander();
  else if(brain.phase==ShadowAwareness.Phase.CHASE&&p!=null){
   if(f_19797_%10==0)m_21573_().m_26519_(p.m_20185_(),p.m_20186_(),p.m_20189_(),rare()?1.25:1.05);facePlayer(p);
   if(m_20280_(p)<=3.6&&m_142582_(p)){brain.attack();m_21573_().m_26573_();}
  }else if(brain.phase==ShadowAwareness.Phase.ATTACK){m_21573_().m_26573_();if(!valid){brain.reset();}else if(brain.ticks>=12){if(m_20280_(p)<=6.25&&m_142582_(p))ShadowWorldService.ambush(p,this);brain.reset();}}
  if(TartarusDungeonService.inside(this)&&(room(m_20185_())!=room(homeX)||room(m_20189_())!=room(homeZ))){m_6034_(homeX,m_20186_(),homeZ);m_21573_().m_26573_();brain.reset();}
  int mood=brain.phase.ordinal();if(mood!=mood()){boolean question=brain.phase==ShadowAwareness.Phase.NOTICE||brain.phase==ShadowAwareness.Phase.SEARCH;boolean danger=brain.phase==ShadowAwareness.Phase.CHASE||brain.phase==ShadowAwareness.Phase.ATTACK;m_6593_(net.minecraft.network.chat.Component.m_237113_(question?"§e§l?":danger?"§c§l!":"Shadow"));m_20340_(question||danger);}f_19804_.m_135381_(MOOD,mood);
 }
 public void resumeExploration(){target=null;brain.reset();wanderDelay=20;f_19804_.m_135381_(MOOD,0);m_20340_(false);m_6593_(net.minecraft.network.chat.Component.m_237113_("Shadow"));}
 private void turn(float yaw){float delta=(yaw-m_146908_())%360;if(delta>180)delta-=360;if(delta< -180)delta+=360;face(m_146908_()+Math.max(-6,Math.min(6,delta)));}
 private void facePlayer(ServerPlayer p){turn((float)Math.toDegrees(Math.atan2(m_20185_()-p.m_20185_(),p.m_20189_()-m_20189_())));}
 private void wander(){
  if(--wanderDelay>0)return;wanderDelay=45+m_217043_().m_188503_(65);
  boolean dungeon=TartarusDungeonService.inside(this);int origin=dungeon?TartarusInteriorLayout.origin(TartarusDungeonService.floor(this)):0;
  double cx=dungeon?origin+room(homeX-origin)*24-36:homeX,cz=dungeon?room(homeZ)*24-36:homeZ;
  for(int i=0;i<12;i++){
   double x=cx+m_217043_().m_188500_()*16-8,z=cz+m_217043_().m_188500_()*16-8,y=m_20186_();
   var box=m_20191_().m_82386_(x-m_20185_(),0,z-m_20189_());
   if(!m_9236_().m_45756_(this,box)||m_9236_().m_45756_(this,box.m_82386_(0,-.7,0)))continue;
   if(m_21573_().m_26519_(x,y,z,.65))return;
  }
 }
 @Override public AnimatableInstanceCache getAnimatableInstanceCache(){return cache;}
 @Override public void registerControllers(AnimatableManager.ControllerRegistrar c){c.add(new AnimationController<FieldShadowEntity>(this,"main",2,s->s.setAndContinue(mood()==ShadowAwareness.Phase.ATTACK.ordinal()?RawAnimation.begin().thenPlay("action.attack"):RawAnimation.begin().thenLoop("misc.idle"))));}
}
