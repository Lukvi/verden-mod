"""Texturas dos viajantes de Verden (moradores andantes, guardas, magos...). Gera textures/entity/walker/<id>.png"""
import os, random, json
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

# ------------------------------------------------------------ novas profissões de loja (NpcRole)
from PIL import ImageChops
SHOP_NPCS = {
    "ferreiro":   ((200, 150, 115), (40, 30, 25), (80, 70, 65), (160, 70, 30), (40, 28, 20), "beard"),
    "alquimista": ((225, 190, 160), (150, 150, 160), (60, 110, 90), (200, 190, 80), (80, 150, 110), "beard"),
    "armeiro":    ((215, 170, 135), (90, 55, 35), (105, 110, 125), (170, 50, 40), (50, 35, 25), "short"),
    "merceiro":   ((230, 195, 165), (120, 80, 50), (170, 130, 70), (235, 225, 190), (60, 40, 25), "short"),
}
tint = {"ferreiro": (255, 170, 120), "alquimista": (150, 255, 190), "armeiro": (170, 190, 255), "merceiro": (255, 225, 140)}
base_icon = Image.open(os.path.join(ASSETS, "textures/item/npc_rashad.png")).convert("RGBA")
npc_dir = os.path.join(ASSETS, "textures/entity/npc")
for n, (skin, hair, cloth, trim, eyes, style) in SHOP_NPCS.items():
    npc_skin(skin, hair, cloth, trim, eyes, style).save(os.path.join(npc_dir, n + ".png"))
    t = Image.new("RGBA", base_icon.size, tint[n] + (255,))
    ic = ImageChops.multiply(base_icon, t)
    ic.putalpha(base_icon.getchannel("A"))
    ic.save(os.path.join(ASSETS, "textures/item/npc_" + n + ".png"))
    import json as _j
    _j.dump({"parent": "minecraft:item/generated", "textures": {"layer0": "verden:item/npc_" + n}},
            open(os.path.join(ASSETS, "models/item/npc_" + n + ".json"), "w"), indent=2)
print(len(SHOP_NPCS), "profissões de loja")

# ------------------------------------------------------------ RAZOR KITSUJI (boss)
def razor_skin(phase):
    skin, blond, suit, tie, eyes = (236, 218, 205), (226, 196, 112), (24, 24, 30), (170, 20, 38), (225, 25, 45)
    im = npc_skin(skin, blond, suit, tie, eyes, "short")
    d = ImageDraw.Draw(im)
    # camisa branca no peito (frente do tronco: 20..28 x 20..32) e gravata vermelha
    d.rectangle([22, 20, 25, 28], fill=(235, 235, 240, 255))
    d.rectangle([23, 21, 24, 27], fill=(*tie, 255))
    # lapelas do terno
    d.line([(20, 20), (23, 26)], fill=(10, 10, 14, 255)); d.line([(27, 20), (24, 26)], fill=(10, 10, 14, 255))
    # cartola (camada externa da cabeça, UV 32,0)
    d.rectangle([40, 0, 47, 7], fill=(12, 12, 16, 255))         # topo
    d.rectangle([32, 8, 63, 11], fill=(12, 12, 16, 255))        # laterais da copa
    d.rectangle([32, 11, 63, 11], fill=(*tie, 255))             # faixa vermelha
    d.rectangle([32, 12, 63, 12], fill=(12, 12, 16, 255))       # aba
    if phase == 2:
        # costelas negras e coração cristalino dourado/púrpura exposto
        d.rectangle([20, 20, 27, 31], fill=(30, 12, 20, 255))
        for y in (21, 24, 27, 30):
            d.line([(20, y), (27, y)], fill=(8, 6, 8, 255))
        d.line([(23, 20), (23, 31)], fill=(8, 6, 8, 255)); d.line([(24, 20), (24, 31)], fill=(8, 6, 8, 255))
        d.rectangle([22, 24, 25, 27], fill=(250, 205, 70, 255))
        d.rectangle([23, 25, 24, 26], fill=(190, 80, 230, 255))
        d.point((22, 24), fill=(120, 40, 150, 255)); d.point((25, 27), fill=(120, 40, 150, 255))
        # veias de sombra no rosto e nos braços
        for (x0, y0, x1, y1) in [(9, 9, 10, 13), (13, 8, 14, 12), (44, 20, 45, 30), (36, 52, 37, 62)]:
            d.line([(x0, y0), (x1, y1)], fill=(70, 20, 90, 255))
        # braço direito (garra de sombras)
        d.rectangle([44, 20, 47, 31], fill=(18, 10, 28, 255))
    return im

ent = os.path.join(ASSETS, "textures/entity")
razor_skin(1).save(os.path.join(ent, "razor_kitsuji.png"))
razor_skin(2).save(os.path.join(ent, "razor_kitsuji_2.png"))

def icon(draw_fn, name):
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw_fn(ImageDraw.Draw(im))
    im.save(os.path.join(ASSETS, "textures/item", name + ".png"))
    json.dump({"parent": "minecraft:item/generated", "textures": {"layer0": "verden:item/" + name}},
              open(os.path.join(ASSETS, "models/item", name + ".json"), "w"), indent=2)

def coracao(d):
    gold, pur, dark = (250, 205, 70, 255), (190, 80, 230, 255), (70, 20, 90, 255)
    for (x, y) in [(4, 3), (5, 3), (6, 3), (9, 3), (10, 3), (11, 3)]: d.point((x, y), fill=gold)
    d.rectangle([3, 4, 12, 7], fill=gold)
    d.rectangle([4, 8, 11, 9], fill=gold); d.rectangle([5, 10, 10, 10], fill=gold); d.rectangle([6, 11, 9, 11], fill=gold)
    d.rectangle([7, 12, 8, 12], fill=gold)
    d.rectangle([5, 5, 7, 7], fill=pur); d.point((6, 6), fill=(255, 220, 255, 255))
    d.line([(3, 4), (3, 7)], fill=dark); d.line([(12, 4), (12, 7)], fill=dark); d.line([(4, 8), (7, 12)], fill=dark)

def morke(d):
    d.rectangle([3, 2, 12, 14], fill=(22, 14, 30, 255)); d.rectangle([3, 2, 4, 14], fill=(50, 25, 70, 255))
    d.rectangle([3, 2, 12, 2], fill=(60, 30, 80, 255)); d.rectangle([3, 14, 12, 14], fill=(10, 6, 14, 255))
    d.ellipse([6, 5, 10, 9], outline=(190, 30, 60, 255)); d.point((8, 7), fill=(255, 60, 90, 255))
    d.line([(8, 10), (8, 12)], fill=(190, 30, 60, 255))

def selo(d):
    d.ellipse([1, 1, 14, 14], fill=(30, 16, 44, 255), outline=(150, 60, 200, 255))
    d.ellipse([4, 4, 11, 11], outline=(210, 40, 70, 255))
    d.line([(8, 2), (8, 13)], fill=(210, 40, 70, 255)); d.line([(2, 8), (13, 8)], fill=(210, 40, 70, 255))
    d.point((8, 8), fill=(255, 230, 120, 255))

def cartola(d):
    d.rectangle([4, 3, 11, 10], fill=(18, 18, 24, 255)); d.rectangle([4, 9, 11, 9], fill=(170, 20, 38, 255))
    d.rectangle([1, 11, 14, 12], fill=(12, 12, 16, 255)); d.rectangle([4, 3, 5, 10], fill=(40, 40, 50, 255))

icon(coracao, "coracao_vestigio"); icon(morke, "grimorio_morke"); icon(selo, "selo_limbo"); icon(cartola, "cartola_kitsuji")

# camadas de armadura da cartola (64x32): camada 1 = capacete
for layer in (1, 2):
    im = Image.new("RGBA", (64, 32), (0, 0, 0, 0)); d = ImageDraw.Draw(im)
    if layer == 1:
        d.rectangle([8, 0, 15, 7], fill=(12, 12, 16, 255))        # topo
        d.rectangle([0, 8, 31, 11], fill=(12, 12, 16, 255))       # laterais
        d.rectangle([0, 11, 31, 11], fill=(170, 20, 38, 255))     # faixa
        d.rectangle([0, 12, 31, 12], fill=(8, 8, 10, 255))
        d.rectangle([16, 0, 23, 7], fill=(12, 12, 16, 255))
    im.save(os.path.join(ASSETS, "textures/models/armor", f"kitsuji_layer_{layer}.png"))
print("assets do Razor gerados")
