#!/usr/bin/env python3
"""Gera skins 64x64 (layout clássico) de Trinity/Reeves/Dan (paleta amostrada das imagens de referência do autor)
e dos NPCs do prólogo (Narber, expedicionário, Majestade, soldado). Skins provisórias: aparência de Narber, expedicionário,
Majestade e soldado só segue o que o texto diz (pele pálida/olhos vermelhos/cabelos loiros longos); o resto é genérico."""
import os
from PIL import Image, ImageDraw
A = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'src/main/resources/assets/verden/textures/entity')
os.makedirs(A + '/npc', exist_ok=True); os.makedirs(A + '/player', exist_ok=True)
TRI = dict(hair=(141, 89, 78), skin=(212, 165, 150), cape=(130, 31, 42), tunic=(167, 132, 109), boot=(69, 42, 38), gold=(210, 170, 80), stock=(70, 45, 40), glove=(75, 48, 42), belt=(95, 60, 45))
DAN = dict(hair=(212, 202, 206), skin=(225, 190, 170), suit=(26, 26, 30), gold=(190, 150, 60), red=(150, 30, 30), boot=(22, 22, 25))
def sh(c, f): return tuple(max(0, min(255, int(v * f))) for v in c)
def paint(hair, skin, eyes, torso, arms, legs, boots, gloves=None, long_hair=False, helmet=None):
    im = Image.new('RGBA', (64, 64), (0, 0, 0, 0)); d = ImageDraw.Draw(im)
    def R(x1, y1, x2, y2, c): d.rectangle([x1, y1, x2 - 1, y2 - 1], fill=c + (255,))
    R(0, 8, 32, 16, skin); R(8, 0, 24, 8, hair); R(8, 8, 16, 10, hair); R(0, 8, 8, 11, hair); R(16, 8, 24, 11, hair); R(24, 8, 32, 16, hair)
    if long_hair: R(0, 8, 8, 16, hair); R(16, 8, 24, 16, hair); R(8, 8, 9, 12, hair); R(15, 8, 16, 12, hair)
    R(9, 12, 11, 13, eyes); R(13, 12, 15, 13, eyes); R(10, 14, 14, 15, sh(skin, 0.7))
    if helmet: R(0, 0, 32, 16, helmet); R(9, 12, 15, 13, (20, 20, 24))
    R(16, 20, 40, 32, torso); R(20, 16, 36, 20, torso)
    for ox, oy in ((40, 16), (32, 48)):
        R(ox, oy + 4, ox + 16, oy + 16, arms); R(ox + 4, oy, ox + 12, oy + 4, arms); R(ox, oy + 12, ox + 16, oy + 16, gloves or skin)
    for ox, oy in ((0, 16), (16, 48)):
        R(ox, oy + 4, ox + 16, oy + 16, legs); R(ox + 4, oy, ox + 12, oy + 4, legs); R(ox, oy + 8, ox + 16, oy + 16, boots)
    return im, d
def trinity():
    im, d = paint(TRI['hair'], TRI['skin'], (110, 60, 40), TRI['tunic'], TRI['tunic'], TRI['stock'], TRI['boot'], gloves=TRI['glove'], long_hair=True)
    c = lambda x1, y1, x2, y2, col: d.rectangle([x1, y1, x2 - 1, y2 - 1], fill=col + (255,))
    c(0, 11, 3, 16, sh(TRI['hair'], 0.75)); c(0, 13, 2, 15, TRI['stock'])                 # trança lateral
    c(32, 20, 40, 32, TRI['cape']); c(32, 31, 40, 32, TRI['gold'])                         # capa vermelha (costas) com barra dourada
    for ox in (40, 32): c(ox, 20 if ox == 40 else 52, ox + 16, 24 if ox == 40 else 56, TRI['cape']); c(ox, 23 if ox == 40 else 55, ox + 16, 24 if ox == 40 else 56, TRI['gold'])
    c(16, 20, 20, 32, TRI['cape']); c(28, 20, 32, 32, TRI['cape'])                         # capa nas laterais do tronco
    c(20, 28, 28, 30, TRI['belt']); c(23, 28, 25, 30, TRI['gold'])                         # cinto
    c(23, 21, 25, 23, TRI['gold'])                                                           # broche da ave dourada de Jord
    for ox in (4, 20): c(ox, 26, ox + 4, 28, TRI['gold'])                                   # detalhes das botas
    return im
def dan():
    im, d = paint(DAN['hair'], DAN['skin'], (70, 130, 220), DAN['suit'], DAN['suit'], DAN['suit'], DAN['boot'], gloves=DAN['suit'])
    c = lambda x1, y1, x2, y2, col: d.rectangle([x1, y1, x2 - 1, y2 - 1], fill=col + (255,))
    c(20, 16, 36, 17, DAN['red']); c(21, 21, 22, 28, DAN['gold']); c(26, 21, 27, 28, DAN['gold']); c(20, 28, 28, 29, DAN['gold']); c(23, 28, 25, 29, sh(DAN['gold'], 1.3))
    for ox, oy in ((0, 16), (16, 48)): c(ox, oy + 8, ox + 16, oy + 10, DAN['gold']); c(ox, oy + 14, ox + 16, oy + 16, DAN['gold'])
    return im
def trinity_jord():
    # VISUAL DO LIVRO (prólogo e cap. 1): armadura branca sobre vestimenta justa/vestes leves azuladas, broche de ave dourada de Jord.
    WHITE, BLUE = (232, 234, 240), (110, 140, 200)
    im, d = paint(TRI['hair'], TRI['skin'], (110, 60, 40), WHITE, BLUE, BLUE, (200, 204, 214), gloves=(190, 194, 205), long_hair=True)
    c = lambda x1, y1, x2, y2, col: d.rectangle([x1, y1, x2 - 1, y2 - 1], fill=col + (255,))
    c(0, 11, 3, 16, sh(TRI['hair'], 0.75)); c(0, 13, 2, 15, BLUE)                      # trança lateral
    c(20, 28, 28, 30, sh(WHITE, 0.8)); c(23, 28, 25, 30, TRI['gold'])                  # cinto
    c(23, 21, 25, 23, TRI['gold'])                                                        # broche da ave dourada
    c(20, 20, 28, 22, sh(WHITE, 0.92)); c(32, 20, 40, 32, sh(WHITE, 0.88))               # placa peitoral / costas
    for ox, oy in ((40, 16), (32, 48)): c(ox, oy + 4, ox + 16, oy + 8, WHITE)              # ombreiras
    for ox in (0, 16):
        oy = 16 if ox == 0 else 48
        c(ox, oy + 8, ox + 16, oy + 16, (200, 204, 214))                                  # grevas
    return im
trinity_jord().save(A + '/player/trinity_jord.png'); trinity_jord().save(A + '/npc/reeves.png')   # Reeves == Trinity (mesmo personagem; roupa do livro)
trinity().save(A + '/player/trinity.png')                                                          # visual da imagem de referência do autor (alternativo)
dan().save(A + '/player/dan.png'); dan().save(A + '/npc/dan.png')
paint((60, 45, 35), (232, 226, 222), (235, 20, 20), (35, 50, 80), (35, 50, 80), (70, 70, 78), (30, 25, 22))[0].save(A + '/npc/narber.png')
paint((60, 45, 35), (214, 170, 138), (60, 40, 30), (35, 50, 80), (35, 50, 80), (70, 70, 78), (30, 25, 22))[0].save(A + '/npc/narber_comum.png')  # Narber ainda normal (antes da influência da ilha)
paint((90, 60, 40), (220, 175, 140), (60, 40, 30), (150, 125, 85), (150, 125, 85), (90, 80, 60), (55, 40, 30))[0].save(A + '/npc/expedicionario.png')
paint((230, 205, 120), (236, 230, 224), (200, 20, 30), (35, 20, 28), (35, 20, 28), (30, 18, 24), (20, 14, 18), long_hair=True)[0].save(A + '/npc/majestade.png')
paint((120, 120, 130), (200, 160, 130), (60, 50, 40), (120, 124, 134), (120, 124, 134), (100, 104, 114), (70, 72, 80), helmet=(128, 132, 142))[0].save(A + '/npc/soldado.png')
import json
json.dump({"status": "PROVISÓRIO", "nota": "Todas as skins são geradas por tools/gen_skins.py e NÃO são arte final.",
  "skins": {"player/trinity_jord.png": "Trinity/Reeves — visual do LIVRO (armadura branca) — provisória", "player/trinity.png": "Trinity — visual da imagem de referência (capa vermelha) — provisória",
  "player/dan.png": "Dan — da imagem de referência (traje preto/dourado; as asas NÃO estão na skin) — provisória", "npc/narber.png": "Narber possuído — só pele pálida e olhos vermelhos são do texto; resto genérico", "npc/narber_comum.png": "Narber antes da influência (aparência genérica; o livro não o descreve antes)",
  "npc/expedicionario.png": "genérica", "npc/majestade.png": "só cabelos longos loiros, pele clara e olhos avermelhados são do texto; resto genérico", "npc/soldado.png": "só 'armadurado' é do texto"}},
  open(A + '/SKINS_PROVISORIAS.json', 'w', encoding='utf-8'), ensure_ascii=False, indent=1)
print('skins ok')
