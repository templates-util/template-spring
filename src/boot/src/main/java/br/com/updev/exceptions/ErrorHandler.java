package br.com.updev.exceptions;


import br.com.updev.dto.Erro;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class ErrorHandler {


    @ExceptionHandler({AccessDeniedException.class})
    public ResponseEntity<Erro> handleException(AccessDeniedException error, WebRequest request) {
        return new ResponseEntity<>(new Erro("403", error.getMessage()), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({Throwable.class})
    public ResponseEntity<Erro> handleException(Throwable error, WebRequest request) {
        error.printStackTrace();
        return new ResponseEntity<>(new Erro("500", error.getMessage()), HttpStatus.BAD_REQUEST);
    }



    @ExceptionHandler({MissingServletRequestParameterException.class})
    public ResponseEntity<Erro> handleException(MissingServletRequestParameterException error, WebRequest request) {
        return new ResponseEntity<>(new Erro("400", "O parâmetro " + error.getParameterName() + " é obrigatório"), HttpStatus.BAD_REQUEST);
    }



    @ExceptionHandler({BindException.class})
    public ResponseEntity<Erro> handleException(BindException error, WebRequest request) {
        return new ResponseEntity<>(new Erro("400", error), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ServiceError.class})
    public ResponseEntity<Erro> handleException(ServiceError error, WebRequest request) {
        Erro erro = new Erro(error);
        return new ResponseEntity<>(erro, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler({NotFoundError.class})
    public ResponseEntity<Erro> handleException(NotFoundError error, WebRequest request) {
        Erro erro = new Erro();
        erro.setCode("NOT_FOUND");
        erro.setMessage(error.getMessage());
        return new ResponseEntity<>(erro, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({AccessError.class})
    public ResponseEntity<Erro> handleException(AccessError error, WebRequest request) {
        Erro erro = new Erro();
        erro.setCode("ACCESS_ERROR");
        erro.setMessage(error.getMessage());
        return new ResponseEntity<>(erro, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<Erro> methodArgumentNotValidException(MethodArgumentNotValidException ex) {
        BindingResult result = ex.getBindingResult();

        Erro erro = new Erro("BAD_REQUEST", new BindException(result));
        return new ResponseEntity<>(erro, HttpStatus.BAD_REQUEST);
    }


}
