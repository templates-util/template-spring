package br.com.updev;

import br.com.updev.exceptions.ServiceError;

import java.text.SimpleDateFormat;

public class UtilData {
	
	private UtilData() {
        throw new ServiceError("Classe utilitaria");
    }
	
    /**
     * @return yyyy-MM-dd'T'HH:mm:ss
     */
	public static SimpleDateFormat isoDateTime() {
		return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	}
	
    /**
     * @return yyyy-MM-dd
     */
	public static SimpleDateFormat isoDate() {
		return new SimpleDateFormat("yyyy-MM-dd");
	}
	
    /**
     * @return dd/MM/yyyy
     */
	public static SimpleDateFormat formatoDataBr() {
        return new SimpleDateFormat("dd/MM/yyyy");
    }
}
