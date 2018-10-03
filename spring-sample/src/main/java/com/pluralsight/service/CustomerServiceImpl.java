/**
 * 
 */
package com.pluralsight.service;

import java.util.List;

import com.pluralsight.model.Customer;
import com.pluralsight.repository.CustomerRepository;
import com.pluralsight.repository.HiberanteCustsomerRepositoryImpl;

/**
 * @author alvgu02
 *
 */
public class CustomerServiceImpl implements CustomerService {

	private CustomerRepository customerRepository = new HiberanteCustsomerRepositoryImpl();
	
	/* (non-Javadoc)
	 * @see com.pluralsight.service.CustomerService#findAll()
	 */
	public List<Customer> findAll(){
		return customerRepository.findAll();
		
	}
}
