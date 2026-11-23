# Roteiro do pitch (Anexo D) — 5 minutos + 3 de perguntas

**Solução:** Casa Digital do "Isso Não É Um Rádio"
O arquivo final para a banca é **`docs/pitch.pdf`** (poucos slides, só o que apoia a fala). **A demonstração ao vivo vem primeiro**; os slides não substituem o sistema.

| Parte | Tempo | Quem fala | Conteúdo | Anotações da equipe |
|---|---|---|---|---|
| **Abertura** | 0:00–0:40 | [PREENCHER] | "Casa Digital: uma casa própria para o podcast *Isso Não É Um Rádio*. Hoje os episódios se perdem no feed das redes sociais e quem quer achar um tema, ou saber de uma liga ou evento da UCSAL, não tem um lugar só." | Frase curta, sem ler slide. |
| **Público e contexto** | (junto) | [PREENCHER] | Dois públicos: **visitantes** (alunos, professores e funcionários da UCSAL) e a **equipe do podcast** (estudantes de Comunicação). | |
| **Evidências** | 0:40–1:15 | [PREENCHER] | Pesquisa com **19 pessoas** da comunidade: **42%** acharam difícil encontrar informações sobre ligas, projetos e eventos; **84%** provavelmente acessariam um site com esse conteúdo. Regras de publicação **confirmadas com a equipe do podcast**. Limite honesto: amostra pequena, mede intenção. | Dizer que esses dados vêm da disciplina anterior. **[PREENCHER]** o que o teste com usuário mudou. |
| **Proposta** | 1:15–1:50 | [PREENCHER] | Um só lugar: matéria completa por episódio (vídeo + texto + referências), seis editorias, home em capa de jornal e seção UCSAL conferida com a fonte. Recorte do MVP: **a equipe publica e o visitante lê**. | |
| **Demonstração** | 1:50–3:30 | [PREENCHER] | Roteiro de `demo/roteiro-demonstracao.md`: (1) home; (2) login; (3) publicar com campos faltando → erros; (4) publicar completo e fixar; (5) home e editoria atualizadas; (6) *Demonstração → Provocar falha* e tentar publicar: erro claro, home intacta. | Dados preparados; não criar cadastros na hora. |
| **Decisões e limites** | 3:30–4:20 | [PREENCHER] | Java 21 puro para mostrar POO avançada (padrões GRASP/GoF, reflexão, threads virtuais); 68 testes automatizados; e-mail simulado; JSON em arquivo; vídeo depende do YouTube. | Falar das limitações **antes** de perguntarem. |
| **Aprendizado e próximo passo** | 4:20–5:00 | [PREENCHER] | Resultado: fluxo principal completo e testado. Próximo passo: homologar com a equipe do podcast e trocar o e-mail simulado por SMTP. | Fechar no tempo. |

**Perguntas prováveis (preparar resposta coletiva):** por que Java sem framework? · como a falha de e-mail é tratada sem perder a sugestão? (RN09) · o que impede duas pessoas de editar a mesma matéria ao mesmo tempo? (trava por id) · como a rota é descoberta? (reflexão) · o que é preexistente e o que é novo? (declaração) · como vocês validaram o problema?

**Divisão da fala e plano de contingência:** quem inicia, quem demonstra e quem conclui: **[PREENCHER]**. Contingência (só se o sistema falhar): `demo/contingencia/` (vídeo curto e roteiro visual); o MVP continua sendo o item principal.
