package br.com.updev;

import br.com.updev.exceptions.ServiceError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

public class UtilMetodo {

    private static final Logger logger = LoggerFactory.getLogger("UtilMetodo");

    // Private constructor to prevent initialization
    private UtilMetodo() {
        throw new ServiceError("Classe utilitaria");
    }

    public static void cleanUp(Path path) {
        try {
            Files.delete(path);
        } catch (IOException e) {
            logger.error("Não foi possível apagar o arquivo {}", path, e);
        }
    }

    public static Random rand() {
        return new Random();
    }
}
