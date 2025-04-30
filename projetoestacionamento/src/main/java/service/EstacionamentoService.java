package service;

import model.Ticket;
import java.time.Duration;
import java.time.LocalDateTime;

public class EstacionamentoService {

    public void calcularValor(Ticket ticket) {
        if (ticket.getSaida() == null) {
            ticket.setSaida(LocalDateTime.now());
        }

        Duration duracao = Duration.between(ticket.getEntrada(), ticket.getSaida());
        long minutos = duracao.toMinutes();

        double valor = 10.0; // Valor inicial: R$10 para até 30 minutos

        if (minutos > 30) {
            // Cada hora extra (arredondado pra cima) custa + R$5
            long horasExtras = (minutos - 30 + 59) / 60; // Arredondamento pra cima
            valor += horasExtras * 5.0;
        }

        ticket.setValorCobrado(valor);
    }
}