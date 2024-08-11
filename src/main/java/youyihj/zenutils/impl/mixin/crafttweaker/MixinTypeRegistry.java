package youyihj.zenutils.impl.mixin.crafttweaker;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import stanhebben.zenscript.compiler.ITypeRegistry;
import stanhebben.zenscript.compiler.TypeRegistry;
import stanhebben.zenscript.type.ZenType;
import youyihj.zenutils.Reference;
import youyihj.zenutils.impl.member.ClassData;
import youyihj.zenutils.impl.member.ClassDataFetcher;
import youyihj.zenutils.impl.zenscript.nat.NativeClassValidate;
import youyihj.zenutils.impl.zenscript.nat.ZenTypeJavaNative;
import youyihj.zenutils.impl.zenscript.nat.ZenTypeJavaNativeIterable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * @author youyihj
 */
@Mixin(value = TypeRegistry.class, remap = false)
public abstract class MixinTypeRegistry implements ITypeRegistry {
    @Shadow
    @Final
    private Map<Class<?>, ZenType> types;

    @Shadow
    public abstract ZenType getType(Type type);

    @Inject(method = "getClassType", at = @At(value = "NEW", target = "stanhebben/zenscript/type/ZenTypeNative"), cancellable = true)
    private void redirectToJavaNative(Class<?> cls, CallbackInfoReturnable<ZenType> cir) {
        ClassData classData = Reference.classDataFetcher.forClass(cls);
        if (NativeClassValidate.isValid(classData)) {
            cir.setReturnValue(new ZenTypeJavaNative(classData, this));
            types.put(cls, cir.getReturnValue());
        }
    }

    @Inject(
            method = "getType",
            at = @At(
                    value = "INVOKE",
                    target = "Lstanhebben/zenscript/compiler/TypeRegistry;getClassType(Ljava/lang/Class;)Lstanhebben/zenscript/type/ZenType;",
                    ordinal = 0
            ),
            cancellable = true
    )
    private void getIterableType(Type type, CallbackInfoReturnable<ZenType> cir, @Local ParameterizedType pType, @Local Class<?> rawClass) {
        ClassDataFetcher classDataFetcher = Reference.classDataFetcher;
        if (classDataFetcher.forClass(Iterable.class).isAssignableFrom(classDataFetcher.forClass(rawClass))) {
            cir.setReturnValue(new ZenTypeJavaNativeIterable(classDataFetcher.forClass(rawClass), getType(pType.getActualTypeArguments()[0]), this));
        }
    }
}
