package br.com.gestaocontas.models;

/**
 *
 * @author caio
 */
public class Fornecedores {

    private int id;
    String nome, cnpj, contato, status;

    public Fornecedores() {
    }

    public Fornecedores(String nome, String cnpj, String contato, String status) {
        this.nome = nome;
        this.cnpj = cnpj;
        this.contato = contato;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getContato() {
        return contato;
    }

    public void setContato(String contato) {
        this.contato = contato;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
}
