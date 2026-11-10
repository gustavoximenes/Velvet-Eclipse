package com.gx.velveteclipse.roguelike;
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
import static com.gx.velveteclipse.localization.ModText.component;

public final class ArcanaItems {
 private static final DeferredRegister<Item> ITEMS=DeferredRegister.create(ForgeRegistries.ITEMS,"velvet_eclipse");
 public static final Map<String,RegistryObject<Item>> CARDS=new LinkedHashMap<>();
 public static final Map<String,RegistryObject<Item>> UPS=new LinkedHashMap<>();
 static{for(var c:RogueCatalog.CARDS)CARDS.put(c.id(),ITEMS.register("arcana_buff_"+c.id(),()->new ArcanaItem(c.id())));
  for(var c:ArcanaUpCatalog.CARDS)UPS.put(c.id(),ITEMS.register("arcana_up_"+c.id(),()->new ArcanaItem(c.key())));}
 public static RegistryObject<Item> entry(String id){var up=ArcanaUpCatalog.offered(id);return up==null?CARDS.get(id):UPS.get(up.id());}
 public static void register(IEventBus bus){ITEMS.register(bus);}
 public static String id(ItemStack stack){return !stack.m_41619_()&&stack.m_41720_() instanceof ArcanaItem a?a.id:"";}
 public static final class ArcanaItem extends Item {
  final String id;
  ArcanaItem(String id){super(new Item.Properties().m_41487_(64));this.id=id;}
  @Override public Component m_7626_(ItemStack stack){var up=ArcanaUpCatalog.offered(id);return component(up==null?"Arcana Buff · "+RogueCatalog.card(id).name():"Arcana Up · "+up.name());}
  @Override public void m_7373_(ItemStack stack,Level level,List<Component> lines,TooltipFlag flag){
   var up=ArcanaUpCatalog.offered(id);if(up!=null){
    lines.add(component("Raises this Arcana by 1 rank, up to rank 10."));
    lines.add(component("Each rank grants 10% of the next-level EXP when fusing a Persona of this Arcana."));
    lines.add(component("Right-click to review and confirm. No card is consumed at max rank."));
    lines.add(component("Requires Roguelike mode. Ranks persist after death."));return;
   }
   var c=RogueCatalog.card(id);lines.add(component(c.arcana()));lines.add(component(c.benefit()));
   if(c.pact())lines.add(component("Trade-off: "+c.drawback()));
   lines.add(component("Right-click, then confirm to consume one card and activate its buff."));
   lines.add(component("Requires Roguelike mode. Duplicate buffs do not stack."));
  }
  @Override public InteractionResultHolder<ItemStack> m_7203_(Level level,Player player,InteractionHand hand){if(player instanceof ServerPlayer p)RogueService.offer(p,hand);return InteractionResultHolder.m_19092_(player.m_21120_(hand),level.f_46443_);}
  @Override public InteractionResult m_6225_(UseOnContext c){if(c.m_43723_() instanceof ServerPlayer p)RogueService.offer(p,c.m_43724_());return InteractionResult.SUCCESS;}
 }
}
