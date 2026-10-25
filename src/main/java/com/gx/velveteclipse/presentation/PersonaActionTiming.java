package com.gx.velveteclipse.presentation;

import com.google.gson.JsonParser;
import com.gx.velveteclipse.core.persona.PersonaRegistry;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Authoritative invocation clock. Clip lengths come from the same packaged resources as GeckoLib. */
public final class PersonaActionTiming {
    public static final int START = 55, TRANSITION = 3, TRACKING_GRACE = 4, RESULT_TAIL = 40;
    public record Plan(int animationTicks) {
        public Plan { if (animationTicks < 1 || animationTicks > 200) throw new IllegalArgumentException("Persona clip length"); }
        public int animationEnd() { return START + TRANSITION + animationTicks; }
        public int impact() { return animationEnd() + TRACKING_GRACE + 1; }
        public int finish() { return impact() + RESULT_TAIL; }
        public boolean attacking(int tick) { return tick >= START && tick < impact(); }
    }
    private static final Map<String, Plan> CACHE = new ConcurrentHashMap<>();
    public static boolean magic(String skill) { return PersonaRegistry.skill(skill).map(s -> s.usesMagicStat()).orElse(true); }
    public static Plan forAction(String persona, String skill) { return forClip(persona, magic(skill)); }
    public static Plan forClip(String persona, boolean magic) {
        String asset = switch (persona) { case "cinder_wisp" -> "thanatos"; case "umbra_slime" -> "wealth_hand"; default -> persona; };
        if (!asset.matches("[a-z0-9_]+")) throw new IllegalArgumentException("Persona asset ID");
        String clip = magic ? "action.magic" : "action.physical";
        return CACHE.computeIfAbsent(asset + ":" + clip, key -> {
            String path = "/assets/velvet_eclipse/animations/entity/" + asset + ".animation.json";
            try (var in = PersonaActionTiming.class.getResourceAsStream(path)) {
                if (in == null) throw new IllegalStateException("Missing Persona animations: " + asset);
                var clips = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("animations");
                double seconds = clips.getAsJsonObject(clip).get("animation_length").getAsDouble();
                if (!Double.isFinite(seconds) || seconds <= 0 || seconds > 10) throw new IllegalStateException("Invalid Persona duration: " + key);
                return new Plan((int)Math.ceil(seconds * 20 - 1e-6));
            } catch (java.io.IOException e) { throw new IllegalStateException(path, e); }
        });
    }
    public static int impactFromDuration(int duration) { return duration - RESULT_TAIL; }
    private PersonaActionTiming() {}
}
