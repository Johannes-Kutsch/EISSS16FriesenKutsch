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

###Rapid Prototype
####Beschreibung
Der Rapid Prototype wurde in Android Studio entwickelt, für den Server wurde NodeJS gewählt und als Datenbank findet MongoDB verwendung. Es wurden im Prototypen für das Projekt wichtige Funktionalitäten umgesetzt.
* Einpflegen der GTFS Fahrplandaten in die MongoDB (Diese werden monatlich von der VRS aktualisiert)
* Eintragen der Ticket-Anbietenden und -Suchenden nach Typ in die Datenbank (Search, Offer)
* Abrufen der eingetragenen Daten nach Typ (Search, Offer)
* Autocomplete mit den Verfügbaren Haltestellen im Eingabeformular (Haltestellen über die GTFS Daten in der MongoDB bezogen und somit kompatible mit direktem API Zugriff)
* Matching zwischen Ticket-Anbietend und -Suchend anhand der eingetragenen Daten (PoC 11.1)
* Abrufen der GPS Informationen Latitude und Longitude on demand (PoC 11.4)
* Ausgabe von Haltestellen, welche sich in einem Umkreis von 2km befinden (Radius kann variiert werden) (PoC 11.5)

####Installation
1. [NodeJS herunterladen](https://nodejs.org/en/download/) und installieren.
2. [MongoDB herunterladen](https://www.mongodb.org/downloads#production) und installieren.
3. Den Git Ordner ```DTSharing``` herunterladen und entpacken.
4. Terminal öffnen und in den Ordner ```DTSharing/Server``` wechseln.
5. Ein 2. Terminal in dem Ordner öffnen, da MongoDB und NodeJS ausgeführt werden müssen.
6. Um die MongoDB zu starten ```mongod --dbpath mongo/db``` ausführen.
 * Um die MongoDB mit den GTFS Daten zu füllen:
 * In den Ordner gtfs_data wechseln und die ``` google_transit_DB.zip ``` entpacken (Die .txt Dateien müssen ohne Unterordner in ``` DTSharing/gtfs_data ``` liegen
 * Ein 3. Terminal öffnen und ```node gtfs2mongo.js``` im Ordner ```DTSharing/Server``` ausführen.
 * ```node gtfs2mongo.js``` ausführen (Achtung! - Dauert recht lange)
7. Um den NodeJS Server zu starten ```node server.js```ausführen.

