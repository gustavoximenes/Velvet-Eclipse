package com.gx.velveteclipse.client.equipment;
public final class EquipmentText {
 public static String effect(String s){if(s==null||s.isBlank())return "Sem efeito adicional.";return s.replace("Orgia duration +1 turn","Postura inicial: ataque e defesa por 4 turnos").replace("Faster overheat recovery","Recuperação acelerada de estados negativos").replace("Strength","Força").replace("Magic","Magia").replace("Endurance","Resistência").replace("Agility","Agilidade").replace("Luck","Sorte").replace("All Stats","Todos os atributos").replace("Ignore enemy resistances","Ignora resistências inimigas").replace("attack","ataque").replace("Reduce","Reduz").replace("evasion","esquiva").replace("dmg","dano").replace("high","alto").replace("med","médio").replace("low","baixo");}
}
