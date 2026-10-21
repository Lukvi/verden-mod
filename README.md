# Verden — mod para Minecraft (Fabric 1.21.1)

Versão 0.2.0 (Fases 1 e 2). Compila pelo GitHub Actions (aba Actions → artifact `verden-mod`). Use o jar SEM `-sources`,
com Minecraft **1.21.1**, Fabric Loader e Fabric API para 1.21.1.

## Fase 1
Grimório com poderes (Chamas, Vento, Telecinese, Gelo, Névoa Carmesim, Cronocinese, Magia Negra), Cromus, Berserker,
Revólver Cubus + Munição .45, Corneta de Lucy (Rinoy), Mneumônio, Amuleto da Triquetra, Coroa Kunori, Machado de Hiraku,
Espada de Liam; Licantropo, Encapuzado Kunori, Tentáculo, Rinoy e o chefe Amálgama de Koold.

## Fase 2 (poder e combate)
- **Novo poder:** Metamorfose Felina (Noite) — velocidade, pulo, visão noturna e queda suave.
- **Licantropo Adaptado:** nova geração, mais forte, imune à fraqueza da água.
- **Soldado da Legião das Espadas:** infantaria do reino (usa ovo de spawn).
- **Zytra Twin e Haigol Twin:** mini-chefes com barra de vida. Juntos ganham Força e Resistência; Zytra avança em
  investidas, Haigol dá pisões que arremessam. Dropam Adaga de Zytra / Espada de Haigol, Insígnias e Poções.
- **Condolyus:** criatura de fogo domesticável (maçã). Segue você e cospe metal derretido nos inimigos. Nasce no Nether.
- **Itens:** Espada da Legião, Adaga de Zytra, Espada de Haigol, Insígnia da Legião, Poção de Recuperação (receita incluída).

## Limitações
Modelos vanilla com textura própria (Condolyus usa modelo de porco pequeno; Tentáculo usa modelo de traça).
Biomas, estruturas, NPCs, moedas e Limbo ficam para as próximas fases.

Texturas/lang/receitas são geradas por `python3 tools/generate_assets.py` (requer Pillow).


## v0.5.0 — Moedas, NPCs, armaduras, HUD e sons
- **Legião corrigida**: soldados, Zytra e Haigol caçam licantropos/Kunori/Amálgama e ficam neutros com o jogador (só reagem se atacados). Licantropos também atacam a Legião.
- **Amálgama de Koold** ~9,5 blocos de altura, pisão de 12 blocos, tema musical próprio.
- **Moedas**: Mønter e Copta Flereniana (1 mønter = 9 coptas, crafting nos dois sentidos).
- **Itens**: Bússola de Verden, Lanterna do Fauno, Mochila de Couro, Ração de Viagem, Poção de Visão Noturna, Elixir de Calor, Lança de Gelo, Katana de Chamas, Munição Incendiária/Perfurante, Garra de Licantropo, armaduras da Legião e de Pele de Rinoy (bônus de set; a pele protege do congelamento).
- **15 NPCs** (ovos no menu criativo): Luke, Lucy, Noite, Milly, Liam, Hiraku, Rose, Vanessa, Rei Konge, Menetryx, Rashad, Yann, Thalen, Taverneiro, Raoni. Clique direito conversa; 11 deles têm loja.
- **Acampamento da Legião** gerado no mundo (tendas, fogueira, baú, guardas, às vezes Menetryx).
- **HUD**: painel de poderes com recarga, munição do revólver e bolsa de moedas.
- **Sons**: rugidos, trompa da Legião, magia, moedas, fala de NPC e tema do chefe (gerados por `tools/sound_gen.py`).
- **Conquistas** (aba Verden). Diálogos estão só em português.
Para regenerar tudo: `python3 tools/generate_assets.py` e `python3 tools/sound_gen.py`.

## v0.6.0 — O mundo "Verden"

Ao criar um mundo, escolha **Tipo de mundo → Verden**. O terreno é gerado a partir do mapa do livro (1 pixel = 8 blocos, ~8000 x 6000 blocos), com os reinos, biomas, rios, lagos, montanhas e desertos nos lugares certos. O spawn fica em Floresta Grøn.

Lugares construídos: Veroogh (Partenon, Rei Konge), Nagdaar (fortaleza negra), Jørd (salão das guildas), Flerin (palácio de cúpulas), Maariz (porto pirata), Mosteiro de Montainezz, Torre de Merlim, Templo de Casthiel, cidades de torres (Vesten Hayvmand, Ost Sirener), vilas de neve/planície/floresta/porto/deserto, ruínas, acampamentos em Karamarraw e covis de licantropos. Os NPCs ficam em suas cidades.

Código: `world/gen` (terreno, mapa, gerador) e `world/build` (construções). O mapa vem de `tools/map_gen.py`.

## v0.7.0 — Mundo vivo

- **Estradas** ligam as cidades da mesma massa de terra (rotas por A* em `tools/roads_gen.py`), com pontes sobre rios e **marcos com placas** (destino e distância) a cada 500 blocos. Ilhas só se alcançam por mar.
- **Viajantes de Verden** (`verden:viajante`): camponeses, guardas, magos, caçadores, mercenários, mercadores, sacerdotes e piratas. Passeiam pelas cidades, formam **caravanas** nas estradas e combatem licantropos e outros monstros do livro (magos lançam chamas, gelo e vento; caçadores atiram; sacerdotes curam).
- **Lojas e tavernas** nas cidades: Taverneiro, Ferreiro, Alquimista, Armeiro e Merceiro (novos NPCs com comércio).
- **12 novas magias do Grimório** no Grimório do jogador: Hidrocinese, Geocinese, Dom da Cura, Magia da Luz, Teletransporte, Barreira Arcana, Invisibilidade, Controlador da Areia, Aura Explosiva, Senhor das Pragas, Thu'um e Hipnose.

## Boss: Razor Kitsuji

Invoque com o **Selo do Limbo** (raro, nos baús do Templo de Casthiel) ou pelo ovo no criativo. Duas fases (HP 600):
- **Fase 1:** *Durma* (aparece atrás e atordoa), *Ji* (esfera gravitacional que atrai) seguido de *Kan* (onda repulsiva), raios negros.
- **Fase 2** (< 50%): garra de sombras, escudo de almas do Limbo (reflete dano), invoca licantropos e o **Colapso do Vazio** (esconda-se atrás de blocos!). Atacar de frente acerta o coração exposto.
- Projéteis o atravessam. **Fraqueza: Magia da Luz** (poder do Grimório). Drenagem de vida ao agarrar.
- Drops: Coração do Vestígio Corrompido, Cartola de Kitsuji (imunidade mental + magia negra mais forte), Grimório de Mørke.

## v0.8.1 — desempenho do mundo Verden
- Medido fora do jogo: o terreno (mapa fixo + ruído de detalhe) custa ~0,3 ms por chunk e as cavernas ~1–2 ms; não era o gargalo.
- Plantas das cidades montadas uma vez, em segundo plano, sem travar threads de geração.
- Preenchimentos das construções só escrevem blocos que realmente mudam (antes reescreviam todo o ar).
- Cache de bioma por coluna, marcos das estradas pré-calculados, cavernas com 2º ruído sob demanda.
- Caminhantes/caravanas não forçam mais a geração de chunks distantes no thread do servidor.
