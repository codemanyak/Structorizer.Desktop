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

import java.util.HashMap;
import java.util.regex.Pattern;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Class TypeRegister, a global and several local type registers
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
 * @author kay
 *
 */
public class TypeRegistry {

	private static final Pattern BIN_PATTERN = Pattern.compile("0b[01]+");
	private static final Pattern OCT_PATTERN = Pattern.compile("0[0-7]+");
	private static final Pattern HEX_PATTERN = Pattern.compile("0x[0-9A-Fa-f]+");

	@SuppressWarnings("serial")
	private static final HashMap<String, Type> globalMap = new HashMap<String, Type>() {{
		try {
			put(":dummy", Type.getDummyType());
			put(":byte", new PrimitiveType("byte", Byte.valueOf((byte)0)));
			put(":short", new PrimitiveType("short", Short.valueOf((short)0)));
			put(":int", new PrimitiveType("int", Integer.valueOf((int)0)));
			put(":long", new PrimitiveType("long", Long.valueOf((long)0)));
			put(":float", new PrimitiveType("float", Float.valueOf((float)0.0)));
			put(":double", new PrimitiveType("double", Double.valueOf((double)0.0)));
			put(":char", new PrimitiveType("char", Character.valueOf('\0')));
			put(":string", new PrimitiveType("string", new String()));
			put(":boolean", new PrimitiveType("boolean", Boolean.valueOf(false)));
		}
		catch (Exception exc)
		{}
	}};
	
	private HashMap<String, Type> typeMap = null;
	
	/**
	 * Used for static access to global register, lazy initialisation. The global
	 * instance will automatically be created as soon as a new TypeRegister is
	 * requested 
	 */
	private static TypeRegistry globalInstance = null;
	
	/**
	 * Creates a new (local) TypeRegistry. For standard types
	 */
	public TypeRegistry()
	{
		if (globalInstance == null) {
			getGlobalInstance();
		}
		typeMap = new HashMap<String, Type>();
		typeMap.putAll(globalMap);
	}
	
	private TypeRegistry(boolean asGlobalInstance) {
		typeMap = globalMap;
	}
	
	private static TypeRegistry getGlobalInstance()
	{
		if (globalInstance == null) {
			globalInstance = new TypeRegistry(true);
		}
		return globalInstance;
	}
	
	@Override
	public String toString()
	{
		return this.getClass().getSimpleName() + this.typeMap.toString();
	}
	
	public static Type getStandardType(String name)
	{
		return getGlobalInstance().getType(name);
	}
	
	public static Type getStandardTypeFor(String literal)
	{
		Type stdType = Type.getDummyType();
		if (Expression.BOOL_LITERALS.contains(literal)) {
			stdType = TypeRegistry.getStandardType("boolean");
		}
		else if (literal.startsWith("'") && literal.endsWith("'")) {
			// This is a very rough heuristics but not worse than before in Element.identifyExprType()
			if (literal.length() == 3 || literal.length() == 4 && literal.charAt(1) == '\\') {
				stdType = getStandardType("char");
			}
			else {
				stdType = getStandardType("string");
			}
		}
		else if (literal.length() > 2 && literal.startsWith("\"") && literal.endsWith("\"")) {
			stdType = getStandardType("string");
		}
		// START KGU#354 2017-05-22: Enh. #354
		// These literals cause errors with Double.parseDouble(expr) and Integer.parseInt(expr)
		else if (BIN_PATTERN.matcher(literal).matches()
				|| OCT_PATTERN.matcher(literal).matches()
				|| HEX_PATTERN.matcher(literal).matches()) {
			stdType = getStandardType("int");
		}
		// END KGU#354 2017-05-22
		else {
			// In this cascade the last successful try simply wins the game
			try {
				Double.parseDouble(literal);
				stdType = getStandardType("double");
				Long.parseLong(literal);
				stdType = getStandardType("long");
				Integer.parseInt(literal);
				stdType = getStandardType("int");
			}
			catch (NumberFormatException exc) {
			}
		}
		return stdType;
	}
	
	/**
	 * Retrieves the data type with the given type name {@code typeName}.
	 * If it hadn't been registered here then returns {@code null}.
	 * @param typeName - must be an identifier
	 * @return the {@link Type} or {@code null}
	 */
	public Type getType(String typeName)
	{
		return this.typeMap.get(":" + typeName);
	}

	/**
	 * Retrieves the data type for the given variable name {@code varName}.
	 * If it hadn't been registered here then returns {@code null}.
	 * @param varName - must be an identifier
	 * @return the {@link Type} or {@code null}
	 */
	public Type getTypeFor(String varName)
	{
		return this.typeMap.get(varName);
	}
	
	/**
	 * Registers the given {@link Type} {@code type} under its name unless it
	 * is anonymous or there is already an entry with the same name. Both
	 * restrictions can be overridden with {@code force = true}.
	 * Standard types may not be overridden in any case.
	 * @param type - the {@link Type} to be registered
	 * @param force - whether it is even to be put if it is an anonymous type
	 * or would override an existing entry (which is dangerous as references to
	 * the type might get inconsistent).
	 * @return {@code null} if the registration failed, {@code type} if the
	 * registration worked without overriding another entry, otherwise the
	 * overwritten previous {@link Type} entry.
	 */
	public Type putType(Type type, boolean force)
	{
		// TODO: Check for equivalent type
		Type result = null;
		if (getStandardType(type.getName()) == null &&
				(force || !type.isAnonymous() && !typeMap.containsKey(":" + type.getName()))) {
			result = typeMap.put(":" + type.getName(), type);
			if (result == null) {
				result = type;
			}
		}
		return result;
	}
	
	/**
	 * Registers the type association between the variable with name {@code varName}
	 * and the given {@link Type} {@code type} unless {@code varName} is already
	 * associated with another type or a different type is registered with the same
	 * name as {@code type}. Both restrictions can be overrun with {@code force = true}.<br/>
	 * If the registration was done then the registration of {@code type} as data
	 * type is also ensured.
	 * @param type - the {@link Type} to be registered
	 * @param force - whether it is even to be put if it is an anonymous type
	 * or would override an existing entry (which is dangerous as references to
	 * the type might get inconsistent).
	 * @return {@code null} if the registration failed, {@code type} if the
	 * registration worked without overriding another entry, otherwise the
	 * overwritten previous {@link Type} entry.
	 */
	public Type putTypeFor(String varName, Type type, boolean force)
	{
		Type result = null;
		Type prevType = null;
		if (force || !typeMap.containsKey(varName)
				&& ((prevType = getType(type.getName())) == null || prevType == type)) {
			// TODO: Check for equivalent type
			result = typeMap.put(varName, type);
			if (result == null) {
				result = type;
			}
			// Ensure the type is registered as well
			putType(type, false);
		}
		return result;
	}
}
