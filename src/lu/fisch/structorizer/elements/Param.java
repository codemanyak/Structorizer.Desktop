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

package lu.fisch.structorizer.elements;

import lu.fisch.structorizer.syntax.Line;
import lu.fisch.structorizer.syntax.LineParser;
import lu.fisch.structorizer.syntax.Syntax;
import lu.fisch.structorizer.syntax.SyntaxException;
import lu.fisch.structorizer.syntax.Type;
import lu.fisch.structorizer.syntax.TypeRegistry;

/******************************************************************************************************
 *
 *      Author:         Bob Fisch
 *
 *      Description:    Parameter record for subroutine diagrams, contains name, type, and default value
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Bob Fisch       2015-12-10      First Issue
 *      Kay Gürtzig     2019-03-07      Enh. #385 support for optional parameters
 *      Kay Gürtzig     2021-02-04      Bugfix #925 - we must be able to suppress "const" prefixes in type
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///

/**
 * Parameter record for subroutine diagrams, contains name, type description, and
 * possibly a default value (in case of an optional parameter)
 * @author robertfisch
 */
public class Param {
	/** The parameter name (identifier) */
    protected String name;
    /** A type name or description*/
    protected String type;
    // START KGU#371 2019-03-07: Enh. #385 - allow default values for parameters
    /**
     * Default value (as string) for this parameter, or {@code null} in case of
     * a mandatory argument
     */
    protected String defaultValue = null;

    /**
     * Creates a Param object for a (possibly optional) parameter
     * 
     * @param name - the parameter name as declared
     * @param type - the (possibly prefixed) type description as declared
     *     (a possible prefix might be "const")
     * @param defaultLiteral a default value description (usually some literal)
     *     for an optional parameter, {@code null} for a mandatory parameter
     */
    public Param(String name, String type, String defaultLiteral) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultLiteral;
    }
    // END KGU#371 2019-03-07

    /**
     * Creates a Param object for a regular (mandatory) parameter
     * @param name - the parameter name as declared
     * @param type - the (possibly prefixed) type description as declared
     * (a possible prefix might be "const")
     * @see #Param(String, String, String)
     */
    public Param(String name, String type) {
        this.name = name;
        this.type = type;
        // START KGU#371 2019-03-07: Enh. #385 - allow default values for parameters
        this.defaultValue = null;
        // END KGU#371 2019-03-07
    }

    /**
     * @return the declared parameter name
     * @see #getType(boolean)
     * @see #getDefault()
     */
    public String getName() {
        return name;
    }

    /**
     * Provides the type description of the parameter
     * @param withoutPrefix - if {@code true} then prefixes like "const" will be
     * removed such that the "true" type description is obtained
     * @return the type description of the parameter declaration
     * @see #getName()
     * @see #getDefault()
     */
    // START KGU#925 2021-02-04: Bugfix #925
    //public String getType() {
    public String getType(boolean withoutPrefix) {
        if (type != null) {
            String typeDescr = this.type;
            // FIXME This approach is order-sensitive in case of several prefixes
            for (String prefix: new String[] {"const "}) {
                if (typeDescr.startsWith(prefix)) {
                    typeDescr = typeDescr.substring(prefix.length());
                }
            }
            return typeDescr;
        }
    // END KGU#925 2021-02-04
        return type;
    }
    // END KGU#925 2021-02-04
    
    // START KGU#371 2019-03-07: Enh. #385 - allow default values for parameters
    /**
     * @return {@code null} in case of a mandatory parameter or the default value
     * string (usually a literal) in case of an optional parameter
     * @see #getName()
     * @see #getType(boolean)
     */
    public String getDefault() {
        return this.defaultValue;
    }
    // END KGU#371 2019-03-07
    
    // START KGU#790 2021-12-08: Issue #800
    /**
     * With help of TypeRegistry {@code _dataTypes} retrieves a {@link Type} object
     * representing the data type of this parameter if it can be identified. In case
     * {@code null} is returned, it does not necessarily mean that no type was specified;
     * check {@link #getType(boolean)} in this case in order to find out the textual
     * type description if there was any.
     * 
     * @param _dataTypes - a type registry holding the known types and type associations
     * @return a {@link Type} object reflecting the structure of the parameter values.
     */
    public Type getDataType(TypeRegistry _dataTypes) {
        Type paramType = _dataTypes.getTypeFor(name);
        if (paramType == null) {
            String typeDescr = getType(true);
            if (Syntax.isIdentifier(typeDescr, false, null)) {
                paramType = _dataTypes.getType(typeDescr);
                if (paramType == null) {
                    try {
                        paramType = new Type(typeDescr);
                    } catch (SyntaxException exc) {}
                }
            }
            else {
                // parse / construct the type from typeDescr
                Line decl = LineParser.getInstance().parse(
                        "var " + name + ": " + typeDescr, null, 0, _dataTypes);
                if (decl.getType() == Line.LineType.LT_VAR_DECL) {
                    paramType = decl.getDataType(true);
                }
            }
        }
        return paramType;
    }
    // END KGU#790 2021-12-08
}
