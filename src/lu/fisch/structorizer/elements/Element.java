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

package lu.fisch.structorizer.elements;

/******************************************************************************************************
 *
 *      Author:         Bob Fisch
 *
 *      Description:    Abstract class for all Elements.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Bob Fisch       2007-12-09      First Issue
 *      Kay Gürtzig     2014-11-11      Operator highlighting modified (sse comment)
 *      Kay Gürtzig     2015-10-09      Methods selectElementByCoord(x,y) and getElementByCoord() merged
 *      Kay Gürtzig     2015-10-11      Comment drawing centralized and breakpoint mechanism prepared
 *      Kay Gürtzig     2015-10-13      Execution state separated from selected state
 *      Kay Gürtzig     2015-11-01      operator unification and intermediate syntax transformation ready
 *      Kay Gürtzig     2015-11-12      Issue #25 (= KGU#80) fixed in unifyOperators, highlighting corrected
 *      Kay Gürtzig     2015-12-01      Bugfixes #39 (= KGU#91) and #41 (= KGU#92)
 *      Kay Gürtzig     2015-12-11      Enhancement #54 (KGU#101): Method splitExpressionList added
 *      Kay Gürtzig     2015-12-21      Bugfix #41/#68/#69 (KGU#93): Method transformIntermediate revised
 *      Kay Gürtzig     2015-12-23      Bugfix #74 (KGU#115): Pascal operators accidently disabled
 *                                      Enh. #75 (KGU#116): Highlighting of jump keywords (orange)
 *      Kay Gürtzig     2016-01-02      Bugfix #78 (KGU#119): New method equals(Element)
 *      Kay Gürtzig     2016-01-03/04   Enh. #87 for collapsing/expanding (KGU#122/KGU#123)
 *      Kay Gürtzig     2016-01-12      Bugfix #105: flaw in string literal tokenization (KGU#139)
 *      Kay Gürtzig     2016-01-12      Bugfix #104: transform caused index errors
 *      Kay Gürtzig     2016-01-14      Enh. #84: Added "{" and "}" to the token separator list (KGU#100)
 *      Kay Gürtzig     2016-01-15      Enh. #61,#107: Highlighting for "as" added (KGU#109)
 *      Kay Gürtzig     2016-01-16      Changes having got lost on a Nov. 2014 merge re-inserted
 *      Kay Gürtzig     2016-01-22      Bugfix for Enh. #38 (addressing moveUp/moveDown, KGU#144).
 *      Kay Gürtzig     2016-03-02      Bugfix #97: steady selection on dragging (see comment, KGU#136),
 *                                      Element self-description improved (method toString(), KGU#152)
 *      Kay Gürtzig     2016-03-06      Enh. #77 (KGU#117): Fields for test coverage tracking added
 *      Kay Gürtzig     2016-03-10      Enh. #124 (KGU#156): Counter fields for histographic tracking added
 *      Kay Gürtzig     2016-03-12      Enh. #124 (KGU#156): Runtime data collection accomplished
 *      Kay Gürtzig     2016-03-26      KGU#165: New option CodeParser.ignoreCase introduced
 *      Kay Gürtzig     2016-04-24      Issue #169: Method findSelected() introduced, copy() modified (KGU#183)
 *      Kay Gürtzig     2016-07-07      Enh. #188: Modification of getText(boolean) to cope with transmutation,
 *                                      Enh. #185: new abstract method convertToCalls() for code import
 *      Kay Gürtzig     2016-07-25      Bugfix #205: Alternative comment bar colour if fill colour equals (KGU#215)
 *      Kay Gürtzig     2016-07-28      Bugfix #210: Execution counting mechanism fundamentally revised
 *      Kay Gürtzig     2016-07-29      Issue #211: Modification in writeOutVariables() for E_TOGGLETC mode.
 *                                      Enh. #128: New mode E_COMMENTSPLUSTEXT
 *      Kay Gürtzig     2016-08-02      Enh. #215: Infrastructure for conditional breakpoints added.
 *      Kay Gürtzig     2016-09-21      Issue #248: API of setBreakTriggerCount() modified to prevent negative values
 *      Kay Gürtzig     2016-09-25      Enh. #253: CodeParser.keywordMap refactored
 *      Kay Gürtzig     2016-09-28      KGU#264: Font name property renamed from "Name" to "Font".
 *      Kay Gürtzig     2016-10-13      Issue #270: New field "disabled" for execution and code export
 *      Kay Gürtzig     2016-11-06      Issue #279: Several modifications to circumvent direct access to CodeParser.keywordMap
 *      Kay Gürtzig     2017-01-06      Issue #327: French default structure preferences replaced by English ones
 *      Kay Gürtzig     2017-01-13      Issue #333: Display of compound comparison operators as unicode symbols
 *      Kay Gürtzig     2017-01-27      Enh. #335: "dim" highlighted like "var" and ":" like "as"
 *      Kay Gürtzig     2017-02-01      KGU#335: Method splitLexically now reassembles floating-point literals (without sign)
 *      Kay Gürtzig     2017-02-07      Bugfix #341: Reconstruction of strings with mixed quotes in line fixed
 *      Kay Gürtzig     2017-03-30      Bugfix #333 (defective operator substitution), enh. #388 (const keyword)
 *      Kay Gürtzig     2017-04-14      Enh. #380: New highlighting mechanism troubleMakers / E_TROUBLECOLOR
 *      Kay Gürtzig     2017-05-22      Issue #354: Fixes type detection of binary, octal and hexadecimal literals
 *      Kay Gürtzig     2017-06-09      Enh. #416: Methods getUnbrokenText(), getBrokenText() introduced
 *      Kay Gürtzig     2017-07-02      Enh. #389: Signature of addFullText() reverted to two arguments
 *      Kay Gürtzig     2017-09-13      Enh. #423: New methods supporting type definitions
 *      Kay Gürtzig     2017-09-17      Enh. #423: Type name highlighting
 *      Kay Gürtzig     2017-09-18      Enh. #423: Recursive record definitions, splitLexically() improved
 *      Kay Gürtzig     2017-09-29      Enh. #423: splitLexically() debugged, now ellipses are tokens too
 *      Kay Gürtzig     2017-10-02      Enh. #423: Method extractDeclarationsFromList() and regex mechanisms revised
 *      Kay Gürtzig     2017-12-10/11   Enh. #487: Method access modifications to support hiding of declarations
 *      Kay Gürtzig     2018-01-21      Enh. #490: Methods for replacement of DiagramController aliases
 *      Kay Gürtzig     2018-02-02      Bugfix #501: Methods setAliasText() corrected (Case and Parallel elements)
 *      Kay Gürtzig     2018-07-02      KGU#245 Code revision: color0, color1,... fields replaced with colors array
 *      Kay Gürtzig     2018-07-20      Enh. #563: Intelligent conversion of simplified record initializers (see comment)
 *      Kay Gürtzig     2018-07-26      Issue #566: New central fields E_HOME_PAGE, E_HELP_PAGE
 *      Kay Gürtzig     2018-08-17      Bugfix #579: isConditionedBreakpoint() didn't work properly
 *      Kay Gürtzig     2018-09-10      Issue #508: New mechanism for proportional paddings (setFont(), E_PADDING_FIX) 
 *      Kay Gürtzig     2018-09-17      Issue #594: Last remnants of com.stevesoft.pat.Regex replaced
 *      Kay Gürtzig     2018-09-19      Structure preference field initialization aligned with ini defaults
 *      Kay Gürtzig     2018-09-24      Bugfix #605: Handling of const modifiers in declaration lists fixed
 *      Kay Gürtzig     2018-10-05      Bugfix #619: Declaration status of function result variable fixed
 *      Kay Gürtzig     2018-10-25      Enh. #419: New method breakTextLines(...)
 *      Kay Gürtzig     2019-03-07      Enh. #385: method extractDeclarationsFromList now also extracts default values
 *      Kay Gürtzig     2019-03-13      Issues #518, #544, #557: Element drawing now restricted to visible rect.
 *      Kay Gürtzig     2019-03-18      Enh. #56: Handling and highlighting of the throw keyword.
 *      Kay Gürtzig     2019-03-21      Enh. #707: Configurations for filename proposals
 *      Kay Gürtzig     2019-03-24      Bugfix #711: Eternal loop on parsing an instruction line
 *      Kay Gürtzig     2019-03-29      Issue #718: Breakthrough in drawing speed with syntax highlighting
 *      Kay Gürtzig     2019-05-15      Issue #724: Workaround for diagram titles in writeOutVariables
 *      Kay Gürtzig     2019-08-02      Issue #733: New method getPreferenceKeys() for partial preference export
 *      Kay Gürtzig     2019-11-17      Issue #739: Support for enum type definitions, addToTypeMap simplified
 *      Kay Gürtzig     2019-11-24      Bugfix #783: workaround for missing record type info
 *      Kay Gürtzig     2019-12-02      KGU#782: identifyExprType now also tries to detect char type
 *      Kay Gürtzig     2020-01-30      Missing newlines in E_THANKS (About > Implicated persons) inserted.
 *      Kay Gürtzig     2020-02-04      Bugfix #805 - method saveToINI decomposed
 *      Kay Gürtzig     2020-04-12      Bugfix #847 inconsistent handling of upper and lowercase in operator names (esp. DIV)
 *      Kay Gürtzig     2020-08-12      Enh. #800: Started to delegate syntactic analysis to class Syntax
 *      Kay Gürtzig     2020-10-17/19   Enh. #872: New mode to display operators in C style
 *      Kay Gürtzig     2020-10-30      Enh. #800: Methods unifyOperators moved to class Syntax
 *      Kay Gürtzig     2020-11-01      Issue #881: Highlighting of bit operators and Boolean literals
 *      Kay Gürtzig     2021-01-01      Issue #903: Syntax highlighting also in Popup when text and comment are switched
 *                                      Bugfix #904: Controller alias display wiped off all other routine names
 *                                      Issue #872: '=' in routine headers must not be replaced by "==" for C operator mode
 *      Kay Gürtzig     2021-01-02      Enh. #905: Method to draw a red triangle if an error entry refers to the element
 *      Kay Gürtzig     2021-01-10      Enh. #910: New method isImmutable(), synchronisation in writeOut...
 *      Kay Gürtzig     2021-01-22      Bugfix KGU#914 in splitExpressionList(StringList,...)
 *      Kay Gürtzig     2021-01-25      Enh. #915: New Structures preference "useInputBoxCase"
 *      Kay Gürtzig     2021-02-01/03   Bugfix #923: Method identifyExprType had ignored qualified names
 *      Kay Gürtzig     2021-02-03      Issue #920: Highlighting and tokenizing support for "Infinity" and '∞'
 *      Kay Gürtzig     2021-02-04      Enh. #905, #926: Improved drawing of Analyser flags and backlink support
 *      Kay Gürtzig     2021-02-24      Enh. #410: "?" added as lexical delimiter and operator symbol
 *      Kay Gürtzig     2021-03-03      Issue #954: Modified breakpoint behaviour
 *      Kay Gürtzig     2021-06-10      Enh. #926, #979: New method getAnalyserMarkerBounds() to support tooltip
 *      Kay Gürtzig     2021-10-25      Issue #800: cutOutRedundantMarkers substituted by Syntax.removeDecorators()
 *      Kay Gürtzig     2021-11-17      Bugfix #1021 in getHighlightUnits()
 *      Kay Gürtzig     2022-05-31      Bugfix #1037 in getHighlightUnits()
 *      Kay Gürtzig     2022-07-07      Issue #653: Consistency with Colors.defaultColors ensured
 *      Kay Gürtzig     2022-08-22      Bugfix #1068: Type inference failure for array initialisers mended
 *      Kay Gürtzig     2023-11-06      Issue #800: First bugfixing after code revision towards TokenList
 *      Kay Gürtzig     2023-12-14      Issue #1119: To set an empty string as text now leads to an empty StringList
 *      Kay Gürtzig     2024-01-22      Bugfix #1125: Equality check must consider disabled state
 *      Kay Gürtzig	    2024-03-07      Bugfix #1128 Risk of endless loop in method retrieveComponentNmes() fixed;
 *                                      Issue #1129: Limitation of error lines in the Analyser warning popup
 *      Kay Gürtzig     2024-03-21      Bugfix #1128 revised (method retrieveComponentNmes()).
 *      Kay Gürtzig     2024-03-22      Issue #1154: New method drawHatched(Rect, Canvas) to allow subclassing
 *      Kay Gürtzig     2024-04-16      Bugfix #1160: Separate X and Y text offset for drawing rotated elements
 *      Kay Gürtzig     2024-10-09      Enh. #1171: New methods fetchViewSettings(Ini) and cacheViewSettings(Ini)
 *      Kay Gürtzig     2025-07-02      Issue #270: Implementation of isDisabled(boolean) was defective.
 *      Kay Gürtzig     2025-08-01      Enh. #1197: Support/precaution for IFork branch colouring,
 *                                      Enh. #1198: Case editor choice option changed from boolean to enum
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *      2018-07-20 (Kay Gürtzig): Enh. #563
 *      - splitRecordInitializer can now also associate bare initializers (i.e. without explicit component names),
 *        provided that it obtains a valid record type entry as parameter.
 *      2016-07-28 (Kay Gürtzig): Bugfix #210 (KGU#225)
 *      - Before this fix the execution count values were held locally in the Elements. Without recursion,
 *        this wasn't a problem. But for recursive algorithms, particularly for spawning recursion as
 *        in Fibonacci, QuickSort, or binary search trees, all attempts to combine the counts from the
 *        various copies of an algorithm failed in the end. So now the counters are placed in a static
 *        vector on base class Element, the actual element instances hold only indices into this table.
 *        Hence, cloning of elements is no longer a problem, all copies of an element (recursion copies
 *        the diagram!) look via the shared index into the same vector slot and increment it when executed,
 *        at what call level ever. Still, the differences in run data copying between the element classes
 *        must still be put under scrutinous analysis. Not all differences seem plausible.
 *      2016-03-06 / 2016-03-12 (Kay Gürtzig): Enhancements #77, #124 (KGU#117/KGU#156)
 *      - According to an ER by [elemhsb], first a mechanism optionally to visualise code coverage (for
 *        white-box test completeness) was implemented. A green background colour was proposed and used
 *        to highlight covered Element. It soon became clear that with respect to subroutines a dis-
 *        tinction among loose (shallow) and strict (deep) coverage was necessary, particularly when
 *        recursion comes in. So the coverage tracking could be switched between shallow mode (where
 *        subroutines were automatically regarded as proven to have been covered previously, such the
 *        first CALL to a routine it was automatically marked as covered as well) and deep mode where
 *        a CALL was only marked after the subroutine (regarded as brand-new and never analyzed) had
 *        fully been covered at runtime.
 *      - When this obviously worked, I wanted to get more out of the new mechanism. Instead of
 *        deciding first which coverage tracking to do and having to do another run to see the effect
 *        of the complementary option, always both kinds of analysis were done at once, and the user
 *        could arbitrarily switch between the two possible coverage results.
 *      - And then I had a really great idea: Why not add some more runtime data collection, once data
 *        are collected? And so I added an execution counter for every very element, such that after
 *        a run one might easily see, how often a certain operation was executed. And a kind of
 *        histographic analysis seemed also sensible, i.e. to show how the load is distributed over
 *        the elements (particularly the structured ones) and how many instruction steps were needed
 *        in total to run the algorithm for certain data. This is practically an empirical abstract
 *        time estimation. Both count numbers (execution counter / instruction load) are now written
 *        to the upper right corner of any element, and additionally a scaled colouring from deep
 *        blue to hot red is used to visualize the hot spots and the lonesome places.
 *      2016-02-25 / 2016-03-02 (Kay Gürtzig): Bugfix #97 (KGU#136)
 *      - Methods prepareDraw() and draw() used the same field rect for temporary calculations but in
 *        a slightly different way: draw() left a bounding rec related to the Root coordinates whereas
 *        prepareDraw() always produced a (0,0)-bound rectangle i. e. with (0,0) as upper left corner.
 *      - getElementByCoord(), however compared the cursor coordinates with rect expecting it to contain
 *        the real drawing coordinates
 *      - getElementByCoord() was not ensured to be called after a draw() invocation but could follow a
 *        prepareDraw() call in which case the coordinate comparison led to wrong results
 *      - So a new field rect0 was introduced for prepareDraw() - in combination with field isRectUpToDate
 *        it even allows to avoid unnecessary re-calculation.
 *      - Field rect was also converted to a (0,0)-bound and hence position-independent bounds rectangle
 *        (in contrast to rect0 representing actual context-sensitive drawing extension (important for
 *        selection).
 *      2015.12.01 (Kay Gürtzig: KGU#91/KGU#92)
 *      - Methods setText() were inconsistent and caused nasty effects including data losses (bug #39).
 *      - Operator unification enhanced (issue #41)
 *      2015.11.03 (Kay Gürtzig: KGU#18/KGU#23/KGU#63)
 *      - Methods writeOutVariables() and getWidthOutVariables re-merged, lexical splitter extracted from
 *        them.
 *      2015.11.01 (Kay Gürtzig: KGU#18/KGU#23)
 *      - Methods unifyOperators(), transformIntermediate() and getIntermediateText() now support different
 *        activities like code generation and execution in a unique way.
 *      2015.10.11/13 (Kay Gürtzig: KGU#41 + KGU#43)
 *      - New fields added to distinguish states of selection from those of current execution, this way
 *        inducing more stable colouring and execution path tracking
 *      - a field and several methods introduced to support the setting of breakpoints for execution (it had
 *        always been extremely annoying that for the investigation of some issues near the end of the diagram
 *        either the entire execution had to be started in step more or you had to be utterly quick to pause
 *        in the right moment. Now breakpoints allow to catch the execution wherever necessary.
 *      2015.10.09 (Kay Gürtzig)
 *      - In E_SHOWCOMMENTS mode, substructures had been eclipsed by the top-level elements popping their
 *        comments. This was due to an incomplete subclassing of method getElementByCoord (in contrast
 *        to the nearly identical method selectElementByCoord), both methods were merged by means of a
 *        discriminating additional parameter to identifyElementByCoord(_x, _y, _forSelection)
 *      2014.10.18 / 2014.11.11 (Kay Gürtzig)
 *      - Additions for highlighting of logical operators (both C and Pascal style) in methods
 *        writeOutVariables() and getWidthOutVariables(),
 *      - minor code revision respecting 2- and 3-character operator symbols
 *
 ****************************************************************************************************///


import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;

import lu.fisch.utils.*;
import lu.fisch.graphics.*;
import lu.fisch.structorizer.syntax.Expression;
import lu.fisch.structorizer.syntax.Line;
import lu.fisch.structorizer.syntax.LineParser;
import lu.fisch.structorizer.syntax.Syntax;
import lu.fisch.structorizer.syntax.SyntaxException;
import lu.fisch.structorizer.syntax.TokenList;
import lu.fisch.structorizer.syntax.Type;
import lu.fisch.structorizer.syntax.TypeRegistry;
import lu.fisch.structorizer.executor.Executor;
import lu.fisch.structorizer.gui.FindAndReplace;
import lu.fisch.structorizer.gui.IconLoader;
import lu.fisch.structorizer.io.*;
import lu.fisch.structorizer.syntax.Function;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

/**
 * Abstract parent class for all kinds of elements of Nassi-Shneiderman diagrams,
 * i.e., the basic algorithmic structure blocks.
 * Provides primitives and utilities for drawing, syntax analysis, preferences,
 * and traversal.
 * @author Bob Fisch
 */
public abstract class Element {
	
	// START AS 2021-03-25: Enh. #967 (ARM code variant) - KGU 2021-04-15 disabled
	//public static boolean ARM_GNU = false;
	// END AS 2021-03-25
	/** This enumeration type distinguishes drawing contexts for selection display */
	// START KGU#906 2021-01-06: Enh. #905 need another context to suppress triangles
	//public enum DrawingContext {DC_STRUCTORIZER, DC_ARRANGER};
	public enum DrawingContext {DC_STRUCTORIZER, DC_ARRANGER, DC_IMAGE_EXPORT};
	// END KGU#906 2021-01-06
	
	/** A cached text snippet with associated style information for syntax highlighting */
	protected class HighlightUnit {
		String textSnippet = null;
		Color textColor = Color.BLACK;
		boolean bold = false, underlined = false;
		
		public HighlightUnit(String text, Color color, boolean bold, boolean underlined)
		{
			this.textSnippet = text;
			this.textColor = color;
			this.bold = bold;
			// START KGU#903 2021-01-01: Bugfix #904 Underlining of aliases didn't work
			//this.underlined = bold;
			this.underlined = underlined;
			// END KGU#903 2021-01-01
		}
		public String toString()
		{
			return "HLU(" + this.textSnippet + ":" + (bold ? "B" : "") + (underlined ? "U" : "") + ")";
		}
	};

	// START KGU#484 2018-03-22: Issue #463
	public static final Logger logger = Logger.getLogger(Element.class.getName());
	// END KGU#484 2018-03-22

	// Program CONSTANTS
	// START KGU#563 2018-07-26: Issue #566 - we need a central homepage URL
	public static final String E_HOME_PAGE = "https://structorizer.fisch.lu";
	public static final String E_HELP_PAGE = "https://help.structorizer.fisch.lu/index.php";
	// END KGU#563 2018-007-26
	// START KGU#791 2020-01-20: Enh. #801 - support for offline help
	public static final String E_HELP_FILE = "structorizer_user_guide.pdf";
	/** Estimated size of the User Guide PDF file (to be adapted when User Guide significantly grows) */
	public static final long E_HELP_FILE_SIZE = 12300000;
	public static final String E_DOWNLOAD_PAGE = "https://www.fisch.lu/Php/download.php";
	// END KGU#791 2020-01-20
	public static final String E_VERSION = "3.32-28";
	public static final String E_THANKS =
	"Developed and maintained by\n"+
	" - Robert Fisch <robert.fisch@education.lu>\n"+
	" - Kay Gürtzig <kay.guertzig@fh-erfurt.de>\n"+
	"\n"+
	"Export classes initially written by\n"+
	" - Oberon: Klaus-Peter Reimers <k_p_r@freenet.de>\n"+
	" - Perl: Jan Peter Klippel <structorizer@xtux.org>\n"+
	" - KSH: Jan Peter Klippel <structorizer@xtux.org>\n"+
	" - BASH: Markus Grundner <markus@praised-land.de>\n"+
	" - Java: Gunter Schillebeeckx <gunter.schillebeeckx@tsmmechelen.be>\n"+
	" - C: Praveen Kumar <praveen_sonal@yahoo.com>\n"+
	" - C#: Gunter Schillebeeckx <gunter.schillebeeckx@tsmmechelen.be>\n"+
	" - C++: Kay Gürtzig <kay.guertzig@fh-erfurt.de>\n"+
	" - PHP: Rolf Schmidt <rolf.frogs@t-online.de>\n"+
	" - BASIC: Jacek Dzieniewicz\n" +
	" - Python: Daniel Spittank <kontakt@daniel.spittank.net>\n"+
	" - Javascript: Kay Gürtzig <kay.guertzig@fh-erfurt.de>\n"+
	" - ARM code: Alessandro Simonetta <alessandro.simonetta@gmail.com>\n"+
	" - PapDesigner: Kay Gürtzig <kay.guertzig@fh-erfurt.de>\n"+
	"Import grammars and parsers written and maintained by\n"+
	" - ANSI-C: Kay Gürtzig <kay.guertzig@fh-erfurt.de>\n"+
	" - COBOL: Simon Sobisch, Kay Gürtzig\n"+
	" - Java: Kay Gürtzig <kay.guertzig@fh-erfurt.de>\n"+
	" - Processing: Kay Gürtzig <kay.guertzig@fh-erfurt.de>\n"+
	" - Struktogrammeditor: Kay Gürtzig\n"+
	" - hus-Struktogrammer: Kay Gürtzig\n"+
	" - sbide: Kay Gürtzig\n"+
	"\n"+
	"License setup and checking done by\n"+
	" - Marcus Radisch <radischm@googlemail.com>\n"+
	" - Stephan <clauwn@freenet.de>\n"+
	" - Simon Sobisch (https://github.com/Gitmensch)\n"+
	"\n"+
	"User manual edited and updated by\n"+
	" - David Morais <narutodc@hotmail.com>\n"+
	" - Praveen Kumar <praveen_sonal@yahoo.com>\n"+
	" - Jan Ollmann <bkgmjo@gmx.net>\n"+
	" - Kay Gürtzig <kay.guertzig@fh-erfurt.de>\n"+
	"\n"+
	"Translations initially provided or substantially updated by\n"+
	" - NL: Jerone <jeronevw@hotmail.com>, Jaap Woldringh\n"+
	" - DE: Klaus-Peter Reimers <k_p_r@freenet.de>\n"+
	" - LU: Laurent Zender <laurent.zender@hotmail.de>\n"+
	" - ES: Andres Cabrera <andrescabrera20@gmail.com>\n"+
	" - PT/BR: Theldo Cruz Franqueiro <cruz@pucminas.br>\n"+
	" - IT: Andrea Maiani <andreamaiani@gmail.com>, A. Simonetta (University of Rome Tor Vergata)\n"+
	" - ZH-CN: Wang Lei <wanglei@hollysys.com>\n"+
	" - ZH-TW: Joe Chem <hueyan_chen@yahoo.com.tw>\n"+
	" - CZ: Vladimír Vaščák <vascak@spszl.cz>\n"+
	" - RU: Юра Лебедев <elita.alegator@gmail.com>\n"+
	" - FR: tph002 (https://github.com/tph002)\n"+
	"\n"+
	"Different good ideas and improvements contributed by\n"+
	" - Serge Marelli <serge.marelli@education.lu>\n"+
	" - T1IF1 2006/2007\n"+
	" - Gil Belling <gil.belling@education.lu>\n"+
	" - Guy Loesch <guy.loesch@education.lu>\n"+
	" - Claude Sibenaler <claude.sibenaler@education.lu>\n"+
	" - Tom Van Houdenhove <tom@vanhoudenhove.be>\n"+
	" - Sylvain Piren <sylvain.piren@education.lu>\n"+
	" - Bernhard Wiesner <bernhard.wiesner@informatik.uni-erlangen.de>\n"+
	" - Christian Fandel <christian_fandel@web.de>\n"+
	" - Sascha Meyer <harlequin2@gmx.de>\n"+
	" - Andreas Jenet <ajenet@gmx.de>\n"+
	" - Jan Peter Klippel <structorizer@xtux.org>\n"+
	" - David Tremain <DTremain@omnisource.com>\n"+
	" - Rolf Schmidt <rolf.frogs@t-online.de>\n"+
	" - Benjamin Neuberg (https://github.com/newboerg)\n"+
	" - Johannes Kässinger <johannes.kaessinger@gmail.com>\n" +
	" - Simon Sobisch (https://github.com/Gitmensch)\n"+
	
	"\n"+
	"File dropper class by\n"+
	" - Robert W. Harder <robertharder@mac.com>\n"+
	"\n"+
	"AKDocLayout class by\n"+
	" - Kobrix Software, Inc. <http://www.kobrix.com>\n"+
	"\n"+
	"GOLDParser Java engine by\n"+
	" - Ralph Iden <https://www.creativewidgetworks.com>\n"+
	"\n"+
	"Delphi grammar by\n"+
	" - Rob F.M. van den Brink <R.F.M.vandenBrink@hccnet.nl>\n"+
	"\n"+
	"Vector graphics export by\n"+
	" - FreeHEP Team <http://freehep.github.io/freehep-vectorgraphics/>\n"+
	"\n"+
	"Command interpreter provided by\n"+
	" - Pat Niemeyer <pat@pat.net>\n"+
	"\n"+
	"SuggestionDropDownDecorator (Content Assist) provided by\n"+
	" - LogicBig <www.logicbic.com>\n"+
	"";
	public final static String E_CHANGELOG = "";

	// some static quasi-constants
	// START KGU#494 2018-09-10: Enh. #508
	/** Mode for fixed, i.e. font-independent, E_PADDING (= standard behaviour before 3.28-07) */
	public static boolean E_PADDING_FIX = false;
	// END KGU#494 2018-09-10
	/** Padding between e.g. the content of elements and their borders */
	protected static int E_PADDING = 20;
	// START KGU#444 2018-12-18: Issue #417 - we may need to have an external access to the padding
	/** @return Current padding between e.g. the content of elements and their borders */
	public static int getPadding() {
		return E_PADDING;
	}
	// END KGU#444 2018-12-18
	// START KGU#412 2017-06-09: Enh. #416 re-dedicated this apparently unused constant for drawing continuation lines
	//public static int E_INDENT = 2;
	/** Used as minimum text indentation for continuation lines (after lines ending with backslash) */
	public static int E_INDENT = 4;
	// END KGU#412 2017-06-09
	/** Actually, the background colour for selected elements */
	public static Color E_DRAWCOLOR = Color.YELLOW;
	/** Background colour for collapsed elements and drawing surrogates for eclipsed declarations */
	public static Color E_COLLAPSEDCOLOR = Color.LIGHT_GRAY;
	// START KGU#41 2015-10-13: Executing status now independent from selection
	/** Background colour for Elements currently (to be) executed */
	public static Color E_RUNNINGCOLOR = Color.ORANGE; 
	// END KGU#41 2015-10-13
	/** Background colour for Elements with pending execution (substructure is currently executed) */
	public static Color E_WAITCOLOR = new Color(255,255,210);
	/** Colour for the comment indicator bar */
	public static Color E_COMMENTCOLOR = Color.LIGHT_GRAY;
	// START KGU#43 2015-10-11: New fix color for breakpoint marking
	/** Colour of the breakpoint indicator bar at element top */
	public static Color E_BREAKPOINTCOLOR = Color.RED;
	// END KGU#43 2015-10-11
	// START KGU#365 2017-04-14: Enh. #380 Introduced to highlight trouble-making elements during GUI activities
	/** Colour for temporary highlighting of elements causing trouble */
	public static final Color E_TROUBLECOLOR = Color.RED;
	// END KGU#365 2017-04-14
	// START KGU#701 2019-03-29: Issue #718 accelerate syntax highlighting by caching as much as possible
	private static final Color E_HL_VARIABLE_COLOR = Color.decode("0x000099");	// dark blue
	private static final Color E_HL_OPERATOR_COLOR = Color.decode("0x990000");	// burgundy
	private static final Color E_HL_STRING_COLOR   = Color.decode("0x770077");	// violet
	private static final Color E_HL_INOUT_COLOR    = Color.decode("0x007700");	// green
	private static final Color E_HL_JUMP_COLOR     = Color.decode("0xff5511");	// orange
	// END KGU#701 2019-03-29
	// START KGU#117 2016-03-06: Test coverage colour and mode for Enh. #77
	/** Background colour for test-covered elements in run data tracking mode */
	public static Color E_TESTCOVEREDCOLOR = Color.GREEN;
	/** Flag for run data tracking mode */
	public static boolean E_COLLECTRUNTIMEDATA = false;
	/** Current mode for run data visualisation */
	public static RuntimeDataPresentMode E_RUNTIMEDATAPRESENTMODE = RuntimeDataPresentMode.NONE;
	// END KGU#117 2016-03-06
	
	// START KGU

	/** Highlight variables, operators, string literals, and certain keywords? */
	public static boolean E_VARHIGHLIGHT = false;
	/** Enable comment bars and comment popups? */
	public static boolean E_SHOWCOMMENTS = true;
	/** Swap text and comment on displaying? */
	public static boolean E_TOGGLETC = false;
	// START KGU#227 2016-07-29: Enh. #128
	/** Draw elements with both text and comments? */
	public static boolean E_COMMENTSPLUSTEXT = false;
	// END KGU#227 2016-07-29
	// START KGU#477 2017-12-06: Enh. #487
	/** Eclipse sequences of mere declaration behind their first elements? */
	public static boolean E_HIDE_DECL = false;
	// END KGU#477 2017-12-06
	/** Show FOR loops according to DIN 66261? */
	public static boolean E_DIN = false;
	/** Is Analyser enabled? */
	public static boolean E_ANALYSER = true;
	// START KGU#906 2021-01-02: Enh. #905
	/** Shall warning markers be drawn in flawed elements? */
	public static boolean E_ANALYSER_MARKER = true;
	// END KGU#906 2021-01-02
	// START KGU#1116 2024-03-07: Issue #1129
	public static int E_ANALYSER_MAX_POPUP_LINES = 10;
	// END KGU#1116 2024-03-07
	// START KGU#123 2016-01-04: New toggle for Enh. #87
	/** Is collapsing by mouse wheel rotation enabled? */
	public static boolean E_WHEELCOLLAPSE = false;
	// END KGU#123 2016-01-04
	// START KGU#503 2018-03-14: Enh. #519 Configuration of the mouse wheel zoom direction
	/** Whether ctrl + wheel up is to zoom in (otherwise zoom out) */
	public static boolean E_WHEEL_REVERSE_ZOOM = false;
	// END KGU#503 2018-03-14
	// START KGU#699 2019-03-27: Issue #717
	public static int E_WHEEL_SCROLL_UNIT = 0;	// dummy value for OS adaptation on first scrolling ever.
	// END KGU#699 2019-03-27
	// START KGU#309 2016-12-15: Enh. #310 new saving options
	public static boolean E_AUTO_SAVE_ON_EXECUTE = false;
	public static boolean E_AUTO_SAVE_ON_CLOSE = false;
	public static boolean E_MAKE_BACKUPS = true;
	// END KGU#309 20161-12-15
	// START KGU#690 2019-03-21: Issue #707 - new saving options
	/**
	 * Specifies whether argument numbers are to be appended to a proposed file name
	 * @see #E_FILENAME_SIG_SEPARATOR
	 */
	public static boolean E_FILENAME_WITH_ARGNUMBERS = true;
	/**
	 * Specifies the separator character between base file name and argument numbers
	 * @see #E_FILENAME_WITH_ARGNUMBERS
	 */
	public static char E_FILENAME_SIG_SEPARATOR = '-';
	// END KGU#690 2019-03-21
	// START KGU#287 2017-01-11: Issue #81 (workaround)
	/** GUI scaling factor prepared for the next session */
	public static double E_NEXT_SCALE_FACTOR;
	// END KGU#287 2017-01-15
	// START KGU#331 2017-01-13:
	public static boolean E_SHOW_UNICODE_OPERATORS = true;
	// END KGU#331 2017-01-13
	// START KGU#872 2020-10-17: Enh. #872
	/** Option to show operators in C style (overrides {@link #E_SHOW_UNICODE_OPERATORS}) */
	public static boolean E_SHOW_C_OPERATORS = false;
	// END KGU#872 2020-10-17
	// START KGU#456 2017-11-05: Enh. #452
	/** Shall only the most important toolbar buttons be presented (beginners' mode)?*/
	public static boolean E_REDUCED_TOOLBARS = false;
	// END KGU#456 2017-11-05
	// START KGU#480 2018-01-21: Enh. #490
	/** Is the replacement of DiagramController aliases active? */
	public static boolean E_APPLY_ALIASES = false;
	// END KGU#480 2018-01-21
	// START KGU#952 2021-03-03: Issue #954 - Allow temporarily to ignore all breakpoints
	/** Controls the validity of existing breakpoints */
	public static boolean E_BREAKPOINTS_ENABLED = true;
	// END KGU#952 2021-03-03


	// some colors
	// START KGU#245 2018-02-07
//	public static Color color0 = Color.decode("0xFFFFFF");
//	public static Color color1 = Color.decode("0xFF8080");
//	public static Color color2 = Color.decode("0xFFFF80");
//	public static Color color3 = Color.decode("0x80FF80");
//	public static Color color4 = Color.decode("0x80FFFF");
//	public static Color color5 = Color.decode("0x0080FF");
//	public static Color color6 = Color.decode("0xFF80C0");
//	public static Color color7 = Color.decode("0xC0C0C0");
//	public static Color color8 = Color.decode("0xFF8000");
//	public static Color color9 = Color.decode("0x8080FF");
	private final static String[] defaultColors = {
			"FFFFFF",
			"FF8080",
			"FFFF80",
			"80FF80",
			"80FFFF",
			"0080FF",
			"FF80C0",
			"C0C0C0",
			"FF8000",
			"8080FF"
	};
	public final static Color[] colors = new Color[defaultColors.length];
	static {
		for (int i = 0; i < colors.length; i++) {
			colors[i] = Color.decode("0x" + defaultColors[i]);
		}
	}
	// END KGU#245 2018-02-07
	// START KGU#622 2022-07-07: Issue #653 the default colours in Colors had differed
	/**
	 * @return the array of default colours (for external use) */
	public static final Color[] getDefaultColors() {
		Color[] defCols = new Color[defaultColors.length];
		for (int i = 0; i < colors.length; i++) {
			defCols[i] = Color.decode("0x" + defaultColors[i]);
		}
		return defCols;
	}
	// END KGU#622 2022-07-07

	// text "constants"
	public static String preAlt = "(?)";
	public static String preAltT = "T";
	public static String preAltF = "F";
	public static String preCase = "(?)\n!\n!\ndefault";
	public static String preFor = "for ? <- ? to ?";
	public static String preWhile = "while (?)";
	public static String preRepeat = "until (?)";
	// START KGU#376 2017-07-01: Enh #389 - Configurable caption for the includes box of Root
	public static String preImport = "Included diagrams:";
	// END KGU#376 2017-07-01
	// START KGU#686 2019-03-15: Enh. #56 - configurable captions for the TRY block
	public static String preTry = "try";
	public static String preCatch = "catch";
	public static String preFinally = "finally";
	// END KGU#686 2019-03-15
	
	// START KGU#480 2018-01-19: Enh. #490 controller API alias mechanism
	/**
	 * Maps {@link DiagramController} routine signatures (@code <name>#<arity>) to aliases.
	 * @see #controllerAlias2Name
	 */
	public static final HashMap<String, String> controllerName2Alias = new HashMap<String, String>();
	/**
	 * Maps alias routine signatures (@code <alias>#<arity>) to the genuine {@link DiagramController} routine names
	 * @see #controllerAlias2Name
	 */
	public static final HashMap<String, String> controllerAlias2Name = new HashMap<String, String>();
	// END KGU#480 2018-01-19
	
	/** Used font for drawing element text */
	protected static Font font = new Font("Dialog", Font.PLAIN, 12);
	// START KGU#701 2019-03-29: Issue #718 We cache the dependent fonts too to save time
	protected static Font boldFont = null;
	protected static Font underlinedFont = null;
	protected static Font smallFont = null;
	// END KGU#701 2019-03-29
	/** A string indicating that the shortened text in collapsed elements may continue (an ellipse) */
	public static final String COLLAPSED =  "...";
	/** Whether the right branch of an alternative is to be padded (width enlarged) */
	public static boolean altPadRight = true;
	// START KGU#401 2017-05-17: Issue #405
	/** Number of CASE branches to trigger the attempt to shrink width by rotating branches */
	public static int caseShrinkByRot = 8;
	// END KGU#401 2017-05-17
	// START KGU#916 2021-01-25: Enh. #915
	// START KGU#997 2025-08-01: Enh. #1198 More than two choices sensible
	//public static boolean useInputBoxCase = true;
	public static CaseEditorChoice useInputBoxCase = CaseEditorChoice.NON_EMPTY;
	// END KGU#997 2025-08-01
	// END KGU#916 2021-01-25
	
	// START KGU#453 2017-11-01: Issue #447 - cope with line continuation
	protected static final String SOFT_LINE_BREAK = "\u00B6";
	// END KGU#453 2017-11-01

	// START KGU#906 2021-01-02: Enh. #905 Draw markers on elements with related Analyser reports
	/** Defines the bit number of the canvas flag for drawing Analyser markers or not */
	protected static final int CANVAS_FLAGNO_ERROR_CHECK = 0;
	// END KGU#906 2021-01-02

	// START KGU#156 2016-03-10; Enh. #124
	/** Maximum number of executions of any element while runEventTracking has been on */
	protected static int maxExecCount = 0;
	/** Maximum number of operation steps carried out directly per element */
	protected static int maxExecStepCount = 0;
	/** Maximum combined number of directly and indirectly performed operation steps */
	protected static int maxExecTotalCount = 0;
	// END KGU156 2016-03-10
	// START KGU#477 2017-12-10: Enh. #487 - mode E_HIDE_DECL required an additional max count
	/** Maximum combined number of performed steps including aggregated eclipsed declarations */
	protected static int maxExecStepsEclCount = 0; 
	// END KGU#477 2017-12-10
	// START KGU#225 2016-07-28: Bugfix #210
	/** Execution counter table, each element (together with all its clones) has an individual index */
	protected static Vector<Integer> execCounts = new Vector<Integer>();
	// END KGU#225 2016-07-28
	// START KGU#213 2016-08-02: Enh. #215
	/**
	 *  Container for temporarily (i.e. during execution) modified breakpoint count triggers
	 *  Keys are the indices into execCounts
	 */
	protected static Map<Integer, Integer> breakTriggersTemp = new Hashtable<Integer, Integer>();
	// END KGU#213 2016-08-2

	// START KGU#365 2017-04-14: Enh. #380 - New mechanism to mark trouble-making elements
	/**
	 * Set for quick highlighting elements that cause trouble in some complex GUI activities.
	 * Intended to be highlighted in the E_TROUBLECOLOR with high fill color priority
	 */
	public static final Set<Element> troubleMakers = new HashSet<Element>();
	// END KGU#365 2017-04-14
	
//	element attributes
	protected StringList text = new StringList();
	public StringList comment = new StringList();

	// START KGU#701 2019-03-29: Issue #718 Attempt to accelerate syntax highlighting by caching
	/** Maps text lines to preprocessed highlighting units (string + colour + style flags) */
	protected HashMap<String, Vector<HighlightUnit>> highlightCache = new HashMap<String, Vector<HighlightUnit>>();
	// END KGU#701 2019-03-29
	
	// START KGU#790 2020-11-02: Issue #800
	/**
	 * Caches the syntax structure for the unbroken lines of text, to be cleared on every change
	 */
	protected Line[] parsedLines = null;
	// END KGU#790 2020-11-03
	
	/** If the element is to be displayed rotated by 90° counter-clockwise (only used within CASE structures) */
	public boolean rotated = false;

	public Element parent = null;
	public boolean selected = false;
	// START KGU#41 2015-10-13: Execution mark had to be separated from selection
	/** Is set while being executed */
	public boolean executed = false;
	// END KGU#41 2015-10-13
	/** Is set while a substructure Element is under execution */
	public boolean waited = false;
	// START KGU#117 2016-03-06: Enh. #77 - for test coverage mode
	/** Flag indicates shallow test coverage */
	public boolean simplyCovered = false;
	/** Flag indicates full test coverage (including called subroutine diagrams etc.) */
	public boolean deeplyCovered = false;
	// END KGU#117 2016-03-06
	// START KGU#156 2016-03-10; Enh. #124
	///** Number of times this was executed while runEventTracking has been on */
	//protected int execCount = 0;
	/** Number of instructions carried out directly by this element */
	protected int execStepCount = 0;
	/** Number of instructions carried out by substructures of this element */
	protected int execSubCount;
	// END KGU#156 2016-03-11
	// START KGU#225 2016-07-28: Bugfix #210
	/** Index into the static execution counter table {@link #execCounts}, shared by all clones of the element */
	protected int execCountIndex = -1;
	// END KGU#225 2016-07-28
	// START KGU#277 2016-10-13: Enh. #270 Option to disable an Element from execution and export
	/**
	 * If true then this element is to be skipped on execution and outcommented on code export!
	 * Also see {@link #isDisabled(boolean)} for recursively inherited disabled state
	 */
	protected boolean disabled = false;
	// END KGU#277 2016-10-13

	// END KGU156 2016-03-10
	
	private Color color = Color.WHITE;

	private boolean collapsed = false;
	
	// START KGU#43 2015-10-11: States whether the element serves as breakpoint for execution (stop before!)
	protected boolean breakpoint = false;
	// END KGU#43 2015-10-11
	// START KGU#213 2016-08-01: Enh. #215 - Optional execution Count trigger for breakpoint (0 = always active)
	public int breakTriggerCount = 0;
	// END KGU#213 2016-08-01

	// used for drawing
	// START KGU#136 2016-02-25: Bugfix #97 - New separate 0-based Rect for prepareDraw()
	/** bounds aligned to fit in the context, no longer public (use {@link #getRect()} to read) */
	protected Rect rect = new Rect();
	/** minimum bounds for stand-alone representation */
	protected Rect rect0 = new Rect();
	/** upper left corner coordinate offset wrt drawPoint */
	protected Point topLeft = new Point(0, 0);
	// END KGU#136 2016-03-01
	/** Will be set and used by {@link #prepareDraw(Canvas)} (validity of {@link rect0}, avoids repeated evaluation) - to be reset on changes */
	protected boolean isRect0UpToDate = false;
	// START KGU#502/KGU#524/KGU#553 2019-03-14: We need a second flag for correct drawing confinement
	/** Will be set after the first complete drawing of the element (validity of {@link rect} - to be reset on changes */
	protected boolean wasDrawn = false;
	// END KGU#502/KGU#524/KGU#553 2019-03-14

	/** Strings to be highlighted in the element text (lazy initialisation) */
	// START KGU#843 2020-04-12: Bugfix #847 We should distinguish between case-indifferent and case-relevant search
	//private static StringList specialSigns = null;
	private static HashSet<String> specialSigns = null;
	/** Strings to be highlighted in the element text case-independently (lazy initialisation) */
	private static StringList specialSignsCi = null;
	// END KGU#843 2020-04-12

	// START KGU#261 2017-01-19: Enh. #259 prepare the variable type map
	private static long lastId = 0;

	/**
	 * Change- and cloning-invariant id of this element
	 */
	private long id = 0;
	private void makeNewId()
	{
		id = ++lastId;
	}
	public long getId()
	{
		return id;
	}

	public Element()
	{
		makeNewId();
	}

	public Element(String _string)
	{
		makeNewId();
		setText(_string);
	}

	public Element(StringList _strings)
	{
		makeNewId();
		setText(_strings);
	}
	// END KGU#261 2017-01-19

	public Element(ArrayList<TokenList> _tokenizedLines)
	{
		makeNewId();
		for (TokenList tokens: _tokenizedLines) {
			text.add(tokens.getString());
		}
	}

	/**
	 * Resets my cached drawing info
	 * 
	 * @see #isRect0UpToDate
	 * @see #prepareDraw(Canvas)
	 * @see #resetDrawingInfoUp()
	 * @see #resetDrawingInfoDown()
	 */
	protected final void resetDrawingInfo()
	{
		this.isRect0UpToDate = false;
		// START KGU#502/KGU#524/KGU#553 2019-03-14: Issues #518,#544,#557
		this.wasDrawn = false;
		// END KGU#502/KGU#524/KGU#553 2019-03-14
		// START KGU#401 2017-05-17: Issue #405
		this.rotated = false;
		// END KGU#401 2017-05-17
		// START KGU#701 2019-03-29: Issue #718
		this.highlightCache.clear();
		// END KGU#701 2019-03-29
	}
	/**
	 * Resets my drawing info and that of all of my ancestors
	 * 
	 * @see #isRect0UpToDate
	 * @see #prepareDraw(Canvas)
	 * @see #resetDrawingInfoDown()
	 */
	public final void resetDrawingInfoUp()
	{
		// If this element is touched then all ancestry information must be invalidated
		Element ancestor = this;
		do {
			ancestor.resetDrawingInfo();
		} while ((ancestor = ancestor.parent) != null);
	}
	/**
	 * Recursively clears all drawing info this subtree down
	 * 
	 * @see #isRect0UpToDate
	 * @see #prepareDraw(Canvas)
	 * @see #resetDrawingInfoUp()
	 */
	// START KGU#238 2016-08-11: Code revision
	//public abstract void resetDrawingInfoDown();
	public void resetDrawingInfoDown()
	{
		traverse(new IElementVisitor() {
			@Override
			public boolean visitPreOrder(Element _ele) {
				_ele.resetDrawingInfo();
				return true;
			}
			@Override
			public boolean visitPostOrder(Element _ele) {
				return true;
			}
		});
	}
	// END KGU#238 2016-08-11
	
	// START KGU#502/KGU#524/KGU#553 2019-03-13: New approach to reduce drawing contention
	/**
	 * Checks whether parts of this element will be visible within the given
	 * drawing region {@code _viewport}.
	 * 
	 * @param _viewport - the extent of the visible area
	 * @param _topLeft - the predicted upper left corner of this element
	 * @return {@code true} if some part of this elements bounds will be placed
	 *     within {@code _viewport}
	 */
	protected boolean checkVisibility(Rectangle _viewport, Rect _topLeft)
	{
		// START KGU#502/KGU#524/KGU#553 2019-03-14: Issues #518,#544,#557 - we must refer to the eventual record
		//Rectangle rctgl = new Rectangle(_topLeft.left, _topLeft.top, rect0.right, rect0.bottom);
		//return !this.isRect0UpToDate || _viewport == null || rctgl.intersects(_viewport);
		Rectangle rctgl = new Rectangle(_topLeft.left - E_PADDING/2, _topLeft.top - E_PADDING/2, rect.right - rect.left + E_PADDING, rect.bottom - rect.top + E_PADDING);
		return !this.wasDrawn || _viewport == null || rctgl.intersects(_viewport);
		// END KGU#502/KGU#524/KGU#553 2019-03-14
	}
	// END KGU#502/KGU#524/KGU#553 2019-03-13

	// abstract things
	/**
	 * Recursively computes the drawing extensions of the element and stores
	 * them in the 0-based rect0 attribute, which is also returned.
	 * 
	 * @param _canvas - the drawing canvas for which the drawing is to be prepared
	 * @return the origin-based extension record.
	 * 
	 * @see #draw(Canvas, Rect, Rectangle, boolean)
	 * @see #resetDrawingInfo()
	 * @see #resetDrawingInfoDown()
	 * @see #resetDrawingInfoUp()
	 */
	public abstract Rect prepareDraw(Canvas _canvas);

	/**
	 * Actually draws this element within the given canvas, using argument
	 * {@code _top_left} for the placement of the upper left corner. Uses
	 * attribute {@link #rect0} as prepared by {@link #prepareDraw(Canvas)}
	 * to determine the expected extensions and stores the the actually drawn
	 * bounds in attribute {@link #rect}.
	 * 
	 * @param _canvas - the drawing canvas where the drawing is to be done in 
	 * @param _top_left - conveyes the upper-left corner for the placement
	 * @param _viewport - the visible area
	 * @param _inContention - whether there is a massive drawing event contention
	 * 
	 * @see #checkVisibility(Rectangle, Rect)
	 * @see #isRect0UpToDate
	 */
	public abstract void draw(Canvas _canvas, Rect _top_left, Rectangle _viewport, boolean _inContention);
	
	/**
	 * Copies this element. Implementing subclasses may make use of some helper methods
	 * like {@link #copyDetails(Element, boolean)} to ensure that all relevant associated
	 * data will be copied (possibly depending on the context).
	 * @return a copy of this element.
	 * @see #copyDetails(Element, boolean)
	 */
	public abstract Element copy();
	
	/**
	 * Generic diagram traversal to be called with an IElementVisitor implementor
	 * in order to gather information or to modify the status of this Element and
	 * all its substructure.
	 * The _visitor may break the traversal on any visit.
	 * 
	 * @param _visitor - the visiting instance (must have access to the required
	 *          attributes or methods, of course)
	 * @return false iff the traversal is to be exited after this call
	 */
	public abstract boolean traverse(IElementVisitor _visitor);
	
	// START KGU#1142 2024-03-22: Issue #1154 Intended to be subclassed for special purposes
	/**
	 * Draws a dark grey hatch pattern into the given rectangle {@code rect} on the
	 * {@link Canvas} {@code canvas}.
	 * 
	 * @param myrect - the rectangle to be (partially) hatched
	 * @param canvas - the target canvas
	 */
	protected void drawHatched(Rect rect, Canvas canvas) {
		canvas.hatchRect(rect, 5, 10);
	}
	// END KGU#1142 2024-03-22
	
	// START KGU#156 2016-03-11: Enh. #124
	/**
	 * Copies the runtime data that is to be cloned - Usually this comprises the deep
	 * coverage status and the execution counter index. Only for certain kinds of elements
	 * the shallow coverage status is to be copied as well - therefore the argument.
	 * @param _target - target element of the copy operation
	 * @param _simply_too - whether the shallow coverage status is to be copied, too
	 */
	protected void copyRuntimeData(Element _target, boolean _simply_too)
	{
		if (Element.E_COLLECTRUNTIMEDATA)
		{
			if (_simply_too)	// This distinction is important
			{
				_target.simplyCovered = this.simplyCovered;
			}
			_target.deeplyCovered = this.deeplyCovered;
			// START KGU#225 2016-07-28: Bugfix #210
			//_target.execCount = this.execCount;
			this.makeExecutionCount();
			_target.execCountIndex = this.execCountIndex;
			// END KGU#225 2016-07-28
		}
	}
	// END KGU#156 2016-03-11
	
	// START KGU#213 2016-08-01: Enh. #215 - derived from Instruction
	/**
	 * Copies important attributes of this element and associated data to the target
	 * element {@code _ele}.
	 * @param _ele - the target element
	 * @param _simplyCoveredToo - whether on copying runtime data the shallow coverage
	 * status is to be copied, too
	 * @see #copyRuntimeData(Element, boolean)
	 */
	protected void copyDetails(Element _ele, boolean _simplyCoveredToo)
	{
		// START KGU#261 2017-01-19: Enh. #259 (type map)
		_ele.id = this.id;
		// END KGU#261 2017-01-19
		_ele.setComment(new StringList(this.getComment()));
		_ele.setColor(this.getColor());
		_ele.breakpoint = this.breakpoint;
		_ele.breakTriggerCount = this.breakTriggerCount;
		this.copyRuntimeData(_ele, _simplyCoveredToo);
		// START KGU#183 2016-04-24: Issue #169
		_ele.selected = this.selected;
		// END KGU#183 2016-04-24
		// START KGU#277 2016-10-13: Enh. #270
		_ele.disabled = this.disabled;
		// END KGU#277 2016-10-13
		_ele.collapsed = this.collapsed;
		
		// START KGU#701 2019-03-29: Issue #718 It should not cause harm to share this info (it's a map!)
		_ele.highlightCache = this.highlightCache;
		// END KGU#701 2019-03-29

		/* TODO (KGU#790 2020-11-02: Issue #800) It is to be decided whether
		 * it might be a good idea to copy (i.e. initially to share) the cached
		 * parsedLines - would only be wrong if a different text had been copied.
		 * And it could cause trouble if the copied element lies in a different
		 * TypeRegistry context (i.e. belongs to a different Root). So we better
		 * don't do it for now. */
		//_ele.parsedLines = parsedLines;
	}
	// END KGU#213 2016-08-01

	// START KGU#119 2016-01-02 Bugfix #78
	/**
	 * Returns true iff another is of same class, all persistent attributes are equal, and
	 * all substructure of another recursively equals the substructure of this. 
	 * @param _another - the Element to be compared
	 * @return true on recursive structural equality, false else
	 */
	public boolean equals(Element _another)
	{
		boolean isEqual = this.getClass() == _another.getClass();
		if (isEqual) isEqual = this.getText().getText().equals(_another.getText().getText());
		if (isEqual) isEqual = this.getComment().getText().equals(_another.getComment().getText());
		// START KGU#156 2016-03-12: Colour had to be disabled due to races
		//if (isEqual) isEqual = this.getColor().equals(_another.getColor());
		// END KGU#156 2016-03-12
		// START KGU#1113 2024-01-22: Enh. #270, Bugfix #1125 disabled state is a difference
		if (isEqual) isEqual = this.disabled == _another.disabled;
		// END KGU#1113 2024-01-22
		return isEqual;
	}
	// END KGU#119 2016-01-02
	
	// START KGU#911 2021-01-10: Enh. #910 DiagramController Includables are immutable
	/**
	 * @return {@code true} if this element must not be edited, moved, deleted etc.
	 */
	public boolean isImmutable()
	{
		Root myRoot = getRoot(this);
		return myRoot.isRepresentingDiagramController();
	}
	// END KGU#911 2021-01-19

	// START KGU#117 2016-03-07: Enh. #77
	/**
	 * Disjunctively combines the test coverage status and the execution counts
	 * of _cloneOfMine (which is supposed to a clone of this) with this own
	 * runtime data (coverage status, execution and step counts)
	 * (Important for recursive tests)
	 * @param _cloneOfMine - the Element to be combined (must be equal to this)
	 * @return true on recursive structural equality, false else
	 */
	public boolean combineRuntimeData(Element _cloneOfMine)
	{
		if (this.equals(_cloneOfMine))	// This is rather paranoia
		{
			this.simplyCovered = this.simplyCovered || _cloneOfMine.simplyCovered;
			this.deeplyCovered = this.deeplyCovered || _cloneOfMine.deeplyCovered;
			// START KGU#225 2016-07-28: Bugfix #210 - no longer needed, seemed wrong
			//this.execCount += _cloneOfMine.execCount;
			// END KGU#225 2016-07-28
			//this.execStepCount += _cloneOfMine.execStepCount;
			// In case of (direct or indirect) recursion the substructure steps will
			// gathered on a different way! We must not do it twice
//			if (!this.getClass().getSimpleName().equals("Root"))
//			{
//				this.execSubCount += _cloneOfMine.execSubCount;
//			}
			return true;
		}
		logger.log(Level.WARNING, "CombineRuntimeData for {0} FAILED!", this);
		return false;
	}
	// END KGU#117 2016-03-07

	// draw point
	Point drawPoint = new Point(0,0);

	/**
	 * @return the top left drawing coordinate of the containing diagram's root element
	 */
	public Point getDrawPoint()
	{
		Element ele = this;
		while(ele.parent!=null) ele=ele.parent;
		return ele.drawPoint;
	}

	/**
	 * Sets the top left drawing coordinate for the containing diagram's root element.
	 * @param point
	 */
	public void setDrawPoint(Point point)
	{
		Element ele = this;
		while(ele.parent!=null) ele=ele.parent;
		ele.drawPoint=point;
	}

	// START KGU#227 2016-07-30: Enh. #128
	// START KGU#1150 2024-04-16: Bugfix #1160 For rotation we need X and Y
	///**
	// * Provides a subclassable left offset for drawing the text
	// */
	//protected int getTextDrawingOffset()
	//{
	//	return 0;
	//}
	/**
	 * Provides a subclassable left offset for drawing the text
	 */
	protected int getTextDrawingOffsetX()
	{
		return 0;
	}
	/**
	 * Provides a subclassable top offset for drawing the text
	 */
	protected int getTextDrawingOffsetY()
	{
		return 0;
	}
	// END KGU#1150 2024-04-16
	// END KGU#227 2016-07-30

	/**
	 * Overwrites the text by the list of all strings resulting from splitting
	 * {@code _text} into separate lines (at all contained newline characters).
	 * Subclasses may override this with more complex behaviour.
	 * 
	 * @param _text - the source string (will be split by '\n')
	 */
	public void setText(String _text)
	{
		synchronized (text) {
			if (_text.isEmpty()) {
				text.clear();
			}
			else {
				text.setText(_text);	// Convert to a StringList
			}
		}
	}

	/**
	 * Just overwrites the text StringList by the given {@link StringList}
	 * {@code _text}
	 * 
	 * @param _text - the new content as {@link StringList}, will be assigned
	 *    as is, unless subclasses override it with more complex behaviour.
	 */
	public void setText(StringList _text)
	{
		synchronized (text) {
			text = _text;
		}
	}
	
	// START KGU#91 2015-12-01: We need a way to get the true value
	/**
	 * Returns the (decoded) content of the text field no matter if mode
	 * {@link #isSwitchTextCommentMode()} is active or not.<br/>
	 * Use {@code getText(false)} for a mode-sensitive effect.<br/>
	 * <b>NOTE:</b> This is no longer a reference to the internal text representation
	 * but a reconstructed copy.
	 * 
	 * @return the text as StringList
	 * 
	 * @see #getText(boolean)
	 * @see #getBrokenText()
	 * @see #getUnbrokenText()
	 * @see #getBrokenTokenText()
	 * @see #getUnbrokenTokenText()U
	 */
	public StringList getText()
	{
		return text;
	}
	/**
	 * Returns the content of the text field unless _alwaysTrueText is false and
	 * mode isSwitchedTextAndComment is active, in which case the comment field
	 * is returned instead.
	 * 
	 * @param _alwaysTrueText - if true then mode isSwitchTextAndComment is ignored
	 * @return either the text or the comment
	 */
	public StringList getText(boolean _alwaysTrueText)
	// END KGU#91 2015-12-01
	{
		if (!_alwaysTrueText & isSwitchTextCommentMode())
		{
			// START KGU#199 2016-07-07: Enh. #188
			// Had to be altered since the combination of instructions may produce
			// multi-line string elements which would compromise drawing
			//return comment;
			return StringList.explode(comment, "\n");
			// END KGU#199 2016-07-07
		}
		else
		{
			return text;
		}
	}
	
	//START KGU#453 2017-11-01: Bugfix #447 - we need a cute representation of broken lines in some cases
	/**
	 * Returns the content of the text field no matter if mode {@code isSwitchedTextAndComment}
	 * is active.<br/>
	 * In contrast to {@link #getText()}, end-standing backslashes will be wiped
	 * off. This is for cases where the deliberate breaking of lines is of no importance
	 * and would unnecessarily spoil the presentation.<br/>
	 * Use {@code getCuteText(false)} for a mode-sensitive effect.
	 * 
	 * @return the text StringList (in normal mode) the comment StringList otherwise
	 * 
	 * @see #getText()
	 * @see #getCuteText(boolean)
	 * @see #isSwitchTextCommentMode()
	 */
	public StringList getCuteText()
	{
		StringList cute = new StringList();
		synchronized (text) {
			for (int i = 0; i < text.count(); i++) {
				String line = text.get(i);
				if (line.endsWith("\\")) {
					line = line.substring(0, line.length() - 1);
				}
				cute.add(line);
			}
		}
		return cute;
	}
	/**
	 * Returns the content of the text field unless {@code _alwaysTrueText} is false and
	 * mode {@code isSwitchedTextAndComment} is active, in which case the comment field
	 * is returned instead.<br/>
	 * In contrast to {@link #getText(boolean)}, end-standing backslashes will be wiped
	 * off. This is for cases where the deliberate breaking of lines is of no importance
	 * and would unnecessarily spoil the presentation. 
	 * @param _alwaysTrueText - if true then mode isSwitchTextAndComment is ignored
	 * @return either the text or the comment
	 * @see #getCuteText()
	 * @see #getText(boolean)
	 * @see #isSwitchTextCommentMode()
	 */
	protected StringList getCuteText(boolean _alwaysTrueText)
	{
		if (!_alwaysTrueText && isSwitchTextCommentMode())
		{
			// START KGU#199 2016-07-07: Enh. #188
			// Had to be altered since the combination of instructions may produce
			// multi-line string elements which would compromise drawing
			//return comment;
			return StringList.explode(comment, "\n");
			// END KGU#199 2016-07-07
		}
		return getCuteText();
	}
	// END KGU#453 2017-11-01

	/**
	 * @return a shortened text for the case this is collapsed (usually the first line plus an ellipse). 
	 */
	public StringList getCollapsedText()
	{
		StringList sl = new StringList();
		// START KGU#91 2015-12-01: Bugfix #39: This is for drawing, so use switch-sensitive methods
		//if(getText().count()>0) sl.add(getText().get(0));
		// START KGU#480 2018-01-21: Enh. #490 - It might be that a controller routine call starts in the 1st line
		//if (getText(false).count()>0) sl.add(getText(false).get(0));
		StringList myText = getText(false);
		if (myText.count() > 0) {
			if (Element.E_APPLY_ALIASES && !isSwitchTextCommentMode()) {
				myText = StringList.explode(Element.replaceControllerAliases(myText.getText(), true, Element.E_VARHIGHLIGHT), "\n");
			}
			sl.add(myText.get(0));
		}
		// END KGU#480 2018-01-21
		// END KGU#91 2015-12-01
		sl.add(COLLAPSED);
		return sl;
	}
	
	// START KGU#413 2017-06-09: Enh. #416 Cope with line continuations
	/**
	 * Returns the text of this element but with re-concatenated and trimmed lines if some
	 * lines were deliberately broken (i.e. ended with backslash).
	 * 
	 * @return a StringList consisting of unbroken text lines
	 */
	public StringList getUnbrokenText()
	{
		return getBrokenText(" ");
	}
	/**
	 * Returns the text of this element as a new StringList where the parts of each broken
	 * line (by means of backslashes at line ends) will form together an element with
	 * newlines after the backslashes.
	 * 
	 * @return a StringList consisting of possibly broken text lines
	 * 
	 * @see #getUnbrokenText()
	 * @see #getBrokenTokenText()
	 */
	public StringList getBrokenText()
	{
		// The separator is actually just a newline character but escaped for regular expression syntax
		return getBrokenText("\\\n");
	}
	/**
	 * Returns the text of this element as a new StringList where each broken line
	 * (by means of backslashes) will be glued together such that instead of the
	 * delimiting backslashes the given {@code separator} will be inserted.
	 * 
	 * @param separator - an arbitrary string working as separator instead of the
	 *    original backslash
	 * @return a StringList consisting of possibly broken text lines
	 */
	protected StringList getBrokenText(String separator)
	{
		StringList sl = new StringList();
		int nLines = text.count();
		int i = 0;
		while (i < nLines) {
			String line = text.get(i).trim();
			while (line.endsWith("\\") && (i + 1 < nLines)) {
				line = line.substring(0, line.length()-1) + separator + text.get(++i).trim();
			}
			sl.add(line);
			i++;
		}
		return sl;
	}
	// END KGU#413 2017-06-09
	// START KGU#1097 2023-11-09: Issue #800 New internal representation
	/**
	 * Returns the text of this element as a new ArrayList of TokenLists where each
	 * broken set of lines (by means of trailing backslashes) will be glued together
	 * to a single token list line.
	 * 
	 * @return a list of TokenLists
	 * 
	 * @see #getBrokenTokenText()
	 * @see #setTokenText(ArrayList)
	 */
	public ArrayList<TokenList> getUnbrokenTokenText()
	{
		StringList unbroken = this.getUnbrokenText();
		ArrayList<TokenList> tt = new ArrayList<TokenList>();
		for (int i = 0; i < unbroken.count(); i++) {
			tt.add(new TokenList(unbroken.get(i), true));
		}
		return tt;
	}
	
	/**
	 * Returns the text of this element as a new ArrayList of TokenLists where each
	 * broken set of lines (by means of trailing backslashes) will be glued together
	 * to a single TokenList with a newline marker where a trailing backslash had
	 * broken the line (still preserving the backslash tokens).
	 * 
	 * @return a list of TokenLists, possibly with cached "\n" positions.
	 * 
	 * @see #getUnbrokenTokenText()
	 * @see #setTokenText(ArrayList)
	 */
	protected ArrayList<TokenList> getBrokenTokenText()
	{
		StringList broken = this.getBrokenText();
		ArrayList<TokenList> tt = new ArrayList<TokenList>();
		for (int i = 0; i < broken.count(); i++) {
			tt.add(new TokenList(broken.get(i), true));
		}
		return tt;
	}
	// END KGU#1097 2023-11-09
	// START KGU#1097 2023-11-15: Issue #800 New internal representation
	/**
	 * Overwrites the previous Element text with the given tokenized multi-line
	 * text. If any of the contained token lists happens to contain an escaped
	 * newline token (i.e., a string "\n") then the line will be split at this position and the part
	 * before it will be suffixed by a "\" token.<br/>
	 * 
	 * @param tokenLines - a list of {@link TokenList}s representing the (possibly
	 *    broken) lines. "Broken" means that the token lists may contain newline
	 *    tokens at which positions the respective token lists will be split into
	 *    several ones.
	 *    
	 * @see #getBrokenTokenText()
	 * @see #getUnbrokenTokenText()
	 */
	public void setTokenText(ArrayList<TokenList> tokenLines)
	{
		// FIXME ensure triggers
		StringList sl = new StringList();
		for (TokenList tokens: tokenLines) {
			int pos0 = 0;
			int posNl = -1;
			while ((posNl = tokens.indexOf("\n", pos0)) >= 0) {
				sl.add(tokens.subSequence(pos0, posNl).getString() + "\\");
				pos0 = posNl + 1;
			}
			sl.add(tokens.subSequenceToEnd(pos0).getString());
		}
		this.setText(sl);
	}
	// END KGU#1097 2023-11-15
	
	// START KGU#790 2020-11-01: Issue #800 new syntax engine
	/**
	 * Parses the (unbroken) lines of the text and returns the array of associated
	 * {@link Line} objects for each unbroken line. Does <b>not</b> overwrite
	 * {@link #parsedLines}.
	 * 
	 * @param problems - a {@link StringList} gathering occurred syntax complaints
	 * @param typeMap - a data type map to retrieve from and add to, or {@code null}
	 * @return the array of parse results per line
	 * 
	 * @see #getParsedText(StringList, StringList, StringList, TypeRegistry, StringList)
	 * @see #parseLines(TypeRegistry)
	 */
	public Line[] getParsedText(StringList problems, TypeRegistry typeMap)
	{
		StringList unbrText = this.getUnbrokenText();
		Line[] lines = new Line[unbrText.count()];
		for (int i = 0; i < lines.length; i++) {
			lines[i] = LineParser.getInstance().parse(unbrText.get(i), this, i, typeMap);
			// START KGU#790 2023-11-15: Issue #800
			if (lines[i].getType() == Line.LineType.LT_RAW) {
				problems.add(lines[i].getParserError() + " in line " + i);
			}
			// END KGU#790 2023-11-15
		}
		return lines;
	}

	/**
	 * Retrieves an array of parsed lines (a {@link Line} object for each unbroken line).
	 * Also collects the names of all variables that are assigned a value, explicitly
	 * declared, or simply used if the respective argument is not {@code null}.<br/>
	 * Does <b>not</b> overwrite {@link #parsedLines}.
	 * 
	 * @param assignedVars - gathers the names of modified variables if not {@code null}
	 * @param declaredVars - gathers the names of declared variables if not {@code null}
	 * @param usedVars - gathers the names of referenced variables if not {@code null}
	 * @param typeMap - a data type map to retrieve from and add to, or {@code null}
	 * @param problems - a {@link StringList} gathering occurred syntax complaints
	 * @return the array of parse results per line
	 */
	public Line[] getParsedText(StringList assignedVars, StringList declaredVars, StringList usedVars, TypeRegistry typeMap, StringList problems)
	{
		ArrayList<TokenList> unbrText = this.getUnbrokenTokenText();
		Line[] lines = new Line[unbrText.size()];
		for (int i = 0; i < lines.length; i++) {
			lines[i] = LineParser.getInstance().parse(unbrText.get(i).getString(), this, i, typeMap);
			if (lines[i].getType() != Line.LineType.LT_RAW) {
				lines[i].gatherVariables(assignedVars, declaredVars, usedVars, problems);
			}
			else {
				problems.add(lines[i].getParserError() + " in line " + i);
			}
		}
		return lines;
	}
	// END KGU#790 2020-11-01
	
	// START KGU#602 2018-10-25: Issue #419 - Tool to break very long lines is requested
	/**
	 * Breaks down all text lines longer than {@code maxLineLength} along the tokens
	 * into continuated lines (i.e. broken lines end with backslash). Already placed
	 * line breaks are preserved unless {@code rebreak} is {@code true}, in which case
	 * broken lines are first concatenated in order to be broken according to
	 * {@code maxLineLength}.<br/>
	 * If a token is longer than {@code maxLineLength} (it might be a string literal)
	 * then it will not be broken or decomposed in any way such that the line length
	 * limit may not always hold.<br/>
	 * If this method leads to a different text layout then the drawing info is
	 * invalidated up-tree.
	 * 
	 * @param maxLineLength - the number of characters a line should not exceed
	 * @param rebreak - if {@code true} then existing line breaks (trailing backslashes)
	 *     will not be preserved.
	 * @return {@code true} if the text was effectively modified, {@code false} otherwise
	 * 
	 * @see Root#breakElementTextLines(int, boolean)
	 */
	public boolean breakTextLines(int maxLineLength, boolean rebreak)
	{
		boolean modified = false;
		ArrayList<TokenList> tokenLines = null;
		// FIXME the critical section is incomplete but we cannot include this.setTokenText()
		synchronized (text) {
			if (rebreak) {
				tokenLines = this.getUnbrokenTokenText();
				modified = tokenLines.size() < this.text.count();
			}
			else {
				tokenLines = this.getBrokenTokenText();
			}
		}
		// We work backwards through the (possibly bre-broken) lines
		for (int i = tokenLines.size()-1; i >= 0; i--) {
			TokenList tokens = tokenLines.get(i);
			int length = tokens.length();
			if (tokens.isEmpty()) {
				continue;
			}
			ArrayList<TokenList> broken = tokens.breakAtLength(maxLineLength);
			if (broken.size() > 1 || !broken.isEmpty() && broken.get(0).length() != length) {
				tokenLines.remove(i);
				tokenLines.addAll(i, broken);
				modified = true;
			}
		}
		if (modified) {
			this.setTokenText(tokenLines);
			this.resetDrawingInfoUp();
		}
		return modified;
	}
	
	// START KGU#602 2018-10-25: Issue #419 - Mechanism to detect and handle long lines
	/**
	 * Detects the maximum text line length either on this very element alone
	 * ({@code _includeSubstructure = false}) or including all elements of the
	 * substructure.
	 * 
	 * @param _includeSubstructure - whether (in case of a complex element) the substructure
	 * is to be involved
	 * @return the maximum line length
	 */
	public int getMaxLineLength(boolean _includeSubstructure)
	{
		int maxLen = 0;
		for (int i = 0; i < this.text.count(); i++) {
			maxLen = Math.max(maxLen, this.text.get(i).length());
		}
		return maxLen;
	}
	// END KGU#602 2018-10-25
	
	// START KGU#480 2018-01-21: Enh. #490
	/**
	 * @return the text of this element, with {@link DiagramController} routine names
	 * replaced by user-specific alias names if {@link #E_APPLY_ALIASES} is {@code true}
	 * 
	 * @see #setAliasText(StringList)
	 * @see #getText()
	 */
	public StringList getAliasText()
	{
		// START KGU#790 2024-12-02: Issue #800 We must also replace symbolic key tokens
		//if (!Element.E_APPLY_ALIASES) {
		//	return this.getText();
		//}
		// END KGU#790 2024-12-02
		StringList sl = new StringList();
		for (TokenList tokens: getBrokenTokenText()) 
		{
			TokenList aliasTokens = replaceControllerAliases(tokens, true, false);
			// START KGU#790 2024-12-02: Issue #800 Now we replace symbolic key tokens
			aliasTokens = Syntax.decodeLine(aliasTokens);
			// END KGU#790 2024-12-02
			sl.add(StringList.explode(aliasTokens.getString(), "\n"));
		}
		return sl;
	}
	
	/**
	 * Sets the element text from {@code aliasText}, after having replaced all
	 * user-specific {@link DiagramController} routine aliases by the original
	 * routine names.
	 * @param aliasText - the intended element text, possibly containing aliases
	 * 
	 * @see #getAliasText()
	 * @see #setAliasText(String)
	 * @see #setText(StringList)
	 */
	public void setAliasText(StringList aliasText)
	{
		// START KGU#790 2024-12-02: Issue #800 We must also replace configured keywords by placeholders
		//if (Element.E_APPLY_ALIASES) {
		//	// START KGU#488 2018-02-02: Bugfix #501 - We must not undermine the effect of overriding of setText()!
		//	//text.setText(Element.replaceControllerAliases(aliasText.getText(), false, false));
		//	this.setText(Element.replaceControllerAliases(aliasText.getText(), false, false));
		//	// END KGU#488 2018-01-02
		//}
		//else {
		//	// START KGU#488 2018-02-02: Bugfix #501 - We must not undermine the effect of overriding of setText()!
		//	//text = aliasText;
		//	this.setText(aliasText);
		//	// END KGU#488 2018-01-02
		//}
		boolean isContinued = false;
		String[] keys = this.getRelevantParserKeys();
		StringList lines = new StringList();
		for (int i = 0; i < aliasText.count(); i++) {
//			StringList keywords = new StringList();
//			for (String key: keys) {
//				String keyword = Syntax.getKeyword(key);
//				if (keyword != null) {
//					keywords.addByLength(keyword);
//				}
//			}
			String line = aliasText.get(i);
			lines.add(Syntax.encodeLine(new TokenList(line), null,
					keys, Syntax.ignoreCase, isContinued).getString());
			isContinued = line.endsWith("\\");
		}
		this.setText(Element.replaceControllerAliases(lines.getText(), false, false));
		// END KGU#790 2024-12-02
	}
	
	/**
	 * Sets the element text from {@code aliasText} by splitting it at newlines,
	 * after having replaced all user-specific {@link DiagramController} routine
	 * aliases by the original routine names.
	 * 
	 * @param aliasText - the intended element text, possibly containing aliases
	 * @see #getAliasText()
	 * @see #setAliasText(StringList)
	 * @see #setText(String)
	 */
	public void setAliasText(String aliasText)
	{
		if (Element.E_APPLY_ALIASES) {
			this.setText(Element.replaceControllerAliases(aliasText, false, false));
		}
		else {
			// START KGU#488 2018-02-02: Bugfix #501 - We must not undermine the effect of overriding of setText()!
			//text.setText(aliasText);
			this.setText(aliasText);
			// END KGU#488 2018-02-02
		}
	}

	/**
	 * If {@link #E_APPLY_ALIASES} is true then replaces the names of all routines
	 * of {@link DiagramController} classes with registered alias names or vice versa,
	 * depending on {@code names2aliases} and returns the modified text. The text may
	 * contain newlines and line continuators (end-standing backslashes).
	 * 
	 * @param text - the (Element) text as concatenated string
	 * @param names2aliases - whether names are to be replaced by aliases (or vice versa)
	 * @param withArity - whether the replacing alias (or original name) is to be combined
	 *     with the number of the routine arguments (like this: "{@code <name>#<arity>}"). 
	 * @return the resulting text.
	 */
	public static String replaceControllerAliases(String text, boolean names2aliases, boolean withArity)
	{
		if (!Element.E_APPLY_ALIASES) {
			return text;
		}
		// FIXME
		// To preserve '\n' we have to split the text, but then a broken parameter list may not be recognised!
		//TokenList tokens = new TokenList(text);
		//replaceControllerAliases(names2aliases, withArity, tokens);
		//return tokens.getString();
		StringList lines = StringList.explode(text, "\n");
		for (int i = 0; i < lines.count(); i++) {
			TokenList tokens = new TokenList(lines.get(i));
			// Compose soft-broken lines
			while (!tokens.isBlank() && "\\".equals(tokens.getLast()) && i+1 < lines.count()) {
				tokens.addAll(new TokenList(lines.get(++i)));
			}
			replaceControllerAliases(names2aliases, withArity, tokens);
			// Restore soft line breaks
			int j = 0;
			int posBs = -1;
			while ((posBs = tokens.lastIndexOf("\\")) >= 0) {
				TokenList tail = tokens.remove(posBs+1, tokens.size());
				lines.set(i - j,  tail.getString());
				j++;
			}
			lines.set(i - j,  tokens.getString());
		}
		return lines.getText();
	}

	/**
	 * If {@link #E_APPLY_ALIASES} is true then replaces the names of all routines
	 * of {@link DiagramController} classes with registered alias names or vice versa,
	 * depending on {@code names2aliases} and returns the modified text. The text may
	 * contain newline characters as tokens.
	 * 
	 * @param tokens - an (Element) text line as TokenList (may contain "\n" as tokens)
	 * @param names2aliases - whether names are to be replaced by aliases (or vice versa)
	 * @param withArity - whether the replacing alias (or original name) is to be combined
	 *     with the number of the routine arguments (like this: "{@code <name>#<arity>}"). 
	 * @return the resulting text.
	 */
	public static TokenList replaceControllerAliases(TokenList tokens, boolean names2aliases, boolean withArity)
	{
		if (!Element.E_APPLY_ALIASES) {
			return tokens;
		}
		TokenList tokens1 = new TokenList(tokens);	// Make copy to avoid argument being changed
		replaceControllerAliases(names2aliases, withArity, tokens1);
		return tokens1;
	}

	/**
	 * Internal helper for {@link #replaceControllerAliases(String, boolean, boolean)} and
	 * {@link #replaceControllerAliases(TokenList, boolean, boolean)}
	 * 
	 * @param names2aliases - whether names are to be replaced by aliases (or vice versa)
	 * @param withArity - whether the replacing alias (or original name) is to be combined
	 *     with the number of the routine arguments (like this: "{@code <name>#<arity>}").
	 * @param tokens - an (Element) text line as TokenList (may contain "\n" as tokens)
	 */
	private static void replaceControllerAliases(boolean names2aliases, boolean withArity, TokenList tokens) {
		HashMap<String, String> substitutions = 
				names2aliases ? controllerName2Alias : controllerAlias2Name;
		for (int i = 0; i < tokens.size(); i++) {
			String token = tokens.get(i);
			if (Syntax.isIdentifier(token, false, null)) {
				// Skip line continuation symbols... (won't help if lines where broken, though)
				int j = i+1;
				String nextToken = null;
				while (j < tokens.size() && (nextToken = tokens.get(j++)).equals("\\"));
				// Now check for a beginning parameter list
				if (i+1 < tokens.size() && "(".equals(nextToken)) {
					int nArgs = Syntax.splitExpressionList(tokens.subSequenceToEnd(i+2), ",").size() - 1;
					String key = token.toLowerCase() + "#" + nArgs;
					String subst = substitutions.get(key);
					if (subst != null) {
						token = subst;
						if (withArity) {
							token += "#" + nArgs;
						}
						tokens.set(i, token);
					}
				}
			}
		}
	}
	// END KGU#480 2018-01-21

	public void setComment(String _comment)
	{
		comment.setText(_comment);
	}

	public void setComment(StringList _comment)
	{
		comment = _comment;
	}

	// START KGU#91 2015-12-01: We need a way to get the true value
	/**
	 * Returns the content of the comment field no matter if mode isSwitchedTextAndComment
	 * is active, use getComment(false) for a mode-sensitive effect.
	 * @return the text StringList (in normal mode) the comment StringList otherwise
	 */
	public StringList getComment()
	{
		return comment;
	}

	/**
	 * Returns the content of the comment field unless _alwaysTrueComment is false and
	 * mode isSwitchedTextAndComment is active, in which case the text field
	 * content is returned instead.
	 * 
	 * @param _alwaysTrueText - if true then mode isSwitchTextAndComment is ignored
	 * @return either the text or the comment
	 */
	public StringList getComment(boolean _alwaysTrueComment)
	// END KGU#91 2015-12-01
	{
		if (!_alwaysTrueComment && isSwitchTextCommentMode())
		{
			return getText();
		}
		else
		{
			return comment;
		}
	}

	/* START KGU#172 2016-04-01: Issue #145: Make it easier to obtain this information,
	 * 2019-03-16 made static, 2020-12-15 made public */
	/**
	 * Checks whether texts and comments are to be swapped for display.
	 * @return true iff the swichTextAndComments flag is on and commentsPlusText mode is not
	 */
	public static boolean isSwitchTextCommentMode()
	{
//		Root root = getRoot(this);
//		return (root != null && root.isSwitchTextAndComments());
		// START KGU#227 2016-07-31: Enh. #128 - Mode "comments and text" overrides "switch text/comments" 
		//return Element.E_TOGGLETC;
		return !Element.E_COMMENTSPLUSTEXT && Element.E_TOGGLETC;
		// END KGU#227 2016-07-31
	}
	/* END KGU#172 2016-04-01 */

	/**
	 * Returns whether this element appears as selected in the standard {@link DrawingContext}.
	 * @return true if the element is marked selected.
	 * @see #getSelected(DrawingContext)
	 */
	public boolean getSelected()
	{
		return selected;
	}
	
	/**
	 * Returns whether this element appears as selected w.r.t. the given {@link DrawingContext}.
	 * For most kinds of Element the {@code drawingContext} doesn't make a difference.
	 * @param drawingContext
	 * @return true if this element is selected (in @code drawingContext}.
	 * @see #getSelected()
	 */
	public boolean getSelected(DrawingContext drawingContext)
	{
		return selected;
	}

	/**
	 * Sets the selection flag on this element
	 * @param _sel - if the element is to be selected or not
	 * @return the element(s) actually being selected (null if _sel = false).
	 */
	public Element setSelected(boolean _sel)
	{
		selected = _sel;
		return _sel ? this : null;
	}

	/**
	 * Sets the selection flag on this element for the given {@code _drawingContext}
	 * @param _sel - if the element is to be selected or not
	 * @param _drawingContext - the drawing context for which this is intended.
	 * @return the element(s) actually being selected (null if _sel = false).
	 * @see #setSelected(boolean)
	 */
	public Element setSelected(boolean _sel, DrawingContext _drawingContext)
	{
		// Default is the same as setSelected()
		return setSelected(_sel);
	}


	// START KGU#183 2016-04-24: Issue #169 
	/**
	 * Recursively searches the subtree for the currently selected Element or Element
	 * sequence and returns it
	 * @return selected Element (null if none was found)
	 */
	public abstract Element findSelected();
	// END KGU#183 2016-04-24
	
	// START KGU 2016-04-24: replaces Root.checkChild(this, _ancestor)
	/**
	 * Checks if this is a descendant of _ancestor in the tree
	 * @param _parent - Element to be verified as ancestor of _child
	 * @return true iff this is a descendant of _ancestor
	 */
	public boolean isDescendantOf(Element _ancestor)
	{
		Element tmp = this.parent;
		boolean res = false;
		while ((tmp != null) && !(res = tmp == _ancestor))
		{
			tmp = tmp.parent;
		}
		return res;
	}
	// END KGU 2016-04-24

	// START KGU#143 2016-01-22: Bugfix #114 - we need a method to decide execution involvement
	/**
	 * Checks execution involvement.
	 * @return true iff this or some substructure of this is currently executed. 
	 */
	public boolean isExecuted()
	{
		// START KGU#143 2016-11-17: Issue #114 refinement
		//return this.executed || this.waited;
		return this.isExecuted(true);
		// END KGU#143 2016-11-17
	}
	// END KGU#143 2016-01-22
	
	// START KGU#143 2016-11-17: Bugfix #114 - we need a method avoiding cyclic recursion
	/**
	 * Checks execution involvement.
	 * @param checkParent - whether the waiting status of the owning Subqueue is relevant
	 * @return true iff this or some substructure of this is currently executed. 
	 */
	public boolean isExecuted(boolean checkParent)
	{
		return this.executed || this.waited
				|| checkParent && this.parent != null && this.parent.getClass().getSimpleName().equals("Subqueue") && this.parent.isExecuted();
	}
	// END KGU#143 2016-11-17

	// START KGU#156 2016-03-11: Enh. #124 - We need a consistent execution step counting
	/**
	 * Resets all element execution counts and the derived maximum execution count as well
	 * as all centrally held temporary breakpoint triggers
	 */
	public static void resetMaxExecCount()
	{
		Element.maxExecTotalCount = Element.maxExecStepCount = Element.maxExecCount = 0;
		// START KGU#477 2017-12-10: Enh. #487 - consider maximum steps of eclipsed declarations
		Element.maxExecStepsEclCount = 0;
		// END KGU#477 2017-12-10
		// START KGU#225 2016-07-28: Bugfix #210
		Element.execCounts.clear();
		// END KGU#225 2016-07-28
		// START KGU#213 2016-08-02: Enh. #215
		Element.breakTriggersTemp.clear();
		// END KGU#213 2016-08-02
	}

	// START KGU#225 2016-07-28: Bugfix #210
	/**
	 * Resets the execution count value of this element and all its clones
	 */
	protected void resetExecCount()
	{
		if (this.execCountIndex >= 0)
		{
			if (this.execCountIndex < Element.execCounts.size())
			{
				Element.execCounts.set(this.execCountIndex, 0);
			}
			else
			{
				this.execCountIndex = -1;
			}
		}
	}
	
	/*
	 * Ensures an entry in the static execution count array. If there
	 * hadn't been an entry, then its value will be 0 and its index is
	 * stored in this.execCountEntry
	 */
	protected void makeExecutionCount()
	{
		if (this.execCountIndex < 0 || this.execCountIndex >= Element.execCounts.size())
		{
			this.execCountIndex = Element.execCounts.size();
			Element.execCounts.add(0);
		}
	}
	
	/**
	 * Retrieves the associated execution count and returns it.
	 * @return current execution count for this element (and all its clones)
	 */
	protected int getExecCount()
	{
		int execCount = 0;
		if (this.execCountIndex >= 0)
		{
			if (this.execCountIndex < Element.execCounts.size())
			{
				execCount = Element.execCounts.get(this.execCountIndex);
			}
			else
			{
				logger.log(Level.SEVERE, "Illegal execCountIndex {0} on {1}", new Object[]{this.execCountIndex, this});
			}
		}
		return execCount;
	}
	// END KGU#225 2016-07-28

	/**
	 * Returns the summed up execution steps of this element and - if {@code _combined} is true - 
	 * all its substructure.
	 * @param _combined - whether the (cached) substructure step counts are to be added
	 * @return the requested step count
	 */
	public int getExecStepCount(boolean _combined)
	{
		return this.execStepCount + (_combined ? this.execSubCount : 0);
	}

	/**
	 * Increments the execution counter (provided that {@link #E_COLLECTRUNTIMEDATA} is
	 * enabled). If the new counter value exceeds {@link #maxExecCount} then the latter
	 * will be updated to that value. 
	 */
	public final void countExecution()
	{
		// Element execution is always counting 1, no matter whether element is structured or not
		// START KGU#225 2016-07-28: Bugfix #210
		//if (Element.E_COLLECTRUNTIMEDATA && ++this.execCount > Element.maxExecCount)
		//{
		//	Element.maxExecCount = this.execCount;
		//}
		if (Element.E_COLLECTRUNTIMEDATA)
		{
			this.makeExecutionCount();
			int execCount = this.getExecCount() + 1;
			Element.execCounts.set(this.execCountIndex, execCount);
			if (execCount > Element.maxExecCount)
			{
				Element.maxExecCount = execCount;
			}
		}
		// END KGU#225 2016-07-28
	}
	
	/**
	 * Updates the own or substructure instruction counter by adding the {@code _growth} value.
	 * @param _growth - the amount by which the counter is to be increased
	 * @param _directly - whether is to be counted as own instruction or the substructure's
	 */
	public void addToExecTotalCount(int _growth, boolean _directly)
	{
		if (Element.E_COLLECTRUNTIMEDATA)
		{
			if (_directly)
			{
				this.execStepCount += _growth;
				if (this.execStepCount > Element.maxExecStepCount)
				{
					Element.maxExecStepCount = this.execStepCount;
				}
			}
			else
			{
				this.execSubCount += _growth;
				Element.maxExecTotalCount = 
						Math.max(this.getExecStepCount(true),
								Element.maxExecTotalCount);			
			}
		}
	}
	// END KGU#156 2016-03-11
	
	// START KGU#117 2016-03-10: Enh. #77
	/**
	 * In test coverage mode, sets the local tested flag if element is fully covered
	 * and then recursively checks test coverage upwards all ancestry if
	 * {@code _propagateUpwards} is {@code true} (otherwise it would be postponed to
	 * the termination of the superstructure).
	 * @param _propagateUpwards if {@code true} then the change is immediately propagated  
	 */
	public void checkTestCoverage(boolean _propagateUpwards)
	{
		//System.out.print("Checking coverage of " + this + " --> ");
		if (Element.E_COLLECTRUNTIMEDATA)
		{
			boolean hasChanged = false;
			if (!this.simplyCovered && this.isTestCovered(false))
			{
				hasChanged = true;
				this.simplyCovered = true;
			}
			if (!this.deeplyCovered && this.isTestCovered(true))
			{
				hasChanged = true;
				this.deeplyCovered = true;
			}
			if (hasChanged && _propagateUpwards)
			{
				Element parent = this.parent;
				while (parent != null)
				{
					parent.checkTestCoverage(false);
					parent = parent.parent;
				}
			}
		}
		//System.out.println(this.tested ? "SET" : "unset");
	}
	
	/**
	 * Detects shallow or deep test coverage of this element according to the
	 * argument _deeply
	 * @param _deeply if exhaustive coverage (including subroutines is requested)
	 * @return true iff element and all its sub-structure is test-covered
	 */
	public boolean isTestCovered(boolean _deeply)
	{
		return _deeply ? this.deeplyCovered : this.simplyCovered;
	}
	// END KGU#117 2016-03-10

	// START KGU#144 2016-01-22: Bugfix for #38 - Element knows best whether it can be moved up or down
	/**
	 * Checks whether this has a successor within the parenting Subqueue
	 * @return true iff this is element of a Subqueue and has a successor
	 */
	public boolean canMoveDown()
	{
		boolean canMove = false;
		if (parent != null && parent.getClass().getSimpleName().equals("Subqueue"))
		{
			int i = ((Subqueue)parent).getIndexOf(this);
			canMove = (i+1 < ((Subqueue)parent).getSize()) && !this.isExecuted() && !((Subqueue)parent).getElement(i+1).isExecuted();
		}
		return canMove;
	}

	/**
	 * Checks whether this has a predecessor within the parenting Subqueue
	 * @return true iff this is element of a Subqueue and has a predecessor
	 */
	public boolean canMoveUp()
	{
		boolean canMove = false;
		if (parent != null && parent.getClass().getSimpleName().equals("Subqueue"))
		{
			int  i = ((Subqueue)parent).getIndexOf(this);
			canMove = (i > 0) && !this.isExecuted() && !((Subqueue)parent).getElement(i-1).isExecuted();
		}
		return canMove;
	}
	//	END KGU#144 2016-01-22
	
	// START KGU#199 2016-07-07: Enh. #188 - ensure Call elements for known subroutines
	/**
	 * Recursively identifies Instruction elements with call syntax matching one
	 * of the given subroutine signatures and converts respective elements to Call
	 * elements.
	 * @param signatures - strings of the form "&lt;routinename&gt;#&lt;arity&gt;"
	 */
	public abstract void convertToCalls(StringList _signatures);
	// END KGU#199 2016-07-07

	/**
	 * @return the current background colour of this element
	 * 
	 * @see #getFillColor()
	 * @see #getHexColor()
	 * @see #setColor(Color)
	 */
	public Color getColor()
	{
		return color;
	}

	/**
	 * @return the hexadecimal RGB repesentation of the current background colour
	 *     of this element
	 * 
	 * @see #getColor()
	 */
	public String getHexColor()
	{
		String rgb = Integer.toHexString(color.getRGB());
		return rgb.substring(2, rgb.length());
	}

	/**
	 * Extracts a hexadecimal string representation of the pure RGB value of 
	 * given Color {@code _color}.
	 * 
	 * @param _color - the Color to get the hex representation from
	 * @return the hexadecimal string of the pure RGB components
	 */
	public static String getHexColor(Color _color)
	{
		String rgb = Integer.toHexString(_color.getRGB());
		return rgb.substring(2, rgb.length());
	}

	/**
	 * Sets the general background colour for this Element
	 * 
	 * @param _color - the new Color to be assigned
	 * 
	 * @see #getColor()
	 * @see #getHexColor()
	 */
	public void setColor(Color _color)
	{
		// START KGU#1182 2025-08-01: Enh. #1197 precaution against possible null
		//color = _color;
		if (_color != null) {
			color = _color;
		}
		// END KGU#1182 2025-08-01
	}
	
	/**
	 * Returns the status-dependent background color or just the user-defined background color
	 * for this element.
	 * 
	 * @see #getFillColor(DrawingContext)
	 * @see #getColor()o
	 */
	protected Color getFillColor()
	{
		return getFillColor(DrawingContext.DC_STRUCTORIZER);
	}
	// START KGU#41 2015-10-13: The highlighting rules are getting complex
	// but are more or less the same for all kinds of elements
	protected Color getFillColor(DrawingContext drawingContext)
	{
		// This priority might be arguable but represents more or less what was found in the draw methods before
		if (this.waited) {
			// FIXME (KGU#117): Need a combined colour for waited + tested
			return Element.E_WAITCOLOR;
		}
		else if (this.executed) {
			return Element.E_RUNNINGCOLOR;
		}
		// START KGU#365 2017-04-14: Enh. #380
		else if (troubleMakers.contains(this)) {
			return Element.E_TROUBLECOLOR;
		}
		// END KGU#365 2017-04-14
		else if (this.getSelected(drawingContext)) {
			return Element.E_DRAWCOLOR;
		}
		// START KGU#117/KGU#156 2016-03-06: Enh. #77 + #124 Specific colouring for test coverage tracking
		else if (E_COLLECTRUNTIMEDATA &&
				E_RUNTIMEDATAPRESENTMODE != RuntimeDataPresentMode.NONE) {
			switch (E_RUNTIMEDATAPRESENTMODE) {
			case SHALLOWCOVERAGE:
			case DEEPCOVERAGE:
				if (this.isTestCovered(Element.E_RUNTIMEDATAPRESENTMODE == RuntimeDataPresentMode.DEEPCOVERAGE)) {
					return Element.E_TESTCOVEREDCOLOR;
				}
				break;
			default:
				return getScaleColorForRTDPM();
			}
		}
		// END KGU#117 2016-03-06
		else if (this.isCollapsed(true)) {
			// NOTE: If the background colour for collapsed elements should once be discarded, then
			// for Instruction subclasses the icon is to be activated in Instruction.draw() 
			return Element.E_COLLAPSEDCOLOR;
		}
		return getColor();
	}
	// END KGU#41 2015-10-13
	
	// START KGU#408 2021-02-26: Enh. #410 Allows Elements to modify this depending on certain status
	/** Returns the (default) text colour for the drawing of Element text */
	protected Color getTextColor()
	{
		return Color.BLACK;
	}
	// END KGU#408 2021-02-26
	
	// START KGU#156 2016-03-12: Enh. #124 (Runtime data visualisation)
	protected Color getScaleColorForRTDPM()
	{
		int maxValue = 0;
		int value = 0;
		boolean logarithmic = false;
		switch (Element.E_RUNTIMEDATAPRESENTMODE) {
		case EXECCOUNTS:
			maxValue = Element.maxExecCount;
			value = this.getExecCount();
			break;
		case EXECSTEPS_LOG:
			logarithmic = true;
		case EXECSTEPS_LIN:
			maxValue = Element.maxExecStepCount;
			// START KGU#477 2017-12-10: Enh. #487 - consider amalgamated declarations
			if (Element.E_HIDE_DECL && Element.maxExecStepsEclCount > Element.maxExecStepCount) {
				maxValue = Element.maxExecStepsEclCount;
			}
			// END KGU#477 2017-12-10
			value = this.getExecStepCount(false);
			break;
		case TOTALSTEPS_LOG:
			logarithmic = true;
		case TOTALSTEPS_LIN:
			maxValue = Element.maxExecTotalCount;
			// START KGU#477 2017-12-10: Enh. #487 - consider amalgamated declarations
			if (Element.E_HIDE_DECL && Element.maxExecStepsEclCount > Element.maxExecTotalCount) {
				maxValue = Element.maxExecStepsEclCount;
			}
			// END KGU#477 2017-12-10
			value = this.getExecStepCount(true);
			break;
		default:
				;
		}
		if (logarithmic) {
			// We scale the logarithm a little up lest there should be too few
			// discrete possible values
			if (maxValue > 0) maxValue = (int) Math.round(25 * Math.log(maxValue));
			if (value > 0) value = (int) Math.round(25 * Math.log(value));
		}
		return getScaleColor(value, maxValue);
	}
	
	/**
	 * Converts the value in the range 0 ... maxValue in the a colour
	 * from deep blue to hot red.
	 * @param value	- a count value in the range 0 (blue) ... maxValue (red)
	 * @param maxValue - the end of the scale 
	 * @return the corresponding spectral colour.
	 */
	protected final Color getScaleColor(int value, int maxValue)
	{
		// Actually we split the range in four equally sized sections
		// In section 0, blue is at the brightness limit, red sticks to 0,
		// and green is linearly rising from 0 to he brightness limit.
		// In section 1, green is at the brightness limit and blue is linearly
		// falling from the brightness limit down to 0.
		// In section 2, green is at the brightness limit and red in linearly
		// rising from 0 to he brightness limit.
		// In section 1, red is at the brightness limit and green in linearly
		// decreasing from the brightness limit down to 0.
		
		// Lest the background colour should get too dark for the text to remain
		// legible, we shift the scale (e.g. by a twelfth) and this way reduce
		// the effective spectral range such that the latter starts with a less
		// deep blue...
		int rangeOffset = maxValue/12;
		value += rangeOffset;
		maxValue += rangeOffset;
		
		if (maxValue == 0)
		{
			return Color.WHITE;
		}
		int maxBrightness = 255;
		int blue = 0, green = 0, red = 0;
		int value4 = 4 * value * maxBrightness;
		int maxValueB = maxValue * maxBrightness;
		if (value4 < maxValueB)
		{
			blue = maxBrightness;
			green = (int)Math.round(value4 * 1.0 / maxValue);
		}
		else if (value4 < 2 * maxValueB)
		{
			green = maxBrightness;
			blue = Math.max((int)Math.round((maxValueB * 2.0 - value4) / maxValue), 0);
		}
		else if (value4 < 3 * maxValueB)
		{
			green = maxBrightness;
			red = Math.max((int)Math.round((value4 - 2.0 * maxValueB) / maxValue), 0);
		}
		else
		{
			red = maxBrightness;
			green = Math.max((int)Math.round((maxValueB * 3.0 - value4) / maxValue), 0);
		}
		return new Color(red, green, blue);
	}
	// END KGU#156 2016-03-12
	
	// START KGU#43 2015-10-12: Methods to control the new breakpoint property
	/**
	 * Alternatingly enables / disables the breakpoint property of the element
	 * Does not change the stored break trigger counter
	 */
	public void toggleBreakpoint()
	{
		this.breakpoint = !this.breakpoint;
	}
	
	/**
	 * Returns whether this Element works as breakpoint on execution
	 * @return true if breakpoint is enabled (no matter whether it is a count trigger or unconditioned)
	 */
	public boolean isBreakpoint()
	{
		return this.breakpoint;
	}
	
	// START KGU#213 2016-08-01: Enh. #215
	/**
	 * Tells whether this element may trigger a break with execution counting
	 * @return true iff breakpoint is enabled and conditioned (waiting for certain execution count)
	 */
	public boolean isConditionedBreakpoint()
	{
		// START KGU#570 2018-08-17: Bugfix #579 Dynamic triggers hadn't been detected (i.e. if set after debugging started)
		//return this.breakpoint && this.breakTriggerCount > 0;
		return this.breakpoint && this.getBreakTriggerCount() > 0;
		// END KGU#570 2018-08-17
	}
	
	/**
	 * Informs whether this element triggers a break just now - regarding the breakpoint info
	 * @return true iff breakpoint is enabled and either unconditioned or the execution count will match the trigger count on completion
	 */
	public boolean triggersBreakNow()
	{
		// START KGU#952 2021-03-03: Issue #954 Breakpoints may be generally disabled
		if (!E_BREAKPOINTS_ENABLED) {
			return false;
		}
		// END KGU#952 2021-03-03
		int trigger =  this.getBreakTriggerCount();
		return this.breakpoint && (trigger == 0 || Element.E_COLLECTRUNTIMEDATA && trigger == this.getExecCount()+1);
	}
	
	/**
	 * Gets the current break trigger value for this element
	 * @return either the permanent or the temporary (runtime) trigger value (0 if there isn't any)
	 */
	public int getBreakTriggerCount()
	{
		int trigger = this.breakTriggerCount;
		if (Element.E_COLLECTRUNTIMEDATA && Element.breakTriggersTemp.containsKey(this.execCountIndex))
		{
			trigger = Element.breakTriggersTemp.get(this.execCountIndex);
		}
		return trigger;
	}

	/**
	 * Sets a new trigger count value (to be compared with the execution counter).
	 * Trigger value must not be negative. Otherwise it is ignored and the method
	 * returns false.
	 * @param newTriggerCount - new trigger (0 means no dependency of execution counter)
	 */
	// START KGU#252 2016-09-21: Issue #248 (Linux workaround)
	//public void setBreakTriggerCount(int newTriggerCount)
	public boolean setBreakTriggerCount(int newTriggerCount)
	// END KGU#252 2016-09-21
	{
		// START KGU#252 2016-09-21: Issue #248 (Linux workaround)
		if (newTriggerCount < 0) return false;
		// END KGU#252 2016-09-21
		// After execution has begun we must face the existence of recursion clones
		// So change must be held in a central map rather tahn being stored in an
		// arbitrary clone of the element.. 
		if (Element.E_COLLECTRUNTIMEDATA &&
				(Executor.getInstance().isRunning()
				|| Executor.getInstance().getPaus()))
		{
			this.makeExecutionCount();
			Element.breakTriggersTemp.put(this.execCountIndex, newTriggerCount);
		}
		else
		{
			this.breakTriggerCount = newTriggerCount;
		}
		// START KGU#252 2016-09-21: Issue #248 (Linux workaround)
		return true;
		// END KGU#252 2016-09-21
	}
	// END KGU#213 2016-08-01
	
	/**
	 * Recursively clears all breakpoints in this branch
	 */
	public void clearBreakpoints()
	{
		// START KGU#238 2016-08-11: Code revision
		//this.breakpoint = false;
		traverse(new IElementVisitor(){
			public boolean visitPreOrder(Element _ele)
			{
				_ele.breakpoint = false;
				return true;
			}
			public boolean visitPostOrder(Element _ele)
			{
				return true;
			}
				});
		// END KGU#238 2016-08-11
	}
	// END KGU#43 2015-10-12

	// START KGU#41 2015-10-13
	/**
	 * Recursively clears all execution flags in this branch
	 */
	// START KGU#238 2016-08-11: Code revision
	public void clearExecutionStatus()
	{
		traverse(new IElementVisitor(){
			public boolean visitPreOrder(Element _ele)
			{
				_ele.intClearExecutionStatus();
				return true;
			}
			public boolean visitPostOrder(Element _ele)
			{
				return true;
			}
		});
	}
	
	private void intClearExecutionStatus()
	// END KGU#238
	{
		this.executed = false;
		this.waited = false;
		// START KGU#117 2016-03-06: Enh. #77 - extra functionality in test coverage mode
		if (!E_COLLECTRUNTIMEDATA)
		{
			this.deeplyCovered = this.simplyCovered = false;;
			// START KGU#156 2016-03-10: Enh. #124
			// START KGU#225 2016-07-28: Bugfix #210
			//this.execCount = this.execStepCount = this.execSubCount = 0;
			this.execStepCount = this.execSubCount = 0;
			this.execCountIndex = -1;
			// END KGU#225 2016-07-28
			// END KGU#156 2016-03-10
		}
		// KGU#117 2016-03-06
	}
	// END KGU#41 2015-10-13

	// START KGU#117 2016-03-07: Enh. #77
	/** 
	 * Recursively clears test coverage flags and execution counts in this branch
	 */
	// START KGU#238 2016-08-11: Code revision
	public void clearRuntimeData()
	{
		traverse(new IElementVisitor(){
			public boolean visitPreOrder(Element _ele)
			{
				_ele.intClearRuntimeData();
				return true;
			}
			public boolean visitPostOrder(Element _ele)
			{
				return true;
			}
				});
	}
	
	private void intClearRuntimeData()
	// END KGU#238 2016-08-11
	{
		this.deeplyCovered = this.simplyCovered = false;;
		// START KGU#156 2016-03-11: Enh. #124
		// START KGU#225 2016-07-28: Bugfix #210
		//this.execCount = this.execStepCount = this.execSubCount = 0;
		this.execStepCount = this.execSubCount = 0;
		// START KGU#213 2016-08-02: Enh. #215
		Element.breakTriggersTemp.remove(this.execCountIndex);
		// END KGU#213 2016-08-02
		if (this.execCountIndex >= Element.execCounts.size())
		{
			this.execCountIndex = -1;
		}
		else if (this.execCountIndex >= 0)
		{
			Element.execCounts.set(this.execCountIndex, 0);
		}
		// END KGU#225 2016-07-28
		// END KGU#156 2016-03-11
	}
	// END KGU#117 2016-03-07

	// START KGU 2015-10-09 Methods selectElementByCoord(int, int) and getElementByCoord(int, int) merged
	/**
	 * Retrieves the the most specific (i.e. smallest or deepest nested) Element
	 * containing coordinate (_x, _y) and flags it as {@link #selected}.
	 * @param _x - element-local (i.e. w.r.t. {@link #rect}) horizontal coordinate
	 * @param _y - element-local (i.e. w.r.t. {@link #rect}) vertical coordinate
	 * @return the selected Element (if any)
	 * @see #getElementByCoord(int, int)
	 * @see #findSelected()
	 */
	public Element selectElementByCoord(int _x, int _y)
	{
//            Point pt=getDrawPoint();
//
//            if ((rect.left-pt.x<_x)&&(_x<rect.right-pt.x)&&
//                    (rect.top-pt.y<_y)&&(_y<rect.bottom-pt.y))
//            {
//                    return this;
//            }
//            else
//            {
//                    selected=false;
//                    return null;
//            }
		return this.getElementByCoord(_x, _y, true);
	}

	// 
	/**
	 * Retrieves the most specific (i.e. smallest or deepest nested) Element
	 * containing coordinate {@code (_x, _y)}. Does not touch the {@link #selected}
	 * state of any of the elements along the path.
	 * @param _x - element-local (i.e. w.r.t. {@link #rect}) horizontal coordinate
	 * @param _y - element-local (i.e. w.r.t. {@link #rect}) vertical coordinate
	 * @return the (sub-)Element at the given coordinate (null if there is none such)
	 * @see #getElementByCoord(int, int, boolean)
	 */
	public Element getElementByCoord(int _x, int _y)
	{
//            Point pt=getDrawPoint();
//
//            if ((rect.left-pt.x<_x)&&(_x<rect.right-pt.x)&&
//                    (rect.top-pt.y<_y)&&(_y<rect.bottom-pt.y))
//            {
//                    return this;
//            }
//            else
//            {
//                    return null;
//            }
		return this.getElementByCoord(_x, _y, false);
	}

	/**
	 * Retrieves the most specific (i.e. smallest or deepest nested) Element
	 * containing coordinate {@code (_x, _y)} and marks it as selected (if
	 * {@code _forSelection} is true). Other elements along the search path
	 * are marked as unselected (i.e. their {@link #selected} attribute is
	 * reset).
	 * @param _x - element-local (i.e. w.r.t. {@link #rect}) horizontal coordinate
	 * @param _y - element-local (i.e. w.r.t. {@link #rect}) vertical coordinate
	 * @param _forSelection - whether the identified element is to be selected
	 * @return the (sub-)Element at the given coordinate (null, if there is none such)
	 */
	public Element getElementByCoord(int _x, int _y, boolean _forSelection)
	{
		// START KGU#136 2016-03-01: Bugfix #97 - we will now have origin-bound rects and coords
		//Point pt=getDrawPoint();
		// END KGU#136 2016-03-01

		// START KGU#136 2016-03-01: Bugfix #97: Now all coords will be "local"
//		if ((rect.left-pt.x < _x) && (_x < rect.right-pt.x) &&
//				(rect.top-pt.y < _y) && (_y < rect.bottom-pt.y))
		if ((rect.left <= _x) && (_x <= rect.right) &&
				(rect.top <= _y) && (_y <= rect.bottom))
		// END KGU#136 2016-03-01
		{
			//System.out.println("YES");
			return this;
		}
		else 
		{
			//System.out.println("NO");
			if (_forSelection)	
			{
				selected = false;	
			}
			return null;
		}
	}
	// END KGU 2015-10-09
	
	// START KGU 2015-10-11: Helper methods for all Element types' drawing
	
	/**
	 * Draws the marker bar on the left-hand side of the given _rect 
	 * @param _canvas - the canvas to be drawn in
	 * @param _rect - supposed to be the Element's surrounding rectangle
	 */
	protected void drawCommentMark(Canvas _canvas, Rect _rect)
	{
		// START KGU#215 2015-07-25: Bugfix # 205 - If fill colour is the same use an alternative colour
		//_canvas.setBackground(E_COMMENTCOLOR);
		//_canvas.setColor(E_COMMENTCOLOR);
		Color commentColor = E_COMMENTCOLOR;
		if (commentColor.equals(this.getFillColor()))
		{
			commentColor = Color.WHITE;
		}
		_canvas.setBackground(commentColor);
		_canvas.setColor(commentColor);
		// END KGU#215 2015-07-25
		
		Rect markerRect = new Rect(_rect.left + 2, _rect.top + 2,
				_rect.left + 4, _rect.bottom - 2);
		
		if (breakpoint)
		{
			// spare the area of the breakpoint bar
			markerRect.top += 5;
		}
		
		_canvas.fillRect(markerRect);
	}
 
	/**
	 * Draws the marker bar on the top side of the given _rect
	 * @param _canvas - the canvas to be drawn in
	 * @param _rect - the surrounding rectangle of the Element (or relevant part of it)
	 */
	protected void drawBreakpointMark(Canvas _canvas, Rect _rect)
	{
		if (breakpoint) {
			_canvas.setBackground(E_BREAKPOINTCOLOR);
			_canvas.setColor(E_BREAKPOINTCOLOR);

			Rect markerRect = _rect.copy();

			markerRect.left += 2;
			markerRect.top += 2;
			markerRect.right -= 2;
			markerRect.bottom = markerRect.top+4;

			// START KGU#213 2016-08-01: Enh. #215
			//_canvas.fillRect(markerRect);
			if (this.isConditionedBreakpoint())
			{
				Rect squareDot = markerRect.copy();
				int height = markerRect.bottom - markerRect.top;
				squareDot.right = squareDot.left + height;
				while (squareDot.right <= markerRect.right)
				{
					_canvas.fillRect(squareDot);
					squareDot.left += 2*height;
					squareDot.right += 2*height;
				}
			}
			else
			{
				_canvas.fillRect(markerRect);
			}
			// END KGU#213 2016-08-01
		}
	}
	// END KGU 2015-10-11

	// START KGU#906 2021-01-02: Enh. #905
	/**
	 * Places a small red triangle in the upper left corner if this element
	 * is referred to by some {@link DetectedError} record in the owning
	 * {@link Root}.
	 * 
	 * @param _canvas - the drawing canvas
	 * @param _rect - the outer drawing rectangle
	 */
	protected void drawWarningSignOnError(Canvas _canvas, Rect _rect) {
		if (E_ANALYSER && E_ANALYSER_MARKER && _canvas.isSetFlag(CANVAS_FLAGNO_ERROR_CHECK)) {
			// START KGU#906 2021-02-04: Bugfix for #905 - consider collapsed case
//			Root myRoot = getRoot(this);
//			if (myRoot != null && myRoot.errors != null && !myRoot.errors.isEmpty()) {
//				for (DetectedError error: myRoot.errors) {
//					if (this == error.getElement()) {
//						Color oldCol = _canvas.getColor();
//						if (error.isWarning()) {
//							_canvas.setColor(Color.RED);
//						}
//						else {
//							_canvas.setColor(Color.BLUE);
//						}
//						int height = (int)Math.round(E_PADDING * Math.sin(Math.PI/3) / 2);
//						int yBase = _rect.top + E_PADDING/4 + height;
//						int[] xCoords = new int[] {
//								_rect.left + E_PADDING/4,		// left base corner
//								_rect.left + 3 * E_PADDING/4,	// right base corner
//								_rect.left + E_PADDING/2		// top corner
//						};
//						int[] yCoords = new int[] {
//								yBase,					// left base corner
//								yBase,					// right base corner
//								_rect.top + E_PADDING/4	// top corner
//						};
//						_canvas.fillPoly(new Polygon(xCoords, yCoords, xCoords.length));
//						_canvas.setColor(oldCol);
//						break;
//					}
//				}
//			}
			HashMap<Element, Vector<DetectedError>> errorMap = this.getRelatedErrors(false);
			{
				// There should be at most a single entry with a single error object
				for (Vector<DetectedError> errList: errorMap.values()) {
					DetectedError error = errList.firstElement();
					Color oldCol = _canvas.getColor();
					if (error.isWarning()) {
						_canvas.setColor(Color.RED);
					}
					else {
						_canvas.setColor(Color.BLUE);
					}
					// START KGU#1152 2024-04-17: Issue #1162 Avoid the background colour
					if (isSimilarToFillColor(_canvas.getColor(), 25)) {
						_canvas.setColor(Color.WHITE);
					}
					// END KGU#1152 2024-04-17
					Rect markerBounds = getAnalyserMarkerBounds(_rect, false);
					int[] xCoords = new int[] {
							markerBounds.left,		// left base corner
							markerBounds.right,		// right base corner
							markerBounds.left + E_PADDING/4		// top corner
					};
					int[] yCoords = new int[] {
							markerBounds.bottom,	// left base corner
							markerBounds.bottom,	// right base corner
							markerBounds.top		// top corner
					};
					_canvas.fillPoly(new Polygon(xCoords, yCoords, xCoords.length));
					_canvas.setColor(oldCol);
					break;
				}
			}
			// END KGU#906 2021-02-04
		}
	}
	// END KGU#906 2021-01-02
	
	// START KGU#1152 2024-04-17: Issue #1162 Auxiliary method for colour comparison
	/**
	 * Compares the given {@code color} with the current fill colour of this element.
	 * 
	 * @param color - the proposed draw colour
	 * @param tolerance - an integer tolerance for the RGB values
	 * @return {@code true} if all RGB components differ no more than by
	 *    {@code tolerance}
	 */
	private boolean isSimilarToFillColor(Color color, int tolerance)
	{
		Color fill = getFillColor();
		return (Math.abs(color.getRed() - fill.getRed()) < tolerance
				&& Math.abs(color.getGreen() - fill.getGreen()) < tolerance
				&& Math.abs(color.getBlue() - fill.getBlue()) < tolerance);
	}
	// END KGU#1152 2024-04-17
	
	// START KGU#979 2021-06-10: Enh. #926, #979 - tooltip on the Analyser marker 
	/**
	 * Returns the bounds for the Analyser marker "driehoekje" with respect to the given
	 * Element rectangle {@code Rect}
	 * 
	 * @param _rect - The bounding rectangle of the Element (with whatever relative reference point)
	 * @param _outer - whether {@code _rect} is the total bounds or just the text field's bounds
	 * @return the "driehoekje" bounds with respect to {@code _rect}
	 */
	public Rect getAnalyserMarkerBounds(Rect _rect, boolean _outer)
	{
		int height = (int)Math.round(E_PADDING * Math.sin(Math.PI/3) / 2);
		int xBase = _rect.left + E_PADDING/4;
		int yBase = _rect.top + E_PADDING/4 + height;
		if (this.isCollapsed(true)) {
			// Put it below or aside the icon
			int iconHeight = this.getIcon().getIconHeight();
			if (yBase + iconHeight < _rect.bottom) {
				yBase += iconHeight;
			}
			else {
				xBase += this.getIcon().getIconWidth();
			}
		}
		return new Rect(xBase, yBase - height, xBase + E_PADDING/2, yBase);
	}
	// END KGU#979 2021-06-10

	/**
	 * Returns a copy of the (relocatable i. e. 0-bound) extension rectangle.
	 * 
	 * @return a rectangle starting at (0,0) and spanning to (width, height).
	 * 
	 * @see #getRect(Point)
	 * @see #getRectOffDrawPoint()
	 */
	public Rect getRect()
	{
		return rect.copy();
	}

	// START KGU#136 2016-03-01: Bugfix #97
	/**
	 * Returns the bounding rectangle translated to point {@code relativeTo}.
	 * 
	 * @return a rectangle having {@code relativeTo} as its upper left corner.
	 * 
	 * @see #getRect()
	 * @see #getRectOffDrawPoint()
	 */
	public Rect getRect(Point relativeTo)
	{
		return new Rect(rect.left + relativeTo.x, rect.top + relativeTo.y,
				rect.right + relativeTo.x, rect.bottom + relativeTo.y);		
	}

	/**
	 * Returns the bounding rectangle translated relative to the drawingPoint.
	 * 
	 * @return a rectangle starting at {@link #topLeft}.
	 * 
	 * @see #getRect()
	 * @see #getRect(Point)
	 */
	public Rect getRectOffDrawPoint()
	{
		return getRect(this.topLeft);		
	}
	// END KGU#136 2016-03-01
	
	/** 
	 * @return the font currently used for diagram element texts
	 * 
	 * @see #setFont(Font)
	 */
	public static Font getFont()
	{
		return font;
	}

	/**
	 * Sets the font to be used for diagram element texts to {@code _font}.
	 * Does not trigger a redrawing of all diagrams but is effective for
	 * subsequent drawing.
	 * 
	 * @param _font - the new font to be used for further diagram rendering
	 * 
	 * @see #getFont()
	 */
	public static void setFont(Font _font)
	{
		font = _font;
		// START KGU 2019-03-29: Cache all dependent fonts to enhance drawing performance
		boldFont = new Font(Element.font.getName(), Font.BOLD, Element.font.getSize());
		// START KGU#480 2018-01-21: Enh. #490 - we will underline alias names
		// underlined font
		Map<TextAttribute, Integer> fontAttributes = new HashMap<TextAttribute, Integer>();
		fontAttributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
		underlinedFont = new Font(Element.font.getName(), Font.PLAIN, Element.font.getSize()).deriveFont(fontAttributes);
		smallFont = new Font(Element.font.getName(), Font.PLAIN, Element.font.getSize() * 2 / 3);
		// END KGU 2019-03-29
		// START KGU#572 2018-09-10: Issue #508
		if (!E_PADDING_FIX) {
			// set the padding relative to the used font size
			// by using a padding of 20 px as reference with a default font of 12 pt
			E_PADDING = (int)(20./12 * font.getSize());
		}
		else {
			// Adhere to the old 
			E_PADDING = 20;
		}
		// END KGU#572 2018-09-10: Issue #508
	}
	
	// START KGU#494 2018-09-11: Bundle the font height retrieval strewn over several subclasss and methods
	/**
	 * Derives the font height to be used for drawing preparation and drawing itself
	 * from the given {@link FontMetrics} (should correspond to on an UNzoomed (!) {@link Graphics2D}
	 * object).<br/>
	 * Note: This method is possibly subject to tuning.
	 * 
	 * @param fm - the underlying {@link FontMetrics}
	 * @return the font height in px.
	 */
	protected static int getFontHeight(FontMetrics fm)
	{
		//return fm.getHeight();					// Original measure (before and up to version 3.28-07)
		return fm.getLeading() + fm.getAscent();	// As introduced by Bob Fisch 2018-09-08 (omits the descent, stronger rounding impact)
	}
	// END KGU#494 2018-09-11

	/************************
	 * static things
	 ************************/

	/**
	 * Reloads the ini file (???) and adopts SOME of the configurable properties
	 * held on class Element. These are:<br/>
	 * - structure preferences<br/>
	 * - font</br>
	 * - colours
	 */
	public static void loadFromINI()
	{
		try
		{
			Ini ini = Ini.getInstance();
			ini.load();
			// elements
			// START KGU#494 2018-02-14: Enh. #508 (still disabled because not desirable either, e.g. in presentation mode)
			//E_PADDING = Math.round((float)(E_PADDING_BASE * Double.parseDouble(ini.getProperty("scaleFactor", "1.0"))));
			// END KGU#494 2018-01-14
			// START KGU 2017-01-06: Issue #327: Default changed to English
			preAltT = ini.getProperty("IfTrue", "T");
			preAltF = ini.getProperty("IfFalse", "F");
			preAlt  = ini.getProperty("If", "(?)");
			// START KGU 2016-07-31: Bugfix #212 - After corrected effect the default is also turned
			//altPadRight = Boolean.valueOf(ini.getProperty("altPadRight", "true"));
			altPadRight = Boolean.valueOf(ini.getProperty("altPadRight", "false"));
			// END KGU#228 2016-07-31
			StringList sl = new StringList();
			sl.setCommaText(ini.getProperty("Case","\"(?)\",\"!\",\"!\",\"default\""));
			preCase = sl.getText();
			// START KGU#401 2017-05-18: Issue #405 - allow to reduce CASE width by branch element rotation
			caseShrinkByRot = Integer.parseInt(ini.getProperty("CaseShrinkRot", "8"));
			// END KGU#401 2017-05-18
			// START KGU#916 2021-01-25: Enh. #915
			// START KGU#997 2025-08-01: Enh. #1198
			//useInputBoxCase = ini.getProperty("CaseEditor", "true").equals("true");
			String caseEditorMode = ini.getProperty("CaseEditor", "NON-EMPTY");
			// Legacy identification
			useInputBoxCase = CaseEditorChoice.NEVER;
			if (caseEditorMode.equals("true")) {
				useInputBoxCase = CaseEditorChoice.ALWAYS;
			}
			for (CaseEditorChoice ce: CaseEditorChoice.values()) {
				if (caseEditorMode.equals(ce.name())) {
					useInputBoxCase = ce;
				}
			}
			// END KGU#997 2025-08-01
			// END KGU#916 2021-01-25
			preFor    = ini.getProperty("For", "for ? <- ? to ?");
			preWhile  = ini.getProperty("While", "while (?)");
			preRepeat = ini.getProperty("Repeat", "until (?)");
			// END KGU 2017-01-06 #327
			// START KGU#686 2019-03-22: Enh. #56
			preTry    = ini.getProperty("Try", "try");
			preCatch  = ini.getProperty("Catch", "catch");
			preFinally= ini.getProperty("Finally", "finally");
			// END KGU#686 2019-03-22
			// START KGU#376 2017-07-02: Enh. #389
			preImport = ini.getProperty("Import", "Included diagrams:");
			// END KGU#376 2017-07-02
			// font
			// START KGU#264 2016-09-28: key Name replaced by the more expressive "Font"
			//setFont(new Font(ini.getProperty("Name","Dialog"), Font.PLAIN,Integer.valueOf(ini.getProperty("Size","12")).intValue()));
			String fontName = ini.getProperty("Name","Dialog");	// legacy property name, will be overridden by the newer "Font" if present
			setFont(new Font(ini.getProperty("Font",fontName), Font.PLAIN,Integer.valueOf(ini.getProperty("Size","12")).intValue()));
			// colors
			// START KGU#245 2018-07-02
//			color0=Color.decode("0x"+ini.getProperty("color0","FFFFFF"));
//			color1=Color.decode("0x"+ini.getProperty("color1","FF8080"));
//			color2=Color.decode("0x"+ini.getProperty("color2","FFFF80"));
//			color3=Color.decode("0x"+ini.getProperty("color3","80FF80"));
//			color4=Color.decode("0x"+ini.getProperty("color4","80FFFF"));
//			color5=Color.decode("0x"+ini.getProperty("color5","0080FF"));
//			color6=Color.decode("0x"+ini.getProperty("color6","FF80C0"));
//			color7=Color.decode("0x"+ini.getProperty("color7","C0C0C0"));
//			color8=Color.decode("0x"+ini.getProperty("color8","FF8000"));
//			color9=Color.decode("0x"+ini.getProperty("color9","8080FF"));
			for (int i = 0; i < colors.length; i++) {
				colors[i] = Color.decode("0x"+ini.getProperty("color" + i, defaultColors[i]));
			}
			// END KGU#245 2018-07-02
		}
		catch (Exception e)
		{
			logger.log(Level.SEVERE, "Error", e);
		}
	}
	
	// START KGU#1157 2024-10-08: Issue #1171 support for batch picture export
	/**
	 * Adopts several view settings held in fields from the given {@link Ini}
	 * instance
	 * 
	 * @param ini - the {@link Ini} instance (a singleton)
	 * 
	 * @see #cacheViewSettings(Ini)
	 */
	public static void fetchViewSettings(Ini ini)
	{
		E_DIN = ini.getProperty("DIN","1").equals("1");
		E_SHOWCOMMENTS = ini.getProperty("showComments","1").equals("1"); // default = 1
		E_COMMENTSPLUSTEXT = ini.getProperty("commentsPlusText","0").equals("1");	// default = 0
		E_TOGGLETC = ini.getProperty("switchTextComments","0").equals("1");
		E_VARHIGHLIGHT = ini.getProperty("varHightlight","1").equals("1");
		E_SHOW_C_OPERATORS = ini.getProperty("showOpsLikeC", "0").equals("1");
		E_HIDE_DECL = ini.getProperty("hideDeclarations","0").equals("1");	// default = 0
		// START KGU#331 2017-01-15: Enh. #333 Comparison operator display
		E_SHOW_UNICODE_OPERATORS = ini.getProperty("unicodeCompOps", "1").equals("1");
		// END KGU#331 2017-01-15
		// START KGU#494 2018-09-10: Issue #508
		E_PADDING_FIX = ini.getProperty("fixPadding", "0").equals("1");
		// END KGU#494 2018-09-10
	}
	
	/**
	 * Caches several view settings held in fields to the given {@link Ini} instance
	 *
	 * @param ini - the {@link Ini} instance (a singleton)
	 * 
	 * @see #fetchViewSettings(Ini)
	 */
	public static void cacheViewSettings(Ini ini)
	{
		// DIN, comments
		ini.setProperty("DIN", (Element.E_DIN ? "1" : "0"));
		ini.setProperty("showComments", (E_SHOWCOMMENTS ? "1" : "0"));
		// START KGU#227 2016-08-01: Enh. #128
		ini.setProperty("commentsPlusText", E_COMMENTSPLUSTEXT ? "1" : "0");
		// END KGU#227 2016-08-01
		ini.setProperty("switchTextComments", (E_TOGGLETC ? "1" : "0"));
		ini.setProperty("varHightlight", (E_VARHIGHLIGHT ? "1" : "0"));
		// START KGU#477 2017-12-06: Enh. #487
		ini.setProperty("hideDeclarations", E_HIDE_DECL ? "1" : "0");
		// END KGU#227 2016-12-06
		// START KGU#494 2018-09-10: Issue #508
		ini.setProperty("fixPadding", (E_PADDING_FIX ? "1" : "0"));
		// END KGU#494 2018-09-10
		// START KGU#331 2017-01-15: Enh. #333 Comparison operator display
		ini.setProperty("unicodeCompOps", (E_SHOW_UNICODE_OPERATORS ? "1" : "0"));
		// END KGU#331 2017-01-15
		// START KGU#872 2020-10-17: Enh. #872 Operator display in C style
		ini.setProperty("showOpsLikeC", (E_SHOW_C_OPERATORS ? "1" : "0"));
		// END KGU#872 2020-10-17
	}
	// END KGU#1157 2024-10-08

	// START KGU#466 2019-08-02: Issue #733 - selective preferences export
	/**
	 * Returns the preference keys used in the ini file for the given {@code category}
	 * (if class {@code Element} is responsible for the saving and loading of the properties
	 * of this category. Currently, the following categories are supported here:
	 * <ul>
	 * <li>"structure": Structure preferences</li>
	 * <li>"color": chosen set of colours</li>
	 * </ul>
	 * @param category - name of the category of interest (see method comment)
	 * @return a String array containing the relevant keys for the ini file
	 * 
	 * @see Element#getPreferenceKeys(String)
	 * @see Root#getPreferenceKeys()
	 * @see Syntax#getPreferenceKeys()
	 */
	public static String[] getPreferenceKeys(String category)
	{
		if (category.equals("structure")) {
			return new String[] {"IfTrue", "IfFalse", "If", "altPadRight",
					"Case", "CaseShrinkRot",
					// START KGU#916 2021-01-26: Enh. #915
					"CaseEditor",
					// END KGU#916 2021-01-26
					"For", "While", "Repeat", "Try", "Catch", "Finally", "Import"};
		}
		else if (category.equals("color")) {
			String[] colKeys = new String[defaultColors.length];
			for (int i = 0; i < colKeys.length; i++) {
				colKeys[i] = "color" + i;
			}
			return colKeys;
		}
		return new String[]{};
	}
	// END KGU#466 2019-08-02
	
	/**
	 * Saves most Element-based settings to the INI file.
	 * @see #cacheToIni()
	 */
	public static void saveToINI()
	{
		try
		{
			Ini ini = Ini.getInstance();
			ini.load();
			// elements
			cacheToIni();

			ini.save();
		}
		catch (Exception e)
		{
			logger.log(Level.SEVERE, "Error", e);
		}
	}
	
	// START KGU#792 2020-02-04: Bugfix #805
	/**
	 * Caches most Element-based preferences as properties to the Ini instance.
	 * @see #saveToINI()
	 */
	public static void cacheToIni() {
		Ini ini = Ini.getInstance();
		ini.setProperty("IfTrue", preAltT);
		ini.setProperty("IfFalse", preAltF);
		ini.setProperty("If", preAlt);
		// START KGU 2016-01-16: Stuff having got lost by a Nov. 2014 merge
		ini.setProperty("altPadRight", String.valueOf(altPadRight));
		// END KGU 2016-01-16
		StringList sl = new StringList();
		sl.setText(preCase);
		ini.setProperty("Case", sl.getCommaText());
		// START KGU#401 2017-05-18: Issue #405 - allow to reduce CASE width by branch element rotation
		ini.setProperty("CaseShrinkRot", Integer.toString(caseShrinkByRot));
		// END KGU#401 2017-05-18
		// START KGU#916 2021-01-25: Enh.#915 - offer alternative CASE editor
		// START KGU#997 2025-08-01: Enh. #1198 - more choice options
		//ini.setProperty("CaseEditor", Boolean.toString(useInputBoxCase));
		ini.setProperty("CaseEditor", useInputBoxCase.name());
		// END KGU#997 2025-08-01
		// END KGU#916 2021-01-25
		ini.setProperty("For", preFor);
		ini.setProperty("While", preWhile);
		ini.setProperty("Repeat", preRepeat);
		// START KGU#686 2019-03-22: Enh. #56
		ini.setProperty("Try", preTry);
		ini.setProperty("Catch", preCatch);
		ini.setProperty("Finally", preFinally);
		//END KGU#686 2019-03-22
		// START KGU#376 2017-07-02: Enh. #389
		ini.setProperty("Import", preImport);
		// END KGU#376 2017-07-02
		// font
		// START KGU#264 2016-09-28: font name property renamed 
		//ini.setProperty("Name",getFont().getFamily());
		ini.setProperty("Font", getFont().getFamily());
		// END KGU#264 2016-09-28
		ini.setProperty("Size", Integer.toString(getFont().getSize()));
		// colors
		for (int i = 0; i < colors.length; i++) {
			ini.setProperty("color" + i, getHexColor(colors[i]));
		}
	}
	// END KGU#792 2020-02-04

	/**
	 * Returns the {@link Root} the given Element {@code _element} is residing in.
	 * @param _element - the interesting Element
	 * @return the owning {@link Root} or null (if {@code _element} is orphaned).
	 * @see #getNestingDepth()
	 */
	public static Root getRoot(Element _element)
	{
		if (_element == null) {
			return null;
		}
		while (_element.parent != null)
		{
			_element = _element.parent;
		}
		if (_element instanceof Root)
			return (Root) _element;
		else
			return null;
	}
	
	/**
	 * Retrieves the length of the path from the given Element up to the rot (not
	 * counting the {@link Subqueue} levels).
	 * @param _element - the interesting Element
	 * @return the path length (0 for a Root, 1 for any of its immediate children etc.)
	 */
	public static int getNestingDepth(Element _element)
	{
		int depth = 0;
		while (_element.parent != null)
		{
			_element = _element.parent;
			depth++;
		}
		return depth / 2;
	}

//	// START KGU#101 2015-12-11: Enhancement #54: We need to split expression lists (might go to a helper class)
//	/**
//	 * Splits the string {@code _text}, which is supposed to represent a list of expressions
//	 * separated by character sequences {@code _listSeparator}, into strings comprising one of
//	 * the listed expressions each.<br/>
//	 * This does not mean mere string splitting but is aware of string literals, argument lists
//	 * of function calls etc. (These must not be broken.)<br/>
//	 * The analysis stops as soon as there is a level underflow (i.e. an unmatched right parenthesis,
//	 * bracket, or the like).<br/>
//	 * The remaining string from the unsatisfied right parenthesis, bracket, or brace on will
//	 * be ignored!
//	 * @param _text - string containing one or more expressions
//	 * @param _listSeparator - a character sequence serving as separator among the expressions (default: ",") 
//	 * @return a StringList, each element of which contains one of the separated expressions (order preserved)
//	 * @see #splitExpressionList(String, String, boolean)
//	 * @see #splitExpressionList(StringList, String, boolean)
//	 */
//	public static StringList splitExpressionList(String _text, String _listSeparator)
//	// START KGU#93 2015-12-21 Bugfix #41/#68/#69
//	{
//		return splitExpressionList(_text, _listSeparator, false);
//	}
//	
//	/**
//	 * Splits the string {@code _text}, which is supposed to represent a list of expressions
//	 * separated by character sequence {@code _listSeparator}, into strings comprising one of
//	 * the listed expressions each.<br/>
//	 * This does not mean mere string splitting but is aware of string literals, argument lists
//	 * of function calls etc. (These must not be broken.)<br/>
//	 * The analysis stops as soon as there is a level underflow (i.e. an unmatched right parenthesis,
//	 * bracket, or the like).<br/>
//	 * The remaining string from the unsatisfied right parenthesis, bracket, or brace on will
//	 * be added as last element to the result if {@code _appendTail} is true - otherwise there is no
//	 * difference to method {@link #splitExpressionList(String, String)}!<br/>
//	 * If the last result element is empty then the expression list was syntactically "clean".
//	 * @param _text - string containing one or more expressions
//	 * @param _listSeparator - a character sequence serving as separator among the expressions (default: ",") 
//	 * @param _appendTail - if the remaining part of _text from the first unaccepted character on is to be added 
//	 * @return a StringList consisting of the separated expressions (and the tail if _appendTail was true).
//	 * @see #splitExpressionList(StringList, String, boolean)
//	 */
//	public static StringList splitExpressionList(String _text, String _listSeparator, boolean _appendTail)
//	// END KGU#93 2015-12-21
//	{
//		//StringList expressionList = new StringList();
//		//if (_listSeparator == null) _listSeparator = ",";
//		
//	// START KGU#388 2017-09-13: New subroutine
//		//StringList tokens = Element.splitLexically(_text, true);
//		return splitExpressionList(Syntax.splitLexically(_text.trim(), true), _listSeparator, _appendTail);
//	}
//	
//	/**
//	 * Splits the token list {@code _tokens}, which is supposed to represent a sequence of expressions
//	 * separated by separators {@code _listSeparator}, into strings comprising one of the listed expressions
//	 * each.<br/>
//	 * This is aware of string literals, argument lists of function calls etc. (These must not be broken.)
//	 * The analysis stops as soon as there is a level underflow (i.e. an unmatched right parenthesis,
//	 * bracket, or the like).<br/>
//	 * The remaining tokens from the unsatisfied right parenthesis, bracket, or brace on will
//	 * be concatenated and added as last element to the result if {@code _appendTail} is true.
//	 * If the last result element is empty in mode {@code _appendTail} then the expression list was syntactically
//	 * "clean".<br/>
//	 * FIXME If the expression was given without some parentheses as delimiters then a tail won't be added.
//	 * @param _tokens - {@link StringList} of tokens representing one or more expressions
//	 * @param _listSeparator - a character sequence serving as separator among the expressions (default: ",") 
//	 * @param _appendTail - if the remaining part of _text from the first unaccepted character on is to be added 
//	 * @return a StringList consisting of the separated expressions (and the tail if _appendTail was true).
//	 * @see #splitExpressionList(String, String, boolean)
//	 */
//	public static StringList splitExpressionList(StringList _tokens, String _listSeparator, boolean _appendTail)
//	{
//
//		StringList expressionList = new StringList();
//		if (_listSeparator == null) _listSeparator = ",";
//	// END KGU#388 2017-09-13
//		int parenthDepth = 0;
//		boolean isWellFormed = true;
//		Stack<String> enclosings = new Stack<String>();
//		int tokenCount = _tokens.count();
//		// START KGU#914 2021-01-22: Bugfix - identifiers were glued in expressions if _tokens is condensed
//		//String currExpr = "";
//		StringList currExpr = new StringList();
//		// END KGU#914 2021-01-22
//		String tail = "";
//		for (int i = 0; isWellFormed && parenthDepth >= 0 && i < tokenCount; i++)
//		{
//			String token = _tokens.get(i);
//			if (token.equals(_listSeparator) && enclosings.isEmpty())
//			{
//				// store the current expression and start a new one
//				// START KGU#914 2021-01-22: Bugfix - see above
//				//expressionList.add(currExpr.trim());
//				//currExpr = new String();
//				expressionList.add(currExpr.trim().concatenate(null));
//				currExpr.clear();
//				// END KGU#914 2021-01-22
//			}
//			else
//			{ 
//				if (token.equals("("))
//				{
//					enclosings.push(")");
//					parenthDepth++;
//				}
//				else if (token.equals("["))
//				{
//					enclosings.push("]");
//					parenthDepth++;
//				}
//				else if (token.equals("{"))
//				{
//					enclosings.push("}");
//					parenthDepth++;
//				}
//				else if ((token.equals(")") || token.equals("]") || token.equals("}")))
//				{
//					isWellFormed = parenthDepth > 0 && token.equals(enclosings.pop());
//					parenthDepth--;
//				}
//				
//				if (isWellFormed)
//				{
//					// START KGU#914 2021-01-22: Bugfix - see above
//					//currExpr += token;
//					currExpr.add(token);
//					// END KGU#914 2021-01-22
//				}
//				else if (_appendTail)
//				{
//					// START KGU#914 2021-01-22: Bugfix - see above
//					//expressionList.add(currExpr.trim());
//					//currExpr = "";
//					//tail = _tokens.concatenate("", i).trim();
//					// START KGU#1061 2022-08-23: Bugfix #1068 an empty list generated a list with empty string
//					//expressionList.add(currExpr.trim().concatenate(null));
//					if (!(currExpr = currExpr.trim()).isEmpty() || !expressionList.isEmpty()) {
//						// There must have been at least one separator - so add even an empty term
//						expressionList.add(currExpr.concatenate(null));
//					}
//					// END KGU#1061 2022-08-23
//					currExpr.clear();
//					tail = _tokens.concatenate(null, i).trim();
//					// END KGU#914 2021-01-22
//				}
//			}
//		}
//		// add the last expression if it's not empty
//		if (!(currExpr = currExpr.trim()).isEmpty())
//		{
//			// START KGU#914 2021-01-22: Bugfix - see above
//			//expressionList.add(currExpr);
//			expressionList.add(currExpr.concatenate(null));
//			// END KGU#914 2021-01-22
//		}
//		// Add the tail if requested. Empty if there is no bad tail
//		if (_appendTail) {
//			expressionList.add(tail);
//		}
//		return expressionList;
//	}
//	// END KGU#101 2015-12-11
	
	// START KGU#388 2017-09-13: Enh. #423; KGU#371 2019-03-07: Enh. #385 - parameter declDefaults added
	/**
	 * Extracts the parameter or component declarations from the parameter list (or
	 * record type definition, respectively) given by {@code declText} and adds their
	 * names and type descriptions to the respective StringList {@code declNames} and
	 * {@code declTypes}.<br/>
	 * CAUTION: Some elements of {@code declTypes} may be {@code null} on return!
	 * 
	 * @param declText - the text of the declaration inside the parentheses or braces
	 * @param declNames - the names of the declared parameters or record components (in
	 *    order of occurrence), or {@code null}
	 * @param declTypes - the types of the declared parameters or record components (in
	 *    order of occurrence), or {@code null}
	 * @param declDefaults - the literals of the declared parameter/component defaults
	 *    (in order of occurrence), or {@code null}
	 */
	protected static void extractDeclarationsFromList(String declText, StringList declNames,
			StringList declTypes, StringList declDefaults) {
		extractDeclarationsFromList(new TokenList(declText), declNames, declTypes, declDefaults);
	}
	/**
	 * Extracts the parameter or component declarations from the tokenized parameter list (or
	 * record type definition, respectively) given by {@code declText} and adds their names
	 * and type descriptions to the respective StringList {@code declNames} and {@code declTypes}.<br/>
	 * CAUTION: Some elements of {@code declTypes} may be {@code null} on return!
	 * 
	 * @param declTokens - the tokenized text of the declaration inside the parentheses or braces
	 * @param declNames - the names of the declared parameters or record components (in order
	 *    of occurrence), or {@code null}
	 * @param declTypes - the types of the declared parameters or record components (in order
	 *    of occurrence), or {@code null}
	 * @param declDefaults - the literals of the declared parameter/component defaults (in order
	 *    of occurrence), or {@code null}
	 */
	protected static void extractDeclarationsFromList(TokenList declTokens, StringList declNames,
			StringList declTypes, StringList declDefaults) {
		// Enh. #385 We have to face e.g. string literals in the argument list (default values)!
		ArrayList<TokenList> declGroups = Syntax.splitExpressionList(declTokens, ";");
		for (int i = 0; i < declGroups.size() - 1; i++)
		{
			// common type for parameter / component group
			TokenList type = null;
			TokenList group = declGroups.get(i);
			// Enh. #385 - cope with default values
			TokenList defltGr = null;
			int posColon = group.indexOf(":");
			if (posColon >= 0)
			{
				type = group.subSequenceToEnd(posColon + 1);
				group = group.subSequence(0, posColon);
				// Enh. #385 - cope with default values
				int posEq = type.indexOf("=");
				if (posEq >= 0) {
					defltGr = type.subSequenceToEnd(posEq+1);
					type.remove(posEq, type.size());
				}
			}
			// There may be several listed variables, must first split by ','
			// Enh. #385 - we must face complex literals here
			ArrayList<TokenList> vars = Syntax.splitExpressionList(group, ",");
			for (int j = 0; j < vars.size() - 1; j++)
			{
				TokenList decl = vars.get(j);
				if (!decl.isBlank())
				{
					String prefix = "";	// Enh. #388, for constants
					// Enh. #385 Get default values
					TokenList deflt = null;
					if (defltGr != null) {
						deflt = new TokenList(defltGr);
					}
					if (type == null && (posColon = decl.indexOf("as", false)) >= 0)
					{
						// Enh. #385 Scan for default / initial values
						type = decl.subSequenceToEnd(posColon + 1);
						decl.remove(posColon, decl.size());
						int posEq = type.indexOf("=");
						if (posEq >= 0) {
							deflt = type.subSequenceToEnd(posEq+1);
							type = type.subSequence(0, posEq);
							// The redundant 'optional' keyword is to be ignored 
							if (!decl.isBlank() && decl.get(0).equalsIgnoreCase("optional")) {
								decl.remove(0);
							}
						}
					}
					//StringList tokens = splitLexically(decl, true);
					if (decl.size() > 1) {
						// Is a C or Java array type involved? 
						if (declGroups.size() == 1 && posColon < 0 || type == null) {
							// START KGU#371 2019-03-07: Enh. #385 Scan for default / initial values
							int posEq = decl.indexOf("=");
							if (posEq >= 0) {
								if (deflt == null) {
									deflt = decl.subSequenceToEnd(posEq + 1);
								}
								decl.remove(posEq, decl.size());
							}						
							// END KGU#371 2019-03-07							
							int posBrack1 = decl.indexOf("[");
							int posBrack2 = decl.lastIndexOf("]");
							TokenList tokens = new TokenList(decl);
							if (posBrack1 > 0 && posBrack2 > posBrack1) {
								// FIXME This is a syntax we don't want to support, actually!
								//String indices = tokens.concatenate(null, posBrack1, posBrack2+1);
								ArrayList<TokenList> indices = Syntax.splitExpressionList(tokens.subSequenceToEnd(posBrack1+1), ",");
								// TODO check this logic! Looks somewhat poor
								if (posBrack2 == tokens.size()-1) {
									// C-style: brackets right of the variable id
									decl.remove(posBrack1, decl.size());
									decl.remove(0, posBrack1-1);
									if (posBrack1 > 1 && type == null) {
										type = tokens.subSequence(0, posBrack1-1);
										type.add("[");
										for (int k = 0; k < indices.size() - 1; k++) {
											if (k > 0) {
												type.add(",");
											}
											type.addAll(indices.get(k));
										}
										type.add("]");
									}
								}
								else {
									// Java style: brackets between element type and variable id
									decl = tokens.subSequenceToEnd(posBrack2+1);
									if (type == null) {
										type = tokens.subSequence(0, posBrack2+1);
									}
								}
							}
							else {
								// No brackets...
								if (decl.get(0).equals("const")) {
									prefix = "const ";
									decl.remove(0);
								}
								// Issue #385 - We shouldn't return an empty string but null if there is no type
								if (decl.size() > 1) {
									// Type could comprise more words, e.g. unsigned int
									type = decl.subSequence(0, decl.size()-1);
								}
								decl.remove(0, decl.size()-1);
							}
						}
						// Enh. #388 - Care for constant declarations
						else if (decl.get(0).equals("const")) {
							decl.remove(0);
							prefix = "const ";
						}
					}
					//System.out.println("Adding parameter: " + vars.get(j).trim());
					if (declNames != null) declNames.add(decl.getString().trim());
					// Enh. #388 Care for constant declarations
					if (declTypes != null){
						if (!prefix.isEmpty() || type != null) {
							declTypes.add(prefix + type.getString().trim());
						}
						else {
							// type must be null but we cannot add it directly, because null is not a String
							String dummy = null;
							declTypes.add(dummy);
						}
					}
					// Enh. #385 gather the possible default values
					if (declDefaults != null) {
						if (deflt != null) {
							declDefaults.add(deflt.getString().trim());
						}
						else {
							// default must be null but we cannot add it directly, because null is not a String
							String dummy = null;
							declDefaults.add(dummy);
						}
					}
				}
			}
		}
	}

	/**
	 * Decomposes the interior of a record initializer of one of the forms
	 * <ul>
	 * <li> "[typename]{compname1: value1, compname2: value2, ...}"</li>
	 * <li> "[typename]{value1, value2, ...}"</li>
	 * </ul>
	 * into a hash table mapping the component names to the corresponding value
	 * strings.<br/>
	 * If there is text following the closing brace it will be mapped to key
	 * "§TAIL§". If {@code typename} is given then it will be mapped to key
	 * "§TYPENAME§".<br/>
	 * If {@code _typeInfo} is given and either {@code typename} was omitted or
	 * matches the name of {@code _typeInfo} then unprefixed component values
	 * will be associated to the component names of the type in order of
	 * occurrence unless an explicit component name prefix occurs.<br/>
	 * If {@code _typeInfo} is {@code null} then generic pseudo component
	 * names of form {@code "FIXME_<typename>_<i>"} may be provided for
	 * components with missing names in the {@code _text}.
	 * 
	 * @param _text - the initializer expression with or without {@code typename}
	 *    but with braces.
	 * @param _typeInfo - the type map entry for the corresponding record type
	 *    if available.
	 * @return the component map (or null if there are no braces).
	 * 
	 * @deprecated prefer {@link Syntax#splitRecordInitializer(TokenList, TypeMapEntry)}
	 */
	public static HashMap<String, String> splitRecordInitializer(String _text, TypeMapEntry _typeInfo)
	{
		return Syntax.splitRecordInitializer(new TokenList(_text), _typeInfo);
	}
//	/**
//	 * Decomposes the interior of a record initializer of the form<br/>
//	 * [typename]{compname1: value1, compname2: value2, ...}<br/>
//	 * into a hash table mapping the component names to the corresponding value
//	 * strings.<br/>
//	 * If there is text following the closing brace it will be mapped to key "§TAIL§".
//	 * If the typename is given then it will be provided mapped to key "§TYPENAME§".
//	 * If {@code _typeInfo} is given and either {@code typename} was omitted or matches
//	 * name of {@code _typeInfo} then unprefixed component values will be associated
//	 * to the component names of the type in order of occurrence unless an explicit
//	 * component name prefix occurs.<br/>
//	 * If {@code _typeInfo} is null and {@code generateDummyCompNames} is {@code true}
//	 * then generic component names of form {@code "FIXME_<typename>_<i>"} may be
//	 * provided for components with missing names in the {@code _text}.
//	 * 
//	 * @param _tokens - the tokenized initializer expression with or without typename
//	 *    but with braces.
//	 * @param _typeInfo - the type map entry for the corresponding record type if available
//	 * @return the component map (or null if there are no braces).
//	 */
//	public static HashMap<String, String> splitRecordInitializer(TokenList _tokens, TypeMapEntry _typeInfo)
//	{
//		// START KGU#526 2018-08-01: Enh. #423 - effort to make the component order more stable (at higher costs, though)
//		//HashMap<String, String> components = new HashMap<String, String>();
//		HashMap<String, String> components = new LinkedHashMap<String, String>();
//		// END KGU#526 2018-08-01
//		int posBrace = _tokens.indexOf("{");
//		if (posBrace < 0) {
//			return null;
//		}
//		String typename = _tokens.subSequence(0, posBrace).getString().trim();
//		if (!typename.isEmpty()) {
//			components.put("§TYPENAME§", typename);
//		}
//		ArrayList<TokenList> parts = Syntax.splitExpressionList(_tokens.subSequenceToEnd(posBrace+1), ",");
//		TokenList tail = parts.get(parts.size()-1);
//		if (!tail.startsWith("}")) {
//			return null;
//		}
//		else if (tail.size() > 1) {
//			components.put("§TAIL§", tail.subSequence(1, tail.size()).getString().trim());
//		}
//		// START KGU#559 2018-07-20: Enh. #563 In case of a given type, we may guess the target fields
//		boolean guessComponents = _typeInfo != null && _typeInfo.isRecord()
//				&& (typename.isEmpty() || typename.equals(_typeInfo.typeName));
//		String[] compNames = null;
//		if (guessComponents) {
//			Set<String> keys = _typeInfo.getComponentInfo(true).keySet();
//			compNames = keys.toArray(new String[keys.size()]);
//		}
//		// END KGU#559 2018-07-20
//		for (int i = 0; i < parts.size()-1; i++) {
//			TokenList tokens = parts.get(i);
//			int posColon = tokens.indexOf(":");
//			if (posColon >= 0) {
//				String name = tokens.subSequence(0, posColon).getString().trim();
//				String expr = tokens.subSequenceToEnd(posColon + 1).getString().trim();
//				if (Syntax.isIdentifier(name, false, null)) {
//					components.put(name, expr);
//					// START KGU#559 2018-07-20: Enh. #563 Stop associating from type as soon as an explicit name is given
//					guessComponents = false;
//					// END KGU#559 2018-07-20
//				}
//			}
//			// START KGU#559 2018-07-20: Enh. #563
//			else if (guessComponents && i < compNames.length) {
//				components.put(compNames[i], parts.get(i).getString());
//			}
//			// END KGU#559 2018-07-20
//			// START KGU#711 2019-11-24: Bugfix #783 workaround for missing type info
//			else if (compNames == null && !typename.isEmpty()) {
//				components.put("FIXME_" + typename + "_" + i, parts.get(i).getString());
//			}
//			// END KGU#711 2019-11-24
//		}
//		return components;
//	}
//	// END KGU#388 2017-09-13

	
//	// START KGU#1057 2022-08-20: Enh. #1066 Interactive input assistent
//	/**
//	 * Analyses the token list {@code tokens} preceding a dot in backwards direction for
//	 * record structure information.<br/>
//	 * If the pretext describes an object with record structure then returns the list
//	 * of component names.
//	 * 
//	 * @param tokens - the lexically split line content up to (but not including) a dot
//	 * @param typeMap - the current mapping of variables and type names to type info
//	 * @param firstSeen - must either be {@code null} or an int array with at least one
//	 *     element, at position 0 of which the index of the first token that contributed
//	 *     to the analysis will be placed.
//	 * @return either a list of component names or {@code null}
//	 */
//	public static ArrayList<String> retrieveComponentNames(
//			StringList tokens,
//			HashMap<String, TypeMapEntry> typeMap,
//			int[] firstSeen) {
//		ArrayList<String> proposals = null;
//		tokens.removeAll(" ");
//		// Go as far backward as we can go to find the base variable
//		// We will not go beyond a function call, so what may precede is an id or ']'
//		StringList path = new StringList();
//		int ix = tokens.count() -1;
//		while (path != null && ix >= 0) {
//			String prevToken = tokens.get(ix);
//			// There might be several index expressions
//			while (path != null && prevToken.equals("]")) {
//				// We will have to find the corresponding opening bracket
//				int ixClose = ix;
//				int level = 1;
//				ix--;
//				while (level > 0 && ix >= 0) {
//					prevToken = tokens.get(ix);
//					if (prevToken.equals("]")) {
//						level++;
//					}
//					else if (prevToken.equals("[")) {
//						level--;
//					}
//					ix--;
//					/* If more than one index expression is listed here,
//					 * then we will find out via expression analysis below
//					 */
//				}
//				if (level > 0) {
//					path = null;
//				}
//				else {
//					// Now find out how many indices are given between the brackets
//					StringList indexExprs = Element.splitExpressionList(
//							tokens.subSequence(ix + 2, ixClose + 1), ",", false);
//					// Add as many bracket pairs to the path
//					for (int i = 0; i < indexExprs.count(); i++) {
//						path.add("[]");
//					}
//					prevToken = tokens.get(ix);
//				}
//			}
//			if (path != null && Syntax.isIdentifier(prevToken, true, null)) {
//				path.add(prevToken);
//				ix--;
//				if (ix > 0 && tokens.get(ix).equals(".")) {
//					ix--; // Continue path collection
//				}
//				else {
//					break;	// Stop analysis, path may be valid
//				}
//			}
//			else {
//				path = null;
//			}
//		}
//		if (path != null && path.count() >= 1) {
//			// Now we may have a reverse valid access path
//			path = path.reverse();
//			TypeMapEntry varType = typeMap.get(path.get(0));
//			path.remove(0);
//			while (varType != null && !path.isEmpty()) {
//				if (varType.isArray() && path.get(0).equals("[]")) {
//					String typeStr = varType.getCanonicalType(true, true);
//					while (typeStr.startsWith("@") && !path.isEmpty()
//							&& path.get(0).equals("[]")) {
//						typeStr = typeStr.substring(1);
//						path.remove(0);
//					}
//					varType = typeMap.get(":" + typeStr);
//				}
//				if (varType != null && varType.isRecord()) {
//					if (!path.isEmpty()) {
//						LinkedHashMap<String,TypeMapEntry> compInfo = varType.getComponentInfo(true);
//						varType = compInfo.get(path.get(0));
//						path.remove(0);
//					}
//				}
//				// START KGU#1115 2024-03-21: Bugfix #1128: We must not get trapped in the loop
//				if (varType != null && !varType.isArray() && !varType.isRecord()) {
//					varType = null;
//				}
//				// END KGU#1115 2024-03-21
//			}
//			if (varType != null && varType.isRecord()) {
//				// path must now be exhausted, the component names are our proposals
//				LinkedHashMap<String,TypeMapEntry> compInfo = varType.getComponentInfo(true);
//				proposals = new ArrayList<String>();
//				proposals.addAll(compInfo.keySet());
//			}
//		}
//		if (firstSeen != null && firstSeen.length > 0) {
//			firstSeen[0] = ix + 1;
//		}
//		return proposals;
//	}
//	// END KGU#1057 2022-08-20
	
	// START KGU#63 2015-11-03: getWidthOutVariables and writeOutVariables were nearly identical (and had to be!)
	// Now it's two wrappers and a common algorithm -> ought to avoid duplicate work and prevents from divergence
	/**
	 * Computes the expected width of text line {@code _text} of Element
	 * {@code _this} on the given {@code _canvas} (with its current font), where
	 * mode {@link #E_VARHIGHLIGHT} is considered.
	 * @param _canvas - the target {@link Canvas}
	 * @param _text - a line of element text
	 * @param _this - the Element this retrieval is done for
	 * @return the projected text width in pixel
	 */
	public static int getWidthOutVariables(Canvas _canvas, String _text, Element _this)
	{
		return writeOutVariables(_canvas, 0, 0, _text, _this, false, false);
	}

	/**
	 * Actually writes the of text line {@code _text} of Element {@code _this}
	 * to the given {@code _canvas} (with its current font), where mode
	 * {@link #E_VARHIGHLIGHT} is considered.
	 * 
	 * @param _canvas - the target {@link Canvas}
	 * @param _x - the horizontal start position
	 * @param _y - the vertical start position
	 * @param _text - the text line to be drawn
	 * @param _this - the responsible Element
	 * @param _inContention - a flag indication a possible event queue contention,
	 *    in which case the drawing ought to be simplified (e.g. by suppressing
	 *    syntax highlighting)
	 */
	public static void writeOutVariables(Canvas _canvas, int _x, int _y, String _text, Element _this, boolean _inContention)
	{
		writeOutVariables(_canvas, _x, _y, _text, _this, true, _inContention);
	}
	
	private static int writeOutVariables(Canvas _canvas, int _x, int _y, String _text, Element _this, boolean _actuallyDraw, boolean _inContention)
	// END KGU#63 2015-11-03
	{
		// init total (the result, telling the width in pixels)
		int total = 0;
		Root root = getRoot(_this);
		
		if (root != null)
		{
			// START KGU#226 2016-07-29: Issue #211: No syntax highlighting in comments
			//if (root.hightlightVars==true)
			// START KGU#502/KGU#524/KGU#553 2019-03-14: Bugfix #518,#544,#557 - No syntax highlighting in high contention
			//if (Element.E_VARHIGHLIGHT && !root.isSwitchTextCommentMode())
			if (Element.E_VARHIGHLIGHT && !isSwitchTextCommentMode() && !_inContention)
			// END KGU#502/KGU#524/KGU#553 2019-03-14
			// END KGU#226 2016-07-29
			{
				// START KGU#701 2019-03-29: Issue #718 - highlighting acceleration by caching
				// backup the original font
				Font backupFont = _canvas.getFont();
				// START KGU#902 2020-12-31: Issue #903 - method decomposed
				Vector<HighlightUnit> hlUnits = getHighlightUnits(_text, _this, root);
				// END KGU#902 2020-12-31
				// This is now the pure drawing
				synchronized (hlUnits) {
					for (HighlightUnit unit: hlUnits) {
						// START KGU#707 2019-05-15: Bugfix #724 special font properties of the canvas weren't used anymore
						// (This workaround will still only affect the standard font and have no impact on derived fonts)
						//_canvas.setFont(unit.bold ? boldFont : (unit.underlined ? underlinedFont : font));
						_canvas.setFont(unit.bold ? boldFont : (unit.underlined ? underlinedFont : backupFont));
						// END KGU#707 2019-05-15
						_canvas.setColor(unit.textColor);
						if (_actuallyDraw)
						{
							// write out text
							_canvas.writeOut(_x + total, _y, unit.textSnippet);
						}

						// add to the total
						total += _canvas.stringWidth(unit.textSnippet);
					}
				}
				// reset color
				_canvas.setColor(Color.BLACK);
				// reset font
				_canvas.setFont(backupFont);
				// END KGU#701 2019-03-29
			}
			else
			{
				_text = Syntax.decodeLine(new TokenList(_text)).getString();
				if (_actuallyDraw)
				{
					_canvas.writeOut(_x + total, _y, _text);
				}

				// add to the total
				total += _canvas.stringWidth(_text);

			}
		}
		
		return total;
	}
	
	// START KGU#902 2020-12-31: Enh. #903 Now the comment popup may contain highlighted text
	/**
	 * Composes an HTML string of the element comment or text (depending on
	 * {@link #isSwitchTextCommentMode()}) to be presented in the "show comment"
	 * popup.
	 * @param sb - a {@link StringBuilder} to append the HTML text parts to
	 * @return the {@link StringList} of the actual (not HTML) text lines to be
	 * presented.
	 */
	public StringList appendHtmlComment(StringBuilder sb)
	{
		sb.append("<html>");
		StringList lines = this.getComment(false);
		if (isSwitchTextCommentMode()) {
			if (Element.E_APPLY_ALIASES) {
				lines = StringList.explode(Element.replaceControllerAliases(lines.getText(), true, Element.E_VARHIGHLIGHT), "\n");
			}
			Root myRoot = getRoot(this);
			for (int i = 0; i < lines.count(); i++) {
				if (i > 0) {
					sb.append("<br/>");
				}
				appendLineToHtml(sb, lines.get(i), myRoot);
			}
		}
		else {
			// START KGU#199 2016-07-07: Enh. #188 - we must cope with combined comments now
			//StringList comment = selEle.getComment(false);
			lines = StringList.explode(lines, "\n");
			lines.removeAll("");	// Don't include empty lines here
			// END KGU#199 2016-07-07
			sb.append(BString.encodeToHtml(lines.getText()).replace("\n", "<br/>"));
		}
		sb.append("</html>");
		return lines;
	}
	
	/**
	 * Appends an HTML equivalent for the given {@code line} of text to the
	 * content of the {@link StringBuilder} {@code sb}.
	 * @param sb - a {@link StringBuilder} gathering the HTML text
	 * @param line - a line of text
	 * @param myRoot - the owning {@link Root}
	 */
	private void appendLineToHtml(StringBuilder sb, String line, Root myRoot) {
		if (Element.E_VARHIGHLIGHT) {
			Vector<HighlightUnit> hlUnits = getHighlightUnits(line, this, myRoot);
			for (HighlightUnit unit: hlUnits) {
				boolean hasColor = unit.textColor != null && !unit.textColor.equals(Color.BLACK);
				boolean hasSpan = hasColor || unit.underlined;
				boolean isStrong = unit.bold;
				if (hasSpan) {
					sb.append("<span style=\"");
					if (hasColor) {
						sb.append("color: #");
						sb.append(Integer.toHexString(unit.textColor.getRGB()).substring(2));
						sb.append(";");
					}
					if (unit.underlined) {
						sb.append("text-decoration: underline;");
					}
					sb.append("\">");
				}
				if (isStrong) {
					sb.append("<strong>");
				}
				sb.append(BString.encodeToHtml(unit.textSnippet));
				if (isStrong) {
					sb.append("</strong>");
				}
				if (hasSpan) {
					sb.append("</span>");
				}
			}
		}
		else {
			sb.append(BString.encodeToHtml(line));
		}
	}
	
	private static final StringList ioKeys = StringList.explode("input,output", ",");
	private static final StringList jpKeys = StringList.explode("preLeave,preReturn,preExit,preThrow", ",");
	/**
	 * Retrieves or creates the highlight units for the text of Element {@code _elem} from/in
	 * the {@link #highlightCache}
	 * @param _text - the interesting line of the element text
	 * @param _elem - the responsible Element
	 * @param _root - the owing {@link Root}
	 * @return the vector of cached highlight units for this line of text to be used for drawing
	 */
	private static Vector<HighlightUnit> getHighlightUnits(String _text, Element _elem, Root _root) {
		Vector<HighlightUnit> hlUnits = _elem.highlightCache.get(_text);
		if (hlUnits == null) {
			_elem.highlightCache.put(_text, hlUnits = new Vector<HighlightUnit>());
			// START KGU#686 2019-03-16: Enh. #56
			Set<String> variableSet = _elem.getVariableSetFor(_elem);
			// END KGU#686 2019-03-16

			TokenList parts = new TokenList(_text, true);

			// START KGU#701 2019-03-29: Issue #718 Derived fonts now cached in static fields
			//Font boldFont = new Font(Element.font.getName(), Font.BOLD, Element.font.getSize());
			// START KGU#480 2018-01-21: Enh. #490 - we will underline alias names
			//Map<TextAttribute, Integer> fontAttributes = new HashMap<TextAttribute, Integer>();
			//fontAttributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
			//Font underlinedFont = new Font(Element.font.getName(), Font.PLAIN, Element.font.getSize()).deriveFont(fontAttributes);
			// END KGU#480 2018-01-21
			//Font backupFont = _canvas.getFont();
			// END KGU#701 2019-03-29

			// START KGU#64 2015-11-03: Not to be done again and again. Private static field now!
			//StringList specialSigns = new StringList();
			if (specialSigns == null)	// lazy initialisation
			{
				// START KGU#843 2020-04-12: Bugfix #847
				specialSigns = new HashSet<String>();
				// START KGU#843 2020-04-12: Bugfix #847
			// END KGU#64 2015-11-03
				// START KGU#425 2017-09-29: Add the possible ellipses, too
				specialSigns.add("...");
				specialSigns.add("..");					
				// END KGU#425 2017-09-29
				specialSigns.add(".");
				specialSigns.add("[");
				specialSigns.add("]");
				specialSigns.add("\u2190");
				specialSigns.add(":=");
				// START KGU#332 2017-01-27: Enh. #306 "dim" as declaration keyword
				specialSigns.add(":");
				// END KGU#332 2017-01-27
				// START KGU#408 2021-02-24: Enh. #410 Since the " ? : " is executable, it should be highlighted
				specialSigns.add("?");
				// END KGU#408 2021-02-24

				specialSigns.add("+");
				specialSigns.add("/");
				// START KGU 2015-11-03: This operator had been missing
				specialSigns.add("%");
				// END KGU 2015-11-03
				specialSigns.add("*");
				specialSigns.add("-");
				specialSigns.add("var");
				// START KGU#332 2017-01-27: Enh. #306 "dim" as declaration keyword
				specialSigns.add("dim");
				// END KGU#332 2017-01-27
				// START KGU#375 2017-03-30: Enh. #388 "const" as declaration keyword
				specialSigns.add("const");
				// END KGU#375 2017-03-30
				// START KGU#388 2017-09-13: Enh. #423 "type", "record", and "struct" as type definition keywords
				specialSigns.add("type");
				specialSigns.add("record");
				specialSigns.add("struct");
				// END KGU#388 2017-09-13
				// START KGU#542 2019-11-17: Enh. #739 "enum" added to type definition keywords
				specialSigns.add("enum");
				// END KGU#542 2019-11-17
				// START KGU#331 2017-01-13: Enh. #333
				//specialSigns.add("<=");
				//specialSigns.add(">=");
				//specialSigns.add("<>");
				//specialSigns.add("!=");
				specialSigns.add("\u2260");
				specialSigns.add("\u2264");
				specialSigns.add("\u2265");
				// END KGU#331 2017-01-13
				specialSigns.add("<<");
				specialSigns.add(">>");
				specialSigns.add("<");
				specialSigns.add(">");
				specialSigns.add("==");
				specialSigns.add("=");
				specialSigns.add("!");
				// START KGU#24 2014-10-18
				specialSigns.add("&&");
				specialSigns.add("||");
				// END KGU#24 2014-10-18
				// START KGU#109 2016-01-15: Issues #61, #107 highlight the BASIC declarator keyword, too
				specialSigns.add("as");
				// END KGU#109 2016-01-15

				// START KGU#100 2016-01-16: Enh. #84: Also highlight the initialiser delimiters
				specialSigns.add("{");
				specialSigns.add("}");
				// END KGU#100 2016-01-16

				// The quotes will only occur as tokens if they are unpaired!
				specialSigns.add("'");
				specialSigns.add("\"");
				// START KGU#64 2015-11-03: See above
				
				// START KGU#872 2020-10-17: Enh. #872 operator symbols for C style
				specialSigns.add("!=");
				specialSigns.add("<=");
				specialSigns.add(">=");
				// END KGU#872 2020-10-17
				// START KGU#920 2021-02-03: Issue #920 Infinity now also literal
				specialSigns.add("Infinity");
				specialSigns.add("\u221E");
				// END KGU#920 2021-02-03
				// START KGU#883 2020-11-01: Enh. #881 bit operators and Boolean literal were missing
				specialSigns.add("false");
				specialSigns.add("true");
				specialSigns.add("&");
				specialSigns.add("|");
				specialSigns.add("^");
				specialSigns.add("~");
				// END KGU#883 2020-11-01
			}
			// START KGU#611/KGU843 2020-04-12: Issue #643, bugfix #847
			if (specialSignsCi == null) {
				specialSignsCi = new StringList();
				specialSignsCi.add("mod");
				specialSignsCi.add("div");
				// START KGU#24 2014-10-18
				specialSignsCi.add("and");
				specialSignsCi.add("or");
				specialSignsCi.add("xor");
				specialSignsCi.add("not");
				// END KGU#24 2014-10-18
				// START KGU#115 2015-12-23: Issue #74 - These Pascal operators hadn't been supported
				specialSignsCi.add("shl");
				specialSignsCi.add("shr");
				// END KGU#115 2015-12-23
			}
			// END KGU#611/KGU#843 2020-04-12
			// END KGU#64 2015-11-03

			// START KGU#1018 2021-11-17: Bugfix #1021 We must consider composed keywords
			// These markers might have changed by configuration, so don't cache them
			//StringList ioSigns = new StringList();
			//ioSigns.add(Syntax.getKeywordOrDefault("input", "").trim());
			//ioSigns.add(Syntax.getKeywordOrDefault("output", "").trim());
			// START KGU#116 2015-12-23: Enh. #75 - highlight jump keywords
			//StringList jumpSigns = new StringList();
			//jumpSigns.add(Syntax.getKeywordOrDefault("preLeave", "leave").trim());
			//jumpSigns.add(Syntax.getKeywordOrDefault("preReturn", "return").trim());
			//jumpSigns.add(Syntax.getKeywordOrDefault("preExit", "exit").trim());
			// START KGU#686 2019-03-18: Enh. #56
			//jumpSigns.add(Syntax.getKeywordOrDefault("preThrow", "throw").trim());
			// END KGU#686 2019-03-18
			// END KGU#116 2015-12-23
			// START KGU#1097 2023-11-16: Issue #800 New internal keyword representation
//			String ioSign = null;
//			for (String ioKey: new String[] {"input", "output"}) {
//				TokenList splitKey = new TokenList(Syntax.getKeywordOrDefault(ioKey, ioKey), false);
//				// START KGU#1031 2022-05-31: Bugfix #1037
//				//if (parts.indexOf(splitKey, 0, Syntax.ignoreCase) == 0) {
//				if (parts.indexOf(splitKey, 0, !Syntax.ignoreCase) == 0) {
//				// END KGU#1031 2022-05-31
//					ioSign = parts.subSequence(0, splitKey.size()).getString();
//					parts.remove(1, splitKey.size());
//					parts.set(0, ioSign);
//					break;
//				}
//			}
//			String jumpSign = null;
//			if (ioSign == null && !parts.isEmpty()) {
//				for (String jumpKey: new String[] {"preLeave", "preReturn", "preExit", "preThrow"}) {
//					//TokenList splitKey = new TokenList(Syntax.getKeywordOrDefault(jumpKey, jumpKey.substring(3).toLowerCase()), false);
//					//if (parts.indexOf(splitKey, 0, Syntax.ignoreCase) == 0) {
//					//	jumpSign = parts.subSequence(0, splitKey.size()).getString();
//					//	parts.remove(1, splitKey.size());
//					//	parts.set(0, jumpSign);
//					//	break;
//					//}
//					if (parts.get(0).equals(Syntax.key2token(jumpKey))) {
//						parts.set(0, jumpSign = Syntax.getKeywordOrDefault(jumpKey, jumpKey.substring(3).toLowerCase()));
//					}
//				}
//			}
			// END KGU#1097 2023-11-16
			// END KGU#1018 2021-11-17

			// START KGU#377 2017-03-30: Bugfix #333
			parts.replaceAll("<-","\u2190");
			if (E_SHOW_UNICODE_OPERATORS) {
				parts.replaceAll("<>","\u2260");
				parts.replaceAll("!=","\u2260");
				parts.replaceAll("<=","\u2264");
				parts.replaceAll(">=","\u2265");
			}
			// END KGU#377 2017-03-30
			// START KGU#872 2020-10-17: Enh. #872 - show operators in C style
			if (E_SHOW_C_OPERATORS) {
				// Don't replace '=' in type definitions and optional parameters!
				// START KGU#872 2021-01-01: Issue #872 - we must suppress it with routine headers, too
				//if (!(_elem instanceof Instruction && ((Instruction)_elem).isTypeDefinition())) {
				if (!(_elem instanceof Instruction && ((Instruction)_elem).isTypeDefinition()
						|| _elem instanceof Root && ((Root)_elem).isSubroutine())) {
				// END KGU#872 2021-01-01
					parts.replaceAll("=", "==");
				}
				parts.replaceAll("\u2190", "=");
				parts.replaceAll(":=", "=");
				parts.replaceAll("\u2260", "!=");
				parts.replaceAll("<>", "!=");
				parts.replaceAll("\u2264", "<=");
				parts.replaceAll("\u2265", ">=");
				parts.replaceAll("not", "!", false);
				parts.replaceAll("and", "&&", false);
				parts.replaceAll("or", "||", false);
				parts.replaceAll("xor", "^", false);
				parts.replaceAll("div", "/", false);
				parts.replaceAll("mod", "%", false);
				parts.replaceAll("shl", "<<", false);
				parts.replaceAll("shr", ">>", false);
			}
			// END KGU#872 2020-10-17

			// START KGU#701 2019-03-29: Issue #718 concatenate normal text parts
			StringBuilder normalText = new StringBuilder();
			boolean lastWasNormal = false;
			// END KGU#701 2019-03-29
			synchronized (hlUnits) {
				for (int i = 0; i < parts.size(); i++)
				{
					// START KGU#790 2023-11-05: In TokenLists, the space is to be handled separately
					int gap = parts.getPadding(i)[0];
					if (gap > 0) {
						normalText.append(" ".repeat(gap));
						lastWasNormal = true;
					}
					// END KGU#790 2023-11-05
					
					String display = parts.get(i);

					if (!display.equals(""))
					{
						// START KGU#1097 2023-11-20: Issue #800
						String key = null;
						// END KGU#1097 2023-11-20
						// if this part has to be colored
						if (variableSet.contains(display))
						{
							// dark blue, bold
							// START KGU#701 2019-03-29: Issue #718
							//_canvas.setColor(E_HL_VARIABLE_COLOR);
							//_canvas.setFont(boldFont);
							if (lastWasNormal) {
								hlUnits.add(_elem.makeHighlightUnit(normalText.toString()));
								normalText.delete(0, Integer.MAX_VALUE);
								lastWasNormal = false;
							}
							hlUnits.add(_elem.makeHighlightUnit(display, E_HL_VARIABLE_COLOR, true, false));
							// END KGU#701 2019-03-29
						}
						// START KGU#388 2017-09-17: Enh. #423 Highlighting of defined types
						// START KGU#790 2022-04-25: Issue #800
						//else if (_root.getTypeInfo().containsKey(":" + display) || TypeMapEntry.isStandardType(display)) {
						else if (_root.getDataTypes().getType(display) != null) {
						// END KGU#790 2022-04-25
							// black, bold
							// START KGU#701 2019-03-29: Issue #718
							//_canvas.setFont(boldFont);
							if (lastWasNormal) {
								hlUnits.add(_elem.makeHighlightUnit(normalText.toString()));
								normalText.delete(0, Integer.MAX_VALUE);
								lastWasNormal = false;
							}
							hlUnits.add(_elem.makeHighlightUnit(display, Color.BLACK, true, false));
							// END KGU#701 2019-03-29
						}
						// END KGU#388 2017-09-17
						// if this part has to be coloured with special colour
						// START KGU#611/KGU#843 2020-04-12: Issue #643, bugfix #847
						//else if(specialSigns.contains(display))
						else if(specialSigns.contains(display) || specialSignsCi.contains(display, false))
						// END KGU#611/KGU#843 2020-04-12
						{
							// burgundy, bold
							// START KGU#701 2019-03-29: Issue #718
							//_canvas.setColor(E_HL_OPERATOR_COLOR);
							//_canvas.setFont(boldFont);
							if (lastWasNormal) {
								hlUnits.add(_elem.makeHighlightUnit(normalText.toString()));
								normalText.delete(0, Integer.MAX_VALUE);
								lastWasNormal = false;
							}
							hlUnits.add(_elem.makeHighlightUnit(display, E_HL_OPERATOR_COLOR, true, false));
							// END KGU#701 2019-03-29
						}
						// START KGU#1097 2023-11-20: Issue #800 New internal representation
//						// if this part has to be coloured with io colour
//						// START KGU#165 2016-03-25: consider the new option
//						//else if(ioSigns.contains(display))
//						// START KGU#1018 2021-11-17: Bugfix #1021
//						//else if(ioSigns.contains(display, !Syntax.ignoreCase))
//						else if (i == 0 && display.equals(ioSign))
//						// END KGU#1018 2021-11-17
//						// END KGU#165 2016-03-25
//						{
//							// green, bold
//							// START KGU#701 2019-03-29: Issue #718
//							//_canvas.setColor(E_HL_INOUT_COLOR);
//							//_canvas.setFont(boldFont);
//							if (lastWasNormal) {
//								hlUnits.add(_elem.makeHighlightUnit(normalText.toString()));
//								normalText.delete(0, Integer.MAX_VALUE);
//								lastWasNormal = false;
//							}
//							hlUnits.add(_elem.makeHighlightUnit(display, E_HL_INOUT_COLOR, true, false));
//							// END KGU#701 2019-03-29
//						}
//						// START KGU 2015-11-12
//						// START KGU#116 2015-12-23: Enh. #75
//						// START KGU#165 2016-03-25: consider the new case option
//						//else if(jumpSigns.contains(display))
//						// START KGU#1018 2021-11-17: Bugfix #1021
//						//else if(jumpSigns.contains(display, !Syntax.ignoreCase))
//						else if (i == 0 && display.equals(jumpSign))
//						// END KGU#1018 2021-11-17
//						// END KGU#165 2016-03-25
//						{
//							// orange, bold
//							// START KGU#701 2019-03-29: Issue #718
//							//_canvas.setColor(E_HL_JUMP_COLOR);
//							//_canvas.setFont(boldFont);
//							if (lastWasNormal) {
//								hlUnits.add(_elem.makeHighlightUnit(normalText.toString()));
//								normalText.delete(0, Integer.MAX_VALUE);
//								lastWasNormal = false;
//							}
//							hlUnits.add(_elem.makeHighlightUnit(display, E_HL_JUMP_COLOR, true, false));
//							// END KGU#701 2019-03-29
//						}
//						// END KGU#116 2015-12-23
						else if ((key = Syntax.token2key(display)) != null) {
							display = Syntax.getKeywordOrDefault(key, "");
							if (lastWasNormal) {
								hlUnits.add(_elem.makeHighlightUnit(normalText.toString()));
								normalText.delete(0, Integer.MAX_VALUE);
								lastWasNormal = false;
							}
							if (i == 0 && ioKeys.contains(key)) {
								hlUnits.add(_elem.makeHighlightUnit(display, E_HL_INOUT_COLOR, true, false));
							}
							else if (i == 0 && jpKeys.contains(key)) {
								hlUnits.add(_elem.makeHighlightUnit(display, E_HL_JUMP_COLOR, true, false));
							}
							else {
								// Remaining keywords are not specifically highlighted
								normalText.append(display);
								lastWasNormal = true;
							}
						}
						// END KGU#1097 2023-11-20
						// if it's a String or Character literal then mark it as such
						else if (display.startsWith("\"") && display.endsWith("\"") ||
								display.startsWith("'") && display.endsWith("'"))
						{
							// violet, plain
							// START KGU#701 2019-03-29: Issue #718
							//_canvas.setColor(E_HL_STRING_COLOR);
							if (lastWasNormal) {
								hlUnits.add(_elem.makeHighlightUnit(normalText.toString()));
								normalText.delete(0, Integer.MAX_VALUE);
								lastWasNormal = false;
							}
							hlUnits.add(_elem.makeHighlightUnit(display, E_HL_STRING_COLOR, false, false));
							// END KGU#701 2019-03-29
						}
						// END KGU 2015-11-12
						// START KGU#480 2018-01-21: Enh. #490 DiagramController routine aliases?
						else if (E_APPLY_ALIASES && Syntax.isIdentifier(display, false, "#")) {
							// START KGU#903 2021-01-01: Bugfix #904
							boolean wasHandled = false;
							// END KGU#903 3021-01-01
							int j = i;
							while (j < parts.size() && parts.get(++j).trim().isEmpty());
							if (j < parts.size() && parts.get(j).equals("(")) {
								if (Element.controllerAlias2Name.containsKey(display.toLowerCase())) {
									// Replace the name and show it underlined
									// START KGU#701 2019-03-29: Issue #718
									//display = display.substring(0, display.indexOf('#'));
									//_canvas.setFont(underlinedFont);
									if (lastWasNormal) {
										hlUnits.add(_elem.makeHighlightUnit(normalText.toString()));
										normalText.delete(0, Integer.MAX_VALUE);
										lastWasNormal = false;
									}
									hlUnits.add(_elem.makeHighlightUnit(display.substring(0, display.indexOf('#')),
											Color.BLACK, false, true));
									// END KGU#701 2019-03-29
									// START KGU#903 2021-01-01: Bugfix #904
									wasHandled = true;
									// END KGU#903 3021-01-01
								}
							}
							// START KGU#903 2021-01-01: Bugfix #904
							if (!wasHandled) {
								normalText.append(display);
								lastWasNormal = true;
							}
							// END KGU#903 3021-01-01
						}
						// START KGU#701 2019-03-29: Issue #718
						else {
							normalText.append(display);
							lastWasNormal = true;
						}
						// END KGU#701 2019-03-29
						// END KGU#480 2018-01-21
					}

					// START KGU#701 2019-03-29: Issue #718
					//if (_actuallyDraw)
					//{
					//	// write out text
					//	_canvas.writeOut(_x + total, _y, display);
					//}
					//
					//// add to the total
					//total += _canvas.stringWidth(display);
					//
					//// reset color
					//_canvas.setColor(Color.BLACK);
					//// reset font
					//_canvas.setFont(backupFont);
					// END KGU#701 2019-03-29

				} //for(int i = 0; i < parts.count(); i++)

				if (lastWasNormal) {
					hlUnits.add(_elem.makeHighlightUnit(normalText.toString()));
				}
			}
		}
		return hlUnits;
	}
	// END KGU#902 2020-12-31
	
	// START KGU#701 2019-03-29: Issue #718 - approach to accelerate syntax highlighting
	private HighlightUnit makeHighlightUnit(String string)
	{
		return new HighlightUnit(string, getTextColor(), false, false);
	}
	private HighlightUnit makeHighlightUnit(String string, Color color, boolean bold, boolean underlined) {
		return new HighlightUnit(string, color, bold, underlined);
	}
	// END KGU#701 2019-03-29
	
	// START KGU#686 2019-03-16: Enh. #56 introduction of Try elements
	/**
	 * @return the set of cached variable names for the element context of Element {@code _child}
	 */
	protected Set<String> getVariableSetFor(Element _child) {
		if (this.parent == null) {
			return new HashSet<String>();
		}
		return this.parent.getVariableSetFor(this);
	}
	// END KGU#686 2019-03-16
	
	// START KGU#227 2016-07-29: Enh. #128
	/**
	 * Writes the non-empty comment lines at position _x, _y to _canvas with 2/3 font height and in dark gray
	 * @param _canvas - the drawing canvas
	 * @param _x - left text anchor coordinate for the text area
	 * @param _y - top text anchor coordinate for the text area
	 * @param _actuallyDraw - if the text is actually to be written (otherwise we just return the bonds)
	 * @return - bounding box of the text
	 */
	protected Rect writeOutCommentLines(Canvas _canvas, int _x, int _y, boolean _actuallyDraw)
	{
		int height = 0;
		int width = 0;
		// smaller font - KGU 2019-03-29: Now taken from static field
		//Font smallFont = new Font(Element.font.getName(), Font.PLAIN, Element.font.getSize() * 2 / 3);
		FontMetrics fm = _canvas.getFontMetrics(smallFont);
		int fontHeight = fm.getHeight();	// Here we don't reduce to fm.getLeading() + fm.getAscend()
		int extraHeight = this.isBreakpoint() ? fontHeight/2 : 0;
		// backup the original font
		Font backupFont = _canvas.getFont();
		_canvas.setFont(smallFont);
		_canvas.setColor(Color.DARK_GRAY);
		int nLines = this.getComment().count();
		for (int i = 0; i < nLines; i++)
		{
			String line = this.getComment().get(i).trim();
			if (!line.isEmpty())
			{
				height += fontHeight;
				width = Math.max(width, _canvas.stringWidth(line));
				if (_actuallyDraw)
				{
					_canvas.writeOut(_x, _y + height + extraHeight, line);
				}
			}
		}
		
		_canvas.setFont(backupFont);
		_canvas.setColor(Color.BLACK);
		if (height > 0)
		{
			height += fontHeight/2;
		}
		return new Rect(_x, _y, _x+width, _y+height);
	}
	
	protected boolean haveOuterRectDrawn()
	{
		return true;
	}
	// END KGU#227 2016-07-29

	// START KGU#156 2016-03-11: Enh. #124 - helper routines to display run-time info
	/**
	 * Writes the selected runtime information in half-size font to the lower
	 * left of position (_right, _top).
	 * @param _canvas - the Canvas to write to
	 * @param _right - right border x coordinate
	 * @param _top - upper border y coordinate
	 */
	protected void writeOutRuntimeInfo(Canvas _canvas, int _right, int _top)
	{
		if (Element.E_COLLECTRUNTIMEDATA)
		{
			// smaller font - KGU 2019-03-29: now taken from static field
			//Font smallFont = new Font(Element.font.getName(), Font.PLAIN, Element.font.getSize()*2/3);
			FontMetrics fm = _canvas.getFontMetrics(smallFont);
			// backup the original font
			Font backupFont = _canvas.getFont();
			String info = this.getRuntimeInfoString();
			int yOffs = fm.getHeight() + (this.isBreakpoint() ? 4 : 0); 
			_canvas.setFont(smallFont);
			int width = _canvas.stringWidth(info);
			// START KGU#213 2016-08-01: Enh. #215
			//_canvas.setColor(Color.BLACK);
			if (this.isConditionedBreakpoint())
			{
				String triggerInfo = this.getBreakTriggerCount() + ": ";
				int extraWidth = _canvas.stringWidth(triggerInfo);
				_canvas.setColor(Color.RED);
				_canvas.writeOut(_right - width - extraWidth, _top + yOffs, triggerInfo);
			}
			// END KGU#213 2016-08-01
			_canvas.setColor(Color.BLACK);
			_canvas.writeOut(_right - width, _top + yOffs, info);
			_canvas.setFont(backupFont);
		}
	}
	
	/**
	 * Returns a runtime counter string, composed from execution count
	 * and a mode-dependent number of steps (pure or aggregated, with or
	 * without parenthesis). 
	 * @return the decoration string for runtime data visualisation
	 */
	protected String getRuntimeInfoString()
	{
		return this.getExecCount() + " / " + this.getExecStepCount(this.isCollapsed(true));
	}
	// END KGU#156 2016-03-11
	
    /**
     * Detect whether the element is currently collapsed (or to be shown as collapsed by other reasons)
     * @param _orHidingOthers - if some additional element-specific hiding criterion is to be considered, too  
     * @return true if element is to be shown in collapsed shape
     */
    public boolean isCollapsed(boolean _orHidingOthers) {
        return collapsed;
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
        // START KGU#136 2016-03-01: Bugfix #97
        this.resetDrawingInfoUp();
        // END KGU#136 2016-03-01
    }
    
    // START KGU#122 2016-01-03: Enh. #87
    /**
     * @return the element-type-specific icon image intended to be placed in the upper left
     * corner of the drawn element if being collapsed.
     * @see #getMiniIcon()
     */
    public ImageIcon getIcon()
    {
    	return IconLoader.getIcon(57);
    }
    // END KGU#122 2016-01-03
    
    // START KGU#535 2018-06-28
    /**
     * @return the (somewhat smaller) element-type-specific icon image intended to be used in
     * the {@link FindAndReplace} dialog.
     * @see #getIcon()
     */
    public ImageIcon getMiniIcon()
    {
    	return IconLoader.getIcon(10);
    }
    // END KGU#535 2018-06-28

    // START KGU 2015-10-16: Some Root stuff properly delegated to the Element subclasses
    // (The obvious disadvantage is slightly reduced performance, of course)
    /**
     * Returns the serialised texts held within this element and its substructure.
     * The argument {@code _instructionsOnly} controls whether mere expressions like
     * logical conditions or even call statements are included. As a rule, no lines
     * that may not potentially introduce new variables are added if {@code true}
     * (which not only reduces time and space requirements but also avoids "false positives"
     * in variable detection).<br/>
     * Uses {@link #addFullText(StringList, boolean)} - so possibly better override that method if
     * necessary.
     * @param _instructionsOnly - if {@code true} then only the texts of Instruction elements are included
     * @return the composed {@link StringList}
     */
    public ArrayList<TokenList> getFullText(boolean _instructionsOnly)
    {
    	// The default...
    	ArrayList<TokenList> sl = new ArrayList<TokenList>();
    	this.addFullText(sl, _instructionsOnly);
    	return sl;
    }
    
    /**
     * Appends all the tokenized texts held within this element and its substructure to the given list.
     * The argument _instructionsOnly controls whether mere expressions like logical conditions or
     * even call statements are included. As a rule, no lines that may not potentially introduce new
     * variables are added if true (which not only reduces time and space requirements but also avoids
     * "false positives" in variable detection). 
     * (To be overridden by structured subclasses)
     * 
     * @param _lines - the list of TokenLists to append to 
     * @param _instructionsOnly - if true then texts not possibly containing variable declarations are omitted
     */
    protected abstract void addFullText(ArrayList<TokenList> _lines, boolean _instructionsOnly);
    // END KGU 2015-10-16
    
    /**
     * Returns a (hopefully) lossless representation of the stored text as a
     * StringList in a common intermediate language (code generation phase 1).
     * This allows the language-specific Generator subclasses to concentrate
     * on the translation into their respective target languages (code generation
     * phase 2).<br/>
     * Conventions of the intermediate language:<br/>
     * Operators (note the surrounding spaces - no double spaces will exist):
     * <ul>
     * <li>Assignment:	" <- "
     * <li>Comparison:	" = ", " < ", " > ", " <= ", " >= ", " <> "
     * <li>Logic:		" && ", " || ", " §NOT§ ", " ^ "
     * <li>Arithmetics:	usual Java operators without padding
     * <li>Control key words:<ul>
     *   <li>If, Case:		none (wiped off)</li>
     *   <li>While, Repeat:	none (wiped off)</li>
     *   <li>For:			unchanged</li>
     *   <li>Forever:		none (wiped off)</li>
     * </ul></li>
     * </ul>
     * @return a padded intermediate language equivalent of the stored text
     * 
     * @see #getIntermediateTokenText()
     */
    public StringList getIntermediateText()
    {
        var interSl = new StringList();
        ArrayList<TokenList> lines = this.getUnbrokenTokenText();
        for (int i = 0; i < lines.size(); i++)
        {
            interSl.add(transformIntermediate(lines.get(i)).getString());
        }
        return interSl;
    }
    
    /**
     * Returns a (hopefully) lossless representation of the stored text as a
     * list of TokenLists in a common intermediate language (code generation phase 1).
     * This allows the language-specific Generator subclasses to concentrate
     * on the translation into their respective target languages (code generation
     * phase 2).<br/>
     * Conventions of the intermediate language:<br/>
     * Operators (note the surrounding spaces - no double spaces will exist):
     * <ul>
     * <li>Assignment:	" <- "
     * <li>Comparison:	" = ", " < ", " > ", " <= ", " >= ", " <> "
     * <li>Logic:		" && ", " || ", " §NOT§ ", " ^ "
     * <li>Arithmetics:	usual Java operators without padding
     * <li>Control key words:<ul>
     *   <li>If, Case:		none (wiped off)</li>
     *   <li>While, Repeat:	none (wiped off)</li>
     *   <li>For:			unchanged</li>
     *   <li>Forever:		none (wiped off)</li>
     * </ul></li>
     * </ul>
     * @return a padded intermediate language equivalent of the stored text
     */
    public ArrayList<TokenList> getIntermediateTokenText()
    {
        var interTt = new ArrayList<TokenList>();
        ArrayList<TokenList> tokenLines = this.getUnbrokenTokenText();
        for (int i = 0; i < tokenLines.size(); i++)
        {
            interTt.add(transformIntermediate(tokenLines.get(i)));
        }
        return interTt;
    }
    
//    /**
//     * Translates the Pascal procedure calls {@code inc(var), inc(var, offs), dec(var)},
//     * and {@code dec(var, offs)} into simple assignments in Structorizer syntax.
//     * 
//     * @param code - the piece of text possibly containing {@code inc} or {@code dec} references
//     * @return the transformed string.
//     * 
//     * @deprecated prefer {@link #transform_inc_dec(TokenList)}
//     */
//    public static String transform_inc_dec(String code)
//    {
//        code = INC_PATTERN1.matcher(code).replaceAll("$1 <- $1 + $2");
//        code = INC_PATTERN2.matcher(code).replaceAll("$1 <- $1 + 1");
//        code = DEC_PATTERN1.matcher(code).replaceAll("$1 <- $1 - $2");
//        code = DEC_PATTERN2.matcher(code).replaceAll("$1 <- $1 - 1");
//        return code;
//    }
    
	/**
	 * Translates the Pascal procedure calls {@code inc(var), inc(var, offs), dec(var)},
	 * and {@code dec(var, offs)} into simple assignments in Structorizer syntax.
	 * 
	 * @param code - the piece of text possibly containing {@code inc} or {@code dec} references
	 * @return the transformed string.
	 */
	public static TokenList transform_inc_dec(TokenList code)
	{
//		TokenList argListEnd = new TokenList(")");
//		TokenList transf = new TokenList(code); // Avoid to backfeed argument modifications
//		for (String[] pair: substPairs) {
//			TokenList funcPattern = new TokenList(pair[0] + "(");
//			while ((pos = transf.indexOf(funcPattern, pos+1, false)) >= 0) {
//				ArrayList<TokenList> argExprs = Syntax.splitExpressionList(transf.subSequenceToEnd(pos + 2), ",");
//				int nArgs = argExprs.size() - 1;
//				if (argExprs.get(nArgs).startsWith(argListEnd, true) &&
//						nArgs >= 1 && nArgs <= 2) {
//					transf.remove(pos, pos + argExprs.get(0).size() + 1);
//					if (nArgs == 1) {
//						transf.add(pos, "1");
//					}
//					else {
//						transf.addAll(pos, argExprs.get(1));
//					}
//					transf.add(pos, pair[1]);
//					transf.addAll(pos, argExprs.get(0));
//					transf.add(pos, " <- ");
//					transf.addAll(pos, argExprs.get(0));
//				}
//			}
//		}
//		return transf;
		// Since inc and dec are procedures they can only be placed at the very beginning
		Function incOrDec = new Function(code);
		int arity = 0;
		String name = null;
		if (incOrDec.isFunction() && (arity = incOrDec.paramCount()) >= 1 && arity <= 2 &&
				((name = incOrDec.getName().toLowerCase()).equals("inc") || name.equals("dec"))) {
			TokenList variable = incOrDec.getParamTokens(0);
			code = new TokenList(variable);
			code.add(" <- ");
			code.addAll(variable);
			if (name.equals("inc")) {
				code.add(" + ");
			}
			else {
				code.add(" - ");
			}
			if (arity == 1) {
				code.add("1");
			}
			else {
				code.addAll(incOrDec.getParamTokens(1));
			}
		}
		return code;
	}

	/**
	 * Translates the Pascal procedure calls {@code insert(w, s, i), delete(s, i, n)} into
	 * Java-compatible assignments with the respective Structorizer functions:<br/>
	 * {@code s <- insert(w, s, i)}<br/>
	 * {@code s <- delete(s, i, n)}<br/>
	 * 
	 * @param code - the tokenized text possibly containing {@code insert} or {@code delete}
	 *     references.
	 * @return the transformed string.
	 */
	public static TokenList transform_insert_delete(TokenList code)
	{
		Function insOrDel = new Function(code);
		String name = null;
		TokenList transf = code;
		if (insOrDel.isFunction() && insOrDel.paramCount() == 3 &&
				((name = insOrDel.getName().toLowerCase()).equals("insert") || name.equals("delete"))) {
			TokenList variable = insOrDel.getParamTokens(name.equals("insert") ? 1 : 0);
			transf = new TokenList(variable);
			transf.add(" <- ");
			transf.addAll(code);
		}
		return transf;
	}

	/**
	 * Creates a (hopefully) lossless representation of the {@code _text} String as a
	 * tokens list of a common intermediate language (code generation phase 1).
	 * This allows the language-specific Generator subclasses to concentrate
	 * on the translation into their target language (code generation phase 2).
	 * Conventions of the intermediate language:<br/>
	 * Operators (note the surrounding spaces - no double spaces will exist):
	 * <ul>
	 * <li>Assignment:		"<-"</li>
	 * <li>Comparison:		"=", "<", ">", "<=", ">=", "<>"</li>
	 * <li>Logic:			"&&", "||", "!", "^"</li>
	 * <li>Arithmetics:		usual Java operators</li>
	 * <li>Control key words:<ul>
	 *   <li>If, Case:		none (wiped off)</li>
	 *   <li>While, Repeat:	none (wiped off)</li>
	 *   <li>For:			unchanged</li>
	 *   <li>Forever:		none (wiped off)</li>
	 * </ul></li>
	 * </ul>
	 * @param _text - a line of the Structorizer element
	 * //@return a padded intermediate language equivalent of the stored text
	 * @return a StringList consisting of tokens translated into a unified intermediate language
	 */

	// START KGU#93 2023-10-29: Bugfix #41/#68/#69
	public static TokenList transformIntermediate(TokenList tokens)
	{
		TokenList interm = transform_inc_dec(tokens);
		Syntax.removeDecorators(interm);
		Syntax.unifyOperators(interm, false);
		return interm;
	}
	// END KGU#18/KGU#23 2023-10-29
    
    // START KGU#162 2016-03-31: Enh. #144 - undispensible part of transformIntermediate
    // START KGU#790 2021-10-25: Issue #800 replaced by Syntax.removeDecorators(StringList)
    //public static void cutOutRedundantMarkers(StringList _tokens)
    //{
    //    // Collect redundant placemarkers to be deleted from the text
    //    // FIXME: Find a more efficient way to do this
    //    StringList redundantMarkers = new StringList();
    //    redundantMarkers.addByLength(Syntax.getKeyword("preAlt"));
    //    redundantMarkers.addByLength(Syntax.getKeyword("preCase"));
    //    redundantMarkers.addByLength(Syntax.getKeyword("preWhile"));
    //    redundantMarkers.addByLength(Syntax.getKeyword("preRepeat"));
    //
    //    redundantMarkers.addByLength(Syntax.getKeyword("postAlt"));
    //    redundantMarkers.addByLength(Syntax.getKeyword("postCase"));
    //    redundantMarkers.addByLength(Syntax.getKeyword("postWhile"));
    //    redundantMarkers.addByLength(Syntax.getKeyword("postRepeat"));
    //    
    //    for (int i = 0; i < redundantMarkers.count(); i++)
    //    {
    //    	String marker = redundantMarkers.get(i);
    //    	if (marker != null && !marker.trim().isEmpty())
    //    	{
    //    		StringList markerTokens = Syntax.splitLexically(marker, false);
    //    		int markerLen = markerTokens.count();
    //    		int pos = -1;
    //    		while ((pos = _tokens.indexOf(markerTokens, 0, !Syntax.ignoreCase)) >= 0)
    //    		{
    //    			for (int j = 0; j < markerLen; j++)
    //    			{
    //    				_tokens.delete(pos);
    //    			}
    //    		}
    //    	}
    //    }
    //}
    // END KGU#790 2021-10-25
    // END KGU#162 2016-03-31

	// START KGU#152 2016-03-02: Better self-description of Elements
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + '@' + Integer.toHexString(hashCode()) +
				// START KGU#261 2017-01-19: Enh. #259 (type map)
				//"(" + (this.getText().count() > 0 ? this.getText().get(0) : "") + ")";
				"(" + this.id + (this.getText().count() > 0 ? (": " + this.getText().get(0)) : "") + ")";
		// END KGU#261 2017-01-19
	}
	// END KGU#152 2016-03-02

	// START KGU#258 2016-09-26: Enh. #253
	/**
	 * Returns a fixed array of names of parser preferences being relevant for
	 * the current type of Element (e.g. in case of refactoring) with one of
	 * the placement prefixes "^" for start of line, "$" for end of line and
	 * "*" anywhere in line.
	 * 
	 * @return Arrays of prefixed key strings for Syntax.keywordMap
	 */
	protected abstract String[] getRelevantParserKeys();

// START KGU#1097 2023-11-09: Issue #800 Obsolete
//	/**
//	 * Looks up the associated token sequence in _splitOldKeywords for any of the parser preference names
//	 * provided by getRelevantParserKeys(). If there is such a token sequence then it will be
//	 * replaced throughout my text by the associated current parser preference for the respective name
//	 * @param _oldKeywords - a map of tokenized former non-empty parser preference keywords to be replaced
//	 * @param _ignoreCase - whether case is to be ignored on comparison.
//	 */
//	public void refactorKeywords(HashMap<String, StringList> _splitOldKeywords, boolean _ignoreCase)
//	{
//		String[] relevantKeys = getRelevantParserKeys();
//		if (relevantKeys != null && !_splitOldKeywords.isEmpty())
//		{
//			StringList result = new StringList();
//			for (int i = 0; i < this.text.count(); i++)
//			{
//				result.add(refactorLine(text.get(i), _splitOldKeywords, relevantKeys, _ignoreCase));
//			}
//			this.text = result;
//		}
//	}
//	
//	/**
//	 * Looks up the associated token sequence in _splitOldKeys for any of the parser
//	 * preference names provided by _prefNames. If there is such a token sequence
//	 * then it will be replaced throughout {@code _line} by the associated current
//	 * parser preference for the respective name.
//	 * 
//	 * @param _line - line of element text
//	 * @param _splitOldKeys - a map of tokenized former non-empty parser preference keywords to be replaced
//	 * @param _prefNames - Array of parser preference names being relevant for this kind of element
//	 * @param _ignoreCase - whether case is to be ignored on comparison
//	 * @return refactored line
//	 */
//	protected final String refactorLine(String _line, HashMap<String, TokenList> _splitOldKeys, String[] _prefNames, boolean _ignoreCase)
//	{
//		TokenList tokens = new TokenList(_line);
//		boolean isModified = false;
//		// FIXME: We should order the keys by decreasing length first!
//		for (int i = 0; i < _prefNames.length; i++)
//		{
//			TokenList splitKey = _splitOldKeys.get(_prefNames[i]);
//			if (splitKey != null)
//			{
//				String subst = Syntax.getKeyword(_prefNames[i]);
//				// line shouldn't be inflated ...
//				//if (!splitKey.get(0).equals(" ")) {
//				//	while (subst.startsWith(" ")) subst = subst.substring(1); 
//				//}
//				//if (!splitKey.get(splitKey.size()-1).equals(" ")) {
//				//	while (subst.endsWith(" ")) subst = subst.substring(0, subst.length()-1);
//				//}
//				// ... but spaces must not get lost either!
//				//if (splitKey.get(0).equals(" ") && !subst.startsWith(" ")) {
//				//	subst = " " + subst;
//				//}
//				//if (splitKey.size() > 1 && splitKey.get(splitKey.size()-1).equals(" ") && !subst.endsWith(" ")) {
//				//	subst += " ";
//				//}
//				// Now seek old keyword and replace it where found
//				int pos = -1;
//				while ((pos = tokens.indexOf(splitKey, pos+1, !_ignoreCase)) >= 0)
//				{
//					// Replace the first part of the saved keyword by the entire current keyword... 
//					tokens.set(pos, subst);
//					tokens.setPadding(pos, 1, 1);
//					// ... and remove the remaining parts of the saved key
//					tokens.remove(pos+1, pos+splitKey.size());
//					isModified = true;
//				}
//			}
//		}
//		if (isModified)
//		{
//			_line = tokens.getString().trim();
//		}
//		return _line;
//	}
//	// END KGU#258 2016-09-26
// END KGU#1097 2023-11-09

	// START KGU#258/KGU#1097 2023-11-09: Enh. #253, issue #800 TokenList version
	/**
	 * Looks up the associated token sequence in {@code _splitOldKeywords} for any of
	 * the parser preference keys provided by getRelevantParserKeys(). If there
	 * is such a token sequence then it will be replaced throughout my text by
	 * the respective internal marker token.
	 * 
	 * @param _oldKeywords - a map of tokenized former non-empty parser preference
	 *    keywords to be replaced, or {@code null} if the currently configured
	 *    keywords are expected in the current token lists
	 * @param _ignoreCase - whether case is to be ignored on comparison.
	 */
	public void encodeKeywords(HashMap<String, TokenList> _splitOldKeywords, boolean _ignoreCase)
	{
		/* Each member of relevant keys consists of an initial character specifying where
		 * to look for the keyword tokens in an unbroken line:
		 * '^' if only at the beginning
		 * '$' if only at the end
		 * '*' (or any other character) if at an arbitrary position
		 * The remaining character sequence is the actual keyword key
		 */
		String[] relevantKeys = getRelevantParserKeys();
		if (relevantKeys != null)
		{
			ArrayList<TokenList> result = new ArrayList<TokenList>();
			boolean isContinued = false;
			for (int i = 0; i < this.text.count(); i++)
			{
				// FIXME: We should order the keys by decreasing length first!
				TokenList encoded = Syntax.encodeLine(new TokenList(this.text.get(i)), _splitOldKeywords,
						relevantKeys, _ignoreCase, isContinued);
				isContinued = encoded.endsWith("\\");
				result.add(encoded);
			}
			this.setTokenText(result);
		}
	}
	// END KGU#258/KGU#1097 2023-10-29

	// START KGU#301 2016-12-01: Bugfix #301
	/**
	 * Helper method to detect exactly whether the given {@code expression} is enclosed in parentheses.
	 * Simply check whether it starts with "(" and ends with ")" is NOT sufficient because the expression
	 * might look like this: {@code (4 + 8) * sqrt(3.5)}, which starts and ends with parentheses without
	 * being parenthesized.
	 * @param expression - the expression to be analysed as string
	 * @return true if the expression is properly parenthesized. (Which is to be ensured e.g for conditions
	 * in C and derived languages.
	 */
	public static boolean isParenthesized(String expression)
	{
		boolean isEnclosed = expression.startsWith("(") && expression.endsWith(")");
		if (isEnclosed) {
			TokenList tokens = new TokenList(expression);
			isEnclosed = isParenthesized0(tokens);
		}
		return isEnclosed;
	}
	// END KGU#301 2016-12-01
	
	// START KGU#301 2017-09-19: Issue #302: Method isParenthesized(String expression) decomposed
	/**
	 * Helper method to detect exactly whether expression represented by the given {@code tokens} is enclosed
	 * in parentheses.<br>
	 * Simply to check whether it starts with "(" and ends with ")" is NOT sufficient because the expression
	 * might look like this: {@code (4 + 8) * sqrt(3.5)}, which starts and ends with parentheses without
	 * being parenthesized.
	 * @param tokens - the tokenised expression to be analysed as StringList
	 * @return true if the expression is properly parenthesized. (Which is to be ensured e.g for conditions
	 * in C and derived languages.
	 */
	public static boolean isParenthesized(TokenList tokens)
	{
		return tokens.size() > 1 && tokens.get(0).equals("(") && tokens.get(tokens.size()-1).equals(")")
				&& isParenthesized0(tokens);
	}
	
	// Internal check for both public isParenthesized() methods
	private static boolean isParenthesized0(TokenList tokens) {
		boolean isEnclosed;
		int level = 0;
		for (int i = 1; level >= 0 && i < tokens.size()-1; i++) {
			String token = tokens.get(i);
			if (token.equals("(")) {
				level++;
			}
			else if (token.equals(")")) {
				level--;
			}
		}
		isEnclosed = level == 0;
		return isEnclosed;
	}
	// END KGU#301 2019-09-19

	// START KGU#277 2016-10-13: Enh. #270 - Option to disable an Element from execution and export
	/**
	 * Checks whether this element or one of its ancestors is disabled 
	 * @param individually - if {@code true} then only the individual setting
	 *    will be reported
	 * @return {@code true} if directly (or indirectly) disabled
	 */
	public boolean isDisabled(boolean individually)
	{
		// START KGU#1179 2025-07-02: Issue #270 Implementation was flawed in case of true
		//return this.disabled || (this.parent != null && this.parent.isDisabled(individually));
		return disabled || !individually && (this.parent != null && this.parent.isDisabled(false));
		// END KGU#1179 2025-07-02
	}
	// END KGU#277 2016-10-13
	
	// START KGU#408 2021-02-26: Enh. #410 - We need a composed disabled check
	/** Sets the {@link #disabled} flag to {@code b} */
	public void setDisabled(boolean b)
	{
		this.disabled = b;
	}
	// END KG#408 2021-02-26
	
	// START KGU 2017-10-21 New deep reachability check
	/** @return whether an entered control flow may leave this element sequentially. */
	public boolean mayPassControl()
	{
		// Normally, the control flow will leave every element.
		return true;
	}
	// END KGU 2017-10-21
	
	// START KGU#261 2017-01-19: Enh. #259 (type map)
	public Element findElementWithId(long _id)
	{
		final class ElementFinder implements IElementVisitor {
			
			private long id;
			private Element foundElement = null;

			public ElementFinder(long _id)
			{
				id = _id;
			}
			
			@Override
			public boolean visitPreOrder(Element _ele) {
				if (_ele.getId() == id) {
					foundElement = _ele;
					return false;
				}
				return true;
			}

			@Override
			public boolean visitPostOrder(Element _ele) {
				return true;
			}
			
		}
		ElementFinder finder = new ElementFinder(_id);
		traverse(finder);
		return finder.foundElement;
	}
	
	/**
	 * Adds own variable declarations (only this element, no substructure!) to the given
	 * type map (varname -> typeinfo).
	 * 
	 * @param typeMap - the type map to be used an extended
	 * 
	 * @deprecated Use {@link #updateTypeRegistry(TypeRegistry)} instead
	 */
	public void updateTypeMap(HashMap<String, TypeMapEntry> typeMap)
	{
		// Does nothing - to be sub-classed where necessary
	}
	// END KGU#261 2017-01-19
	
	/**
	 * Adds own variable declarations and type definitions (only this element,
	 * no substructure!) to the given {@link TypeRegistry}.
	 * 
	 * @param _dataTypes
	 */
	public void updateTypeRegistry(TypeRegistry _dataTypes)
	{
		Line[] lines = this.getParsedText(comment, _dataTypes);
		for (int i = 0; i < lines.length; i++) {
			Line line = lines[i];
			Type dataType = line.getDataType(false);
			if (dataType != null) {
				boolean force = false;
				int nExpr = line.getExprCount();
				Expression expr;
				switch (line.getType()) {
				case LT_CONST_DEF:
				case LT_VAR_INIT:
					force = true;
				case LT_ASSIGNMENT:
				case LT_FOR_LOOP:
					if (nExpr > 0
							&& (expr = line.getExpression(0)) != null
							&& expr.type == Expression.NodeType.OPERATOR
							&& expr.children.size() == 2
							&& (expr = expr.children.get(0)).type == Expression.NodeType.IDENTIFIER) {
						_dataTypes.putTypeFor(expr.text, dataType, force);
					}
					break;
				case LT_CATCH:
					if (nExpr > 0
							&& (expr = line.getExpression(0)).type == Expression.NodeType.IDENTIFIER) {
						// FIXME: How to cope with block scope here?
						_dataTypes.putTypeFor(expr.text, dataType, this, i, true);
					}
					break;
					// TODO To be done in Call class
//				case LT_CONST_FUNCT_CALL: 
//					break;
//				case LT_ROUTINE_CALL:
//					break;
				case LT_FOREACH_LOOP:
					if (nExpr > 0
							&& (expr = line.getExpression(0)).type == Expression.NodeType.IDENTIFIER) {
						// FIXME: How to cope with block scope here?
						_dataTypes.putTypeFor(expr.text, dataType, this, i, false);
					}
					break;
				case LT_TYPE_DEF:
					// FIXME Cause an error if the type already had been defined
					_dataTypes.putType(dataType, this, i, true);
					break;
				case LT_VAR_DECL:
					for (int j = 0; j < nExpr; j++) {
						expr = line.getExpression(j);
						if (expr.type == Expression.NodeType.IDENTIFIER) {
							// FIXME We should warn / raise an error if there had already been a type
							_dataTypes.putTypeFor(expr.text, dataType, this, i, true);
						}
					}
					break;
				default:
					break;
				}
			}
		}
	}
	
	// START KGU#261 2017-01-26: Enh. #259
	/**
	 * Analyses the given {@code typeSpec} string and adds a derived {@code TypeMapEntry.VarDeclaration}
	 * to the {@code typeMap} associated to the given {@code varName}.
	 * 
	 * @param typeMap - maps variable and type names to gathered detailed type information 
	 * @param varName - name of a variable being declared
	 * @param typeSpec - a type-describing string (might be a type name or a type construction)
	 * @param lineNo - number of the element text line containing the type description
	 * @param isAssigned - is to indicate whether a value is assigned here
	 * @param explicitly - whether the type association was an explicit declaration or just guessed
	 */
	protected void addToTypeMap(HashMap<String,TypeMapEntry> typeMap, String varName, String typeSpec, int lineNo, boolean isAssigned, boolean explicitly)
	{
		if (varName != null && !typeSpec.isEmpty()) {
			TypeMapEntry entry = typeMap.get(varName);
			// Get the referred type entry in case typeSpec is a previously defined type
			TypeMapEntry typeEntry = null;
			if (Syntax.isIdentifier(typeSpec, false, null)) {
				typeEntry = typeMap.get(":" + typeSpec);
			}
			if (entry == null) {
				if (typeEntry != null) {
					typeMap.put(varName, typeEntry);
				}
				else {
					// Add a new entry to the type map
					typeMap.put(varName, new TypeMapEntry(typeSpec, null, null, this, lineNo, isAssigned, explicitly));
				}
			}
			else if (typeEntry == null || !typeEntry.isRecord()) {
				// START KGU#593 2018-10-05: Bugfix #619
				if (explicitly && !entry.isDeclaredWithin(null)) {
					entry.isDeclared = true;
				}
				// END KGU#593 2018-10-05
				// add an alternative declaration to the type map entry
				entry.addDeclaration(typeSpec, this, lineNo, isAssigned);
			}
		}
	}
	
	// START KGU#388 2017-09-13: Enh. #423
	/**
	 * Adds a record type definition with name {@code typeName} and component definitions
	 * from lists {@code compNames} and {@code compTypes} to the {@code typeMap}.
	 * @param typeMap - maps variable and type names to gathered detailed type information 
	 * @param typeName - name of the new defined type
	 * @param typeSpec - a type-describing string as found in the definition
	 * @param compNames - list of the component identifiers (strings)
	 * @param compTypes - list of type-describing strings (a type name or a type construction or null!)
	 * @param lineNo - number of the element text line containing the type description
	 * @return true if the {@code typeName} was new and could be placed in the {@code typeMap}.
	 */
	protected boolean addRecordTypeToTypeMap(HashMap<String,TypeMapEntry> typeMap, String typeName, String typeSpec, StringList compNames, StringList compTypes, int lineNo)
	{
		boolean done = false;
		if (typeName != null && compNames.count() > 0 && compTypes.count() == compNames.count()) {
			TypeMapEntry entry = typeMap.get(":" + typeName);
			// Get the referred type entry in case typeSpec is a previously defined type
			if (entry == null) {
				// Add a new entry to the type map
				boolean isRecursive = false;
				// FIXME KGU#687 2019-03-16: Issue #408, #56 - shall we replace this by compTypes now?
				LinkedHashMap<String, TypeMapEntry> components = new LinkedHashMap<String, TypeMapEntry>();
				for (int i = 0; i < compNames.count(); i++) {
					TypeMapEntry compEntry = null; 
					if (i < compTypes.count()) {
						String type = compTypes.get(i);
						if (type != null) {
							if (Syntax.isIdentifier(type, false, null)) {
								// Try to find an existing type entry with this name
								compEntry = typeMap.get(":" + type);
								if (compEntry == null) {
									if (type.equals(typeName)) {
										isRecursive = true;
										// We postpone the completion of this self-referencing component 
									}
									else {
										// Create a named dummy entry
										compEntry = new TypeMapEntry(type, type, typeMap, this, lineNo, false, true);
									}
								}
							}
							// FIXME KGU#687 2019-03-16: Issue #408 - no longer needed?
							else {
								// Create an unnamed dummy entry
								compEntry = new TypeMapEntry(type, null, null, this, lineNo, false, true);
							}
						}
					}
					// Note that compEntry may be null here
					if (compEntry == null) compEntry = TypeMapEntry.getDummy();
					components.put(compNames.get(i), compEntry);
				}
				entry = new TypeMapEntry(typeSpec, typeName, typeMap, components, this, lineNo);
				// In case of self-references map the respective component names to the created TypeMapEntry 
				if (isRecursive) {
					for (int i = 0; i < compNames.count(); i++) {
						if (i < compTypes.count() && typeName.equals(compTypes.get(i))) {
							components.put(compNames.get(i), entry);
						}
					}
				}
				// Now register the accomplished type entry
				typeMap.put(":" + typeName, entry);
				done = true;
			}
			else {
				logger.log(Level.WARNING, "Type redefinition attempt for \"{1}\"!", typeName);
			}
		}
		return done;
	}
	// END KGU#388 2017-09-13

	/**
	 * Negates the given condition as intelligently as possible.
	 * 
	 * @param condition - a boolean expression (in Structorizer-conform syntax)
	 * @return an expression representing the logical opposite of {@code condition}
	 */
	public static String negateCondition(String condition)
	{
		String negCondition = null;
		TokenList condTokens = new TokenList(condition, true);
		// START KGU#790 2020-11-01: Issue #800 Replaced by new syntax stuff
		//int length = condTokens.count();
		//String first = condTokens.get(0);
		//// Already explicitly negated?
		//if (first.equals("not") || first.equals("!")) {
		//	int i = 1;
		//	while (i < length && condTokens.get(i).trim().isEmpty()) i++;
		//	if (i == length-1) {
		//		// Obviously a single negated token, so just drop the operator
		//		negCondition = condTokens.get(i); 
		//	}
		//	else if (i < length && Element.isParenthesized(condTokens.subSequence(i, length))) {
		//		negCondition = condTokens.subSequence(i+1, length-1).concatenate();
		//	}
		//}
		try {
			List<Expression> exprs = Expression.parse(condTokens, null, (short)0);
			if (exprs.size() == 1) {
				Expression cond = exprs.get(0);
				cond = Expression.negateCondition(cond, true);
				negCondition = cond.toString();
			}
		} catch (SyntaxException exc) {
			logger.log(Level.WARNING, "Trouble with negating " + condition, exc);
		}
		// END KGU#790 2020-11-01
		if (negCondition == null) {
			if (!Element.isParenthesized(condTokens)) {
				condition = "(" + condition + ")";
			}
			negCondition = "not " + condition;
		}
		return negCondition;
	}
	
	public void setRotated(boolean _rotated)
	{
		if (rotated != _rotated) {
			// Flip the stored rectangle
			this.rect0 = new Rect(rect0.left, rect0.top, rect0.bottom, rect0.right);
		}
	}
	
	// START KGU#906/KGU#926 2021-02-04: Enh. #905, #926 consider eclipsed substructure
	/**
	 * Retrieves all (or just the first) {@link DetectedError} objects related to this
	 * element or some substructure element (in case this element is collapsed)
	 * @param getAll - if not {@code true} then only the result will only contain the
	 * first related {@link DetectedError} found (allowing an efficient existence check)
	 * @return a map from element to lists of related {@link DetectedError} objects,
	 *  may be empty
	 */
	public LinkedHashMap<Element, Vector<DetectedError>> getRelatedErrors(boolean getAll)
	{
		LinkedHashMap<Element, Vector<DetectedError>> errorMap = 
				new LinkedHashMap<Element, Vector<DetectedError>>();
		addRelatedErrors(getAll, errorMap);
		return errorMap;
	}
	
	/**
	 * Internal helper for {@link #getRelatedErrors(boolean)}
	 * 
	 * @param getAll - if not {@code true} then the result will contain only the
	 * first related {@link DetectedError} found (allowing an efficient existence check)
	 * @param errorMap - a map from element to lists of related {@link DetectedError} objects,
	 *  may be empty
	 */
	protected final void addRelatedErrors(boolean getAll, LinkedHashMap<Element, Vector<DetectedError>> errorMap)
	{
		if (E_ANALYSER) {
			Root myRoot = getRoot(this);
			if (myRoot != null && myRoot.errors != null && !myRoot.errors.isEmpty()) {
				boolean descend = this.isCollapsed(false);
				// Now collect the information from the substructure if necessary
				final class ErrorFinder implements IElementVisitor {
					
//					private Root root;
//					public LinkedHashMap<Element, Vector<DetectedError>> errorMap;
//					boolean descend;
//					boolean oneIsEnough;
//
//					public ErrorFinder(Root _root, LinkedHashMap<Element, Vector<DetectedError>> _errorMap,
//							boolean _descend, boolean _getAll)
//					{
//						root = _root;
//						errorMap = _errorMap;
//						descend = _descend;
//						oneIsEnough = !_getAll;
//					}
					
					@Override
					public boolean visitPreOrder(Element _ele) {
						for (DetectedError error: myRoot.errors) {
							if (_ele == error.getElement()) {
								if (!errorMap.containsKey(_ele)) {
									errorMap.put(_ele, new Vector<DetectedError>());
								}
								errorMap.get(_ele).add(error);
								if (!getAll) {
									return false;
								}
							}
						}
						return descend;
					}

					@Override
					public boolean visitPostOrder(Element _ele) {
						return true;
					}

				}
				ErrorFinder finder = new ErrorFinder();
//				ErrorFinder finder = new ErrorFinder(myRoot, errorMap, descend, getAll);
				traverse(finder);
			}
		}
		
	}
	// END KGU#906/KGU#926 2021-02-04
	
	// START KGU#790 2020-11-02: Issue #800
	/**
	 * Get the parsed line structure for the text line {@code lineNo} if it exists.
	 * If the line type (to be obtained with {@link Line#getType()}) is
	 * {@link Line.LineType#LT_RAW} then something will have gone wrong and
	 * the original text line is all you can rely on. The corresponding error
	 * message can be obtained via {@link Line#getParserError()} in this case.<br/>
	 * <b>Note:</b> If there haven't been associated Line structures cached then
	 * this method will do the parsing for all lines of this element and cache the
	 * parsed Lines. If you want to obtain an array of freshly parsed Line objects
	 * without setting the cache then you might consider using
	 * {@link #getParsedText(StringList, TypeRegistry)} instead.
	 * 
	 * @param lineNo - index of the text line (w.r.t. unbroken text) - must be between
	 *     0 and the number of lines - 1.
	 * @return a {@link Line} structure for the requested text line.
	 * 
	 * @see #parseLines(TypeRegistry)
	 * @see #resetParsedLines()
	 */
	public Line getParsedLine(int lineNo)
	{
		if (parsedLines == null) {
			parseLines();
		}
		return parsedLines[lineNo];
	}
	
	/**
	 * Re-parses all text lines no matter whether there have already been parsed
	 * lines in the cache. Overwrites {@link #parsedLines}. Uses the
	 * {@link TypeRegistry} of he owning {@link Root} for data type retrieval.
	 * 
	 * @return A {@link StringList} of possible error descriptions
	 * 
	 * @see #parseLines(TypeRegistry)
	 * @see #getParsedLine(int)
	 * @see #getParsedText(StringList, Typeregistry)
	 * @see #getParsedText(StringList, StringList, Typeregistry, StringList)
	 */
	protected StringList parseLines()
	{
		return parseLines(null);
	}
	/**
	 * Re-parses all text lines no matter whether there have already been parsed
	 * lines in the cache. Overwrites {@link #parsedLines}. Uses the passed-in
	 * {@link TypeRegistry} {@code _dataTypes} for data type retrieval rather than
	 * the one held by the owning {@link Root}.
	 * 
	 * @param _dataTypes - the {@link TypeRegistry} to be used for data type
	 *     retrieval, falls back to {@link Root#getDataTypes()} if {@code null}.
	 * @return A {@link StringList} of possible error descriptions
	 * 
	 * @see #parseLines()
	 * @see #getParsedLine(int)
	 * @see #getParsedText(StringList, Typeregistry)
	 * @see #getParsedText(StringList, StringList, Typeregistry, StringList)
	 */
	protected StringList parseLines(TypeRegistry _dataTypes)
	{
		StringList errors = new StringList();
		StringList unbrokenLines = this.getUnbrokenText();
		parsedLines = new Line[unbrokenLines.count()];
		if (_dataTypes == null) {
			Root root = getRoot(this);
			if (root != null) {
				_dataTypes = root.getDataTypes();
			}
		}
		for (int i = 0; i < parsedLines.length; i++) {
			parsedLines[i] = LineParser.getInstance().parse(unbrokenLines.get(i), this, i, _dataTypes);
			String trouble = parsedLines[i].getParserError();
			if (trouble != null) {
				errors.add(trouble);
			}
		}
		return errors;
	}

	/**
	 * Wipes the parsing results for all text lines, thus forcing a reparsing
	 * on next {@link #getParsedLine(int)} call. This methd should be called
	 * whenever the text was changed.
	 */
	public void resetParsedLines()
	{
		parsedLines = null;
	}
	// END KGU#790 2020-11-02
}
