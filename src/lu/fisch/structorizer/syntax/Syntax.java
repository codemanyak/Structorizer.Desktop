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
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import lu.fisch.structorizer.io.Ini;
import lu.fisch.utils.StringList;

/**
 * @author Kay Gürtzig
 */
public class Syntax {

	private static final Pattern FLOAT_PATTERN1 = Pattern.compile("[0-9]+([eE][0-9]+)?");
	private static final Pattern FLOAT_PATTERN2 = Pattern.compile("[0-9]+[eE]");
	private static final Pattern INT_PATTERN = Pattern.compile("[0-9]+");
	private static final Pattern SIGN_PATTERN = Pattern.compile("[+-]");
	public static final Pattern STRING_PATTERN = Pattern.compile("(^\\\".*\\\"$)|(^\\\'.*\\\'$)");
	// START KGU#425 2017-09-29: Lexical core mechanisms revised
	private static final String[] LEXICAL_DELIMITERS = new String[] {
			" ",
			"\t",
			"\n",
			".",
			",",
			";",
			"(",
			")",
			"[",
			"]",
			// START KGU#100 2016-01-14: We must also catch the initialiser delimiters
			"{",
			"}",
			// END KGU#100 2016-01-14
			"-",
			"+",
			"/",
			"*",
			">",
			"<",
			"=",
			":",
			"!",
			"'",
			"\"",
			"\\",
			"%",
			// START KGU#331 2017-01-13: Enh. #333 Precaution against unicode comparison operators
			"\u2260",
			"\u2264",
			"\u2265"
			// END KGU#331 2017-01-13
	};
	// END KGU#425 2017-09-29


	private static Syntax instance = null;
	
	/**
	 * Maps Element IDs to vectors of syntactical representations of the
	 * respective unbroken lines of the Element text.
	 */
	private HashMap<Long, Line[]> syntaxMap = new HashMap<Long, Line[]>();
	
	/**
	 * @return the instance of this class
	 */
	public static Syntax getInstance()
	{
		if (instance == null) {
			instance = new Syntax();
		}
		return instance;
	}
	
	// START KGU#165 2016-03-25: Once and for all: It should be a transparent choice, ...
	/**
	 * whether or not the keywords are to be handled in a case-independent way
	 */
	public static boolean ignoreCase = true;
	// END KGU#165 2016-03-25

	// START KGU#288 2016-11-06: Issue #279: Access limited to private, compensated by new methods
	//public static final HashMap<String, String> keywordMap = new LinkedHashMap<String, String>();
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
	private static HashMap<String, StringList> splitKeywords = new HashMap<String, StringList>();
	// END KGU 2016-03-29

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
		String[] props = new String[]{};
		return keywordMap.values().toArray(props);
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
				splitKeywords.put(_key,  tokens);
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
		}
	}
	// END KGU#288 2016-11-06
	
	// START KGU#18/KGU#23 2015-11-04: Lexical splitter extracted from writeOutVariables
	/**
	 * Splits the given _text into lexical morphemes (lexemes). This will possibly overdo
	 * somewhat (e. g. signs of number literals will be separated, but floating-point literals
	 * like 123.45 or .09e-8 will properly be preserved as contiguous tokens).<br>
	 * By setting {@code _restoreStrings} true, string literals will be re-assembled, too, consuming
	 * a little more time, of course.<br>
	 * Note that inter-lexeme whitespace will NOT be eliminated but forms elements of the result,
	 * more precisely: a sequence of whitespace characters (like {@code "    "}) will form a series of
	 * 1-character whitespace strings (here: " ", " ", " ", " "). So they can easily be removed
	 * with removeAll(" ").
	 * @param _text - String to be exploded into lexical units
	 * @param _restoreLiterals - if true then accidently split numeric and string literals will be reassembled 
	 * @return StringList consisting of the separated lexemes including isolated spaces etc.
	 */
	public static StringList splitLexically(String _text, boolean _restoreStrings)
	{
		StringList parts = new StringList();
		parts.add(_text);
		
		// split
		for (int i = 0; i < LEXICAL_DELIMITERS.length; i++) {
			parts = StringList.explodeWithDelimiter(parts, LEXICAL_DELIMITERS[i]);
		}

		// reassemble symbols
		int i = 0;
		while (i < parts.count())
		{
			String thisPart = parts.get(i);
			if (i < parts.count()-1)
			{
				String nextPart = parts.get(i+1);
				boolean isInt = false;
				boolean isSign = false;
				boolean isEllipse = false;
				if (thisPart.equals("<") && nextPart.equals("-"))
				{
					parts.set(i,"<-");
					parts.delete(i+1);
					// START KGU 2014-10-18 potential three-character assignment symbol?
					if (i < parts.count()-1 && parts.get(i+1).equals("-"))
					{
						parts.delete(i+1);
					}
					// END KGU 2014-10-18
				}
				else if (thisPart.equals(":") && nextPart.equals("="))
				{
					parts.set(i,":=");
					parts.delete(i+1);
				}
				else if (thisPart.equals("!") && nextPart.equals("="))
				{
					parts.set(i,"!=");
					parts.delete(i+1);
				}
				// START KGU 2015-11-04
				else if (thisPart.equals("=") && nextPart.equals("="))
				{
					parts.set(i,"==");
					parts.delete(i+1);
				}
				// END KGU 2015-11-04
				else if (thisPart.equals("<"))
				{
					if (nextPart.equals(">"))
					{
						parts.set(i,"<>");
						parts.delete(i+1);
					}
					else if (nextPart.equals("="))
					{
						parts.set(i,"<=");
						parts.delete(i+1);
					}
					// START KGU#92 2015-12-01: Bugfix #41
					else if (nextPart.equals("<"))
					{
						parts.set(i,"<<");
						parts.delete(i+1);
					}					
					// END KGU#92 2015-12-01
				}
				else if (thisPart.equals(">"))
				{
					if (nextPart.equals("="))
					{
						parts.set(i,">=");
						parts.delete(i+1);
					}
					// START KGU#92 2015-12-01: Bugfix #41
					else if (nextPart.equals(">"))
					{
						parts.set(i,">>");
						parts.delete(i+1);
					}					
					// END KGU#92 2015-12-01
				}
				// START KGU#24 2014-10-18: Logical two-character operators should be detected, too ...
				else if (thisPart.equals("&") && nextPart.equals("&"))
				{
					parts.set(i,"&&");
					parts.delete(i+1);
				}
				else if (thisPart.equals("|") && nextPart.equals("|"))
				{
					parts.set(i,"||");
					parts.delete(i+1);
				}
				// END KGU#24 2014-10-18
				// START KGU#26 2015-11-04: Find escaped quotes
				else if (thisPart.equals("\\"))
				{
					if (nextPart.equals("\""))
					{
						parts.set(i, "\\\"");
						parts.delete(i+1);
					}
					// START KGU#344 201702-08: Issue #341 - Precaution against string/character delimiter replacement
					else if (nextPart.equals("'"))
					{
						parts.set(i, "\\'");
						parts.delete(i+1);
					}
					// END KGU#344 2017-02-08
					else if (nextPart.equals("\\"))
					{
						parts.set(i, "\\\\");
						parts.delete(i+1);
					}
				}
				// END KGU#26 2015-11-04
				// START KGU#331 2017-01-13: Enh. #333 Precaution against unicode comparison operators
				else if (thisPart.equals("\u2260")) {
					parts.set(i, "<>");
				}
				else if (thisPart.equals("\u2264")) {
					parts.set(i, "<=");
				}
				else if (thisPart.equals("\u2265")) {
					parts.set(i, ">=");
				}
				// END KGU#331 2017-01-13
				// START KGU#335/KGU#425 2017-09-29: Re-compose floating-point literals (including those starting or ending with ".")
				// These are legal cases ($ = line end, ? = don't care):
				// i             i+1             i+2           i+3        comment
				// .              .               ?             ?         two-dot-ellipse (Pascal range)
				// .              .               .             ?         three-dot-ellipse (rarely used)
				// .            FLOAT1            ?             ?         float literal
				// .            FLOAT2           [+-]        [0-9]+       float literal
				// [+-]           .            FLOAT1           ?         float literal - reduce this case the the one -2
				// [+-]           .            FLOAT2         [+-] [0-9]+ float literal - reduce this case the the one -2
				// [0-9]+         .            FLOAT1           ?         float literal - reduce this case the the one -4
				// [0-9]+         .            FLOAT2         [+-] [0-9]+ float literal - reduce this case the the one -4
				// These are the illegal cases:
				// [+-]           .               $
				// [+-]           .               ?
				// [0-9]+         .               .
				// So we will first do the necessary lookahead before we manipulate parts
				else if ( (isEllipse = thisPart.equals("."))	// a single dot might merge with another one or a float pattern
						|| (	// Otherwise a digit sequence might melt with a dot
								(isInt = INT_PATTERN.matcher(thisPart).matches())
								|| (isSign = (thisPart.equals("+") || thisPart.equals("-"))	// a sign with a dot requires more...
										&& i+2 < parts.count())
								&& nextPart.equals(".")) 
						) {
					int nDelete = 0;
					// Glue the two together - the only pathologic case would be 
					if (nextPart.equals(".")) {
						thisPart += nextPart;
						nDelete = 1;
						// Is there anything left at all?
						if (i+2 < parts.count()) {
							nextPart = parts.get(i+2);
						}
						if (isEllipse && nextPart.equals(".")) {
							// Okay, then be it a three-point ellipse "..."
							thisPart += nextPart;
							nDelete++;
						}
						// In case of an ellipse we are done here
					}
					else {
						isEllipse = false;
					}
					// nextPart.matches("[0-9]+([eE][0-9]+)?")
					if (!isEllipse && FLOAT_PATTERN1.matcher(nextPart).matches()) {
						thisPart += nextPart;
						nDelete++;
					}
					// nextPart.matches("[0-9]+[eE]")
					else if (!isEllipse && FLOAT_PATTERN2.matcher(nextPart).matches()
							&& i+nDelete+3 < parts.count()
							&& SIGN_PATTERN.matcher(parts.get(i+nDelete+2)).matches()
							&& INT_PATTERN.matcher(parts.get(i+nDelete+3)).matches()) {
						for (int j = 1; j <= 3; j++) {
							nDelete++;
							thisPart += parts.get(i+nDelete);
						}
					}
					else if (isSign || isInt && i+2 < parts.count() && parts.get(i+2).equals(".")) {
						// In this case the amalgamation may not take place
						nDelete = 0;
					}
					// Now carry out the amalgamation if sensible
					if (nDelete > 0) {
						parts.set(i, thisPart);
						parts.remove(i+1, i+nDelete+1);
					}
				}
				// END KGU#335/KGU#425 2017-09-29
			}
			i++;
		}
		
		if (_restoreStrings)
		{
			// START KGU#344 2017-02-07: Bugfix #341 Wrong loop inclusion
			//String[] delimiters = {"\"", "'"};
			final String delimiters = "\"'";
			// END KGU#344 2017-02-07
			// START KGU#139 2016-01-12: Bugfix #105 - apparently incomplete strings got lost
			// We mustn't eat seemingly incomplete strings, instead we re-feed them
			StringList parkedTokens = new StringList();
			// END KGU#139 2016-01-12
			// START #344 2017-02-07: Bugfix #341: Wrong strategy - the token must select the start delimiter
			//for (int d = 0; d < delimiters.length; d++)
			//{
			int ixDelim = -1;	// delimiter index in delimiters
			String delim = "";	// starting delimiter for matching the closing delimiter
			// END KGU#344 2017-02-07
				boolean withinString = false;
				String composed = "";
				i = 0;
				while (i < parts.count())
				{
					String lexeme = parts.get(i);
					if (withinString)
					{
						composed = composed + lexeme;
						// START KGU#344 2017-02-07: Bugfix #341
						//if (lexeme.equals(delimiters[d]))
						if (lexeme.equals(delim))
						// END KGU#344 2017-02-07
						{
							// START KGU#139 2016-01-12: Bugfix #105
							parkedTokens.clear();
							// END KGU#139 2016-01-12
							parts.set(i, composed+"");
							composed = "";
							withinString = false;
							i++;
						}
						else
						{
							// START KGU#139 2016-01-12: Bugfix #105
							parkedTokens.add(lexeme);
							// END KGU#139 2016-01-12
							parts.delete(i);
						}
					}
					// START KGU#344 2017-02-07: Bugfix #341
					//else if (lexeme.equals(delimiters[d]))
					else if (lexeme.length() == 1 && (ixDelim = delimiters.indexOf(lexeme)) >= 0)
					// END KGU#344 2017-02-27
					{
						// START KGU#139 2016-01-12: Bugfix #105
						parkedTokens.add(lexeme);
						// END KGU#139 2016-01-12
						withinString = true;
						// START KGU#344 2017-02-07: Bugfix #341
						delim = delimiters.substring(ixDelim, ixDelim+1);
						// END KGU#344 2017-02-07
						composed = lexeme+"";
						parts.delete(i);
					}
					else
					{
						i++;
					}
				}
			// START KGU#344 2017-02-07: Bugfix #341 No outer loop anymore
			//}
			// END KGU#344 2017-02-07
			// START KGU#139 2916-01-12: Bugfix #105
			if (parkedTokens.count() > 0)
			{
				parts.add(parkedTokens);
			}
			// END KGU#139 2016-01-12
		}
		return parts;
	}
	// END KGU#18/KGU#23 2015-11-04
	
	/**
	 * Stores the syntactical representation of the given Element text {@code _lines}
	 * in the central syntax map under the Element ID {@code _id}.
	 * @param _id - Element ID
	 * @param _lines - unbroken lines of Element text
	 * @return true if syntax analysis succeeded, false otherwise (no entry then)
	 */
	public static boolean registerElementSyntax(long _id, StringList _lines)
	{
		Line[] parsedLines = new Line[_lines.count()];
		getInstance().syntaxMap.put(_id, parsedLines);
		// TODO Do the syntactical analysis
		for (int i = 0; i < _lines.count(); i++) {
			StringList tokens = splitLexically(_lines.get(i), true);
			tokens.trim();
			parsedLines[i] = getInstance().parseLine(tokens);
		}
		return true;
	}
	
	/**
	 * Retrieves the parsed syntax of the lines of Element with the given
	 * ID {@code _id} if it has been in the map, null otherwise
	 * @param _id
	 * @return A vector of {@link Line} structures or {@code null}
	 */
	public static Line[] getElementSyntax(long _id)
	{
		return instance.syntaxMap.get(_id);
	}
	
	/**
	 * Retrieves the parsed syntax of line {@code _lineNo} of the Element with
	 * the given ID {@code _id} if it has been in the map, null otherwise
	 * @param _id
	 * @param _lineNo
	 * @return
	 */
	public static Line getLineSyntax(long _id, int _lineNo)
	{
		Line[] lines = getElementSyntax(_id);
		if (lines != null && _lineNo < lines.length) {
			return lines[_lineNo];
		}
		return null;
	}
	
	/**
	 * @param tokens
	 * @return
	 */
	private Line parseLine(StringList tokens) {
		// Check if the line starts with a command keyword
		for (String key:keywordSet()) {
			StringList splitKey = getSplitKeyword(key);
			if (splitKey != null && tokens.indexOf(splitKey, 0, !Syntax.ignoreCase) == 0) {
				// Now it depends on the type of keyword what to do (element-type-specific?).
				// Should there be Syntax subclasses for Root, Instrucion, Jump etc.?
				// What about element transmutation?
			}
		}
		// If so, identify the separators and expression parts
		return null;
	}
	
}
