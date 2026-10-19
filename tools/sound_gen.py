#!/usr/bin/env python3
"""Gera os efeitos sonoros e o tema do chefe do mod Verden (síntese procedural -> .ogg via ffmpeg).
Uso: python3 tools/sound_gen.py"""
import os, subprocess, tempfile, wave
import numpy as np

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
OUT = os.path.join(ROOT, "src/main/resources/assets/verden/sounds")
os.makedirs(OUT, exist_ok=True)
SR = 32000
rng = np.random.default_rng(7)


def t_(d):
    return np.arange(int(SR * d)) / SR


def env(n, a=0.01, r=0.1):
    e = np.ones(n)
    na, nr = max(1, int(a * SR)), max(1, int(r * SR))
    e[:na] = np.linspace(0, 1, na)
    e[-nr:] *= np.linspace(1, 0, nr)
    return e


def lowpass(x, k):
    """filtro passa-baixa de um polo; k em (0,1] (menor = mais escuro)."""
    y = np.empty_like(x)
    acc = 0.0
    for i, v in enumerate(x):
        acc += k * (v - acc)
        y[i] = acc
    return y


def saw(f, t):
    return 2 * ((f * t) % 1.0) - 1


def norm(x, peak=0.85):
    m = np.max(np.abs(x)) or 1.0
    return x / m * peak


def save(name, x):
    x = np.clip(norm(x), -1, 1)
    pcm = (x * 32767).astype(np.int16)
    with tempfile.TemporaryDirectory() as d:
        wav = os.path.join(d, "a.wav")
        with wave.open(wav, "wb") as w:
            w.setnchannels(1); w.setsampwidth(2); w.setframerate(SR); w.writeframes(pcm.tobytes())
        subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-i", wav, "-c:a", "libvorbis", "-q:a", "4",
                        os.path.join(OUT, name + ".ogg")], check=True)
    print("som:", name, f"{len(x) / SR:.1f}s")


def growl(d, f0, wob, dirty):
    t = t_(d)
    f = f0 * (1 + 0.25 * np.sin(2 * np.pi * wob * t)) * np.linspace(1.15, 0.8, len(t))
    ph = 2 * np.pi * np.cumsum(f) / SR
    x = np.sign(np.sin(ph)) * 0.5 + np.sin(ph * 2) * 0.3 + saw(f0, t) * 0.3
    x += rng.normal(0, dirty, len(t))
    x = lowpass(x, 0.18)
    am = 0.6 + 0.4 * np.sin(2 * np.pi * 28 * t)
    return x * am * env(len(t), 0.05, d * 0.5)


save("growl", growl(1.3, 80, 4, 0.35))
save("lycan_hurt", growl(0.45, 150, 9, 0.5) * np.linspace(1, 0.3, int(SR * 0.45)))
save("lycan_death", growl(1.6, 110, 3, 0.4))
roar = growl(2.4, 48, 2, 0.6)
roar = np.tanh(roar * 3.0)
save("roar", roar)

# Trompa da Legião: harmônicos graves com ataque lento e um segundo tom
t = t_(2.6)
horn = sum(a * np.sin(2 * np.pi * f * t + 0.3 * np.sin(2 * np.pi * 5 * t)) for f, a in
           [(196, 1), (392, 0.55), (588, 0.4), (784, 0.25), (294, 0.5), (147, 0.6)])
horn = lowpass(horn, 0.35) * env(len(t), 0.25, 0.9)
save("horn", horn)

# Conjuração: ruído filtrado em varredura + brilho
t = t_(0.9)
noise = rng.normal(0, 1, len(t))
sweep = lowpass(noise, 0.5) - lowpass(noise, 0.04)
shim = np.sin(2 * np.pi * (500 + 900 * t / 0.9) * t) * 0.4 + np.sin(2 * np.pi * (760 + 600 * t) * t) * 0.25
save("cast", (sweep * 0.8 + shim) * env(len(t), 0.03, 0.6))

# Moedas: dois tinidos metálicos
def ping(f, d):
    t = t_(d)
    return (np.sin(2 * np.pi * f * t) + 0.5 * np.sin(2 * np.pi * f * 2.76 * t) + 0.3 * np.sin(2 * np.pi * f * 5.4 * t)) * np.exp(-t * 14)
coin = np.zeros(int(SR * 0.7))
for off, f in [(0.0, 1760), (0.09, 2349)]:
    p = ping(f, 0.5)
    i = int(off * SR)
    coin[i:i + len(p)] += p
save("coin", coin)

# Fala de NPC: sequência de blips curtos, estilo "balbucio"
x = np.zeros(int(SR * 0.45))
pos = 0
for f in [320, 400, 290, 360]:
    t = t_(0.09)
    b = (np.sin(2 * np.pi * f * t) + 0.3 * np.sin(2 * np.pi * f * 2 * t)) * env(len(t), 0.005, 0.05)
    x[pos:pos + len(b)] += b
    pos += int(SR * 0.1)
save("npc_talk", x)

# ---------------------------------------------------------------- Tema do chefe (32 s)
BPM = 90
beat = 60 / BPM
bars = 12
total = bars * 4 * beat
T = t_(total)
mix = np.zeros(len(T))
chords = [(146.83, 174.61, 220.0), (116.54, 146.83, 174.61), (98.0, 116.54, 146.83), (110.0, 138.59, 164.81)]  # Dm Bb Gm A
bar_len = 4 * beat
for b in range(bars):
    ch = chords[b % 4]
    s = int(b * bar_len * SR)
    n = int(bar_len * SR)
    tt = np.arange(n) / SR
    pad = sum(saw(f, tt) + saw(f * 1.004, tt) for f in ch)
    pad = lowpass(pad, 0.04) * env(n, 0.5, 0.6) * 0.5
    bass = np.sin(2 * np.pi * (ch[0] / 2) * tt) * 0.9 * env(n, 0.02, 0.2)
    mix[s:s + n] += pad[:len(mix) - s] + bass[:len(mix) - s]
    # arpejo no segundo ciclo em diante
    if b >= 4:
        for i in range(8):
            f = ch[i % 3] * 2
            ts = np.arange(int(beat / 2 * SR)) / SR
            note = np.sin(2 * np.pi * f * ts) * np.exp(-ts * 5) * 0.35
            o = s + int(i * beat / 2 * SR)
            mix[o:o + len(note)] += note[:len(mix) - o]
    # percussão
    for k in range(4):
        o = s + int(k * beat * SR)
        kt = np.arange(int(0.35 * SR)) / SR
        kick = np.sin(2 * np.pi * (110 * np.exp(-kt * 14) + 38) * kt) * np.exp(-kt * 9) * 1.0
        mix[o:o + len(kick)] += kick[:len(mix) - o]
        if k in (1, 3) and b >= 2:
            sn = lowpass(rng.normal(0, 1, int(0.18 * SR)), 0.6) * np.exp(-np.arange(int(0.18 * SR)) / SR * 22) * 0.6
            mix[o:o + len(sn)] += sn[:len(mix) - o]
mix *= np.minimum(1, np.linspace(0, 1, len(mix)) * 12) * np.minimum(1, np.linspace(1, 0, len(mix)) * 8)
save("boss_music", mix)
