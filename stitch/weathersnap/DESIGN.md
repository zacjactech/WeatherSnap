---
name: WeatherSnap
colors:
  surface: '#0e1323'
  surface-dim: '#0e1323'
  surface-bright: '#34394a'
  surface-container-lowest: '#080d1d'
  surface-container-low: '#161b2b'
  surface-container: '#1a1f30'
  surface-container-high: '#25293a'
  surface-container-highest: '#2f3446'
  on-surface: '#dee1f9'
  on-surface-variant: '#c1c7d3'
  inverse-surface: '#dee1f9'
  inverse-on-surface: '#2b3041'
  outline: '#8b919d'
  outline-variant: '#414751'
  surface-tint: '#a4c9ff'
  primary: '#a4c9ff'
  on-primary: '#00315d'
  primary-container: '#4d93e5'
  on-primary-container: '#002a51'
  inverse-primary: '#0060ac'
  secondary: '#80d2e9'
  on-secondary: '#003641'
  secondary-container: '#007084'
  on-secondary-container: '#b3edff'
  tertiary: '#ffb951'
  on-tertiary: '#452b00'
  tertiary-container: '#c2841a'
  on-tertiary-container: '#3c2500'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#d4e3ff'
  primary-fixed-dim: '#a4c9ff'
  on-primary-fixed: '#001c39'
  on-primary-fixed-variant: '#004883'
  secondary-fixed: '#aeecff'
  secondary-fixed-dim: '#80d2e9'
  on-secondary-fixed: '#001f26'
  on-secondary-fixed-variant: '#004e5d'
  tertiary-fixed: '#ffddb3'
  tertiary-fixed-dim: '#ffb951'
  on-tertiary-fixed: '#291800'
  on-tertiary-fixed-variant: '#633f00'
  background: '#0e1323'
  on-background: '#dee1f9'
  surface-variant: '#2f3446'
typography:
  display-temp:
    fontFamily: Hanken Grotesk
    fontSize: 96px
    fontWeight: '700'
    lineHeight: 100px
    letterSpacing: -0.04em
  display-temp-mobile:
    fontFamily: Hanken Grotesk
    fontSize: 72px
    fontWeight: '700'
    lineHeight: 76px
    letterSpacing: -0.04em
  headline-lg:
    fontFamily: Hanken Grotesk
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Hanken Grotesk
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
  body-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 28px
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  label-caps:
    fontFamily: Geist
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.08em
  metadata:
    fontFamily: Geist
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  margin-mobile: 20px
  margin-tablet: 32px
  gutter: 12px
  unit-x: 4px
  stack-sm: 8px
  stack-md: 16px
  stack-lg: 32px
---

## Brand & Style

The design system is engineered for high-precision environmental observation. It prioritizes professional utility with a cinematic, atmospheric execution tailored for Android’s high-density displays. The target audience includes field researchers, meteorologists, and outdoor professionals who require rapid data density and high legibility in low-light environments.

The visual style is **Minimalist-Atmospheric**. It rejects "glassy" trends and rainbow gradients in favor of deep, layered surfaces that mimic the natural depth of the atmosphere. The interface is characterized by expansive whitespace (negative space), rigorous typographic hierarchy, and a strict adherence to tonal layering to establish depth without excessive decoration.

## Colors

The palette is anchored in a monochromatic midnight foundation (#0B1020) to maximize contrast and reduce eye strain during nighttime field reports. 

- **Primary Blue & Mist Cyan:** Reserved for active data states, precipitation indicators, and core navigation.
- **Accent Amber:** Utilized exclusively for severe weather alerts, warnings, and sunlight-related data points.
- **Surface Strategy:** We use a three-tier tonal system (Background > Surface > Card) to create visual hierarchy. This replaces traditional shadows with color-based elevation, ensuring the UI feels solid and dependable.
- **Semantic Usage:** Success Mint is used for "Safe" conditions and completed report uploads.

## Typography

The typography system is built for extreme legibility and professional metadata density.

- **Display Hierarchy:** Large-scale temperatures use **Hanken Grotesk** with tight tracking to create an editorial, cinematic impact.
- **Utility & Body:** **Inter** handles all descriptive text and weather summaries, providing a neutral, systematic feel.
- **Technical Metadata:** **Geist** is used for all technical readouts (Wind speed, Barometric pressure, GPS coordinates). Its monospaced-leaning proportions ensure that changing numbers do not cause layout shifts and maintain a technical, "instrument" aesthetic.
- **Contrast:** High contrast is maintained between `text_primary` for data and `text_secondary` for labels to allow users to scan for values instantly.

## Layout & Spacing

This design system utilizes a **8px square grid** with a fluid layout model. 

- **Columns:** A 4-column grid for mobile and 8-column for tablet/foldables. 
- **The "Dashboard" Rhythm:** Content is organized into "Observation Blocks." Vertical rhythm is maintained through 32px spacing between major logical sections (`stack-lg`) and 16px between elements within a block (`stack-md`).
- **Touch Targets:** All interactive elements must adhere to a minimum 48x48dp touch target, optimized for one-handed use in the field.
- **Safe Areas:** Ensure bottom-sheet handles and primary actions are cleared from the Android gesture navigation bar.

## Elevation & Depth

In this dark-mode-first environment, depth is achieved through **Tonal Layering** rather than shadows.

- **Level 0 (Base):** #0B1020 - The primary canvas.
- **Level 1 (Surface):** #121A2D - Used for grouped background sections or "well" containers.
- **Level 2 (Cards):** #1A243A - The primary interactive layer.
- **Accents:** We utilize **Low-contrast outlines** (1px solid #2A3652) on card elements to provide crisp definition against the dark background without the "fuzzy" feel of shadows. Shadows are only permitted for floating action buttons (FABs) to separate them from the primary content scroll.

## Shapes

The shape language balances modern software aesthetics with industrial precision. 

- **Primary Radius:** 0.5rem (8px) is the standard for cards and input fields.
- **Large Components:** Bottom sheets and prominent weather summary cards use 1.5rem (24px) top-radius to feel integrated into the mobile hardware.
- **Utility Elements:** Buttons and tags use a higher roundedness (1rem) to distinguish them from data-containers, signaling interactivity.

## Components

### Premium Observation Cards
Cards are the primary container. They feature a 1px border (#2A3652). Within cards, use `label-caps` for headers and `display-temp` or `headline-md` for the primary data point.

### Buttons
- **Primary:** Solid #4A90E2 background with #F4F7FB text. No gradient.
- **Secondary/Outline:** Transparent background with #2A3652 border. 
- **Haptic Feedback:** All buttons must trigger a light mechanical haptic on press to reinforce the "instrument" feel.

### Input Fields
Field containers use #121A2D (Surface) with a subtle bottom-border indicator. Focus states use #7FD1E8. Use `metadata` font style for placeholder text.

### Weather Condition Chips
Small, high-radius chips (pill-shaped) using `label-caps`. For extreme conditions, the background shifts to #FFB84D with dark text to force immediate attention.

### List Items
List items in "History" or "Settings" views should use #1A243A separators (1px height) and contain a minimum 16px horizontal padding. Chevron icons must be reduced to 0.6 opacity to keep focus on the data labels.