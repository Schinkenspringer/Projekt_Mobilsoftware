package de.hka.Haupt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> liste1;
    String string1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        liste1= new ArrayList<>();
        liste1.add("Anton");
        liste1.add("Berta");
        liste1.add("Cäsar");
        liste1.add("Dora");
        liste1.add("Friedrich");
        liste1.add("GERHART");
        liste1.add("Halt Stop!");



        Button btn_click= this.findViewById(R.id.button_klick); // vorgang des button clicks mit ausgabe eines Textes


        Button btn_next= this.findViewById(R.id.Button_weiter);

        Button btn_map= this.findViewById(R.id.button_map);

        TextView txtMessage= this.findViewById(R.id.textnachricht); // txtMessage überschreibt "textnachricht"

        btn_click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Toast.makeText(MainActivity.this, "Ausgabe :DDDDDDD", Toast.LENGTH_SHORT).show();    beispiel für erstellung eines textes direkt vor und für die Ausgabe
                Collections.shuffle(liste1); // die liste wird gemischt
                string1= liste1.get(1); // holt sich erstes Element aus der liste
                txtMessage.setText(string1);


            }
        });
        btn_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent (MainActivity.this, MapActivity.class);


                startActivity(intent);


            }
        });

        btn_next.setOnClickListener(new View.OnClickListener() {   // Aufruf funktion durch button "weiter"
        @Override
        public void onClick(View view) {



            Intent intent = new Intent (MainActivity.this, ZweiteAktivitaet.class); //  "erstellung" der zweiten Aktivity

           intent.putExtra("key",string1);



            startActivity(intent);

        }


    });




    }


}