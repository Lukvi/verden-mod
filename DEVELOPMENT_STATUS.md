# DEVELOPMENT_STATUS — Verden RPG (Fase 1: Karamarraw → fim do Capítulo Um)

## Revisão 3 (feedback: terror, Narber ausente, tela preta, orientação de Reeves)
- **Narber agora existe desde o início**: embarca com a expedição (nome visível "Narber", skin normal), trabalha na praia, fica para trás encarando a mata,
  afasta-se sozinho e chega à clareira como o Narber possuído (pele pálida, olhos vermelhos); só então o primeiro tiro. Se por qualquer motivo a entidade não existir, é recriada na fase correta (recuperação). Antes ele só nascia no estágio 4 e a flag era marcada mesmo se o spawn falhasse.
- **Terror com acontecimentos**: Narber estranho e ausente do grupo, sussurros, gritos distantes, vultos de almas, passos atrás do jogador, galhos quebrando, batimento cardíaco, náusea/escuridão, clima, e o grupo que se encolhe e congela voltado para o tiro.
- **Tela preta**: (1) chunks são pré-gerados ANTES do fade-in e as cenas esperam 2–4 s após teleportar; (2) falha numa instrução de cutscene é registrada e não trava nem derruba o servidor; (3) o estado enviado ao cliente traz `cine`: se o cliente ficar preso na tela de cinema com o servidor sem cutscene, ele devolve o controle sozinho; (4) fade/letterbox/título são zerados ao terminar; (5) limite de 5 min.
- **Orientação**: NPCs são desenhados por headYaw/bodyYaw; eu só fixava yaw, então todo NPC estático (Reeves, soldado, Nefertari) nascia olhando para o sul. Corrigido em `Fase01.orient`.
- Pendente de verdade: nada disto foi executado no Minecraft (sem Gradle/jogo aqui).

## Revisão 2 (após o primeiro teste do autor)
- **Karamarraw agora é a ilha REAL do mapa do mod** (~4,8 km × 1,2 km; serra de até y≈116 colada à costa norte). Nada de terreno é construído:
  só navio+prancha, suprimentos na praia, marcos, mata ao longo da trilha e pedras (todos só sobre ar/água). A costa norte é achada em tempo de execução (x=3000 → z≈1464).
- **Bloqueio de progressão (causa):** Narber detectava o jogador ANTES da zona "origem dos disparos" e o teleportava de volta (laço infinito), e sua patrulha dependia de pathfinding que podia nunca terminar. Corrigido:
  Narber agora se move por script, detecta só quem está de pé/visível a <22 blocos (agachar = invisível), nunca teleporta o jogador (só o empurra) e **sai sozinho** (≈37 s, limite de 75 s).
- **Tensão com preparação:** sussurros → gritos distantes (novo som `grito`) → vultos de almas → náusea/escuridão → primeiro tiro longínquo ao entrar no estágio 4.
- **Expedição:** 5 NPCs com rotinas por script (carregam/conferem carga, vigiam a praia), marcham pela trilha esperando o jogador, encolhem-se com a tensão e congelam voltados para o som no primeiro tiro.
- **Clareza:** bússola no topo da HUD (direção + distância do objetivo), textos de missão explícitos (inclui "agache com Shift"), dicas automáticas após 60 s parado.
- Tipos checados contra os jars reais (stubs só de libs ausentes); testes do núcleo 31/31; recursos OK. **Continua sem Gradle/Minecraft neste ambiente: nada foi executado no jogo.**


## Estado real (leia primeiro)
- **Escrito e integrado ao runtime do Minecraft** (servidor + cliente + rede): menu inicial próprio, Novo Jogo automático, prólogo de Karamarraw,
  cutscenes (câmera, letterbox, fades, falas, título VERDEN AF, música/sons), Narber (patrulha, detecção, ocultação), cena do relatório,
  Yettus/Binky/Dan, caminho, Flerin, zigurate, audiência, saída. **Checado por tipos contra os jars reais do MC 1.21.1/Fabric API**
  (stubs só de bibliotecas ausentes no sandbox). **NADA foi executado dentro do Minecraft. O Gradle não foi executado** (sem rede/Gradle no sandbox).
- Testes do núcleo (Java puro): 31/31. Auditoria de recursos (`tools/check_assets.py`): OK. Isso NÃO prova que o jogo funciona.

## As 18 etapas (índice da missão `fase_01_karamarraw_flerin`)
| # | Etapa | Gatilho/implementação | Pendência/risco |
|---|---|---|---|
| 1 | Desembarcar | cutscene `c_chegada` + zona praia | navio/praia nunca vistos em jogo |
| 2 | Acompanhar expedição | NPCs `expedicionario` seguem o jogador | modelo vanilla de NPC, sem animação própria |
| 3 | Investigar a mata | zona + atmosfera (partículas, sussurros, náusea, clima, tensão) | intensidade não calibrada |
| 4 | Origem dos disparos | zona; Narber surge e patrulha | — |
| 5 | Esconder-se | agachar perto de pedra/árvore; visto = volta ao início da mata, sem morte | equilíbrio não testado |
| 6 | Voltar ao navio | zona do convés | — |
| 7 | Destino de Narber | cutscene não pulável, tiro distante, sem violência gráfica | câmera nunca vista em jogo |
| 8 | Relatório | sala fechada, Majestade/Reeves/soldado, 8 planos | texto do relatório do livro; local da cena NÃO existe no livro (sala genérica) |
| 9 | Título + Yettus | `c_titulo` (música própria) → `c_yettus_intro` encadeadas | — |
| 10 | Homem ferido | cavalgar Binky (cavalo vanilla c/ nome) até a zona | Binky sem modelo próprio |
| 11 | Poção | item `Poção de Recuperação` usado em Dan (clique direito) | — |
| 12 | Conversa | `dan_yettus_2` (texto literal do livro) | — |
| 13 | Caminho | Dan segue a pé; `dan_caminho` | Dan pode ficar para trás (teleporta se >40) |
| 14–15 | Cidade / zigurate | zonas; `flerin_rua` | **Flerin é a cidade já gerada do mod (sem adobe/mosaicos específicos do livro)**; zigurate é protótipo a leste da cidade |
| 16 | Binky ao estábulo | `zigurate_binky`; cavalo é levado | sem estábulo construído |
| 17–18 | Audiência / proposta | `nefertari_audiencia_1/2` sem escolhas | sala do trono simplificada (sem porta dupla) |
| 19 | Corredores / fim | zona do corredor; `c_fim_fase` + selo | termina aqui; Fase 2 não iniciada |

## Não feito (e por quê)
- GeckoLib / Player Animation Library / Better Combat / CinematicTools / RPG Dialogue **não estão integrados**: sem rede aqui não consigo
  verificar um build com novas dependências, e animações exigem modelos/animações ainda não criados. NPCs usam o modelo vanilla de jogador.
- A skin do jogador não muda com a troca de personagem (exigiria Mixin/biblioteca de animação). Skins existem (provisórias) e são usadas pelos NPCs.
- Tela de saves própria (pedida para depois). "Continuar Jogo" abre a lista de mundos do Minecraft.

## Como retomar
1. `python3 tools/split_livro.py <livro.txt>`; `tools/run_core_tests.sh` (31/31); `python3 tools/check_assets.py`.
2. Compilar de verdade (CI: `.github/workflows/build.yml`, Gradle 8.10.2) e testar no jogo com `TESTING_GUIDE.md`.
3. Próximo: corrigir o que o teste real revelar; só então GeckoLib/animações e a tela de saves.
