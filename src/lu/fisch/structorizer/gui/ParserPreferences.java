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

package lu.fisch.structorizer.gui;

/******************************************************************************************************
 *
 *      Author:         Bob Fisch
 *
 *      Description:    This is the parser preferences dialog window.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2008-01-03      First Issue
 *      Kay Gürtzig     2015-11-08      Enh. #10: step keyword setting manually added (FOR loop)
 *      Kay Gürtzig     2016-03-21      Enh. #84: FOR-IN loop settings manually added
 *      Kay Gürtzig     2016-03-23      Enh. #23: Settings for JUMP statements prepared (but not enabled)
 *      Kay Gürtzig     2016-11-11      Issue #81: DPI-awareness workaround for checkboxes
 *      Kay Gürtzig     2017-01-07      Bugfix #330 (issue #81): checkbox scaling suppressed for "Nimbus" l&f
 *      Kay Gürtzig     2017-01-09      Issue #81 / bugfix #330: Scaling stuff outsourced to class GUIScaler
 *      Kay Gürtzig     2018-12-29      Issue #658: Configuration of leave, return, and exit keywords enabled
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      2015-11-08 Kay Gürtzig
 *          NOTE: Design is no longer JFormDesigner-based
 *          
 *      2008-01-03 Bob Fisch
 *          I used JFormDesigner to design this window graphically.
 *
 ******************************************************************************************************///

import lu.fisch.structorizer.locales.LangDialog;
import lu.fisch.structorizer.locales.LangTextHolder;

import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;

import javax.swing.*;
import javax.swing.border.*;


/**
 * @author Robert Fisch
 */
@SuppressWarnings("serial")
public class ParserPreferences extends LangDialog {
    
	public boolean OK = false;

	protected JPanel dialogPane;
	protected JPanel contentPanel;
	protected JLabel lblNothing;
	protected JLabel lblPre;
	protected JLabel lblPost;
	protected JLabel lblNothing2;
	// START KGU#628 2018-12-30: Enh. #658 Renamed lblNothing3 -> lblInputOutput 
	protected JLabel lblInputOutput;	// was lblNothing2;
	// END KGU#628 2018-12-30
	protected JLabel lblInput;
	protected JLabel lblOutput;
	protected JLabel lblAlt;
	protected JTextField edtAltPre;
	protected JTextField edtAltPost;
	protected JTextField edtInput;
	protected JTextField edtOutput;
	protected JLabel lblCase;
	protected JTextField edtCasePre;
	protected JTextField edtCasePost;
	protected JLabel lblFor;
	protected JTextField edtForPre;
	protected JTextField edtForPost;
	// START KGU#3 2015-11-08: Enh. #10 - consistent parsing for steps
	protected JLabel lblNothing4;
	protected JLabel lblForStep;
	protected JTextField edtForStep;
	// END KGU#3 2015-11-08
	// START KGU#61 2016-03-21: Enh. #84 - New set of keywords for FOR-IN loops
	protected JLabel lblForIn;
	protected JTextField edtForInPre;
	protected JTextField edtForInPost;
	// END KGU#61 2016-03-21
	protected JLabel lblWhile;
	protected JTextField edtWhilePre;
	protected JTextField edtWhilePost;
	protected JLabel lblRepeat;
	protected JTextField edtRepeatPre;
	protected JTextField edtRepeatPost;
	// START KGU#78 2016-03-25: Enh. #23 - configurability introduced
	protected JLabel lblJump;
	protected JLabel lblNothing5;
	protected JLabel lblNothing6;
	protected JTextField edtJumpLeave;
	protected JTextField edtJumpReturn;
	protected JTextField edtJumpExit;
	protected JLabel lblJumpLeave;
	protected JLabel lblJumpReturn;
	protected JLabel lblJumpExit;
	// END KGU#78 2016-03-25
	protected JPanel buttonBar;
	protected JButton btnOK;
	// START KGU 2016-03-25: New general option for handling these keywords
	protected JCheckBox chkIgnoreCase;
	// END KGU 2016-03-25
	
	// START KGU 2016-03-25: Labels replaced by light-weight objects
	//protected JLabel lblErrorSign;
	protected LangTextHolder lblErrorSign;
	// START KGU#61 2016-03-21: Enh. #84 - New set of keywords for FOR-IN loops
	//protected JLabel lblErrorSign2;
	protected LangTextHolder lblErrorSign2;
	// END KGU#61 2016-03-21
	// START KGU#657 2019-02-17: Issue #684 - new check for mandatory fields
	protected LangTextHolder lblErrorSign3;
	private JTextField[] mandatoryFields;
	private static final Color mandatoryBackGround = new Color(255,255,210);
	protected static final LangTextHolder ttlError = new LangTextHolder("Error");
	protected JPanel headPanel;
	protected JLabel lblHeadline;
	// END KGU#657 2019-02-17
	
	/*public ParserPreferences()
	{
		super();
		setModal(true);
		initComponents();
	}*/
	
	public ParserPreferences(Frame owner) {
		super(owner);
		setModal(true);
		initComponents();
	}
	
	/*public ParserPreferences(Dialog owner) {
		super(owner);
		initComponents();
	}*/

	private void initComponents() {
		dialogPane = new JPanel();
		// START KGU#657 2019-02-17: Issue #684 - new check for mandatory fields
		headPanel = new JPanel();
		lblHeadline = new JLabel();
		// END KGU#657 2019-02-17
		contentPanel = new JPanel();
		lblNothing = new JLabel();
		lblNothing2 = new JLabel();
		lblInputOutput = new JLabel();
		lblPre = new JLabel();
		lblPost = new JLabel();
		lblInput = new JLabel();
		lblOutput = new JLabel();
		lblAlt = new JLabel();
		edtAltPre = new JTextField();
		edtAltPost = new JTextField();
		lblCase = new JLabel();
		edtCasePre = new JTextField();
		edtCasePost = new JTextField();
		lblFor = new JLabel();
		edtForPre = new JTextField();
		edtForPost = new JTextField();
		// START KGU# 2015-11-08
		lblNothing4 = new JLabel();
		lblForStep = new JLabel();
		edtForStep = new JTextField();
		// END KGU#3 2015-11-08
		// START KGU#61 2016-03-21: Enh. #84 - New set of keywords for FOR-IN loops
		lblForIn = new JLabel();
		edtForInPre = new JTextField();
		edtForInPost = new JTextField();
		// END KGU#61 2016-03-21
		lblWhile = new JLabel();
		edtWhilePre = new JTextField();
		edtWhilePost = new JTextField();
		lblRepeat = new JLabel();
		edtRepeatPre = new JTextField();
		edtRepeatPost = new JTextField();
		// START KGU#78 2016-03-25: Enh. #23 - configurability introduced
		lblJump = new JLabel();
		lblNothing5 = new JLabel();
		lblNothing6 = new JLabel();
		edtJumpLeave = new JTextField();
		edtJumpReturn = new JTextField();
		edtJumpExit = new JTextField();
		lblJumpLeave = new JLabel();
		lblJumpReturn = new JLabel();
		lblJumpExit = new JLabel();
		// END KGU#78 2016-03-25
		buttonBar = new JPanel();
		btnOK = new JButton();
		edtInput = new JTextField();
		edtOutput = new JTextField();
		// START KGU 2016-03-25: New general option for handling these keywords
		chkIgnoreCase = new JCheckBox();
		// END KGU 2016-03-25

		lblErrorSign = new LangTextHolder("Your are not allowed to use the character ':' in any parser string!");
		// START KGU#61 2016-03-21: Enh. #84 - New set of keywords for FOR-IN loops
		//lblErrorSign2 = new JLabel();
		// START KGU#628 2018-12-28: Enh. #658 - The text had to be generalized
		//lblErrorSign2 = new LangTextHolder();
		//lblErrorSign2.setText("The post-FOR-IN loop keyword must not be equal to any other token!");
		lblErrorSign2 = new LangTextHolder("There are name conflicts among the key words marked red - they must all differ!");
		// END KGU#628 2018-12-28
		// END KGU#61 2016-03-21
		// START KGU#657 2019-02-17: Issue #684 - new check for mandatory fields
		lblErrorSign3 = new LangTextHolder("% of the mandatory key words (cream background) aren't specified!");
		mandatoryFields = new JTextField[] {
				edtForPre, edtForPost, edtForStep,
				/*edtForInPre,*/ edtForInPost,
				edtJumpLeave, edtJumpReturn, edtJumpExit,
				edtInput, edtOutput
		};
		// END KGU#657 2019-02-17

		//======== this ========
		setModal(true);
		setResizable(false);
		setTitle("Parser Preferences");
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== dialogPane ========
		{
			dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));

			dialogPane.setLayout(new BorderLayout());
			
			//======== Headline ========

			// START KGU#657 2019-02-17: Issue #684 - new check for mandatory fields
			lblHeadline.setText("Fields with this background are mandatory");
			lblHeadline.setIcon(IconLoader.generateIcon(mandatoryBackGround));
			headPanel.add(lblHeadline);
			
			dialogPane.add(headPanel, BorderLayout.NORTH);
			// END KGU#657 2019-02-17

			//======== contentPanel ========
			{
				// START KGU#3 2015-11-08: Need an additional line for For
				//contentPanel.setLayout(new GridLayout(8, 3, 8, 8));
				// START KGU#61 2016-03-21: Need still an additional line for For-In
				//contentPanel.setLayout(new GridLayout(9, 3, 8, 8));
				// START KGU#78 2016-03-25: Enh. #23 - Jump configurability introduced
				//contentPanel.setLayout(new GridLayout(10, 3, 8, 8));
				contentPanel.setLayout(new GridLayout(13, 3, 8, 8));
				// END KGU#78 2016-03-25
				// END KGU#61 2016-03-21
				// END KGU#3 2015-11-08
				contentPanel.add(lblNothing);

				//---- lblPre ----
				lblPre.setText("Pre");
				contentPanel.add(lblPre);

				//---- lblPost ----
				lblPost.setText("Post");
				contentPanel.add(lblPost);

				//---- lblAlt ----
				lblAlt.setText("IF statement");
				contentPanel.add(lblAlt);
				contentPanel.add(edtAltPre);
				contentPanel.add(edtAltPost);

				//---- lblCase ----
				lblCase.setText("CASE statement");
				contentPanel.add(lblCase);
				contentPanel.add(edtCasePre);
				contentPanel.add(edtCasePost);

				//---- lblFor ----
				lblFor.setText("FOR loop");
				contentPanel.add(lblFor);
				contentPanel.add(edtForPre);
				contentPanel.add(edtForPost);
				// START KGU#3 2015-11-08: Enh. #10
				lblForStep.setText("Step separator");
				contentPanel.add(lblNothing4);
				contentPanel.add(lblForStep);
				contentPanel.add(edtForStep);
				// END KGU#3 20155-11-08
				// START KGU#61 2016-03-21: Enh. #84 - For-In preferences
				lblForIn.setText("FOR-IN loop");
				contentPanel.add(lblForIn);
				contentPanel.add(edtForInPre);
				contentPanel.add(edtForInPost);
				// END KGU#61 2016-03-21

				//---- lblWhile ----
				lblWhile.setText("WHILE loop");
				contentPanel.add(lblWhile);
				contentPanel.add(edtWhilePre);
				contentPanel.add(edtWhilePost);

				//---- lblRepeat ----
				lblRepeat.setText("REPEAT loop");
				contentPanel.add(lblRepeat);
				contentPanel.add(edtRepeatPre);
				contentPanel.add(edtRepeatPost);

				// START KGU#78 2016-03-26: Enh. #23 - still not enabled
				//---- lblJump ----
				lblJump.setText("JUMP statement");
				contentPanel.add(lblJump);
				
				contentPanel.add(edtJumpLeave);
				lblJumpLeave.setText("from loop(s)");
				contentPanel.add(lblJumpLeave);
				
				contentPanel.add(lblNothing5);
				contentPanel.add(edtJumpReturn);
				lblJumpReturn.setText("from routine");
				contentPanel.add(lblJumpReturn);
				
				contentPanel.add(lblNothing6);
				contentPanel.add(edtJumpExit);
				lblJumpExit.setText("from program");
				contentPanel.add(lblJumpExit);
				// END KGU#78 2016-03-26

				//---- Input / Output ----
				contentPanel.add(lblNothing2);
				lblInput.setText("Input");
				contentPanel.add(lblInput);
				lblOutput.setText("Output");
				contentPanel.add(lblOutput);

				contentPanel.add(lblInputOutput);
				contentPanel.add(edtInput);
				contentPanel.add(edtOutput);
			}
			dialogPane.add(contentPanel, BorderLayout.CENTER);
			
			// START KGU#657 2019-02-17: Issue #684
			for (JTextField field: mandatoryFields) {
				field.setBackground(mandatoryBackGround);
			}
			// END KGU#657 2019-02-17

			//======== buttonBar ========
			{
				buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
				buttonBar.setLayout(new GridBagLayout());
				((GridBagLayout)buttonBar.getLayout()).columnWidths = new int[] {0, 80};
				((GridBagLayout)buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0};

				//---- chkIgnoreCase ---
				chkIgnoreCase.setText("Ignore case");
				buttonBar.add(chkIgnoreCase);
				
				//---- okButton ----
				btnOK.setText("OK");
				buttonBar.add(btnOK, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));
			}
			dialogPane.add(buttonBar, BorderLayout.SOUTH);
		}
		contentPane.add(dialogPane, BorderLayout.CENTER);
		// START KGU#287 2017-01-09: Issue #81 / bugfix #330
		GUIScaler.rescaleComponents(this);
		// END KGU#287 2017-01-09

		pack();
		setLocationRelativeTo(getOwner());
		
		// add the LIST-listeners
		// add the KEY-listeners
		KeyListener keyListener = new KeyListener()
		{
			public void keyPressed(KeyEvent e) 
			{
				if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
				{
					setVisible(false);
				}
				else if(e.getKeyCode() == KeyEvent.VK_ENTER && (e.isShiftDown() || e.isControlDown()))
				{
					done();
				}
			}
			
			public void keyReleased(KeyEvent ke) {} 
			public void keyTyped(KeyEvent kevt) {}
		};
		edtAltPre.addKeyListener(keyListener);
		edtAltPost.addKeyListener(keyListener);
		edtCasePre.addKeyListener(keyListener);
		edtCasePost.addKeyListener(keyListener);
		edtForPre.addKeyListener(keyListener);
		edtForPost.addKeyListener(keyListener);
		// START KGU#3 2015-11-08: Enh. #10
		edtForStep.addKeyListener(keyListener);
		// END KGU#3 2015-11-08
		// START KGU#61 2016-03-21: Enh. #84 - New set of keywords for FOR-IN loops
		edtForInPre.addKeyListener(keyListener);
		edtForInPost.addKeyListener(keyListener);
		// END KGU#61 2016-03-21
		edtWhilePre.addKeyListener(keyListener);
		edtWhilePost.addKeyListener(keyListener);
		edtRepeatPre.addKeyListener(keyListener);
		edtRepeatPost.addKeyListener(keyListener);
		// START KGU#78 2016-03-25: Enh. #23 - Jump keyword configurability introduced
		edtJumpLeave.addKeyListener(keyListener);
		edtJumpReturn.addKeyListener(keyListener);
		edtJumpExit.addKeyListener(keyListener);
		// END KGU#78 2016-03-25
		edtInput.addKeyListener(keyListener);
		edtOutput.addKeyListener(keyListener);
		btnOK.addKeyListener(keyListener);
		
		// add the ACTION-listeners
		ActionListener actionListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				done();
			}
		};
		btnOK.addActionListener(actionListener);
	}

	public void done()
	{
		HashSet<JTextField> conflictingFields = null;	// conflicting text fields
		// START KGU#654 2019-02-17: Issue #684 - first of all, check mandatory fields
		//if(
		int nEmptyMandFields = 0;
		for (int i = 0; i < mandatoryFields.length; i++) {
			if (mandatoryFields[i].getText().trim().isEmpty()) {
				nEmptyMandFields++;
			}
		}
		if (nEmptyMandFields > 0) {
			JOptionPane.showMessageDialog(ParserPreferences.this, lblErrorSign3.getText().replace("%", Integer.toString(nEmptyMandFields)),
					ttlError.getText(), JOptionPane.ERROR_MESSAGE);
		}
		else if (
		// END KGU#654 2019-02-17
				edtAltPre.getText().contains(":") ||
				edtAltPost.getText().contains(":") ||
				edtCasePre.getText().contains(":") ||
				edtCasePost.getText().contains(":") ||
				edtForPre.getText().contains(":") ||
				edtForPost.getText().contains(":") ||
				// START KGU#3 2015-11-08
				edtForStep.getText().contains(":") ||
				// START KGU#3 2015-11-08
				// START KGU#61 2016-03-21: Enh. #84 - New set of keywords for FOR-IN loops
				edtForInPre.getText().contains(":") ||
				edtForInPost.getText().contains(":") ||
				// END KGU#61 2016-03-21
				edtWhilePre.getText().contains(":") ||
				edtWhilePost.getText().contains(":") ||
				edtRepeatPre.getText().contains(":") ||
				edtRepeatPost.getText().contains(":") ||
				// START KGU#78 2016-03-25: Enh. #23 - configurability introduced
				edtJumpLeave.getText().contains(":") ||
				edtJumpReturn.getText().contains(":") ||
				edtJumpExit.getText().contains(":") ||
				// END KGU#78 2016-03-25
				edtInput.getText().contains(":") ||
				edtOutput.getText().contains(":")
				) {
			JOptionPane.showMessageDialog(ParserPreferences.this, lblErrorSign.getText(), ttlError.getText(), JOptionPane.ERROR_MESSAGE);
		}
		// START KGU#61/KGU#628 2018-12-29: Enh. #84, #658 - Test against duplicates 
		else if (!(conflictingFields = this.hasConflicts()).isEmpty())
		{
			Color oldColour = null;
			for (JTextField textField: conflictingFields) {
				if (oldColour == null) {
					oldColour = textField.getForeground();
				}
				textField.setForeground(Color.RED);
			}
			JOptionPane.showMessageDialog(null, lblErrorSign2.getText(), ttlError.getText(), JOptionPane.ERROR_MESSAGE);
			for (JTextField textField: conflictingFields) {
				textField.setForeground(oldColour);
			}
		}
		// END KGU#61/KGU#628 2018-12-28
		else
		{
			setVisible(false);
			OK=true;
		}    

	}

	// START KGU#165 2016-03-25
	// START KGU#628 2018-12-29: Enh. #658 - we need more independent checks
	//private JTextField hasConflicts()
	private HashSet<JTextField> hasConflicts()
	// END KGU#628 2018-12-29
	{
		HashSet<JTextField> conflicts = new HashSet<JTextField>();
		JTextField conflicting = null;
		boolean ignoreCase = chkIgnoreCase.isSelected();
		JTextField[] fieldsToCheck = {
				edtAltPre,		edtAltPost,
				edtCasePre,		edtCasePost,
				edtForPre,		edtForPost,		edtForStep,
				edtForInPre,
				edtWhilePre,	edtWhilePost,
				edtRepeatPre,	edtRepeatPost,
				edtJumpLeave,	edtJumpReturn,	edtJumpExit,
				edtInput,
				edtOutput
		};
		int indexCheck2 = 12;
		String forInPost = edtForInPost.getText().trim();

		for (int i = 0; conflicting == null && i < fieldsToCheck.length; i++)
		{
			if (forInPost.equalsIgnoreCase(fieldsToCheck[i].getText().trim())
					&& (ignoreCase || forInPost.equals(fieldsToCheck[i].getText().trim())))
			{
				conflicting = fieldsToCheck[i];
			}
		}
		if (conflicting != null) {
			conflicts.add(edtForInPost);
			conflicts.add(conflicting);
		}
		for (int i = indexCheck2; i+1 < fieldsToCheck.length; i++) {
			String key1 = fieldsToCheck[i].getText().trim();
			for (int j = i+1; j < fieldsToCheck.length; j++) {
				String key2 = fieldsToCheck[j].getText().trim();
				if (key1.equalsIgnoreCase(key2) && (ignoreCase || key1.equals(key2))) {
					conflicts.add(fieldsToCheck[i]);
					conflicts.add(fieldsToCheck[j]);
				}
			}
		}
		return conflicts;
	}
	// END KGU#165 2016-03-25

}
