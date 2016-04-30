### Dauerticket Sharing
Ein Projekt für das Modul Entwicklung interaktiver Systeme von Thomas Friesen und Johannes Kutsch.
#### Problem
Zu bestimmten Zeiten bietet das Dauerticket die Möglichkeit eine zusätzliche Person
kostenlos mit der Bahn mitzunehmen. Momentan gibt es keine einfache Möglichkeit
sich mit fremden Personen zusammenzuschließen um das Ticket gemeinsam zu
nutzen. Des weiteren ist es aufgrund von teilweise überfüllten Bahnhöfen und der
begrenzten Zeit bis zur Einfahrt des Zuges oft umständlich und kompliziert fremde
Personen am Bahnhof ausfindig zu machen.
####Ziel
Das Ziel des Projektes ist es, eine Plattform zu schaffen, welche es ermöglicht Kontakt
zwischen einander unbekannten Benutzern herzustellen, deren Route ganz oder
teilweise miteinander übereinstimmt und so die gemeinsame Nutzung eines bereits
vorhandenen Dauertickets ermöglicht. Außerdem soll es durch das System
vereinfacht werden Personen am Bahnhof ausfindig zu machen.

###Applikation
####Beschreibung
Platzhalter
####Installation
1. [NodeJS herunterladen](https://nodejs.org/en/download/) und installieren.
2. [MongoDB herunterladen](https://www.mongodb.org/downloads#production) und installieren.
3. Den Git Ordner ``` DTSharing ``` herunterladen und entpacken.
4. Terminal öffnen und in den Ordner "DTSharing/Server" wechseln.
5. Ein 2. Terminal in dem Ordner öffnen, da MongoDB und NodeJS ausgeführt werden müssen.
6. Um die MongoDB zu starten ``` mongod --dbpath mongo/db ``` ausführen.
 * Um die MongoDB mit den GTFS Daten zu füllen ein neues Terminal Fenster in dem Ordner öffnen.
 * ```node gtfs2mongo.js ```ausführen (Achtung! - Dauert recht lange)
7. Um den NodeJS Server zu starten ``` node server.js ```ausführen.

