# TrainPilot Native V2 — kötelező teljes funkcióparitás, nem demó (2026-10-30)

**Állapot: követelményátadás, nem implementációs készültség.** Az első natív próbálkozás bizonyítottan nem elég teljes; a V2 csak akkor tekinthető késznek, ha az alábbi teljes lista és a `docs/03-RESZLETES-FUNKCIOLELTAR.md` valamennyi eleme ténylegesen működik és ellenőrzött. Nem elfogadható sem az üres képernyő, sem a mockupot utánzó, de adatot nem kezelő demo. A lista nyitott kiegészítésre: nem felső korlát és nem helyettesíti a 81 történeti követelményt.

## P0 — az összesen 106 gyakorlat és 13 kész program
- A `data/reference/native-catalog-main-7473.json` **a teljes, tényleges, eredeti 1.3.71-es adatcsomag**, nem puszta dokumentáció. Ebből a V2-nek beépített, többnyelvű katalógust és 13 előre elkészített programot kell előállítania. **Ne generálj új, eltérő 106-os listát.**
- Pontosan **106 egyedi stabil exercise ID**. Kötelező magyar, angol, német, román név és leírás; izomcsoportok, eszközigény, terheléstípus, mérés jellege, alap sorozat/cél/terhelés/pihenő, hitelesített videólink vagy őszinte nem elérhető állapot. Keresés, izomcsoport/felszerelés szerinti szűrés, kedvencek, részletnézet, kedvenc megőrzése backupban.
- Pontosan **13 előre elkészített program**, mindegyik tényleges nappal és érvényes exercise ID-kkel; teljes HU/EN/DE/RO program/nap címek. Név, leírás, napok, gyakorlatok, sorrend. Mindegyik megtekinthető, indítható és tudatosan aktiválható. A felhasználó módosításai nem íródnak felül seed/migráció miatt.
- Saját gyakorlat: létrehozás, szerkesztés, törlés, mérés- és terheléstípus, megjegyzés, személyes gyakorlatok belistázása.
- Saját programok: létrehozás, sablonból másolat, átnevezés, nap hozzáadás/törlés/átrendezés, gyakorlatválasztás/csere/kizárás/sorrend, sorozat/ismétlés/súly/pihenő/cél szerkesztése, mentés/visszavonás, aktiválás, edzésindítás.
- Beépített sablonból keletkezett személyes A/B napok gyakorlatainak célzott visszaállítása csak előnézet/jóváhagyás után, ne törölje az egyéb saját programot vagy naplót.
- Újraindítás és ismételt katalógus-betöltés után az adatkészlet és a személyes módosítások stabilak; 106+13 önmagában nem elég, ha program→gyakorlat kapcsolatok töröttek.

## P0 — profil, személyre szabás, tervezés
- Valódi szerkeszthető **edzésprofil**: cél, tapasztalat, aktivitás, edzés helye, heti ritmus, bontás, fókusz, életkor, testmagasság, testsúly, alkalom hossza, rendelkezésre álló eszközök, kizárt gyakorlatok, kímélendő testrészek. Mentés, visszaolvasás, validálás, megőrzés beállításban/mentésben, nincs rejtett profil-adatvesztés.
- A profil alapján az **Otthoni A/B Alap és 2. szint** program személyre szabása: felszerelés, terhelési tapasztalat, kizárások, időkeret, korábbi teljesített edzés előnézetes figyelembevétele. A jóváhagyás előtt részletes terv/eltérés/kizárás/gyakorlatcsere látszik; a profil szerkesztése nem írja át automatikusan a programokat vagy naplót.
- Beépített program, profilból létrehozott személyes program és kézzel készített program között egyértelmű eredet és állapot; kiválasztott program és nap megőrzése.

## P0 — edzésstílusok és teljesítményprogresszió
- Két edzésstílus: **Normál** és **HIT-inspirált**. Három külön progressziós mód: **dupla**, **lineáris**, **RIR/Coach**. Globális alapbeállítás és külön program-specifikus felülbírálás.
- Változtatás előnézettel és kifejezett jóváhagyással, visszavonás/reset lehetőségével; a beállítás megváltoztatása nem írja felül a már naplózott edzést vagy a folyamatban lévő sessiont.
- **0–10 tényleges RIR minden sorozathoz, opcionálisan**: `null`/ismeretlen különbözik a `0`-tól, cél-RIR különbözik a mért RIR-től; mentés és read-back, naplórészletben megjelenítés, Coach felhasználás, export/import. Edzés közben gyors, hozzáférhető RIR-választás és szerkesztés.
- Súly/ismétlés/cél, befejezett sorozat és legutóbbi azonos gyakorlat alapján automatikus súly/ismétlés-előtöltés mind tervezett, mind gyors edzéshez, kézi felülírás/draft megőrzéssel. Tilos véletlenül késznek jelölni az előtöltött sorozatot vagy régi RIR-t/mérést átörökíteni.
- Működő pihenő, időzítő, +/-15 mp, szünet/folytatás, skip, hang/rezgés/haptika, set OK/undo, edzés véglegesítés, megszakítás és adatmentés; kétoldali (bal/jobb), időalapú és távolságalapú gyakorlatok.

## P0 — Coach, progresszió, egészség
- Nem statikus AI-tanácskártya: tényleges korábbi edzések, terhelés, visszajelzés (könnyű/nehéz/fájdalom), RIR, regenerációs adatok és a program célja alapján működő tanács. Különbség: emelés/tartás/csökkentés, ismétlésszám módosítása és testtömeges nehezebb variáció.
- Bizonytalan/hiányos/idejétmúlt egészségmérésnél nem szabad fiktív pontot, pulzust, HRV-t, alvást vagy Samsung Energy Score-t mutatni. Az elérhető forrás és frissesség látszik; hiteles mai Samsung pontszám prioritása a saját becsléssel szemben, visszaesési logikával.
- Coach-javaslat és tényleges programmódosítás két külön esemény. Elfogadás/jóváhagyás, előnézet, kézi ellenőrzés és visszavonás. Sérülés/fájdalom esetén terhelési korlátok. Nincs automatikus csendes progressziónövelés.
- Fejlődés: PR-jelvény és részletek, korábbi→új rekord, volumen, becsült 1RM címkével, heti/havi grafikonok, izomcsoport terhelési ábra, idő/ismétlés/táv/edzésszám, privát edzésfotók, testsúly és egészségnapló.
- Health Connect, közvetlen Samsung Health ahol engedélyezett/ténylegesen elérhető, órás mérés forrásainak megkülönböztetése, engedélyek, retry, naplózás és kézi frissítés; minden adat tárolva és forráshoz kötve.

## P0 — térképes GPS és összes kardióedzés
- Telefonon és támogatott órán választható GPS-es kültéri edzés: futás, túra/séta, kerékpár és minden katalógusban szereplő megfelelő távmérésű aktivitás, illetve helyesen jelölt beltéri idő/táv mód; a lista nem korlátozható pusztán három bemutató példára.
- Mérés: tényleges eltelt idő, táv, tempó/sebesség, opcionális pulzus/lépés/energia megfelelő forrásból; GPS engedély/vétel/pontosság, indítás/szünet/folytatás/explicit leállítás, app háttérbe kerülése, rendszermegszakítás, újranyitás, telefon/óra ownership.
- A GPS **útvonal-koordinátákat** privátan és megfelelően összekötve menti, a naplózott edzésnél teljes képernyős térképen mutatja. Nem elég egy statikus térképkép, szöveges táv vagy kitalált vonal. Útvonalhiány és térképszolgáltatás-hiba őszinte állapot, piros X csak a térképet zárja.
- Rögzítés csak kifejezetten sikeres lezárás után, nincs implicit automatikus készre jelölés. Privát útvonal nem kerül automatikusan Google Drive backupba; export és adatvédelmi szabály külön.
- GPS nélküli beltéri időmérés/intervallum és manuális távadat rögzítése is működik.

## P0 — tervező, napló, naptár, Wear, adatbiztonság
- Naptár: edzésnapok ütemezése/módosítása/törlése, edzésszám-/táv-célok, kihagyott edzések biztonságos áttervezése előnézettel, Google Calendar opcionális integráció; ütközés és offline mód.
- Edzésnapló: teljes részletek gyakorlatonként/sorozatonként, dátum/PR/GPS szűrők, sorrend, szerkesztés és törlés megerősítéssel, idempotens újranyitás, valódi naplóhoz kapcsolódó eredmény/útvonal/fotó.
- Wear OS: külön óraalkalmazás, önálló/offline edzés, telefon↔óra ack/handoff, egyetlen aktív session/timer-owner, mini napló, Tile/komplikáció, korona/lünetta szerkesztés, érintészár és három másodperces feloldás, adatösszevonás/undo.
- Helyi offline adatbázis, JSON/ZIP mentés-visszaállítás, egyszeri régi appból import (nem élő szinkron), opcionális Google Drive appDataFolder és Calendar jogosultságkezeléssel, törlési/ütközésvédelemmel.
- HU/EN/DE/RO; 32 paletta és témakezelés, tipográfia, 320dp/2x betűméret, teljes értékű TalkBack/hozzáférhető 48dp gombok.

## Elfogadás — minden sorra bizonyíték, semmi késznek jelölt placeholder
1. Katalógus unit teszt: **106 különböző ID**, 106×4 nem üres név és leírás, referenciaadat pontos egyezése. A 13 programnál minden nap minden gyakorlata feloldható; semmi változás seed második futásakor.
2. Gyors programteszt: 13 kész program látható, aktiválható, indítható; új saját program létrehozható, menthető, klónozható, törölhető; profil-alapú A/B előnézet és jóváhagyás működik.
3. Edzésmód és RIR teszt: Normál/HIT × dupla/lineáris/RIR program-szinten, 0–10 és null, session→napló→export/import körút, manuális felülírás és undo.
4. Coach teszt: nincs adat → nincs hamis score; valós egészségadat frissességgel, fájdalom-tiltás, progressziós javaslat és külön jóváhagyás.
5. GPS teszt: engedélytagadás, start–pause–resume–stop, visszajelzett route pontok és naplótérkép, background és megszakítási szituáció, elhibázott megállítás nem teljesítés.
6. Naptár/Wear/import teszt: offline és ownership, edzésnap/PR/fotó megőrzése, deduplikáció, visszaállítás; készülékes vizsgálat nem helyettesíthető Gradle-zölddel.
7. Mind a 24 mintanézet valós state- és callback-bekötéssel; mind a 81 megőrzési ID ellenőrizve, plusz az összes tétel a teljes leltárból. Valós Android screenshot, 320dp, betűméret 2.0, HU/EN/DE/RO és világos/sötét témák.

**Kötelező státuszsor egyes funkciókhoz:** NOT_STARTED / IMPLEMENTED / UNIT_TESTED / INTEGRATION_TESTED / DEVICE_VERIFIED. A V2 jelenlegi kezdeti státusza **NOT_STARTED minden implementációra**. Vizuális komponensből nem következik valós funkció.

## Kapcsolódó források
- [V2 pontos katalógus](../data/reference/native-catalog-main-7473.json)
- [Katalógusátadás és sablonok](08-KATALOGUS_ES_13_PROGRAM.md)
- [Eredeti részletes leltár](03-RESZLETES-FUNKCIOLELTAR.md)
- [24 nézet szerződése](../design/specifications/02-SCREEN_CONTRACTS_24.md)
- [81 követelmény](../design/specifications/05-FEATURE_PRESERVATION_81.md)
