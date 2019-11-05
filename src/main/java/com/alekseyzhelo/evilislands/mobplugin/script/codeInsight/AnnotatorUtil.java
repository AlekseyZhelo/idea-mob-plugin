package com.alekseyzhelo.evilislands.mobplugin.script.codeInsight;

import com.alekseyzhelo.evilislands.mobplugin.script.codeInsight.util.EIErrorMessages;
import com.alekseyzhelo.evilislands.mobplugin.script.psi.EIType;
import com.alekseyzhelo.evilislands.mobplugin.script.util.EITypeToken;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.util.TextRange;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class AnnotatorUtil {

    private AnnotatorUtil() {

    }

    @Nullable
    static Annotation createIncompatibleTypesAnnotation(@NotNull AnnotationHolder holder,
                                                        @NotNull TextRange textRange,
                                                        EITypeToken lType,
                                                        EITypeToken rType) {
        boolean assignable = lType == null || rType == null || lType.equals(rType);
        String toolTip = EIErrorMessages.message("incompatible.types.html.tooltip",
                redIfNotMatch(lType, assignable),
                redIfNotMatch(rType, assignable));
        String message = EIErrorMessages.message(
                "incompatible.types", getName(lType), getName(rType));

        return holder.createAnnotation(HighlightSeverity.ERROR, textRange, message, toolTip);
    }

    @NotNull
    private static String redIfNotMatch(@Nullable EITypeToken type, boolean matches) {
        if (matches) return getName(type);
        String color = UIUtil.isUnderDarcula() ? "FF6B68" : "red";
        return "<font color='" + color + "'><b>" + getName(type) + "</b></font>";
    }

    private static String getName(@Nullable EITypeToken type) {
        return type == null ? "" : type.getTypeString();
    }
}
