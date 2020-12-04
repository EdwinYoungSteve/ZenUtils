package youyihj.zenutils.util;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.api.data.DataMap;
import crafttweaker.api.data.IData;

/**
 * @author youyihj
 */
public final class InternalUtils {
    private InternalUtils() {}

    public static void checkDataMap(IData data) {
        if (!(data instanceof DataMap)) {
            CraftTweakerAPI.logError("data argument must be DataMap", new IllegalArgumentException());
        }
    }

    public static boolean isValidHashKey(Class<?> clazz) {
        try {
            clazz.getDeclaredMethod("hashCode");
            clazz.getDeclaredMethod("equals", Object.class);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
