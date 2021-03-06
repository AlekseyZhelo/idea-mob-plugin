package com.alekseyzhelo.evilislands.mobplugin.script.lexer;

import com.alekseyzhelo.evilislands.mobplugin.script.psi.ScriptTypes;
import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;

%%

%class EIScriptLexer
%implements FlexLexer
%public
%unicode
%caseless

%function advance
%type IElementType

%eof{  return;
%eof}

LINE_TERMINATOR = \r|\n|\r\n
INPUT_CHARACTER = [^\r\n]
STRING_CHARACTER = [^\r\n\"]
WHITE_SPACE = [\ \t\f]
WHITE_SPACE_OR_TERMINATOR = {WHITE_SPACE} | {LINE_TERMINATOR}
COMMENT = "//" {INPUT_CHARACTER}*
FLOAT = "float"
STRING = "string"
OBJECT = "object"
GROUP = "group"
GLOBALVARS = "globalvars"
DECLARESCRIPT = "declarescript"
SCRIPT = "script"
IF = "if"
THEN = "then"
FOR = "for"
FOR_IF = "forif"
WORLDSCRIPT = "worldscript"
IDENTIFIER = [#_a-zA-Z]([#_a-zA-Z0-9-])*
FLOATNUMBER = -?[0-9][0-9]*|-?[0-9]+"."[0-9]+

%state STRING, MULTILINE_COMMENT

%%
// apparently it is very important not to ignore any characters from the input
<YYINITIAL> {
    {FLOAT}                                      { return ScriptTypes.FLOAT; }
    {STRING}                                     { return ScriptTypes.STRING; }
    {OBJECT}                                     { return ScriptTypes.OBJECT; }
    {GROUP}                                      { return ScriptTypes.GROUP; }
    {GLOBALVARS}                                 { return ScriptTypes.GLOBALVARS; }
    {DECLARESCRIPT}                              { return ScriptTypes.DECLARESCRIPT; }
    {WORLDSCRIPT}                                { return ScriptTypes.WORLDSCRIPT; }
    {SCRIPT}                                     { return ScriptTypes.SCRIPT; }
    {IF}                                         { return ScriptTypes.IF; }
    {THEN}                                       { return ScriptTypes.THEN; }
    {FOR}                                        { return ScriptTypes.FOR; }
    {FOR_IF}                                     { return ScriptTypes.FOR_IF; }
    {IDENTIFIER}                                 { return ScriptTypes.IDENTIFIER; }
    {FLOATNUMBER}                                { return ScriptTypes.FLOATNUMBER; }
    "="                                          { return ScriptTypes.EQUALS; }
    "("                                          { return ScriptTypes.LPAREN; }
    ")"                                          { return ScriptTypes.RPAREN; }
    ","                                          { return ScriptTypes.COMMA; }
    ":"                                          { return ScriptTypes.COLON; }
    {WHITE_SPACE_OR_TERMINATOR}+                 { return TokenType.WHITE_SPACE; }
    ^{COMMENT}                                   { return ScriptTypes.WHOLE_LINE_COMMENT; }
    {COMMENT}                                    { return ScriptTypes.COMMENT; }
    \"                                           { yybegin(STRING); }
    "/*"                                         { yybegin(MULTILINE_COMMENT); }
}

<STRING> {
     \" | {LINE_TERMINATOR}                      { yybegin(YYINITIAL); return ScriptTypes.CHARACTER_STRING; }
     {STRING_CHARACTER}+                         { }
}

<MULTILINE_COMMENT> {
    "*/"                                         { yybegin(YYINITIAL); return ScriptTypes.MULTILINE_COMMENT; }
    <<EOF>>                                      { yybegin(YYINITIAL); return ScriptTypes.MULTILINE_COMMENT; }
    [^]                                          { }
}

[^] { return TokenType.BAD_CHARACTER; }