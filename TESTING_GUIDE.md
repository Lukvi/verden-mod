# TESTING_GUIDE — Fase 1

## Instalar
Minecraft Java 1.21.1 + Fabric Loader (>= 0.16.9) + **Fabric API 0.115.0+1.21.1**. Nenhum outro mod é necessário nesta versão.
Gere o JAR pelo GitHub Actions (ou `gradle build` com Gradle 8.10.2 e Java 21) e coloque `build/libs/*.jar` em `mods/`.

## Jogar
1. Abra o jogo: o menu é o de Verden (Novo Jogo / Continuar Jogo / Configurações / Sair).
2. **Novo Jogo**: o mundo de Verden é criado sozinho. Ao entrar (~3 s) o prólogo começa (tela preta, câmera, música).
3. Karamarraw: desça à praia, siga o caminho para o norte, observe a atmosfera. Ao ouvir os tiros, chegue perto do ponto marcado e **agache atrás de pedras/árvores** quando Narber passar. Volte ao navio.
4. Cenas automáticas: destino de Narber → relatório → título VERDEN AF → Yettus. **Esc/Enter/Espaço** pulam as cenas puláveis.
5. Yettus: cavalgue para o leste, fale com o homem ferido, use a poção nele (botão direito), conversem, siga até Flerin, zigurate, audiência. Termina ao sair da sala do trono.
6. `V` abre o menu (missões, personagens, codex). Salva sozinho a cada 5 min, ao fechar o mundo e a cada cena.

## Comandos (depuração)
`/verden rpg status` (estágio atual), `/verden rpg skip` (pula cena), `/verden rpg restart`, `/verden rpg save`,
`/verden rpg tp praia|mata|navio|yettus|portao|zigurate|salao` (requer cheats).

## Se algo falhar
Envie `logs/latest.log` (linhas com `verden`) e diga em que estágio parou (`/verden rpg status`).
