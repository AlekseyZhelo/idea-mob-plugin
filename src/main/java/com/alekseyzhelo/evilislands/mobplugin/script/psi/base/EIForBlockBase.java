package com.alekseyzhelo.evilislands.mobplugin.script.psi.base;

import com.alekseyzhelo.evilislands.mobplugin.script.psi.EIExpression;
import com.alekseyzhelo.evilislands.mobplugin.script.util.EITypeToken;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface EIForBlockBase extends EIScriptPsiElement {
    @NotNull
    List<EIExpression> getArguments();

    int indexOfArgument(@NotNull EIExpression expression);

    @NotNull
    EITypeToken[] getArgumentTypes();
}
