package com.gx.velveteclipse.shuffle;
import com.gx.velveteclipse.core.persona.PersonaRegistry;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraftforge.registries.*;
import net.minecraftforge.eventbus.api.IEventBus;
import java.util.*;
public final class SkillCardRegistry {
 private static final DeferredRegister<Item> ITEMS=DeferredRegister.create(ForgeRegistries.ITEMS,"velvet_eclipse");
 public static final Map<String,RegistryObject<Item>> CARDS=new LinkedHashMap<>();
 static{for(String skill:PersonaRegistry.skills().keySet())CARDS.put(skill,ITEMS.register("skill_card_"+skill,()->new SkillCard(skill)));}
 // Retain old inventory stacks. They now teach Agi and are hidden from the creative catalogue.
 static{CARDS.put("ember",ITEMS.register("skill_card_ember",()->new SkillCard("agi")));}
 public static void register(IEventBus bus){ITEMS.register(bus);}
 public static final class SkillCard extends Item {
  private final String skill;
  SkillCard(String skill){super(new Item.Properties().m_41487_(64));this.skill=skill;}
  @Override public Component m_7626_(ItemStack stack){return Component.m_237113_("Skill Card • "+PersonaRegistry.skill(skill).orElseThrow().displayName());}
  @Override public void m_7373_(ItemStack stack,Level level,List<Component> lines,TooltipFlag flags){String description=com.gx.velveteclipse.core.persona.SkillDescriptions.describe(PersonaRegistry.skill(skill).orElseThrow());if(!description.isBlank()){for(String line:description.split("(?<=\\.) "))lines.add(Component.m_237113_(line));}lines.add(Component.m_237113_("Usar: escolher a Persona que aprenderá a habilidade."));}
  @Override public InteractionResultHolder<ItemStack> m_7203_(Level level,Player player,InteractionHand hand){if(player instanceof ServerPlayer p)ShuffleService.openSkill(p,hand,skill);return InteractionResultHolder.m_19092_(player.m_21120_(hand),level.f_46443_);}
  @Override public InteractionResult m_6225_(UseOnContext c){if(c.m_43723_() instanceof ServerPlayer p)ShuffleService.openSkill(p,c.m_43724_(),skill);return InteractionResult.SUCCESS;}
 }
}
