package com.gx.velveteclipse.localization;

import java.io.IOException;
import java.nio.file.*;
import java.util.Set;

/** One client profile, never a world-wide or server-wide option. */
public final class LanguagePreference {
    public static boolean valid(String code){return Set.of("en_us","pt_br").contains(code);}
    public static String read(Path file){
        try{String value=Files.readString(file).trim();return valid(value)?value:"en_us";}
        catch(IOException e){return "en_us";}
    }
    public static void save(Path file,String code)throws IOException{
        if(!valid(code))throw new IllegalArgumentException("Unsupported language");
        Path temp=file.resolveSibling(file.getFileName()+".tmp");Files.createDirectories(file.getParent());
        Files.writeString(temp,code+"\n");
        try{Files.move(temp,file,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}
        catch(AtomicMoveNotSupportedException e){Files.move(temp,file,StandardCopyOption.REPLACE_EXISTING);}
    }
    private LanguagePreference(){}
}
