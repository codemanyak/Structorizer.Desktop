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

import java.util.ArrayList;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Data type representing class for array types
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2019-12-20      First Issue
 *      Kay Gürtzig     2021-10-17      getDimensions() modified (both signature and algorithm)
 *      Kay Gürtzig     2021-11-24      Field rangeExprs and method getRangeExpressions() added
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
	private int size = 0;	// 0 means unknown or flexible, -1 means to be dynamically calculated via expression
	private int offset = 0;	// By default, indexing starts at 0
	private Expression[] rangeExprs = null;	// holds the [offset and] size expressions if not literals
	
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
		super(name);
		elType = elementType;
		if (size > 0) {
			this.size = size;
		}
	}

	/**
	 * Constructs an array type for the given {@code name} over elements of data
	 * type {@code elementType} with given index range {@code indexRange}.
	 * 
	 * @param name - the type name (must fit to strict Ascii identifier syntax), or
	 *  {@code null} (for an anonymous type)
	 * @param elementType - data type of the elements (or {@code null} if unspecified)
	 * @param indexRange - a pair of lower and upper index bounds where both values
	 * are meant to be included.
	 * 
	 * @throws SyntaxException if the name does not fit to identifier syntax
	 */
	public ArrayType(String name, Type elementType, int[] indexRange) throws SyntaxException {
		super(name);
		elType = elementType;
		if (indexRange != null) {
			offset = indexRange[0];
			size = indexRange[1] - offset + 1;
		}
	}

	/**
	 * Constructs an array type for the given {@code name} over elements of data
	 * type {@code elementType} with given index range expressions {@code rangeExprs}.
	 * 
	 * @param name - the type name (must fit to strict Ascii identifier syntax) or
	 *  {@code null} (for an anonymous type)
	 * @param elementType - data type of the elements (or {@code null} if unspecified)
	 * @param rangeExpressions - an array of one or two {@link Expression}s, in case
	 * of one expression it describes the computation of the element number (array size),
	 * in case of two expressions, {@code rangeExpressions[0]} denotes the lower index
	 * bound, {@code rangeExpressions[1]} the upper index bound.
	 * 
	 * @throws SyntaxException if the name does not fit to identifier syntax
	 */
	public ArrayType(String name, Type elementType, Expression[] rangeExpressions) throws SyntaxException {
		super(name);
		elType = elementType;
		size = -1;	// Mark the association of expressions
		rangeExprs = rangeExpressions;
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
	 * and, in parentheses, the element type specification, the index start offset
	 * and the number of elements (if specified exactly, otherwise 0), e.g.:<br/>
	 * {@code @id(elType,0,100)}
	 * 
	 * @param altName - an alternative name to be used instead of {@link #getName()},
	 * if {@code null} then the internal identifier will be used.
	 * @param deep - whether possible substructure is to be fully described (otherwise
	 * embedded types will just be represented by their names (if they are named).
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
	 * @return the data type of the array elements (if known, {@code null} otherwise).
	 */
	public Type getElementType()
	{
		return this.elType;
	}
	
	/**
	 * @return the list of array sizes and index range offsets in direct nesting
	 * succession, i.e. as far as the element type is an array type itself.
	 * 
	 * @see #getRangeExpressions(int)
	 */
	public ArrayList<int[]> getDimensions()
	{
		ArrayList<int[]> dimensions = new ArrayList<int[]>();
		dimensions.add(new int[] {this.size, this.offset});
		Type elemT = this.elType;
		while (elemT instanceof ArrayType) {
			dimensions.add(new int[] {((ArrayType)elemT).size, ((ArrayType)elemT).offset});
			elemT = ((ArrayType)elemT).elType;
		}
		return dimensions;
	}
	
	/**
	 * Retrieves the {@link Expressions} describing either the size of the
	 * nested array type at level {@code _depth} (where 0 denotes this type)
	 * if the index offset is 0 or of the index start offset (lower index bound)
	 * and of the upper index bound if the index offset is not 0.
	 * If the size was not specified at all or if there are no {@code _depth}
	 * subsequent array dimensions in this type then returns {@code null}.
	 * 
	 * @param _depth - the dimension level (0 is outer etc.)
	 * @return an array of one or two {@link Expression} objects (representing
	 * syntax trees), or {@code null}
	 * 
	 * @see #getDimensions()
	 */
	public Expression[] getRangeExpressions(int _depth)
	{
		Type elemT = this.elType;
		while (elemT instanceof ArrayType && _depth >= 0) {
			ArrayType elemType = (ArrayType)elemT;
			if (_depth == 0) {
				if (elemType.size == -1) {
					return elemType.rangeExprs;
				}
				else if (elemType.size == 0) {
					return null;
				}
				else if (elemType.offset == 0) {
					return new Expression[] {
							new Expression(Expression.NodeType.LITERAL,
									Integer.toString(elemType.size), (short)0)
					};
				}
				else {
					return new Expression[] {
							new Expression(Expression.NodeType.LITERAL,
									Integer.toString(elemType.offset), (short)0),
							new Expression(Expression.NodeType.LITERAL,
									Integer.toString(elemType.size -1), (short)1)
					};
				}
			}
			elemT = elemType.elType;
			_depth--;
		}
		return null;
	}
}
