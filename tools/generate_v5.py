#!/usr/bin/env python3
"""v0.5.0: texturas de itens/armaduras/NPCs, lang, receitas, loot, tags, conquistas, worldgen e sounds.json.
Roda sozinho (python3 tools/generate_v5.py) e também ao final de generate_assets.py."""
import json, os, random
from PIL import Image, ImageDraw

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
RES = os.path.join(ROOT, "src/main/resources")
ASSETS = os.path.join(RES, "assets/verden")
DATA = os.path.join(RES, "data/verden")
rng = random.Random(55)


def w(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(obj, f, ensure_ascii=False, indent=2)


def sh(c, d):
    return tuple(max(0, min(255, int(x + d))) for x in c[:3]) + (255,)


def A(c):
    return tuple(c[:3]) + (255,)


def new16():
    return Image.new("RGBA", (16, 16), (0, 0, 0, 0))


# ------------------------------------------------------------ ÍCONES 16x16
def coin(base, rim, mark):
    im = new16(); d = ImageDraw.Draw(im)
    d.ellipse([2, 2, 13, 13], fill=A(rim)); d.ellipse([3, 3, 12, 12], fill=A(base))
    d.point((5, 5), fill=A((255, 255, 255)))
    mark(d)
    return im


def monter():
    def m(d):
        d.line([(6, 10), (6, 5), (9, 8), (9, 5)], fill=(120, 80, 10, 255))
    return coin((245, 200, 60), (160, 110, 20), m)


def copta():
    def m(d):
        d.line([(6, 6), (9, 6), (9, 9), (6, 9), (6, 6)], fill=(110, 60, 30, 255))
    return coin((205, 135, 75), (110, 60, 30), m)


def garra():
    im = new16(); d = ImageDraw.Draw(im)
    for i, x in enumerate((4, 7, 10)):
        d.polygon([(x, 13), (x + 2, 13), (x + 3, 3 + i)], fill=(235, 228, 205, 255), outline=(110, 95, 70, 255))
    d.rectangle([3, 12, 12, 14], fill=(80, 50, 40, 255))
    return im


def bussola():
    im = new16(); d = ImageDraw.Draw(im)
    d.ellipse([1, 1, 14, 14], fill=(150, 105, 40, 255), outline=(70, 45, 15, 255))
    d.ellipse([3, 3, 12, 12], fill=(225, 215, 190, 255))
    d.polygon([(8, 3), (6, 8), (10, 8)], fill=(190, 30, 40, 255))
    d.polygon([(8, 13), (6, 8), (10, 8)], fill=(60, 60, 70, 255))
    d.point((8, 8), fill=(255, 220, 90, 255))
    return im


def lanterna():
    im = new16(); d = ImageDraw.Draw(im)
    d.rectangle([5, 1, 10, 2], fill=(70, 55, 40, 255))
    d.line([(6, 0), (9, 0)], fill=(70, 55, 40, 255))
    d.rectangle([4, 3, 11, 12], fill=(255, 225, 120, 255), outline=(90, 70, 30, 255))
    d.rectangle([6, 5, 9, 10], fill=(255, 250, 200, 255))
    d.rectangle([4, 13, 11, 14], fill=(70, 55, 40, 255))
    return im


def mochila():
    im = new16(); d = ImageDraw.Draw(im)
    d.rectangle([3, 4, 12, 14], fill=(150, 95, 50, 255), outline=(70, 40, 20, 255))
    d.rectangle([5, 1, 10, 4], fill=(120, 75, 40, 255), outline=(70, 40, 20, 255))
    d.rectangle([4, 8, 11, 11], fill=(125, 78, 42, 255), outline=(70, 40, 20, 255))
    d.point((8, 9), fill=(235, 190, 70, 255))
    d.line([(3, 6), (12, 6)], fill=(235, 190, 70, 255))
    return im


def racao():
    im = new16(); d = ImageDraw.Draw(im)
    d.rectangle([2, 4, 13, 12], fill=(215, 170, 100, 255), outline=(110, 75, 35, 255))
    d.line([(2, 8), (13, 8)], fill=(150, 100, 50, 255))
    d.line([(7, 4), (7, 12)], fill=(150, 100, 50, 255))
    d.point((4, 6), fill=(255, 235, 180, 255)); d.point((10, 10), fill=(255, 235, 180, 255))
    return im


def frasco(liquid, glow):
    im = new16(); d = ImageDraw.Draw(im)
    d.rectangle([6, 1, 9, 3], fill=(150, 110, 70, 255), outline=(70, 50, 30, 255))
    d.rectangle([7, 4, 8, 5], fill=(210, 230, 235, 255))
    d.ellipse([3, 5, 12, 14], fill=A(liquid), outline=sh(liquid, -80))
    d.ellipse([5, 7, 7, 9], fill=A(glow))
    return im


def arma(blade, edge, guard, grip, flame=None):
    im = new16(); d = ImageDraw.Draw(im)
    for i in range(3, 14):
        d.point((i, 14 - i), fill=A(blade)); d.point((i + 1, 14 - i), fill=A(edge))
    d.line([(2, 9), (6, 13)], fill=A(guard))
    d.line([(1, 14), (3, 12)], fill=A(grip))
    if flame:
        for x, y in [(11, 3), (13, 5), (9, 5)]:
            d.point((x, y), fill=A(flame))
    return im


def lanca():
    im = new16(); d = ImageDraw.Draw(im)
    d.line([(2, 14), (12, 4)], fill=(120, 90, 60, 255))
    d.polygon([(11, 5), (15, 0), (14, 6)], fill=(190, 235, 255, 255), outline=(70, 130, 190, 255))
    d.polygon([(9, 7), (10, 3), (13, 6)], fill=(150, 215, 250, 255))
    return im


def municao(tint):
    im = new16(); d = ImageDraw.Draw(im)
    d.rectangle([5, 6, 10, 14], fill=(205, 150, 70, 255), outline=(110, 70, 30, 255))
    d.polygon([(5, 6), (10, 6), (9, 2), (6, 2)], fill=A(tint), outline=sh(tint, -80))
    d.point((6, 9), fill=(255, 225, 150, 255))
    return im


def armadura(kind, pal):
    main, dark, trim = pal
    im = new16(); d = ImageDraw.Draw(im)
    o = sh(dark, -40)
    if kind == "capacete":
        d.rectangle([3, 3, 12, 12], fill=A(main), outline=o); d.rectangle([5, 8, 10, 12], fill=(10, 10, 14, 255))
        d.line([(3, 4), (12, 4)], fill=A(trim)); d.rectangle([7, 1, 8, 3], fill=A(trim))
    elif kind == "peitoral":
        d.rectangle([2, 2, 13, 14], fill=A(main), outline=o); d.rectangle([5, 2, 10, 4], fill=A(dark))
        d.line([(5, 6), (10, 11)], fill=A(trim)); d.line([(10, 6), (5, 11)], fill=A(trim))
    elif kind == "calcas":
        d.rectangle([3, 2, 12, 7], fill=A(main), outline=o); d.rectangle([3, 8, 6, 14], fill=A(main), outline=o)
        d.rectangle([9, 8, 12, 14], fill=A(main), outline=o); d.line([(3, 3), (12, 3)], fill=A(trim))
    else:
        d.rectangle([2, 7, 6, 13], fill=A(main), outline=o); d.rectangle([9, 7, 13, 13], fill=A(main), outline=o)
        d.line([(2, 8), (6, 8)], fill=A(trim)); d.line([(9, 8), (13, 8)], fill=A(trim))
    return im


PAL_LEGIAO = ((120, 126, 142), (70, 74, 88), (200, 40, 50))
PAL_RINOY = ((215, 210, 198), (120, 100, 80), (240, 240, 235))


def egg_icon(hair, skin, cloth):
    im = new16(); d = ImageDraw.Draw(im)
    d.rectangle([4, 11, 11, 15], fill=A(cloth)); d.rectangle([5, 3, 10, 10], fill=A(skin), outline=sh(skin, -60))
    d.rectangle([5, 2, 10, 4], fill=A(hair)); d.rectangle([4, 3, 4, 7], fill=A(hair))
    d.point((6, 7), fill=(20, 20, 20, 255)); d.point((9, 7), fill=(20, 20, 20, 255))
    return im


# ------------------------------------------------------------ NPCs: skins 64x64
# nome: (pele, cabelo, roupa, roupa2/detalhe, olhos, estilo)
NPCS = {
    "luke":       ((235, 200, 175), (30, 25, 30), (60, 90, 160), (230, 230, 240), (40, 30, 20), "short"),
    "lucy":       ((240, 210, 185), (235, 215, 150), (90, 120, 70), (150, 100, 50), (60, 140, 80), "long"),
    "noite":      ((235, 205, 185), (25, 35, 80), (30, 30, 45), (60, 140, 230), (80, 220, 255), "long"),
    "milly":      ((230, 195, 170), (110, 70, 40), (60, 110, 70), (130, 90, 50), (60, 40, 20), "long"),
    "liam":       ((235, 200, 170), (235, 200, 90), (120, 80, 50), (190, 40, 40), (70, 120, 200), "short"),
    "hiraku":     ((225, 190, 160), (20, 20, 25), (35, 30, 45), (235, 110, 30), (50, 30, 20), "short"),
    "rose":       ((240, 205, 180), (200, 50, 30), (50, 40, 45), (150, 30, 30), (80, 60, 30), "long"),
    "vanessa":    ((240, 210, 190), (45, 25, 30), (150, 25, 40), (235, 200, 200), (210, 30, 40), "long"),
    "konge":      ((235, 225, 225), (210, 215, 225), (235, 235, 240), (215, 175, 60), (200, 30, 40), "long"),
    "menetryx":   ((225, 190, 165), (50, 40, 35), (120, 126, 142), (200, 40, 50), (60, 50, 40), "short"),
    "rashad":     ((165, 115, 80), (30, 22, 20), (90, 85, 55), (150, 70, 40), (40, 25, 15), "beard"),
    "yann":       ((225, 190, 165), (190, 190, 195), (235, 235, 240), (70, 110, 180), (90, 90, 100), "beard"),
    "thalen":     ((225, 195, 170), (235, 235, 240), (100, 50, 140), (230, 200, 70), (120, 80, 200), "beard"),
    "taverneiro": ((225, 180, 150), None, (150, 100, 60), (235, 230, 220), (60, 40, 20), "beard"),
    "raoni":      ((195, 150, 120), (35, 28, 25), (225, 225, 230), (140, 110, 80), (40, 30, 20), "beard"),
}


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


# ------------------------------------------------------------ ARMADURAS (camadas 64x32)
def armor_layers(name, pal):
    main, dark, trim = pal
    for layer in (1, 2):
        im = Image.new("RGBA", (64, 32), (0, 0, 0, 0)); d = ImageDraw.Draw(im)
        if layer == 1:
            fill(im, (0, 0, 32, 16), main, 8)
            fill(im, (16, 16, 40, 32), main, 8)
            fill(im, (40, 16, 56, 32), main, 8)
            fill(im, (0, 16, 16, 32), dark, 6)             # botas usam as pernas
            d.rectangle([0, 28, 15, 31], fill=A(sh(dark, -30)))
            d.rectangle([0, 6, 31, 6], fill=A(trim))        # faixa do capacete
            d.line([(21, 21), (26, 28)], fill=A(trim)); d.line([(26, 21), (21, 28)], fill=A(trim))  # espadas cruzadas
            d.rectangle([40, 16, 55, 17], fill=A(trim))
        else:
            fill(im, (16, 16, 40, 32), dark, 6)
            fill(im, (0, 16, 16, 32), main, 8)
            d.rectangle([16, 16, 39, 18], fill=A(trim))     # cinto
            d.rectangle([0, 16, 15, 17], fill=A(trim))
        im.save(os.path.join(ASSETS, f"textures/models/armor/{name}_layer_{layer}.png"))


os.makedirs(os.path.join(ASSETS, "textures/models/armor"), exist_ok=True)
armor_layers("legiao", PAL_LEGIAO)
armor_layers("pele_rinoy", PAL_RINOY)

# ------------------------------------------------------------ SALVAR ITENS
ITEMS = {
    "monter": monter, "copta_flereniana": copta, "garra_licantropo": garra, "bussola_verden": bussola,
    "lanterna_fauno": lanterna, "mochila_couro": mochila, "racao_viagem": racao,
    "pocao_visao_noturna": lambda: frasco((90, 70, 200), (200, 190, 255)),
    "elixir_calor": lambda: frasco((240, 90, 40), (255, 220, 120)),
    "lanca_gelo": lanca,
    "katana_chamas": lambda: arma((200, 205, 215), (255, 140, 40), (60, 50, 50), (120, 30, 30), (255, 200, 60)),
    "municao_incendiaria": lambda: municao((255, 120, 30)),
    "municao_perfurante": lambda: municao((120, 200, 255)),
}
for kind in ("capacete", "peitoral", "calcas", "botas"):
    ITEMS["legiao_" + kind] = (lambda k=kind: armadura(k, PAL_LEGIAO))
for nm, kind in (("capuz", "capacete"), ("casaco", "peitoral"), ("calcas", "calcas"), ("botas", "botas")):
    ITEMS["rinoy_" + nm] = (lambda k=kind: armadura(k, PAL_RINOY))
for n, (skin, hair, cloth, trim, eyes, style) in NPCS.items():
    ITEMS["npc_" + n] = (lambda s=skin, h=hair, c=cloth: egg_icon(h or (90, 60, 40), s, c))
    npc_skin(skin, hair, cloth, trim, eyes, style).save(os.path.join(ASSETS, "textures/entity/npc", n + ".png")) \
        if os.makedirs(os.path.join(ASSETS, "textures/entity/npc"), exist_ok=True) is None else None

HANDHELD = {"lanca_gelo", "katana_chamas"}
for name, fn in ITEMS.items():
    p = os.path.join(ASSETS, "textures/item", name + ".png")
    os.makedirs(os.path.dirname(p), exist_ok=True)
    fn().save(p)
    w(os.path.join(ASSETS, "models/item", name + ".json"),
      {"parent": "minecraft:item/handheld" if name in HANDHELD else "minecraft:item/generated",
       "textures": {"layer0": f"verden:item/{name}"}})

# ------------------------------------------------------------ LANG
DIALOGO = {
    "luke": ["E-eu… eu nem sei se tenho magia no sangue. Mas vou continuar tentando.",
             "A Srta. Marie dizia que meu pai era um inútil sem magia… Será que puxei a ele?",
             "A Vanessa parte um toco só com a Névoa. Um dia eu também vou conseguir.",
             "Se você tem um Grimório, me ensina? Eu só preciso de uma chance."],
    "lucy": ["Eu me chamo Lúcius… mas pode me chamar de Lucy.",
             "O vento de Koold conta histórias para quem sabe escutar. Quer ouvir?",
             "Rinoy não gosta de gente apressada. Respire fundo e ele virá até você.",
             "Se precisa de calor e de uma boa lança, eu tenho o que procura."],
    "noite": ["Cuidado com o Condolyus. Ele cospe metal derretido quando está nervoso… e quando está com fome também.",
              "A Cromus é minha. Não importa quantas vezes me perguntem.",
              "Às vezes eu prefiro ser gato. Ninguém olha para um gato azul.",
              "Já deu pedaços de maçã a um Condolyus? Ele fica manso na hora."],
    "milly": ["O Berserker não erra quando o Alkorykan pulsa. Mas flechas custam coptas, viu?",
              "Zytra Twin não era uma legionária qualquer. Se ela caiu, o perigo é sério.",
              "Já ouviu falar de mneumônio? Vale uma fortuna para quem souber vender.",
              "Prefere flechas ou pão? Eu vendo os dois."],
    "liam": ["Que tal? Eu tô faminto.",
             "Tem uma taverna ali, o Búfalo Escarlate. Cheiro de pão quente e carne assada.",
             "Espada curva é questão de pulso, não de força. Vem, eu mostro.",
             "Não me olhe assim. Eu sempre tenho um plano… quase sempre."],
    "hiraku": ["Se não estão atrás da gente, não é problema nosso.",
               "Magia sem disciplina só serve para se queimar. Pergunte à minha katana.",
               "Quer uma lâmina que queima o que corta? Traga mønters.",
               "Não confie em quem sorri demais em tempos de licantropos."],
    "rose": ["Seis tiros, uma recarga e nenhuma dúvida. É assim que eu trabalho.",
             "Coptas Flerinianas, mønters… aceito os dois. Chumbo não tem preferência.",
             "Um quarto decente custa duzentos e cinquenta coptas. Dá pra acreditar?",
             "Munição incendiária? Só para quem sabe onde apontar."],
    "vanessa": ["Legal, né? Andei treinando. Quero usar a Névoa Carmesim igual a mamãe usa.",
                "A névoa obedece a quem fecha a mão com vontade. É só treinar.",
                "Tenho uns remédios. Poção de Recuperação, bem fresquinha.",
                "O Luke tem mais talento do que imagina. Eu sei."],
    "konge": ["Juntos, conseguimos.",
              "Os licantropos não são apenas feras. São sinal de que algo maior se move no escuro.",
              "A Legião das Espadas responde a mim. Caçar essas criaturas é a minha ordem.",
              "O Palácio Branco vigia a Capital do Norte. E eu vigio o reino."],
    "menetryx": ["A Legião paga por cada garra de licantropo. Quatro garras, um mønter.",
                 "Zytra Twin foi morta em Grandeza. Não foi acaso: havia marcas de carruagem na saída.",
                 "Traga insígnias de legionários caídos. Elas não ficam sem recompensa.",
                 "Quer vestir o símbolo das espadas cruzadas? Precisa merecer."],
    "rashad": ["Hm. Mneumônios… faz tempo que não vejo um. Pago bem.",
               "Viajante? Então leve uma bússola e uma lanterna. Os caminhos de Jørd não perdoam.",
               "Um mønter vale nove coptas. Faço o câmbio sem cobrar nada.",
               "Meus preços são justos. Só desconfio de quem pechincha demais."],
    "yann": ["Os licantropos se espalham mais rápido do que a Legião consegue conter.",
             "Estou cansado, mas não derrotado. Ainda há muito a fazer.",
             "Traga mneumônios e garras. A Academia e a Legião precisam deles.",
             "Que as espadas cruzadas protejam você."],
    "thalen": ["Ofereci quase cem mønters pelo meu objeto encantado. Ele saiu voando por Jørd e sumiu!",
               "Dizem que sou maluco. Dizem também que meus elixires funcionam.",
               "Um mneumônio? Interessante… muito interessante. Deixe-me ver.",
               "Quando tudo congela, o Elixir de Calor é o melhor amigo do viajante."],
    "taverneiro": ["Bem-vindo ao Búfalo Escarlate! O melhor pão e a carne mais macia da cidade.",
                   "Tá frio lá fora, hein? Sente, coma, aqueça os ossos.",
                   "Aceitamos coptas. Mønters também, mas aí o troco é por minha conta.",
                   "Ouvi dizer que a Legião marchou pela rua hoje. Tempos estranhos."],
    "raoni": ["As coisas estão mudando, não é, Lucy?",
              "Koold é cruel com quem a subestima. Os rinoys sabem disso.",
              "Quem cruza a neve sem pele de rinoy não volta inteiro.",
              "Vi algo enorme numa caverna. Tentáculos, mandíbulas… Não vá sozinho."],
}
NOMES = {"luke": "Luke", "lucy": "Lucy", "noite": "Noite", "milly": "Milly", "liam": "Liam", "hiraku": "Hiraku",
         "rose": "Rose", "vanessa": "Vanessa", "konge": "Rei Konge", "menetryx": "Menetryx", "rashad": "Rashad",
         "yann": "Grão-mestre Yann", "thalen": "Thalen, o Mago", "taverneiro": "Taverneiro do Búfalo Escarlate",
         "raoni": "Raoni"}

pt_new = {
    "item.verden.monter": "Mønter", "item.verden.copta_flereniana": "Copta Flereniana",
    "item.verden.garra_licantropo": "Garra de Licantropo", "item.verden.bussola_verden": "Bússola de Verden",
    "item.verden.lanterna_fauno": "Lanterna do Fauno", "item.verden.mochila_couro": "Mochila de Couro",
    "item.verden.racao_viagem": "Ração de Viagem", "item.verden.pocao_visao_noturna": "Poção de Visão Noturna",
    "item.verden.elixir_calor": "Elixir de Calor", "item.verden.lanca_gelo": "Lança de Gelo",
    "item.verden.katana_chamas": "Katana de Chamas", "item.verden.municao_incendiaria": "Munição Incendiária",
    "item.verden.municao_perfurante": "Munição Perfurante",
    "item.verden.legiao_capacete": "Elmo da Legião", "item.verden.legiao_peitoral": "Peitoral da Legião",
    "item.verden.legiao_calcas": "Grevas da Legião", "item.verden.legiao_botas": "Botas da Legião",
    "item.verden.rinoy_capuz": "Capuz de Pele de Rinoy", "item.verden.rinoy_casaco": "Casaco de Pele de Rinoy",
    "item.verden.rinoy_calcas": "Calças de Pele de Rinoy", "item.verden.rinoy_botas": "Botas de Pele de Rinoy",
    "item.verden.npc_spawn": "Invocar: %s",
    "entity.verden.npc": "Morador de Verden",
    "tooltip.verden.lanca_gelo": "Cada golpe congela e retarda o alvo.",
    "tooltip.verden.katana_chamas": "Cada golpe incendeia o alvo.",
    "tooltip.verden.lanterna": "Na mão: Visão Noturna.",
    "tooltip.verden.mochila": "Clique direito: abre um inventário extra de 27 espaços.",
    "tooltip.verden.bussola": "Clique direito: coordenadas e seta para o ponto de partida.",
    "tooltip.verden.elixir_calor": "Derrete o gelo no corpo e protege por 2 min.",
    "message.verden.bussola": "X %s  Y %s  Z %s   |   Início %s %sm",
    "message.verden.npc_trade_hint": "Negocie com mønters e coptas.",
    "advancement.verden.root.title": "Verden",
    "advancement.verden.root.desc": "Pegue o Grimório e comece a sua história.",
    "advancement.verden.moeda.title": "Algumas moedas",
    "advancement.verden.moeda.desc": "Consiga mønters ou coptas flerinianas.",
    "advancement.verden.licantropo.title": "Caçador de licantropos",
    "advancement.verden.licantropo.desc": "Derrote um licantropo.",
    "advancement.verden.legiao.title": "Espadas cruzadas",
    "advancement.verden.legiao.desc": "Vista o Peitoral da Legião.",
    "advancement.verden.zytra.title": "Juntos, conseguimos",
    "advancement.verden.zytra.desc": "Derrote Zytra Twin.",
    "advancement.verden.haigol.title": "O irmão de armadura",
    "advancement.verden.haigol.desc": "Derrote Haigol Twin.",
    "advancement.verden.amalgama.title": "O que vive em Koold",
    "advancement.verden.amalgama.desc": "Derrote a Amálgama de Koold.",
}
for k, v in DIALOGO.items():
    pt_new[f"npc.verden.{k}.name"] = NOMES[k]
    for i, line in enumerate(v):
        pt_new[f"npc.verden.{k}.line.{i}"] = line
en_new = dict(pt_new)
en_new.update({
    "item.verden.monter": "Mønter", "item.verden.copta_flereniana": "Flerinian Copta",
    "item.verden.garra_licantropo": "Lycanthrope Claw", "item.verden.bussola_verden": "Verden Compass",
    "item.verden.lanterna_fauno": "Faun's Lantern", "item.verden.mochila_couro": "Leather Backpack",
    "item.verden.racao_viagem": "Travel Rations", "item.verden.pocao_visao_noturna": "Night Vision Potion",
    "item.verden.elixir_calor": "Warmth Elixir", "item.verden.lanca_gelo": "Ice Spear",
    "item.verden.katana_chamas": "Flame Katana", "item.verden.municao_incendiaria": "Incendiary Ammo",
    "item.verden.municao_perfurante": "Piercing Ammo",
    "item.verden.legiao_capacete": "Legion Helm", "item.verden.legiao_peitoral": "Legion Breastplate",
    "item.verden.legiao_calcas": "Legion Greaves", "item.verden.legiao_botas": "Legion Boots",
    "item.verden.rinoy_capuz": "Rinoy Fur Hood", "item.verden.rinoy_casaco": "Rinoy Fur Coat",
    "item.verden.rinoy_calcas": "Rinoy Fur Pants", "item.verden.rinoy_botas": "Rinoy Fur Boots",
    "item.verden.npc_spawn": "Summon: %s", "entity.verden.npc": "Verden Villager",
    "tooltip.verden.lanca_gelo": "Every hit freezes and slows the target.",
    "tooltip.verden.katana_chamas": "Every hit sets the target on fire.",
    "tooltip.verden.lanterna": "In hand: Night Vision.",
    "tooltip.verden.mochila": "Right-click: opens an extra 27-slot inventory.",
    "tooltip.verden.bussola": "Right-click: coordinates and an arrow to the world start.",
    "tooltip.verden.elixir_calor": "Melts body frost and protects for 2 min.",
    "message.verden.bussola": "X %s  Y %s  Z %s   |   Start %s %sm",
    "message.verden.npc_trade_hint": "Trade with mønters and coptas.",
    "advancement.verden.root.desc": "Get the Grimoire and begin your story.",
    "advancement.verden.moeda.title": "A few coins", "advancement.verden.moeda.desc": "Obtain mønters or Flerinian coptas.",
    "advancement.verden.licantropo.title": "Lycanthrope hunter", "advancement.verden.licantropo.desc": "Defeat a lycanthrope.",
    "advancement.verden.legiao.title": "Crossed swords", "advancement.verden.legiao.desc": "Wear the Legion Breastplate.",
    "advancement.verden.zytra.desc": "Defeat Zytra Twin.", "advancement.verden.haigol.title": "The armored brother",
    "advancement.verden.haigol.desc": "Defeat Haigol Twin.", "advancement.verden.amalgama.title": "What lives in Koold",
    "advancement.verden.amalgama.desc": "Defeat the Koold Amalgam.",
})
for fname, extra in (("pt_br", pt_new), ("en_us", en_new)):
    p = os.path.join(ASSETS, "lang", fname + ".json")
    cur = json.load(open(p, encoding="utf-8")) if os.path.exists(p) else {}
    cur.update(extra)
    w(p, cur)

# ------------------------------------------------------------ RECEITAS
def recipe(name, obj):
    w(os.path.join(DATA, "recipe", name + ".json"), obj)


def shaped(name, pattern, key, result, count=1):
    recipe(name, {"type": "minecraft:crafting_shaped", "pattern": pattern,
                  "key": {k: {"item": v} for k, v in key.items()}, "result": {"id": result, "count": count}})


def shapeless(name, items, result, count=1):
    recipe(name, {"type": "minecraft:crafting_shapeless", "ingredients": [{"item": i} for i in items],
                  "result": {"id": result, "count": count}})


V = lambda n: "verden:" + n
shapeless("bussola_verden", ["minecraft:compass", "minecraft:amethyst_shard"], V("bussola_verden"))
shapeless("lanterna_fauno", ["minecraft:lantern", "minecraft:glow_berries"], V("lanterna_fauno"))
shaped("mochila_couro", ["LLL", "LCL", "LLL"], {"L": "minecraft:leather", "C": "minecraft:chest"}, V("mochila_couro"))
shapeless("racao_viagem", ["minecraft:bread", "minecraft:cooked_beef", "minecraft:dried_kelp"], V("racao_viagem"), 2)
shapeless("pocao_visao_noturna", ["minecraft:glass_bottle", "minecraft:golden_carrot", "minecraft:glow_berries"], V("pocao_visao_noturna"))
shapeless("elixir_calor", ["minecraft:glass_bottle", "minecraft:magma_cream", "minecraft:blaze_powder"], V("elixir_calor"))
shaped("lanca_gelo", [" BD", " SB", "S  "], {"B": "minecraft:blue_ice", "D": "minecraft:diamond", "S": "minecraft:stick"}, V("lanca_gelo"))
shaped("katana_chamas", ["  D", " DM", "B  "], {"D": "minecraft:diamond", "M": "minecraft:magma_cream", "B": "minecraft:blaze_rod"}, V("katana_chamas"))
shapeless("municao_incendiaria", [V("municao_45"), V("municao_45"), "minecraft:blaze_powder"], V("municao_incendiaria"), 2)
shapeless("municao_perfurante", [V("municao_45"), V("municao_45"), "minecraft:amethyst_shard"], V("municao_perfurante"), 2)
shapeless("monter_de_coptas", [V("copta_flereniana")] * 9, V("monter"))
shapeless("coptas_de_monter", [V("monter")], V("copta_flereniana"), 9)
I = {"I": "minecraft:iron_ingot", "X": V("insignia_legiao")}
shaped("legiao_capacete", ["III", "I I"], I, V("legiao_capacete"))
shaped("legiao_peitoral", ["I I", "IXI", "III"], I, V("legiao_peitoral"))
shaped("legiao_calcas", ["III", "I I", "I I"], I, V("legiao_calcas"))
shaped("legiao_botas", ["I I", "I I"], I, V("legiao_botas"))
F = {"L": "minecraft:leather", "W": "minecraft:white_wool"}
shaped("rinoy_capuz", ["LWL", "L L"], F, V("rinoy_capuz"))
shaped("rinoy_casaco", ["L L", "LWL", "LLL"], F, V("rinoy_casaco"))
shaped("rinoy_calcas", ["LWL", "L L", "L L"], F, V("rinoy_calcas"))
shaped("rinoy_botas", ["L L", "W W"], F, V("rinoy_botas"))

# ------------------------------------------------------------ LOOT
def pool(entries):
    pools = []
    for item, mn, mx, chance in entries:
        p = {"rolls": 1, "entries": [{"type": "minecraft:item", "name": item,
             "functions": [{"function": "minecraft:set_count", "count": {"min": mn, "max": mx}}]}]}
        if chance < 1:
            p["conditions"] = [{"condition": "minecraft:random_chance", "chance": chance}]
        pools.append(p)
    return pools


def loot(name, entries):
    w(os.path.join(DATA, "loot_table/entities", name + ".json"), {"type": "minecraft:entity", "pools": pool(entries)})


loot("licantropo", [("minecraft:bone", 0, 2, 1), ("minecraft:rotten_flesh", 0, 2, 1), ("verden:garra_licantropo", 1, 1, 0.5),
                    ("verden:mneumonio", 1, 1, 0.1)])
loot("licantropo_adaptado", [("minecraft:bone", 1, 3, 1), ("verden:garra_licantropo", 1, 2, 0.8), ("verden:mneumonio", 1, 1, 0.25)])
loot("encapuzado_kunori", [("minecraft:coal", 0, 2, 1), ("verden:copta_flereniana", 1, 4, 0.7),
                           ("verden:coroa_espinhos_kunori", 1, 1, 0.1), ("verden:municao_45", 1, 3, 0.2)])
loot("soldado_legiao", [("minecraft:iron_nugget", 1, 4, 1), ("verden:copta_flereniana", 1, 3, 0.6),
                        ("verden:insignia_legiao", 1, 1, 0.5), ("verden:espada_legiao", 1, 1, 0.08),
                        ("verden:pocao_recuperacao", 1, 1, 0.1), ("verden:racao_viagem", 1, 1, 0.2)])
loot("zytra_twin", [("verden:adaga_zytra", 1, 1, 1), ("verden:insignia_legiao", 3, 6, 1), ("verden:monter", 2, 4, 1),
                    ("verden:pocao_recuperacao", 1, 3, 1), ("minecraft:diamond", 1, 3, 1)])
loot("haigol_twin", [("verden:espada_haigol", 1, 1, 1), ("verden:insignia_legiao", 3, 6, 1), ("verden:monter", 3, 5, 1),
                     ("verden:pocao_recuperacao", 1, 3, 1), ("minecraft:diamond", 2, 4, 1)])
loot("amalgama_koold", [("minecraft:netherite_scrap", 2, 4, 1), ("verden:mneumonio", 3, 6, 1), ("verden:monter", 8, 14, 1),
                        ("verden:amuleto_triquetra", 1, 1, 0.5), ("verden:garra_licantropo", 3, 6, 1)])
loot("rinoy", [("minecraft:leather", 1, 3, 1), ("minecraft:white_wool", 0, 2, 0.5)])

w(os.path.join(DATA, "loot_table/chests/acampamento_legiao.json"), {"type": "minecraft:chest", "pools": [
    {"rolls": {"min": 3, "max": 5}, "entries": [
        {"type": "minecraft:item", "name": "verden:copta_flereniana", "weight": 10, "functions": [{"function": "minecraft:set_count", "count": {"min": 2, "max": 8}}]},
        {"type": "minecraft:item", "name": "verden:racao_viagem", "weight": 8, "functions": [{"function": "minecraft:set_count", "count": {"min": 1, "max": 3}}]},
        {"type": "minecraft:item", "name": "verden:pocao_recuperacao", "weight": 5},
        {"type": "minecraft:item", "name": "verden:municao_45", "weight": 5, "functions": [{"function": "minecraft:set_count", "count": {"min": 3, "max": 8}}]},
        {"type": "minecraft:item", "name": "verden:insignia_legiao", "weight": 4},
        {"type": "minecraft:item", "name": "verden:monter", "weight": 3},
        {"type": "minecraft:item", "name": "verden:bussola_verden", "weight": 2},
        {"type": "minecraft:item", "name": "verden:legiao_capacete", "weight": 1},
        {"type": "minecraft:item", "name": "verden:legiao_botas", "weight": 1},
        {"type": "minecraft:item", "name": "verden:elixir_calor", "weight": 2}]}]})

# ------------------------------------------------------------ TAGS, WORLDGEN, CONQUISTAS, SONS
w(os.path.join(RES, "data/minecraft/tags/item/freeze_immune_wearables.json"),
  {"replace": False, "values": ["verden:rinoy_capuz", "verden:rinoy_casaco", "verden:rinoy_calcas", "verden:rinoy_botas"]})
w(os.path.join(DATA, "worldgen/configured_feature/acampamento_legiao.json"),
  {"type": "verden:acampamento_legiao", "config": {}})
w(os.path.join(DATA, "worldgen/placed_feature/acampamento_legiao.json"),
  {"feature": "verden:acampamento_legiao", "placement": [
      {"type": "minecraft:rarity_filter", "chance": 45}, {"type": "minecraft:in_square"},
      {"type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG"}, {"type": "minecraft:biome"}]})


def adv(name, parent, icon, frame, criteria, requirements=None):
    o = {"display": {"icon": {"id": icon}, "title": {"translate": f"advancement.verden.{name}.title"},
                     "description": {"translate": f"advancement.verden.{name}.desc"}, "frame": frame,
                     "show_toast": True, "announce_to_chat": True, "hidden": False},
         "criteria": criteria}
    if name == "root":
        o["display"]["background"] = "minecraft:textures/gui/advancements/backgrounds/stone.png"
        o["display"]["announce_to_chat"] = False
    if parent:
        o["parent"] = parent
    if requirements:
        o["requirements"] = requirements
    w(os.path.join(DATA, "advancement", name + ".json"), o)


def has(item):
    return {"trigger": "minecraft:inventory_changed", "conditions": {"items": [{"items": item}]}}


def kill(ent):
    return {"trigger": "minecraft:player_killed_entity", "conditions": {"entity": [
        {"condition": "minecraft:entity_properties", "entity": "this", "predicate": {"type": ent}}]}}


adv("root", None, "verden:grimorio", "task", {"grimorio": has("verden:grimorio")})
adv("moeda", "verden:root", "verden:monter", "task", {"m": has("verden:monter"), "c": has("verden:copta_flereniana")}, [["m", "c"]])
adv("licantropo", "verden:root", "verden:garra_licantropo", "task", {"k": kill("verden:licantropo"), "a": kill("verden:licantropo_adaptado")}, [["k", "a"]])
adv("legiao", "verden:root", "verden:legiao_peitoral", "task", {"peito": {"trigger": "minecraft:inventory_changed", "conditions": {"items": [{"items": "verden:legiao_peitoral"}]}}})
adv("zytra", "verden:legiao", "verden:adaga_zytra", "goal", {"k": kill("verden:zytra_twin")})
adv("haigol", "verden:legiao", "verden:espada_haigol", "goal", {"k": kill("verden:haigol_twin")})
adv("amalgama", "verden:licantropo", "verden:amuleto_triquetra", "challenge", {"k": kill("verden:amalgama_koold")})

w(os.path.join(ASSETS, "sounds.json"), {
    "licantropo.growl": {"sounds": [{"name": "verden:growl", "volume": 0.9}]},
    "licantropo.hurt": {"sounds": ["verden:lycan_hurt"]},
    "licantropo.death": {"sounds": ["verden:lycan_death"]},
    "amalgama.roar": {"sounds": ["verden:roar"]},
    "music.amalgama": {"sounds": [{"name": "verden:boss_music", "stream": True}]},
    "legiao.horn": {"sounds": ["verden:horn"]},
    "magia.cast": {"sounds": ["verden:cast"]},
    "moeda.tilintar": {"sounds": ["verden:coin"]},
    "npc.falar": {"sounds": ["verden:npc_talk"]},
})
print("v0.5.0: assets gerados.")
