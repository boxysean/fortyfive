# Logo provenance

The three SVG wordmarks in this directory are the vendors' own published assets,
downloaded unmodified on 2026-11-04. Nothing here is redrawn or re-tinted at the
source: the white and monochrome variants are produced at render time by the
compositor, not by editing these files.

| File | Source URL | Size | sha256 |
| --- | --- | --- | --- |
| `dbt-labs.svg` | https://www.getdbt.com/img/logos/dbt-labs-logo.svg | 391 × 90, dbt orange `#FE6703` and black | `503e8011a2b6520430f32ab5ebc16fd061390b1208081e7e2cc1702066ef4c47` |
| `fivetran-dark.svg` | https://cdn.prod.website-files.com/6130fa1501794ed4d11867ba/63d9599008ad50523f8ce26a_logo.svg | 115 × 32, Fivetran blue `#306BEA` | `abec08cde9830c6b73ef0b424755341dfe8bdb5569304712ce8ecd86bc891a7a` |
| `fivetran-white.svg` | https://cdn.prod.website-files.com/6130fa1501794ed4d11867ba/63d92f202436dcbbcced29b8_logo-white.svg | 115 × 32, white | `e6f97b315feb6b2c8dd9ce9d1b834009ecf57ba9fb26879461615200925aa231` |

Verify with:

    shasum -a 256 banners/assets/*.svg

Each banner's sidecar JSON repeats the sha256 of the asset it used, so a banner
PNG can be traced back to the exact bytes that were composited into it.

Both wordmarks are trademarks of their owners and are used here to identify the
vendors they name.
