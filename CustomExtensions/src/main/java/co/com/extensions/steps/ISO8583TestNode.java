/**
 * 
 */
package co.com.extensions.steps;

import org.w3c.dom.Element;

import com.itko.lisa.test.TestCase;
import com.itko.lisa.test.TestDefException;
import com.itko.lisa.test.TestExec;
import com.itko.lisa.test.TestNode;
import com.itko.lisa.test.TestRunException;

/**
 * @author alvgu02
 *
 */
public class ISO8583TestNode extends TestNode {
	
	private String configFile;
	private String mti;
	private String hostname;
	private String port;

	public String getTypeName() throws Exception {
		// TODO Auto-generated method stub
		return "Send ISO8583 Message";
	}

	@Override
    public boolean isQuietTheDefault() {
        return true;
    }
	
	@Override
	protected void execute(TestExec arg0) throws TestRunException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initialize(TestCase arg0, Element arg1) throws TestDefException {
		// TODO Auto-generated method stub
		
	}

}
