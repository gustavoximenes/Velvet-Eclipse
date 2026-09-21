package com.gx.velveteclipse.party;
import java.util.*;
/** Server-owned membership. Invitations never imply consent to join. */
public final class PartyRoster {
 public static final int LIMIT=8;
 public record Group(String id,List<String> members){}
 public record Invite(String group,String from,long expires){}
 public Map<String,Group> groups=new LinkedHashMap<>();
 private transient Map<String,Invite> invitations=new HashMap<>();
 public Group group(String player){return groups.values().stream().filter(g->g.members().contains(player)).findFirst().orElse(null);}
 public boolean same(String a,String b){var g=group(a);return !a.equals(b)&&g!=null&&g.members().contains(b);}
 public String create(String player){if(group(player)!=null)return "Você já está em uma party.";String id=UUID.randomUUID().toString();groups.put(id,new Group(id,new ArrayList<>(List.of(player))));return "Party criada. Convide jogadores na aba CONVIDAR.";}
 public String invite(String from,String to,long now){var g=group(from);if(g==null||!g.members().get(0).equals(from))return "Somente o líder pode convidar.";if(from.equals(to)||group(to)!=null)return "Esse jogador já possui uma party.";if(g.members().size()>=LIMIT)return "A party está cheia.";invitations.put(to,new Invite(g.id(),from,now+120000));return "Convite enviado (válido por 2 minutos).";}
 public Invite invitation(String player,long now){var i=invitations.get(player);return i!=null&&i.expires()>now&&groups.containsKey(i.group())?i:null;}
 public String accept(String player,long now){var i=invitation(player,now);if(i==null)return "O convite expirou.";var g=groups.get(i.group());if(group(player)!=null||g.members().size()>=LIMIT)return "Não foi possível entrar nesta party.";g.members().add(player);invitations.remove(player);return "Você entrou na party.";}
 public void decline(String player){invitations.remove(player);}
 public String leave(String player){var g=group(player);if(g==null)return "Você não está em uma party.";g.members().remove(player);if(g.members().isEmpty())groups.remove(g.id());return "Você saiu da party. O próximo integrante assume a liderança.";}
 public String kick(String leader,String target){var g=group(leader);if(g==null||!g.members().get(0).equals(leader)||leader.equals(target)||!g.members().contains(target))return "Somente o líder pode remover outro integrante.";g.members().remove(target);return "Integrante removido.";}
 public void sanitize(){if(groups==null)groups=new LinkedHashMap<>();invitations=new HashMap<>();var seen=new HashSet<String>();groups.entrySet().removeIf(e->{var g=e.getValue();if(g==null||g.members()==null||g.members().isEmpty())return true;g.members().removeIf(id->!seen.add(id));return g.members().isEmpty();});}
}
