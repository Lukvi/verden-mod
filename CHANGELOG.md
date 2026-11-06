# CHANGELOG
## Revisão 3
- Narber presente desde o início (papéis `narber_comum` → `narber`), arco de deterioração e recuperação de spawn; nome visível.
- Terror: passos, galhos, batimento; escalada completa por k.
- Tela preta: preload de chunks, esperas, exceções isoladas, flag `cine` no estado, auto-recuperação do cliente, limpeza de fade, limite de 5 min.
- Orientação de NPCs (headYaw/bodyYaw) corrigida.
## Revisão 2 — feedback do 1º teste
- Karamarraw: usa a ilha real do mapa (sem construir terreno); KarBuilder reescrito (navio, prancha, suprimentos, marcos, mata, pedras); câmeras recalculadas.
- Fase01 reescrita: expedição por script, Narber sem laço de bloqueio e com saída garantida, escalada de tensão, dicas, bússola (campo `goal` no pacote de estado + HUD).
- Novo som `rpg.grito`; textos de missão mais claros.
## Etapa 3 / sessão 2
- Núcleo: `CharacterDef`, `Appearance`; `Session.switchTo`, roupas (`outfit`, `outfit+`, `switch` como efeitos; `outfit:` como condição); estado por personagem (roupa, posição) no save.
- Dados: personagens Trinity/Marie/Dan/Lena (só fatos confirmados; resto PENDENTE).
- Livro: divisor reescrito (numeração impressa, títulos quebrados, duplicatas); `docs/CHAPTER_TO_MISSION_MAP.md`, `docs/fases.json`.
- Docs: `DEVELOPMENT_STATUS.md`, `TESTING_GUIDE.md`; plano atualizado. Testes: 21 → 27.
- Corrigido: `Json.parse` com entrada truncada; regex de efeitos numéricos que aceitava '-' na chave.
## Etapa 3 / sessão 1
- Núcleo RPG, `RpgRuntime`, protótipo de dados dos caps. 1–3, testes, diagnóstico e plano. `VerdenMod` ganhou 1 linha (`RpgRuntime.register()`).
