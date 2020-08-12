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

import java.awt.Container;
import java.awt.BorderLayout;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.gui.Diagram;
import lu.fisch.structorizer.gui.Editor;
import lu.fisch.structorizer.gui.IconLoader;
import lu.fisch.structorizer.gui.Menu;
import lu.fisch.structorizer.gui.NSDController;
import lu.fisch.structorizer.io.Ini;
import lu.fisch.structorizer.locales.Locales;
import lu.fisch.structorizer.parsers.CodeParser;

@SuppressWarnings("serial")
public class StructorizerApplet extends JApplet  implements NSDController
{

    private Diagram diagram = null;
    private Menu menu = null;
    private Editor editor = null;

    private String lang = "en.txt";
    private String laf = "";


    @Override
    public void init()
    {
        //Execute a job on the event-dispatching thread:
        //creating this applet's GUI.
        try {
            javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    createGUI();
                }
            });
        } catch (Exception e) {
            System.err.println("createGUI didn't successfully complete");
        }
    }

    // setup the form
    public void createGUI()
    {
        // setting up the editor
        editor = new Editor(this);
        diagram = editor.diagram;
        Container container = getContentPane();
        container.setLayout(new BorderLayout());
        container.add(editor,BorderLayout.CENTER);

        // add menu
        menu = new Menu(diagram,this);
        setJMenuBar(menu);

        doButtons();
    }

    public void doButtons()
    {
        if(menu!=null)
        {
            menu.doButtonsLocal();
        }

        if (editor!=null)
        {
            editor.doButtonsLocal();
        }
    }

    public void doButtonsLocal() {}

    public void updateColors() 
    {
        if (editor!=null)
           editor.updateColors();
    }
    
    public void setLookAndFeel(String _laf)
    {
        // START KGU#661 2019-02-20: Issue #686
        javax.swing.LookAndFeel currentLaf = UIManager.getLookAndFeel();
        String currentLafName = currentLaf.getName();
        // END KGU#661 2019-02-20
        UIManager.LookAndFeelInfo plafs[] = UIManager.getInstalledLookAndFeels();
        for(int j = 0; j < plafs.length; ++j)
        {
            if(_laf.equals(plafs[j].getName()))
            {
                try
                {
                    UIManager.setLookAndFeel(plafs[j].getClassName());
                    // START KGU#661 2019-02-20: Issue #686
                    laf = _laf;
                    // END KGU#661 2019-02-20
                    SwingUtilities.updateComponentTreeUI(this);
                    // START KGU#661 2019-02-20: Issue #686
                    return;
                    // END KGU#661 2019-02-20
                }
                catch (Exception e)
                {
                    // show error
                    JOptionPane.showOptionDialog(null,e.getMessage(),
                            "Error ...",
                            JOptionPane.OK_OPTION,JOptionPane.ERROR_MESSAGE,null,null,null);
                }
            }
            // START KGU#661 2019-02-20: Issue #686
            else if (plafs[j].getClassName().equals(currentLaf.getClass().getName())) {
                currentLafName = plafs[j].getName();
            }
            // END KGU#661 2019-02-20
        }
        // START KGU#661 2019-02-20: Issue #686
        laf = currentLafName;
        // END KGU#661 2019-02-20
    }

    public String getLookAndFeel()
    {
        return laf;
    }

    public void savePreferences() {};

    public JFrame getFrame()
    {
        return null;
    }


    public void loadFromINI()
    {
        try
        {
            Ini ini = Ini.getInstance();
            ini.load();
            ini.load();

            double scaleFactor = Double.valueOf(ini.getProperty("scaleFactor","1")).intValue();
                    IconLoader.setScaleFactor(scaleFactor);
            

            // language	
            lang = ini.getProperty("Lang","en");
            Locales.getInstance().setLocale(lang);

            // colors
            Element.loadFromINI();
            updateColors();

            // parser
            CodeParser.loadFromINI();

            // look & feel
            laf = ini.getProperty("laf","Mac OS X");
            setLookAndFeel(laf);

            if (diagram != null) 
            {
                // START KGU#602 2018-10-28: outsourced to Diagrm.fetchIniProperties(Ini)
//                // current directories (KGU#95 2018-10-08: Issue #42 - defaults changed from root to home)
//                diagram.currentDirectory = new File(ini.getProperty("currentDirectory", System.getProperty("user.home")));
//                // START KGU#354 2017-04-26: Enh. #354 Also retain the other directories
//                diagram.lastCodeExportDir = new File(ini.getProperty("lastExportDirectory", System.getProperty("user.home")));
//                diagram.lastCodeImportDir = new File(ini.getProperty("lastImportDirectory", System.getProperty("user.home")));
//                diagram.lastImportFilter = ini.getProperty("lastImportDirectory", "");
//                // END KGU#354 2017-04-26
//                // START KGU#602 2018-10-28: Enh. #419
//                try {
//                	diagram.lastWordWrapLimit = Short.parseShort(ini.getProperty("wordWrapLimit", "0"));
//                }
//                catch (NumberFormatException ex) {}
//                // END KGU#602 2018-10-28
                diagram.fetchIniProperties(ini);
                // END KGU#602 2018-10-28

                // din
                if (ini.getProperty("DIN","0").equals("1")) // default = 0
                {
                    diagram.setDIN();
                }
                // comments
                if (ini.getProperty("showComments","1").equals("0")) // default = 1
                {
                    diagram.setComments(false);
                }
                // comments
                if (ini.getProperty("varHightlight","1").equals("1")) // default = 0
                {
                    diagram.setHightlightVars(true);
                }
                // analyser
                /*
                if (ini.getProperty("analyser","0").equals("0")) // default = 0
                {
                    diagram.setAnalyser(false);
                }
                 * */
            }

            // START KGU#602 2018-10-28: Outsourced to the method Diagram.fetchIniProperties(Ini) called above
//            // recent files
//            try
//            {
//                if(diagram!=null)
//                {
//                    for (int i = 9; i >= 0; i--)
//                    {
//                        if (ini.keySet().contains("recent"+i))
//                        {
//                            if (!ini.getProperty("recent"+i,"").trim().equals(""))
//                            {
//                                diagram.addRecentFile(ini.getProperty("recent"+i,""),false);
//                            }
//                        }
//                    }
//                }
//            }
//            catch(Exception e)
//            {
//                e.printStackTrace();
//                System.out.println(e.getMessage());
//            }
            // END KGU#602 2018-10-28

            // analyser
            // START KGU#239 2016-08-12: Enh. #231 + Code revision
//			Root.check1 = ini.getProperty("check1","1").equals("1");
//			Root.check2 = ini.getProperty("check2","1").equals("1");
//			Root.check3 = ini.getProperty("check3","1").equals("1");
//			Root.check4 = ini.getProperty("check4","1").equals("1");
//			Root.check5 = ini.getProperty("check5","1").equals("1");
//			Root.check6 = ini.getProperty("check6","1").equals("1");
//			Root.check7 = ini.getProperty("check7","1").equals("1");
//			Root.check8 = ini.getProperty("check8","1").equals("1");
//			Root.check9 = ini.getProperty("check9","1").equals("1");
//			Root.check10 = ini.getProperty("check10","1").equals("1");
//			Root.check11 = ini.getProperty("check11","1").equals("1");
//			Root.check12 = ini.getProperty("check12","1").equals("1");
//			Root.check13 = ini.getProperty("check13","1").equals("1");
//			Root.check14 = ini.getProperty("check14","1").equals("1");
            for (int i = 1; i <= Root.numberOfChecks(); i++)
            {
                Root.setCheck(i, ini.getProperty("check" + i, "1").equals("1"));
            }
            // END KGU#239 2016-08-12

            doButtons();
        }
        catch (Exception e) 
        {
            e.printStackTrace();
            System.out.println(e);
        }
    }

	@Override
	public boolean isRestricted() {
		return false;
	}

}