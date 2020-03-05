SMX Workshop Beta 1.4 Help

About
====================================================
This is used to load, create or modify sprite files of various editions of Age of Empires 1 & 2, including AOE and AOK's SLP files, AOE: DE's SLP files and AOE2: DE's SMP or SMX files. You can convert between different formats, import from images and export to iamge files, or apply many other interior modification functions.

Open/Save Files
====================================================
Besides open file browser via menu or hotkey Ctrl+O, you can also drag and drop sprite files into the window to open them.
At present, the tool supports those files: SLP 2.0N(of AOE and AOK), SLP 4.0X(of AOE: DE), SMP and SMX(both of AOE2: DE). However, you can't save as SLP 4.0X or SMP files now.
Different formats have respective features:
SLP 2.0N - Only 256 levels of normal color, 16 levels of player color, one level shadow and one level of outline. Shadow and outline can't overlap main image.
SLP 4.0X - Only 256 levels of normal color, player color and shadow. Shadow can overlap main image.
SMP - 1024 levels of normal color and player color, 256 levels of shadow, one level of outline. Shadow and outline can overlap main image. Main image can attach 1024 levels of smudge data.
SMX - Similar with SMP, except smaller size.
Sprite contains main image, shadow, outline and smudge data. There are:
Main image - Primary data of sprites, including normal image and player image, the latter can show different colors of players. In classic edition games, only one palette is used to draw images, while player colors are parts of the palette. In both definitive editions, multi palettes can be adopted on different sprite, and palette for player colors are independent from main colors.
Shadow - Semi-transparent black image. Different levels stands for different opacity, higher is darker. It would darken pixels below in game. In DEs, shadow is also used to smooth borders of main image. When SLP is loaded, the single level will be regarded as level 128.
Outline - Units covered by buildings or trees in game will show player colored outline, usually shaped by main image. Outline is always only one level.
Smudge - Addition to normal image when buildings are damaged. The more buildings damaged, the darker it shows. The smudge data is bounded with main image data.
There are also two edit modes named SLP and SMX. Similar with same named format, combined data in one layer or separated.

Import/Export Images
====================================================
Just as open sprite files, drag and drop image files can import them too.
Supported files include BMP, PNG, JPEG and GIF, while GIF can only read first frame. But we usually use PNG file, because BMP doesn't support transparent alpha channel, JPEG compresses data causing lost pixels, while GIF can only store up to 256 colors. Exporting supports same file formats.
Owing to complex data of each frames, simple picture can't contain all information. So we provided some image modes to preserve them: combined shadow, separated shadow and so on.
When exporting images, if certain data is selected, picture will expand to several times width. Arranged from left to right are main image, shadow, outline and smudge. If combined main image and shadow, they'd be in same grid, shadow is under main image.
If no background color, levels of shadow and smudge are determined by alpha channel value, outline is also set in this way. If background is set, data will be stored by brightness. So it is always black bakground, different from main image. When you import some images within background mode, the background color must be the top-left corner pixel's color. The right number field in import image dialog is tolerance, meaning that colors differ from background color in given range will be removed too. So it is usually set as 0.
In general modes, pixels whose alpha channel values less than 255 may be converted to shadow, if their brightness less than 128. Darker pixels have higher alpha value threshold in convertion, avoid generating black borders around light main image. Player color can be select from foure versions, and you can set the tolerance from normal colors, making pixels easier to match player colors.
When mode is "distinct by alpha", not only main image and shadow are combined, but also they'll set alpha channel in different way: Player color is 255, normal color is 254, the other shadow. Otherwise, both player color and normal color is of 255 alpha channel value.
And you can combine multi exported frames to one picture, or divide one in import. That makes fewer files and more convenient to edit. When numbers of rows and columns are assigned, import or export images will be regarded as strips, grid size equals to maximum size of frames. For example, if you export a sprite with 160 frames by 8-by-8, it will create 3 image files. The third image is still 8-by-8, but has only 32 frames, half of the image is blank.
There are three anchor modes in the tool. If you choose "Tight, unaligned", all frames will ignore anchor location(but not for interior datas in each frame), they stay at left-top corner of grids. Align makes frames algined by anchor points among grids. If you choose "Align, centered", frames will also put their anchor points at centers of grids, this is what default import mode does too. Images are larger from first to third modes, but more anchor information is saved in image files. You can save anchor information as CSV files when export, or load them in import. Names of anchor files are corresponding ones with .CSV postfix, so they have double extension names.

Palettes
====================================================
Despite we haven't tested, both DEs seem to be able to use different palettes in different frames of one sprite.
If preview of one frame is wrong colored, you should set its palette in the menu. So the main image data changes nothing, but palette is changed. If you want to change the palette of a correct image to another, click destination palette in "Change Palette to" menu. This will map source palettes to the destination palette, changing colors to the nearest new ones.
Different from normal image palettes, one sprite can only have one player color mode, and this is only used in edit, not saved in files. So you have to convert all frames' colors. Player color versions include AOE(10 levels), AOK(8 levels), AOE: DE and AOE2: DE(both 256 levels).
You can add custom normal palettes, put files on directory Palettes/Custom/. Supported formats are PAL & PALX in game(describe by text), and ACT of Photoshop(describe one color by three bytes). Custom palettes are indexed from 512, but you are not supposed to save sprites with them directly, please convert to original ones before saving.

Modify
====================================================
If you selected some frames, you can copy, delete, reverse them, or change their orders in sequence. If selected frames are not serial, they will become serial after shift.
"Rotate" functions can rotate by certain angle, or flip horizontal or vertical by their anchors. "Scale..." changes image sizes of frames. If interpolation is set, linear algorithm will be used instead of nearest. It brings better image quality, but more time cost. "Crop" will trim blank paddings around all datas, making selection mask and file size smaller.
There is two ways to set anchor locations, via dialog or mouse motion. The former can adjust one or all of main image, shadow and outline, is useful to fix mistakes of shadow anchors. If set all datas' anchors in absolute, it changes main images first, then move other datas in relative. The latter lets you left-click in left view to set or right-drag to move anchors.
"Convert Shadow" in tools menu will convert different levels of shadow to level 128 or black(darkest color in palette in fact) main image or both. It uses pixel pattern to simulate gradient shadow. Range is minimum and maximum of gradient, pixels out of range will be converted to transparent or fully filled image. Number of levels determines pattern types, more levels makes transition more smooth, but patterns will be rougher. The last one "Special 4" is a optimized mode of normal 4 levels, with better 1/4 and 3/4 patterns, but transition border is a little worse.
When you add outlines to frames, it will remove all original outline datas. Outlines refers to main image and only outermost is generate in default.

Interface
====================================================
Left of the window is current frame image, you can toggle display of main image, shadow, outline, smudge and their bounding boxes. Left click to toggle animation mode, same as one of the menu item. Right drag can move viewport to see more parts. Mouse wheel will scale view, can achieve more levels than menu items.
Right of the window listed all frames with thumbnails. Mouse wheel or scroll can slide the list. click thumbnails will select current frame. If pressed ctrl key, you can select multi frames. Shift will select serial frames. Selected frames can be changed in the same time, but only first selected frame is shown in left.
Like Turtle Pack, you can move anchors by ctrl and arrow keys. Page up and page down can select previous or next frame. Press ctrl key to jump to first or last frame.