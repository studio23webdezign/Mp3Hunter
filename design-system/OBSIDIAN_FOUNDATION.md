# VIBE PLAYER — Obsidian design foundation

This directory records the visual language and asset contracts before screen implementation.

## Direction

Obsidian Luxury: premium audio hardware, editorial typography and cinematic media presentation. The interface should feel calm, tactile and deliberate—not like a generic neon dashboard.

## Core palette

| Token | Hex | Intended use |
|---|---|---|
| Obsidian | #101113 | App canvas |
| Carbon | #191B1E | Primary surfaces |
| Graphite | #272A2E | Raised surfaces |
| Titanium | #858B92 | Secondary labels and controls |
| Pearl | #E9E7E1 | Primary text |
| Champagne | #D3B77C | Primary accent and selected states |
| Muted Champagne | #927B50 | Subtle accent details |

## Layout rules

- Use an 8 dp spacing rhythm.
- Keep page gutters consistent at 24 dp on standard phones.
- Use 20 dp panel corners, 14 dp artwork corners and 12 dp button corners.
- Avoid stacking bordered cards inside bordered cards.
- Reserve champagne for meaningful actions and selected states.
- Let artwork provide colour; do not decorate every surface with gradients.
- Keep playback controls reachable by one hand.
- Respect system insets and large font settings.

## Typography

Poppins is the intended typeface. Font files must be added under app/src/main/res/font/ in the asset pass and wired into the theme. Until then, use the platform sans-serif fallback; do not fake Poppins by changing letter spacing.

## Asset contract

- Icons: individual vector assets where possible; transparent PNG only for artwork that cannot be represented cleanly as vectors.
- Artwork: separate square source files, minimum 1024 × 1024.
- Background textures: subtle, tileable and never used behind dense text.
- No emoji as production icons.
- Every asset must have a stable, descriptive filename.

## Component contract

Shared primitives live in ui/components/. Screen-specific styling must consume VibePalette, VibeSpacing, VibeRadii and VibeTypeScale; avoid new one-off colour literals.

## Screen implementation order

1. Player shell and navigation
2. Home / library
3. Now playing
4. Video library and video player
5. Queue and playlists
6. Equalizer and settings
7. Sharing and social features

## Quality gate

A screen is not complete until loading, empty, error, selected, playing, paused and accessibility states have been considered. Validate on compact and large Android phones before release.
