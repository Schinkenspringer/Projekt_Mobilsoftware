package de.hka.Haupt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ZweiteAktivitaet extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zweite_aktivitaet);

        Button btn_zrk= this.findViewById(R.id.button_zurück);

        Intent intent = this.getIntent();

       String ausgabe2= intent.getStringExtra("key");

        TextView textView2 = this.findViewById(R.id.textView2);
        textView2.setText(ausgabe2);

        btn_zrk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });



    }
}