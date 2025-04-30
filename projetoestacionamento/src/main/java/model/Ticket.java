package model;

import java.time.LocalDateTime;

public class Ticket {

    private int id;
    private Veiculo veiculo;
    private Vaga vaga;
    private LocalDateTime entrada;
    private LocalDateTime saida;
    private double valor; 

    public Ticket(Veiculo veiculo, Vaga vaga, LocalDateTime entrada) {
    this.veiculo = veiculo;
    this.vaga = vaga;
    this.entrada = entrada;
    }
    
    // Getters e setters
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Veiculo getVeiculo() {
        return veiculo;
    }

    public void setVeiculo(Veiculo veiculo) {
        this.veiculo = veiculo;
    }

    public Vaga getVaga() {
        return vaga;
    }

    public void setVaga(Vaga vaga) {
        this.vaga = vaga;
    }

    public LocalDateTime getEntrada() {
        return entrada;
    }

    public void setEntrada(LocalDateTime entrada) {
        this.entrada = entrada;
    }

    public LocalDateTime getSaida() {
        return saida;
    }

    public void setSaida(LocalDateTime saida) {
        this.saida = saida;
    }

    public double getValorCobrado() {
        return valor;
    }

    public void setValorCobrado(double valor) {
        this.valor = valor;
    }
}
