package com.gx.velveteclipse.core.persona;

/** Explains the mechanics used by this mod instead of exposing data-table templates. */
public final class SkillDescriptions {
 public static String describe(SkillDefinition s){
  var i=ReloadCatalogue.info(s.id());if(i==null)return "";
  String d=term(i.detail()),v=Integer.toString((int)i.value());
  if(s.isPassive())return "Passiva: "+switch(i.effect()){
   case "FMTResistElem"->"reduz pela metade o dano de "+d+".";
   case "FMTNullElem"->"anula dano de "+d+".";
   case "FMTRepelElem"->"reflete dano de "+d+" ao atacante.";
   case "FMTDrainElem"->"converte dano de "+d+" em recuperação de HP.";
   case "FMTResistAilment"->"reduz pela metade a chance de sofrer "+d+".";
   case "FMTNullAilment"->"impede "+d+".";
   case "FMTDodgeElem"->"aumenta a esquiva de "+d+" em "+(int)(i.value()-1100)+" pontos, respeitando o limite combinado.";
   case "FMTPersonaCounterN"->v+"% de chance de refletir ataques físicos. Vale o maior nível de Counter.";
   case "FMTRegenerateN"->"recupera "+v+"% do HP máximo no início de cada turno.";
   case "FMTInvigorateN"->"recupera "+v+" SP no início de cada turno.";
   case "FMTElemBoost"->"aumenta "+d+" em "+(int)(i.value()-1100)+"%.";
   case "FMTAilmentBoost"->s.id().equals("hama_boost")||s.id().equals("mudo_boost")?"multiplica por 1,5 a chance de "+d+".":"aumenta a chance de causar "+d+" em "+(s.id().equals("ailment_boost")?10:15)+" pontos.";
   case "FMTAutoSkill"->s.id().startsWith("anti_")?"ganha resistência ao elemento indicado, reduzindo seu dano pela metade nos três primeiros turnos.":"ativa "+d+" no início da batalha, por três turnos.";
   case "FMTTimes"->switch(s.id()){case "apt_pupil"->"dobra a chance de crítico, até o limite de 60%.";case "sharp_student"->"reduz pela metade a chance de receber crítico.";case "ali_dance"->"reduz em 50% a chance de ser acertado.";case "spell_master"->"reduz pela metade o custo em SP das skills (mínimo 1).";case "arms_master"->"reduz pela metade o custo em HP das skills.";default->"reduz "+d+" pela metade.";};
   case "FMTEndure"->"sobrevive a um golpe fatal com 1 HP, uma vez por batalha.";
   case "FMTEnduringSoul"->"sobrevive a um golpe fatal com HP cheio, uma vez por batalha.";
   case "FMTLifeAidN"->"recupera "+v+"% do HP e SP máximos após uma vitória, se estiver vivo.";
   case "FMTHealBoost"->"aumenta em 50% a cura de HP das skills.";
   case "FMTGrowthN"->"recebe "+v+"% da experiência de batalha mesmo sem ser a Persona ativa.";
   case "$2 x$1 but cannot dodge"->"reduz pela metade o dano recebido e impede esquivar.";
   case "$1% chance to endure $2"->v+"% de chance de sobreviver a "+d+" com 1 HP, uma vez por batalha.";
   case "$2 recovery time x$1"->"estados negativos perdem dois turnos de duração por turno.";
   case "$2 recovery time reduced to $1 turn"->"remove estados negativos no próximo turno.";
   case "FMTPlus","$2 +$1%, overwrites Crit Rate Boost"->"acrescenta "+v+" pontos à chance de crítico; vale o maior bônus da família.";
   case "$1 $2 to ally when Shifting to them"->"o aliado que recebe seu Shift recupera "+v+" SP.";
   case "$1 $2 when receiving Shift"->"recupera "+v+" SP ao receber Shift.";
   case "dmg dealt x$1 $2"->s.id().equals("raging_tiger")?"aumenta o dano em 50% enquanto está em fúria.":"o multiplicador de dano ao receber Shift passa a "+(s.id().equals("shift_amp")?"2":"1,75")+"x.";
   default->throw new IllegalStateException("Missing passive description: "+s.id());
  };
  if(s.isDamageSkill()){
   if(i.detail().equals("Instakill")||i.effect().equals("FMTInstakillWhen"))return "Chance base de "+v+"% de morte instantânea"+(i.effect().equals("FMTInstakillWhen")?" contra alvos com medo":" de "+d.replace("morte instantânea",""))+". Chefes especiais são imunes.";
   if(i.effect().equals("FMTFracDamage"))return "Remove "+v+"% do HP atual do alvo; respeita afinidades. Chefes especiais são imunes.";
   if(i.effect().equals("FMTPersonaLifeDrainN"))return "Absorve até "+v+" "+(s.id().equals("spirit_drain")?"SP":"HP")+" do alvo e recupera a mesma quantidade.";
   String text="Dano de "+term(s.element().name())+" em "+target(s.target())+".";
   if(i.maxHits()>1)text+=" Acerta de "+i.minHits()+" a "+i.maxHits()+" vezes.";
   if(i.effect().equals("FMTExact")&&!i.detail().equals("-"))text+=" Chance base de "+v+"% de causar "+d+".";
   if(i.effect().equals("dmg dealt x$1 $2"))text+=" Dano aumentado "+d+".";
   if(i.effect().contains("ignores resistances"))text+=" Ignora resistências.";
   if(s.id().equals("armageddon"))text="Causa 9999 de dano onipotente e deixa o usuário com 1 HP e 1 SP.";
   return text;
  }
  return switch(i.effect()){
   case "FMTPersonaKaja"->(s.id().endsWith("kaja")?"Aumenta ":"Reduz ")+d+" de "+target(s.target())+" por três turnos. Efeitos opostos se cancelam.";
   case "FMTElemCharge"->"Multiplica por 2,3 o próximo ataque "+d+" do usuário.";
   case "Next $2 attack x$1 dmg and crit up"->"Fortalece o próximo ataque físico em 2,3x e aumenta a chance de crítico em 15 pontos.";
   case "FMTElemKarn"->"Reflete o próximo ataque "+d+" recebido; não afeta Onipotente.";
   case "FMTElemBreak"->"Remove resistência, anulação, reflexão e absorção de "+d+" por três turnos. Preserva fraquezas.";
   case "$2 +$1% for 3 turns"->"Aumenta a chance de crítico em "+v+" pontos por três turnos"+(i.universal()?" de todos, incluindo inimigos":" do aliado")+".";
   case "FMTFoulBreathN"->"Aumenta em 50% a vulnerabilidade a estados negativos por três turnos"+(i.universal()?" de todos, incluindo aliados":" do inimigo")+".";
   case "FMTCureAilment"->s.id().equals("dekaja")?"Remove bônus de ataque, defesa e precisão dos inimigos.":s.id().equals("dekunda")?"Remove penalidades de ataque, defesa e precisão dos aliados.":"Cura "+d+" de "+target(s.target())+".";
   case "FMTRecarm"->"Revive um aliado com "+v+"% do HP máximo.";
   case "$1% $2, user's HP reduced to 1"->"Recupera todo o HP dos aliados e reduz o HP do usuário a 1.";
   case "$1% $2 + Sukukaja"->"Recupera 50% do HP dos aliados e aumenta precisão e evasão por três turnos.";
   case "FMTExact"->i.detail().equals("HP restore")?"Recupera "+v+"% do HP de "+target(s.target())+".":"Chance base de "+v+"% de causar "+d+" em "+target(s.target())+".";
   case "FMTBase"->switch(s.id()){case "heat_riser"->"Aumenta ataque, defesa, precisão e evasão de um aliado por três turnos.";case "debilitate"->"Reduz ataque, defesa, precisão e evasão de um inimigo por três turnos.";case "best_friends"->"Ativa Charge e Concentrate em um aliado.";case "salvation"->"Recupera todo o HP e cura estados negativos dos aliados.";case "amrita_shower","amrita_drop"->"Cura todos os estados negativos de "+target(s.target())+".";default->"Recupera "+s.power()+" HP de "+target(s.target())+".";};
   default->throw new IllegalStateException("Missing active description: "+s.id());
  };
 }
 private static String target(SkillTarget t){return switch(t){case ONE_ENEMY->"um inimigo";case ALL_ENEMIES->"todos os inimigos";case SELF->"si mesmo";case ONE_ALLY->"um aliado";case ALL_ALLIES->"todos os aliados";};}
 private static String term(String text){String s=text.toLowerCase(java.util.Locale.ROOT);String[][] words={{"hp + sp restore","HP e SP"},{"hp restore","HP"},{"sp restore","SP"},{"non-almighty magic","magia, exceto onipotente"},{"single-target skill","skills de alvo único"},{"multi-target skill","skills de múltiplos alvos"},{"life drain + spirit drain","absorção de HP e SP"},{"all ailments except dizzy","estados negativos"},{"light instakill","morte instantânea de luz"},{"dark instakill","morte instantânea de trevas"},{"instakill","morte instantânea"},{"physical","impacto"},{"strike","impacto"},{"slash","corte"},{"pierce","perfuração"},{"fire","fogo"},{"ice","gelo"},{"electric","elétrico"},{"elec","elétrico"},{"wind","vento"},{"light","luz"},{"dark","trevas"},{"almighty","onipotente"},{"nuclear","nuclear"},{"psy","psíquico"},{"sleep","sono"},{"poison","veneno"},{"charm","encanto"},{"confuse","confusão"},{"distress","aflição"},{"fear","medo"},{"rage","fúria"},{"freeze","congelamento"},{"shock","choque"},{"dizzy","tontura"},{"on downed foes","contra alvos caídos"},{"during full moon","durante a lua cheia"},{"during new moon","durante a lua nova"},{"attack","ataque"},{"defense","defesa"},{"accuracy/evasion","precisão e evasão"},{"magic","mágico"},{"phys","físico"},{"ailment","estado negativo"},{"down","queda"}};for(var pair:words)s=s.replace(pair[0],pair[1]);return s;}
 private SkillDescriptions(){}
}
