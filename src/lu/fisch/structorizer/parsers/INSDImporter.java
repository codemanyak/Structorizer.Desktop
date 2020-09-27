/*
    Structorizer
    A little tool which you can use to create Nassi-Schneiderman Diagrams (NSD)

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
package lu.fisch.structorizer.parsers;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Interface for foreign NSD format importers
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2017.04.26      First Issue
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

import java.io.IOException;

import org.xml.sax.SAXException;

import lu.fisch.structorizer.elements.Root;

/**
 * @author Kay Gürtzig
 *
 */
public interface INSDImporter {

	/**
	 * Returns a title for dialog message references
	 * @return the importer name for dialog
	 */
	public String getDialogTitle();

	/**
	 * Returns a description of the file type for the file chooser
	 * @return file description text
	 */
	public String getFileDescription();

	/**
	 * Returns expected file name extensions for the file filter 
	 * @return list of extensions (without dot!)
	 */
	public String[] getFileExtensions();

	/**
	 * Returns a suitable FileFilter matching the parameters provided by
	 * {@link #getFileDescription()} and {@link #getFileExtensions()}.
	 * @return
	 */
	public javax.swing.filechooser.FileFilter getFileFilter();
	
	/**
	 * Parses the file with path {@code _filename} (possibly employing a SAX
	 * parser) and converts the content into a Structorizer diagram that is
	 * to be returned
	 * @param _filename - path of the file assumed to represent a structured algorithm
	 * @return the extracted Nassi-Shneiderman diagram as Structorizer {@link Root} object
	 * @throws SAXException in case of some XML format or schema violation
	 * @throws IOException - in case of a general IO error (e.g. file not readable)
	 */
	public Root parse(String _filename) throws SAXException, IOException;
}
