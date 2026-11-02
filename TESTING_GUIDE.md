# TESTING_GUIDE

## A. Testes automáticos (sem Minecraft) — funcionam agora
Requer Java 21. Na raiz do projeto: `tools/run_core_tests.sh`
Resultado esperado: última linha `27 passaram, 0 falharam`. Qualquer FAIL lista o motivo.
O que cobrem: partida completa dos caps. 1–3 (missões, escolhas, reputação), troca Trinity↔Marie com skin/ficha/habilidades, roupas, salvamento com arquivo corrompido/truncado/versão nova.

## B. Dentro do Minecraft — única parte visível hoje: o salvamento
(1) Gere o mod (GitHub Actions ou `./gradlew build`) e coloque o JAR de `build/libs` em `mods/` (Fabric 1.21.1 + Fabric API).
(2) Abra um mundo de Verden e feche-o normalmente.
(3) Na pasta do mundo deve existir `verden_rpg/campaign.json`. **Isso indica que o sistema de campanha iniciou e salvou.** Em `logs/latest.log` procure `verden/rpg`: não deve haver linhas de erro; avisos "Conteúdo do RPG: …" indicam referência quebrada nos JSON.
(4) Reabra o mundo: se existir save anterior, o log mostra "Campanha carregada".
Limitações: **ainda não há missões, NPCs, HUD ou comandos da campanha dentro do jogo** — isso é o marco M1. Se algo falhar na compilação do CI, envie o log do erro.
