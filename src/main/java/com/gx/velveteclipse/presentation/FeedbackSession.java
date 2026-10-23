package com.gx.velveteclipse.presentation;
import java.util.UUID;
/** Ordered reliable channel gate: session changes and duplicates cannot replay feedback. */
public final class FeedbackSession {
 private UUID session;private long last;
 public void start(UUID id){if(!id.equals(session)){session=id;last=Long.MIN_VALUE;}}
 public boolean accept(UUID id,long event){if(session==null||!session.equals(id)||event<=last)return false;last=event;return true;}
 public UUID id(){return session;}
 public void clear(){session=null;last=Long.MIN_VALUE;}
}
