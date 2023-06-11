package br.com.updev.services;

public interface CacheService {
	
	String get(String key);
	
	void set(String key, String value);
	
	void set(String key, String value, int ttl);
	
	void remove(String key);
	
	void clean();
	
	String getType();

}
