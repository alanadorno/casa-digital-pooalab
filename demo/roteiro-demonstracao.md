# Roteiro de demonstração (≈ 2 minutos)

**Preparação (antes de começar, 1 vez):**
1. JDK 21 instalado (`java -version`).
2. Na pasta do projeto: `java Tarefas.java run` e aguarde a mensagem com o endereço.
3. Abra `http://localhost:8080/` e, em outra aba, `http://localhost:8080/painel`.
4. Para ver o reenvio de e-mail em segundos (em vez de 15 minutos), inicie com
   `java -Dcasa.reenvio.segundos=20 -Dcasa.agendador.segundos=5 Tarefas.java run`.
5. Para repetir a demonstração do zero: encerre o servidor, apague a pasta `data/` e inicie de novo.

**Credenciais fictícias:** `equipe@exemplo.com` / `trocar-senha-123`.

## Fluxo principal — P3: publicar uma matéria

| # | Ação | Resultado esperado | Regra |
|---|---|---|---|
| 1 | Mostrar a home `/` | Capa de jornal: 1 matéria principal + 3 secundárias, das matérias de demonstração | RN05 |
| 2 | Entrar em `/painel` com a conta fictícia | Lista de matérias (12 publicadas + 1 rascunho) | RN01 |
| 3 | **Nova matéria**, digitar só um título e clicar **Publicar** | Página volta com mensagens **junto de cada campo** (editoria, vídeo, texto, referência, revisão) | RN02, RN04 |
| 4 | Preencher: editoria *UCSAL*; vídeo `https://www.youtube.com/watch?v=476X2Cs6vAM`; texto com dois parágrafos; referência `Reunião de pauta`; marcar **revisada** e **fixar como principal**; **Publicar** | Volta ao painel com aviso verde de publicação | RN02–RN05 |
| 5 | Abrir `/` | A matéria nova é a **principal** da home; abrir `/editoria/ucsal` e a página da matéria: vídeo, texto e referências | RN05, RN11 |

## Tratamento de falhas (mostrar sem improviso)

| # | Ação | Resultado esperado |
|---|---|---|
| 6 | Painel → **Demonstração** → "Falha ao gravar a matéria" → **Provocar falha** | Etiqueta laranja "falhando" |
| 7 | Tentar publicar outra matéria completa | Erro claro "Não foi possível gravar a matéria agora. Ela continua como rascunho"; **a home não muda** (P3) |
| 8 | **Desligar falha**. No site público, enviar uma sugestão (formulário "Contato" no fim da home) | "Sugestão registrada!" |
| 9 | Ligar "Falha ao enviar o e-mail à equipe" e enviar outra sugestão | O visitante **continua vendo a confirmação**; em Painel → Sugestões aparece **"e-mail pendente"** (RN09) |
| 10 | Desligar a falha e clicar **Tentar enviar agora** (ou esperar o reenvio automático) | Aviso verde; o e-mail aparece na "caixa de saída simulada" da tela Demonstração |
| 11 | Sugestões → **Aproveitar na pauta** → **Enviar resposta por e-mail** | Estado NOVA → AVALIADA → RESPONDIDA (RN10) |

**Se algo der errado:** use `demo/contingencia/` (vídeo `demonstracao.mp4` e `roteiro-visual.md`). Contingência não substitui o sistema.

## Para mostrar o conteúdo de POOA (30 s, se perguntarem)
Painel → **Demonstração**: rotas descobertas por reflexão (`@Rota`), eventos do Observer, chamadas medidas pelo Proxy dinâmico. E `java Tarefas.java test`.
