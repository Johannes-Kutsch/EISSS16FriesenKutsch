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

####Anforderungen
#####Smartphone
* Mindestens Android 4.4
* 10 MB Speicherplatz
* Internetzugang1 1wird für Firebase Messaging benötigt

#####Server
* Mindestens 500 MB Speicherplatz
* Internetzugang (FCM)

####Installation
1. [NodeJS herunterladen](https://nodejs.org/en/download/) und installieren.
2. [MongoDB herunterladen](https://www.mongodb.org/downloads#production) und installieren.
3. Den Git Ordner [MS3/DTSharing](https://github.com/netrox91/EISSS16FriesenKutsch/tree/master/MS3/DTSharing) herunterladen und entpacken.
4. Terminal öffnen und in den Ordner ```DTSharing/Server``` wechseln.
5. Die benötigten Module durch ```npm install``` installieren.
6. Ein 2. Terminal öffnen, da MongoDB und NodeJS ausgeführt werden müssen.
7. Um die MongoDB zu starten in den Ordner ```MS3/DTSharing/Server``` wechseln und ```mongod --dbpath mongo/db``` ausführen.
 * Um die MongoDB mit den GTFS Daten zu füllen:
 * Die Config Datei aus dem Ordner ```DTSharing/Server/config``` in den Ordner ```DTSharing/Server/node_modules/gtfs``` kopieren
 * Mit dem erstem Terminal in den Ordner ```DTSharing/Server/node_modules/gtfs``` wechseln und die Daten mit dem Befehl ```node ./scripts/download``` einlesen (Achtung! - Dauert ca 30 Sekunden).
8. Um den NodeJS Server zu starten mit dem erstem Terminal in den Ordner ```DTSharing/Server``` wechseln und ```node server.js```ausführen.


###Rapid Prototype
####Beschreibung
Der Rapid Prototype wurde in Android Studio entwickelt, für den Server wurde NodeJS gewählt und als Datenbank findet MongoDB verwendung. Es wurden im Prototypen für das Projekt wichtige Funktionalitäten umgesetzt.
* Einpflegen der GTFS Fahrplandaten* in die MongoDB. *(werden monatlich von der VRS aktualisiert)
* Eintragen der Ticket-Anbietenden und -Suchenden nach Typ in die Datenbank (Search, Offer)
* Abrufen der eingetragenen Daten nach Typ (Search, Offer)
* Autocomplete mit den Verfügbaren Haltestellen im Eingabeformular (Haltestellen über die GTFS Daten in der MongoDB bezogen und somit kompatible mit direktem API Zugriff)
* Matching zwischen Ticket-Anbietend und -Suchend anhand der eingetragenen Daten (PoC 11.1)
* Abrufen der GPS Informationen Latitude und Longitude on demand (PoC 11.4)
* Ausgabe von Haltestellen, welche sich in einem Umkreis von 2km befinden (Radius kann variiert werden) (PoC 11.5)

####Screenshots
![alt text](https://github.com/netrox91/EISSS16FriesenKutsch/blob/master/MS1/Rapid%20Prototype%20Screenshots/All.png "Screenshot")
