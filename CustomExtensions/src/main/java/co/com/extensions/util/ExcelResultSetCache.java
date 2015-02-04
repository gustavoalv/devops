/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.extensions.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.itko.lisa.dynexec.dtodatasets.XLSUtils;
import com.itko.lisa.resultset.ResultSetCacheBase;
import com.itko.lisa.test.TestExec;
import com.itko.util.DateUtils;
import com.itko.util.StrUtil;

/**
 *
 * @author rajeev
 */
public class ExcelResultSetCache extends ResultSetCacheBase {
    //private SQLWarning warning = null;

	static protected Logger cat = Logger.getLogger(ExcelResultSetCache.class);
	
    private int maxRows;

    public ExcelResultSetCache(File csvFile) {
        try {
            FileReader reader = new FileReader(csvFile);
            BufferedReader r = new BufferedReader(reader);
            String line;
            int rowNumber = 0;
            while ( (line = r.readLine()) != null) {
                line = line.trim();
                //if (line.startsWith("#"))   continue;
                String[] arr = line.split(",");
                ArrayList al = new ArrayList();
                for(int i = 0; i < arr.length; i++) {
                    if(rowNumber == 0) {
                        columns.add(arr[i].trim());
                    } else {
                        al.add(arr[i].trim());
                    }
                }
                if(rowNumber != 0) rows.add(al);
                rowNumber++;
            }
            r.close();
            reader.close();
        } catch (java.io.FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (java.io.IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public ExcelResultSetCache(HSSFWorkbook wbObj, String sheet, TestExec ts) throws Exception {
        this(wbObj, sheet, 0, -1, ts);
    }

    public ExcelResultSetCache(HSSFWorkbook wbObj, String sheet, int startRow, int mRows, TestExec ts) throws Exception {
        this(wbObj, sheet, startRow, mRows, ts, false, false);
    }

    public ExcelResultSetCache(HSSFWorkbook wbObj, String sheet, int startRow, int mRows, TestExec ts, boolean pruneEmptyRows, boolean pruneEmptyCols)
            throws Exception {

        XLSUtils xlsUtils = new XLSUtils(wbObj);
        ArrayList<Integer> physicalComumns = new ArrayList();

        HSSFSheet s = wbObj.getSheet(sheet);

        int firstRow = findFirstRow(s, startRow);

        int phyRows = s.getPhysicalNumberOfRows();

        maxRows = mRows;

        if ((mRows < 0) || (mRows > phyRows)) {
            maxRows = phyRows - 1;
        }

        HSSFRow headerRow = s.getRow(firstRow);
        if(pruneEmptyCols) {
            int cellCount = headerRow.getPhysicalNumberOfCells();
            cat.debug("Physical Number of Cells = " + cellCount);

            Iterator it = headerRow.cellIterator();
            while (it.hasNext()) {
                HSSFCell c = (HSSFCell) it.next();
                int colNum = c.getColumnIndex(); // use getColumnIndex in newer version
                columns.add(xlsUtils.getCellValue(s, firstRow, colNum));
                physicalComumns.add(new Integer(colNum));
            }
        } else {
            int cellCount = headerRow.getLastCellNum() - headerRow.getFirstCellNum();
            cat.debug("Physical Number of Cells = " + cellCount);
            for (int i=headerRow.getFirstCellNum(); i < headerRow.getLastCellNum() ; i++) {
                physicalComumns.add(new Integer(i));
                String v = xlsUtils.getCellValue(s, firstRow, i);
                if(StrUtil.isEmpty(v)) {
                    columns.add("COL"+i);
                } else {
                    columns.add(v);
                }
            }
        }

        //columnCount = physicalComumns.size();

        for (int i = firstRow + 1; i < maxRows + firstRow + 1; i++) {
            HSSFRow row = s.getRow(i);
            ArrayList al = new ArrayList();
            Iterator pit = physicalComumns.iterator();


            if ((row == null) || (row.getPhysicalNumberOfCells() == 0)) {
                if (!pruneEmptyRows) {
                    while (pit.hasNext()) {
                        pit.next();
                        al.add(""); //adding empty row
                    }
                    rows.add(al);
                }
                continue;
            }

            while (pit.hasNext()) {
                int pCol = ((Integer) pit.next()).intValue();

                HSSFCell c = row.getCell(pCol);
//                   String colValue = xlsUtils.getCellValue(s, i, pCol);
//                   al.add(colValue);
                if (c == null) {
                    al.add("");
                    continue;
                }

                switch (c.getCellType()) {
                    case HSSFCell.CELL_TYPE_STRING:
                    case HSSFCell.CELL_TYPE_BLANK:
                    case HSSFCell.CELL_TYPE_BOOLEAN:
                        String colValue = xlsUtils.getCellValue(s, i, pCol);
                        al.add(colValue);
                        break;
                    default:
                        if (HSSFDateUtil.isCellDateFormatted(c)) {
                            Date d = c.getDateCellValue();
                            //controlled by lisa.excel.dateTimeFormat;
                            String format = (String) ts.getStateValue("lisa.excel.dateTimeFormat");
                            if (StrUtil.isEmpty(format)) {
                                format = "MM-dd-yyyy";
                            }
                            String dateString = DateUtils.formatDate(d, format);
                            al.add(dateString);
                        } else {
                            colValue = xlsUtils.getCellValue(s, i, pCol);
                            al.add(colValue);
                        }
                }
            }
            rows.add(al);
        }
    }

    int findFirstRow(HSSFSheet sheet, int from) {
        if (sheet.getPhysicalNumberOfRows() == 0) {
            return 0;
        }

        for (int i = from; i <= sheet.getPhysicalNumberOfRows(); i++) {
            HSSFRow rowObj = sheet.getRow(i);
            if (rowObj != null) {
                int cellNos = rowObj.getPhysicalNumberOfCells();
                if (cellNos != 0) {
                    return i;
                }
            }
        }
        return 0;
    }

    Date getCellValueAsDate(HSSFSheet sheet, HSSFCell objCell) {
        // protect against this b/c we do it alot
        if (sheet == null || objCell == null) {
            return null;
        }

        // Chip Killmar, November 6, 2007
        // FogBugz 6561: Problem with date formatted cell in Excel DTO.
        // The cell could have a custom format, but a date can still be created from it.
        // Rather than checking the format, check that a valid date is available.
        switch (objCell.getCellType()) {
            case HSSFCell.CELL_TYPE_NUMERIC: {
                if (HSSFDateUtil.isValidExcelDate(objCell.getNumericCellValue())) {
                    return HSSFDateUtil.getJavaDate(objCell.getNumericCellValue());
                }
                break;
            }

            case HSSFCell.CELL_TYPE_STRING: {
                Date date;
                try {
                    date = DateUtils.parseRFC3339DateString(objCell.getRichStringCellValue().getString());
                    return date;
                } catch (NumberFormatException e) {
                    cat.debug("Couldn't parse RFC 3339 date/time string: " + objCell.getRichStringCellValue().getString());
                }
                break;
            }

            default: {
                break;
            }
        }

        cat.warn("Invalid date, cell value=" + objCell.toString());
        return null;
    }
}
