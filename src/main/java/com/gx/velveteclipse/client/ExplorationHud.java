package com.gx.velveteclipse.client;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import com.gx.velveteclipse.client.menu.MenuCanvas;
public final class ExplorationHud {
    public static boolean visible(){var m=Minecraft.m_91087_();return m.f_91074_!=null&&m.f_91080_==null&&!ClientBattleState.snapshot().active()&&!AllOutPresentation.active()&&!BattleResultsClient.active()&&!com.gx.velveteclipse.client.darkhour.DarkHourClient.clock()&&!com.gx.velveteclipse.client.exploration.ExplorationClient.entry();}
    public static void draw(GuiGraphics g,java.util.function.BiConsumer<MenuCanvas,ExplorationHudLayout> paint){
        var m=Minecraft.m_91087_();var l=ExplorationHudLayout.of(m.m_91268_().m_85445_(),m.m_91268_().m_85446_());var p=g.m_280168_();g.m_280262_();p.m_85836_();p.m_85841_((float)l.scale(),(float)l.scale(),1);
        try{g.m_286007_(() -> paint.accept(new MenuCanvas(g),l));}finally{g.m_280262_();p.m_85849_();}
    }
    private ExplorationHud(){}
}
