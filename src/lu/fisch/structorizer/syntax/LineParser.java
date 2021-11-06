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
 *      Kay Gürtzig     2021-02-17      Some updates to interface changes and e.g. date formatting
 *
 ******************************************************************************************************
 *
 *      Revision List (this parser)
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2018.10.23      First Issue (generated with GOLDprog.exe)
 *      Kay Gürtzig     2021-02-25      Grammar table updated/corrected
 *      Kay Gürtzig     2021-11-03      Adapted to new grammar StructorizerLine.grm
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
 * Code import parser class of Structorizer 3.32-05, based on GOLDParser 5.0 for the LALR(1)
 * Structorizer line grammar, derived from Java grammar language, concentrating on the expression
 * part of it.<br/>
 * This file contains grammar-specific constants and individual routines to build
 * structograms (Nassi-Shneiderman diagrams) from the parsing tree. 
 * @author Kay Gürtzig
 */
public class ExprParser /*extends CodeParser*/
{

	//---------------------- Grammar specification ---------------------------

	protected final String getCompiledGrammar()
	{
		return "StructorizerLine.egt";
	}
	
	protected final String getGrammarTableName()
	{
		return "StructorizerLine";
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
		final int SYM_EOF                                  =   0;  // (EOF)
		final int SYM_ERROR                                =   1;  // (Error)
		final int SYM_WHITESPACE                           =   2;  // Whitespace
		final int SYM_MINUS                                =   3;  // '-'
		final int SYM_MINUSMINUS                           =   4;  // '--'
		final int SYM_EXCLAM                               =   5;  // '!'
		final int SYM_EXCLAMEQ                             =   6;  // '!='
		final int SYM_PERCENT                              =   7;  // '%'
		final int SYM_AMP                                  =   8;  // '&'
		final int SYM_AMPAMP                               =   9;  // '&&'
		final int SYM_LPAREN                               =  10;  // '('
		final int SYM_RPAREN                               =  11;  // ')'
		final int SYM_TIMES                                =  12;  // '*'
		final int SYM_COMMA                                =  13;  // ','
		final int SYM_DOT                                  =  14;  // '.'
		final int SYM_DOTDOT                               =  15;  // '..'
		final int SYM_DIV                                  =  16;  // '/'
		final int SYM_COLON                                =  17;  // ':'
		final int SYM_COLONEQ                              =  18;  // ':='
		final int SYM_SEMI                                 =  19;  // ';'
		final int SYM_QUESTION                             =  20;  // '?'
		final int SYM_LBRACKET                             =  21;  // '['
		final int SYM_LBRACKETRBRACKET                     =  22;  // '[]'
		final int SYM_RBRACKET                             =  23;  // ']'
		final int SYM_CARET                                =  24;  // '^'
		final int SYM_LBRACE                               =  25;  // '{'
		final int SYM_PIPE                                 =  26;  // '|'
		final int SYM_PIPEPIPE                             =  27;  // '||'
		final int SYM_RBRACE                               =  28;  // '}'
		final int SYM_TILDE                                =  29;  // '~'
		final int SYM_PLUS                                 =  30;  // '+'
		final int SYM_PLUSPLUS                             =  31;  // '++'
		final int SYM_LT                                   =  32;  // '<'
		final int SYM_LTMINUS                              =  33;  // '<-'
		final int SYM_LTLT                                 =  34;  // '<<'
		final int SYM_LTEQ                                 =  35;  // '<='
		final int SYM_LTGT                                 =  36;  // '<>'
		final int SYM_EQ                                   =  37;  // '='
		final int SYM_EQEQ                                 =  38;  // '=='
		final int SYM_GT                                   =  39;  // '>'
		final int SYM_GTEQ                                 =  40;  // '>='
		final int SYM_GTGT                                 =  41;  // '>>'
		final int SYM_GTGTGT                               =  42;  // '>>>'
		final int SYM_ANDOPR                               =  43;  // AndOpr
		final int SYM_ARRAYKEY                             =  44;  // ArrayKey
		final int SYM_ASKEY                                =  45;  // AsKey
		final int SYM_BOOLEANLITERAL                       =  46;  // BooleanLiteral
		final int SYM_CALLKEY                              =  47;  // CallKey
		final int SYM_CASEKEY                              =  48;  // CaseKey
		final int SYM_CATCHKEY                             =  49;  // CatchKey
		final int SYM_CONDKEY                              =  50;  // CondKey
		final int SYM_CONSTKEY                             =  51;  // ConstKey
		final int SYM_DIMKEY                               =  52;  // DimKey
		final int SYM_DIVOPR                               =  53;  // DivOpr
		final int SYM_ENUM                                 =  54;  // enum
		final int SYM_EXITKEY                              =  55;  // ExitKey
		final int SYM_FLOATINGPOINTLITERAL                 =  56;  // FloatingPointLiteral
		final int SYM_FLOATINGPOINTLITERALEXPONENT         =  57;  // FloatingPointLiteralExponent
		final int SYM_FORINKEY                             =  58;  // ForInKey
		final int SYM_FORKEY                               =  59;  // ForKey
		final int SYM_HEXESCAPECHARLITERAL                 =  60;  // HexEscapeCharLiteral
		final int SYM_HEXINTEGERLITERAL                    =  61;  // HexIntegerLiteral
		final int SYM_IDENTIFIER                           =  62;  // Identifier
		final int SYM_INDIRECTCHARLITERAL                  =  63;  // IndirectCharLiteral
		final int SYM_INKEY                                =  64;  // InKey
		final int SYM_INPUTKEY                             =  65;  // InputKey
		final int SYM_LEAVEKEY                             =  66;  // LeaveKey
		final int SYM_MODOPR                               =  67;  // ModOpr
		final int SYM_NOT                                  =  68;  // not
		final int SYM_NULLLITERAL                          =  69;  // NullLiteral
		final int SYM_OCTALESCAPECHARLITERAL               =  70;  // OctalEscapeCharLiteral
		final int SYM_OCTALINTEGERLITERAL                  =  71;  // OctalIntegerLiteral
		final int SYM_OFKEY                                =  72;  // OfKey
		final int SYM_OROPR                                =  73;  // OrOpr
		final int SYM_OUTPUTKEY                            =  74;  // OutputKey
		final int SYM_RECORDKEY                            =  75;  // RecordKey
		final int SYM_RETURNKEY                            =  76;  // ReturnKey
		final int SYM_SELECTORKEY                          =  77;  // SelectorKey
		final int SYM_SHLOPR                               =  78;  // ShlOpr
		final int SYM_SHROPR                               =  79;  // ShrOpr
		final int SYM_STANDARDESCAPECHARLITERAL            =  80;  // StandardEscapeCharLiteral
		final int SYM_STARTWITHNOZERODECIMALINTEGERLITERAL =  81;  // StartWithNoZeroDecimalIntegerLiteral
		final int SYM_STARTWITHZERODECIMALINTEGERLITERAL   =  82;  // StartWithZeroDecimalIntegerLiteral
		final int SYM_STEPKEY                              =  83;  // StepKey
		final int SYM_STRINGLITERAL                        =  84;  // StringLiteral
		final int SYM_THROWKEY                             =  85;  // ThrowKey
		final int SYM_TOKEY                                =  86;  // ToKey
		final int SYM_TYPEKEY                              =  87;  // TypeKey
		final int SYM_VARKEY                               =  88;  // VarKey
		final int SYM_ADDITIVEEXPRESSION                   =  89;  // <AdditiveExpression>
		final int SYM_ANDEXPRESSION                        =  90;  // <AndExpression>
		final int SYM_ARRAYACCESS                          =  91;  // <ArrayAccess>
		final int SYM_ARRAYDECL                            =  92;  // <ArrayDecl>
		final int SYM_ARRAYINITIALIZER                     =  93;  // <ArrayInitializer>
		final int SYM_ARRAYOF                              =  94;  // <ArrayOf>
		final int SYM_ASSIGNMENT                           =  95;  // <Assignment>
		final int SYM_ASSIGNMENTEXPRESSION                 =  96;  // <AssignmentExpression>
		final int SYM_ASSIGNMENTOPERATOR                   =  97;  // <AssignmentOperator>
		final int SYM_CALL                                 =  98;  // <Call>
		final int SYM_CASEDISCRIMINATOR                    =  99;  // <CaseDiscriminator>
		final int SYM_CASESELECTORS                        = 100;  // <CaseSelectors>
		final int SYM_CATCHCLAUSE                          = 101;  // <CatchClause>
		final int SYM_CHARACTERLITERAL                     = 102;  // <CharacterLiteral>
		final int SYM_COMPONENTGROUP                       = 103;  // <ComponentGroup>
		final int SYM_COMPONENTINITIALIZER                 = 104;  // <ComponentInitializer>
		final int SYM_COMPONENTINITIALIZERLIST             = 105;  // <ComponentInitializerList>
		final int SYM_COMPONENTINITIALIZERS                = 106;  // <ComponentInitializers>
		final int SYM_COMPONENTLIST                        = 107;  // <ComponentList>
		final int SYM_CONDITION                            = 108;  // <Condition>
		final int SYM_CONDITIONALANDEXPRESSION             = 109;  // <ConditionalAndExpression>
		final int SYM_CONDITIONALEXPRESSION                = 110;  // <ConditionalExpression>
		final int SYM_CONDITIONALOREXPRESSION              = 111;  // <ConditionalOrExpression>
		final int SYM_CONSTDEFINITION                      = 112;  // <ConstDefinition>
		final int SYM_DECIMALINTEGERLITERAL                = 113;  // <DecimalIntegerLiteral>
		final int SYM_DIMENSION                            = 114;  // <Dimension>
		final int SYM_DIMENSIONLIST                        = 115;  // <DimensionList>
		final int SYM_DIMS                                 = 116;  // <Dims>
		final int SYM_ELEMENTLINE                          = 117;  // <ElementLine>
		final int SYM_ENUMDEF                              = 118;  // <EnumDef>
		final int SYM_ENUMLIST                             = 119;  // <EnumList>
		final int SYM_EQUALITYEXPRESSION                   = 120;  // <EqualityExpression>
		final int SYM_EXCLUSIVEOREXPRESSION                = 121;  // <ExclusiveOrExpression>
		final int SYM_EXPRESSION                           = 122;  // <Expression>
		final int SYM_EXPRESSIONLIST                       = 123;  // <ExpressionList>
		final int SYM_FIELDACCESS                          = 124;  // <FieldAccess>
		final int SYM_FLOATPOINTLITERAL                    = 125;  // <FloatPointLiteral>
		final int SYM_FORHEADER                            = 126;  // <ForHeader>
		final int SYM_FORINHEADER                          = 127;  // <ForInHeader>
		final int SYM_IDLIST                               = 128;  // <IdList>
		final int SYM_INCLUSIVEOREXPRESSION                = 129;  // <InclusiveOrExpression>
		final int SYM_INPUTINSTRUCTION                     = 130;  // <InputInstruction>
		final int SYM_INTEGERLITERAL                       = 131;  // <IntegerLiteral>
		final int SYM_JUMP                                 = 132;  // <Jump>
		final int SYM_LEFTHANDSIDE                         = 133;  // <LeftHandSide>
		final int SYM_LITERAL                              = 134;  // <Literal>
		final int SYM_METHODINVOCATION                     = 135;  // <MethodInvocation>
		final int SYM_MULTIPLICATIVEEXPRESSION             = 136;  // <MultiplicativeExpression>
		final int SYM_NAME                                 = 137;  // <Name>
		final int SYM_OUTPUTINSTRUCTION                    = 138;  // <OutputInstruction>
		final int SYM_POSTDECREMENTEXPRESSION              = 139;  // <PostDecrementExpression>
		final int SYM_POSTFIXEXPRESSION                    = 140;  // <PostfixExpression>
		final int SYM_POSTINCREMENTEXPRESSION              = 141;  // <PostIncrementExpression>
		final int SYM_PREDECREMENTEXPRESSION               = 142;  // <PreDecrementExpression>
		final int SYM_PREINCREMENTEXPRESSION               = 143;  // <PreIncrementExpression>
		final int SYM_PRIMARY                              = 144;  // <Primary>
		final int SYM_PROMPT                               = 145;  // <Prompt>
		final int SYM_QUALIFIEDNAME                        = 146;  // <QualifiedName>
		final int SYM_RANGE                                = 147;  // <Range>
		final int SYM_RANGELIST                            = 148;  // <RangeList>
		final int SYM_RECORDINITIALIZER                    = 149;  // <RecordInitializer>
		final int SYM_RELATIONALEXPRESSION                 = 150;  // <RelationalExpression>
		final int SYM_ROUTINEINVOCATION                    = 151;  // <RoutineInvocation>
		final int SYM_SHIFTEXPRESSION                      = 152;  // <ShiftExpression>
		final int SYM_SIMPLENAME                           = 153;  // <SimpleName>
		final int SYM_STEPCLAUSE                           = 154;  // <StepClause>
		final int SYM_TARGETLIST                           = 155;  // <TargetList>
		final int SYM_TYPECONSTRUCTION                     = 156;  // <TypeConstruction>
		final int SYM_TYPEDEFINITION                       = 157;  // <TypeDefinition>
		final int SYM_TYPEDESCRIPTION                      = 158;  // <TypeDescription>
		final int SYM_UNARYEXPRESSION                      = 159;  // <UnaryExpression>
		final int SYM_UNARYEXPRESSIONNOTPLUSMINUS          = 160;  // <UnaryExpressionNotPlusMinus>
		final int SYM_VALUELIST                            = 161;  // <ValueList>
		final int SYM_VARDECLARATION                       = 162;  // <VarDeclaration>
		final int SYM_VARINITIALISATION                    = 163;  // <VarInitialisation>
	};

	// Symbolic constants naming the table indices of the grammar rules
	@SuppressWarnings("unused")
	private interface RuleConstants
	{
		final int PROD_ELEMENTLINE                                                =   0;  // <ElementLine> ::= <Assignment>
		final int PROD_ELEMENTLINE2                                               =   1;  // <ElementLine> ::= <VarInitialisation>
		final int PROD_ELEMENTLINE3                                               =   2;  // <ElementLine> ::= <InputInstruction>
		final int PROD_ELEMENTLINE4                                               =   3;  // <ElementLine> ::= <OutputInstruction>
		final int PROD_ELEMENTLINE5                                               =   4;  // <ElementLine> ::= <VarDeclaration>
		final int PROD_ELEMENTLINE6                                               =   5;  // <ElementLine> ::= <ConstDefinition>
		final int PROD_ELEMENTLINE7                                               =   6;  // <ElementLine> ::= <TypeDefinition>
		final int PROD_ELEMENTLINE8                                               =   7;  // <ElementLine> ::= <Condition>
		final int PROD_ELEMENTLINE9                                               =   8;  // <ElementLine> ::= <ForHeader>
		final int PROD_ELEMENTLINE10                                              =   9;  // <ElementLine> ::= <ForInHeader>
		final int PROD_ELEMENTLINE11                                              =  10;  // <ElementLine> ::= <Jump>
		final int PROD_ELEMENTLINE12                                              =  11;  // <ElementLine> ::= <Call>
		final int PROD_ELEMENTLINE13                                              =  12;  // <ElementLine> ::= <CatchClause>
		final int PROD_ELEMENTLINE14                                              =  13;  // <ElementLine> ::= <CaseDiscriminator>
		final int PROD_ELEMENTLINE15                                              =  14;  // <ElementLine> ::= <CaseSelectors>
		final int PROD_CHARACTERLITERAL_INDIRECTCHARLITERAL                       =  15;  // <CharacterLiteral> ::= IndirectCharLiteral
		final int PROD_CHARACTERLITERAL_STANDARDESCAPECHARLITERAL                 =  16;  // <CharacterLiteral> ::= StandardEscapeCharLiteral
		final int PROD_CHARACTERLITERAL_OCTALESCAPECHARLITERAL                    =  17;  // <CharacterLiteral> ::= OctalEscapeCharLiteral
		final int PROD_CHARACTERLITERAL_HEXESCAPECHARLITERAL                      =  18;  // <CharacterLiteral> ::= HexEscapeCharLiteral
		final int PROD_DECIMALINTEGERLITERAL_STARTWITHZERODECIMALINTEGERLITERAL   =  19;  // <DecimalIntegerLiteral> ::= StartWithZeroDecimalIntegerLiteral
		final int PROD_DECIMALINTEGERLITERAL_STARTWITHNOZERODECIMALINTEGERLITERAL =  20;  // <DecimalIntegerLiteral> ::= StartWithNoZeroDecimalIntegerLiteral
		final int PROD_FLOATPOINTLITERAL_FLOATINGPOINTLITERAL                     =  21;  // <FloatPointLiteral> ::= FloatingPointLiteral
		final int PROD_FLOATPOINTLITERAL_FLOATINGPOINTLITERALEXPONENT             =  22;  // <FloatPointLiteral> ::= FloatingPointLiteralExponent
		final int PROD_INTEGERLITERAL                                             =  23;  // <IntegerLiteral> ::= <DecimalIntegerLiteral>
		final int PROD_INTEGERLITERAL_HEXINTEGERLITERAL                           =  24;  // <IntegerLiteral> ::= HexIntegerLiteral
		final int PROD_INTEGERLITERAL_OCTALINTEGERLITERAL                         =  25;  // <IntegerLiteral> ::= OctalIntegerLiteral
		final int PROD_LITERAL                                                    =  26;  // <Literal> ::= <IntegerLiteral>
		final int PROD_LITERAL2                                                   =  27;  // <Literal> ::= <FloatPointLiteral>
		final int PROD_LITERAL_BOOLEANLITERAL                                     =  28;  // <Literal> ::= BooleanLiteral
		final int PROD_LITERAL3                                                   =  29;  // <Literal> ::= <CharacterLiteral>
		final int PROD_LITERAL_STRINGLITERAL                                      =  30;  // <Literal> ::= StringLiteral
		final int PROD_LITERAL_NULLLITERAL                                        =  31;  // <Literal> ::= NullLiteral
		final int PROD_NAME                                                       =  32;  // <Name> ::= <SimpleName>
		final int PROD_NAME2                                                      =  33;  // <Name> ::= <QualifiedName>
		final int PROD_SIMPLENAME_IDENTIFIER                                      =  34;  // <SimpleName> ::= Identifier
		final int PROD_QUALIFIEDNAME_DOT_IDENTIFIER                               =  35;  // <QualifiedName> ::= <Name> '.' Identifier
		final int PROD_ARRAYINITIALIZER_LBRACE_RBRACE                             =  36;  // <ArrayInitializer> ::= '{' <ExpressionList> '}'
		final int PROD_ARRAYINITIALIZER_LBRACE_RBRACE2                            =  37;  // <ArrayInitializer> ::= '{' '}'
		final int PROD_RECORDINITIALIZER_IDENTIFIER_LBRACE_RBRACE                 =  38;  // <RecordInitializer> ::= Identifier '{' <ComponentInitializers> '}'
		final int PROD_RECORDINITIALIZER_IDENTIFIER_LBRACE_RBRACE2                =  39;  // <RecordInitializer> ::= Identifier '{' '}'
		final int PROD_EXPRESSIONLIST                                             =  40;  // <ExpressionList> ::= <Expression>
		final int PROD_EXPRESSIONLIST_COMMA                                       =  41;  // <ExpressionList> ::= <ExpressionList> ',' <Expression>
		final int PROD_COMPONENTINITIALIZERS                                      =  42;  // <ComponentInitializers> ::= <ComponentInitializerList>
		final int PROD_COMPONENTINITIALIZERS2                                     =  43;  // <ComponentInitializers> ::= <ExpressionList>
		final int PROD_COMPONENTINITIALIZERS_COMMA                                =  44;  // <ComponentInitializers> ::= <ExpressionList> ',' <ComponentInitializerList>
		final int PROD_COMPONENTINITIALIZERLIST_COMMA                             =  45;  // <ComponentInitializerList> ::= <ComponentInitializerList> ',' <ComponentInitializer>
		final int PROD_COMPONENTINITIALIZERLIST                                   =  46;  // <ComponentInitializerList> ::= <ComponentInitializer>
		final int PROD_COMPONENTINITIALIZER_IDENTIFIER_COLON                      =  47;  // <ComponentInitializer> ::= Identifier ':' <Expression>
		final int PROD_PRIMARY                                                    =  48;  // <Primary> ::= <Literal>
		final int PROD_PRIMARY_LPAREN_RPAREN                                      =  49;  // <Primary> ::= '(' <Expression> ')'
		final int PROD_PRIMARY2                                                   =  50;  // <Primary> ::= <FieldAccess>
		final int PROD_PRIMARY3                                                   =  51;  // <Primary> ::= <MethodInvocation>
		final int PROD_PRIMARY4                                                   =  52;  // <Primary> ::= <ArrayAccess>
		final int PROD_FIELDACCESS_DOT_IDENTIFIER                                 =  53;  // <FieldAccess> ::= <Primary> '.' Identifier
		final int PROD_ROUTINEINVOCATION_LPAREN_RPAREN                            =  54;  // <RoutineInvocation> ::= <Name> '(' <ExpressionList> ')'
		final int PROD_ROUTINEINVOCATION_LPAREN_RPAREN2                           =  55;  // <RoutineInvocation> ::= <Name> '(' ')'
		final int PROD_METHODINVOCATION                                           =  56;  // <MethodInvocation> ::= <RoutineInvocation>
		final int PROD_METHODINVOCATION_DOT_IDENTIFIER_LPAREN_RPAREN              =  57;  // <MethodInvocation> ::= <Primary> '.' Identifier '(' <ExpressionList> ')'
		final int PROD_METHODINVOCATION_DOT_IDENTIFIER_LPAREN_RPAREN2             =  58;  // <MethodInvocation> ::= <Primary> '.' Identifier '(' ')'
		final int PROD_ARRAYACCESS_LBRACKET_RBRACKET                              =  59;  // <ArrayAccess> ::= <Name> '[' <ExpressionList> ']'
		final int PROD_ARRAYACCESS_LBRACKET_RBRACKET2                             =  60;  // <ArrayAccess> ::= <Primary> '[' <ExpressionList> ']'
		final int PROD_POSTFIXEXPRESSION                                          =  61;  // <PostfixExpression> ::= <Primary>
		final int PROD_POSTFIXEXPRESSION2                                         =  62;  // <PostfixExpression> ::= <Name>
		final int PROD_POSTFIXEXPRESSION3                                         =  63;  // <PostfixExpression> ::= <PostIncrementExpression>
		final int PROD_POSTFIXEXPRESSION4                                         =  64;  // <PostfixExpression> ::= <PostDecrementExpression>
		final int PROD_POSTINCREMENTEXPRESSION_PLUSPLUS                           =  65;  // <PostIncrementExpression> ::= <PostfixExpression> '++'
		final int PROD_POSTDECREMENTEXPRESSION_MINUSMINUS                         =  66;  // <PostDecrementExpression> ::= <PostfixExpression> '--'
		final int PROD_UNARYEXPRESSION                                            =  67;  // <UnaryExpression> ::= <PreIncrementExpression>
		final int PROD_UNARYEXPRESSION2                                           =  68;  // <UnaryExpression> ::= <PreDecrementExpression>
		final int PROD_UNARYEXPRESSION_PLUS                                       =  69;  // <UnaryExpression> ::= '+' <UnaryExpression>
		final int PROD_UNARYEXPRESSION_MINUS                                      =  70;  // <UnaryExpression> ::= '-' <UnaryExpression>
		final int PROD_UNARYEXPRESSION3                                           =  71;  // <UnaryExpression> ::= <UnaryExpressionNotPlusMinus>
		final int PROD_PREINCREMENTEXPRESSION_PLUSPLUS                            =  72;  // <PreIncrementExpression> ::= '++' <UnaryExpression>
		final int PROD_PREDECREMENTEXPRESSION_MINUSMINUS                          =  73;  // <PreDecrementExpression> ::= '--' <UnaryExpression>
		final int PROD_UNARYEXPRESSIONNOTPLUSMINUS                                =  74;  // <UnaryExpressionNotPlusMinus> ::= <PostfixExpression>
		final int PROD_UNARYEXPRESSIONNOTPLUSMINUS_TILDE                          =  75;  // <UnaryExpressionNotPlusMinus> ::= '~' <UnaryExpression>
		final int PROD_UNARYEXPRESSIONNOTPLUSMINUS_EXCLAM                         =  76;  // <UnaryExpressionNotPlusMinus> ::= '!' <UnaryExpression>
		final int PROD_UNARYEXPRESSIONNOTPLUSMINUS_NOT                            =  77;  // <UnaryExpressionNotPlusMinus> ::= not <UnaryExpression>
		final int PROD_MULTIPLICATIVEEXPRESSION                                   =  78;  // <MultiplicativeExpression> ::= <UnaryExpression>
		final int PROD_MULTIPLICATIVEEXPRESSION_TIMES                             =  79;  // <MultiplicativeExpression> ::= <MultiplicativeExpression> '*' <UnaryExpression>
		final int PROD_MULTIPLICATIVEEXPRESSION_DIV                               =  80;  // <MultiplicativeExpression> ::= <MultiplicativeExpression> '/' <UnaryExpression>
		final int PROD_MULTIPLICATIVEEXPRESSION_DIVOPR                            =  81;  // <MultiplicativeExpression> ::= <MultiplicativeExpression> DivOpr <UnaryExpression>
		final int PROD_MULTIPLICATIVEEXPRESSION_MODOPR                            =  82;  // <MultiplicativeExpression> ::= <MultiplicativeExpression> ModOpr <UnaryExpression>
		final int PROD_MULTIPLICATIVEEXPRESSION_PERCENT                           =  83;  // <MultiplicativeExpression> ::= <MultiplicativeExpression> '%' <UnaryExpression>
		final int PROD_ADDITIVEEXPRESSION                                         =  84;  // <AdditiveExpression> ::= <MultiplicativeExpression>
		final int PROD_ADDITIVEEXPRESSION_PLUS                                    =  85;  // <AdditiveExpression> ::= <AdditiveExpression> '+' <MultiplicativeExpression>
		final int PROD_ADDITIVEEXPRESSION_MINUS                                   =  86;  // <AdditiveExpression> ::= <AdditiveExpression> '-' <MultiplicativeExpression>
		final int PROD_SHIFTEXPRESSION                                            =  87;  // <ShiftExpression> ::= <AdditiveExpression>
		final int PROD_SHIFTEXPRESSION_LTLT                                       =  88;  // <ShiftExpression> ::= <ShiftExpression> '<<' <AdditiveExpression>
		final int PROD_SHIFTEXPRESSION_GTGT                                       =  89;  // <ShiftExpression> ::= <ShiftExpression> '>>' <AdditiveExpression>
		final int PROD_SHIFTEXPRESSION_GTGTGT                                     =  90;  // <ShiftExpression> ::= <ShiftExpression> '>>>' <AdditiveExpression>
		final int PROD_SHIFTEXPRESSION_SHLOPR                                     =  91;  // <ShiftExpression> ::= <ShiftExpression> ShlOpr <AdditiveExpression>
		final int PROD_SHIFTEXPRESSION_SHROPR                                     =  92;  // <ShiftExpression> ::= <ShiftExpression> ShrOpr <AdditiveExpression>
		final int PROD_RELATIONALEXPRESSION                                       =  93;  // <RelationalExpression> ::= <ShiftExpression>
		final int PROD_RELATIONALEXPRESSION_LT                                    =  94;  // <RelationalExpression> ::= <RelationalExpression> '<' <ShiftExpression>
		final int PROD_RELATIONALEXPRESSION_GT                                    =  95;  // <RelationalExpression> ::= <RelationalExpression> '>' <ShiftExpression>
		final int PROD_RELATIONALEXPRESSION_LTEQ                                  =  96;  // <RelationalExpression> ::= <RelationalExpression> '<=' <ShiftExpression>
		final int PROD_RELATIONALEXPRESSION_GTEQ                                  =  97;  // <RelationalExpression> ::= <RelationalExpression> '>=' <ShiftExpression>
		final int PROD_EQUALITYEXPRESSION                                         =  98;  // <EqualityExpression> ::= <RelationalExpression>
		final int PROD_EQUALITYEXPRESSION_EQEQ                                    =  99;  // <EqualityExpression> ::= <EqualityExpression> '==' <RelationalExpression>
		final int PROD_EQUALITYEXPRESSION_EQ                                      = 100;  // <EqualityExpression> ::= <EqualityExpression> '=' <RelationalExpression>
		final int PROD_EQUALITYEXPRESSION_EXCLAMEQ                                = 101;  // <EqualityExpression> ::= <EqualityExpression> '!=' <RelationalExpression>
		final int PROD_EQUALITYEXPRESSION_LTGT                                    = 102;  // <EqualityExpression> ::= <EqualityExpression> '<>' <RelationalExpression>
		final int PROD_ANDEXPRESSION                                              = 103;  // <AndExpression> ::= <EqualityExpression>
		final int PROD_ANDEXPRESSION_AMP                                          = 104;  // <AndExpression> ::= <AndExpression> '&' <EqualityExpression>
		final int PROD_EXCLUSIVEOREXPRESSION                                      = 105;  // <ExclusiveOrExpression> ::= <AndExpression>
		final int PROD_EXCLUSIVEOREXPRESSION_CARET                                = 106;  // <ExclusiveOrExpression> ::= <ExclusiveOrExpression> '^' <AndExpression>
		final int PROD_INCLUSIVEOREXPRESSION                                      = 107;  // <InclusiveOrExpression> ::= <ExclusiveOrExpression>
		final int PROD_INCLUSIVEOREXPRESSION_PIPE                                 = 108;  // <InclusiveOrExpression> ::= <InclusiveOrExpression> '|' <ExclusiveOrExpression>
		final int PROD_CONDITIONALANDEXPRESSION                                   = 109;  // <ConditionalAndExpression> ::= <InclusiveOrExpression>
		final int PROD_CONDITIONALANDEXPRESSION_AMPAMP                            = 110;  // <ConditionalAndExpression> ::= <ConditionalAndExpression> '&&' <InclusiveOrExpression>
		final int PROD_CONDITIONALANDEXPRESSION_ANDOPR                            = 111;  // <ConditionalAndExpression> ::= <ConditionalAndExpression> AndOpr <InclusiveOrExpression>
		final int PROD_CONDITIONALOREXPRESSION                                    = 112;  // <ConditionalOrExpression> ::= <ConditionalAndExpression>
		final int PROD_CONDITIONALOREXPRESSION_PIPEPIPE                           = 113;  // <ConditionalOrExpression> ::= <ConditionalOrExpression> '||' <ConditionalAndExpression>
		final int PROD_CONDITIONALOREXPRESSION_OROPR                              = 114;  // <ConditionalOrExpression> ::= <ConditionalOrExpression> OrOpr <ConditionalAndExpression>
		final int PROD_CONDITIONALEXPRESSION                                      = 115;  // <ConditionalExpression> ::= <ConditionalOrExpression>
		final int PROD_CONDITIONALEXPRESSION_QUESTION_COLON                       = 116;  // <ConditionalExpression> ::= <ConditionalOrExpression> '?' <Expression> ':' <ConditionalExpression>
		final int PROD_ASSIGNMENTEXPRESSION                                       = 117;  // <AssignmentExpression> ::= <ConditionalExpression>
		final int PROD_EXPRESSION                                                 = 118;  // <Expression> ::= <AssignmentExpression>
		final int PROD_EXPRESSION2                                                = 119;  // <Expression> ::= <ArrayInitializer>
		final int PROD_EXPRESSION3                                                = 120;  // <Expression> ::= <RecordInitializer>
		final int PROD_ASSIGNMENT                                                 = 121;  // <Assignment> ::= <LeftHandSide> <AssignmentOperator> <Expression>
		final int PROD_LEFTHANDSIDE                                               = 122;  // <LeftHandSide> ::= <Name>
		final int PROD_LEFTHANDSIDE2                                              = 123;  // <LeftHandSide> ::= <FieldAccess>
		final int PROD_LEFTHANDSIDE3                                              = 124;  // <LeftHandSide> ::= <ArrayAccess>
		final int PROD_ASSIGNMENTOPERATOR_LTMINUS                                 = 125;  // <AssignmentOperator> ::= '<-'
		final int PROD_ASSIGNMENTOPERATOR_COLONEQ                                 = 126;  // <AssignmentOperator> ::= ':='
		final int PROD_VARINITIALISATION                                          = 127;  // <VarInitialisation> ::= <VarDeclaration> <AssignmentOperator> <Expression>
		final int PROD_VARINITIALISATION2                                         = 128;  // <VarInitialisation> ::= <TypeDescription> <ArrayDecl> <AssignmentOperator> <Expression>
		final int PROD_ARRAYDECL_IDENTIFIER                                       = 129;  // <ArrayDecl> ::= Identifier <DimensionList>
		final int PROD_DIMENSIONLIST                                              = 130;  // <DimensionList> ::= <DimensionList> <Dimension>
		final int PROD_DIMENSIONLIST2                                             = 131;  // <DimensionList> ::= 
		final int PROD_DIMENSION_LBRACKET_RBRACKET                                = 132;  // <Dimension> ::= '[' <ShiftExpression> ']'
		final int PROD_INPUTINSTRUCTION_INPUTKEY                                  = 133;  // <InputInstruction> ::= InputKey
		final int PROD_INPUTINSTRUCTION_INPUTKEY2                                 = 134;  // <InputInstruction> ::= InputKey <Prompt> <TargetList>
		final int PROD_TARGETLIST                                                 = 135;  // <TargetList> ::= <LeftHandSide>
		final int PROD_TARGETLIST_COMMA                                           = 136;  // <TargetList> ::= <TargetList> ',' <LeftHandSide>
		final int PROD_PROMPT_STRINGLITERAL                                       = 137;  // <Prompt> ::= StringLiteral
		final int PROD_PROMPT_STRINGLITERAL_COMMA                                 = 138;  // <Prompt> ::= StringLiteral ','
		final int PROD_PROMPT                                                     = 139;  // <Prompt> ::= 
		final int PROD_OUTPUTINSTRUCTION_OUTPUTKEY                                = 140;  // <OutputInstruction> ::= OutputKey
		final int PROD_OUTPUTINSTRUCTION_OUTPUTKEY2                               = 141;  // <OutputInstruction> ::= OutputKey <ExpressionList>
		final int PROD_VARDECLARATION_VARKEY_IDENTIFIER_COLON                     = 142;  // <VarDeclaration> ::= VarKey Identifier ':' <TypeDescription>
		final int PROD_VARDECLARATION_DIMKEY_IDENTIFIER_ASKEY                     = 143;  // <VarDeclaration> ::= DimKey Identifier AsKey <TypeDescription>
		final int PROD_CONSTDEFINITION_CONSTKEY_IDENTIFIER                        = 144;  // <ConstDefinition> ::= ConstKey Identifier <AssignmentOperator> <Expression>
		final int PROD_CONSTDEFINITION_CONSTKEY_IDENTIFIER_COLON                  = 145;  // <ConstDefinition> ::= ConstKey Identifier ':' <TypeDescription> <AssignmentOperator> <Expression>
		final int PROD_CONSTDEFINITION_CONSTKEY_IDENTIFIER_ASKEY                  = 146;  // <ConstDefinition> ::= ConstKey Identifier AsKey <TypeDescription> <AssignmentOperator> <Expression>
		final int PROD_TYPEDEFINITION_TYPEKEY_IDENTIFIER_EQ                       = 147;  // <TypeDefinition> ::= TypeKey Identifier '=' <TypeConstruction>
		final int PROD_TYPECONSTRUCTION                                           = 148;  // <TypeConstruction> ::= <TypeDescription>
		final int PROD_TYPECONSTRUCTION_RECORDKEY_LBRACE_RBRACE                   = 149;  // <TypeConstruction> ::= RecordKey '{' <ComponentList> '}'
		final int PROD_TYPECONSTRUCTION_ENUM_LBRACE_RBRACE                        = 150;  // <TypeConstruction> ::= enum '{' <EnumList> '}'
		final int PROD_TYPEDESCRIPTION_IDENTIFIER                                 = 151;  // <TypeDescription> ::= Identifier <Dims>
		final int PROD_TYPEDESCRIPTION                                            = 152;  // <TypeDescription> ::= <ArrayOf> <TypeDescription>
		final int PROD_DIMS_LBRACKETRBRACKET                                      = 153;  // <Dims> ::= <Dims> '[]'
		final int PROD_DIMS                                                       = 154;  // <Dims> ::= 
		final int PROD_ARRAYOF_ARRAYKEY_OFKEY                                     = 155;  // <ArrayOf> ::= ArrayKey OfKey
		final int PROD_ARRAYOF_ARRAYKEY_LBRACKET_RBRACKET_OFKEY                   = 156;  // <ArrayOf> ::= ArrayKey '[' <RangeList> ']' OfKey
		final int PROD_RANGELIST                                                  = 157;  // <RangeList> ::= <Range>
		final int PROD_RANGELIST_COMMA                                            = 158;  // <RangeList> ::= <RangeList> ',' <Range>
		final int PROD_RANGE                                                      = 159;  // <Range> ::= <ShiftExpression>
		final int PROD_RANGE_DOTDOT                                               = 160;  // <Range> ::= <IntegerLiteral> '..' <IntegerLiteral>
		final int PROD_COMPONENTLIST                                              = 161;  // <ComponentList> ::= <ComponentGroup>
		final int PROD_COMPONENTLIST_SEMI                                         = 162;  // <ComponentList> ::= <ComponentList> ';' <ComponentGroup>
		final int PROD_COMPONENTGROUP_ASKEY                                       = 163;  // <ComponentGroup> ::= <IdList> AsKey <TypeDescription>
		final int PROD_COMPONENTGROUP_COLON                                       = 164;  // <ComponentGroup> ::= <IdList> ':' <TypeDescription>
		final int PROD_IDLIST_IDENTIFIER                                          = 165;  // <IdList> ::= Identifier
		final int PROD_IDLIST_COMMA_IDENTIFIER                                    = 166;  // <IdList> ::= <IdList> ',' Identifier
		final int PROD_ENUMLIST                                                   = 167;  // <EnumList> ::= <EnumDef>
		final int PROD_ENUMLIST_COMMA                                             = 168;  // <EnumList> ::= <EnumList> ',' <EnumDef>
		final int PROD_ENUMDEF_IDENTIFIER                                         = 169;  // <EnumDef> ::= Identifier
		final int PROD_ENUMDEF_IDENTIFIER_EQ                                      = 170;  // <EnumDef> ::= Identifier '=' <ShiftExpression>
		final int PROD_CONDITION_CONDKEY                                          = 171;  // <Condition> ::= CondKey <ConditionalOrExpression>
		final int PROD_FORHEADER_FORKEY_IDENTIFIER_TOKEY                          = 172;  // <ForHeader> ::= ForKey Identifier <AssignmentOperator> <Expression> ToKey <Expression> <StepClause>
		final int PROD_STEPCLAUSE_STEPKEY                                         = 173;  // <StepClause> ::= StepKey <DecimalIntegerLiteral>
		final int PROD_STEPCLAUSE_STEPKEY_MINUS                                   = 174;  // <StepClause> ::= StepKey '-' <DecimalIntegerLiteral>
		final int PROD_STEPCLAUSE_STEPKEY_PLUS                                    = 175;  // <StepClause> ::= StepKey '+' <DecimalIntegerLiteral>
		final int PROD_STEPCLAUSE                                                 = 176;  // <StepClause> ::= 
		final int PROD_FORINHEADER_FORINKEY_IDENTIFIER_INKEY                      = 177;  // <ForInHeader> ::= ForInKey Identifier InKey <ValueList>
		final int PROD_VALUELIST                                                  = 178;  // <ValueList> ::= <ArrayInitializer>
		final int PROD_VALUELIST2                                                 = 179;  // <ValueList> ::= <Primary>
		final int PROD_JUMP_RETURNKEY                                             = 180;  // <Jump> ::= ReturnKey
		final int PROD_JUMP_RETURNKEY2                                            = 181;  // <Jump> ::= ReturnKey <Expression>
		final int PROD_JUMP_EXITKEY                                               = 182;  // <Jump> ::= ExitKey
		final int PROD_JUMP_EXITKEY2                                              = 183;  // <Jump> ::= ExitKey <Expression>
		final int PROD_JUMP_LEAVEKEY                                              = 184;  // <Jump> ::= LeaveKey
		final int PROD_JUMP_LEAVEKEY2                                             = 185;  // <Jump> ::= LeaveKey <DecimalIntegerLiteral>
		final int PROD_JUMP_THROWKEY                                              = 186;  // <Jump> ::= ThrowKey <Expression>
		final int PROD_CALL_CALLKEY                                               = 187;  // <Call> ::= CallKey <RoutineInvocation>
		final int PROD_CALL_CALLKEY2                                              = 188;  // <Call> ::= CallKey <LeftHandSide> <AssignmentOperator> <RoutineInvocation>
		final int PROD_CATCHCLAUSE_CATCHKEY_IDENTIFIER                            = 189;  // <CatchClause> ::= CatchKey Identifier
		final int PROD_CATCHCLAUSE_CATCHKEY_IDENTIFIER_COLON                      = 190;  // <CatchClause> ::= CatchKey Identifier ':' <TypeDescription>
		final int PROD_CATCHCLAUSE_CATCHKEY_IDENTIFIER_ASKEY                      = 191;  // <CatchClause> ::= CatchKey Identifier AsKey <TypeDescription>
		final int PROD_CATCHCLAUSE_CATCHKEY_IDENTIFIER2                           = 192;  // <CatchClause> ::= CatchKey <TypeDescription> Identifier
		final int PROD_CASEDISCRIMINATOR_CASEKEY                                  = 193;  // <CaseDiscriminator> ::= CaseKey <AssignmentExpression>
		final int PROD_CASESELECTORS_SELECTORKEY                                  = 194;  // <CaseSelectors> ::= SelectorKey <ExpressionList>
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
