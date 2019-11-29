package com.example.finalapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class MainActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener {

    //xml init
    private TextView detect;
    private Button play;
    private TextView problem;
    private TextView time;

    //Record settings
    private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    private static final int RECORDER_SAMPLERATE = 44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;


    private AudioRecord recorder = null;
    private int bufferSize = 0;
    private boolean isRecording = false;
    private byte [] recordData;
    private int frequency1 = 19000;
    private int frequency2 = 20000;
    private FFT a;

    //file error statement
    final int REQUEST_PERMISSION_CODE = 1000;
    String extStore = Environment.getExternalStorageDirectory().getPath();

    //fft part
    byte[] music;
    short[] music2Short;
    int boucle = 0;

    //play/stop part
    MediaPlayer mediaPlayer19;
    MediaPlayer mediaPlayer20;

    boolean Play;
    private Thread managementThread;

    long time_start;
    long time_end;
    long timer = 0;

    //vars relating to BLE



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!checkPermissionFromDevice()) {
            RequestPermission();
        }

        detect = findViewById(R.id.detection);
        problem = findViewById(R.id.problem);
        play = findViewById(R.id.play);
        time = findViewById(R.id.time);
        Play = false;

        problem.setText("problem");
        detect.setText("detect");
        time.setText("time");

        mediaPlayer19 = new MediaPlayer();
        mediaPlayer19 = MediaPlayer.create(MainActivity.this, R.raw.short19khz);

        mediaPlayer20 = new MediaPlayer();
        mediaPlayer20 = MediaPlayer.create(MainActivity.this, R.raw.short20khz);

        mediaPlayer19.setOnCompletionListener(this);
        mediaPlayer20.setOnCompletionListener(this);


        //Tamaño del buffer donde se almacenará lo que se graba
        bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        recordData = new byte [bufferSize];

        startRecording();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        recordManagementThread();

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Playing", Toast.LENGTH_SHORT).show();
                Play=true;
                problem.setText("SENT 19 KHZ SIGNAL");
                time_start=System.currentTimeMillis();
                play19();
            }
        });
    }

    private void recordManagementThread() {
        managementThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    startCalculating();
                }
            }
        }, "Calculating thread");

        managementThread.start();
    }

    private String getFilename() throws IOException {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, AUDIO_RECORDER_FOLDER);

        if (!file.exists()) {
            file.createNewFile();
        }

        return (extStore + "/" + "AudioRead" + AUDIO_RECORDER_FILE_EXT_WAV);
    }


    private void handleEmitter (){

        while (Play) { //this device sent the 19 khz signal

            while (!detection20(frequency2, a, boucle)) {
                //ES LA RESPUESTA DEL RECEPTOR. HAY QUE MOSTRAR EL TIEMPO.
                setUpdatedFFT();
            }

            float val22kHz = a.getFreq(frequency2);
            Log.d(String.valueOf(Log.DEBUG), "22khz: "+ val22kHz);

            time_end = System.currentTimeMillis();
            timer = time_end - time_start;
            Play = false;
            problem.setText("");
            problem.setText("RECEIVED 22 KHZ SIGNAL");
            Log.d("tiempoTotal", Long.toString(timer));
            time.setText("time: " + timer + "ms");

            while (detection19(frequency1, a, boucle)) {
                //ES LA RESPUESTA DEL RECEPTOR. HAY QUE MOSTRAR EL TIEMPO.
                playAudio();
                a = new FFT(1024, RECORDER_SAMPLERATE);
                a.noAverages();
                int rec = 0;
                if (isRecording) {
                    rec = recorder.read(recordData, 0, bufferSize);
                }

                if (rec != -1) {
                    ByteBuffer.wrap(recordData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(music2Short);
                    a.forward(Tofloat(music2Short));
                }
            }
        }

    }

    @SuppressLint({"WrongConstant", "SetTextI18n"})
    private void startCalculating() {

        setUpdatedFFT();

        while (!detection19(frequency1, a , boucle) && !Play) {
            //ES LA PREGUNTA DEL EMISOR. HAY QUE RESPONDERLE CON 22KHZ
           setUpdatedFFT();
        }

        if (Play){
            handleEmitter();
        }

        else {
            float val19kHz = a.getFreq(frequency1);
            Log.d(String.valueOf(Log.DEBUG), "19khz: "+ val19kHz);

            problem.setText("RECEIVED 19 KHZ SIGNAL");
            problem.setText("SENT 22 KHZ SIGNAL");
            play20();

            while (detection20(frequency2, a, boucle)) {
                //ES LA PREGUNTA DEL EMISOR. HAY QUE RESPONDERLE CON 22KHZ
               setUpdatedFFT();
            }
        }


    }

    private boolean detection19(int frequency, FFT a, int boucle) {

        int aff = Math.round(a.getFreq(frequency));
        if (aff >= 100000) {
            return true;
        } else {
            return false;
        }
    }

    private boolean detection20(int frequency, FFT a, int boucle) {

        int aff = Math.round(a.getFreq(frequency));
        if (aff >= 70000) {
            return true;
        }
        else {
            return false;
        }
    }

    private void startRecording() {
        //Inicializa la grabadora
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, bufferSize);

        if (recorder.getState() == AudioRecord.STATE_INITIALIZED) {
            //Comienza a grabar
            recorder.startRecording();
        }

        isRecording = true;

    }

    private boolean checkPermissionFromDevice() {
        int result_from_storage_permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        int ble_result = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH);
        int ble_admin_result = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN);
        int acces_fine_loc_result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return  (result_from_storage_permission == PackageManager.PERMISSION_GRANTED) &&
                (record_audio_result == PackageManager.PERMISSION_GRANTED) &&
                (ble_result == PackageManager.PERMISSION_GRANTED) &&
                (ble_admin_result == PackageManager.PERMISSION_GRANTED) &&
                (acces_fine_loc_result == PackageManager.PERMISSION_GRANTED);
    }

    private void RequestPermission(){
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
        }, REQUEST_PERMISSION_CODE);
    }

    //Re-computes the frequency spectrum of the recorded signal at this precise moment.
    private void setUpdatedFFT (){
        playAudio();
        a = new FFT(1024, RECORDER_SAMPLERATE);
        a.noAverages();
        int rec = 0;
        if (isRecording) {
            rec = recorder.read(recordData, 0, bufferSize);
        }

        if (rec != -1) {
            ByteBuffer.wrap(recordData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(music2Short);
            a.forward(Tofloat(music2Short));
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case REQUEST_PERMISSION_CODE:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(MainActivity.this, "Permission granted", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(MainActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
            }
            break;
        }

    }

    public void playAudio() {

        if ( (bufferSize/2) % 2 != 0 ) {
            /*If minSize divided by 2 is odd, then subtract 1 and make it even*/
            music2Short     = new short [((bufferSize /2) - 1)/2];
            music           = new byte  [(bufferSize/2) - 1];
        }
        else {
            /* Else it is even already */
            music2Short     = new short [bufferSize/2]; //pour motorola sinon /4
            music           = new byte  [bufferSize];  //pour motorola sinon /2
        }

    }

    public float[] Tofloat(short[] s){
        int len = s.length;
        float[] f= new float[len];
        for (int i=0;i<len;i++){
            f[i]=s[i];
        }
        return f;
    }

    public void play19() {
        //problem.setText("is sending a 20khz signal");
        if (mediaPlayer19 != null) {
            mediaPlayer19.start();
        }
        else{
            mediaPlayer19 = new MediaPlayer();
            mediaPlayer19 = MediaPlayer.create(MainActivity.this, R.raw.short19khz);
            mediaPlayer19.start();
        }
    }

    public void play20() {

        if (mediaPlayer20 != null) {
            mediaPlayer20.start();

        }
        else{
            mediaPlayer20 = new MediaPlayer();
            mediaPlayer20 = MediaPlayer.create(MainActivity.this, R.raw.short20khz);
            mediaPlayer20.start();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mp == mediaPlayer19){ //Ha acabado la señal de 19 khz
            //mediaPlayer19.release();
            //mediaPlayer19 = null;
            //mediaPlayer19 = new MediaPlayer();
            //mediaPlayer19.setOnCompletionListener(this);
            //mediaPlayer19 = MediaPlayer.create(MainActivity.this, R.raw.short19khz);
        }
        else if (mp == mediaPlayer20){  //Ha acabado la señal de 22 khz
            //mediaPlayer20.release();
            //mediaPlayer20 = null;
            //mediaPlayer20 = new MediaPlayer();
            //mediaPlayer20.setOnCompletionListener(this);
            //mediaPlayer20 = MediaPlayer.create(MainActivity.this, R.raw.short20khz);
        }
    }
}