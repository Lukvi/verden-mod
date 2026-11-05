"""Gera as estradas de Verden (roads.json): liga cidades e marcos por caminhos de menor custo (A*) sobre o mapa."""
import heapq, json, math, os, struct
import numpy as np

ROOT = os.path.join(os.path.dirname(__file__), "..")
MAPDIR = os.path.join(ROOT, "src/main/resources/data/verden/map")
raw = open(os.path.join(MAPDIR, "verden_map.bin"), "rb").read()
_, W, H, SCALE, OX, OY = struct.unpack(">4sHHHHH", raw[:14])
n = W * H
planes = [np.frombuffer(raw, dtype=np.uint8, count=n, offset=14 + i * n).reshape(H, W) for i in range(4)]
height, cls, rd, rw = [p.astype(float) if i == 0 else p for i, p in enumerate(planes)]
OCEAN = {0, 1, 22, 23, 24}
sites = json.load(open(os.path.join(MAPDIR, "sites.json")))["sites"]
S = {s["id"]: s for s in sites}

# grade grossa (2 px = 16 blocos)
F = 2
gh, gw = H // F, W // F
step = SCALE * F
def blk(a):  # reduz por média
    return a[:gh * F, :gw * F].reshape(gh, F, gw, F).mean(axis=(1, 3))
gH = blk(height)
land = np.zeros((gh, gw), bool)
water = np.zeros((gh, gw), bool)
cc = cls[:gh * F, :gw * F].reshape(gh, F, gw, F)
isocean = np.isin(cc, list(OCEAN)).mean(axis=(1, 3)) > 0.4
land = ~isocean
rdd = rd[:gh * F, :gw * F].reshape(gh, F, gw, F).min(axis=(1, 3)).astype(float) * 2
rww = rw[:gh * F, :gw * F].reshape(gh, F, gw, F).max(axis=(1, 3)).astype(float)
river = (rww > 0) & (rdd <= rww + 2)
cmode = np.zeros((gh, gw), int)
for i in range(gh):
    for j in range(gw):
        cmode[i, j] = np.bincount(cc[i, :, j, :].ravel()).argmax()
PEAKS = {4, 21}
mult = np.ones((gh, gw))
mult[np.isin(cmode, list(PEAKS))] = 4.0
mult[cmode == 9] = 1.6
mult[cmode == 12] = 1.4
mult[cmode == 10] = 1.25
mult[cmode == 11] = 1.5
mult[gH > 100] *= 1.5

def to_g(x, z):
    px = x / SCALE + OX
    py = z / SCALE + OY
    return int(round(py / F)), int(round(px / F))

def to_b(i, j):
    return int(round((j * F - OX) * SCALE)), int(round((i * F - OY) * SCALE))

def astar(a, b):
    (si, sj), (ti, tj) = a, b
    if not (land[si, sj] and land[ti, tj]):
        # procura célula de terra próxima
        def fix(i, j):
            for r in range(1, 8):
                for di in range(-r, r + 1):
                    for dj in range(-r, r + 1):
                        y, x = i + di, j + dj
                        if 0 <= y < gh and 0 <= x < gw and land[y, x]:
                            return y, x
            return None
        sa, tb = fix(si, sj), fix(ti, tj)
        if not sa or not tb:
            return None
        (si, sj), (ti, tj) = sa, tb
    INF = 1e18
    g = np.full((gh, gw), INF)
    par = {}
    g[si, sj] = 0
    pq = [(0.0, si, sj)]
    nb = [(-1, 0), (1, 0), (0, -1), (0, 1), (-1, -1), (-1, 1), (1, -1), (1, 1)]
    while pq:
        f, i, j = heapq.heappop(pq)
        if (i, j) == (ti, tj):
            path = [(i, j)]
            while (i, j) in par:
                i, j = par[(i, j)]
                path.append((i, j))
            return path[::-1]
        if f - math.hypot(ti - i, tj - j) > g[i, j] + 1e-6 and False:
            continue
        for di, dj in nb:
            y, x = i + di, j + dj
            if not (0 <= y < gh and 0 <= x < gw) or not land[y, x]:
                continue
            d = math.hypot(di, dj)
            dh = abs(gH[y, x] - gH[i, j])
            slope = dh / (d * step)
            c = d * mult[y, x] * (1 + 14 * slope * slope + 3 * slope)
            if river[y, x]:
                c += 6
            ng = g[i, j] + c
            if ng < g[y, x]:
                g[y, x] = ng
                par[(y, x)] = (i, j)
                heapq.heappush(pq, (ng + math.hypot(ti - y, tj - x) * 0.9, y, x))
    return None

def chaikin(pts, it=2):
    for _ in range(it):
        out = [pts[0]]
        for a, b in zip(pts, pts[1:]):
            out.append((0.75 * a[0] + 0.25 * b[0], 0.75 * a[1] + 0.25 * b[1]))
            out.append((0.25 * a[0] + 0.75 * b[0], 0.25 * a[1] + 0.75 * b[1]))
        out.append(pts[-1])
        pts = out
    return pts

def endpoints(s):
    k, R, cx, cz = s["kind"], s["radius"], s["x"], s["z"]
    if k in ("capital_norte", "capital_oeste", "capital_sul"):
        r = R - 8 + 5
    elif k == "capital_leste":
        r = R - 6 + 5
    elif k in ("porto_pirata",) or k.startswith("vila"):
        r = R - 2
    elif k == "cidade_torres":
        r = R - 6
    elif k == "mosteiro":
        return [(cx, cz + 45)]
    elif k == "templo_sol":
        r = 26
    else:
        r = 14
    return [(cx + r, cz), (cx - r, cz), (cx, cz + r), (cx, cz - r)]

nodes = [s for s in sites if s["kind"] != "caverna"]
def dist(a, b):
    return math.hypot(a["x"] - b["x"], a["z"] - b["z"])

edges = set()
# MST (Prim) + 2 vizinhos mais próximos
inn = {nodes[0]["id"]}
while len(inn) < len(nodes):
    best = None
    for a in nodes:
        if a["id"] in inn:
            for b in nodes:
                if b["id"] not in inn:
                    d = dist(a, b)
                    if not best or d < best[0]:
                        best = (d, a["id"], b["id"])
    inn.add(best[2])
    edges.add(tuple(sorted((best[1], best[2]))))
for a in nodes:
    near = sorted((b for b in nodes if b is not a), key=lambda b: dist(a, b))[:2]
    for b in near:
        if dist(a, b) < 2600:
            edges.add(tuple(sorted((a["id"], b["id"]))))
caps = [s["id"] for s in nodes if s["kind"].startswith("capital")]
for i, a in enumerate(caps):
    for b in caps[i + 1:]:
        edges.add(tuple(sorted((a, b))))

roads = []
for a, b in sorted(edges):
    sa, sb = S[a], S[b]
    ea = min(endpoints(sa), key=lambda p: math.hypot(p[0] - sb["x"], p[1] - sb["z"]))
    eb = min(endpoints(sb), key=lambda p: math.hypot(p[0] - sa["x"], p[1] - sa["z"]))
    p = astar(to_g(*ea), to_g(*eb))
    if not p:
        print("sem caminho:", a, b)
        continue
    pts = [to_b(i, j) for i, j in p]
    pts = [pts[0]] + pts[3:-3:3] + [pts[-1]] if len(pts) > 8 else pts
    pts[0] = ea
    pts[-1] = eb
    pts = [(float(x), float(z)) for x, z in pts]
    pts = chaikin(pts, 2)
    pts[0] = (float(ea[0]), float(ea[1]))
    pts[-1] = (float(eb[0]), float(eb[1]))
    ln = sum(math.hypot(q[0] - r[0], q[1] - r[1]) for q, r in zip(pts, pts[1:]))
    if ln > 4.2 * dist(sa, sb) + 400:
        print("rota longa demais, ignorada:", a, b, int(ln), int(dist(sa, sb)))
        continue
    roads.append({"a": a, "b": b, "len": int(ln), "pts": [[int(round(x)), int(round(z))] for x, z in pts]})
    print("ok", a, b, int(ln))

json.dump({"roads": roads}, open(os.path.join(MAPDIR, "roads.json"), "w"), separators=(",", ":"))
print(len(roads), "estradas")

# pré-visualização
try:
    from PIL import Image, ImageDraw
    img = Image.open(os.path.join(ROOT, "tools/preview/verden_mapa_gerado.png")).convert("RGB")
    dr = ImageDraw.Draw(img)
    sx = img.width / W
    for r in roads:
        pp = [((x / SCALE + OX) * sx, (z / SCALE + OY) * sx) for x, z in r["pts"]]
        dr.line(pp, fill=(255, 220, 0), width=2)
    img.save(os.path.join(ROOT, "tools/preview/verden_estradas.png"))
except Exception as e:
    print("preview:", e)
