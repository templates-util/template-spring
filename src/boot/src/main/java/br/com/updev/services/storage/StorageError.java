package br.com.updev.services.storage;

import br.com.updev.exceptions.ServiceError;

public class StorageError extends ServiceError {
	
	public StorageError(String message) {
		super(message);
	}
	
	public StorageError(String message, Throwable t) {
		super(message, t);
	}

}
