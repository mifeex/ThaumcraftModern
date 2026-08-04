#!/usr/bin/env python3
"""Generate procedural Faceless Witness sound layers from decoded TC4 references."""

from __future__ import annotations

import argparse
import math
import wave
from pathlib import Path

import numpy as np


RATE = 22050


def read_wav(path: Path) -> np.ndarray:
    with wave.open(str(path), "rb") as wav:
        channels = wav.getnchannels()
        width = wav.getsampwidth()
        source_rate = wav.getframerate()
        frames = wav.readframes(wav.getnframes())
    if width != 2:
        raise ValueError(f"Expected 16-bit PCM: {path}")
    data = np.frombuffer(frames, dtype="<i2").astype(np.float64) / 32768.0
    if channels > 1:
        data = data.reshape(-1, channels).mean(axis=1)
    if source_rate != RATE:
        size = max(1, round(len(data) * RATE / source_rate))
        data = np.interp(
            np.linspace(0, len(data) - 1, size), np.arange(len(data)), data
        )
    return data


def fit(data: np.ndarray, seconds: float, speed: float = 1.0) -> np.ndarray:
    target = round(seconds * RATE)
    source_size = min(len(data), max(1, round(target * speed)))
    start = max(0, (len(data) - source_size) // 2)
    source = data[start : start + source_size]
    return np.interp(
        np.linspace(0, len(source) - 1, target), np.arange(len(source)), source
    )


def lowpass(data: np.ndarray, cutoff: float) -> np.ndarray:
    alpha = 1.0 - math.exp(-2.0 * math.pi * cutoff / RATE)
    out = np.empty_like(data)
    state = 0.0
    for index, value in enumerate(data):
        state += alpha * (value - state)
        out[index] = state
    return out


def highpass(data: np.ndarray, cutoff: float) -> np.ndarray:
    return data - lowpass(data, cutoff)


def envelope(size: int, attack: float = 0.08, release: float = 0.35) -> np.ndarray:
    attack_size = max(1, round(attack * RATE))
    release_size = max(1, round(release * RATE))
    env = np.ones(size)
    env[: min(size, attack_size)] = np.linspace(0.0, 1.0, min(size, attack_size))
    count = min(size, release_size)
    env[-count:] *= np.linspace(1.0, 0.0, count) ** 1.7
    return env


def metallic(size: int, rng: np.random.Generator, strength: float) -> np.ndarray:
    time = np.arange(size) / RATE
    base = rng.uniform(43.0, 67.0)
    signal = np.zeros(size)
    for ratio, gain, decay in ((1.0, 1.0, 1.8), (2.71, 0.45, 2.7), (4.13, 0.23, 3.8)):
        signal += gain * np.sin(2 * np.pi * base * ratio * time + rng.uniform(0, 6.28)) * np.exp(-time * decay)
    return signal * strength


def delay(data: np.ndarray, seconds: float, feedback: float) -> np.ndarray:
    offset = round(seconds * RATE)
    out = data.copy()
    for repeat in range(1, 4):
        shift = offset * repeat
        if shift >= len(data):
            break
        out[shift:] += data[:-shift] * feedback**repeat
    return out


def finish(data: np.ndarray) -> np.ndarray:
    data = np.tanh(data * 1.35)
    data -= data.mean()
    peak = max(1e-9, np.max(np.abs(data)))
    return data / peak * 0.86


def write_wav(path: Path, data: np.ndarray) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    pcm = np.clip(data, -1.0, 1.0)
    pcm = (pcm * 32767.0).astype("<i2")
    with wave.open(str(path), "wb") as wav:
        wav.setnchannels(1)
        wav.setsampwidth(2)
        wav.setframerate(RATE)
        wav.writeframes(pcm.tobytes())


def build(kind: str, variant: int, refs: dict[str, np.ndarray]) -> np.ndarray:
    seeds = {"idle": 1100, "alert": 2200, "attack": 3300, "hurt": 4400, "death": 5500}
    rng = np.random.default_rng(seeds[kind] + variant)
    seconds = {
        "idle": rng.uniform(3.8, 5.4),
        "alert": rng.uniform(1.8, 2.6),
        "attack": rng.uniform(0.65, 1.05),
        "hurt": rng.uniform(0.45, 0.8),
        "death": rng.uniform(3.2, 4.6),
    }[kind]
    size = round(seconds * RATE)
    time = np.arange(size) / RATE

    chant = fit(refs[f"chant{1 + (variant - 1) % 3}"], seconds, rng.uniform(0.62, 0.86))
    guardian_key = {
        "idle": f"egidle{1 + (variant - 1) % 2}",
        "alert": "egscreech",
        "attack": "egattack",
        "hurt": "egattack",
        "death": "egdeath",
    }[kind]
    guardian = fit(refs[guardian_key], seconds, rng.uniform(0.72, 1.04))

    reversed_chant = fit(chant[::-1], seconds, rng.uniform(0.9, 1.1))
    sub = np.sin(2 * np.pi * rng.uniform(28.0, 42.0) * time + 0.3 * np.sin(2 * np.pi * 0.31 * time))
    pulse = 0.55 + 0.45 * np.sin(2 * np.pi * rng.uniform(0.35, 0.7) * time + rng.uniform(0, 6.28))

    if kind == "idle":
        mix = lowpass(chant, 1450) * 0.18 + lowpass(reversed_chant, 1100) * 0.07
        mix += lowpass(guardian, 1050) * 0.30
        mix += sub * pulse * 0.075 + metallic(size, rng, 0.075)
        mix = delay(mix, rng.uniform(0.29, 0.41), 0.18)
    elif kind == "alert":
        silence_curve = np.clip((time - 0.18) / 0.22, 0, 1)
        mix = lowpass(reversed_chant, 1700) * (1.0 - silence_curve) * 0.24
        mix += lowpass(guardian, 2200) * silence_curve * 0.43
        mix += metallic(size, rng, 0.18)
        mix += sub * 0.08
        mix = delay(mix, 0.19, 0.16)
    elif kind == "attack":
        mix = highpass(guardian, 150) * 0.52 + lowpass(chant, 1800) * 0.07
        mix += metallic(size, rng, 0.31)
        mix += sub * np.exp(-time * 4.0) * 0.13
    elif kind == "hurt":
        mix = highpass(guardian, 210) * 0.48 + lowpass(reversed_chant, 1400) * 0.08
        mix += metallic(size, rng, 0.24)
        mix *= np.exp(-time * rng.uniform(3.5, 5.5))
    else:
        collapse = np.clip(1.0 - time / seconds, 0, 1)
        mix = lowpass(guardian, 1800) * 0.39 + lowpass(reversed_chant, 1300) * 0.16
        mix += metallic(size, rng, 0.23)
        mix += sub * collapse * 0.11
        mix = delay(mix, 0.34, 0.22)

    return finish(mix * envelope(size, 0.035 if kind in {"attack", "hurt"} else 0.1, 0.22 if kind in {"attack", "hurt"} else 0.55))


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--references", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()

    names = ["chant1", "chant2", "chant3", "egidle1", "egidle2", "egattack", "egscreech", "egdeath"]
    refs = {name: read_wav(args.references / f"{name}.wav") for name in names}
    counts = {"idle": 4, "alert": 3, "attack": 3, "hurt": 2, "death": 2}
    for kind, count in counts.items():
        for variant in range(1, count + 1):
            write_wav(args.output / f"witness_{kind}{variant}.wav", build(kind, variant, refs))


if __name__ == "__main__":
    main()
