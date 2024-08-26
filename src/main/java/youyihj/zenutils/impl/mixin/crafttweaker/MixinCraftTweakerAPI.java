package youyihj.zenutils.impl.mixin.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.preprocessor.PreprocessorManager;
import crafttweaker.runtime.ITweaker;
import crafttweaker.zenscript.GlobalRegistry;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import stanhebben.zenscript.expression.partial.PartialType;
import youyihj.zenutils.api.preprocessor.*;
import youyihj.zenutils.api.util.ZenUtilsGlobal;
import youyihj.zenutils.impl.core.Configuration;
import youyihj.zenutils.impl.runtime.ZenUtilsTweaker;
import youyihj.zenutils.impl.zenscript.mixin.ZenTypeMixinCallbackInfo;
import youyihj.zenutils.impl.zenscript.mixin.ZenTypeMixinCallbackInfoReturnable;
import youyihj.zenutils.impl.zenscript.nat.PartialJavaNativeClassOrPackage;

import java.lang.reflect.Method;

/**
 * @author youyihj
 */
@Mixin(value = CraftTweakerAPI.class, remap = false)
public abstract class MixinCraftTweakerAPI {
    @Mutable
    @Shadow
    @Final
    public static ITweaker tweaker;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void redirectTweaker(CallbackInfo ci) {
        tweaker = new ZenUtilsTweaker(tweaker);
        zu$registerGlobalMethods();
        GlobalRegistry.getRoot().put("native", pos -> new PartialJavaNativeClassOrPackage(pos, ""), null);
        if (Configuration.enableMixin) {
            GlobalRegistry.getRoot().put("mixin.CallbackInfo", pos -> new PartialType(pos, ZenTypeMixinCallbackInfo.INSTANCE), null);
            GlobalRegistry.getRoot().put("mixin.CallbackInfoReturnable", pos -> new PartialType(pos, ZenTypeMixinCallbackInfoReturnable.INSTANCE), null);
        }
        PreprocessorManager preprocessorManager = tweaker.getPreprocessorManager();
        preprocessorManager.registerPreprocessorAction(SuppressErrorPreprocessor.NAME, SuppressErrorPreprocessor::new);
        preprocessorManager.registerPreprocessorAction(NoFixRecipeBookPreprocessor.NAME, NoFixRecipeBookPreprocessor::new);
        preprocessorManager.registerPreprocessorAction(HardFailPreprocessor.NAME, HardFailPreprocessor::new);
        preprocessorManager.registerPreprocessorAction(ReloadablePreprocessor.NAME, ReloadablePreprocessor::new);
        preprocessorManager.registerPreprocessorAction(NotReloadablePreprocessor.NAME, NotReloadablePreprocessor::new);
        preprocessorManager.registerPreprocessorAction(DownloadReobfMappingPreprocessor.NAME, DownloadReobfMappingPreprocessor::new);
    }

    @Unique
    private static void zu$registerGlobalMethods() {
        for (Method method : ZenUtilsGlobal.class.getDeclaredMethods()) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            String name = method.getName();
            // skip typeof for primitive types
            if (name.equals("typeof") && parameterTypes[0].isPrimitive())
                continue;
            GlobalRegistry.registerGlobal(name, CraftTweakerAPI.getJavaStaticMethodSymbol(ZenUtilsGlobal.class, name, parameterTypes));
        }
    }
}
