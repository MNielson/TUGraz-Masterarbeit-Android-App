package com.example.matthias.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = findViewById(R.id.sample_text);
        //tv.setText(stringFromJNI());
        PitchDetector pd = new PitchDetector();

        double[] bar = {0, 0.5, 1, 0.5, 0, -0.5, -1, -0.5, 0, 0.5, 1, 0.5, 0, -0.5, -1, -0.5};
        double foo = pd.computePitch(bar, 0, bar.length);
        tv.setText(String.valueOf(foo));

    }


}
