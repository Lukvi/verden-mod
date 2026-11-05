# DEVELOPMENT_STATUS — Verden RPG (Fase 1: Karamarraw → fim do Capítulo Um)

## Estado real (leia primeiro)
- **Escrito e integrado ao runtime do Minecraft** (servidor + cliente + rede): menu inicial próprio, Novo Jogo automático, prólogo de Karamarraw,
  cutscenes (câmera, letterbox, fades, falas, título VERDEN AF, música/sons), Narber (patrulha, detecção, ocultação), cena do relatório,
  Yettus/Binky/Dan, caminho, Flerin, zigurate, audiência, saída. **Checado por tipos contra os jars reais do MC 1.21.1/Fabric API**
  (stubs só de bibliotecas ausentes no sandbox). **NADA foi executado dentro do Minecraft. O Gradle não foi executado** (sem rede/Gradle no sandbox).
- Testes do núcleo (Java puro): 31/31. Auditoria de recursos (`tools/check_assets.py`): OK. Isso NÃO prova que o jogo funciona.

## As 18 etapas (índice da missão `fase_01_karamarraw_flerin`)
| # | Etapa | Gatilho/implementação | Pendência/risco |
|---|---|---|---|
| 1 | Desembarcar | cutscene `c_chegada` + zona praia | ilha/navio nunca vistos em jogo; tempo de construção |
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
