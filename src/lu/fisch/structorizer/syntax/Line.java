/*
    Structorizer
    A little tool which you can use to create Nassi-Shneiderman Diagrams (NSD)

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.syntax.Declaration.DeclarationRule;
import lu.fisch.structorizer.syntax.Expression.NodeType;
import lu.fisch.structorizer.syntax.Expression.Operator;
import lu.fisch.utils.StringList;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Class representing a line of text for all kinds of Element.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2019-12-27      First Issue
 *      Kay Gürtzig     2020-10-24      Changed from Interface to Class
 *      Kay Gürtzig     2020-11-01      Parsing and variable gathering mechanism implemented
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

/**
 * Represents the syntactic structure of a line of an {@link Element}
 * @author Kay Gürtzig
 */
public class Line {

	/**
	 * Type of an {@link Element} text line.<br/>
	 * Each line type corresponds to a distinguishable syntactical structure and
	 * is associated to a certain number of constitutive expressions.
	 * @see Line#getType()
	 * @see Line#getMinExprCount(LineType)
	 * @see Line#getMaxExprCount(LineType)
	 * @author Kay Gürtzig
	 */
	public static enum LineType {
		/** Line could not be parsed, 0 expr. [, 1 error message?] */
		LT_RAW,
		/** Assignment without declaration (function call, too?), 1 expr */
		LT_ASSIGNMENT,
		/** Input instruction, >= 0 expr. (1st = prompt string) */
		LT_INPUT,
		/** Output instruction, >= 0 expr. */
		LT_OUTPUT,
		/** Condition of an Alternative or loop, 1 expr */
		LT_CONDITION,
		/** For loop head, 4 expr. (1st = name, 3rd = int literal) */
		LT_FOR_LOOP,
		/** Foreach loop head, 2 expr. (1st = name, 2nd = name/array/string/expression list) */
		LT_FOREACH_LOOP,
		/** Procedure or function call, 1 expr. (= assignment or proc call) */
		LT_ROUTINE_CALL,
		/** Function call with constant definition, 1 expr. (= assignment) + type */
		LT_CONST_FUNCT_CALL,
		/** Return instruction, 0..1 expr. */
		LT_RETURN,
		/** Leave instruction, 0..1 expr. */
		LT_LEAVE,
		/** Exit instruction, 1 expr. */
		LT_EXIT,
		/** Throw instruction, 1 expr. */
		LT_THROW,
		/** Case head, 1 expr. */
		LT_CASE,
		/** Case selector, 1..n expr. */
		LT_SELECTOR,
		/** Case default label, id or % */
		LT_DEFAULT,
		/** Catch clause (TRY block), 1 expr. */
		LT_CATCH,
		/** Type definition, 1 expr. (= type id) + type */
		LT_TYPE_DEF,
		/** Constant definition, 1 expr. + type */
		LT_CONST_DEF,
		/** Variable declaration, 1..n expr. (= var ids) + type */
		LT_VAR_DECL,
		/** Variable initialisation, 1 expr. (assignment) + type */
		LT_VAR_INIT,
		/** Routine header, 1 Routine (name + n Declarations + type/null) */
		LT_ROUTINE
	};
	
	public static final int LT_ASSIGNMENT_MASK   = 1 << LineType.LT_ASSIGNMENT.ordinal();
	public static final int LT_INPUT_MASK        = 1 << LineType.LT_INPUT.ordinal();
	public static final int LT_OUTPUT_MASK       = 1 << LineType.LT_OUTPUT.ordinal();
	public static final int LT_CONDITION_MASK    = 1 << LineType.LT_CONDITION.ordinal();
	public static final int LT_FOR_LOOP_MASK     = 1 << LineType.LT_FOR_LOOP.ordinal();
	public static final int LT_FOREACH_LOOP_MASK = 1 << LineType.LT_FOREACH_LOOP.ordinal();
	public static final int LT_PROC_CALL_MASK    = 1 << LineType.LT_ROUTINE_CALL.ordinal();
	public static final int LT_LEAVE_MASK        = 1 << LineType.LT_LEAVE.ordinal();
	public static final int LT_RETURN_MASK       = 1 << LineType.LT_RETURN.ordinal();
	public static final int LT_EXIT_MASK         = 1 << LineType.LT_EXIT.ordinal();
	public static final int LT_THROW_MASK        = 1 << LineType.LT_THROW.ordinal();
	public static final int LT_CASE_MASK         = 1 << LineType.LT_CASE.ordinal();
	public static final int LT_SELECTOR_MASK     = 1 << LineType.LT_SELECTOR.ordinal();
	public static final int LT_DEFAULT_MASK      = 1 << LineType.LT_DEFAULT.ordinal();
	public static final int LT_CATCH_MASK        = 1 << LineType.LT_CATCH.ordinal();
	public static final int LT_TYPE_DEF_MASK     = 1 << LineType.LT_TYPE_DEF.ordinal();
	public static final int LT_CONST_DEF_MASK    = 1 << LineType.LT_CONST_DEF.ordinal();
	public static final int LT_VAR_DECL_MASK     = 1 << LineType.LT_VAR_DECL.ordinal();
	public static final int LT_VAR_INIT_MASK     = 1 << LineType.LT_VAR_INIT.ordinal();
	public static final int LT_ROUTINE_MASK      = 1 << LineType.LT_ROUTINE.ordinal();
	
	/**
	 * Maps internal prefix keys and some actual keywords like "var", "dim", "const",
	 * "type" to corresponding {@link LineType}s
	 */
	private static final HashMap<String, LineType> lineStartsToTypes = new HashMap<String, LineType>();
	static {
		lineStartsToTypes.put("input", LineType.LT_INPUT);
		lineStartsToTypes.put("output", LineType.LT_OUTPUT);
		lineStartsToTypes.put("preAlt", LineType.LT_CONDITION);
		lineStartsToTypes.put("preCase", LineType.LT_CASE);
		lineStartsToTypes.put("preFor", LineType.LT_FOR_LOOP);
		lineStartsToTypes.put("preForIn", LineType.LT_FOREACH_LOOP);
		lineStartsToTypes.put("preWhile", LineType.LT_CONDITION);
		lineStartsToTypes.put("preRepeat", LineType.LT_CONDITION);
		lineStartsToTypes.put("preLeave", LineType.LT_LEAVE);
		lineStartsToTypes.put("preReturn", LineType.LT_RETURN);
		lineStartsToTypes.put("preExit", LineType.LT_EXIT);
		lineStartsToTypes.put("preThrow", LineType.LT_THROW);
		lineStartsToTypes.put("var", LineType.LT_VAR_DECL);	// FIXME could also be LT_VAR_INIT
		lineStartsToTypes.put("dim", LineType.LT_VAR_DECL);	// FIXME could also be LT_VAR_INIT
		lineStartsToTypes.put("const", LineType.LT_CONST_DEF);
		lineStartsToTypes.put("type", LineType.LT_TYPE_DEF);
	}
	
	private LineType type;
	private Expression[] expressions;
	private Type dataType = null;
	
	public Line(LineType _type, Expression[] _expressions)
	{
		type = _type;
		expressions = _expressions;
	}
	
	public Line(LineType _type, Expression[] _expressions, Type _dataType)
	{
		type = _type;
		expressions = _expressions;
		dataType = _dataType;
	}
	
	/**
	 * @return the {@link LineType} of this line.
	 * @see #getExprCount()
	 * @see #getExpression(int)
	 */
	public LineType getType()
	{
		return type;
	}
	
	/**
	 * Get the {@code _index}-th associated parsed expression (a syntax tree) for this
	 * line.
	 * @param _index - the index of the wanted expression
	 * @return the requested expression as syntax tree if available, null otherwise.
	 * @see #getExprCount()
	 * @see #getType()
	 * @see #getMinExprCount(LineType)
	 * @see #getMaxExprCount(LineType)
	 */
	public Expression getExpression(int _index)
	{
		Expression expr = null;
		if (expressions != null && _index >= 0 && _index < expressions.length) {
			expr = expressions[_index];
		}
		return expr;
	}
	
	/**
	 * @return the number of stored expression syntax trees. Value should lie between
	 * {@link #getMinExprCount(LineType)} and {@link #getMaxExprCount(LineType)} for
	 * this {@link LineType}
	 * @see #getType()
	 * @see #getExpression(int)
	 */
	public int getExprCount()
	{
		if (expressions == null) {
			return 0;
		}
		return expressions.length;
	}
	
	/**
	 * Yields teh associated data type.
	 * @param _explicitOnly - st this {@code true} if you are only interested in an
	 * explicitly assigned type here (e.g. from a type definition or variable declaration),
	 * otherwise the attached expressions will be asked for their data type.
	 * @return the explicitly associated data type (e.g. in case of a type definition
	 * or variable declaration)
	 */
	public Type getDataType(boolean _explicitOnly)
	{
		Type dType = dataType;
		if (dType == null && !_explicitOnly && expressions != null) {
			for (Expression expr: expressions) {
				if ((dType = expr.dataType) != null) {
					return dType;
				}
			}
		}
		return dType;
	}
	
	/**
	 * @param _type - the {@link LineType} the query is for
	 * @return the minimum number of required expressions for this type.
	 * @see #getMaxExprCount(LineType)
	 * @see #getType()
	 * @see #getExprCount()
	 * @see #getExpression(int)
	 */
	public static int getMinExprCount(LineType _type)
	{
		int count = -1;
		switch (_type) {
		case LT_ASSIGNMENT:
		case LT_CASE:
		case LT_SELECTOR:
		case LT_CATCH:
		case LT_CONDITION:
		case LT_EXIT:
		case LT_ROUTINE_CALL:
		case LT_THROW:
		case LT_INPUT:
		case LT_CONST_DEF:
		case LT_VAR_DECL:
			count = 1;
			break;
		case LT_FOR_LOOP:
			count = 3;
			break;
		case LT_FOREACH_LOOP:
			count = 2;
			break;
		case LT_LEAVE:
		case LT_OUTPUT:
		case LT_RAW:
		case LT_RETURN:
		case LT_TYPE_DEF:
			count = 0;
		case LT_ROUTINE:
			count = 1;
			break;
		default:
			break;
		}
		return count;
	}
	
	/**
	 * @param _type - the {@link LineType} the query is for
	 * @return the maximum number of associated expressions for this type
	 * @see #getMinExprCount(LineType)
	 * @see #getType()
	 * @see #getExprCount()
	 * @see #getExpression(int)
	 */
	public static int getMaxExprCount(LineType _type)
	{
		int count = -1;
		switch(_type) {
		case LT_ASSIGNMENT:
		case LT_CASE:
		case LT_CATCH:
		case LT_CONDITION:
		case LT_CONST_DEF:
		case LT_EXIT:
		case LT_LEAVE:
		case LT_THROW:
		case LT_RETURN:
		case LT_ROUTINE_CALL:
		case LT_VAR_INIT:
			count = 1;
			break;
		case LT_ROUTINE:
			// Name (id) and parameter list
			count = 2;
			break;
		case LT_FOR_LOOP:
			count = 4;
			break;
		case LT_INPUT:
		case LT_OUTPUT:
		case LT_SELECTOR:
		case LT_FOREACH_LOOP:
		case LT_VAR_DECL:
		case LT_RAW:
			count = Integer.MAX_VALUE;
			break;
		case LT_TYPE_DEF:
			count = 0;
			break;
		default:
			break;
		}
		return count;
	}
	
	/**
	 * Tries to parse the given (unbroken) {@code textLine} into a Line structure
	 * 
	 * @param tokens - the tokenized text line (without operator unification!)
	 * @param expectedTypes - a bitmap marking the expected line types (may depend on the
	 * Element type). Controls the validity.
	 * @param typeMap - a {@link TypeRegistry} for data type retrieval or creation, may be
	 * {@code null}
	 * @return a Line object or {@code null} (e.g. in case of an empty line)
	 * 
	 * @throws SyntaxException if there is a syntactic error in the text
	 * 
	 * @see #parse(String, LineType, TypeRegistry, StringList)
	 */
	public static Line parse(StringList tokens, int expectedTypes, TypeRegistry typeMap) throws SyntaxException
	{
		Type dType = null;
		short tokenPos = 0;
		// Check if the line starts with a command keyword
		LineType lType = null;	// Detected line type
		String token0 = null;	// Starting token
		ArrayList<Expression> exprs = new ArrayList<Expression>();
		/* First of all, condense all keywords (i.e. replace the respective token lists
		 * by a single compact token), then unify the operators and remove spaces.
		 */
		for (String key: Syntax.keywordSet()) {
			String keyword = Syntax.getKeyword(key);
			if (keyword != null && !keyword.trim().isEmpty()) {
				StringList splitKey = Syntax.getSplitKeyword(key);
				int len = splitKey.count();
				int start = -1;
				while ((start = tokens.indexOf(splitKey, start+1, !Syntax.ignoreCase)) >= 0) {
					tokens.remove(start, start + len);
					// Don't reinsert redundant keywords
					if (!key.startsWith("post") || key.startsWith("postFor")) {
						tokens.insert(keyword, start);	// FIXME shouldn't we insert key here?
					}
					else {
						start--;
					}
				}
			}
		}
		// Level case for "var", "dim", and "as" keywords
		tokens.replaceAllCi("var", "var");
		tokens.replaceAllCi("dim", "dim");
		tokens.replaceAllCi("as", "as");
		tokens.trim();
		Syntax.unifyOperators(tokens, false);
		// Now check the start token against prefix keywords
		// FIXME: We should get rid of postfix keywords
		if (!tokens.isEmpty()) {
			token0 = tokens.get(0);
			for (Map.Entry<String, LineType> entry: lineStartsToTypes.entrySet()) {
				String keyword = Syntax.getKeyword(entry.getKey());
				if (keyword != null && token0.equals(keyword) 
						// This applies e.g. for "var", "dim", "const", "type" etc.
						|| keyword == null && token0.equals(entry.getKey())) {
					lType = entry.getValue();
					// Initial keyword is identified, so we can remove it
					tokens.remove(0);
					break;
				}
			}
		}
		if (tokens.contains("<-")) {
			if (lType == null) {
				lType = LineType.LT_ASSIGNMENT;
			}
			else if (lType == LineType.LT_VAR_DECL) {
				lType = LineType.LT_VAR_INIT;
			}
		}
		// The type may still be unidentified (e.g. if no optional pre-key was used)
		if (lType != null && (expectedTypes & (1 << lType.ordinal())) == 0) {
			// Something is wrong here, list all accepted line types
			StringList acceptedTypes = new StringList();
			for (LineType lt: LineType.values()) {
				if ((expectedTypes & (1 << lt.ordinal())) != 0) {
					acceptedTypes.add(lt.toString());
				}
			}
			throw new SyntaxException("wrongLineType",
					new String[] {lType.toString(), acceptedTypes.concatenate("|")},
					0, null, 0);
		}
		if (lType == null) {
			// Adopt the expected type if unique
			for (LineType lt: LineType.values()) {
				if ((expectedTypes & (1 << lt.ordinal())) != 0) {
					if (lType != null) {
						// More than one accepted type - wipe it again
						lType = null;
						break;
					}
					lType = lt;
				}
			}
		}
		StringList declTokens = new StringList(tokens);
		if (lType == null) {
			lType = LineType.LT_RAW;
		}
		switch (lType) {
		case LT_VAR_INIT:
		case LT_CONST_DEF:
			tokens.removeBlanks();	// Make sure the token number of the operator is correct
			tokenPos = (short)tokens.indexOf("<-");
			declTokens = declTokens.subSequence(0, tokenPos);
			tokens.remove(0, tokenPos+1);
		case LT_VAR_DECL: {
			dType = Declaration.parse(declTokens, Declaration.DeclarationRule.VDECL, (short)0, typeMap, exprs);
			if (tokenPos >= 0 && exprs.size() == 1) {
				List<Expression> valueExprs = Expression.parse(tokens, null, (short)(tokenPos+1));
				Expression asgnmt = new Expression(Expression.NodeType.OPERATOR, "<-", tokenPos);
				asgnmt.children.add(exprs.get(0));
				asgnmt.children.add(valueExprs.get(0));
				exprs.clear();
				exprs.add(asgnmt);
			}
			break;
		}
		case LT_TYPE_DEF:
		{
			/* TODO
			 * These are the possible syntax variants to detect:
			 * type <id> '=' <record>'{' <record_group_List> '}';
			 * type <id> '=' enum '{' <enum_list> '}';
			 * type <id> '=' <type>
			 * where:
			 * <colon> ::= ':' | as
			 * <record> ::= record | struct
			 */
			int nTokens = tokens.count();
			if (nTokens < 3
					|| !Syntax.isIdentifier(tokens.get(0), false, null)
					|| !tokens.get(1).equals("=")) {
				throw new SyntaxException("Wrong type definition syntax: missing '='", 2);
			}
			dType = Declaration.parse(tokens, Declaration.DeclarationRule.TYPE_DEF, tokenPos, typeMap, exprs);
		}
			break;
		default:
			// Identify the separators and extract the expressions
			lType = extractExpressions(tokens, lType, exprs, typeMap);
		}
		return new Line(lType, exprs.toArray(new Expression[exprs.size()]));
	}

	/**
	 * Extract the expressions from the unprefixed token list {@code _tokens} according
	 * to the expected or detected line type {@code _type} and gather them in {@code _exprs}.
	 * Consider the known variable names {@code _varNames} if given.
	 * 
	 * @param _tokens - The token list without an identified leading keyword (this should
	 * reflect in the given {@code _type}). May still contain blanks and non-unified operators.
	 * Any still contained keyword must already have been condensed into a single token.
	 * @param _type - the {@link LineType} as far as already detected or expected (or {@code null})
	 * @param _exprs - the {@link ArrayList} to which the parsed expressions are to be added.
	 * @param _typeMap  - a {@link TypeRegistry} for data type retrieval or creation, may be
	 * {@code null}
	 * @return the eventual line type
	 * 
	 * @throws SyntaxException if there are syntactic errors
	 */
	private static LineType extractExpressions(StringList _tokens, LineType _type, 
			ArrayList<Expression> _exprs, TypeRegistry _typeMap) throws SyntaxException
	{
		if (_type == null) {
			_type = LineType.LT_RAW;
		}
		// Condense the token list
		_tokens.removeBlanks();
		Syntax.unifyOperators(_tokens, false);	// Can we make this an option (argument)?
		int nTokens = _tokens.count();
		switch (_type) {
		case LT_ASSIGNMENT:
		{
			// This might be a variable initialisation in C or Java style - so try both
			SyntaxException wrongExpr = null;
			boolean assignmentFailed = false;
			int posAsgn = _tokens.indexOf("<-");
			List<Expression> parsed = null;
			try {
				parsed = Expression.parse(_tokens, null, (short)0);
				if (nTokens == 0 && parsed.size() == 1) {
					_exprs.add(parsed.get(0));
				}
				else {
					assignmentFailed = true;
				}
			}
			catch (SyntaxException ex) {
				wrongExpr = ex;
				assignmentFailed = true;
			}
			if (assignmentFailed) {
				// FIXME still relevant here?
				ArrayList<Expression> ids = new ArrayList<Expression>();
				Type varType = Declaration.parse(_tokens, DeclarationRule.CDECL, (short)0, _typeMap, ids);
				parsed = Expression.parse(_tokens.subSequence(posAsgn+1, nTokens), null, (short)(posAsgn+1));
				if (nTokens == 0 && parsed.size() == 1) {
					_exprs.add(parsed.get(0));
					_type = LineType.LT_VAR_INIT;
				}
				else if (wrongExpr != null) {
					throw wrongExpr;
				}
			}
		}
			break;
		case LT_CONST_DEF:
			// It might involve a type association ("typed constant" in Pascal)
		{
			int posAsgn = _tokens.indexOf("<-");
			StringList left = _tokens.subSequence(0, posAsgn).trim();
			if (left.contains(":") || left.contains("as", false) || left.count() > 1) {
				// Same handling as in LT_VAR_INIT
				StringList right = _tokens.subSequence(posAsgn+1, nTokens);
				ArrayList<Expression> ids = new ArrayList<Expression>();
				Type constType = Declaration.parse(left, DeclarationRule.VDECL, (short)1, _typeMap, ids);
				List<Expression> parsed = Expression.parse(right, null, (short)(posAsgn + 2));
				if (ids.size() == 1 && right.isEmpty() && parsed.size() == 1) {
					_exprs.add(ids.get(0));
					_exprs.add(parsed.get(0));
					break;
				}
				else {
					// Obviously something went wrong
					throw new SyntaxException("Wrong constant expression", nTokens - right.count());
				}
			}
			// Otherwise do the same thing as follows
		}
		case LT_CASE:
		case LT_CONDITION:
		case LT_EXIT:
		case LT_THROW:
		case LT_ROUTINE_CALL:
			// We expect exactly one expression and no further keywords in general
			{
				List<Expression> parsed = Expression.parse(_tokens, null, (short)0);
				if (_tokens.isEmpty() && parsed.size() == 1) {
					_exprs.add(parsed.get(0));
				}
				else {
					if (_type == LineType.LT_EXIT || _type == LineType.LT_THROW
							|| _type == LineType.LT_CONST_DEF) {
						nTokens++;	// A keyword had been cut off
					}
					throw new SyntaxException("Wrong expression", nTokens - _tokens.count());
				}
			}
			break;
		case LT_FOREACH_LOOP:
		case LT_FOR_LOOP:
			// This might still be a loop of the respective other kind
			{
				int ixIn = _tokens.indexOf(Syntax.getSplitKeyword("postForIn"), 0, !Syntax.ignoreCase);
				int ixTo = _tokens.indexOf(Syntax.getSplitKeyword("postFor"), 0, !Syntax.ignoreCase);
				if (Syntax.getKeyword("preFor").equals(Syntax.getKeyword("preForIn"))) {
					if (ixIn > 0) {
						_type = LineType.LT_FOREACH_LOOP;
					}
					else if (ixTo > 0) {
						_type = LineType.LT_FOR_LOOP;
					}
				}
				boolean isForIn = _type == LineType.LT_FOREACH_LOOP;
				if (isForIn && ixTo > 0 || !isForIn && ixIn > 0) {
					// This is obviously wrong
					throw new SyntaxException("wrongForLoop",
							new String[] {
									(isForIn ? "@f" : "@e"),
									(isForIn ? Syntax.getKeyword("postFor") : Syntax.getKeyword("postForIn"))
									},
							1 + (isForIn ? ixTo : ixIn), null, 0);
				}
				int ix0 = isForIn ? ixIn : ixTo;
				// This should contain the first expression (an assignment or a variable name, respectively)
				StringList tokens0 = _tokens.subSequence(0, ix0);
				List<Expression> parsed = Expression.parse(tokens0, null, (short)1);
				if (tokens0.isEmpty() && parsed.size() == 1) {
					Expression varSpec = parsed.get(0);
					if (isForIn && varSpec.type != NodeType.IDENTIFIER ||
							!isForIn && (varSpec.type != NodeType.OPERATOR
							|| !varSpec.text.equals("<-") && !varSpec.text.equals(":=")
							|| varSpec.children.size() != 2
							|| varSpec.children.getFirst().type != Expression.NodeType.IDENTIFIER)) {
						throw new SyntaxException("Wrong loop variable specification: " + parsed.toString(), 1);
					}
					if (isForIn) {
						_exprs.add(parsed.get(0));
					}
					else {
						// append assigned variable and start value expression separately
						_exprs.addAll(varSpec.children);
					}
				}
				else {
					// FIXME Just for debugging? Otherwise we better throw an exception or leave
					System.err.println("Wrong parsing result or remainder: "
							+ parsed.toString() + " | " + tokens0.concatenate(null));
				}
				// The tail contains either end value and step clause or the item list
				_tokens.remove(0, ix0+1);
				_tokens = _tokens.trim();
				if (isForIn) {
					// Extract the item list (may be an array initializer, a variable or a sequence of expressions)
					parsed = Expression.parse(_tokens, null, (short)(ix0 + 2));
					if (_tokens.isEmpty() && !parsed.isEmpty()) {
						_exprs.addAll(parsed);
					}
					else {
						throw new SyntaxException("Incorrect item list: " + _tokens.concatenate(null),
								ix0 + 2);
					}
				}
				else {
					// First extract the end value expression
					String stepKey = Syntax.getKeyword("stepFor");
					nTokens = _tokens.count();
					parsed = Expression.parse(_tokens, StringList.getNew(stepKey), (short)(ix0 + 2));
					if (parsed.size() == 1) {
						_exprs.addAll(parsed);
					}
					else {
						throw new SyntaxException("Wrong end value specification: " + parsed.toString(),
								ix0 + 2);
					}
					// Then get the increment, which ought to be an integral literal
					if (!_tokens.isEmpty()) {
						int consumed = nTokens - _tokens.count();	
						if (_tokens.get(0).equals(stepKey)) {
							_tokens.remove(0);
							consumed++;
							parsed = Expression.parse(_tokens, null, (short)(ix0+2 + consumed));
							if (parsed.size() == 1) {
								_exprs.addAll(parsed);
								// TODO we might check for signed or unsigned literal
							}
							else {
								throw new SyntaxException("Wrong step value specification: " + parsed.toString(),
										ix0+2 + consumed);
							}
						}
						else {
							throw new SyntaxException("Wrong step syntax: " + _tokens.concatenate(null),
									ix0+2 + consumed);
						}
					}
				}
			}
			break;
		case LT_INPUT:
			{
				List<Expression> parsed = Expression.parse(_tokens, null, (short)1);
				if (!parsed.isEmpty()) {
					Expression first = parsed.get(0);
					if (first.type != Expression.NodeType.LITERAL) {
						// Insert null to make clear that there is no prompt string
						parsed.add(0, null);
					}
				}
				_exprs.addAll(parsed);
			}
			break;
		case LT_LEAVE:
		case LT_RETURN:
			{
				List<Expression> parsed = Expression.parse(_tokens, null, (short)1);
				if (_tokens.isEmpty() && parsed.size() <= 1) {
					// TODO: In case of leave we ought to check that it is a cardinal number
					_exprs.addAll(parsed);
				}
				else {
					throw new SyntaxException("Wrong exit value specification: "
							+ parsed.toString() + " | " + _tokens.concatenate(null), 0);
				}
			}
			break;
		case LT_RAW:
			{
				// Just give it a try, we will only attach expressions if they cover the entire line
				try {
					List<Expression> parsed = Expression.parse(_tokens, null, (short)0);
					if (_tokens.isEmpty()) {
						_exprs.addAll(parsed);
					}
				}
				catch (SyntaxException exc) {}
			}
		case LT_OUTPUT:
			// Just extract all available expressions
			_exprs.addAll(Expression.parse(_tokens, null, (short)1));
			break;
		case LT_ROUTINE:
			break;
		default:
			break;
		}
		return _type;
	}

	/**
	 * Tries to parse the given (unbroken) {@code textLine} into a Line structure
	 * 
	 * @param textLine - the (unbroken) text line as string
	 * @param expectedTypes - a bit pattern composed of LineType masks, e.g.
	 *        {@code LT_ASSIGNMENT_MASK | LT_VAR_INIT_MASK} to specify the
	 *        set of expected (and acceptable) line types (may depend on the Element
	 *        type). Controls the validity.
	 * @param typeMap - a data type map to retrieve from and add to, or {@code null}
	 * @param errors - all detected errors and warnings will be appended to this {@link StringList}.
	 * @return a Line object. In case of syntactic errors, a Line of type {@link LineType#LT_RAW}
	 * would be returned.
	 */
	public static Line parse(String textLine, int expectedTypes, TypeRegistry typeMap,
			StringList errors)
	{
		StringList tokens = Syntax.splitLexically(textLine, true).trim();
		if (!tokens.isEmpty()) {
			try {
				return parse(tokens, expectedTypes, typeMap);
			} catch (SyntaxException exc) {
				if (errors != null) {
					errors.add(exc.getMessage());
				}
			}
		}
		return new Line(LineType.LT_RAW, null);
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(this.type.toString());
		if (this.dataType != null) {
			sb.append("(");
			sb.append(dataType.toString());
			sb.append(")");
		}
		if (this.expressions != null) {
			sb.append("[");
			String sepa = "";
			for (Expression expr: this.expressions) {
				sb.append(sepa);
				// In an LT_INPUT line, the first expression may be null
				if (expr != null) {
					sb.append(expr.toString());
				}
				sepa = "; ";
			}
			sb.append("]");
		}
		return sb.toString();
	}
	
	/**
	 * Retrieves all
	 * @param assignedVars
	 * @param declaredVars
	 * @param usedVars
	 * @return
	 */
	public boolean gatherVariables(StringList assignedVars, StringList declaredVars, StringList usedVars)
	{
		boolean okay = true;
		switch (this.type) {
		case LT_CONST_DEF:
		case LT_ASSIGNMENT:
		case LT_CATCH:
			if (expressions != null && expressions.length == 1) {
				okay = expressions[0].gatherVariables(assignedVars, usedVars,
						this.type == LineType.LT_CATCH);
			}
			break;
		case LT_CASE:
		case LT_CONDITION:
		case LT_EXIT:
		case LT_LEAVE:
		case LT_RETURN:
		case LT_THROW:
		case LT_OUTPUT:
		case LT_ROUTINE_CALL:
			for (Expression expr: expressions) {
				okay = expr.gatherVariables(assignedVars, usedVars, false) && okay;
			}
			break;
		case LT_FOREACH_LOOP:
		case LT_FOR_LOOP:
			if (expressions != null && expressions.length > 1) {
				if (assignedVars != null) {
					assignedVars.addIfNew(expressions[0].text);
				}
				for (int i = 1; i < expressions.length; i++) {
					okay = expressions[i].gatherVariables(assignedVars, usedVars, false) && okay;
				}
			}
			else {
				okay = false;
			}
			break;
		case LT_INPUT:
			if (expressions != null && expressions.length > 1) {
				for (int i = 1; i < expressions.length; i++) {
					okay = expressions[i].gatherVariables(assignedVars, usedVars, true) && okay;
				}
			}
			break;
		case LT_RAW:
			okay = false;
		case LT_TYPE_DEF:
			// Nothing to do here
			break;
		case LT_VAR_DECL:
			if (this.expressions != null && this.expressions.length >= 1) {
				Expression firstExpr = this.expressions[0];
				if (firstExpr.type == Expression.NodeType.OPERATOR
						&& "<-,:=".contains(firstExpr.text)) {
					okay = firstExpr.gatherVariables(assignedVars, usedVars, false);
				}
				else if (declaredVars != null) {
					for (Expression expr: expressions) {
						if (firstExpr.type == Expression.NodeType.IDENTIFIER) {
							declaredVars.addIfNew(expr.text);
						}
					}
				}
			}
			break;
		case LT_ROUTINE:
			// TODO fetch the parameters
			break;
		default:
			okay = false;
			break;
		}
		return okay;
	}
	
	//========================================================================
	// Provisional test stuff
	//========================================================================
	
	/**
	 * Test method
	 * @param args
	 */
	public static void main(String[] args) {
		Syntax.loadFromINI();
		
//		byte[] opdpos = new byte[] {(byte)1, (byte)0, (byte)2};
//		System.out.println(Arrays.toString(opdpos));

		// C priorities but some operators as functions or methods
		@SuppressWarnings("serial")
		HashMap<String, Operator> someAsFunctionOrMethod = new HashMap<String, Operator>() {{
			put("<-", new Operator(":=", 0));
			put("||", new Operator("or", new int[] {1, 2}));	// as method
			put("&&", new Operator("and", new int[] {0,2,1}));	// as function with swapped operands
			put("^", new Operator("xor", 4));
			put("==", new Operator("equals", new int[] {1, 2}));// as method
			put("!=", new Operator("<>", 6));
			put("<", new Operator("<", 7));
			put(">", new Operator(">", 7));
			put("<=", new Operator("<=", 7));
			put(">=", new Operator(">=", 7));
			put("%", new Operator("mod"));	// As function
			put("!", new Operator("not", 11));
			put("+1", new Operator("+1", 11));	// sign
			put("-1", new Operator("-1", 11));	// sign
			put("*1", new Operator("1^", 11));	// pointer deref (Pascal)
			put("&1", new Operator("@1", 11));	// address (Pascal)
		}};
		
		System.out.println(someAsFunctionOrMethod);
		
		// C priorities but some operators as functions or methods
		HashMap<String, Operator> flatPriorities = new HashMap<String, Operator>();
		
		@SuppressWarnings("serial")
		HashMap<String, Operator> pascalOperators = new HashMap<String, Operator>() {{
			put("<-", new Operator(":=", 0));
			put("or", new Operator("or", 9));
			put("||", new Operator("or", 9));
			put("and", new Operator("and", 10));
			put("&&", new Operator("and", 10));
			put("|", new Operator("|", 9));
			put("^", new Operator("xor", 9));
			put("xor", new Operator("xor", 9));
			put("&", new Operator("&", 10));
			put("==", new Operator("=", 6));
			put("!=", new Operator("=", 6));
			put("<", new Operator("<", 6));
			put(">", new Operator(">", 6));
			put("<=", new Operator("<=", 6));
			put(">=", new Operator(">=", 6));
			put("%", new Operator("mod", 10));
			put("!", new Operator("not", 11));
			put("+1", new Operator("+1", 11));	// sign
			put("-1", new Operator("-1", 11));	// sign
			put("*1", new Operator("1^", 11));	// pointer deref (Pascal)
			put("&1", new Operator("@1", 11));	// address (Pascal)
			put("?,:", new Operator("ifthenelse"));
		}};
		
		String[] exprTests = new String[] {
				// "good" expressions
				"varPart <- conjugateStrings(userInput, entry.keyword, findInfo[1], reflexions)",
				"c <- 57 > 3 ? 12 : -5",
				"c + 5 * 3 == 8 ? (423 * 7) : 128 + 9",
				"(c + 5 * 3 == 8) ? (57 > 3 ? 12 : -5) : 128 + 9",
				"(c + 5 * 3 == 8) ? 57 > 3 ? 12 : -5 : 128 + 9",
				"a <- 7 * (15 - sin(1.3))",
				"a <- 7 * (15 - sin(b))",
				"a[i+1] <- { 16, \"doof\", 45+9, b}",
				"7 * (15 - sin(1.3)), { 16, \"doof\", 45+9, b}",
				"7 * (15 - pow(-18, 1.3)) + len({ 16, \"doof\", 45+9, b})",
				"rec <- Date{2020, a + 4, max(29, d)}",
				"rec <- Date{year: 2020, month: a + 4, day: max(29, d)}",
				"test[top-1]",
				"25 * -a - b",
				"a < 5 && b >= c || isDone",
				"not hasFun(person)",
				"a = 17 and (c != 5)",
				"word == \"nonsense\"",
				"(18 + b) % (a + (23 * c))",
				"28 - b % 13 > 4.5 / sqrt(23) * x",
				"*p <- 17 + &x",
				"a & ~(17 | 86) ^ ~b | ~c | ~1",
				"m := date.month",
				"len <- str.length()",
				"0b01101",
				"hx <- 0xaedf",
				// Defective lines - are to provoke SyntaxExceptions
				"7 * (15 - sin(1.3)) }, { 16, \"doof\", 45+9, b}",
				"6[-6 * -a] + 34",
				"(23 + * 6",
				"(23 + * / 6)",
				"z * y.98",		// a double literal following to a variable
				"w <- 76[56].(aha)"	// non-identifier following to a '.' operator
		};
		String[] lineTests = new String[] {
				"foreach i in {17+ 9, -3, pow(17, 11.4, -8.1), \"doof\"}",
				"for k <- 23/4 to pow(2, 6) " + Syntax.getKeyword("stepFor") + " 2",
				"foreach val in 34 + 8 19 true \"doof\"",
				"foreach thing in 67/5 + 8, \"fun\" + \"ny\", pow(67, 3)",
				"if not isNice",
				"while answer == 'J' or answer == 'N'",
				"until (value < 15.5) and not (length(array)*2 >= 5) or a <> b",
				"leave",
				"leave 3",
				"RETURN",
				"return {\"dull\", \"silly\", \"braindead\", \"as thick as the wall\"}",
				"exit 5",
				"throw \"all wrong here\"",
				"input",
				"INPUT a, b[3]",
				"input \"prompt\" date.year, date.month",
				"output",
				"OUTPUT 17, a*z, 18 + \" km/h\""
		};
		String[] negationTests = new String[] {
				"a < 5 && b >= c || isDone",
				"not hasFun(person)",
				"q >= 5",
				"length({8, 34, 9.7}) = 4",
				"2 != sqrt(8)",
				"true",
				"not (28 - b * 13 > 4.5 / sqrt(23) * x)"
		};
		
		TypeRegistry types = new TypeRegistry();
		
		long timeTotal = 0L;
		long maxTime = -1L;
		long minTime = -1L;
		int nTests = 0;
		for (String test: exprTests) {
			try {
				System.out.println("===== " + test + " =====");
				long startTime = System.currentTimeMillis();
				StringList tokens = Syntax.splitLexically(test, true);
				List<Expression> exprs = Expression.parse(tokens, /*sepas/**/ /**/null/**/, (short)0);
				long endTime = System.currentTimeMillis();
				int i = 1;
				for (Expression expr: exprs) {
					StringList vars1 = new StringList();
					StringList vars2 = new StringList();
					System.out.println(i + ": " + expr.toString());
					System.out.println(i + ": " + expr.translate(Expression.verboseOperators));
					System.out.println(i + ": " + expr.translate(flatPriorities));
					System.out.println(i + ": " + expr.translate(someAsFunctionOrMethod));
					System.out.println(i + ": " + expr.translate(pascalOperators));
					boolean okay = expr.gatherVariables(vars1, vars2, false);
					System.out.println("Assigned: " + vars1.toString() +
							", used: " + vars2.toString() + (okay ? "" : ", errors"));
					Type exprType = expr.inferType(types, true);
					if (exprType != null) {
						System.out.println(exprType.toString(true) + " (" + expr.isDataTypeSafe + ")");
					}
					else {
						System.out.println("No type retrieved");
					}
					i++;
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
			} catch (SyntaxException exc) {
				System.err.println(exc.getMessage() + " at " + exc.getPosition());
			}
		}
		System.out.println("---------------------------------------------------------------------------------------");
		System.out.println("sum = " + timeTotal + ", ave = " + (timeTotal *1.0 / nTests) + ", min = " + minTime + ", max = " + maxTime);
		System.out.println("---------------------------------------------------------------------------------------");
		
		for (String line: lineTests) {
			System.out.println("===== " + line + " =====");
			StringList errors = new StringList();
			StringList vars1 = new StringList();
			StringList vars2 = new StringList();
			StringList vars3 = new StringList();
			Line aLine = Line.parse(line, ~0, null, errors);
			System.out.println(aLine);
			System.err.println(errors.getText());
			boolean okay = aLine.gatherVariables(vars1, vars2, vars3);
			System.out.println("Assigned: " + vars1.toString() +
					", declared: " + vars2.toString() + 
					", used: " + vars3.toString() + (okay ? "" : ", errors"));
		}
		
		for (String test: negationTests) {
			System.out.println("===== " + test + " =====");
			long startTime = System.currentTimeMillis();
			StringList tokens = Syntax.splitLexically(test, true);
			try {
				List<Expression> exprs = Expression.parse(tokens, null, (short)0);
				long endTime = System.currentTimeMillis();
				Expression cond = exprs.get(0);
				System.out.println(cond.toString());
				Type exprType = cond.inferType(types, false);
				if (exprType != null) {
					System.out.println(exprType.toString(true));
				}
				else {
					System.out.println("No type retrieved");
				}
				Expression neg = Expression.negateCondition(cond, false);
				System.out.print(neg.toString() + " <-> ");
				exprType = neg.inferType(types, false);
				System.out.println(Expression.negateCondition(neg, false));
				if (exprType != null) {
					System.out.println(exprType.toString(true));
				}
				else {
					System.out.println("No type retrieved");
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
			} catch (SyntaxException exc) {
				System.err.println(exc.getMessage() + " at " + exc.getPosition());
			}
		}
		System.out.println("---------------------------------------------------------------------------------------");
		System.out.println("sum = " + timeTotal + ", ave = " + (timeTotal *1.0 / nTests) + ", min = " + minTime + ", max = " + maxTime);
		System.out.println("---------------------------------------------------------------------------------------");
		
		System.out.println(types);
	}

}
