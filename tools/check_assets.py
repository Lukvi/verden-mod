#!/usr/bin/env python3
"""Auditoria de recursos da Fase 1: tudo que o código/dados referenciam existe? (sons, texturas, idioma, JSON do índice)."""
import json, os, re, sys
R = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..')
A = R + '/src/main/resources/assets/verden'; D = R + '/src/main/resources/data/verden/rpg'
bad = []
def need(c, m):
    if not c: bad.append(m)
sj = json.load(open(A + '/sounds.json', encoding='utf-8'))
for ev, v in sj.items():
    for s in v['sounds']:
        n = s['name'] if isinstance(s, dict) else s
        need(os.path.exists(f"{A}/sounds/{n.split(':')[-1]}.ogg"), f'som ausente: {ev} -> {n}')
src = open(R + '/src/main/java/com/verden/registry/ModSounds.java', encoding='utf-8').read()
for m in re.finditer(r'reg\("(rpg\.[a-z_]+)"\)', src): need(m.group(1) in sj, f'evento registrado sem entrada em sounds.json: {m.group(1)}')
for ev in ('rpg.mar', 'rpg.tensao', 'rpg.sussurros', 'rpg.tiro', 'rpg.relatorio', 'rpg.titulo', 'rpg.vento'): need(ev in sj, f'evento usado pelo código ausente: {ev}')
roles = re.findall(r'^\s+([A-Z_]+)\("([a-z_]+)", \d+\)', open(R + '/src/main/java/com/verden/entity/NpcRole.java', encoding='utf-8').read(), re.M)
lang = json.load(open(A + '/lang/pt_br.json', encoding='utf-8'))
for _, rid in roles:
    need(os.path.exists(f'{A}/textures/entity/npc/{rid}.png'), f'textura de NPC ausente: {rid}')
    need(f'npc.verden.{rid}.name' in lang, f'chave de idioma ausente: npc.verden.{rid}.name')
for p in ('trinity', 'dan', 'marie', 'lena', 'nefertari', 'homem_parapeito', 'narrador'): need(os.path.exists(f'{A}/textures/gui/portrait/{p}.png'), f'retrato ausente: {p}')
for ic in ('quest_main', 'quest_side', 'quest_invest', 'quest_done', 'heart', 'xp', 'interact', 'codex', 'lock'): need(os.path.exists(f'{A}/textures/gui/icon/{ic}.png'), f'ícone ausente: {ic}')
need(os.path.exists(A + '/textures/gui/logo.png'), 'logo ausente')
ix = json.load(open(D + '/index.json', encoding='utf-8'))
for k, l in ix.items():
    for p in l:
        f = R + '/src/main/resources/' + p
        need(os.path.exists(f), f'índice aponta para arquivo inexistente: {p}')
        if os.path.exists(f): json.load(open(f, encoding='utf-8'))
for q in ('trinity', 'dan', 'marie', 'lena', 'narrador'):
    d = json.load(open(f'{D}/characters/{q}.json', encoding='utf-8'))
    for o in d['outfits']: need(os.path.exists(R + '/src/main/resources/assets/verden/' + o['skin'].split(':')[1]) or 'PENDENTE' in o['name'] or 'PENDENTE' in o['description'] or q in ('marie', 'lena', 'narrador'), f'skin de {q}/{o["id"]} ausente: {o["skin"]}')
print('PROBLEMAS:' if bad else 'recursos OK', *bad, sep='\n  ')
sys.exit(1 if bad else 0)
