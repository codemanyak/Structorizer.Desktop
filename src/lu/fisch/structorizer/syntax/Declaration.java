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

import lu.fisch.utils.StringList;

/**
 * Specific {@link Expression} subclass to cover variable declarations, parameter
 * declarations, record component declarations, and constant definitions, possibly
 * even enumerator definitions.<br/>
 * Brings an own parsing algorithm and translation support.
 * @author Kay Gürtzig
 */
public class Declaration extends Expression {
	
	/**
	 * Declaration context - needed for parsing to cover the slight differences
	 * @author Kay Gürtzig
	 */
	public static enum Context{
		/** Variable declaration in an instruction line after having cut off a "var" or "dim" keyword */
		DC_VAR,
		/** Variable declaration in an instruction line in assumed C style i.e. {@code <type> <id>} */
		DC_CSTYLE,
		/** Parameter declaration within a routine header */
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
	
	public Declaration(boolean isConst, short tokenPos)
	{
		super(NodeType.DECLARATION, "", tokenPos);
		isConstant = isConst;
	}

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
		// TODO
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
