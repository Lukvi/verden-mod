#!/usr/bin/env python3
"""Divide Prelúdio_e_Agouro.txt em um arquivo por capítulo + docs/livro/_indice.json.
Nomeia pelo NÚMERO IMPRESSO no título (L1_05 = "CAPÍTULO CINCO" de Prelúdio; L2_.. = Agouro).
Junta títulos quebrados em duas linhas ("CAPÍTULO VINTE E" / "UM"). Se um número impresso se repete
(erro do original), a repetição recebe sufixo 'b' (L1_52b) e fica sinalizada no índice ("duplicado").
Uso: python3 tools/split_livro.py caminho/Prelúdio_e_Agouro.txt"""
import json, os, re, sys, glob
src = sys.argv[1] if len(sys.argv) > 1 else 'Prelúdio_e_Agouro.txt'
out = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'docs', 'livro')
os.makedirs(out, exist_ok=True)
for f in glob.glob(os.path.join(out, 'L*.txt')): os.remove(f)
U = {'um':1,'uma':1,'dois':2,'duas':2,'três':3,'tres':3,'quatro':4,'cinco':5,'seis':6,'sete':7,'oito':8,'nove':9,'dez':10,'onze':11,'doze':12,'treze':13,'catorze':14,'quatorze':14,'quinze':15,'dezesseis':16,'dezessete':17,'dezoito':18,'dezenove':19}
T = {'vinte':20,'trinta':30,'quarenta':40,'cinquenta':50,'sessenta':60,'setenta':70,'oitenta':80,'noventa':90,'cem':100}
def num(s):
    tot = 0
    for w in re.split(r'\s+', s.lower().replace('capítulo', '').replace(' e ', ' ').strip()):
        if w in U: tot += U[w]
        elif w in T: tot += T[w]
        elif w == 'e' or not w: pass
        else: return None
    return tot or None
L = open(src, encoding='utf-8', errors='replace').read().replace('\r', '').replace('\u00a0', ' ').split('\n')
marks = []
for i, l in enumerate(L):
    s = re.sub(r'\s+', ' ', l.strip())
    if len(s) < 45 and re.match(r'^(CAP[ÍI]TULO\b|EP[ÍI]LOGO\b|Prel[úu]dio$)', s, flags=re.I):
        title = s
        if s.upper().startswith('CAP'):  # título quebrado em duas linhas: "VINTE E" / "UM"  ou  "QUARENTA" / "E UM"
            j = i + 1
            while j < len(L) and not L[j].strip(): j += 1
            nxt = re.sub(r'\s+', ' ', L[j].strip()) if j < len(L) else ''
            if s.upper().endswith(' E') or re.fullmatch(r'(E )?(UM|DOIS|TR[ÊE]S|QUATRO|CINCO|SEIS|SETE|OITO|NOVE)', nxt, flags=re.I):
                title = s + ' ' + nxt
        marks.append((i, title))
first_epi = next(i for i, s in marks if s.upper().startswith('EP'))
idx, seen = [], {}
for k, (i, s) in enumerate(marks):
    end = marks[k + 1][0] if k + 1 < len(marks) else len(L)
    liv = 1 if i <= first_epi else 2
    dup = False
    if s.lower().startswith('prel'): code = 'L1_00_prologo'
    elif s.upper().startswith('EP'): code = f'L{liv}_epilogo'
    else:
        n = num(s)
        if n is None: raise SystemExit(f'título não reconhecido: {s!r} (linha {i})')
        code = f'L{liv}_{n:02d}'
        if (liv, n) in seen: dup = True; code += 'b'
        seen[(liv, n)] = True
    txt = '\n'.join(L[i:end]).strip()
    open(os.path.join(out, code + '.txt'), 'w', encoding='utf-8').write(txt)
    idx.append(dict(code=code, livro=liv, titulo=s, palavras=len(txt.split()), duplicado=dup))
json.dump(idx, open(os.path.join(out, '_indice.json'), 'w', encoding='utf-8'), ensure_ascii=False, indent=1)
for liv in (1, 2):
    ns = sorted({int(re.search(r'_(\d+)', x['code']).group(1)) for x in idx if x['livro'] == liv and re.search(r'_\d+b?$', x['code'])})
    gaps = [n for n in range(1, ns[-1] + 1) if n not in ns]
    print(f'Livro {liv}: caps impressos 1..{ns[-1]}, lacunas={gaps}, duplicados={[x["code"] for x in idx if x["livro"]==liv and x["duplicado"]]}')
print(len(idx), 'seções;', sum(x['palavras'] for x in idx), 'palavras (split)')
