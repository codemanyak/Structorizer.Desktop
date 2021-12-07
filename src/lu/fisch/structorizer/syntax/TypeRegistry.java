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

import lu.fisch.structorizer.elements.Element;
import lu.fisch.utils.StringList;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Class TypeRegistry, register for type definitions and associations
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2020-11-01      First Issue (for #800)
 *      Kay Gürtzig     2021-11-24      Method putType(String, Type, Element, int, boolean) added to allow
 *                                      synonym registrations. Synonym type definitions will be of little
 *                                      use, though as variables are not associated to type names but to
 *                                      types. So the synonym will practically be bridged.
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

/**
 * A look-up table for type definitions and associations, consists of a global and several local type
 * registers
 * @author Kay Gürtzig
 */
public class TypeRegistry {
	
	public static final class TypeRegEntry {
		public Type type;
		public Element defined = null;
		public int lineNo = -1;
		
		public TypeRegEntry(Type type) {
			this.type = type;
		}
		
		public TypeRegEntry(Type type, Element el, int line)
		{
			this.type = type;
			this.defined = el;
			if (el != null) {
				this.lineNo = line;
			}
		}
	};

	private static final Pattern BIN_PATTERN = Pattern.compile("0b[01]+");
	private static final Pattern OCT_PATTERN = Pattern.compile("0[0-7]+");
	private static final Pattern HEX_PATTERN = Pattern.compile("0x[0-9A-Fa-f]+");

	@SuppressWarnings("serial")
	private static final HashMap<String, TypeRegEntry> globalMap = new HashMap<String, TypeRegEntry>() {{
		try {
			StringList modifiers = StringList.getNew("unsigned");
			put(":dummy", new TypeRegEntry(Type.getDummyType()));
			put(":boolean", new TypeRegEntry(new PrimitiveType("boolean", Boolean.valueOf(false))));
			put(":byte", new TypeRegEntry(new PrimitiveType("byte", Byte.valueOf((byte)0))));
			put(":short", new TypeRegEntry(new PrimitiveType("short", Short.valueOf((short)0))));
			put(":int", new TypeRegEntry(new PrimitiveType("int", Integer.valueOf((int)0))));
			put(":long", new TypeRegEntry(new PrimitiveType("long", Long.valueOf((long)0))));
			put(":ubyte", new TypeRegEntry(new PrimitiveType("ubyte", modifiers, Byte.valueOf((byte)0))));
			put(":ushort", new TypeRegEntry(new PrimitiveType("ushort", modifiers, Short.valueOf((short)0))));
			put(":uint", new TypeRegEntry(new PrimitiveType("uint", modifiers, Integer.valueOf((int)0))));
			put(":ulong", new TypeRegEntry(new PrimitiveType("ulong", modifiers, Long.valueOf((long)0))));
			put(":float", new TypeRegEntry(new PrimitiveType("float", Float.valueOf((float)0.0))));
			put(":double", new TypeRegEntry(new PrimitiveType("double", Double.valueOf((double)0.0))));
			put(":char", new TypeRegEntry(new PrimitiveType("char", Character.valueOf('\0'))));
			put(":string", new TypeRegEntry(new PrimitiveType("string", new String())));
		}
		catch (Exception exc)
		{}
	}};
	
	private HashMap<String, TypeRegEntry> typeMap = null;
	
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
		typeMap = new HashMap<String, TypeRegEntry>();
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
	
	/**
	 * Retrieves the global standard type associated to the given type name
	 * @param name - type name
	 * @return the associated global {@link Type} instance or {@code null}
	 */
	public static Type getStandardType(String name)
	{
		return getGlobalInstance().getType(name);
	}
	
	/**
	 * Retrieves the matching Type Registry entry for the given literal
	 * @param literal - a literal from an element text
	 * @return either the corresponding {@code Type} instance of {@code null}
	 */
	public static Type getStandardTypeFor(String literal)
	{
		Type stdType = Type.getDummyType();
		if (Expression.BOOL_LITERALS.contains(literal)) {
			stdType = TypeRegistry.getStandardType("boolean");
		}
		// START KGU#790 2021-12-07: Issue #920
		if ("∞".equals(literal) || "Infinity".equals(literal)) {
			stdType = TypeRegistry.getStandardType("double");
		}
		// END KGU#790 2021-12-07
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
			// In this cascade the last successful attempt simply wins the game
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
	 * @see #getTypeFor(String)
	 * @see #getTypeEntryFor(String)
	 */
	public Type getType(String typeName)
	{
		TypeRegEntry entry = this.typeMap.get(":" + typeName);
		if (entry != null) {
			return entry.type;
		}
		return null;
	}

	/**
	 * Retrieves the data type for the given variable name {@code varName}.
	 * If it hadn't been registered here then returns {@code null}.
	 * @param varName - must be an identifier
	 * @return the {@link Type} or {@code null}
	 */
	public Type getTypeFor(String varName)
	{
		TypeRegEntry entry = this.typeMap.get(varName);
		if (entry != null) {
			return entry.type;
		}
		return null;
	}
	
	/**
	 * Retrieves the {@link TypeRegEntry} for the given variable name {@code varName}.
	 * If it hadn't been registered here then returns {@code null}. Otherwise the
	 * returned entry will contain the information about a possible declaring element
	 * line.
	 * @param varName - must be an identifier
	 * @return the {@link TypeRegEntry} or {@code null}
	 * @see #getTypeFor(String)
	 */
	public TypeRegEntry getTypeEntryFor(String varName)
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
	 * @see #putType(Type, Element, int, boolean)
	 */
	public Type putType(Type type, boolean force)
	{
		return putType(type, null, -1, force);
	}
	/**
	 * Registers the given {@link Type} {@code type} being defined in line
	 * {@code defLine} of Element {@code definingEl} under its name unless it
	 * is anonymous or there is already an entry with the same name. Both
	 * restrictions can be overridden with {@code force = true}.
	 * Standard types may not be overridden in any case.
	 * 
	 * @param type - the {@link Type} to be registered
	 * @param definingEl - the Element where the type is defined, or {@code null}
	 * @param defLine - number of the defining text line, or -1
	 * @param force - whether it is even to be put if it is an anonymous type
	 * or would override an existing entry (which is dangerous as references to
	 * the type might get inconsistent).
	 * @return {@code null} if the registration failed, {@code type} if the
	 * registration worked without overriding another entry, otherwise the
	 * overwritten previous {@link Type} entry.
	 * 
	 * @see #putType(String, Type, Element, int, boolean)
	 */
	public Type putType(Type type, Element definingEl, int defLine, boolean force)
	{
		// TODO: Check for equivalent type
		// TODO: Don't override an explicitly defined type, either
		Type result = null;
		String name = type.getName();
		if (getStandardType(name) == null &&
				(force || !type.isAnonymous() && !typeMap.containsKey(":" + name))) {
			TypeRegEntry oldEntry = typeMap.put(":" + name, new TypeRegEntry(type, definingEl, defLine));
			type.registry = this;
			if (oldEntry == null) {
				result = type;
			}
			else {
				result = oldEntry.type;
			}
		}
		return result;
	}
	/**
	 * Registers the given {@link Type} {@code type} being defined in line
	 * {@code defLine} of Element {@code definingEl} under the passed name
	 * {@code typeId} name unless it is anonymous or there is already an 
	 * entry with the same name.<br/>
	 * Both restrictions can be overridden with {@code force = true}.
	 * Standard types may not be overridden in any case.
	 * 
	 * @param typeId - the (synonym) type name under which {@code type} is to be
	 * additionally registered
	 * @param type - the {@link Type} to be registered
	 * @param definingEl - the Element where the type is defined or {@code null}
	 * @param defLine - number of the defining text line or -1
	 * @param force - whether it is even to be put if it is an anonymous type
	 * or would override an existing entry (which is dangerous as references to
	 * the type might get inconsistent).
	 * @return {@code null} if the registration failed, {@code type} if the
	 * registration worked without overriding another entry, otherwise the
	 * overwritten previous {@link Type} entry.
	 * @throws SyntaxException if the creation of a redirection type is illegal
	 * (e.g. because of a 
	 * 
	 * @see #putType(Type, Element, int, boolean)
	 */
	public Type putType(String typeId, Type type, Element definingEl, int defLine, boolean force) throws SyntaxException
	{
		// TODO: Check for equivalent type
		// TODO: Don't override an explicitly defined type, either
		String name = type.getName();
		if (type instanceof RedirType && (typeId == null || typeId.equals(name))) {
			// The caller seems to have prepared a redirection type already
			return putType(type, definingEl, defLine, force);
		}
		Type result = null;
		if (getStandardType(typeId) == null &&
				(force || !type.isAnonymous() && !typeMap.containsKey(":" + typeId))) {
			Type oldType = getType(type.getName());
			Type refType = type;
			if (oldType == null) {
				refType = new RedirType(typeId, type);
				typeMap.put(":" + typeId, new TypeRegEntry(refType, definingEl, defLine));
				refType.registry = this;
			}
			if (oldType == null) {
				result = refType;
			}
			else {
				result = oldType;
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
		return putTypeFor(varName, type, null, -1, force);
	}
	public Type putTypeFor(String varName, Type type, Element declaringEl, int declLine, boolean force)
	{
		Type result = null;
		Type prevType = null;
		if (force || !typeMap.containsKey(varName)
				&& ((prevType = getType(type.getName())) == null || prevType == type)) {
			// TODO: Check for equivalent type
			TypeRegEntry entry = typeMap.put(varName, new TypeRegEntry(type, declaringEl, declLine));
			type.registry = this;
			if (entry == null) {
				result = type;
			}
			else {
				result = entry.type;
			}
			// Ensure the type is registered as well
			putType(type, false);
		}
		return result;
	}
}
