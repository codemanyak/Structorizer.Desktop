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
 *      Kay Gürtzig     2021-12-09      Method parse discarded in favour of LineParser
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

import java.util.HashMap;
import java.util.List;

import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.syntax.Expression.Operator;
import lu.fisch.utils.StringList;

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
	private String parserError = null;
	
	
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
	
	public Line(LineType _type, String _errorMessage)
	{
		type = _type;
		expressions = null;
		parserError = _errorMessage;
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
	 * 
	 * @param _index - the index of the requested expression (0 .. {@link #getExprCount()}-1)
	 * @return the requested expression as syntax tree if available, {@code null}
	 *     otherwise.
	 * 
	 * @see #getExprCount()
	 * @see #getType()
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
	 * @return the number of stored expression syntax trees.
	 * 
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
	 * Yields the associated data type.
	 * 
	 * @param _explicitOnly - set this {@code true} if you are only interested in
	 *    an explicitly assigned type here (e.g. from a type definition or variable
	 *    declaration), otherwise the attached expressions will be asked for their
	 *    data type, where the first expression associated with a data type will
	 *    determine the result.
	 * @return the explicitly associated data type (e.g. in case of a type definition
	 *    or variable declaration) or the data type associated to the involved
	 *    expressions (without an attempt to infer the type, which would require a
	 *    {@link TypeRegistry}.
	 *    
	 * @see #getOrInferDataType(TypeRegistry)
	 */
	public Type getDataType(boolean _explicitOnly)
	{
		Type dType = dataType;
		if (dType == null && !_explicitOnly && expressions != null) {
			for (Expression expr: expressions) {
				if ((dType = expr.getDataType()) != null) {
					return dType;
				}
			}
		}
		return dType;
	}
	
	/**
	 * Yields the explicitly associated data type (e.g. from a type definition or
	 * variable declaration) or gets or infers the data type from the contained
	 * expressions where the first responding expression determines the result.
	 *
	 * @param _registry - the {@link TypeRegistry} to retrieve the data types from
	 * @return the explicitly associated data type (e.g. in case of a type definition
	 *    or variable declaration) or the data type inferred from to the involved
	 *    expressions
	 *
	 * @see #getDataType(boolean)
	 */
	public Type getOrInferDataType(TypeRegistry _registry)
	{
		Type dType = dataType;
		if (dataType == null && expressions != null) {
			for (Expression expr: expressions) {
				if ((dType = expr.getDataType()) != null 
						|| (dType = expr.inferType(_registry, false)) != null) {
					break;
				}
			}
		}
		return dType;
	}
	
	/**
	 * Ensures that all valid type references get properly updated (with respect
	 * to possibly replaced underlying types in the associated or given
	 * {@link TypeReference}).
	 */
	public void updateTypeReferences(TypeRegistry _dataTypes) {
		if (dataType != null) {
			Type refType = (dataType.registry == null ? _dataTypes : dataType.registry).
					getType(dataType.getName());
			if (refType != null) {
				dataType = refType;
			}
		}
		if (expressions != null) {
			for (int i = 0; i < expressions.length; i++) {
				expressions[i].clearDataTypes(false);
				expressions[i].inferType(_dataTypes, true);
			}
		}
	}
	
	/**
	 * @return a parser error message if no line structure could be built due to
	 *     a lexical or syntactical error, otherwise {@code null}
	 */
	public String getParserError()
	{
		return parserError;
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
		if (this.parserError != null) {
			sb.append("(");
			sb.append(this.parserError);
			sb.append(")");
		}
		return sb.toString();
	}
	
	/**
	 * Recursively retrieves the names of all variables (and constants) occurring
	 * in this expression, be it as initialisation/assignment target, as declaration
	 * subject, or as value source.
	 * 
	 * @param assignedVars - {@link StringList} to gather names of assignment targets,
	 *     or {@code null} (if there is no interest in them)
	 * @param declaredVars - {@link StringList} to gather names of variables that are
	 *     explicitly declared
	 * @param usedVars - {@link StringList} to gather names of variables that are merely
	 *     used as value source.
	 * @param problems - a {@link StringList} to gather syntax problem descriptions, or
	 *     {@code null}. A possible problem might e.g. be a duplicate declaration.
	 * @return {@code false} if the line consistency or expression structure is wrong
	 */
	public boolean gatherVariables(StringList assignedVars, StringList declaredVars, StringList usedVars, StringList problems)
	{
		boolean okay = true;
		switch (this.type) {
		case LT_ASSIGNMENT:
		case LT_CATCH:
		case LT_CASE:
		case LT_CONDITION:
		case LT_ROUTINE_CALL:
			if (expressions != null && expressions.length == 1) {
				okay = expressions[0].gatherVariables(assignedVars, usedVars,
						this.type == LineType.LT_CATCH);
			}
			else {
				okay = false;
			}
			break;
		case LT_EXIT:
		case LT_LEAVE:
		case LT_RETURN:
		case LT_THROW:
		case LT_OUTPUT:
			if (expressions != null) {
				for (Expression expr: expressions) {
					okay = expr.gatherVariables(assignedVars, usedVars, false) && okay;
				}
			}
			break;
		case LT_FOREACH_LOOP:
		case LT_FOR_LOOP:
			if (expressions != null && expressions.length > 1) {
				for (int i = 0; i < expressions.length; i++) {
					boolean isLeft = i == 0 && this.type == Line.LineType.LT_FOREACH_LOOP;
					okay = expressions[i].gatherVariables(assignedVars, usedVars, isLeft) && okay;
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
		case LT_CONST_DEF:
		case LT_CONST_FUNCT_CALL:
		case LT_VAR_INIT:
		case LT_VAR_DECL:
			if (this.expressions != null && this.expressions.length >= 1) {
				Expression firstExpr = this.expressions[0];
				if (firstExpr.type == Expression.NodeType.OPERATOR
						&& ("<-".equals(firstExpr.text) || ":=".equals(firstExpr.text))) {
					// Variables that are both explicitly declared and assigned
					StringList targets = new StringList();
					okay = firstExpr.gatherVariables(targets, usedVars, false);
					if (declaredVars != null) {
						for (int i = 0; i < targets.count(); i++) {
							if (!declaredVars.addIfNew(targets.get(i)) && problems != null) {
								problems.add("declaration.duplicate: " + targets.get(i));
							}
						}
					}
					if (assignedVars != null) {
						assignedVars.addIfNew(targets);
					}
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
				" 23, \"hserjj\", 'i', 'ojj8f', {746, #, rod}",
				"(23 + * / 6)",
				"z * y.98",	// a double literal following to a variable
				"w <- 76[56].(aha)"		// non-identifier following to a '.' operator
		};
		String[] lineTests = new String[] {
				"§FOREACH§ i §IN§ {17+ 9, -3, pow(17, 11.4, -8.1), \"doof\"}",
				"§FOR§ k <- 23/4 §TO§ pow(2, 6) §STEP§ 2",
				"§FOREACH§ val §IN§ 34 + 8 19 true \"doof\"",
				"§FOREACH§ thing §IN§ 67/5 + 8, \"fun\" + \"ny\", pow(67, 3)",
				"§COND§ !isNice",
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
				"§OUTPUT§ 17, a*z, 18 + \" km/h\""
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
				TokenList tokens = new TokenList(test, true);
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
//					int count1 = expr.getNodeCount();
//					int count2 = expr.getTokenCount(true);
//					int count3 = expr.getTokenCount(false);
//					if (count1 != count2 || count1 != count3) {
//						System.err.println(String.format("# nodes: %d, # tokens in: %d, # tokens out: %d",
//								count1, count2, count3));
//						StringBuilder sb = new StringBuilder();
//						expr.traverseInOrder(sb);
//						System.err.println(sb.toString());
//					}
//					else {
//						System.out.println(String.format("# nodes: %d, # tokens in: %d, # tokens out: %d",
//							count1, count2, count3));
//					}
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
			//Line aLine = Line.parse(line, ~0, null, errors);
			Line aLine = LineParser.getInstance().parse(line, null, 0, types);
			System.out.println(aLine);
			System.err.println(errors.getText());
			boolean okay = aLine.gatherVariables(vars1, vars2, vars3, null);
			System.out.println("Assigned: " + vars1.toString() +
					", declared: " + vars2.toString() + 
					", used: " + vars3.toString() + (okay ? "" : ", errors"));
		}
		
		for (String test: negationTests) {
			System.out.println("===== " + test + " =====");
			long startTime = System.currentTimeMillis();
			TokenList tokens = new TokenList(test, true);
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
