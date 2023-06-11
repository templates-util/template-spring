package br.com.updev.dto;

import br.com.updev.exceptions.ServiceError;
import org.springframework.util.StringUtils;

public class Busca {
	
    private int pageSize;

    private int page;
    
    private String order;
    
    protected Busca() {
    	//construtor padrão
    }
    
    protected Busca(int pageSize, int page) {
    	super();
    	this.pageSize = pageSize;
    	this.page = page;
    }
    
    protected Busca(int pageSize, int page, String order) {
    	super();
    	this.pageSize = pageSize;
    	this.page = page;
        if(StringUtils.hasText(order) && (order.equals("asc") || order.equals("desc"))){
            this.order = order;
        }else{
            throw new ServiceError("Ordem inválida");
        }
    }

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

}
