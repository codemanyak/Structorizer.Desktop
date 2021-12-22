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
 *      Kay Gürtzig     2021-12-22      Constant evaluation mechanism integrated
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      2021-12-22 Kay Gürtzig
 *      - We should possibly also store the computed enumerator values. Unfortunately, we need a map of
 *        constant names to evaluated numbers for this purpose, but this might lead to redundancies or
 *        even circular induction among Root.constants and this type...
 *
 ******************************************************************************************************///

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import bsh.EvalError;
import bsh.Interpreter;
import lu.fisch.utils.StringList;

/**
 * Data type class for enumeration types
 * In the self-description string a name prefix '#' is used.
 * @author Kay Gürtzig
 */
public class EnumType extends Type {

	private LinkedHashMap<String, String> items = new LinkedHashMap<String, String>();
	private HashMap<String, Integer> values = null;

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
	 * @param items - a sequential map of names to value descriptions or null.
	 * @throws SyntaxException if the name does not fit to identifier syntax
	 */
	public EnumType(String name, LinkedHashMap<String, String> items) throws SyntaxException {
		super(name);
		this.items.putAll(items);
	}

	/**
	 * Constructs an enumeration type from its name, possible modifiers, and a list
	 * of items, which may be a simple name or a name with an assigned value each.
	 * Items without explicitly given value are generically coded (by incrementing
	 * the value assigned to the previous element.
	 * @param name - type name
	 * @param items - a sequential map of names to value descriptions or null
	 * @param constants - a lookup table of (previously) defined constants for code
	 *     evaluation
	 * @throws SyntaxException if the name does not fit to identifier syntax
	 */
	public EnumType(String name, LinkedHashMap<String, String> items,
			HashMap<String, String> constants) throws SyntaxException {
		super(name);
		this.items.putAll(items);
		this.evaluateItems(constants, false);
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
		super(name);
		for (int i = 0; i < itemNames.count(); i++) {
			this.items.put(itemNames.get(i), null);
		}
	}
	
	/**
	 * Evaluates and assignes the enumeration codes to the enumerator keys. This
	 * may be straightforward but can also require to interpret constant expressions
	 * if an expression evaluation fails, then no value will be assigned.
	 * 
	 * @param constants - (previously) defined constants, such that occurring
	 *     references may be evaluated as well.
	 * @param force - whether 
	 * @return StringList of possible errors
	 */
	public StringList evaluateItems(HashMap<String, String> constants, boolean force)
	{
		StringList errors = new StringList();
		if (values == null) {
			values = new HashMap<String, Integer>();
		}
		// FIXME Should we better cache this a a static field?
		Interpreter interpreter = null;
		
		int code = 0;
		for (Map.Entry<String, String> item: items.entrySet()) {
			String name = item.getKey();
			Integer val = values.get(item.getKey());
			if (val == null || force) {
				if (item.getValue() == null) {
					if (code != Integer.MIN_VALUE) {
						values.put(name, code);
					}
				}
				else {
					StringList tokens = Syntax.splitLexically(item.getValue(), true, true);
					if (interpreter == null) {
						interpreter = new Interpreter();
						try {
							Function.implementBuiltInFunctions(interpreter);
						} catch (EvalError exc) {
							errors.add(exc.toString());
						}
					}
					// Now replace all constant names by the associated expressions
					for (int i = 0; i < tokens.count(); i++) {
						String token = tokens.get(i);
						if (Syntax.isIdentifier(token, false, null)
								&& (i+1 == tokens.count() || !tokens.get(i+1).equals("("))) {
							if (values.containsKey(token)) {
								// An own evaluated enum constant, so get its value
								tokens.set(i, Integer.toString(values.get(token)));
							}
							else if (constants != null && constants.containsKey(token)) {
								// Seems to be a registered constant, so evaluate it
								String expr = constants.get(token);
								// Check enumerator constants
								int ePos = expr.indexOf("€");
								if (expr.startsWith(":") && ePos > 1) {
									/* If it a constant of this type then it is an illegal
									 * forward reference otherwise its code should have been
									 * found in values. We may let it crash
									 */
									// Skim off the enumerator type name
									expr = expr.substring(expr.indexOf('€')+1);
								}
								tokens.set(i, "(" + expr + ")");
							}
						}
					}
					// Eventually, evaluate the expression
					try {
						Object value = interpreter.eval(tokens.concatenate());
						if (value instanceof Integer) {
							values.put(name, code = (Integer)value);
						}
					} catch (EvalError exc) {
						code = Integer.MIN_VALUE;
						errors.add(exc.toString());
					}
				}
			}
			else {
				code = val;
			}
			if (code != Integer.MIN_VALUE) {
				code++;
			}
		}
		return errors;
	}
	
	/**
	 * @return a copy of the stored enumeration items, i.e., a mapping of the
	 * enumerator names to the constant expressions. In case the codes have
	 * already been evaluated, then the constant expression will be the number
	 * literal.
	 */
	public HashMap<String, String> getEnumItems()
	{
		LinkedHashMap<String, String> enumItems = new LinkedHashMap<String, String>();
		int code = 0;
		for (Map.Entry<String, String> item: items.entrySet()) {
			String name = item.getKey();
			String val = item.getValue();
			if (values != null && values.get(name) != null) {
				val = Integer.toString(values.get(name));
			}
			else if (val == null && code != Integer.MIN_VALUE) {
				val = Integer.toString(code);
			}
			else {
				try {
					if (val.startsWith("0b")) {
						code = Integer.parseInt(val, 2);
					}
					else if (val.startsWith("0x")) {
						code = Integer.parseInt(val, 16);
					}
					else if (val.startsWith("0")) {
						code = Integer.parseInt(val, 8);
					}
					else {
						code = Integer.parseInt(val);
					}
					val = Integer.toString(code);
				}
				catch (NumberFormatException nfe) {
					code = Integer.MIN_VALUE;
				}
			}
			enumItems.put(name, val);
			if (code != Integer.MIN_VALUE) {
				code++;
			}
		}
		return enumItems;
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
