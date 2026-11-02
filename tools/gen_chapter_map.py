#!/usr/bin/env python3
"""Gera docs/CHAPTER_TO_MISSION_MAP.md a partir de docs/livro/ (rodar tools/split_livro.py antes).
Mapeia capítulo → fase do plano de campanha e lista, por capítulo, quem aparece (detecção por palavra).
NÃO substitui a leitura: a coluna 'Status' fica A VALIDAR até um capítulo ser lido e resumido."""
import json, os, re
root = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..')
L = os.path.join(root, 'docs', 'livro')
idx = json.load(open(os.path.join(L, '_indice.json'), encoding='utf-8'))
LIDOS = {'L1_00_prologo', 'L1_01', 'L1_02', 'L1_03'}   # lidos integralmente até agora (atualizar ao ler mais)
FASES = [  # id, título de trabalho, livro, de, até  (do plano de campanha enviado pelo usuário)
 ('F1','A Proposta de Jord',1,1,4),('F2','Os Desaparecimentos de Lóros',1,5,7),('F3','Rio Kejne e a Infiltração',1,8,9),
 ('F4','O Altar da Seita Kunori',1,10,11),('F5','A Assembleia e o Ataque em Flerin',1,12,16),('F6','Rotas Separadas',1,17,24),
 ('F7','A Expedição de Trinity',1,25,27),('F8','Okse, Dagnus e as Rotas de Marie',1,28,34),('F9','A Mina de Yettus e o Enigma de Razor',1,35,43),
 ('F10','Os Sete Dias / Jornada para Hyjnottir',1,44,53),('F11','Koold, a Mansão e a Perda de Gabe',1,54,63),('F12','O Julgamento e o Epílogo de Prelúdio',1,64,67),
 ('F13','O Ultimato de Kizaki',2,1,3),('F14','A Infiltração em Nagdaar',2,4,12),('F15','A Subida ao Templo Yak',2,5,13),
 ('F16','A Prisão de Marie e o Caminho de Mark',2,14,20),('F17','Retorno, Refugiados e a Mina de Yettus',2,21,39),('F18','A Crise em Flerin',2,40,49),
 ('F19','Presídio Motry e a Nova Organização',2,50,59),('F20','O Plano para Karamarraw',2,60,69),('F21','A Ofensiva Final e Diana',2,70,80),
 ('F22','O Vestígio e a Floresta Mørke',2,81,82),('F23','O Palácio, o Coração e o Fim',2,83,92)]
json.dump([dict(id=f, titulo=t, livro=l, de=a, ate=b) for f, t, l, a, b in FASES], open(os.path.join(root, 'docs', 'fases.json'), 'w', encoding='utf-8'), ensure_ascii=False, indent=1)
NOMES = ['Trinity','Marie','Dan','Lena','Gabe','Razor','Caesar','Hethephéres','Penélope','Tarek','Diana','Nefertari','Kizaki','Konge','Franklin','Mark','Yonjii','Beric','Aldo','Ruby','Casthiel']
LUGARES = ['Yettus','Flerin','Lóros','Kejne','Kapenge','Karamarraw','Okse','Hyjnottir','Nagdaar','Koold','Veroogh','Montainezz','Nahy','Morke','Kartoffel','Motry','Hejury','Vestígio','Interseção']
def fases_de(l, n):
    return [f for f, _, ll, a, b in FASES if ll == l and a <= n <= b]
rows = ['# CHAPTER_TO_MISSION_MAP\n',
 'Gerado por `tools/gen_chapter_map.py`. Numeração = **número impresso no título** (L1 = Prelúdio, L2 = Agouro). '
 'Presença de personagens/lugares é detecção automática por palavra (indica, não prova). '
 '**Status: LIDO** só onde o capítulo foi lido integralmente; o resto é A VALIDAR.\n',
 '## Divergências entre o plano de campanha e o texto\n',
 '- **Numeração repetida no original**: Prelúdio tem dois capítulos intitulados "CINQUENTA E DOIS" (`L1_52`, `L1_52b`); Agouro tem dois "SETENTA E SETE" (`L2_77`, `L2_77b`). Tratados como capítulos distintos, na ordem do arquivo. Confirmar com o autor se é erro de numeração.',
 '- **Fase 2 (L1 5–7)**: o nome "Kunori" só aparece a partir do cap. 8; nos caps. 6–7 fala-se em "seita" (inclusive "seita morkeísta"). Não nomear Kunori antes da hora.',
 '- **Fase 15 (L2 5–13)**: Brottus só é citada no cap. 1; Verlaaf no cap. 1 (e depois 17, 63); Palusfor nos caps. 1, 13, 58, 73, 74. A instrução de "evitar" essas rotas vem do planejamento do cap. 1.',
 '- Os demais 107 de 110 marcadores de ancoragem do plano (personagens/lugares/objetos por fase) foram confirmados nos intervalos indicados.\n',
 '## Mapa\n', '| Capítulo | Título impresso | Palavras | Fase(s) | Personagens (detecção) | Lugares (detecção) | Classificação | Status |', '|---|---|---|---|---|---|---|---|']
for x in idx:
    code = x['code']; t = open(os.path.join(L, code + '.txt'), encoding='utf-8').read()
    m = re.search(r'_(\d+)b?$', code); n = int(m.group(1)) if m else None
    fs = ', '.join(fases_de(x['livro'], n)) if n else ('F12' if x['livro'] == 1 and 'epilogo' in code else 'F23' if 'epilogo' in code else 'prólogo')
    ch = ', '.join(w for w in NOMES if re.search(r'\b' + w, t)) or '—'
    lg = ', '.join(w for w in LUGARES if re.search(w, t)) or '—'
    st = 'LIDO' if code in LIDOS else 'A VALIDAR'
    extra = ' ⚠ título repetido' if x['duplicado'] else ''
    rows.append(f"| {code} | {x['titulo'].title()}{extra} | {x['palavras']} | {fs} | {ch} | {lg} | A DEFINIR (CANÔNICO / ADAPTAÇÃO / OPCIONAL ORIGINAL) | {st} |")
open(os.path.join(root, 'docs', 'CHAPTER_TO_MISSION_MAP.md'), 'w', encoding='utf-8').write('\n'.join(rows) + '\n')
print(len(idx), 'linhas')
