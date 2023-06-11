package br.com.updev.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ConfigService {
	
	@Autowired
	private Environment environment;
	
	public String info(String key) {
		return environment.getProperty(key);
	}

}
