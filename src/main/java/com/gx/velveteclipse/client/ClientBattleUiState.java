package com.gx.velveteclipse.client;
import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.network.*;
import java.util.List;
public final class ClientBattleUiState {
 private static Page page=Page.MAIN;
 private static boolean actionLocked,switchPending;
 private static int selected;
 private static String pendingSkill="";
 private static long opened;
 private static double lastMouseX=-1,lastMouseY=-1;
 public enum Page { MAIN, SKILLS, PERSONAS, ITEMS, TARGET }
 public static Page page(){return page;}
 public static int selected(){return selected;}
 public static java.util.List<String> items(){var a=new java.util.ArrayList<String>();if(ClientBattleState.snapshot().pulseTonics()>0)a.add("pulse_tonic");a.addAll(com.gx.velveteclipse.client.equipment.EquipmentScreen.bag().supplies.keySet());return a;}
 private static java.util.List<String> options(BattleSnapshotS2C s){return page==Page.ITEMS?items():s.skills();}
 public static double age(){return (System.nanoTime()-opened)*1e-9;}
 public static boolean selector(){return page==Page.SKILLS||page==Page.PERSONAS||page==Page.ITEMS;}
 public static boolean actionLocked(){return BattleResultsClient.active()||AllOutPresentation.active()||BattleTacticsClient.awaitingAllOut()||actionLocked||switchPending||PersonaCinematics.skillSequenceActive()||PersonaCinematics.meleeActive();}
 public static void onSnapshot(BattleSnapshotS2C old,BattleSnapshotS2C next){
  if(next==null||!next.active()){page=Page.MAIN;actionLocked=switchPending=false;selected=0;pendingSkill="";return;}
  if(old==null||old.playerTurn()!=next.playerTurn()){page=Page.MAIN;pendingSkill="";}
  // Target selection belongs to the client whose turn is active.  Closing a
  // stale selector here prevents every participant in a shared battle from
  // retaining its own copy of the enemy HUD after the turn moves on.
  if(!next.playerTurn()&&page==Page.TARGET){page=Page.MAIN;pendingSkill="";selected=0;}
  if(next.playerTurn()){actionLocked=false;switchPending=false;}
  selected=Math.min(selected,Math.max(0,options(next).size()-1));
 }
 public static void open(Page next){if(!actionLocked()){page=next==Page.PERSONAS?Page.SKILLS:next==null?Page.MAIN:next;opened=System.nanoTime();pendingSkill="";selected=0;lastMouseX=lastMouseY=-1;}}
 public static void back(){if(com.gx.velveteclipse.client.equipment.ItemHelpHud.active()){com.gx.velveteclipse.client.equipment.ItemHelpHud.close();return;}if(page==Page.TARGET&&!pendingSkill.isBlank()&&!pendingSkill.equals("__baton_pass")){page=pendingSkill.startsWith("__item:")?Page.ITEMS:Page.SKILLS;opened=System.nanoTime();}else page=Page.MAIN;pendingSkill="";}
 private static boolean canAct(BattleSnapshotS2C s){return s!=null&&s.active()&&s.playerTurn()&&!actionLocked()&&!BattleTacticsClient.prompt();}
 public static com.gx.velveteclipse.equipment.EquipmentBonuses bonuses(BattleSnapshotS2C s){return com.gx.velveteclipse.client.equipment.EquipmentScreen.bag().bonuses().withSkills(s.passives());}
 public static boolean affordable(SkillDefinition skill,BattleSnapshotS2C s){return skill!=null&&!skill.isPassive()&&(skill.spCost()>0&&bonuses(s).has("spell master")?Math.max(1,skill.spCost()/2):skill.spCost())<=s.sp()&&(skill.hpPercentCost()==0||s.hp()>Math.max(1,s.maxHp()*(bonuses(s).has("arms master")?Math.max(1,skill.hpPercentCost()/2):skill.hpPercentCost())/100));}
 public static boolean selectIndex(int index,BattleSnapshotS2C s){
  if(!canAct(s)||com.gx.velveteclipse.client.equipment.ItemHelpHud.active())return false;
  if(page==Page.TARGET)return confirm(s);
  if(!selector()||index<0||index>=options(s).size())return false;
  if(page==Page.ITEMS){pendingSkill="__item:"+items().get(index);page=Page.TARGET;BattleTargetHud.chooseDefault();return true;}
  selected=index;String id=s.skills().get(index);
  if(!affordable(PersonaRegistry.skill(id).orElse(null),s))return false;
  var skill=PersonaRegistry.skill(id).orElseThrow();
  pendingSkill=id;page=Page.TARGET;BattleTargetHud.chooseDefault();return true;
 }
 public static boolean selectVisible(int row,BattleSnapshotS2C s){return selectIndex(selected/7*7+row,s);}
 public static void attack(){var s=ClientBattleState.snapshot();if(canAct(s)){pendingSkill="";page=Page.TARGET;BattleTargetHud.chooseDefault();}}
 public static void baton(){var s=ClientBattleState.snapshot();if(canAct(s)&&BattleTacticsClient.canPass()){pendingSkill="__baton_pass";page=Page.TARGET;BattleTargetHud.chooseDefault();}}
 public static boolean confirm(BattleSnapshotS2C s){if(!canAct(s))return false;if(selector())return selectIndex(selected,s);if(page!=Page.TARGET||BattleTargetHud.selectedId()==null)return false;send(pendingSkill.startsWith("__item:")?"item":pendingSkill.equals("__baton_pass")?"baton_pass":pendingSkill.isEmpty()?"attack":"skill",pendingSkill.startsWith("__item:")?pendingSkill.substring(7):pendingSkill);return true;}
 public static void move(int delta){if(page==Page.TARGET&&!actionLocked()){BattleTargetHud.move(delta);return;}var s=ClientBattleState.snapshot();if(selector()&&canAct(s)&&!options(s).isEmpty())selected=Math.floorMod(selected+delta,options(s).size());}
 public static void cycle(int direction){if(page==Page.ITEMS){move(direction);return;}if(page==Page.TARGET){move(direction);return;}var s=ClientBattleState.snapshot();if(!selector()||!canAct(s)||BattleTacticsClient.oneMore()||s.roster().size()<2)return;
  int i=s.roster().indexOf(s.personaId());String id=s.roster().get(Math.floorMod(i+direction,s.roster().size()));switchPending=true;selected=0;ModNetwork.sendAction("persona",id);
 }
 public static void hover(double x,double y,int w,int h){
  if(page==Page.TARGET&&!actionLocked()){BattleTargetHud.hover(x,y);return;}
  if(!selector()||actionLocked())return;
  if(Math.abs(x-lastMouseX)+Math.abs(y-lastMouseY)<.2)return;lastMouseX=x;lastMouseY=y;
  var s=ClientBattleState.snapshot();int first=selected/7*7;
  for(int row=0;row<Math.min(7,options(s).size()-first);row++)if(PersonaBattleDesign.row(w,h,row,age()).contains(x,y)){selected=first+row;break;}
 }
 public static String pendingSkill(){return pendingSkill;}
 public static Element selectedElement(){
  String id=PersonaCinematics.skillSequenceActive()?PersonaCinematics.activeSkill():page==Page.TARGET?pendingSkill:selector()&&selected<ClientBattleState.snapshot().skills().size()?ClientBattleState.snapshot().skills().get(selected):"";
  return id!=null&&id.startsWith("__item:")?com.gx.velveteclipse.equipment.DamageItems.element(id.substring(7)):"__baton_pass".equals(id)?Element.SUPPORT:PersonaRegistry.skill(id==null?"":id).map(SkillDefinition::element).orElse(com.gx.velveteclipse.client.equipment.EquipmentScreen.bag().bonuses().element());
 }
 public static boolean click(double x,double y,int w,int h,BattleSnapshotS2C s){
  if(!canAct(s)||com.gx.velveteclipse.client.equipment.ItemHelpHud.active())return false;
  if(page==Page.TARGET){if(BattleTargetHud.click(x,y)||y>h-28)return confirm(s);return false;}
  if(selector()){
   if(PersonaBattleDesign.previous(w,h,age()).contains(x,y)){cycle(-1);return true;}
   if(PersonaBattleDesign.next(w,h,age()).contains(x,y)){cycle(1);return true;}
   int first=selected/7*7;
   for(int row=0;row<Math.min(7,options(s).size()-first);row++)if(PersonaBattleDesign.row(w,h,row,age()).contains(x,y))return selectIndex(first+row,s);
   if(PersonaBattleDesign.footer(w,h).contains(x,y)){back();return true;}return false;
  }
  var rects=BattleMenuLayout.main(w,h);for(int i=0;i<rects.length;i++)if(rects[i].contains(x,y)){
   switch(i){case 0->attack();case 1,2->open(Page.SKILLS);case 3->open(Page.ITEMS);case 4->send("guard","");case 5->send("escape","");}return true;
  }return false;
 }
 public static String displaySkill(String id){return PersonaRegistry.skill(id==null?"":id).map(SkillDefinition::displayName).orElse(id==null?"":id);}
 public static String displayPersona(String id){return PersonaRegistry.persona(id).map(PersonaDefinition::displayName).orElse(id==null?"":id);}
 public static void escape(){if(canAct(ClientBattleState.snapshot()))send("escape","");}
 private static void send(String type,String id){var target=page==Page.TARGET?BattleTargetHud.selectedId():null;actionLocked=true;page=Page.MAIN;ModNetwork.sendAction(type,id,target);}
}
