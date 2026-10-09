# Inventário de Almoxarifado

Aplicação desktop em Java para controlar o inventário de um almoxarifado. Os itens são carregados a partir de um arquivo **CSV** (exportado de uma planilha) e armazenados em um banco de dados MySQL que fica **fora do projeto**, no ambiente do órgão. A quantidade de cada item também pode ser atualizada pela própria tela (ver [Roadmap](#roadmap)).

Projeto desenvolvido em Java com NetBeans, seguindo a arquitetura **MVC + DAO**.

---

## Sumário

1. [Funcionalidades](#funcionalidades)
2. [Tecnologias](#tecnologias)
3. [Arquitetura](#arquitetura)
4. [Estrutura do projeto](#estrutura-do-projeto)
5. [Requisitos](#requisitos)
6. [Configuração do banco de dados](#configuração-do-banco-de-dados)
7. [Configuração da conexão](#configuração-da-conexão)
8. [Formato do arquivo CSV](#formato-do-arquivo-csv)
9. [Executando em desenvolvimento](#executando-em-desenvolvimento)
10. [Gerando o executável](#gerando-o-executável)
11. [Implantação no órgão](#implantação-no-órgão)
12. [Segurança e dados sigilosos](#segurança-e-dados-sigilosos)
13. [Roadmap](#roadmap)
14. [Problemas comuns](#problemas-comuns)

---

## Funcionalidades

**Disponíveis**

- Importar itens a partir de um arquivo CSV (carga inicial e atualização em lote).
- Reimportar o CSV atualiza a quantidade dos itens já existentes (mesma Categoria + Nome) e insere os novos, sem duplicar.
- Listar itens em tabela, ordenados por categoria e nome.
- Buscar itens por categoria ou nome.

**Em desenvolvimento** (ver [Roadmap](#roadmap))

- Ajustar a quantidade de cada item diretamente na tela, com botões **+** e **−**.

---

## Tecnologias

| Item | Versão / detalhe |
|---|---|
| Linguagem | Java 17 (ajustar à versão instalada no órgão) |
| IDE | Apache NetBeans (somente para desenvolvimento) |
| Build | Maven |
| Interface | Java Swing |
| Banco de dados | MySQL 8 |
| Driver JDBC | `mysql-connector-j` 8.4.0 |
| Empacotamento | `maven-shade-plugin` (JAR único) e/ou `jpackage` (Java embutido) |

A leitura do CSV usa apenas a biblioteca padrão do Java; não há dependência de bibliotecas de planilha.

---

## Arquitetura

O projeto segue **MVC + DAO**:

| Camada | Pacote | Responsabilidade |
|---|---|---|
| Model | `model` | Representa os dados (classe `Item`). |
| View | `view` | Tela em Swing. Só conversa com o controller. |
| Controller | `controller` | Coordena a view, o importador e o DAO. |
| DAO | `dao` | Único ponto de acesso ao banco (SQL). |
| Util | `util` | Conexão com o banco e leitura do CSV. |

Fluxo: `View → Controller → DAO → Banco de dados`. A view nunca acessa o DAO nem o banco diretamente.

---

## Estrutura do projeto

```
InventarioAlmoxarifado/
├── pom.xml
├── .gitignore
├── README.md
├── sql/
│   └── schema.sql                 # apenas a estrutura, sem dados
└── src/main/java/com/
    ├── pkg/
    │   └── Main.java
    ├── model/
    │   └── Item.java
    ├── dao/
    │   └── ItemDAO.java
    ├── controller/
    │   └── ItemController.java
    ├── view/
    │   └── TelaPrincipal.java
    └── util/
        ├── ConnectionFactory.java
        └── ImportadorPlanilha.java
```

> Ajuste o nome do pacote (`com`) caso o projeto use outro.

---

## Requisitos

**Para desenvolver**

- JDK 17 ou superior
- Apache NetBeans com suporte a Maven
- MySQL Server 8 e MySQL Workbench (banco **local de testes**, com dados fictícios)
- Git

**Para executar no órgão**

- Java 17+ (se usar o JAR) **ou** nenhuma instalação (se usar o pacote do `jpackage`, que já traz o Java)
- Acesso de rede ao servidor MySQL do órgão
- Autorização da TI para executar o programa

---

## Configuração do banco de dados

1. Crie o banco e o usuário da aplicação (o servidor do órgão é configurado pela TI).

```sql
CREATE DATABASE IF NOT EXISTS almoxarifado CHARACTER SET utf8mb4;

CREATE USER 'almox_app'@'localhost' IDENTIFIED BY 'SENHA_FORTE';
GRANT SELECT, INSERT, UPDATE, DELETE ON almoxarifado.* TO 'almox_app'@'localhost';
FLUSH PRIVILEGES;
```

Em produção, troque `localhost` pelo endereço do computador que acessa o banco.

2. Execute o `sql/schema.sql` com um usuário administrador:

```sql
USE almoxarifado;

CREATE TABLE IF NOT EXISTS item (
  id            INT AUTO_INCREMENT PRIMARY KEY,
  categoria     VARCHAR(60)  NOT NULL,
  nome          VARCHAR(120) NOT NULL,
  quantidade    INT NOT NULL DEFAULT 0,
  atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_categoria_nome (categoria, nome)
);
```

A chave única `(categoria, nome)` é o que identifica cada item e permite reimportar sem duplicar.

O usuário da aplicação recebe somente as permissões necessárias (sem `CREATE`, `DROP` ou `ALTER`).

---

## Configuração da conexão

A aplicação **não guarda** endereço, usuário nem senha do banco no código. Esses dados são lidos de um arquivo na pasta do usuário do Windows, ou de variáveis de ambiente.

**Opção 1 — arquivo `db.properties`**

Caminho: `C:\Users\<usuário>\.almoxarifado\db.properties`

Para criar pelo Prompt de Comando:

```
mkdir %USERPROFILE%\.almoxarifado
notepad %USERPROFILE%\.almoxarifado\db.properties
```

Conteúdo:

```
db.url=jdbc:mysql://SERVIDOR:3306/almoxarifado
db.user=almox_app
db.password=SENHA_FORTE
```

Cuidados: sem aspas, sem espaços no final das linhas, e confirme que o arquivo não ficou como `db.properties.txt`.

**Opção 2 — variáveis de ambiente** (têm prioridade sobre o arquivo)

```
ALMOX_DB_URL
ALMOX_DB_USER
ALMOX_DB_PASSWORD
```

O arquivo `db.properties` fica **fora do repositório** e fora do JAR. Nunca o versione.

---

## Formato do arquivo CSV

Exporte a planilha pelo Excel: *Arquivo → Salvar como → CSV UTF-8*.

Estrutura esperada:

```
INVENTÁRIO ALMOXARIFADO;;
Categoria;Nome;Qtd.
Cabo;HDMI;0
Cabo;VGA;5
Cabo;Display;
Periférico;Mouse;
Periférico;Monitor;
```

Regras do importador:

- A linha de título é ignorada; a leitura começa depois da linha cujo primeiro campo é `Categoria`.
- Separador `;` ou `,` detectado automaticamente.
- Codificação UTF-8 (com ou sem BOM) ou Windows-1252, detectada automaticamente.
- Linhas sem categoria ou sem nome são ignoradas (inclusive as linhas finais `;;`).
- **Quantidade vazia é importada como 0.**
- Itens existentes (mesma Categoria + Nome) têm a quantidade atualizada; itens que não estão no CSV **não** são apagados do banco.
- Campos com quebra de linha dentro da célula não são suportados.

---

## Executando em desenvolvimento

1. Clone o repositório e abra a pasta no NetBeans (*File → Open Project*).
2. Suba o MySQL local e crie o banco conforme [Configuração do banco de dados](#configuração-do-banco-de-dados).
3. Crie o `db.properties` apontando para o banco local de testes.
4. Execute com **F6** (ou *Run Project*).
5. Use **Importar CSV** com um arquivo **fictício** no formato acima.

> Mantenha o projeto fora de pastas sincronizadas pelo OneDrive (por exemplo, use `C:\dev\`). O OneDrive costuma travar a pasta `target` e causar falhas no *Clean and Build*.

---

## Gerando o executável

### Opção A — JAR único (exige Java instalado)

1. No NetBeans: botão direito no projeto → **Clean and Build**.
2. O arquivo gerado fica em `target/<nome-do-projeto>-<versão>.jar`, com todas as dependências dentro.
3. Para executar: `java -jar target/<nome-do-projeto>-<versão>.jar`

Para dar duplo clique, crie um `Almoxarifado.bat` ao lado do JAR:

```bat
@echo off
cd /d "%~dp0"
javaw -jar <nome-do-projeto>-<versão>.jar
```

O `pom.xml` usa o `maven-shade-plugin` com a classe principal `br.ufn.almoxarifado.Main` e o `ServicesResourceTransformer` (necessário para o driver do MySQL).

### Opção B — Aplicativo com Java embutido (`jpackage`)

Para computadores sem Java instalado:

```
jpackage --type app-image --name Almoxarifado --input target ^
  --main-jar <nome-do-projeto>-<versão>.jar --main-class br.ufn.almoxarifado.Main --dest dist
```

Gera `dist/Almoxarifado/` com um `.exe`. Basta copiar a pasta. O `jpackage` gera o pacote para o sistema operacional em que é executado.

O que levar para o órgão: **somente** o JAR com o `.bat`, ou a pasta do `jpackage`. Nenhum dado e nenhuma senha.

---

## Implantação no órgão

1. Solicite à TI autorização para executar o programa.
2. Peça um banco `almoxarifado` no servidor MySQL do órgão, o usuário `almox_app` com as permissões descritas acima, e a execução do `sql/schema.sql`.
3. Copie o JAR e o `.bat` (ou a pasta do `jpackage`) para o computador.
4. Crie o `db.properties` em `C:\Users\<usuário>\.almoxarifado\` com os dados do servidor do órgão e restrinja o acesso ao arquivo ao usuário do sistema.
5. Abra o programa e importe o CSV real **no computador do órgão**.
6. Combine com a TI um backup periódico do banco.

Os dados reais do almoxarifado nunca devem passar pelo computador de desenvolvimento.

---

## Segurança e dados sigilosos

- Nenhum banco, dump, planilha/CSV real ou arquivo de configuração com senha deve ir para o repositório. O `.gitignore` bloqueia esses arquivos:

```
target/
nbproject/private/
dist/
*.xlsx
*.xls
*.csv
*.properties
```

- Credenciais ficam fora do código e fora do JAR.
- Todas as consultas usam `PreparedStatement` (proteção contra SQL injection).
- O usuário do banco usado pela aplicação tem permissões mínimas.
- Dados reais existem somente no ambiente do órgão.
- Antes de cada commit, confira com `git status` que nenhum arquivo sensível foi incluído.

---

## Roadmap

### Ajuste de quantidade com + e −

Hoje a quantidade só muda por importação de CSV. A evolução prevista é permitir o ajuste direto na tela:

- Botões **+** e **−** para o item selecionado (ou em cada linha da tabela), incrementando/decrementando de 1 em 1.
- Campo opcional para ajustar por uma quantidade maior de uma vez.
- Quantidade nunca fica negativa (o mínimo é 0).
- O ajuste deve ser feito pelo banco, de forma atômica, para evitar perda de atualização se dois usuários clicarem ao mesmo tempo:

```sql
UPDATE item
SET quantidade = GREATEST(quantidade + ?, 0)
WHERE id = ?;
```

Alterações previstas por camada:

| Camada | Alteração |
|---|---|
| DAO | Novo método `ajustarQuantidade(int id, int delta)` com o SQL acima. |
| Controller | Métodos `incrementar(id)` e `decrementar(id)` chamando o DAO. |
| View | Botões **+** e **−** na tela e atualização da tabela após o clique. |

Ponto de atenção: depois que as quantidades passarem a ser ajustadas pela tela, **reimportar um CSV sobrescreve** as quantidades do banco com as do arquivo (e quantidade vazia vira 0). Uma opção futura é a importação cadastrar apenas itens novos, sem alterar a quantidade dos existentes.

### Outras ideias

- Tabela de categorias separada (chave estrangeira) para evitar grafias diferentes da mesma categoria.
- Histórico de movimentações (quem ajustou, quando e quanto).
- Alerta de estoque mínimo.
- Cadastro e exclusão de itens pela tela.
- Exportação do inventário atual para CSV.

---

## Problemas comuns

| Sintoma | Causa provável | Solução |
|---|---|---|
| `Configuração do banco não encontrada` | `db.properties` ausente, na pasta errada ou com nome `db.properties.txt` | Criar em `C:\Users\<usuário>\.almoxarifado\` e conferir com `dir` |
| `No suitable driver found` | Driver fora do JAR | Executar o JAR gerado pelo *shade* e conferir o `ServicesResourceTransformer` |
| `Communications link failure` | Servidor desligado, endereço/porta errados ou firewall | Verificar o serviço MySQL e a porta 3306 |
| `Access denied for user` | Usuário/senha errados ou sem permissão para esse computador | Revisar o `db.properties` e o `GRANT` |
| `Unknown database` | Banco não criado | Executar o `CREATE DATABASE` e o `schema.sql` |
| `UnsupportedClassVersionError` | Java do órgão mais antigo que o da compilação | Ajustar `maven.compiler.release` e gerar de novo |
| `Failed to delete ...\target\test-classes` | OneDrive ou programa em execução travando a pasta | Fechar o programa, apagar `target` ou mover o projeto para fora do OneDrive |
| `Cabeçalho ... não encontrado` | CSV sem a linha com `Categoria` na primeira coluna | Conferir o [formato do CSV](#formato-do-arquivo-csv) |
| Acentos quebrados | Codificação | Salvar o CSV como UTF-8 e usar `utf8mb4` no banco |
