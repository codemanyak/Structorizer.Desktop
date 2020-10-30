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
import java.util.Iterator;

import java.util.LinkedList;
import java.util.NoSuchElementException;

import lu.fisch.structorizer.elements.TypeMapEntry;
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
 * <tr><td>VARIABLE</td><td>variable name</td><td>(empty)</td></tr>
 * <tr><td>OPERATOR</td><td>operator symbol</td><td>operands</td></tr>
 * <tr><td>INDEX</td><td>""</td><td>array, index expressions</td></tr>
 * <tr><td>FUNCTION</td><td>function name</td><td>arguments</td></tr>
 * <tr><td>QUALIFIER</td><td>comp name</td><td>record</td></tr>
 * <tr><td>ARRAY_INITIALIZATION</td><td>type name (if any)</td><td>elements</td></tr>
 * <tr><td>RECORD_INITIALIZATION</td><td>type name</td><td>components</td></tr>
 * <tr><td>COMPONENT</td><td>comp name</td><td>comp value</td></tr>
 * </table>
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
	 * @see #appendToTokenList(StringList, HashMap, Integer)
	 */
	@SuppressWarnings("serial")
	public static final HashMap<String, Integer> OPERATOR_PRECEDENCE = new HashMap<String, Integer>() {{
		put("<-", 0);
		put("or", 1);
		put("||", 1);
		put("and", 2);
		put("&&", 2);
		put("|", 3);
		put("^", 4);
		put("xor", 4);
		put("&", 5);
		put("=", 6);
		put("==", 6);
		put("<>", 6);
		put("!=", 6);
		put("<", 7);
		put(">", 7);
		put("<=", 7);
		put(">=", 7);
		put("shl", 8);
		put("<<", 8);
		put("shr", 8);
		put(">>", 8);
		put(">>>", 8);
		put("+", 9);
		put("-", 9);
		put("*", 10);
		put("/", 10);
		put("div", 10);
		put("mod", 10);
		put("%", 10);
		put("not", 11);
		put("!", 11);
		put("+1", 11);	// sign
		put("-1", 11);	// sign
		put("*1", 11);	// pointer deref (C)
		put("&1", 11);	// address (C)
		put("[]", 12);
		put(".", 12);
	}};
	
	/** COMPONENT means the colon separating a component name and a component value in a record initializer,<br/>
	 * QUALIFIER is the dot separating a record and a component name (whereas the dot in a method call is handled as OPERATOR) <br/>
	 * PARENTH symbolizes an opening parenthesis and is only temporarily used within the shunting yard algorithm in {@link Expression#parse(StringList, StringList, StringList)} */
	public static enum NodeType {LITERAL, VARIABLE, OPERATOR, FUNCTION, /*QUALIFIER, INDEX,*/ ARRAY_INITIALIZER, RECORD_INITIALIZER, COMPONENT, PARENTH};
	public NodeType type;
	public String text;
	public LinkedList<Expression> children;
	public short tokenPos = 0;
	
	public Expression(NodeType _type, String _text, short _position)
	{
		type = _type;
		text = _text;
		children = new LinkedList<Expression>();
		tokenPos = _position;
	}
	
	public Expression(NodeType _type, String _text, short _position, LinkedList<Expression> _children)
	{
		type = _type;
		text = _text;
		children = new LinkedList<Expression>(_children);
		tokenPos = _position;
	}
	
	/**
	 * Derives the tree from the given tokens.
	 * Use {@link #parse(StringList, StringList, StringList)} instead.
	 * @param _tokens
	 */
	@Deprecated
	public Expression(StringList _tokens)
	{
		// FIXME: Derive the tree from _tokens
		type = NodeType.LITERAL;
		text = "";
		children = new LinkedList<Expression>();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return toString(-1);
	}
	/**
	 * Returns this expression as linearized string in Structorizer-compatible
	 * syntax, assuming that this expression is embedded as operand of an operator
	 * with precedence level {@code parentPrec}
	 * @param parentPrec - the assumed parent operator precedence
	 * @return the composed expression string
	 * @see #OPERATOR_PRECEDENCE
	 * @see #appendToTokenList(StringList)
	 * @see #appendToTokenList(StringList, HashMap)
	 * @see #appendToTokenList(StringList, HashMap, Integer)
	 */
	private String toString(int parentPrec)
	{
		StringBuilder sb = new StringBuilder();
		String sepa = "";	// separator
		Iterator<Expression> iter = null;
		switch (type) {
		case LITERAL:
		case VARIABLE:
			sb.append(text);
			break;
		case OPERATOR: {
			// May be unary or binary
			int myPrec = OPERATOR_PRECEDENCE.get(text);
			if (children.size() <= 1) {
				// Unary operator
				if (text.endsWith("1")) {
					sb.append(text.substring(0, text.length()-1));
				}
				else if (children.isEmpty() || !text.equals("[]")) {
					sb.append(text);
				}
				// Insert a gap if the operator is an identifier (word)
				if (Function.testIdentifier(text, false, null)) {
					sb.append(" "); 
				}
			}
			// Without pointers, there is no need to put parentheses if parent is . or []
			else if (myPrec < parentPrec && !(myPrec < 11 && parentPrec == 12)) {
				sepa = "(";
			}
			iter = children.iterator();
			while (iter.hasNext()) {
				sb.append(sepa + iter.next().toString(myPrec));
				if (text.equals("[]")) {
					sepa = sepa.isEmpty() ? "[" : ", ";
				}
				else if (!text.equals(".")) {
					sepa = " " + text + " ";
				}
				else {
					sepa = text;					
				}
			}
			if (text.equals("[]") && !children.isEmpty()) {
				sb.append(']');
			}
			if (children.size() > 1 && myPrec < parentPrec && !(myPrec < 11 && parentPrec == 12)) {
				sb.append(')');
			}
			break;
		}
//		case INDEX:	// obsolete
//			// First child expresses the array
//			sb.append(children.getFirst().toString(OPERATOR_PRECEDENCE.get("[]")) + "[");
//			iter = children.listIterator(1);
//			while (iter.hasNext()) {
//				sb.append(sepa + iter.next().toString());
//				sepa = ", ";
//			}
//			sb.append(']');
//			break;
		case RECORD_INITIALIZER:
			sb.append(text);
		case ARRAY_INITIALIZER:
			sb.append('{');
			iter = children.iterator();
			while (iter.hasNext()) {
				sb.append(sepa + iter.next().toString());
				sepa = ", ";
			}
			sb.append('}');
			break;
		case COMPONENT:
			sb.append(text + ": ");
			if (!children.isEmpty()) {
				sb.append(children.getFirst().toString());
			}
			break;
		case FUNCTION:
			sb.append(text + "(");
			iter = children.iterator();
			while (iter.hasNext()) {
				sb.append(sepa + iter.next().toString());
				sepa = ", ";
			}
			sb.append(')');
			break;
//		case QUALIFIER:	// obsolete
//			sb.append(children.getFirst().toString(OPERATOR_PRECEDENCE.get(".")));
//			sb.append("." + text);
//			break;
		case PARENTH:
			sb.append(text);
			sb.append(children.size());	// This element counts aggregated expressions by adding null entries
			break;
		default:
			break;		
		}
		return sb.toString();
	}
	
	/**
	 * Append this expression tree in tokenized form to the given token list {@code tokens}.
	 * Uses standard operator precedence table {@link #OPERATOR_PRECEDENCE}.
	 * @param tokens - a non-null {@link #toString(int)} to append my tokens to.
	 * @see #appendToTokenList(StringList, HashMap)
	 */
	public void appendToTokenList(StringList tokens)
	{
		appendToTokenList(tokens, OPERATOR_PRECEDENCE, -1);
	}
	/**
	 * Append this expression tree in tokenized form to the given token list {@code tokens}.
	 * Uses the given operator precedence table @{@code precMap} instead of
	 * {@link #OPERATOR_PRECEDENCE} to decide whether parentheses must be used. This way,
	 * differing precedence orders for target languages may be addressed. An empty map will
	 * force parentheses around any non-atomic sub-expression.
	 * @param tokens - a non-null {@link #toString(int)} to append my tokens to.
	 * @param precMap - a customized operator precedence map (if an operator is not found there,
	 * then composed operand expressions will be parenthesized).
	 * @see #appendToTokenList(StringList)
	 * @see #appendToTokenList(StringList, HashMap, Integer)
	 */
	public void appendToTokenList(StringList tokens, HashMap<String, Integer> precMap)
	{
		appendToTokenList(tokens, precMap, -1);
	}
	/**
	 * Append this expression tree in tokenized form to the given token list {@code tokens}.
	 * Uses the given operator precedence table @{@code precMap} instead of
	 * {@link #OPERATOR_PRECEDENCE} to decide whether parentheses must be used. This way,
	 * differing precedence orders for target languages may be addressed. An empty map will
	 * force parentheses around any non-atomic sub-expression.
	 * @param tokens - a non-null {@link #toString(int)} to append my tokens to.
	 * @param precMap - a customized operator precedence map (if an operator is not found there,
	 * then composed operand expressions will be parenthesized).
	 * @param parentPrec - the operator precedence of the operator this expression forms an
	 * operand for. -1 means there is no parent operator, {@code null} forces parentheses if
	 * non-atomic.
	 * @see #appendToTokenList(StringList)
	 * @see #appendToTokenList(StringList, HashMap, Integer)
	 */
	public void appendToTokenList(StringList tokens, HashMap<String, Integer> precMap, Integer parentPrec)
	{
		String sepa = "";
		Iterator<Expression> iter = null;
		switch (type) {
		case LITERAL:
		case VARIABLE:
			tokens.add(text);
			break;
		case OPERATOR: {
			Integer myPrec = precMap.get(text);
			if (children.size() <= 1) {
				if (text.endsWith("1")) {
					tokens.add(text.substring(0, text.length()-1));
				}
				else if (children.isEmpty() || !text.equals("[]")){
					tokens.add(text);
				}
				if (text.equals("+") || text.equals("-")) {
					// Seems to be a sign operator - same precedence level as negation
					myPrec = OPERATOR_PRECEDENCE.get("!");
				}
			}
			// Without pointers, there is no need to put parentheses if parent is . or []
			else if (myPrec == null || parentPrec == null || myPrec < parentPrec && !(myPrec < 11 && parentPrec == 12)) {
				sepa = "(";
			}
			iter = children.iterator();
			while (iter.hasNext()) {
				tokens.add(sepa);
				iter.next().appendToTokenList(tokens, precMap, myPrec);
				if (text.equals("[]")) {
					sepa = sepa.isEmpty() ? "[" : ", ";
				}
				else {
					sepa = text;
				}
			}
			if (text.equals("[]")) {
				tokens.add("]");
			}
			if (children.size() > 1
					&& (myPrec == null || parentPrec == null
					|| myPrec < parentPrec && !(myPrec < 11 && parentPrec == 12))) {
				tokens.add(")");
			}
			break;
		}
//		case INDEX:	// obsolete
//			children.getFirst().appendToTokenList(tokens, precMap, precMap.get("[]"));
//			iter = children.listIterator(1);
//			while (iter.hasNext()) {
//				tokens.add(sepa);
//				iter.next().appendToTokenList(tokens, precMap);
//				sepa = ",";
//			}
//			tokens.add("]");
//			break;
		case RECORD_INITIALIZER:
			tokens.add(text);
		case ARRAY_INITIALIZER:
			tokens.add("{");
			iter = children.iterator();
			while (iter.hasNext()) {
				tokens.add(sepa);
				iter.next().appendToTokenList(tokens, precMap);;
				sepa = ",";
			}
			tokens.add("}");
			break;
		case COMPONENT:
			tokens.add(text);
			tokens.add(":");
			children.getFirst().appendToTokenList(tokens, precMap);
			break;
		case FUNCTION:
			tokens.add(text);
			tokens.add("(");
			iter = children.iterator();
			while (iter.hasNext()) {
				tokens.add(sepa);
				iter.next().appendToTokenList(tokens, precMap);
				sepa = ",";
			}
			tokens.add(")");
			break;
//		case QUALIFIER:	// obsolete
//			children.getFirst().appendToTokenList(tokens, precMap, precMap.get("."));
//			tokens.add(".");
//			tokens.add(text);
//			break;
		default:
			break;		
		}		
	}
	
	public boolean isFunction()
	{
		return type == NodeType.FUNCTION;
	}
	
	public boolean isNumeric(HashMap<String, TypeMapEntry> _typeMap, boolean _trueIfUnknown)
	{
		TypeMapEntry entry = inferType(_typeMap);
		if (entry == null) {
			return _trueIfUnknown;	// there is no "maybe"
		}
		return entry.isNumeric(_trueIfUnknown);
	}
	
	public TypeMapEntry inferType(HashMap<String, TypeMapEntry> _typeMap)
	{
		TypeMapEntry typeEntry = null;
		switch (type) {
		case ARRAY_INITIALIZER:
		{
			TypeMapEntry elemType = null;
			for (Expression elem: children) {
				TypeMapEntry entry = elem.inferType(_typeMap);
				if (entry != null) {
					if (elemType == null) {
						elemType = entry;
					}
					else if (entry != elemType && !entry.getCanonicalType(true, true).equals(elemType.getCanonicalType(true, true))) {
						elemType = null;
						break;
					}
				}
			}
			if (elemType == null) {
				typeEntry = new TypeMapEntry("array of object", null, _typeMap, null, 0, false, false);
			}
			else {
				typeEntry = new TypeMapEntry("array of " + elemType.getCanonicalType(true, true), null, _typeMap, null, 0, false, false);
			}
		}
			break;
		case COMPONENT:
			// FIXME How to we know the record type of the entire expression?
//		{
//			TypeMapEntry recType = _typeMap.get(":" + text);
//			if (recType != null && recType.isRecord()) {
//				typeEntry = recType.getComponentInfo(true).get(children.get(0).text);
//			}
//			if (typeEntry == null) {
//				typeEntry = children.get(1).inferType(_typeMap);
//			}
//		}
			break;
		case FUNCTION:
			/* TODO: We should have a list of all built-in functions, scan all controller
			 * functions and investigate the Arranger routines
			 */
			break;
//		case INDEX:	// obsolete
//			break;
		case LITERAL:
			// TODO find the code that derives the type in other modules
			break;
		case OPERATOR:
			if (StringList.explode("=,==,!=,<>,<,>,<=,>=,and,or,not,!", ",").contains(text)) {
				typeEntry = new TypeMapEntry("bool","bool", _typeMap, null, 0, false, false);
			}
			else if (text.equals("[]")) {
				TypeMapEntry arrType = children.get(0).inferType(_typeMap);
				if (arrType != null && arrType.isArray() && arrType.isConflictFree()) {
					StringList typeDescrs = arrType.getTypes(false);
					if (!typeDescrs.isEmpty()) {
						String typeName = typeDescrs.get(0);
						if (typeName.startsWith("@")) {	// Should do so
							typeName = typeName.substring(1);
							if ((typeEntry = _typeMap.get(":" + typeName)) == null) {
								// FIXME this is somewhat quick and dirty - what about possible record components?
								typeEntry = new TypeMapEntry(typeName.replace("@", "array of "), null, _typeMap, null, 0, false, false);
							}
						}
					}
				}
			}
			else if (text.equals(".")) {
				TypeMapEntry recType = children.get(0).inferType(_typeMap);
				if (recType != null && recType.isRecord()) {
					typeEntry = recType.getComponentInfo(true).get(children.get(1).text);
				}
			}
			else {
				TypeMapEntry[] operandTypes = new TypeMapEntry[children.size()];
				boolean allSame = true;
				boolean allNumeric = true;
				TypeMapEntry stringEntry = null;
				TypeMapEntry floatEntry = null;
				for (int i = 0; i < children.size(); i++) {
					operandTypes[i] = children.get(i).inferType(_typeMap);
					String canonical = null;
					if (operandTypes[i] != null) {
						canonical = operandTypes[i].getCanonicalType(true, true);
						if (!operandTypes[i].isNumeric(true)) {
							allNumeric = false;
						}
						else if (stringEntry == null && canonical.equals("string")) {
							stringEntry = operandTypes[i];
						}
						else if (canonical.equals("double") || floatEntry == null && canonical.equals("float")) {
							floatEntry = operandTypes[i];
						}
					}
					if (i > 0 && (canonical == null || operandTypes[i-1] == null
							|| canonical.equals(operandTypes[i-1].getCanonicalType(true, true)))) {
						allSame = false;
						break;
					}
				}
				if (text.equals("+")) {
					if (allSame) {
						typeEntry = operandTypes[0];
					}
					else if (stringEntry != null) {
						typeEntry = stringEntry;
					}
					else if (floatEntry != null) {
						typeEntry = floatEntry;
					}
				}
				else if (StringList.explode("%,div,mod,<<,>>,>>>,shl,shr,|,&,^,xor", ",").contains(text)) {
					if (allSame && allNumeric) {
						typeEntry = operandTypes[0];
					}
					else if ((typeEntry = _typeMap.get(":int")) == null) {
						typeEntry = new TypeMapEntry("int", "int", _typeMap, null, 0, false, false);
					}
				}
				else if (StringList.explode("-,*,/,+1,-1", ",").contains(text) && allNumeric) {
					if (allNumeric) {
						if (allSame) {
							typeEntry = operandTypes[0];
						}
						else if (floatEntry != null) {
							typeEntry = floatEntry;
						}
					}
				}
			}
			break;
//		case QUALIFIER:	// obsolete
//		{
//			// FIXME: This relies (questionably) on a variable as first operand
//			Expression rec = children.get(0);
//			
//		}
//			break;
		case RECORD_INITIALIZER:
			typeEntry = _typeMap.get(":" + text);
			break;
		case VARIABLE:
			typeEntry = _typeMap.get(text);
			break;
		default:
			break;
		}
		return typeEntry;
	}
	
	/**
	 * Parses a token list into a list of Expression trees. The resulting Expression
	 * list will usually contain a single element. Raises an exception in case of
	 * syntactical errors.
	 * @param unifiedTokens - a {@link StringList} containing the tokenized and
	 * unified expression text, will be consumed on parsing until a first token that
	 * does not fit well. Spaces will be eliminated (if still present).
	 * @param stopTokens - a {@link StringList} containing possible delimiters,
	 * which, at top level, shall stop the parsing. The found stop token will not be
	 * consumed from {@code unifiedTokens}. If being {@code null} then stops without
	 * exception at the first token not being expected, provided the stack is empty
	 * and output is already containing an Expression.
	 * @param varNames - List of variable names if known (otherwise all identifiers without
	 * following argument list would be interpreted as values)
	 * @return the syntax tree or null.
	 * @throws ExpressionException
	 * @see {@link #parseList(StringList, String, String, StringList)}
	 */
	public static LinkedList<Expression> parse(StringList unifiedTokens, StringList stopTokens, StringList varNames) throws SyntaxException
	{
		// FIXME: signs like in -1, 8 * -7, etc.
		/* FIXME: Array access:
		 * stack[top-1]  --->  stack
		 * stack[top-1]  --->  top
		 * stack[top-1]  --->  1[-]
		 */
		// Basically, this follows Dijkstra's shunting yard algorithm
		Expression expr = null;
		unifiedTokens.removeAll(" ");	// Just in case...
		LinkedList<Expression> stack = new LinkedList<Expression>();
		LinkedList<Expression> output = new LinkedList<Expression>();
		short position = 0;
		boolean stopped = false;	// Flag for a detected stopToken
		short nestingLevel = 0;	// bracket nesting - used to check expression listing
		boolean wasOpd = false;	// true if the previous token was an operand
		boolean signPos = true;	// true if a following '+' or '-' must be a sign
		//while (!unifiedTokens.isEmpty() && (stopTokens == null || !stopTokens.contains(unifiedTokens.get(0)) || !stack.isEmpty())) {
		while (!unifiedTokens.isEmpty() && !stopped) {
			String token = unifiedTokens.get(0);
			if (Function.testIdentifier(token, false, null)
					&& (stopTokens == null || !stopTokens.contains(token))) {
				String nextToken = null;
				if (unifiedTokens.count() > 2) {
					nextToken = unifiedTokens.get(1);
				}
				if ("(".equals(nextToken)) {
					expr = new Expression(NodeType.FUNCTION, token, position);
					stack.addLast(expr);
					wasOpd = false;
				}
				else if ("{".equals(nextToken)) {
					// TODO: Identify record type name to verify the record initializer entry
					expr = new Expression(NodeType.RECORD_INITIALIZER, token, position);
					stack.addLast(expr);
					wasOpd = false;
				}
				else if (":".equals(nextToken)) {
					// Check that we are within a record initializer context
					Expression paren = stack.peekLast();
					if (paren == null || paren.type != NodeType.PARENTH || !"{".equals(paren.text)) {
						throw new SyntaxException("Found '" + token + ":' outside a record initializer.", position);
					}
					// now provisionally remove the stack top (which is already cached in paren)
					stack.removeLast();
					// beneath the opening brace there must be a recod initializer node
					try {
						if ((expr = stack.peekLast()) == null || expr.type != NodeType.RECORD_INITIALIZER) {
							throw new SyntaxException("Found '" + token + ":' outside a record initializer.", position);
						}
					}
					finally {
						// push the popped parenthesis node again
						stack.addLast(paren);
					}
					// TODO We might check for the record type and verify token is a component id
					expr = new Expression(NodeType.COMPONENT, token, position);
					stack.addLast(expr);
					// Drop the component name now, such that the loop will remove the colon
					unifiedTokens.remove(0); position++;
					wasOpd = false;
				}
				else /*if (varNames == null || varNames.contains(token))*/ {
					// If the previous token was an operand then a new expression might start here, so clean up
					if (wasOpd) {
						if (nestingLevel > 0) {
							// Not allowed within brackets
							throw new SyntaxException("Operand «" + token + "» immediately following another", position);
						}
						while (!stack.isEmpty() && !output.isEmpty()
								&& composeExpression(stack.peekLast(), output)) {
							stack.removeLast();
						}
					}
					expr = new Expression(NodeType.VARIABLE, token, position);
					output.addLast(expr);
					signPos = false;
					wasOpd = true;
				}
			}
			else if (token.startsWith("\"") || token.startsWith("'")
					|| token.equals("false") || token.equals("true")
					|| !token.isEmpty() && Character.isDigit(token.charAt(0))) {
				// If the previous token was an operand then a new expression might start here, so clean up
				if (wasOpd) {
					if (nestingLevel > 0) {
						// Not allowed within brackets
						throw new SyntaxException("Operand «" + token + "» immediately following another", position);
					}
					while (!stack.isEmpty() && !output.isEmpty()
							&& composeExpression(stack.peekLast(), output)) {
						stack.removeLast();
					}
				}
				expr = new Expression(NodeType.LITERAL, token, position);
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
							System.out.println("Misplaced ',' at pos. " + position + " or missing '(' or '[' or '{'");
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
			else if (OPERATOR_PRECEDENCE.containsKey(token) || token.equals("[")) {
				if (token.equals("[")) {
					token = "[]";
				}
				int prec = OPERATOR_PRECEDENCE.get(token);
				// Check for overloaded unary operator (also consider C deref and address operators)
				boolean mayBeSign = signPos && (token.equals("+") || token.equals("-") || token.equals("*") || token.equals("&"));
				while ((expr = stack.peekLast()) != null
						&& expr.type == NodeType.OPERATOR
						&& !mayBeSign
						&& !token.equals("!") && !token.equals("not")	// is left-associative
						&& prec <= OPERATOR_PRECEDENCE.get(expr.text)) {
					try {
						expr.children.addFirst(output.removeLast());	// second operand
						if (!expr.text.equals("!") && !expr.text.equalsIgnoreCase("not") && !expr.text.endsWith("1")) {
							expr.children.addFirst(output.removeLast());	// first operand
						}
					}
					catch (NoSuchElementException ex) {
						throw new SyntaxException("Too few operands for operator '" + expr.text + "'.", expr.tokenPos, ex);
					}
					output.addLast(expr);
					stack.removeLast();
				}
				expr = new Expression(NodeType.OPERATOR, mayBeSign ? token+"1" : token, position);
				stack.addLast(expr);
				if (token.equals("[]")) {
					expr = new Expression(NodeType.PARENTH, "[", position);
					expr.children.addLast(null);		// one operand is the array
					if (unifiedTokens.count() > 2 && !unifiedTokens.get(1).equals("]")) {
						expr.children.addLast(null);	// expect at least one index
					}
					stack.addLast(expr);
					nestingLevel++;
				}
				signPos = true;
				wasOpd = false;
			}
			else if (token.equals("(")) {
				// May be a parenthesized arithmetic expression or an argument list
				expr = new Expression(NodeType.PARENTH, token, position);
				stack.addLast(expr);
				nestingLevel++;
				if (unifiedTokens.count() > 1 && !unifiedTokens.get(1).equals(")")) {
					expr.children.add(null);	// Expect an element
				}
				signPos = true;
				wasOpd = false;
			}
			else if (token.equals("{")) {
				// May be an array or a record initializer
				expr = stack.peekLast();
				if (expr == null || expr.type != NodeType.RECORD_INITIALIZER) {
					stack.addLast(new Expression(NodeType.ARRAY_INITIALIZER, "{}", position));
				}
				stack.addLast(expr = new Expression(NodeType.PARENTH, token, position));
				nestingLevel++;
				if (unifiedTokens.count() > 1 && !unifiedTokens.get(1).equals("}")) {
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
						if (stopTokens == null && output.size() == 1) {
							return output;
						}
						// END KGU#790 2020-10-26
						throw new SyntaxException("'" + token + "' without preceding '" + opening + "'.", position);
					}
					if (parenthFound = expr.type == NodeType.PARENTH) {
						if (!expr.text.equals(opening)) {
							// Wrong type of opening bracket
							throw new SyntaxException("'" + token + "' without matching '" + opening + "'.", position);
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
								throw new SyntaxException("Lost arguments / items / indices for " + expr.text, expr.tokenPos, ex);
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
				unifiedTokens.remove(0); position++;
			}
		} // while (!unifiedTokens.isEmpty() && !stopped)
		
		// Now resolve the remaining operator stack content as far a possible
		while (!stack.isEmpty()) {
			expr = stack.removeLast();
			if (expr.type == NodeType.PARENTH) {
				throw new SyntaxException("There are more opening '" + expr.text + "' than closing brackets.", expr.tokenPos);
			}
			int nOpds = expr.children.size();
			if (expr.type == NodeType.OPERATOR && !expr.text.equals("[]")) {
				if (expr.text.equals("!") || expr.text.equalsIgnoreCase("not") || expr.text.endsWith("1")) {
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
				throw new SyntaxException("Too few operands for operator '" + expr.text + "'.", expr.tokenPos, ex);
			}
			output.addLast(expr);
		}
		return output;
	}

	/**}
	 * Given the incomplete expression node {@code expr}, which is supposed to be
	 * an operator or a record component initializer, completes the expression by
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
				if (!expr.text.equals("!") && !expr.text.equalsIgnoreCase("not") && !expr.text.endsWith("1")) {
					expr.children.addFirst(operands.removeLast());
				}
			}
			catch (NoSuchElementException ex) {
				throw new SyntaxException("Too few operands for operator " + expr.text, 0, ex);
			}
		}
		else if (expr.type == NodeType.COMPONENT) {
			try {
				expr.children.addFirst(operands.removeLast());
			}
			catch (NoSuchElementException ex) {
				throw new SyntaxException("Missing value for record component «" + expr.text + "»", 0, ex);
			}
		}
		else {
			done = false;
			//throw new SyntaxException("Parsing stack inconsistent, found: " + expr.toString(), 0);
		}
		operands.addLast(expr);
		return done;
	}

}
