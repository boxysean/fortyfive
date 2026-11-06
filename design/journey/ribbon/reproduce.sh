#!/bin/sh
# Re-render every study from its pinned render.sh into a temp dir and compare sha256
# with the committed PNG and the manifest. Independent of anything the generator wrote.
set -eu
root=$(cd "$(dirname "$0")" && pwd)
out=${1:-$(mktemp -d)}
echo "out=$out"
fail=0
for d in "$root"/0*/; do
  name=$(basename "$d")
  ( cd "$d" && python3 ../ribbon.py --study "$name" \
      --seed "$(sed -n 's/.*--seed \([0-9]*\).*/\1/p' render.sh)" \
      --out "$out/$name.png" --manifest "$out/$name.json" --preview-360 "$out/$name-360.png" >/dev/null )
  a=$(sha256sum "$out/$name.png" | cut -d' ' -f1)
  b=$(sha256sum "$d/preview-1x.png" | cut -d' ' -f1)
  c=$(python3 -c "import json,sys;print(json.load(open('$d/manifest.json'))['sha256'])")
  if [ "$a" = "$b" ] && [ "$b" = "$c" ]; then
    echo "OK   $name  $a"
  else
    echo "FAIL $name  fresh=$a committed=$b manifest=$c"
    fail=1
  fi
done
exit $fail
