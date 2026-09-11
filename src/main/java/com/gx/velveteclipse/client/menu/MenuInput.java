package com.gx.velveteclipse.client.menu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import com.gx.velveteclipse.client.ClientBattleState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.settings.KeyConflictContext;

public final class MenuInput {
    public static final KeyMapping OPEN=new KeyMapping("key.velvet_eclipse.open_menu",KeyConflictContext.IN_GAME,InputConstants.Type.KEYSYM,258,"key.categories.velvet_eclipse");
    @Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static class Registration {
        @SubscribeEvent public static void register(RegisterKeyMappingsEvent e){e.register(OPEN);}
    }
    @Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.FORGE,value=Dist.CLIENT)
    public static class Input {
        @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
            if(e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.m_91087_();
            while(OPEN.m_90859_())if(mc.f_91074_!=null&&mc.f_91080_==null&&!com.gx.velveteclipse.client.exploration.ExplorationClient.blocksMenu()&&!ClientBattleState.snapshot().active())mc.m_91152_(new PersonaMenuScreen());
        }
    }
}
