package com.alekseyzhelo.evilislands.mobplugin.script.codeInsight;

import com.alekseyzhelo.evilislands.mobplugin.script.EIScriptLanguage;
import com.alekseyzhelo.evilislands.mobplugin.script.codeInsight.util.EICallLookupElementRenderer;
import com.alekseyzhelo.evilislands.mobplugin.script.psi.*;
import com.alekseyzhelo.evilislands.mobplugin.script.psi.impl.EIArea;
import com.alekseyzhelo.evilislands.mobplugin.script.psi.impl.EIGSVar;
import com.alekseyzhelo.evilislands.mobplugin.script.util.ArgumentPositionPatternCondition;
import com.alekseyzhelo.evilislands.mobplugin.script.util.EIScriptNamingUtil;
import com.alekseyzhelo.evilislands.mobplugin.script.util.EIScriptNativeFunctionsUtil;
import com.alekseyzhelo.evilislands.mobplugin.script.util.UsefulPsiTreeUtil;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// TODO: insertHandler-s for script declarations, script implementations, function calls, script calls, etc
public class EIScriptCompletionContributor extends CompletionContributor {

    private List<LookupElement> functionLookupElements;
    private final static String QUOTED_DUMMY = "'" + CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED + "'";
    private final PsiElementPattern.Capture<PsiElement> hasErrorChild = PlatformPatterns.psiElement()
            .withChild(PlatformPatterns.psiElement(PsiErrorElement.class))
            .withLanguage(EIScriptLanguage.INSTANCE);


    public EIScriptCompletionContributor() {
        // TODO: rework?
        // TODO: basically always triggers, as the error is produced by the IntellijIdeaRulezzz dummy identifier, fix!
        extend(CompletionType.BASIC,
                PlatformPatterns
                        .psiElement()
                        .withTreeParent(hasErrorChild)
                        .withLanguage(EIScriptLanguage.INSTANCE),
                new CompletionProvider<CompletionParameters>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {
                        PsiElement parent = UsefulPsiTreeUtil.getParentByPattern(parameters.getOriginalPosition(), hasErrorChild);
                        PsiErrorElement[] errors = UsefulPsiTreeUtil.getChildrenOfType(parent, PsiErrorElement.class, null);
                        if (errors != null && errors.length > 0) {
                            String errorDescription = errors[0].getErrorDescription();
                            fillSuggestedTokens(result, parent, errorDescription);
                        }
                    }
                });

        // TODO: rework?
        extend(CompletionType.BASIC,
                PlatformPatterns
                        .psiElement(ScriptTypes.IDENTIFIER)
                        .withParent(PlatformPatterns.psiElement(PsiErrorElement.class))
                        .withLanguage(EIScriptLanguage.INSTANCE),
                new CompletionProvider<CompletionParameters>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {
                        PsiElement element = parameters.getOriginalPosition().getParent();
                        PsiElement parent = UsefulPsiTreeUtil.getParentByPattern(parameters.getOriginalPosition(), hasErrorChild);
                        if (!(element instanceof PsiErrorElement)) {  // TODO: makes sense to check the original pos first?
                            // check if non-original has 'dummy' and expected?
                            if (parent != null) {
                                PsiErrorElement[] errors = UsefulPsiTreeUtil.getChildrenOfType(parent, PsiErrorElement.class, null);
                                if (errors != null && errors.length > 0) {
                                    element = errors[0];
                                }
                            } else {
                                element = parameters.getPosition().getParent();
                            }
                        }
                        String errorDescription = ((PsiErrorElement) element).getErrorDescription();
                        fillSuggestedTokens(result, parent, errorDescription);
                    }
                });

        extend(CompletionType.BASIC, PlatformPatterns
                        .psiElement(ScriptTypes.CHARACTER_STRING)
                        .withLanguage(EIScriptLanguage.INSTANCE)
                        .withSuperParent(3, PlatformPatterns
                                .psiElement(EIFunctionCall.class)
                                .withFirstChild(PlatformPatterns
                                        .psiElement()
                                        .withText(StandardPatterns.string().oneOfIgnoreCase(EIGSVar.relevantFunctions.toArray(new String[0])))
                                )
                        )
                        .with(new ArgumentPositionPatternCondition(1)),
                new CompletionProvider<CompletionParameters>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                        Map<String, EIGSVar> vars = ((ScriptPsiFile) parameters.getOriginalFile()).findGSVars();
                        PsiElement element = parameters.getOriginalPosition();
                        EIFunctionCall call = PsiTreeUtil.getParentOfType(element, EIFunctionCall.class);

                        assert element != null;
                        assert call != null;
                        String varName = EIGSVar.getVarName(element.getText());
                        boolean isRead = EIGSVar.isGSVarRead(call);
                        EIGSVar myVar = vars.get(varName);

                        for (EIGSVar gsVar : vars.values()) {
                            if (gsVar == myVar && ((isRead && (myVar.getReads() == 1 && myVar.getWrites() == 0))
                                    || (!isRead && (myVar.getWrites() == 1 && myVar.getReads() == 0)))) {
                                continue;
                            }
                            suggestToken(result, gsVar.toString());
                        }
                    }
                }
        );
        // TODO: try GetObject completion here? also areas
        // TODO: wonky, fix grammar and dummy and try again
        extend(CompletionType.BASIC, PlatformPatterns
                        .psiElement()
                        .withLanguage(EIScriptLanguage.INSTANCE)
                        .withSuperParent(3, PlatformPatterns
                                .psiElement(EIFunctionCall.class)
                                .withFirstChild(PlatformPatterns
                                        .psiElement()
                                        .withText(StandardPatterns.string().oneOfIgnoreCase(EIArea.relevantFunctions.toArray(new String[0])))
                                )
                        ),
                new CompletionProvider<CompletionParameters>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                        Map<Integer, EIArea> areas = ((ScriptPsiFile) parameters.getOriginalFile()).findAreas();
                        PsiElement element = parameters.getOriginalPosition().getPrevSibling().getLastChild();
                        if (!ArgumentPositionPatternCondition.FIRST_ARGUMENT.accepts(element, context)) {
                            return;
                        }

                        EIFunctionCall call = PsiTreeUtil.getParentOfType(element, EIFunctionCall.class);

                        assert element != null;
                        assert call != null;
                        try {
                            int areaId = Integer.parseInt(element.getText());
                            boolean isRead = EIArea.isAreaRead(call);
                            EIArea myArea = areas.get(areaId);

                            for (EIArea area : areas.values()) {
                                if (area == myArea && ((isRead && (myArea.getReads() == 1 && myArea.getWrites() == 0))
                                        || (!isRead && (myArea.getWrites() == 1 && myArea.getReads() == 0)))) {
                                    continue;
                                }
                                suggestToken(result, area.toString());
                            }
                        } catch (NumberFormatException ignored) {
                            //whatewz
                        }
                    }
                }
        );
        extend(CompletionType.BASIC,
                PlatformPatterns
                        .psiElement(ScriptTypes.IDENTIFIER)
                        .inside(EIScriptIfBlock.class),
                new CompletionProvider<CompletionParameters>() {
                    public void addCompletions(@NotNull CompletionParameters parameters,
                                               ProcessingContext context,
                                               @NotNull CompletionResultSet resultSet) {
                        if (functionLookupElements == null) {
                            functionLookupElements = initFunctionLookup(parameters.getOriginalFile().getProject());
                        }
                        resultSet.addAllElements(functionLookupElements);
                    }
                }
        );
        // XXX: already done by my references
//        extend(CompletionType.BASIC,
//                PlatformPatterns.psiElement(ScriptTypes.IDENTIFIER)
//                        .inside(EIScriptThenBlock.class)
//                        .withLanguage(EIScriptLanguage.INSTANCE),
//                new CompletionProvider<CompletionParameters>() {
//                    public void addCompletions(@NotNull CompletionParameters parameters,
//                                               ProcessingContext context,
//                                               @NotNull CompletionResultSet resultSet) {
//                        if (functionLookupElements == null) {
//                            functionLookupElements = initFunctionLookup(parameters.getOriginalFile().getProject());
//                        }
//                        resultSet.addAllElements(functionLookupElements);
//                    }
//                }
//        );
    }

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet
            result) {
        super.fillCompletionVariants(parameters, result);
    }

    @Override
    public boolean invokeAutoPopup(@NotNull PsiElement position, char typeChar) {
        return super.invokeAutoPopup(position, typeChar);
    }

    private void fillSuggestedTokens(@NotNull CompletionResultSet result, PsiElement parent, String
            errorDescription) {
        if (errorDescription.contains("expected, got")) {
            suggestExpectedTerms(result, parent, errorDescription);
        } else if (errorDescription.contains("unexpected")) {
            suggestUnexpected(result, parent, errorDescription);
        }
    }

    private void suggestExpectedTerms(@NotNull CompletionResultSet result, PsiElement parent, String
            errorDescription) {
        // TODO: if parent is a ThenBlock or WorldScript, then we can also suggest global variables, functions or scripts
        // (or equals?); also check right-hand side of assignments? (or is this done automatically?)
        int indexOfSuggestion = errorDescription.indexOf(" expected, got ");
        if (indexOfSuggestion >= 0) {
            errorDescription = errorDescription.substring(0, indexOfSuggestion);
            errorDescription = errorDescription.replaceAll("ScriptTokenType\\.", "");
            String[] suggestedTokens = errorDescription.split("(, )|( or )");
            for (String suggestedToken : suggestedTokens) {
                if ("<type>".equals(suggestedToken)) {
                    suggestToken(result, EIScriptNamingUtil.FLOAT);
                    suggestToken(result, EIScriptNamingUtil.STRING);
                    suggestToken(result, EIScriptNamingUtil.OBJECT);
                    suggestToken(result, EIScriptNamingUtil.GROUP);
                } else if (EIScriptNamingUtil.tokenMap.get(suggestedToken) != null) {
                    suggestToken(result, EIScriptNamingUtil.tokenMap.get(suggestedToken));
                }
            }
        }
    }

    private void suggestUnexpected(@NotNull CompletionResultSet result, PsiElement parent, String errorDescription) {
        String unexpectedTerm = errorDescription.split("'")[1];
        // TODO: should be parent-dependent
        for (String token : EIScriptNamingUtil.tokenMap.values()) {
            if (token.toLowerCase(Locale.ENGLISH).startsWith(unexpectedTerm.toLowerCase(Locale.ENGLISH))) {
                suggestToken(result, token);
            }
        }
    }

    private void suggestToken(CompletionResultSet result, String token) {
        String prefix = "IntellijIdeaRulezzz".equals(result.getPrefixMatcher().getPrefix()) ? "" : result.getPrefixMatcher().getPrefix();
        if (prefix.length() == 0 || result.getPrefixMatcher().prefixMatches(token)) {
            result.addElement(LookupElementBuilder.create(token));
        }
    }

    private LookupElementBuilder prefixedToken(String prefix, String token, boolean withWhitespace) {
        return LookupElementBuilder.create(prefix + (withWhitespace ? " " : "") + token);
    }

    private boolean spaceForToken(String suggestedToken) {
        return !("COMMA".equals(suggestedToken) || "LPAREN".equals(suggestedToken) || "RPAREN".equals(suggestedToken));
    }

    private List<LookupElement> initFunctionLookup(Project project) {
        List<EIFunctionDeclaration> functions = EIScriptNativeFunctionsUtil.getAllFunctions(project);
        List<LookupElement> lookupElements = new ArrayList<>(functions.size());


        for (EIFunctionDeclaration function : functions) {
            lookupElements.add(LookupElementBuilder.create(function)
                    .withCaseSensitivity(false)
                    .withRenderer(new EICallLookupElementRenderer<>()));
        }
        return lookupElements;
    }
}