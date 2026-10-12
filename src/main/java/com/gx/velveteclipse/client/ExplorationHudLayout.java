package com.gx.velveteclipse.client;

/** One physical-size policy for all exploration cards, independent of Minecraft GUI scale. */
public record ExplorationHudLayout(double scale,double width,double height,double bottom) {
    public static ExplorationHudLayout of(int guiWidth,int guiHeight){
        double scale=Math.max(.1,Math.min(guiWidth/960.0,guiHeight/540.0));
        // The vanilla hotbar, armour, hunger and XP use GUI units, not our design units.
        return new ExplorationHudLayout(scale,guiWidth/scale,guiHeight/scale,(guiHeight-52)/scale);
    }
    public double vitalsY(){return bottom-43;}
    public double mapX(){return width-107;}
    public double shuffleX(){return width-145;}
}
