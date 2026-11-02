#!/usr/bin/env python3
"""Divide Prelúdio_e_Agouro.txt em um arquivo por capítulo (docs/livro/L1_NN.txt, L2_NN.txt) + _indice.json.
Uso: python3 tools/split_livro.py caminho/Prelúdio_e_Agouro.txt
L1 = Prelúdio (prólogo + 67 capítulos + epílogo); L2 = Agouro (93 capítulos + epílogo)."""
import json, os, re, sys
src = sys.argv[1] if len(sys.argv) > 1 else 'Prelúdio_e_Agouro.txt'
out = os.path.join(os.path.dirname(__file__), '..', 'docs', 'livro')
os.makedirs(out, exist_ok=True)
L = open(src, encoding='utf-8', errors='replace').read().replace('\r', '').replace('\u00a0', ' ').split('\n')
marks = []
for i, l in enumerate(L):
    s = re.sub(r'\s+', ' ', l.strip())
    if len(s) < 45 and re.match(r'^(CAP[ÍI]TULO\b|EP[ÍI]LOGO\b|Prel[úu]dio$)', s, flags=re.I):
        marks.append((i, s))
# o 2º livro começa no primeiro "CAPÍTULO UM" que vem depois do primeiro EPÍLOGO
first_epi = next(i for i, s in marks if s.upper().startswith('EP'))
idx, n = [], {1: 0, 2: 0}
for k, (i, s) in enumerate(marks):
    end = marks[k + 1][0] if k + 1 < len(marks) else len(L)
    liv = 1 if i <= first_epi else 2
    if s.lower().startswith('prel'): code = 'L1_00_prologo'
    elif s.upper().startswith('EP'): code = f'L{liv}_epilogo'
    else:
        n[liv] += 1; code = f'L{liv}_{n[liv]:02d}'
    txt = '\n'.join(L[i:end]).strip()
    open(os.path.join(out, code + '.txt'), 'w', encoding='utf-8').write(txt)
    idx.append(dict(code=code, livro=liv, titulo=s, palavras=len(txt.split())))
json.dump(idx, open(os.path.join(out, '_indice.json'), 'w', encoding='utf-8'), ensure_ascii=False, indent=1)
print(len(idx), 'seções;', sum(x['palavras'] for x in idx), 'palavras')
