# 00 — comparison: five ribbon studies

Five studies of the same idea: parallel traces on an invisible twisting ribbon, drawn only
as longitudinal lines that fade by `|n · view|`. 1000 × 1400, ink `#E9EEF7` on `#0B0F16`,
one accent trace `#FE6703`, no surface, no outline, no shading term. Every folder
reproduces its own `preview-1x.png` byte for byte: `sh design/journey/ribbon/reproduce.sh`
re-renders all five from their pinned seeds and checks the hashes against the manifests.

## The verdict

**Build 05-pinch.** It is the only study where the drawing already carries the page's
structure: three lobes and two pinches, and the pinch is a real event — 33 traces converge
into a single point and the band leaves and re-enters as a line. That is exactly where the
chapter labels want to sit, so the composition does the labelling work instead of the
typography. It is also the most robust at 360 px: the lobes and the pinches still read, and
the traces stay separate lines rather than merging into a bar.

**The interesting failure is 03-interleaved.** Two ribbons on one centreline read as one
band with a mesh in it, because the narrow ribbon always lies inside the wide one and no
twist rate can turn that overlap into a crossing. Separate them laterally and they read as
two bands, but then they never share a pixel, so there is no crossing either. Both
versions are measured in `03-interleaved/notes.md`. A crossing that reads as a knot needs
the overlap to be short — roughly one band-width tall — which means a fast swing in the
lateral offset around the midpoint, and an occlusion rule (the near ribbon blanking the far
one) that this renderer deliberately does not have.

**Runner-up is 02-half-turn**, and it is the one that proves the technique. The band wrings
itself down to a single edge-on thread at about y = 1100 and opens again below it: the fade
rule does all the work and the shape is never drawn. It is a better picture than 05 and a
worse section, because it is one gesture rather than three chapters.

**01-quarter-turn** is the control and it is honest about the limit: a quarter turn on a
flat ribbon only shows as a change of apparent width (400 px to 200 px), so it reads as a
sheet swinging, not as a twist. Nothing is wrong with it; a quarter turn is simply not
enough of an event to carry a full section.

**04-gradient-mesh** has the best surface quality of the five — spacing that tightens to
one edge reads as light falling across a curve, with no shading term at all — but its dense
edge merges into a solid rail at 360 px. It is the right ingredient, not the right
composition.

| folder | seed | traces | twist | pure ground | ink px |
|---|---|---|---|---|---|
| 01-quarter-turn | 5101 | 29 | −20° → +70° (0.25 turn) | 86.1% | 188,446 |
| 02-half-turn | 5102 | 43 | −45° → +135° (0.5 turn) | 86.5% | 184,699 |
| 03-interleaved | 5103 | 21 + 21 | A 0° → +180°, B +90° → −90° | 84.4% | 213,257 |
| 04-gradient-mesh | 5104 | 37 | −25° → +85° (0.31 turn) | 85.2% | 200,072 |
| 05-pinch | 5105 | 33 | −15° → +85° (0.28 turn) | 86.7% | 181,233 |

## What the five establish

1. **The technique works.** Parallel traces plus `|n · view|` gives a readable solid with
   no surface: the band is bright where it faces the camera, and it dissolves where it
   turns edge-on. 02 is the proof — the waist is the silhouette, and no line is ever drawn
   to make it.
2. **On a flat ribbon, twist and width are the same variable.** `|n · view|` is `|cos φ|`
   and the apparent width is `W · |cos φ|`, so a study only reads as a twist if it changes
   the width a lot (05) or takes the band all the way to edge-on (02). 01 does neither
   strongly enough, which is why it looks like a fan rather than a ribbon.
3. **The pinch is the only structural device that survives 360 px.** Line count, spacing
   and twist rate all soften at phone width; a width that goes to zero does not.
4. **Two ribbons need two centre lines and an occlusion rule.** Interleaving is not a
   twist-rate problem.

## The first hour of real work on 05-pinch

1. **Re-time the pinch to the visible page.** The spine overhangs the canvas, so the page
   only shows t ≈ 0.08–0.92 and the band enters already ~400 px wide. Compress the pinch
   profile into the visible range and put the two pinches at exactly one third and two
   thirds of the section, so the drawing opens as a line, opens out, pinches, opens,
   pinches, and closes as a line.
2. **Phase the twist to the pinches.** Set `φ = 90°` at each pinch (and alternate the sign
   between lobes) so the band is edge-on exactly where it is also zero-width. The chapter
   boundary then becomes two events at once — the width closes and the fade takes the last
   of the ink — which is the one thing 02 does better than 05 and costs one line of
   arithmetic.
3. **Take 04's spacing gradient** (`u = -1 + 2·s^1.6`) so the band has a dense edge and
   reads as lit between the pinches. Loosen it from 1.9 to about 1.6 so the dense edge does
   not merge into a rail at 360.
4. **Only then set type.** The label sits in the pinch, in the gap the band leaves, not on
   top of the traces. Nothing in these five studies draws text, and nothing needs to: the
   pinch is already a text slot.

The section is one PNG per breakpoint, no motion, no engine: the generator is 280 lines of
stdlib Python and Pillow, and it renders in under a second per study on this box.
