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
 *      Description:    Class Declaration, a specific Expression subclass
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2020-11-03      First Issue (for #800)
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      BNF
 *      <par_list>        ::= <par_group_list> | <par_decl_list>
 *      <par_group_list>  ::= <par_group> | <par_group> ';' <par_group_list>
 *      <par_group>       ::= [const] <rec_group> | [const] <cdecl> | [const] <def>
 *      <rec_group_list>  ::= <rec_group> | <rec_group> ';' <rec_group_list>
 *      <rec_group>       ::= <id_list> <colon> <type>
 *      <par_decl_list>   ::= <par_decl> | <par_decl> ',' <par_decl_list>
 *      <par_decl>        ::= [const] <cdecl> | [const] <cdef>
 *      <cdecl_list>      ::= <carray_list> | <type> <carray_list>
 *      <def>             ::= <vdecl> '=' <expr> | <cdef>
 *      <vdecl>           ::= <id> <colon> <type>
 *      <cdef>            ::= <cdecl> '=' <expr>
 *      <cid>             ::= <id> | <cdecl>
 *      <cdecl>           ::= <carray> | <type> <carray>
 *      <carray_list>     ::= <carray> | <carray> ',' <carray_list>
 *      <carray>          ::= <id> | <id> <brackets>
 *      <id_list>         ::= <id> | <id> ',' <id_list>
 *      <enum_list>       ::= <enum_item> | <enum_item>, <enum_list>
 *      <enum_item>       ::= <id> | <id> '=' <int_const>
 *      <brackets>        ::= [ <expr> ] | []
 *      <colon>           ::= ':' | as
 *      <asgn_op>         ::= '<-' | ':='
 *      The above vocabulary except <colon> will be modelled by enumerator type DeclarationRule.
 *      The separators (like ';', ',') will not be stored explicitly in the nodes.
 *      The expected rule is rather unambiguously determined the following way:
 *      - Root "<id>(?)[<colon> <type>] | [<type>] <id>(?)"--> <par_list>
 *      - Instruction - "type <id> = <record>{?}" --> <rec_group_list>
 *                    - "type <id> = <enum>{?}" --> <enum_list>
 *                    - "<var> ? <asgn_op> <expr>" --> <vdecl>
 *                    - "<var> ?" --> <rec_group>
 *                    - "? <asgn_op> <expr>" && > 1 <id> --> <cdecl>
 *      
 ******************************************************************************************************///

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.regex.Pattern;

import lu.fisch.utils.StringList;

/**
 * Specific {@link Expression} subclass to cover variable declarations, parameter
 * declarations, record component declarations, and constant definitions, possibly
 * even enumerator definitions.<br/>
 * Brings an own parsing algorithm and translation support.
 * @author Kay Gürtzig
 */
public class Declaration extends Expression {
	
	/** Enumerator representing the respective grammar rule */
	public static enum DeclarationRule {
		// FIXME: Pointers should be considered in future
		/** {@code <par_list>        ::= <par_group_list> | <par_decl_list>} */
		PAR_LIST,
		/** {@code <par_group_list>  ::= <par_group> | <par_group> ';' <par_group_list>} */
		PAR_GROUP_LIST,
		/** {@code <par_group>       ::= [const] <rec_group> | [const] <def>} */
		PAR_GROUP,
		/** {@code <rec_group_list>  ::= <rec_group> | <rec_group> ';' <rec_group_list>} */
		REC_GROUP_LIST,
		/** {@code <rec_group>       ::= <id_list> <colon> <type> | <cdecl_list>} */
		REC_GROUP,
		/** {@code <par_decl_list>   ::= <par_decl> | <par_decl> ',' <par_decl_list>} */
		PAR_DECL_LIST,
		/** {@code <par_decl>        ::= [const] <cdecl> | [const] <cdef>} */
		PAR_DECL,
		/** {@code <cdecl_list>      ::= <carray_list> | <type> <carray_list>} */
		CDECL_LIST,
		/** {@code <def>             ::= <vdecl> = <expr> | <cdef>} */
		DEF,
		/** {@code <vdecl>           ::= <id> <colon> <type>} */
		VDECL,
		/** {@code <cdef>            ::= <cdecl> = <expr>} */
		CDEF,
		/** {@code <cdecl>           ::= <carray> | <type> <carray>} */
		CDECL,
		/** {@code <carrays_list>    ::= <carray> | <carray> ',' <carray_list>} */
		CARRAY_LIST,
		/** {@code <carray>          ::= <id> | <id> <brackets>} */
		CARRAY,
		/** {@code <id_list>         ::= <id> | <id> , <id_list>} */
		ID_LIST,
		/** {@code <enum_list>       ::= <enum_item> | <enum_item> , <enum_list>} */
		ENUM_LIST,
		/** {@code <enum_item>       ::= <id> | <id> = <expression>} */
		ENUM_ITEM
	};

	//private static final int PAR_LIST_MASK       = 1 << DeclarationRule.PAR_LIST.ordinal();
	private static final int PAR_GROUP_LIST_MASK = 1 << DeclarationRule.PAR_GROUP_LIST.ordinal();
	private static final int PAR_GROUP_MASK      = 1 << DeclarationRule.PAR_GROUP.ordinal();
	private static final int REC_GROUP_LIST_MASK = 1 << DeclarationRule.REC_GROUP_LIST.ordinal();
	private static final int REC_GROUP_MASK      = 1 << DeclarationRule.REC_GROUP.ordinal();
	private static final int PAR_DECL_LIST_MASK  = 1 << DeclarationRule.PAR_DECL_LIST.ordinal();
	private static final int PAR_DECL_MASK       = 1 << DeclarationRule.PAR_DECL.ordinal();
	private static final int CDECL_LIST_MASK     = 1 << DeclarationRule.CDECL_LIST.ordinal();
	private static final int DEF_MASK            = 1 << DeclarationRule.DEF.ordinal();
	private static final int VDECL_MASK          = 1 << DeclarationRule.VDECL.ordinal();
	private static final int CDEF_MASK           = 1 << DeclarationRule.CDEF.ordinal();
	private static final int CDECL_MASK          = 1 << DeclarationRule.CDECL.ordinal();
	private static final int CARRAY_LIST_MASK    = 1 << DeclarationRule.CARRAY_LIST.ordinal();
	private static final int CARRAY_MASK         = 1 << DeclarationRule.CARRAY.ordinal();
	private static final int ID_LIST_MASK        = 1 << DeclarationRule.ID_LIST.ordinal();
	private static final int ENUM_LIST_MASK      = 1 << DeclarationRule.ENUM_LIST.ordinal();
	private static final int ENUM_ITEM_MASK      = 1 << DeclarationRule.ENUM_ITEM.ordinal();
	
	/**
	 * The declaration rule this Declaration represents
	 */
	private DeclarationRule rule;

	/**
	 * Whether all contained declarations are constant
	 */
	private boolean isConstant = false;
	
	/**
	 * @param declRule - the production rule this declaration is representing
	 * @param isConst
	 * @param tokenPos
	 */
	public Declaration(DeclarationRule declRule, LinkedList<Expression> components, short tokenPos, boolean isConst)
	{
		super(NodeType.DECLARATION, "", tokenPos, components);
		rule = declRule;
		isConstant = isConst;
	}

	/**
	 * @return the {@link DeclarationRule} represented by this Declaration
	 */
	public DeclarationRule getRule()
	{
		return rule;
	}

	/**
	 * Parses the given token list (maybe a declaration line or a zone within a
	 * parameter declaration list and returns a Declaration syntax tree
	 * according to the structure. Copes with all Structorizer declaration styles
	 * (Pascal, BASIC, C, Java)
	 * @param tokens - the token list representing a declaration part of a line,
	 *        expected to be condensed (i.e. <b>not containing white space</b>) 
	 * @param expectedRule - a {@link DeclarationRule} specifying the accepted
	 *        declaration syntax
	 * @param tokenNo - the number of the preceding tokens of the line not contained
	 *        in {@code tokens}, i.e. the global index of the first token in {@code tokens}
	 * @param typeMap - a {@link TypeRegistry} for data type retrieval from names,
	 *        might be enhanced by anonymous types in case we bump into an explicit
	 *        array declaration.
	 * @return a Declaration object if nothing goes wrong (otherwise an Exception
	 *        ought to be expected)
	 * @throws SyntaxException in case of syntactical errors
	 */
	public static Declaration parse(StringList tokens, DeclarationRule expectedRule,
			short tokenNo, TypeRegistry typeMap) throws SyntaxException
	{
		/*
		 * This is not a clean parsing algorithm but a rather pragmatic and heuristic
		 * approach to cope with an ambiguous syntax and to make the best out of it...
		 * The advantage is that we are not restricted to a limited lookahead scope.
		 * We will reduce the rules as much as possible, e.g. if a PAR_GROUP_LIST
		 * is expected but no semicolon is found then we reduce the rule to a
		 * single PAR_GROUP etc.
		 * If the token sequence cannot be matched with none of the expected rules
		 * then a SyntaxException will be thrown
		 */
		short tokenPos = 0;
		LinkedList<Expression> declarations = new LinkedList<Expression>();
		if (expectedRule == DeclarationRule.PAR_LIST) {
			if (tokens.contains(";") || tokens.contains(":") || tokens.contains("as", false)) {
				expectedRule = DeclarationRule.PAR_GROUP_LIST;
			}
			else {
				expectedRule = DeclarationRule.PAR_DECL_LIST;
			}
		}
		// Rule set for the case of ambiguity (as bitset)
		int acceptedRules = 1 << expectedRule.ordinal();
		
		// First level splitting approach (groups) - no need to use Syntax.splitExpressionList() here
		ArrayList<StringList> listItems = new ArrayList<StringList>();
		int posSemi = tokens.indexOf(";");
		// At least two items - should be either PAR_GROUP_LIST or REC_GROUP_LIST
		if (posSemi >= 0 && (acceptedRules &= (PAR_GROUP_LIST_MASK | REC_GROUP_LIST_MASK)) == 0) {
			throw new SyntaxException("Unexpected ';'", tokenNo + posSemi);
		}
		int posStart = 0;
		while (posSemi >= 0) {
			listItems.add(tokens.subSequence(posStart, posSemi));
			posSemi = tokens.indexOf(";", posStart = posSemi + 1);
		}
		listItems.add(tokens.subSequence(posStart, tokens.count()));
		DeclarationRule thisRule = expectedRule;
		if (thisRule == DeclarationRule.PAR_GROUP_LIST) {
			expectedRule = DeclarationRule.PAR_GROUP;
		}
		else if (thisRule == DeclarationRule.REC_GROUP_LIST) {
			expectedRule = DeclarationRule.REC_GROUP;
		}
		if (listItems.size() > 1) {
			// Should be either PAR_GROUP_LIST or REC_GROUP_LIST now
			// Parse each group
			for (StringList listItem: listItems) {
				declarations.add(Declaration.parse(listItem, expectedRule,
						(short)(tokenNo + tokenPos), typeMap));
				tokenPos += listItem.count() + 1; // list item length and semicolon
			}
			return new Declaration(thisRule, declarations, tokenNo, false);
		}
		// Obviously it is not a group list (but might be a group), so reduce the expectation
		acceptedRules = 1 << expectedRule.ordinal();
		
		/* Second level splitting approach
		 * Now there are alternative splitting approaches: by '=', by ',', and by <colon>
		 * It strongly depends on the expected rules which way to go first:
		 * In case of ENUM_LIST, definitions may be members of a list,
		 * in case of PAR_GROUP, either an id list may be member of a type
		 * association, or a type association may be part of a definition,
		 * but both a comma and an equality sign may not be contained, which
		 * is similar with a PAR_DECL_LIST except that no <colon> may occur.
		 * So the best approach will be to split by <colon> now.
		 */
		int posColon = tokens.indexOf(":");
		int posAs = tokens.indexOf("as", false);
		if (posColon >= 0 || posAs >= 0) {
			boolean isConst = false;
			if (posColon >= 0 && posAs >= 0) {
				throw new SyntaxException("There must not be both \"" + tokens.get(posAs) + "\" and ':' in a declaration!",
						tokenNo + Math.max(posColon, posAs));
			}
			posColon = Math.max(posAs, posColon);
			if ((acceptedRules &= (PAR_GROUP_MASK | REC_GROUP_MASK | VDECL_MASK | DEF_MASK)) == 0) {
				throw new SyntaxException("Unexpected '" + tokens.get(posColon) + "'",
						tokenNo + posColon);
			}
			if (tokens.get(0).equals("const")) {
				if ((acceptedRules &= (PAR_GROUP_MASK | DEF_MASK)) == 0) {
					throw new SyntaxException("Unexpected \"const\"",
							tokenNo);
				}
				isConst = true;
				tokens.remove(0);
				tokenNo++;
				posColon--;
			}
			// Is there a default value (optional argument)? 
			int posDef = tokens.indexOf("=", posColon);
			if (posDef >= 0) {
				if ((acceptedRules &= (PAR_GROUP_MASK | DEF_MASK)) == 0) {
					throw new SyntaxException("Unexpected '='",
							tokenNo + posDef);
				}
				// As there was a ':' or "as", the left-hand side must be a VDECL
				declarations.add(Declaration.parse(tokens.subSequence(0, posDef),
						DeclarationRule.VDECL, tokenNo, typeMap));
				// Parse the right-hand side (the default value, an expression)
				LinkedList<Expression> exprs = 
						Expression.parse(tokens.subSequence(posDef+1, tokens.count()), null, (short)(tokenNo + posDef+1));
				if (exprs == null || exprs.size() != 1) {
					// Actually, method Expression.parse is likely to have thrown a more expressive exception itself
					throw new SyntaxException("Defective default value expression", tokenNo + posDef + 1);
				}
				declarations.add(exprs.get(0));
				return new Declaration(DeclarationRule.DEF, declarations, tokenNo, isConst);
			}
			// No default value, hence a REC_GROUP
			Type type = null;
			StringList typeDescr = tokens.subSequence(posColon+1, tokens.count());
			if (typeDescr.count() == 1 && typeMap != null) {
				type = typeMap.getType(typeDescr.get(0));	// This may fail, of course
				if (type == null) {
					throw new SyntaxException("Unknown type \"" + typeDescr.get(0) +"\"",
							tokenNo + posColon+1);
				}
			}
			else {
				// FIXME we must parse the type right of posColon!
				System.err.println("Type parsing not implemented in REC_GROUP!");
			}
			// pare the left-hand side (should be an identifier or an identifier list)
			Declaration declared = Declaration.parse(tokens.subSequence(0, posColon),
					DeclarationRule.ID_LIST, tokenNo, typeMap);
			declared.dataType = type;
			// the children should also be tagged
			for (Expression id: declared.children) {
				id.dataType = type;
			}
			declarations.add(declared);
			// FIXME: Should we tag the REC_GROUP with the type as well?
			return new Declaration(DeclarationRule.REC_GROUP, declarations, tokenNo, isConst);
		}
		
		// If there was neither a semicolon nor a <colon> separator then we can restrict the rule
		if (expectedRule == DeclarationRule.REC_GROUP) {
			expectedRule = DeclarationRule.CDECL_LIST;
		}
		else if (expectedRule == DeclarationRule.DEF) {
			expectedRule = DeclarationRule.CDEF;
		}
		else if (expectedRule == DeclarationRule.VDECL) {
			throw new SyntaxException("Missing ':' or \"as\" in declaration", tokenNo);
		}
		
		// Third-level splitting approach (declarations per group or enum/id lists)
		// PAR_LIST, PAR_GROUP_LIST, REC_GROUP_LIST, VDECL, CDECL are out now,
		// REC_GROUP and DEF have been reduced
		listItems = Syntax.splitExpressionList(tokens, ",");
		if (listItems.size() > 1) {
			if ((acceptedRules & (PAR_DECL_LIST_MASK | CDECL_LIST_MASK
					| CARRAY_LIST_MASK | PAR_GROUP_MASK
					| ENUM_LIST_MASK | ID_LIST_MASK)) == 0) {
				throw new SyntaxException("Unexpected ','", tokenNo + tokens.indexOf(","));
			}
			StringList typeDescr = null;
			Type type = null;
			boolean isConst = false;
			StringList listItem0 = listItems.get(0);
			int posBrack1 = listItem0.indexOf("[");
			int posBrack2 = listItem0.lastIndexOf("]");
			switch (expectedRule) {
			case ENUM_LIST:
				for (StringList listItem: listItems) {
					declarations.add(Declaration.parse(listItem,
							DeclarationRule.ENUM_ITEM, (short)(tokenNo + tokenPos), typeMap));
					tokenPos += listItem.count() + 1;	// length of listItem plus comma
				}
				return new Declaration(expectedRule, declarations, tokenNo, true);
			case ID_LIST:
				for (StringList listItem: listItems) {
					if (listItem.count() != 1 || !Syntax.isIdentifier(listItem.get(0), false, null)) {
						throw new SyntaxException("Not a valid identifier", tokenNo + tokenPos);
					}
					declarations.add(new Expression(NodeType.IDENTIFIER, listItem.get(0),
							(short)(tokenNo + tokenPos)));
					tokenPos += listItem.count() + 1;	// length of listItem plus comma
				}
				return new Declaration(expectedRule, declarations, tokenNo, false);
			case PAR_DECL_LIST:
				isConst = listItems.get(0).get(0).equals("const");
				if (isConst) {
					listItems.get(0).remove(0);
					tokenPos++;
				}
				for (StringList listItem: listItems) {
					declarations.add(Declaration.parse(listItem,
							DeclarationRule.PAR_DECL, (short)(tokenNo + tokenPos), typeMap));
					tokenPos += listItem.count() + 1;	// length of listItem plus comma
				}
				return new Declaration(expectedRule, declarations, tokenNo, isConst);
			case PAR_GROUP:
				isConst = listItems.get(0).get(0).equals("const");
				if (isConst) {
					listItems.get(0).remove(0);
					tokenPos++;
				}
				// Can't be a DEF or CDEF anymore (they wouldn't allow a comma).
				expectedRule = DeclarationRule.CDECL_LIST;
				// We deliberately run into the next case here!
			case CDECL_LIST:
				// Check whether there is a common type specification
				if (listItem0.count() > 1 && (posBrack1 < 0 || posBrack2 == listItem0.count()-1)) {
					// We must extract the type information from the first list item
					if (posBrack1 < 0) {
						/* No C array declaration, hence it is all before the last token,
						 * which is expected to be an id
						 */
						posBrack1 = listItem0.count();
					} // posBrack2 must be listItem0.count()-1, so it is an array
					typeDescr = listItem0.subSequence(0, posBrack1 - 1);
				}
				for (StringList listItem: listItems) {
					/* FIXME This is a dirty trick because we cannot simply assign the type
					 * to all subdeclarations as some of them might be arrays of it
					 */
					int offset = 0;
					if (typeDescr != null) {
						if (listItem != listItem0) {
							listItem.insert(typeDescr, 0);
							offset -= typeDescr.count();
						}
					}
					Declaration decl = Declaration.parse(listItem,
							DeclarationRule.CDECL, (short)(tokenNo + tokenPos + offset), typeMap);
					decl.isConstant = isConst;
					declarations.add(decl);
					tokenPos += offset + listItem.count() + 1;	// length of the orig. listItem plus comma
				}
				return new Declaration(expectedRule, declarations, tokenNo, isConst);
			case CARRAY_LIST:
				for (StringList listItem: listItems) {
					int posBrack = listItem.indexOf("[");
					if (posBrack < 0 && listItem.count() == 1
							&& Syntax.isIdentifier(listItem.get(0), false, null)) {
						declarations.add(new Expression(NodeType.IDENTIFIER,
								listItem.get(0), (short)(tokenNo + tokenPos)));
					}
					else {
						// FIXME we can't know the type here, we just know that it is an array
						// Shall we always assign the element type to a CARRAY declaration?
						declarations.add(Declaration.parse(listItem,
								DeclarationRule.CARRAY, (short)(tokenNo + tokenPos), typeMap));
					}
					tokenPos += listItem.count() + 1;
				}
				return new Declaration(expectedRule, declarations, tokenNo, false);
			default:
				throw new SyntaxException("Unexpected ','", listItems.get(0).count());
			}
		}
		
		if (expectedRule == DeclarationRule.PAR_GROUP) {
			if (tokens.contains("=")) {
				expectedRule = DeclarationRule.CDEF;
			}
			else {
				expectedRule = DeclarationRule.CDECL;
			}
		}
		
		// FIXME: We still have to process rules ENUM_ITEM, CDEF, CARRAY, PAR_DECL, CDECL
		switch (expectedRule) {
		case CARRAY:
			break;
		case CDECL:
			break;
		case CDEF:
			break;
		case ENUM_ITEM:
			break;
		case PAR_DECL:
			break;
		default:
			break;
		
		}
			
//			// We try the most atomic list first
//			DeclarationRule rule = DeclarationRule.ID_LIST;
//			boolean ok = true;
//			short iProblem = 0;
//			if ((acceptedRules & (1 << rule.ordinal())) != 0) {
//				for (short i = 0; i < listItems.size(); i++) {
//					StringList element = listItems.get(i);
//					if (element.count() != 1 || !Syntax.isIdentifier(element.get(0), false, null)) {
//						ok = false;
//						iProblem = i;
//						break;
//					}
//					declarations.add(new Expression(NodeType.IDENTIFIER, element.get(0),
//							(short)(tokenNo + tokenPos)));
//					tokenPos += 2;
//				}
//				if (ok) {
//					return new Declaration(rule, declarations, tokenNo, false);
//				}
//			}
//			// Not a pure ID_LIST, try with an ENUM_LIST
//			rule = DeclarationRule.ENUM_LIST;
//			if ((acceptedRules & (1 << rule.ordinal())) != 0) {
//				for (short i = iProblem; i < listItems.size(); i++) {
//					StringList element = listItems.get(i);
//					int count = element.count();
//					if (count < 1 || count == 2
//							|| count >= 3 && !element.get(1).equals("=")
//							|| !Syntax.isIdentifier(element.get(0), false, null)) {
//						ok = false;
//						iProblem = i;
//						throw new SyntaxException("Wrong enumerator item", );
//					}
//					else {
//						declarations.add(new Expression(NodeType.IDENTIFIER, element.get(0),
//								(short)(tokenNo + i)));
//					}
//				}
//			}
//		}
		for (int i = 0; i < tokens.count(); i++) {
			String token = tokens.get(i);
			if (token.equals("const")) {
				
			}
		}
		return null;
	}
	
//	/**
//	 * 
//	 * @param tokens
//	 * @param context
//	 * @param declSpec
//	 */
//	public void appendToTokenList(StringList tokens)
//	{
//		if (isConstant) {
//			tokens.add(declSpec.constKeyword);
//		}
//		// TODO
//	}
	
}
