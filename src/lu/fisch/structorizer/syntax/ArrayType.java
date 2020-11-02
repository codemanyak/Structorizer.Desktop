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
 *      Description:    Type class for arrays
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2019-12-20      First Issue
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      - Major difficulty is handling of lower index bounds like in Pascal:
 *        * How do we derive the number of elements?
 *        * How to derive a type description for languages that expect index ranges to start at 0?
 *        * How would a symbolic type description like like?
 *
 ******************************************************************************************************///

/**
 * Data type class for array types. Multidimensional arrays are to be represented
 * as nested array types.
 * In the self-description string a prefix '@' is used.
 * @author Kay Gürtzig
 */
public class ArrayType extends Type {

	private Type elType = null;
	int size = 0;	// 0 means unknown or flexible
	int offset = 0;	// By default, indexing starts at 0
	
//	/**
//	 * Extracts name, element type, and index range from the given type
//	 * specification tokens {@code specTokens}. Assumed cases:<br/>
//	 * <ul>
//	 * <li>elType[]</li>
//	 * <li>elType[size]</li>
//	 * <li>array of elType</li>
//	 * <li>array [size] of elType</li>
//	 * <li>array [index0..index1] of elType</li>
//	 * </ul>
//	 * The trouble is that {@code elType} is not necessarily a name but may
//	 * itself be a complex type description. So we need some kind of parser
//	 * here.
//	 * @param name - type name
//	 * @param specTokens - blank-free token list
//	 */
//	public ArrayType(String name, StringList specTokens) {
//		super(name, null);
//		// TODO Auto-generated constructor stub
//		int posArray = specTokens.indexOf("array", false);
//		int posBrack1 = specTokens.indexOf("[");
//		int posBrack2 = specTokens.lastIndexOf("]");
//		
//	}

	/**
	 * Constructs an array type for the given {@code name} and {@code size} elements
	 * of data type {@code elementType}, assuming that indexing starts with 0.
	 * @param name - the type name (must fit to strict Ascii identifier syntax)
	 * @param elementType - data type of the elements (or {@code null} if unspecified)
	 * @param size - maximum element number (or 0 if unspecified)
	 * @throws SyntaxException if the name does not fit to identifier syntax
	 */
	public ArrayType(String name, Type elementType, int size) throws SyntaxException {
		super(name, null);
		elType = elementType;
		if (size > 0) {
			this.size = size;
		}
	}

	/**
	 * 
	 * @param name
	 * @param elementType
	 * @param indexRange
	 * @throws SyntaxException if the name does not fit to identifier syntax
	 */
	public ArrayType(String name, Type elementType, int[] indexRange) throws SyntaxException {
		super(name, null);
		elType = elementType;
		if (indexRange != null) {
			offset = indexRange[0];
			size = indexRange[1] - offset + 1;
		}
	}

	@Override
	public boolean isStructured()
	{
		return true;
	}

	/**
	 * Returns a string expressing the type structure either in a shallow way
	 * ({@code deep = false}) or in a completely recursive way ({@code deep = true}).
	 * The result will start with symbol {@code @}, followed by the identifier (if named)
	 * and the element type specification, the index start offset and the number of elements
	 * in parentheses, e.g.:<br/>
	 * {@code @id(elType,0,100)}
	 * @param altName - an alternative name to be used instead of {@link #getName()}
	 * if {@code null} then the internal identifier will be used.
	 * @param deep - whether possible substructure is to be fully described (otherwise
	 * embedded types will just be represented by their names (if the are named).
	 * @return the composed string
	 */
	@Override
	protected String toStringWithName(String altName, boolean deep)
	{
		Type eType = this.elType;
		if (eType == null) {
			eType = getDummyType();
		}
		String elTypeStr = null;
		if (deep) {
			elTypeStr = eType.toString(true);
		}
		else {
			elTypeStr = eType.getName();
		}
		return "@" + (altName != null ? altName : this.name)
				+ "(" + elTypeStr + "," + offset + "," + size + ")";
	}
	
	/**
	 * @return the data type of the array elements (if known), {@code null} otherwise.
	 */
	public Type getElementType()
	{
		return this.elType;
	}
	
	/**
	 * @return the number of dimensions (= array nesting level), minimum is 1.
	 */
	public int getDimensions()
	{
		int nDims = 1;
		if (this.elType instanceof ArrayType) {
			nDims += ((ArrayType) this.elType).getDimensions();
		}
		return nDims;
	}
	
}
