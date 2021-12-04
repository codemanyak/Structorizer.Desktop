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
 *      Kay Gürtzig     2021-10-17      Field unsigned and constructor with modifiers argument added
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

	/** prototype value, e.g. a Byte, Short, Integer, Float, etc. object */
	private Object proto;
	
	/** restriction to unsigned values (for integral types only) */
	private boolean unsigned = false;
	
	/**
	 * Creates a primitive type with given {@code name} for expressions of the
	 * type represented by the {@code prototype} value.
	 * @param name - the type name
	 * @param prototype - an object serving as prototype for the values of the type
	 * @throws SyntaxException
	 */
	protected PrimitiveType(String name, Object prototype) throws SyntaxException {
		super(name);
		if (prototype == null) {
			prototype = 0;	// Integer as default
		}
		// FIXME: Remove the debug print
		//System.out.println(prototype.getClass().getTypeName());
		proto = prototype;
	}

	/**
	 * Constructs the type from the given {@code name} and {@code modifiers}
	 * list (which will be reduced, i.e. blanks and empty parts will be removed).
	 * The modifiers must not be essential for the type (e.g. "unsigned") in a way
	 * that there be several possible types with same name only distinguished by the
	 * modifiers.
	 * @param name - type name, must be an Ascii identifier
	 * @param modifiers - list of modifiers or {@code null}
	 * @param prototype - an object serving as prototype for the values of the type
	 * @throws SyntaxException if {@code name} does not fit to identifier syntax
	 */
	protected PrimitiveType(String name, StringList modifiers, Object prototype) throws SyntaxException {
		this(name, prototype);
		if (modifiers != null) {
			for (int i = 0; i < modifiers.count(); i++) {
				// Most potential modifiers are simply ignored
				String modif = modifiers.get(i).trim();
				if (!modif.isEmpty()) {
					if (isIntegral()) {
						unsigned = modif.equalsIgnoreCase("unsigned");
						if (modif.equalsIgnoreCase("short")) {
							prototype = Short.valueOf((short)0);
						}
						else if (modif.equalsIgnoreCase("long")) {
							prototype = 0L;
						}
					}
				}
			}
		}
		//this.modifiers = modifiers;
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
	
	public boolean isUnsigned()
	{
		return unsigned;
	}
	
}
