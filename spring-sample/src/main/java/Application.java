import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.pluralsight.service.CustomerService;

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
		
		ApplicationContext appContext = new ClassPathXmlApplicationContext("ApplicationContext.xml");
		CustomerService customerService =  appContext.getBean("customerService", CustomerService.class);
		
		System.out.println(customerService.findAll().get(0).getFirstName());
	}

}
