"""Verify ribbon study folders. One JSON line per folder; non-zero exit on any failure.

    python3 check.py 01-quarter-turn 02-half-turn ...
"""

import hashlib
import json
import math
import os
import re
import sys

from PIL import Image

GROUND = (0x0B, 0x0F, 0x16)
INK = (0xE9, 0xEE, 0xF7)
ACCENT = (0xFE, 0x67, 0x03)
TOLERANCE = 3.0


def segment_distance(c, a, b):
    """Distance from colour c to the RGB segment a->b, and the position along it."""
    ab = [b[i] - a[i] for i in range(3)]
    ac = [c[i] - a[i] for i in range(3)]
    t = sum(x * y for x, y in zip(ab, ac)) / sum(x * x for x in ab)
    t = min(1.0, max(0.0, t))
    p = [a[i] + t * ab[i] for i in range(3)]
    return math.dist(c, p), t


def classify(c):
    if c == GROUND:
        return "ground"
    if c == INK:
        return "ink"
    if c == ACCENT:
        return "accent"
    d_ink, _ = segment_distance(c, GROUND, INK)
    d_acc, _ = segment_distance(c, GROUND, ACCENT)
    if min(d_ink, d_acc) > TOLERANCE:
        return None
    return "ink_blend" if d_ink <= d_acc else "accent_blend"


def check(folder):
    result = {"folder": folder, "failures": []}
    fail = result["failures"].append

    png = os.path.join(folder, "preview-1x.png")
    img = Image.open(png)
    result["size"] = list(img.size)
    result["mode"] = img.mode
    if img.size != (1000, 1400) or img.mode != "RGB":
        fail("preview-1x.png is %sx%s %s, want 1000x1400 RGB" % (*img.size, img.mode))

    with open(png, "rb") as f:
        digest = hashlib.sha256(f.read()).hexdigest()
    with open(os.path.join(folder, "manifest.json")) as f:
        manifest = json.load(f)
    result["sha256"] = digest
    if manifest.get("sha256") != digest:
        fail("manifest sha256 %s != file %s" % (manifest.get("sha256"), digest))

    with open(os.path.join(folder, "render.sh")) as f:
        m = re.search(r"--seed\s+(\d+)", f.read())
    render_seed = int(m.group(1)) if m else None
    result["seed"] = render_seed
    if render_seed != manifest.get("seed"):
        fail("render.sh seed %s != manifest seed %s" % (render_seed, manifest.get("seed")))

    counts = {"ground": 0, "ink": 0, "accent": 0, "ink_blend": 0, "accent_blend": 0}
    stray = []
    colours = img.convert("RGB").getcolors(maxcolors=1 << 24)
    for n, c in colours:
        kind = classify(c)
        if kind is None:
            stray.append((n, c))
        else:
            counts[kind] += n
    total = img.size[0] * img.size[1]
    result["distinct_colours"] = len(colours)
    result["pixels"] = counts
    result["ground_fraction"] = round(counts["ground"] / total, 4)
    result["stray_pixels"] = sum(n for n, _ in stray)
    if stray:
        worst = sorted(stray, reverse=True)[:5]
        fail("%d colours off the palette lines, e.g. %s" % (
            len(stray), ", ".join("#%02X%02X%02X x%d" % (*c, n) for n, c in worst)))

    p360 = os.path.join(folder, "preview-360.png")
    if not os.path.exists(p360):
        fail("preview-360.png missing")
    else:
        size = Image.open(p360).size
        result["preview_360"] = list(size)
        if size != (360, 504):
            fail("preview-360.png is %sx%s, want 360x504" % size)

    result["ok"] = not result["failures"]
    return result


def main(argv):
    if not argv:
        print(__doc__.strip(), file=sys.stderr)
        return 2
    ok = True
    for folder in argv:
        r = check(folder.rstrip("/"))
        ok = ok and r["ok"]
        print(json.dumps(r))
    return 0 if ok else 1


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
