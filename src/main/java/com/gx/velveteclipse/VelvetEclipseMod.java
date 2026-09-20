package com.gx.velveteclipse;

import com.gx.velveteclipse.core.persona.PersonaRegistry;
import com.gx.velveteclipse.network.ModNetwork;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import software.bernie.geckolib.GeckoLib;

@Mod("velvet_eclipse")
public final class VelvetEclipseMod {
   public static final String MOD_ID = "velvet_eclipse";
   public static final Logger LOGGER = LogUtils.getLogger();

   public VelvetEclipseMod() {
      GeckoLib.initialize();
      com.gx.velveteclipse.darkhour.DarkHourRegistry.register(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus());
      com.gx.velveteclipse.room.VelvetRoomRegistry.register(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus());
      PersonaRegistry.bootstrap();
      com.gx.velveteclipse.shuffle.SkillCardRegistry.register(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus());
      ModNetwork.register();
      LOGGER.info("Velvet Eclipse 0.6.19 guardian recovery, transition floors and creative props initialized.");
   }
}
