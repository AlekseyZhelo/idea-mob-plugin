package com.alekseyzhelo.evilislands.mobplugin.script.codeInsight.fixes;

import com.alekseyzhelo.evilislands.mobplugin.EIMessages;
import com.alekseyzhelo.evilislands.mobplugin.script.psi.EIFormalParameter;
import com.alekseyzhelo.evilislands.mobplugin.script.psi.EIType;
import com.alekseyzhelo.evilislands.mobplugin.script.psi.ScriptPsiFile;
import com.alekseyzhelo.evilislands.mobplugin.script.psi.base.EIVariableBase;
import com.alekseyzhelo.evilislands.mobplugin.script.util.EIScriptElementFactory;
import com.alekseyzhelo.evilislands.mobplugin.script.util.EITypeToken;
import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInspection.LocalQuickFixOnPsiElement;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.command.undo.UndoUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

// TODO v2: make it an Intention as well?
public class ChangeLvalueTypeFix extends LocalQuickFixOnPsiElement {

    private final String varName;
    private final EITypeToken newType;
    private final boolean isParameter;

    public ChangeLvalueTypeFix(@NotNull EIVariableBase element, EITypeToken newType) {
        super(element);
        varName = element.getName();
        this.newType = newType;
        isParameter = element instanceof EIFormalParameter;
    }

    @NotNull
    @Override
    public String getText() {
        return EIMessages.message(isParameter ? "fix.change.parameter.type" : "fix.change.variable.type", varName, newType.getTypeString());
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getFamilyName() {
        return EIMessages.message("fix.change.type.base");
    }

    @Override
    public void invoke(@NotNull Project project, @NotNull PsiFile file, @NotNull PsiElement startElement, @NotNull PsiElement endElement) {
        ScriptPsiFile psiFile = (ScriptPsiFile) startElement.getContainingFile();
        EIVariableBase myVar = (EIVariableBase) startElement;

        if (!FileModificationService.getInstance().preparePsiElementForWrite(psiFile)) return;

        EIType type = myVar.getType();
        if (type != null) {
            WriteCommandAction.writeCommandAction(project, file).withName(getText()).run(() -> {
                try {
                    EIType newEIType = EIScriptElementFactory.createType(psiFile.getProject(), newType);
                    type.replace(newEIType);
                    UndoUtil.markPsiFileForUndo(psiFile);
                } catch (IncorrectOperationException e) {
                    LOG.error(e);
                }
            });
        }
    }
}
