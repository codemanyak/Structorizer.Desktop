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
 *      Description:    Type class for records/structs.
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
 *      
 *
 ******************************************************************************************************///

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import lu.fisch.utils.StringList;

/**
 * @author kay
 *
 */
public class RecordType extends Type {
	
	private LinkedHashMap<String, Type> compTypes = null;

//	/**
//	 * Extracts name, element type, and index range from the given type
//	 * specification tokens {@code specTokens}. Assumed cases:<br/>
//	 * <ul>
//	 * <li>struct{compList}</li>
//	 * <li>record{compList}</li>
//	 * </ul>
//	 * {@code compList} is:
//	 * <ul>
//	 * <li>compSpec</li>
//	 * <li>compSpec; compList</li>
//	 * </ul>
//	 * {@code compSpec} is:
//	 * <ul>
//	 * <li>compNames: compType</li>
//	 * </ul>
//	 * {@code compNames} is:
//	 * <ul>
//	 * <li>compName</li>
//	 * <li>compName, compNames</li>
//	 * </ul>
//	 * The trouble is that {@code compType} are not necessarily a names but may
//	 * themselves be complex type descriptions. So we need some kind of parser
//	 * here.
//	 * @param name - type name
//	 * @param specTokens - blank-free token list
//	 */
//	public RecordType(String name, StringList specTokens) {
//		super(name, null);
//		// TODO Auto-generated constructor stub
//	}

	/**
	 * Constructs the type from the given {@code name} with the specified
	 * {@code components}.
	 * @param name - the type identifier
	 * @param components maps the component names to the respective component types
	 */
	public RecordType(String name, LinkedHashMap<String, Type> components) {
		super(name, null);
		this.compTypes = components;
	}
	
	/**
	 * Returns a string expressing the type structure either in a shallow way
	 * ({@code deep = false}) or in a completely recursive way ({@code deep = true}).
	 * The result will start with symbol {@code $}, followed by the identifier and the
	 * component specifications in parentheses, e.g.:<br/>
	 * {@code $id(compId1: type1; compId2: type2 ...)}
	 * @param deep - whether possible substructure is to be fully described (otherwise
	 * embedded types will just be represented by their names (if the are named).
	 * @return the composed string
	 * @see #toString()
	 */
	@Override
	public String toString(boolean deep)
	{
		StringList compStr = new StringList();
		if (this.compTypes != null) {
			for (Entry<String, Type> entry: this.compTypes.entrySet()) {
				String compType = dummy;
				if (entry.getValue() != null) {
					if (deep) {
						compType = entry.getValue().toString(true);
					}
					else {
						compType = entry.getValue().getName();
					}
				}
				compStr.add(entry.getKey() + ":" + compType);
			}
		}
		return "$" + this.name + "(" + compStr.concatenate(";") + ")";
	}
}
