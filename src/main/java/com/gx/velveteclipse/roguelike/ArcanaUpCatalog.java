package com.gx.velveteclipse.roguelike;

import com.gx.velveteclipse.core.persona.Arcana;
import java.util.*;

/** One rank card for every Arcana supported by the mod, including World and Aeon. */
public final class ArcanaUpCatalog {
    public static final int MAX_RANK = 10;
    public record Card(Arcana arcana, String id, String name, int number) {
        public String key() { return "up:" + id; }
    }
    public static final List<Card> CARDS = Arrays.stream(Arcana.values()).map(a -> {
        String id = a == Arcana.HANGED ? "hanged_man" : a.name().toLowerCase(Locale.ROOT);
        String name = switch(a) {
            case HANGED -> "Hanged Man";
            default -> a.name().charAt(0) + a.name().substring(1).toLowerCase(Locale.ROOT);
        };
        return new Card(a, id, name, a == Arcana.AEON ? 20 : a.ordinal());
    }).toList();
    public static Card card(String id) { return CARDS.stream().filter(c -> c.id().equals(id)).findFirst().orElse(null); }
    public static Card offered(String key) { return key != null && key.startsWith("up:") ? card(key.substring(3)) : null; }
    public static Card of(Arcana arcana) { return CARDS.stream().filter(c -> c.arcana() == arcana).findFirst().orElseThrow(); }
    public static Map<String,Integer> clean(Map<String,Integer> ranks) {
        var out = new LinkedHashMap<String,Integer>();
        if(ranks != null)for(var c : CARDS) {
            var value = ranks.get(c.id());
            if(value != null && value > 0)out.put(c.id(), Math.min(MAX_RANK, value));
        }
        return out;
    }
    private ArcanaUpCatalog() {}
}
