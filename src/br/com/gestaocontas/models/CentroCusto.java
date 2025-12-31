package br.com.gestaocontas.models;

/**
 *
 * @author caio
 */

public class CentroCusto {
    private int id;
    String nome, status;

    public CentroCusto() {
    }

    public CentroCusto(String nome, String status) {
        this.nome = nome;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
}
