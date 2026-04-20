
Android application icons (specifically **launcher icons**, which appear on the home screen and app drawer) are **primarily in PNG format**. Here's a clear breakdown to avoid confusion:

### 1. **Legacy Icons (Pre-Android 8.0 / API 26)**
   - **Format**: **PNG** (with transparency support, e.g., for non-square icons).
   - **Why PNG?**  
     PNG supports lossless compression and alpha transparency (critical for icons with soft edges or non-rectangular shapes). JPEG would introduce artifacts and lack transparency, making it unsuitable.
   - **Sizes**: Multiple densities (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi) as separate PNG files (e.g., 48x48px base for mdpi, scaling up).
   - **Location**: `res/mipmap-[density]/ic_launcher.png`.

### 2. **Adaptive Icons (Android 8.0+ / API 26+)**  
   Introduced to support OEM-specific icon shapes (circles, squiggles, etc.).
   - **Format**: **Still PNG**, but structured differently:
     - Two **PNG layers**: 
       - `foreground.png` (the main icon artwork, e.g., your logo).
       - `background.png` (solid color or gradient behind it).
     - An **XML file** (`ic_launcher.xml`) in `res/mipmap-anydpi-v26/` that references these PNGs and defines how they mask/adapt.
   - **Why PNG for the layers?**  
     The foreground/background layers *must* be PNG (with transparency where needed) to allow the system to dynamically mask them into the correct shape. The XML itself is not the image format—it’s a descriptor for how the PNG layers are combined.
   - **Sizes**: 108x108dp base (scales to density-specific PNGs, e.g., 108x108px for mdpi, 216x216px for xxxhdpi).

### Key Clarifications
- **❌ Not SVG**: While you might *design* icons in SVG (for scalability), Android **does not use SVG directly** for launcher icons. You must export to PNG (or use Vector Drawables for *some* UI elements, but **not** for launcher icons—these require raster PNGs due to launcher/home screen constraints).
- **❌ Not WebP/JPEG for icons**:  
  - WebP is supported for *general* app images (e.g., in `ImageView`), but **launcher icons must be PNG** (officially required by Android Studio and the Play Store).  
  - JPEG is **never used** for icons (no transparency, lossy compression causes artifacts).
- **✅ Why PNG is mandatory**:  
  Launchers/home screens expect precise, lossless, transparent raster images to render correctly across all device shapes and themes. PNG guarantees this.

### Practical Advice
- **For new apps**: Use **Adaptive Icons** (PNG layers + XML) as the modern standard. Android Studio’s [Image Asset Studio](https://developer.android.com/studio/write/image-asset-studio) auto-generates these correctly.
- **For legacy support**: Still include traditional `mipmap-*` PNGs (though adaptive icons cover most devices now).
- **Never use JPEG/WebP for launcher icons**—it will cause display issues or be rejected by the Play Store.

### Source
- [Android Icon Design Guidelines (Legacy)](https://developer.android.com/guide/practices/ui_guidelines/icon_design_launcher)
- [Adaptive Icons Guide](https://developer.android.com/guide/practices/ui_guidelines/icon_design_adaptive)

**In short: Yes, the actual image files are PNG.** The XML in adaptive icons is just a configuration layer—it doesn’t replace the PNG format for the icon artwork itself. Stick to PNG for reliability! 🚀
