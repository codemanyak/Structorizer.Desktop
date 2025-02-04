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
 *      Description:    Class represents a lexicographically split line of text, maintaining
 *                      the distances between the tokens
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2023-10-19      First Issue
 *      Kay Gürtzig     2023-11-07      Several corrections, set of methods extended
 *      Kay Gürtzig     2024-03-21      indexOf(TokenList, ...), lastIndexOf(TokenList,...) fixed;
 *                                      '_' allowed as initial character of names (to be consistent with
 *                                      System.isIdentifier(...) and code import.
 *                                      New methods removePaddings(), removePaddings(int, int)
 *      Kay Gürtzig     2024-12-03/05   Attribute newlines added and functional integration begun
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import lu.fisch.utils.StringList;

/**
 * Class represents a lexicographically split line of text, maintaining
 * the distances between the tokens.
 * 
 * <p><strong>Note that this implementation is not synchronized.</strong>
 * If multiple threads access a {@code TokenList} instance concurrently,
 * and at least one of the threads modifies the list structurally, it
 * <i>must</i> be synchronized externally. (A structural modification is
 * any operation that adds or deletes one or more elements, or explicitly
 * resizes the backing array; merely setting the value of an element is not
 * a structural modification.)  This is typically accomplished by
 * synchronizing on some object that naturally encapsulates the list.
 *
 * If no such object exists, the list should be "wrapped" using the
 * {@link Collections#synchronizedList Collections.synchronizedList}
 * method. This is best done at creation time, to prevent accidental
 * unsynchronized access to the list:
 * <pre>List list = Collections.synchronizedList(new TokenList(...));</pre>
 *   
 * @author Kay Gürtzig
 *
 */
public class TokenList implements Comparable<TokenList>{

	/**
	 * States of the scanner state machine. {@code LX_0} is the initial state,
	 * others like LX_WHITESPACE, LX_NAME, etc. represent different lexicographic
	 * entities.
	 */
	private static enum LexState {
		LX_0,
		LX_WHITESPACE,
		LX_STRING1, LX_STRING2,
		LX_NAME,
		LX_INTERNAL_KEY,	// pseudo identifiers §[A-Z]+§
		LX_INT, LX_INT0, LX_INTB, LX_INTO, LX_INTX,
		LX_FLOAT, LX_FLOATSE, LX_FLOATE,
		LX_SYMBOL};
	/**
	 * Non-alphanumeric character sequences to be detected as lexicographic tokens,
	 * chiefly operator symbols relevant for Structorizer, as string array.
	 * 
	 * @see #LEX_SYMBOL_LIST
	 */
	private static final String[] LEX_SYMBOLS = {
			":=", "<-",
			"<=", ">=", "<>", "==", "!=",
			"<<", ">>>", ">>",
			"&&", "||",
			"..", "...",
			"++", "--",
			"+=", "-=", "*=", "/=", "%=", "&=", "|=", "<<=", ">>=",
			"\\\\"};
	/**
	 * Same as {@link LEX_SYMBOLS} but as {@link StringList}, which facilitates
	 * several operations.
	 * 
	 * @see LEX_SYMBOLS
	 */
	private static final StringList LEX_SYMBOL_LIST = new StringList(LEX_SYMBOLS);
	/**
	 * A string used as a set of all characters that may occur as second or third
	 * character of a symbol from {@link LEX_SYMBOLS}. Used to facilitate symbol
	 * detection and composition from the input stream.
	 */
	private static final String SYMBOL_CONTINUATIONS = ".+-<>=:&|\\";
	/**
	 * A string containing non-ASCII characters that may serve as operator symbols.
	 * (Comprises at least &le;, &ne;, and &ge;.)
	 */
	private static final String SPEC_OPR_SYMBOLS = "\u2260\u2264\u2265";

	/**
	 * List of non-empty lexicographic tokens representing a specific syntactical
	 * unit, without white space (which is held separately in the field
	 * {@link paddings}).
	 * 
	 * @see #paddings
	 */
	private ArrayList<String> tokens;
	
	/**
	 * List of paddings (blank sequences) between the {@link tokens}. The length
	 * of this list always equals {@code tokens.size() + 1}, i.e.,
	 * {@code paddings.get(0)} is the number of blanks before the first token,
	 * {@code paddings.get(i)} with i > 0 is the length of the gap between
	 * {@code tokens.get(i-1)} and {@code tokens.get(i)}.
	 */
	private ArrayList<Integer> paddings;
	
	/**
	 * Ordered list of possible newline positions (paddings indices). If some i
	 * is included, then this means that the first character of padding i is to
	 * be interpreted as newline character.
	 */
	private ArrayList<Integer> newlines;
	
	/**
	 * The total text length of the string represented by this token list, will
	 * be updated on every modifying operation over {@code this}.
	 */
	private int len = 0;

	/**
	 * Creates an empty token list, i.e. a token list representing an empty string.
	 * 
	 * @see #TokenList(String)
	 */
	public TokenList() {
		tokens = new ArrayList<String>();
		paddings = new ArrayList<Integer>();
		paddings.add(0);
		newlines = new ArrayList<Integer>();
	}

	/**
	 * Creates a token list from the input string {@code _text}.<br/>
	 * Splits the given {@code _text} into lexical morphemes (lexemes). This
	 * will possibly overdo somewhat (e. g. signs of number literals will form
	 * separated tokens, but floating-point literals like {@code 123.45} or
	 * {@code .09e-8} will properly be preserved as contiguous tokens).<br/>
	 * Preserves string and character literals.<br/>
	 * The lengths of the inter-lexeme whitespace will be cached internally
	 * such that a nearly complete reconstruction of the original string is
	 * possible by {@link #getString()}. All whitespace characters will be
	 * replaced by ASCII space characters, however.<br/>
	 * The whitespace lengths can also be retrieved or manipulated by e.g.
	 * {@link #getPadding(int)}, {@link #setPadding(int, int, int)} 
	 * 
	 * @param _text - the text line to be split into tokens
	 * 
	 * @see #TokenList(String, boolean)
	 * @see #TokenList(String, StringList, boolean)
	 */
	public TokenList(String _text) {
		this(_text, true);
	}
		
	/**
	 * Creates a token list from the input string {@code _text}.<br/>
	 * Splits the given {@code _text} into lexical morphemes (lexemes). This
	 * will possibly overdo somewhat (e. g. signs of number literals will form
	 * separated tokens, but floating-point literals like {@code 123.45} or
	 * {@code .09e-8} will properly be preserved as contiguous tokens).<br/>
	 * By default, {@code preserveStrings} should be set {@code true},
	 * which ensures that string and character literals will be preserved.
	 * For keyword preferences, however, which possibly contain quotes like
	 * {@code "jusqu'à"} it may be necessary to set it {@code false}.<br/>
	 * Inter-lexeme whitespace will be preserved internally such that a
	 * nearly complete reconstruction of the original string is possible
	 * by {@link #getString()}. It can also be retrieved or manipulated
	 * by e.g. {@link #getPadding(int)}, {@link #setPadding(int, int, int)} 
	 * 
	 * @param _text - the text line to be split into tokens
	 * @param preserveStrings - whether strings pairwise delimited by {@code "}
	 *    or {@code '} are to be preserved rather than split.
	 * 
	 * @see #TokenList(String, StringList, boolean)
	 */
	public TokenList(String _text, boolean preserveStrings) {
		this(_text, preserveStrings, null, false);
	}
	
	/**
	 * Creates a token list from the input string {@code _text}.<br/>
	 * Splits the given {@code _text} into lexical morphemes (lexemes). This
	 * will possibly overdo somewhat (e. g. signs of number literals will form
	 * separated tokens, but floating-point literals like {@code 123.45} or
	 * {@code .09e-8} will properly be preserved as contiguous tokens).<br/>
	 * Preserves string and character literals unless some special keywords
	 * containing one or more quote characters like {@code "jusqu'à"} and
	 * contained in the list {@code specialKeywords} match a token subsequence,
	 * in which case these are prioritized to form a token. {@code _ignoreCase}
	 * is taken into account.<br/>
	 * Inter-lexeme whitespace will be preserved internally such that a
	 * nearly complete reconstruction of the original string is possible
	 * by {@link #getString()}. It can also be retrieved or manipulated
	 * by e.g. {@link #getPadding(int)}, {@link #setPadding(int, int, int)} 
	 * 
	 * @param _text - the text line to be split into tokens
	 * @param _specialKeywords - optionally a list of keyword preferences,
	 *    which contain single (or double) quotes like {@code "jusqu'à"} and
	 *    are to produce token. <b>Note</b>: The list should be ordered by
	 *    decreasing length!
	 * @param _ignoreCase - whether the matching for special keys is not to be
	 *    done in a case-sensitive way.
	 *    
	 * @see #TokenList(String, boolean)
	 */
	public TokenList(String _text, StringList _specialKeywords, boolean _ignoreCase) {
		this(_text, true, _specialKeywords, _ignoreCase);
	}
	
	/**
	 * Creates a token list from the input string {@code _text}.<br/>
	 * Splits the given {@code _text} into lexical morphemes (lexemes). This
	 * will possibly overdo somewhat (e. g. signs of number literals will form
	 * separated tokens, but floating-point literals like {@code 123.45} or
	 * {@code .09e-8} will properly be preserved as contiguous tokens).<br/>
	 * If {@code preserveStrings} is {@code false} then string and character
	 * literals are ignored and split into smaller tokens (the quotes forming
	 * isolated tokens) and argument {@code specialKeywords will be ignored}.
	 * Otherwise preserves string and character literals unless some of the
	 * {@code specialKeywords} (if given) that are containing one or more quote
	 * characters like {@code "jusqu'à"} match a token subsequence, in which
	 * case these are prioritized to form a token. {@link Syntax#ignoreCase}
	 * is taken into account.<br/>
	 * Inter-lexeme whitespace will be preserved internally such that a
	 * nearly complete reconstruction of the original string is possible
	 * by {@link #getString()}. It can also be retrieved or manipulated
	 * by e.g. {@link #getPadding(int)}, {@link #setPadding(int, int, int)} 
	 * 
	 * @param _text - the text line to be split into tokens
	 * @param preserveStrings - whether strings pairwise delimited by {@code "}
	 *    or {@code '} are to be preserved rather than split.
	 * @param specialKeywords - optionally a list of keyword preferences,
	 *    which contain single (or double) quotes like {@code "jusqu'à"} and
	 *    are to produce a token. <b>Note</b>: The list should be ordered by
	 *    decreasing length!
	 * @param _ignoreCase - whether the matching for special keys is not to be
	 *    done in a case-sensitive way.
	 */
	private TokenList(String _text, boolean preserveStrings, StringList specialKeywords, boolean _ignoreCase) {
		tokens = new ArrayList<String>();
		paddings = new ArrayList<Integer>();
		newlines = new ArrayList<Integer>();
		
		int cpnl = Character.codePointAt("\n", 0);
		
		TokenList[] tokenizedKeys = null;
		if (preserveStrings && specialKeywords != null && !specialKeywords.isEmpty()) {
			tokenizedKeys = new TokenList[specialKeywords.count()];
			for (int k = 0; k < tokenizedKeys.length; k++) {
				tokenizedKeys[k] = new TokenList(specialKeywords.get(k));
			}
		}
		
		int nBlanks = 0;
		LexState state = LexState.LX_0;
		boolean escape = false;
		StringBuilder sbToken = new StringBuilder();
		for (int ix = 0; ix < _text.length(); ix++) {
			int cp = _text.codePointAt(ix);
			switch (state) {
			case LX_0:	// Initial and interregnum state
				// We try to preserve '\n' characters in an appropriate way
				if (Character.isWhitespace(cp)) {
					if (cp == cpnl && !newlines.contains(paddings.size())) {
						newlines.add(paddings.size());
					}
					nBlanks++;
					state = LexState.LX_WHITESPACE;
				}
				// START KGU#470 2024-12-02: Issue #800 Check special keys with priority
				else if (preserveStrings && specialKeywords != null && sbToken.length() == 0) {
					for (int k = 0; k < specialKeywords.count(); k++) {
						String skey = specialKeywords.get(k);
						if (skey.length() <= _text.length() - ix
								// Simply check on substring base ...
								&& (_ignoreCase && _text.substring(ix, ix + skey.length()).equalsIgnoreCase(skey)
										|| !_ignoreCase && _text.substring(ix, ix + skey.length()).equals(skey))) {
							paddings.add(nBlanks);
							tokens.add(skey);
							len += nBlanks + skey.length();
							nBlanks = 0;
							state = LexState.LX_0;
							ix += skey.length()-1;
							break;
						}
					}
				}
				// END KGU#470 2024-12-02
				// START KGU 2024-03-21: We ought to allow names beginning with '_'
				//else if (Character.isLetter(cp)) {
				else if (Character.isLetter(cp) || cp == '_') {
				// END KGU 2024-03-21
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
					if (ix + 1 < _text.length() &&
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
					state = preserveStrings ? LexState.LX_STRING1 : LexState.LX_SYMBOL;
				}
				else if (cp == '\"') {
					sbToken.appendCodePoint(cp);
					state = preserveStrings ? LexState.LX_STRING2 : LexState.LX_SYMBOL;
				}
				// FIXME: Should these really be unified (decomposed) here?
				else if (SPEC_OPR_SYMBOLS.indexOf(cp) >= 0) {
					if (cp == '\u2260') {
						paddings.add(0);
						tokens.add("<>");
						len += 2;
					}
					else if (cp == '\u2264') {
						paddings.add(0);
						tokens.add("<=");
						len += 2;
					}
					else if (cp == '\u2265') {
						paddings.add(0);
						tokens.add(">=");
						len += 2;
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
					paddings.add(nBlanks);
					tokens.add(sbToken.toString());
					len += nBlanks + sbToken.length();
					nBlanks = 0;
					sbToken.delete(0, sbToken.length());
					state = LexState.LX_0;
				}
				else {
					// Something different seems to start here
					paddings.add(nBlanks);
					tokens.add(sbToken.toString());
					len += nBlanks + sbToken.length();
					nBlanks = 0;
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
					paddings.add(nBlanks);
					tokens.add(sbToken.toString());
					len += nBlanks + sbToken.length();
					nBlanks = 0;
					sbToken.delete(0, sbToken.length());
					state = LexState.LX_0;
				}
				else {
					// Something different seems to start here
					paddings.add(nBlanks);
					tokens.add(sbToken.toString());
					len += nBlanks + sbToken.length();
					nBlanks = 0;
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
					paddings.add(nBlanks);
					tokens.add(sbToken.toString());
					len += nBlanks + sbToken.length();
					nBlanks = 0;
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
					paddings.add(nBlanks);
					tokens.add(sbToken.toString());
					len += nBlanks + sbToken.length();
					nBlanks = 0;
					sbToken.delete(0, sbToken.length());
					state = LexState.LX_0;
				}
				else {
					// Something different seems to start here
					paddings.add(nBlanks);
					tokens.add(sbToken.toString());
					len += nBlanks + sbToken.length();
					nBlanks = 0;
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
					paddings.add(nBlanks);
					tokens.add(sbToken.toString());
					len += nBlanks + sbToken.length();
					nBlanks = 0;
					sbToken.delete(0, sbToken.length());
					state = LexState.LX_0;
				}
				else {
					// Something different seems to start here
					paddings.add(nBlanks);
					tokens.add(sbToken.toString());
					len += nBlanks + sbToken.length();
					nBlanks = 0;
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
					paddings.add(nBlanks);
					tokens.add(sbToken.toString());
					len += nBlanks + sbToken.length();
					nBlanks = 0;
					sbToken.delete(0, sbToken.length());
					state = LexState.LX_0;
				}
				else {
					// Something different seems to start here
					paddings.add(nBlanks);
					tokens.add(sbToken.toString());
					len += nBlanks + sbToken.length();
					nBlanks = 0;
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
					paddings.add(nBlanks);
					tokens.add(sbToken.toString());
					len += nBlanks + sbToken.length();
					nBlanks = 0;
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
						paddings.add(nBlanks);
						tokens.add(sbToken.toString());
						len += nBlanks + sbToken.length();
						nBlanks = 0;
						sbToken.delete(0, sbToken.length());
						state = LexState.LX_0;
					}
					else {
						// Nothing between the two '§'
						// Handle one of them as singular token and stay in the state
						paddings.add(nBlanks);
						tokens.add("§");
						len += nBlanks + sbToken.length();
						nBlanks = 0;
						// The new '§' is again a potential start (already in sbToken)
					}
				}
				else {
					// Make the initial '§' a single token and check the remainder
					paddings.add(nBlanks);
					tokens.add("§");
					len += nBlanks + sbToken.length();
					nBlanks = 0;
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
					paddings.add(nBlanks);
					tokens.add(sbToken.toString());
					len += nBlanks + sbToken.length();
					nBlanks = 0;
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
					paddings.add(nBlanks);
					tokens.add(sbToken.toString());
					len += nBlanks + sbToken.length();
					nBlanks = 0;
					sbToken.delete(0, sbToken.length());
					state = LexState.LX_0;
				}
				break;
			case LX_WHITESPACE:
				if (Character.isWhitespace(cp)) {
					nBlanks++;
					state = LexState.LX_WHITESPACE;
				}
				else {
					if (sbToken.length() > 0) {
						paddings.add(nBlanks);
						tokens.add(sbToken.toString());
						len += nBlanks + sbToken.length();
						nBlanks = 0;
						sbToken.delete(0, sbToken.length());
					}
					state = LexState.LX_0;
					ix--;	// Process this code point again
				}
				break;
			case LX_SYMBOL:
				// Lets's see if there is some possible combination
				if (SYMBOL_CONTINUATIONS.indexOf(cp) >= 0) {
					// May belong to the symbol,
					int oldLen = sbToken.length();
					sbToken.appendCodePoint(cp);
					if (!LEX_SYMBOL_LIST.contains(sbToken.toString())) {
						String token = sbToken.substring(0, oldLen);
						// May not be part, so push the former symbol
						paddings.add(nBlanks);
						tokens.add(token);
						len += nBlanks + sbToken.length();
						nBlanks = 0;
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
					paddings.add(nBlanks);
					tokens.add(sbToken.toString());
					len += nBlanks + sbToken.length();
					nBlanks = 0;
					sbToken.delete(0, sbToken.length());
					state = LexState.LX_0;
					escape = false;
				}
				else {
					paddings.add(nBlanks);
					tokens.add(sbToken.toString());
					len += nBlanks + sbToken.length();
					nBlanks = 0;
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
			paddings.add(nBlanks);
			tokens.add(sbToken.toString());
			len += nBlanks + sbToken.length();
			nBlanks = 0;
		}
		paddings.add(nBlanks);
		len += nBlanks;
	}
	
	/**
	 * Constructs a token list containing the tokens and padding widths of
	 * the specified token list {@code other}.
	 * 
	 * @param other - the token list to be copied
	 */
	public TokenList(TokenList other)
	{
		tokens = new ArrayList<String>(other.tokens);
		paddings = new ArrayList<Integer>(other.paddings);
		newlines = new ArrayList<Integer>(other.newlines);
		len = other.len;
	}
	
	/**
	 * Concatenates the token lists held in collection {@code tokenLists} into a single token
	 * list according to the order of the token lists in {@code tokenLists}, possibly inserting
	 * the tokens and whitespace emerging from {@code separator} between them unless
	 * {@code separator} is {@code null}.
	 * 
	 * @param tokenLists - a collection of {@link TokenList}s
	 * @param separator - if it contains only whitespace then this amount of blanks will be
	 *    garanteed between the original token lists, otherwise (and if not {@code null}) the
	 *    token sequence derived from {@code separator} will be inserted between the token lists
	 *    contained in {@code tokenLists}. If {@code tokenLists} is empty then {@code separator}
	 *    will not be used.
	 * 
	 * @return the composed concatenation from {@code tokenLists} and {@code separator}
	 */
	public static TokenList concatenate(Collection<? extends TokenList> tokenLists, String separator)
	{
		TokenList total = new TokenList();
		int i = 0;
		for (TokenList tokens: tokenLists) {
			if (i > 0 && separator != null) {
				if (separator.isBlank()) {
					if (separator.contains("\n")
							&& !total.newlines.contains(total.size())
							&& !tokens.newlines.contains(0)) {
						total.newlines.add(total.size());
					}
					if (!separator.isEmpty() && !total.isBlank()) {
						total.setPadding(total.size()-1, -1, separator.length());
					}
				}
				else {
					total.add(separator);
				}
			}
			total.addAll(tokens);
			i++;
		}
		return total;
	}
	
	/**
	 * Restores the text line represented by this token list as string
	 * including the padding whitespace between the tokens.
	 * 
	 * @return the concatenated string
	 */
	public String getString()
	{
		StringBuilder sb = new StringBuilder();
		int nTokens = tokens.size();
		for (int i = 0; i < nTokens; i++) {
			if (newlines.contains(i)) {
				sb.append("\n");
				sb.append(" ".repeat(paddings.get(i)-1)); // padding should not be 0
			}
			else {
				sb.append(" ".repeat(paddings.get(i)));
			}
			sb.append(tokens.get(i));
		}
		if (newlines.contains(nTokens)) {
			sb.append("\n");
			sb.append(" ".repeat(paddings.get(nTokens)-1)); // padding should not be 0
		}
		else {
			sb.append(" ".repeat(paddings.get(nTokens)));
		}
		return sb.toString();
	}
	
	/**
	 * Returns the tokens as {@link StringList} either without gaps or with
	 * whitespace elements according to the paddings between the tokens.
	 * 
	 * @param withGaps - if {@code false} then teh result will only contain
	 *    the (non-blank) tokens, otherwise ({@code true}) the paddings will
	 *    be inserted as space strings between them.
	 * @return the composed {@link StringList}
	 */
	public StringList getTokens(boolean withGaps)
	{
		StringList tokStrings = new StringList();
		for (int i = 0; i < tokens.size(); i++) {
			if (withGaps) {
				tokStrings.add(" ".repeat(paddings.get(i)));
			}
			tokStrings.add(tokens.get(i));
		}
		if (withGaps) {
			tokStrings.add(" ".repeat(paddings.get(tokens.size())));
		}
		return tokStrings;
	}
	
	/**
	 * @return the number of tokens this token list consists of
	 * 
	 * @see #length()
	 * @see #getPadding()
	 */
	public int size()
	{
		return tokens.size();
	}

	/**
	 * @return the length (total number of characters) of the represented
	 * text string including whitespace (but excluding a terminating backslash).
	 * 
	 * @see #size()
	 * @see #getPadding()
	 */
	public int length()
	{
		return len;
	}
	
	/**
	 * Returns the token at the specified position in this token list.
	 * 
	 * @param index - index of the token to return
	 * @return the token at the specified position in this token list.
	 * 
	 * @throws IndexOutOfBoundsException if the index is out of range
	 *     {@code (index < 0 || index >= size())}
	 * 
	 * @see #set(int, String)
	 * @see #getPadding(int)
	 */
	public String get(int index) {
		return tokens.get(index);
	}
	
	/**
	 * @return the very first token of this token list (if it has got one).
	 * 
	 * @throws IndexOutOfBoundsException if this token list does not contain
	 *     any token (i.e. {@code isBlank() == true})
	 * 
	 * @see #get(int)
	 * @see #getLast()
	 * @see #getPadding(int)
	 */
	public String getFirst()
	{
		return tokens.get(0);
	}

	/**
	 * @return the very last token of this token list (if it has got one).
	 * 
	 * @throws IndexOutOfBoundsException if this token list does not contain
	 *     any token (i.e. {@code isBlank() == true})
	 * 
	 * @see #get(int)
	 * @see #getFirst()
	 * @see #getPadding(int)
	 */
	public String getLast()
	{
		return tokens.get(tokens.size() - 1);
	}

	/**
	 * Replaces the token at the specified position in this list with
	 * the specified string, which should have token properties (i.e.
	 * should not fall apart into several tokens if passed to e.g.
	 * {@link #TokenList(String)}).
	 * 
	 * @param index - position of the token to be replaced
	 * @param token - the new token to replace the one at position {@code index}
	 * @return the token previously at the specified position
	 * @throws IndexOutOfBoundsException if the index is out of range
	 *    {@code (index < 0 || index >= size())}
	 * 
	 * @see #get(int)
	 * @see #add(String)
	 * @see #add(int, String)
	 * @see #addAll(int, TokenList)
	 * @see #setPadding(int, int, int)
	 */
	public String set(int index, String token) {
		if (token == null || token.isEmpty()) {
			// Here the gap check is integrated in remove(index)...
			return remove(index);
		}
		String replaced = tokens.set(index, token);
		len += token.length() - replaced.length();
		// Check whether gaps will have to be enforced
		ensureGap(index);
		ensureGap(index+1);
		return replaced;
	}
	
	/**
	 * Sets the paddings (i.e. the number of blanks) around the token at given
	 * {@code index}.<br/>
	 * <b>Note:</b> This method will automatically ensure that at least a single
	 * blank separates two tokens that otherwise would amalgamate into one token
	 * on concatenating.
	 * 
	 * @param index - the position of the token the paddings of which are to be set
	 * @param left - the intended left padding (towards the preceding token or the
	 *     start, respectively), ignored when negative.
	 * @param right - the intended right padding (towards the successor token or the
	 *     end, respectively), ignored when negative
	 * @returns the number of blanks the total padding sum grows (or is reduced) by
     * @throws IndexOutOfBoundsException if the index is out of range
     *      {@code (index < 0 || index >= size())}
     * 
     * @see #getPadding(int)
     * @see #set(int, String)
	 */
	public int setPadding(int index, int left, int right)
	{
		int growth = 0;
		if (left >= 0) {
			growth += left - paddings.set(index, left);
			if (index > 0 && paddings.get(index) == 0
					&& this.ensureGap(index-1)) {
				growth++;
			}
		}
		if (right >= 0) {
			growth += right - paddings.set(index + 1, right);
			if (index+1 < tokens.size() && paddings.get(index + 1) == 0
				&& this.ensureGap(index)) {
				growth++;
			}
		}
		len += growth;
		return growth;
	}

	/**
	 * Returns the total number of blanks the paddings at the beginning, the end,
	 * and between the tokens amount to. Will not report blanks inside tokens
	 * (tokens are assumed to be contiguous).
	 * 
	 * @return total padding (number of inter-token blanks)
	 * 
	 * @see #getPadding(int)
	 * @see #setPadding(int, int, int)
	 */
	public int getPadding()
	{
		int nBlanks = 0;
		for (int i = 0; i < paddings.size(); i++) {
			nBlanks += paddings.get(i);
		}
		return nBlanks;
	}
	
	/**
	 * Returns the padding pair around the token with given {@code index}.
	 * 
	 * @param index - position of the interesting token
	 * @return an array of two integer values representing the number of
	 *     blanks to the left and right of the token, respectively
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0
     *      || index >= size())
     * 
     * @see #setPadding(int, int, int)
     * @see #getPadding()
     * @see #length()
	 */
	public int[] getPadding(int index)
	{
		return new int[] {paddings.get(index), paddings.get(index+1)};
	}
	
	/**
	 * Removes all (but necessary) inter-token whitespace and trims the token
	 * list at the beginning and the end.
	 * 
	 * @return change of the number of whitespace characters (likely to be a
	 *     negative result, but could even be positive, if more necessary gaps
	 *     are restored than superfluous whitespace removed).
	 * 
	 * @see #trim()
	 * @see #trimEnd()
	 * @see #trimStart()
	 */
	public int shrink()
	{
		int nWS = 0;
		for (int i = 0; i < tokens.size(); i += 2) {
			nWS += setPadding(i, 0, 0);
			if (paddings.get(i) == 0) {
				newlines.remove(Integer.valueOf(i));
			}
			if (paddings.get(i+1) == 0) {
				newlines.remove(Integer.valueOf(i+1));
			}
		}
		if (tokens.size() % 2 == 1) {
			Integer pd = paddings.set(tokens.size(), 0);
			nWS += pd;
			newlines.remove(pd);
		}
		return nWS;
	}
	
	/**
	 * Eliminates absolutely all whitespace around the tokens without ensuring
	 * minimum gaps between tokens that would amalgamate with {@link #getString()}.<br/>
	 * <b>Hint:</b> Necessary gaps can be automatically restored with
	 * {@link #shrink()} or, individually, by {@link #setPadding(int, int, int)}.
	 * 
	 * @param fromIndex - index of the token before the wiping
	 * @param toIndex - index of the token behind the wiping
	 * @return change of the number of whitespace characters. Can only be 0
	 *    or negative.
	 * 
	 * @see #removePaddings(int, int)
	 * @see #shrink()
	 * @see #setPadding(int, int, int)
	 */
	public int removePaddings()
	{
		int nWS = 0;
		for (int i = 0; i < paddings.size(); i++) {
			nWS -= paddings.set(i, 0);
		}
		newlines.clear();
		return nWS;
	}
	
	/**
	 * Eliminates all whitespace between token {@code fromIndex} and token
	 * {@code toIndex} without ensuring minimum gaps between tokens that
	 * would amalgamate with {@link #getString()}. To have any effect,
	 * {@code toIndex > @code fromIndex} must hold. The outer paddings will
	 * remain even if {@code fromIndex == 0} and {@code toIndex == size()-1}.
	 * 
	 * To remove them alone use {@link #trim()}, to remove all paddings use
	 * {@link #removePaddings()}.<br/>
	 * <b>Hint:</b> Necessary gaps can be automatically restored with
	 * {@link #shrink()} or, individually, by {@link #setPadding(int, int, int)}.
	 * 
	 * @param fromIndex - index of the token before the wiping
	 * @param toIndex - index of the token behind the wiping
	 * @return change of the number of whitespace characters. Can only be 0
	 *    or negative.
	 * @throws #IndexOutOfBoundsException - if {@code fromIndex} or {@code toIndex}
	 *    is out of range ({@code fromIndex < 0 || toIndex >= size()})
	 * 
	 * @see #removePaddings()
	 * @see #shrink()
	 * @see #trim()
	 */
	public int removePaddings(int fromIndex, int toIndex)
	{
		int nWS = 0;
		for (int i = fromIndex+1; i <= toIndex; i++) {
			nWS -= paddings.set(i, 0);
			newlines.remove(Integer.valueOf(i));
		}
		return nWS;
	}
	
	/**
	 * Removes white space characters from the beginning and end of the token list.
	 *
	 * @return the number of removed blanks
	 * 
	 * @see #trimStart()
	 * @see #trimEnd()
	 */
	public int trim()
	{
		int shortened = paddings.set(0, 0) + paddings.set(tokens.size(), 0);
		newlines.remove(Integer.valueOf(0));
		newlines.remove(Integer.valueOf(tokens.size()));
		len -= shortened;
		return shortened;
	}
	
	/**
	 * Removes trailing white space characters from the end of the token list.
	 *
	 * @return the number of removed blanks
	 * 
	 * @see #trim()
	 * @see #trimStart()
	 */
	public int trimEnd()
	{
		int shortened = paddings.set(tokens.size(), 0);
		newlines.remove(Integer.valueOf(tokens.size()));
		len -= shortened;
		return shortened;
	}
	
	/**
	 * Removes white space characters from the beginning of the token list.
	 *
	 * @return the number of removed blanks
	 * 
	 * @see #trim()
	 * @see #trimEnd()
	 */
	public int trimStart()
	{
		int shortened = paddings.set(0, 0);
		newlines.remove(Integer.valueOf(0));
		len -= shortened;
		return shortened;
	}
	
	/**
	 * Returns a copy of the portion of this token list between the specified
	 * {@code fromIndex}, inclusive, and {@code toIndex}, exclusive.  (If
	 * {@code fromIndex} and {@code toIndex} are equal, the returned list is
	 * empty.)
	 *
	 * @param fromIndex - low endpoint (inclusive) of the subList
	 * @param toIndex - high endpoint (exclusive) of the subList
	 * @return a copy of the specified range within this list

	 * @throws IndexOutOfBoundsException for an illegal endpoint index value
	 *    ({@code fromIndex < 0 || toIndex > size || fromIndex > toIndex})
	 * @throws IllegalArgumentException if the endpoint indices are out of
	 *    order ({@code fromIndex > toIndex}).
	 * 
	 * @see #subSequenceToEnd(int)
	 * @see #remove(int, int)
	 */
	public TokenList subSequence(int fromIndex, int toIndex)
	{
		TokenList part = new TokenList();
		part.tokens.addAll(tokens.subList(fromIndex, toIndex));
		if (fromIndex < toIndex) {
			part.paddings.addAll(1, paddings.subList(fromIndex+1, toIndex));
			part.paddings.add(0);
		}
		for (int i = 0; i < part.tokens.size(); i++) {
			part.len += part.tokens.get(i).length() + part.paddings.get(i);
		}
		for (int i = 0; i < newlines.size(); i++) {
			int nlPos = newlines.get(i);
			if (nlPos > fromIndex && nlPos < toIndex - 1) {
				part.newlines.add(nlPos - fromIndex);
			}
		}
		return part;
	}

	/**
	 * Returns a copy of the portion of this token list from the specified
	 * {@code fromIndex}, inclusive, towards the end. If {@code fromIndex}
	 * equals the number of tokens ({@code fromIndex = size()} then the returned
	 * token list will not contain any token.
	 *
	 * @param fromIndex - low endpoint (inclusive) of the subList
	 * @return a copy of the specified range within this list

	 * @throws IndexOutOfBoundsException for an illegal start index value
	 *    ({@code fromIndex < 0 || fromIndex > size()})
	 * 
	 * @see #subSequence(int, int)
	 * @see #remove(int, int)
	 */
	public TokenList subSequenceToEnd(int fromIndex)
	{
		return subSequence(fromIndex, tokens.size());
	}

	/**
	 * Checks whether the number of tokens is zero.
	 * 
	 * @return {@code true} if this token list does not contain a single token
	 *    (but it might still contain whitespace)<br/>
	 *    
	 * @see #isEmpty()
	 * @see #size()
	 * @see #trim()
	 */
	public boolean isBlank() {
		return tokens.isEmpty(); // or size() == null
	}

	/**
	 * @return {@code true} if this token list neither contains tokens nor
	 *    whitespace, i.e. its string length would be 0
	 * 
	 * @see #isBlank()
	 * @see #length()
	 * @see #trim()
	 */
	public boolean isEmpty() {
		return len == 0;
	}

	/**
	 * Check whether this token list contains the specified string as
	 * lexical token.<br/>
	 * Hint: In order to check whether certain string might be a substring
	 * of the string represented by this token list, consider one of:
	 * <ul>
	 * <li> {@code getString().contains(string)}</li>
	 * <li> {@code contains(new TokenList(string), true)}</li>
	 * </ul>
	 * 
	 * @param token - the token to look for
	 * @return {@code true} if the specified string is member of this token
	 *     list
	 * 
	 * @see #contains(String, boolean)
	 * @see #indexOf(String)
	 * @see #contains(TokenList, boolean)
	 * @see #getString()
	 */
	public boolean contains(String token) {
		return tokens.contains(token);
	}
	
	/**
	 * Check whether this token list contains a lexical token that equals the
	 * specified string {@code token} in either a case-aware ore case-ignorant way.<br/>
	 * Hint: In order to check whether certain string might be a substring
	 * of the string represented by this token list, consider one of:
	 * <ul>
	 * <li> {@code getString().toLowerCase().contains(string.toLowerCase())}</li>
	 * <li> {@code contains(new TokenList(string), false)}</li>
	 * </ul>
	 * 
	 * @param token - the token to look for
	 * @param caseSensitive - whether case is to matter on comparison
	 * @return {@code true} if a token that equals the specified string {@code token}
	 *     in a case-aware or case-insensitive way is member of this token list
	 * 
	 * @see #contains(String)
	 * @see #indexOf(String)
	 * @see #indexOf(String, boolean)
	 * @see #lastIndexOf(String, boolean)
	 * @see #contains(TokenList, boolean)
	 * @see #getString()
	 */
	public boolean contains(String token, boolean caseSensitive) {
		return indexOf(token, caseSensitive) >= 0;
	}
	
	/**
	 * Check whether this token list contains token list {@code subList } as an
	 * equivalent sublist in a possibly case-ignorant way.<br/>
	 * Hint: In order to check whether certain string might be a substring
	 * of the string represented by this token list, consider one of:
	 * <ul>
	 * <li> {@code getString().toLowerCase().contains(string.toLowerCase())}</li>
	 * <li> {@code contains(new TokenList(string), false)}</li>
	 * </ul>
	 * 
	 * @param token - the token to look for
	 * @return {@code true} if a token that equals the specified string in a case-
	 *     insensitive way is member of this token list
	 * 
	 * @see #contains(String)
	 * @see #contains(String, boolean)
	 * @see #indexOf(String, boolean)
	 * @see #lastIndexOf(TokenList, boolean)
	 * @see #getString()
	 */
	public boolean contains(TokenList subList, boolean matchCase) {
		return indexOf(subList, 0, matchCase) >= 0;
	}

	/**
	 * Counts the exact occurrences of the given string {@code token} among
	 * the tokens of this list
	 *
	 * @param token - the string to search for
	 * @return the number of occurrences
	 *
	 * @see #count(String, boolean)
	 */
	public int count(String token) {
		return count(token, true);
	}

	/**
	 * Counts the (exact or case-ignorant) occurrences of the given string
	 * {@code token} among the tokens of this list
	 *
	 * @param token - the string to search for
	 * @param matchCase - whether upper/lower case should make a difference
	 * @return the number of occurrences
	 */
	public int count(String token, boolean matchCase) {
		int cnt = 0;
		if (matchCase) {
			for (int i = 0; i < tokens.size(); i++) {
				if (tokens.get(i).equals(token)) {
					cnt++;
				}
			}
		} else {
			for (int i = 0; i < tokens.size(); i++) {
				if (tokens.get(i).equalsIgnoreCase(token)) {
					cnt++;
				}
			}
		}
		return cnt;
	}
	
	/**
	 * Returns an array containing all the tokens of this list in proper
	 * sequence (from first to last element).<br/>
	 * The returned array will be "safe" in that no references to it are
	 * maintained by this list.<br/>
	 * The caller is thus free to modify the returned array.<br/> 
	 * 
	 * @param withPadding - if {@code true} then the paddings will be
	 *     placed around as blank string elements and between the tokens
	 *     in the array, such that the returned array will have 2 *
	 *     {@link #size()} + 1 elements.
	 * @return the array of the tokens or of alternating whitespace
	 *    strings and tokens.
	 */
	public String[] toArray(boolean withPadding) {
		int size = tokens.size() + (withPadding ? paddings.size() : 0);
		String[] result = new String[size];
		if (withPadding) {
			result[0] = " ".repeat(paddings.get(0));
			for (int i = 0; i < tokens.size(); i++) {
				result[2*i] = tokens.get(i);
				result[2*i+1] = " ".repeat(paddings.get(i));
			}	
		}
		else {
			for (int i = 0; i < tokens.size(); i++) {
				result[i] = tokens.get(i);
			}
		}
		return result;
	}

	/**
	 * Appends the token list derived from the given {@code string} at the
	 * end, beyond the end padding.<br/>
	 * May automatically insert a blank between the formerly last token and
	 * the first appended one if both would otherwise amalgamate into a
	 * single one on concatenation.
	 * 
	 * @param string - the token to be added at end. Nothing will happen
	 *    if {@code string} is {@code null} or empty.
	 * @return {@code true} if the token was appended, {@code} otherwise
	 */
	public boolean add(String string) {
		if (string == null || string.isEmpty()) {
			return false;
		}
		// We may not be sure to obtain a genuine token, so better split
//		tokens.add(string);
//		paddings.add(0);
//		len += string.length();
//		if (nTokens >= 1 && (new TokenList(tokens.get(nTokens-1) + tokens.get(nTokens))).size() == 1) {
//			paddings.set(nTokens, 1);
//			len++;
//		}
//		return true;
		return addAll(new TokenList(string));	
	}

	/**
	 * Adds (inserts) the token list derived from the given {@code string}
	 * at the given {@code index}, such that tokens at this and any higher
	 * position will be shifted.<br/>
	 * May automatically insert blanks around the new tokens if otherwise
	 * neighbouring tokens might amalgamate into a single one on concatenation.
	 * 
	 * @param string - the string to be split and inserted before the token with
	 *    position {@index} (if {@code index = size()} then at end). Nothing
	 *    will happen if {@code string} is {@code null} or empty.
	 * @return {@code true} if the token was appended, {@code} otherwise
	 */
	public boolean add(int index, String string)
	{
		if (string == null || string.isEmpty()) {
			return false;
		}
		// We may not be sure to obtain a genuine token, so better split
//		tokens.add(index, token);
//		paddings.add(index, paddings.get(index));
//		len += token.length() + paddings.get(index);
//		// Ensure sensible gaps
//		if (index > 0 && paddings.get(index) == 0
//				&& (new TokenList(tokens.get(index-1)+tokens.get(index))).size() == 1) {
//			paddings.set(index, 1);
//			len++;
//		}
//		if (index + 1 < tokens.size() && paddings.get(index+1) == 0
//				&& (new TokenList(tokens.get(index)+tokens.get(index+1))).size() == 1) {
//			paddings.set(index + 1, 1);
//			len++;
//		}
//		return true;
		return this.addAll(index, new TokenList(string));
	}

	/**
	 * Removes the token at the specified position from this list. Shifts
	 * any subsequent elements to the left (subtracts one from their indices).
	 * 
	 * @param index - the index of the token to be removed
	 * @return the element that was removed from the list
	 * 
	 * @throws IndexOutOfBoundsException if {@code index} index is out of range
	 *     ({@code index < 0 || index >= size()})
	 */
	public String remove(int index) {
		synchronized (this) {
			if (index == 0) {
				len -= paddings.remove(1);
			}
			else if (index == tokens.size() - 1) {
				len -= paddings.remove(index);
			}
			else {
				int gap = paddings.get(index) + paddings.get(index+1);
				paddings.set(index+1, gap/2 + gap % 2);
				paddings.remove(index);
				len -= gap/2;
				if (paddings.get(index) == 0
						&& (new TokenList(tokens.get(index-1)+tokens.get(index))).size() == 1) {
					paddings.set(index, 1);
					len++;
				}
			}
			len -= tokens.get(index).length();
			String removed = tokens.remove(index);
			int iNlDup = -1;
			for (int i = 0; i < newlines.size(); i++) {
				int nlPos = newlines.get(i);
				if (nlPos > index) {
					if (newlines.contains(nlPos - 1)) {
						// Remember the index of the duplicate position to remove it later
						iNlDup = i;
					}
					newlines.set(i, nlPos - 1);
				}
			}
			// If two newline positions clashed then remove one of them
			if (iNlDup >= 0) {
				newlines.remove(iNlDup);
			}
			if (index > 0) {
				ensureGap(index-1);
			}
			return removed;
		}
	}
	
	/**
	 * Cuts out and removes the portion of this token list between the specified
	 * {@code fromIndex}, inclusive, and {@code toIndex}, exclusive. (If
	 * {@code fromIndex} and {@code toIndex} are equal, then nothing will be
	 * removed and the returned list will be empty.)<br/>
	 * Paddings surrounding the remove subsequence will be left with the remaining
	 * token list.
	 *
	 * @param fromIndex - low endpoint (inclusive) of the removed subList
	 * @param toIndex - high endpoint (exclusive) of the remove subList
	 * @return the removed token sequence from this list
	
	 * @throws IndexOutOfBoundsException for an illegal endpoint index value
	 *    (fromIndex < 0 || toIndex > size ||fromIndex > toIndex)
	 * @throws IllegalArgumentException if the endpoint indices are out of
	 *    order (fromIndex > toIndex).
	 * 
	 * @see #remove(int, int, boolean)
	 * @see #subSequence(int, int)
	 */
	public TokenList remove(int fromIndex, int toIndex) {
		return remove(fromIndex, toIndex, false);
	}

	/**
	 * Cuts out and removes the portion of this token list between the specified
	 * {@code fromIndex}, inclusive, and {@code toIndex}, exclusive.  (If
	 * {@code fromIndex} and {@code toIndex} are equal, then nothing will be
	 * removed and the returned list will be empty.)
	 *
	 * @param fromIndex - low endpoint (inclusive) of the removed subList
	 * @param toIndex - high endpoint (exclusive) of the remove subList
	 * @param removePaddings - if {@code true} then the paddings around the
	 *    removed subsequence will be removed to a minimum (i.e. a single
	 *    blank if the neighbouring tokens might otherwise coagulate on
	 *    concatenation) in this token list but put around the extracted
	 *    subsequence.
	 * @return the removed token sequence from this list. Will always be
	 *    trimmed

	 * @throws IndexOutOfBoundsException for an illegal endpoint index value
	 *    (fromIndex < 0 || toIndex > size ||fromIndex > toIndex)
	 * @throws IllegalArgumentException if the endpoint indices are out of
	 *    order (fromIndex > toIndex).
	 * 
	 * @see #remove(int)
	 * @see #subSequence(int, int)
	 */
	public TokenList remove(int fromIndex, int toIndex, boolean removePaddings) {
		TokenList removed = new TokenList();
		if (fromIndex < toIndex) {
			removed.tokens.addAll(tokens.subList(fromIndex, toIndex));
			removed.paddings.addAll(1, paddings.subList(fromIndex+1, toIndex));
			removed.paddings.add(0);
			if (removePaddings) {
				removed.paddings.set(0, Math.max(0, -this.setPadding(fromIndex, 0, -1)));
				removed.paddings.set(removed.size(), Math.max(0, -this.setPadding(toIndex, -1, 0)));
			}
			removed.len = paddings.get(removed.tokens.size());
			for (int i = 0; i < removed.tokens.size(); i++) {
				removed.len += removed.tokens.get(i).length() + removed.paddings.get(i);
			}
			for (int i = toIndex - 1; i >= fromIndex; i--) {
				remove(i);
			}
			// If at least tokens remained, check their minimum gap
			if (fromIndex > 0) {
				ensureGap(fromIndex-1);
			}
		}
		return removed;
	}
	
	/**
	 * Check if token i and its successor (if existent) need separating space if they do but
	 * haven't got then insert a blank between them.
	 * 
	 * @param index - index of the first of two neighbouring tokens
	 * @return {@code true} if a blank had to be inserted, {@code false} otherwise
	 */
	private boolean ensureGap(int i) {
		if (i >= 0 && i + 1 < tokens.size() && paddings.get(i+1) == 0) {
			String token0 = tokens.get(i);
			String token1 = tokens.get(i+1);
			if (Character.isJavaIdentifierPart(token0.charAt(token0.length()-1))
					&& Character.isJavaIdentifierPart(token1.charAt(0))) {
				paddings.set(i+1, 1);
				len++;
				return true;
			}
		}
		return false;
	}

	/**
	 * Removes the first token equal to the specified string from this list. Shifts
	 * any subsequent elements to the left (subtracts one from their indices).
	 * 
	 * @param token - the token to be removed
	 * @return the element that was removed from the list
	 */
	public boolean remove(String token) {
		int index = tokens.indexOf(token);
		if (index >= 0) {
			this.remove(index);
			return true;
		}
		return false;
	}

	/**
	 * Removes the first token equal to the specified string from this list. Shifts
	 * any subsequent elements to the left (subtracts one from their indices).
	 * 
	 * @param token - the token to be removed
	 * @return the element that was removed from the list
	 */
	public boolean removeLast(String token) {
		int index = tokens.lastIndexOf(token);
		if (index >= 0) {
			this.remove(index);
			return true;
		}
		return false;
	}

	/**
	 * Removes all tokens equal to the specified string from this list. Shifts
	 * any subsequent elements to the left (subtracts one from their indices).<br/>
	 * Automatically ensures necessary gaps between tokens that might otherwise
	 * amalgamate on concatenation.
	 * 
	 * @param token - the token to be removed
	 * @return the number of tokens removed from the list
	 */
	public int removeAll(String token) {
		int count = 0;
		for (int i = tokens.size()-1; i >= 0; i--)
		{
			if (tokens.get(i).equals(token)) {
				this.remove(i);
				count++;
			}
		}
		return count;
	}

	/**
	 * Returns {@code true} if this token list contains all of the elements of
	 * the specified collection {@code c} as token at least once, no matter at
	 * what position. This will of course fail if only one element of {@code c}
	 * is not a string or not a contiguous token.
 	 * 
	 * @param c - the collection to be checked for containment in this collection
	 * @return {@code true} if this collection contains all of the elements in
	 *     the specified collection {@code c}
	 */
	public boolean containsAll(Collection<?> c) {
		return tokens.containsAll(c);
	}

	/**
	 * Returns {@code true} if this token list contains all of the elements of
	 * the specified {@code StringList strings} as token at least once, no matter at
	 * what position.
 	 * 
	 * @param strings - the {@link StringList} to be checked for containment in
	 *    this collection.
	 * @return {@code true} if this token list contains all of the element strings
	 *     from the specified {@code StringList strings}
	 */
	public boolean containsAll(StringList strings) {
		for (int i = 0; i < tokens.size(); i++) {
			if (!tokens.contains(strings.get(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns {@code true} if this token list contains any of the elements of
	 * the specified collection {@code c} as token, no matter at what position.
	 * This will of course fail if only one element of {@code c} is not a string
	 * or not a contiguous token.
 	 * 
	 * @param c - a collection to be checked for containment in the token list
	 * @return {@code true} if this collection contains all of the elements in
	 *     the specified collection {@code c}
	 */
	public boolean containsAny(Collection<?> c) {
		for (Object o: c) {
			if (tokens.contains(o)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns {@code true} if this token list contains any of the elements of
	 * the specified {@code StringList strings} as token, no matter at
	 * what position.
 	 * 
	 * @param strings - the {@link StringList} to be checked for containment in
	 *    this collection.
	 * @return {@code true} if this token list contains some of the element strings
	 *     from the specified {@code StringList strings}
	 */
	public boolean containsAny(StringList strings) {
		for (int i = 0; i < tokens.size(); i++) {
			if (tokens.contains(strings.get(i))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Appends all of the tokens of the specified token list {@code other}
	 * to this list. The new tokens will appear in this list in the same
	 * order as in {@code other}. The tokens will not be checked or split.
	 * 
	 * @param other - sourc tokens list
	 * @return {@code true} if at least one token could be added.
	 */
	public boolean addAll(TokenList other) {
		return addAll(tokens.size(), other);
	}

	/**
	 * Inserts all of the elements in the specified token list {@code other}#
	 * into this list, starting at the specified position. Shifts the token
	 * currently at that position (if any) and any subsequent tokens (together
	 * with their whitespace widths) to the right (increases their indices).
	 * The new tokens will appear in this list in the same order as in {@code
	 * other}.
	 * 
	 * @param index - insertion position
	 * @param tokens - source token list
	 * @return {@code true} if at least one token could be added.
	 */
	public boolean addAll(int index, TokenList other) {
		synchronized (this) {
			int otherSize = other.size();
			int pad0 = other.paddings.get(0);
			int padI = paddings.get(index);
			// We split the padding at the insertion position into halves
			int pad1 = padI/2 + pad0;
			int pad2 = padI/2 + padI%2;
			// Don't eliminate a previous pre-token padding
			if (pad1 > 0) {
				len += pad1 - paddings.set(index, pad1);
			}
			else {
				pad1 = padI;
			}
			// Increase all newline positions >= index by other.size()
			int iNl1 = newlines.size();
			for (; iNl1 > 0; iNl1--) {
				int nlPos = 0;
				if ((nlPos = newlines.get(iNl1-1)) > index) {
					newlines.set(iNl1-1, nlPos + otherSize);
				}
				else {
					break;	// Larger positions shouldn't follow
				}
			}
			/*
			 * As an invariant for this loop: The padding before the next token
			 * to be inserted (i) will already have been handled and will have
			 * length pad1, the post-token padding is to be prepared within the
			 * loop (for the next cycle) and will have to be cached in pad1 again.
			 */
			for (int i = 0; i < otherSize; i++) {
				String otherToken = other.tokens.get(i);
				tokens.add(index + i, otherToken);
				// post-token padding
				pad1 = other.paddings.get(i+1);
				paddings.add(index + i+1, pad1);
				len += pad1 + otherToken.length();
			}
			// The other half of the split padding at index; don't eliminate a former padding
			if (pad2 > 0) {
				len += pad1 + pad2 - paddings.set(index + otherSize, pad1 + pad2);
			}
			ensureGap(index-1);
			ensureGap(index-1 + other.size());
			for (int nlPos: other.newlines) {
				newlines.add(iNl1, index + nlPos);
			}
		}
		return !other.isEmpty();
	}

	/**
	 * Appends all of the elements in the specified collection to this list.
	 * The new elements will appear at the end of the list in the order that
	 * they are returned by the specified collection's iterator.<br/>
	 * In contrast to {@link #addAll(TokenList)} the contained strings will be
	 * tokenized on appending, however.
	 * 
	 * @param coll - iterable collection of strings
	 * @return {@code true} if this list changed as result of this call
	 */
	public boolean addAll(Collection<? extends String> coll) {
		boolean done = false;
		for (String str: coll) {
			done = this.add(str) || done;
		}
		return done;
	}

	/**
	 * Inserts all of the elements in the specified collection into this list,
	 * starting at the specified position. Shifts the element currently at
	 * that position (if any) and any subsequent elements to the right
	 * (increases their indices). The new elements will be tokenized and
	 * their emerging tokens will appear in this list in the order that they
	 * their originating string returned by the specified collection's
	 * iterator. Empty strings and blank strings won't be added.
	 * 
	 * @param index - insertion position
	 * @param coll - source collection of strings to be added as tokens
	 * @return {@code true} if at least one token could be added.
	 * 
	 * @see #addAll(int, TokenList)
	 */
	public boolean addAll(int index, Collection<? extends String> coll) {
		boolean done = false;
		for (String str: coll) {
			done = this.add(index++, str) || done;
		}
		return done;
	}
	
	/**
	 * Replaces all occurrences of string {@code toFind} as token by String
	 * {@code subst} and returns the number of replacements. Performs a case-
	 * sensitive search.
	 * 
	 * @param toFind - an assumed token to be replaced by {@code subst}
	 * @param subst - a string to replace in tokenized form all occurrences
	 *     of {@code toFind}
	 * @return the number of substitutions
	 */
	public int replaceAll(String toFind, String subst) {
		return replaceAll(toFind, subst, true);
	}
	
	/**
	 * Replaces all occurrences of string {@code toFind} as token by String
	 * {@code subst} and returns the number of replacements.
	 * 
	 * @param toFind - an assumed token to be replaced by {@code subst}
	 * @param subst - a string to replace in tokenized form all occurrences
	 *     of {@code toFind}
	 * @param matchCase - if {@code true} then case will make a difference
	 *    in the comparison, otherwise it won't.
	 * @return the number of substitutions
	 */
	public int replaceAll(String toFind, String subst, boolean matchCase) {
		int found = -1;
		int replaced = 0;
		while ((found = indexOf(toFind, found+1, matchCase)) >= 0) {
			// By removing a token between words, paddings could conjure up - so avoid inflation
			int[] oldPaddings = this.getPadding(found);
			this.remove(found);
			int oldSize = tokens.size();
			this.add(found, subst);
			if (!subst.startsWith(" ")) {
				this.setPadding(found, oldPaddings[0], -1);
			}
			found += tokens.size() - oldSize;	// Avoid recursive replacement
			if (!subst.endsWith(" ")) {
				this.setPadding(found-1, -1, oldPaddings[1]);
			}
			replaced++;
		}
		return replaced;
	}

	/**
	 * Replaces all occurrences of string {@code toFind} as token by String
	 * {@code subst} and returns the number of replacements.
	 * 
	 * @param toFind - an assumed token to be replaced by {@code subst}
	 * @param subst - a string to replace in tokenized form all occurrences
	 *     of {@code toFind}
	 * @param matchCase - if {@code true} then case will make a difference
	 *    in the comparison, otherwise it won't.
	 * @param fromIndex - index of first element to be affected
	 * @param toIndex - index beyond the last element to be affected
	 * @return the number of substitutions
	 */
	public int replaceAllBetween(String toFind, String subst, boolean matchCase,
			int fromIndex, int toIndex) {
		int found = toIndex;
		int replaced = 0;
		while ((found = lastIndexOf(toFind, found-1, matchCase)) >= fromIndex) {
			// By removing a token between words, paddings could conjure up - so avoid inflation
			int[] paddings = this.getPadding(found);
			this.remove(found);
			int oldSize = tokens.size();
			this.add(found, subst);
			if (!subst.startsWith(" ")) {
				this.setPadding(found, paddings[0], -1);
			}
			if (!subst.endsWith(" ")) {
				this.setPadding(found + tokens.size() - oldSize - 1, -1, paddings[1]);
			}
			replaced++;
		}
		return replaced;
	}

	/**
	 * Replaces all occurrences of sub-Tokenlist {@code toFind} by {@link
	 * TokenList} {@code subst} and returns the number of replacements.
	 * If token list {@code toFind} is blank (i.e. contains no tokens) then
	 * no replacement will be done.<br/>
	 * Note that newlines will be ignored on matching but replaced accordingly.
	 * 
	 * @param toFind - the sub-TokenList to be replaced by {@code subst}
	 * @param subst - a TokenList to substitute each occurrence of {@code
	 *    toFind}
	 * @param matchCase - if {@code true} then case will make a difference
	 *    in the comparison, otherwise it won't.
	 * @return the number of formal substitutions (i.e. there is <b>no check for
	 *    equivalence<b/> {@code subst.equals(toFind)})
	 */
	public int replaceAll(TokenList toFind, TokenList subst, boolean matchCase) {
		int size1 = toFind.size();
		int size2 = subst.size();
		int found = -1;
		int replaced = 0;
		if (size1 == 0) {
			return 0;
		}
		while ((found = indexOf(toFind, found+1, matchCase)) >= 0) {
			int oldPadding0 = paddings.get(found);
			int oldPadding1 = paddings.get(found + size1);
			this.remove(found, found + size1);
			this.addAll(found, subst);
			if (subst.paddings.get(0) == 0) {
				setPadding(found, oldPadding0, -1);
			}
			found += size2-1;	// Avoid recursive replacement
			if (subst.paddings.get(size2) == 0) {
				setPadding(found, -1, oldPadding1);
			}
			replaced++;
		}
		return replaced;
	}

	// START AS 2021-03-25: Enh. #967 (for ARMGenerator)
	/**
	 * Replaces substring {@code stringOld} by {@code stringNew} in all tokens.
	 *
	 * @param stringOld - the searched substring
	 * @param stringNew - the string to replace occurrences of {@code _stringOld}
	 * 
	 * @see #replaceAll(String, String)
	 * @see #replaceAll(String, String, boolean)
	 */
	public void replaceInTokens(String stringOld, String stringNew) {
		for (int i = 0; i < tokens.size(); i++) {
			String c = tokens.get(i).replace(stringOld, stringNew);
			tokens.set(i, c);
		}
	}
	// END AS 2021-03-25

	//	/**
//	 * Removes from this list all of its elements that are contained in the
//	 * specified collection.
//	 * 
//	 * @param coll
//	 * @return
//	 */
//	public boolean removeAll(Collection<?> coll) {
//		// TODO Auto-generated method stub
//		tokens.removeAll(coll); // FIXME handle the paddings!
//		return false;
//	}

//	/**
//	 * Retains only the elements in this list that are contained in the
//	 * specified collection. In other words, removes from this list all
//	 * of its tokens that are not contained in the specified collection.
//	 * The remaining tokens keep their relative order.
//	 * 
//	 * @param coll
//	 * @return
//	 */
//	public boolean retainAll(Collection<?> coll) {
//		// This won't get efficient
//		tokens.retainAll(coll);	// FIXME handle the surplus paddings
//		return false;
//	}

	/**
	 * Removes all the tokens and paddings from this list. The list will
	 * be empty after this call returns.
	 * 
	 * @see #shrink()
	 * @see #trim()
	 * @see #remove(int, int, boolean)
	 */
	public void clear() {
		tokens.clear();
		paddings.clear();
		newlines.clear();
		paddings.add(0);
	}

	/**
	 * Returns the index of the first occurrence of the specified {@code token}
	 * in this list, or -1 if this list does not contain the token.<br/>
	 * More formally, returns the lowest index i such that {@code token.equals(get(i))},
	 * or -1 if there is no such index.
	 * 
	 * @param token - the string to be found as token
	 * @return the index of the first occurrence or -1
	 * 
	 * @see #lastIndexOf(String)
	 * @see #indexOf(String, int)
	 * @see #indexOf(String, boolean)
	 * @see #indexOf(String, int, boolean)
	 * @see #indexOf(TokenList, int, boolean)
	 */
	public int indexOf(String token) {
		return tokens.indexOf(token);
	}

	/**
	 * Returns the index of the first occurrence of the specified {@code token}
	 * in this list from index {@code from} on, or -1 if this list does not contain
	 * the token in the specified subsequence.<br/>
	 * More formally, returns the lowest index {@code i >= from} such that
	 * {@code token.equals(get(i))} or {@code token.equalsIgnoreCase(get(i))},
	 * or -1 if there is no such index.
	 * 
	 * @param token - the string to be found as token
	 * @param from - the start token index for the search
	 * @return the index of the first occurrence or -1
	 * 
	 * @see #lastIndexOf(String, int)
	 * @see #indexOf(String)
	 * @see #indexOf(String, boolean)
	 * @see #indexOf(String, int, boolean)
	 */
	public int indexOf(String token, int from) {
		return indexOf(token, from, true);
	}

	/**
	 * Returns the index of the first token in this list that matches the specified
	 * {@code token} either in a case-aware ore case-ignorant way, depending on
	 * {@code matchCase}, or -1 if this list does not contain such a token.<br/>
	 * More formally, returns the lowest index i such that {@code token.equals(get(i))}
	 * or {@code token.equalsIgnoreCase(get(i))}, or -1 if there is no such index.
	 * 
	 * @param token - the string to be found as token
	 * @param matchCase - whether the comparison has to distinguish case
	 * @return the index of the first occurrence or -1
	 * 
	 * @see #lastIndexOf(String, boolean)
	 * @see #indexOf(String)
	 * @see #indexOf(String, int)
	 * @see #indexOf(String, int, boolean)
	 * @see #indexOf(TokenList, int, boolean)
	 */
	public int indexOf(String token, boolean matchCase) {
		if (matchCase) {
			return tokens.indexOf(token);
		}
		return indexOf(token, 0, false);
	}

	/**
	 * Returns the index of the first occurrence of the specified {@code token}
	 * in this list from index {@code from} on, or -1 if this list does not contain
	 * the token in the specified subsequence.<br/>
	 * More formally, returns the lowest index {@code i >= from} such that
	 * {@code token.equals(get(i))} or {@code token.equalsIgnoreCase(get(i))},
	 * or -1 if there is no such index.
	 * 
	 * @param token - the string to be found as token
	 * @param from - the start token index for the search
	 * @param matchCase - whether the comparison has to distinguish case
	 * @return the index of the first occurrence {@code >= from}, or -1
	 * @throws IndexOutOfBoundsException if the specified index is negative
	 */
	public int indexOf(String token, int from, boolean matchCase) {
		if (token == null) {
			for (int i = from; i < tokens.size(); i++) {
				if (tokens.get(i) == null) {
					return i;
				}
			}
		}
		else if (matchCase) {
			for (int i = from; i < tokens.size(); i++) {
				if (tokens.get(i).equals(token)) {
					return i;
				}
			}
		}
		else {
			for (int i = from; i < tokens.size(); i++) {
				if (tokens.get(i).equalsIgnoreCase(token)) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * Returns the index of the last occurrence of the specified {@code token}
	 * in this list, or -1 if this list does not contain the token.<br/>
	 * More formally, returns the highest index i such that {@code token.equals(get(i))},
	 * or -1 if there is no such index.
	 * 
	 * @param token - the string to be found as token
	 * @return the index of the last occurrence or -1
	 * 
	 * @see #indexOf(String)
	 * @see #lastIndexOf(String, int)
	 * @see #lastIndexOf(String, boolean)
	 * @see #lastIndexOf(String, int, boolean)
	 * @see #lastIndexOf(TokenList, int, boolean)
	 */
	public int lastIndexOf(String token) {
		return tokens.lastIndexOf(token);
	}

	/**
	 * Returns the index of the last occurrence of the specified {@code token}
	 * in this list at a position {@code <= backFrom}, or -1 if this list does
	 * not contain the token in the specified subsequence.<br/>
	 * More formally, returns the highest index i {@code <= backFrom} such that
	 * {@code token.equals(get(i))}, or -1 if there is no such index.
	 * 
	 * @param token - the string to be found as token
	 * @param backFrom - the index from which to search backwards
	 * @return the index of the last occurrence or -1
	 * 
	 * @see #indexOf(String, int)
	 * @see #lastIndexOf(String)
	 * @see #lastIndexOf(String, boolean)
	 * @see #lastIndexOf(String, int, boolean)
	 * @see #lastIndexOf(TokenList, int, boolean)
	 */
	public int lastIndexOf(String token, int backFrom) {
		return lastIndexOf(token, backFrom, true);
	}

	/**
	 * Returns the index of the last occurrence of the specified {@code token}
	 * in this list, or -1 if this list does not contain the token.<br/>
	 * More formally, returns the highest index i such that {@code token.equals(get(i))}
	 * or {@code token.equalsIgnoreCase(get(i)), or -1 if there is no such index.
	 * 
	 * @param token - the string to be found as token
	 * @param matchCase - whether the comparison has to distinguish case
	 * @return the index of the last occurrence or -1
	 * 
	 * @see #indexOf(String, boolean)
	 * @see #lastIndexOf(String)
	 * @see #lastIndexOf(String, int)
	 * @see #lastIndexOf(String, int, boolean)
	 * @see #lastIndexOf(TokenList, int, boolean)
	 */
	public int lastIndexOf(String token, boolean matchCase) {
		if (matchCase) {
			return tokens.lastIndexOf(token);
		}
		else {
			return lastIndexOf(token, 0, false);
		}
	}

	/**
	 * Returns the index of the last occurrence of a token in this list that
	 * matches the specified {@code token} in an either case-aware or case-ignorant
	 * way at a position {@code <= backFrom}, or -1 if this list does not contain
	 * such a token in the specified subsequence.<br/>
	 * More formally, returns the lowest index {@code i <= backFrom} such that
	 * {@code token.equals(get(i))} or {@code token.equalsIgnoreCase(get(i)),
	 * or -1 if there is no such index.
	 * 
	 * @param token - the string to be found as token
	 * @param backFrom - the highest start token index for the search
	 * @param matchCase - whether the comparison has to distinguish case
	 * @return the index of the first occurrence {@code <= backFrom}, or -1
	 */
	public int lastIndexOf(String token, int backFrom, boolean matchCase) {
		if (token == null) {
			backFrom = Math.min(backFrom, tokens.size()-1);
			for (int i = backFrom; i >= 0; i--) {
				if (tokens.get(i) == null) {
					return i;
				}
			}
		}
		else if (matchCase) {
			for (int i = backFrom; i >= 0; i--) {
				if (tokens.get(i).equals(token)) {
					return i;
				}
			}
		}
		else {
			for (int i = backFrom; i >= 0; i--) {
				if (tokens.get(i).equalsIgnoreCase(token)) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * Returns the token index of the start of a first occurrence of the
	 * given TokenList {@code subList} within this list, or -1 if this list
	 * does not contain the TokenList as subsequence.<br/>
	 * More formally, returns the lowest index i such that
	 * {@code subSequence(i, subLis.size()).equals(subList)} or
	 * {@code subSequence(i, subLis.size()).equalsIgnoreCase(subList)),
	 * or -1 if there is no such index.
	 * 
	 * @param subList - another TokenList to find as subsequence
	 * @param matchCase - whether case matters in the comparison
	 * @return the start index for the first occurrence of {@code subList}
	 *    as subsequence of this list
	 * 
	 * @see #indexOf(String)
	 * @see #indexOf(String, boolean)
	 * @see #indexOf(TokenList, int, boolean)
	 * @see #lastIndexOf(TokenList, boolean)
	 */
	public int indexOf(TokenList subList, boolean matchCase) {
		return indexOf(subList, 0, matchCase);
	}

	/**
	 * Returns the token index of the start of a first occurrence of the
	 * given TokenList {@code subList} within this list from index {@code
	 * from} , or -1 if this list does not contain the TokenList {@code
	 * subList} as subsequence at or after token index {@code from}.
	 * More formally, returns the lowest index {@code i >= from} such that
	 * {@code subSequence(i, subLis.size()).equals(subList)} or
	 * {@code subSequence(i, subLis.size()).equalsIgnoreCase(subList)),
	 * or -1 if there is no such index.
	 * 
	 * @param subList - another TokenList to find as subsequence
	 * @param from - start index for the search
	 * @param matchCase - whether case matters in the comparison
	 * @return the start index for the first occurrence of {@code subList}
	 *    as subsequence of this list after index {@code from} , or -1
	 * 
	 * @see #indexOf(String)
	 * @see #indexOf(String, boolean)
	 * @see #indexOf(String, int, boolean)
	 * @see #lastIndexOf(TokenList, int, boolean)
	 */
	public int indexOf(TokenList subList, int from, boolean matchCase) {
		// TODO There is a well-known faster algorithm (for substring search)...
		if (subList.isBlank()) {
			return from >= tokens.size() ? -1 : from;
		}
		String token0 = subList.get(0);
		for (int i = from; i <= tokens.size() - subList.size(); i++) {
			String token1 = tokens.get(i);
			if (!matchCase && token1.equalsIgnoreCase(token0)
					|| token1.equals(token0)) {
				boolean found = true;
				for (int j = 1; j < subList.size(); j++) {
					String token2 = subList.get(j);
					token1 = tokens.get(i+j);
					if ((matchCase || !token1.equalsIgnoreCase(token2))
							&& !token1.equalsIgnoreCase(token2)) {
						found = false;
						break;
					}
				}
				if (found) {
					return i;
				}
			}
		}
		return -1;
	}
	
	/**
	 * Returns the token index of the start of a last occurrence of the
	 * given TokenList {@code subList} within this list, or -1 if this list
	 * does not contain the TokenList as subsequence.<br/>
	 * More formally, returns the highest index i such that
	 * {@code subSequence(i, subLis.size()).equals(subList)} or
	 * {@code subSequence(i, subLis.size()).equalsIgnoreCase(subList)),
	 * or -1 if there is no such index.
	 * 
	 * @param subList - another TokenList to find as subsequence
	 * @param caseSensitive - whether case matters in the comparison
	 * @return the start index for the last occurrence of {@code subList}
	 *    as subsequence of this list
	 * 
	 * @see #lastIndexOf(String)
	 * @see #lastIndexOf(String, boolean)
	 * @see #indexOf(TokenList, boolean)
	 * @see #lastIndexOf(TokenList, int, boolean)
	 * @see #contains(TokenList, boolean)
	 */
	public int lastIndexOf(TokenList subList, boolean caseSensitive) {
		return lastIndexOf(subList, tokens.size()-1, caseSensitive);
	}

	/**
	 * Returns the token index of the start of a last occurrence of the
	 * given TokenList {@code subList} within this list at or before index
	 * {@code backFrom}, or -1 if this list does not contain the TokenList
	 * as subsequence in the index range {@code 0..backFrom}.<br/>
	 * More formally, returns the highest index {@code i <= backFrom} such that
	 * {@code subSequence(i, subLis.size()).equals(subList)} or
	 * {@code subSequence(i, subLis.size()).equalsIgnoreCase(subList)),
	 * or -1 if there is no such index.
	 * 
	 * @param subList - another TokenList to find as subsequence
	 * @param backFrom - the highest start index for the backward search
	 * @param caseSensitive - whether case matters in the comparison
	 * @return the start index for the last occurrence of {@code subList}
	 *    as subsequence of this list
	 * 
	 * @see #lastIndexOf(String)
	 * @see #lastIndexOf(String, boolean)
	 * @see #lastIndexOf(String, int, boolean)
	 * @see #indexOf(TokenList, int, boolean)
	 * @see #contains(TokenList, boolean)
	 */
	public int lastIndexOf(TokenList subList, int backFrom, boolean caseSensitive) {
		// TODO There is a well-known faster algorithm (for substring search)...
		backFrom = Math.min(backFrom, tokens.size()-subList.size());
		if (subList.isBlank()) {
			return backFrom;
		}
		String token0 = subList.get(0);
		for (int i = backFrom; i >= 0; i--) {
			String token1 = tokens.get(i);
			if (!caseSensitive && token1.equalsIgnoreCase(token0)
					|| token1.equals(token0)) {
				boolean found = true;
				for (int j = 1; j < subList.size(); j++) {
					String token2 = subList.get(j);
					token1 = tokens.get(i+j);
					if ((caseSensitive || !token1.equalsIgnoreCase(token2))
							&& !token1.equals(token2)) {
						found = false;
						break;
					}
				}
				if (found) {
					return i;
				}
			}
		}
		return -1;
	}
	
	/**
	 * Compares the contained tokens between this and {@code other}
	 * TokenList for sequential equality. The paddings don't play a
	 * role in the comparison.<br/>
	 * To compare for exact string equality use {@code
	 * getString().equals(other.getString())}
	 * 
	 * @param other - the other TokenList to be compared
	 * @return {@code true} if the token sequences coincide
	 * 
	 * @see #getString()
	 * @see #equalsIgnoreCase(TokenList)
	 * @see #contains(TokenList, boolean)
	 * @see #indexOf(TokenList, int, boolean)
	 * @see #lastIndexOf(TokenList, int, boolean)
	 */
	public boolean equals(TokenList other)
	{
		// For performance reasons, a quick test
		if (tokens.size() != other.size()) {
			return false;
		}
		for (int i = 0; i < tokens.size(); i++) {
			if (tokens.get(i).equals(other.get(i))) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Compares the contained tokens between this and {@code other}
	 * TokenList for sequential case-ignorant equality. The paddings
	 * don't play a role in the comparison.<br/>
	 * To compare for case-insensitive string equality in including
	 * whitespace use {@code getString().equalsIgoreCase(other.getString())}
	 * 
	 * @param other - the other TokenList to be compared
	 * @return {@code true} if the token sequences coincide
	 * 
	 * @see #getString()
	 * @see #equals(TokenList)
	 * @see #contains(TokenList, boolean)
	 * @see #indexOf(TokenList, int, boolean)
	 * @see #lastIndexOf(TokenList, int, boolean)
	 */
	public boolean equalsIgnoreCase(TokenList other)
	{
		// For performance reasons, a quick test
		if (tokens.size() != other.size()) {
			return false;
		}
		for (int i = 0; i < tokens.size(); i++) {
			if (tokens.get(i).equalsIgnoreCase(other.get(i))) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Compares this token list with the specified {@code other} one for
	 * lexicographic order in case-sensitive way. Returns a negative
	 * integer, zero, or a positive integer as this object is
	 * lexicographically less than, equal to, or greater than the
	 * {@code other} token list.
	 * 
	 * @param other - a TokenList object
	 * @return an int < 0 if this String is less than the specified String,
	 *     0 if they are equal, and > 0 if this String is greater
	 * 
	 * @see #equals(TokenList)
	 * @see #equalsIgnoreCase(TokenList)
	 * @see #compareToIgnoreCase(TokenList)
	 */
	@Override
	public int compareTo(TokenList other)
	{
		int cmp = 0;
		for (int i = 0; i < Math.min(tokens.size(), other.size()); i++) {
			if ((cmp = tokens.get(i).compareTo(other.get(i))) != 0) {
				return cmp;
			}
		}
		return Integer.compare(tokens.size(), other.size());
	}
	
	/**
	 * Compares this token list with the specified {@code other} one for
	 * lexicographic order in case-insensitive way. Returns a negative
	 * integer, zero, or a positive integer as this object is
	 * lexicographically less than, equal to, or greater than the
	 * {@code other} token list.
	 * 
	 * @param other - a TokenList object
	 * @return an int < 0 if this String is less than the specified String,
	 *     0 if they are equal, and > 0 if this String is greater
	 * 
	 * @see #equalsIgnoreCase(TokenList)
	 */
	public int compareToIgnoreCase(TokenList other)
	{
		int cmp = 0;
		for (int i = 0; i < Math.min(tokens.size(), other.size()); i++) {
			if ((cmp = tokens.get(i).compareToIgnoreCase(other.get(i))) != 0) {
				return cmp;
			}
		}
		return Integer.compare(tokens.size(), other.size());
	}
	
	/**
	 * Returns {@code true} if the specified string is a prefix of the string
	 * represented by this token list.
	 * 
	 * @param prefix - the string to check for
	 * @return {@code true} if the specified string is a prefix of this token
	 *    list, {@code false} otherwise
	 * @throws NullPointerException when {@code prefix} is {@code null}
	 * 
	 * @see #startsWithIgnoreCase(String)
	 * @see #startsWith(TokenList, boolean)
	 */
	public boolean startsWith(String prefix)
	{
		return getString().startsWith(prefix);
	}

	/**
	 * Returns {@code true} if the specified string is a suffix of the string
	 * represented by this token list.
	 * 
	 * @param suffix - the string to check for
	 * @return {@code true} if the specified string is a suffix of this token
	 *    list, {@code false} otherwise
	 * @throws NullPointerException when {@code suffix} is {@code null}
	 * 
	 * @see #endsWithIgnoreCase(String)
	 * @see #endsWith(TokenList, boolean)
	 */
	public boolean endsWith(String suffix)
	{
		return getString().endsWith(suffix);
	}

	/**
	 * Compares the specified string with the string represented by this token
	 * list in a case-ignorant way to determine whether or not the former is a
	 * (case-insensitive) prefix of the latter.
	 * 
	 * @param prefix - the string to check for
	 * @return {@code true} if the specified string is a prefix of this token
	 *    list in a case-ignorant way, {@code false} otherwise
	 * @throws NullPointerException when {@code prefix} is {@code null}
	 * 
	 * @see #startsWith(String)
	 * @see #startsWith(TokenList, boolean)
	 */
	public boolean startsWithIgnoreCase(String prefix)
	{
		return getString().toLowerCase().startsWith(prefix.toLowerCase());
	}

	/**
	 * Compares the specified string with the string represented by this token
	 * list in a case-ignorant way to determine whether or not the former is a
	 * (case-insensitive) suffix of the latter.
	 * 
	 * @param suffix - the string to check for
	 * @return {@code true} if the specified string is a suffix of this token
	 *    list in a case-ignorant way, {@code false} otherwise
	 * @throws NullPointerException when {@code suffix} is {@code null}
	 * 
	 * @see #endsWith(String)
	 * @see #endsWith(TokenList, boolean)
	 */
	public boolean endsWithIgnoreCase(String suffix)
	{
		return getString().toLowerCase().endsWith(suffix.toLowerCase());
	}

	/**
	 * Compares (possibly case-ignorantly) the specified token list with this
	 * TokenList to determine whether or not the former is a (case-insensitive)
	 * prefix of the latter. The widths of padding or separating whitespace is
	 * completely ignored.
	 * 
	 * @param prefix - the TokenList to check if it's a prefix
	 * @param caseSensitive - if {@code true} then the comparison is done by
	 *     respecting case, if {@code false} then case will be ignored.
	 * @return {@code true} if the token elements of {@code tokens} correspond
	 *     to the first {@code tokens.size()} tokens of this TokenList,
	 *     {@code false} otherwise
	 */
	public boolean startsWith(TokenList prefix, boolean caseSensitive)
	{
		int otherSize = prefix.size();
		if (this.tokens.size() < otherSize) {
			// tokens cannot be a prefix
			return false;
		}
		for (int i = 0; i < otherSize; i++) {
			if ((!caseSensitive || !this.tokens.get(i).equalsIgnoreCase(prefix.get(i)))
					&& !this.tokens.get(i).equals(prefix.get(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Compares (possibly case-ignorantly) the specified token list with this
	 * TokenList to determine whether or not the former is a (case-insensitive)
	 * prefix of the latter. The widths of padding or separating whitespace is
	 * completely ignored.
	 * 
	 * @param suffix - the TokenList to check if it's a prefix
	 * @param caseSensitive - if {@code true} then the comparison is done by
	 *     respecting case, if {@code false} then case will be ignored.
	 * @return {@code true} if the token elements of {@code tokens} correspond
	 *     to the first {@code tokens.size()} tokens of this TokenList,
	 *     {@code false} otherwise
	 */
	public boolean endsWith(TokenList suffix, boolean caseSensitive)
	{
		int otherSize = suffix.size();
		if (this.tokens.size() < otherSize) {
			// tokens cannot be a suffix
			return false;
		}
		for (int i = 0, j = this.tokens.size() - otherSize; i < otherSize; i++, j++) {
			if ((!caseSensitive || !this.tokens.get(j).equalsIgnoreCase(suffix.get(i)))
					&& this.tokens.get(j).equals(suffix.get(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks whether this token list ends with a backslash token, thus
	 * symbolising a broken line (continued in a subsequent token list).
	 * 
	 * @param mindWhiteSpaceTail - if {@code true} then trailing whitespace
	 *     would avert the detection of a backslash as last token, otherwise
	 *     trailing whitespace will be ignored.
	 * @return {@code true} if this token list ends with a token consisting
	 *     of an (isolated) backslash character.
	 */
	public boolean endsWithBackslash(boolean mindWhiteSpaceTail)
	{
		return !tokens.isEmpty()
				&& (!mindWhiteSpaceTail || paddings.get(tokens.size()) == 0)
				&& tokens.get(tokens.size()-1).equals("\\");
	}
	
	/**
	 * Returns a list of TokenLists representing the parts of the line
	 * emerging from this token list after having broken (wrapped) such that
	 * the lengths of the partial lines do not excess a text length of
	 * {@code maxChars} if possible. (If a single token happens to be longer
	 * than {@code maxChars} then it will not of course be split.)<br/>
	 * The first n-1 TokenLists of the result will end with a "\" token.<br/>
	 * This token list itself will not be affected. If {@code maxChars > length()} 
	 * then the result will contain a single TokenList which is an exact copy
	 * of this.
	 * 
	 * @param maxChars - intended maximum number of charactes per line.
	 * @return the list of the split parts of this token lists.
	 */
	public ArrayList<TokenList> breakAtLength(int maxChars)
	{
		var broken = new ArrayList<TokenList>();
		if (len <= maxChars) {
			// Copy this as is
			broken.add(new TokenList(this));
		}
		else if (tokens.isEmpty()) {
			// Can only be whitespace, so just shorten the whitespace sequence.
			broken.add(new TokenList(" ".repeat(maxChars)));
		}
		else {
			var tList = new TokenList();
			for (int i = 0; i < tokens.size(); i++) {
				int pd = paddings.get(i);
				int plus = pd + tokens.get(i).length();
				boolean nl = pd > 0 && newlines.contains(i);
				if (i == 0 || tList.len + plus <= maxChars) {
					tList.paddings.set(tList.tokens.size(), pd);
					if (nl) {
						tList.newlines.add(tList.size());
					}
					tList.tokens.add(tokens.get(i));
					tList.paddings.add(0);
					tList.len += plus;
				}
				else {
					tList.tokens.add("\\");
					tList.paddings.add(0);
					tList.len++;
					broken.add(tList);
					tList = new TokenList();
					tList.paddings.set(0, pd);
					if (nl) {
						tList.newlines.add(0);
					}
					tList.tokens.add(tokens.get(i));
					tList.paddings.add(0);
					tList.len += plus;
				}
			}
			if (!tList.isEmpty()) {
				broken.add(tList);
			}
		}
		return broken;
	}

//	@Override
//	public Iterator<String> iterator() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public ListIterator<String> listIterator() {
//		// TODO Auto-generated method stub
//		return null;
//	}

//	@Override
//	public ListIterator<String> listIterator(int index) {
//		// TODO Auto-generated method stub
//		return null;
//	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName());
		sb.append('[');
		int pd = 0;
		for (int i = 0; i < tokens.size(); i++) {
			if (i >= paddings.size()) {
				sb.append('#');
			}
			else if ((pd = paddings.get(i)) > 0) {
				if (newlines.contains(Integer.valueOf(i))) {
					sb.append("‖");
				}
				sb.append(pd);
			}
			sb.append('┤');
			sb.append(tokens.get(i));
			sb.append('├');
		}
		if (tokens.size() >= paddings.size()) {
			sb.append('#');
		}
		else if ((pd = paddings.get(tokens.size())) > 0) {
			if (newlines.contains(Integer.valueOf(tokens.size()))) {
				sb.append("‖");
			}
			sb.append(pd);
		}
		sb.append(']');
		return sb.toString();
	}

}
