package lu.fisch.structorizer.syntax;

import lu.fisch.utils.StringList;

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
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

/**
 * Base class for data type description in Structorizer
 * @author Kay Gürtzig
 */
public class Type {

	/**
	 * Name to be shown for unspecified types
	 */
	protected static final String dummy = "???";

	/**
	 * A counter for anonymous types
	 */
	private static long id = 0;
	
	/**
	 * The type identifier, will be tested to adhere to identifier syntax
	 */
	protected String name = "";
	/**
	 * {@code null} or a list of modifiers (each of which ought to be an identifier)
	 */
	protected StringList modifiers = null;

	/**
	 * Extracts name and modifiers from the given type specification tokens
	 * {@code specTokens}. Assumed cases:<br/>
	 * <ul>
	 * <li>[mod1 mod2 ...] name</li>
	 * </ul>
	 * @param specTokens blank-free token list
	 * @throws SyntaxException if the name does not fit to identifier syntax
	 */
	public Type(StringList specTokens) throws SyntaxException {
		// TODO: somewhat rough, we ought to check for non-identifiers
		int length = specTokens.count();
		if (length >= 1) {
			this.name = specTokens.get(length-1);
			if (!Syntax.isIdentifier(this.name, true, null)) {
				throw new SyntaxException("Type name must be an Ascii identifier", 0);
			}
			this.modifiers = specTokens.subSequence(0, length-1);
		}
	}

	/**
	 * Constructs the type from the given {@code name} and {@code modifiers}
	 * list (which will be reduced, i.e. blanks and empty parts will be removed).
	 * @param name - type name, must be an Ascii identifier
	 * @param modifiers - list of modifiers or null
	 * @throws SyntaxException if {@code name} does not fit to identifier syntax
	 */
	public Type(String name, StringList modifiers) throws SyntaxException {
		if (name == null) {
			name = "#" + id++;	// Create an anonymous name
		}
		else if (!Syntax.isIdentifier(name, true, null)) {
			throw new SyntaxException("Type name must be an Ascii identifier", 0);
		}
		this.name = name.trim();
		if (modifiers != null) {
			modifiers = new StringList(modifiers);
			modifiers.removeAll(" ");
			modifiers.removeAll("");
			for (int i = 0; i < modifiers.count(); i++) {
				modifiers.set(i, modifiers.get(i).trim());
			}
		}
		this.modifiers = modifiers;
	}
	
	public String getName()
	{
		if (name.equals("#0")) {
			return dummy;
		}
		return name.replace("#", "AnonType");
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
		StringList mods = this.modifiers;
		if (mods == null) {
			mods = new StringList();
		}
		mods.add(getName());
		return mods.getLongString();
	}
	
	/**
	 * @return {@code true} if this type represents numeric values
	 */
	public boolean isNumeric()
	{
		return false;
	}
	
	/**
	 * @return {@code true} if this type represents a primitive data type
	 */
	public boolean isPrimitive()
	{
		return false;
	}

	public boolean isAnonymous()
	{
		return name.startsWith("#");
	}
}
