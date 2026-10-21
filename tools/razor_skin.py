"""Skin 64x64 (braços clássicos) de Razor Kitsuji. Camada externa usada para o chapéu (relevo), paletó, mangas e calças."""
import os, random
from PIL import Image

ROOT = os.path.join(os.path.dirname(__file__), "..")
OUT = os.path.join(ROOT, "tools/skin")
os.makedirs(OUT, exist_ok=True)

def C(r, g, b, a=255): return (r, g, b, a)
def sh(c, d): return C(*[max(0, min(255, v + d)) for v in c[:3]], c[3])

SK, SKS, SKH = C(241, 214, 190), C(212, 170, 142), C(250, 232, 212)
SKSH = C(190, 146, 120)                       # pele sob a sombra do chapéu
HAIR, HAIRD, HAIRL, HAIRX = C(226, 150, 52), C(178, 104, 32), C(248, 190, 88), C(140, 80, 26)
EYE, EYED, EYEL = C(236, 52, 30), C(58, 12, 10), C(255, 196, 120)
HAT, HATM, HATL = C(22, 20, 26), C(40, 36, 46), C(66, 60, 74)
BAND, BANDD, BANDL = C(188, 92, 34), C(130, 60, 24), C(226, 130, 54)
BR, BRL, BRD = C(186, 136, 50), C(236, 194, 96), C(108, 74, 28)
SUIT, SUITL, SUITD = C(44, 41, 47), C(66, 62, 70), C(24, 22, 27)
VEST, VESTB = C(20, 18, 24), C(120, 112, 126)
SHIRT, SHIRTS = C(236, 232, 224), C(188, 184, 178)
TIE, TIED, TIEL = C(192, 92, 34), C(136, 62, 22), C(228, 124, 52)
PANT, PANTL = C(32, 30, 35), C(52, 49, 56)
SHOE, SHOEL, SHOED, SOLE = C(112, 64, 38), C(150, 92, 56), C(72, 40, 24), C(28, 20, 14)

img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
rnd = random.Random(2024)

def px(x, y, c):
    if c is not None:
        img.putpixel((x, y), c)

def vary(c, amt=3):
    return sh(c, rnd.randint(-amt, amt))

def face(x0, y0, w, h, fn, noise=3):
    """Pinta uma face (w x h) a partir de fn(col, row) -> cor|None."""
    for r in range(h):
        for c in range(w):
            col = fn(c, r)
            if col is not None:
                px(x0 + c, y0 + r, vary(col, noise) if noise else col)

def solid(col):
    return lambda c, r: col

# ====================================================================== CABEÇA (camada base)
# UV: topo (8,0) base (16,0) direita (0,8) frente (8,8) esquerda (16,8) trás (24,8), cada uma 8x8
face(8, 0, 8, 8, solid(HAIR), 3)                     # topo (coberto pelo chapéu)
face(16, 0, 8, 8, solid(SKS), 2)

def front(c, r):
    if r <= 2:                                       # franja
        if r == 2 and c in (3, 4):
            return SKSH
        return HAIRD if (r == 2 and c in (0, 7)) else (HAIRL if (c * 3 + r) % 7 == 0 else HAIR)
    if r == 3:                                       # sobrancelhas e sombra do chapéu
        if c in (0, 7):
            return HAIR
        if c in (1, 2, 5, 6):
            return HAIRX
        return SKSH
    if r == 4:                                       # olhos vermelhos
        if c in (0, 7):
            return HAIRD
        if c in (1, 6):
            return EYED
        if c in (2, 5):
            return EYE
        return SKSH
    if r == 5:                                       # pálpebra inferior / bochecha
        if c in (0, 7):
            return HAIR
        if c in (1, 6):
            return SKS
        if c in (2, 5):
            return EYEL if False else SK
        return SK
    if r == 6:                                       # nariz
        if c in (0, 7):
            return HAIRD
        if c == 3:
            return SKS
        if c == 4:
            return SKH
        return SK
    # r == 7: sorriso de canto
    if c in (0, 7):
        return HAIRX
    if c == 2:
        return SKS
    if c in (3, 4, 5):
        return C(178, 98, 82)
    if c == 6:
        return SKS
    return SK
face(8, 8, 8, 8, front, 1)

def side(left):
    def f(c, r):
        # c=0 é a borda de trás para a direita; ajusta para esquerda
        cc = c if left else 7 - c
        if r <= 2:
            return HAIR if (c + r) % 4 else HAIRD
        if r in (3, 4):
            return HAIR if cc >= 3 else (HAIRD if r == 4 else SKSH)
        if r in (5, 6):
            return HAIR if cc >= 4 else SK
        return HAIRD if cc >= 5 else SKS
    return f
face(0, 8, 8, 8, side(False), 2)                     # direita (do personagem)
face(16, 8, 8, 8, side(True), 2)                     # esquerda

def back(c, r):
    if r == 7 and c % 2 == 0:
        return None if False else HAIRX
    return HAIRD if (c * 5 + r * 3) % 9 == 0 else (HAIRL if (c * 3 + r) % 8 == 0 else HAIR)
face(24, 8, 8, 8, back, 3)

# ====================================================================== CABEÇA (camada externa): CHAPÉU E CABELO EM RELEVO
# UV externa = base + 32 em x: topo (40,0) base (48,0) direita (32,8) frente (40,8) esquerda (48,8) trás (56,8)
face(40, 0, 8, 8, lambda c, r: HATL if (c in (3, 4) and r in (3, 4)) else (HATM if (c in (0, 7) or r in (0, 7)) else HAT), 2)   # copa
def hat_ring(c, r, left=None):
    if r <= 1:
        return HAT if r == 0 else HATM
    if r == 2:                                       # fita
        return BANDL if c == 2 else (BANDD if c in (5, 7) else BAND)
    if r == 3:                                       # aba
        return HATL if c % 3 == 0 else HAT
    return None
face(40, 8, 8, 8, lambda c, r: hat_ring(c, r), 2)    # frente
def hat_side(c, r):
    base = hat_ring(c, r)
    return base
face(32, 8, 8, 8, hat_side, 2)                       # direita
face(48, 8, 8, 8, hat_side, 2)                       # esquerda (recebe o medalhão)
face(56, 8, 8, 8, lambda c, r: hat_ring(c, r), 2)    # trás
# medalhão de bronze na lateral esquerda do chapéu (relevo na camada externa)
for (cx, cy, col) in [(2, 1, BRL), (3, 1, BR), (4, 1, BRD), (2, 2, BR), (3, 2, BRL), (4, 2, BRD), (2, 3, BRD), (3, 3, BR), (4, 3, BRD)]:
    px(48 + cx, 8 + cy, col)
# mechas de cabelo em relevo saindo por baixo do chapéu (camada externa) – lados e nuca
def tufts(c, r):
    if r < 4:
        return None
    if r == 4 and c % 2 == 0:
        return HAIRL
    if r == 5 and c % 3 != 1:
        return HAIR
    if r == 6 and c % 3 == 0:
        return HAIRD
    if r == 7 and c in (1, 4, 6):
        return HAIRX
    return None
face(32, 8, 8, 8, lambda c, r: hat_side(c, r) or (tufts(c, r) if c > 2 else None), 3)
face(48, 8, 8, 8, lambda c, r: (hat_side(c, r) if r < 4 else (tufts(c, r) if c > 2 else None)), 3)
for (cx, cy, col) in [(2, 1, BRL), (3, 1, BR), (4, 1, BRD), (2, 2, BR), (3, 2, BRL), (4, 2, BRD), (2, 3, BRD), (3, 3, BR), (4, 3, BRD)]:
    px(48 + cx, 8 + cy, col)
face(56, 8, 8, 8, lambda c, r: hat_ring(c, r) if r < 4 else (HAIR if (c + r) % 3 else HAIRD), 3)
# mechas da franja caindo sobre os olhos (frente, camada externa)
for (c, r, col) in [(0, 4, HAIRL), (0, 5, HAIR), (0, 6, HAIRD), (1, 6, HAIR), (7, 4, HAIR), (7, 5, HAIRL), (7, 6, HAIRD), (6, 6, HAIR)]:
    px(40 + c, 8 + r, col)

# ====================================================================== TRONCO
# base: topo (20,16) fundo (28,16) direita (16,20) frente (20,20) esquerda (28,20) trás (32,20)
face(20, 16, 8, 4, solid(SUIT), 3)
face(28, 16, 8, 4, solid(SUITD), 2)
def torso_front(c, r):
    x = c
    # camisa e colarinho
    if r == 0:
        return SHIRT if 2 <= x <= 5 else SUIT
    if r == 1:
        if x in (3, 4):
            return TIED
        return SHIRT if x in (2, 5) else SUIT
    if r == 2:
        if x in (3, 4):
            return TIE if x == 3 else TIEL
        return SHIRTS if x in (2, 5) else SUIT
    if r in (3, 4, 5, 6):
        if x in (3, 4):
            return TIE if x == 3 else TIED
        return VEST if 2 <= x <= 5 else SUIT
    if r == 7:
        if x in (3, 4):
            return TIED if x == 3 else TIE
        return VEST if 2 <= x <= 5 else SUIT
    if r in (8, 10):
        if x in (3, 4):
            return VESTB
        return VEST if 2 <= x <= 5 else SUIT
    if r in (9, 11):
        return SHOED if False else (VEST if 2 <= x <= 5 else SUIT)
    return SUIT
face(20, 20, 8, 12, torso_front, 2)
def torso_back(c, r):
    if c in (3, 4):
        return SUITD
    return SUITL if (c + r) % 7 == 0 else SUIT
face(32, 20, 8, 12, torso_back, 3)
face(16, 20, 4, 12, lambda c, r: SUITL if r == 11 else SUIT, 3)
face(28, 20, 4, 12, lambda c, r: SUITL if r == 11 else SUIT, 3)

# camada externa do tronco (paletó): topo (20,32) fundo (28,32) direita (16,36) frente (20,36) esquerda (28,36) trás (32,36)
face(20, 32, 8, 4, solid(SUITL), 2)
def jacket_front(c, r):
    if c in (0, 1, 6, 7):
        if r == 11:
            return SUITD
        if c in (1, 6) and r <= 6:
            return SUITL                             # lapela
        if r in (8, 9) and c in (0, 7):
            return SUITD if r == 8 else SUITL       # bolsos com aba
        return SUIT
    if c in (2, 5) and r <= 3:
        return SUITL                                 # lapela
    if c in (2, 5) and r == 4:
        return SUITD
    return None
face(20, 36, 8, 12, jacket_front, 2)
face(32, 36, 8, 12, lambda c, r: (SUITD if c in (3, 4) and r >= 6 else (SUITL if (c * r) % 9 == 5 else SUIT)), 2)
face(16, 36, 4, 12, lambda c, r: SUITD if r == 11 else SUIT, 2)
face(28, 36, 4, 12, lambda c, r: SUITD if r == 11 else SUIT, 2)

# ====================================================================== BRAÇOS (4px)
# braço direito base: topo (44,16) fundo (48,16) externa (40,20) frente (44,20) interna (48,20) trás (52,20)
def arm_front(c, r):
    if r <= 8:
        return SUITL if c == 0 else SUIT
    if r == 9:
        return SHIRT
    return SK if r == 10 else SKS
face(44, 16, 4, 4, solid(SUIT), 2); face(48, 16, 4, 4, solid(SK), 2)
for ux in (40, 44, 48, 52):
    face(ux, 20, 4, 12, arm_front, 2)
# manga externa direita: (40,36) externa, (44,36) frente, (48,36) interna, (52,36) trás ; topo (44,32), fundo (48,32)
def sleeve(c, r):
    if r <= 8:
        return SUITL if (c + r) % 6 == 0 else SUIT
    if r == 9:
        return SUITD
    return None
face(44, 32, 4, 4, solid(SUITL), 2)
for ux in (40, 44, 48, 52):
    face(ux, 36, 4, 12, sleeve, 2)
# braço esquerdo base: topo (36,48) fundo (40,48) interna (32,52) frente (36,52) externa (40,52) trás (44,52)
face(36, 48, 4, 4, solid(SUIT), 2); face(40, 48, 4, 4, solid(SK), 2)
for ux in (32, 36, 40, 44):
    face(ux, 52, 4, 12, arm_front, 2)
# manga esquerda: topo (52,48) fundo (56,48) interna (48,52) frente (52,52) externa (56,52) trás (60,52)
face(52, 48, 4, 4, solid(SUITL), 2)
for ux in (48, 52, 56, 60):
    face(ux, 52, 4, 12, sleeve, 2)

# ====================================================================== PERNAS
def leg(c, r, crease_col):
    if r <= 8:
        return PANTL if c == crease_col else PANT
    if r == 9:
        return SHOEL if c in (1, 2) else SHOE
    if r == 10:
        return SHOE
    return SOLE
# perna direita: topo (4,16) fundo (8,16) externa (0,20) frente (4,20) interna (8,20) trás (12,20)
face(4, 16, 4, 4, solid(PANT), 2); face(8, 16, 4, 4, solid(SOLE), 1)
face(0, 20, 4, 12, lambda c, r: leg(c, r, 9), 2)
face(4, 20, 4, 12, lambda c, r: leg(c, r, 1), 2)
face(8, 20, 4, 12, lambda c, r: leg(c, r, 9), 2)
face(12, 20, 4, 12, lambda c, r: leg(c, r, 9), 2)
# calça externa direita (camada): topo (4,32) fundo (8,32) lados (0,36) (4,36) (8,36) (12,36)
def pants(c, r, crease):
    if r <= 8:
        return PANTL if c == crease else PANT
    return None
face(4, 32, 4, 4, solid(PANT), 1)
face(0, 36, 4, 12, lambda c, r: pants(c, r, 9), 2)
face(4, 36, 4, 12, lambda c, r: pants(c, r, 1), 2)
face(8, 36, 4, 12, lambda c, r: pants(c, r, 9), 2)
face(12, 36, 4, 12, lambda c, r: pants(c, r, 9), 2)
# perna esquerda base: topo (20,48) fundo (24,48) externa (16,52) frente (20,52) interna (24,52) trás (28,52)
face(20, 48, 4, 4, solid(PANT), 2); face(24, 48, 4, 4, solid(SOLE), 1)
face(16, 52, 4, 12, lambda c, r: leg(c, r, 9), 2)
face(20, 52, 4, 12, lambda c, r: leg(c, r, 2), 2)
face(24, 52, 4, 12, lambda c, r: leg(c, r, 9), 2)
face(28, 52, 4, 12, lambda c, r: leg(c, r, 9), 2)
# calça externa esquerda: topo (4,48) fundo (8,48) lados (0,52) (4,52) (8,52) (12,52)
face(4, 48, 4, 4, solid(PANT), 1)
face(0, 52, 4, 12, lambda c, r: pants(c, r, 9), 2)
face(4, 52, 4, 12, lambda c, r: pants(c, r, 2), 2)
face(8, 52, 4, 12, lambda c, r: pants(c, r, 9), 2)
face(12, 52, 4, 12, lambda c, r: pants(c, r, 9), 2)

img.save(os.path.join(OUT, "razor_kitsuji_skin.png"))

# ====================================================================== PRÉ-VISUALIZAÇÃO (frente, costas, esquerda), camadas base + externa
def comp(x, y, w, h, ox, oy, canvas):
    """cola o retângulo (x,y,w,h) da skin: base e depois a camada externa deslocada (+32 x p/ cabeça, +0/+16 y p/ corpo)"""
    pass

def view(kind):
    S = 12
    W, H = 24 * S, 36 * S
    cv = Image.new("RGBA", (W, H), (236, 168, 80, 255))
    def put(sx, sy, bx, by, w, h, outer=None, flip=False):
        for j in range(h):
            for i in range(w):
                ii = (w - 1 - i) if flip else i
                c = img.getpixel((bx + ii, by + j))
                if outer is not None:
                    o = img.getpixel((outer[0] + ii, outer[1] + j))
                    if o[3] > 0:
                        c = o
                if c[3] > 0:
                    for dy in range(S):
                        for dx in range(S):
                            cv.putpixel(((sx + i) * S + dx, (sy + j) * S + dy), c)
    if kind == "frente":
        put(8, 0, 8, 8, 8, 8, (40, 8))            # cabeça
        put(8, 8, 20, 20, 8, 12, (20, 36))        # tronco
        put(4, 8, 44, 20, 4, 12, (44, 36), False)  # braço direito (à esquerda na tela)
        put(16, 8, 36, 52, 4, 12, (52, 52), False)  # braço esquerdo
        put(8, 20, 4, 20, 4, 12, (4, 36))
        put(12, 20, 20, 52, 4, 12, (4, 52))
    elif kind == "costas":
        put(8, 0, 24, 8, 8, 8, (56, 8))
        put(8, 8, 32, 20, 8, 12, (32, 36))
        put(16, 8, 52, 20, 4, 12, (52, 36))
        put(4, 8, 44, 52, 4, 12, (60, 52))
        put(12, 20, 12, 20, 4, 12, (12, 36))
        put(8, 20, 28, 52, 4, 12, (12, 52))
    else:
        put(10, 0, 16, 8, 8, 8, (48, 8))
        put(10, 8, 28, 20, 4, 12, (28, 36))
        put(10, 20, 8, 20, 4, 12, (8, 36))
    return cv

for k in ("frente", "costas", "lado"):
    view(k).save(os.path.join(OUT, f"preview_{k}.png"))
print("skin gerada")
