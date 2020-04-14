package lu.fisch.structorizer.syntax;

import lu.fisch.utils.StringList;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Base class for type descriptions
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     20198-12-19     First Issue
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

public class Type implements Entity {

	protected static final String dummy = "???";
	
	protected String name = "";
	protected StringList modifiers = null;

	/**
	 * Extracts name and modifiers from the given type specification tokens
	 * {@code specTokens}. Assumed cases:<br/>
	 * <ul>
	 * <li>[mod1 mod2 ...] name</li>
	 * </ul>
	 * @param specTokens blank-free token list
	 */
	public Type(StringList specTokens) {
		// TODO: somewhat rough, we ought to check for non-identifiers
		int length = specTokens.count();
		if (length >= 1) {
			this.name = specTokens.get(length-1);
			this.modifiers = specTokens.subSequence(0, length-1);
		}
	}

	/**
	 * Constructs the type from the given {@code name} and {@code modifiers}
	 * list (which will be reduced, i.e. blanks and empty parts will be removed.
	 * @param name - type name
	 * @param modifiers - list of modifiers or null
	 */
	public Type(String name, StringList modifiers) {
		this.name = (name != null ? name.trim() : "");
		if (modifiers != null) {
			modifiers.copy();
			modifiers.removeAll(" ");
			modifiers.removeAll("");
		}
		this.modifiers = modifiers;
	}
	
	public String getName()
	{
		return name;
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
		if (!this.name.trim().isEmpty()) {
			mods.add(this.name);
		}
		else {
			mods.add(dummy);
		}
		return mods.getLongString();
	}

}
