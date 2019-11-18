package com.alekseyzhelo.evilislands.mobplugin.script.util;

import com.intellij.psi.PsiNamedElement;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EICommonUtil {

    private EICommonUtil() {
    }


    @NotNull
    public static <T extends PsiNamedElement> Map<String, T> toNameMap(@NotNull List<T> list) {
        final Map<String, T> map = new LinkedHashMap<>();
        for (T val : list) {
            map.putIfAbsent(val.getName(), val);
        }
//        return list.stream().collect(Collectors.toMap(PsiNamedElement::getName, Function.identity()));
        return map;
    }
}