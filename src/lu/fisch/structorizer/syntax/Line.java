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
import java.util.Vector;

import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.Instruction;
import lu.fisch.structorizer.io.Ini;
import lu.fisch.structorizer.syntax.Expression.NodeType;
import lu.fisch.utils.StringList;

/******************************************************************************************************
 *
 *      Author:         kay
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
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

/**
 * Represents the syntacic structure of a line of an {@link Element}
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
		/** Line could not be parsed, 0 expr. */
		LT_RAW,
		/** Assignment without declaration, 1 expr. */
		LT_ASSIGNMENT,
		/** Input instruction, >= 1 expr. (1st = prompt string) */
		LT_INPUT,
		/** Output instruction, >= 0 expr. */
		LT_OUTPUT,
		/** Condition of an Alternative or loop, 1 expr */
		LT_CONDITION,
		/** For loop head, 3 expr. */
		LT_FOR_LOOP,
		/** Foreach loop head, 2 expr. */
		LT_FOREACH_LOOP,
		/** Procedure call, 1 expr */
		LT_PROC_CALL,
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
		/** Catch clause (TRY block), 1 expr. */
		LT_CATCH,
		/** Type definition, 1 expr. (= type name)  */
		LT_TYPE_DEF,
		/** Constant definition, 1 expr. */
		LT_CONST_DEF,
		/** Variable declaration, 2 expr. (type, variable name or assignment */
		LT_VAR_DECL
	};
	
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
		lineStartsToTypes.put("var", LineType.LT_VAR_DECL);
		lineStartsToTypes.put("dim", LineType.LT_VAR_DECL);
		lineStartsToTypes.put("const", LineType.LT_CONST_DEF);
		lineStartsToTypes.put("type", LineType.LT_TYPE_DEF);
		
	}
	
	private LineType type;
	private Expression[] expressions;
	
	public Line(LineType _type, Expression[] _expressions)
	{
		type = _type;
		expressions = _expressions;
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
		case LT_CATCH:
		case LT_CONDITION:
		case LT_CONST_DEF:
		case LT_EXIT:
		case LT_PROC_CALL:
		case LT_THROW:
		case LT_INPUT:
			count = 1;
			break;
		case LT_FOR_LOOP:
		case LT_FOREACH_LOOP:
			count = 2;
			break;
		case LT_LEAVE:
		case LT_OUTPUT:
		case LT_RAW:
		case LT_RETURN:
		case LT_TYPE_DEF:
		case LT_VAR_DECL:
			count = 0;
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
		case LT_PROC_CALL:
		case LT_VAR_DECL:
			count = 1;
			break;
		case LT_FOR_LOOP:
			count = 3;
			break;
		case LT_INPUT:
		case LT_OUTPUT:
		case LT_FOREACH_LOOP:
		case LT_RAW:
			count = Integer.MAX_VALUE;
			break;
		case LT_TYPE_DEF:
			count = 0;
			break;
		}
		return count;		
	}
	
	/**
	 * Tries to parse the given (unbroken) {@code textLine} into a Line structure
	 * @param tokens - the tokenized text line (without operator unification!)
	 * @param expectedType - optionally an expected line type (may depend on the
	 * Element type or {@code null}. If given, controls the validity.
	 * @return a Line object or {@code null} (e.g. in case of an empty line)
	 * @throws SyntaxException if there is a syntactic error in the text
	 * @see #parse(String, LineType, StringList)
	 */
	public static Line parse(StringList tokens, LineType expectedType) throws SyntaxException
	{
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
						tokens.insert(keyword, start);
					}
					else {
						start--;
					}
				}
			}
		}
		tokens.trim();
		Syntax.unifyOperators(tokens, false);
		// Now check the start token against prefix keywords
		// FIXME: We should get rid of postfix keywords
		if (!tokens.isEmpty()) {
			token0 = tokens.get(0);
			for (String key: Syntax.keywordSet()) {
				LineType lntp = lineStartsToTypes.get(key);
				if (lntp != null) {
					String keyword = Syntax.getKeyword(key);
					if (token0.equals(keyword)) {
						lType = lntp;
						// Initial keyword is identified, so we can remove it
						tokens.remove(0);
						break;
					}
				}
			}
		}
		if (lType == null && tokens.contains("<-")) {
			lType = LineType.LT_ASSIGNMENT;
		}
		// The type may still be unidentified (e.g. if no optional pre-key was used)
		if (lType != null && expectedType != null && lType != expectedType) {
			// Something is wrong here
			throw new SyntaxException("wrongLineType",
					new String[] {lType.toString(), expectedType.toString()},
					0, null);
		}
		else if (lType == null) {
			// Adopt the expected type if imposed
			lType = expectedType;
		}
		// Identify the separators and extract the expressions
		lType = extractExpressions(tokens, lType, exprs);
		return new Line(lType, exprs.toArray(new Expression[exprs.size()]));
	}

	/**
	 * Extract the expressions from the unprefixed token list {@code _tokens} according
	 * to the expected or detected line type {@code _type} and gather them in {@code _exprs}.
	 * Consider the known variable names {@code _varNames} if given.
	 * @param _tokens - The token list without an identified leading keyword (this should
	 * reflect in the given {@code _type}). May still contain blanks and non-unified operators.
	 * @param _type - the {@link LineType} as far as already detected or expected (or {@code null})
	 * @param _exprs - the {@link ArrayList} to which the parsed expressions are to be added.
	 * @return the eventual line type
	 * @throws SyntaxException if there are syntactic errors
	 */
	private static LineType extractExpressions(StringList _tokens, LineType _type, ArrayList<Expression> _exprs) throws SyntaxException {
		if (_type == null) {
			_type = LineType.LT_RAW;
		}
		Syntax.unifyOperators(_tokens, false);
		switch (_type) {
		case LT_ASSIGNMENT:
		case LT_CASE:
		case LT_CONDITION:
		case LT_CONST_DEF:
		case LT_EXIT:
		case LT_THROW:
		case LT_PROC_CALL:
			// We expect exactly one expression and no further keywords in general
			{
				List<Expression> parsed = Expression.parse(_tokens, null);
				if (_tokens.isEmpty() && parsed.size() == 1) {
					_exprs.add(parsed.get(0));
				}
				else {
					System.err.println("Wrong parsing result or remainder: "
							+ parsed.toString() + " | " + _tokens.concatenate(null));
				}
			}
			break;
		case LT_FOREACH_LOOP:
		case LT_FOR_LOOP:
			// This might still be a loop of the respective other kind
			{
				int ixIn = _tokens.indexOf(Syntax.getKeyword("postForIn"), 0, !Syntax.ignoreCase);
				int ixTo = _tokens.indexOf(Syntax.getKeyword("postFor"), 0, !Syntax.ignoreCase);
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
							Math.max(ixTo, ixIn), null);
				}
				int ix0 = isForIn ? ixIn : ixTo;
				// This should contain the first expression (an assignment or a variable name, respectively)
				StringList tokens0 = _tokens.subSequence(0, ix0);
				List<Expression> parsed = Expression.parse(tokens0, null);
				if (tokens0.isEmpty() && parsed.size() == 1) {
					Expression varSpec = parsed.get(0);
					if (isForIn && varSpec.type != NodeType.VARIABLE
							|| !isForIn && (varSpec.type != NodeType.OPERATOR || !varSpec.text.equals("<-"))) {
						throw new SyntaxException("Wrong loop variable specification: " + parsed.toString(), 0);
					}
					_exprs.add(parsed.get(0));
				}
				else {
					System.err.println("Wrong parsing result or remainder: "
							+ parsed.toString() + " | " + tokens0.concatenate(null));
				}
				// The tail contains either end value and step clause or the item list
				_tokens.remove(0, ix0+1);
				_tokens = _tokens.trim();
				if (isForIn) {
					// Extract the item list (may be an array initializer, a variable or a sequence of expressions)
					parsed = Expression.parse(_tokens, null);
					if (_tokens.isEmpty() && !parsed.isEmpty()) {
						_exprs.addAll(parsed);
					}
					else {
						throw new SyntaxException("Incorrect item list: " + _tokens.concatenate(null), 0);
					}
				}
				else {
					// First extract the end value expression
					String stepKey = Syntax.getKeyword("stepFor");
					parsed = Expression.parse(_tokens, StringList.getNew(stepKey));
					if (parsed.size() == 1) {
						_exprs.addAll(parsed);
					}
					else {
						throw new SyntaxException("Wrong end value specification: " + parsed.toString(), 0);
					}
					// Then get the increment, which ought to be an integral literal
					if (!_tokens.isEmpty()) {
						if (_tokens.get(0).equals(stepKey)) {
							_tokens.remove(0);
							parsed = Expression.parse(_tokens, null);
							if (parsed.size() == 1) {
								_exprs.addAll(parsed);
								// TODO we might check for signed or unsigned literal
							}
							else {
								throw new SyntaxException("Wrong step value specification: " + parsed.toString(), 0);
							}
						}
						else {
							throw new SyntaxException("Wrong step syntax: " + _tokens.concatenate(null), 0);
						}
					}
				}
			}
			break;
		case LT_INPUT:
			{
				List<Expression> parsed = Expression.parse(_tokens, null);
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
				List<Expression> parsed = Expression.parse(_tokens, null);
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
					List<Expression> parsed = Expression.parse(_tokens, null);
					if (_tokens.isEmpty()) {
						_exprs.addAll(parsed);
					}
				}
				catch (SyntaxException exc) {}
			}
		case LT_OUTPUT:
			// Just extract all available expressions
			_exprs.addAll(Expression.parse(_tokens, null));
			break;
		case LT_TYPE_DEF:
		{
			/* TODO Do we need to describe type definition syntax recursively
			 * (semicolons, component declarations, value definitions) or can we
			 * omit it and have the original string parsed in the traditional way?
			 */
		}
			break;
		case LT_VAR_DECL:
		{
			// TODO We might want to extract the mere assignment part - if any
			int posAsgn = _tokens.indexOf("<-");
			if (posAsgn > 0) {
				// Keyword "var" should already have been cut off
				StringList left = _tokens.subSequence(0, posAsgn);
				_tokens.remove(0, posAsgn);	// The right part
				left.removeAll(" ");
				// FIXME: For now we will just call the Instruction method
				String varName = Instruction.getAssignedVarname(left, true);
				_tokens.insert(Syntax.splitLexically(varName, true), 0);
				List<Expression> parsed = Expression.parse(_tokens, null);
				if (parsed.size() == 1) {
					_exprs.add(parsed.get(0));
				}
			}
		}
			break;
		default:
			break;
		}
		return _type;
	}

	/**
	 * Tries to parse the given (unbroken) {@code textLine} into a Line structure
	 * @param textLine - the (unbroken) text line as string
	 * @param expectedType - optionally an expected line type (may depend on the
	 * Element type or {@code null}. If given, controls the validity.
	 * @param errors - all detected errors and warnings will be appended to this {@link StringList}.
	 * @return a Line object. In case of syntactic errors, a Line of type {@link LineType#LT_RAW}
	 * would be returned.
	 */
	public static Line parse(String textLine, LineType expectedType, StringList errors)
	{
		StringList tokens = Syntax.splitLexically(textLine, true);
		if (!tokens.isEmpty()) {
			try {
				return parse(tokens, null);
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
	
	public static void main(String[] args) {
		//Syntax.loadFromINI();
		
		String[] exprTests = new String[] {
				// "good" expressions
//				"a <- 7 * (15 - sin(1.3))",
//				"a <- 7 * (15 - sin(b))",
//				"a[i+1] <- { 16, \"doof\", 45+9, b}",
//				"7 * (15 - sin(1.3)), { 16, \"doof\", 45+9, b}",
//				"7 * (15 - pow(-18, 1.3)) + len({ 16, \"doof\", 45+9, b})",
//				"rec <- Date{2020, a + 4, max(29, d)}",
//				"rec <- Date{year: 2020, month: a + 4, day: max(29, d)}",
//				"test[top-1]",
//				"25 * -a - b",
//				"a < 5 && b >= c || isDone",
//				"not hasFun(person)",
//				"28 - b % 13 > 4.5 / sqrt(23) * x",
//				"*p <- 17 + &x",
				"a & ~(17 | 86) ^ ~b | ~c | ~1",
				// Defective lines
				"7 * (15 - sin(1.3)) }, { 16, \"doof\", 45+9, b}",
				"6[-6 * -a] + 34",
				"(23 + * 6",
				"(23 + * / 6)",
		};
		String[] lineTests = new String[] {
				"foreach i in {17+ 9, -3, pow(17.4, -8.1), \"doof\"}",
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
		
		for (String test: exprTests) {
			try {
				StringList tokens = Syntax.splitLexically(test, true);
				System.out.println("===== " + test + " =====");
				List<Expression> exprs = Expression.parse(tokens, /*sepas/**/ /**/null/**/);
				int i = 1;
				for (Expression expr: exprs) {
					System.out.println(i + ": " + expr.toString());
					System.out.println(i + ": " + expr.translate(Expression.verboseOperators));
					i++;
				}
			} catch (SyntaxException exc) {
				System.err.println(exc.getMessage() + " at " + exc.getPosition());
			}
		}
		
		for (String line: lineTests) {
			System.out.println("===== " + line + " =====");
			StringList errors = new StringList();
			Line aLine = Line.parse(line, null, errors);
			System.out.println(aLine);
			System.err.println(errors.getText());
		}
		
		for (String test: negationTests) {
			StringList tokens = Syntax.splitLexically(test, true);
			System.out.println("===== " + test + " =====");
			try {
				List<Expression> exprs = Expression.parse(tokens, null);
				Expression cond = exprs.get(0);
				System.out.println(cond.toString());
				Expression neg = Expression.negateCondition(cond, false);
				System.out.print(neg.toString() + " <-> ");
				System.out.println(Expression.negateCondition(neg, false));
			} catch (SyntaxException exc) {
				System.err.println(exc.getMessage() + " at " + exc.getPosition());
			}
		}
	}

}
