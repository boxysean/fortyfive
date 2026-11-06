#!/bin/sh
set -eu
cd "$(dirname "$0")" && python3 ../ribbon.py --study 03-interleaved --seed 5103 --out preview-1x.png --manifest manifest.json --preview-360 preview-360.png
