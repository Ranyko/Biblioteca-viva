package br.com.bibliotecaviva.exception;

public class UsuarioInativoException extends RuntimeException {

    public UsuarioInativoException() {
        super("Usuário inativo");
    }
}
