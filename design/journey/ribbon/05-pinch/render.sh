#!/bin/sh
set -eu
cd "$(dirname "$0")" && python3 ../ribbon.py --study 05-pinch --seed 5105 --out preview-1x.png --manifest manifest.json --preview-360 preview-360.png
