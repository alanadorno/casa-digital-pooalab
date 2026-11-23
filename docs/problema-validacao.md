# Problema e validação (Anexos A e B do manual POOALAB)

> **Aviso de transparência.** As evidências abaixo vêm do trabalho anterior da equipe na disciplina **Processos de Negócios** (Prof. Haroldo Peon),
> feito antes do POOALAB e **apresentado aqui como material preexistente**, não como validação feita nesta disciplina.
> Nada foi inventado: onde a equipe ainda precisa registrar um dado que só ela possui (datas, perfis, teste com usuário), está escrito **[PREENCHER]**.
> Resultados de IA **não** foram usados como evidência de validação com usuários reais.

## Anexo A — Ficha do problema

**Solução:** Casa Digital do "Isso Não É Um Rádio"
**Equipe, turma e integrantes:** [PREENCHER — de 4 a 6 integrantes, nome completo e turma]

| Campo | Preenchimento |
|---|---|
| **Público** | Alunos, professores e funcionários da UCSAL (e público externo interessado em cultura e bem-estar) que querem acompanhar o podcast e saber o que acontece na universidade; e a **equipe do podcast** (estudantes de Comunicação), que produz e publica o conteúdo. |
| **Situação** | Quando alguém quer achar um episódio antigo, entender melhor um tema ou saber de ligas, projetos e eventos da UCSAL, e quando a equipe quer publicar e organizar o que produz. Acontece no dia a dia, no celular, pelas redes sociais. |
| **Problema** | O visitante tenta encontrar o conteúdo do podcast e a informação da UCSAL num só lugar, mas o conteúdo vive nas redes sociais, onde os episódios "se perdem no feed", sem arquivo por editoria e sem lugar próprio para a matéria completa. A equipe não tem um canal próprio para publicar e organizar a produção. |
| **Impacto** | Dificuldade para encontrar informação (na pesquisa, 42% dos respondentes acharam difícil achar informações sobre ligas, projetos e eventos) e conteúdo do podcast subaproveitado. |
| **Alternativa atual** | Instagram e YouTube do podcast, cartazes com QR Code, canais da UCSAL e grupos de WhatsApp. Não há arquivo organizado por editoria nem matéria completa. |
| **Hipótese crítica** | H1: a comunidade teria interesse em um site próprio que reúna episódios, matérias e informações da UCSAL. H2: a equipe do podcast consegue manter o conteúdo e a conferência de informações da UCSAL com um painel simples. H3: as regras de publicação (revisão, destaque, reenvio de e-mail, vigência de eventos) refletem como a equipe realmente trabalha. |
| **Evidência** | H1: pesquisa com 19 pessoas da comunidade UCSAL (ver Anexo B). H3: regras propostas pela equipe do projeto e **confirmadas com a equipe do podcast** (RN04, RN05, RN09, RN10, RN13, RN14). H2 ainda **não** foi testada com a equipe usando o MVP: ver "Teste com usuário" abaixo. |
| **Recorte** | MVP com um fluxo central completo: **a equipe publica uma matéria pelo painel e ela aparece na home e na editoria** (P3), apoiado pela consulta pública (P4). Também entram sugestões de pauta com tratamento de falhas (P5) e a seção UCSAL com vigência (P6). Busca por título, app para celular e envio real de e-mail ficam fora. |

## Anexo B — Registro de validação

**Solução:** Casa Digital do "Isso Não É Um Rádio"

| Data e perfil | Hipótese | Evidência observada | Decisão |
|---|---|---|---|
| [PREENCHER data] — 19 pessoas da comunidade UCSAL (perfis agregados: [PREENCHER distribuição entre alunos, professores e funcionários]). *Origem: pesquisa da disciplina Processos de Negócios, 1ª entrega.* | A comunidade tem dificuldade para encontrar informações sobre ligas, projetos e eventos. | **42%** responderam nota 1 ou 2 (escala de 1 a 5) em "é fácil encontrar informações sobre ligas, projetos e eventos". | Manter a seção UCSAL com informação conferida e eventos com vigência no MVP. |
| [PREENCHER data] — as mesmas 19 pessoas. | A comunidade usaria um site com esse conteúdo. | **84%** disseram que **provavelmente acessariam** um site com esse conteúdo. | Manter o site público, de acesso livre e sem cadastro, como prioridade. |
| [PREENCHER data] — reunião com a equipe do podcast (estudantes de Comunicação; sem identificar pessoas). | As regras de publicação propostas pela equipe do projeto refletem o trabalho real. | Foram **confirmadas** com a equipe do podcast as regras: revisão antes de publicar (RN04), destaque manual de 7 dias (RN05), 3 tentativas de reenvio de e-mail a cada 15 minutos (RN09), estados NOVA/AVALIADA/RESPONDIDA (RN10), registro de quem conferiu a informação (RN13) e encerramento automático de eventos (RN14). | Implementar as regras como confirmadas; essas regras viraram critérios de pronto e testes automatizados (ver `docs/definicao-mvp.md`). |
| [PREENCHER data] — **teste com usuário: uma pessoa da equipe do podcast executando o fluxo P3 no MVP Java.** | A equipe consegue publicar uma matéria sozinha, sem ajuda técnica. | **[PREENCHER: o que foi observado — tempo, dúvidas, erros]** | **[PREENCHER: o que mudou por causa do teste]** |

**Síntese dos aprendizados:** o problema está no "achar e reunir" (conteúdo disperso nas redes), não em produzir mais conteúdo. A procura por um site próprio é alta entre os respondentes (84%), mas a amostra é pequena (19 pessoas, não aleatória) e mede **intenção** de uso, não uso real; isso é uma limitação declarada. A conferência de informações com a fonte e a revisão antes de publicar foram as exigências mais fortes da equipe do podcast, e por isso são regras obrigatórias do sistema e não "boas práticas".

**Mudanças realizadas no problema ou na solução:** [PREENCHER — por exemplo: a busca por título foi retirada do escopo; o envio real de e-mail foi substituído por um e-mail simulado neste MVP por não haver credenciais reais permitidas no repositório. Confirmem o que de fato aconteceu antes de manter este texto.]

## O que é preexistente e o que é do POOALAB

| Preexistente (disciplina Processos de Negócios) | Feito no POOALAB |
|---|---|
| Problema, público, pesquisa de 19 pessoas, hipóteses; 14 regras de negócio, 6 processos (BPMN) e requisitos RQ01–RQ09; identidade visual do podcast (CSS, imagens, fontes); protótipo no Figma. | Reescrita completa da aplicação em Java 21 orientado a objetos (arquitetura em camadas, padrões GRASP/GoF, reflexão, coleções/streams, concorrência), **testes automatizados**, painel de demonstração com simulação de falhas e toda a documentação técnica deste repositório. |
