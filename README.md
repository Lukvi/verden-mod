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
