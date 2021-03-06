package com.alekseyzhelo.evilislands.mobplugin.script.codeInsight;

import com.alekseyzhelo.evilislands.mobplugin.EIMessages;
import com.alekseyzhelo.evilislands.mobplugin.script.psi.*;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.CustomFoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// TODO v2: plurals
public class EIScriptCustomFoldingBuilder extends CustomFoldingBuilder {

    @Override
    protected void buildLanguageFoldRegions(@NotNull List<FoldingDescriptor> descriptors, @NotNull PsiElement root, @NotNull Document document, boolean quick) {
        processGlobals(root, descriptors);
        processScriptDeclarations(root, descriptors);
        processScriptImplementations(root, descriptors);
        processWorldScript(root, descriptors);
    }

    @Override
    protected String getLanguagePlaceholderText(@NotNull ASTNode node, @NotNull TextRange range) {
        return "...";
    }

    @Override
    protected boolean isRegionCollapsedByDefault(@NotNull ASTNode node) {
        return false;
    }

    private void processGlobals(@NotNull PsiElement root, @NotNull List<FoldingDescriptor> descriptors) {
        EIGlobalVars globals = PsiTreeUtil.getChildOfType(root, EIGlobalVars.class);
        if (globals != null) {
            final int variablesCount = globals.getGlobalVarList().size();
            final ASTNode lParen = globals.getNode().findChildByType(ScriptTypes.LPAREN);
            if (lParen != null) {
                final int lParenOffset = lParen.getStartOffset();
                descriptors.add(new FoldingDescriptor(globals.getNode(),
                        new TextRange(lParenOffset,
                                globals.getTextRange().getEndOffset()), null) {
                    @Override
                    public String getPlaceholderText() {
                        return "(" + EIMessages.message("folding.globalVarDeclarations", variablesCount) + ")";
                    }
                });
            }
        }
    }

    private void processScriptDeclarations(@NotNull PsiElement root, @NotNull List<FoldingDescriptor> descriptors) {
        EIDeclarations declarations = PsiTreeUtil.getChildOfType(root, EIDeclarations.class);
        if (declarations != null) {
            final int declarationsCount = declarations.getScriptDeclarationList().size();
            foldWholeElement(descriptors, declarations, "{" + EIMessages.message("folding.scriptDeclarations", declarationsCount) + "}");
        }
    }

    private void processScriptImplementations(@NotNull PsiElement root, @NotNull List<FoldingDescriptor> descriptors) {
        EIScripts scripts = PsiTreeUtil.getChildOfType(root, EIScripts.class);
        if (scripts != null) {
            final int implementationsCount = scripts.getScriptImplementationList().size();
            foldWholeElement(descriptors, scripts, "{" + EIMessages.message("folding.scriptImplementations", implementationsCount) + "}");
            for (EIScriptImplementation scriptImplementation : scripts.getScriptImplementationList()) {
                processScriptImplementation(scriptImplementation, descriptors);
            }
        }
    }

    private void processScriptImplementation(@NotNull EIScriptImplementation scriptImplementation, @NotNull List<FoldingDescriptor> descriptors) {
        foldFromLParenToEnd(descriptors, scriptImplementation);
        for (EIScriptBlock block : scriptImplementation.getScriptBlockList()) {
            processScriptBlock(block, descriptors);
        }
    }

    private void processScriptBlock(@NotNull EIScriptBlock block, @NotNull List<FoldingDescriptor> descriptors) {
        foldFromLParenToEnd(descriptors, block.getScriptIfBlock());
        if (block.getScriptThenBlock() != null) {
            foldFromLParenToEnd(descriptors, block.getScriptThenBlock());
        }
    }

    private void processWorldScript(@NotNull PsiElement root, @NotNull List<FoldingDescriptor> descriptors) {
        EIWorldScript worldScript = PsiTreeUtil.getChildOfType(root, EIWorldScript.class);
        if (worldScript != null) {
            foldFromLParenToEnd(descriptors, worldScript);
        }
    }

    private void foldWholeElement(
            @NotNull List<FoldingDescriptor> descriptors,
            final PsiElement element,
            final String placeholderText
    ) {
        if (element.getTextRange().getStartOffset() >= element.getTextRange().getEndOffset()) {
            return;
        }
        descriptors.add(new FoldingDescriptor(element.getNode(),
                new TextRange(element.getTextRange().getStartOffset(),
                        element.getTextRange().getEndOffset()), null) {
            @Nullable
            @Override
            public String getPlaceholderText() {
                return placeholderText;
            }
        });
    }

    private void foldFromLParenToEnd(@NotNull List<FoldingDescriptor> descriptors, @NotNull final PsiElement element) {
        ASTNode lParen = element.getNode().findChildByType(ScriptTypes.LPAREN);
        if (lParen != null) {
            final int lParenOffset = lParen.getStartOffset();
            if (lParenOffset >= element.getTextRange().getEndOffset()) {
                return;
            }
            descriptors.add(new FoldingDescriptor(element.getNode(),
                    new TextRange(lParenOffset,
                            element.getTextRange().getEndOffset()), null) {
                @Override
                public String getPlaceholderText() {
                    return "(...)";
                }
            });
        }
    }
}