package com.gx.velveteclipse.city;

import com.gx.velveteclipse.room.VelvetRoomRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import java.util.*;

@Mod.EventBusSubscriber(modid="velvet_eclipse")
public final class CityShopActors {
    public static final Map<String,RegistryObject<EntityType<CityShopActor>>> TYPES=new LinkedHashMap<>();
    static {
        for(var point:CityShopAccess.all()){
            String id="city_shop_"+point.shop()+"_actor";
            var type=VelvetRoomRegistry.ENTITIES.register(id,()->EntityType.Builder.<CityShopActor>m_20704_((t,l)->new CityShopActor(t,l,point.shop()),MobCategory.MISC)
                .m_20699_(point.width(),point.height()).m_20702_(8).m_20717_(3).m_20712_("velvet_eclipse:"+id));
            TYPES.put(point.shop(),type);
            if(point.npc())VelvetRoomRegistry.ITEMS.register("city_"+point.shop()+"_model",()->new DisplayItem(point.shop()));
        }
    }
    public static void init(){}
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent event){
        if(event.phase!=TickEvent.Phase.END)return;
        var server=net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();if(server==null)return;
        var city=server.m_129880_(CityService.DIMENSION);var state=CityState.get(server);
        if(city==null||state.choice!=1||!state.ready||city.m_46467_()%40!=0)return;
        for(var point:CityShopAccess.all()){
            if(server.m_6846_().m_11314_().stream().noneMatch(p->CityService.inside(p)&&point.distanceSquared(p.m_20185_(),p.m_20186_(),p.m_20189_())<4096))continue;
            var at=BlockPos.m_274561_(point.x(),point.y(),point.z());
            if(!city.m_143319_(new ChunkPos(at).m_45588_()))continue;
            var existing=city.m_8791_(point.uuid());
            if(existing instanceof CityShopActor actor){
                if(actor.managed()&&actor.shop().equals(point.shop())&&point.distanceSquared(actor.m_20185_(),actor.m_20186_(),actor.m_20189_())>.001){actor.m_6034_(point.x(),point.y(),point.z());actor.m_146922_(point.yaw());}
                continue;
            }
            var npc=TYPES.get(point.shop()).get().m_20615_(city);if(npc==null)continue;
            npc.m_20084_(point.uuid());npc.manage();npc.m_6034_(point.x(),point.y(),point.z());npc.m_146922_(point.yaw());city.m_7967_(npc);
        }
    }
    private static final class DisplayItem extends Item {
        private final String shop;
        DisplayItem(String shop){super(new Item.Properties().m_41487_(1));this.shop=shop;}
        @Override public InteractionResult m_6225_(UseOnContext context){
            var player=context.m_43723_();var level=context.m_43725_();
            if(player==null||com.gx.velveteclipse.forge.BattleService.current(player)!=null||SceneryProtection.protectedDimension(level.m_46472_()))return InteractionResult.FAIL;
            var point=CityShopAccess.get(shop);var at=context.m_8083_().m_121945_(context.m_43719_());double x=at.m_123341_()+.5,y=at.m_123342_(),z=at.m_123343_()+.5;
            if(!level.m_45756_(null,new AABB(x-point.width()/2,y,z-point.width()/2,x+point.width()/2,y+point.height(),z+point.width()/2)))return InteractionResult.FAIL;
            if(!level.f_46443_){var npc=TYPES.get(shop).get().m_20615_(level);if(npc==null)return InteractionResult.FAIL;npc.m_6034_(x,y,z);npc.m_146922_(player.m_146908_()+180);if(!level.m_7967_(npc))return InteractionResult.FAIL;if(!player.m_150110_().f_35937_)context.m_43722_().m_41774_(1);}
            return InteractionResult.SUCCESS;
        }
    }
    private CityShopActors(){}
}
