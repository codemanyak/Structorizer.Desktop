package lu.fisch.structorizer.syntax;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    New base class for type descriptions
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2019-12-19      First Issue (#800)
 *      Kay Gürtzig     2020-11-01      Name check inserted
 *      Kay Gürtzig     2020-11-06      Uplink to a TypeRegistry added
 *      Kay Gürtzig     2021-10-17      Field modifiers removed
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

/**
 * Base class for data type description in Structorizer. Can be used as mere
 * type name reference (while an owning {@link TypeRegistry} is not available,
 * to be replaced by a specified Type subclass object as soon as possible).
 * @author Kay Gürtzig
 */
public class Type {

	/**
	 * Dummy type for unspecified types
	 * @see #getDummyType()
	 */
	private static Type dummyType = null;
	
	/**
	 * A counter for anonymous types
	 */
	private static long nextId = 0;
	
	/**
	 * The type identifier, will be tested to adhere to identifier syntax
	 */
	protected String name = "";
	/**
	 * {@code null} or a list of modifiers (each of which ought to be an identifier)
	 */
	
	/**
	 * Link to the owning {@link TypeRegistry}
	 */
	protected TypeRegistry registry = null;
	
	/** Internal constructor, only used by {@link #getDummyType()} */
	private Type()
	{
		this.name = "???";
	}

	/**
	 * Constructs the type from the given {@code name}.
	 * @param name - type name, must be an Ascii identifier
	 * @throws SyntaxException if {@code name} does not fit to identifier syntax
	 */
	public Type(String name) throws SyntaxException {
		if (name == null) {
			name = "%" + nextId++;	// Create an anonymous name
		}
		else if (!Syntax.isIdentifier(name, true, null)) {
			throw new SyntaxException("Type name must be an Ascii identifier", 0);
		}
		this.name = name.trim();
	}
	
	/**
	 * @return the dummy type, used as default for unspecified types
	 */
	public static Type getDummyType()
	{
		if (dummyType == null) {
			dummyType = new Type();
		}
		return dummyType;
	}
	
	/**
	 * @return the name of this type (where the name starts with "AnonType" in case of
	 * an anonymous type)
	 */
	public String getName()
	{
		return name.replace("%", "AnonType");
	}
	
	/**
	 * Answers a string containing a concise, human-readable description of the receiver.
	 * @return a printable (shallow) symbolic type specification for the receiver.
	 * @see #toString(boolean)
	 */
	@Override
	public String toString()
	{
		return this.toString(false);
	}
	
	/**
	 * Returns a string expressing the type structure either in a shallow way
	 * ({@code deep = false}) or in a completely recursive way ({@code deep = true}).
	 * On this level, the result will just be the sequence of modifiers and (as tail)
	 * the name.
	 * @param deep - whether possible substructure is to be fully described (otherwise
	 * embedded types will just be represented by their names (if the are named).
	 * @return the composed string
	 * @see #toString()
	 */
	public String toString(boolean deep)
	{
		return toStringWithName(getName(), deep);
	}
	
	/**
	 * Returns a string expressing name and type structure either in a shallow way
	 * ({@code deep = false}) or in a completely recursive way ({@code deep = true}).
	 * The result will either involve the passed-in {@code altName}, or the internal
	 * name if {@code altName} is {@code null}.
	 * @param altName - an alternative name to be used instead of {@link #getName()},
	 * if {@code null} then the internal identifier will be used.
	 * @param deep - whether possible substructure is to be fully described (otherwise
	 * embedded types will just be represented by their names (if the are named).
	 * @return the composed string
	 * @see #toString()
	 */
	protected String toStringWithName(String altName, boolean deep)
	{
		return altName != null ? altName : this.name;
	}
	
	/**
	 * Convenience method for internal retrieval of referenced types within
	 * the same {@link TypeRegistry}.
	 * @param name
	 * @return the type corresponding to the given  or {@code null}
	 */
	protected Type getType(String typeName)
	{
		Type foundType = null;
		if (this.registry != null) {
			foundType = this.registry.getType(typeName);
		}
		return foundType;
	}
	
	/**
	 * @return {@code true} if this type represents numeric values
	 * @see #isPrimitive()
	 * @see #isAnonymous()
	 */
	public boolean isNumeric()
	{
		return false;
	}
	
	/**
	 * @return {@code true} if this type represents a primitive data type
	 * @see #isNumeric()
	 * @see #isAnonymous()
	 */
	public boolean isPrimitive()
	{
		return false;
	}

	/**
	 * @return {@code true} iff this is the dummy type or a nameless type
	 * @see #isDummy()
	 */
	public boolean isAnonymous()
	{
		return isDummy() || name.startsWith("%");
	}
	
	/**
	 * @return {@code true} iff this is the dummy type or a nameless type
	 * @see #isAnonymous()
	 */
	public boolean isDummy()
	{
		return "???".equals(name);
	}
	
	/**
	 * @return {@code true} if the type is composed
	 */
	public boolean isStructured()
	{
		return false;
	}
	
	/**
	 * Checks whether this type and {@code another} are structurally equal
	 * and have the same name. In case of anonymous types, the comparison
	 * of the top-level name will be suppressed.<br/>
	 * (The comparison is simply done via the textual representation.)
	 * @param another - the type to compare with
	 * @return {@code true} if both types are structurally equivalent.
	 */
	public boolean equals(Type another)
	{
		if (this == another) {
			return true;
		}
		String str1 = toString(true);
		String str2 = another.toString(true);
		boolean equiv = str1.equals(str2);
		if (!equiv) {
			// In case of anonymous types compare with equalised nmes
			if (isAnonymous()) {
					str1 = toStringWithName(another.getName(), true);
			}
			else if (another.isAnonymous()) {
				str2 = another.toStringWithName(this.getName(), true);
			}
			equiv = str1.equals(str2);
		}
		return equiv;
	}
}
