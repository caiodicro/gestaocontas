package br.com.gestaocontas.models;

/**
 *
 * @author caio
 */
public class LogCadastros {
    
    private int id;
    private String usuario, dtHora, operacao, descricao;

    public LogCadastros() {
    }

    public LogCadastros(int id, String usuario, String dtHora, String operacao, String descricao) {
        this.id = id;
        this.usuario = usuario;
        this.dtHora = dtHora;
        this.operacao = operacao;
        this.descricao = descricao;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getDtHora() {
        return dtHora;
    }

    public void setDtHora(String dtHora) {
        this.dtHora = dtHora;
    }

    public String getOperacao() {
        return operacao;
    }

    public void setOperacao(String operacao) {
        this.operacao = operacao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

}
