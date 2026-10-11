package com.gx.velveteclipse.localization;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Presentation-only adapter for the existing bilingual content and dynamic sentence fragments. */
public final class TextCatalog {
    private final Map<String,String> phrases;
    private final Map<Character,List<Map.Entry<String,String>>> fragments=new HashMap<>();
    private final Map<String,String> cache=Collections.synchronizedMap(new LinkedHashMap<>(256,.75f,true){
        @Override protected boolean removeEldestEntry(Map.Entry<String,String> e){return size()>4096;}
    });
    public TextCatalog(Map<String,String> entries){
        phrases=Map.copyOf(entries);
        entries.entrySet().stream().filter(e->!e.getKey().isEmpty())
            .sorted(Comparator.<Map.Entry<String,String>>comparingInt(e->e.getKey().length()).reversed())
            .forEach(e->fragments.computeIfAbsent(e.getKey().charAt(0),k->new ArrayList<>()).add(e));
    }
    public String translate(String source){
        if(source==null||source.isEmpty())return source;
        String exact=phrases.get(source);if(exact!=null)return exact;
        String cached=cache.get(source);if(cached!=null)return cached;
        StringBuilder out=new StringBuilder();
        for(int i=0;i<source.length();){
            Map.Entry<String,String> match=null;
            for(var entry:fragments.getOrDefault(source.charAt(i),List.of())){
                String key=entry.getKey();int end=i+key.length();
                if(source.startsWith(key,i)&&boundary(source,i,key.charAt(0),true)&&boundary(source,end,key.charAt(key.length()-1),false)){match=entry;break;}
            }
            if(match==null)out.append(source.charAt(i++));else{out.append(match.getValue());i+=match.getKey().length();}
        }
        String result=out.toString();cache.put(source,result);return result;
    }
    private static boolean boundary(String s,int i,char edge,boolean start){
        return !Character.isLetterOrDigit(edge)||(start?(i==0||!word(s.charAt(i-1))):(i==s.length()||!word(s.charAt(i))));
    }
    private static boolean word(char c){return Character.isLetterOrDigit(c)||c=='_';}
    public static Map<String,String> read(String path){
        try(var in=TextCatalog.class.getResourceAsStream(path)){
            if(in==null)throw new IllegalStateException("Missing language resource: "+path);
            return new Gson().fromJson(new InputStreamReader(in,StandardCharsets.UTF_8),new TypeToken<LinkedHashMap<String,String>>(){}.getType());
        }catch(IOException e){throw new UncheckedIOException(e);}
    }
}
