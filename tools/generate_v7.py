"""Texturas dos viajantes de Verden (moradores andantes, guardas, magos...). Gera textures/entity/walker/<id>.png"""
import os, random
from PIL import Image, ImageDraw

ROOT = os.path.join(os.path.dirname(__file__), "..")
ASSETS = os.path.join(ROOT, "src/main/resources/assets/verden")
rng = random.Random(77)

def sh(c, d):
    return tuple(max(0, min(255, int(x + d))) for x in c[:3]) + (255,)


def A(c):
    return tuple(c[:3]) + (255,)




def fill(im, box, col, var=6):
    x0, y0, x1, y1 = box
    for y in range(y0, y1):
        for x in range(x0, x1):
            im.putpixel((x, y), sh(col, rng.randint(-var, var)))


def npc_skin(skin, hair, cloth, trim, eyes, style):
    im = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    fill(im, (0, 0, 32, 16), skin, 5)                       # cabeça
    for box in [(40, 16, 56, 32), (32, 48, 48, 64)]:        # braços
        fill(im, box, skin, 5)
    fill(im, (16, 16, 40, 32), cloth, 7)                    # tronco
    for x0, y0, x1, y1 in [(0, 16, 16, 32), (16, 48, 32, 64)]:  # pernas
        fill(im, (x0, y0, x1, y1), sh(cloth, -25), 6)
        d.rectangle([x0, y1 - 4, x1 - 1, y1 - 1], fill=sh(trim, -90))   # botas
    # mangas
    for x0, y0 in [(40, 16), (32, 48)]:
        fill(im, (x0, y0 + 4, x0 + 16, y0 + 10), cloth, 6)
        fill(im, (x0 + 4, y0, x0 + 12, y0 + 4), cloth, 6)
    # detalhes de roupa
    d.rectangle([16, 29, 39, 30], fill=A(trim))             # cinto
    d.rectangle([27, 20, 28, 31], fill=A(trim))             # faixa central
    # cabelo
    if hair is not None:
        fill(im, (8, 0, 24, 8), hair, 8)
        fill(im, (24, 8, 32, 16), hair, 8)
        fill(im, (8, 8, 16, 10), hair, 8)
        fill(im, (0, 8, 8, 12), hair, 8); fill(im, (16, 8, 24, 12), hair, 8)
        if style == "long":
            fill(im, (0, 8, 8, 16), hair, 8); fill(im, (16, 8, 24, 16), hair, 8)
            fill(im, (24, 8, 32, 16), hair, 8)
    # barba
    if style == "beard":
        bc = hair if hair is not None else (80, 55, 35)
        fill(im, (8, 13, 16, 16), bc, 6); fill(im, (0, 13, 8, 16), bc, 6); fill(im, (16, 13, 24, 16), bc, 6)
    # rosto
    d.rectangle([9, 11, 10, 11], fill=A(eyes)); d.rectangle([13, 11, 14, 11], fill=A(eyes))
    d.point((9, 10), fill=sh(hair or skin, -10)); d.point((14, 10), fill=sh(hair or skin, -10))
    if style != "beard":
        d.rectangle([11, 14, 12, 14], fill=sh(skin, -60))
    return im



WALKERS = {
    # id: (pele, cabelo, roupa, detalhe, olhos, estilo)
    "campones":   ((225, 185, 150), (95, 60, 35), (150, 120, 80), (110, 80, 50), (60, 40, 25), "short"),
    "camponesa":  ((235, 200, 170), (140, 85, 40), (170, 90, 100), (235, 225, 200), (70, 45, 30), "long"),
    "viajante":   ((215, 170, 135), (40, 30, 25), (70, 95, 75), (150, 120, 70), (50, 35, 25), "short"),
    "guarda":     ((225, 185, 155), (60, 45, 35), (110, 118, 135), (190, 40, 45), (60, 45, 30), "short"),
    "mago":       ((230, 200, 180), (215, 215, 225), (80, 50, 140), (235, 200, 70), (120, 90, 220), "beard"),
    "cacador":    ((205, 160, 125), (70, 45, 30), (60, 105, 60), (110, 80, 45), (50, 35, 20), "short"),
    "mercenario": ((200, 150, 115), (30, 25, 22), (85, 70, 65), (150, 40, 30), (40, 28, 20), "beard"),
    "mercador":   ((220, 180, 145), (120, 80, 45), (150, 55, 55), (235, 200, 80), (60, 40, 25), "beard"),
    "sacerdote":  ((230, 200, 175), (180, 180, 185), (235, 235, 240), (235, 205, 90), (90, 90, 110), "beard"),
    "pirata":     ((190, 140, 105), (25, 20, 20), (45, 45, 55), (200, 60, 50), (35, 25, 20), "beard"),
}
out = os.path.join(ASSETS, "textures/entity/walker")
os.makedirs(out, exist_ok=True)
for n, (skin, hair, cloth, trim, eyes, style) in WALKERS.items():
    npc_skin(skin, hair, cloth, trim, eyes, style).save(os.path.join(out, n + ".png"))
print(len(WALKERS), "texturas de viajantes")
