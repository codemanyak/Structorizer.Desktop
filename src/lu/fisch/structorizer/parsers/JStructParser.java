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

package lu.fisch.structorizer.parsers;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Class JStructParser
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2021-01-20      First Issue for #913
 *      Kay Gürtzig     2021-02-12      Processing of FOR loops revised.
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

import javax.xml.XMLConstants;
import javax.xml.parsers.*;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import lu.fisch.utils.*;
import lu.fisch.structorizer.archivar.ArchivePool;
import lu.fisch.structorizer.elements.*;
import lu.fisch.structorizer.executor.Function;
import lu.fisch.structorizer.io.Ini;

/**
 * Imports a JStruct file converting the structure into a roughly analogous archive
 * of diagrams that can be used to form an arrangement group.<br/>
 * JStruct is a fork of Structorizer 3.20 adapted for mere Java code design and
 * "reverse engineering", i.e. Java code import "as is".<br/>
 * @author Kay Gürtzig
 */
public class JStructParser extends DefaultHandler {

	public static final Logger logger = Logger.getLogger(JStructParser.class.getName());

	private static Schema nsdSchema = null;
	
	private static final String[] ACCESS = new String[] {"public", "protected", "private", "static"};
	// Type names typically found in the first zone of a for loop header
	private static final StringList AUTO_INCR = StringList.explode("+,+", ",");
	private static final StringList AUTO_DECR = StringList.explode("-,-", ",");
	private static final StringList AUTO_ADD = StringList.explode("+,=", ",");
	private static final StringList AUTO_SUBT = StringList.explode("-,=", ",");
	
	private ArchivePool archive = null;
	private String filePath = "";
	
	private Root root = null;
	
	private Stack<Element>  stack   = new Stack<Element>();
	private Stack<Subqueue> ifStack = new Stack<Subqueue>();
	private Stack<Subqueue> qStack  = new Stack<Subqueue>();
	private Stack<Case>     cStack  = new Stack<Case>();
	private Stack<Parallel> pStack  = new Stack<Parallel>();
	// START KGU#686 2019-03-17: Enh. #56
	private Stack<Try> tryStack = new Stack<Try>();
	// END KGU#686 2019-03-17
	
	/** current {@link Subqueue} Elements are to be added to */
	private Subqueue lastQ = null;
	/** Current {@link Element} being parsed */
	private Element lastE = null;
	
	private Root lastIncludable = null;

	// START KGU#400 2017-06-20: Issue #404
	public boolean validationError = false;  
	public SAXParseException saxParseException = null;

	private boolean convertSyntax = false;
	
	public void error(SAXParseException exception) throws SAXException
	{
		validationError = true;
		saxParseException = exception;
	}

	public void fatalError(SAXParseException exception) throws SAXException
	{
		validationError = true;	    
		saxParseException = exception;
	}

	public void warning(SAXParseException exception) throws SAXException
	{
		logger.log(Level.WARNING, "", exception);
	}
	// END KGU#400 2017-06-20
	
	@Override
	public void startElement(String namespaceUri, String localName, String qualifiedName, Attributes attributes) throws SAXException 
	{
//		System.out.println("start " + qualifiedName + " entry");
//		System.out.println("stack:\t" + stack.size());
//		System.out.println("ifStack:\t" + ifStack.size());
//		System.out.println("tryStack:\t" + tryStack.size());
//		System.out.println("qStack:\t" + qStack.size());
//		System.out.println("pStack:\t" + pStack.size());
//		System.out.println("cStack:\t" + cStack.size() + "\n");
		// --- ELEMENTS ---
		// START KGU#913 2021-01-20: Enh. JStruct file import
		if (qualifiedName.equals("jclass")) {
			// read attributes
			root = new Root();
			root.setProgram(true);
			root.isBoxed = false;
			if (attributes.getIndex("code") != -1) {
				root.getText().setCommaText(attributes.getValue("code"));
			}
			if (attributes.getIndex("comment") != -1) {
				root.getComment().setCommaText(attributes.getValue("comment"));
			}
			if (attributes.getIndex("color") != -1 && !attributes.getValue("color").equals("")) {
				root.setColor(Color.decode("0x"+attributes.getValue("color")));
			}
			root.origin = ("JStruct " + filePath).trim();
						
			this.convertJava2Struct(root);
			
			StringList text = root.getText();
			StringList classVars = null;
			// Check for class attributes and form an Includable if there are some
			int posGlobs = text.indexOf("/* class global variables */");
			int nLines = text.count();
			if (posGlobs >= 0) {
				classVars = root.getText().subSequence(posGlobs + 1, nLines);
				text.remove(posGlobs, nLines);
			}
			// First decompose the header lines
			for (int i = 0; i < nLines; i++) {
				String line = text.get(i);
				int posClass = line.indexOf("class");
				if (posClass >= 0) {
					root.getComment().add(text);
					root.setText(line.substring(posClass + 5).trim());
					break;
				}
			}
			// Now extract the fields
			lastIncludable = null;
			if (posGlobs >= 0) {
				if (!classVars.isEmpty()) {
					nLines = classVars.count();
					lastIncludable = new Root();
					lastIncludable.setInclude();
					lastIncludable.setText(root.getMethodName() + "Attributes");
					for (int i = 0; i < nLines; i++) {
						String comment = "";
						String line = classVars.get(i).trim();
						if (line.startsWith("//")) {
							continue;
						}
						else if (line.startsWith("@")) {
							root.comment.add(line);
							continue;
						}
						StringList tokens = Element.splitLexically(classVars.get(i), true);
						if (convertSyntax) {
							int posFinal = tokens.indexOf("final");
							String var = "var";
							if (tokens.contains("<-")) {
								var = "";
							}
							if (posFinal >= 0) {
								tokens.replaceAll("final", "const");
								var = "";
							}
							for (String modifier: ACCESS) {
								if (tokens.replaceAll(modifier, var) > 0) {
									comment += modifier + " ";
									var = "";
								}
							}
						}
						Instruction instr = new Instruction(tokens.trim().concatenate(null));
						if (!(comment = comment.trim()).isEmpty()) {
							instr.setComment(comment);
						}
						lastIncludable.children.addElement(instr);
					}
				}
			}
			
			// place stack
			lastE = root;
			stack.push(root);
		}
		else if (qualifiedName.equals("method"))
		{
			root = new Root();
			// read attributes
			root.setProgram(false);
			root.isBoxed = false;
			if (attributes.getIndex("code") != -1) {
				root.getText().setCommaText(attributes.getValue("code"));
			}
			if (attributes.getIndex("comment") != -1) {
				root.getComment().setCommaText(attributes.getValue("comment"));
			}
			if (attributes.getIndex("color") != -1 && !attributes.getValue("color").equals("")) {
				root.setColor(Color.decode("0x"+attributes.getValue("color")));
			}
			root.origin = ("JStruct " + filePath).trim();
			
			int i = 0;
			StringList text = root.getText();
			while (i < text.count()) {
				String line = text.get(i).trim();
				if (line.startsWith("@")) {
					root.comment.add(line);
					text.remove(i);
				}
				else {
					if (convertSyntax) {
						StringList tokens = Element.splitLexically(line, true);
						boolean modified = false;
						for (String modifier: ACCESS) {
							if (tokens.replaceAll(modifier, "") > 0) {
								root.comment.add(modifier);
								modified = true;
							}
						}
						if (!tokens.trim().isEmpty() && tokens.get(0).equals("void")) {
							tokens.remove(0);
							modified = true;
						}
						int posThrows = tokens.indexOf("throws");
						if (posThrows >= 0) {
							root.comment.add(tokens.subSequence(posThrows, tokens.count()).concatenate());
							tokens.remove(posThrows, tokens.count());
							modified = true;
						}
						if (modified) {
							text.set(i, tokens.concatenate().trim());
						}
					}
					i++;
				}
			}
			
			// place stack
			lastE = root;
			stack.push(root);
		}
		// END KGU#913 2021-01-20
		else if (qualifiedName.equals("instruction"))
		{
			// create element
			Instruction ele = new Instruction(StringList.getNew("???"));
			
			// read attributes
			if (attributes.getIndex("code") != -1) {
				ele.getText().setCommaText(attributes.getValue("code"));
			}
			if (attributes.getIndex("comment") != -1) {
				ele.getComment().setCommaText(attributes.getValue("comment"));
			}
			if (attributes.getIndex("color") != -1 && !attributes.getValue("color").equals("")) {
				ele.setColor(Color.decode("0x"+attributes.getValue("color")));
			}
			
			convertJava2Struct(ele);
			
			// place stack
			lastE = ele;
			stack.push(ele);
			lastQ.addElement(ele);
		}
		else if (qualifiedName.equals("jump"))
		{
			// create element
			Jump ele = new Jump(StringList.getNew("???"));
			
			// read attributes
			if (attributes.getIndex("code") != -1) {
				ele.getText().setCommaText(attributes.getValue("code"));
			}
			if (attributes.getIndex("comment") != -1) {
				ele.getComment().setCommaText(attributes.getValue("comment"));
			}
			if (attributes.getIndex("color") != -1 && !attributes.getValue("color").equals("")) {
				ele.setColor(Color.decode("0x"+attributes.getValue("color")));
			}
			
			convertJava2Struct(ele);
			
			// place stack
			lastE = ele;
			stack.push(ele);
			lastQ.addElement(ele);
		}
		else if (qualifiedName.equals("call"))
		{
			// create element
			Call ele = new Call(StringList.getNew("???"));
			
			// read attributes
			if (attributes.getIndex("code") != -1) {
				ele.getText().setCommaText(attributes.getValue("code"));
			}
			if (attributes.getIndex("comment") != -1) {
				ele.getComment().setCommaText(attributes.getValue("comment"));
			}
			if (attributes.getIndex("color") != -1 && !attributes.getValue("color").equals("")) {
				ele.setColor(Color.decode("0x"+attributes.getValue("color")));
			}
			
			convertJava2Struct(ele);
			
			// place stack
			lastE = ele;
			stack.push(ele);
			lastQ.addElement(ele);
		}
		else if (qualifiedName.equals("alternative"))
		{
			// create element
			Alternative ele = new Alternative(StringList.getNew("???"));
			
			// read attributes
			if (attributes.getIndex("code") != -1) {
				ele.getText().setCommaText(attributes.getValue("code"));
			}
			if (attributes.getIndex("comment") != -1) {
				ele.getComment().setCommaText(attributes.getValue("comment"));
			}
			if (attributes.getIndex("color") != -1 && !attributes.getValue("color").equals("")) {
				ele.setColor(Color.decode("0x"+attributes.getValue("color")));
			}

			// Get rid of a postfix question mark unless it is accepted by the parser
			if (!CodeParser.getKeyword("postAlt").equals("?")) {
				StringList text = ele.getText();
				if (text.get(text.count()-1).trim().equals("?")) {
					text.remove(text.count()-1);
				}
			}
			convertJava2Struct(ele);
			
			// set children 
			ele.qTrue.setColor(ele.getColor());
			ele.qFalse.setColor(ele.getColor());
			
			// place stack
			lastE = ele;
			stack.push(ele);
			lastQ.addElement(ele);
		}
		else if (qualifiedName.equals("while"))
		{
			// create element
			While ele = new While(StringList.getNew("???"));
			
			
			// read attributes
			if (attributes.getIndex("code") != -1) {
				ele.getText().setCommaText(attributes.getValue("code"));
			}
			if (attributes.getIndex("comment") != -1) {
				ele.getComment().setCommaText(attributes.getValue("comment"));
			}
			if (attributes.getIndex("color") != -1 && !attributes.getValue("color").equals("")) {
				ele.setColor(Color.decode("0x"+attributes.getValue("color")));
			}
			
			convertJava2Struct(ele);
			
			// set children
			ele.q.setColor(ele.getColor());
			
			// place stack
			lastE = ele;
			stack.push(ele);
			lastQ.addElement(ele);
		}
		else if (qualifiedName.equals("for"))
		{
			// create element
			Element ele = new For(StringList.getNew("???"));
			
			// read attributes
			if (attributes.getIndex("code") != -1) {
				ele.getText().setCommaText(attributes.getValue("code"));
			}
			if (attributes.getIndex("comment") != -1) {
				ele.getComment().setCommaText(attributes.getValue("comment"));
			}
			if (attributes.getIndex("color") != -1 && !attributes.getValue("color").equals("")) {
				ele.setColor(Color.decode("0x"+attributes.getValue("color")));
			}

			convertJava2Struct(ele);
			String asgnOpr = convertSyntax ? "<-" : "=";

			/* Parse and decompose the content - will either be something like
			 * for (int i = a; a < b; a++)
			 * or
			 * for (Object x: coll)
			 */
			((For)ele).style = For.ForLoopStyle.FREETEXT;
			String text = ele.getText().getLongString();
			StringList tokens = Element.splitLexically(text, true);
			tokens.removeAll(" ");
			if (tokens.count() >= 5 && tokens.get(0).equals("for")
					&& tokens.get(1).equals("(") && tokens.get(tokens.count()-1).equals(")")) {
				StringList exprs = Element.splitExpressionList(tokens.subSequence(2, tokens.count()), ";", true);
				if (exprs.count() == 4) {
					// Seems to be a counter-controlled loop
					String loopVar = null;
					String startVal = null;
					String endVal = null;
					int stepConst = 1;
					boolean allSet = true;
					// Parse the initialisation
					tokens = Element.splitLexically(exprs.get(0), true);
					tokens.removeAll(" ");
					int posOpr = tokens.indexOf(asgnOpr);
					if (posOpr > 0) {
						// Get the variable
						StringList varTokens = tokens.subSequence(0, posOpr).trim();
						// Try to cut off the type (we might of course insert a declaration)
						if (Function.testIdentifier(varTokens.get(varTokens.count()-1), false, null)) {
							varTokens.remove(0, varTokens.count()-1);
						}
						loopVar = varTokens.concatenate(null);
						((For)ele).setCounterVar(loopVar);	// Won't help
						// Get the start value
						startVal = tokens.concatenate(null, posOpr+1);
						((For)ele).setStartValue(startVal);	// Won't help
					}
					else {
						allSet = false;
					}
					// Try to extract the step value
					tokens = Element.splitLexically(exprs.get(2), true);
					tokens.removeAll(" ");
					if ((posOpr = tokens.indexOf("++")) >= 0 
							|| (posOpr = tokens.indexOf(AUTO_INCR, 0, true)+1) > 0) {
						stepConst = 1;
						((For)ele).setStepConst(1);
					}
					else if ((posOpr = tokens.indexOf("--")) >= 0
							|| (posOpr = tokens.indexOf(AUTO_DECR, 0, true)+1) > 0) {
						stepConst = -1;
						((For)ele).setStepConst(-1);
					}
					else if ((posOpr = tokens.indexOf("+=")) >= 0
							|| (posOpr = tokens.indexOf(AUTO_ADD, 0, true)+1) > 0) {
						String stepString = tokens.concatenate(null, posOpr+1);
						allSet = ((For)ele).setStepConst(stepString) && allSet;
						if (allSet) {
							stepConst = Integer.parseInt(stepString);
						}
					}
					else if ((posOpr = tokens.indexOf("-=")) >= 0
							|| (posOpr = tokens.indexOf(AUTO_SUBT, 0, true)+1) > 0) {
						String stepString = "-" + tokens.concatenate(null, posOpr+1);
						allSet = ((For)ele).setStepConst(stepString) && allSet;
						if (allSet) {
							stepConst = Integer.parseInt(stepString);
						}
					}
					else {
						allSet = false;
					}
					// Try to get the end value from the condition
					tokens = Element.splitLexically(exprs.get(1), true);
					if ((posOpr = tokens.indexOf("<")) >= 0
							|| (posOpr = tokens.indexOf(">")) >= 0
							|| (posOpr = tokens.indexOf("<=")) >= 0
							|| (posOpr = tokens.indexOf(">=")) >= 0
							|| (posOpr = tokens.indexOf("==")) >= 0
							|| (posOpr = tokens.indexOf("!=")) >= 0
							|| convertSyntax && (posOpr = tokens.indexOf("=")) >= 0
							|| convertSyntax && (posOpr = tokens.indexOf("<>")) >= 0) {
						// FIXME: Too rough, could be a combined condition
						String opr = tokens.get(posOpr);
						endVal = tokens.concatenate(null, posOpr+1);
						int step = ((For)ele).getStepConst();
						if (opr.equals("<") || (opr.equals("!=") || opr.equals("<>")) && step > 0) {
							endVal = "(" + endVal + ") - 1";
						}
						else if ((opr.equals(">") || opr.equals("!=") || opr.equals("<>")) && step < 0) {
							endVal = "(" + endVal + ") + 1";
						}
						((For)ele).setEndValue(endVal);	// Won't help
					}
					else {
						allSet = false;
					}
					if (convertSyntax) {
						StringList comment = ele.getComment();
						comment.add(text);
						if (allSet) {
							ele = new For(loopVar, startVal, endVal, stepConst);
						}
						else {
							// Better produce a While loop
							Instruction instr = new Instruction(exprs.get(0));
							instr.setColor(ele.getColor());
							lastQ.addElement(instr);
							ele = new While(exprs.get(1));
							ele.setColor(instr.getColor());
							instr = new Instruction(exprs.get(2));
							instr.setColor(ele.getColor());
							// This will have to be placed at the end in endElement()
							((While)ele).q.addElement(instr);
						}
					}
				}
				else {
					exprs = Element.splitExpressionList(tokens.subSequence(2, tokens.count()), ":", true);
					if (exprs.count() == 3 && exprs.get(2).equals(")")) {
						// Try to get the variable
						String var = exprs.get(0);
						if (convertSyntax && var.contains(" ")) {
							// Try to cut off the type
							StringList varTokens = Element.splitLexically(var, true);
							varTokens.removeAll(" ");
							if (Function.testIdentifier(varTokens.get(varTokens.count()-1), false, null)) {
								var = varTokens.get(varTokens.count()-1);
							}
						}
						((For)ele).setCounterVar(var);
						//tokens = Element.splitLexically(exprs.get(0), true);
						// Try to get the collection
						((For)ele).setValueList(exprs.get(1));
						((For)ele).style = For.ForLoopStyle.TRAVERSAL;
					}
					else {
						ele.setColor(Color.RED);
					}
				}
			}
			
			// set children
			((ILoop)ele).getBody().setColor(ele.getColor());
			
			// place stack
			lastE = ele;
			stack.push(ele);
			lastQ.addElement(ele);
		}
		else if (qualifiedName.equals("repeat"))
		{
			// create element
			Repeat ele = new Repeat(StringList.getNew("???"));
			
			// read attributes
			if (attributes.getIndex("code") != -1) {
				ele.getText().setCommaText(attributes.getValue("code"));
			}
			if (attributes.getIndex("comment") != -1) {
				ele.getComment().setCommaText(attributes.getValue("comment"));
			}
			if (attributes.getIndex("color") != -1 && !attributes.getValue("color").equals("")) {
				ele.setColor(Color.decode("0x"+attributes.getValue("color")));
			}

			convertJava2Struct(ele);

			// set children
			ele.q.setColor(ele.getColor());
			
			// place stack
			lastE = ele;
			stack.push(ele);
			lastQ.addElement(ele);
		}
		else if (qualifiedName.equals("case"))
		{
			// create element
			Case ele = new Case(StringList.getNew("???"));

			// read attributes
			if (attributes.getIndex("code") != -1) {
				ele.getText().setCommaText(attributes.getValue("code"));
			}
			ele.qs.clear();
			if (attributes.getIndex("comment") != -1) {
				ele.getComment().setCommaText(attributes.getValue("comment"));
			}
			if (attributes.getIndex("color") != -1 && !attributes.getValue("color").equals("")) {
				ele.setColor(Color.decode("0x"+attributes.getValue("color")));
			}

			convertJava2Struct(ele);

			// place stack
			lastE = ele;
			stack.push(ele);
			lastQ.addElement(ele);
			cStack.push(ele);
		}
		// --- QUEUES ---
		else if (qualifiedName.equals("qCase") || qualifiedName.equals("qCatch"))
		{
			// create new queue
			lastQ = new Subqueue();
			// setup queue
			lastQ.parent = cStack.peek();	// the Case element
			// END KGU 2016-12-21: Bugfix #317
			//lastQ.setColor(lastQ.parent.getColor());
			if (attributes.getIndex("color") != -1 && !attributes.getValue("color").equals("")) {
				lastQ.setColor(Color.decode("0x"+attributes.getValue("color")));
			}
			// END KGU 2016-12-21
			// handle stacks
			cStack.peek().qs.addElement(lastQ);
			qStack.push(lastQ);
		}
		else if (qualifiedName.equals("try"))
		{
			// create element
			Try ele = new Try(StringList.getNew("???"));
			
			// read attributes
			if (attributes.getIndex("comment") != -1) {
				ele.getComment().setCommaText(attributes.getValue("comment"));
			}
			if (attributes.getIndex("color") != -1 && !attributes.getValue("color").equals("")) {
				ele.setColor(Color.decode("0x"+attributes.getValue("color")));
			}
			
			// set children colour (???)
			ele.qTry.setColor(ele.getColor());
			ele.qCatch.setColor(ele.getColor());
			ele.qFinally.setColor(ele.getColor());
			
			// place stack
			lastE = ele;
			stack.push(ele);
			lastQ.addElement(ele);
			tryStack.push(ele);
		}
		else if (qualifiedName.equals("catch")) {
			lastQ = tryStack.peek().qCatch;
			//qStack.push(lastQ);
			// create element
			Case ele = new Case(StringList.getNew("???"));

			// read attributes
			if (attributes.getIndex("code") != -1) {
				ele.getText().setCommaText(attributes.getValue("code"));
			}
			ele.qs.clear();
			if (attributes.getIndex("comment") != -1) {
				ele.getComment().setCommaText(attributes.getValue("comment"));
			}
			if (attributes.getIndex("color") != -1 && !attributes.getValue("color").equals("")) {
				ele.setColor(Color.decode("0x"+attributes.getValue("color")));
			}
			
			// Identify the exception variables in order to unify them
			StringList text = ele.getText();
			String exName = null;
			for (int i = 1; i < text.count()-1; i++) {
				StringList tokens = Element.splitLexically(text.get(i), true);
				tokens.removeAll(" ");
				if (tokens.count() > 1) {
					String lastId = tokens.get(tokens.count()-1);
					if (exName == null) {
						exName = lastId;
					}
					else if (!exName.equals(lastId)) {
						exName = null;
						break;
					}
				}
			}
			if (exName != null) {
				tryStack.peek().setText(exName);
				if (text.count() > 2 && text.get(0).equals("catch")) {
					text.set(0, exName + " instanceof");
				}
				for (int i = 1; i < text.count()-1; i++) {
				StringList tokens = Element.splitLexically(text.get(i), true).trim();
				String lastId = tokens.get(tokens.count()-1);
				if (exName.equals(lastId)) {
					tokens.remove(tokens.count()-1);
					text.set(i, tokens.trim().concatenate());
				}
			}
			}
			
			// place stack
			lastE = ele;
			stack.push(ele);
			lastQ.addElement(ele);
			cStack.push(ele);
		}
		// END KGU#913 2021-01-20
		else if (qualifiedName.equals("body"))
		{
			// handle stacks
			lastQ = root.children;
			// START KGU 2106-12-21: Bugfix #317
			if (attributes.getIndex("color")!=-1 && !attributes.getValue("color").equals("")) {
				lastQ.setColor(Color.decode("0x"+attributes.getValue("color")));
			}
			// END KGU 2016-12-21
			qStack.push(lastQ);
		}
		else if (qualifiedName.equals("qTrue"))
		{
			// create new queue
			lastQ = ((Alternative) lastE).qTrue;
			// START KGU 2016-12-21: Bugfix #317
			if (attributes.getIndex("color") !=-1 && !attributes.getValue("color").equals("")) {
				lastQ.setColor(Color.decode("0x"+attributes.getValue("color")));
			}
			// END KGU 2016-12-21
			ifStack.push(((Alternative) lastE).qFalse);
			qStack.push(lastQ);
		}
		else if (qualifiedName.equals("qFalse"))
		{
			// handle stacks
			lastQ = ifStack.pop();
			// START KGU 2106-12-21: Bugfix #317
			if (attributes.getIndex("color") != -1 && !attributes.getValue("color").equals("")) {
				lastQ.setColor(Color.decode("0x"+attributes.getValue("color")));
			}
			// END KGU 2016-12-21
			qStack.push(lastQ);
		}
		else if (qualifiedName.equals("qFor"))
		{
			// handle stacks
			// Caution: lastE might be a replacing While element!
			lastQ = ((ILoop) lastE).getBody();
			// START KGU 2106-12-21: Bugfix #317
			if (attributes.getIndex("color") != -1 && !attributes.getValue("color").equals("")) {
				lastQ.setColor(Color.decode("0x"+attributes.getValue("color")));
			}
			// END KGU 2016-12-21
			qStack.push(lastQ);
		}
		else if (qualifiedName.equals("qWhile"))
		{
			// handle stacks
			lastQ = ((While) lastE).q;
			// START KGU 2106-12-21: Bugfix #317
			if (attributes.getIndex("color") != -1 && !attributes.getValue("color").equals("")) {
				lastQ.setColor(Color.decode("0x"+attributes.getValue("color")));
			}
			// END KGU 2016-12-21
			qStack.push(lastQ);
		}
		else if (qualifiedName.equals("qRepeat"))
		{
			// handle stacks
			lastQ = ((Repeat) lastE).q;
			// START KGU 2106-12-21: Bugfix #317
			if (attributes.getIndex("color") != -1 && !attributes.getValue("color").equals("")) {
				lastQ.setColor(Color.decode("0x"+attributes.getValue("color")));
			}
			// END KGU 2016-12-21
			qStack.push(lastQ);
		}
		// START KGU#686 2019-03-17: Enh. #56
		else if (qualifiedName.equals("qTry"))
		{
			// create new queue
			lastQ = tryStack.peek().qTry;
			// START KGU 2106-12-21: Bugfix #317
			if (attributes.getIndex("color") != -1 && !attributes.getValue("color").equals("")) {
				lastQ.setColor(Color.decode("0x"+attributes.getValue("color")));
			}
			// END KGU 2016-12-21
			qStack.push(lastQ);
		}
		else if (qualifiedName.equals("qFinally"))
		{
			lastQ = tryStack.peek().qFinally;
			// START KGU 2106-12-21: Bugfix #317
			if (attributes.getIndex("color") != -1 && !attributes.getValue("color").equals("")) {
				lastQ.setColor(Color.decode("0x"+attributes.getValue("color")));
			}
			// END KGU 2016-12-21
			qStack.push(lastQ);
		}
		// END KGU#686 2019-03-17: Enh. #56
//		System.out.println("start " + qualifiedName + " EXIT");
//		System.out.println("stack:\t" + stack.size());
//		System.out.println("ifStack:\t" + ifStack.size());
//		System.out.println("tryStack:\t" + tryStack.size());
//		System.out.println("qStack:\t" + qStack.size());
//		System.out.println("pStack:\t" + pStack.size());
//		System.out.println("cStack:\t" + cStack.size() + "\n");
	}

//	// START KGU#913 2021-02-12: Enh. #913 FOR loops may not remain as they are
//	/**
//	 * More specific version of {@link #convertJava2Struct(Element)} for
//	 * {@link For} elements. Tries to make sense of Java-specific FOR loop
//	 * syntax.<br/>
//	 * Only the text syntax of the given {@link For} loop {@code _for}
//	 * if specified by {@link #convertSyntax}, which is set according to
//	 * import preference "impConvertJStruct".
//	 * @param _for - the imported {@link For}
//	 * @return the element (or some replacement for it (e.g. a {@link Subqueue})
//	 */
//	private Element convertJava2Struct(For _for) {
//		Element ele = _for;
//		if (convertSyntax) {
//			String text = _for.getUnbrokenText().getLongString();
//			StringList tokens = Element.splitLexically(text, true);
//			tokens.removeAll(" ");
//			if (tokens.count() < 5 || !tokens.get(0).equals("for")
//					|| !tokens.get(1).equals("(")
//					|| !tokens.get(tokens.count()-1).equals(")")) {
//				// Something is wrong here
//				_for.setColor(Color.RED);
//				return _for;
//			}
//			// Let's see whether it is a count-controlled loop
//			StringList exprs = Element.splitExpressionList(
//					tokens.subSequence(2, tokens.count()), ";", true);
//			if (exprs.count() == 4 && exprs.get(3).equals(")")) {
//				// It should be a counting loop, so try the best
//				boolean ok = true;
//				//newText.add(CodeParser.getKeywordOrDefault("preFor", "for"));
//				// Check the first zone (initialisation)
//				tokens = Element.splitLexically(exprs.get(0), true);
//				tokens.replaceAll("=", "<-");
//				if (tokens.count() >= 3) {
//					String[] parts = new String[] {null, null, null};
//					if (INTEGRAL_TYPES.contains(tokens.get(0))) {
//						tokens.remove(0);
//					}
//					if (tokens.get(1).equals("<-")
//							&& Function.testIdentifier(tokens.get(0), false, null)) {
//						parts[0] = tokens.get(0);
//						parts[1] = tokens.concatenate(null, 2);
//					}
//					else {
//						ok = false;
//					}
//					// Check the third zone (increment)
//					int step = 1;
//					tokens = Element.splitLexically(exprs.get(2), true);
//					if (tokens.count() == 3) {
//						int posIncr = tokens.indexOf(AUTO_INCR, 0, true);
//						int posDecr = tokens.indexOf(AUTO_DECR, 0, true);
//						if ((posIncr == 0 || posDecr == 0) && tokens.get(2).equals(parts[0])
//								|| (posIncr == 1 || posDecr == 1) && tokens.get(0).equals(parts[0])) {
//							step = posDecr >= 0 ? -1 : 1;
//						}
//					}
//					else if (tokens.count() == 4 && tokens.get(0).equals(parts[0])) {
//						int posIncr = tokens.indexOf(AUTO_ADD, 0, true);
//						int posDecr = tokens.indexOf(AUTO_SUBT, 0, true);
//						if (posIncr == 1 || posDecr == 1) {
//							try {
//								step = - Integer.parseInt(tokens.get(3));
//								if (posDecr == 1) {
//									step = -1;
//								}
//							}
//							catch (NumberFormatException exc) {
//								ok = false;
//							}
//						}
//					}
//					else {
//						ok = false;
//					}
//					// Check the second zone (condition)
//					tokens = Element.splitLexically(exprs.get(1), true);
//					if (tokens.count() >= 3 && tokens.get(0).equals(parts[0])
//							&& COMP_OPRS.contains(tokens.get(1))) {
//						String endVal = tokens.concatenate(null, 2);
//						if (tokens.get(1).equals("<")
//								|| tokens.get(1).equals("!=") && step > 0) {
//							parts[2] = "(" + endVal + ") - 1";
//						}
//						else if (tokens.get(1).equals(">")
//								|| tokens.get(1).equals("!=") && step < 0) {
//							parts[2] = "(" + endVal + ") + 1";
//						}
//						else if (!tokens.get(1).equals("==")) {
//							parts[2] = endVal;
//						}
//						else {
//							ok = false;
//						}
//					}
//					if (ok) {
//						StringList comment = _for.getComment();
//						ele = new For(parts[0], parts[1], parts[2], step);
//						ele.setComment(comment);
//					}
//				}
//				if (!ok) {
//					// Form a while loop.
//					Subqueue sq = new Subqueue();
//					While whil = new While(exprs.get(1));
//					sq.addElement(convertJava2Struct(new Instruction(exprs.get(0))));
//					sq.addElement(whil);
//					whil.q.addElement(convertJava2Struct(new Instruction(exprs.get(2))));
//					ele = sq;
//				}
//			}
//			else {
//				// Apparently a collection-controlled loop
//				exprs = Element.splitExpressionList(
//						tokens.subSequence(2, tokens.count()), ":", true);
//				if (exprs.count() == 3) {
//					Subqueue sq = null;
//					StringList newText = new StringList();
//					newText.add(CodeParser.getKeyword("preForIn"));
//					tokens = Element.splitLexically(exprs.get(0), true);
//					if (tokens.count() > 1) {
//						// Seems to be a declaration
//						sq = new Subqueue();
//						// Transform the declaration, somehow for now
//						sq.addElement(new Instruction(
//								"var " + tokens.get(tokens.count()-1)
//								+ ": " + tokens.concatenate(null, 0, tokens.count()-1)));
//					}
//					if (!tokens.isEmpty()) {
//						newText.add(tokens.get(tokens.count()-1));
//					}
//					else {
//						newText.add("dummy" + Integer.toHexString(_for.hashCode()));
//					}
//					newText.add(CodeParser.getKeyword("postForIn"));
//					newText.add(exprs.get(1));
//					_for.setText(newText.concatenate(null));
//					if (sq != null) {
//						sq.addElement(_for);
//						ele = sq;
//					}
//				}
//			}
//		}
//		return ele;
//	}
//	// END KGU#913 2021-02-12
	
	/**
	 * Converts the syntax of the text of element {@code ele} from Java to
	 * Structorizer if specified by {@link #convertSyntax}, which is set
	 * according to import preference "impConvertJStruct".
	 * @param ele - the imported {@link Element}
	 */
	private void convertJava2Struct(Element ele) {
		if (convertSyntax) {
			for (int i = 0; i < ele.getText().count(); i++) {
				StringList tokens = Element.splitLexically(ele.getText().get(i), true);
				boolean changed = tokens.replaceAll("=", "<-") > 0;
				int j = tokens.count()-1;
				while (j >= 0 && tokens.get(j).trim().isEmpty()) {j--;}
				if (tokens.get(j).equals(";")) {
					tokens.remove(j, tokens.count());
					changed = true;
				}
				if (changed) {
					ele.getText().set(i,  tokens.concatenate());
				}
			}
		}
	}

	@Override
	public void endElement(String namespaceUri, String localName, String qualifiedName) throws SAXException 
	{
//		System.out.println("end " + qualifiedName + " entry");
//		System.out.println("stack:\t" + stack.size());
//		System.out.println("ifStack:\t" + ifStack.size());
//		System.out.println("tryStack:\t" + tryStack.size());
//		System.out.println("qStack:\t" + qStack.size());
//		System.out.println("pStack:\t" + pStack.size());
//		System.out.println("cStack:\t" + cStack.size() + "\n");
		// --- STRUCTURES ---
		if (qualifiedName.equals("call") ||
		   qualifiedName.equals("jump") ||
		   qualifiedName.equals("instruction") ||
		   qualifiedName.equals("alternative") ||
		   qualifiedName.equals("while") ||
		   qualifiedName.equals("repeat") ||
		   qualifiedName.equals("for") 
		   )
		{
			lastE = stack.pop();
		}
		else if (qualifiedName.equals("case") ||
				qualifiedName.equals("catch"))
		{
			lastE = stack.pop();
			cStack.pop();
		}
		else if (qualifiedName.equals("try"))
		{
			lastE = stack.pop();
			tryStack.pop();			
		}
		// -- QUEUES ---
		else if (qualifiedName.equals("qFor")) {
			// Check if the parent is a substituting While
			if (lastQ.parent instanceof While) {
				// The put the first instruction (the step!) to the end
				if (lastQ.getSize() > 0) {
					Element stepInstr = lastQ.getElement(0);
					lastQ.removeElement(0);
					lastQ.addElement(stepInstr);
				}
			}
			qStack.pop();
			lastQ = qStack.peek();
		}
		else if(qualifiedName.equals("qCase") ||
				qualifiedName.equals("qPara") ||
				qualifiedName.equals("qForever") ||
				qualifiedName.equals("qWhile") ||
				qualifiedName.equals("qRepeat") ||
				// START KGU#686 2019-03-17: Enh. #56
				qualifiedName.equals("qTry") ||
				qualifiedName.equals("qCatch") ||
				qualifiedName.equals("qFinally") ||
				// END KGU#686 2019-03-17
				qualifiedName.equals("qTrue") ||
				qualifiedName.equals("qFalse")
				)
		{
			qStack.pop();
			lastQ = qStack.peek();
		}
		else if(qualifiedName.equals("body"))
		{
			lastQ = qStack.pop();
		}
		else if (qualifiedName.equals("jclass") ||
				qualifiedName.equals("method")) {
			lastE = stack.pop();
			if (lastIncludable != null) {
				((Root)lastE).addToIncludeList(lastIncludable);
				if (qualifiedName.equals("jclass")) {
					archive.addDiagram(lastIncludable);
				}
			}
			archive.addDiagram((Root)lastE);
		}
		// START KGU 2019-03-17: Was dead code ("qTrue" had already been handled above...)
		//else if (qualifiedName.equals("qTrue"))
		//{
		//	lastQ = qStack.pop();
		//}
		// END KGU 2019-03-17
//		System.out.println("end " + qualifiedName + " EXIT");
//		System.out.println("stack:\t" + stack.size());
//		System.out.println("ifStack:\t" + ifStack.size());
//		System.out.println("tryStack:\t" + tryStack.size());
//		System.out.println("qStack:\t" + qStack.size());
//		System.out.println("pStack:\t" + pStack.size());
//		System.out.println("cStack:\t" + cStack.size() + "\n");
	}
	
	@Override
	public void characters(char[] chars, int startIndex, int endIndex) 
	{
		//String dataString =	new String(chars, startIndex, endIndex).trim();
	}
	
	// START KGU#363 2017-05-21: Issue #372 It was sensible to change this signature
//	/**
//	 * Parses the NSD file specified by the URI (!) {@code _filename} and returns the composed Root if possible
//	 * @param _filename a URI specifying the file path. CAUTION: This string is not expected to be usable for e.g.
//	 * {@code new File(_filename)}. It may be derived e.g. from a {@code File} object f by {@code f.toURI().toString()}.  
//	 * @return the built diagram
//	 * @throws SAXException
//	 * @throws IOException
//	 */
//	public Root parse(String _filename) throws SAXException, IOException
	/**
	 * Parses the NSD file specified by the given {@code File} object {@code _file} and returns the
	 * composed {@link Root} (if possible), otherwise raises exceptions. 
	 * @param _file - a {@code File} object representing the NSD file to be parsed.  
	 * @return the built diagram
	 * @throws SAXException
	 * @throws IOException
	 */
	public ArchivePool parse(File _file) throws SAXException, IOException
	{
		// setup a new archive
		archive = new ArchivePool(_file.getName());
		filePath = _file.getAbsolutePath();
		
		// clear stacks
		stack.clear();
		ifStack.clear();
		qStack.clear();
		cStack.clear();
		pStack.clear();
		// START KGU#686 2019-03-17: Enh. #56
		tryStack.clear();
		// END KGU#686 2019-03-17
				
		Ini ini = Ini.getInstance();
		ini.load();
		convertSyntax = ini.getProperty("impConvertJStruct", "false").equals("true");

		SAXParserFactory factory = SAXParserFactory.newInstance();
		// START KGU#400 2017-06-20: Issue #404
		if (nsdSchema == null) {
			URL schemaLocal = this.getClass().getResource("structorizer.xsd");
			SchemaFactory sFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			try {
				nsdSchema = sFactory.newSchema(schemaLocal);
			} catch (SAXException ex) {
				logger.log(Level.WARNING, "structorizer.xsd", ex);
			}
		}
		// FIXME: This doesn't work properly -maybe it requires full tag qualification
		//factory.setNamespaceAware(true);
//		factory.setValidating(true);
//		factory.setSchema(nsdSchema);
		// END KGU#400 2017-06-20
		try		
		{
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(_file/*.toURI().toString()*/, this);
		} 
		catch(Exception e) 
		{
			String errorMessage = "Error parsing " + _file + ":";
			logger.log(Level.SEVERE, errorMessage, e);
			// START KGU#111 2015-12-16: Bugfix #63 re-throw the exception!
			if (e instanceof SAXException)
			{
				throw (SAXException)e;
			}
			else if (e instanceof IOException)
			{
				throw (IOException)e;
			}
			// END KGU#111 2015-12-16
		}
		
		// START KGU#137 2016-01-11: In theory no longer needed - should have been initialized so
		//root.hasChanged=false;
		// END KGU#137 2016-01-11
		
		return archive;
	}
	
	// START KGU#177 2016-04-14: Enh. 158 - we need an opportunity to parse an XML string as well
	// (FIXME: This is just a copy-and-paste clone of Root parse(String _filename))
	public Root parse(InputStream _is) throws SAXException, IOException
	{
		// setup a new root
		root = new Root();
		
		// clear stacks
		stack.clear();
		ifStack.clear();
		qStack.clear();
		cStack.clear();
		pStack.clear();
		// START KGU#686 2019-03-17: Enh. #56
		tryStack.clear();
		// END KGU#686 2019-03-17
						
		SAXParserFactory factory = SAXParserFactory.newInstance();
		// START KGU#400 2017-06-20: Issue #404
		if (nsdSchema == null) {
			URL schemaLocal = this.getClass().getResource("structorizer.xsd");
			SchemaFactory sFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			try {
				nsdSchema = sFactory.newSchema(schemaLocal);
			}
			catch (SAXException ex) {
				logger.log(Level.WARNING, "structorizer.xsd", ex);
			}
		}
		// FIXME: This doesn't work properly
		factory.setNamespaceAware(true);
		factory.setValidating(true);
		factory.setSchema(nsdSchema);
		// END KGU#400 2017-06-20
		try		
		{
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(_is, this);
		} 
		catch(Exception e) 
		{
			String errorMessage = "Error parsing NSD:";
			logger.log(Level.SEVERE, errorMessage, e);
			// START KGU#111 2015-12-16: Bugfix #63 re-throw the exception!
			if (e instanceof SAXException)
			{
				throw (SAXException)e;
			}
			else if (e instanceof IOException)
			{
				throw (IOException)e;
			}
			// END KGU#111 2015-12-16
		}
		return root;
	}
	// END KGU#177 2016-04-14
}
