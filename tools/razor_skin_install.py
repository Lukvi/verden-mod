"""Instala a skin do Razor no mod: fase 1 = skin base; fase 2 = Vestígio exposto (coração cristalino, costelas, veias de sombra)."""
import os
from PIL import Image
D = os.path.dirname(__file__)
src = Image.open(os.path.join(D, "skin/razor_kitsuji_skin.png")).convert("RGBA")
ent = os.path.join(D, "../src/main/resources/assets/verden/textures/entity")
src.save(os.path.join(ent, "razor_kitsuji.png"))

p2 = src.copy()
def px(x, y, c): p2.putpixel((x, y), c)
RIB, RIBL = (14, 10, 16, 255), (60, 40, 70, 255)
GOLD, GOLDL, PUR, PURD = (255, 208, 80, 255), (255, 240, 170, 255), (150, 60, 200, 255), (84, 24, 120, 255)
VEIN = (96, 24, 140, 255)
# jaqueta aberta (camada externa frente 20,36 8x12): limpa as colunas 2..5 das linhas 1..9 e mostra peito na base (20,20)
for r in range(1, 10):
    for c in range(2, 6):
        px(20 + c, 36 + r, (0, 0, 0, 0))
# peito base (20,20): caixa torácica
for r in range(1, 10):
    for c in range(2, 6):
        px(20 + c, 20 + r, (22, 14, 26, 255))
for r in (2, 4, 6, 8):
    for c in range(2, 6):
        px(20 + c, 20 + r, RIBL if c in (2, 5) else RIB)
for r in range(1, 10):
    px(20 + 3, 20 + r, RIBL if r % 2 else RIB)   # esterno
# coração cristalino 2x3 dourado/púrpura
for (c, r, col) in [(3, 4, GOLD), (4, 4, GOLDL), (3, 5, PUR), (4, 5, GOLD), (3, 6, PURD), (4, 6, PUR), (3, 3, PURD), (4, 3, PURD)]:
    px(20 + c, 20 + r, col)
# veias de sombra no rosto (base frente 8,8) e olhos mais brilhantes
for (c, r) in [(1, 5), (1, 6), (6, 5), (6, 6), (3, 6), (4, 7)]:
    px(8 + c, 8 + r, VEIN)
# garra de sombras: braço esquerdo (36,52 frente) inteiro roxo-sombra com veias
for r in range(0, 12):
    for c in range(4):
        base = (18, 10, 24, 255) if (c + r) % 3 else VEIN
        px(36 + c, 52 + r, base); px(52 + c, 52 + r, (0, 0, 0, 0) if r < 8 else (12, 8, 16, 255))
for r in range(8, 12):
    for c in range(4):
        px(36 + c, 52 + r, (14, 8, 18, 255) if c % 2 else (60, 20, 90, 255))
p2.save(os.path.join(ent, "razor_kitsuji_2.png"))
print("instalado")
