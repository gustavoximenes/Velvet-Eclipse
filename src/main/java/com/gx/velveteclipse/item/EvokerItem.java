package com.gx.velveteclipse.item;

import net.minecraft.world.item.Item;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import java.util.function.Consumer;

/** The awakening trigger remains server-owned; only the prop renderer is customized. */
public final class EvokerItem extends Item {
    public EvokerItem(Properties properties){super(properties);}
    @Override public void initializeClient(Consumer<IClientItemExtensions> consumer){
        consumer.accept(new IClientItemExtensions(){
            private com.gx.velveteclipse.client.EvokerItemRenderer renderer;
            @Override public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer(){
                if(renderer==null)renderer=new com.gx.velveteclipse.client.EvokerItemRenderer();return renderer;
            }
        });
    }
}
