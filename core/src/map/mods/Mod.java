package map.mods;

/**
 * Created by Miv on 9/28/2017.
 */
public enum Mod {
    BUBBLY(Bubbly.class),
    SHRINKING_MAP(ShrinkingMap.class),
    EXPLOSIVE(Explosive.class),
    BULKY(Bulky.class),
    SPEEDY(Speedy.class),
    HOMING_BULLETS(HomingBullets.class),
    FRACTURE(Fracture.class),
    WINDY(Windy.class),
    OVERSIZED(Oversized.class),
    STRONG(Strong.class),
    AGGRESSIVE(Aggressive.class);

    private Class<? extends MapAreaModifier> impl;
    Mod(Class<? extends MapAreaModifier> impl) {
        this.impl = impl;
    }

    public Class<? extends MapAreaModifier> getImpl() {
        return impl;
    }
}
