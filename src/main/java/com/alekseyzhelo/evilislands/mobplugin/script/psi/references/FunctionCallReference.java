package com.alekseyzhelo.evilislands.mobplugin.script.psi.references;

import com.alekseyzhelo.evilislands.mobplugin.script.codeInsight.intellij.EIFunctionsService;
import com.alekseyzhelo.evilislands.mobplugin.script.psi.*;
import com.alekseyzhelo.evilislands.mobplugin.script.psi.base.EIForBlockBase;
import com.alekseyzhelo.evilislands.mobplugin.script.util.EIScriptTypingUtil;
import com.alekseyzhelo.evilislands.mobplugin.script.util.EITypeToken;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

// This is either a function or a script call
public class FunctionCallReference extends PsiReferenceBase<EIFunctionCall> {

    private final String name;

    public FunctionCallReference(@NotNull EIFunctionCall element, TextRange textRange) {
        super(element, textRange);
        name = element.getName();
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
        PsiElement element = resolve();
        if (element instanceof EIFunctionDeclaration) {
            throw new IncorrectOperationException();
        } else {
            return myElement;
        }
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        return ResolveCache.getInstance(myElement.getProject()).resolveWithCaching(
                this,
                MyResolver.INSTANCE,
                true,
                false
        );
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        ScriptPsiFile file = (ScriptPsiFile) myElement.getContainingFile();
//        if (file == null) {
//            return new Object[0];
//        }
        PsiElement parent = myElement.getParent();
        List<LookupElement> variants = new ArrayList<>();
        EIFunctionsService service = EIFunctionsService.getInstance(file.getProject());

        if (parent instanceof EIParams) {
            EITypeToken expectedType = EIScriptTypingUtil.getExpectedType((EIParams) parent, myElement);
            variants.addAll(service.getFunctionLookupElements(expectedType));
        } else if (parent instanceof EICallStatement) {
            variants.addAll(file.getScriptLookupElements());
            variants.addAll(service.getFunctionLookupElements(EITypeToken.VOID));
        } else if (parent instanceof EIScriptIfBlock) {
            variants.addAll(service.getFunctionLookupElements(EITypeToken.FLOAT));
        } else if (parent instanceof EIAssignment) {
            EITypeToken expectedType = ((EIAssignment) parent).getLeftSide().getType();
            // TODO v2: ASSIGNABLE type instead of ANY?
            variants.addAll(service.getFunctionLookupElements(expectedType != null ? expectedType : EITypeToken.ANY));
        } else if (parent instanceof EIForBlockBase) {
            int myIdx = ((EIForBlockBase) parent).indexOfArgument(myElement);
            if (myIdx == 1) {
                variants.addAll(service.getFunctionLookupElements(EITypeToken.GROUP));
            } else if (myIdx == 2) {
                variants.addAll(service.getFunctionLookupElements(EITypeToken.FLOAT));
            }
        }

        return variants.toArray();
    }

    private static class MyResolver implements ResolveCache.AbstractResolver<FunctionCallReference, PsiElement> {
        static final MyResolver INSTANCE = new MyResolver();

        @Override
        public PsiElement resolve(@NotNull FunctionCallReference ref, boolean incompleteCode) {
            ScriptPsiFile file = (ScriptPsiFile) ref.myElement.getContainingFile();
            EIFunctionsService service = EIFunctionsService.getInstance(file.getProject());
            EIFunctionDeclaration function = service.getFunctionDeclaration(ref.name);
            return function != null ? function : file.findScriptDeclaration(ref.name);
        }
    }
}
