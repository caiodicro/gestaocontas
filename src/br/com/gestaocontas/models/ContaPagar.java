package br.com.gestaocontas.models;

/**
 *
 * @author caio
 */
public class ContaPagar {

    private int fornecedor, categoria, centroCusto;
    private String conta;
    private float valor;
    private int parcelas;
    private String dtVenc, dtPrevisao, status, obs;

    public ContaPagar() {
    }

    public ContaPagar(int fornecedor, int categoria, int centroCusto, String conta, float valor, int parcelas, String dtVenc, String dtPrevisao, String status, String obs) {
        this.fornecedor = fornecedor;
        this.categoria = categoria;
        this.centroCusto = centroCusto;
        this.conta = conta;
        this.valor = valor;
        this.parcelas = parcelas;
        this.dtVenc = dtVenc;
        this.dtPrevisao = dtPrevisao;
        this.status = status;
        this.obs = obs;
    }

    public int getFornecedor() {
        return fornecedor;
    }

    public void setFornecedor(int fornecedor) {
        this.fornecedor = fornecedor;
    }

    public int getCategoria() {
        return categoria;
    }

    public void setCategoria(int categoria) {
        this.categoria = categoria;
    }

    public int getCentroCusto() {
        return centroCusto;
    }

    public void setCentroCusto(int centroCusto) {
        this.centroCusto = centroCusto;
    }

    public String getConta() {
        return conta;
    }

    public void setConta(String conta) {
        this.conta = conta;
    }

    public float getValor() {
        return valor;
    }

    public void setValor(float valor) {
        this.valor = valor;
    }

    public int getParcelas() {
        return parcelas;
    }

    public void setParcelas(int parcelas) {
        this.parcelas = parcelas;
    }

    public String getDtVenc() {
        return dtVenc;
    }

    public void setDtVenc(String dtVenc) {
        this.dtVenc = dtVenc;
    }

    public String getDtPrevisao() {
        return dtPrevisao;
    }

    public void setDtPrevisao(String dtPrevisao) {
        this.dtPrevisao = dtPrevisao;
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
