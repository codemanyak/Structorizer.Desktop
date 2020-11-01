/*
    Structorizer
    A little tool which you can use to create Nassi-Shneiderman Diagrams (NSD)

    Copyright (C) 2009  Bob Fisch

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

import lu.fisch.structorizer.gui.ElementNames;
import lu.fisch.structorizer.locales.Locale;
import lu.fisch.structorizer.locales.Locales;
import lu.fisch.utils.StringList;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Exception subclass for lu.fisch.structorizer.syntax package.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2019-11-10      First Issue
 *      Kay Gürtzig     2020-10-25      Fields messageKey and messageDetails added
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
@SuppressWarnings("serial")
public class SyntaxException extends Exception {
	
	/** Number of the token where the error was detected */
	private int tokenIndex = 0;

	/**
	 * Constructs a new SyntaxException with the specified detail message.  The
	 * cause is not initialized, and may subsequently be initialized by
	 * a call to {@link #initCause}.
	 * @param message - the detail message. The detail message is saved for
	 *          later retrieval by the {@link #getMessage()} method.
	 * @param position - the index of the inducing token (in a condensed token
	 *          list)
	 */
	public SyntaxException(String message, int position) {
		super(message);
		tokenIndex = position;
	}

	/**
	 * Constructs a new SyntaxException with the specified cause and a detail message
	 * of {@code (cause==null ? null : cause.toString())} (which typically contains
	 * the class and detail message of {@code cause}).
	 * @param position  - the index of the inducing token (in a condensed token
	 * list)
	 * @param cause - the cause (which is saved for later retrieval by the
	 * {@link #getCause()} method). (A {@code null} value is permitted, and indicates
	 * that the cause is nonexistent or unknown.)
	 */
	public SyntaxException(int position, Throwable cause) {
		super(cause);
		tokenIndex = position;
	}

	/**
	 * Constructs a new SyntaxException with the specified detail message and cause.<br/>
	 * Note that the detail message associated with {@code cause} is <i>not</i>
	 * automatically incorporated in this exception's detail message.
	 * @param message - message the detail message (which is saved for later retrieval
	 *         by the {@link #getMessage()} method).
	 * @param position  - the index of the inducing token (in a condensed token
	 *         list)
	 * @param cause - the cause (which is saved for later retrieval by the
	 *         {@link #getCause()} method). (A {@code null} value is
	 *         permitted, and indicates that the cause is nonexistent or
	 *         unknown.)
	 */
	public SyntaxException(String message, int position, Throwable cause) {
		super(message, cause);
		tokenIndex = position;
	}

	/**
	 * Constructs a new SyntaxException with a detail message constructed
	 * from {@code msgKey} and {@code msgDetails} and with given cause.<br/>
	 * Note that the detail message associated with {@code cause} is <i>not</i>
	 * automatically incorporated in this exception's detail message.
	 * @param msgKey - a symbolic name used as keyword for localization
	 * @param msgDetails - detail information to substitute placeholders in the
	 * translated message template (if there are)
	 * @param position  - the index of the inducing token (in a condensed token
	 * list)
	 * @param cause - a possible directly causing exception
	 */
	public SyntaxException(String msgKey, String[] msgDetails, int position, Throwable cause) {
		super(composeMessage(msgKey, msgDetails), cause);
		tokenIndex = position;
	}

	/**
	 * Constructs a localized message from symbolic message name {@code msgKey} and
	 * detail information {@code msgDetails} in the current locale or (if it does not
	 * provide a translation) in the default locale.
	 * @param msgKey - a symbolic name used as keyword for localization
	 * @param msgDetails - an array of informative strings to be substituted into the message.
	 * @return the localized composed message
	 */
	private static String composeMessage(String msgKey, String[] msgDetails) {
		Locale loc0 = Locales.getInstance().getDefaultLocale();
		Locale loc = Locales.getLoadedLocale(true);
		String msg = null;
		if (loc != null) {
			msg = loc.getValue("Syntax", msgKey);
			if (msg == null && loc0 != null) {
				msg = loc0.getValue("Syntax", msgKey);
			}
			if (msg != null && msgDetails != null) {
				for (int i = 0; i < msgDetails.length; i++) {
					msg = msg.replace("%" + i, msgDetails[i]);
				}
			}
		}
		if (msg == null) {
			msg = msgKey + " (" + (new StringList(msgDetails)).concatenate("; ") + ")";
		}
		return ElementNames.resolveElementNames(msg, null);
	}
	
	/**
	 * @return the token index where the exception was detected (refers to a condensed
	 * tokenized input line) where "condensed" means that keywords consisting of several
	 * lexic units have been consolidated into single tokens and all inter-lexeme spaces
	 * have been removed
	 */
	public int getPosition()
	{
		return this.tokenIndex;
	}
}
