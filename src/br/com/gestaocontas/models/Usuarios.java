package br.com.gestaocontas.models;

/**
 *
 * @author caio
 */

public class Usuarios {
    private int id;
    private String nome, usuario, senha, grupo, contato, status, obs;
    
    public Usuarios(){
    }
    
    public Usuarios(String nome, String usuario, String senha, String grupo, String contato, String status, String obs){
        this.nome = nome;
        this.usuario = usuario;
        this.senha = senha;
        this.grupo = grupo;
        this.contato = contato;
        this.status = status;
        this.obs = obs;
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

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
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

    public String getObs() {
        return obs;
    }

    public void setObs(String obs) {
        this.obs = obs;
    }
    
}
