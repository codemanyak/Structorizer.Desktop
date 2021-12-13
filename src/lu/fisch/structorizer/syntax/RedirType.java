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
 *      Description:    Data type class representing a synonym for another type
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2021-11-28      First Issue
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *      2021-12-10 Kay Gürtzig
 *      - It might be sensible to replace all direct Type references by type names to achieve more
 *        robustness against redefinitions of referenced types, e.g. in included diagrams or preceding
 *        type definitions. A direct type reference would not reflect such changes and require complete
 *        reconstruction of the entire type tree. Of course, retrieval would cost more time but due to
 *        the incorporated TypeRegistry reference it would be feasible.
 *
 ******************************************************************************************************///

/**
 * Data type class for type synonyms, i.e. redirects to another existing type.
 * A redirection type may refer to another redirection type but the end of the
 * reference chain must always be non-redirecting type. Recursive (cyclic) type
 * references must be prevented.<br/>
 * Method {@link #getType()} retrieves the eventual material type.
 * 
 * @author Kay Gürtzig
 */
public class RedirType extends Type {

	/**
	 * Contains the reference to the type this one is a synonym for and
	 * redirecting to.
	 */
	private Type referredType = null;
	
	/**
	 * Constructs a new synonym type that refers to the given {@code targetType}
	 * @param name - new synonym for {@code targetType}, must not be equal to the
	 * name of {@code targetType}
	 * 
	 * @throws SyntaxException e.g. in case of a recursive type reference
	 */
	public RedirType(String name, Type targetType) throws SyntaxException {
		super(name);
		Type refType = targetType.getType();
		if (name.equals(targetType.getName())
				|| refType == null
				|| name.equals(refType.getName())) {
			throw new SyntaxException("The type reference must not be recursive!", 0);
		}
		referredType = targetType;
	}

	/**
	 * @return the eventually referred type, i.e. end of the reference chain.
	 */
	protected Type getType()
	{
		// We must not risk an endless recursion here!
		Type refType = referredType;
		while (refType != null && refType != this && refType instanceof RedirType) {
			refType = ((RedirType)refType).referredType;
		}
		return refType;
	}
	
	/**
	 * @return {@code true} if the referred type represents numeric values
	 * @see #isPrimitive()
	 * @see #isAnonymous()
	 */
	public boolean isNumeric()
	{
		return getType().isNumeric();
	}
	
	/**
	 * @return {@code true} if this type refers to a primitive data type
	 * @see #isNumeric()
	 * @see #isAnonymous()
	 */
	public boolean isPrimitive()
	{
		return getType().isPrimitive();
	}

	/**
	 * @return {@code true} iff this is referring to the dummy type or a nameless type
	 * @see #isDummy()
	 */
	public boolean isAnonymous()
	{
		return getType().isAnonymous();
	}
	
	/**
	 * @return {@code true} iff this is is referring to the dummy type
	 * @see #isAnonymous()
	 */
	public boolean isDummy()
	{
		return getType().isDummy();
	}
	
	/**
	 * @return {@code true} if the referred type is composed
	 */
	@Override
	public boolean isStructured()
	{
		return getType().isStructured();
	}
	
	/**
	 * Recursively refreshes all incorporated type references via name
	 * retrieval from the {@link #registry}.
	 */
	@Override
	public void updateTypeReferences()
	{
		Type refType = null;
		if (referredType != null) {
			refType = getType(referredType.getName());
			if (refType != null) {
				refType.updateTypeReferences();
				referredType = refType;
			}
		}
	}

}
