# V2 — egyszeri személyes adatátvitel, nem általános cross-app szinkron

A V2 új felhasználóknak önálló app és saját helyi adatséma. A meglévő TrainPilot felhasználó személyes naplójának áthozatala **egyszeri, kifejezetten jóváhagyott import**, nem a régi main és a V2 közötti állandó élő szinkron.

## Kötelező adat-megőrzés
Edzésnapló, sorozat/mértékegység/terhelés, PR-k, edzésprogramok és programnapok, dátumok, testsúly/egészségnapló, beállítások, edzésfotók ahol a backup megengedi, egyéb hordozható metaadatok. Tartós azonosítók, időzóna és tizedesértékek veszteség nélkül.

## Felhasználói folyamat
1. A régi appban hiteles, teljes JSON/ZIP backup készül; az eredeti adat és telepítés érintetlen marad.
2. A V2 import előnézetben ellenőrzi a backup-formátumot, entitásszámokat, dátumokat és PR-t, valamint a nem támogatott mezőket.
3. Felhasználói jóváhagyás után atomikus helyi import/rollback; megszakított műveletből biztonságos folytatás vagy visszaállás.
4. Újraimport esetén stabil ID és determinisztikus deduplikáció: nincs duplázódó napló/PR/terv.
5. Read-back és egyezőségi jelentés, a régi alkalmazás törlése nélkül.
6. A személyes import **nem kötelező** új felhasználóknak.

A GPS privát útvonal tárolása és a fotók/felhős adatok biztonsági korlátai a korábbi funkciók és forrásszerződések szerint külön ellenőrzendők. A V2 csomagazonosítója kezdetben különbözik az eredetiétől, ezért nem olvas közvetlenül másik app sandboxjába.
