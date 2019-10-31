package com.alekseyzhelo.evilislands.mobplugin.script.codeInsight.lookup;

import com.alekseyzhelo.eimob.util.Float3;
import com.alekseyzhelo.evilislands.mobplugin.icon.Icons;
import com.alekseyzhelo.evilislands.mobplugin.mob.psi.objects.PsiMobEntityBase;
import com.alekseyzhelo.evilislands.mobplugin.script.psi.*;
import com.alekseyzhelo.evilislands.mobplugin.script.util.EIScriptNamingUtil;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import org.jetbrains.annotations.NotNull;

public final class EILookupElementFactory {

    private EILookupElementFactory() {

    }

    @NotNull
    public static LookupElement create(EIFormalParameter param) {
        EIType type = param.getType();
        return LookupElementBuilder.create(param)
                // TODO: icon
                .withIcon(Icons.FILE)
                .withTypeText(type != null ? type.getText() : "unknown")
                .withCaseSensitivity(false);
    }

    @NotNull
    public static LookupElement create(EIGlobalVar globalVar) {
        return LookupElementBuilder.create(globalVar)
                .withIcon(Icons.GLOBAL_VAR)
                .withTypeText(
                        globalVar.getType() != null
                                ? globalVar.getType().getText()
                                : EIScriptNamingUtil.UNKNOWN
                )
                .withCaseSensitivity(false);
    }

    @NotNull
    public static LookupElement create(EIScriptDeclaration scriptDeclaration) {
        return new EICallableLookupElement(scriptDeclaration, true);
    }

    @NotNull
    public static LookupElement create(EIFunctionDeclaration function) {
        return new EICallableLookupElement(function, false);
//        return new EICallableTemplateLookupElement(function, false);
    }

    @NotNull
    public static LookupElement create(PsiMobEntityBase entity) {
        return LookupElementBuilder
                .create(entity.getText())
                .withTypeText(entity.getObjectKind())
                .withPresentableText(lookupText(entity));
    }

    private static String lookupText(PsiMobEntityBase entity) {
        Float3 location = entity.getLocation();
        return String.format("%-10d %s at (%.2f, %.2f, %.2f)", entity.getId(), entity.getName(),
                location.getX(), location.getY(), location.getZ());
    }
}