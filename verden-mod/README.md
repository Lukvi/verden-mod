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
