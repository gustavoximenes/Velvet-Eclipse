package com.gx.velveteclipse.forge;

import com.gx.velveteclipse.room.VelvetRoomRegistry;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

/** Creative display figures use the same approved rig and idle as summoned Personas. */
public final class PersonaModelItems {
    static {
        var ids=new java.util.LinkedHashSet<>(com.gx.velveteclipse.core.persona.ReloadCatalogue.ids());ids.add("arsene");
        for(String id:ids)VelvetRoomRegistry.ITEMS.register(id+"_model",()->new Item(new Item.Properties()){
            @Override public InteractionResult m_6225_(UseOnContext c){
                var player=c.m_43723_();var level=c.m_43725_();
                if(player==null||BattleService.current(player)!=null)return InteractionResult.FAIL;
                var at=c.m_8083_().m_121945_(c.m_43719_());
                var bounds=new net.minecraft.world.phys.AABB(at.m_123341_()+.05,at.m_123342_(),at.m_123343_()+.05,at.m_123341_()+.95,at.m_123342_()+2.2,at.m_123343_()+.95);
                if(!level.m_45756_(null,bounds))return InteractionResult.FAIL;
                if(!level.f_46443_){
                    var type=PersonaMobRegistry.all().get(id);if(type==null)return InteractionResult.FAIL;
                    var entity=type.m_20615_(level);if(entity==null)return InteractionResult.FAIL;
                    PersonaMobRegistry.configurePersonaMob(entity,id);
                    entity.m_6034_(at.m_123341_()+.5,at.m_123342_(),at.m_123343_()+.5);
                    entity.m_146922_(player.m_146908_()+180);
                    entity.m_20049_("velvet_persona_figure");
                    if(!level.m_7967_(entity))return InteractionResult.FAIL;
                    if(!player.m_150110_().f_35937_)c.m_43722_().m_41774_(1);
                }
                return InteractionResult.SUCCESS;
            }
        });
    }
    public static void init(){}
}
