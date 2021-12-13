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
 *      Author:         Bob Fisch
 *
 *      Description:    Auxiliary class to check function syntax and to separate signature parts.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch                       First Issue
 *      Kay Gürtzig     2015-10-27      For performance reasons, now stores name and parsed parameters
 *      Kay Gürtzig     2015-11-13      KGU#2 (Enhancement #9): No longer automatically renames to lowercase
 *      Kay Gürtzig     2015-12-12      KGU#106: Parameter splitting mended (using enhancement #54 = KGU#101)
 *      Kay Gürtzig     2016-12-22      KGU#311: New auxiliary method getSourceLength()
 *      Kay Gürtzig     2017-01-29      Enh. #335: Enhancements for better type analysis
 *      Kay Gürtzig     2020-10-16      Bugfix #874: Too strict identifier check w.r.t. non-ascii letters
 *      Kay Gürtzig     2020-08-12      Enh. #800: Started to redirect syntactic analysis to class Syntax
 *      Kay Gürtzig     2020-11-01      Issue #800: Moved testIdentifier to Syntax and countChar to BString,
 *                                      indentation aligned
 *      Kay Gürtzig     2020-11-02      Issue #800: Completely revised, now using Syntax and Expression
 *      Kay Gürtzig     2021-03-05      Bugfix #961: Method isFunction() extended (for method tests)
 *      Kay Gürtzig     2021-06-06      sgn function added to knownResultTypes
 *      Kay Gürtzig     2021-12-10      New result type mapping for DiagramController routines added
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///

import java.lang.reflect.Method;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import lu.fisch.diagrcontrol.DiagramController;
//import lu.fisch.structorizer.syntax.Expression;
//import lu.fisch.structorizer.syntax.Syntax;
//import lu.fisch.structorizer.syntax.SyntaxException;
//import lu.fisch.structorizer.syntax.Type;
import lu.fisch.utils.StringList;

/**
 * Represents and analyzes an expression or declaration looking like a subprogram call.
 * May confirm function syntax resemblance and report name and parameter strings.
 * @author robertfisch
 */
public class Function
{
	// START KGU#332 2017-01-29: Enh. #335 - result type forecast
	/**
	 * Maps signatures of built-in functions and procedures (see
	 * {@link Executor#builtInFunctions}) to Java type names (where "void" indicates
	 * a procedure).<br/>
	 * The signature syntax is: {@code "<name>#<arg_count>"}.
	 */
	private static final Map<String, String> knownResultTypes;
	static {
		knownResultTypes = new HashMap<String,String>();
		//knownResultTypes.put("abs#1", "numeric");
		//knownResultTypes.put("max#2", "numeric");
		//knownResultTypes.put("min#2", "numeric");
		knownResultTypes.put("sgn#1", "int");
		knownResultTypes.put("round#1", "int");
		knownResultTypes.put("ceil#1", "double");
		knownResultTypes.put("floor#1", "double");
		knownResultTypes.put("sqr#1", "double");
		knownResultTypes.put("sqrt#1", "double");
		knownResultTypes.put("exp#1", "double");
		knownResultTypes.put("log#1", "double");
		knownResultTypes.put("pow#2", "double");
		knownResultTypes.put("cos#1", "double");
		knownResultTypes.put("sin#1", "double");
		knownResultTypes.put("tan#1", "double");
		knownResultTypes.put("acos#1", "double");
		knownResultTypes.put("asin#1", "double");
		knownResultTypes.put("atan#1", "double");
		knownResultTypes.put("toRadians#1", "double");
		knownResultTypes.put("toDegrees#1", "double");
		knownResultTypes.put("random#1", "int");
		knownResultTypes.put("length#1", "int");
		knownResultTypes.put("lowercase#1", "String");
		knownResultTypes.put("uppercase#1", "String");
		knownResultTypes.put("pos#2", "int");
		knownResultTypes.put("copy#3", "string");
		knownResultTypes.put("trim#1", "String");
		knownResultTypes.put("ord#1", "int");
		knownResultTypes.put("chr#1", "char");
		knownResultTypes.put("isArray#1", "boolean");
		knownResultTypes.put("isChar#1", "boolean");
		knownResultTypes.put("isBool#1", "boolean");
		knownResultTypes.put("inc#2", "void");
		knownResultTypes.put("dec#2", "void");
		knownResultTypes.put("randomize#0", "void");
		knownResultTypes.put("insert#3", "void");
		knownResultTypes.put("delete#3", "void");
		knownResultTypes.put("fileOpen#1", "int");
		knownResultTypes.put("fileCreate#1", "int");
		knownResultTypes.put("fileAppend#1", "int");
		knownResultTypes.put("fileEOF#1", "boolean");
		//knownResultTypes.put("fileRead", "Object");
		knownResultTypes.put("fileReadChar#1", "char");
		knownResultTypes.put("fileReadInt#1", "int");
		knownResultTypes.put("fileReadDouble#1", "double");
		knownResultTypes.put("fileReadLine#1", "String");
		knownResultTypes.put("fileWrite#2", "void");
		knownResultTypes.put("fileWriteLine#2", "void");
		knownResultTypes.put("fileClose#1", "void");
		// START KGU#790 2020-11-01: Issue #800
		knownResultTypes.put("max#2", "double");
		knownResultTypes.put("min#2", "double");
		// END KGU#790 2020-11-01
	}
	// END KGU#332 2017-01-29
	// START KGU#448/KGU#790 2021-12-10: Issues #443, #800
	/**
	 * Maps signatures of currently enabled DiagramController functions and procedures
	 * to Java type names (where "void" indicates a procedure).<br/>
	 * This map is supposed to be updated whenever the set of enabled controllers changes.
	 * The signature syntax is: {@code "<name>#<arg_count>"}.
	 * 
	 * @see #setupControllerRoutineMap(Set)
	 */
	private static HashMap<String, String> controllerRoutineTypes =
			new HashMap<String, String>();
	// END KGU#448/KGU#790 2021-12-10

	private String str = new String();		// The original string this is derived from
	// START KGU#790 2020-11-02: Issue #800 No longer needed
	// START KGU#56 2015-10-27: Performance improvement approach (bug fixed 2015-11-09)
	//private StringList parameters = null;	// parameter strings as split by commas
	//private boolean isFunc = false;			// basic syntactic plausibility check result
	//private String name = null;				// The string before the opening parenthesis
	// END KGU#56 2015-10-27
	private Expression expr = null;			// The parsed input string
	// END KGU#790 2020-11-02

	/**
	 * Parses the expression held in string {@code expr} and tries to build a
	 * Function object from it. The resulting object can be asked if it
	 * actually represents a valid function (or method) invocation.
	 * 
	 * @param exprStr - a linearised expression in Structorizer syntax as string
	 * 
	 * @see #Function(Expression)
	 * @see #isFunction()
	 * @see #isMethod()
	 */
	public Function(String exprStr)
	{
		this.str = exprStr.trim();
		// START KGU#790 2020-11-02: Issue #800 - totally redesigned
//		// START KGU#56 2015-10-27
//		// START KGU#332 2017-01-29: Enh. #335 We need a more precise test
//		//int posLP = str.indexOf("(");
//		//this.isFunc =
//		//		posLP < str.indexOf(")") && posLP >=0 &&
//		//        countChar(str,'(') == countChar(str,')') &&
//		//        str.endsWith(")");
//		this.isFunc = isFunction(this.str);
//		// END KGU#332 2017-01-29
//		if (this.isFunc)
//		{
//			int posLP = str.indexOf("(");
//			// START KGU#2 (#9) 2015-11-13: In general, we don't want to flatten the case!
//			//this.name = str.substring(0, posLP).trim().toLowerCase();
//			this.name = str.substring(0, posLP).trim();
//			// END KGU#2 (#9) 2015-11-13
//			String params = str.substring(posLP+1, str.length()-1).trim();
//			if (!params.equals(""))
//			{
//				// START KGU#106 2015-12-12: Face nested function calls with comma-separated arguments!
//				//this.parameters = StringList.explode(params, ",");
//				this.parameters = Element.splitExpressionList(params, ",");
//				// END KGU#106 2015-12-12
//			}
//            // START KGU#341 2017-02-06
//            String nPars = Integer.toString(this.parameters.count());
//            for (String key: knownResultTypes.keySet()) {
//                String[] parts = key.split("#");
//                if (name.equalsIgnoreCase(parts[0]) && parts[1].equals(nPars)) {
//                    this.caseAlignedName = parts[0];
//                    break;
//                }
//            }
//            // END KGU#341 2017-02-06
//		}
//		// END KGU#56 2015-10-27
		StringList tokens = Syntax.splitLexically(exprStr, true);
		try {
			LinkedList<Expression> exprs = Expression.parse(tokens, null, (short)0);
			if (exprs.size() == 1) {
				if (isFunction(exprs.get(0), true)) {
					expr = exprs.get(0);
				}
			}
		} catch (SyntaxException exc) {
			// Not a function, it seems.
		}
		// END KGU#790 2002-11-02
	}
	
	/**
	 * Formally creates a Function object for the given {@link Expression}
	 * {@code expr}, but this does not necessarily mean it is a valid function
	 * call - you will have to check via {@link #isFunction()} or {@link #isFunction()}
	 * to be sure (and before trying to retrieve name and arguments or the like).
	 * @param expr - the syntax tree to be form this object for.
	 */
	public Function(Expression expr)
	{
		this.str = expr.toString();
		if (isFunction(expr, true)) {
			this.expr = expr;
		}
	}
	

// START KGU 2020-11-01: Moved to lu.fisch.utils.BString
//    // This is just a very general string helper function 
//    public static int countChar(String s, char c)
//    {
//    	int res = 0;
//    	for (int i=0; i<s.length(); i++)
//    	{
//    		if (s.charAt(i)==c)
//    		{
//    			res++;
//    		}
//    	}
//    	return res;
//    }
// END KGU 2020-11-01

	/**
	 * For historical reasons, this method does not differentiate between
	 * function and method. So if you want to know whether this is a pure
	 * function, you will also have to check that {@link #isMethod()} does
	 * <i>not</i> return {@code true}.
	 * 
	 * @return {@code true} if this object represents a syntactically correct
	 * function or method call, i.e. one of:
	 * <ul>
	 * <li>{@code <functionname>(<arg>,...)}</li>
	 * <li>{@code <object>.<methodname>(<arg>, ...) }</li>
	 * </ul>
	 * 
	 * @see #isMethod()
	 * @see #isFunction(String, boolean)
	 * @see #isFunction(Expression, boolean)
	 */
	public boolean isFunction()
	{
		return expr != null;
	}

	// START KGU#790 2020-11-02: New for issue #800
	/**
	 * Checks that this represents a method call (OOP); will return {@code false}
	 * if this is a mere function (not-OO).
	 * 
	 * @return {@code true} if this object represents a syntactically correct
	 * method call, i.e. something like {@code <object>.<methodname>(<arg>, ...) }.
	 * 
	 * @see #isFunction()
	 * @see #isFunction(String, boolean)
	 * @see #isFunction(Expression, boolean)
	 */
	public boolean isMethod()
	{
		return expr != null && expr.isMethodCall();
	}
	
	/**
	 * Checks the given {@link Expression} for correct function or method
	 * call syntax.
	 * 
	 * @param expr - an {@link Expression} to be checked for method
	 * or function call syntax.
	 * @param acceptQualifiers - whether a qualified name (i.e an explicit method call)
	 *  is okay
	 * @return {@code true} if this is either a method or function call
	 * 
	 * @see #isFunction(String, boolean)
	 */
	// START KGU#959 2021-03-05: Issue #961 We need a possibility to detect method calls
	//public static boolean isFunction(Expression expr)
	public static boolean isFunction(Expression expr, boolean acceptQualifiers)
	// END KGU#959 2021-03-05
	{
		// START KGU#959 2021-03-05: Issue #961 We need a possibility to detect method calls
		//return expr.isFunctionCall() || expr.isMethodCall();
		return expr.isFunctionCall() || acceptQualifiers && expr.isMethodCall();
		// END KGU#959 2021-03-05
	}
	// END KGU#790 2020-11-02

	
	// START KGU#332 2017-01-29: Enh. #335
	/**
	 * Tests whether the passed-in expression string {@code expr} may represent a
	 * routine (or method) call, i.e., consists of an identifier or component access
	 * followed by a parenthesized comma-separated list of argument expressions.
	 * @param expr - an expression as string
	 * @param acceptQualifiers - whether a qualified name (i.e an explicit method call)
	 *  is okay
	 * @return {@code true} if the expression has got function or method call syntax
	 * @see #isFunction(Expression, boolean)
	 */
	// START KGU#959 2021-03-05: Issue #961 We need a possibility to detect method calls
	//public static boolean isFunction(String expr)
	public static boolean isFunction(String expr, boolean acceptQualifiers)
	// END KGU#959 2021-03-05
	{
		// START KGU#790 2020-11-02: Issue #800 Completely revised
//		expr = expr.trim();
//		int posLP = expr.indexOf("(");
//		// START KGU#560 2018-07-22: Bugfix #564 - Parenthesis may not be on index 0
//		//boolean isFunc = posLP < expr.indexOf(")") && posLP >=0 &&
//		boolean isFunc = posLP < expr.indexOf(")") && posLP >0 &&
//				// END KGU#560 2018-07
//				BString.countChar(expr,'(') == BString.countChar(expr,')') &&
//				expr.endsWith(")");
//		// The test above is way too easy, it would also hold for e.g. "(a+b)*(c+d)";
//		// So we restrict the result in the following
//		if (isFunc) {
//			isFunc = Syntax.isIdentifier(expr.substring(0, posLP), false, null);
//			// Tokenize string between the outer parentheses 
//			StringList tokens = Syntax.splitLexically(expr.substring(posLP+1, expr.length()-1), true);
//			int parLevel = 0;	// parenthesis level, must never get < 0
//			for (int i = 0; isFunc && i < tokens.count(); i++) {
//				String token = tokens.get(i);
//				if (token.equals("(")) parLevel++;
//				else if (token.equals(")")) {
//					isFunc = --parLevel >= 0;
//				}
//			}
//		}
//		return isFunc;
		StringList tokens = Syntax.splitLexically(expr, true);
		LinkedList<Expression> exprs = null;
		try {
			exprs = Expression.parse(tokens, null, (short)0);
		} catch (SyntaxException exc) {}
		if (exprs != null && exprs.size() == 1) {
			// START KGU#959 2021-03-05: Issue #961 We need a possibility to detect method calls
			return isFunction(exprs.getFirst(), acceptQualifiers);
			// END KGU#959 2021-03-05
		}
		return false;
		// END KGU#790 2020-11-02
	}
	// END KGU#332 2017-01-29

	// START KGU 2017-02-21: signature string uniform with Root.getSignatureString(false)
	/**
	 * Returns a string composed of the function name and the number of arguments in parentheses,
	 * e.g. {@code "sub0815(4)"}.
	 * @return the signature string
	 */
	public String getSignatureString()
	{
		String sigStr = null;
		// START KGU#790 2020-11-02: Issue #800
		//if (this.isFunc) {
		if (this.isFunction()) {
		// END KGU#790 2020-11-02
			sigStr = this.getName() + "(" + this.paramCount() + ")";
		}
		return sigStr;
	}
	// END KGU 2017-02-21

	public String getName()
	{
		// START KGU#56 2015-10-27: Analysis now already done by the constructor
		//if (isFunction())
		//    return str.trim().substring(0,str.trim().indexOf("(")).trim().toLowerCase();
		//else
		//    return null;
		// END KGU#56 2015-10-27
		// START KGU#790 2020-11-02: Issue #800
		//return this.name;
		String name = null;
		if (expr != null) {
			if (expr.type == Expression.NodeType.OPERATOR) {
				// FIXME might it be necessary to enclose the left side in parentheses?
				name = expr.children.getFirst().toString() + "." + expr.children.getLast().text;
			}
			else {
				name = expr.text;
			}
		}
		return name;
		// END KGU#790 2020-11-02
	}

//    // START KGU 2017-02-06: Support for case-ignorant mode
//    /**
//     * Returns the subroutine name unitized in case to a built-in subroutine
//     * with otherwise matching signature if ignoreCase is true (otherwise as is)
//     * @param ignoreCase - whether the name is to be adapted to a built-in one
//     * @return Subroutine name adapted in case
//     */
//    public String getName(boolean ignoreCase)
//    {
//        return ignoreCase ? this.caseAlignedName : this.name;
//    }
//    
//    /**
//     * Returns the originating command with a function name unitized in case to a
//     * built-in function if ignoreCase is true and the signature only differs in case. 
//     * @param ignoreCase - whether the name is to be adapted to a built-in one
//     * @return Subroutine call with adapted subroutine name
//     */
//    public String getInvokation(boolean ignoreCase)
//    {
//        String invok = this.str;
//        if (this.isFunc && ignoreCase) {
//    	    invok = this.getName(ignoreCase) + this.str.substring(this.name.length());
//        }
//        return invok;
//    }
//    // END KGU 2017-02-06

	public int paramCount()
	{
		// START KGU#56 2015-10-27: Performance improvements
//        if (isFunction())
//        {
//            String params = str.trim().substring(str.trim().indexOf("(")+1,str.length()-1).trim();
//            if(!params.equals(""))
//            {
//                StringList sl = StringList.explode(params,",");
//                return sl.count();
//            }
//            else return 0;
//        }
		// START KGU#790 2020-11-02: Issue #800
//		if (this.parameters != null)
//		{
//			return this.parameters.count();
//		}
		if (this.expr != null) {
			Expression funcNode = expr;
			if (expr.type == Expression.NodeType.OPERATOR) {
				funcNode = expr.children.getLast();
			}
			return funcNode.children.size();
		}
		// END KGU#790 2020-11-02
		// END KGU#56 2015-10-27
		else return 0;
	}

	public String getParam(int index)
	{
		// START KGU#56 2015-10-27: Analysis now only done once by the constructor
//        if (isFunction())
//        {
//            String params = str.trim().substring(str.trim().indexOf("(")+1,str.length()-1).trim();
//            if(!params.equals(""))
//            {
//                StringList sl = StringList.explode(params,",");
//                return sl.get(index);
//            }
//            else return null;
//        }
		// START KGU#790 2020-11-02: Issue #800
		//if (this.parameters != null)
		//{
		//	return this.parameters.get(index);
		//}
		//else return null;
		if (this.expr != null) {
			Expression funcNode = expr;
			if (expr.type == Expression.NodeType.OPERATOR) {
				funcNode = expr.children.getLast();
			}
			if (index >= 0 && index < funcNode.children.size()) {
				return funcNode.children.get(index).toString();
			}
		}
		return null;
		// END KGU#790 2020-11-02
		// END KGU#56 2015-10-27
	}

	// START KGU#332 2017-01-29: Enh. #335 - type map
	// START KGU#790 2020-11-27: Issue #800
//    /**
//     * Returns the name of the result type of this subroutine call if known as
//     * built-in function with unambiguous type.
//     * If this is known as built-in procedure then it returns "void".
//     * If unknown then returns the given defaultType
//     * @param defaultType - null or some default type name for unsuccessful retrieval
//     * @return name of the result type (Java type name)
//     */
//    public String getResultType(boolean ignoreCase, String defaultType)
//    {
//        String type = knownResultTypes.get(this.getName(ignoreCase) + "#" + this.paramCount());
//        if (type == null) {
//            type = defaultType;
//        }
//        return type;
//    }
	/**
	 * Returns the name of the result type of this subroutine call if known as
	 * built-in function with unambiguous type.
	 * If this is known as built-in procedure then it returns "void".
	 * If unknown then returns the given defaultType
	 * @param defaultType - null or some default type name for unsuccessful retrieval
	 * @return name of the result type (Java type name), or {@code null} in case
	 * this function does not actually represent a function
	 * @see #isFunction()
	 */
	public String getResultType(String defaultType)
	{
		String typeName = null;
		if (expr != null) {
			// We must not call expr.inferType() here because this in turn calls getResultType()
			Type type = expr.getDataType();
			if (type != null) {
				typeName = type.getName();
			}
			else {
				String signature = this.getName() + "#" + this.paramCount();
				typeName = knownResultTypes.get(signature);
				if (typeName == null) {
					typeName = controllerRoutineTypes.get(signature);
				}
			}
		}
		if (typeName == null) {
			typeName = defaultType;
		}
		return typeName;
	}
	// END KGU#790 2020-11-27
	// END KGU#332 2017-01-29

	// START KGU#448/KGU#790 2021-12-10: Issues #443, #800
	/**
	 * Sets up the DiagramController API tables for data type inference
	 */
	public static void setupControllerRoutineMap(Set<DiagramController> controllers) {
		controllerRoutineTypes.clear();
		StringList conflicts = new StringList();	// Signature conflicts
		for (DiagramController controller: controllers) {
			for (Map.Entry<String, Method> entry: controller.getFunctionMap().entrySet()) {
				String typeName = "void";
				String key = entry.getKey();
				Class<?> typeClass = entry.getValue().getReturnType();
				if (typeClass != null) {
					typeName = typeClass.getSimpleName();
				}
				if (controllerRoutineTypes.containsKey(key)
						&& !typeName.equals(controllerRoutineTypes.get(key))) {
					// We will remove the conflicting entries in the end
					conflicts.addIfNew(key);
				}
				else {
					controllerRoutineTypes.put(key, typeName);					
				}
			}
			for (Map.Entry<String, Method> entry: controller.getProcedureMap().entrySet()) {
				String key = entry.getKey();
				if (controllerRoutineTypes.containsKey(key)
						&& !"void".equals(controllerRoutineTypes.get(key))) {
					// We will remove the conflicting entries in the end
					conflicts.addIfNew(key);
				}
				else {
					controllerRoutineTypes.put(key, "void");
				}
			}
		}
		// Now remove all conflicting entries
		for (int i = 0; i < conflicts.count(); i++) {
			controllerRoutineTypes.remove(conflicts.get(i));
		}
	}
	// END KGU#448/KGU#790 2021-12-10

	// START KGU#790 2020-11-01: Issue #800 - moved to Syntax (renamed to .isIdentifier())
//    // START KGU#61 2016-03-22: Moved hitherto from Root (was a private member method there)
//    /**
//     * Checks identifier syntax (i.e. ASCII letters, digits, underscores, and possibly dots)
//     * @param _str - the identifier candidate
//     * @param _strictAscii - whether non-ascii letters are to be rejected
//     * @param _alsoAllowedChars - a String containing additionally accepted characters (e.g. ".") or null
//     * @return true iff _str complies with the strict identifier syntax convention (plus allowed characters)
//     */
//    public static boolean testIdentifier(String _str, boolean _strictAscii, String _alsoAllowedChars)
//    {
//    	_str = _str.trim().toLowerCase();
//    	// START KGU#877 2020-10-16: Bugfix #874 - we should tolerate non-ascii letters
//    	//boolean isIdent = !_str.isEmpty() &&
//    	//		('a' <= _str.charAt(0) && 'z' >= _str.charAt(0) || _str.charAt(0) == '_');
//    	boolean isIdent = false;
//    	if (!_str.isEmpty()) {
//    		char firstChar = _str.charAt(0);
//    		isIdent = ('a' <= firstChar && firstChar <= 'z')
//    				|| !_strictAscii && Character.isLetter(firstChar)
//    				|| firstChar == '_';
//    	}
//    	// END KGU#877 2020-10-16
//    	if (_alsoAllowedChars == null)
//    	{
//    		_alsoAllowedChars = "";
//    	}
//    	for (int i = 1; isIdent && i < _str.length(); i++)
//    	{
//    		char currChar = _str.charAt(i);
//    		if (!(
//    				('a' <= currChar && currChar <= 'z')
//    				// START KGU#877 2020-10-16: Bugfix #874 - we should tolerate non-ascii letters
//    				||
//    				!_strictAscii && Character.isLetter(currChar)
//    				// END KGU#877 2020-10-16
//    				||
//    				('0' <= currChar && currChar <= '9')
//    				||
//    				(currChar == '_')
//    				||
//    				_alsoAllowedChars.indexOf(currChar) >= 0
//    				))
//    			// END KGU 2015-11-25
//    		{
//    			isIdent = false;
//    		}
//    	}
//    	return isIdent;
//    }
//    // END KGU#61 2016-03-22
	// END KGU#790 2020-11-01

	// START KGU 2016-10-16: More informative self-description
	@Override
	public String toString()
	{
		// START KGU#790 2020-11-02: Issue #800
		//String paramNames = this.parameters == null ? "" : this.parameters.concatenate(", ");
		//return getClass().getSimpleName() + '@' + Integer.toHexString(hashCode()) +
		//		": " + this.getName() + "(" + paramNames + ")";
		String description = "---";
		if (expr != null) {
			description = expr.toString();
		}
		return getClass().getSimpleName() + '@' + Integer.toHexString(hashCode()) +
				": " + description;
		// END KGU#790 2020-11-02
	}
	// END KGU# 2016-10-16

	// START KGU#311 2016-12-22: Enh. #314 - We need the expression length for replacements
	/**
	 * Returns the length of the original String that was parsed to this Function
	 * 
	 * @return length of the code source snippet representing this function call
	 * 
	 * @see #getTokenCount()
	 */
	public int getSourceLength()
	{
		return str.length();
	}
	// END KGU#311 2016-12-22
	
	/**
	 * @return the number of lexical tokens representing this function call.
	 * 
	 * @see #getSourceLength()
	 * @see Expression#getTokenCount(boolean)
	 */
	public int getTokenCount()
	{
		if (expr != null) {
			return expr.getTokenCount(false);
		}
		return 0;
	}

}
