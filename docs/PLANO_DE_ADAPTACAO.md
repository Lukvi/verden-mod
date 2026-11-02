# Plano de Adaptação de *Prelúdio e Agouro* — Verden RPG

## 1. Estado da leitura do livro (honesto)
O arquivo tem ≈**442 mil palavras** (`wc`; o contador do script dá 458 mil por tratar espaços especiais de outro modo): *Prelúdio* (prólogo + 67 caps + epílogo; o título "CINQUENTA E DOIS" aparece duas vezes) e *Agouro* (92 caps + epílogo; "SETENTA E SETE" aparece duas vezes). A numeração dos arquivos em `docs/livro/` segue o número **impresso**. Eu **li integralmente apenas os caps. 1–3 de Prelúdio** (+ prólogo e abertura do epílogo). O resto está indexado (`docs/livro/_indice.json`, um arquivo por capítulo) e mapeado por frequência de personagens/lugares, mas **a bíblia narrativa e a linha do tempo só podem ser escritas lendo os capítulos**, o que será feito em blocos (≈10–15 caps por sessão), cada um gerando `docs/livro_resumos/` no modelo do seu §2.2. Nada além dos caps. 1–3 foi transformado em missão.

| Faixa | Conteúdo (pelo índice) | Estado |
|---|---|---|
| L1 1–3 | Yettus, Dan, Flerin, Nefertari, caderno de Lena, grimório | **Lido e implementado** |
| L1 4–9 | Lóros, Aldo, Ruby | Indexado; **próximo bloco** (ver `docs/CHAPTER_TO_MISSION_MAP.md`) |
| L1 12+ | Entram Marie e Gabe; Caesar (23), Razor (19) | A ler |
| L2 | Diana, Varhen, Franklin, Casthiel, Vestígio, licantropos | A ler |

## 2. Arquitetura (camadas)
1. **Núcleo** `rpg/core` (Java puro, testável): `StoryState` (única fonte de verdade persistente), `Session` (condições/efeitos declarativos), `QuestEngine`, `DialogueRunner`, `SaveManager`, `ContentLoader`+validador. **Pronto, 21 testes.**
2. **Ponte** `RpgRuntime`: ciclo de vida do mundo, carga de conteúdo, autosave. **Pronta, conferida contra jars.**
3. Servidor: gatilhos do mundo→`GameEvent`, NPCs, combate, party, Diretor de Experiência. *Pendente.*
4. Rede (payloads Fabric): notificações, diálogo, estado de HUD. *Pendente.*
5. Cliente: HUD, telas (diário, diálogo, inventário, árvore), câmera, áudio. *Pendente.*
6. Conteúdo como **dados** em `data/verden/rpg/` — escrever missão/diálogo não exige recompilar.

Decisões de design já em código: duas fichas independentes (Trinity/Marie) desde o início; escolhas registradas (`choices`) e memórias com testemunhas para reações tardias; conteúdo inválido falha alto e o validador acusa referências quebradas.

## 3. Câmera dinâmica (viabilidade verificada nos jars)
- Fabric API **não** tem API de câmera. Plano: **Mixin** em `net.minecraft.client.render.Camera` (`update`, `setPos`, `setRotation`, `clipToSpace` existem) + `CameraDirector` com perfis (exploração, combate, boss, diálogo, cutscene) e interpolação. `Perspective` (1ª/3ª) serve para a troca simples sem mixin.
- O projeto **não tem mixins configurados**: exige `verden.mixins.json` e entrada no `fabric.mod.json`. Risco médio; só validável rodando o jogo.

## 4. Roadmap por marcos (cada um só "pronto" se testado)
- **M0 ✔** Núcleo + salvamento + protótipo de dados (cap. 1–3).
- **M1** Gatilhos do mundo (zonas `reach`, blocos/entidades `examine/interact`), rede de notificações, HUD de missão animada, diário.
- **M2** Tela de diálogo com retrato + `CameraDirector` (mixin) para diálogo/audiência.
- **M3** Zona jogável Yettus→Flerin→zigurate (sala secreta das 7 tochas, biblioteca, quadro, grimório, ritual do Desvínculo).
- **M4** Lóros/Aldo/Ruby (investigação com pistas e dedução) após ler L1 4–9.
- **M5** Combate de ação, habilidades, árvore, party/IA, Marie jogável, chefes.
- Depois: cutscenes, codex/bestiário, mapa, acampamentos, New Game+ etc., na ordem de prioridade que você definiu.

## 5. Limites e decisões que dependem de você
1. **Arte/áudio "AAA"**: consigo gerar texturas e sons procedurais (como `tools/` já faz) e UI por código, mas **não** concept art, modelos 3D animados de qualidade comercial nem trilha composta. Eu entregaria os *ganchos* (assets trocáveis) e placeholders.
2. **Animação**: (a) keyframes próprios sobre `ModelPart`, sem dependência; ou (b) adicionar GeckoLib (precisa checar compatibilidade 1.21.1 e acesso à rede no CI). Qual prefere?
3. **Origem do conteúdo não atestado** (diagnóstico §4): manter como "expansão do jogo", reclassificar ou remover?
4. **Núcleo da Marie** não está no livro. Será definido a partir do que o livro disser sobre Marie ao ler L1 12+; até lá, nenhuma mecânica é inventada para ele.
5. Confirmar: **sem multiplayer** nesta fase (campanha por mundo).

## 6. Decisões de fidelidade no protótipo (cap. 1–3)
- **Canônico, não alterável**: Trinity entrega a carta, a assembleia é marcada, o caderno é aberto pelo Desvínculo (palavra "Fierne"), ela **não toca** nas asas de aço de Dan.
- **Interpretativo** (muda tom, afinidade, reputação, falas): como se apresenta a Dan; tom da proposta (direto × diplomático); reação ao desaparecimento de Lena (impulsiva × cautelosa).
- **O homem do parapeito não é nomeado** no livro; o jogo também não o nomeia.
- **Expansão do jogo (não é do livro)**: as escolhas acima, o objetivo opcional do mercado e a escolha de "tratar o ferimento" antes de oferecer ajuda. Marcadas como tal.
- Ponto a verificar: Dan diz que já revistou o quarto de Lena e nada achou; o livro (cap. 2–3) não o descreve. Só construiremos o quarto se capítulos posteriores o detalharem.

## 7. Validação do plano de campanha em 23 fases (Etapa A, parcial)
O plano de fases enviado foi conferido **por âncoras** (personagens/lugares/objetos esperados em cada intervalo de capítulos): 107 de 110 confirmadas; 3 divergências e a numeração repetida estão em `docs/CHAPTER_TO_MISSION_MAP.md`. Isto valida a *estrutura*, não o *conteúdo*: cada fase ainda precisa ser lida capítulo a capítulo antes de virar missão. Dados das fases: `docs/fases.json`.
