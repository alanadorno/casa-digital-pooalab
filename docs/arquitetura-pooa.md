# Conteúdo de POOA aplicado — onde está no código

O manual dá **até 5 pontos de bônus** quando os conteúdos da disciplina são aplicados de forma *pertinente e visível*, sem inserção artificial.
Este mapa aponta, para cada conceito, **o arquivo** e **o problema real que ele resolve** no sistema. Caminhos a partir de `src/main/java/br/ucsal/casadigital/`.

## Visão geral em camadas

```
web  (Front Controller, filtros, rotas, telas)  →  servico (casos de uso P1–P6)  →  dominio (regras e estados)
                                                          ↓
                                                    infra (repositórios, JSON, e-mail, proxies)
app/Sistema.java = única "raiz de composição": o único lugar com `new` que liga as camadas.
```
As camadas só dependem "para dentro" e por **interfaces** (`Publicacao`, `Sugestoes`, `Ucsal`, `Consulta`, `Autenticacao`, `Repositorio`, `GatewayEmail`, `ArmazemDeDestaque`).

## Padrões GRASP

| Padrão | Onde | Por quê |
|---|---|---|
| **Information Expert** | `dominio/Sugestao` (próxima tentativa de e-mail, RN09), `dominio/Materia` (pode publicar?), `dominio/ItemUcsal.situacao(hoje)` (RN14) | Quem tem os dados decide. As regras não ficam espalhadas em serviços. |
| **Creator** | `Materia.builder()`, `PublicacaoServico.gravarNova`, `SugestoesServico.registrar` | Quem usa/agrega o objeto o cria. |
| **Controller** | `web/SiteControlador`, `web/PainelControlador` | Só traduzem HTTP → serviço → tela; nenhuma regra de negócio. |
| **Low Coupling / DIP** | Serviços recebem tudo por construtor e dependem de interfaces; `app/Sistema` é a única raiz de composição | Os testes (`Cenario`) montam o sistema com relógio falso e e-mail falso, sem mudar o código de produção. |
| **High Cohesion** | Um serviço por processo (P1/P3, P2, P4, P5, P6) | Classes pequenas e com um único assunto. |
| **Polymorphism** | `PoliticaDestaque` (automático × fixado), `EstadoSugestao`, `Restricao`, `Filtro` | Troca de `if/switch` por comportamento do tipo. |
| **Pure Fabrication** | `servico/TravasPorId`, `LimitadorDeTentativas`, `infra/Banco`, `Agendador` | Classes que não existem no domínio, mas reduzem acoplamento. |
| **Protected Variations** | `infra/GatewayEmail` (SMTP real × simulado), `infra/Repositorio` (arquivo × memória), `ArmazemDeDestaque` | O ponto instável (e-mail, banco) fica atrás de uma interface. |

**SOLID:** SRP (classes pequenas, uma `Regra` por regra de negócio); OCP (novo `Filtro`, nova `Restricao` ou nova `PoliticaDestaque` sem alterar o resto); LSP (`EmailInstavel`/`EmailSimulado` substituem `GatewayEmail`); ISP (`ArmazemDeDestaque` é só o que o serviço de destaque precisa do banco); DIP (acima).

## Padrões GoF

### Criação
| Padrão | Onde |
|---|---|
| **Builder** | `dominio/Materia.Builder` — monta a matéria sem construtor gigante. |
| **Prototype** | `dominio/EntidadeBase.copiar()` (`Cloneable`, genérico recursivo `SELF`); `infra/RepositorioEmMemoria` guarda e devolve **cópias**, então ninguém "meio publica" um objeto em memória se a gravação falhar (P3). |
| **Factory Method** | `servico/FabricaDePoliticaDeDestaque.para(...)` escolhe a *Strategy* conforme exista destaque manual vigente. |
| **Singleton** | `infra/Configuracao.instancia()` (*holder idiom*, seguro entre threads), usado só no `main`. |

### Estruturais
| Padrão | Onde |
|---|---|
| **Adapter** | `web/Requisicao` (adapta o `HttpExchange` do JDK à nossa interface); `infra/GatewayEmail` (adapta qualquer provedor). |
| **Decorator** | `infra/EmailInstavel` embrulha um `GatewayEmail` e simula falha. |
| **Proxy** | `infra/RepositorioComFalhas` e `infra/MedidorDeChamadas` usam **`java.lang.reflect.Proxy`** (proxy dinâmico): injetam falhas na gravação e medem chamadas dos serviços sem alterar as classes reais. |
| **Composite** | `validacao/RegraComposta` — um conjunto de `Regra` se comporta como uma `Regra`. |
| **Facade** | Cada interface de serviço (`Publicacao`, `Sugestoes`…) esconde validação, repositório, e-mail e eventos atrás de poucos métodos. |

### Comportamentais
| Padrão | Onde |
|---|---|
| **Observer** | `dominio/BarramentoEventos` + eventos `sealed` (`EventoDominio`); ouvintes: `servico/CatalogoPublico` (atualiza home/editorias *depois* de gravar), `servico/LimpezaDoDestaque`, `infra/Atividade`. Um ouvinte que quebra não derruba os outros. |
| **Strategy** | `servico/PoliticaDestaque` → `DestaqueAutomatico` / `DestaqueFixado` (RN05); `validacao/Restricao` (uma por anotação). |
| **State** | `dominio/EstadoSugestao` (enum com comportamento por constante: `aoAvaliar`, `aoResponder`) e `StatusMateria.podeIrPara` (RN10, RN04). |
| **Chain of Responsibility** | `web/Filtro` + `FrontController`: `FiltroDeErros → Log → Segurança → ArquivosEstáticos → Sessão → Acesso → rota`. |
| **Command** | `web/Roteador.RotaRegistrada` encapsula "chamar este método deste controlador". |
| **Template Method** | `web/Visao` → `VisaoPublica` / `VisaoPainel`: a moldura é fixa, as subclasses preenchem `conteudo()`. |
| **Front Controller** | `web/FrontController` — ponto único de entrada de todas as requisições. |

## Coleções, Streams e recursos avançados

* **Collections:** `ConcurrentHashMap` (sessões, travas, tentativas de login), `CopyOnWriteArrayList` (ouvintes de eventos, e-mails enviados), `EnumMap` (itens UCSAL por subseção), `ConcurrentLinkedDeque` (atividade recente), listas imutáveis (`List.copyOf`), `Comparator`.
* **Streams:** filtros e ordenação nos repositórios, `groupingBy` para agrupar itens por subseção, paginação com `skip`/`limit` (`servico/Pagina`), seleção de sugestões a reenviar.
* **Java 21:** `record` (formulários, eventos, `Resultado`, `Email`), `sealed interface` (`EventoDominio`, `Resultado`), **`switch` com *pattern matching* exaustivo, sem `default`** (se alguém criar um evento novo, o compilador aponta onde falta tratar), `text blocks` nas telas, `SequencedCollection` (`getFirst()`, `reversed()`).
* **Generics:** `Repositorio<T extends EntidadeBase<T>>`, `Resultado<T>`, `Regra<T>`, `Pagina<T>`, `EntidadeBase<SELF>`.
* **Exceções:** `TransicaoInvalidaException`, `EmailException`, `RepositorioIndisponivelException` — falhas previstas viram `Resultado.Falha`, não erro 500.

## Reflexão e anotações

| Uso | Onde |
|---|---|
| **Rotas por anotação:** `@Rota(metodo, caminho)` e `@ExigeLogin(administrador)` são descobertas por `getDeclaredMethods()`; o caminho `/materia/{id}` vira uma expressão regular com grupos nomeados | `web/Roteador`, `web/Rota`, `web/ExigeLogin`, `web/FiltroDeAcesso` |
| **Validação declarativa:** `@Obrigatorio`, `@EmailValido`, `@Tamanho`, `@LinkYouTube` nos componentes dos `record`s, lidas por `getRecordComponents()` | `validacao/Validador` e `servico/*Form` |
| **Ligação de formulário:** campos HTTP → `record` por reflexão (construtor canônico, conversão de `long`/`boolean`) | `web/Formularios` |
| **Mapeador JSON próprio:** lê/escreve campos privados, herdados, `enum`, `Instant`, listas e `record`s, mais um *parser* recursivo descendente | `infra/Json` (usado por `infra/Banco`) |
| **Proxy dinâmico** | `infra/RepositorioComFalhas`, `infra/MedidorDeChamadas` |
| **Executor de testes** com `@Teste`, descoberta de classes e métodos e instância nova por teste | `src/test/.../ExecutorDeTestes` |

## Concorrência e Virtual Threads

| Recurso | Onde | Problema que resolve |
|---|---|---|
| **Threads virtuais** (`newVirtualThreadPerTaskExecutor`) | `web/ServidorWeb`, `app/Sistema` | Cada requisição e cada envio de e-mail em uma thread leve; um e-mail lento não trava o visitante. |
| **`CompletableFuture`** | `servico/SugestoesServico` | E-mail à equipe em segundo plano (RN09: o visitante recebe a confirmação sem esperar) e reenvios em paralelo (`reenviarPendentes`). |
| **`ReentrantLock` por entidade** | `servico/TravasPorId` | Reenvio automático e avaliação da equipe não pisam na mesma sugestão; edições simultâneas da mesma matéria não se perdem. |
| **`ScheduledExecutorService`** | `servico/Agendador` | Reenvio de e-mails pendentes (RN09) e encerramento automático de eventos (RN14). |
| **Objetos imutáveis + `volatile`** | `servico/CatalogoPublico`, `infra/Banco` | A home é trocada de uma vez: leitores nunca veem uma lista pela metade. |
| **Coleções concorrentes** | ver acima | Sessões, limitador de login. |

Testes: `ConcorrenciaTest` (100 sugestões simultâneas; 30 reenvios simultâneos geram **1** e-mail; 40 edições da mesma matéria; 200 leitores durante 10 publicações).

## Segurança aplicada (requisito do manual)

Senhas com **PBKDF2** e sal por usuário (`infra/Senhas`); cookie de sessão `HttpOnly` + `SameSite=Strict`; token **CSRF** por sessão em todo POST do painel; verificação de `Origin`; *Content-Security-Policy* e demais cabeçalhos (`web/FiltroDeSeguranca`); limite de 5 tentativas de login por minuto; mensagem genérica de login (não revela se o e-mail existe); todo texto do usuário escapado (`web/Html.esc`); proteção contra *path traversal* nos arquivos estáticos; nenhum segredo no repositório.

## Como o professor pode conferir rapidamente

1. `java Tarefas.java test` — 68 testes por reflexão.
2. Tela **Painel → Demonstração**: mostra as rotas descobertas por reflexão, os eventos do *Observer*, as chamadas medidas pelo *Proxy* e o simulador de falhas.
3. Ler, nesta ordem: `app/Sistema.java` → `web/FrontController.java` → `servico/SugestoesServico.java` → `dominio/Sugestao.java`.
