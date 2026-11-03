# Sistema Farmácia

Aplicação desktop em **Java Swing**, com **JDBC e MySQL**, para autenticação de usuários e gerenciamento de medicamentos. Projeto acadêmico com separação entre interface, regras de negócio e persistência.

## Funcionalidades

- Login com validação no banco e senhas armazenadas como hash PBKDF2 SHA256.
- Cadastro de usuário com confirmação de senha e verificação de login duplicado.
- Orientação para procurar o administrador em caso de esquecimento de senha.
- Tela principal maximizada, menu de medicamentos e logout com confirmação.
- Cadastro, consulta, atualização e exclusão de medicamentos.
- Busca por nome ou fabricante, ordenação e seleção na tabela.
- Validação de campos obrigatórios, preços e quantidades. Letras, negativos e valores inválidos têm mensagens específicas.
- Operações JDBC em segundo plano com SwingWorker.

## Requisitos

- JDK 17 ou superior.
- Apache Maven.
- MySQL 8.0.16 ou superior, preferencialmente 8.4.

O repositório contém o código-fonte. O servidor MySQL, o Java e os executáveis compilados não são distribuídos aqui.

## Configuração e execução

1. Clone ou baixe o repositório e abra sua pasta no terminal.
2. Inicie o MySQL e execute todo o arquivo `database/script.sql` em um cliente SQL. Ele cria `farmacia_db`, as tabelas `usuarios` e `remedios` e a conta inicial do exercício.
3. Copie `config/db.properties.example` para `config/db.properties` e preencha as credenciais **do seu servidor MySQL**:

```properties
db.url=jdbc:mysql://localhost:3306/farmacia_db?connectionTimeZone=UTC&connectTimeout=5000&socketTimeout=15000
db.user=seu_usuario_mysql
db.password=sua_senha_mysql
```

4. Compile e execute, a partir da raiz do projeto:

```shell
mvn clean verify
java -jar target/farmacia.jar
```

O driver MySQL é baixado pelo Maven e incluído no JAR. Não é necessário adicioná-lo manualmente à IDE.

No Windows, `compilar.bat` executa os testes e copia o JAR para `dist/farmacia.jar`. Depois, use `executar.bat`. Para conferir a conexão, use `testar-conexao.bat` ou:

```shell
java -cp target/farmacia.jar br.com.farmacia.TesteConexao
```

**Primeiro acesso da aplicação:** usuário `admin`, senha `123`. Esse acesso é uma carga inicial didática. Não use essa senha em uma implantação real. Ela é diferente das credenciais do servidor MySQL.

Novos usuários precisam de senha entre 8 e 128 caracteres. Todos têm acesso ao CRUD, conforme o escopo acadêmico. Não há envio de e-mails nem perfis de acesso.

## Organização

```text
config/db.properties.example     Exemplo de configuração, sem senha
database/script.sql              Banco, tabelas e acesso inicial
src/main/java/br/com/farmacia/
  App.java                       Inicialização
  TesteConexao.java              Diagnóstico da conexão
  config/                        Configuração e conexões JDBC
  model/                         Modelos Usuario e Remedio
  dao/                           Consultas e gravações SQL
  security/                      Hash e verificação de senha
  service/                       Regras de negócio e validações
  ui/                            Telas Swing e modelo da tabela
src/test/java/br/com/farmacia/    Testes automatizados
docs/requisitos.md               Correspondência com o enunciado
```

Fluxo: **tela → serviço → DAO → MySQL**. Preços usam `BigDecimal` e `DECIMAL(10,2)`. Consultas usam `PreparedStatement`, e recursos JDBC são fechados com `try-with-resources`.

O script SQL pode ser reexecutado sem apagar dados ou redefinir o acesso inicial. Ele não migra tabelas antigas de versões incompatíveis. Faça backup antes de alterar uma base existente.

## Testes

```shell
mvn verify
```

Os testes isolados verificam autenticação, duplicidade, hashes, validações, CRUD JDBC e comportamento da tabela. H2 é usado somente nos testes e não substitui o MySQL da aplicação.

O teste Swing é ignorado em ambientes sem interface gráfica. O teste de integração com MySQL só roda quando as variáveis abaixo estão definidas, apontando para um **servidor exclusivo de testes**:

```text
FARMACIA_TEST_MYSQL_URL=jdbc:mysql://localhost:3306/farmacia_db?createDatabaseIfNotExist=true
FARMACIA_TEST_MYSQL_USER=usuario_de_testes
FARMACIA_TEST_MYSQL_PASSWORD=senha_de_testes
```

Esse teste cria o esquema e grava registros temporários. Não o execute contra uma base em uso. O workflow do GitHub Actions fornece um MySQL temporário para executá-lo automaticamente; a parte Swing é ignorada no runner sem ambiente gráfico.

## Credenciais e configuração

`config/db.properties` é ignorado pelo Git. Não publique esse arquivo, senhas reais, dados do MySQL ou arquivos administrativos. O exemplo de configuração está vazio; as senhas citadas no código de teste são credenciais fictícias de testes.

Também é possível configurar `FARMACIA_DB_URL`, `FARMACIA_DB_USER` e `FARMACIA_DB_PASSWORD`; essas variáveis têm prioridade sobre o arquivo. Para selecionar outro arquivo, use `java -Dfarmacia.config=/caminho/db.properties -jar target/farmacia.jar`.

Para abrir na IDE, importe `pom.xml` como projeto Maven, selecione JDK 17+ e execute `br.com.farmacia.App` com a raiz do projeto como diretório de trabalho.
