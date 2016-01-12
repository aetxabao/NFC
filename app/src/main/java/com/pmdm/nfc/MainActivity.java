package com.pmdm.nfc;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.io.File;

import ar.com.daidalos.afiledialog.FileChooserActivity;

//http://code.tutsplus.com/tutorials/sharing-files-with-nfc-on-android--cms-22501
    public class MainActivity extends AppCompatActivity {

        private NfcAdapter nfcAdapter;
        String dir = null;
        String fileName = null;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            PackageManager pm = this.getPackageManager();
            // Check whether NFC is available on device
            if (!pm.hasSystemFeature(PackageManager.FEATURE_NFC)) {
                // NFC is not available on the device.
                Toast.makeText(this, "The device does not have NFC hardware.",  Toast.LENGTH_SHORT).show();
            }
            // Check whether device is running Android 4.1 or higher
            else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                // Android Beam feature is not supported.
                Toast.makeText(this, "Android Beam is not supported.",
                        Toast.LENGTH_SHORT).show();
            }
            else {
                // NFC and Android Beam file transfer is supported.
                Toast.makeText(this, "Android Beam is supported on your device.",
                        Toast.LENGTH_SHORT).show();
            }
            dir = Environment.DIRECTORY_PICTURES;
        }

        public void sendFile(View view) {
            Intent intent = new Intent(this, FileChooserActivity.class);
            intent.putExtra(FileChooserActivity.INPUT_START_FOLDER, dir);
            this.startActivityForResult(intent, 0);
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (resultCode == Activity.RESULT_OK) {
                boolean fileCreated = false;
                String filePath = "";

                Bundle bundle = data.getExtras();
                if(bundle != null) {
                    if(bundle.containsKey(FileChooserActivity.OUTPUT_NEW_FILE_NAME)) {
                        fileCreated = true;
                        File folder = (File) bundle.get(FileChooserActivity.OUTPUT_FILE_OBJECT);
                        String name = bundle.getString(FileChooserActivity.OUTPUT_NEW_FILE_NAME);
                        filePath = folder.getAbsolutePath() + "/" + name;
                    } else {
                        fileCreated = false;
                        File file = (File) bundle.get(FileChooserActivity.OUTPUT_FILE_OBJECT);
                        filePath = file.getAbsolutePath();
                    }
                }

                String message = fileCreated? "File created" : "File opened";
                message += ": " + filePath;
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                dir = filePath.substring(0, filePath.lastIndexOf("/"));//+1);
                fileName = filePath.substring(filePath.lastIndexOf("/")+1,
                        filePath.length());

                nfcAdapter = NfcAdapter.getDefaultAdapter(this);

                // Check whether NFC is enabled on device
                if(!nfcAdapter.isEnabled()){
                    // NFC is disabled, show the settings UI to enable NFC
                    Toast.makeText(this, "Please enable NFC.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
                }
                // Check whether Android Beam feature is enabled on device
                else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    if(!nfcAdapter.isNdefPushEnabled()) {
                        // Android Beam is disabled, show the settings UI
                        // to enable Android Beam
                        Toast.makeText(this, "Please enable Android Beam.",
                                Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Settings.ACTION_NFCSHARING_SETTINGS));
                    }
                    // NFC and Android Beam both are enabled
                    else {
                        // Retrieve the path to the user's public pictures directory
                        File fileDirectory = new File(dir);

                        Toast.makeText(getApplicationContext(),
                                dir + "\n" + fileName, Toast.LENGTH_SHORT).show();
                        // Create a new file using the specified directory and name
                        File fileToTransfer = new File(fileDirectory, fileName);
                        fileToTransfer.setReadable(true, false);

                        nfcAdapter.setBeamPushUris(
                                new Uri[]{Uri.fromFile(fileToTransfer)}, this);
                    }
                }
            }
        }
    }