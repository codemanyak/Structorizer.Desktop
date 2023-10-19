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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import lu.fisch.utils.StringList;

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
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

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
 * method.  This is best done at creation time, to prevent accidental
 * unsynchronized access to the list:<pre>
 *   List list = Collections.synchronizedList(new TokenList(...));</pre>
 *   
 * @author Kay Gürtzig
 *
 */
public class TokenList implements Comparable<TokenList>{

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
	 * List of lexicographic tokens
	 */
	private ArrayList<String> tokens;
	
	/**
	 * List of paddings (blank sequences) between the {@link tokens}. The length always equals
	 * {@code tokens.size() + 1}, i.e. {@code paddings.get(0)} is the number of blanks before
	 * the first token, {@code paddings.get(i)} with i > 0 is the length of the gap between
	 * {@code tokens.get(i-1)} and {@code tokens.get(i)}.
	 */
	private ArrayList<Integer> paddings;
	
	/**
	 * The total text length of the string represented by this token list
	 */
	private int len = 0;
	
	/**
	 * Creates an empty token list
	 * 
	 * @see #TokenList(String)
	 */
	public TokenList() {
		tokens = new ArrayList<String>();
		paddings = new ArrayList<Integer>();
		paddings.add(0);
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
	 * Preserves string and character literals.<br/>
	 * By default, {@code preserveStrings 0 true} should be set {@code true},
	 * which ensures that string and character literals will be preserved (for
	 * keyword preferences, however, which possibly contain quotes like
	 * {@code "jusqu'à"} it may be necessary to set it {@code false}<br/>
	 * The inter-lexeme whitespace will preserved internally such that a
	 * nearly complete reconstruction of the original string is possible
	 * by {@link #getString()}. It can also be retrieved or manipulated
	 * by e.g. {@link #getPadding(int)}, {@link #setPadding(int, int, int)} 
	 * 
	 * @param _text - the text line to be split into tokens
	 */
	public TokenList(String _text, boolean preserveStrings) {
		tokens = new ArrayList<String>();
		paddings = new ArrayList<Integer>();
		
		int nBlanks = 0;
		LexState state = LexState.LX_0;
		boolean escape = false;
		StringBuilder sbToken = new StringBuilder();
		for (int ix = 0; ix < _text.length(); ix++) {
			int cp = _text.codePointAt(ix);
			switch (state) {
			case LX_0:	// Initial and interregnum state
				if (Character.isWhitespace(cp)) {
					nBlanks++;
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
					state = preserveStrings ? LexState.LX_STRING1 : LexState.LX_SYMBOL;
				}
				else if (cp == '\"') {
					sbToken.appendCodePoint(cp);
					state = preserveStrings ? LexState.LX_STRING2 : LexState.LX_SYMBOL;
				}
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
				if (SYMBOL_CONTINUATORS.indexOf(cp) >= 0) {
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
		len = other.len;
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
		for (int i = 0; i < tokens.size(); i++) {
			sb.append(" ".repeat(paddings.get(i)));
			sb.append(tokens.get(i));
		}
		sb.append(" ".repeat(paddings.get(tokens.size())));
		return sb.toString();
	}
	
	/**
	 * @return the number of tokens this token list consists of
	 * 
	 * @see #length()
	 */
	public int size()
	{
		return tokens.size();
	}

	/**
	 * @return the length (total number of characters) of the represented
	 * text string including whitespace.
	 * 
	 * @see #size()
	 */
	public int length()
	{
		return len;
	}
	
	public String get(int index) {
		return tokens.get(index);
	}

	public String set(int index, String token) {
		if (token == null || token.isEmpty()) {
			return remove(index);
		}
		len += token.length() - tokens.get(index).length();
		// Check whether gaps will be necessary
		
		return tokens.set(index, token);
	}
	
	/**
	 * Sets the paddings (i.e. the number of blanks) around the token at given
	 * {@code index}.<br/>
	 * <b>Note:</b> This method will automatically ensure that at least a single
	 * blank separates two tokens that otherwise would amalgamate into one token
	 * on concatenating.
	 * 
	 * @param index - the position of the token the paddings of which are to be set
	 * @param left - the intended padding to the preceding token (or the start)
	 * @param right - the intended padding to the successor token (or the end)
	 * @returns the number of blanks the total padding sum grows (or is reduced)
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0
     *      || index >= size())
	 */
	public int setPadding(int index, int left, int right)
	{
		int growth = left - paddings.set(index, left);
		growth += right - paddings.set(index + 1, right);
		len += growth;
		return growth;
	}

	/**
	 * Returns the total number of blanks the paddings at the beginning, the end,
	 * and between the tokens amount to.
	 * 
	 * @return total padding
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
	 */
	public int[] getPadding(int index)
	{
		return new int[] {paddings.get(index), paddings.get(index+1)};
	}
	
	/**
	 * Removes white space characters from the beginning and end of the token list.
	 *
	 * @return the number of removed blanks
	 */
	public int trim()
	{
		return paddings.set(0, 0) + paddings.set(tokens.size(), 0);
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
	 *    (fromIndex < 0 || toIndex > size ||fromIndex > toIndex)
	 * @throws IllegalArgumentException if the endpoint indices are out of
	 *    order (fromIndex > toIndex).
	 */
	public TokenList subSequence(int fromIndex, int toIndex)
	{
		TokenList part = new TokenList();
		part.tokens.addAll(tokens.subList(fromIndex, toIndex));
		part.paddings.addAll(1, paddings.subList(fromIndex+1, toIndex));
		part.len = paddings.get(tokens.size());
		for (int i = 0; i < tokens.size(); i++) {
			part.len += part.tokens.get(i).length() + part.paddings.get(i);
		}
		return part;
	}

	public boolean isBlank() {
		return tokens.isEmpty(); // or size() == null
	}

	public boolean isEmpty() {
		return len == 0;
	}

	public boolean contains(String token) {
		return tokens.contains(token);
	}
	
	public boolean contains(String token, boolean caseSensitive) {
		return indexOf(token, caseSensitive) >= 0;
	}
	
	public boolean contains(TokenList subList, boolean caseSensitive) {
		return indexOf(subList, caseSensitive) >= 0;
	}

//	@Override
//	public Iterator<String> iterator() {
//		// TODO Auto-generated method stub
//		return null;
//	}

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
	 *    if {@code token} is {@code null} or empty.
	 * @return {@code true} if the token was appended, {@code} otherwise
	 */
	public boolean add(String string) {
		if (string == null || string.isEmpty()) {
			return false;
		}
		int nTokens = tokens.size();
		if (string.isBlank()) {
			// Non-empty sequence of whitespace - enlarge the respective padding
			len += string.length();
			paddings.set(nTokens, paddings.get(nTokens) + string.length());
			
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
	 *    will happen if {@code str} is {@code null} or empty.
	 * @return {@code true} if the token was appended, {@code} otherwise
	 */
	public boolean add(int index, String string)
	{
		if (string == null || string.isEmpty()) {
			return false;
		}
		if (string.isBlank()) {
			// Non-empty sequence of whitespace - enlarge the respective padding
			len += string.length();
			paddings.set(index, paddings.get(index) + string.length());
			
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
	 */
	public String remove(int index) {
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
		return tokens.remove(index);
	}
	
	
	/**
	 * Removes the token at the specified position from this list. Shifts
	 * any subsequent elements to the left (subtracts one from their indices).
	 * 
	 * @param index - the index of the token to be removed
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

//	@Override
//	public boolean containsAll(Collection<?> c) {
//		// TODO Auto-generated method stub
//		return false;
//	}

	/**
	 * Appends all of the elements of the specified token list other to
	 * this list,* starting at the specified position. Shifts the element
	 * currently at that position (if any) and any subsequent elements to
	 * the right (increases their indices). The new elements will appear
	 * in the list in same the order as in other.
	 * 
	 * @param coll - 
	 * @return {@code true} if at least one token could be added.
	 */
	public boolean addAll(TokenList other) {
		int pad0 = other.paddings.get(0);
		paddings.set(tokens.size(), paddings.get(tokens.size()) + pad0);
		len += pad0;
		for (int i = 0; i < other.size(); i++) {
			tokens.add(other.tokens.get(i));
			pad0 = other.paddings.get(i+1);
			paddings.add(pad0);
			len += other.tokens.get(i).length();
		}
		return true;
	}

	/**
	 * Inserts all of the elements in the specified collection into this list,
	 * starting at the specified position. Shifts the element currently at
	 * that position (if any) and any subsequent elements to the right
	 * (increases their indices). The new elements will appear in the list
	 * in the order that they are returned by the specified collection's
	 * iterator except that empty strings an blank strings won't be added.
	 * 
	 * @param index - insertion position
	 * @param coll - source collection of strings to be added as tokens
	 * @return {@code true} if at least one token could be added.
	 */
	public boolean addAll(int index, TokenList other) {
		boolean done = false;
		// TODO
		return done;
	}

	
	/**
	 * Appends all of the elements in the specified collection to this list.
	 * The new elements will appear at the end of the list in the order that
	 * they are returned by the specified collection's iterator.
	 * 
	 * @param coll - 
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
	 * (increases their indices). The new elements will appear in the list
	 * in the order that they are returned by the specified collection's
	 * iterator except that empty strings an blank strings won't be added.
	 * 
	 * @param index - insertion position
	 * @param coll - source collection of strings to be added as tokens
	 * @return {@code true} if at least one token could be added.
	 */
	public boolean addAll(int index, Collection<? extends String> coll) {
		boolean done = false;
		for (String str: coll) {
			done = this.add(index++, str) || done;
		}
		return done;
	}

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
	 */
	public void clear() {
		tokens.clear();
		paddings.clear();
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
	 */
	public int indexOf(String token) {
		return tokens.indexOf(token);
	}

	/**
	 * Returns the index of the last occurrence of the specified {@code token}
	 * in this list, or -1 if this list does not contain the token.<br/>
	 * More formally, returns the highest index i such that {@code token.equals(get(i))},
	 * or -1 if there is no such index.
	 * 
	 * @param token - the string to be found as token
	 * @return the index of the last occurrence or -1
	 */
	public int lastIndexOf(String token) {
		return tokens.lastIndexOf(token);
	}

	/**
	 * Returns the index of the first occurrence of the specified {@code token}
	 * in this list, or -1 if this list does not contain the token.<br/>
	 * More formally, returns the lowest index i such that {@code token.equals(get(i))}
	 * or {@code token.equalsIgnoreCase(get(i)), or -1 if there is no such index.
	 * 
	 * @param token - the string to be found as token
	 * @param caseSensitive - whether the comparison has to distinguish case
	 * @return the index of the first occurrence or -1
	 */
	public int indexOf(String token, boolean caseSensitive) {
		if (caseSensitive) {
			return tokens.indexOf(token);
		}
		for (int i = 0; i < tokens.size(); i++) {
			if (tokens.get(i).equalsIgnoreCase(token)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Returns the index of the last occurrence of the specified {@code token}
	 * in this list, or -1 if this list does not contain the token.<br/>
	 * More formally, returns the highest index i such that {@code token.equals(get(i))}
	 * or {@code token.equalsIgnoreCase(get(i)), or -1 if there is no such index.
	 * 
	 * @param token - the string to be found as token
	 * @param caseSensitive - whether the comparison has to distinguish case
	 * @return the index of the last occurrence or -1
	 * 
	 * @see #lastIndexOf(String)
	 */
	public int lastIndexOf(String token, boolean caseSensitive) {
		if (caseSensitive) {
			return tokens.lastIndexOf(token);
		}
		for (int i = tokens.size()-1; i >= 0; i--) {
			if (tokens.get(i).equalsIgnoreCase(token)) {
				return i;
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
	 * @param caseSensitive - whether case matters in the comparison
	 * @return the start index for the first occurrence of {@code subList}
	 *    as subsequence of this list
	 * 
	 * @see #indexOf(String)
	 * @see #indexOf(String, boolean)
	 */
	public int indexOf(TokenList subList, boolean caseSensitive) {
		// TODO There is a well-known faster algorithm (for substring search)...
		if (subList.isEmpty()) {
			return 0;
		}
		String token0 = subList.get(0);
		for (int i = 0; i < tokens.size() - subList.size(); i++) {
			String token1 = tokens.get(i);
			if (!caseSensitive && token1.equalsIgnoreCase(token0)
					|| token1.equals(token0)) {
				boolean found = true;
				for (int j = 1; j < subList.size(); j++) {
					token0 = subList.get(j);
					token1 = tokens.get(i+j);
					if (caseSensitive && !token1.equals(token0)
							|| !token1.equalsIgnoreCase(token0)) {
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
	 * @see #contains(TokenList, boolean)
	 */
	public int lastIndexOf(TokenList subList, boolean caseSensitive) {
		// TODO There is a well-known faster algorithm (for substring search)...
		if (subList.isEmpty()) {
			return 0;
		}
		String token0 = subList.get(0);
		for (int i = tokens.size() - subList.size() - 1; i >= 0; i--) {
			String token1 = tokens.get(i);
			if (!caseSensitive && token1.equalsIgnoreCase(token0)
					|| token1.equals(token0)) {
				boolean found = true;
				for (int j = 1; j < subList.size(); j++) {
					token0 = subList.get(j);
					token1 = tokens.get(i+j);
					if (caseSensitive && !token1.equals(token0)
							|| !token1.equalsIgnoreCase(token0)) {
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
	 * @see #indexOf(TokenList, boolean)
	 * @see #lastIndexOf(TokenList, boolean)
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
	 * @see #indexOf(TokenList, boolean)
	 * @see #lastIndexOf(TokenList, boolean)
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

}
