#!/usr/bin/env python3
"""Gera texturas procedurais, modelos de item, lang, receitas e loot tables do mod Verden.
Uso: python3 tools/generate_assets.py   (rodar na raiz do projeto)"""
import json, os, random
from PIL import Image, ImageDraw

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
RES = os.path.join(ROOT, "src/main/resources")
ASSETS = os.path.join(RES, "assets/verden")
DATA = os.path.join(RES, "data/verden")


def w(path, obj=None, text=None):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        if obj is not None:
            json.dump(obj, f, ensure_ascii=False, indent=2)
        else:
            f.write(text)


def clamp(v):
    return max(0, min(255, int(v)))


def shade(c, d):
    return tuple(clamp(x + d) for x in c[:3]) + ((c[3],) if len(c) == 4 else ())


# ---------------------------------------------------------------- ITEM TEXTURES (16x16)
def new16():
    return Image.new("RGBA", (16, 16), (0, 0, 0, 0))


def line(img, pts, col):
    ImageDraw.Draw(img).line(pts, fill=col, width=1)


def px(img, x, y, col):
    if 0 <= x < 16 and 0 <= y < 16:
        img.putpixel((x, y), col)


def sword(blade, edge, guard, grip):
    im = new16()
    for i in range(3, 13):
        px(im, i + 1, 14 - i - 1 + 0, edge)
        px(im, i, 14 - i, blade)
    px(im, 13, 1, edge); px(im, 14, 0, edge)
    for d in (-1, 0, 1):
        px(im, 3 + d, 10 + d + 1, guard) if False else None
    line(im, [(2, 9), (6, 13)], guard)
    line(im, [(2, 13), (1, 14)], grip)
    px(im, 3, 12, grip); px(im, 2, 13, grip); px(im, 1, 14, shade(grip, -30))
    return im


def tex_grimorio():
    im = new16(); d = ImageDraw.Draw(im)
    d.rectangle([3, 1, 12, 14], fill=(70, 20, 90, 255), outline=(25, 5, 35, 255))
    d.rectangle([4, 2, 11, 13], fill=(100, 35, 125, 255))
    d.rectangle([3, 1, 4, 14], fill=(45, 10, 60, 255))
    cx, cy = 8, 7
    d.polygon([(cx, cy - 4), (cx - 3, cy + 2), (cx + 3, cy + 2)], outline=(255, 210, 90, 255))
    d.ellipse([cx - 1, cy - 1, cx + 1, cy + 1], fill=(255, 90, 30, 255))
    return im


def tex_cromus():
    return sword((60, 40, 90, 255), (170, 120, 255, 255), (30, 20, 40, 255), (20, 15, 25, 255))


def tex_espada_liam():
    im = sword((200, 215, 230, 255), (245, 250, 255, 255), (70, 120, 190, 255), (60, 40, 30, 255))
    return im


def tex_machado():
    im = new16(); d = ImageDraw.Draw(im)
    d.line([(3, 14), (11, 3)], fill=(110, 75, 45, 255), width=1)
    d.line([(4, 14), (12, 3)], fill=(80, 55, 30, 255), width=1)
    d.polygon([(8, 1), (14, 4), (13, 8), (10, 6)], fill=(190, 60, 40, 255), outline=(90, 20, 15, 255))
    d.polygon([(5, 5), (2, 6), (3, 10), (7, 8)], fill=(190, 60, 40, 255), outline=(90, 20, 15, 255))
    return im


def tex_revolver():
    im = new16(); d = ImageDraw.Draw(im)
    d.rectangle([2, 5, 13, 7], fill=(95, 95, 105, 255), outline=(30, 30, 40, 255))
    d.rectangle([5, 4, 8, 8], fill=(125, 125, 135, 255), outline=(30, 30, 40, 255))
    d.rectangle([3, 8, 6, 13], fill=(110, 70, 40, 255), outline=(50, 30, 15, 255))
    d.point((11, 5), fill=(255, 215, 80, 255))
    return im


def tex_municao():
    im = new16(); d = ImageDraw.Draw(im)
    for x in (4, 7, 10):
        d.rectangle([x, 4, x + 1, 12], fill=(205, 160, 60, 255))
        d.rectangle([x, 3, x + 1, 4], fill=(230, 90, 60, 255))
        d.point((x, 5), fill=(250, 220, 130, 255))
    return im


def tex_corneta():
    im = new16(); d = ImageDraw.Draw(im)
    d.polygon([(2, 12), (3, 10), (9, 5), (13, 2), (14, 5), (10, 9), (4, 13)], fill=(225, 220, 200, 255), outline=(110, 100, 80, 255))
    d.rectangle([2, 11, 4, 13], fill=(200, 160, 60, 255))
    d.line([(11, 3), (13, 5)], fill=(150, 140, 120, 255))
    return im


def tex_mneumonio():
    im = new16(); d = ImageDraw.Draw(im)
    d.polygon([(8, 1), (13, 6), (11, 14), (5, 14), (3, 6)], fill=(60, 200, 210, 255), outline=(10, 80, 100, 255))
    d.polygon([(8, 3), (11, 6), (8, 12), (5, 6)], fill=(150, 245, 250, 255))
    d.point((7, 5), fill=(255, 255, 255, 255))
    return im


def tex_triquetra():
    im = new16(); d = ImageDraw.Draw(im)
    gold = (230, 190, 70, 255)
    d.arc([2, 3, 9, 10], 0, 360, fill=gold)
    d.arc([7, 3, 14, 10], 0, 360, fill=gold)
    d.arc([4.5, 6, 11.5, 13], 0, 360, fill=gold)
    d.point((8, 8), fill=(200, 30, 60, 255))
    d.line([(8, 0), (8, 2)], fill=(150, 150, 160, 255))
    return im


def tex_coroa():
    im = new16(); d = ImageDraw.Draw(im)
    d.rectangle([2, 9, 13, 12], fill=(60, 50, 55, 255), outline=(20, 15, 20, 255))
    for x in (2, 5, 8, 11, 13):
        d.polygon([(x, 9), (x + 1, 4), (x + 2, 9)], fill=(170, 40, 50, 255))
    return im


def tex_bow(pull):
    im = new16(); d = ImageDraw.Draw(im)
    wood = (95, 25, 25, 255); edge = (40, 8, 8, 255)
    off = {0: 0, 1: 1, 2: 2}[pull]
    d.arc([1 + off, 1, 13, 15], 270, 90, fill=wood)
    d.arc([2 + off, 1, 14, 15], 270, 90, fill=edge)
    d.line([(7 + off, 1), (7 + off - pull * 2, 8), (7 + off, 14)], fill=(230, 230, 230, 255))
    if pull:
        d.line([(7 - pull * 2, 8), (13, 8)], fill=(170, 150, 120, 255))
    return im


def tex_espada_legiao():
    return sword((170, 175, 185, 255), (225, 230, 240, 255), (150, 30, 40, 255), (70, 45, 30, 255))


def tex_adaga_zytra():
    im = new16(); d = ImageDraw.Draw(im)
    d.line([(4, 11), (11, 4)], fill=(235, 235, 245, 255), width=2)
    d.line([(11, 4), (13, 2)], fill=(255, 255, 255, 255))
    d.line([(3, 8), (7, 12)], fill=(230, 190, 70, 255), width=1)
    d.line([(2, 13), (4, 11)], fill=(90, 60, 40, 255), width=2)
    return im


def tex_espada_haigol():
    return sword((60, 62, 75, 255), (140, 145, 165, 255), (230, 190, 70, 255), (40, 30, 30, 255))


def tex_insignia():
    im = new16(); d = ImageDraw.Draw(im)
    d.ellipse([1, 1, 14, 14], fill=(60, 62, 75, 255), outline=(25, 25, 35, 255))
    d.line([(4, 11), (11, 4)], fill=(225, 230, 240, 255), width=1)
    d.line([(4, 4), (11, 11)], fill=(225, 230, 240, 255), width=1)
    d.point((8, 8), fill=(170, 30, 40, 255))
    return im


def tex_pocao():
    im = new16(); d = ImageDraw.Draw(im)
    d.rectangle([6, 1, 9, 3], fill=(150, 110, 70, 255), outline=(70, 50, 30, 255))
    d.rectangle([7, 4, 8, 5], fill=(210, 230, 235, 255))
    d.ellipse([3, 5, 12, 14], fill=(70, 200, 110, 255), outline=(20, 90, 50, 255))
    d.ellipse([5, 7, 7, 9], fill=(190, 255, 210, 255))
    return im


ITEM_TEX = {
    "espada_legiao": tex_espada_legiao, "adaga_zytra": tex_adaga_zytra, "espada_haigol": tex_espada_haigol,
    "insignia_legiao": tex_insignia, "pocao_recuperacao": tex_pocao,
    "grimorio": tex_grimorio, "cromus": tex_cromus, "espada_curva_liam": tex_espada_liam,
    "machado_duplo_hiraku": tex_machado, "revolver_cubus": tex_revolver, "municao_45": tex_municao,
    "corneta_lucy": tex_corneta, "mneumonio": tex_mneumonio, "amuleto_triquetra": tex_triquetra,
    "coroa_espinhos_kunori": tex_coroa,
}

for name, fn in ITEM_TEX.items():
    p = os.path.join(ASSETS, "textures/item", name + ".png")
    os.makedirs(os.path.dirname(p), exist_ok=True)
    fn().save(p)
for n, pull in [("berserker", 0), ("berserker_pulling_0", 0), ("berserker_pulling_1", 1), ("berserker_pulling_2", 2)]:
    tex_bow(pull).save(os.path.join(ASSETS, "textures/item", n + ".png"))

HANDHELD = {"cromus", "espada_curva_liam", "machado_duplo_hiraku", "espada_legiao", "adaga_zytra", "espada_haigol"}
for name in ITEM_TEX:
    parent = "minecraft:item/handheld" if name in HANDHELD else "minecraft:item/generated"
    w(os.path.join(ASSETS, "models/item", name + ".json"),
      {"parent": parent, "textures": {"layer0": f"verden:item/{name}"}})

w(os.path.join(ASSETS, "models/item/berserker.json"), {
    "parent": "minecraft:item/bow",
    "textures": {"layer0": "verden:item/berserker"},
    "overrides": [
        {"predicate": {"pulling": 1}, "model": "verden:item/berserker_pulling_0"},
        {"predicate": {"pulling": 1, "pull": 0.65}, "model": "verden:item/berserker_pulling_1"},
        {"predicate": {"pulling": 1, "pull": 0.9}, "model": "verden:item/berserker_pulling_2"},
    ]})
for i in range(3):
    w(os.path.join(ASSETS, f"models/item/berserker_pulling_{i}.json"),
      {"parent": f"minecraft:item/bow_pulling_{i}", "textures": {"layer0": f"verden:item/berserker_pulling_{i}"}})

for egg in ["licantropo", "encapuzado_kunori", "tentaculo", "amalgama_koold", "rinoy",
            "licantropo_adaptado", "soldado_legiao", "zytra_twin", "haigol_twin", "condolyus"]:
    w(os.path.join(ASSETS, f"models/item/{egg}_spawn_egg.json"), {"parent": "minecraft:item/template_spawn_egg"})

# ---------------------------------------------------------------- ENTITY TEXTURES
rng = random.Random(1234)


def noise_fill(img, box, base, var):
    x0, y0, x1, y1 = box
    for y in range(y0, y1):
        for x in range(x0, x1):
            d = rng.randint(-var, var)
            img.putpixel((x, y), shade(base + (255,), d))


def humanoid(base_skin, cloth, accent, eyes, size=(64, 64), fur=False, hood=False):
    """Textura 64x64 no layout de skin clássica (compatível com BipedEntityModel)."""
    im = Image.new("RGBA", size, (0, 0, 0, 0))
    # regiões principais do layout do jogador
    regions = {
        "head": (0, 0, 32, 16), "body": (16, 16, 40, 32), "rarm": (40, 16, 56, 32),
        "rleg": (0, 16, 16, 32), "lleg": (16, 48, 32, 64), "larm": (32, 48, 48, 64),
    }
    head_c = cloth if hood else base_skin
    noise_fill(im, regions["head"], head_c, 14 if fur else 8)
    noise_fill(im, regions["body"], cloth, 12 if fur else 8)
    noise_fill(im, regions["rarm"], base_skin if not hood else cloth, 12 if fur else 8)
    noise_fill(im, regions["larm"], base_skin if not hood else cloth, 12 if fur else 8)
    noise_fill(im, regions["rleg"], shade(cloth, -20), 10)
    noise_fill(im, regions["lleg"], shade(cloth, -20), 10)
    d = ImageDraw.Draw(im)
    # rosto (face frontal da cabeça = x 8..16, y 8..16)
    if hood:
        d.rectangle([9, 10, 14, 14], fill=(10, 5, 12, 255))
    d.rectangle([9, 11, 10, 11], fill=eyes)
    d.rectangle([13, 11, 14, 11], fill=eyes)
    if not hood:
        d.rectangle([10, 14, 13, 14], fill=shade(base_skin, -70) + (255,))
        if fur:
            d.rectangle([11, 13, 12, 13], fill=(240, 240, 235, 255))  # presas
    # detalhes de cor de destaque no corpo
    d.rectangle([27, 17, 28, 31], fill=accent + (255,))
    return im


def save_ent(name, im):
    p = os.path.join(ASSETS, "textures/entity", name + ".png")
    os.makedirs(os.path.dirname(p), exist_ok=True)
    im.save(p)


save_ent("licantropo", humanoid((78, 62, 48), (62, 48, 38), (130, 30, 30), (255, 200, 40, 255), fur=True))
save_ent("encapuzado_kunori", humanoid((120, 90, 80), (28, 14, 34), (190, 35, 45), (255, 60, 40, 255), hood=True))


def amalgama():
    im = humanoid((55, 20, 70), (40, 14, 55), (255, 110, 30), (255, 90, 30, 255), fur=True)
    d = ImageDraw.Draw(im)
    # núcleo brilhante no peito (face frontal do corpo = x 20..28, y 20..32)
    d.ellipse([21, 22, 27, 28], fill=(255, 120, 40, 255), outline=(255, 220, 120, 255))
    d.point((24, 25), fill=(255, 255, 220, 255))
    # veias
    for i in range(10):
        x = rng.randint(16, 39); y = rng.randint(16, 31)
        d.line([(x, y), (x + rng.randint(-3, 3), y + rng.randint(2, 5))], fill=(150, 40, 150, 255))
    return im


save_ent("amalgama_koold", amalgama())


def tentaculo():
    im = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    noise_fill(im, (0, 0, 64, 32), (90, 25, 100), 14)
    d = ImageDraw.Draw(im)
    for _ in range(40):
        d.point((rng.randint(0, 63), rng.randint(0, 31)), fill=(190, 80, 200, 255))
    return im


save_ent("tentaculo", tentaculo())


def rinoy():
    im = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    noise_fill(im, (0, 0, 64, 64), (46, 38, 34), 16)
    d = ImageDraw.Draw(im)
    for _ in range(260):  # pelagem clara nas pontas
        d.point((rng.randint(0, 63), rng.randint(0, 63)), fill=(205, 212, 220, 255))
    return im


save_ent("rinoy", rinoy())

save_ent("licantropo_adaptado", humanoid((46, 38, 44), (38, 30, 36), (200, 30, 30), (255, 40, 40, 255), fur=True))
save_ent("soldado_legiao", humanoid((205, 165, 140), (105, 110, 122), (165, 30, 40), (30, 30, 40, 255)))
save_ent("zytra_twin", humanoid((226, 190, 160), (135, 140, 158), (150, 30, 40), (255, 210, 80, 255)))
save_ent("haigol_twin", humanoid((200, 160, 130), (48, 50, 62), (180, 30, 40), (255, 210, 80, 255)))


def condolyus():
    im = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    noise_fill(im, (0, 0, 64, 32), (205, 90, 30), 22)
    d = ImageDraw.Draw(im)
    for _ in range(120):  # brasas
        d.point((rng.randint(0, 63), rng.randint(0, 31)), fill=(255, 215, 70, 255))
    return im


save_ent("condolyus", condolyus())

# ---------------------------------------------------------------- LANG
pt = {
    "itemGroup.verden": "Verden",
    "item.verden.grimorio": "Grimório",
    "item.verden.cromus": "Cromus",
    "item.verden.berserker": "Berserker",
    "item.verden.revolver_cubus": "Revólver Cubus",
    "item.verden.municao_45": "Munição .45",
    "item.verden.corneta_lucy": "Corneta de Lucy",
    "item.verden.mneumonio": "Mneumônio",
    "item.verden.amuleto_triquetra": "Amuleto da Triquetra",
    "item.verden.coroa_espinhos_kunori": "Coroa de Espinhos Kunori",
    "item.verden.machado_duplo_hiraku": "Machado Duplo de Hiraku",
    "item.verden.espada_curva_liam": "Espada Curva de Liam",
    "item.verden.licantropo_spawn_egg": "Ovo de Licantropo",
    "item.verden.encapuzado_kunori_spawn_egg": "Ovo de Encapuzado Kunori",
    "item.verden.tentaculo_spawn_egg": "Ovo de Tentáculo",
    "item.verden.amalgama_koold_spawn_egg": "Ovo de Amálgama de Koold",
    "item.verden.rinoy_spawn_egg": "Ovo de Rinoy",
    "entity.verden.licantropo": "Licantropo",
    "entity.verden.encapuzado_kunori": "Encapuzado Kunori",
    "entity.verden.tentaculo": "Tentáculo de Koold",
    "entity.verden.amalgama_koold": "Amálgama de Koold",
    "entity.verden.rinoy": "Rinoy",
    "power.verden.chamas": "Chamas",
    "power.verden.vento": "Vento",
    "power.verden.telecinese": "Telecinese",
    "power.verden.gelo": "Gelo",
    "power.verden.nevoa": "Névoa Carmesim",
    "power.verden.crono": "Cronocinese da Matéria",
    "power.verden.negra": "Magia Negra",
    "message.verden.power_selected": "Poder: %s",
    "message.verden.need_triquetra": "A Magia Negra exige o Amuleto da Triquetra.",
    "message.verden.too_weak": "Você está fraco demais para a Magia Negra.",
    "message.verden.no_ammo": "Sem munição .45!",
    "item.verden.espada_legiao": "Espada da Legião",
    "item.verden.adaga_zytra": "Adaga de Zytra",
    "item.verden.espada_haigol": "Espada de Haigol",
    "item.verden.insignia_legiao": "Insígnia da Legião",
    "item.verden.pocao_recuperacao": "Poção de Recuperação",
    "item.verden.licantropo_adaptado_spawn_egg": "Ovo de Licantropo Adaptado",
    "item.verden.soldado_legiao_spawn_egg": "Ovo de Soldado da Legião",
    "item.verden.zytra_twin_spawn_egg": "Ovo de Zytra Twin",
    "item.verden.haigol_twin_spawn_egg": "Ovo de Haigol Twin",
    "item.verden.condolyus_spawn_egg": "Ovo de Condolyus",
    "entity.verden.licantropo_adaptado": "Licantropo Adaptado",
    "entity.verden.soldado_legiao": "Soldado da Legião",
    "entity.verden.zytra_twin": "Zytra Twin",
    "entity.verden.haigol_twin": "Haigol Twin",
    "entity.verden.condolyus": "Condolyus",
    "power.verden.metamorfose": "Metamorfose Felina",
    "tooltip.verden.power": "Poder: %s",
    "tooltip.verden.grimorio": "Clique direito: conjurar. Agachado + clique: trocar poder.",
    "tooltip.verden.cromus": "Clique direito: corte de energia à frente.",
    "tooltip.verden.revolver": "Consome Munição .45.",
    "tooltip.verden.corneta": "Chama Rinoy, a montaria gigante de Koold.",
}
en = dict(pt)
en.update({
    "item.verden.grimorio": "Grimoire",
    "item.verden.municao_45": ".45 Ammo",
    "item.verden.corneta_lucy": "Lucy's Horn",
    "item.verden.amuleto_triquetra": "Triquetra Amulet",
    "item.verden.coroa_espinhos_kunori": "Kunori Crown of Thorns",
    "item.verden.machado_duplo_hiraku": "Hiraku's Double Axe",
    "item.verden.espada_curva_liam": "Liam's Curved Sword",
    "item.verden.revolver_cubus": "Cubus Revolver",
    "item.verden.licantropo_spawn_egg": "Lycanthrope Spawn Egg",
    "item.verden.encapuzado_kunori_spawn_egg": "Hooded Kunori Spawn Egg",
    "item.verden.tentaculo_spawn_egg": "Tentacle Spawn Egg",
    "item.verden.amalgama_koold_spawn_egg": "Koold Amalgam Spawn Egg",
    "item.verden.rinoy_spawn_egg": "Rinoy Spawn Egg",
    "entity.verden.licantropo": "Lycanthrope",
    "entity.verden.encapuzado_kunori": "Hooded Kunori",
    "entity.verden.tentaculo": "Koold Tentacle",
    "entity.verden.amalgama_koold": "Koold Amalgam",
    "power.verden.chamas": "Flames",
    "power.verden.vento": "Wind",
    "power.verden.telecinese": "Telekinesis",
    "power.verden.gelo": "Ice",
    "power.verden.nevoa": "Crimson Mist",
    "power.verden.crono": "Matter Chronokinesis",
    "power.verden.negra": "Dark Magic",
    "power.verden.metamorfose": "Feline Transformation",
    "item.verden.espada_legiao": "Legion Sword",
    "item.verden.adaga_zytra": "Zytra's Dagger",
    "item.verden.espada_haigol": "Haigol's Sword",
    "item.verden.insignia_legiao": "Legion Insignia",
    "item.verden.pocao_recuperacao": "Recovery Potion",
    "item.verden.licantropo_adaptado_spawn_egg": "Adapted Lycanthrope Spawn Egg",
    "item.verden.soldado_legiao_spawn_egg": "Legion Soldier Spawn Egg",
    "item.verden.zytra_twin_spawn_egg": "Zytra Twin Spawn Egg",
    "item.verden.haigol_twin_spawn_egg": "Haigol Twin Spawn Egg",
    "item.verden.condolyus_spawn_egg": "Condolyus Spawn Egg",
    "entity.verden.licantropo_adaptado": "Adapted Lycanthrope",
    "entity.verden.soldado_legiao": "Legion Soldier",
    "message.verden.power_selected": "Power: %s",
    "message.verden.need_triquetra": "Dark Magic requires the Triquetra Amulet.",
    "message.verden.too_weak": "You are too weak for Dark Magic.",
    "message.verden.no_ammo": "Out of .45 ammo!",
    "tooltip.verden.power": "Power: %s",
    "tooltip.verden.grimorio": "Right-click: cast. Sneak + right-click: switch power.",
    "tooltip.verden.cromus": "Right-click: energy slash ahead.",
    "tooltip.verden.revolver": "Consumes .45 Ammo.",
    "tooltip.verden.corneta": "Summons Rinoy, the giant mount of Koold.",
})
w(os.path.join(ASSETS, "lang/pt_br.json"), pt)
w(os.path.join(ASSETS, "lang/en_us.json"), en)

# ---------------------------------------------------------------- RECIPES (1.21: pasta "recipe")
def shaped(name, pattern, key, result, count=1):
    w(os.path.join(DATA, "recipe", name + ".json"), {
        "type": "minecraft:crafting_shaped", "pattern": pattern,
        "key": {k: {"item": v} for k, v in key.items()},
        "result": {"id": f"verden:{result}", "count": count}})


shaped("grimorio", ["AEA", "EBE", "AEA"], {"A": "minecraft:amethyst_shard", "E": "minecraft:blaze_powder", "B": "minecraft:book"}, "grimorio")
shaped("municao_45", ["I", "G", "C"], {"I": "minecraft:iron_nugget", "G": "minecraft:gunpowder", "C": "minecraft:copper_ingot"}, "municao_45", 6)
shaped("revolver_cubus", ["II ", " DC", " I "], {"I": "minecraft:iron_ingot", "D": "minecraft:diamond", "C": "minecraft:copper_ingot"}, "revolver_cubus")
shaped("amuleto_triquetra", ["ONO", "NDN", "ONO"], {"O": "minecraft:obsidian", "N": "minecraft:netherite_scrap", "D": "minecraft:diamond"}, "amuleto_triquetra")
shaped("corneta_lucy", [" B ", "BNB", " B "], {"B": "minecraft:bone", "N": "minecraft:goat_horn"}, "corneta_lucy")
shaped("cromus", [" N ", " N ", " S "], {"N": "minecraft:netherite_ingot", "S": "minecraft:blaze_rod"}, "cromus")
shaped("machado_duplo_hiraku", ["DID", "DS ", " S "], {"D": "minecraft:diamond", "I": "minecraft:iron_ingot", "S": "minecraft:stick"}, "machado_duplo_hiraku")
shaped("espada_curva_liam", [" D", "DD", "S "], {"D": "minecraft:diamond", "S": "minecraft:stick"}, "espada_curva_liam")
shaped("berserker", [" SR", "S R", " SR"], {"S": "minecraft:stick", "R": "minecraft:string"}, "berserker")

# ---------------------------------------------------------------- LOOT TABLES (1.21: "loot_table")
def loot(name, entries):
    pools = []
    for item, mn, mx, chance in entries:
        pool = {"rolls": 1, "entries": [{"type": "minecraft:item", "name": item,
                "functions": [{"function": "minecraft:set_count", "count": {"min": mn, "max": mx}}]}]}
        if chance < 1:
            pool["conditions"] = [{"condition": "minecraft:random_chance", "chance": chance}]
        pools.append(pool)
    w(os.path.join(DATA, "loot_table/entities", name + ".json"), {"type": "minecraft:entity", "pools": pools})


loot("licantropo", [("minecraft:bone", 0, 2, 1), ("minecraft:rotten_flesh", 0, 2, 1), ("verden:mneumonio", 1, 1, 0.1)])
loot("encapuzado_kunori", [("minecraft:coal", 0, 2, 1), ("verden:coroa_espinhos_kunori", 1, 1, 0.1), ("verden:municao_45", 1, 3, 0.2)])
loot("tentaculo", [("minecraft:slime_ball", 0, 2, 1)])
loot("amalgama_koold", [("minecraft:netherite_scrap", 2, 4, 1), ("verden:mneumonio", 3, 6, 1), ("verden:amuleto_triquetra", 1, 1, 0.5)])
loot("rinoy", [("minecraft:leather", 1, 3, 1)])
loot("licantropo_adaptado", [("minecraft:bone", 1, 3, 1), ("verden:mneumonio", 1, 1, 0.25)])
loot("soldado_legiao", [("minecraft:iron_nugget", 1, 4, 1), ("verden:insignia_legiao", 1, 1, 0.5),
                        ("verden:espada_legiao", 1, 1, 0.08), ("verden:pocao_recuperacao", 1, 1, 0.1)])
loot("zytra_twin", [("verden:adaga_zytra", 1, 1, 1), ("verden:insignia_legiao", 3, 6, 1),
                    ("verden:pocao_recuperacao", 1, 3, 1), ("minecraft:diamond", 1, 3, 1)])
loot("haigol_twin", [("verden:espada_haigol", 1, 1, 1), ("verden:insignia_legiao", 3, 6, 1),
                     ("verden:pocao_recuperacao", 1, 3, 1), ("minecraft:diamond", 2, 4, 1)])
loot("condolyus", [("minecraft:blaze_powder", 0, 2, 1)])

w(os.path.join(DATA, "recipe", "pocao_recuperacao.json"), {
    "type": "minecraft:crafting_shapeless",
    "ingredients": [{"item": "minecraft:glass_bottle"}, {"item": "minecraft:glistering_melon_slice"},
                    {"item": "minecraft:apple"}],
    "result": {"id": "verden:pocao_recuperacao", "count": 1}})

print("Assets gerados.")

# Modelos 3D próprios e suas texturas (sobrescrevem as texturas simples acima)
import runpy
runpy.run_path(os.path.join(os.path.dirname(os.path.abspath(__file__)), "model_gen.py"), run_name="__main__")
runpy.run_path(os.path.join(os.path.dirname(os.path.abspath(__file__)), "humanoid_gen.py"), run_name="__main__")
