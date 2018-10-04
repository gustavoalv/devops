/**
 * 
 */
package com.pluralsight.persistence;

import java.util.ArrayList;
import java.util.List;

import com.pluralsight.model.Customer;

/**
 * @author alvgu02
 *
 */
public class HiberanteCustsomerPersistenceImpl implements CustomerPersistence {


	/* (non-Javadoc)
	 * @see com.pluralsight.repository.CustomerRepository#findAll()
	 */
	public List<Customer> findAll() {

		List<Customer> customers = new ArrayList<Customer>();
		Customer customer = new Customer();
		customer.setFirstName("Gustavo");
		customer.setLastName("Alvarez");
		
		customers.add(customer);
		
		return customers;
		
	}
}
