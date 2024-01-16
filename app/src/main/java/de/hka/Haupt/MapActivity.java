package de.hka.Haupt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


import Netzwerk.EfaApiClient;
import Objekte.EfaCoordResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



public class MapActivity extends AppCompatActivity {
    private MapView mapView;
    int Busscore;
    int Bahnscore;
    int Scorewert;
    String score_ausgabe;
    String erklaerung;
    List<String> Liste3 = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) { // Zuweisen der einzelnen  Layout-Elemente
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);


        TextView ausgabe= this.findViewById(R.id.ausgabe_score);

        TextView hintergrund = this.findViewById(R.id.hintergund);

        TextView score_erklarung= this.findViewById(R.id.erklarung_score);

        ImageView einklappen = this.findViewById(R.id.Einklappen);

        ImageView ausklappen = this.findViewById(R.id.klappen);

        ProgressBar laden = this.findViewById(R.id.progressBar);
        Thread laden_hintergrund = new Thread(){   // Erstellung eines Threads um die Ladeanimation nach 5s auszublenden
           public void run(){
               laden.postDelayed(new Runnable() {
                   public void run() {
                       laden.setVisibility(View.GONE);
                   }
               }, 5000);

           }
       };
        laden_hintergrund.start();

        Thread Score_updater = new Thread(){    // Erstellung eines Threads um Bussscore, Bahnscore und Mobilityscore im Hintergrund dauerhaft periodisch zu aktualisieren
            @Override
            public void run(){

                while(true){   // Schleife des Threads
                    try {
                        Thread.sleep(500);   // Alle 0,5 s wird der Thread ausgeführt

                        runOnUiThread(new Runnable() {    // Übergabe des Threads an das UI, da normale Threads keinen UI-Zugriff besitzen
                            @Override
                            public void run() {
                                score_ausgabe = ("Mobility Score: "+Scorewert);
                                ausgabe.setText(score_ausgabe);                     // Überschreiben des Ausgabe Strings mit dem neuesten Mobilityscore-Wert
                                score_erklarung.setText(erklaerung);                  //Überschreiben des Ausgabe Strings mit den neuesten Werten für Busscore und Bahnscore
                            }
                        });
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }}}};
        Score_updater.start();

        ausklappen.setOnClickListener(new View.OnClickListener() {      //Funktion zum "Ausklapen" der Score Erklärung
            @Override
            public void onClick(View v) {         // Bei klicken auf den "Ausklapppfeil" wird eine If-Funktion ausgeführt

                if
                (score_erklarung.getVisibility()==View.INVISIBLE){
                    score_erklarung.setVisibility(View.VISIBLE);   // Die Ausklappbare Fläche mit Busscore und Bahnscore wird sichbar gemacht genauso wie ihr Hintergund
                    hintergrund.setVisibility(View.VISIBLE);
                    einklappen.setVisibility(View.VISIBLE);         // Der Pfeil zum Einklappen wird sichtbar/erscheint
                    ausklappen.setVisibility(View.INVISIBLE);       // Der Pfeil zum ausklappen wird unsichtbar/verschwindet
                }}});
        einklappen.setOnClickListener(new View.OnClickListener() {   // Funktion zum "Einklappen" der Score Erklärung
            @Override
            public void onClick(View v) {
                if (score_erklarung.getVisibility()==View.VISIBLE){
                    score_erklarung.setVisibility(View.INVISIBLE);  // Busscore und Bahnscore sowie ihr Hintergund werden unsichtbar
                    hintergrund.setVisibility(View.INVISIBLE);
                    einklappen.setVisibility(View.INVISIBLE);        // Pfeil zum einklappen verschwindet
                    ausklappen.setVisibility(View.VISIBLE);          // Pfeil zum ausklappen erscheint
                }}});

        XYTileSource mapServer = new XYTileSource(
                "MapName",
                8,
                20,
                256,
                ".png",
                new String[]{"https://tileserver.svprod01.app/styles/default/"}
        );

       String authorizationString = this.getMapServerAuthorizationString(  // Anmeldedaten für den Mapserver
               "ws2223@hka",
               "LeevwBfDi#2027"
       );

        Configuration
                .getInstance()
                .getAdditionalHttpRequestProperties()
                .put("Authorization", authorizationString);

        this.mapView = this.findViewById(R.id.mapView);
        this.mapView.setTileSource(mapServer);

        GeoPoint startPoint = new GeoPoint(49.0000, 8.4690); // Startposition der Karte auf KA-Durlach

        IMapController mapController = this.mapView.getController();
        mapController.setCenter(startPoint);
        mapController.setZoom(17.0);  // Zoomlevel der Karte zum Start
        this.mapView.addMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                loadClosestStops(mapView.getMapCenter().getLatitude(), mapView.getMapCenter().getLongitude());
                return false;
            }
            @Override
            public boolean onZoom(ZoomEvent event) {
                loadClosestStops(mapView.getMapCenter().getLatitude(), mapView.getMapCenter().getLongitude());
                return false;
            }});}
    @Override
    protected void onResume() {
        super.onResume();

        String[] permissions = new String[]{

                Manifest.permission.ACCESS_FINE_LOCATION
        };

        Permissions.check(this, permissions, null, null, new PermissionHandler() {
            @Override
            public void onGranted() {
                initlocationListener();
                Log.d("MapActivity", "onGranted");
            }
            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                super.onDenied(context, deniedPermissions);

                Log.d("MapActivity", "onDenied");
            }});}
    @SuppressLint("MissingPermission")
    private void initlocationListener (){
        LocationListener locationListener = new LocationListener() {
            Marker standort = new Marker(mapView); // Erstellung des Markers für die aktuelle Position

            @Override
            public void onLocationChanged(@NonNull Location location) { // Funktion um Kartenansicht auf aktuellen Standort zu verschieben

                double latitude = location.getLatitude();  // get aktuelle breite
                double longitude = location.getLongitude();   // get aktuelle Länge

                GeoPoint startPoint = new GeoPoint(latitude, longitude); //update des geopoint mit  Breite und Länge
                IMapController mapController = mapView.getController();
                mapController.setCenter(startPoint);    // setzen der Karte auf den Geopoint mit geupdateten Breiten- und Längendaten

                standort.setPosition(startPoint);   // Marker "Standort" wird auf Geopoint der aktuellen Position gesetzt
                standort.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);     // Marker wir so dargestellt, dass seine Spitze auf den Standort zeigt
                standort.setTitle("Aktueller Standort");    //Gibt Marker einen Titel
                standort.setIcon(getResources().getDrawable(R.drawable.standort_icon)); // Icon des Markers wird geändert

                mapView.getOverlays().add(standort); // Marker wird als Overlay zur mapView hinzugefügt und damit dargestellt
            }};
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,   // Koordinatendaten ziehen
                2000,                           // alle 2000ms
                10,                             // min. 10m bewegung
                locationListener
        );}
    private String getMapServerAuthorizationString(String username, String password)
    {
        String authorizationString = String.format("%s:%s", username, password);
        return "Basic " + Base64.encodeToString(authorizationString.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
    }
    private void loadClosestStops(double latitude, double longitude) {  // Funktion um Haltestellen aus der API im Umkreis von 500m zu laden
        retrofit2.Call<EfaCoordResponse> efaCall = EfaApiClient
                .getInstance()
                .getClient()
                .loadStopsWithinRadius(
                        EfaApiClient
                                .getInstance()
                                .createCoordinateString(
                                        latitude,
                                        longitude
                                ),
                        500  // Umkreis der Haltestellen
                         );
        efaCall.enqueue(new Callback<EfaCoordResponse>() {  // Callback an die API
             @Override
            public void onResponse(retrofit2.Call<EfaCoordResponse> call, Response<EfaCoordResponse> response) {
                 Log.d("MapActivity", String.format("Response %d Locations", response.body().getLocations().size()));
                 List<Objekte.Location> haltestellen = response.body().getLocations();  // Haltestellen werden in Liste geschrieben
                 Busscore =0;  //Zurücksetzen von Busscore und Bahnscore  jedesmal wenn neue Haltestellendaten ankommen
                 Bahnscore = 0;

                 for (int i = 0; i < haltestellen.size(); i++) {   // Schleife die Positionen der Haltestellen-Liste druchläuft
                    Objekte.Location location = haltestellen.get(i);

                    double[] Hlocation= location.getCoordinates();   // Abfragen der Koordinaten der Haltestelle an der aktuellen Listenposition als Double []
                    String HId = location.getId();                   // Abfragen der ID der Haltestelle an der aktuellen Listenposition
                    String HName = location.getName();               // Abfragen des Namens der Haltestelle an der aktuellen Listenposition
                    int []Vmittel = location.getProductClasses();       // Abfragen der genutzen Verkehrsmittel an der Haltestelle an der aktuellen Listenposition in Liste "Vmittel"
                    double Entfernung= location.getProperties().getDistance();      // Abfragen der Entfernung zum Standort der Haltestelle an der aktuellen Listenposition


                     /**Entferntes bzw. nicht ausreichend funktionsfähiges Feature-> zum Ausprobieren Dokumentationskommentarzeichen in Line 258 und Line 267 entfernen

                     Marker hmarker= new Marker(mapView); //
                     GeoPoint Hpoint = new GeoPoint(Hlocation[0],Hlocation[1]);  // Anbfragen von Position 0 und 1 der Double-Liste -> dies sind die einzelnen Koordinaten

                     hmarker.setPosition(Hpoint); // Marker "hmarker" wird auf Geopoint der Haltestelle
                     hmarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM); // Marker wir so dargestellt dass seine Spitze auf den Standort zeigt
                     hmarker.setIcon(getResources().getDrawable(R.drawable.h_icon)); // Icon des Markers wird geändert
                     mapView.getOverlays().add(hmarker); // Marker wird als Overlay zur mapView hinzugefügt und damit dargestellt
                      */

                     for (int k=0; k < Vmittel.length; k++) { // Schleife zum durchlaufen der Liste "Vmittel" welche die einzelnen Verkehrsmittel der aktuellen Haltestelle enthält
                         //Wenn:
                        if (Vmittel[k]== 5){ Busscore = Busscore+1;} //Busscore wird um 1 erhöht da Stadtbus vorhanden
                        if (Vmittel[k]== 6){ Busscore = Busscore+1;} //Busscore wird um 1 erhöht da Regionalbus vorhanden
                        if (Vmittel[k]== 7){ Busscore = Busscore+1;} //Busscore wird um 1 erhöht da Schnellbus vorhanden

                        if (Vmittel[k]== 0){ Bahnscore = Bahnscore+2;} //Bahnscore wird um 2 erhöht da Zug vorhanden
                        if (Vmittel[k]== 1){ Bahnscore = Bahnscore+2;} //Bahnscore wird um 2 erhöht da S-Bahn vorhanden
                        if (Vmittel[k]== 2){ Bahnscore = Bahnscore+3;} //Bahnscore wird um 3 erhöht da U-Bahn vorhanden
                        if (Vmittel[k]== 3){ Bahnscore = Bahnscore+2;} //Bahnscore wird um 2 erhöht da Stadtbahn vorhanden
                        if (Vmittel[k]== 4){ Bahnscore = Bahnscore+2;} //Bahnscore wird um 2 erhöht da Straßenbahn vorhanden
                        if (Vmittel[k]== 13){ Bahnscore = Bahnscore+2;} //Bahnscore wird um 2 erhöht da Zug(Nahverkehr) vorhanden
                        if (Vmittel[k]== 14){ Bahnscore = Bahnscore+4;} //Bahnscore wird um 2 erhöht da Zug(Fernverkehr) vorhanden

                         String[] Verkehrsmittel = { //Liste mit Verkehrsmitteln um die Zahlen der API den Verkehrsmittel zuordnen zuu können
                                "Zug", "S-Bahn", "U-Bahn", "Stadtbahn", "Straßen-/Trambahn", "Stadtbus", "Regionalbus",
                                 "Schnellbus", "Seil-/Zahnradbahn", "Schiff", "AST", "Sonstige", "Flugzeug", "Zug(Nahverkehr)",
                                 "Zug(Fernverkehr)", "Zug Fernv m Zuschlag", "Zug Fernv m spez Fpr", "SEV", "Zug Shuttle", "Bürgerbus",
                         };
                        Liste3.add(Verkehrsmittel[Vmittel[k]]); // Zuordnen der einzelnen Vekehrsmittel einträge in eine Liste für die Aktuelle Haltestelle
                     }
                     System.out.println("ID: "+HId+" Name: "+HName+" Vmittel: "+ Arrays.toString(Vmittel)+" "+ Liste3+" Entfernung: "+Entfernung +" Koordinaten: "+ Hlocation[0]+ ","+ Hlocation[1]);
                    Liste3.clear(); // Ausgabe von ID, Name, der NUmmer und Namen der verwendeten Verkehrsmittel, der Entfernung zum Standort und der Koordinaten  der aktuellen Haltestelle und ausgabe in der Logcat-> danach leeren von Liste 3 für nächste Haltestelle in der Liste

                    erklaerung = ( "Busscore: "+ Busscore +'\n'+"Bahnscore: "+ Bahnscore); // der String Erklärung zur Ausgabe von Busscore und Bahnscore
                    int add1= Bahnscore; int add2= Busscore;  // Addition von Bus und Bahnscore um den Mobilityscore zu errechnen
                    Scorewert= add1+add2;
            }}@Override
            public void onFailure(Call<EfaCoordResponse> call, Throwable t) {
             };});};};