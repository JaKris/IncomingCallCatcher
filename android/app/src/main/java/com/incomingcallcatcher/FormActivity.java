package com.incomingcallcatcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class FormActivity extends Activity implements Runnable {
    final Calendar calendar = Calendar.getInstance();
    private EditText nomView;
    private EditText telfView;
    private TextView entradaView;
    private TextView sortidaView;
    private EditText numberPicker;
    private RadioGroup radioGroup;
    private Button saveButton;
    private final String myFormat = "Y-M-d";
    private final String browserURL = "https://www.elsolivers.com/intern/formulari_app.php";
    private final String requestURL = "https://www.elsolivers.com/intern/app.php";
    private final String testURL = "http://192.168.1.41/formulari_app.php";

    private MediaPlayer mediaPlayer = new MediaPlayer();
    private SeekBar seekBar;
    boolean wasPlaying = false;
    private Button playButton;
    private Button webViewButton;
    private File audioFile;
    private String phoneNumber;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_activity);

        phoneNumber = getIntent().getStringExtra("phoneNumber");
        nomView = findViewById(R.id.nomEditText);
        telfView = findViewById(R.id.telfEditText);
        telfView.setText(getIntent().getStringExtra("phoneNumber"));
        entradaView = findViewById(R.id.entradaTextView);
        sortidaView = findViewById(R.id.sortidaTextView);
        numberPicker = findViewById(R.id.placesNumPicker);
        radioGroup = findViewById(R.id.radioGroup);
        saveButton = findViewById(R.id.saveButton);
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());

        playButton = findViewById(R.id.playButton);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSong();
            }
        });

        seekBar = findViewById(R.id.seekbar);

        final TextView seekBarHint = findViewById(R.id.timeView);

        audioFile = new File(Environment.getExternalStorageDirectory(), "/Recordings/Record");
        if (!audioFile.exists()) {
            playButton.setVisibility(View.INVISIBLE);
            seekBarHint.setVisibility(View.INVISIBLE);
            seekBar.setVisibility(View.INVISIBLE);
        }

        if (!phoneNumber.trim().equals("")) contactExists(phoneNumber);

        DatePickerDialog.OnDateSetListener entradaDate = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                entradaView.setText(sdf.format(calendar.getTime()));
            }
        };

        DatePickerDialog.OnDateSetListener sortidaDate = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                sortidaView.setText(sdf.format(calendar.getTime()));
            }
        };

        entradaView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(FormActivity.this, entradaDate, calendar
                        .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        sortidaView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(FormActivity.this, sortidaDate, calendar
                        .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkValidFields()) {
                    Toast.makeText(FormActivity.this, "Tots els camps son obligatoris ", Toast.LENGTH_LONG).show();
                    return;
                }
                RadioButton selectedRadio = findViewById(radioGroup.getCheckedRadioButtonId());
                Ion.with(FormActivity.this)
                        .load(requestURL)
                        .setMultipartParameter("nom", nomView.getText().toString())
                        .setMultipartParameter("telf", telfView.getText().toString())
                        .setMultipartParameter("entrada", entradaView.getText().toString())
                        .setMultipartParameter("sortida", sortidaView.getText().toString())
                        .setMultipartParameter("places", numberPicker.getText().toString())
                        .setMultipartParameter("pensio", selectedRadio.getText().toString().toLowerCase())
                        .setMultipartFile("audio", "audio/ogg", new File(audioFile.getAbsolutePath()))
                        .asString()
                        .setCallback(new FutureCallback<String>() {
                            @Override
                            public void onCompleted(Exception e, String result) {
                                Toast.makeText(FormActivity.this, "Dades registrades", Toast.LENGTH_LONG).show();
                                if (result != null) Log.d("DATA_RESP", result);
                                if (e != null) e.printStackTrace();
                                finish();
                            }
                        });
            }
        });


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

                seekBarHint.setVisibility(View.VISIBLE);
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                seekBarHint.setVisibility(View.VISIBLE);
                int x = (int) Math.ceil(progress / 1000f);

                if (x < 10)
                    seekBarHint.setText("0:0" + x);
                else
                    seekBarHint.setText("0:" + x);

                double percent = progress / (double) seekBar.getMax();
                int offset = seekBar.getThumbOffset();
                int seekWidth = seekBar.getWidth();
                int val = (int) Math.round(percent * (seekWidth - 2 * offset));
                int labelWidth = seekBarHint.getWidth();
                seekBarHint.setX(offset + seekBar.getX() + val
                        - Math.round(percent * offset)
                        - Math.round(percent * labelWidth / 2));

                if (progress > 0 && mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    clearMediaPlayer();
                    playButton.setText("PLAY");
                    FormActivity.this.seekBar.setProgress(0);
                }

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {


                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                }
            }
        });

        webViewButton = findViewById(R.id.webViewButton);
        webViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkValidFields()) {
                    Toast.makeText(FormActivity.this, "Tots els camps son obligatoris ", Toast.LENGTH_LONG).show();
                    return;
                }
                RadioButton selectedRadio = findViewById(radioGroup.getCheckedRadioButtonId());

                String postData = "";
                try {
                    postData = "nom=" + URLEncoder.encode(nomView.getText().toString(), "UTF-8") +
                            "&telf=" + URLEncoder.encode(telfView.getText().toString(), "UTF-8") +
                            "&entrada=" + URLEncoder.encode(entradaView.getText().toString(), "UTF-8") +
                            "&sortida=" + URLEncoder.encode(sortidaView.getText().toString(), "UTF-8") +
                            "&places=" + URLEncoder.encode(numberPicker.getText().toString(), "UTF-8") +
                            "&pensio=" + URLEncoder.encode(selectedRadio.getText().toString().toLowerCase(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                if (audioFile.exists()) {
                    try {
                        postData += "&audio=" + URLEncoder.encode(Base64.encodeToString(FileUtils.readFileToByteArray(audioFile), Base64.DEFAULT), "UTF-8");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Intent browserIntent = new Intent(getApplicationContext(), FormWebView.class);
                browserIntent.putExtra("url", browserURL);
                browserIntent.putExtra("postData", postData.getBytes());
                startActivity(browserIntent);
            }
        });
    }

    public void playSong() {

        try {


            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                clearMediaPlayer();
                seekBar.setProgress(0);
                wasPlaying = true;
                playButton.setText("PLAY");
            }


            if (!wasPlaying) {

                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();
                }

                playButton.setText("PAUSE");
                mediaPlayer.setDataSource(audioFile.getAbsolutePath());
                mediaPlayer.prepare();
                mediaPlayer.setVolume(1f, 1f);
                mediaPlayer.setLooping(false);
                seekBar.setMax(mediaPlayer.getDuration());

                mediaPlayer.start();
                new Thread(this).start();

            }

            wasPlaying = false;
        } catch (Exception e) {
            e.printStackTrace();

        }
    }


    private void contactExists(String number) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {
            if (!cursor.moveToFirst()) {
                new AlertDialog.Builder(FormActivity.this)
                        .setTitle("Añadir Contacto")
                        .setMessage("El numero de telefono " + number + " no esta registrado.\n¿Desea registrarlo?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
                                intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
                                intent.putExtra(ContactsContract.Intents.Insert.PHONE, number);
                                intent.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }
            cursor.close();
        }
    }

    public void run() {

        int currentPosition = mediaPlayer.getCurrentPosition();
        int total = mediaPlayer.getDuration();


        while (mediaPlayer != null && mediaPlayer.isPlaying() && currentPosition < total) {
            try {
                Thread.sleep(1000);
                currentPosition = mediaPlayer.getCurrentPosition();
            } catch (InterruptedException e) {
                return;
            } catch (Exception e) {
                return;
            }

            seekBar.setProgress(currentPosition);

        }
    }

    private boolean checkValidFields() {
        return !nomView.getText().toString().trim().isEmpty() && !telfView.getText().toString().trim().isEmpty() && !entradaView.getText().toString().trim().isEmpty() &&
                !sortidaView.getText().toString().trim().isEmpty() && !numberPicker.getText().toString().trim().isEmpty();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearMediaPlayer();
        audioFile.delete();
    }

    private void clearMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
