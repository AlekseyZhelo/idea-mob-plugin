package com.alekseyzhelo.evilislands.mobplugin.script.psi.impl;

import com.alekseyzhelo.evilislands.mobplugin.script.psi.EIScriptDeclaration;
import com.alekseyzhelo.evilislands.mobplugin.script.psi.ScriptImplementationMixin;
import com.intellij.lang.ASTNode;

/**
 * Created by Aleks on 25-07-2015.
 */
public class ScriptImplementationMixinImpl extends ScriptNamedElementImpl
        implements ScriptImplementationMixin {

    public ScriptImplementationMixinImpl(ASTNode node) {
        super(node);
    }

}
