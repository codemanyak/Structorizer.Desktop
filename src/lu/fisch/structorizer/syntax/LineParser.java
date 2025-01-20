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
 *      Kay Gürtzig     2017-03-02      First Issue for Iden GOLDEngine (but based on Hawkins template)
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
 *      Kay Gürtzig     2021-12-04      First complete prototype version.
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import com.creativewidgetworks.goldparser.engine.*;
import com.creativewidgetworks.goldparser.engine.enums.SymbolType;

import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.For;
import lu.fisch.structorizer.elements.Instruction;
import lu.fisch.structorizer.elements.Root;
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

	/**
	 * Symbolic constants naming the table indices of the symbols of the grammar 
	 */
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
		final int SYM_UNSIGNED                             =  87;  // unsigned
		final int SYM_VARKEY                               =  88;  // VarKey
		final int SYM_ADDITIVEEXPRESSION                   =  89;  // <AdditiveExpression>
		final int SYM_ANDEXPRESSION                        =  90;  // <AndExpression>
		final int SYM_ARRAYACCESS                          =  91;  // <ArrayAccess>
		final int SYM_ARRAYDECL                            =  92;  // <ArrayDecl>
		final int SYM_ARRAYINITIALIZER                     =  93;  // <ArrayInitializer>
		final int SYM_ARRAYOF                              =  94;  // <ArrayOf>
		final int SYM_ASSIGNMENT                           =  95;  // <Assignment>
		final int SYM_ASSIGNMENTOPERATOR                   =  96;  // <AssignmentOperator>
		final int SYM_CALL                                 =  97;  // <Call>
		final int SYM_CASEDISCRIMINATOR                    =  98;  // <CaseDiscriminator>
		final int SYM_CASESELECTORS                        =  99;  // <CaseSelectors>
		final int SYM_CATCHCLAUSE                          = 100;  // <CatchClause>
		final int SYM_CHARACTERLITERAL                     = 101;  // <CharacterLiteral>
		final int SYM_COMPONENTGROUP                       = 102;  // <ComponentGroup>
		final int SYM_COMPONENTINITIALIZER                 = 103;  // <ComponentInitializer>
		final int SYM_COMPONENTINITIALIZERLIST             = 104;  // <ComponentInitializerList>
		final int SYM_COMPONENTINITIALIZERS                = 105;  // <ComponentInitializers>
		final int SYM_COMPONENTLIST                        = 106;  // <ComponentList>
		final int SYM_CONDITION                            = 107;  // <Condition>
		final int SYM_CONDITIONALANDEXPRESSION             = 108;  // <ConditionalAndExpression>
		final int SYM_CONDITIONALEXPRESSION                = 109;  // <ConditionalExpression>
		final int SYM_CONDITIONALOREXPRESSION              = 110;  // <ConditionalOrExpression>
		final int SYM_CONSTDEFINITION                      = 111;  // <ConstDefinition>
		final int SYM_DECIMALINTEGERLITERAL                = 112;  // <DecimalIntegerLiteral>
		final int SYM_DIMENSION                            = 113;  // <Dimension>
		final int SYM_DIMENSIONLIST                        = 114;  // <DimensionList>
		final int SYM_DIMSOPT                              = 115;  // <DimsOpt>
		final int SYM_ELEMENTLINE                          = 116;  // <ElementLine>
		final int SYM_ENUMDEF                              = 117;  // <EnumDef>
		final int SYM_ENUMLIST                             = 118;  // <EnumList>
		final int SYM_EQUALITYEXPRESSION                   = 119;  // <EqualityExpression>
		final int SYM_EXCLUSIVEOREXPRESSION                = 120;  // <ExclusiveOrExpression>
		final int SYM_EXPRESSION                           = 121;  // <Expression>
		final int SYM_EXPRESSIONLIST                       = 122;  // <ExpressionList>
		final int SYM_FIELDACCESS                          = 123;  // <FieldAccess>
		final int SYM_FLOATPOINTLITERAL                    = 124;  // <FloatPointLiteral>
		final int SYM_FORHEADER                            = 125;  // <ForHeader>
		final int SYM_FORINHEADER                          = 126;  // <ForInHeader>
		final int SYM_IDLIST                               = 127;  // <IdList>
		final int SYM_INCLUSIVEOREXPRESSION                = 128;  // <InclusiveOrExpression>
		final int SYM_INPUTINSTRUCTION                     = 129;  // <InputInstruction>
		final int SYM_INTEGERLITERAL                       = 130;  // <IntegerLiteral>
		final int SYM_JUMP                                 = 131;  // <Jump>
		final int SYM_LEFTHANDSIDE                         = 132;  // <LeftHandSide>
		final int SYM_LITERAL                              = 133;  // <Literal>
		final int SYM_METHODINVOCATION                     = 134;  // <MethodInvocation>
		final int SYM_MULTIPLICATIVEEXPRESSION             = 135;  // <MultiplicativeExpression>
		final int SYM_NAME                                 = 136;  // <Name>
		final int SYM_OUTPUTINSTRUCTION                    = 137;  // <OutputInstruction>
		final int SYM_POSTFIXEXPRESSION                    = 138;  // <PostfixExpression>
		final int SYM_PRIMARY                              = 139;  // <Primary>
		final int SYM_PROMPT                               = 140;  // <Prompt>
		final int SYM_QUALIFIEDNAME                        = 141;  // <QualifiedName>
		final int SYM_RANGE                                = 142;  // <Range>
		final int SYM_RANGELIST                            = 143;  // <RangeList>
		final int SYM_RECORDINITIALIZER                    = 144;  // <RecordInitializer>
		final int SYM_RELATIONALEXPRESSION                 = 145;  // <RelationalExpression>
		final int SYM_ROUTINEINVOCATION                    = 146;  // <RoutineInvocation>
		final int SYM_SHIFTEXPRESSION                      = 147;  // <ShiftExpression>
		final int SYM_SIMPLENAME                           = 148;  // <SimpleName>
		final int SYM_STEPCLAUSE                           = 149;  // <StepClause>
		final int SYM_TARGETLIST                           = 150;  // <TargetList>
		final int SYM_TYPECONSTRUCTION                     = 151;  // <TypeConstruction>
		final int SYM_TYPEDEFINITION                       = 152;  // <TypeDefinition>
		final int SYM_TYPEDESCRIPTION                      = 153;  // <TypeDescription>
		final int SYM_UNARYEXPRESSION                      = 154;  // <UnaryExpression>
		final int SYM_UNARYEXPRESSIONNOTPLUSMINUS          = 155;  // <UnaryExpressionNotPlusMinus>
		final int SYM_VARDECL1ORINIT                       = 156;  // <VarDecl1OrInit>
		final int SYM_VARDECLARATION                       = 157;  // <VarDeclaration>
		final int SYM_VARDECLARATION1                      = 158;  // <VarDeclaration1>
		final int SYM_VARINITIALISATION                    = 159;  // <VarInitialisation>
		final int SYM_VARINITOPT                           = 160;  // <VarInitOpt>
	};

	/**
	 * Symbolic constants naming the table indices of the grammar rules
	 * 
	 * @author Kay Gürtzig
	 */
	@SuppressWarnings("unused")
	private interface RuleConstants
	{
		final int PROD_ELEMENTLINE                                                =   0;  // <ElementLine> ::= <Assignment>
		final int PROD_ELEMENTLINE2                                               =   1;  // <ElementLine> ::= <VarInitialisation>
		final int PROD_ELEMENTLINE3                                               =   2;  // <ElementLine> ::= <InputInstruction>
		final int PROD_ELEMENTLINE4                                               =   3;  // <ElementLine> ::= <OutputInstruction>
		final int PROD_ELEMENTLINE5                                               =   4;  // <ElementLine> ::= <RoutineInvocation>
		final int PROD_ELEMENTLINE6                                               =   5;  // <ElementLine> ::= <VarDecl1OrInit>
		final int PROD_ELEMENTLINE7                                               =   6;  // <ElementLine> ::= <VarDeclaration>
		final int PROD_ELEMENTLINE8                                               =   7;  // <ElementLine> ::= <ConstDefinition>
		final int PROD_ELEMENTLINE9                                               =   8;  // <ElementLine> ::= <TypeDefinition>
		final int PROD_ELEMENTLINE10                                              =   9;  // <ElementLine> ::= <Condition>
		final int PROD_ELEMENTLINE11                                              =  10;  // <ElementLine> ::= <ForHeader>
		final int PROD_ELEMENTLINE12                                              =  11;  // <ElementLine> ::= <ForInHeader>
		final int PROD_ELEMENTLINE13                                              =  12;  // <ElementLine> ::= <Jump>
		final int PROD_ELEMENTLINE14                                              =  13;  // <ElementLine> ::= <Call>
		final int PROD_ELEMENTLINE15                                              =  14;  // <ElementLine> ::= <CatchClause>
		final int PROD_ELEMENTLINE16                                              =  15;  // <ElementLine> ::= <CaseDiscriminator>
		final int PROD_ELEMENTLINE17                                              =  16;  // <ElementLine> ::= <CaseSelectors>
		final int PROD_CHARACTERLITERAL_INDIRECTCHARLITERAL                       =  17;  // <CharacterLiteral> ::= IndirectCharLiteral
		final int PROD_CHARACTERLITERAL_STANDARDESCAPECHARLITERAL                 =  18;  // <CharacterLiteral> ::= StandardEscapeCharLiteral
		final int PROD_CHARACTERLITERAL_OCTALESCAPECHARLITERAL                    =  19;  // <CharacterLiteral> ::= OctalEscapeCharLiteral
		final int PROD_CHARACTERLITERAL_HEXESCAPECHARLITERAL                      =  20;  // <CharacterLiteral> ::= HexEscapeCharLiteral
		final int PROD_DECIMALINTEGERLITERAL_STARTWITHZERODECIMALINTEGERLITERAL   =  21;  // <DecimalIntegerLiteral> ::= StartWithZeroDecimalIntegerLiteral
		final int PROD_DECIMALINTEGERLITERAL_STARTWITHNOZERODECIMALINTEGERLITERAL =  22;  // <DecimalIntegerLiteral> ::= StartWithNoZeroDecimalIntegerLiteral
		final int PROD_FLOATPOINTLITERAL_FLOATINGPOINTLITERAL                     =  23;  // <FloatPointLiteral> ::= FloatingPointLiteral
		final int PROD_FLOATPOINTLITERAL_FLOATINGPOINTLITERALEXPONENT             =  24;  // <FloatPointLiteral> ::= FloatingPointLiteralExponent
		final int PROD_INTEGERLITERAL                                             =  25;  // <IntegerLiteral> ::= <DecimalIntegerLiteral>
		final int PROD_INTEGERLITERAL_HEXINTEGERLITERAL                           =  26;  // <IntegerLiteral> ::= HexIntegerLiteral
		final int PROD_INTEGERLITERAL_OCTALINTEGERLITERAL                         =  27;  // <IntegerLiteral> ::= OctalIntegerLiteral
		final int PROD_INTEGERLITERAL_BINARYINTEGERLITERAL                        =  28;  // <IntegerLiteral> ::= BinaryIntegerLiteral
		final int PROD_LITERAL                                                    =  29;  // <Literal> ::= <IntegerLiteral>
		final int PROD_LITERAL2                                                   =  30;  // <Literal> ::= <FloatPointLiteral>
		final int PROD_LITERAL_BOOLEANLITERAL                                     =  31;  // <Literal> ::= BooleanLiteral
		final int PROD_LITERAL3                                                   =  32;  // <Literal> ::= <CharacterLiteral>
		final int PROD_LITERAL_STRINGLITERAL                                      =  33;  // <Literal> ::= StringLiteral
		final int PROD_LITERAL_NULLLITERAL                                        =  34;  // <Literal> ::= NullLiteral
		final int PROD_NAME                                                       =  35;  // <Name> ::= <SimpleName>
		final int PROD_NAME2                                                      =  36;  // <Name> ::= <QualifiedName>
		final int PROD_SIMPLENAME_IDENTIFIER                                      =  37;  // <SimpleName> ::= Identifier
		final int PROD_QUALIFIEDNAME_DOT_IDENTIFIER                               =  38;  // <QualifiedName> ::= <Name> '.' Identifier
		final int PROD_ARRAYINITIALIZER_LBRACE_RBRACE                             =  39;  // <ArrayInitializer> ::= '{' <ExpressionList> '}'
		final int PROD_ARRAYINITIALIZER_LBRACE_RBRACE2                            =  40;  // <ArrayInitializer> ::= '{' '}'
		final int PROD_RECORDINITIALIZER_IDENTIFIER_LBRACE_RBRACE                 =  41;  // <RecordInitializer> ::= Identifier '{' <ComponentInitializers> '}'
		final int PROD_RECORDINITIALIZER_IDENTIFIER_LBRACE_RBRACE2                =  42;  // <RecordInitializer> ::= Identifier '{' '}'
		final int PROD_EXPRESSIONLIST                                             =  43;  // <ExpressionList> ::= <Expression>
		final int PROD_EXPRESSIONLIST_COMMA                                       =  44;  // <ExpressionList> ::= <ExpressionList> ',' <Expression>
		final int PROD_COMPONENTINITIALIZERS                                      =  45;  // <ComponentInitializers> ::= <ComponentInitializerList>
		final int PROD_COMPONENTINITIALIZERS2                                     =  46;  // <ComponentInitializers> ::= <ExpressionList>
		final int PROD_COMPONENTINITIALIZERS_COMMA                                =  47;  // <ComponentInitializers> ::= <ExpressionList> ',' <ComponentInitializerList>
		final int PROD_COMPONENTINITIALIZERLIST_COMMA                             =  48;  // <ComponentInitializerList> ::= <ComponentInitializerList> ',' <ComponentInitializer>
		final int PROD_COMPONENTINITIALIZERLIST                                   =  49;  // <ComponentInitializerList> ::= <ComponentInitializer>
		final int PROD_COMPONENTINITIALIZER_IDENTIFIER_COLON                      =  50;  // <ComponentInitializer> ::= Identifier ':' <Expression>
		final int PROD_PRIMARY                                                    =  51;  // <Primary> ::= <Literal>
		final int PROD_PRIMARY_LPAREN_RPAREN                                      =  52;  // <Primary> ::= '(' <Expression> ')'
		final int PROD_PRIMARY2                                                   =  53;  // <Primary> ::= <FieldAccess>
		final int PROD_PRIMARY3                                                   =  54;  // <Primary> ::= <MethodInvocation>
		final int PROD_PRIMARY4                                                   =  55;  // <Primary> ::= <ArrayAccess>
		final int PROD_FIELDACCESS_DOT_IDENTIFIER                                 =  56;  // <FieldAccess> ::= <Primary> '.' Identifier
		final int PROD_ROUTINEINVOCATION_LPAREN_RPAREN                            =  57;  // <RoutineInvocation> ::= <Name> '(' <ExpressionList> ')'
		final int PROD_ROUTINEINVOCATION_LPAREN_RPAREN2                           =  58;  // <RoutineInvocation> ::= <Name> '(' ')'
		final int PROD_METHODINVOCATION                                           =  59;  // <MethodInvocation> ::= <RoutineInvocation>
		final int PROD_METHODINVOCATION_DOT_IDENTIFIER_LPAREN_RPAREN              =  60;  // <MethodInvocation> ::= <Primary> '.' Identifier '(' <ExpressionList> ')'
		final int PROD_METHODINVOCATION_DOT_IDENTIFIER_LPAREN_RPAREN2             =  61;  // <MethodInvocation> ::= <Primary> '.' Identifier '(' ')'
		final int PROD_ARRAYACCESS_LBRACKET_RBRACKET                              =  62;  // <ArrayAccess> ::= <Name> '[' <ExpressionList> ']'
		final int PROD_ARRAYACCESS_LBRACKET_RBRACKET2                             =  63;  // <ArrayAccess> ::= <Primary> '[' <ExpressionList> ']'
		final int PROD_POSTFIXEXPRESSION                                          =  64;  // <PostfixExpression> ::= <Primary>
		final int PROD_POSTFIXEXPRESSION2                                         =  65;  // <PostfixExpression> ::= <Name>
		final int PROD_UNARYEXPRESSION_PLUS                                       =  66;  // <UnaryExpression> ::= '+' <UnaryExpression>
		final int PROD_UNARYEXPRESSION_MINUS                                      =  67;  // <UnaryExpression> ::= '-' <UnaryExpression>
		final int PROD_UNARYEXPRESSION                                            =  68;  // <UnaryExpression> ::= <UnaryExpressionNotPlusMinus>
		final int PROD_UNARYEXPRESSIONNOTPLUSMINUS                                =  69;  // <UnaryExpressionNotPlusMinus> ::= <PostfixExpression>
		final int PROD_UNARYEXPRESSIONNOTPLUSMINUS_TILDE                          =  70;  // <UnaryExpressionNotPlusMinus> ::= '~' <UnaryExpression>
		final int PROD_UNARYEXPRESSIONNOTPLUSMINUS_EXCLAM                         =  71;  // <UnaryExpressionNotPlusMinus> ::= '!' <UnaryExpression>
		final int PROD_UNARYEXPRESSIONNOTPLUSMINUS_NOTOPR                         =  72;  // <UnaryExpressionNotPlusMinus> ::= NotOpr <UnaryExpression>
		final int PROD_MULTIPLICATIVEEXPRESSION                                   =  73;  // <MultiplicativeExpression> ::= <UnaryExpression>
		final int PROD_MULTIPLICATIVEEXPRESSION_TIMES                             =  74;  // <MultiplicativeExpression> ::= <MultiplicativeExpression> '*' <UnaryExpression>
		final int PROD_MULTIPLICATIVEEXPRESSION_DIV                               =  75;  // <MultiplicativeExpression> ::= <MultiplicativeExpression> '/' <UnaryExpression>
		final int PROD_MULTIPLICATIVEEXPRESSION_DIVOPR                            =  76;  // <MultiplicativeExpression> ::= <MultiplicativeExpression> DivOpr <UnaryExpression>
		final int PROD_MULTIPLICATIVEEXPRESSION_MODOPR                            =  77;  // <MultiplicativeExpression> ::= <MultiplicativeExpression> ModOpr <UnaryExpression>
		final int PROD_MULTIPLICATIVEEXPRESSION_PERCENT                           =  78;  // <MultiplicativeExpression> ::= <MultiplicativeExpression> '%' <UnaryExpression>
		final int PROD_ADDITIVEEXPRESSION                                         =  79;  // <AdditiveExpression> ::= <MultiplicativeExpression>
		final int PROD_ADDITIVEEXPRESSION_PLUS                                    =  80;  // <AdditiveExpression> ::= <AdditiveExpression> '+' <MultiplicativeExpression>
		final int PROD_ADDITIVEEXPRESSION_MINUS                                   =  81;  // <AdditiveExpression> ::= <AdditiveExpression> '-' <MultiplicativeExpression>
		final int PROD_SHIFTEXPRESSION                                            =  82;  // <ShiftExpression> ::= <AdditiveExpression>
		final int PROD_SHIFTEXPRESSION_LTLT                                       =  83;  // <ShiftExpression> ::= <ShiftExpression> '<<' <AdditiveExpression>
		final int PROD_SHIFTEXPRESSION_GTGT                                       =  84;  // <ShiftExpression> ::= <ShiftExpression> '>>' <AdditiveExpression>
		final int PROD_SHIFTEXPRESSION_GTGTGT                                     =  85;  // <ShiftExpression> ::= <ShiftExpression> '>>>' <AdditiveExpression>
		final int PROD_SHIFTEXPRESSION_SHLOPR                                     =  86;  // <ShiftExpression> ::= <ShiftExpression> ShlOpr <AdditiveExpression>
		final int PROD_SHIFTEXPRESSION_SHROPR                                     =  87;  // <ShiftExpression> ::= <ShiftExpression> ShrOpr <AdditiveExpression>
		final int PROD_RELATIONALEXPRESSION                                       =  88;  // <RelationalExpression> ::= <ShiftExpression>
		final int PROD_RELATIONALEXPRESSION_LT                                    =  89;  // <RelationalExpression> ::= <RelationalExpression> '<' <ShiftExpression>
		final int PROD_RELATIONALEXPRESSION_GT                                    =  90;  // <RelationalExpression> ::= <RelationalExpression> '>' <ShiftExpression>
		final int PROD_RELATIONALEXPRESSION_LTEQ                                  =  91;  // <RelationalExpression> ::= <RelationalExpression> '<=' <ShiftExpression>
		final int PROD_RELATIONALEXPRESSION_GTEQ                                  =  92;  // <RelationalExpression> ::= <RelationalExpression> '>=' <ShiftExpression>
		final int PROD_EQUALITYEXPRESSION                                         =  93;  // <EqualityExpression> ::= <RelationalExpression>
		final int PROD_EQUALITYEXPRESSION_EQEQ                                    =  94;  // <EqualityExpression> ::= <EqualityExpression> '==' <RelationalExpression>
		final int PROD_EQUALITYEXPRESSION_EQ                                      =  95;  // <EqualityExpression> ::= <EqualityExpression> '=' <RelationalExpression>
		final int PROD_EQUALITYEXPRESSION_EXCLAMEQ                                =  96;  // <EqualityExpression> ::= <EqualityExpression> '!=' <RelationalExpression>
		final int PROD_EQUALITYEXPRESSION_LTGT                                    =  97;  // <EqualityExpression> ::= <EqualityExpression> '<>' <RelationalExpression>
		final int PROD_ANDEXPRESSION                                              =  98;  // <AndExpression> ::= <EqualityExpression>
		final int PROD_ANDEXPRESSION_AMP                                          =  99;  // <AndExpression> ::= <AndExpression> '&' <EqualityExpression>
		final int PROD_EXCLUSIVEOREXPRESSION                                      = 100;  // <ExclusiveOrExpression> ::= <AndExpression>
		final int PROD_EXCLUSIVEOREXPRESSION_CARET                                = 101;  // <ExclusiveOrExpression> ::= <ExclusiveOrExpression> '^' <AndExpression>
		final int PROD_INCLUSIVEOREXPRESSION                                      = 102;  // <InclusiveOrExpression> ::= <ExclusiveOrExpression>
		final int PROD_INCLUSIVEOREXPRESSION_PIPE                                 = 103;  // <InclusiveOrExpression> ::= <InclusiveOrExpression> '|' <ExclusiveOrExpression>
		final int PROD_CONDITIONALANDEXPRESSION                                   = 104;  // <ConditionalAndExpression> ::= <InclusiveOrExpression>
		final int PROD_CONDITIONALANDEXPRESSION_AMPAMP                            = 105;  // <ConditionalAndExpression> ::= <ConditionalAndExpression> '&&' <InclusiveOrExpression>
		final int PROD_CONDITIONALANDEXPRESSION_ANDOPR                            = 106;  // <ConditionalAndExpression> ::= <ConditionalAndExpression> AndOpr <InclusiveOrExpression>
		final int PROD_CONDITIONALOREXPRESSION                                    = 107;  // <ConditionalOrExpression> ::= <ConditionalAndExpression>
		final int PROD_CONDITIONALOREXPRESSION_PIPEPIPE                           = 108;  // <ConditionalOrExpression> ::= <ConditionalOrExpression> '||' <ConditionalAndExpression>
		final int PROD_CONDITIONALOREXPRESSION_OROPR                              = 109;  // <ConditionalOrExpression> ::= <ConditionalOrExpression> OrOpr <ConditionalAndExpression>
		final int PROD_CONDITIONALEXPRESSION                                      = 110;  // <ConditionalExpression> ::= <ConditionalOrExpression>
		final int PROD_CONDITIONALEXPRESSION_QUESTION_COLON                       = 111;  // <ConditionalExpression> ::= <ConditionalOrExpression> '?' <Expression> ':' <ConditionalExpression>
		final int PROD_EXPRESSION                                                 = 112;  // <Expression> ::= <ConditionalExpression>
		final int PROD_EXPRESSION2                                                = 113;  // <Expression> ::= <ArrayInitializer>
		final int PROD_EXPRESSION3                                                = 114;  // <Expression> ::= <RecordInitializer>
		final int PROD_ASSIGNMENT                                                 = 115;  // <Assignment> ::= <LeftHandSide> <AssignmentOperator> <Expression>
		final int PROD_LEFTHANDSIDE                                               = 116;  // <LeftHandSide> ::= <Name>
		final int PROD_LEFTHANDSIDE2                                              = 117;  // <LeftHandSide> ::= <FieldAccess>
		final int PROD_LEFTHANDSIDE3                                              = 118;  // <LeftHandSide> ::= <ArrayAccess>
		final int PROD_ASSIGNMENTOPERATOR_LTMINUS                                 = 119;  // <AssignmentOperator> ::= '<-'
		final int PROD_ASSIGNMENTOPERATOR_COLONEQ                                 = 120;  // <AssignmentOperator> ::= ':='
		final int PROD_VARINITIALISATION                                          = 121;  // <VarInitialisation> ::= <TypeDescription> <ArrayDecl> <AssignmentOperator> <Expression>
		final int PROD_VARDECL1ORINIT                                             = 122;  // <VarDecl1OrInit> ::= <VarDeclaration1> <VarInitOpt>
		final int PROD_VARINITOPT                                                 = 123;  // <VarInitOpt> ::= <AssignmentOperator> <Expression>
		final int PROD_VARINITOPT2                                                = 124;  // <VarInitOpt> ::= 
		final int PROD_ARRAYDECL_IDENTIFIER                                       = 125;  // <ArrayDecl> ::= Identifier <DimensionList>
		final int PROD_DIMENSIONLIST                                              = 126;  // <DimensionList> ::= <DimensionList> <Dimension>
		final int PROD_DIMENSIONLIST2                                             = 127;  // <DimensionList> ::= 
		final int PROD_DIMENSION_LBRACKET_RBRACKET                                = 128;  // <Dimension> ::= '[' <ShiftExpression> ']'
		final int PROD_INPUTINSTRUCTION_INPUTKEY                                  = 129;  // <InputInstruction> ::= InputKey
		final int PROD_INPUTINSTRUCTION_INPUTKEY2                                 = 130;  // <InputInstruction> ::= InputKey <Prompt> <TargetList>
		final int PROD_TARGETLIST                                                 = 131;  // <TargetList> ::= <LeftHandSide>
		final int PROD_TARGETLIST_COMMA                                           = 132;  // <TargetList> ::= <TargetList> ',' <LeftHandSide>
		final int PROD_PROMPT_STRINGLITERAL                                       = 133;  // <Prompt> ::= StringLiteral
		final int PROD_PROMPT_STRINGLITERAL_COMMA                                 = 134;  // <Prompt> ::= StringLiteral ','
		final int PROD_PROMPT                                                     = 135;  // <Prompt> ::= 
		final int PROD_OUTPUTINSTRUCTION_OUTPUTKEY                                = 136;  // <OutputInstruction> ::= OutputKey
		final int PROD_OUTPUTINSTRUCTION_OUTPUTKEY2                               = 137;  // <OutputInstruction> ::= OutputKey <ExpressionList>
		final int PROD_VARDECLARATION1_VARKEY_IDENTIFIER_COLON                    = 138;  // <VarDeclaration1> ::= VarKey Identifier ':' <TypeDescription>
		final int PROD_VARDECLARATION1_DIMKEY_IDENTIFIER_ASKEY                    = 139;  // <VarDeclaration1> ::= DimKey Identifier AsKey <TypeDescription>
		final int PROD_VARDECLARATION_VARKEY_IDENTIFIER_COMMA_COLON               = 140;  // <VarDeclaration> ::= VarKey Identifier ',' <IdList> ':' <TypeDescription>
		final int PROD_VARDECLARATION_DIMKEY_IDENTIFIER_COMMA_ASKEY               = 141;  // <VarDeclaration> ::= DimKey Identifier ',' <IdList> AsKey <TypeDescription>
		final int PROD_CONSTDEFINITION_CONSTKEY_IDENTIFIER                        = 142;  // <ConstDefinition> ::= ConstKey Identifier <AssignmentOperator> <Expression>
		final int PROD_CONSTDEFINITION_CONSTKEY_IDENTIFIER_COLON                  = 143;  // <ConstDefinition> ::= ConstKey Identifier ':' <TypeDescription> <AssignmentOperator> <Expression>
		final int PROD_CONSTDEFINITION_CONSTKEY_IDENTIFIER_ASKEY                  = 144;  // <ConstDefinition> ::= ConstKey Identifier AsKey <TypeDescription> <AssignmentOperator> <Expression>
		final int PROD_CONSTDEFINITION_CONSTKEY                                   = 145;  // <ConstDefinition> ::= ConstKey <TypeDescription> <ArrayDecl> <AssignmentOperator> <Expression>
		final int PROD_TYPEDEFINITION_TYPEKEY_IDENTIFIER_EQ                       = 146;  // <TypeDefinition> ::= TypeKey Identifier '=' <TypeConstruction>
		final int PROD_TYPECONSTRUCTION                                           = 147;  // <TypeConstruction> ::= <TypeDescription>
		final int PROD_TYPECONSTRUCTION_RECORDKEY_LBRACE_RBRACE                   = 148;  // <TypeConstruction> ::= RecordKey '{' <ComponentList> '}'
		final int PROD_TYPECONSTRUCTION_ENUM_LBRACE_RBRACE                        = 149;  // <TypeConstruction> ::= enum '{' <EnumList> '}'
		final int PROD_TYPEDESCRIPTION_IDENTIFIER                                 = 150;  // <TypeDescription> ::= Identifier <DimsOpt>
		final int PROD_TYPEDESCRIPTION_UNSIGNED_IDENTIFIER                        = 151;  // <TypeDescription> ::= unsigned Identifier <DimsOpt>
		final int PROD_TYPEDESCRIPTION                                            = 152;  // <TypeDescription> ::= <ArrayOf> <TypeDescription>
		final int PROD_DIMSOPT_LBRACKETRBRACKET                                   = 153;  // <DimsOpt> ::= <DimsOpt> '[]'
		final int PROD_DIMSOPT                                                    = 154;  // <DimsOpt> ::= 
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
		final int PROD_CONDITION_CONDKEY                                          = 171;  // <Condition> ::= CondKey <ConditionalExpression>
		final int PROD_FORHEADER_FORKEY_IDENTIFIER_TOKEY                          = 172;  // <ForHeader> ::= ForKey Identifier <AssignmentOperator> <Expression> ToKey <Expression> <StepClause>
		final int PROD_STEPCLAUSE_STEPKEY                                         = 173;  // <StepClause> ::= StepKey <DecimalIntegerLiteral>
		final int PROD_STEPCLAUSE_STEPKEY_MINUS                                   = 174;  // <StepClause> ::= StepKey '-' <DecimalIntegerLiteral>
		final int PROD_STEPCLAUSE_STEPKEY_PLUS                                    = 175;  // <StepClause> ::= StepKey '+' <DecimalIntegerLiteral>
		final int PROD_STEPCLAUSE                                                 = 176;  // <StepClause> ::= 
		final int PROD_FORINHEADER_FORINKEY_IDENTIFIER_INKEY                      = 177;  // <ForInHeader> ::= ForInKey Identifier InKey <ExpressionList>
		final int PROD_JUMP_RETURNKEY                                             = 178;  // <Jump> ::= ReturnKey
		final int PROD_JUMP_RETURNKEY2                                            = 179;  // <Jump> ::= ReturnKey <Expression>
		final int PROD_JUMP_EXITKEY                                               = 180;  // <Jump> ::= ExitKey
		final int PROD_JUMP_EXITKEY2                                              = 181;  // <Jump> ::= ExitKey <Expression>
		final int PROD_JUMP_LEAVEKEY                                              = 182;  // <Jump> ::= LeaveKey
		final int PROD_JUMP_LEAVEKEY2                                             = 183;  // <Jump> ::= LeaveKey <DecimalIntegerLiteral>
		final int PROD_JUMP_THROWKEY                                              = 184;  // <Jump> ::= ThrowKey <Expression>
		final int PROD_CALL_CALLKEY                                               = 185;  // <Call> ::= CallKey <RoutineInvocation>
		final int PROD_CALL_CALLKEY2                                              = 186;  // <Call> ::= CallKey <LeftHandSide> <AssignmentOperator> <RoutineInvocation>
		final int PROD_CALL_CALLKEY_CONSTKEY_IDENTIFIER                           = 187;  // <Call> ::= CallKey ConstKey Identifier <AssignmentOperator> <RoutineInvocation>
		final int PROD_CALL_CALLKEY_CONSTKEY_IDENTIFIER_COLON                     = 188;  // <Call> ::= CallKey ConstKey Identifier ':' <TypeDescription> <AssignmentOperator> <RoutineInvocation>
		final int PROD_CALL_CALLKEY_CONSTKEY_IDENTIFIER_ASKEY                     = 189;  // <Call> ::= CallKey ConstKey Identifier AsKey <TypeDescription> <AssignmentOperator> <RoutineInvocation>
		final int PROD_CATCHCLAUSE_CATCHKEY_IDENTIFIER                            = 190;  // <CatchClause> ::= CatchKey Identifier
		final int PROD_CATCHCLAUSE_CATCHKEY_IDENTIFIER_COLON                      = 191;  // <CatchClause> ::= CatchKey Identifier ':' <TypeDescription>
		final int PROD_CATCHCLAUSE_CATCHKEY_IDENTIFIER_ASKEY                      = 192;  // <CatchClause> ::= CatchKey Identifier AsKey <TypeDescription>
		final int PROD_CATCHCLAUSE_CATCHKEY_IDENTIFIER2                           = 193;  // <CatchClause> ::= CatchKey <TypeDescription> Identifier
		final int PROD_CASEDISCRIMINATOR_CASEKEY                                  = 194;  // <CaseDiscriminator> ::= CaseKey <ConditionalExpression>
		final int PROD_CASESELECTORS_SELECTORKEY                                  = 195;  // <CaseSelectors> ::= SelectorKey <ExpressionList>
	};
	
	/**
	 * Collection of expression list rule identifiers used in
	 * {@link #buildExpressionList(Token, int, List, TypeRegistry)}
	 */
	private static final HashSet<Integer> EXPR_LIST_RULE_IDS = new HashSet<Integer>();
	static {
		EXPR_LIST_RULE_IDS.add(RuleConstants.PROD_EXPRESSIONLIST_COMMA);
		EXPR_LIST_RULE_IDS.add(RuleConstants.PROD_IDLIST_COMMA_IDENTIFIER);
	}
	
	//----------------------------- Preprocessor -----------------------------

	private static final String[] IO_KEYS = {"input", "output"};
	private static final String[] FOR_KEYS = {"preFor", "postFor", "stepFor"};
	private static final String[] FOR_IN_KEYS = {"preForIn", "postForIn"};
	private static final String[] JUMP_KEYS = {"preReturn", "preLeave", "preExit", "preThrow"};
	
	/**
	 * Preprocesses the given element line for the parser, i.e. replaces configured
	 * keywords by grammar-defined ones, inserts classifying prefixes where needed
	 * and unifies operator symbols.
	 * @param _line - the element line to be parsed or checked
	 * @param _element - the owning NSD element
	 * @param _lineNo - the line index
	 * @param _unifyOprs - whether to unify all operators (otherwise only "not" will be
	 *     replaced by "!", due to some parser difficulties)
	 * @return the preprocessed line as token list
	 */
	public TokenList preprocessLine(String _line, Element _element, int _lineNo, boolean _unifyOprs)
	{
		String className = _element.getClass().getSimpleName();
		TokenList tokens = new TokenList(_line, true);
		//StringList tokens1 = Syntax.splitLexically(_line, true, false);
		//if (tokens.indexOf(tokens1, 0, true) != 0) {
		//	System.err.println("Splitting differs for: " + _line);
		//	System.out.println(tokens);
		//	System.out.println(tokens1);
		//}
		if (className.equals("Alternative")
				|| className.equals("While")
				|| className.equals("Repeat")) {
			Syntax.removeSplitDecorators(tokens);
			tokens.add(0, "§COND§");
		}
		else if (className.equals("Call")) {
			tokens.add(0, "§CALL§");
		}
		else if (className.equals("Case")) {
			if (_lineNo == 0) {
				Syntax.removeDecorators(tokens);
				tokens.add(0, "§CASE§");
			}
			else {
				tokens.add(0, "§SELECT§");
			}
		}
		else if (className.equals("Jump")) {
			if (tokens.isBlank()) {
				tokens.trim();
				tokens.add("§LEAVE§");
			}
			else {
				for (String key: JUMP_KEYS) {
					TokenList splitKey = Syntax.getSplitKeyword(key);
					if (tokens.indexOf(splitKey, 0, !Syntax.ignoreCase) == 0) {
						tokens.remove(1, splitKey.size());
						tokens.set(0, "§" + key.substring(3).toUpperCase() + "§");
						break;
					}
				}
			}
		}
		else if (className.equals("Try")) {
			tokens.add(0, "§CATCH§");
		}
		else {
			if (className.equals("For")) {
				String[] keys;
				String[] markers;
				if (((For)_element).isForInLoop()) {
					keys = FOR_IN_KEYS;
					markers = new String[] {"§FOREACH§", "§IN§"};
				}
				else {
					keys = FOR_KEYS;
					markers = new String[] {"§FOR§", "§TO§", "§STEP§"};
				}
				for (int i = 0; i < keys.length; i++) {
					String keyWord = Syntax.getKeyword(keys[i]);
					TokenList splitKey = new TokenList(keyWord, false);
					int posKey = tokens.indexOf(splitKey, 0, !Syntax.ignoreCase);
					if (posKey >= 0) {
						tokens.remove(posKey+1, posKey + splitKey.size());
						tokens.set(posKey, markers[i]);
					}
				}
			}
			else if (className.equals("Instruction")) {
				// FIXME ugly workaround for type definitions: we suppress operator unification
				if (Instruction.isTypeDefinition(_line)) {
					_unifyOprs = false;
				}
				TokenList splitKey = Syntax.getSplitKeyword("preReturn");
				if (tokens.indexOf(splitKey, 0, !Syntax.ignoreCase) == 0) {
					tokens.remove(1, splitKey.size());
					tokens.set(0, "§RETURN§");
				}
				else {
					for (String key: IO_KEYS) {
						splitKey = Syntax.getSplitKeyword(key);
						if (tokens.indexOf(splitKey, 0, !Syntax.ignoreCase) == 0) {
							tokens.remove(1, splitKey.size());
							tokens.set(0, "§" + key.toUpperCase() + "§");
							break;
						}
					}
				}
			}
		}
		if (_unifyOprs) {
			Syntax.unifyOperators(tokens, false);
		}
		else {
			// Grammar does not cope with "not" tokens in front of identifiers
			tokens.replaceAll("not", "!", false);
		}
		return tokens;
	}

	/**
	 * Preprocesses the given element line for the parser, i.e. replaces configured
	 * keywords by grammar-defined ones, inserts classifying prefixes where needed
	 * and unifies operator symbols.
	 * 
	 * @param _line - the element line to be parsed or checked
	 * @param _element - the owning NSD element
	 * @param _lineNo - the line index
	 * @param _unifyOprs - whether to unify all operators (otherwise only "not" will be
	 *     replaced by "!", due to some parser difficulties)
	 * @return the preprocessed line as token list
	 */
	public TokenList preprocessLine(TokenList tokens, Element _element, int _lineNo, boolean _unifyOprs)
	{
		String className = _element.getClass().getSimpleName();
		//StringList tokens1 = Syntax.splitLexically(_line, true, false);
		//if (tokens.indexOf(tokens1, 0, true) != 0) {
		//	System.err.println("Splitting differs for: " + _line);
		//	System.out.println(tokens);
		//	System.out.println(tokens1);
		//}
		if (className.equals("Alternative")
				|| className.equals("While")
				|| className.equals("Repeat")) {
			Syntax.removeDecorators(tokens);
			tokens.add(0, "§COND§");
		}
		else if (className.equals("Call")) {
			tokens.add(0, "§CALL§");
		}
		else if (className.equals("Case")) {
			if (_lineNo == 0) {
				Syntax.removeDecorators(tokens);
				tokens.add(0, "§CASE§");
			}
			else {
				tokens.add(0, "§SELECT§");
			}
		}
		else if (className.equals("Jump")) {
			if (tokens.isBlank()) {
				tokens.trim();
				tokens.add("§LEAVE§");
			}
			else {
				tokens.replaceAll("§PRELEAVE§", "§LEAVE§");
				tokens.replaceAll("§PRERETURN§", "§RETURN§");
				tokens.replaceAll("§PREEXIT§", "§EXIT§");
				tokens.replaceAll("§PRETHROW§", "§THROW§");
			}
		}
		else if (className.equals("Try")) {
			tokens.add(0, "§CATCH§");
		}
		else if (className.equals("For")){
			String[] keys;
			String[] markers;
			if (((For)_element).isForInLoop()) {
				keys = FOR_IN_KEYS;
				markers = new String[] {"§FOREACH§", "§IN§"};
			}
			else {
				keys = FOR_KEYS;
				markers = new String[] {"§FOR§", "§TO§", "§STEP§"};
			}
			for (int i = 0; i < keys.length; i++) {
				String key = keys[i];
				int posKey = tokens.indexOf("§" + key.toUpperCase() +"§");
				if (posKey == 0) {
					// Just replace the marker
					tokens.set(0, markers[i]);
				}
			}
		}
		else if (className.equals("Instruction")) {
			if (Instruction.isTypeDefinition(tokens, null)) {
				// FIXME ugly workaround for type definitions: we suppress operator unification
				_unifyOprs = false;
			}
			else {
				tokens.replaceAll("§PRERETURN§", "§RETURN§");
			}
		}		
		if (_unifyOprs) {
			Syntax.unifyOperators(tokens, false);
		}
		else {
			// Grammar does not cope with "not" tokens in front of identifiers
			tokens.replaceAll("not", "!", false);
		}
		return tokens;
	}
	
	//----------------------------- Postprocessor ----------------------------
	
	private static final String[] NULLABLE_MARKERS = {"§COND§", "§CATCH§", "§CALL§", "§CASE§", "§SELECT§"};

	private String undoReplacements(String line) {
		for (String marker: NULLABLE_MARKERS) {
			line = line.replace(marker, "");
		}
		line = line.replace("§INPUT§", Syntax.getKeyword("input"))
				.replace("§OUTPUT§", Syntax.getKeyword("output"))
				.replace("§FOR§", Syntax.getKeyword("preFor"))
				.replace("§TO§", Syntax.getKeyword("postFor"))
				.replace("§STEP§", Syntax.getKeyword("stepFor"))
				.replace("§FOREACH§", Syntax.getKeyword("preForIn"))
				.replace("§IN§", Syntax.getKeyword("postForIn"))
				.replace("§RETURN§", Syntax.getKeyword("preReturn"))
				.replace("§LEAVE§", Syntax.getKeyword("preLeave"))
				.replace("§EXIT§", Syntax.getKeyword("preExit"))
				.replace("§THROW§", Syntax.getKeyword("preThrow"));
		return line.trim();
	}
	
	//----------------------- Actual check / translation ---------------------
		
	/**
	 * Checks the syntax of the given Element line {@code _lineToCheck}
	 * 
	 * @param _lineToCheck - the preprocessed source line to be checked against the
	 *    line grammar
	 * @return an error description with the syntax position marked in the source
	 *    line
	 * 
	 * @see #preprocessLine(String, Element, int, boolean)
	 * @see #parse(String, Element, int, TypeRegistry)
	 */
	public String check(String _lineToCheck)
	{
		String trouble = null;
		boolean parsedWithoutError = parser.parseSourceStatements(_lineToCheck);
		if (!parsedWithoutError) {
			StringBuilder sb = new StringBuilder();
			composeErrorString(_lineToCheck, sb);
			trouble = sb.toString();
		}
		return trouble;
	}

	/**
	 * Parses the given Element line {@code _textToParse} for the {@link Element}. A possible
	 * parsing error description will be available via {@link Line#getParserError()}.
	 * 
	 * @param _textToParse - the (unbroken) line to parse (must already have been preprocessed
	 *     in case {@code _element} is {@code null}!)
	 * @param _element - the origin Element (if available) or {@code null} (in the latter
	 *     case the line must already have been preprocessed!)
	 * @param _lineNo - the number of the line within the origin element
	 * @param _types - a {@link TypeRegistry} or {@code null}
	 * @return The composed {@link Line} object, or {@code null} in case of an error or
	 *     lacking implementation
	 * 
	 * @see #preprocessLine(String, Element, int, boolean)
	 * @see #check(String)
	 */
	public Line parse(String _textToParse, Element _element, int _lineNo, TypeRegistry _types)
	{
		Line line = null;
		StringBuilder problems = new StringBuilder();
		Line.LineType lineType = Line.LineType.LT_RAW;
		ArrayList<Expression> expressions = new ArrayList<Expression>();
//		synchronized(this) {
			if (_element != null) {
				_textToParse = preprocessLine(_textToParse, _element, _lineNo, true).getString();
			}
			boolean parsedWithoutError = parser.parseSourceStatements(_textToParse);
			Type dataType = null;

			if (parsedWithoutError) {
				try {
					// FIXME we should rely on the token positions
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
						tokenPos += expr.children.get(0).getTokenCount(true);
						expr.tokenPos = tokenPos;
						expr.children.add(buildExpression(red.get(2), tokenPos + 1, _types));
						break;
					}
					case RuleConstants.PROD_VARDECL1ORINIT: {
						// <VarDecl1OrInit> ::= <VarDeclaration1> <VarInitOpt>
						lineType = Line.LineType.LT_VAR_DECL;
						Reduction declRed = red.get(0).asReduction();
						// <VarDeclaration1> ::= VarKey Identifier ':' <TypeDescription>
						// <VarDeclaration1> ::= DimKey Identifier AsKey <TypeDescription>
						String id = getContent_R(declRed.get(1), "");
						Expression idExpr = new Expression(Expression.NodeType.IDENTIFIER, id, ++tokenPos);
						short[] tokenCounter = {(short)(tokenPos + 2)};
						dataType = buildType(declRed.get(3), _types, tokenCounter, _element);
						red = red.get(1).asReduction();
						// <VarInitOpt> ::= <AssignmentOperator> <Expression>
						// <VarInitOpt> ::= 
						if (red.size() > 0) {
							lineType = Line.LineType.LT_VAR_INIT;
							Expression initExpr = new Expression(Expression.NodeType.OPERATOR, getContent_R(red.get(0), ""), ++tokenCounter[0]);
							initExpr.children.add(idExpr);
							initExpr.children.add(buildExpression(red.get(1), ++tokenCounter[0], _types));
							expressions.add(initExpr);
						}
						else {
							expressions.add(idExpr);
						}
						// TODO add the type mapping for the variable to _types?
						break;
					}
					case RuleConstants.PROD_VARINITIALISATION: {
						// <VarInitialisation> ::= <TypeDescription> <ArrayDecl> <AssignmentOperator> <Expression>
						lineType = Line.LineType.LT_VAR_INIT;
						short[] tokenCounter = {tokenPos};
						dataType = buildType(red.get(0), _types, tokenCounter, _element);
						Reduction arrayDeclRed = red.get(1).asReduction();
						// Identifier
						tokenPos = (short)tokenCounter[0];
						String id = getContent_R(arrayDeclRed.get(0), "");
						// <DimensionList>
						tokenCounter[0]++;
						dataType = applyArrayDimensions(arrayDeclRed.get(1).asReduction(), dataType, _types, tokenCounter, _element);
						Expression initExpr = new Expression(Expression.NodeType.OPERATOR, getContent_R(red.get(2), ""), (short)(tokenPos+1));
						initExpr.children.add(new Expression(Expression.NodeType.IDENTIFIER, id, tokenPos));
						initExpr.children.add(buildExpression(red.get(3), tokenCounter[0]+1, _types));
						expressions.add(initExpr);
						// TODO add the type mapping for the variable to _types?
						break;
					}
					case RuleConstants.PROD_INPUTINSTRUCTION_INPUTKEY:
						// <InputInstruction> ::= InputKey
					case RuleConstants.PROD_INPUTINSTRUCTION_INPUTKEY2:
						// <InputInstruction> ::= InputKey <Prompt> <TargetList>
						lineType = Line.LineType.LT_INPUT;
						if (red.size() > 1) {
							LinkedList<Expression> targets = new LinkedList<Expression>();
							Reduction promptRed = red.get(1).asReduction();
							if (promptRed.size() > 0) {
								expressions.add(buildExpression(promptRed.get(0), tokenPos + 1, _types));
								tokenPos += expressions.get(0).getTokenCount(true);
								if (red.size() > 1) {
									tokenPos++;
								}
							}
							else {
								expressions.add(null);
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
								tokenPos += tgt.getTokenCount(true) + 1;
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
						
					case RuleConstants.PROD_ROUTINEINVOCATION_LPAREN_RPAREN:
					case RuleConstants.PROD_ROUTINEINVOCATION_LPAREN_RPAREN2:
						lineType = Line.LineType.LT_ROUTINE_CALL;
						expressions.add(buildExpression(parser.getCurrentToken(), tokenPos, _types));
						break;

					case RuleConstants.PROD_VARDECLARATION_VARKEY_IDENTIFIER_COMMA_COLON:
						// <VarDeclaration> ::= VarKey Identifier ',' <IdList> ':' <TypeDescription>
					case RuleConstants.PROD_VARDECLARATION_DIMKEY_IDENTIFIER_COMMA_ASKEY:
						// <VarDeclaration> ::= DimKey Identifier ',' <IdList> AsKey <TypeDescription>
						lineType = Line.LineType.LT_VAR_DECL;
						{
							
							Expression idExpr1 = buildExpression(red.get(1), ++tokenPos, _types);
							tokenPos += idExpr1.getTokenCount(true);
							expressions.add(idExpr1);
							this.buildExpressionList(red.get(3), tokenPos + 1, expressions, _types);
							for (Expression expr: expressions) {
								tokenPos += expr.getTokenCount(true) + 1;
							}
							short[] tokenCounter = {++tokenPos};
							dataType = buildType(red.get(5), _types, tokenCounter, _element);
						}
						// TODO add the type mapping for the variables to _types?
						break;

					case RuleConstants.PROD_CONSTDEFINITION_CONSTKEY_IDENTIFIER: {
						// <ConstDefinition> ::= ConstKey Identifier <AssignmentOperator> <Expression>
						lineType = Line.LineType.LT_CONST_DEF;
						Expression expr = new Expression(Expression.NodeType.OPERATOR,
								getContent_R(red.get(2).asReduction(), ""), (short)2);
						String id = getContent_R(red.get(1), "");
						expr.children.add(new Expression(Expression.NodeType.IDENTIFIER, id, ++tokenPos));
						expr.children.add(buildExpression(red.get(3), ++tokenPos, _types));
						expressions.add(expr);
						// TODO add the constant value expression to the constant map?
						// TODO add the type mapping for the constant to _types?
						break;
					}
					case RuleConstants.PROD_CONSTDEFINITION_CONSTKEY_IDENTIFIER_COLON:
						// <ConstDefinition> ::= ConstKey Identifier ':' <TypeDescription> <AssignmentOperator> <Expression>
					case RuleConstants.PROD_CONSTDEFINITION_CONSTKEY_IDENTIFIER_ASKEY:
						// <ConstDefinition> ::= ConstKey Identifier AsKey <TypeDescription> <AssignmentOperator> <Expression>
						lineType = Line.LineType.LT_CONST_DEF;
						{
							short[] tokenCounter = {(short)(tokenPos+3)};
							dataType = buildType(red.get(3), _types, tokenCounter, _element);
							Expression expr = new Expression(Expression.NodeType.OPERATOR,
									getContent_R(red.get(4).asReduction(), ""), ++tokenCounter[0]);
							String id = getContent_R(red.get(1), "");
							expr.children.add(new Expression(Expression.NodeType.IDENTIFIER, id, ++tokenPos));
							expr.children.add(buildExpression(red.get(5), ++tokenCounter[0], _types));
							expressions.add(expr);
							// TODO add the constant value expression to the constant map?
							// TODO add the type mapping for the constant to _types?
						}
						break;
					case RuleConstants.PROD_CONSTDEFINITION_CONSTKEY: {
						// <ConstDefinition> ::= ConstKey <TypeDescription> <ArrayDecl> <AssignmentOperator> <Expression>
						lineType = Line.LineType.LT_CONST_DEF;
						short[] tokenCounter = {(short)(tokenPos+1)};
						dataType = buildType(red.get(1), _types, tokenCounter, _element);
						Reduction arrayDeclRed = red.get(2).asReduction();
						// Identifier
						tokenPos = (short)tokenCounter[0];
						String id = getContent_R(arrayDeclRed.get(0), "");
						// <DimensionList>
						tokenCounter[0]++;
						dataType = applyArrayDimensions(arrayDeclRed.get(1).asReduction(), dataType, _types, tokenCounter, _element);
						Expression expr = new Expression(Expression.NodeType.OPERATOR,
								getContent_R(red.get(3).asReduction(), ""), ++tokenCounter[0]);
						expr.children.add(new Expression(Expression.NodeType.IDENTIFIER, id, tokenPos));
						expr.children.add(buildExpression(red.get(4), ++tokenCounter[0], _types));
						expressions.add(expr);
						// TODO add the constant value expression to the constant map?
						// TODO add the type mapping for the constant to _types?
						break;
					}
					case RuleConstants.PROD_TYPEDEFINITION_TYPEKEY_IDENTIFIER_EQ:
						// <TypeDefinition> ::= TypeKey Identifier '=' <TypeConstruction>
						lineType = Line.LineType.LT_TYPE_DEF;
						try {
							String id = getContent_R(red.get(1), "");
							short[] tokenCounter = {(short)(tokenPos + 2)};
							dataType = buildType(red.get(3), id, _types, tokenCounter, _element);
							// FIXME: should we actually modify the type registry here?
							//if (_types != null && dataType != null) {
							//	_types.putType(dataType, _element, _lineNo, true);
							//}
						}
						catch (SyntaxException ex) {
							problems.append(ex.getMessage());
						}
						break;

					case RuleConstants.PROD_CONDITION_CONDKEY:
						// <Condition> ::= CondKey <ConditionalExpression>
						lineType = Line.LineType.LT_CONDITION;
						expressions.add(buildExpression(red.get(1), 0, _types));
						break;

					case RuleConstants.PROD_FORHEADER_FORKEY_IDENTIFIER_TOKEY:
						// <ForHeader> ::= ForKey Identifier <AssignmentOperator> <Expression> ToKey <Expression> <StepClause>
						lineType = Line.LineType.LT_FOR_LOOP;
						buildForLineExpressions(red, _types, expressions, tokenPos);
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
							tokenPos += expr.children.getFirst().getTokenCount(true);
							expr.tokenPos = tokenPos;
							expr.children.add(buildExpression(red.get(3), tokenPos + 1, _types));
							expressions.add(expr);
						}
						else {
							expressions.add(buildExpression(red.get(1), tokenPos, _types));
						}
						break;
					case RuleConstants.PROD_CALL_CALLKEY_CONSTKEY_IDENTIFIER:
						// <Call> ::= CallKey ConstKey Identifier <AssignmentOperator> <RoutineInvocation>
					case RuleConstants.PROD_CALL_CALLKEY_CONSTKEY_IDENTIFIER_COLON:
						// <Call> ::= CallKey ConstKey Identifier ':' <TypeDescription> <AssignmentOperator> <RoutineInvocation>
					case RuleConstants.PROD_CALL_CALLKEY_CONSTKEY_IDENTIFIER_ASKEY:
						// <Call> ::= CallKey ConstKey Identifier AsKey <TypeDescription> <AssignmentOperator> <RoutineInvocation>
						lineType = Line.LineType.LT_CONST_FUNCT_CALL;
						{
							Expression idExpr = buildExpression(red.get(2), tokenPos + 2, _types);
							int ixAsgnOpr = 3;
							tokenPos += ixAsgnOpr;
							if (red.size() > 4) {
								ixAsgnOpr++;
								short[] tokenCounter = {++tokenPos};
								dataType = buildType(red.get(4), _types, tokenCounter, _element);
								tokenPos = (short)(tokenCounter[0] + 1);
								// TODO add the type mapping for the constant to _types?
							}
							Expression expr = new Expression(Expression.NodeType.OPERATOR,
									red.get(ixAsgnOpr).asString(), tokenPos);
							expr.children.add(idExpr);
							expr.children.add(buildExpression(red.get(ixAsgnOpr + 1), tokenPos + 1, _types));
							expressions.add(expr);
							// TODO add the constant value expression to the constant map?
						}
						break;
						
					case RuleConstants.PROD_CASEDISCRIMINATOR_CASEKEY:
						// <CaseDiscriminator> ::= CaseKey <ConditionalExpression>
						lineType = Line.LineType.LT_CASE;
						expressions.add(buildExpression(red.get(1), tokenPos + 1, _types));
						break;
						
					case RuleConstants.PROD_CASESELECTORS_SELECTORKEY:
						// <CaseSelectors> ::= SelectorKey <ExpressionList>
						lineType = Line.LineType.LT_SELECTOR;
						buildExpressionList(red.get(1), tokenPos + 1, expressions, _types);
						break;
						
					case RuleConstants.PROD_CATCHCLAUSE_CATCHKEY_IDENTIFIER2:
						// <CatchClause> ::= CatchKey <TypeDescription> Identifier
						lineType = Line.LineType.LT_CATCH;
						{
							short[] tokenCounter = {(short)(tokenPos + 1)};
							dataType = buildType(red.get(1), _types, tokenCounter, _element);
							expressions.add(buildExpression(red.get(2), tokenCounter[0], _types));
						}
						break;
					case RuleConstants.PROD_CATCHCLAUSE_CATCHKEY_IDENTIFIER:
						// <CatchClause> ::= CatchKey Identifier
					case RuleConstants.PROD_CATCHCLAUSE_CATCHKEY_IDENTIFIER_ASKEY:
						// <CatchClause> ::= CatchKey Identifier AsKey <TypeDescription>
					case RuleConstants.PROD_CATCHCLAUSE_CATCHKEY_IDENTIFIER_COLON:
						// <CatchClause> ::= CatchKey Identifier ':' <TypeDescription>
						lineType = Line.LineType.LT_CATCH;
						expressions.add(buildExpression(red.get(1), tokenPos + 1, _types));
						if (red.size() > 3) {
							short[] tokenCounter = {(short)(tokenPos+3)};
							dataType = buildType(red.get(3), _types, tokenCounter, _element);
						}
						break;
					}
					line = new Line(lineType, expressions.toArray(new Expression[expressions.size()]), dataType);
				}
				catch (SyntaxException ex) {
					problems.append(ex.getMessage());
					int pos = ex.getPosition();
					TokenList tokens = new TokenList(_textToParse, true);
					problems.append(": ");
					int ix = 0;
					while (ix < tokens.size() && pos > 0) {
						String token = tokens.get(ix++);
						if (!token.isBlank()) {
							pos--;
						}
						problems.append(token);
					}
					problems.append(" ► ");
					problems.append(tokens.subSequenceToEnd(ix).getString());
					line = new Line(lineType, problems.toString());
				}
			}
			else {
				composeErrorString(_textToParse, problems);
				line = new Line(lineType, problems.toString());
			}
//		}

		return line;
	}

	/**
	 * Builds the member expressions (start value assignment, end value, step value) of a
	 * counter-controlled FOR loop header represented by {@code _reduction} and adds them
	 * to {@code _expressions}
	 * 
	 * @param _reduction - {@link Reduction} representing the FOR loop header syntax tree
	 * @param _types - a {@link TypeRegistry} for the expression validation, or {@code null}
	 * @param _expressions - a list of {@link Expression} objects to be accomplished
	 * @param _tokenPos - the index of the first token of the header line
	 * 
	 * @throws SyntaxException in case of expression errors
	 */
	private void buildForLineExpressions(Reduction _reduction, TypeRegistry _types, ArrayList<Expression> _expressions,
			short _tokenPos) throws SyntaxException {
		{
			Expression expr = new Expression(Expression.NodeType.OPERATOR,
					getContent_R(_reduction.get(2).asReduction(), ""), (short)(_tokenPos));
			expr.children.add(buildExpression(_reduction.get(1), _tokenPos + 1, _types));
			_tokenPos += 2 + expr.getTokenCount(true);
			expr.children.add(buildExpression(_reduction.get(3), _tokenPos + 1, _types));
			_expressions.add(expr);
			_tokenPos = (short)(2 + expr.getTokenCount(true));
			_expressions.add(buildExpression(_reduction.get(5), _tokenPos, _types));
			// <StepClause>
			_reduction = _reduction.get(6).asReduction();
			if (_reduction.size() >= 2) {
				// <StepClause> ::= StepKey <DecimalIntegerLiteral>
				// <StepClause> ::= StepKey '-' <DecimalIntegerLiteral>
				// <StepClause> ::= StepKey '+' <DecimalIntegerLiteral>
				_tokenPos += _expressions.get(1).getTokenCount(true) + 1;
				expr = buildExpression(_reduction.get(_reduction.size() - 1), _tokenPos, _types);
				if (_reduction.size() > 2) {
					// has sign
					String opr = _reduction.get(1).asString() + "1";
					Expression expr1 = new Expression(Expression.NodeType.OPERATOR, opr, _tokenPos);
					expr.tokenPos++;
					expr1.children.addLast(expr);
					expr = expr1;
				}
				_expressions.add(expr);
			}
		}
	}

	//---------------------- Error message composition -----------------------

	/**
	 * Composes an error description with position marked in the source text
	 * (in case a preceding parser run caused a syntax error).
	 * 
	 * @param _textToParse - the parsed line
	 * @param _problems - StringBuilder the error description is to be appended to
	 */
	private void composeErrorString(String _textToParse, StringBuilder _problems) {
		Position pos = parser.getCurrentPosition();
		int colNo = pos.getColumn() - 1;
		_problems.append(parser.getErrorMessage());
		String problem = _textToParse + " ►";
		if (_textToParse.length() >= colNo) {
			problem = _textToParse.substring(0, colNo) + " ► " + _textToParse.substring(colNo);
		}
		_problems.append(": " + undoReplacements(problem).trim());
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
	
	//------------------- Build method for Expression objects ----------------

	/**
	 * Recursively constructs an {@link Expression} tree from the given {@link Token} and
	 * its possible substructure.
	 * 
	 * @param _token - the GOLDEngine token being the root of the parsed expression
	 * @param _tokenPos - the number of the token within the line
	 * @param _types - a {@link TypeRegistry} filled by the preceding lines, or {@code null}
	 * @return the built {@link Expression} object, or {@code null} if build failed.
	 * @throws SyntaxException in case of semantic errors
	 * 
	 * @see #buildExpression(Reduction, int, TypeRegistry)
	 * @see #buildExpressionList(Token, int, List, TypeRegistry)
	 */
	protected Expression buildExpression(Token _token, int _tokenPos, TypeRegistry _types) throws SyntaxException
	{
		Expression expr = null;
		if (_token.getType() == SymbolType.NON_TERMINAL) {
			expr = buildExpression(_token.asReduction(), _tokenPos, _types);
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
			// FIXME Remove this after test completion
			System.out.println("Token " + content + " found at position " + _tokenPos);
		}
		return expr;
	}
	
	/**
	 * Recursively constructs an {@link Expression} tree from the given {@link Reduction}
	 * and its substructure.
	 * 
	 * @param _reduction - the GOLDEngine token being the root of the parsed expression
	 * @param _tokenPos - the number of the token within the line
	 * @param _types - a {@link TypeRegistry} filled by the preceding lines, or {@code null}
	 * @return the built {@link Expression} object, or {@code null} if build failed.
	 * @throws SyntaxException if a semantic error occurs
	 * 
	 * @see #buildExpression(Token, int, TypeRegistry)
	 * @see #buildExpressionList(Token, int, List, TypeRegistry)
	 */
	private Expression buildExpression(Reduction _reduction, int _tokenPos, TypeRegistry _types) throws SyntaxException {
		Expression expr = null;
		//String rule = _reduction.getParent().toString();
		int ruleId = _reduction.getParent().getTableIndex();
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
			expr = new Expression(Expression.NodeType.LITERAL, getContent_R(_reduction, ""), (short)_tokenPos);
			break;
			
		case RuleConstants.PROD_NAME:
			// <Name> ::= <SimpleName>
		case RuleConstants.PROD_SIMPLENAME_IDENTIFIER:  // <SimpleName> ::= Identifier
			expr = new Expression(Expression.NodeType.IDENTIFIER, _reduction.get(0).asString(), (short)_tokenPos);
			break;

		case RuleConstants.PROD_QUALIFIEDNAME_DOT_IDENTIFIER:
			// <QualifiedName> ::= <Name> '.' Identifier
		case RuleConstants.PROD_FIELDACCESS_DOT_IDENTIFIER:
			// <FieldAccess> ::= <Primary> '.' Identifier
		{
			Expression expr0 = buildExpression(_reduction.get(0), _tokenPos, _types);
			_tokenPos += expr0.getTokenCount(true);
			expr = new Expression(Expression.NodeType.OPERATOR, ".", (short)_tokenPos);
			expr.children.add(expr0);
			expr.children.add(new Expression(Expression.NodeType.IDENTIFIER, _reduction.get(2).asString(), (short)(_tokenPos + 1)));
			break;
		}
		case RuleConstants.PROD_ARRAYINITIALIZER_LBRACE_RBRACE:  // <ArrayInitializer> ::= '{' <ExpressionList> '}'
		case RuleConstants.PROD_ARRAYINITIALIZER_LBRACE_RBRACE2:  // <ArrayInitializer> ::= '{' '}'
			expr = new Expression(Expression.NodeType.ARRAY_INITIALIZER, null, (short)_tokenPos);
			if (_reduction.size() > 2) {
				buildExpressionList(_reduction.get(1), _tokenPos + 1, expr.children, _types);
			}
			break;
		case RuleConstants.PROD_RECORDINITIALIZER_IDENTIFIER_LBRACE_RBRACE:
			// <RecordInitializer> ::= Identifier '{' <ComponentInitializers> '}'
		case RuleConstants.PROD_RECORDINITIALIZER_IDENTIFIER_LBRACE_RBRACE2:
			// <RecordInitializer> ::= Identifier '{' '}'
		{
			String typeName = _reduction.get(0).asString();
			expr = new Expression(Expression.NodeType.RECORD_INITIALIZER, typeName, (short)_tokenPos);
			if (_reduction.size() > 3) {
				Type recType = null;
				if (_types != null) {
					recType = _types.getType(typeName);
				}
				buildComponentList(_reduction.get(2), _tokenPos + 1, expr.children, recType);
			}
			break;
		}
		case RuleConstants.PROD_PRIMARY_LPAREN_RPAREN:
			// <Primary> ::= '(' <Expression> ')'
			expr = buildExpression(_reduction.get(1), _tokenPos + 1, _types);
			// FIXME: token counting gets corrupted
			break;
		case RuleConstants.PROD_ROUTINEINVOCATION_LPAREN_RPAREN:
			// <RoutineInvocation> ::= <Name> '(' <ExpressionList> ')'
		case RuleConstants.PROD_ROUTINEINVOCATION_LPAREN_RPAREN2:
			// <RoutineInvocation> ::= <Name> '(' ')'
			expr = buildExpression(_reduction.get(0), _tokenPos, _types);
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
				_tokenPos += 3 + expr1.children.getFirst().getTokenCount(true);
				expr = expr1;
			}
			else if (_reduction.get(0).getType() == SymbolType.NON_TERMINAL) {
				// FIXME seems to be illegal
				String name = getContent_R(_reduction.get(0).asReduction(), "");
				expr = new Expression(Expression.NodeType.FUNCTION, name, (short)_tokenPos);
				_tokenPos += 2;
			}
			else {
				// FIXME raise an error message at position _tokenPos
				System.err.println("Trouble in buildExpression() at token " + _tokenPos);
			}
			if (expr != null && _reduction.size() > 3) {
				buildExpressionList(_reduction.get(2), _tokenPos , expr.children, _types);;
			}
			break;
		case RuleConstants.PROD_METHODINVOCATION_DOT_IDENTIFIER_LPAREN_RPAREN:
			// <MethodInvocation> ::= <Primary> '.' Identifier '(' <ExpressionList> ')'
		case RuleConstants.PROD_METHODINVOCATION_DOT_IDENTIFIER_LPAREN_RPAREN2:
			// <MethodInvocation> ::= <Primary> '.' Identifier '(' ')'
			expr = new Expression(Expression.NodeType.METHOD, _reduction.get(2).asString(), (short)_tokenPos);
			expr.children.add(buildExpression(_reduction.get(0), _tokenPos, _types));
			expr.tokenPos += (1 + expr.children.getFirst().getTokenCount(true));
			if (_reduction.size() > 4) {
				buildExpressionList(_reduction.get(4), _tokenPos + 2, expr.children, _types);
			}
			break;
		case RuleConstants.PROD_ARRAYACCESS_LBRACKET_RBRACKET:
			// <ArrayAccess> ::= <Name> '[' <ExpressionList> ']'
		case RuleConstants.PROD_ARRAYACCESS_LBRACKET_RBRACKET2:
			// <ArrayAccess> ::= <Primary> '[' <ExpressionList> ']'
			expr = new Expression(Expression.NodeType.OPERATOR, "[]", (short)_tokenPos);
			expr.children.addLast(buildExpression(_reduction.get(0), _tokenPos, _types));
			expr.tokenPos += expr.children.getFirst().getTokenCount(true);
			// We check the type of the primary expression: Must not be e.g. a scalar literal
			if (expr.children.getLast().type == Expression.NodeType.LITERAL) {
				throw new SyntaxException("Not an array", expr.tokenPos);
			}
			buildExpressionList(_reduction.get(2), expr.tokenPos + 1, expr.children, _types);
			break;
		case RuleConstants.PROD_IDLIST_IDENTIFIER:
			// <IdList> ::= Identifier
			expr = buildExpression(_reduction.get(0), _tokenPos, _types);
			break;
		case RuleConstants.PROD_UNARYEXPRESSION_PLUS:
			// <UnaryExpression> ::= '+' <UnaryExpression>
		case RuleConstants.PROD_UNARYEXPRESSION_MINUS:
			// <UnaryExpression> ::= '-' <UnaryExpression>
			expr = new Expression(
					Expression.NodeType.OPERATOR,
					_reduction.get(0).asString() + "1",
					(short)_tokenPos);
			expr.children.addLast(buildExpression(_reduction.get(1), _tokenPos + 1, _types));
			break;
		case RuleConstants.PROD_UNARYEXPRESSIONNOTPLUSMINUS_TILDE:
			// <UnaryExpressionNotPlusMinus> ::= '~' <UnaryExpression>
		case RuleConstants.PROD_UNARYEXPRESSIONNOTPLUSMINUS_EXCLAM:
			// <UnaryExpressionNotPlusMinus> ::= '!' <UnaryExpression>
		case RuleConstants.PROD_UNARYEXPRESSIONNOTPLUSMINUS_NOTOPR:
			// <UnaryExpressionNotPlusMinus> ::= not <UnaryExpression>
			expr = new Expression(
					Expression.NodeType.OPERATOR,
					_reduction.get(0).asString(),
					(short)_tokenPos);
			expr.children.addLast(buildExpression(_reduction.get(1), _tokenPos + 1, _types));
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
					_reduction.get(1).asString(),
					(short)0);
			expr.children.addLast(buildExpression(_reduction.get(0), _tokenPos, _types));
			_tokenPos += expr.children.getFirst().getTokenCount(true);
			expr.tokenPos = (short)_tokenPos;
			expr.children.addLast(buildExpression(_reduction.get(2), _tokenPos + 1, _types));
			break;
		case RuleConstants.PROD_CONDITIONALEXPRESSION_QUESTION_COLON:
			// <ConditionalExpression> ::= <ConditionalOrExpression> '?' <Expression> ':' <ConditionalExpression>
			expr = new Expression(
					Expression.NodeType.TERNARY,
					"?,:",
					(short)0);
			expr.children.addLast(buildExpression(_reduction.get(0), _tokenPos, _types));
			_tokenPos += expr.children.getFirst().getTokenCount(true);
			expr.tokenPos = (short)_tokenPos;
			expr.children.addLast(buildExpression(_reduction.get(2), _tokenPos + 1, _types));
			_tokenPos += expr.children.getLast().getTokenCount(true) + 2;
			expr.children.addLast(buildExpression(_reduction.get(4), _tokenPos, _types));
			break;
		default:
			// This should not actually happen - it is a case on non-implementation...
			if (_reduction.size() > 0)
			{
				for (int i = 0; i < _reduction.size(); i++)
				{
					if (_reduction.get(i).getType() == SymbolType.NON_TERMINAL)
					{
						// No idea what to do here 
						buildExpression(_reduction.get(i), _tokenPos + i, _types);
					}
				}
			}
		}
		return expr;
	}

	/**
	 * Appends the expressions held by the expression list token (may be a single
	 * expression symbol/reduction or an expression list reduction) to the given
	 * {@code _expressions} list.
	 * 
	 * @param _token a {@link Token} expected to be some constituent of {@link RuleConstants#PROD_EXPRESSIONLIST_COMMA}.
	 * @param _tokenPos - position of the token within the line
	 * @param _expressions - a list of {@link Expression} object
	 * @return the next token position beyond the expression list.
	 * 
	 * @throws SyntaxException in case of illegal expressions
	 * 
	 * @see #buildExpression(Token, int, TypeRegistry)
	 */
	protected int buildExpressionList(Token _token, int _tokenPos, List<Expression> _expressions, TypeRegistry _types) throws SyntaxException {
		LinkedList<Expression> exprs = new LinkedList<Expression>();
		Reduction red = null;
		while (_token.getType() == SymbolType.NON_TERMINAL
				&& EXPR_LIST_RULE_IDS.contains((red = _token.asReduction()).getParent().getTableIndex())) {
				exprs.addFirst(buildExpression(red.get(2), 0, _types));
				_token = red.get(0);
		}
		exprs.addFirst(buildExpression(_token, 0, _types));
		for (Expression expr: exprs) {
			expr.tokenPos = (short)_tokenPos;
			_tokenPos += expr.getTokenCount(true) + 1;
		}
		_expressions.addAll(exprs);
		return _tokenPos;
	}

	/**
	 * Appends the component expressions held by the expression list token
	 * (may be a single expression symbol/reduction or an expression list
	 * reduction) to the given {@code _expressions} list.
	 * 
	 * @param _token a {@link Token} expected to be some constituent of
	 *  {@link RuleConstants#PROD_EXPRESSIONLIST_COMMA}.
	 * @param _tokenPos - position of the token within the line
	 * @param _expressions - a list of {@link Expression} object
	 * 
	 * @throws SyntaxException in case of illegal expressions
	 */
	public void buildComponentList(Token _token, int _tokenPos, LinkedList<Expression> _expressions, Type _recType) throws SyntaxException {
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
						_tokenPos = compExpr.tokenPos + compExpr.getTokenCount(true) + 1;
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
				_tokenPos += compExpr.getTokenCount(true) + 1;
				for (Expression expr: exprs) {
					if (_tokenPos > expr.tokenPos) {
						expr.tokenPos = (short)_tokenPos;
					}
					_tokenPos += expr.getTokenCount(true) + 2;	// colon and comma
				}
				_expressions.addAll(exprs);
				break;
			}
			} // end switch
		}
	}

	//---------------------- Build method for Types ---------------------------
	
	/**
	 * Retrieves or derives a {@link Type} object from the {@link Token} {@code _token},
	 * which is to represent a parsed type description, i.e. one of<ul>
	 * <li>{@link RuleConstants#PROD_TYPEDESCRIPTION_IDENTIFIER}</li>
	 * <li>{@link RuleConstants#PROD_TYPEDESCRIPTION_UNSIGNED_IDENTIFIER}</li>
	 * <li>{@link RuleConstants#PROD_TYPEDESCRIPTION}</li>
	 * </ul>
	 * 
	 * @param _token - the root token of a type description
	 * @param _types - a type registry to check for referred types
	 * @param _tokenCounter - an int array containing the token counter (will be updated)
	 * @param _element - the NSD element the parsed line belongs to
	 * @return the derived data type
	 * 
	 * @throws SyntaxException if the type name or construction is illegal
	 */
	private Type buildType(Token _token, TypeRegistry _types, short[] _tokenCounter, Element _element) throws SyntaxException {
		// <TypeDescription> ::= Identifier <DimsOpt>
		// <TypeDescription> ::= unsigned Identifier <DimsOpt>
		// <TypeDescription> ::= <ArrayOf> <TypeDescription>
		Type type = null;
		if (_token.getType() == SymbolType.NON_TERMINAL) {
			Reduction red = _token.asReduction();
			int ixId = 0;
			switch (red.getParent().getTableIndex()) {
			case RuleConstants.PROD_TYPEDESCRIPTION_UNSIGNED_IDENTIFIER:
				// <TypeDescription> ::= unsigned Identifier <DimsOpt>
				_tokenCounter[0]++;	// skip 'unsigned'
				ixId = 1;
			case RuleConstants.PROD_TYPEDESCRIPTION_IDENTIFIER:
				// <TypeDescription> ::= Identifier <DimsOpt>
			{
				String typeId = getContent_R(red.get(ixId), "");
				if (typeId != null && !typeId.isEmpty() && _types != null) {
					type = _types.getType(typeId);
					if (type != null && ixId > 0) {
						// Check if there is an equivalent unsigned type
						Type uType = _types.getType("u" + typeId);
						if (uType != null) {
							type = uType;
						}
					}
				}
				if (type == null) {
					type = new Type(typeId);
				}
				_tokenCounter[0]++;	// skip Identifier
				type = applyArrayDimensions(red.get(ixId+1).asReduction(), type,
						_types, _tokenCounter, _element);
				break;
			}
			case RuleConstants.PROD_TYPEDESCRIPTION:
				// <TypeDescription> ::= <ArrayOf> <TypeDescription>
			{
				Reduction arrRed = red.get(0).asReduction();
				Root root = Element.getRoot(_element);
				HashMap<String, String> constMap = null;
				if (root != null) {
					constMap = root.constants;
				}
				_tokenCounter[0]++;	// skip ArrayKey
				ArrayList<Object> dims = new ArrayList<Object>();
				if (arrRed.size() > 2) {
					// <ArrayOf> ::= ArrayKey '[' <RangeList> ']' OfKey
					Token tokRl = arrRed.get(2);	// <RangeList>
					_tokenCounter[0]++;	// skip '['
					do {
						Token tokRg = tokRl;
						if (tokRl.getType() == SymbolType.NON_TERMINAL
								&& tokRl.asReduction().getParent().getTableIndex() == RuleConstants.PROD_RANGELIST_COMMA) {
							tokRg = tokRl.asReduction().get(2);
							tokRl = tokRl.asReduction().get(0);
						}
						else {
							tokRl = null;
						}
						Object dim = 0;
						if (tokRg.getType() == SymbolType.NON_TERMINAL
								&& tokRg.asReduction().getParent().getTableIndex() == RuleConstants.PROD_RANGE_DOTDOT) {
							Reduction rangeRed = tokRg.asReduction();
							Expression exprLow = buildExpression(rangeRed.get(0), _tokenCounter[0], _types);
							Object ixObjLo = exprLow.evaluateConstantExpr(constMap, null);
							_tokenCounter[0] += exprLow.getTokenCount(true) + 1;
							Expression exprHigh = buildExpression(rangeRed.get(2), _tokenCounter[0], _types);
							_tokenCounter[0] += exprHigh.getTokenCount(true);
							Object ixObjHi = exprHigh.evaluateConstantExpr(constMap, null);
							if (ixObjLo instanceof Integer && ixObjHi instanceof Integer) {
								dim = new int[] {(int)ixObjLo, (int)ixObjHi};
							}
							else {
								dim = new Expression[] {exprLow, exprHigh};
							}
						}
						else {
							Expression exprRg = buildExpression(tokRg, _tokenCounter[0], _types);
							_tokenCounter[0] += exprRg.getTokenCount(true);
							Object rgObj = exprRg.evaluateConstantExpr(constMap, null);
							if (rgObj instanceof Integer) {
								dim = (int)rgObj;
							}
							else {
								dim = new Expression[] {exprRg};
							}
						}
						dims.add(dim);
					} while (arrRed != null);
					_tokenCounter[0]++;	// skip ']'
				}
				else {
					dims.add(0);
				}
				_tokenCounter[0]++;	// skip OfKey
				type = buildType(red.get(1), _types, _tokenCounter, _element);
				for (Object dim: dims) {
					if (dim instanceof int[]) {
						type = new ArrayType(null, type, (int[])dim);
					}
					else if (dim instanceof Integer) {
						type = new ArrayType(null, type, (int)dim);
					}
					else if (dim instanceof Expression[]) {
						type = new ArrayType(null, type, (Expression[])dim);
					}
				}
				break;
			}
			}
		}
		return type;
	}
	
	/**
	 * Constructs a new {@link Type} object with name {@code _typeId} from the
	 * {@link Token} {@code _token}, which is to represent a parsed type
	 * construction. It is expected to be a reduction of one of the following
	 * types:<ul>
	 * <li>{@link RuleConstants#PROD_TYPECONSTRUCTION_RECORDKEY_LBRACE_RBRACE}</li>
	 * <li>{@link RuleConstants#PROD_TYPECONSTRUCTION_ENUM_LBRACE_RBRACE}</li>
	 * <li>{@link RuleConstants#PROD_TYPEDESCRIPTION_IDENTIFIER}</li>
	 * <li>{@link RuleConstants#PROD_TYPEDESCRIPTION_UNSIGNED_IDENTIFIER}</li>
	 * <li>{@link RuleConstants#PROD_TYPEDESCRIPTION}</li>
	 * </ul>
	 * 
	 * @param _token - the root token of a type construction (definition)
	 * @param _typeId - the name of the type to be constructed (defined)
	 * @param _types - a type registry to check for referred types
	 * @param _tokenCounter - an int array containing the token counter (will be updated)
	 * @param _element - the NSD element the parsed line belongs to
	 * @return the constructed data type
	 * 
	 * @throws SyntaxException if the type construction is illegal or self-recursive
	 */
	private Type buildType(Token _token, String _typeId, TypeRegistry _types, short[] _tokenCounter, Element _element) throws SyntaxException {
		// <TypeConstruction> ::= RecordKey '{' <ComponentList> '}'
		// <TypeConstruction> ::= enum '{' <EnumList> '}'
		// <TypeDescription> ::= Identifier <DimsOpt>
		// <TypeDescription> ::= unsigned Identifier <DimsOpt>
		// <TypeDescription> ::= <ArrayOf> <TypeDescription>
		Type type = null;
		Reduction red = _token.asReduction();
		int arrIx = -1;
		switch (red.getParent().getTableIndex()) {
		case RuleConstants.PROD_TYPECONSTRUCTION_RECORDKEY_LBRACE_RBRACE:
			// <TypeConstruction> ::= RecordKey '{' <ComponentList> '}'
			red = red.get(2).asReduction();	// <ComponentList>
			// TODO
			break;
		case RuleConstants.PROD_TYPECONSTRUCTION_ENUM_LBRACE_RBRACE:
			// <TypeConstruction> ::= enum '{' <EnumList> '}'
			red = red.get(2).asReduction();	// <EnumList>
			break;
		case RuleConstants.PROD_TYPEDESCRIPTION_IDENTIFIER:
			// <TypeDescription> ::= Identifier <DimsOpt>
			arrIx = 1;
		case RuleConstants.PROD_TYPEDESCRIPTION_UNSIGNED_IDENTIFIER:
			// <TypeDescription> ::= unsigned Identifier <DimsOpt>
			arrIx = 2;
		default:
			// <TypeDescription> ::= Identifier <DimsOpt>
			// <TypeDescription> ::= unsigned Identifier <DimsOpt>
			// <TypeDescription> ::= <ArrayOf> <TypeDescription>
			type = buildType(_token, _types, _tokenCounter, _element);
			// 
			if (type != null && arrIx >= 0 && red.get(arrIx).asReduction().size() == 0) {
				// Mere type synonym (redirection)
				type = new RedirType(_typeId, type);
			}
		}
		
		return type;
	}

	/**
	 * 
	 * @param _reduction - a {@link Reduction} supposed to represent one of the
	 * rules<ul>
	 * <li>{@link RuleConstants#PROD_DIMENSIONLIST}</li>
	 * <li>{@link RuleConstants#PROD_DIMENSIONLIST2}</li>
	 * <li>{@link RuleConstants#PROD_DIMSOPT_LBRACKETRBRACKET}</li>
	 * </ul>
	 * 
	 * @param _type - an element type object
	 * @param _types - a type registry (or possibly {@code null})
	 * @param _tokenCounter - an array containing the current token count (will be updated)
	 * @param _element TODO
	 * @return the array type formed over {@code _type} or {@code _type itself}
	 * 
	 * @throws SyntaxException in case of illegal expressions or types
	 */
	private Type applyArrayDimensions(Reduction _reduction, Type _type, TypeRegistry _types, short[] _tokenCounter, Element _element) throws SyntaxException {
		while (_reduction.size() > 0) {
			int elCnt = 0;
			Expression dim = null;
			if (_reduction.size() > 2) {
				Token dimTok = _reduction.get(1).asReduction().get(1);
				dim = buildExpression(dimTok, _tokenCounter[0] + 1, _types);
				// We try to evaluate the constant expression
				HashMap<String, String> constants = null;
				if (_element != null) {
					Root root = Element.getRoot(_element);
					if (root != null) {
						constants = root.constants;
					}
				}
				elCnt = -1;
				Object size = dim.evaluateConstantExpr(constants, null);
				if (size instanceof Integer) {
					elCnt = (int)size;
				}
			}
			try {
				if (elCnt >= 0) {
					_type = new ArrayType(null, _type, elCnt);
				}
				else {
					_type = new ArrayType(null, _type, new Expression[] {dim});
				}
			} catch (SyntaxException exc) {}
			_reduction = _reduction.get(0).asReduction();
		}
		return _type;
	}
	
//	private Type extractDeclaration(Token _token, TypeRegistry _types, short[] _tokenCounter)
//	{
//		Type dataType = buildType(red.get(0), _types, tokenCounter);
//		Reduction arrayDeclRed = red.get(1).asReduction();
//		// Identifier
//		tokenPos = (short)tokenCounter[0];
//		String id = getContent_R(arrayDeclRed.get(0), "");
//		// <DimensionList>
//		tokenCounter[0]++;
//		dataType = applyArrayDimensions(arrayDeclRed.get(1), dataType, _types, tokenCounter);
//		
//	}

	//---------------------- General helper methods ---------------------------

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
		for(int i = 0; i < _reduction.size(); i++)
		{
			_content = getContent_R(_reduction.get(i), _content);
		}
		
		return _content;
	}

	private String getContent_R(Token _token, String _content)
	{
		switch (_token.getType()) 
		{
		case NON_TERMINAL:
			_content = getContent_R(_token.asReduction(), _content);	
			break;
		case CONTENT:
		{
			int idx = _token.getTableIndex();
			switch (idx) {
			// FIXME
			case SymbolConstants.SYM_EXCLAM:
				_content += " not ";
				break;
			default:
				_content += _token.asString();
			}
		}
		break;
		default:
			break;
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
				"§COND§ (value < 15.5) and not (length(arrow)*2 >= 5) or a <> b",
				"§COND§ (value < 15.5) and ! (length(arrow)*2 >= 5) or a <> b",
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
				"var x, y, z: double",
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
				"§FOR§ i <- a + 7 §TO§ sqrt(79.3) §STEP§ 2",
				"§FOREACH§ element §IN§ {35, 23+4, 108, -24}",
				"§FOREACH§ element §IN§ 35, 23+4, 108, -24",
				"§CASE§ length(numbers)",
				"§SELECT§ 3, 5, 9",
				// Defective lines - are to provoke SyntaxExceptions
				"nonsens <- 56[89]",
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
			String error = null;
			long startTime = System.currentTimeMillis();
			Line line = parser.parse(text, null, 0, null);
			long endTime = System.currentTimeMillis();
			if (line != null && (error = line.getParserError()) != null) {
				System.err.println(error);
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
