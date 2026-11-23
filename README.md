# Casa Digital — "Isso Não É Um Rádio"

> **POOALAB 2026.2** · Programação Orientada a Objetos Avançada · Prof. Mario Jorge Pereira · UCSal

**Resumo.** A Casa Digital é um site responsivo que dá uma casa própria ao podcast universitário *Isso Não É Um Rádio*:
reúne os episódios e a matéria completa de cada um (vídeo do YouTube + texto + referências) em seis editorias, com a home
como capa de jornal e uma seção UCSAL conferida com a fonte. A equipe do podcast publica por um painel com login; o público
envia sugestões de pauta sem cadastro. O MVP é uma aplicação Java 21 **sem nenhuma dependência externa**, que roda com um único comando.

## Problema, público e proposta de valor

| | |
|---|---|
| **Público** | Alunos, professores e funcionários da UCSAL e o público externo interessado em cultura e bem-estar (visitantes); e a equipe do podcast, estudantes de Comunicação (editores). |
| **Problema** | O conteúdo do podcast vive nas redes sociais, onde os episódios se perdem no feed; não há um lugar próprio com todos os episódios organizados, com a matéria completa e com informação institucional da UCSAL conferida. |
| **Proposta de valor** | Um só lugar, de acesso livre e sem cadastro, com episódio + matéria por editoria, seção UCSAL conferida e um canal de sugestão de pauta que sempre recebe retorno. |
| **Evidências** | Ver [`docs/problema-validacao.md`](docs/problema-validacao.md). |

## Integrantes e turma

> **[PREENCHER]** — o manual exige de 4 a 6 integrantes, com nome completo e turma (equipes fora desse intervalo precisam de autorização prévia do professor).

| Nome completo | Turma |
|---|---|
| [PREENCHER] | [PREENCHER] |
| [PREENCHER] | [PREENCHER] |
| [PREENCHER] | [PREENCHER] |
| [PREENCHER] | [PREENCHER] |

## Tecnologias e justificativas

| Escolha | Por quê |
|---|---|
| **Java 21 (só o JDK)** | A disciplina é de POO Avançada; usar só a biblioteca padrão deixa **todo** o código visível e explicável, e a instalação é "instale o JDK e rode". Usa *records*, *sealed interfaces*, *pattern matching*, *text blocks*, *threads virtuais*. |
| `com.sun.net.httpserver` | Servidor HTTP do próprio JDK, sem framework: o *Front Controller*, o roteador e os filtros são nossos e fazem parte do conteúdo da disciplina. |
| HTML renderizado no servidor + 1 arquivo JS | Funciona em qualquer celular, não exige build de front-end e o site continua utilizável com JavaScript desligado. |
| Banco em arquivo JSON (`data/banco.json`) | Persistência simples e reproduzível; gravação atômica. Com um mapeador JSON próprio, feito por **reflexão**. |
| E-mail simulado | Não há credenciais reais no repositório. Os e-mails ficam na tela de demonstração e em `data/emails/`. |

O mapa completo "conteúdo da disciplina → onde está no código" está em [`docs/arquitetura-pooa.md`](docs/arquitetura-pooa.md).

## Pré-requisitos

* **JDK 21 ou superior** (testado com o JDK 21). Conferir: `java -version`.
* Um navegador. Nenhuma internet é necessária, **exceto** para as miniaturas e o player dos vídeos do YouTube (veja *Limitações*).
* Não é preciso instalar Maven, Gradle, npm nem baixar bibliotecas.

## Instalação e execução

```bash
git clone <URL-DO-REPOSITORIO> casa-digital
cd casa-digital
java Tarefas.java run
```

Depois abra **http://localhost:8080/** (o endereço também aparece no terminal). Para encerrar: `Ctrl+C`.

Outros comandos (`Tarefas.java` é o nosso script de build, escrito em Java e portável entre Windows, Linux e macOS):

| Comando | O que faz |
|---|---|
| `java Tarefas.java run` | compila e inicia o servidor |
| `java Tarefas.java test` | compila e executa os testes automatizados |
| `java Tarefas.java build` | só compila (para `out/`) |
| `java Tarefas.java limpar` | apaga `out/` |

Outra porta: `java -Dcasa.porta=9090 Tarefas.java run` (qualquer chave de `config/` pode ser dada assim, como `-Dcasa.chave=valor`).
Para recomeçar do zero, apague a pasta `data/`. Mais opções em `config/casa-digital.properties.exemplo`.

## Credenciais fictícias e dados de demonstração

* Painel da equipe: **http://localhost:8080/painel** — e-mail `equipe@exemplo.com`, senha `trocar-senha-123` (**fictícias**, só para demonstração; criadas na primeira execução).
* Na primeira execução são carregados 12 matérias publicadas (7 em "Entretenimento e Cultura", para mostrar a paginação), 1 rascunho e itens de exemplo da seção UCSAL. Tudo marcado como *demonstração*, com títulos e textos fictícios. **Não há dados pessoais nem institucionais reais.**
* Em uso real, defina a senha por `CASA_ADMIN_SENHA` antes de iniciar a primeira vez.

## Como executar os testes

```bash
java Tarefas.java test
```

Saída esperada: lista de testes com `[ok]` e, no fim, `68 passaram, 0 falharam`. Os testes cobrem regras de negócio (RN01–RN14),
processos P2–P6, concorrência (100 sugestões simultâneas em threads virtuais), persistência e o servidor real por HTTP
(login, CSRF, publicação, falhas simuladas). O executor de testes é nosso (usa **reflexão** e a anotação `@Teste`), sem JUnit.

## Roteiro curto para reproduzir o fluxo principal (≈ 2 minutos)

O fluxo principal é **P3 — publicar uma matéria** (entrada → validação → gravação → home atualizada). Roteiro completo em [`demo/roteiro-demonstracao.md`](demo/roteiro-demonstracao.md).

1. Abra `http://localhost:8080/` e veja a home (capa de jornal) com os dados de demonstração.
2. Entre em `/painel` com a conta fictícia. Clique em **Nova matéria**.
3. Preencha o título e clique em **Publicar** *sem* o resto: o sistema aponta os campos que faltam.
4. Complete (editoria, link do YouTube, texto, ao menos uma referência), marque "revisada e aprovada" e "fixar como principal" e publique.
5. Volte ao site: a matéria é a principal da home e aparece na sua editoria. Abra-a: vídeo incorporado, texto e referências.
6. Em **Demonstração** (menu do painel), clique em *Provocar falha* na gravação e tente publicar outra: o painel mostra o erro e a home **não muda**.

## Limitações conhecidas e próximos passos

* **E-mail simulado:** nenhum e-mail sai pela internet; o contrato (`GatewayEmail`) está pronto para um adaptador SMTP real.
* **YouTube:** as miniaturas e o player vêm do YouTube; sem internet, aparecem quadros vazios. Os vídeos de exemplo são públicos, mas não são nossos. Contingência: ver `demo/`.
* **Banco em arquivo JSON:** serve para um único servidor e volumes pequenos; sem backup nem migração de versões do arquivo.
* **Sessões em memória:** reiniciar o servidor desconecta a equipe.
* **Sem HTTPS próprio, sem busca por título, sem recuperação de senha** (fora do escopo; a busca já estava fora do escopo no trabalho original).
* O teste com usuário real e a homologação com a equipe do podcast estão descritos em `docs/problema-validacao.md` como **[PREENCHER]** até serem feitos.
* Próximo passo mais importante: homologar com a equipe do podcast e trocar o e-mail simulado por SMTP.

## Fontes, licenças, componentes de terceiros e uso de IA

Tudo está detalhado em [`docs/declaracao-ia-terceiros.md`](docs/declaracao-ia-terceiros.md) (Anexo E). Em resumo:

* **IA:** o código, os testes e os documentos foram produzidos com apoio do Claude (Anthropic), revisados e testados pela equipe.
* **Terceiros:** não há bibliotecas externas. Há fontes, imagens e CSS **preexistentes** da identidade visual do trabalho anterior, o logotipo da UCSAL e os vídeos do YouTube incorporados.
* **Material preexistente:** a Casa Digital nasceu na disciplina *Processos de Negócios* (problema, regras RN01–RN14, processos P1–P6, pesquisa e identidade visual). **Nesta disciplina foi criada toda a implementação em Java** (código, arquitetura, testes e documentação técnica).

## Estrutura do repositório

```
README.md
Tarefas.java                      script de build/execução/teste (multiplataforma)
src/main/java/br/ucsal/casadigital/
    dominio/   entidades, estados, eventos            validacao/  anotações e regras
    infra/     repositórios, JSON, e-mail, proxies    servico/    casos de uso (P1–P6)
    web/       Front Controller, rotas, filtros, telas   app/      raiz de composição e main
src/main/resources/static/        CSS, JS, imagens e fontes
src/test/java/                    testes automatizados
docs/                             problema-validacao, definicao-mvp, arquitetura-pooa, pitch.pdf, declaracao-ia-terceiros
demo/                             dados, roteiro e material de contingência
config/                           exemplo de configuração
```
