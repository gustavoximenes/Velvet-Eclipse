package com.gx.velveteclipse.client.vfx;

import com.google.gson.*;
import com.gx.velveteclipse.network.BattleLightningS2C.Strike;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

/** Approved elemental study 02. One finite instance at each confirmed impact, with frozen target identity. */
public final class ElementalPhoton {
    private record Live(PhotonElementalInstance effect, long start, int cost) {}
    private static final List<Live> live = new ArrayList<>();
    private static final Map<String, JsonObject> recipes = new LinkedHashMap<>();
    private static final Map<String, String> bindings = new LinkedHashMap<>();
    private static net.minecraft.client.multiplayer.ClientLevel level;
    static {
        try (var r = new InputStreamReader(Objects.requireNonNull(ElementalPhoton.class.getResourceAsStream("/data/velvet_eclipse/presentation/elemental-effects.json")), StandardCharsets.UTF_8)) {
            var data = JsonParser.parseReader(r).getAsJsonObject();
            for (var entry : data.getAsJsonArray("recipes")) { var p = entry.getAsJsonObject(); recipes.put(p.get("id").getAsString(), p); }
            data.getAsJsonObject("bindings").entrySet().forEach(e -> bindings.put(e.getKey(), e.getValue().getAsString()));
        } catch (java.io.IOException e) { throw new IllegalStateException("Elemental effects", e); }
    }
    public static boolean supports(String skill) { return bindings.containsKey(skill); }
    public static void registerModels(Consumer<ResourceLocation> register) {
        var array = new JsonArray(); recipes.values().forEach(array::add); PhotonElementalInstance.registerModels(array, register);
    }
    public static int cost() { return live.stream().mapToInt(Live::cost).sum(); }
    public static void reset() { live.forEach(x -> x.effect.close()); live.clear(); level = null; }
    public static void tick() {
        var world = Minecraft.m_91087_().f_91073_; if (world != level) { reset(); return; } if (world == null) return;
        for (var it = live.iterator(); it.hasNext();) {
            var x = it.next(); int age = (int)(world.m_46467_() - x.start);
            if (age < 0 || age > 80) { x.effect.close(); it.remove(); continue; }
            x.effect.tick(age); if (x.effect.done()) { x.effect.close(); it.remove(); }
        }
    }
    public static void batch(String skill, List<Strike> targets) {
        int budget = Math.min(64, Math.max(1, (256 - cost() - ReloadPhoton.legacyCost()) / Math.max(1, targets.size())));
        for (var t : targets) play(skill, new ImpactAnchor(t), budget);
    }
    public static void play(String skill, ImpactAnchor anchor, int budget) {
        var world = Minecraft.m_91087_().f_91073_; if (world == null || !supports(skill)) return;
        if (level != world) { reset(); level = world; }
        budget = Math.min(budget, 256 - cost() - ReloadPhoton.legacyCost()); if (budget < 1) return;
        var recipe = recipes.get(bindings.get(skill)).deepCopy(); var parts = recipe.getAsJsonArray("parts");
        while (parts.size() > budget) parts.remove(parts.size() - 1);
        var at = anchor.position(1);
        var effect = new PhotonElementalInstance(recipe, () -> world, new Vector3f((float)at.f_82479_, (float)at.f_82480_, (float)at.f_82481_));
        effect.tick(0); live.add(new Live(effect, world.m_46467_(), parts.size()));
    }
    private ElementalPhoton() {}
}
