import com.pluralsight.service.CustomerService;
import com.pluralsight.service.CustomerServiceImpl;

/**
 * 
 */

/**
 * @author alvgu02
 *
 */
public class Application {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		CustomerService customerService =  new CustomerServiceImpl();
		System.out.println(customerService.findAll().get(0).getFirstName());
	}

}
