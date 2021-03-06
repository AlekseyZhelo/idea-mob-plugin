package com.alekseyzhelo.evilislands.mobplugin.mob.psi;

import com.alekseyzhelo.evilislands.mobplugin.mob.EIMobLanguage;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.PsiElementBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class PsiMobElement extends PsiElementBase {

    private final Project project;
    private final PsiElement parent;

    public PsiMobElement(PsiElement parent) {
        this.parent = parent;
        project = parent.getProject();
    }

    @Override
    public PsiElement getParent() {
        return parent;
    }

    @NotNull
    @Override
    public Project getProject() {
        return project;
    }

    @NotNull
    @Override
    public PsiElement[] getChildren() {
        return PsiElement.EMPTY_ARRAY;
    }

    @NotNull
    @Override
    public Language getLanguage() {
        return EIMobLanguage.INSTANCE;
    }

    @Override
    public TextRange getTextRange() {
        return null;
    }

    @Override
    public int getStartOffsetInParent() {
        return 0;
    }

    @Override
    public int getTextLength() {
        return 0;
    }

    @Nullable
    @Override
    public PsiElement findElementAt(int offset) {
        return null;
    }

    @Override
    public int getTextOffset() {
        return 0;
    }

    @Override
    public String getText() {
        return null;
    }

    @NotNull
    @Override
    public char[] textToCharArray() {
        return new char[0];
    }

    @Override
    public ASTNode getNode() {
        return null;
    }
}
