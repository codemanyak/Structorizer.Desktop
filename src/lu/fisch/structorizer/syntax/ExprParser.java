/*
    Structorizer
    A little tool which you can use to create Nassi-Schneiderman Diagrams (NSD)

    Copyright (C) 2009  Bob Fisch
    Copyright (C) 2017  StructorizerParserTemplate.pgt: Kay Gürtzig

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or any
    later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package lu.fisch.structorizer.syntax;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Class to parse a LALR(1) Structorizer expression grammar and build Expression objects
 *
 ******************************************************************************************************
 *
 *      Revision List (Template File!)
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2017-03-02      First Issue for Iden GOLDEngine
 *      Kay Gürtzig     2017-03-11      Parameter annotations and some comments corrected, indentation unified
 *      Kay Gürtzig     2018-03-26      Imports revised
 *      Kay Gürtzig     2018-06-30      Enh. #553: hooks for possible thread cancellation inserted.
 *
 ******************************************************************************************************
 *
 *      Revision List (this parser)
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2018.10.23      First Issue (generated with GOLDprog.exe)
 *      Kay Gürtzig     2021-02-25      Grammar table updated/corrected
 *
 ******************************************************************************************************
 *
 *     Comment:		
 *     Licensed Material - Property of Ralph Iden (GOLDParser) and Mathew Hawkins (parts of the template)
 *     GOLDParser - code downloaded from https://github.com/ridencww/goldengine on 2017-03-05.<br>
 *     Modifications to this code are allowed as it is a helper class to use the engine.<br>
 *     Template File:  StructorizerParserTemplate.pgt (with elements of both<br>
 *                     Java-MatthewHawkins.pgt and Java-IdenEngine.pgt)<br>
 *     Authors:        Ralph Iden, Matthew Hawkins, Bob Fisch, Kay Gürtzig<br>
 *     Description:    A Sample class, takes in a file and runs the GOLDParser engine on it.<br>
 *
 ******************************************************************************************************/

import java.util.logging.Logger;

import com.creativewidgetworks.goldparser.engine.*;
import com.creativewidgetworks.goldparser.engine.enums.SymbolType;

import lu.fisch.structorizer.parsers.AuParser;

/**
 * Code import parser class of Structorizer 3.27, based on GOLDParser 5.0 for the LALR(1) Structorizer expression grammar, derived from Java grammar language.
 * This file contains grammar-specific constants and individual routines to build
 * structograms (Nassi-Shneiderman diagrams) from the parsing tree. 
 * @author Kay Gürtzig
 */
public class ExprParser /*extends CodeParser*/
{

	//---------------------- Grammar specification ---------------------------

	protected final String getCompiledGrammar()
	{
		return "Structorizer2.egt";
	}
	
	protected final String getGrammarTableName()
	{
		return "StructorizerExpr";
	}

	/**
	 * The generic LALR(1) parser providing the parse tree
	 */
	protected AuParser parser;

	//-------------------------------- Logger --------------------------------

	/**
	 * Maximum line width for listing expected tokens in the problem string (used for
	 * line wrapping)
	 */
	protected final int PRBL_LINE_WIDTH = 100;

	private Logger logger;
	/** @return the standard Java logger for this class */
	protected Logger getLogger()
	{
		if (this.logger == null) {
			this.logger = Logger.getLogger(getClass().getName());
		}
		return this.logger;
	}

	//------------------------------ Constructor -----------------------------

	private static ExprParser singleton = null;
	/**
	 * Constructs a parser for Structorizer expressions, loads the grammar as resource and
	 * gets ready to parse an expression or expression list
	 */
	private ExprParser() {
		parser = new AuParser(
				getClass().getResourceAsStream(getCompiledGrammar()),
				getGrammarTableName(),
				true,
				null);
	}
	
	/**
	 * Provides the parser instance for Structorizer expressions with the grammar loaded as resource.<br/>
	 * Beware of concurrent use!
	 */
	public static ExprParser getInstance()
	{
		if (singleton == null) {
			synchronized(ExprParser.class) {
				singleton = new ExprParser();
			}
		}
		return singleton;
	}

	//---------------------- Grammar table constants DON'T MODIFY! ---------------------------

	// Symbolic constants naming the table indices of the symbols of the grammar 
	@SuppressWarnings("unused")
	private interface SymbolConstants 
	{
		final int SYM_EOF                                  =  0;  // (EOF)
		final int SYM_ERROR                                =  1;  // (Error)
		final int SYM_WHITESPACE                           =  2;  // Whitespace
		final int SYM_MINUS                                =  3;  // '-'
		final int SYM_MINUSMINUS                           =  4;  // '--'
		final int SYM_EXCLAM                               =  5;  // '!'
		final int SYM_EXCLAMEQ                             =  6;  // '!='
		final int SYM_PERCENT                              =  7;  // '%'
		final int SYM_AMP                                  =  8;  // '&'
		final int SYM_AMPAMP                               =  9;  // '&&'
		final int SYM_LPAREN                               = 10;  // '('
		final int SYM_RPAREN                               = 11;  // ')'
		final int SYM_TIMES                                = 12;  // '*'
		final int SYM_COMMA                                = 13;  // ','
		final int SYM_DOT                                  = 14;  // '.'
		final int SYM_DIV                                  = 15;  // '/'
		final int SYM_COLON                                = 16;  // ':'
		final int SYM_QUESTION                             = 17;  // '?'
		final int SYM_LBRACKET                             = 18;  // '['
		final int SYM_RBRACKET                             = 19;  // ']'
		final int SYM_CARET                                = 20;  // '^'
		final int SYM_LBRACE                               = 21;  // '{'
		final int SYM_PIPE                                 = 22;  // '|'
		final int SYM_PIPEPIPE                             = 23;  // '||'
		final int SYM_RBRACE                               = 24;  // '}'
		final int SYM_TILDE                                = 25;  // '~'
		final int SYM_PLUS                                 = 26;  // '+'
		final int SYM_PLUSPLUS                             = 27;  // '++'
		final int SYM_LT                                   = 28;  // '<'
		final int SYM_LTLT                                 = 29;  // '<<'
		final int SYM_LTEQ                                 = 30;  // '<='
		final int SYM_LTGT                                 = 31;  // '<>'
		final int SYM_EQ                                   = 32;  // '='
		final int SYM_EQEQ                                 = 33;  // '=='
		final int SYM_GT                                   = 34;  // '>'
		final int SYM_GTEQ                                 = 35;  // '>='
		final int SYM_GTGT                                 = 36;  // '>>'
		final int SYM_GTGTGT                               = 37;  // '>>>'
		final int SYM_AND                                  = 38;  // and
		final int SYM_BOOLEANLITERAL                       = 39;  // BooleanLiteral
		final int SYM_DIV2                                 = 40;  // div
		final int SYM_FLOATINGPOINTLITERAL                 = 41;  // FloatingPointLiteral
		final int SYM_FLOATINGPOINTLITERALEXPONENT         = 42;  // FloatingPointLiteralExponent
		final int SYM_HEXESCAPECHARLITERAL                 = 43;  // HexEscapeCharLiteral
		final int SYM_HEXINTEGERLITERAL                    = 44;  // HexIntegerLiteral
		final int SYM_IDENTIFIER                           = 45;  // Identifier
		final int SYM_INDIRECTCHARLITERAL                  = 46;  // IndirectCharLiteral
		final int SYM_MOD                                  = 47;  // mod
		final int SYM_NOT                                  = 48;  // not
		final int SYM_NULLLITERAL                          = 49;  // NullLiteral
		final int SYM_OCTALESCAPECHARLITERAL               = 50;  // OctalEscapeCharLiteral
		final int SYM_OCTALINTEGERLITERAL                  = 51;  // OctalIntegerLiteral
		final int SYM_OR                                   = 52;  // or
		final int SYM_SHL                                  = 53;  // shl
		final int SYM_SHR                                  = 54;  // shr
		final int SYM_STANDARDESCAPECHARLITERAL            = 55;  // StandardEscapeCharLiteral
		final int SYM_STARTWITHNOZERODECIMALINTEGERLITERAL = 56;  // StartWithNoZeroDecimalIntegerLiteral
		final int SYM_STARTWITHZERODECIMALINTEGERLITERAL   = 57;  // StartWithZeroDecimalIntegerLiteral
		final int SYM_STRINGLITERAL                        = 58;  // StringLiteral
		final int SYM_ADDITIVEEXPRESSION                   = 59;  // <AdditiveExpression>
		final int SYM_ANDEXPRESSION                        = 60;  // <AndExpression>
		final int SYM_ARRAYACCESS                          = 61;  // <ArrayAccess>
		final int SYM_ARRAYINITIALIZER                     = 62;  // <ArrayInitializer>
		final int SYM_ASSIGNMENTEXPRESSION                 = 63;  // <AssignmentExpression>
		final int SYM_CHARACTERLITERAL                     = 64;  // <CharacterLiteral>
		final int SYM_COMPONENTINITIALIZER                 = 65;  // <ComponentInitializer>
		final int SYM_COMPONENTINITIALIZERS                = 66;  // <ComponentInitializers>
		final int SYM_CONDITIONALANDEXPRESSION             = 67;  // <ConditionalAndExpression>
		final int SYM_CONDITIONALEXPRESSION                = 68;  // <ConditionalExpression>
		final int SYM_CONDITIONALOREXPRESSION              = 69;  // <ConditionalOrExpression>
		final int SYM_DECIMALINTEGERLITERAL                = 70;  // <DecimalIntegerLiteral>
		final int SYM_EQUALITYEXPRESSION                   = 71;  // <EqualityExpression>
		final int SYM_EXCLUSIVEOREXPRESSION                = 72;  // <ExclusiveOrExpression>
		final int SYM_EXPRESSION                           = 73;  // <Expression>
		final int SYM_EXPRESSIONLIST                       = 74;  // <ExpressionList>
		final int SYM_FIELDACCESS                          = 75;  // <FieldAccess>
		final int SYM_FLOATPOINTLITERAL                    = 76;  // <FloatPointLiteral>
		final int SYM_INCLUSIVEOREXPRESSION                = 77;  // <InclusiveOrExpression>
		final int SYM_INTEGERLITERAL                       = 78;  // <IntegerLiteral>
		final int SYM_LITERAL                              = 79;  // <Literal>
		final int SYM_METHODINVOCATION                     = 80;  // <MethodInvocation>
		final int SYM_MULTIPLICATIVEEXPRESSION             = 81;  // <MultiplicativeExpression>
		final int SYM_NAME                                 = 82;  // <Name>
		final int SYM_POSTDECREMENTEXPRESSION              = 83;  // <PostDecrementExpression>
		final int SYM_POSTFIXEXPRESSION                    = 84;  // <PostfixExpression>
		final int SYM_POSTINCREMENTEXPRESSION              = 85;  // <PostIncrementExpression>
		final int SYM_PREDECREMENTEXPRESSION               = 86;  // <PreDecrementExpression>
		final int SYM_PREINCREMENTEXPRESSION               = 87;  // <PreIncrementExpression>
		final int SYM_PRIMARY                              = 88;  // <Primary>
		final int SYM_QUALIFIEDNAME                        = 89;  // <QualifiedName>
		final int SYM_RECORDINITIALIZER                    = 90;  // <RecordInitializer>
		final int SYM_RELATIONALEXPRESSION                 = 91;  // <RelationalExpression>
		final int SYM_SHIFTEXPRESSION                      = 92;  // <ShiftExpression>
		final int SYM_SIMPLENAME                           = 93;  // <SimpleName>
		final int SYM_UNARYEXPRESSION                      = 94;  // <UnaryExpression>
		final int SYM_UNARYEXPRESSIONNOTPLUSMINUS          = 95;  // <UnaryExpressionNotPlusMinus>
	};

	// Symbolic constants naming the table indices of the grammar rules
	@SuppressWarnings("unused")
	private interface RuleConstants
	{
		final int PROD_CHARACTERLITERAL_INDIRECTCHARLITERAL                       =  0;  // <CharacterLiteral> ::= IndirectCharLiteral
		final int PROD_CHARACTERLITERAL_STANDARDESCAPECHARLITERAL                 =  1;  // <CharacterLiteral> ::= StandardEscapeCharLiteral
		final int PROD_CHARACTERLITERAL_OCTALESCAPECHARLITERAL                    =  2;  // <CharacterLiteral> ::= OctalEscapeCharLiteral
		final int PROD_CHARACTERLITERAL_HEXESCAPECHARLITERAL                      =  3;  // <CharacterLiteral> ::= HexEscapeCharLiteral
		final int PROD_DECIMALINTEGERLITERAL_STARTWITHZERODECIMALINTEGERLITERAL   =  4;  // <DecimalIntegerLiteral> ::= StartWithZeroDecimalIntegerLiteral
		final int PROD_DECIMALINTEGERLITERAL_STARTWITHNOZERODECIMALINTEGERLITERAL =  5;  // <DecimalIntegerLiteral> ::= StartWithNoZeroDecimalIntegerLiteral
		final int PROD_FLOATPOINTLITERAL_FLOATINGPOINTLITERAL                     =  6;  // <FloatPointLiteral> ::= FloatingPointLiteral
		final int PROD_FLOATPOINTLITERAL_FLOATINGPOINTLITERALEXPONENT             =  7;  // <FloatPointLiteral> ::= FloatingPointLiteralExponent
		final int PROD_INTEGERLITERAL                                             =  8;  // <IntegerLiteral> ::= <DecimalIntegerLiteral>
		final int PROD_INTEGERLITERAL_HEXINTEGERLITERAL                           =  9;  // <IntegerLiteral> ::= HexIntegerLiteral
		final int PROD_INTEGERLITERAL_OCTALINTEGERLITERAL                         = 10;  // <IntegerLiteral> ::= OctalIntegerLiteral
		final int PROD_LITERAL                                                    = 11;  // <Literal> ::= <IntegerLiteral>
		final int PROD_LITERAL2                                                   = 12;  // <Literal> ::= <FloatPointLiteral>
		final int PROD_LITERAL_BOOLEANLITERAL                                     = 13;  // <Literal> ::= BooleanLiteral
		final int PROD_LITERAL3                                                   = 14;  // <Literal> ::= <CharacterLiteral>
		final int PROD_LITERAL_STRINGLITERAL                                      = 15;  // <Literal> ::= StringLiteral
		final int PROD_LITERAL_NULLLITERAL                                        = 16;  // <Literal> ::= NullLiteral
		final int PROD_NAME                                                       = 17;  // <Name> ::= <SimpleName>
		final int PROD_NAME2                                                      = 18;  // <Name> ::= <QualifiedName>
		final int PROD_SIMPLENAME_IDENTIFIER                                      = 19;  // <SimpleName> ::= Identifier
		final int PROD_QUALIFIEDNAME_DOT_IDENTIFIER                               = 20;  // <QualifiedName> ::= <Name> '.' Identifier
		final int PROD_ARRAYINITIALIZER_LBRACE_RBRACE                             = 21;  // <ArrayInitializer> ::= '{' <ExpressionList> '}'
		final int PROD_ARRAYINITIALIZER_LBRACE_RBRACE2                            = 22;  // <ArrayInitializer> ::= '{' '}'
		final int PROD_RECORDINITIALIZER_IDENTIFIER_LBRACE_RBRACE                 = 23;  // <RecordInitializer> ::= Identifier '{' <ComponentInitializers> '}'
		final int PROD_RECORDINITIALIZER_IDENTIFIER_LBRACE_RBRACE2                = 24;  // <RecordInitializer> ::= Identifier '{' '}'
		final int PROD_EXPRESSIONLIST                                             = 25;  // <ExpressionList> ::= <Expression>
		final int PROD_EXPRESSIONLIST_COMMA                                       = 26;  // <ExpressionList> ::= <ExpressionList> ',' <Expression>
		final int PROD_COMPONENTINITIALIZERS                                      = 27;  // <ComponentInitializers> ::= <ComponentInitializer>
		final int PROD_COMPONENTINITIALIZERS_COMMA                                = 28;  // <ComponentInitializers> ::= <ComponentInitializers> ',' <ComponentInitializer>
		final int PROD_COMPONENTINITIALIZER_IDENTIFIER_COLON                      = 29;  // <ComponentInitializer> ::= Identifier ':' <Expression>
		final int PROD_COMPONENTINITIALIZER                                       = 30;  // <ComponentInitializer> ::= <Expression>
		final int PROD_PRIMARY                                                    = 31;  // <Primary> ::= <Literal>
		final int PROD_PRIMARY_LPAREN_RPAREN                                      = 32;  // <Primary> ::= '(' <Expression> ')'
		final int PROD_PRIMARY2                                                   = 33;  // <Primary> ::= <FieldAccess>
		final int PROD_PRIMARY3                                                   = 34;  // <Primary> ::= <MethodInvocation>
		final int PROD_PRIMARY4                                                   = 35;  // <Primary> ::= <ArrayAccess>
		final int PROD_FIELDACCESS_DOT_IDENTIFIER                                 = 36;  // <FieldAccess> ::= <Primary> '.' Identifier
		final int PROD_METHODINVOCATION_LPAREN_RPAREN                             = 37;  // <MethodInvocation> ::= <Name> '(' <ExpressionList> ')'
		final int PROD_METHODINVOCATION_LPAREN_RPAREN2                            = 38;  // <MethodInvocation> ::= <Name> '(' ')'
		final int PROD_METHODINVOCATION_DOT_IDENTIFIER_LPAREN_RPAREN              = 39;  // <MethodInvocation> ::= <Primary> '.' Identifier '(' <ExpressionList> ')'
		final int PROD_METHODINVOCATION_DOT_IDENTIFIER_LPAREN_RPAREN2             = 40;  // <MethodInvocation> ::= <Primary> '.' Identifier '(' ')'
		final int PROD_ARRAYACCESS_LBRACKET_RBRACKET                              = 41;  // <ArrayAccess> ::= <Name> '[' <ExpressionList> ']'
		final int PROD_ARRAYACCESS_LBRACKET_RBRACKET2                             = 42;  // <ArrayAccess> ::= <Primary> '[' <ExpressionList> ']'
		final int PROD_POSTFIXEXPRESSION                                          = 43;  // <PostfixExpression> ::= <Primary>
		final int PROD_POSTFIXEXPRESSION2                                         = 44;  // <PostfixExpression> ::= <Name>
		final int PROD_POSTFIXEXPRESSION3                                         = 45;  // <PostfixExpression> ::= <PostIncrementExpression>
		final int PROD_POSTFIXEXPRESSION4                                         = 46;  // <PostfixExpression> ::= <PostDecrementExpression>
		final int PROD_POSTINCREMENTEXPRESSION_PLUSPLUS                           = 47;  // <PostIncrementExpression> ::= <PostfixExpression> '++'
		final int PROD_POSTDECREMENTEXPRESSION_MINUSMINUS                         = 48;  // <PostDecrementExpression> ::= <PostfixExpression> '--'
		final int PROD_UNARYEXPRESSION                                            = 49;  // <UnaryExpression> ::= <PreIncrementExpression>
		final int PROD_UNARYEXPRESSION2                                           = 50;  // <UnaryExpression> ::= <PreDecrementExpression>
		final int PROD_UNARYEXPRESSION_PLUS                                       = 51;  // <UnaryExpression> ::= '+' <UnaryExpression>
		final int PROD_UNARYEXPRESSION_MINUS                                      = 52;  // <UnaryExpression> ::= '-' <UnaryExpression>
		final int PROD_UNARYEXPRESSION3                                           = 53;  // <UnaryExpression> ::= <UnaryExpressionNotPlusMinus>
		final int PROD_PREINCREMENTEXPRESSION_PLUSPLUS                            = 54;  // <PreIncrementExpression> ::= '++' <UnaryExpression>
		final int PROD_PREDECREMENTEXPRESSION_MINUSMINUS                          = 55;  // <PreDecrementExpression> ::= '--' <UnaryExpression>
		final int PROD_UNARYEXPRESSIONNOTPLUSMINUS                                = 56;  // <UnaryExpressionNotPlusMinus> ::= <PostfixExpression>
		final int PROD_UNARYEXPRESSIONNOTPLUSMINUS_TILDE                          = 57;  // <UnaryExpressionNotPlusMinus> ::= '~' <UnaryExpression>
		final int PROD_UNARYEXPRESSIONNOTPLUSMINUS_EXCLAM                         = 58;  // <UnaryExpressionNotPlusMinus> ::= '!' <UnaryExpression>
		final int PROD_UNARYEXPRESSIONNOTPLUSMINUS_NOT                            = 59;  // <UnaryExpressionNotPlusMinus> ::= not <UnaryExpression>
		final int PROD_MULTIPLICATIVEEXPRESSION                                   = 60;  // <MultiplicativeExpression> ::= <UnaryExpression>
		final int PROD_MULTIPLICATIVEEXPRESSION_TIMES                             = 61;  // <MultiplicativeExpression> ::= <MultiplicativeExpression> '*' <UnaryExpression>
		final int PROD_MULTIPLICATIVEEXPRESSION_DIV                               = 62;  // <MultiplicativeExpression> ::= <MultiplicativeExpression> '/' <UnaryExpression>
		final int PROD_MULTIPLICATIVEEXPRESSION_DIV2                              = 63;  // <MultiplicativeExpression> ::= <MultiplicativeExpression> div <UnaryExpression>
		final int PROD_MULTIPLICATIVEEXPRESSION_MOD                               = 64;  // <MultiplicativeExpression> ::= <MultiplicativeExpression> mod <UnaryExpression>
		final int PROD_MULTIPLICATIVEEXPRESSION_PERCENT                           = 65;  // <MultiplicativeExpression> ::= <MultiplicativeExpression> '%' <UnaryExpression>
		final int PROD_ADDITIVEEXPRESSION                                         = 66;  // <AdditiveExpression> ::= <MultiplicativeExpression>
		final int PROD_ADDITIVEEXPRESSION_PLUS                                    = 67;  // <AdditiveExpression> ::= <AdditiveExpression> '+' <MultiplicativeExpression>
		final int PROD_ADDITIVEEXPRESSION_MINUS                                   = 68;  // <AdditiveExpression> ::= <AdditiveExpression> '-' <MultiplicativeExpression>
		final int PROD_SHIFTEXPRESSION                                            = 69;  // <ShiftExpression> ::= <AdditiveExpression>
		final int PROD_SHIFTEXPRESSION_LTLT                                       = 70;  // <ShiftExpression> ::= <ShiftExpression> '<<' <AdditiveExpression>
		final int PROD_SHIFTEXPRESSION_GTGT                                       = 71;  // <ShiftExpression> ::= <ShiftExpression> '>>' <AdditiveExpression>
		final int PROD_SHIFTEXPRESSION_GTGTGT                                     = 72;  // <ShiftExpression> ::= <ShiftExpression> '>>>' <AdditiveExpression>
		final int PROD_SHIFTEXPRESSION_SHL                                        = 73;  // <ShiftExpression> ::= <ShiftExpression> shl <AdditiveExpression>
		final int PROD_SHIFTEXPRESSION_SHR                                        = 74;  // <ShiftExpression> ::= <ShiftExpression> shr <AdditiveExpression>
		final int PROD_RELATIONALEXPRESSION                                       = 75;  // <RelationalExpression> ::= <ShiftExpression>
		final int PROD_RELATIONALEXPRESSION_LT                                    = 76;  // <RelationalExpression> ::= <RelationalExpression> '<' <ShiftExpression>
		final int PROD_RELATIONALEXPRESSION_GT                                    = 77;  // <RelationalExpression> ::= <RelationalExpression> '>' <ShiftExpression>
		final int PROD_RELATIONALEXPRESSION_LTEQ                                  = 78;  // <RelationalExpression> ::= <RelationalExpression> '<=' <ShiftExpression>
		final int PROD_RELATIONALEXPRESSION_GTEQ                                  = 79;  // <RelationalExpression> ::= <RelationalExpression> '>=' <ShiftExpression>
		final int PROD_EQUALITYEXPRESSION                                         = 80;  // <EqualityExpression> ::= <RelationalExpression>
		final int PROD_EQUALITYEXPRESSION_EQEQ                                    = 81;  // <EqualityExpression> ::= <EqualityExpression> '==' <RelationalExpression>
		final int PROD_EQUALITYEXPRESSION_EQ                                      = 82;  // <EqualityExpression> ::= <EqualityExpression> '=' <RelationalExpression>
		final int PROD_EQUALITYEXPRESSION_EXCLAMEQ                                = 83;  // <EqualityExpression> ::= <EqualityExpression> '!=' <RelationalExpression>
		final int PROD_EQUALITYEXPRESSION_LTGT                                    = 84;  // <EqualityExpression> ::= <EqualityExpression> '<>' <RelationalExpression>
		final int PROD_ANDEXPRESSION                                              = 85;  // <AndExpression> ::= <EqualityExpression>
		final int PROD_ANDEXPRESSION_AMP                                          = 86;  // <AndExpression> ::= <AndExpression> '&' <EqualityExpression>
		final int PROD_EXCLUSIVEOREXPRESSION                                      = 87;  // <ExclusiveOrExpression> ::= <AndExpression>
		final int PROD_EXCLUSIVEOREXPRESSION_CARET                                = 88;  // <ExclusiveOrExpression> ::= <ExclusiveOrExpression> '^' <AndExpression>
		final int PROD_INCLUSIVEOREXPRESSION                                      = 89;  // <InclusiveOrExpression> ::= <ExclusiveOrExpression>
		final int PROD_INCLUSIVEOREXPRESSION_PIPE                                 = 90;  // <InclusiveOrExpression> ::= <InclusiveOrExpression> '|' <ExclusiveOrExpression>
		final int PROD_CONDITIONALANDEXPRESSION                                   = 91;  // <ConditionalAndExpression> ::= <InclusiveOrExpression>
		final int PROD_CONDITIONALANDEXPRESSION_AMPAMP                            = 92;  // <ConditionalAndExpression> ::= <ConditionalAndExpression> '&&' <InclusiveOrExpression>
		final int PROD_CONDITIONALANDEXPRESSION_AND                               = 93;  // <ConditionalAndExpression> ::= <ConditionalAndExpression> and <InclusiveOrExpression>
		final int PROD_CONDITIONALOREXPRESSION                                    = 94;  // <ConditionalOrExpression> ::= <ConditionalAndExpression>
		final int PROD_CONDITIONALOREXPRESSION_PIPEPIPE                           = 95;  // <ConditionalOrExpression> ::= <ConditionalOrExpression> '||' <ConditionalAndExpression>
		final int PROD_CONDITIONALOREXPRESSION_OR                                 = 96;  // <ConditionalOrExpression> ::= <ConditionalOrExpression> or <ConditionalAndExpression>
		final int PROD_CONDITIONALEXPRESSION                                      = 97;  // <ConditionalExpression> ::= <ConditionalOrExpression>
		final int PROD_CONDITIONALEXPRESSION_QUESTION_COLON                       = 98;  // <ConditionalExpression> ::= <ConditionalOrExpression> '?' <Expression> ':' <ConditionalExpression>
		final int PROD_ASSIGNMENTEXPRESSION                                       = 99;  // <AssignmentExpression> ::= <ConditionalExpression>
		final int PROD_EXPRESSION                                                 = 100;  // <Expression> ::= <AssignmentExpression>
		final int PROD_EXPRESSION2                                                = 101;  // <Expression> ::= <ArrayInitializer>
		final int PROD_EXPRESSION3                                                = 102;  // <Expression> ::= <RecordInitializer>
	};

	public Expression parse(String _textToParse, StringBuilder problems)
	{
		Expression expr = null;
//		synchronized(this) {
			boolean parsedWithoutError = parser.parseSourceStatements(_textToParse);

			if (parsedWithoutError) {
				Reduction red = parser.getCurrentReduction();
				expr = new Expression(null); 
				buildExpr_R(red, expr);
			}
			else {
				Position pos = parser.getCurrentPosition();
				int colNo = pos.getColumn() - 1;
				problems.append(parser.getErrorMessage());
				String line = _textToParse + " »";
				if (_textToParse.length() >= colNo) {
					line = _textToParse.substring(0, colNo) + " » " + _textToParse.substring(colNo);
				}
				problems.append("\n" + line.replace("\t", "    "));
				SymbolList sl = parser.getExpectedSymbols();
				Token token = parser.getCurrentToken();
				//final String tokVal = token.toString();
				final String tokVal = (token == null) ? "ε (END OF TEXT)" : token.toString();
				// END KGU#511 2018-04-12
				problems.append("\n\nFound token " + tokVal);
				problems.append("\n\nExpected: ");
				String sepa = "";
				String exp = "";
				for (Symbol sym: sl) {
					exp += sepa + sym.toString();
					sepa = " | ";
					if (exp.length() > PRBL_LINE_WIDTH) {
						problems.append(exp);
						exp = "";
						sepa = "\n        | ";
					}
				}
				problems.append(exp);
			}
//		}

		return expr;
	}
	
	//---------------------- Build methods for Expression objects ---------------------------

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.parsers.CodeParser#buildNSD_R(com.creativewidgetworks.goldparser.engine.Reduction, lu.fisch.structorizer.elements.Subqueue)
	 */
	protected void buildExpr_R(Reduction _reduction, Expression _parentNode)
	{
		//String content = new String();
		if (_reduction.size() > 0)
		{
			String rule = _reduction.getParent().toString();
			String ruleHead = _reduction.getParent().getHead().toString();
			int ruleId = _reduction.getParent().getTableIndex();
			//System.out.println("buildNSD_R(" + rule + ", " + _parentNode.parent + ")...");

			switch (ruleId) {
			/* -------- Begin code example for tree analysis and build -------- */
//			// Assignment or procedure call?
//			case RuleConstants.PROD_OPASSIGN_EQ:
//			case RuleConstants.PROD_VALUE_ID_LPAREN_RPAREN:
//			case RuleConstants.PROD_VALUE_ID_LPAREN_RPAREN2:
//			case RuleConstants.PROD_VALUE_ID_LPAREN_RPAREN:
//			{
//				// Simply convert it as text and create an instruction. In case of a call
//				// we'll try to transmute it after all subroutines will have been parsed.
//				String content = new String();
//				content = getContent_R(_reduction, content).trim();
//				//System.out.println(ruleName + ": " + content);
//				// In case of a variable declaration get rid of the trailing semicolon
//				//if (content.endsWith(";")) {
//				//	content = content.substring(0, content.length() - 1).trim();
//				//}
//				_parentNode.addElement(new Instruction(translateContent(content)));
//			}
//			break;
			/* -------- End code example for tree analysis and build -------- */
			default:
				if (_reduction.size()>0)
				{
					for (int i=0; i<_reduction.size(); i++)
					{
						if (_reduction.get(i).getType() == SymbolType.NON_TERMINAL)
						{
							buildExpr_R(_reduction.get(i).asReduction(), _parentNode);
						}
					}
				}
			}
		}
	}

	/**
	 * Helper method to retrieve and compose the text of the given reduction, combine it with previously
	 * assembled string _content and adapt it to syntactical conventions of Structorizer. Finally return
	 * the text phrase.
	 * @param _content - A string already assembled, may be used as prefix, ignored or combined in another
	 * way 
	 * @return composed and translated text.
	 */
	private String translateContent(String _content)
	{
		//String output = getKeyword("output");
		//String input = getKeyword("input");
		
		
		//System.out.println(_content);
		
		/*
		 _content:=ReplaceEntities(_content);
		*/
		
		// Convert the pseudo function back to array initializers
//		int posIni = _content.indexOf(arrayIniFunc);
//		if (posIni >= 0) {
//			StringList items = Element.splitExpressionList(_content.substring(posIni + arrayIniFunc.length()), ",", true);
//			_content = _content.substring(0, posIni) + "{" + items.subSequence(0, items.count()-1).concatenate(", ") +
//					"}" + items.get(items.count()-1).substring(1);
//		}
		
		//_content = BString.replace(_content, ":="," \u2190 ");
		//_content = BString.replace(_content, " = "," <- "); already done by getContent_R()!

		return _content.trim();
	}
	
	protected String getContent_R(Reduction _reduction, String _content)
	{
		for(int i=0; i<_reduction.size(); i++)
		{
			Token token = _reduction.get(i);
			/* -------- Begin code example for text retrieval and translation -------- */
			switch (token.getType()) 
			{
			case NON_TERMINAL:
//				int ruleId = _reduction.getParent().getTableIndex();
//				_content = getContent_R(token.asReduction(), _content);	
				break;
			case CONTENT:
//				{
//					String toAdd = "";
//					int idx = token.getTableIndex();
//					switch (idx) {
//					case SymbolConstants.SYM_EXCLAM:
//						_content += " not ";
//						break;
//					...
//					}
//				}
				break;
			default:
				break;
			}
			/* -------- End code example for text retrieval and translation -------- */
		}
		
		return _content;
	}


}
