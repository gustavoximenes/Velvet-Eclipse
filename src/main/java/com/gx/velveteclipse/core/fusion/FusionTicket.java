package com.gx.velveteclipse.core.fusion;
import com.gx.velveteclipse.forge.PlayerPersonaData;
import java.util.UUID;
/** A single-use server preview binds inputs, roster state, player level and expiry. */
public final class FusionTicket {
 public final String token=UUID.randomUUID().toString(),first,second;
 private final String fingerprint;private final long expires;private boolean used;
 public FusionTicket(PlayerPersonaData data,String a,String b,long now){first=a;second=b;fingerprint=FusionTransaction.fingerprint(data);expires=now+6000;}
 public boolean confirm(PlayerPersonaData data,String received,long now){
  if(used||!token.equals(received)||now>expires)return false;
  used=true;if(!fingerprint.equals(FusionTransaction.fingerprint(data)))return false;
  return FusionTransaction.commit(data,first,second,null);
 }
}
