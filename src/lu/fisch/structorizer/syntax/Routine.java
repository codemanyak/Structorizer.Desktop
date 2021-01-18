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

import java.util.LinkedList;

import lu.fisch.utils.StringList;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Class representing a routine header with parameter declarations and result type
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

/**
 * Expression subclass representing a routine header with parameter declarations and result type
 * @author Kay Gürtzig
 * @deprecated - I doubt that we need this - {@link Declaration} {@link Declaration.DeclarationRule#PAR_LIST}
 * and result type should be held directly in the {@link Line} object
 */
public class Routine extends Expression {

	public Routine(String id, Type resultType, LinkedList<Expression> parameters)
	{
		super(NodeType.ROUTINE, id, (short) 0, parameters);
	}
	
	public static Routine parse(StringList tokens, TypeRegistry typeMap) throws SyntaxException
	{
		// TODO get it from Root
		return null;
	}
	
	// TODO We ought to add an argument specifying the declaration style (C, Java, Pascal, BASIC)
	public void appendToTokenList(StringList tokens)
	{
		// TODO peek in Diagramm?
	}
	
}
