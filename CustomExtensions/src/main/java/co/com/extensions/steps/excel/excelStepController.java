package co.com.extensions.steps.excel;
import com.itko.lisa.editor.TestNodeInfo;
import com.itko.util.StrUtil;
import com.itko.util.XMLUtils;
import com.itko.lisa.resources.*;

import java.io.PrintWriter;

import org.apache.log4j.Logger;

/**
 *
 * @author rajeev
 */
public class excelStepController extends TestNodeInfo {
	
	static protected Logger cat = Logger.getLogger(excelStepController.class);
    
    public excelStepController() {
    }

    static final String KEY = "excelStep.key";
    static public final String NAME = "Load Excel";
    private TestNodeInfo ni = this;
    
    public String getEditorName()
    {
        return "Load Excel Sheet into ResultSet";
    }


    /**
     * Needed to set our specific help text
     * @return our help text as HTML
     */
    public String getHelpString()
    {
        return "Use this step to load s spreadsheet into a ResultSet";
    }


    /**
     * For icons you have 3 choices.
     * <ul>
     * <li>Do nothing: return null from all the below, and the framework will give you a generic icon
     * <li>Put your icons in com.itko.lisa.resources and provide their names with loadSmallIcon() and loadLargeIcon()
     * <li>Make the icons yourself, implement loadSmall/LargeIcon() but they will be ignored, and implement getSmallIcon
     * and getLargeIcon()
     */
    public String loadSmallIcon()
    {
        return "excel_16x16.png";
    }


    public String loadLargeIcon()
    {
        return "excel_64x64.png";
    }



    /**
     * During construction, you with EITHER get an exsiting test case element, or a new one.
     * Existing elements have this migrate() called, while new ones get the initNewOne().
     *
     * @param tn the test case element to migrate into you
     */

  public void migrate( Object tn )
    {
        excelStep n = (excelStep)tn;
        putAttribute( KEY, n );

        try {
            ni.putAttribute( excelStep.FILELOCATION, n.getFileName() );
            ni.putAttribute( excelStep.SHEETNAME, n.getSheetName() );
            ni.putAttribute( excelStep.STARTROW, n.getStartRowString());
            ni.putAttribute( excelStep.ROWCOUNT, n.getRowCountString());
            ni.putAttribute( excelStep.PRUNEEMPTYROWS, Boolean.valueOf(n.isPruneEmptyRows()) );
            ni.putAttribute( excelStep.PRUNEEMPTYCOLS, Boolean.valueOf(n.isPruneEmptyCols()) );

            safePut( ni, excelStep.ERROR_NODE, n.getErrorNode() );
        }
        catch( Exception ex )
        {
            cat.error( "Unable to migrate test: " + " " + n.getName(), ex );
        }
    }
  
    private static void safePut( TestNodeInfo ni, String k, String v )
    {
        if( v == null )
            ni.putAttribute( k, "" );
        else
            ni.putAttribute( k, v );
    }

    public void initNewOne()
    {
         excelStep n = new excelStep();
         putAttribute( KEY, n );

        ni.putAttribute( excelStep.FILELOCATION, "" );
        ni.putAttribute( excelStep.SHEETNAME, "" );
        ni.putAttribute( excelStep.STARTROW, "0" );
        ni.putAttribute( excelStep.ROWCOUNT, "-1" );
        ni.putAttribute( excelStep.ERROR_NODE, "fail" );
        ni.putAttribute( excelStep.PRUNEEMPTYROWS, Boolean.FALSE );
        ni.putAttribute( excelStep.PRUNEEMPTYCOLS, Boolean.TRUE );

        if( ni.getRet() != null)
            ni.setRet( null );
    }

       
    public void writeSubXML( PrintWriter ps )
    {
        XMLUtils.streamTagAndChild( ps, excelStep.FILELOCATION, (String)ni.getAttribute(excelStep.FILELOCATION) );
        XMLUtils.streamTagAndChild( ps, excelStep.SHEETNAME, (String)ni.getAttribute(excelStep.SHEETNAME) );
        XMLUtils.streamTagAndChild( ps, excelStep.STARTROW, (String)ni.getAttribute(excelStep.STARTROW) );
        XMLUtils.streamTagAndChild( ps, excelStep.ROWCOUNT, (String)ni.getAttribute(excelStep.ROWCOUNT) );
        XMLUtils.streamTagAndChild( ps, excelStep.ERROR_NODE, (String)ni.getAttribute(excelStep.ERROR_NODE) );
        XMLUtils.streamTagAndChild( ps, excelStep.PRUNEEMPTYROWS, (ni.getAttribute(excelStep.PRUNEEMPTYROWS)).toString() );
        XMLUtils.streamTagAndChild( ps, excelStep.PRUNEEMPTYCOLS, (ni.getAttribute(excelStep.PRUNEEMPTYCOLS)).toString() );

        ps.flush();
    }

    public void changeNodeName(String from, String to)
    {
        String n = StrUtil.safeString( getAttribute( excelStep.ERROR_NODE ) );
        if( n.equals( from ) )
            putAttribute( excelStep.ERROR_NODE, to );
    }
    
    public void destroy()
    {
        super.destroy();
    }
}
