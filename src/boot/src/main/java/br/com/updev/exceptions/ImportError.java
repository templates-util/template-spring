package br.com.updev.exceptions;

import java.util.ArrayList;
import java.util.List;

public class ImportError extends Exception {

    private static final long serialVersionUID = 5239540722517824585L;

    private final String code;

    private final transient List<ImportDetailError> errors;

    public ImportError(String code, String message, List<ImportDetailError> errors) {
        super(message);
        this.code = code;
        this.errors = errors;
    }

    public String getCode() {
        return this.code;
    }

    public List<ImportDetailError> getErrors() {
        return this.errors != null ? new ArrayList<>(this.errors) : new ArrayList<>();
    }

}
