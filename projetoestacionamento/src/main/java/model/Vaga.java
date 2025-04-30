package model;

public class Vaga {
    private int numero;
    private boolean ocupada;
    private int id;

    // Construtor para quando não se sabe o ID ainda (por exemplo, em memória)
    public Vaga(int numero) {
        this.numero = numero;
        this.ocupada = false;
    }

    // Construtor completo (usado ao ler do banco)
    public Vaga(int id, int numero, boolean ocupada) {
        this.id = id;
        this.numero = numero;
        this.ocupada = ocupada;
    }

    // getters e setters
    public int getNumero() {
        return numero;
    }

    public boolean isOcupada() {
        return ocupada;
    }

    public void ocupar() {
        this.ocupada = true;
    }

    public void liberar() {
        this.ocupada = false;
    }

    public int getId() {
        return this.id;
    }
}
