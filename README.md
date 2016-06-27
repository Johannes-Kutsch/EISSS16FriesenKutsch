### Dauerticket Sharing
Ein Projekt für das Modul Entwicklung interaktiver Systeme von Thomas Friesen und Johannes Kutsch.
#### Problem
Zu bestimmten Zeiten bietet das Dauerticket die Möglichkeit eine zusätzliche Person
kostenlos mit der Bahn mitzunehmen. Momentan gibt es keine einfache Möglichkeit
sich mit fremden Personen zusammenzuschließen um das Ticket gemeinsam zu
nutzen.
####Ziel
Das Ziel des Projektes ist es, eine Plattform zu schaffen, welche es ermöglicht Kontakt
zwischen einander unbekannten Benutzern herzustellen, deren Route ganz oder
teilweise miteinander übereinstimmt und so die gemeinsame Nutzung eines bereits
vorhandenen Dauertickets ermöglicht.

####Anforderungen
#####Smartphone
* Mindestens Android 4.4, API 19
* 10 MB Speicherplatz
* Internetzugang (FCM)

#####Server
* Mindestens 500 MB Speicherplatz
* Internetzugang (FCM)

####Installation
#####Server
1. [NodeJS herunterladen](https://nodejs.org/en/download/) und installieren.
2. [MongoDB herunterladen](https://www.mongodb.org/downloads#production) und installieren.
3. Den Git Ordner [MS3/DTSharing](https://github.com/netrox91/EISSS16FriesenKutsch/tree/master/MS3/DTSharing) herunterladen und entpacken.
4. Terminal öffnen und in den Ordner ```DTSharing/Server``` wechseln.
5. Die benötigten Module durch ```npm install``` installieren.
6. Ein 2. Terminal öffnen, da MongoDB und NodeJS ausgeführt werden müssen.
7. Um die MongoDB zu starten in den Ordner ```MS3/DTSharing/Server``` wechseln und ```mongod --dbpath mongo/db``` ausführen.
 * Um die MongoDB mit den GTFS Daten zu füllen:
   * Die Config Datei aus dem Ordner ```DTSharing/Server/config``` in den Ordner ```DTSharing/Server/node_modules/gtfs``` kopieren
    * Mit dem ersten Terminal in den Ordner ```DTSharing/Server/node_modules/gtfs``` wechseln und die Daten mit dem Befehl ```node ./scripts/download``` einlesen (Achtung! - Dauert ca 30 Sekunden).
8. Um den NodeJS Server zu starten mit dem ersten Terminal in den Ordner ```DTSharing/Server``` wechseln und ```node server.js```ausführen.

#####Client
1. DTSharing.apk auf dem Smartphone installieren
 * [DTSharing.apk herunterladen](https://github.com/netrox91/EISSS16FriesenKutsch/raw/master/MS3/DTSharing.apk) und installieren
 * oder die DTSharing.apk aus dem Ordner ```MS3/DTSharing.apk``` auf das Handy ziehen und manuell installieren
2. DTSharing starten
 * Lokale IP-Adresse des Servers ermitteln und im Client ändern (Default Port: 3000) 
   * Handelt es sich um einen Android Emulator, der auf demselben Rechner ausgeführt wird, auf dem der NodeJS Server läuft, lautet die IP: 10.0.2.2
    * OSX:
       * ```ifconfig | grep "inet " | grep -v 127.0.0.1 | awk '{print $2}'``` im Terminal eingeben
        * oder alt gedrückt halten und die WLAN Schaltfläche von OSX klicken
     * Windows:
       * ```ipconfig``` im CMD eingeben
 * DTSharing neustarten damit die Stops vom Server geholt werden können
3. Über die Schaltfläche ```Registrieren``` ein Benutzerkonto anlegen
4. Nach Erfolgreicher Registration mit den Daten anmelden

####Screenshots
![alt text](https://github.com/netrox91/EISSS16FriesenKutsch/blob/master/MS3/Screenshots/All_4x4.png "Screenshot")
