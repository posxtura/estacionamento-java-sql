CREATE TABLE Vaga (
    id INT AUTO_INCREMENT PRIMARY KEY,
    numero VARCHAR(10),
    tipo VARCHAR(10),
    ocupada BOOLEAN
);

CREATE TABLE Veiculo (
    placa VARCHAR(10) PRIMARY KEY,
    tipo VARCHAR(10),
    cor VARCHAR(20)
);

CREATE TABLE Ticket (
    id INT AUTO_INCREMENT PRIMARY KEY,
    valor DECIMAL(10,2),
    pago BOOLEAN
);

CREATE TABLE EntradaSaida (
    id INT AUTO_INCREMENT PRIMARY KEY,
    placa VARCHAR(10),
    id_vaga INT,entradasaida
    entrada DATETIME,
    saida DATETIME,
    ticket_id INT,entradasaida_ibfk_1
    FOREIGN KEY (placa) REFERENCES Veiculo(placa),
    FOREIGN KEY (id_vaga) REFERENCES Vaga(id),
    FOREIGN KEY (ticket_id) REFERENCES Ticket(id)
)