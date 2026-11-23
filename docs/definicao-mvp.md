# Definição do MVP (Anexo C do manual POOALAB)

**Solução:** Casa Digital do "Isso Não É Um Rádio"

**Usuário principal:** membro da equipe do podcast (editor), que publica o conteúdo; e o visitante da comunidade UCSAL, que o consulta.

**Resultado principal:** o editor consegue **publicar uma matéria de episódio** (vídeo do YouTube + texto + referências) e vê-la no ar na home e na editoria; o visitante encontra e lê essa matéria sem cadastro.

**Fluxo de ponta a ponta (P3, apoiado por P2 e P4):**
`login (P2) → preencher matéria → validação (título, editoria, vídeo, texto, ≥1 referência, revisão confirmada) → gravação → atualização da home e da editoria → confirmação ao editor → visitante consulta (P4)`.
Se a validação falha, o painel mostra o erro **junto de cada campo**. Se a gravação falha, a matéria continua como rascunho e a home não muda.

**Critérios de pronto** (todos verificáveis; os marcados com ✔ têm teste automatizado):

| # | Critério | Regra / requisito |
|---|---|---|
| 1 | ✔ Só membros autenticados (e-mail + senha) acessam o painel; senha errada recebe mensagem genérica; 5 erros em 1 minuto bloqueiam a tentativa. | RN01, RQ04 |
| 2 | ✔ Publicar exige título, editoria (uma das 6), vídeo do YouTube válido, texto e ao menos uma referência. | RN02, RN03, RQ01 |
| 3 | ✔ Nenhuma matéria é publicada sem a confirmação de revisão por um membro da equipe. | RN04, RQ05 |
| 4 | ✔ A home mostra 1 matéria principal + 3 secundárias (a mais recente é a principal). O editor pode fixar uma por 7 dias; depois a home volta ao automático. | RN05, RQ03 |
| 5 | ✔ Cada editoria lista da mais recente para a mais antiga, 6 por página. | RN11, RQ02 |
| 6 | ✔ Visitante consulta sem cadastro; página da matéria traz vídeo incorporado, texto e referências. | RN07, RN06, RQ09 |
| 7 | ✔ Sugestão de pauta exige nome, sobrenome, e-mail válido e texto; é registrada **antes** de o e-mail ser enviado; se o e-mail falha, fica "e-mail pendente" e é reenviado até 3 vezes a cada 15 min. | RN07–RN09, RQ06 |
| 8 | ✔ Estados da sugestão: NOVA → AVALIADA → RESPONDIDA; se a resposta falha, continua AVALIADA. | RN10, RQ07 |
| 9 | ✔ Seção UCSAL: item só publica com a conferência confirmada; guarda quem conferiu e quando; eventos só aparecem entre início e fim e se encerram sozinhos. | RN12–RN14, RQ08 |
| 10 | ✔ Falha na gravação da matéria não altera a home. Falha ao registrar a sugestão não envia e-mail. | RN09, P3 |
| 11 | O site é utilizável no celular e no computador, sem JavaScript obrigatório. | RQ09 |
| 12 | `java Tarefas.java run` sobe o sistema com dados de demonstração em ambiente limpo; `java Tarefas.java test` passa. | Manual: reprodutibilidade |

**Fora do escopo:** busca por título; aplicativo para celular; envio real de e-mail (SMTP); cadastro de público; recuperação de senha; gerenciamento de usuários da equipe pela tela; comentários; estatísticas de acesso; hospedagem na internet.

**Maior risco:** (1) de **uso** — a equipe do podcast conseguir publicar sozinha (ainda sem teste real: **[PREENCHER]**); (2) de **acesso** — o player do YouTube depende de internet durante a mostra; mitigação: execução local, miniaturas/vídeo de contingência em `demo/` e roteiro visual.

**Teste principal:** (a) automatizado: `java Tarefas.java test` executa 68 testes, incluindo o fluxo P3 ponta a ponta por HTTP; (b) manual: o roteiro de `demo/roteiro-demonstracao.md` feito por uma pessoa fora da equipe de desenvolvimento **[PREENCHER: quem, quando, resultado]**.

**Limitações conhecidas:** e-mail simulado; dados em arquivo JSON e sessões em memória (um servidor só); vídeos e miniaturas dependem do YouTube; fontes de licença não verificada (ver declaração); amostra de validação pequena (19 pessoas) e sobre intenção de uso.
