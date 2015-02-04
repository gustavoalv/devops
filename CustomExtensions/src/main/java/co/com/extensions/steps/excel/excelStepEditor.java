/*
 * excelStepEditor.java
 *
 * Created on Jan 3, 2008, 1:03:06 PM
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package co.com.extensions.steps.excel;

import java.awt.event.ItemEvent;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import co.com.extensions.dph.Iso8583DataProtocolHandler;

import com.itko.lisa.editor.CustomEditor;
import com.itko.lisa.editor.TestNodeInfo;
import com.itko.lisa.editor.NodeListDropdown;
import com.itko.lisa.editor.TestCaseInfo;
import com.itko.lisa.gui.*;
import com.itko.lisa.test.TestExec;
import com.itko.util.swing.Borders;
import com.itko.util.swing.controls.BrowseForStreamButton;
import com.itko.util.swing.panels.ProcessingCallback;
import com.itko.util.swing.panels.ProcessingDialogCancelledException;
import com.itko.util.StrUtil;

//import com.itko.lisa.resources.Strings;
import com.itko.util.module.ModuleITKOUtils;
import com.itko.lisa.test.TestCase;
import com.itko.util.SimpleFileFilter;
import com.itko.util.StreamHelp;
//import com.itko.util.swing.GridPanel;
import com.itko.util.swing.MutableGridPanel;
import com.itko.util.swing.PrefillCombo;

import java.awt.event.ItemListener;
import java.io.*;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.openide.util.Exceptions;

/**
 *
 * @author rajeev
 */
public class excelStepEditor extends CustomEditor {

	static protected Logger cat = Logger.getLogger(excelStepEditor.class);
	
    static public final String NAME = excelStepController.NAME;
    private TestCaseInfo tc;
    private TestNodeInfo ni;
    private ViewResultMasterPanel rv = null;
    protected PrefillCombo fileTF;
    protected PrefillCombo sheetCB;
    protected JTextField rowsTF;
    protected JTextField startRowTF;
    private NodeListDropdown onFail;
    private JCheckBox pruneRowsCB = null;
    private JCheckBox pruneColsCB = null;

    private void setup() {
        ni = getController().getTestNode();

        fileTF = new PrefillCombo(excelStep.FILELOCATION_KEY, System.getProperties(), null, LisaPrefs.getLisaPrefsObj(), true, null);
        sheetCB = new PrefillCombo(excelStep.SHEETNAME_KEY, System.getProperties(), null, LisaPrefs.getLisaPrefsObj(), true, null);
        rowsTF = new JTextField(10);
        startRowTF = new JTextField(10);

        onFail = new NodeListDropdown(false, true);
        pruneRowsCB = new JCheckBox("Prune Empty Rows" + " ");
        pruneColsCB = new JCheckBox("Prune Empty Cols" + " ");

        BrowseForStreamButton dotdotdot = new BrowseForStreamButton(true, new SimpleFileFilter(new String[]{".xls", ".csv"}, "Excel Workbook (*.xls, *.csv)"), fileTF.getComponent());
        dotdotdot.setIcons(GuiMenuBar.fileSysIcon, GuiMenuBar.classPathIcon, GuiMenuBar.urlIcon);
        if (dotdotdot.getComponent() instanceof JComboBox) {
            JComboBox browseCombo = (JComboBox) dotdotdot.getComponent();
            browseCombo.addItemListener(new ItemListener() {

                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED && e.getItem() != null) {
                        try {
                            populateSheets();
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                            LisaGuiPanel.errorMsg(fileTF.getComponent(), "Invalid Selection", ex.getMessage());
                        }
                    }
                }
            });
        }

        fileTF.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED && e.getItem() != null) {
                    try {
                        populateSheets();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                        LisaGuiPanel.errorMsg(fileTF.getComponent(), "Invalid Selection", ex.getMessage());
                    }
                }
            }
        });

        JButton tryIt = new JButton("Load");
        tryIt.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                execute();
            }
        });


        JPanel top = new JPanel(new BorderLayout(0, 0));
        top.setBorder(Borders.createGroupBox(null));

//        GridPanel gridPanel = new GridPanel(4, 5, 2);
        MutableGridPanel gridPanel = new MutableGridPanel(5, 2);

        gridPanel.add(new JLabel("Spreadsheet (.xls, .csv): ", JLabel.LEFT), fileTF.getComponent(), dotdotdot.getComponent());
        gridPanel.add(new JLabel("Sheet: ", JLabel.LEFT), sheetCB.getComponent(), new JLabel(" ", JLabel.RIGHT));

        JPanel rowsPanel = new JPanel(new BorderLayout());
        JPanel startRowPanel = new JPanel(new BorderLayout());
        //startRowPanel.add(new JLabel("Start Row: ", JLabel.LEFT), BorderLayout.WEST);
        startRowPanel.add(startRowTF, BorderLayout.WEST);

        JPanel rowsTFPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        rowsTFPanel.add(new JLabel("       Max Rows: ", JLabel.LEFT), gbc);
        gbc.gridx++;
        gbc.weightx = 0;
        rowsTFPanel.add(rowsTF, gbc);
        gbc.gridx++;
        gbc.weightx = 1;
        rowsTFPanel.add(new JLabel(" "), gbc);

        rowsPanel.add(startRowPanel, BorderLayout.WEST);
        rowsPanel.add(rowsTFPanel, BorderLayout.CENTER);
        rowsPanel.add(new JLabel(" "), BorderLayout.EAST);

        gridPanel.add(new JLabel("Start Row: ", JLabel.LEFT), rowsPanel);

        JPanel prunePanel = new JPanel(new BorderLayout());

        prunePanel.add(pruneRowsCB, BorderLayout.WEST);
        prunePanel.add(pruneColsCB, BorderLayout.CENTER);
        prunePanel.add(new JLabel(" "), BorderLayout.EAST);

        gridPanel.add(new JLabel(" ", JLabel.LEFT), prunePanel, tryIt);

//        GridPanel onFailureDropDown = new GridPanel(1, 2, 2);
        MutableGridPanel onFailureDropDown = new MutableGridPanel(2, 2);

        onFailureDropDown.add(new JLabel(ModuleITKOUtils.resources.get("editor.filenoded.onfailL") + " ", JLabel.LEFT), onFail);

        top.add(gridPanel.getPanel(), BorderLayout.NORTH);
        top.add(onFailureDropDown.getPanel(), BorderLayout.EAST);     // BugzId: 9532

        rv = new ViewResultMasterPanel(false, true);

        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(rv, BorderLayout.CENTER);
    }

    private void populateSheets() throws IOException {
        if (fileTF == null) {
            return;
        }
        if (!StrUtil.isEmpty(fileTF.getText())) {

            cat.debug("Selected Excel: " + fileTF.getText());
            if (ni == null) {
                ni = (TestNodeInfo) getController();
            }
            TestCaseInfo tci = ni.getTestCaseInfo();
            TestExec ts = tci.getTestExec();

            final String loc = ts.parseInState(fileTF.getText());

            if (loc.endsWith(".csv")) {
                sheetCB.setEnabled(false);
            } else {
                sheetCB.setEnabled(true);
                POIFSFileSystem fs = new POIFSFileSystem(StreamHelp.fileOpener(loc));
                HSSFWorkbook wbObj = new HSSFWorkbook(fs);
                String[] sheets = generateSheetNames(wbObj);
                if (sheets != null) {
                    if (sheets.length > 0) {
                        sheetCB.removeAllUserState();
                        sheetCB.reset();
                        sheetCB.setStaticList(sheets);
                        sheetCB.setSelectedItem(sheets[0]);
                    }
                }
            }
        }
    }

    private synchronized String[] generateSheetNames(HSSFWorkbook wbObj) {
        String[] workSheetNames = null;
        if (wbObj != null) {
            int totalNoOfSheets = wbObj.getNumberOfSheets();
            workSheetNames = new String[totalNoOfSheets];
            for (int i = 0; i < totalNoOfSheets; i++) {
                workSheetNames[i] = wbObj.getSheetName(i);
                cat.debug(" SHEET NAME :" + wbObj.getSheetName(i));
            }
        }
        return workSheetNames;
    }

    public void display() {
        if (fileTF == null) {
            setup();
        }

        if (ni == null) {
            ni = (TestNodeInfo) getController();
        }

        excelStep n = (excelStep) ni.getAttribute(excelStepController.KEY);     // BugzId: 9532
        String loc = (String) ni.getAttribute(excelStep.FILELOCATION);
        fileTF.setText(loc);
        sheetCB.setText((String) ni.getAttribute(excelStep.SHEETNAME));
        rowsTF.setText((String) ni.getAttribute(excelStep.ROWCOUNT));
        startRowTF.setText((String) ni.getAttribute(excelStep.STARTROW));
        onFail.setList(ni.getTestCaseInfo().getNodes());
        if (StrUtil.isEmpty(n.getErrorNode())) {
            n.setErrorNode(TestCase.FAIL_NODE);
        }
        onFail.setCurrElement(n.getErrorNode());

        boolean pRows = ((Boolean) ni.getAttribute(excelStep.PRUNEEMPTYROWS)).booleanValue();
        pruneRowsCB.setSelected(pRows);
        pruneColsCB.setSelected(((Boolean) ni.getAttribute(excelStep.PRUNEEMPTYCOLS)).booleanValue());

        if (loc != null) {
            if (loc.endsWith(".csv")) {
                sheetCB.setEnabled(false);
            } else {
                sheetCB.setEnabled(true);
            }
        }

        rv.setNodeInfo(ni.getTestCaseInfo(), ni);
        rv.setNodeInfo(tc, ni);
    }

    private void execute() {
        if (ni == null) {
            ni = (TestNodeInfo) getController();
        }
        TestCaseInfo tci = ni.getTestCaseInfo();
        TestExec ts = tci.getTestExec();

        String msg = isEditorValid();
        if (msg != null) {
            LisaGuiPanel.infoMsg(this, getName(), msg);
            return;
        }
        save();

        try {
            final StringBuffer sb = new StringBuffer();

            LisaGuiPanel.doProcessingDialog(getName(), this,
                    "Loading Spreadsheet",
                    new ProcessingCallback() {

                        public void doCallback(Object o) throws Throwable {
                            sb.append(doExec());
                        }
                    }, true);
            LisaGuiPanel.infoMsg(this, NAME, sb.toString());
            display();
        } catch (ProcessingDialogCancelledException can) {
            cat.info("...");
        } catch (Throwable tw) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            tw.printStackTrace(pw);
            ni.setRet(sw.toString());
            LisaGuiPanel.errorMsg(this, getName(), "Error loading : " + " " + tw.getMessage(), tw);
            display();
        }
    }

    private String doExec() throws Exception {
        if (ni == null) {
            ni = (TestNodeInfo) getController();
        }
        TestCaseInfo tci = ni.getTestCaseInfo();
        TestExec ts = tci.getTestExec();

        final String loc = ts.parseInState(fileTF.getText());
        String sheet = "";
        if (!loc.endsWith(".csv")) {
            sheet = ts.parseInState(sheetCB.getText());
        }
        final String rcString = ts.parseInState(rowsTF.getText());
        final String srString = ts.parseInState(startRowTF.getText());

        excelStep pn = new excelStep();
        pn.setFileName(loc);
        pn.setSheetName(sheet);
        pn.setRowCountString(rcString);
        pn.setStartRowString(srString);
        pn.setPruneEmptyRows(pruneRowsCB.isSelected());
        pn.setPruneEmptyCols(pruneColsCB.isSelected());

        pn.execute(ts);

        if (ts.getLastResponse() != null) {
            ni.setRet(ts.getLastResponse());
        }

        return "Loading Excel Successful!";
    }

    public String toString() {
        return NAME;
    }

    public String isEditorValid() {
        if (StrUtil.isEmpty(fileTF.getText())) {
            return "Need File Name";
        }

        if (onFail.getCurrentNode() == null) {
            return "Please provide an On Error Node";
        }

        TestCaseInfo tci = ni.getTestCaseInfo();
        TestExec ts = tci.getTestExec();
        String loc = ts.parseInState(fileTF.getText());

        if (!(loc.endsWith(".csv") || loc.endsWith(".xls"))) {
            return "File selected must be either .xls or .csv";
        }
        return null;
    }

    public void save() {
        try {
            ni.getTestCaseInfo().getTestExec().saveNodeResponse(ni.getName(), ni.getRet());
            ni.putAttribute(excelStep.FILELOCATION, fileTF.getText());
            ni.putAttribute(excelStep.SHEETNAME, sheetCB.getText());
            ni.putAttribute(excelStep.STARTROW, startRowTF.getText());
            ni.putAttribute(excelStep.ROWCOUNT, rowsTF.getText());
            ni.putAttribute(excelStep.PRUNEEMPTYROWS, new Boolean(pruneRowsCB.isSelected()));
            ni.putAttribute(excelStep.PRUNEEMPTYCOLS, new Boolean(pruneColsCB.isSelected()));
            ni.putAttribute(excelStep.ERROR_NODE, onFail.getCurrentNodeName());
        } catch (Exception ex) {
            LisaGuiPanel.errorMsg(this, NAME, ModuleITKOUtils.resources.get("jdbc.editor.badsaveMsg"), ex);
        }
    }
}
