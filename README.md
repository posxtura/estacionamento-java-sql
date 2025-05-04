# 🚗 Sistema de Gerenciamento de Estacionamento

![Java](https://img.shields.io/badge/Java-17%2B-orange)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)

Sistema completo para controle de vagas e tickets de estacionamento desenvolvido em Java com persistência em MySQL.

## 🎯 Funcionalidades
- Controle de vagas (livres/ocupadas)
- Registro de entrada/saída de veículos
- Cálculo automático de tempo estacionado
- Emissão de tickets
- Relatórios de ocupação

## 🛠 Pré-requisitos
- Java JDK 17+
- MySQL Server 8.0+
- Maven 3.6+
- IDE (Feito no NetBeans)

## ⚙️ Configuração

1. **Banco de Dados**:
   ```sql
   CREATE DATABASE estacionamento;
   USE estacionamento;
   ```

2. **Configure a conexão**:
   Edite `src/main/resources/db.properties`:
   ```properties
   db.url=jdbc:mysql://localhost:3306/estacionamento
   db.user=root
   db.password=estacionamento
   ```

## 📂 Estrutura do Projeto
```
src/
├── main/
│   ├── java/
│   │   ├── app/          # Classe Principal
│   │   ├── dao/          # Data Access Objects
│   │   ├── model/        # Entidades (Vaga, Ticket, Veículo)
│   │   └── service/      # Lógica de negócio
│   └── resources/
│       ├── sql/          # Scripts SQL
│       └── db.properties # Configuração do banco
```
