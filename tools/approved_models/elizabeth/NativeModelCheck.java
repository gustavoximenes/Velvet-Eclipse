import java.nio.file.*;
import com.google.gson.*;
import software.bernie.geckolib.util.JsonUtil;
import software.bernie.geckolib.loading.json.raw.Model;
import software.bernie.geckolib.loading.object.*;

/** Bake standalone draft without copying anything into mod resources. */
class NativeModelCheck {
    public static void main(String[] args)throws Exception {
        Path root=Path.of(args[0]);
        String raw=Files.readString(root.resolve("elizabeth.geo.json"));
        var source=JsonParser.parseString(raw).getAsJsonObject().getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
        var model=JsonUtil.GEO_GSON.fromJson(raw,Model.class);
        var baked=BakedModelFactory.DEFAULT_FACTORY.constructGeoModel(GeometryTree.fromModel(model));
        int cubes=0,bones=0;
        for(var row:source.getAsJsonArray("bones")){
            var bone=row.getAsJsonObject();String name=bone.get("name").getAsString();
            var nativeBone=baked.getBone(name).orElseThrow();bones++;
            int expected=bone.has("cubes")?bone.getAsJsonArray("cubes").size():0;
            if(nativeBone.getCubes().size()!=expected)throw new AssertionError(name+" cubes");
            cubes+=expected;
        }
        var animationJson=JsonParser.parseString(Files.readString(root.resolve("elizabeth.animation.json"))).getAsJsonObject().getAsJsonObject("animations");
        var clips=JsonUtil.GEO_GSON.fromJson(animationJson,BakedAnimations.class);
        if(clips.getAnimation("animation.elizabeth.ground_idle")==null)throw new AssertionError("idle");
        String report="{\"status\":\"passed\",\"native_geckolib_bones\":"+bones+",\"native_geckolib_cuboids\":"+cubes+",\"native_animation_loaded\":true,\"runtime_axis_conversion_applied\":false,\"integrated\":false}";
        Files.writeString(root.resolve("native-validation.json"),report+"\n");
        System.out.println(report);
    }
}
