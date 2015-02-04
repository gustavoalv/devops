package co.com.extensions.steps.excel;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.w3c.dom.Element;

import co.com.extensions.util.ExcelResultSetCache;

import com.itko.lisa.test.LisaException;
import com.itko.lisa.test.TestCase;
import com.itko.lisa.test.TestDefException;
import com.itko.lisa.test.TestEvent;
import com.itko.lisa.test.TestExec;
import com.itko.lisa.test.TestNode;
import com.itko.lisa.test.TestRunException;
import com.itko.util.StreamHelp;
import com.itko.util.XMLUtils;

/**
 *
 * @author rajeev
 */
public class excelStep extends TestNode {
	
	static protected Logger cat = Logger.getLogger(excelStep.class);

    public static final String FILELOCATION = "file";
    public static final String SHEETNAME = "sheet";
    public static final String STARTROW = "startrow";
    public static final String ROWCOUNT = "rows";
    public static final String ERROR_NODE = "onError";
    public static final String PRUNEEMPTYROWS = "prunerows";
    public static final String PRUNEEMPTYCOLS = "prunecols";

    public static final String FILELOCATION_KEY = "lisa.prefill.excel.filekey";
    public static final String SHEETNAME_KEY = "lisa.prefill.excel.sheetkey";
    public static final String STARTROW_KEY = "lisa.prefill.excel.startrowkey";
    public static final String ROWCOUNT_KEY = "lisa.prefill.excel.rowcountkey";
    public static final String PRUNEEMPTYROWS_KEY = "lisa.prefill.excel.prunerowskey";
    public static final String PRUNEEMPTYCOLS_KEY = "lisa.prefill.excel.prunecolskey";

    // our data
    private String fileName;
    private String sheetName;
    private String errorNode;
    private String startRowString;
    private String rowCountString;
    private boolean pruneEmptyRows = false;
    private boolean pruneEmptyCols = true;

    /**
     * What users will think of you as...
     *
     * @return What users will think of you as...
     */
    public String getTypeName() {
        return "Load Excel Sheet";
    }

    // means he won't send status-updating type events....
    @Override
    public boolean isQuietTheDefault() {
        return true;
    }

    /**
     * This is where you'll read your values from the XML Document.
     */
    public void initialize(TestCase test, Element node)
            throws TestDefException {

        setErrorNode(XMLUtils.findChildGetItsText(node, ERROR_NODE));
        setFileName(XMLUtils.findChildGetItsText(node, FILELOCATION));
        setSheetName(XMLUtils.findChildGetItsText(node, SHEETNAME));

        String srString = XMLUtils.findChildGetItsText(node, STARTROW);
        setStartRowString(srString);

        String rcString = XMLUtils.findChildGetItsText(node, ROWCOUNT);
        setRowCountString(rcString);

        String s = XMLUtils.findChildGetItsText(node, PRUNEEMPTYROWS);
        if (s == null) {
            setPruneEmptyRows(false);
        } else {
            setPruneEmptyRows(TestExec.convertStateBoolean(s, true));
        }

        s = XMLUtils.findChildGetItsText(node, PRUNEEMPTYCOLS);
        if (s == null) {
            setPruneEmptyCols(false);
        } else {
            setPruneEmptyCols(TestExec.convertStateBoolean(s, true));
        }
    }

    protected void execute(TestExec ts) throws TestRunException {
        try {
            HSSFWorkbook wbObj = null;

            String loc = ts.parseInState(getFileName());
            String sheet = ts.parseInState(getSheetName());

            String rcString = ts.parseInState(getRowCountString());
            int rc = TestExec.convertStateInt(rcString, -1); // assuming it will automatically throw TestDef

            String srString = ts.parseInState(getStartRowString());
            int sr = TestExec.convertStateInt(srString, -1); // assuming it will automatically throw TestDef

            ts.raiseEvent(TestEvent.EVENT_NODEMSG, "FileName", loc);
            ts.raiseEvent(TestEvent.EVENT_NODEMSG, "Selected Sheet", sheet);
            String rcStr = "" + ((rc < 0) ? "ALL" : rc);
            ts.raiseEvent(TestEvent.EVENT_NODEMSG, "Load Rows", rcStr);
            cat.debug(" INITIALIZING THE WORKBOOK...");
            ExcelResultSetCache rsc = null;

            if(!loc.endsWith(".csv")) {
                POIFSFileSystem fs = new POIFSFileSystem(StreamHelp.fileOpener(loc));
                wbObj = new HSSFWorkbook(fs);
                rsc = new ExcelResultSetCache(wbObj, sheet, sr, rc, ts, isPruneEmptyRows(), isPruneEmptyCols());
            } else {
                // Handle CSV
                File csvFile = new File(loc);
                rsc = new ExcelResultSetCache(csvFile);
            }

            ts.setStateValue("lisa." + getName() + ".columnCount", rsc.getColumnCount());
            ts.setStateValue("lisa." + getName() + ".rowCount", rsc.getRowCount());

            ts.setLastResponse(rsc);
            ts.raiseEvent(TestEvent.EVENT_NODERESPONSE, getName(), rsc.toString(), false);

        } catch (TestDefException tde) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            tde.printStackTrace(pw);

            String msg = LisaException.exceptionToString(getTypeName(), "Test Def Error", getName(), tde);
            ts.raiseEvent(TestEvent.EVENT_TESTDEFERROR, getName(), msg);
        } catch (Exception se) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            se.printStackTrace(pw);

            String msg = LisaException.exceptionToString(getTypeName(), "Error reading file", getName(), se);
            ts.raiseEvent(TestEvent.EVENT_ERROR, getName(), msg);
            ts.setNextNode(errorNode);
            ts.setLastResponse(sw.toString());
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(1024);

        sb.append("Step name=");
        sb.append(name);

        return sb.toString();
    }

    public boolean isPruneEmptyRows() {
        return pruneEmptyRows;
    }

    public void setPruneEmptyRows(boolean p) {
        this.pruneEmptyRows = p;
    }

    public boolean isPruneEmptyCols() {
        return pruneEmptyCols;
    }

    public void setPruneEmptyCols(boolean p) {
        this.pruneEmptyCols = p;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String file) {
        this.fileName = file;
    }

    public String getErrorNode() {
        return errorNode;
    }

    public void setErrorNode(String errorNode) {
        this.errorNode = errorNode;
    }

    public String getStartRowString() {
        return startRowString;
    }

    public void setStartRowString(String srString) {
        this.startRowString = srString;
    }
    public String getRowCountString() {
        return rowCountString;
    }

    public void setRowCountString(String rcString) {
        this.rowCountString = rcString;
    }

    public String getSheetName() {
            return sheetName;
    }

    public void setSheetName(String sheet) {
        this.sheetName = sheet;
    }
}
