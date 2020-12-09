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
 *      Author:         kay
 *
 *      Description:    Type class for enumeration types.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2019-12-20      First Issue (for #800)
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
 * Data type class for enumeration types
 * In the self-description string a name prefix '#' is used.
 * @author Kay Gürtzig
 */
public class EnumType extends Type {

	private LinkedHashMap<String, String> items = new LinkedHashMap<String, String>();

//	/**
//	 * @param specTokens
//	 */
//	public EnumType(StringList specTokens) {
//		super(specTokens);
//		// TODO Auto-generated constructor stub
//	}

	/**
	 * Constructs an enumeration type from its name, possible modifiers, and a list
	 * of items, which may be a simple name or a name with an assigned value each.
	 * Items without explicitly given value are generically coded (by incrementing
	 * the value assigned to the previous element.
	 * @param name - type name
	 * @param modifiers - possible specification of the underlying integral type
	 * @param items - a sequential map of names to value descriptions or null.
	 * @throws SyntaxException if the name does not fit to identifier syntax
	 */
	public EnumType(String name, StringList modifiers, LinkedHashMap<String, String> items) throws SyntaxException {
		super(name, modifiers);
		this.items.putAll(items);
	}

	/**
	 * Constructs an enumeration type from its name, possible modifiers, and a list
	 * of value names with strictly generic coding (i.e. incrementing from 0 on).
	 * @param name - type name
	 * @param modifiers - possible specification of the underlying integral type
	 * @param itemNames - sequence of constant names for this type.
	 * @throws SyntaxException if the name does not fit to identifier syntax
	 */
	public EnumType(String name, StringList modifiers, StringList itemNames) throws SyntaxException {
		super(name, modifiers);
		for (int i = 0; i < itemNames.count(); i++) {
			this.items.put(itemNames.get(i), null);
		}
	}
	
	/**
	 * Returns a string expressing the type structure either in a shallow way
	 * ({@code deep = false}, meaning only name and item number) or with all item
	 * specifications ({@code deep = true}).<br/>
	 * Both results will start with prefix {@code #}, followed by the identifier
	 * and a parenthesis, which contains just the number of items (not deep) or
	 * all item specifications (deep), e.g.:<br/>
	 * {@code #id(name1[=val1], name2[=val2] ...)}
	 * @param deep - whether the items are to be fully described (otherwise
	 * only their number is included).
	 * @return the composed string
	 * @see #toString()
	 */
	@Override
	protected String toStringWithName(String altName, boolean deep)
	{
		StringList itemStrs = new StringList();
		if (!deep) {
			itemStrs.add(Integer.toString(this.items.size()));
		}
		else {
			for (Entry<String, String> entry: this.items.entrySet()) {
				String itemVal = "";
				if (entry.getValue() != null) {
					itemVal = entry.getValue();
				}
				itemStrs.add(entry.getKey() + itemVal);
			}
		}
		return "#" + (altName != null ? altName : this.name) + "(" + itemStrs.concatenate(",") + ")";
	}

}
