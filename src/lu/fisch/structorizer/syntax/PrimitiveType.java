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

import lu.fisch.utils.StringList;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Data type class for primitive types
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2020-11-01      First Issue (for #800)
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

/**
 * Data type class for primitive types, contains a prototype object and allows several tests.
 * @author Kay Gürtzig
 */
public class PrimitiveType extends Type {
	
	private static final StringList FLOAT_NAMES = new StringList(new String[] {"Double", "Float"});

	private Object proto;

	/**
	 * Creates a primitive type with given {@code name} for expressions of the
	 * type represented by the {@code prototype} value.
	 * @param name - the type name
	 * @throws SyntaxException
	 */
	protected PrimitiveType(String name, Object prototype) throws SyntaxException {
		super(name, null);
		System.out.println(prototype.getClass().getTypeName());
		proto = prototype;
	}

	@Override
	public boolean isNumeric()
	{
		return proto instanceof Number;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return true;
	}
	
	public boolean representsValue(Object value)
	{
		return value != null && proto.getClass().getName().equals(value.getClass().getName());
	}
	
	public boolean acceptsValue(Object value)
	{
		boolean canBeCast = false;
		try {
			proto.getClass().cast(value);
			canBeCast = true;
		}
		catch (ClassCastException exc) {	
		}
		return canBeCast;
	}
	
	public boolean isFloating()
	{
		return FLOAT_NAMES.contains(proto.getClass().getName());
	}

	public boolean isIntegral()
	{
		return isNumeric() && !isFloating();
	}
	
}
