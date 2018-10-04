/**
 * 
 */
package com.pluralsight.service;

import java.util.List;

import com.pluralsight.model.Customer;
import com.pluralsight.persistence.CustomerPersistence;
import com.pluralsight.persistence.HiberanteCustsomerPersistenceImpl;

/**
 * @author alvgu02
 *
 */
public class CustomerServiceImpl implements CustomerService {

	private CustomerPersistence customerRepository;
	
	
	/**
	 * @param customerRepository
	 */
	public CustomerServiceImpl(CustomerPersistence customerRepository) {
		super();
		this.customerRepository = customerRepository;
	}

	/* (non-Javadoc)
	 * @see com.pluralsight.service.CustomerService#findAll()
	 */
	public List<Customer> findAll(){
		return customerRepository.findAll();
		
	}

	/**
	 * @param customerRepository the customerRepository to set
	 */
	public void setCustomerRepository(CustomerPersistence customerRepository) {
		this.customerRepository = customerRepository;
	}
	
	
}
