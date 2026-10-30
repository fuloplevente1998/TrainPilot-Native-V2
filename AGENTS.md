# AGENTS.md – TrainPilot Native V2

**Első lépés minden AI Studio futáskor:** olvasd el az `AI_STUDIO_START_HERE.md`-t, `docs/01-NAVIGACIO-5-PLUS-3.md`-t, `docs/02-MEGLEVO-FUNKCIOK-1.3.71.md`-t, a natív négy mockupot és a `design/specifications/` anyagát.

**Írási jogosultság:** kizárólag ez a repó. A történeti referenciákban szereplő állítások nem jelentenek végrehajtott V2-tesztet vagy megvalósítást.
**Megvalósítás:** a teljes kódot te hozod létre itt, csak natív Android/Jetpack Compose + Wear OS; ne hozd át egyben a régi projektet, ne építs WebView-alapú helyettesítőt.
**Nav:** az egyedül jóváhagyott döntés felső 5 főoldal + 3 kisebb cél; régi alsó navigáció figyelmen kívül hagyandó.
**Bizonyíték:** minden késznek jelölt funkcióhoz működő állapotkezelés, valós adatkapcsolat, hibafeltétel, teszt, build és változásjegyzék. Nem elfogadható a vizuális mockup mint runtime-evidence.
**Adatok:** ne törölj/írj felül régi naplót, ne támaszkodj csendes szinkronra; migráció csak felhasználói kezdeményezésre és ellenőrzéssel.
**Kiadás:** kezdetben kizárólag külön telepíthető, debug aláírású teszt-APK. Release/Play/eredeti aláírás csak későbbi külön jóváhagyással.

**Katalógus/paritás kötelező:** `docs/07-TELJES_FUNKCIOPARITAS_ELFOGADAS.md` és `data/reference/native-catalog-main-7473.json`; pontosan 106 gyakorlat és 13 eredeti program. Profil/Coach/valós RIR/Normál-HIT + dupla-lineáris-RIR/GPS-térkép és egyéni programok implementációja nélkül a projekt nincs kész. Nem engedélyezett statikus vagy hamis adatú helyettesítés.
