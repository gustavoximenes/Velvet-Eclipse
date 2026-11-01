package com.gx.velveteclipse.client.vfx;

import com.google.gson.*;
import com.lowdragmc.photon.client.gameobject.emitter.particle.ParticleEmitter;
import com.lowdragmc.photon.client.gameobject.emitter.data.shape.Cone;
import com.lowdragmc.photon.client.gameobject.particle.TileParticle;
import net.minecraft.util.RandomSource;
import org.joml.Vector3f;
import java.nio.file.*;

/** Exercises real Photon particle setup/ticks, without Minecraft, a window or an OpenGL context. */
public final class ElementalLifecycleAudit {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args)throws Exception {
        var root=Path.of("src/main/resources");
        var doc=JsonParser.parseString(Files.readString(root.resolve("data/velvet_eclipse/presentation/elemental-effects.json"))).getAsJsonObject();
        int parts=0;
        for(var recipe:doc.getAsJsonArray("recipes"))for(var entry:recipe.getAsJsonObject().getAsJsonArray("parts")){
            var p=entry.getAsJsonObject();var emitter=new ParticleEmitter();
            PhotonElementalInstance.configureMotion(emitter.config,p);
            emitter.updatePos(new Vector3f(124,65,-300));
            if(parts==0){
                var broken=new ParticleEmitter();PhotonElementalInstance.configureMotion(broken.config,p);
                var cone=new Cone();cone.setRadius(0);cone.setAngle(0);broken.config.shape.setShape(cone);
                var old=new TileParticle(broken,broken.config,RandomSource.m_216335_(42));
                old.tick();check(!old.getWorldPos().isFinite(),"reproduce Photon 1.1.17 zero-radius Cone NaN regression");
            }
            var particle=new TileParticle(emitter,emitter.config,RandomSource.m_216335_(42));
            int life=p.get("life").getAsInt();int visible=0;
            for(int tick=0;tick<life;tick++){
                check(particle.getWorldPos().isFinite(),"finite particle coordinates: "+recipe.getAsJsonObject().get("id")+" / "+p.get("model"));
                check(particle.getRealSize(0).isFinite(),"finite particle size");
                check(particle.getRealColor(0).isFinite(),"finite particle color");
                if(particle.isAlive()&&particle.getRealColor(0).w>0&&particle.getRealSize(0).lengthSquared()>0)visible++;
                particle.tick();
            }
            check(visible>0,"part survives with nonzero alpha/size");
            for(int tick=0;tick<3;tick++)particle.tick();
            check(!particle.isAlive(),"particle expires");
            check(emitter.config.emission.getEmissionCount(0,0,RandomSource.m_216335_(42))==1,"exactly one burst");
            check(emitter.config.emission.getEmissionCount(1,0,RandomSource.m_216335_(42))==0,"no repeated burst");
            parts++;
        }
        var atlas=JsonParser.parseString(Files.readString(root.resolve("assets/minecraft/atlases/blocks.json"))).getAsJsonObject();
        boolean registered=false;for(var s:atlas.getAsJsonArray("sources")){
            var o=s.getAsJsonObject();if(o.has("resource")&&o.get("resource").getAsString().equals("velvet_eclipse:vfx/audit02/white"))registered=true;
        }
        check(registered,"OBJ white sprite is stitched into the block atlas");
        check(Files.exists(root.resolve("assets/velvet_eclipse/textures/vfx/audit02/white.png")),"atlas texture exists");
        System.out.println("PASS Photon particle lifecycle: "+checks+" checks; "+parts+" native particles; old Cone NaN reproduced, finite motion and lifetime, atlas source. No Minecraft execution.");
    }
}
