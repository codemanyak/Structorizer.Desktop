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
import java.util.regex.Pattern;

import lu.fisch.structorizer.elements.Element;
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
	 * Maps {@link Element} ids to vectors of syntactical representations of the
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
		// START KGU#425 2017-09-29: Code revision
		//parts=StringList.explodeWithDelimiter(parts," ");
		//parts=StringList.explodeWithDelimiter(parts,"\t");
		//parts=StringList.explodeWithDelimiter(parts,"\n");
		//parts=StringList.explodeWithDelimiter(parts,".");
		//parts=StringList.explodeWithDelimiter(parts,",");
		//parts=StringList.explodeWithDelimiter(parts,";");
		//parts=StringList.explodeWithDelimiter(parts,"(");
		//parts=StringList.explodeWithDelimiter(parts,")");
		//parts=StringList.explodeWithDelimiter(parts,"[");
		//parts=StringList.explodeWithDelimiter(parts,"]");
		//parts=StringList.explodeWithDelimiter(parts,"{");
		//parts=StringList.explodeWithDelimiter(parts,"}");
		//parts=StringList.explodeWithDelimiter(parts,"-");
		//parts=StringList.explodeWithDelimiter(parts,"+");
		//parts=StringList.explodeWithDelimiter(parts,"/");
		//parts=StringList.explodeWithDelimiter(parts,"*");
		//parts=StringList.explodeWithDelimiter(parts,">");
		//parts=StringList.explodeWithDelimiter(parts,"<");
		//parts=StringList.explodeWithDelimiter(parts,"=");
		//parts=StringList.explodeWithDelimiter(parts,":");
		//parts=StringList.explodeWithDelimiter(parts,"!");
		//parts=StringList.explodeWithDelimiter(parts,"'");
		//parts=StringList.explodeWithDelimiter(parts,"\"");
		//parts=StringList.explodeWithDelimiter(parts,"\\");
		//parts=StringList.explodeWithDelimiter(parts,"%");
		//parts=StringList.explodeWithDelimiter(parts,"\u2260");
		//parts=StringList.explodeWithDelimiter(parts,"\u2264");
		//parts=StringList.explodeWithDelimiter(parts,"\u2265");
		for (int i = 0; i < LEXICAL_DELIMITERS.length; i++) {
			parts = StringList.explodeWithDelimiter(parts, LEXICAL_DELIMITERS[i]);
		}
		// END KGU#425 2017-09-29

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
	// END KGU#18/KGU#23
	
	/**
	 * Stores the syntactical representation of the Element lines in the
	 * central syntax map
	 * @param _el
	 * @return
	 */
	public static boolean registerElementSyntax(Element _el)
	{
		StringList ubLines = _el.getUnbrokenText();
		getInstance().syntaxMap.put(_el.getId(), new Line[ubLines.count()]);
		// TODO Do the syntactical analysis
		return true;
	}
	
	public static Line[] getElementSyntax(Element _el, boolean _force)
	{
		Long id = _el.getId();
		Line[] lines = instance.syntaxMap.get(id);
		if (lines == null && _force && registerElementSyntax(_el)) {
			lines = instance.syntaxMap.get(id);
		}
		return lines;
	}
	
	public static Line getLineSyntax(Element _el, boolean _force, int lineNo)
	{
		Line[] lines = getElementSyntax(_el, _force);
		if (lines != null && lineNo < lines.length) {
			return lines[lineNo];
		}
		return null;
	}
	
	
}
