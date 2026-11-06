#!/bin/sh
set -eu
cd "$(dirname "$0")" && python3 ../ribbon.py --study 04-gradient-mesh --seed 5104 --out preview-1x.png --manifest manifest.json --preview-360 preview-360.png
