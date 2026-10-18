#!/usr/bin/env python3
"""Modelos humanoides próprios (Kunori, Legião, Zytra, Haigol) - usa o motor de model_gen.py."""
import os, sys
HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import model_gen as mg
from model_gen import box, part, put, lighten


def mirror(boxes):
    out = []
    for b in boxes:
        ox, oy, oz = b["o"]
        w, h, d = b["s"]
        out.append(box("l_" + b["id"][2:], (-(ox + w), oy, oz), b["s"], b["tag"], mirror_of=b["id"]))
    return out


def humanoid(head_tag, body_tag, head_boxes, body_boxes, arm_boxes, leg_boxes, extra=()):
    P = [part("head", None, (0, 0, 0), (0, 0, 0), [box("head_b", (-4, -8, -4), (8, 8, 8), head_tag)] + head_boxes),
         part("body", None, (0, 0, 0), (0, 0, 0), [box("body_b", (-4, 0, -2), (8, 12, 4), body_tag)] + body_boxes),
         part("r_arm", None, (-5, 2, 0), (0, 0, 0), arm_boxes),
         part("l_arm", None, (5, 2, 0), (0, 0, 0), mirror(arm_boxes)),
         part("r_leg", None, (-1.9, 12, 0), (0, 0, 0), leg_boxes),
         part("l_leg", None, (1.9, 12, 0), (0, 0, 0), mirror(leg_boxes))]
    P += list(extra)
    return P


def line(img, rect, a, b, color):
    n = max(abs(b[0] - a[0]), abs(b[1] - a[1]), 1)
    for i in range(n + 1):
        put(img, rect, round(a[0] + (b[0] - a[0]) * i / n), round(a[1] + (b[1] - a[1]) * i / n), color)


def swords(img, rect, x1, y1, x2, y2, col=(228, 232, 242), hilt=(220, 180, 70)):
    line(img, rect, (x1, y1), (x2, y2), col)
    line(img, rect, (x2, y1), (x1, y2), col)
    put(img, rect, x1, y2, hilt)
    put(img, rect, x2, y2, hilt)


# ----------------------------------------------------------------- Kunori
def kunori():
    spikes = [("sp0", -3, -3), ("sp1", 2, -3), ("sp2", -0.5, 0), ("sp3", -3, 2), ("sp4", 2, 2)]
    head = [box("hood", (-5, -9, -5), (10, 10, 10), "robe")] + [box(i, (x, -12, z), (1, 3, 1), "crimson") for i, x, z in spikes]
    P = humanoid("dark", "robe", head, [box("sash", (-4.5, 2, -2.5), (9, 2, 5), "crimson")],
                 [box("r_arm_b", (-3, -2, -2), (4, 9, 4), "robe"), box("r_hand", (-3, 7, -2), (4, 3, 4), "skin")],
                 [box("r_leg_b", (-2, 0, -2), (4, 12, 4), "robe")],
                 [part("skirt", "body", (0, 12, 0), (0, 0, 0), [box("skirt_b", (-5, 0, -3), (10, 12, 6), "robe"),
                                                              box("hem", (-5.5, 10, -3.5), (11, 2, 7), "crimson")])])
    pal = {"robe": (30, 15, 36), "dark": (14, 9, 16), "skin": (125, 95, 85), "crimson": (140, 30, 40)}

    def deco(img, glow, R, rng):
        f = R("hood", "front")
        for j in range(2, 10):
            for i in range(2, 8):
                put(img, f, i, j, (7, 4, 9))
        for x in (3, 4, 6, 7):
            put(img, f, x, 5, (255, 60, 45))
            put(glow, f, x, 5, (255, 60, 45))
        sk = R("skirt_b", "front")
        for x in range(1, 10, 3):
            for y in range(0, 10):
                put(img, sk, x, y, (20, 10, 26))
    return "KunoriParts", "encapuzado_kunori", P, pal, deco


# ----------------------------------------------------------------- Soldado da Legião
def legiao():
    head = [box("helm", (-5, -9, -5), (10, 5, 10), "steel"),
            box("r_cheek", (-5, -4, -3), (1, 4, 6), "steel"), box("l_cheek", (4, -4, -3), (1, 4, 6), "steel", mirror_of="r_cheek"),
            box("nasal", (-0.5, -4, -5), (1, 4, 1), "steel"), box("plume", (-1, -12, -4), (2, 3, 8), "crimson")]
    body = [box("plate", (-4.5, 0, -2.5), (9, 7, 5), "steel"), box("tabard", (-3, 7, -2.5), (6, 6, 5), "crimson")]
    arm = [box("r_arm_b", (-3, -2, -2), (4, 8, 4), "cloth"), box("r_gaunt", (-3, 6, -2), (4, 4, 4), "steel"),
           box("r_pauld", (-4, -3, -3), (6, 3, 6), "steel")]
    leg = [box("r_leg_b", (-2, 0, -2), (4, 12, 4), "cloth"), box("r_greave", (-2.5, 4, -2.5), (5, 8, 5), "steel")]
    P = humanoid("skin", "cloth", head, body, arm, leg)
    pal = {"skin": (205, 165, 140), "steel": (150, 156, 168), "cloth": (58, 62, 76), "crimson": (150, 30, 40)}

    def deco(img, glow, R, rng):
        f = R("head_b", "front")
        put(img, f, 2, 4, (40, 28, 24)); put(img, f, 5, 4, (40, 28, 24))
        put(img, f, 3, 6, (140, 70, 70)); put(img, f, 4, 6, (140, 70, 70))
        for x in (2, 5):
            put(img, f, x, 3, (80, 55, 45))
        swords(img, R("plate", "front"), 2, 1, 6, 5)
        swords(img, R("tabard", "front"), 1, 1, 4, 4, (235, 225, 200), (235, 225, 200))
    return "LegiaoParts", "soldado_legiao", P, pal, deco


# ----------------------------------------------------------------- Zytra
def zytra():
    head = [box("hair_top", (-4.5, -9, -4.5), (9, 3, 9), "hair"), box("hair_back", (-4.5, -8, 2.5), (9, 9, 2), "hair"),
            box("fringe", (-4.5, -8, -4.5), (9, 2, 1), "hair"), box("tail", (-1, -7, 4.5), (2, 10, 2), "hair")]
    body = [box("plate", (-4.5, 0, -2.5), (9, 5, 5), "steel"), box("belt", (-4.5, 9, -2.5), (9, 2, 5), "gold")]
    arm = [box("r_arm_b", (-3, -2, -2), (4, 8, 4), "cloth"), box("r_brace", (-3, 6, -2), (4, 4, 4), "steel"),
           box("r_pauld", (-3.5, -2.5, -2.5), (5, 2, 5), "steel")]
    leg = [box("r_leg_b", (-2, 0, -2), (4, 12, 4), "cloth"), box("r_boot", (-2.5, 8, -2.5), (5, 4, 5), "dark")]
    P = humanoid("skin", "cloth", head, body, arm, leg)
    pal = {"skin": (228, 192, 164), "hair": (232, 204, 110), "steel": (176, 184, 200), "cloth": (40, 46, 70),
           "gold": (220, 180, 70), "dark": (30, 28, 34)}

    def deco(img, glow, R, rng):
        f = R("head_b", "front")
        for x in (2, 5):
            put(img, f, x, 4, (255, 225, 90)); put(glow, f, x, 4, (255, 215, 70))
            put(img, f, x, 3, (150, 120, 60))
        put(img, f, 3, 6, (160, 80, 80)); put(img, f, 4, 6, (160, 80, 80))
        swords(img, R("plate", "front"), 3, 1, 5, 3)
    return "ZytraParts", "zytra_twin", P, pal, deco


# ----------------------------------------------------------------- Haigol
def haigol():
    head = [box("helm", (-5, -9, -5), (10, 9, 10), "steel"), box("crest", (-1, -12, -5), (2, 3, 10), "crimson")]
    body = [box("plate", (-5, 0, -3), (10, 12, 6), "steel")]
    arm = [box("r_arm_b", (-3, -2, -2), (4, 5, 4), "cloth"), box("r_brace", (-3.5, 3, -2.5), (5, 5, 5), "steel"),
           box("r_gaunt", (-3.5, 8, -2.5), (5, 2, 5), "steel"), box("r_pauld", (-5, -4, -4), (8, 5, 8), "steel"),
           box("r_spike", (-3, -8, -1), (2, 4, 2), "bone")]
    leg = [box("r_leg_b", (-2, 0, -2), (4, 12, 4), "cloth"), box("r_greave", (-2.5, 3, -2.5), (5, 9, 5), "steel")]
    P = humanoid("skin", "cloth", head, body, arm, leg,
                 [part("cape", "body", (0, 0, 2.5), (0, 0, 0), [box("cape_b", (-5, 0, 0), (10, 20, 1), "crimson")])])
    pal = {"skin": (200, 160, 130), "steel": (74, 78, 92), "cloth": (36, 38, 48), "crimson": (130, 24, 30), "bone": (210, 205, 190)}

    def deco(img, glow, R, rng):
        f = R("helm", "front")
        for x in range(1, 9):
            put(img, f, x, 4, (10, 10, 14)); put(img, f, x, 5, (10, 10, 14))
        for x in (3, 6):
            put(img, f, x, 4, (255, 215, 70)); put(glow, f, x, 4, (255, 215, 70))
        swords(img, R("plate", "front"), 2, 2, 7, 9, (215, 220, 232), (220, 180, 70))
        lighten(img, R("plate", "top"), 0, 0, 20, 20, 1.2)
    return "HaigolParts", "haigol_twin", P, pal, deco


def build():
    report = {}
    for fn, seed in ((kunori, 21), (legiao, 22), (zytra, 23), (haigol, 24)):
        cls, tex, spec, pal, deco = fn()
        h = mg.ground_fix(spec)
        img, glow, _ = mg.draw_spec(tex, spec, 128, 128, pal, deco, seed)
        has_glow = glow.getbbox() is not None
        mg.save(tex, img, glow if has_glow else None)
        mg.emit_java(cls, spec, 128, 128, h)
        mg.preview(tex, spec, h)
        report[cls] = (h, has_glow)
    return report


if __name__ == "__main__":
    for k, (h, g) in build().items():
        print(f"{k}: altura = {h:.1f} px, brilho = {g}")
