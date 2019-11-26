package com.alekseyzhelo.evilislands.mobplugin.mob.psi.objects;

import com.alekseyzhelo.eimob.objects.MobParticle;
import com.alekseyzhelo.eimob.util.Float3;
import com.alekseyzhelo.evilislands.mobplugin.EIMessages;
import com.alekseyzhelo.evilislands.mobplugin.icon.Icons;
import com.alekseyzhelo.evilislands.mobplugin.script.codeInsight.util.DocumentationFormatter;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class PsiMobParticle extends PsiMobEntityBase {

    private final MobParticle value;

    public PsiMobParticle(PsiElement parent, MobParticle particle) {
        super(parent);
        value = particle;
    }

    @Override
    @NotNull
    public String getObjectKind() {
        return EIMessages.message("mob.particle");
    }

    @Nullable
    @Override
    public Icon getIcon(int flags) {
        return Icons.Objects.PARTICLE;
    }

    @Override
    public String getName() {
        return value.getName();
    }

    @Override
    public int getId() {
        return value.getId();
    }

    @NotNull
    @Override
    public Float3 getLocation() {
        return value.getLocation();
    }

    @Override
    public String getText() {
        return String.valueOf(value.getId());
    }

    @Override
    protected String getDocHeader() {
        return DocumentationFormatter.bold(getObjectKind());
    }

    @Override
    protected String getDocContent() {
        return DocumentationFormatter.bold("ID: ") + value.getId() + "<br/>" +
                DocumentationFormatter.bold("Name: ") + value.getName() + "<br/>" +
                DocumentationFormatter.bold("Location: ") + value.getLocation() + "<br/>" +
                DocumentationFormatter.bold("Scale: ") + value.getScale() + "<br/>" +
                DocumentationFormatter.bold("Type: ") + value.getType() + "<br/>";
    }
}
