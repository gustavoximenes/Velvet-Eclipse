package com.gx.velveteclipse.client.render;
import com.gx.velveteclipse.VelvetEclipseMod;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import software.bernie.geckolib.cache.object.GeoBone;
import org.joml.Vector3f;
import org.joml.Vector4f;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class ThanatosMesh {
    private static volatile BoneMeshData data;
    private static volatile boolean attempted;
    private static final ResourceLocation RESOURCE=new ResourceLocation("velvet_eclipse","meshes/entity/thanatos.mesh.json");
    private static BoneMeshData get() {
        if(!attempted) {
            attempted=true;
            try(var reader=new InputStreamReader(Minecraft.m_91087_().m_91098_().m_213713_(RESOURCE).orElseThrow().m_215507_(),StandardCharsets.UTF_8)) {
                data=BoneMeshData.read(reader);
            } catch(Exception e){VelvetEclipseMod.LOGGER.error("Thanatos mesh unavailable; using Gecko geometry fallback",e);}
        }
        return data;
    }
    public static boolean draw(PoseStack stack,GeoBone bone,VertexConsumer out,int light,int overlay,float r,float g,float b,float a) {
        BoneMeshData mesh=get();if(mesh==null)return false;
        if(bone.isHidden())return true;
        float[] v=mesh.vertices(bone.getName());if(v==null)return false;
        var pose=stack.m_85850_();var position=new Vector4f();var normal=new Vector3f();
        for(int i=0;i<v.length;i+=8) {
            position.set(v[i],v[i+1],v[i+2],1).mul(pose.m_252922_());
            normal.set(v[i+5],v[i+6],v[i+7]).mul(pose.m_252943_()).normalize();
            out.m_5954_(position.x,position.y,position.z,r,g,b,a,v[i+3],v[i+4],overlay,light,normal.x,normal.y,normal.z);
        }
        return true;
    }
    @SubscribeEvent public static void reload(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new ResourceManagerReloadListener(){
            public void m_6213_(ResourceManager manager){data=null;attempted=false;}
        });
    }
}
