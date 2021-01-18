/*
    Structorizer
    A little tool which you can use to create Nassi-Schneiderman Diagrams (NSD)

    Copyright (C) 2009  Bob Fisch

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

import java.util.Arrays;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Node class for a syntax tree of expressions (including statements).
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2017-10-24      First Issue (for KGU#455 and other enhancements/problems)
 *      Kay Gürtzig     2019-11-11      Method parse() implemented, new method inferType, some node types dropped
 *      Kay Gürtzig     2019-12-19      Signature change of TypeMapEntry constructor integrated
 *      Kay Gürtzig     2020-10-26      Opportunity of null delimiter in parseList(...) added.
 *      Kay Gürtzig     2020-11-01      Reliable variable gathering and type retrieval mechanism implemented
 *      Kay Gürtzig     2020-11-03      toString() delegated to (the less performant but more more versatile)
 *                                      appendToTokenList() method family,
 *                                      embedded Operator class extended with a translation option to function/method
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      2019-11-11 (Kay Gürtzig)
 *      - Class represents expressions as syntax trees and provides conversions to string or token list
 *        and from token list. Type inference approaches are planned.
 *
 ******************************************************************************************************///

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import lu.fisch.structorizer.executor.Function;
import lu.fisch.utils.StringList;

/**
 * Forms the node of a syntax tree. Each node comprises three components:<br/>
 * 1. a classifying enumerator<br/>
 * 2. a string<br/>
 * 3. a list of nodes, recursively representing the operands<br/>
 * Depending on the (enumerable) type, the string and sub-nodes would be:<br/>
 * <table>
 * <tr><th>Type</th><th>String</th><th>Children</th></tr>
 * <tr><td>LITERAL</td><td>the literal</td><td>(empty)</td></tr>
 * <tr><td>VARIABLE</td><td>variable id</td><td>(empty)</td></tr>
 * <tr><td>OPERATOR</td><td>operator symbol</td><td>operands</td></tr>
 * <tr><td>INDEX</td><td>""</td><td>array, index expressions</td></tr>
 * <tr><td>FUNCTION</td><td>function id</td><td>arguments</td></tr>
 * <tr><td>QUALIFIER</td><td>comp id</td><td>record</td></tr>
 * <tr><td>ARRAY_INITIALIZATION</td><td>type id (if any)</td><td>elements</td></tr>
 * <tr><td>RECORD_INITIALIZATION</td><td>type id</td><td>components</td></tr>
 * <tr><td>COMPONENT</td><td>[comp id]</td><td>comp value</td></tr>
 * <tr><td>DECLARATION</td><td>-</td><td>subdeclarations</td></tr>
 * <tr><td>ROUTINE</td><td>routine id</td><td>argument declaration[s]</td></tr>
 * </table>
 * Types {@link NodeType#DECLARATION} and {@link NodeType#ROUTINE} are reserved
 * for subclasses.
 * @author Kay Gürtzig
 */
public class Expression {
	
	/**
	 * Structorizer operator precedence map (the higher the value the higher the precedence).<br/>
	 * Parentheses {@code ()} are not members because they will be eliminated on constructing the
	 * syntax tree (redundant).
	 * @see #toString()
	 * @see #toString(int)
	 * @see #appendToTokenList(StringList, HashMap)
	 * @see #appendToTokenList(StringList, HashMap, Byte)
	 */
	@SuppressWarnings("serial")
	public static final HashMap<String, Byte> OPERATOR_PRECEDENCE = new HashMap<String, Byte>() {{
		put("<-", (byte) 0);
		put(":=", (byte) 0);
		put("or", (byte) 1);
		put("||", (byte) 1);
		put("and", (byte) 2);
		put("&&", (byte) 2);
		put("|", (byte) 3);
		put("^", (byte) 4);
		put("xor", (byte) 4);
		put("&", (byte) 5);
		put("=", (byte) 6);
		put("==", (byte) 6);
		put("<>", (byte) 6);
		put("!=", (byte) 6);
		put("<", (byte) 7);
		put(">", (byte) 7);
		put("<=", (byte) 7);
		put(">=", (byte) 7);
		put("shl", (byte) 8);
		put("<<", (byte) 8);
		put("shr", (byte) 8);
		put(">>", (byte) 8);
		put(">>>", (byte) 8);
		put("+", (byte) 9);
		put("-", (byte) 9);
		put("*", (byte) 10);
		put("/", (byte) 10);
		put("div", (byte) 10);
		put("mod", (byte) 10);
		put("%", (byte) 10);
		put("not", (byte) 11);
		put("!", (byte) 11);
		put("~", (byte) 11);
		put("+1", (byte) 11);	// sign
		put("-1", (byte) 11);	// sign
		put("*1", (byte) 11);	// pointer deref (C)
		put("&1", (byte) 11);	// address (C)
		put("[]", (byte) 12);
		put(".", (byte) 12);
	}};
	
	/** Logical literals ("false" and "true") */
	public static final StringList BOOL_LITERALS = new StringList(new String[] {"false", "true"});
	/** Logical unary operator symbols ("~" is in here, too, though it is actually a bitwise operator) */
	public static final StringList NEGATION_OPERATORS = new StringList(new String[] {"not", "!", "~"});
	/** Logical binary operator symbols ("xor" is in here, too, though it is actually a bitwise operator) */
	public static final StringList BOOL_OPERATORS = StringList.explode("&&,and,||,or,xor", ",");
	/** Comparison operator symbols in complementary order, i.e. first is complementary to last etc. */
	public static final StringList RELATION_OPERATORS = StringList.explode("==,=,<,>,<=,>=,<>,!=", ",");
	
	/**
	 * Export operator specification for expression mapping. Operator names from the
	 * key set of map {@link Expression#OPERATOR_PRECEDENCE} may be mapped to objects
	 * of this class in order to specify a translation for {@link Expression} trees.
	 * @author Kay Gürtzig
	 */
	public static final class Operator {
		/** The operator symbol - may be an identifier or a sequence of symbol characters */
		public final String symbol;
		/** Operator priority: the higher the value the higher the precedence */
		public final byte precedence;
		/**
		 * Specifies function or method syntax if not {@code null}. An empty array
		 * induces a function with consecutive operand placement as arguments,
		 * otherwise the array lists the operand numbers (index + 1) to place as
		 * method executing object (position 0), 1st argument, 2nd argument etc.
		 * If the array element 0 is 0 then a function will be created, otherwise
		 * a method call, e.g.
		 * <ul>
		 * <li>{{@code }} function with "natural" argument order: {@code <symbol>(<opd1>, <opd2> ,...)}</li>
		 * <li>{{@code 0, 2, 1}} would swap operands: {@code <symbol>(<opd2>, <opd1>)}</li>
		 * <li>{{@code 1, 2}} would produce method syntax: {@code <opd1>.<symbol>(<opd2>)}</li>
		 * </ul>
		 */
		public final byte[] argumentOrder;
		
		/**
		 * Specifies an (infix) operator with given {@code symbol} and {@code precedence}.
		 * @param symbol - the operator symbol (a leading '1' will make it a
		 * postfix operator, if unary).
		 * @param precedence - the priority (the higher the more precedent)
		 * @see #Operator(String)
		 * @see #Operator(String, int[])
		 */
		public Operator(String symbol, int precedence) {
			this.symbol = symbol;
			this.precedence = (byte)precedence;
			this.argumentOrder = null;
		}
		
		/**
		 * Specifies a function with given {@code name} that is to replace an
		 * operator, with the operands as arguments in the same order. It will
		 * automatically get highest priority.
		 * @param name - the function name
		 * @see #Operator(String, int)
		 * @see #Operator(String, int[])
		 */
		public Operator(String name) {
			this.symbol = name;
			this.precedence = Byte.MAX_VALUE;
			this.argumentOrder = new byte[]{};
		}
		
		/**
		 * Specifies a function or method with given {@code name} that is to replace
		 * an operator, with the operands as arguments in the order specified by
		 * {@code argOrder}. It will automatically get highest priority.
		 * @param name - the function name
		 * @param argOrder - array of operand position numbers, see #argumentOrder
		 * @see #Operator(String)
		 * @see #Operator(String, int)
		 */
		public Operator(String name, int[] argOrder) {
			this.symbol = name;
			this.precedence = Byte.MAX_VALUE;
			this.argumentOrder = new byte[argOrder.length];
			for (int i = 0; i < argOrder.length; i++) {
				this.argumentOrder[i] = (byte)argOrder[i];
			}
		}
		
		@Override
		public String toString()
		{
			StringBuilder sb = new StringBuilder();
			sb.append(this.getClass().getSimpleName());
			sb.append("(");
			sb.append(this.symbol);
			sb.append(",");
			sb.append(this.precedence);
			if (this.argumentOrder != null) {
				sb.append(",");
				sb.append(Arrays.toString(this.argumentOrder));
			}
			sb.append(")");
			return sb.toString();
		}
	};
	
	/**
	 * Maps operator symbols from the key set of {@link #OPERATOR_PRECEDENCE} to
	 * pairs of an alternative (more verbose equivalent) symbol and precedence,
	 * e.g. to be used with {@link #translate(HashMap)}
	 */
	@SuppressWarnings("serial")
	public static final HashMap<String, Operator> verboseOperators = new HashMap<String, Operator>() {{
		put("<-", new Operator(":=", 0));
		//put("or", new Operator("or", 1));	// Does not change anything
		put("||", new Operator("or", 1));
		//put("and", new Operator("and", 2));	// Does not change anything
		put("&&", new Operator("and", 2));
		//put("|", new Operator("|", 3));
		put("^", new Operator("xor", 4));
		//put("xor", new Operator("xor", 4));	// Does not change anything
		//put("&", new Operator("&", 5));	// Does not change anything
		//put("=", new Operator("=", 6));	// Does not change anything
		put("==", new Operator("=", 6));
		//put("<>", new Operator("<>", 6));	// Does not change anything
		put("!=", new Operator("<>", 6));
		put("<", new Operator("<", 7));
		put(">", new Operator(">", 7));
		put("<=", new Operator("<=", 7));
		put(">=", new Operator(">=", 7));
		//put("shl", new Operator("shl", 8));	// Does not change anything
		//put("<<", new Operator("<<", 8));	// Does not change anything
		//put("shr", new Operator("shr", 8));	// Does not change anything
		//put(">>", new Operator(">>", 8));	// Does not change anything
		//put(">>>", new Operator(">>>", 8));	// Does not change anything
		//put("+", new Operator("+", 9));	// Does not change anything
		//put("-", new Operator("-", 9));	// Does not change anything
		//put("*", new Operator("*", 10));	// Does not change anything
		//put("/", new Operator("/", 10));	// Does not change anything
		//put("div", new Operator("div", 10));	// Does not change anything
		//put("mod", new Operator("mod", 10));	// Does not change anything
		put("%", new Operator("mod", 10));
		//put("not", new Operator("not", 11));	// Does not change anything
		put("!", new Operator("not", 11));
		//put("~", new Operator("~", 11));	// Does not change anything
		put("+1", new Operator("+1", 11));	// sign
		put("-1", new Operator("-1", 11));	// sign
		put("*1", new Operator("1^", 11));	// pointer deref (C)
		put("&1", new Operator("@1", 11));	// address (C)
		//put("[]", new Operator("[]", 12));	// Does not change anything
		//put(".", new Operator(".", 12));	// Does not change anything
	}};
	
	/** COMPONENT means the colon separating a component name and a component value in a record initializer,<br/>
	 * QUALIFIER is the dot separating a record and a component name (whereas the dot in a method call is handled as OPERATOR) <br/>
	 * PARENTH symbolizes an opening parenthesis and is only temporarily used within the shunting yard algorithm in {@link Expression#parse(StringList, StringList, short)} */
	public static enum NodeType {
		/** leaf node, a literal of an unambiguous data type (safe) */
		LITERAL,
		/** leaf node, an identifier (variable, constant, type), data type may be declared or inferred */
		IDENTIFIER,
		/** an operator (its symbol), parent of operand expressions, data type usually inferred */
		OPERATOR,
		/** function call (its identifier), parent of argument expressions, data type declared */
		FUNCTION,
		/** an array initializer, parent of element expressions, data type determined by elements */
		ARRAY_INITIALIZER,
		/** a record initializer (record type name), parent of {@link #COMPONENT}s */
		RECORD_INITIALIZER,
		/** a component inside a {@link #RECORD_INITIALIZER}: [component id +] value expression */
		COMPONENT,
		/** temporary type (during parsing): parentheses, brackets or braces, parent of expressions */
		PARENTH,
		/** a variable initialisation or a list of variables, data type is declared (safe) */
		DECLARATION,
		/** a routine header (its name), parent of parameter {@link #DECLARATION}s, data type is declared for result */
		ROUTINE};
	public NodeType type;
	public String text;
	public final LinkedList<Expression> children = new LinkedList<Expression>();
	public short tokenPos = 0;
	/**
	 * May hold a retrieved expression data type
	 * Type retrieval can be forced by {@link #inferType(HashMap, boolean)}
	 */
	public Type dataType = null;
	/** Signals whether {@link #dataType} is final (e.g. with literals or function results) */
	public boolean isDataTypeSafe = false;
	
	public Expression(NodeType _type, String _text, short _position)
	{
		type = _type;
		text = _text;
		tokenPos = _position;
	}
	
	public Expression(NodeType _type, String _text, short _position, LinkedList<Expression> _children)
	{
		type = _type;
		text = _text;
		tokenPos = _position;
		this.children.addAll(_children);
	}
	
	/**
	 * Derives the tree from the given tokens.
	 * Use {@link #parse(StringList, StringList, short)} instead.
	 * @param _tokens
	 */
	@Deprecated
	public Expression(StringList _tokens)
	{
		// FIXME: Derive the tree from _tokens
		type = NodeType.LITERAL;
		text = "";
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return toString(-1, null);
	}
	/**
	 * Returns this expression as linearized string in Structorizer-compatible
	 * syntax, assuming that this expression is embedded as operand of an operator
	 * with precedence level {@code parentPrec}.
	 * @param parentPrec - the assumed parent operator precedence
	 * @param _alternOprs - alternative operator specifications (differing symbol
	 * or precedence).
	 * @return the composed expression string
	 * @see #OPERATOR_PRECEDENCE
	 * @see #appendToTokenList(StringList)
	 * @see #appendToTokenList(StringList, HashMap)
	 * @see #appendToTokenList(StringList, byte, HashMap)
	 */
	private String toString(int parentPrec, HashMap<String, Operator> _alternOprs)
	{
		// Now delegated to appendToTokenList, which is less efficient but more versatile
//		StringBuilder sb = new StringBuilder();
//		String sepa = "";	// separator
//		Iterator<Expression> iter = null;
//		switch (type) {
//		case LITERAL:
//		case VARIABLE:
//			sb.append(text);
//			break;
//		case OPERATOR: {
//			// May be unary or binary
//			Operator opr = null;
//			if (_alternOprs != null) {
//				opr = _alternOprs.get(text);
//			}
//			int myPrec = OPERATOR_PRECEDENCE.get(text);
//			String symbol = text;
//			if (opr != null) {
//				myPrec = opr.precedence;
//				symbol = opr.symbol;
//			}
//			if (children.size() <= 1 && !symbol.startsWith("1")) {
//				// Unary prefix operator
//				if (symbol.endsWith("1")) {
//					sb.append(symbol.substring(0, symbol.length()-1));
//				}
//				else if (children.isEmpty() || !text.equals("[]")) {
//					sb.append(symbol);
//				}
//				// Insert a gap if the operator is an identifier (word)
//				if (Syntax.isIdentifier(symbol, false, null)) {
//					sb.append(" ");
//				}
//			}
//			// Without pointers, there is no need to put parentheses if parent is . or []
//			else if (myPrec < parentPrec && !(myPrec < 11 && parentPrec == 12)) {
//				sepa = "(";
//			}
//			iter = children.iterator();
//			while (iter.hasNext()) {
//				sb.append(sepa + iter.next().toString(myPrec, _alternOprs));
//				if (text.equals("[]")) {
//					sepa = sepa.isEmpty() ? symbol.substring(0, 1) : ", ";	// usually '['
//				}
//				else if (!text.equals(".")) {
//					sepa = " " + symbol + " ";
//				}
//				else {
//					sepa = symbol;
//				}
//			}
//			if (text.equals("[]") && !children.isEmpty()) {
//				sb.append(symbol.substring(1));	// usually ']'
//			}
//			if ((children.size() > 1 || symbol.startsWith("1"))
//					&& myPrec < parentPrec && !(myPrec < 11 && parentPrec == 12)) {
//				sb.append(')');
//			}
//			if ((children.size() <= 1) && symbol.startsWith("1")) {
//				sb.append(symbol.substring(1));
//			}
//			break;
//		}
////		case INDEX:	// obsolete
////			// First child expresses the array
////			sb.append(children.getFirst().toString(OPERATOR_PRECEDENCE.get("[]")) + "[");
////			iter = children.listIterator(1);
////			while (iter.hasNext()) {
////				sb.append(sepa + iter.next().toString());
////				sepa = ", ";
////			}
////			sb.append(']');
////			break;
//		case RECORD_INITIALIZER:
//			sb.append(text);
//		case ARRAY_INITIALIZER:
//			sb.append('{');
//			iter = children.iterator();
//			while (iter.hasNext()) {
//				sb.append(sepa + iter.next().toString());
//				sepa = ", ";
//			}
//			sb.append('}');
//			break;
//		case COMPONENT:
//			sb.append(text + ": ");
//			if (!children.isEmpty()) {
//				sb.append(children.getFirst().toString());
//			}
//			break;
//		case FUNCTION:
//			sb.append(text + "(");
//			iter = children.iterator();
//			while (iter.hasNext()) {
//				sb.append(sepa + iter.next().toString());
//				sepa = ", ";
//			}
//			sb.append(')');
//			break;
////		case QUALIFIER:	// obsolete
////			sb.append(children.getFirst().toString(OPERATOR_PRECEDENCE.get(".")));
////			sb.append("." + text);
////			break;
//		case PARENTH:
//			sb.append(text);
//			sb.append(children.size());	// This element counts aggregated expressions by adding null entries
//			break;
//		default:
//			break;		
//		}
//		return sb.toString();
		StringList tokens = new StringList();
		appendToTokenList(tokens, (byte)parentPrec, _alternOprs);
		return tokens.concatenate(null);
	}
	
	/**
	 * Given an operator symbol mapping to alternative operator symbols and preferences,
	 * returns a translated linear expression string
	 * @param _operatorSpecs - maps operator symbols from the key set of {@link #OPERATOR_PRECEDENCE}
	 * to pairs of target operator symbol, and preference.
	 * @return the linearized expression
	 * @see #toString()
	 * @see #OPERATOR_PRECEDENCE
	 * @see #verboseOperators
	 */
	public String translate(HashMap<String, Operator> _operatorSpecs)
	{
		return this.toString(-1, _operatorSpecs);
	}
	
	/**
	 * Append this expression tree in tokenized form to the given token list {@code tokens}.
	 * Uses standard operator precedence table {@link #OPERATOR_PRECEDENCE}.
	 * @param tokens - a non-null {@link #toString(int)} to append my tokens to.
	 * @see #appendToTokenList(StringList, HashMap)
	 */
	public void appendToTokenList(StringList tokens)
	{
		appendToTokenList(tokens, (byte)-1, null);
	}
	/**
	 * Append this expression tree in tokenized form (with beautifying blanks around operator
	 * symbols) to the given token list {@code tokens}.<br/>
	 * If given, uses the alternative operator mapping table @{@code precMap} instead of
	 * {@link #OPERATOR_PRECEDENCE} to use differing symbols and possibly differing precedence
	 * ranks, which has an impact where parentheses must be placed. This way, an
	 * equivalent translation to target languages may be addressed. A given empty map will
	 * force parentheses around any non-atomic sub-expression.
	 * @param tokens - a non-null {@link StringList} to append my tokens to.
	 * @param alternOprs - a customized operator precedence map (if empty, then composed
	 * operand expressions will be parenthesized).
	 * @see #appendToTokenList(StringList)
	 * @see #appendToTokenList(StringList, byte, HashMap)
	 */
	public void appendToTokenList(StringList tokens, HashMap<String, Operator> precMap)
	{
		appendToTokenList(tokens, (byte)-1, precMap);
	}
	/**
	 * Append this expression tree in tokenized form (with beautifying blanks around operator
	 * symbols) to the given token list {@code tokens}.<br/>
	 * If given, uses the alternative operator mapping table @{@code alternOprs} instead of
	 * {@link #OPERATOR_PRECEDENCE} to use differing symbols and possibly differing precedence
	 * ranks, which has an impact where parentheses must be placed. This way, an
	 * equivalent translation to target languages may be addressed. A given empty map will
	 * force parentheses around any non-atomic sub-expression.
	 * @param tokens - a non-null {@link StringList} to append my tokens to.
	 * @param parentPrec - the operator precedence of the operator this expression forms an
	 * operand for. -1 means there is no parent operator.
	 * @param alternOprs - a customized operator precedence map (if empty, then composed
	 * operand expressions will be parenthesized).
	 * @see #appendToTokenList(StringList)
	 */
	private void appendToTokenList(StringList tokens, byte parentPrec, HashMap<String, Operator> alternOprs)
	{
		String[] sepa = new String[]{};
		switch (type) {
		case LITERAL:
		case IDENTIFIER:
			tokens.add(text);
			break;
		case OPERATOR: {
			// May be unary or binary
			Operator opr = null;
			boolean asFunc = false;	// express the operator as function call?
			int methObjIx = -1;		// if >= 0, express the operator as method call
			boolean noPrec = false;	// put all expressions in parentheses?
			if (alternOprs != null) {
				opr = alternOprs.get(text);
				noPrec =  alternOprs.isEmpty();
			}
			Byte myPrec = OPERATOR_PRECEDENCE.get(text);
			String symbol = text;
			if (opr != null) {
				myPrec = opr.precedence;
				symbol = opr.symbol;
				asFunc = opr.argumentOrder != null;
				if (asFunc && opr.argumentOrder.length > 0) {
					methObjIx = opr.argumentOrder[0] - 1;
				}
			}
			if (asFunc || children.size() <= 1 && !symbol.startsWith("1")) {
				// Explicit unary prefix operator
				if (!asFunc && symbol.endsWith("1")) {
					tokens.add(symbol.substring(0, symbol.length()-1));
				}
				// Other unary prefix operator or empty array brackets (during parsing)
				else if (children.isEmpty() || !text.equals("[]")){
					if (methObjIx >= 0 && methObjIx < children.size()) {
						children.get(methObjIx).appendToTokenList(tokens,
								OPERATOR_PRECEDENCE.get("."), alternOprs);
						// TODO Check whether alternOprs contains a different symbol!
						tokens.add(".");
					}
					tokens.add(symbol);
				}
				//if (text.equals("+") || text.equals("-")) {
				//	// Seems to be a sign operator - same precedence level as negation
				//	myPrec = OPERATOR_PRECEDENCE.get("!");
				//}
				// Insert an opening parenthesis in function style
				if (asFunc) {
					sepa = new String[]{"("};
				}
				// Insert a gap if the operator is an identifier (word)
				else if (Syntax.isIdentifier(symbol, false, null)) {
					tokens.add(" ");
				}
			}
			// Without pointers, there is no need to put parentheses if parent is . or []
			else if (noPrec
				|| myPrec < parentPrec && !(myPrec < 11 && parentPrec == 12)) {
				sepa = new String[]{"("};
			}
			if (asFunc && opr.argumentOrder.length > 0) {
				// Specific processing for functions/methods with specified order
				for (int i = 0; i < sepa.length; i++) tokens.add(sepa[i]);
				for (int i = 1; i < opr.argumentOrder.length; i++) {
					if (i > 1) {
						tokens.add(",");
						tokens.add(" ");
					}
					int pos = opr.argumentOrder[i] - 1;
					if (pos < this.children.size()) {
						this.children.get(pos).appendToTokenList(tokens, alternOprs);
					}
				}
			}
			else {
				// Standard processing for operators and functions
				boolean isFirst = true;
				final String[] comma =  new String[]{",", " "};
				for (Expression child: children) {
					for (int i = 0; i < sepa.length; i++) tokens.add(sepa[i]);
					child.appendToTokenList(tokens, asFunc ? 0 : myPrec, alternOprs);
					if (asFunc) {
						sepa = comma;
					}
					else if (text.equals("[]")) {
						// The first operand was the array, now the indices will follow
						if (isFirst) {
							sepa = new String[]{symbol.substring(0, 1)};
						}
						else {
							sepa = comma;
						}
					}
					// Put a gap around all operators except "."
					else if (!text.equals(".")) {
						sepa = new String[]{" ", symbol, " "};
					}
					else {
						sepa = new String[]{symbol};
					}
					isFirst = false;
				}
			}
			if (text.equals("[]") && !children.isEmpty()) {
				tokens.add(symbol.substring(1));	// usually "]"
			}
			// Closing parenthesis?
			if (asFunc ||
					(children.size() > 1 || symbol.startsWith("1"))
					&& (noPrec
					|| myPrec < parentPrec && !(myPrec < 11 && parentPrec == 12))) {
				tokens.add(")");
			}
			// Postfix operator
			if (!asFunc && (children.size() <= 1) && symbol.startsWith("1")) {
				tokens.add(symbol.substring(1));
			}
			break;
		}
		case RECORD_INITIALIZER:
			tokens.add(text);	// The record type name
		case ARRAY_INITIALIZER:
			tokens.add("{");
			for (Expression child: children) {
				for (int i = 0; i < sepa.length; i++) tokens.add(sepa[i]);
				child.appendToTokenList(tokens, alternOprs);;
				sepa = new String[]{",", " "};
			}
			tokens.add("}");
			break;
		case COMPONENT:
			tokens.add(text);
			tokens.add(":");
			tokens.add(" ");
			if (!children.isEmpty()) {
				children.getFirst().appendToTokenList(tokens, alternOprs);
			}
			break;
		case FUNCTION:
			tokens.add(text);
			tokens.add("(");
			for (Expression child: children) {
				for (int i = 0; i < sepa.length; i++) tokens.add(sepa[i]);
				child.appendToTokenList(tokens, alternOprs);
				sepa = new String[]{",", " "};
			}
			tokens.add(")");
			break;
		case PARENTH:
			// Only relevant during parsing (i.e. for debugging purposes)
			tokens.add(text);
			tokens.add(Integer.toString(children.size()));	// This element counts aggregated expressions by adding null entries
			break;
		default:
			break;
		}
	}
	
	/**
	 * Checks whether this expression represents a function call, i.e. something like
	 * {@code <function_name>(<arg>, ...)}.
	 * @return {@code true} if the expression represents a function call
	 * @see #isMethodCall()
	 */
	public boolean isFunctionCall()
	{
		return type == NodeType.FUNCTION && Syntax.isIdentifier(text, false, null);
	}
	
	/**
	 * Checks whether this expression represents a method call, i.e. something like
	 * {@code <object>.<method_name>(<arg>, ...)}.
	 * @return {@code true} if the expression represents a method call
	 */
	public boolean isMethodCall()
	{
		return type == NodeType.OPERATOR && ".".equals(text) &&
				children.size() == 2 && children.getLast().isFunctionCall();
	}
	
	/**
	 * Checks whether the inferred data type of this expression is numeric
	 * (i.e. integral or floating-point of some size and precision)
	 * @param _typeMap - a {@link TypeRegistry} helping to retrieve the
	 * operand types if variables are involved.
	 * @param _trueIfUnknown - the given default value if a definite data
	 * type could not be detected.
	 * @return {@code true} if the data type is definitely numeric or if it
	 * is unknown and {@code _trueIfUnkbown} is {@code true}
	 */
	public boolean isNumeric(TypeRegistry _typeMap, boolean _trueIfUnknown)
	{
		Type dataType = inferType(_typeMap, true);
		if (dataType == null || dataType.isDummy()) {
			return _trueIfUnknown;	// there is no "maybe"
		}
		return dataType.isNumeric();
	}
	
	/**
	 * Recursively tries to infer the data type of this expression (bottom up).
	 * Relies on types already associated to sub-expressions (to renew all,
	 * consider calling {@link #clearDataTypes(boolean)} before).
	 * If {@code _typeMap} is given then new identified types and type associations
	 * will be cached both at the expression substructure and in the {@code _typeMap}.
	 * @param _typeMap - a {@link TypeRegistry} to be used for variable type retrieval
	 * and putting new type associations, or {@code null}.
	 * @param _cacheTypes
	 * @return The top type if identified. May be an anonymous (or temporarily composed)
	 * type, in particular if {@code _typeMap} is not given for retrieval of variable types
	 */
	public Type inferType(TypeRegistry _typeMap, boolean _cacheTypes)
	{
		Type dType = this.dataType;
		if (dType != null) {
			if (_typeMap != null) {
				_typeMap.putType(dType, false);
			}
			// We adopt the associated type and are ready
			return dType;
		}
		boolean isSafe = false;
		switch (type) {
		case ARRAY_INITIALIZER:
		{
			// Check the types of all listed element expressions
			Type elemType = null;
			for (Expression elem: children) {
				Type elType = elem.inferType(_typeMap, _cacheTypes);
				if (elType != null) {
					if (elemType == null) {
						elemType = elType;
					}
					else if (!elemType.getName().equals("dummy")
							&& !elType.equals(elemType)) {
						// Types differ, so return the dummy type
						elemType = Type.getDummyType();
						if (_typeMap == null) {
							// No further retrieval necessary, nothing to cache
							break;
						}
					}
				}
			}
			// Create an anonymous array type that won't be registered
			try {
				dType = new ArrayType(null, elemType, children.size());
			} catch (SyntaxException exc) {
				// FIXME Auto-generated catch block
				exc.printStackTrace();
			}
			break;
		}
		case COMPONENT:
			/* The type of a component initializer is the type of the assigned expression.
			 * If we can't retrieve a component type then we are done because there is no
			 * chance to know the enclosing record initialiser time name. It will be up to
			 * the RECORD_INITIALIZER node above to make sense of the components
			 */
			if (children.size() >= 1) {
				dType = children.getFirst().inferType(_typeMap, _cacheTypes);
			}
			break;
		case FUNCTION:
			/* We have class Function check for built-in functions
			 * TODO: We might look for a way to scan all controller
			 * functions as well and to investigate the Arranger routines
			 */
			{
				Function fun = new Function(this.toString());
				String typName = "???";
				if (fun.isFunction() && (typName = fun.getResultType("???")) != null) {
					if (_typeMap != null) {
						dType = _typeMap.getType(typName);
					}
					else {
						dType = TypeRegistry.getStandardType(typName);
					}
				}
			}
			break;
		case LITERAL:
			dType = TypeRegistry.getStandardTypeFor(text);
			isSafe = true;
			break;
		case OPERATOR:
			if (BOOL_OPERATORS.contains(text)
					|| RELATION_OPERATORS.contains(text)
					|| NEGATION_OPERATORS.contains(text) && !"~".equals(text)) {
				dType = TypeRegistry.getStandardType("boolean");
				isSafe = true;
			}
			else if (text.equals("[]")) {
				if (_typeMap != null) {
					dType = TypeRegistry.getStandardType("boolean");
				}
			}
			else if (text.equals(".")) {
				if (children.size() == 2) {
					Type recType = children.getFirst().inferType(_typeMap, _cacheTypes);
					if (recType != null && recType instanceof RecordType) {
						dType = ((RecordType)recType).getComponentType(children.getLast().text);
						isSafe = children.getFirst().isDataTypeSafe && !dType.isAnonymous();
					}
				}
			}
			else if (text.equals("<-") || text.equals(":=")) {
				if (children.size() == 2) {
					// First check whether the target has a type
					Expression leftSide = children.getFirst();
					Type varType = leftSide.inferType(_typeMap, _cacheTypes);
					// Then try with the right-hand side expression
					dType = children.getLast().inferType(_typeMap, _cacheTypes);
					if (dType == null) {
						dType = varType;
					}
					// We may set the type of the left side ...
					else if (varType == null && _typeMap != null) {
						leftSide.dataType = dType;
						// ... possibly even register the variable type
						if (leftSide.type == NodeType.IDENTIFIER) {
							_typeMap.putTypeFor(leftSide.text, dType, false);
						}
					}
				}
			}
			else {
				Type[] operandTypes = new Type[children.size()];
				boolean allSame = true;
				boolean allNumeric = true;
				boolean allSafe = true;
				Type stringEntry = null;
				Type floatEntry = null;
				int i = 0;
				for (Expression child: children) {
					operandTypes[i] = child.inferType(_typeMap, _cacheTypes);
					if (operandTypes[i] != null) {
						allSafe = child.isDataTypeSafe && allSafe;
						String typeName = operandTypes[i].getName();
						if (!operandTypes[i].isNumeric()) {
							allNumeric = false;
						}
						else if (stringEntry == null && typeName.equals("string")) {
							stringEntry = operandTypes[i];
						}
						else if (typeName.equals("double") || floatEntry == null && typeName.equals("float")) {
							floatEntry = operandTypes[i];
						}
					}
					else {
						allSafe = false;
					}
					if (i > 0 && (operandTypes[i] == null || operandTypes[i-1] == null
							|| !operandTypes[i].equals(operandTypes[i-1]))) {
						allSame = false;
						// Wouldn't we leave the loop now anyway?
						break;
					}
					i++;
				}
				if (text.equals("+")) {
					if (allSame) {
						dType = operandTypes[0];
					}
					else if (stringEntry != null) {
						dType = stringEntry;
					}
					else if (floatEntry != null) {
						dType = floatEntry;
					}
				}
				else if (StringList.explode("%,div,mod,<<,>>,>>>,shl,shr,|,&,^,xor,~", ",").contains(text)) {
					if (allSame && allNumeric && floatEntry == null) {
						// Should actually better be integer
						dType = operandTypes[0];
						isSafe = true;
					}
					else {
						dType = TypeRegistry.getStandardType("int");
					}
				}
				else if (StringList.explode("-,*,/,+1,-1", ",").contains(text) && allNumeric) {
					if (allNumeric) {
						if (allSame) {
							dType = operandTypes[0];
							isSafe = allSafe;
						}
						else if (floatEntry != null) {
							dType = floatEntry;
						}
					}
				}
			}
			break;
		case RECORD_INITIALIZER:
			if (_typeMap != null) {
				dType = _typeMap.getType(text);
				isSafe = dType != null && !dType.isAnonymous();
			}
			// If the data type had not been registered - we we will define a temporary one
			if (dType == null) {
				LinkedHashMap<String, Type> compPairs = new LinkedHashMap<String, Type>();
				int i = 0;
				for (Expression child: this.children) {
					String compName = text + "#" + i++; // For the case of an unnamed component
					if (child.type == NodeType.COMPONENT) {
						compName = child.text;
					}
					compPairs.put(compName, child.inferType(_typeMap, _cacheTypes));
				}
				try {
					dType = new RecordType(text, compPairs);
				} catch (SyntaxException exc) {
					// FIXME Auto-generated catch block
					exc.printStackTrace();
				}
				_cacheTypes = false;
			}
			break;
		case IDENTIFIER:
			if (_typeMap != null) {
				dType = _typeMap.getTypeFor(text);
				isSafe = dType != null && !dType.isAnonymous();
			}
			break;
		default:
			break;
		}
		if (dType != null) {
			if (isSafe && !dType.isDummy()) {
				dataType = dType;
				isDataTypeSafe = !dType.isAnonymous();
			}
			if (_typeMap != null) {
				dataType = dType;
				isDataTypeSafe = isSafe && !dType.isAnonymous();
				// FIXME really force?
				_typeMap.putType(dType, false);
			}
		}

		return dType;
	}
	
	/**
	 * Wipes off all data type associations to give way for an updating
	 * type inference. If {@code onlyUnsafe} is true then safe data type
	 * associations (as for literals) will remain.
	 * @see #inferType(TypeRegistry, boolean)
	 * @see #copy(boolean)
	 */
	public void clearDataTypes(boolean onlyUnsafe)
	{
		if (!onlyUnsafe || !isDataTypeSafe) {
			dataType = null;
			for (Expression child: children) {
				child.clearDataTypes(onlyUnsafe);
			}
		}
	}
	
	/**
	 * Produces a copy of this, either including inferred type references
	 * (if {@code withTypes=true}) or without them (if {@code withTypes=false}).
	 * @param withTypes - whether already attached data type info is to be
	 * copied as well, no matter whether safe or not. (This way, the expression
	 * copy can be transferred to another {@link TypeRegistry} context.)
	 * @return the copy of this
	 * @see #inferType(TypeRegistry, boolean)
	 * @see #clearDataTypes(boolean)
	 */
	public Expression copy(boolean withTypes)
	{
		Expression myCopy = new Expression(this.type, this.text, this.tokenPos, this.children);
		if (withTypes) {
			myCopy.dataType = this.dataType;
			myCopy.isDataTypeSafe = this.isDataTypeSafe;
		}
		return myCopy;
	}
	
	/**
	 * Parses a token list into a list of Expression trees. The resulting Expression
	 * list will usually contain a single element. Raises an exception in case of
	 * syntactical errors.
	 * @param tokens - a {@link StringList} containing the tokenized (and not necessarily
	 * unified) expression text, will be consumed on parsing until a first token that
	 * does not fit well. Spaces will be eliminated (if still present).
	 * @param stopTokens - a {@link StringList} containing possible delimiters,
	 * which, at top level, shall stop the parsing. The found stop token will not be
	 * consumed from {@code tokens}. If being {@code null} then stops without
	 * exception at the first token not being expected, provided the stack is empty
	 * and output is already containing an Expression.
	 * @param tokenNo - the number of preceding tokens of he line not contained in
	 * {@code tokens}, i.e. the overall index of the first token within {@code tokens}
	 * @return the syntax tree or null.
	 * @throws ExpressionException
	 * @see {@link #parseList(StringList, String, String, StringList)}
	 */
	public static LinkedList<Expression> parse(StringList tokens, StringList stopTokens, short tokenNo) throws SyntaxException
	{
		// Basically, this follows Dijkstra's shunting yard algorithm
		Expression expr = null;
		tokens.removeAll(" ");	// Just in case...
		LinkedList<Expression> stack = new LinkedList<Expression>();
		LinkedList<Expression> output = new LinkedList<Expression>();
		short position = 0;
		boolean stopped = false;	// Flag for a detected stopToken
		short nestingLevel = 0;	// bracket nesting - used to check expression listing
		boolean wasOpd = false;	// true if the previous token was an operand
		boolean signPos = true;	// true if a following '+' or '-' must be a sign
		//while (!unifiedTokens.isEmpty() && (stopTokens == null || !stopTokens.contains(unifiedTokens.get(0)) || !stack.isEmpty())) {
		while (!tokens.isEmpty() && !stopped) {
			String token = tokens.get(0);
			if (OPERATOR_PRECEDENCE.containsKey(token.toLowerCase()) || token.equals("[")) {
				// Must be an operator
				if (token.equals("[")) {
					token = "[]";
				}
				int prec = OPERATOR_PRECEDENCE.get(token.toLowerCase());
				// Check for overloaded unary operator (also consider C deref and address operators)
				boolean mayBeSign = signPos && (token.equals("+") || token.equals("-") || token.equals("*") || token.equals("&"));
				while ((expr = stack.peekLast()) != null
						&& expr.type == NodeType.OPERATOR
						&& !mayBeSign
						&& !NEGATION_OPERATORS.contains(token, false)	// is left-associative
						&& prec <= OPERATOR_PRECEDENCE.get(expr.text.toLowerCase())) {
					try {
						expr.children.addFirst(output.removeLast());	// second operand
						if (!NEGATION_OPERATORS.contains(expr.text, false) && !expr.text.endsWith("1")) {
							expr.children.addFirst(output.removeLast());	// first operand
						}
					}
					catch (NoSuchElementException ex) {
						throw new SyntaxException("Too few operands for operator «" + expr.text + "».", expr.tokenPos, ex, 0);
					}
					output.addLast(expr);
					stack.removeLast();
				}
				if (token.equals(".")) {
					if (tokens.count() < 2 || !Syntax.isIdentifier(tokens.get(1), false, null)) {
						throw new SyntaxException("An operator '.' must be followed by an identifier!", tokenNo + position);
					}
				}
				expr = new Expression(NodeType.OPERATOR, mayBeSign ? token+"1" : token, (short)(tokenNo + position));
				stack.addLast(expr);
				if (token.equals("[]")) {
					expr = new Expression(NodeType.PARENTH, "[", (short)(tokenNo + position));
					expr.children.addLast(null);		// one operand is the array
					if (tokens.count() > 2 && !tokens.get(1).equals("]")) {
						expr.children.addLast(null);	// expect at least one index
					}
					stack.addLast(expr);
					nestingLevel++;
				}
				signPos = !token.equals(".");
				wasOpd = false;
			}
			else if (Syntax.isIdentifier(token, false, null)
					&& (stopTokens == null || !stopTokens.contains(token))) {
				String nextToken = null;
				if (tokens.count() > 2) {
					nextToken = tokens.get(1);
				}
				if (BOOL_LITERALS.contains(token)) {
					// Boolean literal
					expr = new Expression(NodeType.LITERAL, token, (short)(tokenNo + position));
					output.addLast((expr));
					wasOpd = true;
				}
				else if ("(".equals(nextToken)) {
					// Function name
					expr = new Expression(NodeType.FUNCTION, token, (short)(tokenNo + position));
					stack.addLast(expr);
					wasOpd = false;
				}
				else if ("{".equals(nextToken)) {
					// Record type name
					// TODO: Identify record type name to verify the record initializer entry
					expr = new Expression(NodeType.RECORD_INITIALIZER, token, (short)(tokenNo + position));
					stack.addLast(expr);
					wasOpd = false;
				}
				else if (":".equals(nextToken)) {
					// Record component name
					// Check that we are within a record initializer context
					Expression paren = stack.peekLast();
					if (paren == null || paren.type != NodeType.PARENTH || !"{".equals(paren.text)) {
						throw new SyntaxException("Found «" + token + ":» outside a record initializer.", tokenNo + position);
					}
					// now provisionally remove the stack top (which is already cached in paren)
					stack.removeLast();
					// beneath the opening brace there must be a recod initializer node
					try {
						if ((expr = stack.peekLast()) == null || expr.type != NodeType.RECORD_INITIALIZER) {
							throw new SyntaxException("Found «" + token + ":» outside a record initializer.", tokenNo + position);
						}
					}
					finally {
						// push the popped parenthesis node again
						stack.addLast(paren);
					}
					// TODO We might check for the record type and verify token is a component id
					expr = new Expression(NodeType.COMPONENT, token, (short)(tokenNo + position));
					stack.addLast(expr);
					// Drop the component name now, such that the loop will remove the colon
					tokens.remove(0); position++;
					wasOpd = false;
				}
				else {
					// If the previous token was an operand then a new expression might start here, so clean up
					if (wasOpd) {
						if (nestingLevel > 0) {
							// Not allowed within brackets
							throw new SyntaxException("Operand «" + token + "» immediately following another",tokenNo +  position);
						}
						while (!stack.isEmpty() && !output.isEmpty()
								&& composeExpression(stack.peekLast(), output)) {
							stack.removeLast();
						}
					}
					expr = new Expression(NodeType.IDENTIFIER, token, (short)(tokenNo + position));
					output.addLast(expr);
					signPos = false;
					wasOpd = true;
				}
			}
			else if (token.startsWith("\"") || token.startsWith("'")
					|| token.equals("false") || token.equals("true")
					|| !token.isEmpty() && Character.isDigit(token.charAt(0))
					// floating-point literal with leading decimal point?
					|| token.length() > 1 && token.charAt(0) == '.' && Character.isDigit(token.charAt(1))) {
				// If the previous token was an operand then a new expression might start here, so clean up
				if (wasOpd) {
					if (nestingLevel > 0) {
						// Not allowed within brackets
						throw new SyntaxException("Operand «" + token + "» immediately following another", tokenNo + position);
					}
					while (!stack.isEmpty() && !output.isEmpty()
							&& composeExpression(stack.peekLast(), output)) {
						stack.removeLast();
					}
				}
				expr = new Expression(NodeType.LITERAL, token, (short)(tokenNo + position));
				output.addLast(expr);
				signPos = false;
				wasOpd = true;
			}
			else if (token.equals(",")) {
				// Argument separator
				boolean parenthFound = false;
				do {
					expr = stack.peekLast();
					if (expr == null) {
						// START KGU#790 2020-10-26: Modified behaviour
						//throw new SyntaxException("Misplaced ',' or missing '(' or '[' or '{'.");
						if (!(stopTokens == null && output.size() >= 1) &&
								!(stopTokens != null && (stopped = stopTokens.contains(",")))) {
							// We leave it to the caller whether the listing of expressions is welcome
							//throw new SyntaxException("Misplaced ',' or missing '(' or '[' or '{'.", position);
							System.out.println("Misplaced ',' at pos. " + (tokenNo + position) + " or missing '(' or '[' or '{'");
						}
						break;
					}
					if (parenthFound = expr.type == NodeType.PARENTH) {
						expr.children.add(null);	// Count the element
					}
					else {
						composeExpression(expr, output);
						stack.removeLast();
					}
				} while (!parenthFound);
//				if (stack.size() > 1) {
//					// Check for an initializer or a function and extend the children list (by null for now)
//					expr = stack.removeLast();	// this is the opening bracket
//					Expression expr0 = stack.getLast();
//					if (expr.text.equals("(") && expr0.type == NodeType.FUNCTION
//							|| expr.text.equals("{") && (expr0.type == NodeType.ARRAY_INITIALIZER || expr0.type == NodeType.RECORD_INITIALIZER)
//							|| expr.text.equals("[") && (expr0.text.equals("[]"))) {
//						expr0.children.addLast(null);
//					}
//					stack.addLast(expr);	// Restore the opening bracket
//				}
				signPos = true;
				wasOpd = false;
			}
			else if (token.equals("(")) {
				// May be a parenthesized arithmetic expression or an argument list
				expr = new Expression(NodeType.PARENTH, token, (short)(tokenNo + position));
				stack.addLast(expr);
				nestingLevel++;
				if (tokens.count() > 1 && !tokens.get(1).equals(")")) {
					expr.children.add(null);	// Expect an element
				}
				signPos = true;
				wasOpd = false;
			}
			else if (token.equals("{")) {
				// May be an array or a record initialiser
				expr = stack.peekLast();
				if (expr == null || expr.type != NodeType.RECORD_INITIALIZER) {
					stack.addLast(new Expression(NodeType.ARRAY_INITIALIZER, "{}", (short)(tokenNo + position)));
				}
				stack.addLast(expr = new Expression(NodeType.PARENTH, token, (short)(tokenNo + position)));
				nestingLevel++;
				if (tokens.count() > 1 && !tokens.get(1).equals("}")) {
					expr.children.add(null);	// Expect an element
				}
				signPos = true;
				wasOpd = false;
			}
			else if ("}])".contains(token)) {
				// End of parentheses, brackets or braces
				int pos = "}])".indexOf(token);
				String opening = "{[(".substring(pos, pos+1);
				boolean parenthFound = false;
				int nItems = 0;
				do {
					expr = stack.peekLast();
					if (expr == null) {
						// START KGU#790 2020-10-26: Modified behaviour
//						if (stopTokens == null && output.size() == 1) {
//							return output;
//						}
						// END KGU#790 2020-10-26
						throw new SyntaxException("'" + token + "' without preceding '" + opening + "'.", tokenNo + position);
					}
					if (parenthFound = expr.type == NodeType.PARENTH) {
						if (!expr.text.equals(opening)) {
							// Wrong type of opening bracket
							throw new SyntaxException("'" + token + "' without matching '" + opening + "'.", tokenNo + position);
						}
						nItems = expr.children.size();
					}
					else {
						composeExpression(expr, output);
					}
					stack.removeLast();
				} while (!parenthFound);
				expr = stack.peekLast();
				// Care for non-arithmetic brackets (argument list, index list, initializer)
				if (expr != null) {
					if (opening.equals("(") && expr.type == NodeType.FUNCTION
							|| opening.equals("{") && (expr.type == NodeType.ARRAY_INITIALIZER || expr.type == NodeType.RECORD_INITIALIZER)
							|| opening.equals("[") && expr.text.equals("[]")) {
						for (int i = 0; i < nItems; i++) {
							try {
								expr.children.addFirst(output.removeLast());
							}
							catch (NoSuchElementException ex) {
								throw new SyntaxException("Lost arguments / items / indices for " + expr.text, expr.tokenPos, ex, 0);
							}
						}
						output.addLast(stack.removeLast());
					}
				}
				nestingLevel--;
				signPos = false;
				wasOpd = true;
			}
			else if (stopTokens != null && stopTokens.contains(token)) {
				stopped = true;
			}
			else {
				// TODO Can we infer a syntactical error here?
				System.err.println("Unexpected token «" + token + "» skipped.");
			}
			if (!stopped) {
				tokens.remove(0); position++;
			}
		} // while (!tokens.isEmpty() && !stopped)
		
		// Now resolve the remaining operator stack content as far a possible
		while (!stack.isEmpty()) {
			expr = stack.removeLast();
			if (expr.type == NodeType.PARENTH) {
				throw new SyntaxException("There are more opening '" + expr.text + "' than closing brackets.", expr.tokenPos);
			}
			int nOpds = expr.children.size();
			if (expr.type == NodeType.OPERATOR && !expr.text.equals("[]")) {
				if (NEGATION_OPERATORS.contains(expr.text, false) || expr.text.endsWith("1")) {
					nOpds = 1;
				}
				else if (!expr.text.equals("[]")) {
					nOpds = 2;
				}
			}
			try {
				for (int i = 0; i < nOpds; i++) {
					expr.children.addFirst(output.removeLast());
				}
			}
			catch (NoSuchElementException ex) {
				throw new SyntaxException("Too few operands for operator '" + expr.text + "'.", expr.tokenPos, ex, 0);
			}
			output.addLast(expr);
		}
		return output;
	}

	/**}
	 * Given the incomplete expression node {@code expr}, which is supposed to be
	 * an operator or a record component initialiser, completes the expression by
	 * consuming elements from the operand stack {@code operands} and pushes the
	 * composed expression node to the {@code operands} stack.
	 * @param expr - the incomplete expression node
	 * @param operands - stack of operands
	 * @return {@code true} if a composition was possible, {@code false} otherwise
	 * @throws SyntaxException if {@code expr} is of an inappropriate type or if
	 * {@code operands} does not contain enough elements to accomplish {@code expr}
	 */
	private static boolean composeExpression(Expression expr, LinkedList<Expression> operands) throws SyntaxException {
		boolean done = true;
		if (expr.type == NodeType.OPERATOR) {
			try {
				expr.children.addFirst(operands.removeLast());
				if (!NEGATION_OPERATORS.contains(expr.text, false) && !expr.text.endsWith("1")) {
					expr.children.addFirst(operands.removeLast());
				}
			}
			catch (NoSuchElementException ex) {
				throw new SyntaxException("Too few operands for operator '" + expr.text + "'", expr.tokenPos, ex, 0);
			}
		}
		else if (expr.type == NodeType.COMPONENT) {
			try {
				expr.children.addFirst(operands.removeLast());
			}
			catch (NoSuchElementException ex) {
				throw new SyntaxException("Missing value for record component «" + expr.text + "»", 0, ex, 0);
			}
		}
		else {
			done = false;
			//throw new SyntaxException("Parsing stack inconsistent, found: " + expr.toString(), 0);
		}
		operands.addLast(expr);
		return done;
	}
	
	/**
	 * Returns a negated condition for the expression {@code _cond}<br/>
	 * @param _cond - an expression supposed to represent a condition (i.e.
	 * a Boolean expression) - <b>may get modified as well!</b>
	 * @param _verbose - if {@code true} then "not" will be used instead of "!" if a
	 * negation operator is to be added.
	 * @return a condition that is the negation of the original {@code _cond}
	 * @throws SyntaxException if the expression was definitely not a Boolean expression
	 */
	public static Expression negateCondition(Expression _cond, boolean _verbose) throws SyntaxException
	{
		int ix = -1;
		boolean okay = false;
		Expression expr = _cond;
		switch (_cond.type) {
		case LITERAL:
		{
			ix = BOOL_LITERALS.indexOf(_cond.text);
			if (ix >= 0) {
				// Just replace it by its opposite
				expr.text = BOOL_LITERALS.get(1- ix);
				okay = true;
			}
			break;
		}
		case IDENTIFIER:
		case FUNCTION:
			// TODO: Should we check the expression data type?
			// We assume it might be of Boolean type
			expr = new Expression(NodeType.OPERATOR, _verbose ? "not" : "!", _cond.tokenPos);
			expr.children.add(_cond);
			okay = true;
			break;
		case OPERATOR:
		{
			if (_cond.text.equals("!") || _cond.text.equalsIgnoreCase("not")) {
				// Just drop the negation
				expr = _cond.children.getFirst();
				okay = true;
			}
			else if ((ix = RELATION_OPERATORS.indexOf(_cond.text)) >= 0) {
				// Replace by its respective complement
				expr.text = RELATION_OPERATORS.get(RELATION_OPERATORS.count()-1 - ix);
				okay = true;
			}
			else if (BOOL_OPERATORS.contains(_cond.text)) {
				expr = new Expression(NodeType.OPERATOR, _verbose ? "not" : "!", _cond.tokenPos);
				expr.children.add(_cond);
				okay = true;
			}
			break;
		}
		default:
			break;
		}
		if (!okay) {
			throw new SyntaxException("«" + _cond.toString() + "» is not a Boolean expression ", _cond.tokenPos);
		}
		return expr;
	}
	
	/**
	 * Recursively gathers variable names occurring in this expression tree,
	 * differentiating between assigned variables (on the left-hand side of
	 * an assignment) and merely referenced variables (all else).
	 * @param assignedVars - {@link StringList} to add assigned variables
	 * @param usedVars - {@link StringList} to add referenced variables
	 * @param isLeftSide - whether this expression was found on the left-hand
	 * side of an assignment (may also be a record or array root)
	 * @return {@code true} if there is no structure inconsistency, {@code false} otherwise.
	 */
	public boolean gatherVariables(StringList assignedVars, StringList usedVars, boolean isLeftSide)
	{
		boolean complete = true;
		if (assignedVars != null || usedVars != null) {
			switch (this.type) {
			case PARENTH:
				// This expression cannot have been parsed correctly, otherwise this would have been gone.
				complete = false;
				break;
			case ARRAY_INITIALIZER:
			case RECORD_INITIALIZER:
			case COMPONENT:
			case FUNCTION:
				if (isLeftSide) {
					// Non of these may occur directly on the left-hand side
					complete = false;
				}
				else {
					for (Expression child: children) {
						complete = child.gatherVariables(assignedVars, usedVars, false) && complete;
					}
				}
				break;
			case LITERAL:
				// No variable, but okay
				break;
			case OPERATOR:
				if ("<-".equals(text) || ":=".equals(text)) {
					if (isLeftSide) {
						// There cannot be an assignment on the left-hand side of an assignment
						complete = false;
					}
					else {
						complete = children.getFirst().gatherVariables(assignedVars, usedVars, true) && complete;
						complete = children.getLast().gatherVariables(assignedVars, usedVars, false) && complete;
					}
				}
				else if ("[]".equals(text)) {
					complete = children.getFirst().gatherVariables(assignedVars, usedVars, isLeftSide) && complete;
					complete = children.getLast().gatherVariables(assignedVars, usedVars, false) && complete;
				}
				else if (".".equals(text)) {
					// ignore the second operand - can only be component names
					complete = children.getFirst().gatherVariables(assignedVars, usedVars, isLeftSide) && complete;
				}
				else if (isLeftSide) {
					complete = false;
				}
				else {
					for (Expression child: children) {
						complete = child.gatherVariables(assignedVars, usedVars, false) && complete;
					}					
				}
				break;
			case IDENTIFIER:
				if (isLeftSide) {
					if (assignedVars != null) {
						assignedVars.addIfNew(text);
					}
				}
				else if (usedVars != null) {
					usedVars.addIfNew(text);
				}
				break;
			default:
				// Something is wrong here
				complete = false;
				break;
			}
		}
		return complete;
	}

}
