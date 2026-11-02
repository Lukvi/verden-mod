# Diagnóstico técnico — Mod Verden v0.8.1

Fonte: análise do ZIP enviado. O que está marcado **[verificado]** foi conferido em arquivo/jar; **[não medido]** exige rodar o jogo.

## 1. Stack
- Minecraft **1.21.1**, Fabric Loader 0.16.9, Fabric API 0.115.0+1.21.1, mappings **Yarn** 1.21.1+build.3, Java 21, Loom 1.8, `splitEnvironmentSourceSets` (main + client). [verificado]
- CI: GitHub Actions (Gradle 8.10.2). É o único build oficial; o sandbox desta análise não tem Gradle, rede nem as libs de terceiros (gson, DFU, Brigadier, slf4j, Loader).
- ~97 classes Java, 74 modelos/63 texturas de item, 30 receitas, 15 NPCs, 14 tipos de entidade, 8 loot tables de baú, 8 advancements.

## 2. O que existe e é reaproveitável
- Mundo "Verden": `ChunkGenerator` próprio sobre mapa em imagem (1 px = 8 blocos), 332 sítios em `sites.json`, estradas por A*, construções (`world/obras`). Base sólida para as regiões.
- Entidades (licantropos, Kunori, bosses Amálgama/Razor, gêmeos, Condolyus), NPCs com loja, magias (`magic/Spells`), itens, armaduras com bônus de set, HUD de poderes, ferramentas de geração de assets e sons (`tools/`).
- Tudo isso permanece intacto.

## 3. Problemas encontrados
| # | Problema | Evidência | Ação proposta |
|---|----------|-----------|---------------|
| 1 | **Pacote duplicado**: `world/build` e `world/obras` têm 7 arquivos homônimos que divergiram (ex.: enum `Mob` diferente). Só `obras` é importado. | grep de imports | Remover `world/build` (código morto) **após sua confirmação** |
| 2 | Lixo versionado na raiz: `);'`, arquivo `rcmainjava…#Uf022` e 8 `javac.*.args` | `git ls-files` | Apagar e adicionar ao `.gitignore` |
| 3 | Árvore de trabalho ≠ `HEAD` (ex.: `VerdenNpcEntity.java` com ~320 linhas alteradas, provável troca de fim de linha CRLF) | `git diff --stat` | Commitar/normalizar antes de novas etapas |
| 4 | **Sem persistência, sem rede, sem mixins.** Nenhum estado de campanha, nenhum payload cliente↔servidor, nenhuma tela própria. | grep | Resolvido em parte: núcleo + `RpgRuntime` (Etapa 3). Rede e mixins vêm nas etapas seguintes |
| 5 | Diálogo de NPC = mensagem de chat com chave de tradução, sem opções, sem estado | `VerdenNpcEntity.interactMob` | Substituir por `DialogueRunner` + tela própria (M2); lojas preservadas |
| 6 | HUD = retângulos `fill()`; sem texturas/fontes próprias | `VerdenHud` | Reescrever sobre o contexto da campanha (M1) |
| 7 | Sem sistema de animação: modelos são `ModelPart` manuais | `client/model/*` | Decisão pendente (ver plano §5) |
| 8 | **Fidelidade de lore**: muito do conteúdo atual não aparece no livro (ver §4) | auditoria por termo | Confirmar origem com você |
| 9 | "Thu'um" (magia) é termo de outra franquia | `README` | Renomear ou justificar |
| 10 | Desempenho do mapa: a v0.8.1 já otimizou geração e povoamento; **não consegui perfilar** (sem jogo). Li `Population` (sem problemas óbvios) e **não auditei `VerdenChunkGenerator` a fundo** | — | Medir no jogo com `spark` antes de mexer |

## 4. Auditoria de lore: mod × livro (contagem de ocorrências no texto)
Ausentes do livro (0 ocorrências): Legião (das Espadas), Zytra, Haigol, Condolyus, Rinoy, Menetryx, Hiraku, Lucy, Rashad, Thalen, Raoni, Milly, Cromus, Berserker, Revólver "Cubus", Mneumônio, Triquetra, Corneta, Mønter/Copta, Partenon, Amálgama, Lanterna do Fauno, Vesten Hayvmand, Ost Sirener, magias Cronocinese/Metamorfose/Hipnose/Thu'um.
Presentes: Razor (1.300+), Kizaki, Konge, Nagdaar, Veroogh, Jord, Karamarraw, Montainezz, Morke, Koold, Kunori, Casthiel, Merlim, Yann, Luke, Vanessa, licantropos (só no 2º livro), Cartola, Selo.
**Isto não prova invenção** — pode vir de outro material seu. Pelas suas regras, estou apenas sinalizando. Em contrapartida, o livro tem elenco central que o mod ignora: Gabe, Caesar, Hethephéres, Penélope, Diana, Varhen, Franklin, Lumina, Darius, Lysander, Cavaleiro Esmeralda.
**"Núcleo da Marie" não existe no texto**; "núcleo" só aparece como metáfora (L1 cap. 13 e L2 cap. 63).

## 5. Limitações do ambiente de verificação
Compilação completa só no CI. Aqui: (a) núcleo em Java puro compilado e testado; (b) `RpgRuntime` conferido contra os jars reais do Minecraft/Fabric (com stub de slf4j); (c) o projeto inteiro mantém o mesmo conjunto de erros da linha de base (todos por bibliotecas ausentes), sem erro novo. **Nada foi executado dentro do Minecraft.**
