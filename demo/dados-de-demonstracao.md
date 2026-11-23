# Dados de demonstração

Gerados automaticamente na primeira execução por `servico/DadosDeDemonstracao.java` (só se a pasta `data/` ainda não tiver dados).

| O quê | Quantidade | Observação |
|---|---|---|
| Matérias publicadas | 12 | Títulos e textos **fictícios** ("... de exemplo"); 7 em *Entretenimento e Cultura* para mostrar a paginação de 6 por página; as demais nas outras editorias |
| Rascunho | 1 | Para mostrar o filtro por status no painel |
| Itens da seção UCSAL | 6 | Sobre a instituição, ligas, projetos e "o que está rolando" (este último com datas relativas a hoje: um agendado, um no ar) — todos fictícios |
| Usuário | 1 | `equipe@exemplo.com` / `trocar-senha-123` (**fictícia**; senha guardada com PBKDF2, nunca em texto) |
| Vídeos | 6 IDs públicos do YouTube | Apenas para mostrar o player; autoria **[PREENCHER]**. Sem internet o player não carrega (use a contingência) |

* Tudo é marcado como `demonstracao` e aparece assim no painel.
* **Nenhum dado pessoal real**: nomes das sugestões digitadas na demonstração são inventados (ex.: "Visitante De Teste").
* Reiniciar a demonstração: encerrar o servidor e apagar a pasta `data/`.
* E-mails "enviados" ficam em `data/emails/` e na tela *Demonstração* (nada sai pela internet).
