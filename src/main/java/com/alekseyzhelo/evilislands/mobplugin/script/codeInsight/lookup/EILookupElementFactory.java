package com.alekseyzhelo.evilislands.mobplugin.script.codeInsight.lookup;

import com.alekseyzhelo.eimob.objects.MobMapEntity;
import com.alekseyzhelo.evilislands.mobplugin.icon.Icons;
import com.alekseyzhelo.evilislands.mobplugin.mob.psi.objects.PsiMobMapEntity;
import com.alekseyzhelo.evilislands.mobplugin.script.psi.*;
import com.alekseyzhelo.evilislands.mobplugin.script.psi.base.EIExtraVarBase;
import com.alekseyzhelo.evilislands.mobplugin.script.util.EIScriptNamingUtil;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.LookupElementDecorator;
import com.intellij.icons.AllIcons;
import org.jetbrains.annotations.NotNull;

public final class EILookupElementFactory {

    private EILookupElementFactory() {

    }

    @NotNull
    public static LookupElement asScriptImplVariant(@NotNull ScriptPsiFile file, @NotNull LookupElement element) {
        return PrioritizedLookupElement.withPriority(
                LookupElementDecorator.withInsertHandler(element, CaseCorrectingInsertHandler.INSTANCE),
                file.findScriptImplementation(element.getLookupString()) == null ? 10 : 1
        );
    }

    @NotNull
    public static LookupElement create(EIFormalParameter param) {
        EIType type = param.getType();
        return LookupElementBuilder.create(param)
                .withIcon(AllIcons.Nodes.Parameter)
                .withTypeText(type != null ? type.getText() : "unknown")
                .withInsertHandler(CaseCorrectingInsertHandler.INSTANCE)
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
                .withInsertHandler(CaseCorrectingInsertHandler.INSTANCE)
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
    public static LookupElement createForToken(String lookupString) {
        return LookupElementBuilder.create(lookupString)
                .withCaseSensitivity(false)
                .withInsertHandler(CaseCorrectingInsertHandler.INSTANCE);
    }

    @SuppressWarnings("rawtypes")
    @NotNull
    public static LookupElement create(EIExtraVarBase var) {
        return LookupElementBuilder.create(var.toString())
                .withCaseSensitivity(false)
                .withInsertHandler(CaseCorrectingInsertHandler.INSTANCE)
                .withIcon(var.getIcon(0));
    }

    @NotNull
    public static LookupElement create(PsiMobMapEntity<? extends MobMapEntity> entity) {
        return create(entity, entity.getText()); // TODO v2: probably shouldn't get the ID via getText
    }

    @NotNull
    public static LookupElement createByName(PsiMobMapEntity<? extends MobMapEntity> entity) {
        return create(entity, entity.getName());
    }

    @NotNull
    private static LookupElement create(PsiMobMapEntity<? extends MobMapEntity> entity, String lookupString) {
        return LookupElementBuilder
                .create(lookupString)
                .withTypeText(entity.getObjectKind())
                .withPresentableText(lookupText(entity))
                .withCaseSensitivity(false)
                .withInsertHandler(CaseCorrectingInsertHandler.INSTANCE)
                .withIcon(entity.getIcon(0));
    }

    private static String lookupText(PsiMobMapEntity<? extends MobMapEntity> entity) {
//        Float3 location = entity.getLocation();
//        return String.format("%-10d %s at (%.2f, %.2f, %.2f)", entity.getId(), entity.getName(),
//                location.getX(), location.getY(), location.getZ());
        return String.format("%-10d %s", entity.getId(), entity.getName());
    }

    private static class CaseCorrectingInsertHandler implements InsertHandler<LookupElement> {
        public static final CaseCorrectingInsertHandler INSTANCE = new CaseCorrectingInsertHandler();

        @Override
        public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement item) {
            context.getDocument().replaceString(context.getStartOffset(), context.getTailOffset(), item.getLookupString());
        }
    }
}
