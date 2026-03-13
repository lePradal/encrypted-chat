# 🛡️ Secure Hexagonal Chat (BIP39 & PGP)

Um chat experimental desenvolvido com **Spring Boot**, focado em privacidade, identidade autossoberana e arquitetura de alta desacoplagem.

---

## 🚀 Sobre o Projeto

Este projeto implementa um sistema de mensagens onde a identidade do usuário é derivada matematicamente de uma frase mnemônica (**BIP39**). Diferente de sistemas tradicionais, não há senhas: a posse das palavras prova a identidade. As mensagens são cifradas via **PGP** antes de qualquer persistência ou tráfego.

### 🔑 Diferenciais Técnicos
* **Identidade Autossoberana:** Usuários são identificados por um `UserHash` derivado de sua chave pública.
* **Arquitetura Hexagonal:** Lógica de negócio (Core) isolada de detalhes técnicos como bancos de dados e corretores de mensagens.
* **Mensageria com RabbitMQ:** Comunicação assíncrona para garantir a entrega e escalabilidade.
* **Criptografia Assimétrica:** Mensagens cifradas de ponta a ponta (E2EE) utilizando o padrão OpenPGP.

---

## 🏗️ Arquitetura e Componentes

O projeto segue o padrão **Ports and Adapters** (Arquitetura Hexagonal), dividindo as responsabilidades de forma clara:



### 1. Camada de Domínio (Core)
O coração da aplicação, livre de frameworks.
* **`MessageService`:** Gerencia o ciclo de vida das mensagens (enviar, editar, deletar).
* **`IdentityService`:** Gerencia a geração de usuários e o fluxo de autenticação por desafio (Challenge-Response).
* **Ports (Interfaces):** Definições abstratas para `MessageRepository`, `MessagePublisher`, `CryptographyPort` e `BIP39Port`.

### 2. Adaptadores de Entrada (Driving)
* **REST API:** Endpoints para gerenciamento de mensagens, chaves e autenticação inicial.
* **WebSockets (Fase 2):** Comunicação duplex em tempo real via Postman/Browser.

### 3. Adaptadores de Saída (Driven)
* **Bouncy Castle:** Implementação técnica do protocolo PGP.
* **BitcoinJ:** Gerador de sementes e dicionários BIP39.
* **RabbitMQ Adapter:** Publicação de eventos de chat para filas distribuídas.
* **JPA Adapter:** Persistência em banco de dados (PostgreSQL/H2).

---

## 🛠️ Tecnologias e Infraestrutura

* **Java 17** & **Spring Boot 3**
* **Docker:** Para orquestração do RabbitMQ local.
* **RabbitMQ:** Broker de mensageria com protocolo AMQP.
* **Lombok:** Para redução de código boilerplate.
* **Maven:** Gestão de dependências.

---

## 🚦 Como Iniciar

### 1. Ambiente Docker
Suba o servidor do RabbitMQ com o plugin de gerenciamento ativado:
```bash
docker run -d --name rabbit-chat -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

### 2. Configuração da Aplicação
Clone o repositório e compile o projeto:

```bash
mvn clean install
mvn spring-boot:run
```

---
## 📡 Fluxo de Utilização (Fase REST)
1. **Geração:** O usuário chama /auth/generate e recebe 12 palavras. Ele anota a semente.

2. **Identidade:** O sistema gera o UserHash a partir dessas palavras.

3. **Envio:** Ao enviar mensagens para o endpoint /messages, o sistema cifra o conteúdo com a chave pública do destinatário.

4. **Entrega:** O RabbitMQ roteia a mensagem para o serviço responsável pela entrega em tempo real.

---

## ✨ Features

### 🔐 Identidade e Segurança
* **Self-Sovereign Identity (SSI):** Criação de contas sem formulários ou bancos de dados centrais, baseada inteiramente em entropia BIP39.
* **Mnemônicos Customizáveis:** Geração de frases de recuperação de 12 ou 24 palavras (compatível com padrões de carteiras cripto).
* **PGP End-to-End Encryption:** Criptografia assimétrica nativa. O servidor atua apenas como um "retransmitidor cego" de pacotes cifrados.
* **Assinatura Digital de Desafio:** Autenticação sem senha via protocolo *Challenge-Response*, garantindo que apenas o dono da chave privada acesse o perfil.

### 💬 Mensageria e Real-Time
* **Real-Time Push:** Integração via WebSockets para recebimento instantâneo de mensagens.
* **Protocolo AMQP (RabbitMQ):** Roteamento inteligente de mensagens entre instâncias da aplicação, garantindo alta disponibilidade.
* **Persistência Híbrida:** Armazenamento de histórico de mensagens cifradas para consulta offline.
* **Broadcast de Status:** Notificações de edição e deleção de mensagens propagadas via broker para todos os clientes conectados.

### 🏗️ Arquitetura e Desenvolvimento
* **Hexagonal Ready:** Domínio 100% isolado de frameworks, facilitando a troca de tecnologias (ex: trocar SQL por MongoDB ou RabbitMQ por Kafka).
* **Extensibilidade de Protocolos:** Suporte nativo para evolução de REST para WebSockets sem alteração na lógica de negócio.
* **Criptografia Agnóstica:** Interface de criptografia que permite alternar entre RSA, EdDSA ou outros algoritmos sem quebrar o sistema.

---

## 🔒 Segurança e Avisos
* **Privacidade:** O servidor nunca armazena a Seed ou a Private Key do usuário.

* **Persistência:** O banco de dados armazena apenas UserHash, PublicKey e mensagens já cifradas.

* **Uso:** Este é um projeto para fins de estudo de arquitetura e criptografia.