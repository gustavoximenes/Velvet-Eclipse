package com.gx.velveteclipse.localization;

/** Pure display catalogue, also usable by offline layout renderers. Never used for game rules. */
public final class DisplayText {
    private static final TextCatalog EN=new TextCatalog(TextCatalog.read("/assets/velvet_eclipse/localization/en_us.json"));
    private static final TextCatalog PT=new TextCatalog(TextCatalog.read("/assets/velvet_eclipse/localization/pt_br.json"));
    private static volatile boolean portuguese;
    public static void language(String code){portuguese="pt_br".equals(code);}
    public static boolean portuguese(){return portuguese;}
    public static String text(String source){return (portuguese?PT:EN).translate(source);}
    private DisplayText(){}
}
