#!/bin/bash
# Compila o núcleo do RPG (Java puro) e roda os testes. Não precisa de Minecraft, Gradle nem rede.
# Uso (na raiz do repositório): tools/run_core_tests.sh
set -e
cd "$(dirname "$0")/.."
OUT=$(mktemp -d)
if command -v javac >/dev/null 2>&1; then JAVAC=(javac); else JAVAC=(java --module jdk.compiler/com.sun.tools.javac.Main); fi
"${JAVAC[@]}" -Xlint:all -d "$OUT" src/main/java/com/verden/rpg/core/*.java tools/rpgtest/com/verden/rpg/core/CoreTests.java
java -Dstdout.encoding=UTF-8 -cp "$OUT" com.verden.rpg.core.CoreTests
