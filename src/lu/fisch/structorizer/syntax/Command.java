package lu.fisch.structorizer.syntax;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Line variant representing some command-like instruction
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      kay       27.12.2019      First Issue
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

import java.util.List;

/**
 * Represents a command-like instruction line as composed from a command keyword and an expression list
 * @author Kay Gürtzig
 */
public class Command implements Line {

	/** The symbolic keyword */
	public String keyword = "";
	public List<Expression> expressions = null;
	
	public Command(String key) {
		keyword = key;
	}
	
	public Command(String key, List<Expression> exprs) {
		keyword = key;
		expressions = exprs;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(this.keyword);
		if (this.expressions != null) {
			char sepa = ' ';
			for (Expression expr: this.expressions) {
				sb.append(sepa);
				sb.append(expr.toString());
			}
		}
		return sb.toString();
	}

}
