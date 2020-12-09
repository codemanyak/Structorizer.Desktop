/*
    Structorizer
    A little tool which you can use to create Nassi-Shneiderman Diagrams (NSD)

    Copyright (C) 2009, 2020  Bob Fisch

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

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Class Declaration, a specific Expression subclass
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2020-11-03      First Issue (for #800)
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

import java.util.LinkedList;
import java.util.regex.Pattern;

import lu.fisch.utils.StringList;

/**
 * Specific {@link Expression} subclass to cover variable declarations, parameter
 * declarations, record component declarations, and constant definitions, possibly
 * even enumerator definitions.<br/>
 * Brings an own parsing algorithm and translation support.
 * @author Kay Gürtzig
 */
public class Declaration extends Expression {
	
	private final static Pattern VAR_PATTERN = Pattern.compile("(^|.*?\\W)var\\s(.*?)");

	/**
	 * Declaration context - needed for parsing to cover the slight differences
	 * @author Kay Gürtzig
	 */
	public static enum Context{
		/** Variable declaration in an instruction line after having cut off a "var" or "dim" keyword */
		DC_VAR,
		/** Variable declaration in an instruction line in assumed C style i.e. {@code <type> <id>} */
		DC_CSTYLE,
		/** Parameter declaration within a routine header, "var" or "const" modifiers may be there */
		DC_PARAM,
		/** Record component declaration in a type definition */
		DC_RECORD
		};
	
	/**
	 * Specifies the declaration syntax for translation - It can make sense to provide
	 * several declaration specifications for a target language, depending on the
	 * context (e.g. for variable declarations, parameter declarations, record
	 * component declarations, constant definitions)
	 * @author kay
	 *
	 */
	public final class DeclarationSpec
	{
		/** Whether grouping of variables with same type is allowed */
		public boolean hasGroups = true;
		/** Separator between items of a group */
		public String separator = ",";
		/** Separator between groups */
		public String separatorGroup = ";";
		/** Separator between identifiers and type */
		public String separatorType = ":";
		/** Symbol for variable initialisation */
		public String asgnSymbol = "<-";
		/** Assignment symbol for constant definition */
		public String defConstSymbol = "<-";
		/** Declaration keyword for variables */
		public String varKeyword = "var";
		/** Modifier for constants (meant to replace variable keyword */
		public String constKeyword = "const";
		/* TODO order of type and names - maybe some template with placeholders?
		 * e.g.:
		 * var decl: var {@id}: @type
		 * var init: var @id: @type @init_op @value
		 * const def: const @id @cinit_op @value
		 * const par: const @id
		 */
		/* TODO placement of array specifiers (cf. C, Java, Pascal)
		 * e.g.:
		 * C: @el_type @id[@size]
		 * Java: @el_type[@size] @id
		 * Pascal: @id: array [@start .. @end] of @el_type
		 */
		
		public DeclarationSpec(
				boolean withGroups,
				boolean withParamGroups,
				String idSeparator,
				String groupSeparator,
				String typeSeparator,
				String varInitSymbol,
				String constDefSymbol,
				String varDeclarator,
				String constModifier)
		{
			hasGroups = withGroups;
			asgnSymbol = varInitSymbol;
			defConstSymbol = constDefSymbol;
			separator = idSeparator;
			separatorGroup = groupSeparator;
			separatorType = typeSeparator;
			varKeyword = varDeclarator;
			constKeyword = constModifier;
		}
	}
	
	/**
	 * Whether all contained declarations are constant
	 */
	private boolean isConstant = false;
	
	/**
	 * 
	 * @param isConst
	 * @param tokenPos
	 */
	public Declaration(boolean isConst, short tokenPos)
	{
		super(NodeType.DECLARATION, "", tokenPos);
		isConstant = isConst;
	}

	/**
	 * 
	 * @param isConst
	 * @param tokenPos
	 * @param initOrVars
	 */
	public Declaration(boolean isConst, short tokenPos, LinkedList<Expression> initOrVars)
	{
		super(NodeType.DECLARATION, "", tokenPos, initOrVars);
		isConstant = isConst;
	}

	/**
	 * Parses the given token list (maybe a declaration line or a zone within a
	 * parameter declaration list and returns one or more declaration expressions
	 * according to the structure. Copes with all Structorizer declaration styles
	 * (Pascal, BASIC, C, Java)
	 * @param tokens - the token list the output is to be added to
	 * @param context - specifies the declaration context (because of syntactic differences)
	 * @param typeMap - a {@link TypeRegistry} for data type retrieval from names
	 * @return One or more Declaration objects if nothing goes wrong
	 * @throws SyntaxException in case of syntactical errors
	 */
	public static LinkedList<Declaration> parse(StringList tokens, Context context, TypeRegistry typeMap) throws SyntaxException
	{
		LinkedList<Declaration> declarations = new LinkedList<Declaration>();
		int tokenNo = 0;
		switch (context) {
		case DC_CSTYLE:
			break;
		case DC_PARAM:
		{
			ArrayList<StringList> declGroups = Syntax.splitExpressionList(tokens, ";");
			
			for(int i = 0; i < declGroups.size()-1; i++)
			{
				// common type for parameter / component group
				StringList type = null;
				StringList group = declGroups.get(i);
				int grpSize = group.count();
				// START KGU#371 2019-03-07: Enh. #385 - cope with default values
				StringList defltGr = null;
				// END KGU#371 2019-03-07
				int posColon = group.indexOf(":");
				if (posColon >= 0)
				{
					type = group.subSequence(posColon + 1, group.count()).trim();
					group = group.subSequence(0, posColon).trim();
					// START KGU#371 2019-03-07: Enh. #385 - cope with default values
					int posEq = type.indexOf("=");
					if (posEq >= 0) {
						defltGr = type.subSequence(posEq+1, type.count()).trim();
						type = type.subSequence(0, posEq).trim();
					}
					// END KGU#371 2019-03-07
				}
				// We must first split by ',' and face complex literals here
				ArrayList<StringList> vars = Syntax.splitExpressionList(group, ",");
				for (int j=0; j < vars.size()-1; j++)
				{
					StringList decl = vars.get(j).trim();
					if (!decl.isEmpty())
					{
						String prefix = "";	// KGU#375 2017-03-30: New for enh. #388 (constants)
						// START KGU#371 2019-03-07: Enh. #385
						StringList deflt = defltGr;
						// END KGU#371 2019-03-07
						// START KGU#109 2016-01-15: Bugfix #61/#107 - we must split every "varName" by ' '.
						// START KGU#371 2019-03-07: Enh. #385 - parameter lists getting more complex...
						//if (type == null && (posColon = decl.indexOf(" as ")) >= 0)
						//StringList tokens = new StringList(decl);
						if (type == null && (posColon = tokens.indexOf("as", false)) >= 0)
						// END KGU#371 2019-03-07
						{
							// START KGU#371 2019-03-07: Enh. #385 Scan for default / initial values
							//type = decl.substring(posColon + " as ".length()).trim();
							//decl = decl.substring(0, posColon).trim();
							type = decl.subSequence(posColon + 1, decl.count()).trim();
							decl.remove(posColon, decl.count());
							decl.trim();
							int posEq = type.indexOf("=");
							if (posEq >= 0) {
								deflt = type.subSequence(posEq+1, type.count()).trim();
								type.remove(posEq, type.count());
								type.trim();
								// The redundant 'optional' keyword is to be ignored 
								if (!decl.isEmpty() && decl.get(0).equalsIgnoreCase("optional")) {
									decl.remove(0);
									decl.trim();
								}
							}						
							// END KGU#371 2019-03-07
						}
						decl.removeAll(" ");
						if (decl.count() > 1) {
							// Is a C or Java array type involved? 
							if (declGroups.size() == 1 && posColon < 0 || type == null) {
								// START KGU#371 2019-03-07: Enh. #385 Scan for default / initial values
								int posEq = decl.indexOf("=");
								if (posEq >= 0) {
									if (deflt == null) {
										deflt = decl.subSequence(posEq + 1, decl.count());
									}
									decl.remove(posEq, decl.count());
								}						
								// END KGU#371 2019-03-07							
								int posBrack1 = decl.indexOf("[");
								int posBrack2 = decl.lastIndexOf("]");
								if (posBrack1 > 0 && posBrack2 > posBrack1) {
									StringList indices = decl.subSequence(posBrack1, posBrack2+1);
									if (posBrack2 == decl.count()-1) {
										// C-style: brackets right of the variable id
										if (posBrack1 > 1 && type == null) {
											type = decl.subSequence(0, posBrack1-1);
											type.add(indices);
										}
										decl = decl.subSequence(posBrack1-1, posBrack1);
									}
									else {
										// Java style: brackets between element type and variable id
										if (type == null) {
											type = decl.subSequence(0, posBrack2+1);
										}
										decl = decl.subSequence(posBrack2+1, tokens.count());
									}
								}
								else {
									// No brackets...
									// START KGU#580 2018-09-24: Bugfix #605
									if (decl.get(0).equals("const")) {
										prefix = "const ";
										decl.remove(0);
									}
									// END KGU#580 2018-09-24
									// START KGU#371 2019-03-08: Issue #385 - We shouldn't return an empty string but null if there is no type
									//type = tokens.concatenate(null, 0, tokens.count()-1);
									if (decl.count() > 1) {
										type = decl.subSequence(0, tokens.count()-1);
									}
									// END KGU#371 2019-03-07
									decl.remove(0, decl.count()-1);
								}
							}
							// START KGU#375 2017-03-30: New for enh. #388 (constants)
							else if (decl.get(0).equals("const")) {
								// START KGU#580 2018-09-24: Bugfix #605							
								decl.remove(0);
								// END KGU#580 2018-09-24
								prefix = "const ";
							}
							// END KGU#375 2017-03-30
						}
						Declaration dcl = null;
						// What about the var name here?
						if (!deflt.isEmpty()) {
							// FIXME count the tokens within the group
							dcl = new Declaration(prefix.contains("constant"), (short)tokenNo, Expression.parse(deflt, null));
						}
						else {
							// FIXME count the tokens within the group
							dcl = new Declaration(prefix.contains("constant"), (short)tokenNo);
						}
						
//						if (declNames != null) declNames.add(decl);
						// START KGU#375 2017-03-30: New for enh. #388 (constants) 
						//if (declTypes != null)	declTypes.add(type);
						if (type != null) {
							// FIXME
							Type tp = typeMap.getType(type.concatenate(null));
							dcl.dataType = tp;
						}
						declarations.add(dcl);
					}
				}
				// FIXME: possibly add one
				tokenNo += grpSize;
			}
		}
			
			break;
		case DC_RECORD:
			break;
		case DC_VAR:
			break;
		default:
			break;
		
		}
		return null;
	}
	
	/**
	 * 
	 * @param tokens
	 * @param context
	 * @param declSpec
	 */
	public void appendToTokenList(StringList tokens, Context context, DeclarationSpec declSpec)
	{
		if (isConstant) {
			tokens.add(declSpec.constKeyword);
		}
		// TODO
	}
	
}
