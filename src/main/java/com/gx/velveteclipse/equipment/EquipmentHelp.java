package com.gx.velveteclipse.equipment;
import java.util.*;
/** Describes the effects actually applied by this mod, including its stat scale. */
public final class EquipmentHelp {
 public static String describe(String text){if(text==null||text.isBlank())return "Sem habilidade adicional. O valor principal altera ataque, defesa ou esquiva conforme a categoria.";return String.join(" ",Arrays.stream(text.split("[,/]")).map(String::trim).map(EquipmentHelp::one).toList());}
 private static String term(String s){return s.replace("all except Almighty","todos os elementos, exceto Onipotente").replace("all ailments","todos os estados negativos").replace("instakill","morte instantânea").replace("Random ailment","estado negativo aleatório").replace("Physical","físico").replace("physical","físico").replace("Magic","magia").replace("magic","mágico").replace("phys","físico").replace("Electricity","eletricidade").replace("Electric","eletricidade").replace("Elec","eletricidade").replace("Fire","fogo").replace("Ice","gelo").replace("Wind","vento").replace("Light","luz").replace("Dark","trevas").replace("Pierce","perfurante").replace("Slash","cortante").replace("Strike","concussão").replace("Poison","veneno").replace("Charm","encanto").replace("Confuse","confusão").replace("Fear","medo").replace("Rage","fúria").replace("Distress","aflição").replace("Dizzy","tontura").replace("Shock","choque").replace("Freeze","congelamento").replace("Ailments","estados negativos");}
 private static String one(String original){String s=original.startsWith("+")?original.substring(1):original;String tier=s.contains("(high)")?"30":s.contains("(med)")?"20":"10";String plain=s.replaceAll(" \\(.*\\)","");
  if(s.contains("evasion"))return "Acrescenta "+tier+"% de chance de esquivar de dano "+term(plain.replace(" evasion",""))+".";
  if(s.startsWith("Reduce "))return "Reduz o dano recebido de "+term(plain.substring(7).replace(" dmg",""))+" em "+tier+"%.";
  if(s.startsWith("Null "))return "Impede "+term(s.substring(5))+".";
  if(s.startsWith("Resist "))return "Reduz a chance de sofrer "+term(s.substring(7))+".";
  if(s.endsWith(" attack"))return "Muda o dano do ataque básico para "+term(s.replace(" attack",""))+".";
  if(s.contains("Critical rate up"))return "Acrescenta "+tier+" pontos percentuais à chance de crítico.";
  if(s.contains("("))return "O ataque básico tem "+tier+"% de chance de causar "+term(plain)+".";
  if(s.matches("Regenerate [123]"))return "Recupera "+(Integer.parseInt(s.substring(s.length()-1))*2)+"% do HP máximo no começo de cada turno.";
  if(s.matches("Invigorate [123]"))return "Recupera "+(Integer.parseInt(s.substring(s.length()-1))*2+1)+" SP no começo de cada turno.";
  if(s.endsWith(" Amp")&&!s.startsWith("Shift"))return "Aumenta dano de "+term(s.replace(" Amp",""))+" em 50%.";
  if(s.endsWith(" Boost")&&!s.startsWith("Shift")){String name=s.replace(" Boost","");return Set.of("Confuse","Distress","Dizzy","Fear","Freeze","Shock").contains(name)?"Acrescenta 15 pontos percentuais à chance de causar "+term(name)+".":"Aumenta dano de "+term(name)+" em 25%.";}
  if(s.endsWith(" HP")||s.endsWith(" SP")||s.endsWith("HP and SP"))return "Aumenta o máximo de "+s+" enquanto equipado.";
  if(s.contains("EXP"))return "Aumenta a experiência recebida em "+s+".";
  if(s.contains(" +"))return "Aumenta o atributo: "+term(s.replace("Strength","Força").replace("Endurance","Resistência").replace("Agility","Agilidade").replace("Luck","Sorte").replace("All Stats","todos os atributos"))+".";
  return switch(s){
   case "Spell Master"->"Reduz pela metade o custo em SP das habilidades (mínimo de 1 SP).";
   case "Arms Master"->"Reduz pela metade o custo em HP das habilidades físicas.";
   case "Magic Ability"->"Aumenta em 25% o dano das habilidades que usam Magia.";
   case "Apt Pupil"->"Dobra a chance calculada de crítico, respeitando o limite de 60%.";
   case "Sharp Student"->"Reduz pela metade a chance de receber um crítico.";
   case "Ali Dance"->"Acrescenta 35% de esquiva, respeitando o limite combinado.";
   case "Firm Stance"->"Reduz pela metade o dano recebido, mas impede esquivar.";
   case "Counter"->"10% de chance de refletir um ataque físico.";
   case "Counterstrike"->"15% de chance de refletir um ataque físico.";
   case "High Counter"->"20% de chance de refletir um ataque físico.";
   case "Endure","Survival Trick"->"Sobrevive a um golpe fatal com 1 HP, uma vez por batalha.";
   case "Endure Light"->"Sobrevive a um golpe fatal de luz com 1 HP, uma vez por batalha.";
   case "Endure Dark"->"Sobrevive a um golpe fatal de trevas com 1 HP, uma vez por batalha.";
   case "Enduring Soul"->"Sobrevive a um golpe fatal e recupera todo o HP, uma vez por batalha.";
   case "Fast Heal","Faster overheat recovery"->"Estados negativos perdem dois turnos de duração a cada turno.";
   case "Insta-Heal"->"Remove estados negativos no começo do próximo turno.";
   case "Unshaken Will"->"Impede confusão, medo, fúria, encanto e aflição.";
   case "Auto Charge"->"Começa a batalha com o próximo ataque físico fortalecido.";
   case "Auto Concentrate"->"Começa a batalha com a próxima magia fortalecida.";
   case "Auto Tarukaja"->"Aumenta o ataque por três turnos no começo da batalha.";
   case "Auto Rakukaja"->"Aumenta a defesa por três turnos no começo da batalha.";
   case "Auto Sukukaja"->"Aumenta a esquiva no começo da batalha.";
   case "Auto Heat Riser"->"Começa com ataque, defesa e esquiva aumentados.";
   case "Auto Mataru"->"Aumenta o ataque de todos os aliados no começo da batalha.";
   case "Auto Maraku"->"Aumenta a defesa de todos os aliados no começo da batalha.";
   case "Auto Masuku"->"Aumenta a esquiva dos aliados por três turnos.";
   case "Auto Rebellion"->"Acrescenta 12 pontos à chance de crítico nos turnos iniciais.";
   case "Auto Tarukabellion"->"Começa com ataque e chance de crítico aumentados.";
   case "Anti-Electric Master","Anti-Fire Master","Anti-Ice Master","Anti-Wind Master"->"Acrescenta 20% de esquiva contra o elemento indicado.";
   case "Shift Boost"->"Ao receber Shift, o multiplicador de dano passa a 1,75x.";
   case "Shift Amp"->"Ao receber Shift, o multiplicador de dano passa a 2x.";
   case "Soul Link"->"Recupera 10 SP ao receber Shift.";
   case "Soul Chain"->"Recupera 5 SP ao receber Shift.";
   case "Soul Shift"->"Recupera 10% do HP máximo ao receber Shift.";
   case "Dmg on downed enemy"->"Aumenta em 30% o dano contra inimigos derrubados.";
   case "All-Out Attack dmg up"->"Aumenta em 30% o dano do All-Out Attack.";
   case "Attack when enraged"->"Durante fúria, o ataque recebe multiplicador de 2,5x.";
   case "Ignore enemy resistances"->"O ataque básico ignora resistência, mas respeita anulação, reflexão e absorção.";
   case "Orgia duration +1 turn"->"Começa com ataque e defesa aumentados por quatro turnos.";
   default->term(s)+".";
  };
 }
}
