package com.alekseyzhelo.evilislands.mobplugin.script.codeInsight.annotator;

import com.alekseyzhelo.evilislands.mobplugin.EIMessages;
import com.alekseyzhelo.evilislands.mobplugin.script.codeInsight.fixes.DeleteListElementFix;
import com.alekseyzhelo.evilislands.mobplugin.script.codeInsight.util.EICallArgumentErrorDetector;
import com.alekseyzhelo.evilislands.mobplugin.script.psi.*;
import com.alekseyzhelo.evilislands.mobplugin.script.psi.base.EICallableDeclaration;
import com.alekseyzhelo.evilislands.mobplugin.script.util.EIScriptTypingUtil;
import com.alekseyzhelo.evilislands.mobplugin.script.util.EITypeToken;
import com.intellij.codeInsight.daemon.impl.quickfix.DeleteElementFix;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.*;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.ui.ColorUtil;
import com.intellij.util.ui.UIUtil;
import com.intellij.xml.util.XmlStringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.alekseyzhelo.evilislands.mobplugin.script.util.EIScriptNamingUtil.getName;

final class AnnotatorUtil {

    private AnnotatorUtil() {

    }

    static void registerReferenceQuickFixes(@NotNull Annotation annotation, @NotNull PsiReference reference) {
        if (reference instanceof LocalQuickFixProvider) {
            LocalQuickFix[] fixes = ((LocalQuickFixProvider) reference).getQuickFixes();
            if (fixes != null) {
                InspectionManager inspectionManager = InspectionManager.getInstance(reference.getElement().getProject());
                for (LocalQuickFix fix : fixes) {
                    ProblemDescriptor descriptor = inspectionManager.createProblemDescriptor(
                            reference.getElement(),
                            annotation.getMessage(),
                            fix,
                            ProblemHighlightType.LIKE_UNKNOWN_SYMBOL,
                            true
                    );
                    annotation.registerFix(fix, null, null, descriptor);
                }
            }
        }
    }

    @Nullable
    static String detectFunctionCallError(@NotNull PsiElement callNameElement,
                                          PsiElement resolvedTo,
                                          boolean inIfBlock) {
        String errorMessage = null;
        if (resolvedTo == null) {
            errorMessage = EIMessages.message(
                    inIfBlock ? "error.unresolved.function"
                            : "error.unresolved.function.or.script",
                    callNameElement.getText()
            );
        } else if (resolvedTo instanceof EIScriptDeclaration && inIfBlock) {
            errorMessage = EIMessages.message("error.not.allowed.in.script.if");
        } else if (resolvedTo instanceof EIFunctionDeclaration && inIfBlock) {
            EIFunctionDeclaration function = (EIFunctionDeclaration) resolvedTo;
            if (function.getType() == null || function.getType().getTypeToken() != EITypeToken.FLOAT) {
                errorMessage = EIMessages.message("error.not.allowed.in.script.if");
            }
        }
        return errorMessage;
    }

    @NotNull
    static Annotation createBadForArgumentsAnnotation(@NotNull AnnotationHolder holder,
                                                      @NotNull TextRange textRange,
                                                      EITypeToken firstArgType,
                                                      EITypeToken secondArgType) {

        EITypeToken[] expected = new EITypeToken[]{EITypeToken.OBJECT, EITypeToken.GROUP};
        EITypeToken[] actual = new EITypeToken[]{firstArgType, secondArgType};
        StringBuilder ms = new StringBuilder();
        for (int i = 0; i < expected.length; i++) {
            boolean assignable = EIScriptTypingUtil.matchingType(expected[i], actual[i]);
            String mismatchColor = assignable ? null : getFittingRed();
            ms.append("<td> " + "<b><nobr>");
            ms.append(i == 0 ? "(" : "");
            ms.append("<font ");
            if (!assignable) ms.append("color=").append(mismatchColor);
            ms.append(">");
            ms.append(XmlStringUtil.escapeString(getName(actual[i])));
            ms.append("</font>");
            ms.append(i == expected.length - 1 ? ")" : ",");
            ms.append("</nobr></b></td>");
        }
        // TODO: message needed here?
        Annotation annotation = holder.createErrorAnnotation(textRange, "");
        annotation.setTooltip(EIMessages.message("for.block.argument.mismatch.html.tooltip", ms.toString()));
        return annotation;
    }

    @NotNull
    static Annotation createIncompatibleCallTypesAnnotation(@NotNull AnnotationHolder holder,
                                                            @NotNull EICallArgumentErrorDetector errorDetector) {
        EIFormalParameter parameter = errorDetector.getFirstWrongParameter();
        EIExpression expression = errorDetector.getFirstWrongArgument();
        return holder.createErrorAnnotation(
                expression.getTextRange(),
                EIMessages.message("error.incompatible.call.types",
                        errorDetector.getFirstWrong() + 1,
                        getName(parameter.getType()),
                        getName(expression.getType()))
        );
    }

    @NotNull
    static Annotation createBadCallArgumentsAnnotation(@NotNull AnnotationHolder holder,
                                                       @NotNull EICallableDeclaration declaration,
                                                       @NotNull EIParams arguments) {
        // TODO; need a message?
        Annotation annotation = holder.createErrorAnnotation(arguments.getTextRange(), "");
        annotation.setTooltip(badCallArgumentsTooltip(declaration, arguments.getExpressionList()));
        return annotation;
    }

    @NotNull
    private static String badCallArgumentsTooltip(@NotNull EICallableDeclaration callable,
                                                  @NotNull List<EIExpression> expressions) {
        List<EIFormalParameter> parameters = callable.getCallableParams();
        final String spacing = "&nbsp;&nbsp;&nbsp;&nbsp;";

        StringBuilder s = new StringBuilder()
                .append("<html><body><table border=0>")
                .append("<tr><td colspan=3>").append("<nobr><b>").append(callable.getName()).append("()")
                .append(String.format("</b> %s</nobr>", EIMessages.message("error.call.cannot.be.applied.to")))
                .append("</td></tr>")
                .append(String.format("<tr><td colspan=2 align=left>%s</td><td align=left>%s</td></tr>",
                        EIMessages.message("error.call.expected.parameters"), EIMessages.message("error.call.actual.arguments")))
                .append("<tr><td colspan=3><hr></td></tr>");
        for (int i = 0; i < Math.max(parameters.size(), expressions.size()); i++) {
            EIFormalParameter parameter = i < parameters.size() ? parameters.get(i) : null;
            EIExpression expression = i < expressions.size() ? expressions.get(i) : null;
            String mismatchColor = EIScriptTypingUtil.matchingType(parameter, expression) ? null : getFittingRed();

            s.append("<tr");
            if (i % 2 == 0) {
                //noinspection SpellCheckingInspection
                String bg = UIUtil.isUnderDarcula() ? ColorUtil.toHex(ColorUtil.shift(UIUtil.getToolTipBackground(), 1.1)) : "eeeeee";
                s.append(" style='background-color: #").append(bg).append("'");
            }
            s.append(">");

            s.append("<td><b><nobr>");
            if (parameter != null) {
                String name = parameter.getName();
                s.append(escape(name)).append(":").append(spacing);
            }
            s.append("</nobr></b></td>");

            s.append("<td><b><nobr>");
            if (parameter != null) {
                EIType type = parameter.getType();
                s.append("<font ");
                if (mismatchColor != null) s.append("color=").append(mismatchColor);
                s.append(">");
                s.append(escape(getName(type))).append(spacing);
                s.append("</font>");
            }
            s.append("</nobr></b></td>");

            s.append("<td><b><nobr>");
            if (expression != null) {
                EITypeToken type = expression.getType();
                s.append("<font ");
                if (mismatchColor != null) s.append("color='").append(mismatchColor).append("'");
                s.append(">");
                s.append(escape(expression.getText()));
                s.append("&nbsp;&nbsp;");
                if (mismatchColor != null && type != null) {
                    s.append("(").append(escape(getName(type))).append(")");
                }
                s.append("</font>");
            }
            s.append("</nobr></b></td>");
            s.append("</tr>");
        }
        s.append("</table>");
        s.append("</body></html>");

        return s.toString();
    }

    @NotNull
    static Annotation createIncompatibleTypesAnnotation(@NotNull AnnotationHolder holder,
                                                        @NotNull TextRange textRange,
                                                        EITypeToken lType,
                                                        EITypeToken rType) {
        boolean assignable = EIScriptTypingUtil.matchingType(lType, rType);
        String toolTip = EIMessages.message("incompatible.types.html.tooltip",
                redIfNotMatch(lType, assignable),
                redIfNotMatch(rType, assignable)
        );
        String message = EIMessages.message(
                "error.incompatible.types", getName(lType), getName(rType)
        );

        Annotation annotation = holder.createErrorAnnotation(textRange, message);
        annotation.setTooltip(toolTip);
        return annotation;
    }

    static Annotation markAsError(@NotNull AnnotationHolder holder,
                                  @NotNull PsiElement nameElement,
                                  @NotNull String errorString,
                                  boolean likeUnknownSymbol) {
        Annotation annotation = holder.createErrorAnnotation(nameElement.getTextRange(), errorString);
        if (likeUnknownSymbol) {
            annotation.setHighlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL);
        }
        return annotation;
    }

    static Annotation markAsWarning(@NotNull AnnotationHolder holder, @NotNull PsiElement element, @NotNull String warningString) {
        return holder.createWarningAnnotation(element.getTextRange(), warningString);
    }

    static void markAsWeakWarning(@NotNull AnnotationHolder holder, @NotNull PsiElement nameElement, @NotNull String warningString) {
        holder.createWeakWarningAnnotation(nameElement.getTextRange(), warningString).setHighlightType(ProblemHighlightType.WEAK_WARNING);
    }

    static IntentionAction createDeleteElementFix(PsiElement toDelete, boolean isInList) {
        return isInList
                ? new DeleteListElementFix(toDelete, EIMessages.message("fix.remove.element"))
                : new DeleteElementFix(toDelete, EIMessages.message("fix.remove.element"));
    }

    @NotNull
    private static String redIfNotMatch(@Nullable EITypeToken type, boolean matches) {
        if (matches) return getName(type);
        return "<font color='" + getFittingRed() + "'><b>" + getName(type) + "</b></font>";
    }

    @NotNull
    private static String getFittingRed() {
        return UIUtil.isUnderDarcula() ? "FF6B68" : "red";
    }

    @NotNull
    private static String escape(@NotNull String s) {
        return XmlStringUtil.escapeString(s);
    }
}
