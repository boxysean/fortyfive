"""Parallel traces on an invisible twisting ribbon (boxysean.com, issue #65).

The ribbon itself is never drawn. Each trace is a polyline at a fixed offset across
the ribbon's width, and each segment fades by |n . view|: bright where the band faces
the camera, gone where it turns edge-on. The shape is carried by that fade alone.

Deterministic: same study and seed reproduce the PNG byte for byte.
"""

import argparse
import hashlib
import json
import math
import platform
import random
import sys

from PIL import Image, ImageChops, ImageDraw

WIDTH, HEIGHT = 1000, 1400
CX, CY = 500, 700
D = 2600.0
STEPS = 900
SUPERSAMPLE = 2

AX, FX = 130.0, 0.9
AZ, FZ = 240.0, 0.5

GROUND = "#0B0F16"
INK = "#E9EEF7"
ACCENT = "#FE6703"
ACCENT_U = 0.55


def _width_const(w):
    return lambda t: w


def _width_pinch(t):
    return 380.0 * (0.03 + 0.97 * abs(math.sin(3 * math.pi * t)) ** 0.8)


def _uniform(count):
    return [-1 + 2 * i / (count - 1) for i in range(count)]


def _power(count):
    return [-1 + 2 * (i / (count - 1)) ** 1.9 for i in range(count)]


# Each ribbon: trace count, width function, twist (start, sweep), depth offset, offsets.
STUDIES = {
    "01-quarter-turn": [
        dict(count=29, width=_width_const(360), start=-20, sweep=90, dz=0, offsets=_uniform),
    ],
    "02-half-turn": [
        dict(count=43, width=_width_const(320), start=-45, sweep=180, dz=0, offsets=_uniform),
    ],
    "03-interleaved": [
        dict(count=21, width=_width_const(210), start=0, sweep=180, dx=-70, dz=70, offsets=_uniform),
        dict(count=21, width=_width_const(210), start=90, sweep=-180, dx=70, dz=-70, offsets=_uniform),
    ],
    "04-gradient-mesh": [
        dict(count=37, width=_width_const(360), start=-25, sweep=110, dz=0, offsets=_power),
    ],
    "05-pinch": [
        dict(count=33, width=_width_pinch, start=-15, sweep=100, dz=0, offsets=_uniform),
    ],
}


# --- vector helpers (tuples, no numpy) ---------------------------------------

def add(a, b):
    return (a[0] + b[0], a[1] + b[1], a[2] + b[2])


def sub(a, b):
    return (a[0] - b[0], a[1] - b[1], a[2] - b[2])


def mul(a, k):
    return (a[0] * k, a[1] * k, a[2] * k)


def dot(a, b):
    return a[0] * b[0] + a[1] * b[1] + a[2] * b[2]


def cross(a, b):
    return (a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0])


def normalize(a):
    n = math.sqrt(dot(a, a))
    return (a[0] / n, a[1] / n, a[2] / n)


def rotate(v, axis, phi):
    """Rodrigues: rotate v about the unit vector axis by phi radians."""
    c, s = math.cos(phi), math.sin(phi)
    return add(add(mul(v, c), mul(cross(axis, v), s)), mul(axis, dot(axis, v) * (1 - c)))


# --- geometry ----------------------------------------------------------------

def spine(px, pz, dz, dx=0):
    # Y(t) = -140 + 1680 t is a page y (top-left origin): it overhangs the 1400-tall
    # page by 140 at each end. The world origin is the page centre, so shift by CY;
    # taken as a world y it would start at page y 560 and leave the top 40% empty.
    ts = [k / (STEPS - 1) for k in range(STEPS)]
    pts = [
        (AX * math.sin(2 * math.pi * FX * t + px) + dx,
         -140 + 1680 * t - CY,
         AZ * math.sin(2 * math.pi * FZ * t + pz) + dz)
        for t in ts
    ]
    return ts, pts


def frames(pts):
    """Tangents and parallel-transported normals (never cross(tangent, up))."""
    n = len(pts)
    tangents = []
    for k in range(n):
        a = pts[max(k - 1, 0)]
        b = pts[min(k + 1, n - 1)]
        tangents.append(normalize(sub(b, a)))
    x = (1.0, 0.0, 0.0)
    normals = [normalize(sub(x, mul(tangents[0], dot(x, tangents[0]))))]
    for k in range(1, n):
        prev = normals[-1]
        normals.append(normalize(sub(prev, mul(tangents[k], dot(prev, tangents[k])))))
    return tangents, normals


def project(p):
    s = D / (D - p[2])
    return (CX + p[0] * s, CY + p[1] * s), s


def view_dir(p):
    return normalize(sub((0.0, 0.0, D), p))


def build_ribbon(spec, px, pz):
    """Per step: centre, twisted normal, surface normal, half-width."""
    ts, pts = spine(px, pz, spec["dz"], spec.get("dx", 0))
    tangents, normals = frames(pts)
    twisted, surface = [], []
    for t, T, N in zip(ts, tangents, normals):
        phi = math.radians(spec["start"] + spec["sweep"] * t)
        Nt = rotate(N, T, phi)
        twisted.append(Nt)
        surface.append(normalize(cross(T, Nt)))
    half = [spec["width"](t) / 2 for t in ts]
    return pts, twisted, surface, half


def draw_trace(ribbon, u, gain):
    """One trace on its own supersampled L layer; alpha = gain * |n . view|."""
    pts, twisted, surface, half = ribbon
    ss = SUPERSAMPLE
    layer = Image.new("L", (WIDTH * ss, HEIGHT * ss), 0)
    draw = ImageDraw.Draw(layer)
    P = [add(c, mul(nt, u * h)) for c, nt, h in zip(pts, twisted, half)]
    for k in range(len(P) - 1):
        a, b = P[k], P[k + 1]
        mid = mul(add(a, b), 0.5)
        n = normalize(add(surface[k], surface[k + 1]))
        alpha = gain * abs(dot(n, view_dir(mid)))
        w = max(1, round(1.15 * ss))  # constant weight; perspective lives in the geometry
        fill = int(round(255 * alpha))
        if fill == 0:
            continue
        (ax, ay), _ = project(a)
        (bx, by), _ = project(b)
        draw.line([(ax * ss, ay * ss), (bx * ss, by * ss)], fill=fill, width=w)
    return layer


def render(study, seed):
    specs = STUDIES[study]
    rng = random.Random(seed)
    px = 0.35 + 0.5 * rng.random()
    pz = 1.05 + 0.5 * rng.random()

    # Trace list in draw order; brightness drawn for every trace so the stream is
    # stable, then the accent's is overridden to 1.0.
    traces = []
    for r, spec in enumerate(specs):
        for u in spec["offsets"](spec["count"]):
            traces.append([r, u, 0.94 + 0.06 * rng.random()])
    accent_idx = min(
        (i for i, tr in enumerate(traces) if tr[0] == 0),
        key=lambda i: abs(traces[i][1] - ACCENT_U),
    )
    traces[accent_idx][2] = 1.0

    ribbons = [build_ribbon(spec, px, pz) for spec in specs]
    size = (WIDTH * SUPERSAMPLE, HEIGHT * SUPERSAMPLE)
    ink_mask = Image.new("L", size, 0)
    accent_mask = None
    for i, (r, u, gain) in enumerate(traces):
        layer = draw_trace(ribbons[r], u, gain)
        if i == accent_idx:
            accent_mask = layer
        else:
            ink_mask = ImageChops.lighter(ink_mask, layer)

    # Downsample the masks, not the colour image: LANCZOS on RGB would mix ink and
    # accent and clip per channel, leaving colours off the ground->ink/accent lines.
    ink_mask = ink_mask.resize((WIDTH, HEIGHT), Image.LANCZOS)
    accent_mask = accent_mask.resize((WIDTH, HEIGHT), Image.LANCZOS)
    # Where the accent trace is the stronger mark, it owns the pixel; otherwise ink
    # does. Max-merge again, so the accent never sits on top of an ink blend and
    # produces a third colour.
    accent_wins = ImageChops.subtract(accent_mask, ink_mask).point(lambda v: 255 if v > 0 else 0)
    ink_mask = ImageChops.composite(Image.new("L", (WIDTH, HEIGHT), 0), ink_mask, accent_wins)
    accent_mask = ImageChops.composite(accent_mask, Image.new("L", (WIDTH, HEIGHT), 0), accent_wins)

    out = Image.new("RGB", (WIDTH, HEIGHT), GROUND)
    out = Image.composite(Image.new("RGB", (WIDTH, HEIGHT), INK), out, ink_mask)
    out = Image.composite(Image.new("RGB", (WIDTH, HEIGHT), ACCENT), out, accent_mask)

    meta = dict(px=px, pz=pz, trace_count=len(traces), specs=specs)
    return out, meta


def sha256(path):
    with open(path, "rb") as f:
        return hashlib.sha256(f.read()).hexdigest()


def main(argv=None):
    ap = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    ap.add_argument("--study", required=True, choices=sorted(STUDIES))
    ap.add_argument("--seed", required=True, type=int)
    ap.add_argument("--out", required=True)
    ap.add_argument("--manifest")
    ap.add_argument("--preview-360")
    args = ap.parse_args(argv)

    img, meta = render(args.study, args.seed)
    img.save(args.out, format="PNG", optimize=False)
    if args.preview_360:
        img.resize((360, 504), Image.LANCZOS).save(args.preview_360, format="PNG", optimize=False)

    if args.manifest:
        specs = meta["specs"]

        def twist(spec):
            return dict(start_deg=spec["start"], sweep_deg=spec["sweep"], turns=spec["sweep"] / 360)

        manifest = dict(
            study=args.study,
            seed=args.seed,
            trace_count=meta["trace_count"],
            twist=twist(specs[0]),
            canvas=[WIDTH, HEIGHT],
            supersample=SUPERSAMPLE,
            palette=dict(ground=GROUND, ink=INK, accent=ACCENT),
            sha256=sha256(args.out),
            generator="design/journey/ribbon/ribbon.py",
            python=platform.python_version(),
        )
        if len(specs) > 1:
            manifest["ribbons"] = [
                dict(trace_count=s["count"], lateral_offset=s.get("dx", 0), depth_offset=s["dz"],
                     twist=twist(s))
                for s in specs
            ]
        with open(args.manifest, "w") as f:
            json.dump(manifest, f, indent=2, sort_keys=True)
            f.write("\n")
    return 0


if __name__ == "__main__":
    sys.exit(main())
