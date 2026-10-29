# Docs site

Static documentation site, served with GitHub Pages from this `docs/` folder.

Live site: <https://elnix90.github.io/Dragon-Launcher/>

## How it works

- GitHub Pages builds this folder with Jekyll (built into GitHub Pages, no build step or workflow to maintain).
- Every `.md` file here becomes a page, e.g. `Getting-Started.md` -> `/Dragon-Launcher/Getting-Started.html`.
- Layout: `_layouts/default.html`. Navigation: `_data/nav.yml`. Styles: `assets/style.css`. Settings: `_config.yml`.
- `README.md` in this folder is excluded from the site (repo-only notes).

## Adding a page

1. Create `docs/My-Page.md` with front matter:
   ```md
   ---
   title: My Page
   ---

   # My Page

   Content here.
   ```
2. Add it to `_data/nav.yml`:
   ```yaml
   - title: My Page
     url: /My-Page.html
   ```
3. Link to it from other pages using the `.html` file name: `[My Page](My-Page.html)`.

## Privacy policy

`privacy-policy.md` renders at
<https://elnix90.github.io/Dragon-Launcher/privacy-policy.html> - this is the
public URL to paste into the Google Play Console (Store listing -> Privacy
policy). Keep that file name and `docs/_config.yml` `baseurl` unchanged so the
link never breaks.

## Enabling deployment

Repo Settings -> Pages -> Source: **Deploy from a branch**, branch `main`,
folder `/docs`. First build takes a minute or two.
