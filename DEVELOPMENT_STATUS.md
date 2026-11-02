# DEVELOPMENT_STATUS — Verden RPG (atualizado: Etapa 3, sessão 2)

## 1. Visão geral
Mod Fabric 1.21.1 (Yarn, Java 21) "Verden", em transformação num RPG narrativo baseado em *Prelúdio e Agouro* (dois livros num só arquivo). Duas protagonistas jogáveis: Trinity Reeves e Marie Chilgster. Veja `docs/DIAGNOSTICO_TECNICO.md` (estado do mod original) e `docs/PLANO_DE_ADAPTACAO.md` (arquitetura/roadmap). Regra de ouro: o livro é a fonte; nada de cânone inventado; dúvidas viram pendência.

## 2. Arquitetura atual
```
src/main/java/com/verden/rpg/core/   Java PURO (sem Minecraft), testável fora do jogo
  StoryState, CharacterSheet        única fonte de verdade persistente
  Session                           condições/efeitos declarativos + troca de protagonista + roupas
  QuestEngine, Quest                missões em estágios; objetivos por evento ou por condição
  Dialogue, DialogueRunner          diálogos ramificados com variantes reativas a escolhas
  CharacterDef, Appearance          personagens e skins como DADOS
  ContentLoader                     JSON → objetos + validação de referências
  SaveManager                       fsync + rotação de 3 backups + checksum + rename atômico
  Json                              parser/serializador sem dependências
src/main/java/com/verden/rpg/RpgRuntime.java   ÚNICA ponte com o Minecraft (carrega/salva por mundo)
src/main/resources/data/verden/rpg/   conteúdo: quests/, dialogues/, characters/, index.json
tools/run_core_tests.sh             compila o núcleo e roda 27 testes
tools/split_livro.py, gen_chapter_map.py   divisão do livro e mapa capítulo→fase
docs/livro/ (gerado), docs/fases.json, docs/CHAPTER_TO_MISSION_MAP.md
```
Campanha salva em `<pasta do mundo>/verden_rpg/campaign.json` (+ `.bak1..3`). Uma campanha por mundo; sem multiplayer.

## 3. Sistemas
**Implementados e testados (núcleo, fora do jogo):** estado da campanha, missões (9 categorias, estágios, objetivos opcionais, XP/itens/efeitos), diálogos com condições e variantes, escolhas/reputação/afinidade/memórias, codex/regiões (como dados), fichas independentes Trinity/Marie com XP e pontos, personagens+roupas+troca real de protagonista (trava narrativa, troca forçada, roupa por personagem, posição por personagem), salvamento robusto.
**Implementado, conferido só por tipos (não executado no jogo):** `RpgRuntime` (carga/autosave a cada 5 min/ao fechar).
**NÃO existe ainda (planejado):** qualquer coisa visível no jogo — gatilhos do mundo, NPCs da campanha, HUD, tela de diálogo/diário, rede cliente↔servidor, câmera dinâmica (exige Mixin), cutscenes, combate RPG, party, skins/modelos renderizados (as texturas citadas nos JSON **não existem**), mapa, áudio. Conteúdo de campanha: só caps. 1–3 de Prelúdio (2 missões, 4 diálogos).

## 4. Problemas conhecidos
- Build completo só no CI (sandbox sem Gradle/rede). Última checagem local: núcleo + RpgRuntime compilam contra os jars reais do MC 1.21.1; projeto inteiro sem erro novo.
- Pendências do diagnóstico (pacote `world/build` duplicado, lixo na raiz, árvore ≠ HEAD): aguardam sua confirmação.
- Lore: conteúdo do mod não atestado no livro; "Núcleo da Marie" não existe no texto; "Thu'um" é de outra franquia. Ver diagnóstico §4.
- Capítulos com número repetido no original (L1_52/52b, L2_77/77b).
- Marie/Lena/Dan: aparência e habilidades **PENDENTES de leitura** (JSON marcado). Atributos de Trinity e curva de XP são provisórios.
- Estado de missão é global, não por protagonista (o plano pede estado individual); resolver quando houver missões de Marie.

## 5. Última versão compilada / última bateria de testes
- JAR: **nenhum gerado** (sem Gradle aqui). Gere pelo GitHub Actions ou `./gradlew build`.
- Testes: `tools/run_core_tests.sh` → **27 passaram, 0 falharam** (JSON, condições, XP, conteúdo, partida completa dos caps. 1–3, personagens/skins/troca, salvamento sob corrupção).

## 6. Próxima tarefa recomendada
1. Você: confirmar as 5 decisões (plano §5) e o item "pacote duplicado/lixo".
2. **M1 — primeira coisa visível no jogo:** `GameEvent` a partir do mundo (zonas `reach`, interação com bloco/entidade), comando de depuração `/verden rpg` (status, iniciar missão, disparar evento, trocar personagem) e notificações de missão na HUD via payload de rede. Isso permite jogar/testar o arco Yettus→Flerin no Minecraft.
3. Ler Prelúdio caps. 4–9 (Lóros) e gerar resumos (`docs/livro_resumos/`), atualizando `LIDOS` em `tools/gen_chapter_map.py`.
4. Ler Marie (cap. 12) antes de definir qualquer habilidade/aparência dela.

## 7. Como continuar (outra sessão/IA)
1. `python3 tools/split_livro.py <livro.txt>` e `python3 tools/gen_chapter_map.py`.
2. `tools/run_core_tests.sh` deve dar 27/27 antes de mexer.
3. Conteúdo novo = JSON em `data/verden/rpg/` + entrada em `index.json`; o teste "carrega o índice e valida referências" pega erros.
4. Sintaxe de condições/efeitos: cabeçalho de `Session.java`.
5. Nunca contar uma missão como "jogável" sem gatilho no mundo (M1).
