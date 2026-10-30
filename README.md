# TrainPilot Native V2

**Tiszta induló projekt az AI Studio számára** — a teljes natív Android és Wear OS alkalmazáskódot **az AI Studio hozza létre**. Ez a repó jelenleg csak a jóváhagyott követelményeket, a natív UI/UX-terveket és a paritásvizsgálat referenciaanyagát tartalmazza.

## AI Studio: innen indulj
- **[AGENTS.md](AGENTS.md)** – minden módosításra érvényes fejlesztői szabályok
- **[AI_STUDIO_START_HERE.md](AI_STUDIO_START_HERE.md)** – a teljes feladat és prioritások
- **[Natív mockupok](design/mockups/REFERENCES.md)** – 4 képtábla, 24 mintanézet
- **[Képernyőszerződések](design/specifications/02-SCREEN_CONTRACTS_24.md)** és **[vizuális rendszer](design/specifications/01-VISUAL_SYSTEM.md)**
- **[Eredeti TrainPilot funkciólista](docs/02-MEGLEVO-FUNKCIOK-1.3.71.md)** és **[tételes audit](docs/03-RESZLETES-FUNKCIOLELTAR.md)**
- **[Jóváhagyott felső 5+3 navigáció](docs/01-NAVIGACIO-5-PLUS-3.md)**

## Megőrzendő architektúra és adat
Az eredeti appban meglévő edzésprogramok, élő edzés, GPS, Wear OS, Health Connect/Samsung Health, Coach, PR, napló, privát média, Google Drive/Naptár, backup és offline képességek teljes körű újramegvalósítása a cél. **Nem demo/placeholder alkalmazást kérünk.**

## Változtatási határok
- Ez a **V2 repo** az egyetlen írható munkaterület.
- A [TrainPilot eredeti repó](https://github.com/fuloplevente1998/TrainPilot) változatlan marad; a [korábbi AI Studio repó](https://github.com/fuloplevente1998/TrainPilot-AI-Studio) csak referencia.
- A felhasználó régi edzésadatait egyszeri, ellenőrzött importtal lehet áthozni. **Nem kell kétirányú, folyamatos main–V2 szinkron.**
- A végleges megjelenés a natív képtáblákból indul, **de** az új navigáció felülírja a képeken lévő régi alsó tabsort.

## Kötelező teljes funkciók — külön ellenőrzési lista

- **[106 eredeti gyakorlat és 13 kész program adatkészlettel](docs/08-KATALOGUS_ES_13_PROGRAM.md)**
- **[Teljes, tesztelhető funkcióparitás: profilok, Coach, RIR, edzésstílusok, saját programok, GPS-es térkép, Wear, integrációk](docs/07-TELJES_FUNKCIOPARITAS_ELFOGADAS.md)**

A katalógus teljes, tényleges négynyelvű adatállománya a [data/reference/native-catalog-main-7473.json](data/reference/native-catalog-main-7473.json) fájlban van; a többi alkalmazáskódot az AI Studio hozza létre.
