#!/usr/bin/env python3
"""Gerador de modelos 3D e texturas das criaturas do Verden.

A partir de uma especificação única (partes, caixas, pivôs), este script produz:
  * o código Java dos modelos   -> src/client/java/com/verden/client/model/*Parts.java
  * as texturas (com UV exato)  -> assets/verden/textures/entity/*.png  (+ *_glow.png emissivas)
  * prévias de silhueta         -> tools/preview/*.png  (só para conferência)
Uso: python3 tools/model_gen.py
"""
import math, os, random
from PIL import Image, ImageDraw

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.normpath(os.path.join(HERE, ".."))
TEX_DIR = os.path.join(ROOT, "src/main/resources/assets/verden/textures/entity")
JAVA_DIR = os.path.join(ROOT, "src/client/java/com/verden/client/model")
PREVIEW_DIR = os.path.join(HERE, "preview")
for d in (TEX_DIR, JAVA_DIR, PREVIEW_DIR):
    os.makedirs(d, exist_ok=True)


# ------------------------------------------------------------------ especificação
def box(id, o, s, tag="fur", mirror_of=None):
    return dict(id=id, o=o, s=s, tag=tag, mirror_of=mirror_of)


def part(name, parent, pivot, rot=(0, 0, 0), boxes=()):
    return dict(name=name, parent=parent, pivot=pivot, rot=rot, boxes=list(boxes))


def mirror_part(p, name, parent, pivot, rot, id_map, dx):
    """Cria a versão espelhada (esquerda) de uma parte, reaproveitando a textura."""
    bs = []
    for b in p["boxes"]:
        ox, oy, oz = b["o"]
        bs.append(box(id_map(b["id"]), (-(ox + b["s"][0]) + 0.0, oy, oz), b["s"], b["tag"], mirror_of=b["id"]))
    return part(name, parent, pivot, rot, bs)


def lyc_spec(boss=False):
    P = []
    P.append(part("hips", None, (0, 9, 2), (0, 0, 0), [box("pelvis", (-4.5, -2, -3.5), (9, 8, 8))]))
    P.append(part("body", "hips", (0, -1, -1), (0.5, 0, 0), [
        box("abdomen", (-5, -6, -4), (10, 7, 8)),
        box("chest", (-7.5, -17, -5), (15, 12, 10))]))
    P.append(part("head", "body", (0, -15, -3), (-0.5, 0, 0), [
        box("skull", (-4, -4, -6), (8, 8, 7)),
        box("snout", (-2.5, -0.5, -12), (5, 3, 6), "fur"),
        box("ear_r", (-4.5, -7, -2), (2, 4, 2), "dark"),
        box("ear_l", (2.5, -7, -2), (2, 4, 2), "dark", mirror_of="ear_r")]))
    P.append(part("jaw", "head", (0, 2.5, -6), (0, 0, 0), [box("jawb", (-2, 0, -6), (4, 2, 6), "jaw")]))
    for side, sx in (("r", -1), ("l", 1)):
        m = side == "l"
        mo = (lambda i: "r_" + i[2:]) if m else None
        P.append(part(f"{side}_arm", "body", (sx * 9.5, -13, 0), (-0.6, 0, sx * 0.06), [
            box(f"{side}_upper", (-2.5, -1, -2.5), (5, 10, 5), mirror_of="r_upper" if m else None)]))
        P.append(part(f"{side}_forearm", f"{side}_arm", (0, 9, 0), (-0.3, 0, 0), [
            box(f"{side}_fore", (-2, 0, -2), (4, 10, 4), mirror_of="r_fore" if m else None)]))
        P.append(part(f"{side}_hand", f"{side}_forearm", (0, 10, 0), (0, 0, 0), [
            box(f"{side}_handb", (-2.5, 0, -2.5), (5, 3, 5), "dark", mirror_of="r_handb" if m else None),
            box(f"{side}_c1", (-2, 3, -3), (1, 3, 1), "bone", mirror_of="r_c1" if m else None),
            box(f"{side}_c2", (-0.5, 3, -3), (1, 3, 1), "bone", mirror_of="r_c2" if m else None),
            box(f"{side}_c3", (1, 3, -3), (1, 3, 1), "bone", mirror_of="r_c3" if m else None)]))
        P.append(part(f"{side}_leg", "hips", (sx * 3.5, 2, 1), (-0.5, 0, 0), [
            box(f"{side}_thigh", (-2.5, 0, -2.5), (5, 8, 5), mirror_of="r_thigh" if m else None)]))
        P.append(part(f"{side}_shin", f"{side}_leg", (0, 8, 0), (0.95, 0, 0), [
            box(f"{side}_shinb", (-2, 0, -2), (4, 7, 4), mirror_of="r_shinb" if m else None)]))
        P.append(part(f"{side}_foot", f"{side}_shin", (0, 7, 0), (-0.45, 0, 0), [
            box(f"{side}_footb", (-2, 0, -5), (4, 2, 6), "dark", mirror_of="r_footb" if m else None),
            box(f"{side}_toe", (-2, 0, -7), (4, 1, 2), "bone", mirror_of="r_toe" if m else None)]))
    P.append(part("tail", "hips", (0, -0.5, 4), (-0.35, 0, 0), [box("tailb", (-2, -1.5, 0), (4, 4, 11))]))
    if boss:
        P.append(part("core", "body", (0, -9, -5), (0, 0, 0), [box("coreb", (-3, -3, -1.5), (6, 6, 2), "glow")]))
        anchors = [(-4, -13, 5), (4, -13, 5), (-8, -9, 4), (8, -9, 4)]
        for n, (ax, ay, az) in enumerate(anchors):
            roll = 0.25 if ax < 0 else -0.25
            P.append(part(f"bt{n}a", "body", (ax, ay, az), (1.2, 0, roll), [box(f"bt{n}a_b", (-1.5, 0, -1.5), (3, 9, 3), "tent")]))
            P.append(part(f"bt{n}b", f"bt{n}a", (0, 9, 0), (0.35, 0, 0), [box(f"bt{n}b_b", (-1, 0, -1), (2, 8, 2), "tent")]))
            P.append(part(f"bt{n}c", f"bt{n}b", (0, 8, 0), (0.35, 0, 0), [box(f"bt{n}c_b", (-1, 0, -1), (2, 7, 2), "tentip")]))
        for n, ax in enumerate((-2, 2)):
            P.append(part(f"jt{n}a", "head", (ax, 1, -10), (0.1, 0, 0), [box(f"jt{n}a_b", (-1, 0, -1), (2, 6, 2), "tent")]))
            P.append(part(f"jt{n}b", f"jt{n}a", (0, 6, 0), (0.2, 0, 0), [box(f"jt{n}b_b", (-1, 0, -1), (2, 5, 2), "tentip")]))
    return P


def rinoy_spec():
    P = []
    P.append(part("body", None, (0, 3, 0), (0, 0, 0), [box("torso", (-8, -8, -13), (16, 16, 26))]))
    P.append(part("hump", "body", (0, -8, -3), (0, 0, 0), [box("humpb", (-6, -5, -5), (12, 5, 10))]))
    P.append(part("head", "body", (0, -3, -12), (0.3, 0, 0), [
        box("skull", (-5, -5, -8), (10, 10, 9)),
        box("snout", (-4, 0, -16), (8, 5, 8)),
        box("beard", (-3, 5, -9), (6, 6, 4)),
        box("horn1", (-1.5, -5, -14), (3, 5, 3), "horn"),
        box("horn2", (-1, -3, -8), (2, 3, 2), "horn"),
        box("ear_r", (-8, -6, -2), (3, 4, 1), "dark"),
        box("ear_l", (5, -6, -2), (3, 4, 1), "dark", mirror_of="ear_r")]))
    legs = [("fr", -6.5, -8.5, None), ("fl", 6.5, -8.5, "fr"), ("br", -6.5, 8.5, None), ("bl", 6.5, 8.5, "br")]
    for n, x, z, m in legs:
        P.append(part(n, "body", (x, 8, z), (0, 0, 0), [
            box(f"{n}_leg", (-3, 0, -3), (6, 10, 6), mirror_of=(m + "_leg") if m else None),
            box(f"{n}_hoof", (-3, 10, -3), (6, 3, 6), "dark", mirror_of=(m + "_hoof") if m else None)]))
    P.append(part("tail", "body", (0, -4, 13), (0.5, 0, 0), [
        box("tailb", (-1, 0, -1), (2, 9, 2)), box("tuft", (-1.5, 8, -1.5), (3, 3, 3), "dark")]))
    return P


def condolyus_spec():
    P = []
    P.append(part("body", None, (0, 15, 0), (0, 0, 0), [box("bodyb", (-3, -3, -5), (6, 5, 10), "scale")]))
    P.append(part("head", "body", (0, -1, -5), (0.1, 0, 0), [
        box("skull", (-2.5, -2.5, -5), (5, 5, 5), "scale"),
        box("snout", (-1.5, 0, -8), (3, 2, 3), "scale"),
        box("ear_r", (-3.5, -5.5, -1), (1, 3, 2), "scale"),
        box("ear_l", (2.5, -5.5, -1), (1, 3, 2), "scale", mirror_of="ear_r")]))
    for n, x, z, m in [("fr", -2, -3.5, None), ("fl", 2, -3.5, "fr"), ("br", -2, 3.5, None), ("bl", 2, 3.5, "br")]:
        P.append(part(n, "body", (x, 2, z), (0, 0, 0), [
            box(f"{n}_b", (-1, 0, -1), (2, 7, 2), "scale", mirror_of=(m + "_b") if m else None)]))
    P.append(part("tail1", "body", (0, -1, 5), (-0.2, 0, 0), [box("tail1b", (-1.5, -1.5, 0), (3, 3, 5), "scale")]))
    P.append(part("tail2", "tail1", (0, 0, 5), (-0.2, 0, 0), [box("tail2b", (-1, -1, 0), (2, 2, 5), "scale")]))
    P.append(part("tail3", "tail2", (0, 0, 5), (-0.2, 0, 0), [box("tail3b", (-1, -1, 0), (2, 2, 2), "glow")]))
    for i, z in enumerate((-3.5, -0.5, 2.5)):
        P.append(part(f"plate{i}", "body", (0, -3, z), (0, 0, 0), [box(f"plate{i}b", (-0.5, -3, -1), (1, 3, 2), "glow")]))
    return P


def tentaculo_spec():
    P = []
    sizes = [(6, 6), (5, 6), (4, 6), (4, 6), (3, 6)]
    parent, py = None, 24
    for i, (w, h) in enumerate(sizes):
        ox = -w / 2
        tag = "tentip" if i == len(sizes) - 1 else "tent"
        P.append(part(f"s{i}", parent, (0, py, 0), (0, 0, 0), [box(f"s{i}b", (ox, -h, ox), (w, h, w), tag)]))
        parent, py = f"s{i}", -h
    return P


# ------------------------------------------------------------------ geometria
def rot_matrix(rx, ry, rz):
    cx, sx, cy, sy, cz, sz = math.cos(rx), math.sin(rx), math.cos(ry), math.sin(ry), math.cos(rz), math.sin(rz)
    Rx = [[1, 0, 0], [0, cx, -sx], [0, sx, cx]]
    Ry = [[cy, 0, sy], [0, 1, 0], [-sy, 0, cy]]
    Rz = [[cz, -sz, 0], [sz, cz, 0], [0, 0, 1]]
    return mm(Rx, mm(Ry, Rz))


def mm(a, b):
    return [[sum(a[i][k] * b[k][j] for k in range(3)) for j in range(3)] for i in range(3)]


def mv(m, v):
    return [sum(m[i][k] * v[k] for k in range(3)) for i in range(3)]


def world_corners(spec):
    """Retorna {part_name: (R, t)} e a lista de (part, box, cantos) no espaço do modelo."""
    xf, out = {}, []
    by_name = {p["name"]: p for p in spec}
    for p in spec:
        Rp, tp = ([[1, 0, 0], [0, 1, 0], [0, 0, 1]], [0, 0, 0]) if p["parent"] is None else xf[p["parent"]]
        R = mm(Rp, rot_matrix(*p["rot"]))
        t = [a + b for a, b in zip(mv(Rp, p["pivot"]), tp)]
        xf[p["name"]] = (R, t)
        for b in p["boxes"]:
            ox, oy, oz = b["o"]
            sx, sy, sz = b["s"]
            cs = []
            for dx in (0, sx):
                for dy in (0, sy):
                    for dz in (0, sz):
                        v = mv(R, (ox + dx, oy + dy, oz + dz))
                        cs.append([v[i] + t[i] for i in range(3)])
            out.append((p, b, cs))
    return xf, out


def ground_fix(spec):
    """Desloca as partes raiz para que o ponto mais baixo toque y=24. Retorna a altura em px."""
    _, out = world_corners(spec)
    max_y = max(c[1] for _, _, cs in out for c in cs)
    shift = 24 - max_y
    for p in spec:
        if p["parent"] is None:
            p["pivot"] = (p["pivot"][0], p["pivot"][1] + shift, p["pivot"][2])
    _, out = world_corners(spec)
    min_y = min(c[1] for _, _, cs in out for c in cs)
    return 24 - min_y


# ------------------------------------------------------------------ empacotamento UV
def pack(spec, W, H):
    uv, x, y, row = {}, 0, 0, 0
    boxes = {}
    for p in spec:
        for b in p["boxes"]:
            boxes[b["id"]] = b
    for p in spec:
        for b in p["boxes"]:
            if b["mirror_of"]:
                continue
            w, h, d = b["s"]
            nw, nh = 2 * d + 2 * w, d + h
            if x + nw > W:
                x, y, row = 0, y + row, 0
            if y + nh > H:
                raise SystemExit(f"textura {W}x{H} pequena demais para {b['id']}")
            uv[b["id"]] = (x, y)
            x += nw
            row = max(row, nh)
    for p in spec:
        for b in p["boxes"]:
            if b["mirror_of"]:
                uv[b["id"]] = uv[b["mirror_of"]]
    return uv, boxes


def face_rect(b, uv, face):
    u, v = uv[b["id"]]
    w, h, d = b["s"]
    return {"top": (u + d, v, w, d), "bottom": (u + d + w, v, w, d),
            "west": (u, v + d, d, h), "front": (u + d, v + d, w, h),
            "east": (u + d + w, v + d, d, h), "back": (u + 2 * d + w, v + d, w, h)}[face]


FACES = ("top", "bottom", "west", "front", "east", "back")


# ------------------------------------------------------------------ pintura
def tone(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c)


def paint_box(img, b, uv, base, rng, tag):
    for face in FACES:
        x0, y0, fw, fh = face_rect(b, uv, face)
        fm = {"top": 1.12, "bottom": 0.65, "front": 1.0, "back": 0.86, "east": 0.93, "west": 0.93}[face]
        for j in range(fh):
            grad = 1.0 - 0.2 * (j / max(1, fh - 1)) if face not in ("top", "bottom") else 1.0
            for i in range(fw):
                if tag in ("steel", "gold", "crimson"):
                    n = rng.uniform(0.94, 1.06)
                    if j == 0:
                        n *= 1.12
                elif tag in ("bone", "horn"):
                    n = rng.uniform(0.95, 1.05)
                    if tag == "horn" and j % 3 == 0:
                        n *= 0.82
                elif tag == "glow":
                    n = rng.uniform(0.85, 1.15)
                    fm_ = 1.0
                else:
                    n = rng.uniform(0.86, 1.14)
                    r = rng.random()
                    if r < 0.10:
                        n *= 0.74
                    elif r > 0.94:
                        n *= 1.22
                f = n * (1.0 if tag == "glow" else fm * grad)
                img.putpixel((x0 + i, y0 + j), tone(base, f) + (255,))


def put(img, rect, rx, ry, color):
    x0, y0, w, h = rect
    if 0 <= rx < w and 0 <= ry < h:
        img.putpixel((x0 + rx, y0 + ry), color if len(color) == 4 else color + (255,))


def lighten(img, rect, x1, y1, x2, y2, f):
    x0, y0, w, h = rect
    for j in range(max(0, y1), min(h, y2 + 1)):
        for i in range(max(0, x1), min(w, x2 + 1)):
            r, g, b, a = img.getpixel((x0 + i, y0 + j))
            img.putpixel((x0 + i, y0 + j), tone((r, g, b), f) + (a,))


def draw_spec(name, spec, W, H, palette, decorate, seed):
    rng = random.Random(seed)
    uv, boxes = pack(spec, W, H)
    img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    glow = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    done = set()
    for p in spec:
        for b in p["boxes"]:
            key = uv[b["id"]]
            if key in done:
                continue
            done.add(key)
            paint_box(img, b, uv, palette[b["tag"]], rng, b["tag"])
            if b["tag"] in ("glow", "tentip") and "glow_" + b["tag"] in palette:
                paint_box(glow, b, uv, palette["glow_" + b["tag"]], rng, "glow")
    R = lambda bid, face: face_rect(boxes[bid], uv, face)
    decorate(img, glow, R, rng)
    return img, glow, uv


# ------------------------------------------------------------------ decorações por criatura
def eyes(img, glow, rect, xs, y, col_glow, col_base=(25, 10, 10)):
    for x in xs:
        put(img, rect, x, y, col_base)
        put(glow, rect, x, y, col_glow)


def dec_lyc(color_glow, veins=False):
    def deco(img, glow, R, rng):
        sk = R("skull", "front")
        eyes(img, glow, sk, (1, 2, 5, 6), 3, color_glow)
        for x in (1, 2):
            put(img, sk, x, 2, (20, 15, 15))
        for x in (5, 6):
            put(img, sk, x, 2, (20, 15, 15))
        sn = R("snout", "front")
        for x in (1, 2, 3):
            put(img, sn, x, 0, (18, 18, 20))
        for x in range(5):
            put(img, sn, x, 2, (40, 15, 18))
        put(img, sn, 0, 2, (235, 232, 215))
        put(img, sn, 4, 2, (235, 232, 215))
        ch = R("chest", "front")
        lighten(img, ch, 4, 2, 10, 11, 1.28)
        if veins:
            for face_id in ("chest", "abdomen"):
                for face in ("front", "back"):
                    rect = R(face_id, face)
                    for _ in range(8):
                        x = rng.randrange(0, rect[2])
                        y = rng.randrange(0, max(1, rect[3] - 3))
                        for k in range(rng.randint(2, 4)):
                            put(img, rect, x, y + k, (150, 20, 25))
                            put(glow, rect, x, y + k, (255, 40, 40))
    return deco


def dec_boss(img, glow, R, rng):
    dec_lyc((255, 150, 40), veins=False)(img, glow, R, rng)
    core = R("coreb", "front")
    w, h = core[2], core[3]
    for j in range(h):
        for i in range(w):
            edge = i in (0, w - 1) or j in (0, h - 1)
            put(img, core, i, j, (255, 120, 40) if not edge else (170, 50, 20))
            put(glow, core, i, j, (255, 200, 90) if not edge else (255, 110, 30))
    ch = R("chest", "front")
    for _ in range(14):  # rachaduras brilhantes ao redor do núcleo
        x, y = rng.randrange(3, 12), rng.randrange(1, 11)
        for k in range(rng.randint(2, 4)):
            put(img, ch, x, y + k, (200, 70, 25))
            put(glow, ch, x, y + k, (255, 140, 40))
    for p in ("skull",):
        pass


def suckers(img, rect):
    x0, y0, w, h = rect
    for j in range(1, h, 2):
        for i in range(1 + (j // 2) % 2, w, 2):
            put(img, rect, i, j, (225, 160, 200))


def dec_tent(img, glow, R, rng, ids):
    for bid in ids:
        suckers(img, R(bid, "front"))


def dec_rinoy(img, glow, R, rng):
    for bid in ("torso", "humpb", "skull", "snout", "beard"):
        for face in FACES:
            rect = R(bid, face)
            for _ in range(int(rect[2] * rect[3] * 0.035)):
                put(img, rect, rng.randrange(rect[2]), rng.randrange(rect[3]), (210, 218, 228))
    lighten(img, R("torso", "bottom"), 0, 0, 40, 40, 1.4)
    sk = R("skull", "front")
    for x in (1, 8):
        put(img, sk, x, 4, (12, 8, 6))
        put(img, sk, x, 5, (12, 8, 6))
        put(img, sk, x, 4, (230, 230, 240))  # brilho no olho
    sn = R("snout", "front")
    put(img, sn, 1, 1, (15, 10, 10)); put(img, sn, 6, 1, (15, 10, 10))
    for x in range(8):
        put(img, sn, x, 4, (30, 20, 18))


def dec_condolyus(img, glow, R, rng):
    sk = R("skull", "front")
    for x in (1, 3):
        put(img, sk, x, 2, (255, 235, 120))
        put(glow, sk, x, 2, (255, 230, 90))
    lighten(img, R("bodyb", "bottom"), 0, 0, 20, 20, 1.5)
    for face in ("top", "west", "east"):
        rect = R("bodyb", face)
        for _ in range(4):  # rachaduras de magma
            x, y = rng.randrange(rect[2]), rng.randrange(rect[3])
            for k in range(rng.randint(2, 4)):
                xx = min(rect[2] - 1, x + k % 2)
                yy = min(rect[3] - 1, y + k // 2)
                put(img, rect, xx, yy, (255, 170, 50))
                put(glow, rect, xx, yy, (255, 150, 40))


# ------------------------------------------------------------------ saída Java
def fl(v):
    return f"{float(v)}f"


def emit_java(cls, spec, W, H, height_px):
    lines = []
    lines.append("// GERADO por tools/model_gen.py - nao edite a mao.")
    lines.append("package com.verden.client.model;\n")
    lines.append("import java.util.HashMap;\nimport java.util.Map;")
    lines.append("import net.minecraft.client.model.Dilation;\nimport net.minecraft.client.model.ModelData;")
    lines.append("import net.minecraft.client.model.ModelPart;\nimport net.minecraft.client.model.ModelPartBuilder;")
    lines.append("import net.minecraft.client.model.ModelPartData;\nimport net.minecraft.client.model.ModelTransform;")
    lines.append("import net.minecraft.client.model.TexturedModelData;\n")
    lines.append(f"public final class {cls} {{")
    lines.append(f"    public static final int TEX_W = {W};\n    public static final int TEX_H = {H};")
    lines.append(f"    public static final float HEIGHT_PX = {fl(height_px)};")
    lines.append("    private static final Map<String, String> PATHS = new HashMap<>();")
    lines.append(f"    private {cls}() {{}}\n")
    path = {}
    for p in spec:
        path[p["name"]] = p["name"] if p["parent"] is None else path[p["parent"]] + "/" + p["name"]
    lines.append("    static {")
    for n, pa in path.items():
        lines.append(f'        PATHS.put("{n}", "{pa}");')
    lines.append("    }\n")
    lines.append("    public static boolean has(String name) { return PATHS.containsKey(name); }\n")
    lines.append("    public static ModelPart find(ModelPart root, String name) {")
    lines.append("        ModelPart p = root;")
    lines.append('        for (String s : PATHS.get(name).split("/")) { p = p.getChild(s); }')
    lines.append("        return p;\n    }\n")
    lines.append("    public static TexturedModelData create() {")
    lines.append("        ModelData md = new ModelData();")
    lines.append("        ModelPartData root = md.getRoot();")
    uvs, _ = pack(spec, W, H)
    for p in spec:
        parent = "root" if p["parent"] is None else "p_" + p["parent"]
        bl = "ModelPartBuilder.create()"
        for b in p["boxes"]:
            u, v = uvs[b["id"]]
            mir = bool(b["mirror_of"])
            bl += f".uv({u}, {v})"
            if mir:
                bl += ".mirrored(true)"
            bl += f".cuboid({fl(b['o'][0])}, {fl(b['o'][1])}, {fl(b['o'][2])}, {fl(b['s'][0])}, {fl(b['s'][1])}, {fl(b['s'][2])})"
            if mir:
                bl += ".mirrored(false)"
        px, py, pz = p["pivot"]
        rx, ry, rz = p["rot"]
        lines.append(f'        ModelPartData p_{p["name"]} = {parent}.addChild("{p["name"]}", {bl}, '
                     f"ModelTransform.of({fl(px)}, {fl(py)}, {fl(pz)}, {fl(rx)}, {fl(ry)}, {fl(rz)}));")
    lines.append("        return TexturedModelData.of(md, TEX_W, TEX_H);\n    }\n}")
    with open(os.path.join(JAVA_DIR, cls + ".java"), "w", encoding="utf-8") as f:
        f.write("\n".join(lines) + "\n")


# ------------------------------------------------------------------ prévia de silhueta
def preview(name, spec, height_px):
    _, out = world_corners(spec)
    colors = {"fur": (120, 100, 90), "dark": (50, 45, 50), "bone": (230, 225, 200), "glow": (255, 170, 60),
              "tent": (140, 70, 150), "tentip": (200, 120, 220), "horn": (225, 216, 190), "scale": (210, 100, 40),
              "jaw": (90, 40, 45)}
    S = 8
    min_y = min(c[1] for _, _, cs in out for c in cs)
    sheet = Image.new("RGB", (2 * 50 * S, int((height_px + 8) * S)), (30, 30, 36))
    for view, off in (("side", 0), ("front", 50 * S)):
        d = ImageDraw.Draw(sheet)
        items = []
        for p, b, cs in out:
            depth = sum(c[0] if view == "side" else c[2] for c in cs) / 8
            pts = [((c[2] if view == "side" else c[0]) * S + off + 25 * S, (c[1] - min_y + 4) * S) for c in cs]
            items.append((depth, pts, colors.get(b["tag"], (150, 150, 150))))
        for _, pts, col in sorted(items, key=lambda t: t[0], reverse=(view == "side")):
            hull = convex_hull(pts)
            d.polygon(hull, fill=col, outline=(15, 15, 15))
    sheet.save(os.path.join(PREVIEW_DIR, name + ".png"))


def convex_hull(points):
    pts = sorted(set((round(x, 1), round(y, 1)) for x, y in points))
    if len(pts) <= 2:
        return pts

    def cross(o, a, b):
        return (a[0] - o[0]) * (b[1] - o[1]) - (a[1] - o[1]) * (b[0] - o[0])
    lo, up = [], []
    for p in pts:
        while len(lo) >= 2 and cross(lo[-2], lo[-1], p) <= 0:
            lo.pop()
        lo.append(p)
    for p in reversed(pts):
        while len(up) >= 2 and cross(up[-2], up[-1], p) <= 0:
            up.pop()
        up.append(p)
    return lo[:-1] + up[:-1]


# ------------------------------------------------------------------ construção
def save(name, img, glow=None):
    img.save(os.path.join(TEX_DIR, name + ".png"))
    if glow is not None:
        glow.save(os.path.join(TEX_DIR, name + "_glow.png"))


def build():
    report = {}

    # Licantropo comum e adaptado
    spec = lyc_spec()
    h = ground_fix(spec)
    pal = {"fur": (82, 66, 54), "dark": (40, 32, 30), "bone": (222, 214, 190), "jaw": (96, 40, 46)}
    img, glow, _ = draw_spec("licantropo", spec, 128, 128, pal, dec_lyc((255, 205, 50)), 11)
    save("licantropo", img, glow)
    pal2 = {"fur": (44, 38, 46), "dark": (22, 18, 24), "bone": (200, 196, 190), "jaw": (110, 28, 34)}
    img, glow, _ = draw_spec("licantropo_adaptado", spec, 128, 128, pal2, dec_lyc((255, 40, 40), veins=True), 12)
    save("licantropo_adaptado", img, glow)
    emit_java("LicantropoParts", spec, 128, 128, h)
    preview("licantropo", spec, h)
    report["LicantropoParts"] = h

    # Amálgama de Koold (chefe)
    spec = lyc_spec(boss=True)
    h = ground_fix(spec)
    pal = {"fur": (62, 30, 70), "dark": (30, 14, 36), "bone": (200, 190, 200), "jaw": (110, 30, 70),
           "glow": (255, 130, 40), "tent": (96, 36, 108), "tentip": (150, 60, 170),
           "glow_tentip": (200, 80, 240), "glow_glow": (255, 190, 80)}
    img, glow, _ = draw_spec("amalgama", spec, 128, 128, pal, dec_boss_wrap(spec), 13)
    save("amalgama_koold", img, glow)
    emit_java("AmalgamaParts", spec, 128, 128, h)
    preview("amalgama", spec, h)
    report["AmalgamaParts"] = h

    # Rinoy
    spec = rinoy_spec()
    h = ground_fix(spec)
    pal = {"fur": (52, 42, 36), "dark": (24, 20, 18), "horn": (226, 216, 192)}
    img, glow, _ = draw_spec("rinoy", spec, 128, 128, pal, dec_rinoy, 14)
    save("rinoy", img)
    emit_java("RinoyParts", spec, 128, 128, h)
    preview("rinoy", spec, h)
    report["RinoyParts"] = h

    # Condolyus
    spec = condolyus_spec()
    h = ground_fix(spec)
    pal = {"scale": (200, 92, 34), "glow": (255, 196, 70)}
    img, glow, _ = draw_spec("condolyus", spec, 64, 64, pal, dec_condolyus, 15)
    save("condolyus", img, glow)
    emit_java("CondolyusParts", spec, 64, 64, h)
    preview("condolyus", spec, h)
    report["CondolyusParts"] = h

    # Tentáculo
    spec = tentaculo_spec()
    h = ground_fix(spec)
    pal = {"tent": (98, 32, 110), "tentip": (140, 56, 150)}
    ids = [b["id"] for p in spec for b in p["boxes"]]
    img, glow, _ = draw_spec("tentaculo", spec, 64, 64, pal, lambda i, g, R, r: dec_tent(i, g, R, r, ids), 16)
    save("tentaculo", img)
    emit_java("TentaculoParts", spec, 64, 64, h)
    preview("tentaculo", spec, h)
    report["TentaculoParts"] = h
    return report


def dec_boss_wrap(spec):
    ids = [b["id"] for p in spec for b in p["boxes"] if b["tag"] in ("tent", "tentip")]

    def deco(img, glow, R, rng):
        dec_boss(img, glow, R, rng)
        dec_tent(img, glow, R, rng, ids)
    return deco


if __name__ == "__main__":
    for k, v in build().items():
        print(f"{k}: altura do modelo = {v:.1f} px")
