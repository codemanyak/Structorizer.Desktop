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
 *      Description:    Type class for pointer types
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
 * Data type class for pointer types
 * @author Kay Gürtzig
 */
public class PointerType extends Type {
	
	private Type refType;

	/**
	 * Constructs a new pointer type with given {@code name} and pointing to
	 * {@code referencedType}.
	 * @param name - name of the pointer type
	 * @param referencedType - the {@link Type} this type is pointing to
	 * @throws SyntaxException if {@code name} does not fit to strict identifier syntax
	 */
	public PointerType(String name, Type referencedType) throws SyntaxException {
		super(name, null);
		refType = referencedType;
	}
	
	@Override
	public String toString(boolean deep)
	{
		String refStr = dummy;
		if (this.refType != null) {
			refStr = deep ? this.refType.toString(true) : this.refType.getName();
		}
		return "^" + this.name + "(" + refStr + ")";
	}
	
	/**
	 * @return the referenced data {@link Type}, or {@code null} if unspecified
	 */
	public Type getReferencedType()
	{
		return this.refType;
	}

}
