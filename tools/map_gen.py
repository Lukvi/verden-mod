#!/usr/bin/env python3
"""Converte o mapa de Verden (tools/verden_map.jpg) em dados de geração de mundo.
Saída: src/main/resources/data/verden/map/verden_map.bin e sites.json (+ pré-visualizações em tools/preview).
Escala: 1 pixel do mapa = 8 blocos. Origem do mundo (0,0) = pixel ORIGIN_PX (Floresta Grøn)."""
import json, os, struct, sys
import numpy as np
from PIL import Image, ImageDraw
from scipy import ndimage as ndi

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.join(HERE, "..")
SRC = os.path.join(HERE, "verden_map.jpg")
OUT = os.path.join(ROOT, "src/main/resources/data/verden/map")
PREV = os.path.join(HERE, "preview")
os.makedirs(OUT, exist_ok=True); os.makedirs(PREV, exist_ok=True)

W, H = 1024, 768
SCALE = 8                     # blocos por pixel
ORIGIN = (235, 478)           # pixel do mapa que vira o bloco (0,0)
SEA = 63

# ids de região (a ordem DEVE bater com Region.java e com o world_preset)
BIOMES = ["ocean", "deep_ocean", "beach", "snowy_plains", "frozen_peaks", "taiga", "dark_forest", "forest", "plains",
          "swamp", "desert", "badlands", "jungle", "savanna", "snowy_taiga", "sunflower_plains", "river", "frozen_river",
          "stony_shore", "snowy_beach", "cherry_grove", "stony_peaks", "cold_ocean", "deep_cold_ocean", "warm_ocean"]
B = {n: i for i, n in enumerate(BIOMES)}

img = Image.open(SRC).convert("RGB")
a = np.array(img).astype(float)
r, g, b = a[..., 0], a[..., 1], a[..., 2]

# ------------------------------------------------------------------ máscara de terra
sea = (g - r >= 14) & (g >= 60)
land = ~sea
land = ndi.median_filter(land.astype(np.uint8), size=5).astype(bool)
land = ndi.binary_opening(land, iterations=2)
land = ndi.binary_closing(land, iterations=4)
land[515:706, 838:929] = False          # rosa dos ventos
land[185:215, 150:237] = False          # texto "Pantano Palufor"
land[84:97, 140:186] = False            # texto "Caverna Frajorn"
land[222:268, 862:906] = False          # texto "Mar Hojre"
land[585:645, 680:860] = False          # dragão do mapa
land[212:272, 856:870] = False
land[180:224, 395:578] = True           # baía de Veroogh preenchida
land[420:470, 100:141] = False          # texto "Templo Casthiel" ligando ilhas
lab, n = ndi.label(land)
DROP = [(950, 60, 1000, 100), (30, 140, 70, 180), (900, 170, 945, 210), (30, 185, 90, 205), (180, 95, 250, 125),
        (755, 235, 780, 270), (777, 239, 843, 268), (745, 275, 780, 310), (95, 300, 165, 355), (900, 380, 935, 420),
        (10, 565, 110, 600), (115, 565, 240, 600), (925, 595, 990, 625), (795, 66, 828, 94)]
keep = np.zeros_like(land)
for i in range(1, n + 1):
    m = lab == i
    area = m.sum()
    if area < 250:
        continue
    cy, cx = ndi.center_of_mass(m)
    if any(x0 <= cx <= x1 and y0 <= cy <= y1 for x0, y0, x1, y1 in DROP) and area < 20000:
        continue
    keep |= m
land = keep
yy, xx = np.mgrid[0:H, 0:W]
land |= ((xx - 122) / 24.0) ** 2 + ((yy - 172) / 30.0) ** 2 <= 1    # ilha de Vesten Hayvmand
land[:5, :] = False; land[-5:, :] = False; land[:, :5] = False; land[:, -5:] = False
land = ndi.binary_fill_holes(land)
landf = ndi.gaussian_filter(land.astype(float), 1.2)
land = landf > 0.5

# ------------------------------------------------------------------ classes de bioma
cls = np.full((H, W), B["plains"], dtype=np.uint8)


def poly(points, value, mask=None):
    im = Image.new("L", (W, H), 0)
    ImageDraw.Draw(im).polygon(points, fill=1)
    m = np.array(im).astype(bool)
    if mask is not None:
        m &= mask
    cls[m & land] = value
    return m


# Koold: neve (cor) acima da linha y~235
mx, mn = a.max(axis=2), a.min(axis=2)
snow = (mn > 138) & (mx - mn < 40)
snow = ndi.binary_closing(snow, iterations=3)
snow = ndi.binary_opening(snow, iterations=1)
region_north = (yy < 232) & (xx > 235) & (xx < 800)
snowm = ndi.binary_fill_holes(ndi.binary_closing(snow & region_north, iterations=6))
snowm = ndi.gaussian_filter(snowm.astype(float), 2.0) > 0.45
cls[land & region_north & snowm] = B["snowy_plains"]
cls[land & region_north & (yy < 200)] = B["snowy_plains"]           # resto da península
# faixa sul de Koold fica como planície fria
poly([(238, 150), (300, 150), (560, 210), (560, 120), (790, 110), (790, 215), (240, 240)], B["snowy_plains"])
# Montainezz (cordilheira)
mont = [(285, 95), (295, 55), (335, 25), (360, 5), (430, 2), (470, 8), (520, 20), (545, 45), (540, 75), (515, 95),
        (470, 115), (430, 135), (385, 140), (340, 150), (300, 150), (285, 125)]
mount_mask = poly(mont, B["frozen_peaks"])
poly([(268, 100), (330, 118), (345, 150), (300, 165), (262, 150)], B["cherry_grove"])     # ipês rosas de Montainezz
# florestas e pântanos
poly([(305, 225), (330, 215), (420, 215), (480, 225), (520, 235), (500, 255), (430, 258), (350, 255), (310, 245)], B["taiga"])
poly([(505, 225), (545, 215), (595, 225), (610, 250), (590, 275), (545, 280), (510, 270)], B["taiga"])
poly([(365, 300), (420, 290), (480, 300), (510, 320), (520, 350), (515, 385), (470, 395), (420, 385), (380, 370), (360, 335)], B["dark_forest"])
poly([(205, 440), (255, 430), (290, 445), (295, 470), (260, 485), (215, 480), (200, 460)], B["forest"])
poly([(205, 185), (250, 175), (295, 190), (300, 225), (255, 235), (215, 215)], B["swamp"])
poly([(345, 285), (430, 282), (480, 300), (440, 312), (380, 312)], B["swamp"])
poly([(355, 430), (400, 425), (455, 435), (450, 455), (400, 462), (360, 455)], B["swamp"])
poly([(390, 552), (440, 548), (520, 555), (570, 570), (540, 585), (470, 590), (410, 580)], B["swamp"])
poly([(165, 478), (240, 482), (245, 505), (190, 508), (165, 498)], B["sunflower_plains"])
poly([(270, 405), (400, 410), (395, 428), (275, 425)], B["sunflower_plains"])
# Yettus: laranja
orange = (r - g >= 28) & (r >= 135) & (b < 110) & (xx > 540) & (yy > 330)
orange = ndi.binary_closing(orange, iterations=4)
orange = ndi.median_filter(orange.astype(np.uint8), size=9).astype(bool)
orange = ndi.binary_fill_holes(orange)
cls[land & orange] = B["desert"]
poly([(640, 335), (832, 335), (844, 520), (700, 592), (560, 590), (556, 400), (640, 400)], B["desert"])
# manchas de rochedos (badlands) nos desertos, por ruído de baixa frequência
rng = np.random.default_rng(11)
noise = ndi.gaussian_filter(rng.normal(size=(H, W)), 14)
noise = (noise - noise.mean()) / noise.std()
cls[(cls == B["desert"]) & (noise > 1.25)] = B["badlands"]
# Karamarraw (ilha sul)
south = np.zeros_like(land)
south[620:770, 205:830] = True
cls[land & south] = B["jungle"]
# ilhas
isl = {"lolity": ((47, 38, 179, 114), "snowy_taiga"), "enseadas": ((757, 16, 858, 71), "snowy_taiga"),
       "xasof": ((34, 210, 138, 305), "forest"), "barvyl": ((10, 340, 110, 450), "plains"),
       "ost": ((852, 52, 905, 108), "stony_shore"), "landy": ((940, 128, 1020, 182), "forest"),
       "cornoy": ((800, 170, 880, 240), "plains"), "maariz": ((880, 270, 1024, 380), "savanna"),
       "runes": ((932, 418, 1012, 479), "stony_shore"), "bellus": ((934, 522, 988, 584), "plains"),
       "motry": ((42, 629, 114, 699), "forest"), "fim": ((921, 678, 983, 746), "stony_shore"),
       "juttor": ((647, 192, 728, 299), "taiga"), "vesten": ((95, 140, 150, 205), "snowy_taiga")}
for k, ((x0, y0, x1, y1), bn) in isl.items():
    sub = np.zeros_like(land); sub[y0:y1, x0:x1] = True
    cls[land & sub & (cls != B["jungle"])] = B[bn]
# Casthiel: ilha do templo
sub = np.zeros_like(land); sub[395:440, 55:115] = True
cls[land & sub] = B["savanna"]
# suavizar fronteiras (maioria)
cls = ndi.generic_filter(cls, lambda v: np.bincount(v.astype(int)).argmax(), size=5, mode="nearest").astype(np.uint8) \
    if "--slow" in sys.argv else cls

# ------------------------------------------------------------------ distâncias
d_land = ndi.distance_transform_edt(land)           # px dentro da terra
d_sea = ndi.distance_transform_edt(~land)           # px no mar

# ------------------------------------------------------------------ alturas
base = {"snowy_plains": 70, "taiga": 71, "dark_forest": 70, "forest": 69, "plains": 67, "swamp": 63.8, "desert": 69,
        "badlands": 74, "jungle": 76, "savanna": 67, "snowy_taiga": 72, "sunflower_plains": 67, "stony_shore": 68,
        "cherry_grove": 82, "frozen_peaks": 80, "beach": 64, "snowy_beach": 64}
Hf = np.full((H, W), SEA, dtype=float)
for name, v in base.items():
    Hf[cls == B[name]] = v
Hf = ndi.gaussian_filter(Hf, 2.2)
# montanhas: altura cresce com a distância à borda
mdist = ndi.distance_transform_edt(mount_mask)
mm = np.clip(mdist / 38.0, 0, 1)
Hf = np.where(mount_mask & land, 80 + 128 * mm ** 0.85, Hf)
Hf = ndi.gaussian_filter(Hf, 1.3)
# serra de Karamarraw
ridge = Image.new("L", (W, H), 0)
ImageDraw.Draw(ridge).line([(250, 668), (330, 690), (420, 672), (520, 690), (610, 680), (700, 668), (790, 690)], fill=255, width=9)
ridgem = ndi.gaussian_filter(np.array(ridge).astype(float) / 255, 7)
Hf = Hf + np.where(south & land, ridgem * 90, 0)
# Yettus: dunas largas
Hf = Hf + np.where((cls == B["desert"]) | (cls == B["badlands"]), np.clip(noise, -1.5, 2.5) * 3.0 + 2, 0)
# rampa costeira: praia a 63..65 junto ao mar
ramp = np.clip(d_land / 4.0, 0, 1)
ramp = ramp * ramp * (3 - 2 * ramp)
Hland = SEA + 0.8 + (Hf - SEA - 0.8) * np.where(mount_mask, np.clip(d_land / 8.0, 0, 1), ramp)
Hland = np.maximum(Hland, SEA + 0.6)
# oceano
floor = 62 - np.minimum(46, 2.6 * d_sea)
Hmap = np.where(land, Hland, floor)

# ------------------------------------------------------------------ rios e lagos
def px_line(pts, width_px):
    im = Image.new("L", (W, H), 0)
    ImageDraw.Draw(im).line(pts, fill=255, width=1)
    return np.array(im) > 0


RIVERS = {   # nome: (pontos, meia-largura em blocos)
    "mar": ([(545, 205), (575, 222), (560, 250), (548, 285), (545, 320), (550, 360), (555, 395)], 9),
    "udrio": ([(440, 312), (480, 322), (520, 350), (548, 380), (555, 395)], 7),
    "real": ([(232, 368), (310, 360), (365, 372), (420, 388), (480, 398), (555, 397)], 10),
    "kejne": ([(555, 397), (615, 400), (650, 410), (690, 430), (740, 455), (775, 465), (800, 480), (830, 508)], 11),
    "maariz": ([(555, 397), (552, 430), (548, 470), (542, 505), (540, 540), (548, 575)], 8),
    "oesn": ([(440, 405), (400, 440), (380, 470), (380, 500), (385, 530), (395, 560)], 7),
}
LAKES = {"ged": (336, 510, 7, 7), "ois": (716, 108, 22, 12), "lago_koold2": (650, 130, 7, 5)}
rd = np.full((H, W), 255.0)       # distância (px) ao rio mais próximo (efetiva, em blocos depois)
rw = np.zeros((H, W))
for name, (pts, hw) in RIVERS.items():
    m = px_line(pts, 1)
    m = ndi.binary_dilation(m, iterations=1)
    dd = ndi.distance_transform_edt(~m) * SCALE
    upd = dd < rd * 1.0
    rd = np.where(upd, dd, rd)
    rw = np.where(upd, hw, rw)
for name, (cx, cy, rx, ry) in LAKES.items():
    d = np.sqrt(((xx - cx) / rx) ** 2 + ((yy - cy) / ry) ** 2)       # 1 = borda
    dd = np.maximum(0, (d - 1) * min(rx, ry) * SCALE)               # blocos fora da borda
    upd = dd < rd
    rd = np.where(upd, dd, rd)
    rw = np.where(upd, 2.0, rw)          # meia-largura 2 => água dentro da elipse (+2 blocos)
# alturas sob rios: suavizar o corredor e manter ≥ 64
corr = (rd < 34) & land
Hs = ndi.gaussian_filter(Hmap, 4.0)
Hmap = np.where(corr, np.maximum(Hs * 0.8 + Hmap * 0.2, SEA + 1.5), Hmap)

# ------------------------------------------------------------------ cidades e marcos (fonte única: sites.json)
SITES = [
    # id, nome, tipo, px, py, raio(blocos)
    ("veroogh", "Veroogh", "capital_norte", 415, 198, 104),
    ("nagdaar", "Nagdaar", "capital_oeste", 268, 300, 100),
    ("jord", "Jørd", "capital_sul", 440, 478, 100),
    ("flerin", "Flerin", "capital_leste", 715, 405, 110),
    ("maariz", "Maariz", "porto_pirata", 952, 318, 80),
    ("geerd", "Geerd", "vila_neve", 365, 186, 56),
    ("oerr", "Oerr", "vila_neve", 757, 162, 44),
    ("cambehy", "Cambehy", "vila_neve", 645, 46, 40),
    ("montainezz", "Montainezz", "mosteiro", 300, 142, 64),
    ("torre_merlim", "Torre de Merlim", "torre", 470, 102, 26),
    ("huguottir", "Huguottir", "vila_planicie", 262, 240, 48),
    ("suttbarth", "Suttbarth", "vila_floresta", 578, 236, 44),
    ("juttor", "Juttor", "vila_floresta", 684, 262, 40),
    ("dejury", "Dejury", "vila_planicie", 300, 387, 48),
    ("orse", "Orse", "vila_porto", 268, 526, 44),
    ("rodi", "Rodi", "vila_planicie", 580, 440, 44),
    ("hjam", "Hjam", "vila_deserto", 592, 518, 44),
    ("verfaes", "Verfaes", "vila_planicie", 606, 356, 44),
    ("vilaes_praianas", "Vilaes Praianas", "vila_deserto", 722, 350, 44),
    ("lolity", "Lolity", "vila_neve", 68, 78, 38),
    ("xasof", "Xasof", "vila_planicie", 68, 270, 36),
    ("barvyl", "Barvyl", "vila_porto", 50, 385, 34),
    ("casthiel", "Templo Casthiel", "templo_sol", 92, 412, 30),
    ("vesten", "Vesten Hayvmand", "cidade_torres", 122, 172, 34),
    ("ost_sirener", "Ost Sirener", "cidade_torres", 878, 76, 30),
    ("cornoy", "Cornoy", "vila_porto", 845, 205, 34),
    ("landy_srew", "Landy Srew", "vila_porto", 978, 155, 34),
    ("runes", "Runes", "ruinas", 975, 450, 30),
    ("bellus", "Bellus", "vila_porto", 958, 553, 30),
    ("motry_life", "Motry Life", "vila_porto", 75, 663, 30),
    ("fim_do_mundo", "Fim do Mundo", "ruinas", 950, 712, 26),
    ("caverna_gelada", "Caverna Gelada", "caverna", 575, 142, 16),
    ("caverna_frajorn", "Caverna Frajorn", "caverna", 110, 90, 16),
    ("caverna_orju", "Caverna Orju", "caverna", 385, 350, 16),
    ("karamarraw_ruinas", "Ruínas de Karamarraw", "ruinas", 525, 728, 36),
    ("karamarraw_acampamento", "Acampamento de Karamarraw", "ruinas", 690, 735, 28),
]
SPAWN_PX = ORIGIN
sites_json = []
Hflat = Hmap.copy()
for sid, name, kind, px, py, rad in SITES:
    rp = rad / SCALE
    lvl = float(np.clip(Hmap[int(py), int(px)], 66, 150)) if kind != "porto_pirata" else 66.0
    if kind in ("capital_norte", "capital_oeste", "capital_sul", "capital_leste", "porto_pirata", "cidade_torres", "templo_sol"):
        lvl = max(lvl, 66.0)
    # nivela sobre uma média do entorno, para não cavar muito
    sl = (slice(max(0, int(py - rp)), int(py + rp) + 1), slice(max(0, int(px - rp)), int(px + rp) + 1))
    cap = {"montainezz": 108, "torre_merlim": 125}.get(sid, 100)
    lvl = float(np.clip(np.median(Hmap[sl]), 66, cap))
    if kind == "templo_sol":
        lvl = max(lvl, 68.0)
    d = np.sqrt((xx - px) ** 2 + (yy - py) ** 2)
    t = np.clip((d - rp * 1.0) / (rp * 0.8 + 2), 0, 1)
    t = t * t * (3 - 2 * t)
    inside = (d < rp * 1.8 + 3) & land
    Hflat = np.where(inside, lvl * (1 - t) + Hflat * t, Hflat)
    rd = np.where(d < rp * 1.5 + 6, 255, rd)           # sem rios dentro das cidades
    sites_json.append({"id": sid, "name": name, "kind": kind, "x": int((px - ORIGIN[0]) * SCALE),
                       "z": int((py - ORIGIN[1]) * SCALE), "radius": rad, "level": int(round(lvl))})
Hmap = Hflat

# ------------------------------------------------------------------ classes de oceano e praias
Hb = np.clip(np.rint(Hmap), 4, 250).astype(np.uint8)
cls_out = cls.copy()
ocean_cls = np.where(yy < 250, B["cold_ocean"], np.where((yy > 560) | ((xx > 800) & (yy > 250)), B["warm_ocean"], B["ocean"]))
deep_cls = np.where(yy < 250, B["deep_cold_ocean"], B["deep_ocean"])
cls_out = np.where(land, cls_out, np.where(d_sea > 14, deep_cls, ocean_cls)).astype(np.uint8)
# praias: faixa de terra junto ao mar (exceto montanhas / pântanos / jangal denso)
beachm = land & (d_land <= 2.2) & ~mount_mask & (cls != B["swamp"]) & (cls != B["stony_shore"])
cls_out[beachm & (cls == B["snowy_plains"])] = B["snowy_beach"]
cls_out[beachm & ~(cls == B["snowy_plains"]) & (cls != B["desert"]) & (cls != B["badlands"])] = B["beach"]

# ------------------------------------------------------------------ binário
rd_b = np.clip(np.rint(rd / 2.0), 0, 255).astype(np.uint8)     # unidades de 2 blocos
rw_b = np.clip(np.rint(rw / 2.0 * 2), 0, 255).astype(np.uint8)   # meia-largura em unidades de 1 bloco
with open(os.path.join(OUT, "verden_map.bin"), "wb") as f:
    f.write(struct.pack(">4sHHHHH", b"VMAP", W, H, SCALE, ORIGIN[0], ORIGIN[1]))
    for plane in (Hb, cls_out, rd_b, rw_b):
        f.write(plane.tobytes())
json.dump({"scale": SCALE, "origin": list(ORIGIN), "sites": sites_json}, open(os.path.join(OUT, "sites.json"), "w"),
          ensure_ascii=False, indent=1)

# ------------------------------------------------------------------ pré-visualizações
pal = {"ocean": (40, 90, 130), "deep_ocean": (20, 50, 100), "beach": (230, 215, 150), "snowy_plains": (235, 240, 245),
       "frozen_peaks": (170, 190, 215), "taiga": (50, 95, 70), "dark_forest": (30, 60, 35), "forest": (60, 130, 60),
       "plains": (140, 180, 80), "swamp": (80, 100, 60), "desert": (225, 190, 110), "badlands": (190, 100, 60),
       "jungle": (30, 150, 60), "savanna": (180, 170, 70), "snowy_taiga": (170, 200, 200), "sunflower_plains": (200, 210, 60),
       "river": (60, 120, 200), "frozen_river": (150, 190, 230), "stony_shore": (130, 130, 130), "snowy_beach": (240, 235, 220),
       "cherry_grove": (240, 150, 190), "stony_peaks": (140, 120, 100), "cold_ocean": (45, 95, 150), "deep_cold_ocean": (25, 55, 110),
       "warm_ocean": (40, 150, 170)}
col = np.zeros((H, W, 3), dtype=np.uint8)
for nme, i in B.items():
    col[cls_out == i] = pal[nme]
shade = np.clip((Hmap - 50) / 150, 0, 1)
col = (col * (0.55 + 0.45 * shade[..., None])).astype(np.uint8)
riv = (rd < rw + 4) & land
col[riv] = (40, 110, 230)
pv = Image.fromarray(col)
dr = ImageDraw.Draw(pv)
for s in sites_json:
    px = s["x"] / SCALE + ORIGIN[0]; py = s["z"] / SCALE + ORIGIN[1]
    dr.ellipse([px - 4, py - 4, px + 4, py + 4], outline=(255, 0, 0))
dr.ellipse([ORIGIN[0] - 5, ORIGIN[1] - 5, ORIGIN[0] + 5, ORIGIN[1] + 5], outline=(255, 255, 0), width=2)
pv.save(os.path.join(PREV, "verden_mapa_gerado.png"))
print("mapa gerado:", W, H, "alvos:", len(sites_json), "terra %.1f%%" % (100 * land.mean()))
