package com.pluralsight.persistence;

import java.util.List;

import com.pluralsight.model.Customer;

public interface CustomerPersistence {

	/**
	 * 
	 * @return
	 */
	List<Customer> findAll();

}