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

import lu.fisch.utils.StringList;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Represents a variable or constant declaration line
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2019-12-27      First Issue
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

/**
 * Represents a (possibly initialized) variable or constant declaration
 * @author Kay Gürtzig
 */
public class Declaration implements Line {

	public boolean isConstant = false;
	public Type type = null;
	public StringList names = null;
	public Expression initilization = null;
	
	/**
	 * Creates a complete declaration object from the arguments
	 * @param asConst - whether this is a constant definition rather than a variable declaration (value must be given!)
	 * @param name - name of the declared entity
	 * @param type - the data type of the declared entity
	 * @param value- the expression specifying the initialization value (may be {@code null})
	 */
	public Declaration(boolean asConst, String name, Type type, Expression value) {
		isConstant = asConst;
		this.names = StringList.getNew(name);
		this.type = type;
		this.initilization = value;
	}

	/**
	 * Creates a common declaration object for several variables from the arguments
	 * @param names - name list of the declared entities
	 * @param type - the data type of the declared variables 
	 */
	public Declaration(StringList names, Type type)
	{
		this.names = names;
		this.type = type;
	}
	
	/**
	 * Creates an untyped declaration object from the arguments
	 * @param asConst - whether this is a constant definition rather than a variable declaration (value must be given!)
	 * @param name - name of the declared entity
	 */
	public Declaration(boolean asConst, String name, Expression value)
	{
		isConstant = asConst;
		this.names = StringList.getNew(name);
		// FIXME We might try to derive a type from the expression - but this would require some context information
		this.initilization = value;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(this.isConstant ? "const" : "var");
		sb.append(this.names.concatenate(","));
		if (type != null) {
			sb.append(":");
			sb.append(type.toString(false));
		}
		if (this.initilization != null) {
			sb.append("<-");
			sb.append(this.initilization.toString());
		}
		return sb.toString();
	}

}
