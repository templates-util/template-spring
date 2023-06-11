package br.com.updev.services;

import br.com.updev.domain.Arquivo;
import br.com.updev.domain.Usuario;

import java.io.InputStream;

public interface StorageService {
	
	Arquivo store(String nomeOriginal, String bucket, Usuario usuario, InputStream conteudo);
	
	InputStream read(Arquivo arquivo, Usuario usuario);
	
	void remove(Arquivo arquivo);
	
	String getType();

}
