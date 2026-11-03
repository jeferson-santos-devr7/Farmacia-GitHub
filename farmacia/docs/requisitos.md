# Correspondência com os documentos

Foi usada como referência funcional a versão integrada de `Projeto01Farmacia.docx`, também reproduzida na parte final de `Projeto01FarmaciaCompleto.docx`. O documento mais longo contém etapas anteriores, simulações e classes repetidas; essas etapas não foram duplicadas no projeto final.

| Requisito | Implementação |
| --- | --- |
| Banco farmacia_db | database/script.sql |
| Tabelas usuarios e remedios | database/script.sql |
| Usuário inicial admin / 123 | Carga inicial SQL com hash verificado por teste |
| Conexão JDBC com MySQL | config/ConexaoDB.java |
| Modelos Usuario e Remedio | model/ |
| Autenticação real no banco | UsuarioDAO + UsuarioService |
| Cadastro real de usuários e retorno ao login | TelaCadastro + UsuarioService |
| Esqueci minha senha | Orientação para contatar o administrador |
| Tela principal maximizada com menus | TelaFarmacia |
| Gerenciamento em JDialog modal | TelaCrudRemedios |
| Cadastrar medicamentos | RemedioService.cadastrar + RemedioDAO.inserir |
| Consultar medicamentos e preencher formulário | Listagem JDBC e seleção da JTable |
| Editar medicamentos | RemedioService.atualizar + RemedioDAO.atualizar |
| Excluir com confirmação | TelaCrudRemedios + RemedioDAO.excluir |
| Limpar campos | TelaCrudRemedios.limparCampos |
| Logout confirmado | TelaFarmacia |
| Teste de conexão | TesteConexao + testar-conexao.bat |

Foram corrigidos os espaços ausentes no código e SQL, o armazenamento de senha em texto puro, os valores monetários em double, a edição sem persistência na tabela e as operações de banco na thread da interface.

A organização foi ampliada com pacotes, serviços, testes e Maven. Os modelos são records imutáveis em Java 17, com os mesmos dados funcionais do exercício. O cadastro abre como diálogo modal sobre o login e retorna a ele ao concluir ou cancelar, preservando o fluxo pedido. As opções que apareciam como links foram implementadas com botões para permitir navegação por teclado. A busca por nome ou fabricante complementa a consulta na tabela.

Não fazem parte do escopo final: vendas, caixa, emissão fiscal, lotes, validade, envio de e-mail, perfis de administrador, JavaFX ou os outros projetos citados como sugestões de aula. O roteiro de 20 minutos era material pedagógico; seu objetivo técnico está coberto pela classe de diagnóstico JDBC e pelas instruções de configuração.
