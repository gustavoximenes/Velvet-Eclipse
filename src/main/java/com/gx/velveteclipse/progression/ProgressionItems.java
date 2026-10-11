package com.gx.velveteclipse.progression;
import com.gx.velveteclipse.room.VelvetRoomRegistry;
import com.gx.velveteclipse.forge.*;
import com.gx.velveteclipse.equipment.ShopProgress;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.registries.RegistryObject;
import java.util.*;
public final class ProgressionItems {
 public static final Map<String,RegistryObject<Item>> ITEMS=new LinkedHashMap<>();
 static {for(int floor=25;floor<=250;floor+=25){int f=floor;String id=RewardLedger.permit(f);ITEMS.put(id,VelvetRoomRegistry.ITEMS.register(id,()->new ProgressionItem(f)));}
  ITEMS.put("igor_contract",VelvetRoomRegistry.ITEMS.register("igor_contract",()->new ProgressionItem(0)));
 }
 public static void init(){}
 private static final class ProgressionItem extends Item {
  final int floor;
  ProgressionItem(int floor){super(new Item.Properties().m_41487_(1));this.floor=floor;}
  @Override public void m_7373_(ItemStack stack,Level level,List<Component> lines,TooltipFlag flags){
   lines.add(com.gx.velveteclipse.localization.ModText.component(floor==0?"Use para ampliar em 1 o limite de Personas (2 → 8).":"Use para registrar a permissão deste andar na loja de Elizabeth."));
   lines.add(com.gx.velveteclipse.localization.ModText.component(floor==0?"Recompensa única da primeira vitória contra a Sacerdotisa.":"As permissões anteriores são necessárias. O progresso é pessoal."));
  }
  private boolean use(Player player,ItemStack stack){
   if(!(player instanceof ServerPlayer p))return true;
   if(!p.m_6084_()||BattleService.current(p)!=null||com.gx.velveteclipse.exploration.ExplorationService.blocksBattleStart(p))return false;
   String message;boolean changed;
   if(floor==0){var d=PlayerPersonaData.load(p);changed=d.expandPersonaCapacity();if(changed){d.save(p);BattleService.sync(p,"Contrato de Igor utilizado.");}message=changed?"Contrato de Igor: limite ampliado para "+d.personaCapacity()+" Personas.":"Seu limite já é de 8 Personas. O contrato foi preservado.";}
   else {int before=ShopProgress.permits(p);message=ShopProgress.use(p,floor);changed=before!=ShopProgress.permits(p);}
   if(changed)stack.m_41774_(1);p.m_5661_(com.gx.velveteclipse.localization.ModText.component(message),false);return changed;
  }
  @Override public InteractionResultHolder<ItemStack> m_7203_(Level level,Player player,InteractionHand hand){var stack=player.m_21120_(hand);return use(player,stack)?InteractionResultHolder.m_19090_(stack):InteractionResultHolder.m_19098_(stack);}
  @Override public InteractionResult m_6225_(UseOnContext c){return c.m_43723_()!=null&&use(c.m_43723_(),c.m_43722_())?InteractionResult.SUCCESS:InteractionResult.FAIL;}
 }
}
