GitaVerse fonts (bundled; an offline app has to ship its own)
=============================================================

English and roman script (UI, English, transliteration): Paper Mono  (SIL Open Font License, OFL-PaperMono.txt)
    papermono_regular.ttf   papermono_medium.ttf   papermono_semibold.ttf   papermono_bold.ttf

Hindi, Sanskrit and every Devanagari word: Noto Sans Devanagari  (SIL Open Font License, OFL-NotoSansDevanagari.txt)
    notosansdevanagari_regular.ttf   notosansdevanagari_medium.ttf   notosansdevanagari_semibold.ttf   notosansdevanagari_bold.ttf

File names are lower case with underscores; AppFonts.kt loads them. If a file is missing the system font is used
instead, so the app still runs. Neither font has the dotted IAST letters (ṛ ṣ ṅ ṭ ḍ ṇ ṁ ḥ): Android draws those from
the system font, one letter at a time.
