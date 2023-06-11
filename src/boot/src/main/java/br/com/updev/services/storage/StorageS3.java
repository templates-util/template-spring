package br.com.updev.services.storage;

import br.com.updev.UtilMetodo;
import br.com.updev.domain.Arquivo;
import br.com.updev.domain.ArquivoDownload;
import br.com.updev.domain.Usuario;
import br.com.updev.repositories.ArquivoDownloadRepository;
import br.com.updev.repositories.ArquivoRepository;
import br.com.updev.repositories.UsuarioRepository;
import br.com.updev.services.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.*;
import java.util.Arrays;
import java.util.UUID;

@Component
public class StorageS3 implements StorageService, HealthIndicator {

	private final ArquivoRepository arquivoRepository;
	
	private final ArquivoDownloadRepository arquivoDownloadRepository;
	
	private final UsuarioRepository usuarioRepository;
	
	private final S3Client s3Client;
	
	private static final Logger logger = LoggerFactory.getLogger("StorageS3");
	
	@Value("${s3.bucket.name}")
	private String bucket;
	
	@Autowired
	public StorageS3(ArquivoRepository arquivoRepository, ArquivoDownloadRepository arquivoDownloadRepository, S3Client s3Client, UsuarioRepository usuarioRepository) {
		this.arquivoRepository = arquivoRepository;
		this.arquivoDownloadRepository = arquivoDownloadRepository;
		this.s3Client = s3Client;
		this.usuarioRepository = usuarioRepository;
	}
	
	private File createTempFile(InputStream input) throws IOException {
		File arquivo = new File(System.getProperty("java.io.tmpdir") + "/tempUpload_" + UUID.randomUUID());
		try (FileOutputStream fos = new FileOutputStream(arquivo)) {
			int c;
			byte[] buffer = new byte[65536];
			while ((c = input.read(buffer)) != -1) {
				fos.write(buffer, 0, c);
			}
		}
		return arquivo;
	}
	
	@Override
	public Arquivo store(String nomeOriginal, String bucket, Usuario usuario, InputStream conteudo) {
		logger.info("Salvando arquivo");
		if (nomeOriginal == null) {
			throw new StorageError("Nome original indefinido");
		}
		
		if (usuario == null) {
			throw new StorageError("Usuário indefinido");
		}
		
		if (conteudo == null) {
			throw new StorageError("Conteúdo indefinido");
		}
		
		Arquivo arquivo = new Arquivo();
		arquivo.setBucket(bucket);
		arquivo.setUuid(UUID.randomUUID().toString());
		
		arquivo.setOriginalName(nomeOriginal);
		
		arquivo.setUsuario(usuario);
		arquivo.setExcluido(false);
		arquivo.setCreatedAt(new java.util.Date());
		
		File arquivoTemp = null;
		try {
			arquivoTemp = this.createTempFile(conteudo);

			this.s3Client.putObject(PutObjectRequest.builder()
					.bucket(bucket)
					.contentLength(arquivoTemp.length())
					.key(arquivo.getUuid())
					.build(), 
					RequestBody.fromFile(arquivoTemp));
		} catch (IOException ex) {
			throw new StorageError("Erro no sistema de arquivos", ex);
		} finally {
			if (arquivoTemp != null && arquivoTemp.exists()) {
				UtilMetodo.cleanUp(arquivoTemp.toPath());
			}
		}
		
		return this.arquivoRepository.save(arquivo);
	}

	@Override
	public InputStream read(Arquivo arquivo, Usuario usuario) {
		if (arquivo == null) {
			throw new StorageError("Arquivo indefinido");
		}
		if (usuario == null) {
			throw new StorageError("Usuário indefinido");
		}
		
		ArquivoDownload download = new ArquivoDownload();
		
		download.setArquivo(arquivo);
		download.setUsuario(usuario);
		arquivoDownloadRepository.save(download);
		
		return this.s3Client.getObject(GetObjectRequest.builder()
													   .bucket(arquivo.getBucket())
													   .key(arquivo.getUuid())
													   .build());
	}

	@Override
	public void remove(Arquivo arquivo) {
		DeleteObjectRequest request = DeleteObjectRequest.builder().bucket(arquivo.getBucket()).key(arquivo.getUuid()).build();
		this.s3Client.deleteObject(request);
		arquivo.setExcluido(true);
		arquivoRepository.save(arquivo);
	}

	@Override
	public String getType() {
		return "AWS S3";
	}

	@Override
	public Health health() {

		String nomeArquivoTeste = "ArquivoTeste-" + UUID.randomUUID();

		StringBuilder builder = new StringBuilder();
		int tamanho = 50; // aqui nós fixamos o tamanho para ter um teste consistente
		for (int i = 0; i < tamanho; i++) {
			builder.append(UUID.randomUUID());
		}

		byte[] conteudoTeste = builder.toString().getBytes();

		Arquivo registro;
		Usuario usuario = usuarioRepository.findByEmail("admin@itexto.com.br");

		try (InputStream inputStream = new ByteArrayInputStream(conteudoTeste)) { // aqui nós criamos um InputStream a partir de um array de bytes
			registro = this.store(nomeArquivoTeste, this.bucket, usuario, inputStream);
		} catch (IOException ex) {
			return Health.down().withDetail("s3.error.write.io", ex.getMessage()).build();
		}

		try (InputStream leituraArquivo = this.read(registro, usuario)) {
			byte[] buffer = new byte[conteudoTeste.length];
			if (leituraArquivo.read(buffer) != buffer.length) {
				throw new IOException("Não foi possível ler todo o conteúdo");
			}
			if (!Arrays.equals(conteudoTeste, buffer)) {
				return Health.down().withDetail("s3.error.inconsistent", "O valor escrito é diferente do lido no bucket").build();
			}
		} catch (IOException error) {
			return Health.down().withDetail("s3.error.read", error.getMessage()).build();
		}

		return Health.up().build();
	}

}
