package youyihj.zenutils.impl.zenscript.nat;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import youyihj.zenutils.impl.member.ClassData;
import youyihj.zenutils.impl.member.ExecutableData;
import youyihj.zenutils.impl.util.InternalUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author youyihj
 */
public enum CraftTweakerBridge {
    INSTANCE;

    private final Map<String, ExecutableData> toNativeCasters = new HashMap<>();
    private final Map<String, ExecutableData> toWrapperCasters = new HashMap<>();

    private LoaderState loaderState;

    CraftTweakerBridge() {
        refresh();
    }

    private void refresh() {
        loaderState = Loader.instance().getLoaderState();
        toNativeCasters.clear();
        toWrapperCasters.clear();
        try {
            for (ExecutableData method : InternalUtils.getClassDataFetcher().forName("crafttweaker.api.minecraft.CraftTweakerMC").methods(true)) {
                if (method.name().startsWith("get") && method.parameterCount() == 1) {
                    ClassData toConvert = method.parameters().get(0).asClassData();
                    String toConvertClassName = toConvert.name();
                    if (toConvertClassName.startsWith("crafttweaker.")) {
                        toNativeCasters.put(toConvert.name(), method);
                    } else if (toConvertClassName.startsWith("net.minecraft")) {
                        toWrapperCasters.put(toConvert.name(), method);
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<ExecutableData> getNativeCaster(final ClassData clazz) {
        if (loaderState != Loader.instance().getLoaderState()) {
            refresh();
        }
        return Optional.ofNullable(toNativeCasters.get(clazz.name()));
    }

    public Optional<ExecutableData> getWrapperCaster(final ClassData clazz) {
        if (loaderState != Loader.instance().getLoaderState()) {
            refresh();
        }
        return Optional.ofNullable(toWrapperCasters.get(clazz.name()));
    }

}
