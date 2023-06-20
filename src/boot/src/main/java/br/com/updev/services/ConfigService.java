package br.com.updev.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ConfigService {
	
	private final Environment environment;

	@Autowired
	public ConfigService(Environment environment) {
		this.environment = environment;
	}

	public String info(String key) {
		return environment.getProperty(key);
	}

}
