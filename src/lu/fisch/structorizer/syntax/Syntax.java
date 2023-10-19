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

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Singleton class as central provider of syntactic routines and representations
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2020-08-12      First Issue
 *      Kay Gürtzig     2020-11-01      Further methods from Element and Function moved hitherto
 *      Kay Gürtzig     2021-10-25      Method removeDecorators now replaces Element.cutOutRedundantMarkers
 *      Kay Gürtzig     2021-12-05      Fundamental redesign of splitLexically(): final automaton instead of
 *                                      repeated splitting (much more efficient), direct elimination of
 *                                      whitespace enabled (also way more efficient than posterior deletion)
 *      Kay Gürtzig     2021-12-08      Redesign of unifyOperators()
 *      Kay Gürtzig     2022-08-20      Enh. #1066: New static method retrieveComponentNames()
 *      Kay Gürtzig     2022-08-23      Bugfix #1068: splitExpressionList returned wrong results with empty lists
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import lu.fisch.structorizer.elements.TypeMapEntry;
import lu.fisch.structorizer.io.Ini;
import lu.fisch.utils.StringList;

/**
 * This class is not intended to be instanced but serves as a library of static
 * methods to do lexical scanning and syntactic checks.
 * 
 * @author Kay Gürtzig
 */
public class Syntax {

	// START KGU#790 2021-12-05 Obsolete with redesign of splitLexically
//	private static final Pattern FLOAT_PATTERN1 = Pattern.compile("[0-9]+([eE][0-9]+)?");
//	private static final Pattern FLOAT_PATTERN2 = Pattern.compile("[0-9]+[eE]");
//	private static final Pattern INT_PATTERN = Pattern.compile("[0-9]+");
//	private static final Pattern SIGN_PATTERN = Pattern.compile("[+-]");
	public static final Pattern STRING_PATTERN = Pattern.compile("(^\\\".*\\\"$)|(^\\\'.*\\\'$)");
//	// START KGU#425 2017-09-29: Lexical core mechanisms revised
//	private static final String[] LEXICAL_DELIMITERS = new String[] {
//			" ",
//			"\t",
//			"\n",
//			".",
//			",",
//			";",
//			"(",
//			")",
//			"[",
//			"]",
//			// START KGU#100 2016-01-14: We must also catch the initialiser delimiters
//			"{",
//			"}",
//			// END KGU#100 2016-01-14
//			"-",
//			"+",
//			"/",
//			"*",
//			">",
//			"<",
//			"=",
//			":",
//			"!",
//			"'",
//			"\"",
//			"\\",
//			"%",
//			// START KGU#790 2020-10-31: Issue #800 We need the bitwise and and the address operator too
//			"&",
//			"|",
//			"~",
//			// END KGU#790 2020-10-31
//			// START KGU#920 2021-02-03: Enh. #920 We allow ∞ as synonym for Infinity
//			"\u221E",
//			// END KGU#920 2021-02-03
//			// START KGU#331 2017-01-13: Enh. #333 Precaution against unicode comparison operators
//			"\u2260", // '≠'
//			"\u2264", // '≤'
//			"\u2265"  // '≥'
//			// END KGU#331 2017-01-13
//	};
//	// END KGU#425 2017-09-29
	// END KGU#790 2021-12-05


//	private static Syntax instance = null;
//	
//	/**
//	 * Maps Element IDs to vectors of syntactical representations of the
//	 * respective unbroken lines of the Element text.
//	 */
//	private HashMap<Long, Line[]> syntaxMap = new HashMap<Long, Line[]>();
//	
//	/**
//	 * @return the instance of this class
//	 */
//	public static Syntax getInstance()
//	{
//		if (instance == null) {
//			instance = new Syntax();
//		}
//		return instance;
//	}
	
	// START KGU#165 2016-03-25: Once and for all: It should be a transparent choice, ...
	/**
	 * whether or not the keywords are to be handled in a case-independent way
	 */
	public static boolean ignoreCase = true;
	// END KGU#165 2016-03-25
	
	/**
	 * Look-up table to unify operator (and some other) symbols in a token list or
	 * Expression instance
	 */
	protected static final HashMap<String, String> UNIFICATION_MAP = new HashMap<String, String>();
	static {
		UNIFICATION_MAP.put(":=", "<-");
		UNIFICATION_MAP.put("\u2190", "<-");
		UNIFICATION_MAP.put("=", "==");
		UNIFICATION_MAP.put("<>", "!=");
		UNIFICATION_MAP.put("\u221E", "!=");
		UNIFICATION_MAP.put("mod", "%");
		UNIFICATION_MAP.put("shl", "<<");
		UNIFICATION_MAP.put("shr", ">>");
		UNIFICATION_MAP.put("and", "&&");
		UNIFICATION_MAP.put("or", "||");
		UNIFICATION_MAP.put("not", "!");
		UNIFICATION_MAP.put("not", "!");
		UNIFICATION_MAP.put("xor", "^");
		UNIFICATION_MAP.put("\u2260", "!=");
		UNIFICATION_MAP.put("\u2264", "<=");
		UNIFICATION_MAP.put("\u2265", ">=");
		UNIFICATION_MAP.put("\u221E", "Infinity");
	}

	// START KGU#288 2016-11-06: Issue #279: Access limited to private, compensated by new methods
	//public static final HashMap<String, String> keywordMap = new LinkedHashMap<String, String>();
	/** Maps formal keyword keys like "preAlt" to the actually configured keywords (as strings) */
	private static final HashMap<String, String> keywordMap = new LinkedHashMap<String, String>();
	// END KGU#288 2016-11-06
	static {
		keywordMap.put("preAlt",     "");
		keywordMap.put("postAlt",    "");
		keywordMap.put("preCase",    "");
		keywordMap.put("postCase",   "");
		keywordMap.put("preFor",     "for");
		keywordMap.put("postFor",    "to");
		keywordMap.put("stepFor",    "by");
		keywordMap.put("preForIn",   "foreach");
		keywordMap.put("postForIn",  "in");
		keywordMap.put("preWhile",   "while");
		keywordMap.put("postWhile",  "");
		keywordMap.put("preRepeat",  "until");
		keywordMap.put("postRepeat", "");
		keywordMap.put("preLeave",   "leave");
		keywordMap.put("preReturn",  "return");
		keywordMap.put("preExit",    "exit");
		// START KGU#686 2019-03-18: Enh. #56
		keywordMap.put("preThrow",   "throw");
		// END KGU#686 2019-03-18
		keywordMap.put("input",      "INPUT");
		keywordMap.put("output",     "OUTPUT");
	}

	// START KGU 2016-03-29: For keyword detection improvement
	/** Like {@link #keywordMap} but holding the tokenized keywords (lazy initialisation) */
	private static HashMap<String, StringList> splitKeywords = new HashMap<String, StringList>();
	// END KGU 2016-03-29
	
	// START KGU 2021-10-25: More efficient implementation of ex Element.cutOutRedundantMarkers()
	private static final String[] DECORATOR_KEYS = {
			"preAlt", "preCase", "preWhile", "preRepeat",
			"postAlt", "postCase", "postWhile", "postRepeat"
			};
	/** Holds the split redundant keywords (i.e. the mere "decorators") by growing length */
	// Use lazy initialisation
	private static ArrayList<StringList> splitDecorators = null;
	// END KGU 2021-10-25

	// START KGU#466 2019-08-02: Issue #733 - Support selective preference export
	public static String[] getPreferenceKeys()
	{
		return new String[] {"Parser*"};
	}
	// END KGU#466 2019-08-02

	/**
	 * Loads the parser-related preferences (i.e. chiefly the configured parser keywords)
	 * from the Ini file into the internal cache.
	 * @see #getPropertyMap(boolean)
	 * @see #saveToINI()
	 */
	public static void loadFromINI()
	{
		/* Define some defaults for all mandatory keywords and some decorators
		 * before loading the configured keyword set from the Ini file
		 */
		final HashMap<String, String> defaultKeys = new HashMap<String, String>();
		// START KGU 2017-01-06: Issue #327: Defaults changed to English
		defaultKeys.put("ParserPreFor", "for");
		defaultKeys.put("ParserPostFor", "to");
		defaultKeys.put("ParserStepFor", "by");
		defaultKeys.put("ParserPreForIn", "foreach");
		defaultKeys.put("ParserPostForIn", "in");
		defaultKeys.put("ParserPreWhile", "while ");
		defaultKeys.put("ParserPreRepeat", "until ");
		defaultKeys.put("ParserPreLeave", "leave");
		defaultKeys.put("ParserPreReturn", "return");
		defaultKeys.put("ParserPreExit", "exit");
		defaultKeys.put("ParserInput", "INPUT");
		defaultKeys.put("ParserOutput", "OUTPUT");
		// END KGU 2017-01-06 #327
		// START KGU#376 2017-04-11: Enh. #389
		defaultKeys.put("ParserPreImport", "include");
		// END KGU#376 2017-04-11
		// START KGU#686 2019-03-18: Enh. #56
		defaultKeys.put("ParserPreThrow", "throw");
		// END KGU#686 2019-03-18
		try
		{
			Ini ini = Ini.getInstance();
			ini.load();

			splitKeywords.clear();
			// START KGU#790 2021-10-25: Issue #800
			splitDecorators = null;
			// END KGU#790 2021-10-25
			for (String key: keywordMap.keySet())
			{
				String propertyName = "Parser" + Character.toUpperCase(key.charAt(0)) + key.substring(1);
				if (defaultKeys.containsKey(propertyName))
				{
					keywordMap.put(key, ini.getProperty(propertyName, defaultKeys.get(propertyName)));
				}
				else
				{
					keywordMap.put(key, ini.getProperty(propertyName, ""));
				}
			}
			// START KGU#659 2019-02-19: Bugfix #684 - An empty FOR-IN keyword (legacy) meant equality with FOR loop keyword 
			if (keywordMap.get("preForIn").trim().isEmpty()) {
				keywordMap.put("preForIn", keywordMap.get("preFor"));
			}
			// END KGU#659 2019-02-19

			// START KGU#165 2016-03-25: Enhancement configurable case awareness
			ignoreCase = ini.getProperty("ParserIgnoreCase", "true").equalsIgnoreCase("true");
			// END KGU#3 2016-03-25

		}
		catch (Exception e)
		{
			Logger.getLogger(Syntax.class.getName()).log(Level.WARNING, "Ini", e);
		}
	}
	
	/**
	 * Saves the parser-related preferences, i.e. chiefly the configured keywords to the
	 * Ini file.
	 * @see #getPropertyMap(boolean)
	 * @see #loadFromINI()
	 */
	public static void saveToINI()
	{
		try
		{
			Ini ini = Ini.getInstance();
			ini.load();			// elements
			for (Map.Entry<String, String> entry: getPropertyMap(true).entrySet())
			{
				String propertyName = "Parser" + Character.toUpperCase(entry.getKey().charAt(0)) + entry.getKey().substring(1);
				ini.setProperty(propertyName, entry.getValue());
			}

			ini.save();
		}
		catch (Exception e)
		{
			Logger.getLogger(Syntax.class.getName()).log(Level.WARNING, "Ini", e);
		}
	}

	// START KGU#163 2016-03-25: For syntax analysis purposes
	/**
	 * Returns the complete set of configurable parser keywords for Elements
	 * @return array of current keyword strings
	 */
	public static String[] getAllProperties()
	{
		return keywordMap.values().toArray(new String[keywordMap.size()]);
	}
	// END KGU#163 2016-03-25

	// START KGU#258 2016-09-25: Enh. #253 (temporary workaround for the needed Hashmap)
	/**
	 * Returns a {@link Hashmap} mapping parser preference labels like "preAlt" to the
	 * configured parser preference keywords.
	 * @param includeAuxiliary - whether or not non-keyword settings (like "ignoreCase") are to be included
	 * @return the hash table with the current settings
	 */
	public static final HashMap<String, String> getPropertyMap(boolean includeAuxiliary)
	{
		HashMap<String, String> keywords = keywordMap;
		if (includeAuxiliary)
		{
			keywords = new HashMap<String,String>(keywordMap);
			// The following information may be important for a correct search
			keywords.put("ignoreCase",  Boolean.toString(ignoreCase));
		}
		return keywords;
	}
	// END KGU#258 2016-09-25

	// START KGU#288 2016-11-06: New methods to facilitate bugfix #278, #279
	/**
	 * @return the set of the (internal) parser preference names (the keys of the map)
	 */
	public static Set<String> keywordSet()
	{
		return keywordMap.keySet();
	}

	/**
	 * Returns the cached keyword for parser preference {@code _key} or {@code null}
	 * @param _key - the name of the requested parser preference
	 * @return the cached keyword or {@code null}
	 */
	public static String getKeyword(String _key)
	{
		return keywordMap.get(_key);
	}

	/**
	 * Returns the cached keyword for parser preference {@code _key} or the given {@code _defaultVal}
	 * if no entry or only an empty entry is found for {@code _key}.
	 * @param _key - the name of the requested parser preference
	 * @param _defaultVal - a default keyword to be returned if there is no non-empty cached value
	 * @return the cached or default keyword
	 */
	public static String getKeywordOrDefault(String _key, String _defaultVal)
	{
		return keywordMap.getOrDefault(_key, _defaultVal);
	}
	
	/**
	 * Returns a tokenized form of the configured parser preference for the
	 * symbolic key {@code _key} if it exists - for more precise comparison.
	 * Works with lazy initialisation.
	 * @param _key - a symbolic keyword name
	 * @return either a token sequence or {@code null}.
	 */
	public static StringList getSplitKeyword(String _key)
	{
		StringList tokens = splitKeywords.get(_key);
		if (tokens == null) {
			String keyword = getKeyword(_key);
			if (keyword != null) {
				tokens = splitLexically(keyword, false);
				splitKeywords.put(_key, tokens);
			}
		}
		return tokens;
	}

	/**
	 * Replaces the cached parser preference {@code _key} with the new keyword
	 * {@code _keyword} for this session.<br/>
	 * Note:
	 * <ol>
	 * <li>
	 * This does NOT influence the Ini file, not even the Ini properties!
	 * </li>
	 * <li>
	 * Only for existing keys a new mapping may be set
	 * </li>
	 * </ol>
	 * @param _key - name of the parser preference
	 * @param _keyword - new value of the parser preference or null
	 */
	public static void setKeyword(String _key, String _keyword)
	{
		if (_keyword == null) {
			_keyword = "";
		}
		// Bugfix #281/#282
		if (keywordMap.containsKey(_key)) {
			keywordMap.put(_key, _keyword);
			splitKeywords.put(_key, splitLexically(_keyword, false));
			// START KGU#790 2021-10-25: Issue #800
			splitDecorators = null;
			// END KGU#790 2021-10-25
		}
	}
	// END KGU#288 2016-11-06
	
	// START KGU#162 2016-03-31/2021-10-25: Enh. #144 - undispensible part of transformIntermediate
	/**
	 * Removes redundant decorator keywords from the passed-in token list.
	 * (Undispensible part of {@link Element#transformIntermediate(String)}.)
	 * @param _tokens - the token list to be cleansed.
	 */
	public static void removeDecorators(StringList _tokens)
	{
		// Collect redundant placemarkers to be deleted from the text
		if (splitDecorators == null) {
			// We must first establish the sorted decorator list
			StringList keys = new StringList();
			StringList decorators = new StringList();
			for (String key: DECORATOR_KEYS) {
				int ix = decorators.addByLength(getKeyword(key));
				if (ix >= 0) {
					keys.insert(key, ix);
				}
			}
			splitDecorators = new ArrayList<StringList>();
			for (int ix = 0; ix < keys.count(); ix++) {
				splitDecorators.add(Syntax.getSplitKeyword(keys.get(ix)));
			}
		}

		for (int i = 0; i < splitDecorators.size(); i++)
		{
			StringList markerTokens = splitDecorators.get(i);
			int markerLen = markerTokens.count();
			int pos = -1;
			while ((pos = _tokens.indexOf(markerTokens, 0, !Syntax.ignoreCase)) >= 0)
			{
				_tokens.remove(pos, pos + markerLen);
			}
		}
	}
	// END KGU#162 2016-03-31

	
	// START KGU#790 2020-11-01: Moved hitherto from Function.testIdentifier() and renamed
	/**
	 * Checks identifier syntax (i.e. ASCII letters, digits, underscores, and possibly dots)
	 * @param _str - the identifier candidate
	 * @param _strictAscii - whether non-ascii letters are to be rejected
	 * @param _alsoAllowedChars - a String containing additionally accepted characters (e.g. ".") or null
	 * @return true iff _str complies with the strict identifier syntax convention (plus allowed characters)
	 */
	public static boolean isIdentifier(String _str, boolean _strictAscii, String _alsoAllowedChars)
	{
		_str = _str.trim().toLowerCase();
		// START KGU#877 2020-10-16: Bugfix #874 - we should tolerate non-ascii letters
		//boolean isIdent = !_str.isEmpty() &&
		//		('a' <= _str.charAt(0) && 'z' >= _str.charAt(0) || _str.charAt(0) == '_');
		boolean isIdent = false;
		if (!_str.isEmpty()) {
			char firstChar = _str.charAt(0);
			isIdent = ('a' <= firstChar && firstChar <= 'z')
					|| !_strictAscii && Character.isLetter(firstChar)
					|| firstChar == '_';
		}
		// END KGU#877 2020-10-16
		if (_alsoAllowedChars == null)
		{
			_alsoAllowedChars = "";
		}
		for (int i = 1; isIdent && i < _str.length(); i++)
		{
			char currChar = _str.charAt(i);
			if (!(
					('a' <= currChar && currChar <= 'z')
					// START KGU#877 2020-10-16: Bugfix #874 - we should tolerate non-ascii letters
					||
					!_strictAscii && Character.isLetter(currChar)
					// END KGU#877 2020-10-16
					||
					('0' <= currChar && currChar <= '9')
					||
					(currChar == '_')
					||
					_alsoAllowedChars.indexOf(currChar) >= 0
					))
				// END KGU 2015-11-25
			{
				isIdent = false;
			}
		}
		return isIdent;
	}
	// END KGU#790 2020-11-01

	
	// START KGU#18/KGU#23 2015-11-04: Lexical splitter extracted from writeOutVariables
	/**
	 * Splits the given _text into lexical morphemes (lexemes). This will possibly overdo
	 * somewhat (e. g. signs of number literals will be separated, but floating-point literals
	 * like 123.45 or .09e-8 will properly be preserved as contiguous tokens).<br/>
	 * By default, {@code _restoreStrings} should be set {@code true}, which ensures that
	 * string and character literals will be preserved (for keyword preferences, however,
	 * which possibly contain quotes like {@code "jusqu'à"} it may be necessary to set it
	 * {@code false}<br/>
	 * Note that inter-lexeme whitespace will <b>not</b> be eliminated but forms elements of
	 * the result, more precisely: a sequence of whitespace characters (like {@code " \t  \n"})
	 * will form a contiguous token. Whitespace tokens may be removed afterwards with method
	 * {@link StringList#removeBlanks()}. Alternatively, inter-lexeme whitespace may be
	 * suppressed by using {@link #splitLexically(String, boolean, boolean)} in the first place.
	 * 
	 * @param _text - String to be exploded into lexical units
	 * @param _restoreStrings - if {@code true} then accidently split numeric and string literals
	 *        will be reassembled 
	 * @return StringList consisting of the separated lexemes (including contiguous whitespace
	 *        sequences).
	 * 
	 * @see #splitLexically(String, boolean, boolean)
	 * @see StringList#removeBlanks()
	 */
	public static StringList splitLexically(String _text, boolean _restoreStrings)
	{
		// START KGU#790 2021-12-05: Issue #800 replaced by a more efficient version (factor ~10)
//		StringList parts = new StringList();
//		parts.add(_text);
//		
//		// split
//		for (int i = 0; i < LEXICAL_DELIMITERS.length; i++) {
//			parts = StringList.explodeWithDelimiter(parts, LEXICAL_DELIMITERS[i]);
//		}
//
//		// reassemble symbols
//		int i = 0;
//		while (i < parts.count())
//		{
//			String thisPart = parts.get(i);
//			if (i < parts.count()-1)
//			{
//				String nextPart = parts.get(i+1);
//				boolean isInt = false;
//				boolean isSign = false;
//				boolean isEllipse = false;
//				if (thisPart.equals("<") && nextPart.equals("-"))
//				{
//					parts.set(i,"<-");
//					parts.delete(i+1);
//					// START KGU 2014-10-18 potential three-character assignment symbol?
//					if (i < parts.count()-1 && parts.get(i+1).equals("-"))
//					{
//						parts.delete(i+1);
//					}
//					// END KGU 2014-10-18
//				}
//				else if (thisPart.equals(":") && nextPart.equals("="))
//				{
//					parts.set(i,":=");
//					parts.delete(i+1);
//				}
//				else if (thisPart.equals("!") && nextPart.equals("="))
//				{
//					parts.set(i,"!=");
//					parts.delete(i+1);
//				}
//				// START KGU 2015-11-04
//				else if (thisPart.equals("=") && nextPart.equals("="))
//				{
//					parts.set(i,"==");
//					parts.delete(i+1);
//				}
//				// END KGU 2015-11-04
//				else if (thisPart.equals("<"))
//				{
//					if (nextPart.equals(">"))
//					{
//						parts.set(i,"<>");
//						parts.delete(i+1);
//					}
//					else if (nextPart.equals("="))
//					{
//						parts.set(i,"<=");
//						parts.delete(i+1);
//					}
//					// START KGU#92 2015-12-01: Bugfix #41
//					else if (nextPart.equals("<"))
//					{
//						parts.set(i,"<<");
//						parts.delete(i+1);
//					}
//					// END KGU#92 2015-12-01
//				}
//				else if (thisPart.equals(">"))
//				{
//					if (nextPart.equals("="))
//					{
//						parts.set(i,">=");
//						parts.delete(i+1);
//					}
//					// START KGU#92 2015-12-01: Bugfix #41
//					else if (nextPart.equals(">"))
//					{
//						parts.set(i,">>");
//						parts.delete(i+1);
//					}
//					// END KGU#92 2015-12-01
//				}
//				// START KGU#24 2014-10-18: Logical two-character operators should be detected, too ...
//				else if (thisPart.equals("&") && nextPart.equals("&"))
//				{
//					parts.set(i,"&&");
//					parts.delete(i+1);
//				}
//				else if (thisPart.equals("|") && nextPart.equals("|"))
//				{
//					parts.set(i,"||");
//					parts.delete(i+1);
//				}
//				// END KGU#24 2014-10-18
//				// START KGU#26 2015-11-04: Find escaped quotes
//				else if (thisPart.equals("\\"))
//				{
//					if (nextPart.equals("\""))
//					{
//						parts.set(i, "\\\"");
//						parts.delete(i+1);
//					}
//					// START KGU#344 201702-08: Issue #341 - Precaution against string/character delimiter replacement
//					else if (nextPart.equals("'"))
//					{
//						parts.set(i, "\\'");
//						parts.delete(i+1);
//					}
//					// END KGU#344 2017-02-08
//					else if (nextPart.equals("\\"))
//					{
//						parts.set(i, "\\\\");
//						parts.delete(i+1);
//					}
//				}
//				// END KGU#26 2015-11-04
//				// START KGU#331 2017-01-13: Enh. #333 Precaution against unicode comparison operators
//				else if (thisPart.equals("\u2260")) {
//					parts.set(i, "<>");
//				}
//				else if (thisPart.equals("\u2264")) {
//					parts.set(i, "<=");
//				}
//				else if (thisPart.equals("\u2265")) {
//					parts.set(i, ">=");
//				}
//				// END KGU#331 2017-01-13
//				// START KGU#335/KGU#425 2017-09-29: Re-compose floating-point literals (including those starting or ending with ".")
//				// These are legal cases ($ = line end, ? = don't care):
//				// i             i+1             i+2           i+3        comment
//				// .              .               ?             ?         two-dot-ellipse (Pascal range)
//				// .              .               .             ?         three-dot-ellipse (rarely used)
//				// .            FLOAT1            ?             ?         float literal
//				// .            FLOAT2           [+-]        [0-9]+       float literal
//				// [+-]           .            FLOAT1           ?         float literal - reduce this case the the one -2
//				// [+-]           .            FLOAT2         [+-] [0-9]+ float literal - reduce this case the the one -2
//				// [0-9]+         .            FLOAT1           ?         float literal - reduce this case the the one -4
//				// [0-9]+         .            FLOAT2         [+-] [0-9]+ float literal - reduce this case the the one -4
//				// These are the illegal cases:
//				// [+-]           .               $
//				// [+-]           .               ?
//				// [0-9]+         .               .
//				// So we will first do the necessary lookahead before we manipulate parts
//				else if ( (isEllipse = thisPart.equals("."))	// a single dot might merge with another one or a float pattern
//						|| (	// Otherwise a digit sequence might melt with a dot
//								(isInt = INT_PATTERN.matcher(thisPart).matches())
//								|| (isSign = (thisPart.equals("+") || thisPart.equals("-"))	// a sign with a dot requires more...
//										&& i+2 < parts.count())
//								&& nextPart.equals(".")) 
//						) {
//					int nDelete = 0;
//					// Glue the two together - the only pathologic case would be 
//					if (nextPart.equals(".")) {
//						thisPart += nextPart;
//						nDelete = 1;
//						// Is there anything left at all?
//						if (i+2 < parts.count()) {
//							nextPart = parts.get(i+2);
//						}
//						if (isEllipse && nextPart.equals(".")) {
//							// Okay, then be it a three-point ellipse "..."
//							thisPart += nextPart;
//							nDelete++;
//						}
//						// In case of an ellipse we are done here
//					}
//					else {
//						isEllipse = false;
//					}
//					// nextPart.matches("[0-9]+([eE][0-9]+)?")
//					if (!isEllipse && FLOAT_PATTERN1.matcher(nextPart).matches()) {
//						thisPart += nextPart;
//						nDelete++;
//					}
//					// nextPart.matches("[0-9]+[eE]")
//					else if (!isEllipse && FLOAT_PATTERN2.matcher(nextPart).matches()
//							&& i+nDelete+3 < parts.count()
//							&& SIGN_PATTERN.matcher(parts.get(i+nDelete+2)).matches()
//							&& INT_PATTERN.matcher(parts.get(i+nDelete+3)).matches()) {
//						for (int j = 1; j <= 3; j++) {
//							nDelete++;
//							thisPart += parts.get(i+nDelete);
//						}
//					}
//					else if (isSign || isInt && i+2 < parts.count() && parts.get(i+2).equals(".")) {
//						// In this case the amalgamation may not take place
//						nDelete = 0;
//					}
//					// Now carry out the amalgamation if sensible
//					if (nDelete > 0) {
//						parts.set(i, thisPart);
//						parts.remove(i+1, i+nDelete+1);
//					}
//				}
//				// END KGU#335/KGU#425 2017-09-29
//			}
//			i++;
//		}
//		
//		if (_restoreStrings)
//		{
//			// START KGU#344 2017-02-07: Bugfix #341 Wrong loop inclusion
//			//String[] delimiters = {"\"", "'"};
//			final String delimiters = "\"'";
//			// END KGU#344 2017-02-07
//			// START KGU#139 2016-01-12: Bugfix #105 - apparently incomplete strings got lost
//			// We mustn't eat seemingly incomplete strings, instead we re-feed them
//			StringList parkedTokens = new StringList();
//			// END KGU#139 2016-01-12
//			// START #344 2017-02-07: Bugfix #341: Wrong strategy - the token must select the start delimiter
//			//for (int d = 0; d < delimiters.length; d++)
//			//{
//			int ixDelim = -1;	// delimiter index in delimiters
//			String delim = "";	// starting delimiter for matching the closing delimiter
//			// END KGU#344 2017-02-07
//				boolean withinString = false;
//				String composed = "";
//				i = 0;
//				while (i < parts.count())
//				{
//					String lexeme = parts.get(i);
//					if (withinString)
//					{
//						composed = composed + lexeme;
//						// START KGU#344 2017-02-07: Bugfix #341
//						//if (lexeme.equals(delimiters[d]))
//						if (lexeme.equals(delim))
//						// END KGU#344 2017-02-07
//						{
//							// START KGU#139 2016-01-12: Bugfix #105
//							parkedTokens.clear();
//							// END KGU#139 2016-01-12
//							parts.set(i, composed+"");
//							composed = "";
//							withinString = false;
//							i++;
//						}
//						else
//						{
//							// START KGU#139 2016-01-12: Bugfix #105
//							parkedTokens.add(lexeme);
//							// END KGU#139 2016-01-12
//							parts.delete(i);
//						}
//					}
//					// START KGU#344 2017-02-07: Bugfix #341
//					//else if (lexeme.equals(delimiters[d]))
//					else if (lexeme.length() == 1 && (ixDelim = delimiters.indexOf(lexeme)) >= 0)
//					// END KGU#344 2017-02-27
//					{
//						// START KGU#139 2016-01-12: Bugfix #105
//						parkedTokens.add(lexeme);
//						// END KGU#139 2016-01-12
//						withinString = true;
//						// START KGU#344 2017-02-07: Bugfix #341
//						delim = delimiters.substring(ixDelim, ixDelim+1);
//						// END KGU#344 2017-02-07
//						composed = lexeme+"";
//						parts.delete(i);
//					}
//					else
//					{
//						i++;
//					}
//				}
//			// START KGU#344 2017-02-07: Bugfix #341 No outer loop anymore
//			//}
//			// END KGU#344 2017-02-07
//			// START KGU#139 2916-01-12: Bugfix #105
//			if (parkedTokens.count() > 0)
//			{
//				parts.add(parkedTokens);
//			}
//			// END KGU#139 2016-01-12
//		}
//		return parts;
		/* The new, more efficient approach behaves slightly different:
		 * W.r.t. names:
		 * Previously, all characters not explicitly listed among the LEXICAL_DELIMITERS
		 * could mix into a name. Now, conversely, all characters not explicitly allowed
		 * as identifier, literal, or operator symbol parts will inevitably handled as
		 * separate tokens.
		 * W.r.t. whitespace:
		 * Before the result split whitespace sequences into sequences of single whitespace
		 * characters, now whitespace sequences will be held together as tokens.
		 */
		return splitLexically(_text, _restoreStrings, false);
		// END KGU#790 2021-12-05
	}
	// END KGU#18/KGU#23 2015-11-04
	
	// START KGU#790 2021-12-05: Issue #800 - way more efficient splitter
	private static enum LexState {
		LX_0,
		LX_WHITESPACE,
		LX_STRING1, LX_STRING2,
		LX_NAME,
		LX_INTERNAL_KEY,	// pseudo identifiers §[A-Z]+§
		LX_INT, LX_INT0, LX_INTB, LX_INTO, LX_INTX,
		LX_FLOAT, LX_FLOATSE, LX_FLOATE,
		LX_SYMBOL};
	private static final String[] LEX_SYMBOLS = {
			":=", "<-",
			"<=", ">=", "<>", "==", "!=",
			"<<", ">>>", ">>",
			"&&", "||",
			"..", "...",
			"++", "--",
			"+=", "-=", "*=", "/=", "%=", "&=", "|=", "<<=", ">>=",
			"\\\\"};
	private static final StringList LEX_SYMBOL_LIST = new StringList(LEX_SYMBOLS);
	private static final String SYMBOL_CONTINUATORS = ".+-<>=:&|\\";
	private static final String SPEC_OPR_SYMBOLS = "\u2260\u2264\u2265";
	/**
	 * Splits the given _text into lexical morphemes (lexemes). This may possibly overdo
	 * somewhat (e. g. signs of number literals will be separated, but floating-point
	 * literals like {@code 123.45} or {@code .09e-8} will properly be preserved as
	 * contiguous tokens).<br/>
	 * This method now uses a finite state machine approach instead of repeated splitting
	 * and recombination.
	 * 
	 * @param _text - String to be exploded into lexical units
	 * @param _preserveStrings - if {@code true} then string literals will be preserved,
	 *        otherwise they will be split as if the content were a sequence of lexemes.
	 * @param _noWhiteSpace - if {@code true} then inter-lexeme whitespace will be eliminated,
	 *        otherwise it will be left in contiguous chunks of whitespace characters.
	 * @return StringList consisting of the separated lexemes including isolated spaces etc.
	 */
	public static StringList splitLexically(String _text, boolean _preserveStrings, boolean _noWhiteSpace)
	{
		StringList tokens = new StringList();
		LexState state = LexState.LX_0;
		boolean escape = false;
		StringBuilder sbToken = new StringBuilder();
		for (int ix = 0; ix < _text.length(); ix++) {
			int cp = _text.codePointAt(ix);
			switch (state) {
			case LX_0:	// Initial and interregnum state
				if (Character.isWhitespace(cp)) {
					if (!_noWhiteSpace) {
						sbToken.appendCodePoint(cp);
					}
					state = LexState.LX_WHITESPACE;
				}
				else if (Character.isLetter(cp)) {
					sbToken.appendCodePoint(cp);
					state = LexState.LX_NAME;
				}
				else if (Character.isDigit(cp)) {
					sbToken.appendCodePoint(cp);
					state = cp == '0' ? LexState.LX_INT0 : LexState.LX_INT;
				}
				else if (cp == '.') {
					sbToken.appendCodePoint(cp);
					int cp1 = 0;
					if (ix + 1 < _text.length() ||
							(cp1 = _text.codePointAt(ix+1)) == 'e'
							|| cp1 == 'E'
							|| Character.isDigit(cp1)) {
						state = LexState.LX_FLOAT;
					}
					else {
						state = LexState.LX_SYMBOL;
					}
				}
				else if (cp == '\'') {
					sbToken.appendCodePoint(cp);
					state = _preserveStrings ? LexState.LX_STRING1 : LexState.LX_SYMBOL;
				}
				else if (cp == '\"') {
					sbToken.appendCodePoint(cp);
					state = _preserveStrings ? LexState.LX_STRING2 : LexState.LX_SYMBOL;
				}
				else if (SPEC_OPR_SYMBOLS.indexOf(cp) >= 0) {
					if (cp == '\u2260') {
						tokens.add("<>");
					}
					else if (cp == '\u2264') {
						tokens.add("<=");
					}
					else if (cp == '\u2265') {
						tokens.add(">=");
					}
				}
				else if (cp == '§') {
					sbToken.appendCodePoint(cp);
					state = LexState.LX_INTERNAL_KEY;
				}
				else {
					sbToken.appendCodePoint(cp);
					if (cp == '\\') {
						escape = true;
					}
					state = LexState.LX_SYMBOL;
				}
				break;
			case LX_FLOAT:	// Fraction part of a floating-point literal
				if (Character.isDigit(cp)) {
					sbToken.appendCodePoint(cp);
				}
				else if (cp == 'e' || cp == 'E') {
					int cp1 = 0;
					if (ix + 1 < _text.length() && Character.isDigit(cp1 = _text.codePointAt(ix+1))) {
						sbToken.appendCodePoint(cp);
						state = LexState.LX_FLOATE;
					}
					else if (ix + 2 < _text.length() && "+-".indexOf(cp1) >= 0 && Character.isDigit(_text.codePointAt(ix+2))) {
						sbToken.appendCodePoint(cp);
						state = LexState.LX_FLOATSE;
					}
				}
				else if (cp == 'f' || cp == 'F') {
					sbToken.appendCodePoint(cp);
					tokens.add(sbToken.toString());
					sbToken.delete(0, sbToken.length());
					state = LexState.LX_0;
				}
				else {
					// Something different seems to start here
					tokens.add(sbToken.toString());
					sbToken.delete(0, sbToken.length());
					state = LexState.LX_0;
					ix--;	// Process this code point again
				}
				break;
			case LX_FLOATSE: // Exponent of a floating-point literal, sign possible
				state = LexState.LX_FLOATE;
				if (cp == '-' || cp == '+') {
					sbToken.appendCodePoint(cp);
					break;
				}
			case LX_FLOATE:	// Exponent of a floating-point literal, no sign allowed
				if (Character.isDigit(cp)) {
					sbToken.appendCodePoint(cp);
				}
				else if (cp == 'f' || cp == 'F') {
					sbToken.appendCodePoint(cp);
					tokens.add(sbToken.toString());
					sbToken.delete(0, sbToken.length());
					state = LexState.LX_0;
				}
				else {
					// Something different seems to start here
					tokens.add(sbToken.toString());
					sbToken.delete(0, sbToken.length());
					state = LexState.LX_0;
					ix--;	// Process this code point again
				}
				break;
			case LX_INT0:	// Integer literal with initial '0'
				if (cp == 'b' || cp == 'B'
					&& ix + 1 < _text.length()
					&& "01".indexOf(_text.codePointAt(ix+1)) >= 0) {
					sbToken.appendCodePoint(cp);
					state = LexState.LX_INTB;
					break;
				}
				else if (cp == 'x' || cp == 'X'
						&& ix + 1 < _text.length()
						&& "0123456789ABCDEFabcdef".indexOf(_text.codePointAt(ix+1)) >= 0) {
					sbToken.appendCodePoint(cp);
					state = LexState.LX_INTX;
					break;
				}
				else if (cp >= '0' && cp <= '7') {
					sbToken.appendCodePoint(cp);
					state = LexState.LX_INTO;
					break;
				}
				else if (Character.isDigit(cp)) {
					tokens.add(sbToken.toString());
					sbToken.delete(0, sbToken.length());
					// Another int literal seems to start here
					sbToken.appendCodePoint(cp);
					state = LexState.LX_INT;
					break;
				}
				// No break here!
			case LX_INT:	// Decimal integer literal ([1-9][0-9]*L?)
				if (Character.isDigit(cp)) {
					sbToken.appendCodePoint(cp);
				}
				else if (cp == '.') {
					sbToken.appendCodePoint(cp);
					state = LexState.LX_FLOAT;
				}
				else if (cp == 'e' || cp == 'E') {
					int cp1 = 0;
					if (ix + 1 < _text.length() && Character.isDigit(cp1 = _text.codePointAt(ix+1))) {
						sbToken.appendCodePoint(cp);
						state = LexState.LX_FLOATE;
					}
					else if (ix + 2 < _text.length() && "+-".indexOf(cp1) >= 0 && Character.isDigit(_text.codePointAt(ix+2))) {
						sbToken.appendCodePoint(cp);
						state = LexState.LX_FLOATSE;
					}
				}
				else if (cp == 'f' || cp == 'F' || cp == 'l' || cp == 'L') {
					// Float or long literal: [0-9]+[fl]
					sbToken.appendCodePoint(cp);
					tokens.add(sbToken.toString());
					sbToken.delete(0, sbToken.length());
					state = LexState.LX_0;
				}
				else {
					// Something different seems to start here
					tokens.add(sbToken.toString());
					sbToken.delete(0, sbToken.length());
					state = LexState.LX_0;
					ix--;	// Process this code point again
				}
				break;
			case LX_INTB:	// Binary integer literal: 0b[01]+
				if (cp == '0' || cp == '1') {
					sbToken.appendCodePoint(cp);
				}
				else if (cp == 'l' || cp == 'L') {
					// binary long literal
					sbToken.appendCodePoint(cp);
					tokens.add(sbToken.toString());
					sbToken.delete(0, sbToken.length());
					state = LexState.LX_0;
				}
				else {
					// Something different seems to start here
					tokens.add(sbToken.toString());
					sbToken.delete(0, sbToken.length());
					state = LexState.LX_0;
					ix--;	// Process this code point again
				}
				break;
			case LX_INTO:	// Octal integer literal: 0[0-7]*
				if (cp >= '0' && cp <= '7') {
					sbToken.appendCodePoint(cp);
				}
				else if (cp == 'l' || cp == 'L') {
					// octal long literal
					sbToken.appendCodePoint(cp);
					tokens.add(sbToken.toString());
					sbToken.delete(0, sbToken.length());
					state = LexState.LX_0;
				}
				else {
					// Something different seems to start here
					tokens.add(sbToken.toString());
					sbToken.delete(0, sbToken.length());
					state = LexState.LX_0;
					ix--;	// Process this code point again
				}
				break;
			case LX_INTX:	// Hexadecimal integer literal: 0x[0-9A-Fa-f]+
				if (cp >= '0' && cp <= '9'
					|| cp >= 'A' && cp <= 'F'
					|| cp >= 'a' && cp <= 'f') {
					sbToken.appendCodePoint(cp);
				}
				else {
					// Something different seems to start here
					tokens.add(sbToken.toString());
					sbToken.delete(0, sbToken.length());
					state = LexState.LX_0;
					ix--;	// Process this code point again
				}
				break;
			case LX_INTERNAL_KEY:
				if (Character.isUpperCase(cp)) {
					sbToken.appendCodePoint(cp);
				}
				else if (cp == '§') {
					if (sbToken.length() > 1) {
						// Accomplishes the internal key
						sbToken.appendCodePoint(cp);
						tokens.add(sbToken.toString());
						sbToken.delete(0, sbToken.length());
						state = LexState.LX_0;
					}
					else {
						// Nothing between the two '§'
						// Handle one of them as singular token and stay in the state
						tokens.add("§");
						// The new '§' is again a potential start (already in sbToken)
					}
				}
				else {
					// Make the initial '§' a single token and check the remainder
					tokens.add("§");
					sbToken.deleteCharAt(0);
					// If not empty it's an upper-case letter sequence, thus a name
					state = sbToken.length() > 0 ? state = LexState.LX_NAME : LexState.LX_0;
					ix--;	// Process this code point again
				}
				break;
			case LX_NAME:
				if (Character.isLetterOrDigit(cp) || cp == '_') {
					sbToken.appendCodePoint(cp);
				}
				else {
					// Something different seems to start here
					tokens.add(sbToken.toString());
					sbToken.delete(0, sbToken.length());
					state = LexState.LX_0;
					ix--;	// Process this code point again
				}
				break;
			case LX_STRING1:
			case LX_STRING2:
				sbToken.appendCodePoint(cp);
				if (cp == '\\') {
					escape = !escape;
				}
				else if (cp == '\'' && state == LexState.LX_STRING1
						|| cp == '\"' && state == LexState.LX_STRING2) {
					escape = false;
					tokens.add(sbToken.toString());
					sbToken.delete(0, sbToken.length());
					state = LexState.LX_0;
				}
				break;
			case LX_WHITESPACE:
				if (Character.isWhitespace(cp)) {
					if (!_noWhiteSpace) {
						sbToken.appendCodePoint(cp);
					}
					state = LexState.LX_WHITESPACE;
				}
				else {
					if (sbToken.length() > 0) {
						tokens.add(sbToken.toString());
						sbToken.delete(0, sbToken.length());
					}
					state = LexState.LX_0;
					ix--;	// Process this code point again
				}
				break;
			case LX_SYMBOL:
				// Lets's see if there is some possible combination
				if (SYMBOL_CONTINUATORS.indexOf(cp) >= 0) {
					// May belong to the symbol,
					int oldLen = sbToken.length();
					sbToken.appendCodePoint(cp);
					if (!LEX_SYMBOL_LIST.contains(sbToken.toString())) {
						String token = sbToken.substring(0, oldLen);
						// May not be part, so push the former symbol
						tokens.add(token);
						if (token.equals("<-") && cp == '-') {
							// Drop the superfluous second hyphen ...
							oldLen = sbToken.length();
							// ... and start from scratch
							state = LexState.LX_0;
						}
						// Start a new symbol with the current cp
						sbToken.delete(0, oldLen);
					}
					else if (escape && cp == '\\') {
						escape = false;
					}
				}
				else if (escape && "0bftnr'\"".indexOf(cp) >= 0) {
					// Amalgamate the floating (outside a string literal) escape sequence
					sbToken.appendCodePoint(cp);
					tokens.add(sbToken.toString());
					sbToken.delete(0, sbToken.length());
					state = LexState.LX_0;
					escape = false;
				}
				else {
					tokens.add(sbToken.toString());
					sbToken.delete(0, sbToken.length());
					state = LexState.LX_0;
					ix--;	// Process this code point again
				}
				break;
			default:
				System.err.println("Unhandled LexState " + state.name());
				break;
			}
		}
		if (sbToken.length() > 0) {
			tokens.add(sbToken.toString());
		}
		return tokens;
	}
	// END KGU#790 2021-12-05
	
	/**
	 * Splits the token list {@code _tokens}, which is supposed to represent a sequence of
	 * expressions separated by separators {@code _listSeparator}, into a list of
	 * {@link StringList}s, each comprising one of the listed expressions in tokenized form.<br/>
	 * This is aware of string literals, argument lists of function calls etc. (These must
	 * not be broken.)
	 * The analysis stops as soon as there is a level underflow (i.e. an unmatched right
	 * parenthesis, bracket, or the like).<br/>
	 * The list of the remaining tokens from the unsatisfied right parenthesis, bracket, or
	 * brace on will be added as last element to the result.
	 * If the last result element is empty then the expression list was syntactically "clean".<br/>
	 * FIXME If the expression was given without some parentheses as delimiters then a tail
	 * won't be added.
	 * 
	 * @param _tokens - {@link StringList} containing one or more expressions in tokenized form
	 * @param _listSeparator - a character sequence serving as separator among the expressions
	 *     (default: ",") 
	 * @return a list consisting of the separated tokenized expressions and the tail.
	 */
	public static ArrayList<StringList> splitExpressionList(StringList _tokens, String _listSeparator)
	{

		ArrayList<StringList> expressionList = new ArrayList<StringList>();
		if (_listSeparator == null) _listSeparator = ",";
		int parenthDepth = 0;
		boolean isWellFormed = true;
		Stack<String> enclosings = new Stack<String>();
		int tokenCount = _tokens.count();
		
		StringList currExpr = new StringList();
		StringList tail = new StringList();
		for (int i = 0; isWellFormed && parenthDepth >= 0 && i < tokenCount; i++)
		{
			String token = _tokens.get(i);
			if (token.equals(_listSeparator) && enclosings.isEmpty())
			{
				// store the current expression and start a new one
				expressionList.add(currExpr.trim());
				currExpr = new StringList();
			}
			else
			{ 
				if (token.equals("("))
				{
					enclosings.push(")");
					parenthDepth++;
				}
				else if (token.equals("["))
				{
					enclosings.push("]");
					parenthDepth++;
				}
				else if (token.equals("{"))
				{
					enclosings.push("}");
					parenthDepth++;
				}
				else if ((token.equals(")") || token.equals("]") || token.equals("}")))
				{
					isWellFormed = parenthDepth > 0 && token.equals(enclosings.pop());
					parenthDepth--;
				}
				
				
				if (isWellFormed)
				{
					currExpr.add(token);
				}
				else
				{
					// START KGU#1061 2022-08-23: Bugfix #1068 an empty list generated a list with empty string
					//expressionList.add(currExpr.trim();
					if (!currExpr.trim().isEmpty() || !expressionList.isEmpty()) {
						// There must have been at least one separator - so add even an empty term
						expressionList.add(currExpr);
					}
					// END KGU#1061 2022-08-23
					currExpr = new StringList();	// We must not clear() because it would be shared
					tail = _tokens.subSequence(i, _tokens.count()).trim();
				}
			}
		}
		// add the last expression if it's not empty
		if (!(currExpr = currExpr.trim()).isEmpty())
		{
			expressionList.add(currExpr);
		}
		// Add the tail. Empty if there is no bad tail
		expressionList.add(tail);
		return expressionList;
	}

	// START KGU#101/KGU#790 2023-10-19: Issue #800
	/**
	 * Splits the token list {@code _tokens}, which is supposed to represent a sequence of
	 * expressions separated by separators {@code _listSeparator}, into a list of
	 * {@link TokenList}s, each comprising one of the listed expressions in tokenized form.<br/>
	 * This is aware of string literals, argument lists of function calls etc. (These must
	 * not be broken.)
	 * The analysis stops as soon as there is a level underflow (i.e. an unmatched right
	 * parenthesis, bracket, or the like).<br/>
	 * The list of the remaining tokens from the unsatisfied right parenthesis, bracket, or
	 * brace on will be added as last element to the result.
	 * If the last result element is empty then the expression list was syntactically "clean".<br/>
	 * FIXME If the expression was given without some parentheses as delimiters then a tail
	 * won't be added.
	 * 
	 * @param _tokens - {@link StringList} containing one or more expressions in tokenized form
	 * @param _listSeparator - a character sequence serving as separator among the expressions
	 *     (default: ",") 
	 * @return a list consisting of the separated tokenized expressions and the tail.
	 */
	public static ArrayList<TokenList> splitExpressionList(TokenList _tokens, String _listSeparator)
	{

		ArrayList<TokenList> expressionList = new ArrayList<TokenList>();
		if (_listSeparator == null) _listSeparator = ",";
		int parenthDepth = 0;
		boolean isWellFormed = true;
		Stack<String> enclosings = new Stack<String>();
		int tokenCount = _tokens.size();
		
		TokenList currExpr = new TokenList();
		TokenList tail = new TokenList();
		for (int i = 0; isWellFormed && parenthDepth >= 0 && i < tokenCount; i++)
		{
			String token = _tokens.get(i);
			if (token.equals(_listSeparator) && enclosings.isEmpty())
			{
				// store the current expression and start a new one
				expressionList.add(currExpr);
				currExpr = new TokenList();
			}
			else
			{ 
				if (token.equals("("))
				{
					enclosings.push(")");
					parenthDepth++;
				}
				else if (token.equals("["))
				{
					enclosings.push("]");
					parenthDepth++;
				}
				else if (token.equals("{"))
				{
					enclosings.push("}");
					parenthDepth++;
				}
				else if ((token.equals(")") || token.equals("]") || token.equals("}")))
				{
					isWellFormed = parenthDepth > 0 && token.equals(enclosings.pop());
					parenthDepth--;
				}
				
				
				if (isWellFormed)
				{
					currExpr.add(token);
				}
				else
				{
					// START KGU#1061 2022-08-23: Bugfix #1068 an empty list generated a list with empty string
					//expressionList.add(currExpr.trim();
					if (!currExpr.isBlank() || !expressionList.isEmpty()) {
						// There must have been at least one separator - so add even an empty term
						expressionList.add(currExpr);
					}
					// END KGU#1061 2022-08-23
					currExpr = new TokenList();	// We must not clear() because it would be shared
					tail = _tokens.subSequence(i, _tokens.size());
					tail.trim();
				}
			}
		}
		// add the last expression if it's not empty
		if (!currExpr.isBlank())
		{
			expressionList.add(currExpr);
		}
		// Add the tail. Empty if there is no bad tail
		expressionList.add(tail);
		return expressionList;
	}
	// END KGU#101/KGU#790 2023-10-19
	
	// START KGU#1057 2022-08-20: Enh. #1066 Interactive input assistent
	/**
	 * Analyses the token list {@code tokens} preceding a dot in backwards direction for
	 * record structure information.<br/>
	 * If the pretext describes an object with record structure then returns the list
	 * of component names.
	 * 
	 * @param tokens - the lexically split line content up to (but not including) a dot
	 * @param typeMap - the current mapping of variables and type names to type info
	 * @param firstSeen - must either be {@code null} or an int array with at least one
	 *     element, at position 0 of which the index of the first token that contributed
	 *     to the analysis will be placed.
	 * @return either a list of component names or {@code null}
	 */
	public static ArrayList<String> retrieveComponentNames(
			StringList tokens,
			HashMap<String, TypeMapEntry> typeMap,
			int[] firstSeen) {
		// FIXME: To be converted to new TypeRegistry system
		ArrayList<String> proposals = null;
		tokens.removeAll(" ");
		// Go as far backward as we can go to find the base variable
		// We will not go beyond a function call, so what may precede is an id or ']'
		StringList path = new StringList();
		int ix = tokens.count() -1;
		while (path != null && ix >= 0) {
			String prevToken = tokens.get(ix);
			// There might be several index expressions
			while (path != null && prevToken.equals("]")) {
				// We will have to find the corresponding opening bracket
				int ixClose = ix;
				int level = 1;
				ix--;
				while (level > 0 && ix >= 0) {
					prevToken = tokens.get(ix);
					if (prevToken.equals("]")) {
						level++;
					}
					else if (prevToken.equals("[")) {
						level--;
					}
					ix--;
					/* If more than one index expression is listed here,
					 * then we will find out via expression analysis below
					 */
				}
				if (level > 0) {
					path = null;
				}
				else {
					// Now find out how many indices are given between the brackets
					ArrayList<StringList> indexExprs = splitExpressionList(
							tokens.subSequence(ix + 2, ixClose + 1), ",");
					// Add as many bracket pairs to the path
					for (int i = 0; i < indexExprs.size() - 1; i++) {
						path.add("[]");
					}
					prevToken = tokens.get(ix);
				}
			}
			if (path != null && isIdentifier(prevToken, true, null)) {
				path.add(prevToken);
				ix--;
				if (ix > 0 && tokens.get(ix).equals(".")) {
					ix--; // Continue path collection
				}
				else {
					break;	// Stop analysis, path may be valid
				}
			}
			else {
				path = null;
			}
		}
		if (path != null && path.count() >= 1) {
			// Now we may have a reverse valid access path
			path = path.reverse();
			TypeMapEntry varType = typeMap.get(path.get(0));
			path.remove(0);
			while (varType != null && !path.isEmpty()) {
				if (varType.isArray() && path.get(0).equals("[]")) {
					String typeStr = varType.getCanonicalType(true, true);
					while (typeStr.startsWith("@") && !path.isEmpty()
							&& path.get(0).equals("[]")) {
						typeStr = typeStr.substring(1);
						path.remove(0);
					}
					varType = typeMap.get(":" + typeStr);
				}
				if (varType != null && varType.isRecord()) {
					if (!path.isEmpty()) {
						var compInfo = varType.getComponentInfo(true);
						varType = compInfo.get(path.get(0));
						path.remove(0);
					}
				}
			}
			if (varType != null && varType.isRecord()) {
				// path must now be exhausted, the component names are our proposals
				var compInfo = varType.getComponentInfo(true);
				proposals = new ArrayList<String>();
				proposals.addAll(compInfo.keySet());
			}
		}
		if (firstSeen != null && firstSeen.length > 0) {
			firstSeen[0] = ix + 1;
		}
		return proposals;
	}
	// END KGU#1057 2022-08-20

	// START KGU#18/KGU#23 2015-10-24 intermediate transformation added and decomposed
	/**
	 * Converts the operator symbols (and some literals) accepted by Structorizer into
	 * the following (mostly Java) operators:
	 * <ul>
	 * <li>Assignment:	"<-"</li>
	 * <li>Comparison:	"==", "<", ">", "<=", ">=", "!="</li>
	 * <li>Logic:		"&&", "||", "!", "^"</li>
	 * <li>Arithmetics:	usual Java operators (e.g. "mod" -> "%") plus "div"</li>
	 * <li>Literals:    "Infinity" (from "&infin;")
	 * </ul>
	 * 
	 * @param _expression - an Element's text (in practically unknown syntax)
	 * @return an equivalent of the {@code _expression} String with replaced operators
	 * 
	 * @see #unifyOperators(StringList, boolean)
	 */
	public static String unifyOperators(String _expression)
	{
		// START KGU#93 2015-12-21: Bugfix #41/#68/#69 Avoid operator padding
		//return unifyOperators(_expression, false);
		StringList tokens = Syntax.splitLexically(_expression, true);
		unifyOperators(tokens, false);
		return tokens.concatenate();
		// END KGU#93 2015-12-21
	}

	// START KGU#92 2015-12-01: Bugfix #41 Okay now, here is the new approach (still a sketch)
	/**
	 * Replaces the operator symbols (and some literals) accepted by Structorizer in
	 * the given token list {@code _tokens} with the following (mostly Java) operators:
	 * <ul>
	 * <li>Assignment:	"<-"</li>
	 * <li>Comparison:	"==", "<", ">", "<=", ">=", "!="</li>
	 * <li>Logic:		"&&", "||", "!", "^"</li>
	 * <li>Arithmetics:	usual Java operators (e. g. "mod" -> "%") plus "div"</li>
	 * <li>Literals:	"Infinity" (from "&infin;")
	 * </ul>
	 * 
	 * @param _tokens - a tokenised line of an Element's text (in practically unknown
	 *     syntax), <b>will be modified by the method</b>
	 * @param _assignmentOnly - if {@code true} then only assignment operators will be unified
	 * @return total number of replacements
	 */
	public static int unifyOperators(StringList _tokens, boolean _assignmentOnly)
	{
		int count = 0;
		// START KGU#790 2021-12-08: More efficient solution (1 pass instead of 12)
//		count += _tokens.replaceAll(":=", "<-");
//		// START KGU#115 2015-12-23: Bugfix #74 - logical inversion
//		//if (_assignmentOnly)
//		if (!_assignmentOnly)
//		// END KGU#115 2015-12-23
//		{
//			count += _tokens.replaceAll("=", "==");
//			count += _tokens.replaceAll("<>", "!=");
//			count += _tokens.replaceAllCi("mod", "%");
//			count += _tokens.replaceAllCi("shl", "<<");
//			count += _tokens.replaceAllCi("shr", ">>");
//			count += _tokens.replaceAllCi("and", "&&");
//			count += _tokens.replaceAllCi("or", "||");
//			count += _tokens.replaceAllCi("not", "!");
//			count += _tokens.replaceAllCi("xor", "^");
//			// START KGU#843 2020-04-11: Bugfix #847 Inconsistency in handling operators (we don't count this, though)
//			count += _tokens.replaceAllCi("DIV", "div");
//			// END KGU#843 2020-04-11
//			// START KGU#920 2021-02-03: Issue #920 Handle Infinity literal
//			count += _tokens.replaceAll("\u221E", "Infinity");
//			// END KGU#920 2021-02-03
//		}
		for (int i = 0; i < _tokens.count(); i++) {
			String subst = UNIFICATION_MAP.get(_tokens.get(i).toLowerCase());
			if (subst != null && (!_assignmentOnly || subst.equals("<-"))) {
				_tokens.set(i, subst);
				count++;
			}
		}
		return count;
		// END KGU#790 2021-12-08
	}
	// END KGU#92 2015-12-01

//	/**
//	 * Removes redundant marker keywords (as configured in the Parser Preferences) from
//	 * the given token list {@code _tokens}.
//	 * @param _tokens - the lexically split text with already condensed keywords
//	 * @param _preMarkers - whether "Pre" markers are to be removed
//	 * @param _postMarkers - whether "Post" markers are to be removed
//	 */
//	public static void removeRedundantMarkers(StringList _tokens, boolean _preMarkers, boolean _postMarkers)
//	{
//		// Collect redundant placemarkers to be deleted from the text
//		StringList redundantMarkers = new StringList();
//		if (_preMarkers) {
//			redundantMarkers.addByLength(Syntax.getKeyword("preAlt"));
//			redundantMarkers.addByLength(Syntax.getKeyword("preCase"));
//			redundantMarkers.addByLength(Syntax.getKeyword("preWhile"));
//			redundantMarkers.addByLength(Syntax.getKeyword("preRepeat"));
//		}
//		if (_postMarkers) {
//			redundantMarkers.addByLength(Syntax.getKeyword("postAlt"));
//			redundantMarkers.addByLength(Syntax.getKeyword("postCase"));
//			redundantMarkers.addByLength(Syntax.getKeyword("postWhile"));
//			redundantMarkers.addByLength(Syntax.getKeyword("postRepeat"));
//		}
//
//		for (int i = 0; i < redundantMarkers.count(); i++)
//		{
//			String marker = redundantMarkers.get(i);
//			if (!marker.trim().isEmpty())
//			{
//				_tokens.removeAll(marker, !Syntax.ignoreCase);
//			}
//		}
//	}

// This was just an obsolete draft
//	/**
//	 * Stores the syntactical representation of the given Element text {@code _lines}
//	 * in the central syntax map under the Element ID {@code _id}.
//	 * @param _id - Element ID
//	 * @param _lines - unbroken lines of Element text
//	 * @return true if syntax analysis succeeded, false otherwise (no entry then)
//	 */
//	public static boolean registerElementSyntax(long _id, StringList _lines)
//	{
//		Line[] parsedLines = new Line[_lines.count()];
//		getInstance().syntaxMap.put(_id, parsedLines);
//		// TODO Do the syntactical analysis
//		for (int i = 0; i < _lines.count(); i++) {
//			StringList tokens = splitLexically(_lines.get(i), true);
//			tokens.trim();
//			parsedLines[i] = getInstance().parseLine(tokens);
//		}
//		return true;
//	}
//	
//	/**
//	 * Retrieves the parsed syntax of the lines of Element with the given
//	 * ID {@code _id} if it has been in the map, null otherwise
//	 * @param _id - the element id.
//	 * @return An array of {@link Line} structures or {@code null}
//	 */
//	public static Line[] getElementSyntax(long _id)
//	{
//		return instance.syntaxMap.get(_id);
//	}
//	
//	/**
//	 * Retrieves the parsed syntax of line {@code _lineNo} of the Element with
//	 * the given ID {@code _id} if it has been in the map, null otherwise
//	 * @param _id
//	 * @param _lineNo
//	 * @return
//	 */
//	public static Line getLineSyntax(long _id, int _lineNo)
//	{
//		Line[] lines = getElementSyntax(_id);
//		if (lines != null && _lineNo < lines.length) {
//			return lines[_lineNo];
//		}
//		return null;
//	}
//	
//	/**
//	 * @param tokens
//	 * @return
//	 */
//	private Line parseLine(StringList tokens) {
//		return null;
//	}
	
}
