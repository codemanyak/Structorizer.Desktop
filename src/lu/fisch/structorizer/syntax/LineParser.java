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
 *      Kay Gürtzig     2021-11-08      Many Line types implemented, declarations still spared
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import com.creativewidgetworks.goldparser.engine.*;
import com.creativewidgetworks.goldparser.engine.enums.SymbolType;

import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.parsers.AuParser;
import lu.fisch.utils.StringList;

/**
 * Code import parser class of Structorizer 3.32-05, based on GOLDParser 5.0 for the LALR(1)
 * Structorizer line grammar, derived from Java grammar language, concentrating on the expression
 * part of it.<br/>
 * This file contains grammar-specific constants and individual routines to build
 * structograms (Nassi-Shneiderman diagrams) from the parsing tree. 
 * @author Kay Gürtzig
 */
public class LineParser /*extends CodeParser*/
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

	private Logger logger;
	/** @return the standard Java logger for this class */
	protected Logger getLogger()
	{
		if (this.logger == null) {
			this.logger = Logger.getLogger(getClass().getName());
		}
		return this.logger;
	}

	/**
	 * Maximum line width for listing expected tokens in the problem string (used for
	 * line wrapping)
	 */
	protected final int PRBL_LINE_WIDTH = 100;

	//------------------------------ Constructor -----------------------------

	private static LineParser singleton = null;
	/**
	 * Constructs a parser for Structorizer expressions, loads the grammar as resource and
	 * gets ready to parse an expression or expression list
	 */
	private LineParser() {
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
	public static LineParser getInstance()
	{
		if (singleton == null) {
			synchronized(LineParser.class) {
				singleton = new LineParser();
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
		final int SYM_EXCLAM                               =   4;  // '!'
		final int SYM_EXCLAMEQ                             =   5;  // '!='
		final int SYM_PERCENT                              =   6;  // '%'
		final int SYM_AMP                                  =   7;  // '&'
		final int SYM_AMPAMP                               =   8;  // '&&'
		final int SYM_LPAREN                               =   9;  // '('
		final int SYM_RPAREN                               =  10;  // ')'
		final int SYM_TIMES                                =  11;  // '*'
		final int SYM_COMMA                                =  12;  // ','
		final int SYM_DOT                                  =  13;  // '.'
		final int SYM_DOTDOT                               =  14;  // '..'
		final int SYM_DIV                                  =  15;  // '/'
		final int SYM_COLON                                =  16;  // ':'
		final int SYM_COLONEQ                              =  17;  // ':='
		final int SYM_SEMI                                 =  18;  // ';'
		final int SYM_QUESTION                             =  19;  // '?'
		final int SYM_LBRACKET                             =  20;  // '['
		final int SYM_LBRACKETRBRACKET                     =  21;  // '[]'
		final int SYM_RBRACKET                             =  22;  // ']'
		final int SYM_CARET                                =  23;  // '^'
		final int SYM_LBRACE                               =  24;  // '{'
		final int SYM_PIPE                                 =  25;  // '|'
		final int SYM_PIPEPIPE                             =  26;  // '||'
		final int SYM_RBRACE                               =  27;  // '}'
		final int SYM_TILDE                                =  28;  // '~'
		final int SYM_PLUS                                 =  29;  // '+'
		final int SYM_LT                                   =  30;  // '<'
		final int SYM_LTMINUS                              =  31;  // '<-'
		final int SYM_LTLT                                 =  32;  // '<<'
		final int SYM_LTEQ                                 =  33;  // '<='
		final int SYM_LTGT                                 =  34;  // '<>'
		final int SYM_EQ                                   =  35;  // '='
		final int SYM_EQEQ                                 =  36;  // '=='
		final int SYM_GT                                   =  37;  // '>'
		final int SYM_GTEQ                                 =  38;  // '>='
		final int SYM_GTGT                                 =  39;  // '>>'
		final int SYM_GTGTGT                               =  40;  // '>>>'
		final int SYM_ANDOPR                               =  41;  // AndOpr
		final int SYM_ARRAYKEY                             =  42;  // ArrayKey
		final int SYM_ASKEY                                =  43;  // AsKey
		final int SYM_BINARYINTEGERLITERAL                 =  44;  // BinaryIntegerLiteral
		final int SYM_BOOLEANLITERAL                       =  45;  // BooleanLiteral
		final int SYM_CALLKEY                              =  46;  // CallKey
		final int SYM_CASEKEY                              =  47;  // CaseKey
		final int SYM_CATCHKEY                             =  48;  // CatchKey
		final int SYM_CONDKEY                              =  49;  // CondKey
		final int SYM_CONSTKEY                             =  50;  // ConstKey
		final int SYM_DIMKEY                               =  51;  // DimKey
		final int SYM_DIVOPR                               =  52;  // DivOpr
		final int SYM_ENUM                                 =  53;  // enum
		final int SYM_EXITKEY                              =  54;  // ExitKey
		final int SYM_FLOATINGPOINTLITERAL                 =  55;  // FloatingPointLiteral
		final int SYM_FLOATINGPOINTLITERALEXPONENT         =  56;  // FloatingPointLiteralExponent
		final int SYM_FORINKEY                             =  57;  // ForInKey
		final int SYM_FORKEY                               =  58;  // ForKey
		final int SYM_HEXESCAPECHARLITERAL                 =  59;  // HexEscapeCharLiteral
		final int SYM_HEXINTEGERLITERAL                    =  60;  // HexIntegerLiteral
		final int SYM_IDENTIFIER                           =  61;  // Identifier
		final int SYM_INDIRECTCHARLITERAL                  =  62;  // IndirectCharLiteral
		final int SYM_INKEY                                =  63;  // InKey
		final int SYM_INPUTKEY                             =  64;  // InputKey
		final int SYM_LEAVEKEY                             =  65;  // LeaveKey
		final int SYM_MODOPR                               =  66;  // ModOpr
		final int SYM_NOTOPR                               =  67;  // NotOpr
		final int SYM_NULLLITERAL                          =  68;  // NullLiteral
		final int SYM_OCTALESCAPECHARLITERAL               =  69;  // OctalEscapeCharLiteral
		final int SYM_OCTALINTEGERLITERAL                  =  70;  // OctalIntegerLiteral
		final int SYM_OFKEY                                =  71;  // OfKey
		final int SYM_OROPR                                =  72;  // OrOpr
		final int SYM_OUTPUTKEY                            =  73;  // OutputKey
		final int SYM_RECORDKEY                            =  74;  // RecordKey
		final int SYM_RETURNKEY                            =  75;  // ReturnKey
		final int SYM_SELECTORKEY                          =  76;  // SelectorKey
		final int SYM_SHLOPR                               =  77;  // ShlOpr
		final int SYM_SHROPR                               =  78;  // ShrOpr
		final int SYM_STANDARDESCAPECHARLITERAL            =  79;  // StandardEscapeCharLiteral
		final int SYM_STARTWITHNOZERODECIMALINTEGERLITERAL =  80;  // StartWithNoZeroDecimalIntegerLiteral
		final int SYM_STARTWITHZERODECIMALINTEGERLITERAL   =  81;  // StartWithZeroDecimalIntegerLiteral
		final int SYM_STEPKEY                              =  82;  // StepKey
		final int SYM_STRINGLITERAL                        =  83;  // StringLiteral
		final int SYM_THROWKEY                             =  84;  // ThrowKey
		final int SYM_TOKEY                                =  85;  // ToKey
		final int SYM_TYPEKEY                              =  86;  // TypeKey
		final int SYM_VARKEY                               =  87;  // VarKey
		final int SYM_ADDITIVEEXPRESSION                   =  88;  // <AdditiveExpression>
		final int SYM_ANDEXPRESSION                        =  89;  // <AndExpression>
		final int SYM_ARRAYACCESS                          =  90;  // <ArrayAccess>
		final int SYM_ARRAYDECL                            =  91;  // <ArrayDecl>
		final int SYM_ARRAYINITIALIZER                     =  92;  // <ArrayInitializer>
		final int SYM_ARRAYOF                              =  93;  // <ArrayOf>
		final int SYM_ASSIGNMENT                           =  94;  // <Assignment>
		final int SYM_ASSIGNMENTOPERATOR                   =  95;  // <AssignmentOperator>
		final int SYM_CALL                                 =  96;  // <Call>
		final int SYM_CASEDISCRIMINATOR                    =  97;  // <CaseDiscriminator>
		final int SYM_CASESELECTORS                        =  98;  // <CaseSelectors>
		final int SYM_CATCHCLAUSE                          =  99;  // <CatchClause>
		final int SYM_CHARACTERLITERAL                     = 100;  // <CharacterLiteral>
		final int SYM_COMPONENTGROUP                       = 101;  // <ComponentGroup>
		final int SYM_COMPONENTINITIALIZER                 = 102;  // <ComponentInitializer>
		final int SYM_COMPONENTINITIALIZERLIST             = 103;  // <ComponentInitializerList>
		final int SYM_COMPONENTINITIALIZERS                = 104;  // <ComponentInitializers>
		final int SYM_COMPONENTLIST                        = 105;  // <ComponentList>
		final int SYM_CONDITION                            = 106;  // <Condition>
		final int SYM_CONDITIONALANDEXPRESSION             = 107;  // <ConditionalAndExpression>
		final int SYM_CONDITIONALEXPRESSION                = 108;  // <ConditionalExpression>
		final int SYM_CONDITIONALOREXPRESSION              = 109;  // <ConditionalOrExpression>
		final int SYM_CONSTDEFINITION                      = 110;  // <ConstDefinition>
		final int SYM_DECIMALINTEGERLITERAL                = 111;  // <DecimalIntegerLiteral>
		final int SYM_DIMENSION                            = 112;  // <Dimension>
		final int SYM_DIMENSIONLIST                        = 113;  // <DimensionList>
		final int SYM_DIMS                                 = 114;  // <Dims>
		final int SYM_ELEMENTLINE                          = 115;  // <ElementLine>
		final int SYM_ENUMDEF                              = 116;  // <EnumDef>
		final int SYM_ENUMLIST                             = 117;  // <EnumList>
		final int SYM_EQUALITYEXPRESSION                   = 118;  // <EqualityExpression>
		final int SYM_EXCLUSIVEOREXPRESSION                = 119;  // <ExclusiveOrExpression>
		final int SYM_EXPRESSION                           = 120;  // <Expression>
		final int SYM_EXPRESSIONLIST                       = 121;  // <ExpressionList>
		final int SYM_FIELDACCESS                          = 122;  // <FieldAccess>
		final int SYM_FLOATPOINTLITERAL                    = 123;  // <FloatPointLiteral>
		final int SYM_FORHEADER                            = 124;  // <ForHeader>
		final int SYM_FORINHEADER                          = 125;  // <ForInHeader>
		final int SYM_IDLIST                               = 126;  // <IdList>
		final int SYM_INCLUSIVEOREXPRESSION                = 127;  // <InclusiveOrExpression>
		final int SYM_INPUTINSTRUCTION                     = 128;  // <InputInstruction>
		final int SYM_INTEGERLITERAL                       = 129;  // <IntegerLiteral>
		final int SYM_JUMP                                 = 130;  // <Jump>
		final int SYM_LEFTHANDSIDE                         = 131;  // <LeftHandSide>
		final int SYM_LITERAL                              = 132;  // <Literal>
		final int SYM_METHODINVOCATION                     = 133;  // <MethodInvocation>
		final int SYM_MULTIPLICATIVEEXPRESSION             = 134;  // <MultiplicativeExpression>
		final int SYM_NAME                                 = 135;  // <Name>
		final int SYM_OUTPUTINSTRUCTION                    = 136;  // <OutputInstruction>
		final int SYM_POSTFIXEXPRESSION                    = 137;  // <PostfixExpression>
		final int SYM_PRIMARY                              = 138;  // <Primary>
		final int SYM_PROMPT                               = 139;  // <Prompt>
		final int SYM_QUALIFIEDNAME                        = 140;  // <QualifiedName>
		final int SYM_RANGE                                = 141;  // <Range>
		final int SYM_RANGELIST                            = 142;  // <RangeList>
		final int SYM_RECORDINITIALIZER                    = 143;  // <RecordInitializer>
		final int SYM_RELATIONALEXPRESSION                 = 144;  // <RelationalExpression>
		final int SYM_ROUTINEINVOCATION                    = 145;  // <RoutineInvocation>
		final int SYM_SHIFTEXPRESSION                      = 146;  // <ShiftExpression>
		final int SYM_SIMPLENAME                           = 147;  // <SimpleName>
		final int SYM_STEPCLAUSE                           = 148;  // <StepClause>
		final int SYM_TARGETLIST                           = 149;  // <TargetList>
		final int SYM_TYPECONSTRUCTION                     = 150;  // <TypeConstruction>
		final int SYM_TYPEDEFINITION                       = 151;  // <TypeDefinition>
		final int SYM_TYPEDESCRIPTION                      = 152;  // <TypeDescription>
		final int SYM_UNARYEXPRESSION                      = 153;  // <UnaryExpression>
		final int SYM_UNARYEXPRESSIONNOTPLUSMINUS          = 154;  // <UnaryExpressionNotPlusMinus>
		final int SYM_VARDECLARATION                       = 155;  // <VarDeclaration>
		final int SYM_VARINITIALISATION                    = 156;  // <VarInitialisation>
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
		final int PROD_INTEGERLITERAL_BINARYINTEGERLITERAL                        =  26;  // <IntegerLiteral> ::= BinaryIntegerLiteral
		final int PROD_LITERAL                                                    =  27;  // <Literal> ::= <IntegerLiteral>
		final int PROD_LITERAL2                                                   =  28;  // <Literal> ::= <FloatPointLiteral>
		final int PROD_LITERAL_BOOLEANLITERAL                                     =  29;  // <Literal> ::= BooleanLiteral
		final int PROD_LITERAL3                                                   =  30;  // <Literal> ::= <CharacterLiteral>
		final int PROD_LITERAL_STRINGLITERAL                                      =  31;  // <Literal> ::= StringLiteral
		final int PROD_LITERAL_NULLLITERAL                                        =  32;  // <Literal> ::= NullLiteral
		final int PROD_NAME                                                       =  33;  // <Name> ::= <SimpleName>
		final int PROD_NAME2                                                      =  34;  // <Name> ::= <QualifiedName>
		final int PROD_SIMPLENAME_IDENTIFIER                                      =  35;  // <SimpleName> ::= Identifier
		final int PROD_QUALIFIEDNAME_DOT_IDENTIFIER                               =  36;  // <QualifiedName> ::= <Name> '.' Identifier
		final int PROD_ARRAYINITIALIZER_LBRACE_RBRACE                             =  37;  // <ArrayInitializer> ::= '{' <ExpressionList> '}'
		final int PROD_ARRAYINITIALIZER_LBRACE_RBRACE2                            =  38;  // <ArrayInitializer> ::= '{' '}'
		final int PROD_RECORDINITIALIZER_IDENTIFIER_LBRACE_RBRACE                 =  39;  // <RecordInitializer> ::= Identifier '{' <ComponentInitializers> '}'
		final int PROD_RECORDINITIALIZER_IDENTIFIER_LBRACE_RBRACE2                =  40;  // <RecordInitializer> ::= Identifier '{' '}'
		final int PROD_EXPRESSIONLIST                                             =  41;  // <ExpressionList> ::= <Expression>
		final int PROD_EXPRESSIONLIST_COMMA                                       =  42;  // <ExpressionList> ::= <ExpressionList> ',' <Expression>
		final int PROD_COMPONENTINITIALIZERS                                      =  43;  // <ComponentInitializers> ::= <ComponentInitializerList>
		final int PROD_COMPONENTINITIALIZERS2                                     =  44;  // <ComponentInitializers> ::= <ExpressionList>
		final int PROD_COMPONENTINITIALIZERS_COMMA                                =  45;  // <ComponentInitializers> ::= <ExpressionList> ',' <ComponentInitializerList>
		final int PROD_COMPONENTINITIALIZERLIST_COMMA                             =  46;  // <ComponentInitializerList> ::= <ComponentInitializerList> ',' <ComponentInitializer>
		final int PROD_COMPONENTINITIALIZERLIST                                   =  47;  // <ComponentInitializerList> ::= <ComponentInitializer>
		final int PROD_COMPONENTINITIALIZER_IDENTIFIER_COLON                      =  48;  // <ComponentInitializer> ::= Identifier ':' <Expression>
		final int PROD_PRIMARY                                                    =  49;  // <Primary> ::= <Literal>
		final int PROD_PRIMARY_LPAREN_RPAREN                                      =  50;  // <Primary> ::= '(' <Expression> ')'
		final int PROD_PRIMARY2                                                   =  51;  // <Primary> ::= <FieldAccess>
		final int PROD_PRIMARY3                                                   =  52;  // <Primary> ::= <MethodInvocation>
		final int PROD_PRIMARY4                                                   =  53;  // <Primary> ::= <ArrayAccess>
		final int PROD_FIELDACCESS_DOT_IDENTIFIER                                 =  54;  // <FieldAccess> ::= <Primary> '.' Identifier
		final int PROD_ROUTINEINVOCATION_LPAREN_RPAREN                            =  55;  // <RoutineInvocation> ::= <Name> '(' <ExpressionList> ')'
		final int PROD_ROUTINEINVOCATION_LPAREN_RPAREN2                           =  56;  // <RoutineInvocation> ::= <Name> '(' ')'
		final int PROD_METHODINVOCATION                                           =  57;  // <MethodInvocation> ::= <RoutineInvocation>
		final int PROD_METHODINVOCATION_DOT_IDENTIFIER_LPAREN_RPAREN              =  58;  // <MethodInvocation> ::= <Primary> '.' Identifier '(' <ExpressionList> ')'
		final int PROD_METHODINVOCATION_DOT_IDENTIFIER_LPAREN_RPAREN2             =  59;  // <MethodInvocation> ::= <Primary> '.' Identifier '(' ')'
		final int PROD_ARRAYACCESS_LBRACKET_RBRACKET                              =  60;  // <ArrayAccess> ::= <Name> '[' <ExpressionList> ']'
		final int PROD_ARRAYACCESS_LBRACKET_RBRACKET2                             =  61;  // <ArrayAccess> ::= <Primary> '[' <ExpressionList> ']'
		final int PROD_POSTFIXEXPRESSION                                          =  62;  // <PostfixExpression> ::= <Primary>
		final int PROD_POSTFIXEXPRESSION2                                         =  63;  // <PostfixExpression> ::= <Name>
		final int PROD_UNARYEXPRESSION_PLUS                                       =  64;  // <UnaryExpression> ::= '+' <UnaryExpression>
		final int PROD_UNARYEXPRESSION_MINUS                                      =  65;  // <UnaryExpression> ::= '-' <UnaryExpression>
		final int PROD_UNARYEXPRESSION                                            =  66;  // <UnaryExpression> ::= <UnaryExpressionNotPlusMinus>
		final int PROD_UNARYEXPRESSIONNOTPLUSMINUS                                =  67;  // <UnaryExpressionNotPlusMinus> ::= <PostfixExpression>
		final int PROD_UNARYEXPRESSIONNOTPLUSMINUS_TILDE                          =  68;  // <UnaryExpressionNotPlusMinus> ::= '~' <UnaryExpression>
		final int PROD_UNARYEXPRESSIONNOTPLUSMINUS_EXCLAM                         =  69;  // <UnaryExpressionNotPlusMinus> ::= '!' <UnaryExpression>
		final int PROD_UNARYEXPRESSIONNOTPLUSMINUS_NOTOPR                         =  70;  // <UnaryExpressionNotPlusMinus> ::= NotOpr <UnaryExpression>
		final int PROD_MULTIPLICATIVEEXPRESSION                                   =  71;  // <MultiplicativeExpression> ::= <UnaryExpression>
		final int PROD_MULTIPLICATIVEEXPRESSION_TIMES                             =  72;  // <MultiplicativeExpression> ::= <MultiplicativeExpression> '*' <UnaryExpression>
		final int PROD_MULTIPLICATIVEEXPRESSION_DIV                               =  73;  // <MultiplicativeExpression> ::= <MultiplicativeExpression> '/' <UnaryExpression>
		final int PROD_MULTIPLICATIVEEXPRESSION_DIVOPR                            =  74;  // <MultiplicativeExpression> ::= <MultiplicativeExpression> DivOpr <UnaryExpression>
		final int PROD_MULTIPLICATIVEEXPRESSION_MODOPR                            =  75;  // <MultiplicativeExpression> ::= <MultiplicativeExpression> ModOpr <UnaryExpression>
		final int PROD_MULTIPLICATIVEEXPRESSION_PERCENT                           =  76;  // <MultiplicativeExpression> ::= <MultiplicativeExpression> '%' <UnaryExpression>
		final int PROD_ADDITIVEEXPRESSION                                         =  77;  // <AdditiveExpression> ::= <MultiplicativeExpression>
		final int PROD_ADDITIVEEXPRESSION_PLUS                                    =  78;  // <AdditiveExpression> ::= <AdditiveExpression> '+' <MultiplicativeExpression>
		final int PROD_ADDITIVEEXPRESSION_MINUS                                   =  79;  // <AdditiveExpression> ::= <AdditiveExpression> '-' <MultiplicativeExpression>
		final int PROD_SHIFTEXPRESSION                                            =  80;  // <ShiftExpression> ::= <AdditiveExpression>
		final int PROD_SHIFTEXPRESSION_LTLT                                       =  81;  // <ShiftExpression> ::= <ShiftExpression> '<<' <AdditiveExpression>
		final int PROD_SHIFTEXPRESSION_GTGT                                       =  82;  // <ShiftExpression> ::= <ShiftExpression> '>>' <AdditiveExpression>
		final int PROD_SHIFTEXPRESSION_GTGTGT                                     =  83;  // <ShiftExpression> ::= <ShiftExpression> '>>>' <AdditiveExpression>
		final int PROD_SHIFTEXPRESSION_SHLOPR                                     =  84;  // <ShiftExpression> ::= <ShiftExpression> ShlOpr <AdditiveExpression>
		final int PROD_SHIFTEXPRESSION_SHROPR                                     =  85;  // <ShiftExpression> ::= <ShiftExpression> ShrOpr <AdditiveExpression>
		final int PROD_RELATIONALEXPRESSION                                       =  86;  // <RelationalExpression> ::= <ShiftExpression>
		final int PROD_RELATIONALEXPRESSION_LT                                    =  87;  // <RelationalExpression> ::= <RelationalExpression> '<' <ShiftExpression>
		final int PROD_RELATIONALEXPRESSION_GT                                    =  88;  // <RelationalExpression> ::= <RelationalExpression> '>' <ShiftExpression>
		final int PROD_RELATIONALEXPRESSION_LTEQ                                  =  89;  // <RelationalExpression> ::= <RelationalExpression> '<=' <ShiftExpression>
		final int PROD_RELATIONALEXPRESSION_GTEQ                                  =  90;  // <RelationalExpression> ::= <RelationalExpression> '>=' <ShiftExpression>
		final int PROD_EQUALITYEXPRESSION                                         =  91;  // <EqualityExpression> ::= <RelationalExpression>
		final int PROD_EQUALITYEXPRESSION_EQEQ                                    =  92;  // <EqualityExpression> ::= <EqualityExpression> '==' <RelationalExpression>
		final int PROD_EQUALITYEXPRESSION_EQ                                      =  93;  // <EqualityExpression> ::= <EqualityExpression> '=' <RelationalExpression>
		final int PROD_EQUALITYEXPRESSION_EXCLAMEQ                                =  94;  // <EqualityExpression> ::= <EqualityExpression> '!=' <RelationalExpression>
		final int PROD_EQUALITYEXPRESSION_LTGT                                    =  95;  // <EqualityExpression> ::= <EqualityExpression> '<>' <RelationalExpression>
		final int PROD_ANDEXPRESSION                                              =  96;  // <AndExpression> ::= <EqualityExpression>
		final int PROD_ANDEXPRESSION_AMP                                          =  97;  // <AndExpression> ::= <AndExpression> '&' <EqualityExpression>
		final int PROD_EXCLUSIVEOREXPRESSION                                      =  98;  // <ExclusiveOrExpression> ::= <AndExpression>
		final int PROD_EXCLUSIVEOREXPRESSION_CARET                                =  99;  // <ExclusiveOrExpression> ::= <ExclusiveOrExpression> '^' <AndExpression>
		final int PROD_INCLUSIVEOREXPRESSION                                      = 100;  // <InclusiveOrExpression> ::= <ExclusiveOrExpression>
		final int PROD_INCLUSIVEOREXPRESSION_PIPE                                 = 101;  // <InclusiveOrExpression> ::= <InclusiveOrExpression> '|' <ExclusiveOrExpression>
		final int PROD_CONDITIONALANDEXPRESSION                                   = 102;  // <ConditionalAndExpression> ::= <InclusiveOrExpression>
		final int PROD_CONDITIONALANDEXPRESSION_AMPAMP                            = 103;  // <ConditionalAndExpression> ::= <ConditionalAndExpression> '&&' <InclusiveOrExpression>
		final int PROD_CONDITIONALANDEXPRESSION_ANDOPR                            = 104;  // <ConditionalAndExpression> ::= <ConditionalAndExpression> AndOpr <InclusiveOrExpression>
		final int PROD_CONDITIONALOREXPRESSION                                    = 105;  // <ConditionalOrExpression> ::= <ConditionalAndExpression>
		final int PROD_CONDITIONALOREXPRESSION_PIPEPIPE                           = 106;  // <ConditionalOrExpression> ::= <ConditionalOrExpression> '||' <ConditionalAndExpression>
		final int PROD_CONDITIONALOREXPRESSION_OROPR                              = 107;  // <ConditionalOrExpression> ::= <ConditionalOrExpression> OrOpr <ConditionalAndExpression>
		final int PROD_CONDITIONALEXPRESSION                                      = 108;  // <ConditionalExpression> ::= <ConditionalOrExpression>
		final int PROD_CONDITIONALEXPRESSION_QUESTION_COLON                       = 109;  // <ConditionalExpression> ::= <ConditionalOrExpression> '?' <Expression> ':' <ConditionalExpression>
		final int PROD_EXPRESSION                                                 = 110;  // <Expression> ::= <ConditionalExpression>
		final int PROD_EXPRESSION2                                                = 111;  // <Expression> ::= <ArrayInitializer>
		final int PROD_EXPRESSION3                                                = 112;  // <Expression> ::= <RecordInitializer>
		final int PROD_ASSIGNMENT                                                 = 113;  // <Assignment> ::= <LeftHandSide> <AssignmentOperator> <Expression>
		final int PROD_LEFTHANDSIDE                                               = 114;  // <LeftHandSide> ::= <Name>
		final int PROD_LEFTHANDSIDE2                                              = 115;  // <LeftHandSide> ::= <FieldAccess>
		final int PROD_LEFTHANDSIDE3                                              = 116;  // <LeftHandSide> ::= <ArrayAccess>
		final int PROD_ASSIGNMENTOPERATOR_LTMINUS                                 = 117;  // <AssignmentOperator> ::= '<-'
		final int PROD_ASSIGNMENTOPERATOR_COLONEQ                                 = 118;  // <AssignmentOperator> ::= ':='
		final int PROD_VARINITIALISATION                                          = 119;  // <VarInitialisation> ::= <VarDeclaration> <AssignmentOperator> <Expression>
		final int PROD_VARINITIALISATION2                                         = 120;  // <VarInitialisation> ::= <TypeDescription> <ArrayDecl> <AssignmentOperator> <Expression>
		final int PROD_ARRAYDECL_IDENTIFIER                                       = 121;  // <ArrayDecl> ::= Identifier <DimensionList>
		final int PROD_DIMENSIONLIST                                              = 122;  // <DimensionList> ::= <DimensionList> <Dimension>
		final int PROD_DIMENSIONLIST2                                             = 123;  // <DimensionList> ::= 
		final int PROD_DIMENSION_LBRACKET_RBRACKET                                = 124;  // <Dimension> ::= '[' <ShiftExpression> ']'
		final int PROD_INPUTINSTRUCTION_INPUTKEY                                  = 125;  // <InputInstruction> ::= InputKey
		final int PROD_INPUTINSTRUCTION_INPUTKEY2                                 = 126;  // <InputInstruction> ::= InputKey <Prompt> <TargetList>
		final int PROD_TARGETLIST                                                 = 127;  // <TargetList> ::= <LeftHandSide>
		final int PROD_TARGETLIST_COMMA                                           = 128;  // <TargetList> ::= <TargetList> ',' <LeftHandSide>
		final int PROD_PROMPT_STRINGLITERAL                                       = 129;  // <Prompt> ::= StringLiteral
		final int PROD_PROMPT_STRINGLITERAL_COMMA                                 = 130;  // <Prompt> ::= StringLiteral ','
		final int PROD_PROMPT                                                     = 131;  // <Prompt> ::= 
		final int PROD_OUTPUTINSTRUCTION_OUTPUTKEY                                = 132;  // <OutputInstruction> ::= OutputKey
		final int PROD_OUTPUTINSTRUCTION_OUTPUTKEY2                               = 133;  // <OutputInstruction> ::= OutputKey <ExpressionList>
		final int PROD_VARDECLARATION_VARKEY_IDENTIFIER_COLON                     = 134;  // <VarDeclaration> ::= VarKey Identifier ':' <TypeDescription>
		final int PROD_VARDECLARATION_DIMKEY_IDENTIFIER_ASKEY                     = 135;  // <VarDeclaration> ::= DimKey Identifier AsKey <TypeDescription>
		final int PROD_CONSTDEFINITION_CONSTKEY_IDENTIFIER                        = 136;  // <ConstDefinition> ::= ConstKey Identifier <AssignmentOperator> <Expression>
		final int PROD_CONSTDEFINITION_CONSTKEY_IDENTIFIER_COLON                  = 137;  // <ConstDefinition> ::= ConstKey Identifier ':' <TypeDescription> <AssignmentOperator> <Expression>
		final int PROD_CONSTDEFINITION_CONSTKEY_IDENTIFIER_ASKEY                  = 138;  // <ConstDefinition> ::= ConstKey Identifier AsKey <TypeDescription> <AssignmentOperator> <Expression>
		final int PROD_TYPEDEFINITION_TYPEKEY_IDENTIFIER_EQ                       = 139;  // <TypeDefinition> ::= TypeKey Identifier '=' <TypeConstruction>
		final int PROD_TYPECONSTRUCTION                                           = 140;  // <TypeConstruction> ::= <TypeDescription>
		final int PROD_TYPECONSTRUCTION_RECORDKEY_LBRACE_RBRACE                   = 141;  // <TypeConstruction> ::= RecordKey '{' <ComponentList> '}'
		final int PROD_TYPECONSTRUCTION_ENUM_LBRACE_RBRACE                        = 142;  // <TypeConstruction> ::= enum '{' <EnumList> '}'
		final int PROD_TYPEDESCRIPTION_IDENTIFIER                                 = 143;  // <TypeDescription> ::= Identifier <Dims>
		final int PROD_TYPEDESCRIPTION                                            = 144;  // <TypeDescription> ::= <ArrayOf> <TypeDescription>
		final int PROD_DIMS_LBRACKETRBRACKET                                      = 145;  // <Dims> ::= <Dims> '[]'
		final int PROD_DIMS                                                       = 146;  // <Dims> ::= 
		final int PROD_ARRAYOF_ARRAYKEY_OFKEY                                     = 147;  // <ArrayOf> ::= ArrayKey OfKey
		final int PROD_ARRAYOF_ARRAYKEY_LBRACKET_RBRACKET_OFKEY                   = 148;  // <ArrayOf> ::= ArrayKey '[' <RangeList> ']' OfKey
		final int PROD_RANGELIST                                                  = 149;  // <RangeList> ::= <Range>
		final int PROD_RANGELIST_COMMA                                            = 150;  // <RangeList> ::= <RangeList> ',' <Range>
		final int PROD_RANGE                                                      = 151;  // <Range> ::= <ShiftExpression>
		final int PROD_RANGE_DOTDOT                                               = 152;  // <Range> ::= <IntegerLiteral> '..' <IntegerLiteral>
		final int PROD_COMPONENTLIST                                              = 153;  // <ComponentList> ::= <ComponentGroup>
		final int PROD_COMPONENTLIST_SEMI                                         = 154;  // <ComponentList> ::= <ComponentList> ';' <ComponentGroup>
		final int PROD_COMPONENTGROUP_ASKEY                                       = 155;  // <ComponentGroup> ::= <IdList> AsKey <TypeDescription>
		final int PROD_COMPONENTGROUP_COLON                                       = 156;  // <ComponentGroup> ::= <IdList> ':' <TypeDescription>
		final int PROD_IDLIST_IDENTIFIER                                          = 157;  // <IdList> ::= Identifier
		final int PROD_IDLIST_COMMA_IDENTIFIER                                    = 158;  // <IdList> ::= <IdList> ',' Identifier
		final int PROD_ENUMLIST                                                   = 159;  // <EnumList> ::= <EnumDef>
		final int PROD_ENUMLIST_COMMA                                             = 160;  // <EnumList> ::= <EnumList> ',' <EnumDef>
		final int PROD_ENUMDEF_IDENTIFIER                                         = 161;  // <EnumDef> ::= Identifier
		final int PROD_ENUMDEF_IDENTIFIER_EQ                                      = 162;  // <EnumDef> ::= Identifier '=' <ShiftExpression>
		final int PROD_CONDITION_CONDKEY                                          = 163;  // <Condition> ::= CondKey <ConditionalExpression>
		final int PROD_FORHEADER_FORKEY_IDENTIFIER_TOKEY                          = 164;  // <ForHeader> ::= ForKey Identifier <AssignmentOperator> <Expression> ToKey <Expression> <StepClause>
		final int PROD_STEPCLAUSE_STEPKEY                                         = 165;  // <StepClause> ::= StepKey <DecimalIntegerLiteral>
		final int PROD_STEPCLAUSE_STEPKEY_MINUS                                   = 166;  // <StepClause> ::= StepKey '-' <DecimalIntegerLiteral>
		final int PROD_STEPCLAUSE_STEPKEY_PLUS                                    = 167;  // <StepClause> ::= StepKey '+' <DecimalIntegerLiteral>
		final int PROD_STEPCLAUSE                                                 = 168;  // <StepClause> ::= 
		final int PROD_FORINHEADER_FORINKEY_IDENTIFIER_INKEY                      = 169;  // <ForInHeader> ::= ForInKey Identifier InKey <ExpressionList>
		final int PROD_JUMP_RETURNKEY                                             = 170;  // <Jump> ::= ReturnKey
		final int PROD_JUMP_RETURNKEY2                                            = 171;  // <Jump> ::= ReturnKey <Expression>
		final int PROD_JUMP_EXITKEY                                               = 172;  // <Jump> ::= ExitKey
		final int PROD_JUMP_EXITKEY2                                              = 173;  // <Jump> ::= ExitKey <Expression>
		final int PROD_JUMP_LEAVEKEY                                              = 174;  // <Jump> ::= LeaveKey
		final int PROD_JUMP_LEAVEKEY2                                             = 175;  // <Jump> ::= LeaveKey <DecimalIntegerLiteral>
		final int PROD_JUMP_THROWKEY                                              = 176;  // <Jump> ::= ThrowKey <Expression>
		final int PROD_CALL_CALLKEY                                               = 177;  // <Call> ::= CallKey <RoutineInvocation>
		final int PROD_CALL_CALLKEY2                                              = 178;  // <Call> ::= CallKey <LeftHandSide> <AssignmentOperator> <RoutineInvocation>
		final int PROD_CATCHCLAUSE_CATCHKEY_IDENTIFIER                            = 179;  // <CatchClause> ::= CatchKey Identifier
		final int PROD_CATCHCLAUSE_CATCHKEY_IDENTIFIER_COLON                      = 180;  // <CatchClause> ::= CatchKey Identifier ':' <TypeDescription>
		final int PROD_CATCHCLAUSE_CATCHKEY_IDENTIFIER_ASKEY                      = 181;  // <CatchClause> ::= CatchKey Identifier AsKey <TypeDescription>
		final int PROD_CATCHCLAUSE_CATCHKEY_IDENTIFIER2                           = 182;  // <CatchClause> ::= CatchKey <TypeDescription> Identifier
		final int PROD_CASEDISCRIMINATOR_CASEKEY                                  = 183;  // <CaseDiscriminator> ::= CaseKey <ConditionalExpression>
		final int PROD_CASESELECTORS_SELECTORKEY                                  = 184;  // <CaseSelectors> ::= SelectorKey <ExpressionList>
	};

	/**
	 * Parses the given Element line {@code _textToParse} for the {@link Element}, gathering
	 * a possible parsing error in {@code _problems}.
	 * @param _textToParse - the (unbroken) and prefixed line to parse
	 * @param _problems - a {@link StringBuilder} where a possible error description may be
	 *         added to.
	 * @param _element - the origin Element (if available) or {@code null}
	 * @param _types - a {@link TypeRegistry} or {@code null}
	 * @return The composed {@link Line} object, or {@code null} in case of an error or
	 *         lacking implementation
	 */
	public Line parse(String _textToParse, StringBuilder _problems, Element _element, TypeRegistry _types)
	{
		Line line = null;
		Line.LineType lineType = Line.LineType.LT_RAW;
		ArrayList<Expression> expressions = new ArrayList<Expression>();
//		synchronized(this) {
			boolean parsedWithoutError = parser.parseSourceStatements(_textToParse);

			if (parsedWithoutError) {
				short tokenPos = 0;
				Reduction red = parser.getCurrentReduction();
				switch(red.getParent().getTableIndex()) {
				case RuleConstants.PROD_ASSIGNMENT: {
					// <LeftHandSide> <AssignmentOperator> <Expression>
					lineType = Line.LineType.LT_ASSIGNMENT;
					Expression expr = new Expression(Expression.NodeType.OPERATOR,
							getContent_R(red.get(1).asReduction(), ""), (short)0);
					expressions.add(expr);
					expr.children.add(buildExpression(red.get(0), tokenPos, _types));
					tokenPos += expr.children.get(0).getLength();
					expr.tokenPos = tokenPos;
					expr.children.add(buildExpression(red.get(2), tokenPos + 1, _types));
					break;
				}
				case RuleConstants.PROD_VARINITIALISATION:
					// <VarInitialisation> ::= <VarDeclaration> <AssignmentOperator> <Expression>
					lineType = Line.LineType.LT_VAR_INIT;
					// TODO
					break;
				case RuleConstants.PROD_VARINITIALISATION2:
					// <VarInitialisation> ::= <TypeDescription> <ArrayDecl> <AssignmentOperator> <Expression>
					lineType = Line.LineType.LT_VAR_INIT;
					// TODO
					break;
					
				case RuleConstants.PROD_INPUTINSTRUCTION_INPUTKEY:	// FIXME Is this needed at all?
					// <InputInstruction> ::= InputKey
				case RuleConstants.PROD_INPUTINSTRUCTION_INPUTKEY2:
					// <InputInstruction> ::= InputKey <Prompt> <TargetList>
					lineType = Line.LineType.LT_INPUT;
					if (red.size() > 1) {
						LinkedList<Expression> targets = new LinkedList<Expression>();
						Reduction promptRed = red.get(1).asReduction();
						if (promptRed.size() > 0) {
							expressions.add(buildExpression(promptRed.get(0), tokenPos + 1, _types));
							tokenPos += expressions.get(0).getLength();
							if (red.size() > 1) {
								tokenPos++;
							}
						}
						Token tgtToken = red.get(2);
						while (tgtToken.getType() == SymbolType.NON_TERMINAL
								&& (red = tgtToken.asReduction()).getParent().getTableIndex() == RuleConstants.PROD_TARGETLIST_COMMA) {
							targets.addFirst(buildExpression(red.get(2), (short)0, _types));
							tgtToken = red.get(0);
						}
						targets.addFirst(buildExpression(tgtToken, tokenPos, _types));
						for (Expression tgt: targets) {
							tgt.tokenPos = tokenPos;
							tokenPos += tgt.getLength() + 1;
						}
						expressions.addAll(targets);
					}
					break;
					
				case RuleConstants.PROD_OUTPUTINSTRUCTION_OUTPUTKEY:	// FIXME Is this needed at all?
					// <OutputInstruction> ::= OutputKey
				case RuleConstants.PROD_OUTPUTINSTRUCTION_OUTPUTKEY2:
					// <OutputInstruction> ::= OutputKey <ExpressionList>
					lineType = Line.LineType.LT_OUTPUT;
					if (red.size() > 1) {
						buildExpressionList(red.get(1), tokenPos+1, expressions, _types);
					}
					break;

				case RuleConstants.PROD_VARDECLARATION_VARKEY_IDENTIFIER_COLON:
					// <VarDeclaration> ::= VarKey Identifier ':' <TypeDescription>
					break;
				case RuleConstants.PROD_VARDECLARATION_DIMKEY_IDENTIFIER_ASKEY:
					// <VarDeclaration> ::= DimKey Identifier AsKey <TypeDescription>
					lineType = Line.LineType.LT_VAR_DECL;
					// TODO
					break;

				case RuleConstants.PROD_CONSTDEFINITION_CONSTKEY_IDENTIFIER:
					// <ConstDefinition> ::= ConstKey Identifier <AssignmentOperator> <Expression>
				case RuleConstants.PROD_CONSTDEFINITION_CONSTKEY_IDENTIFIER_COLON:
					// <ConstDefinition> ::= ConstKey Identifier ':' <TypeDescription> <AssignmentOperator> <Expression>
				case RuleConstants.PROD_CONSTDEFINITION_CONSTKEY_IDENTIFIER_ASKEY:
					// <ConstDefinition> ::= ConstKey Identifier AsKey <TypeDescription> <AssignmentOperator> <Expression>
					lineType = Line.LineType.LT_CONST_DEF;
					// TODO
					break;

				case RuleConstants.PROD_TYPEDEFINITION_TYPEKEY_IDENTIFIER_EQ:
					// <TypeDefinition> ::= TypeKey Identifier '=' <TypeConstruction>
					lineType = Line.LineType.LT_TYPE_DEF;
					// TODO
					break;
					
				case RuleConstants.PROD_CONDITION_CONDKEY:
					// <Condition> ::= CondKey <ConditionalExpression>
					lineType = Line.LineType.LT_CONDITION;
					expressions.add(buildExpression(red.get(1), 0, _types));
					break;
					
				case RuleConstants.PROD_FORHEADER_FORKEY_IDENTIFIER_TOKEY:
					// <ForHeader> ::= ForKey Identifier <AssignmentOperator> <Expression> ToKey <Expression> <StepClause>
					lineType = Line.LineType.LT_FOR_LOOP;
					{
						Expression expr = new Expression(Expression.NodeType.OPERATOR,
								getContent_R(red.get(2).asReduction(), ""), (short)(tokenPos));
						expr.children.add(buildExpression(red.get(1), tokenPos + 1, _types));
						tokenPos += 2 + expr.getLength();
						expr.children.add(buildExpression(red.get(3), tokenPos + 1, _types));
						expressions.add(expr);
						tokenPos = (short)(2 + expr.getLength());
						expressions.add(buildExpression(red.get(5), tokenPos, _types));
						// <StepClause>
						red = red.get(6).asReduction();
						if (red.size() >= 2) {
							// <StepClause> ::= StepKey <DecimalIntegerLiteral>
							// <StepClause> ::= StepKey '-' <DecimalIntegerLiteral>
							// <StepClause> ::= StepKey '+' <DecimalIntegerLiteral>
							tokenPos += expressions.get(1).getLength() + 1;
							expr = buildExpression(red.get(red.size() - 1), tokenPos, _types);
							if (red.size() > 2) {
								// has sign
								String opr = red.get(1).asString() + "1";
								Expression expr1 = new Expression(Expression.NodeType.OPERATOR, opr, tokenPos);
								expr.tokenPos++;
								expr1.children.addLast(expr);
								expr = expr1;
							}
							expressions.add(expr);
						}
					}
					break;
					
				case RuleConstants.PROD_FORINHEADER_FORINKEY_IDENTIFIER_INKEY:
					// <ForInHeader> ::= ForInKey Identifier InKey <ExpressionList>
					lineType = Line.LineType.LT_FOREACH_LOOP;
					expressions.add(buildExpression(red.get(1), tokenPos + 1, _types));
					// For historical reasons, <ValueList> may also be an <ExpressionList>
					buildExpressionList(red.get(3), tokenPos + 3, expressions, _types);
					break;
					
				case RuleConstants.PROD_JUMP_RETURNKEY:
					// <Jump> ::= ReturnKey
				case RuleConstants.PROD_JUMP_RETURNKEY2:
					// <Jump> ::= ReturnKey <Expression>
					lineType = Line.LineType.LT_RETURN;
					if (red.size() > 1) {
						expressions.add(buildExpression(red.get(1), tokenPos + 1, _types));
					}
					break;
				case RuleConstants.PROD_JUMP_EXITKEY:
					// <Jump> ::= ExitKey
				case RuleConstants.PROD_JUMP_EXITKEY2:
					// <Jump> ::= ExitKey <Expression>
					lineType = Line.LineType.LT_EXIT;
					if (red.size() > 1) {
						expressions.add(buildExpression(red.get(1), tokenPos + 1, _types));
					}
					break;
				case RuleConstants.PROD_JUMP_LEAVEKEY:
					// <Jump> ::= LeaveKey
				case RuleConstants.PROD_JUMP_LEAVEKEY2:
					// <Jump> ::= LeaveKey <DecimalIntegerLiteral>
					lineType = Line.LineType.LT_LEAVE;
					if (red.size() > 1) {
						expressions.add(buildExpression(red.get(1), tokenPos + 1, _types));
					}
					break;
				case RuleConstants.PROD_JUMP_THROWKEY:
					// <Jump> ::= ThrowKey <Expression>
					lineType = Line.LineType.LT_THROW;
					expressions.add(buildExpression(red.get(1), tokenPos, _types));
					break;
				case RuleConstants.PROD_CALL_CALLKEY:
					// <Call> ::= CallKey <RoutineInvocation>
				case RuleConstants.PROD_CALL_CALLKEY2:
					// <Call> ::= CallKey <LeftHandSide> <AssignmentOperator> <RoutineInvocation>
					lineType = Line.LineType.LT_ROUTINE_CALL;
					if (red.size() > 2) {
						Expression expr = new Expression(Expression.NodeType.OPERATOR,
								red.get(2).asString(), (short)tokenPos);
						expr.children.add(buildExpression(red.get(1), tokenPos, _types));
						tokenPos += expr.children.getFirst().getLength();
						expr.tokenPos = tokenPos;
						expr.children.add(buildExpression(red.get(3), tokenPos + 1, _types));
						expressions.add(expr);
					}
					else {
						expressions.add(buildExpression(red.get(1), tokenPos, _types));
					}
					break;
				
				}
				line = new Line(lineType, expressions.toArray(new Expression[expressions.size()]));
			}
			else {
				Position pos = parser.getCurrentPosition();
				int colNo = pos.getColumn() - 1;
				_problems.append(parser.getErrorMessage());
				String problem = _textToParse + " ►";
				if (_textToParse.length() >= colNo) {
					problem = _textToParse.substring(0, colNo) + " ► " + _textToParse.substring(colNo);
				}
				_problems.append(": " + problem);
				SymbolList sl = parser.getExpectedSymbols();
				if (_problems.length() + 15 < PRBL_LINE_WIDTH) {
					String sepa = " (Expected: ";
					for (Symbol sym: sl) {
						if (_problems.length() + 4 > PRBL_LINE_WIDTH) {
							_problems.append(sepa + "...");
							break;
						}
						_problems.append(sepa + sym.toString());
						sepa = " | ";
					}
					_problems.append(")");
				}
			}
//		}

		return line;
	}
	
	//---------------------- Build method for Expression objects ---------------------------

	protected Expression buildExpression(Token _token, int _tokenPos, TypeRegistry _types)
	{
		Expression expr = null;
		//String content = new String();
		if (_token.getType() == SymbolType.NON_TERMINAL) {
			Reduction reduction = _token.asReduction();
			//String rule = _reduction.getParent().toString();
			int ruleId = reduction.getParent().getTableIndex();
			//System.out.println("buildNSD_R(" + rule + ", " + _parentNode.parent + ")...");

			switch (ruleId) {
			// FIXME Will we get these at all? Won't we have the symbols instead 
			case RuleConstants.PROD_CHARACTERLITERAL_INDIRECTCHARLITERAL:
				// <CharacterLiteral> ::= IndirectCharLiteral
			case RuleConstants.PROD_CHARACTERLITERAL_STANDARDESCAPECHARLITERAL:
				// <CharacterLiteral> ::= StandardEscapeCharLiteral
			case RuleConstants.PROD_CHARACTERLITERAL_OCTALESCAPECHARLITERAL:
				// <CharacterLiteral> ::= OctalEscapeCharLiteral
			case RuleConstants.PROD_CHARACTERLITERAL_HEXESCAPECHARLITERAL:
				// <CharacterLiteral> ::= HexEscapeCharLiteral
			case RuleConstants.PROD_DECIMALINTEGERLITERAL_STARTWITHZERODECIMALINTEGERLITERAL:
				// <DecimalIntegerLiteral> ::= StartWithZeroDecimalIntegerLiteral
			case RuleConstants.PROD_DECIMALINTEGERLITERAL_STARTWITHNOZERODECIMALINTEGERLITERAL:
				// <DecimalIntegerLiteral> ::= StartWithNoZeroDecimalIntegerLiteral
			case RuleConstants.PROD_FLOATPOINTLITERAL_FLOATINGPOINTLITERAL:
				// <FloatPointLiteral> ::= FloatingPointLiteral
			case RuleConstants.PROD_FLOATPOINTLITERAL_FLOATINGPOINTLITERALEXPONENT:
				// <FloatPointLiteral> ::= FloatingPointLiteralExponent
			case RuleConstants.PROD_INTEGERLITERAL:
				// <IntegerLiteral> ::= <DecimalIntegerLiteral>
			case RuleConstants.PROD_INTEGERLITERAL_HEXINTEGERLITERAL:
				// <IntegerLiteral> ::= HexIntegerLiteral
			case RuleConstants.PROD_INTEGERLITERAL_OCTALINTEGERLITERAL:
				// <IntegerLiteral> ::= OctalIntegerLiteral
			case RuleConstants.PROD_INTEGERLITERAL_BINARYINTEGERLITERAL:
				// <IntegerLiteral> ::= BinaryIntegerLiteral
			case RuleConstants.PROD_LITERAL_BOOLEANLITERAL:
				// <Literal> ::= BooleanLiteral
			case RuleConstants.PROD_LITERAL_STRINGLITERAL:
				// <Literal> ::= StringLiteral
			case RuleConstants.PROD_LITERAL_NULLLITERAL:
				// <Literal> ::= NullLiteral
				expr = new Expression(Expression.NodeType.LITERAL, getContent_R(reduction, ""), (short)_tokenPos);
				break;
				
			case RuleConstants.PROD_NAME:
				// <Name> ::= <SimpleName>
			case RuleConstants.PROD_SIMPLENAME_IDENTIFIER:  // <SimpleName> ::= Identifier
				expr = new Expression(Expression.NodeType.IDENTIFIER, reduction.get(0).asString(), (short)_tokenPos);
				break;

			case RuleConstants.PROD_QUALIFIEDNAME_DOT_IDENTIFIER:
				// <QualifiedName> ::= <Name> '.' Identifier
			case RuleConstants.PROD_FIELDACCESS_DOT_IDENTIFIER:
				// <FieldAccess> ::= <Primary> '.' Identifier
			{
				Expression expr0 = buildExpression(reduction.get(0), _tokenPos, _types);
				_tokenPos += expr0.getLength();
				expr = new Expression(Expression.NodeType.OPERATOR, ".", (short)_tokenPos);
				expr.children.add(expr0);
				expr.children.add(new Expression(Expression.NodeType.IDENTIFIER, reduction.get(2).asString(), (short)(_tokenPos + 1)));
				break;
			}
			case RuleConstants.PROD_ARRAYINITIALIZER_LBRACE_RBRACE:  // <ArrayInitializer> ::= '{' <ExpressionList> '}'
			case RuleConstants.PROD_ARRAYINITIALIZER_LBRACE_RBRACE2:  // <ArrayInitializer> ::= '{' '}'
				expr = new Expression(Expression.NodeType.ARRAY_INITIALIZER, null, (short)_tokenPos);
				if (reduction.size() > 2) {
					buildExpressionList(reduction.get(1), _tokenPos + 1, expr.children, _types);
				}
				break;
			case RuleConstants.PROD_RECORDINITIALIZER_IDENTIFIER_LBRACE_RBRACE:
				// <RecordInitializer> ::= Identifier '{' <ComponentInitializers> '}'
			case RuleConstants.PROD_RECORDINITIALIZER_IDENTIFIER_LBRACE_RBRACE2:
				// <RecordInitializer> ::= Identifier '{' '}'
			{
				String typeName = reduction.get(0).asString();
				expr = new Expression(Expression.NodeType.RECORD_INITIALIZER, typeName, (short)_tokenPos);
				if (reduction.size() > 3) {
					Type recType = null;
					if (_types != null) {
						recType = _types.getType(typeName);
					}
					buildComponentList(reduction.get(2), _tokenPos + 1, expr.children, recType);
				}
				break;
			}
			case RuleConstants.PROD_PRIMARY_LPAREN_RPAREN:
				// <Primary> ::= '(' <Expression> ')'
				expr = buildExpression(reduction.get(1), _tokenPos + 1, _types);
				// FIXME: token counting gets corrupted
				break;
			case RuleConstants.PROD_ROUTINEINVOCATION_LPAREN_RPAREN:
				// <RoutineInvocation> ::= <Name> '(' <ExpressionList> ')'
			case RuleConstants.PROD_ROUTINEINVOCATION_LPAREN_RPAREN2:
				// <RoutineInvocation> ::= <Name> '(' ')'
				expr = buildExpression(reduction.get(0), _tokenPos, _types);
				if (expr.type == Expression.NodeType.IDENTIFIER) {
					expr.type = Expression.NodeType.FUNCTION;
					_tokenPos += 2;
				}
				else if (expr.type == Expression.NodeType.OPERATOR
						&& ".".equals(expr.text)
						&& expr.children.size() == 2
						&& expr.children.getLast().type == Expression.NodeType.IDENTIFIER) {
					Expression expr1 = new Expression(Expression.NodeType.METHOD,
							expr.children.getLast().text,
							(short)_tokenPos);
					expr1.children.addLast(expr.children.getFirst());
					_tokenPos += 3 + expr1.children.getFirst().getLength();
					expr = expr1;
				}
				else if (reduction.get(0).getType() == SymbolType.NON_TERMINAL) {
					// FIXME seems to be illegal
					String name = getContent_R(reduction.get(0).asReduction(), "");
					expr = new Expression(Expression.NodeType.FUNCTION, name, (short)_tokenPos);
					_tokenPos += 2;
				}
				else {
					// FIXME raise an error message at position _tokenPos
				}
				if (expr != null && reduction.size() > 3) {
					buildExpressionList(reduction.get(2), _tokenPos , expr.children, _types);;
				}
				break;
			case RuleConstants.PROD_METHODINVOCATION_DOT_IDENTIFIER_LPAREN_RPAREN:
				// <MethodInvocation> ::= <Primary> '.' Identifier '(' <ExpressionList> ')'
			case RuleConstants.PROD_METHODINVOCATION_DOT_IDENTIFIER_LPAREN_RPAREN2:
				// <MethodInvocation> ::= <Primary> '.' Identifier '(' ')'
				expr = new Expression(Expression.NodeType.METHOD, reduction.get(2).asString(), (short)_tokenPos);
				expr.children.add(buildExpression(reduction.get(0), _tokenPos, _types));
				if (reduction.size() > 4) {
					_tokenPos += expr.children.getFirst().getLength() + 3;
					buildExpressionList(reduction.get(4), _tokenPos, expr.children, _types);
				}
				break;
			case RuleConstants.PROD_ARRAYACCESS_LBRACKET_RBRACKET:
				// <ArrayAccess> ::= <Name> '[' <ExpressionList> ']'
			case RuleConstants.PROD_ARRAYACCESS_LBRACKET_RBRACKET2:
				// <ArrayAccess> ::= <Primary> '[' <ExpressionList> ']'
				expr = new Expression(Expression.NodeType.OPERATOR, "[]", (short)0);
				expr.children.addLast(buildExpression(reduction.get(0), _tokenPos, _types));
				expr.tokenPos += expr.children.getFirst().getLength();
				buildExpressionList(reduction.get(2), expr.tokenPos + 1, expr.children, _types);
				break;
			case RuleConstants.PROD_UNARYEXPRESSION_PLUS:
				// <UnaryExpression> ::= '+' <UnaryExpression>
			case RuleConstants.PROD_UNARYEXPRESSION_MINUS:
				// <UnaryExpression> ::= '-' <UnaryExpression>
				expr = new Expression(
						Expression.NodeType.OPERATOR,
						reduction.get(0).asString() + "1",
						(short)_tokenPos);
				expr.children.addLast(buildExpression(reduction.get(1), _tokenPos + 1, _types));
				break;
			case RuleConstants.PROD_UNARYEXPRESSIONNOTPLUSMINUS_TILDE:
				// <UnaryExpressionNotPlusMinus> ::= '~' <UnaryExpression>
			case RuleConstants.PROD_UNARYEXPRESSIONNOTPLUSMINUS_EXCLAM:
				// <UnaryExpressionNotPlusMinus> ::= '!' <UnaryExpression>
			case RuleConstants.PROD_UNARYEXPRESSIONNOTPLUSMINUS_NOTOPR:
				// <UnaryExpressionNotPlusMinus> ::= not <UnaryExpression>
				expr = new Expression(
						Expression.NodeType.OPERATOR,
						reduction.get(0).asString(),
						(short)_tokenPos);
				expr.children.addLast(buildExpression(reduction.get(1), _tokenPos + 1, _types));
				break;
			case RuleConstants.PROD_MULTIPLICATIVEEXPRESSION_TIMES:
				// <MultiplicativeExpression> ::= <MultiplicativeExpression> '*' <UnaryExpression>
			case RuleConstants.PROD_MULTIPLICATIVEEXPRESSION_DIV:
				// <MultiplicativeExpression> ::= <MultiplicativeExpression> '/' <UnaryExpression>
			case RuleConstants.PROD_MULTIPLICATIVEEXPRESSION_DIVOPR:
				// <MultiplicativeExpression> ::= <MultiplicativeExpression> DivOpr <UnaryExpression>
			case RuleConstants.PROD_MULTIPLICATIVEEXPRESSION_MODOPR:
				// <MultiplicativeExpression> ::= <MultiplicativeExpression> ModOpr <UnaryExpression>
			case RuleConstants.PROD_MULTIPLICATIVEEXPRESSION_PERCENT:
				// <MultiplicativeExpression> ::= <MultiplicativeExpression> '%' <UnaryExpression>
			case RuleConstants.PROD_ADDITIVEEXPRESSION_PLUS:
				// <AdditiveExpression> ::= <AdditiveExpression> '+' <MultiplicativeExpression>
			case RuleConstants.PROD_ADDITIVEEXPRESSION_MINUS:
				// <AdditiveExpression> ::= <AdditiveExpression> '-' <MultiplicativeExpression>
			case RuleConstants.PROD_SHIFTEXPRESSION_LTLT:
				// <ShiftExpression> ::= <ShiftExpression> '<<' <AdditiveExpression>
			case RuleConstants.PROD_SHIFTEXPRESSION_GTGT:
				// <ShiftExpression> ::= <ShiftExpression> '>>' <AdditiveExpression>
			case RuleConstants.PROD_SHIFTEXPRESSION_GTGTGT:
				// <ShiftExpression> ::= <ShiftExpression> '>>>' <AdditiveExpression>
			case RuleConstants.PROD_SHIFTEXPRESSION_SHLOPR:
				// <ShiftExpression> ::= <ShiftExpression> ShlOpr <AdditiveExpression>
			case RuleConstants.PROD_SHIFTEXPRESSION_SHROPR:
				// <ShiftExpression> ::= <ShiftExpression> ShrOpr <AdditiveExpression>
			case RuleConstants.PROD_RELATIONALEXPRESSION_LT:
				// <RelationalExpression> ::= <RelationalExpression> '<' <ShiftExpression>
			case RuleConstants.PROD_RELATIONALEXPRESSION_GT:
				// <RelationalExpression> ::= <RelationalExpression> '>' <ShiftExpression>
			case RuleConstants.PROD_RELATIONALEXPRESSION_LTEQ:
				// <RelationalExpression> ::= <RelationalExpression> '<=' <ShiftExpression>
			case RuleConstants.PROD_RELATIONALEXPRESSION_GTEQ:
				// <RelationalExpression> ::= <RelationalExpression> '>=' <ShiftExpression>
			case RuleConstants.PROD_EQUALITYEXPRESSION:
				// <EqualityExpression> ::= <RelationalExpression>
			case RuleConstants.PROD_EQUALITYEXPRESSION_EQEQ:
				// <EqualityExpression> ::= <EqualityExpression> '==' <RelationalExpression>
			case RuleConstants.PROD_EQUALITYEXPRESSION_EQ:
				// <EqualityExpression> ::= <EqualityExpression> '=' <RelationalExpression>
			case RuleConstants.PROD_EQUALITYEXPRESSION_EXCLAMEQ:
				// <EqualityExpression> ::= <EqualityExpression> '!=' <RelationalExpression>
			case RuleConstants.PROD_EQUALITYEXPRESSION_LTGT:
				// <EqualityExpression> ::= <EqualityExpression> '<>' <RelationalExpression>
			case RuleConstants.PROD_ANDEXPRESSION_AMP:
				// <AndExpression> ::= <AndExpression> '&' <EqualityExpression>
			case RuleConstants.PROD_EXCLUSIVEOREXPRESSION_CARET:
				// <ExclusiveOrExpression> ::= <ExclusiveOrExpression> '^' <AndExpression>
			case RuleConstants.PROD_INCLUSIVEOREXPRESSION_PIPE:
				// <InclusiveOrExpression> ::= <InclusiveOrExpression> '|' <ExclusiveOrExpression>
			case RuleConstants.PROD_CONDITIONALANDEXPRESSION_AMPAMP:
				// <ConditionalAndExpression> ::= <ConditionalAndExpression> '&&' <InclusiveOrExpression>
			case RuleConstants.PROD_CONDITIONALANDEXPRESSION_ANDOPR:
				// <ConditionalAndExpression> ::= <ConditionalAndExpression> AndOpr <InclusiveOrExpression>
			case RuleConstants.PROD_CONDITIONALOREXPRESSION:
				// <ConditionalOrExpression> ::= <ConditionalAndExpression>
			case RuleConstants.PROD_CONDITIONALOREXPRESSION_PIPEPIPE:
				// <ConditionalOrExpression> ::= <ConditionalOrExpression> '||' <ConditionalAndExpression>
			case RuleConstants.PROD_CONDITIONALOREXPRESSION_OROPR:
				// <ConditionalOrExpression> ::= <ConditionalOrExpression> OrOpr <ConditionalAndExpression>
				expr = new Expression(
						Expression.NodeType.OPERATOR,
						reduction.get(1).asString(),
						(short)0);
				expr.children.addLast(buildExpression(reduction.get(0), _tokenPos, _types));
				_tokenPos += expr.children.getFirst().getLength();
				expr.tokenPos = (short)_tokenPos;
				expr.children.addLast(buildExpression(reduction.get(2), _tokenPos + 1, _types));
				break;
			case RuleConstants.PROD_CONDITIONALEXPRESSION_QUESTION_COLON:
				// <ConditionalExpression> ::= <ConditionalOrExpression> '?' <Expression> ':' <ConditionalExpression>
				expr = new Expression(
						Expression.NodeType.TERNARY,
						"?,:",
						(short)0);
				expr.children.addLast(buildExpression(reduction.get(0), _tokenPos, _types));
				_tokenPos += expr.children.getFirst().getLength();
				expr.tokenPos = (short)_tokenPos;
				expr.children.addLast(buildExpression(reduction.get(2), _tokenPos + 1, _types));
				_tokenPos += expr.children.getLast().getLength() + 2;
				expr.children.addLast(buildExpression(reduction.get(4), _tokenPos, _types));
				break;
			default:
				// This should not actually happen - it is a case on non-implementation...
				if (reduction.size() > 0)
				{
					for (int i = 0; i < reduction.size(); i++)
					{
						if (reduction.get(i).getType() == SymbolType.NON_TERMINAL)
						{
							// No idea what to do here 
							buildExpression(reduction.get(i), _tokenPos + i, _types);
						}
					}
				}
			}
		}
		else {
			// TODO Find the proper translation for certain symbols...
			String content = _token.asString();
			int symId = _token.getTableIndex();
			switch (symId) {
			case SymbolConstants.SYM_MINUS:    // '-'
			case SymbolConstants.SYM_EXCLAM:   // '!'
			case SymbolConstants.SYM_EXCLAMEQ: // '!='
			case SymbolConstants.SYM_PERCENT:  // '%'
			case SymbolConstants.SYM_AMP:      // '&'
			case SymbolConstants.SYM_AMPAMP:   // '&&'
			case SymbolConstants.SYM_TIMES:    // '*'
			case SymbolConstants.SYM_DIV:      // '/'
			case SymbolConstants.SYM_COLONEQ:  // ':='
			case SymbolConstants.SYM_CARET:    // '^'
			case SymbolConstants.SYM_PIPE:     // '|'
			case SymbolConstants.SYM_PIPEPIPE: // '||'
			case SymbolConstants.SYM_TILDE:    // '~'
			case SymbolConstants.SYM_PLUS:     // '+'
			case SymbolConstants.SYM_LT:       // '<'
			case SymbolConstants.SYM_LTMINUS:  // '<-'
			case SymbolConstants.SYM_LTLT:     // '<<'
			case SymbolConstants.SYM_LTEQ:     // '<='
			case SymbolConstants.SYM_LTGT:     // '<>'
			case SymbolConstants.SYM_EQ:       // '='
			case SymbolConstants.SYM_EQEQ:     // '=='
			case SymbolConstants.SYM_GT:       // '>'
			case SymbolConstants.SYM_GTEQ:     // '>='
			case SymbolConstants.SYM_GTGT:     // '>>'
			case SymbolConstants.SYM_GTGTGT:   // '>>>'
			case SymbolConstants.SYM_DOT:      // '.'
				expr = new Expression(Expression.NodeType.OPERATOR, content, (short)_tokenPos);
				break;
			case SymbolConstants.SYM_ANDOPR:   // AndOpr
			case SymbolConstants.SYM_DIVOPR:   // DivOpr
			case SymbolConstants.SYM_MODOPR:   // ModOpr
			case SymbolConstants.SYM_NOTOPR:   // NotOpr
			case SymbolConstants.SYM_OROPR:    // OrOpr
			case SymbolConstants.SYM_SHLOPR:   // ShlOpr
			case SymbolConstants.SYM_SHROPR:   // ShrOpr
				expr = new Expression(Expression.NodeType.OPERATOR, content.toLowerCase(), (short)_tokenPos);
				break;

			//case SymbolConstants.SYM_LBRACKETRBRACKET:  // '[]'

			case SymbolConstants.SYM_IDENTIFIER:  // Identifier
				expr = new Expression(Expression.NodeType.IDENTIFIER, _token.asString(), (short)_tokenPos);
				break;

			case SymbolConstants.SYM_BOOLEANLITERAL:  // BooleanLiteral
			case SymbolConstants.SYM_FLOATINGPOINTLITERAL:  // FloatingPointLiteral
			case SymbolConstants.SYM_FLOATINGPOINTLITERALEXPONENT:  // FloatingPointLiteralExponent
			case SymbolConstants.SYM_HEXESCAPECHARLITERAL:  // HexEscapeCharLiteral
			case SymbolConstants.SYM_HEXINTEGERLITERAL:  // HexIntegerLiteral
			case SymbolConstants.SYM_INDIRECTCHARLITERAL:  // IndirectCharLiteral
			case SymbolConstants.SYM_NULLLITERAL:  // NullLiteral
			case SymbolConstants.SYM_OCTALESCAPECHARLITERAL:  // OctalEscapeCharLiteral
			case SymbolConstants.SYM_OCTALINTEGERLITERAL:  // OctalIntegerLiteral
			case SymbolConstants.SYM_BINARYINTEGERLITERAL:  // BinaryIntegerLiteral
			case SymbolConstants.SYM_STANDARDESCAPECHARLITERAL:  // StandardEscapeCharLiteral
			case SymbolConstants.SYM_STARTWITHNOZERODECIMALINTEGERLITERAL:  // StartWithNoZeroDecimalIntegerLiteral
			case SymbolConstants.SYM_STARTWITHZERODECIMALINTEGERLITERAL:  // StartWithZeroDecimalIntegerLiteral
			case SymbolConstants.SYM_STRINGLITERAL:  // StringLiteral
				expr = new Expression(Expression.NodeType.LITERAL, _token.asString(), (short)_tokenPos);
				break;
			}
			System.out.println("Token " + content + " found at position " + _tokenPos);
		}
		return expr;
	}
	
	/**
	 * Appends the expressions held by the expression list token (may be a single
	 * expression symbol/reduction or an expression list reduction) to the given
	 * {@code _expressions} list.
	 * @param _token a {@link Token} expected to be some constituent of {@link RuleConstants#PROD_EXPRESSIONLIST_COMMA}.
	 * @param _tokenPos - position of the token within the line
	 * @param _expressions - a list of {@link Expression} object
	 */
	public void buildExpressionList(Token _token, int _tokenPos, List<Expression> _expressions, TypeRegistry _types) {
		LinkedList<Expression> exprs = new LinkedList<Expression>();
		Reduction red = null;
		while (_token.getType() == SymbolType.NON_TERMINAL
				&& (red = _token.asReduction()).getParent().getTableIndex() == RuleConstants.PROD_EXPRESSIONLIST_COMMA) {
				exprs.addFirst(buildExpression(red.get(2), 0, _types));
				_token = red.get(0);
		}
		exprs.addFirst(buildExpression(_token, 0, _types));
		for (Expression expr: exprs) {
			expr.tokenPos = (short)_tokenPos;
			_tokenPos += expr.getLength() + 1;
		}
		_expressions.addAll(exprs);
	}

	/**
	 * Appends the component expressions held by the expression list token (may be a single
	 * expression symbol/reduction or an expression list reduction) to the given
	 * {@code _expressions} list.
	 * @param _token a {@link Token} expected to be some constituent of
	 *  {@link RuleConstants#PROD_EXPRESSIONLIST_COMMA}.
	 * @param _tokenPos - position of the token within the line
	 * @param _expressions - a list of {@link Expression} object
	 */
	public void buildComponentList(Token _token, int _tokenPos, LinkedList<Expression> _expressions, Type _recType) {
		LinkedList<Expression> exprs = new LinkedList<Expression>();
		TypeRegistry types = null;
		if (_recType != null) {
			types = _recType.registry;
		}
		if (_token.getType() == SymbolType.NON_TERMINAL) {
			Reduction red = _token.asReduction();
			switch (red.getParent().getTableIndex()) {
			case RuleConstants.PROD_EXPRESSIONLIST_COMMA:
				// We don't try to identify the component ids here...
				buildExpressionList(_token, _tokenPos, exprs, types);
				if (_recType != null && _recType instanceof RecordType) {
					StringList compNames = ((RecordType)_recType).getComponentNames();
					int i = 0;
					for (Expression valExpr: exprs) {
						if (i < compNames.count()) {
							Expression compExpr = new Expression(Expression.NodeType.COMPONENT, compNames.get(i), valExpr.tokenPos);
							compExpr.children.add(valExpr);
							_expressions.add(compExpr);
						}
						else {
							_expressions.add(valExpr);
						}
					}
				}
				else {
					_expressions.addAll(exprs);
				}
				break;
			case RuleConstants.PROD_COMPONENTINITIALIZERS_COMMA:
				// <ComponentInitializers> ::= <ExpressionList> ',' <ComponentInitializerList>
			{
				int ix1st = _expressions.size();
				buildComponentList(red.get(0), _tokenPos, _expressions, _recType);
				// Now we correct the token position according to the last expression
				if (_expressions.size() > ix1st) {
					Expression compExpr = _expressions.getLast();
					if (compExpr.type == Expression.NodeType.COMPONENT) {
						compExpr = compExpr.children.getFirst();
					}
					if (compExpr != null) {
						_tokenPos = compExpr.tokenPos + compExpr.getLength() + 1;
					}
				}
				red = red.get(2).asReduction();
				// NO BREAK here - we deliberately run into the next cases!
			}
			case RuleConstants.PROD_COMPONENTINITIALIZERLIST_COMMA:
				// <ComponentInitializerList> ::= <ComponentInitializerList> ',' <ComponentInitializer>
			case RuleConstants.PROD_COMPONENTINITIALIZER_IDENTIFIER_COLON:
				// <ComponentInitializer> ::= Identifier ':' <Expression>
			{
				while (red.getParent().getTableIndex() == RuleConstants.PROD_COMPONENTINITIALIZERLIST_COMMA) {
					// This will add the component at start of exprs!
					// Must be PROD_COMPONENTINITIALIZER_IDENTIFIER_COLON
					buildComponentList(red.get(2), 0, exprs, _recType);
					red = red.get(0).asReduction();
				}
				// Should be PROD_COMPONENTINITIALIZER_IDENTIFIER_COLON now
				String compName = red.get(0).asString();
				Expression valExpr = buildExpression(red.get(2), _tokenPos + 2, types);
				Expression compExpr = new Expression(Expression.NodeType.COMPONENT, compName, (short)_tokenPos);
				compExpr.children.add(valExpr);
				_expressions.addFirst(compExpr);
				_tokenPos += compExpr.getLength() + 1;
				for (Expression expr: exprs) {
					if (_tokenPos > expr.tokenPos) {
						expr.tokenPos = (short)_tokenPos;
					}
					_tokenPos += expr.getLength() + 2;	// colon and comma
				}
				_expressions.addAll(exprs);
				break;
			}
			} // end switch
		}
	}


//	/**
//	 * Helper method to retrieve and compose the text of the given reduction, combine it with previously
//	 * assembled string _content and adapt it to syntactical conventions of Structorizer. Finally return
//	 * the text phrase.
//	 * @param _content - A string already assembled, may be used as prefix, ignored or combined in another
//	 * way 
//	 * @return composed and translated text.
//	 */
//	private String translateContent(String _content)
//	{
//		//String output = getKeyword("output");
//		//String input = getKeyword("input");
//		
//		
//		//System.out.println(_content);
//		
//		/*
//		 _content:=ReplaceEntities(_content);
//		*/
//		
//		// Convert the pseudo function back to array initializers
////		int posIni = _content.indexOf(arrayIniFunc);
////		if (posIni >= 0) {
////			StringList items = Element.splitExpressionList(_content.substring(posIni + arrayIniFunc.length()), ",", true);
////			_content = _content.substring(0, posIni) + "{" + items.subSequence(0, items.count()-1).concatenate(", ") +
////					"}" + items.get(items.count()-1).substring(1);
////		}
//		
//		//_content = BString.replace(_content, ":="," \u2190 ");
//		//_content = BString.replace(_content, " = "," <- "); already done by getContent_R()!
//
//		return _content.trim();
//	}
	
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
				{
					int idx = token.getTableIndex();
					switch (idx) {
					// FIXME
					case SymbolConstants.SYM_EXCLAM:
						_content += " not ";
						break;
						default:
						_content += token.asString();
					}
				}
				break;
			default:
				break;
			}
			/* -------- End code example for text retrieval and translation -------- */
		}
		
		return _content;
	}

	/**
	 * Test method
	 * @param args
	 */
	public static void main(String[] args) {
		Syntax.loadFromINI();
		
		String[] lineTests = new String[] {
				"§FOREACH§ i §IN§ {17+ 9, -3, pow(17, 11.4, -8.1), \"doof\"}",
				"§FOR§ k <- 23/4 §TO§ pow(2, 6) §STEP§ 2",
				"§FOREACH§ val §IN§ 34 + 8 19 true \"doof\"",
				"§FOREACH§ thing §IN§ 67/5 + 8, \"fun\" + \"ny\", pow(67, 3)",
				"§COND§ not isNice", // This is rejected by the parser, hell knows why
				"§COND§ !isNice",
				"§COND§ not !isNice",
				"§COND§ !!!isNice",
				"§COND§ not (isNice)",
				"§COND§ answer == 'J' or answer == 'N'",
				"§COND§ (value < 15.5) and not (length(array)*2 >= 5) or a <> b",
				"§LEAVE§",
				"§LEAVE§ 3",
				"§RETURN§",
				"§RETURN§ {\"dull\", \"silly\", \"braindead\", \"as thick as the wall\"}",
				"§EXIT§ 5",
				"§THROW§ \"all wrong here\"",
				"§INPUT§",
				"§INPUT§ a, b[3]",
				"§INPUT§ \"prompt\" date.year, date.month",
				"§OUTPUT§",
				"§OUTPUT§ 17, a*z, 18 + \" km/h\"",
				"c <- 57 > 3 ? 12 : -5",
				"§COND§ c + 5 * 3 == 8 ? (423 * 7) : 128 + 9",
				"§OUTPUT§ (c + 5 * 3 == 8) ? (57 > 3 ? 12 : -5) : 128 + 9",
				"§COND§ (c + 5 * 3 == 8) ? 57 > 3 ? 12 : -5 : 128 + 9",
				"§COND§ a < 5 && b >= c || isDone",
				"§COND§ not hasFun(person)",
				"§COND§ q >= 5",
				"§COND§ length({8, 34, 9.7}) = 4",
				"§COND§ 2 != sqrt(8)",
				"§COND§ true",
				"§COND§ not (28 - b * 13 > 4.5 / sqrt(23) * x)",
				// Declarative stuff
				"const a <- 14",
				"var z: double",
				"var z: int <- 15",
				"unsigned int paul <- 7",
				"var plus: unsigned int <- 13",
				// "good" expressions
				"a <- 7 * (15 - sin(1.3))",
				"a <- 7 * (15 - sin(b))",
				"a[i+1] <- { 16, \"doof\", 45+9, b}",
				"§OUTPUT§ 7 * (15 - sin(1.3)), { 16, \"doof\", 45+9, b}",
				"§OUTPUT§ 7 * (15 - pow(-18, 1.3)) + len({ 16, \"doof\", 45+9, b})",
				"rec <- Date{2020, a + 4, max(29, d)}",
				"rec <- Date{year: 2020, month: a + 4, day: max(29, d)}",
				"§OUTPUT§ test[top-1]",
				"§OUTPUT§ 25 * -a - b",
				"§OUTPUT§ a < 5 && b >= c || isDone",
				"§OUTPUT§ not hasFun(person)",
				"§COND§ a = 17 and (c != 5)",
				"§OUTPUT§ word == \"nonsense\"",
				"§OUTPUT§ (18 + b) % (a + (23 * c))",
				"§OUTPUT§ 28 - b % 13 > 4.5 / sqrt(23) * x",
				"*p <- 17 + &x",
				"§OUTPUT§ a & ~(17 | 86) ^ ~b | ~c | ~1",
				"m := date.month",
				"len <- str.length()",
				"§OUTPUT§ 0b01101",
				"hx <- 0xaedf",
				// Defective lines - are to provoke SyntaxExceptions
				"§OUTPUT§ 7 * (15 - sin(1.3)) }, { 16, \"doof\", 45+9, b}",
				"6[-6 * -a] + 34",
				"§OUTPUT§ (23 + * 6",
				"§OUTPUT§ (23 + * / 6)",
				"§OUTPUT§ z * y.98",		// a double literal following to a variable
				"w <- 76[56].(aha)"	// non-identifier following to a '.' operator
		};
		
		long timeTotal = 0L;
		long maxTime = -1L;
		long minTime = -1L;
		int nTests = 0;
		LineParser parser = new LineParser();
		for (String text: lineTests) {
			StringBuilder errors = new StringBuilder();
			long startTime = System.currentTimeMillis();
			Line line = parser.parse(text, errors, null, null);
			long endTime = System.currentTimeMillis();
			if (line == null) {
				System.err.println(errors);
			}
			else {
				System.out.println(line);
			}
			long timeDiff = endTime - startTime;
			System.out.println(timeDiff);
			timeTotal += (timeDiff);
			if (nTests == 0) {
				maxTime = timeDiff;
				minTime = timeDiff;
			}
			else if (timeDiff > maxTime) {
				maxTime = timeDiff;
			}
			else if (timeDiff < minTime) {
				minTime = timeDiff;
			}
			nTests++;
		}
		System.out.println("sum = " + timeTotal + ", ave = " + (timeTotal *1.0 / nTests) + ", min = " + minTime + ", max = " + maxTime);
	}
	

}
