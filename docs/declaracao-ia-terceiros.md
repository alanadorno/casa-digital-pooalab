# Declaração de uso de IA e de componentes de terceiros (Anexo E)

**Solução:** Casa Digital do "Isso Não É Um Rádio"
**Turma:** [PREENCHER]    **Equipe:** [PREENCHER]
**Integrantes:** [PREENCHER — de 4 a 6, nome completo]

> Itens marcados **[PREENCHER]** só a equipe pode confirmar. Esta declaração precisa ser lida, corrigida se necessário e assinada por **todos** antes da entrega.

## 1. Uso de inteligência artificial

**Houve uso de IA?**  ( X ) Sim    (   ) Não

| Ferramenta ou modelo | Finalidade ou tarefa | Parte afetada | Revisão e validação realizadas |
|---|---|---|---|
| Claude (Anthropic), via Claude Code / claude.ai | Análise do manual do POOALAB e do trabalho anterior; **projeto da arquitetura e escrita do código Java** (domínio, serviços, camada web, painel, infraestrutura) | `src/main/java/**`, `Tarefas.java`, `src/main/resources/static/css/painel.css`, `js/app.js` | Compilação e execução locais; **67 testes automatizados** (`java Tarefas.java test`); teste manual no navegador do fluxo principal. Revisão de código pela equipe: **[PREENCHER — quem revisou, quando e o que foi alterado]**. |
| Claude (Anthropic) | **Geração dos testes automatizados** | `src/test/java/**` | Os testes foram executados e passam; os casos refletem as regras RN01–RN14 do documento da equipe. |
| Claude (Anthropic) | **Redação da documentação** e do roteiro/PDF do pitch | `README.md`, `docs/**`, `demo/**` | Conferência dos dados com o documento original da disciplina Processos de Negócios. Revisão final pela equipe: **[PREENCHER]**. |
| Claude (Anthropic) | O trabalho **anterior** (site em Node/React, disciplina Processos de Negócios) também foi gerado com apoio de IA, conforme informado pela equipe | **Não foi copiado**: serviu só de referência para regras, telas e identidade visual (CSS e imagens reaproveitados, ver seção 2) | — |

**Observações sobre o uso de IA:** a IA **não** foi usada para produzir evidências de validação. Os números da pesquisa (19 pessoas, 42%, 84%) vêm do documento da equipe da disciplina anterior e as lacunas estão marcadas como **[PREENCHER]** em `docs/problema-validacao.md`. Nenhum dado pessoal real, credencial ou documento institucional restrito foi inserido na ferramenta, até onde a equipe confirma: **[PREENCHER — confirmar]**. A equipe se responsabiliza por explicar qualquer trecho do código (a explicação por padrão está em `docs/arquitetura-pooa.md`).

## 2. Componentes e recursos de terceiros

**Houve uso de componentes de terceiros?**  ( X ) Sim    (   ) Não

| Componente ou recurso | Origem ou link | Versão e licença | Função e alterações realizadas |
|---|---|---|---|
| **JDK 21** (compilador, biblioteca padrão, `com.sun.net.httpserver`) | OpenJDK / distribuição do JDK instalada pelo usuário | 21+ · GPLv2 com Classpath Exception (OpenJDK) | Plataforma de execução. **Nenhuma biblioteca externa (Maven/Gradle/npm) foi usada.** |
| Fontes **Brasika Display** e **Hey Gotcha** | Protótipo no Figma do trabalho anterior; autor/origem: **[PREENCHER]** | **Licença NÃO verificada** (arquivos de fonte "Trial" costumam permitir só uso pessoal/teste) · ver `src/main/resources/static/fonts/LEIA-ME.txt` | Títulos e menu. Sem alteração. Uso restrito à demonstração acadêmica; trocar por fonte livre antes de qualquer publicação. |
| Imagens de textura (papel, fitas, mancha) em `static/img/` | Protótipo no Figma/trabalho anterior; origem original: **[PREENCHER]** | **[PREENCHER licença]** | Identidade visual de "capa de jornal". Sem alterações neste projeto. |
| Logotipo "Isso Não É Um Rádio" (`logo-radio.webp`) | Identidade do podcast (criado pela equipe do podcast) | Uso autorizado para o projeto: **[PREENCHER — confirmar com a equipe do podcast]** | Cabeçalho do site. Sem alteração. |
| Logotipo da UCSAL (`logo-ucsal.webp`) | Universidade Católica do Salvador | Marca registrada da instituição; **[PREENCHER — confirmar autorização]** | Rodapé do site, apenas para identificar a instituição. |
| `static/css/app.css` | Derivado de `src/styles.css` do site em Node/React do trabalho anterior (feito com apoio de IA), **adaptado** a HTML renderizado no servidor | Autoria da própria equipe/IA, sem licença de terceiros | Estilo da área pública. Adaptações neste projeto: **[PREENCHER — resumir o que foi ajustado]**. `painel.css` e `app.js` são novos. |
| **YouTube** — player incorporado (`youtube-nocookie.com`) e miniaturas (`i.ytimg.com`) | https://www.youtube.com | Termos de Serviço do YouTube / API de incorporação | Cada matéria incorpora o vídeo do episódio. Os **vídeos de demonstração** têm IDs públicos de exemplo: autor e permissão **[PREENCHER]**; em uso real o vídeo é do próprio podcast. |

## 3. Materiais ou código preexistentes

**Houve material produzido antes do POOALAB?**  ( X ) Sim    (   ) Não

**Identificação e origem:** o projeto **Casa Digital do "Isso Não É Um Rádio"**, desenvolvido na disciplina **Processos de Negócios** (Prof. Haroldo Peon, 4ª entrega), por Anielly Maria Dias Nunes e Alana Alves Adorno (conforme o documento daquela disciplina; as demais integrantes desta equipe: **[PREENCHER]**). Existiam antes: o problema e o público; a pesquisa com 19 pessoas; as **14 regras de negócio (RN01–RN14)**, os **6 processos (P1–P6) em BPMN** e os requisitos RQ01–RQ09; o protótipo no Figma e a identidade visual; e um site em Node/React gerado com apoio de IA (referência, **não copiado**).

**Contribuição nova realizada pela equipe no POOALAB:** toda a **implementação em Java 21 orientado a objetos**, construída do zero e sem reaproveitar código do site anterior: arquitetura em camadas e raiz de composição; domínio com Builder, Prototype, State e eventos selados; serviços dos processos P1–P6 com tratamento de falhas e concorrência (threads virtuais, `CompletableFuture`, travas por entidade, agendador); Front Controller com cadeia de filtros e rotas por reflexão/anotações; validação por anotações; mapeador JSON próprio; proxies dinâmicos e simulador de falhas; painel da equipe e tela de demonstração; 67 testes automatizados; script de build multiplataforma; e a documentação técnica. A contribuição pode ser explicada pela equipe em `docs/arquitetura-pooa.md`.

> **Atenção (integridade acadêmica):** o trabalho anterior pertence a *outra disciplina*. O manual exige separar o que já existia do que foi criado aqui (feito acima). Recomenda-se **avisar o professor do POOALAB** de que o problema e as regras vêm de um trabalho anterior, para evitar qualquer dúvida de reaproveitamento sem identificação.

## 4. Declaração da equipe

Declaramos que as informações acima são completas e verdadeiras; que revisamos e testamos as contribuições apoiadas por IA; que identificamos as fontes, licenças e partes preexistentes relevantes; que não apresentamos como autoria própria material de terceiros; e que não compartilhamos dados pessoais, credenciais ou informações restritas de forma indevida.

**Responsável pela submissão:** [PREENCHER]

**Ciência dos integrantes**

| Nome completo | Assinatura ou registro de ciência |
|---|---|
| [PREENCHER] | [PREENCHER] |
| [PREENCHER] | [PREENCHER] |
| [PREENCHER] | [PREENCHER] |
| [PREENCHER] | [PREENCHER] |

**Data:** [PREENCHER] / [PREENCHER] / 2026
