package android.com.demovideomuxer;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.com.demovideomuxer.apputil.Utility;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private final String TAG = getClass().getSimpleName();
    private Button btnStart = null;
    private String audioFilePath = null;
    private String videoFilePath = null;
    private TextView txtAudio = null, txtVideo = null;
    private String selectedVideoPath = null;
    private String selectedAudioPath = null;
    private int REQUEST_AUDIO = 101;
    private File ImageFilePath;
    private int REQUEST_VIDEO = 102;
    private String filemanagerstring = null;
    private ProgressDialog pd = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        startActivity(new Intent(this, MediaMuxerTest.class));

        init();
    }


    /**
     * initialize view elements
     */
    private void init(){
        btnStart = (Button) findViewById(R.id.btn_start);
        txtAudio = (TextView) findViewById(R.id.txt_audiofile);
        txtVideo = (TextView) findViewById(R.id.txt_videopath);
        pd = new ProgressDialog(this);
        pd.setTitle("please wait");
        pd.setMessage("Muxing is in progress");

        btnStart.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_start:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if(Utility.getRuntimePermission(MainActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE})){
                        prePareForMuxing();
                    }
                }else
                    prePareForMuxing();
                break;
        }
    }


    /**
     *prepare for video mux
     */
    private void prePareForMuxing(){
        if(selectedAudioPath == null){
            Intent intent = new Intent();
            intent.setType("audio/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"Select Audio "), REQUEST_AUDIO);
        }else {
            if(pd != null && !pd.isShowing())
             pd.show();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    muxing(selectedAudioPath);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(pd != null && pd.isShowing())
                                pd.dismiss();

                        }
                    });
                }
            }).start();

        }
    }

    /**
     *
     * @param audioFilePath
     */
    private void muxing(String audioFilePath) {



        String outputFile = "";

        try {

            File file = new File(Environment.getExternalStorageDirectory() + File.separator + "final2.mp4");
            file.createNewFile();
            outputFile = file.getAbsolutePath();

            MediaExtractor videoExtractor = new MediaExtractor();
            listAssetFiles("");
            AssetFileDescriptor afdd = getAssets().openFd("Produce.mp4");
            videoExtractor.setDataSource(afdd.getFileDescriptor() ,afdd.getStartOffset(),afdd.getLength());

            MediaExtractor audioExtractor = new MediaExtractor();
            audioExtractor.setDataSource(audioFilePath);

            Log.d(TAG, "Video Extractor Track Count " + videoExtractor.getTrackCount() );
            Log.d(TAG, "Audio Extractor Track Count " + audioExtractor.getTrackCount() );

            MediaMuxer muxer = new MediaMuxer(outputFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            videoExtractor.selectTrack(0);
            MediaFormat videoFormat = videoExtractor.getTrackFormat(0);
            int videoTrack = muxer.addTrack(videoFormat);

            audioExtractor.selectTrack(0);
            MediaFormat audioFormat = audioExtractor.getTrackFormat(0);
            int audioTrack = muxer.addTrack(audioFormat);

            Log.d(TAG, "Video Format " + videoFormat.toString() );
            Log.d(TAG, "Audio Format " + audioFormat.toString() );

            boolean sawEOS = false;
            int frameCount = 0;
            int offset = 100;
            int sampleSize = 256 * 1024;
            ByteBuffer videoBuf = ByteBuffer.allocate(sampleSize);
            ByteBuffer audioBuf = ByteBuffer.allocate(sampleSize);
            MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
            MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();


            videoExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
            audioExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);

            muxer.start();

            while (!sawEOS)
            {
                videoBufferInfo.offset = offset;
                videoBufferInfo.size = videoExtractor.readSampleData(videoBuf, offset);


                if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0)
                {
                    Log.d(TAG, "saw input EOS.");
                    sawEOS = true;
                    videoBufferInfo.size = 0;

                }
                else
                {
                    videoBufferInfo.presentationTimeUs = videoExtractor.getSampleTime();
                    videoBufferInfo.flags = videoExtractor.getSampleFlags();
                    muxer.writeSampleData(videoTrack, videoBuf, videoBufferInfo);
                    videoExtractor.advance();


                    frameCount++;
                    Log.d(TAG, "Frame (" + frameCount + ") Video PresentationTimeUs:" + videoBufferInfo.presentationTimeUs +" Flags:" + videoBufferInfo.flags +" Size(KB) " + videoBufferInfo.size / 1024);
                    Log.d(TAG, "Frame (" + frameCount + ") Audio PresentationTimeUs:" + audioBufferInfo.presentationTimeUs +" Flags:" + audioBufferInfo.flags +" Size(KB) " + audioBufferInfo.size / 1024);

                }
            }

            boolean sawEOS2 = false;
            int frameCount2 =0;
            while (!sawEOS2)
            {
                frameCount2++;

                audioBufferInfo.offset = offset;
                audioBufferInfo.size = audioExtractor.readSampleData(audioBuf, offset);

                if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0)
                {
                    Log.d(TAG, "saw input EOS.");
                    sawEOS2 = true;
                    audioBufferInfo.size = 0;
                }
                else
                {
                    audioBufferInfo.presentationTimeUs = audioExtractor.getSampleTime();
//                    audioBufferInfo.flags = audioExtractor.getSampleFlags();
                    muxer.writeSampleData(audioTrack, audioBuf, audioBufferInfo);
                    audioExtractor.advance();


                    Log.d(TAG, "Frame (" + frameCount + ") Video PresentationTimeUs:" + videoBufferInfo.presentationTimeUs +" Flags:" + videoBufferInfo.flags +" Size(KB) " + videoBufferInfo.size / 1024);
                    Log.d(TAG, "Frame (" + frameCount + ") Audio PresentationTimeUs:" + audioBufferInfo.presentationTimeUs +" Flags:" + audioBufferInfo.flags +" Size(KB) " + audioBufferInfo.size / 1024);

                }
            }

            muxer.stop();
            muxer.release();


        } catch (IOException e) {
            Log.d(TAG, "Mixer Error 1 " + e.getMessage());
        } catch (Exception e) {
            Log.d(TAG, "Mixer Error 2 " + e.getMessage());
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_AUDIO) {
                if (null == data)
                    return;

                Uri selectedImageUri = data.getData();
                System.out.println(selectedImageUri.toString());
                selectedAudioPath = Utility.getPath(this,selectedImageUri);
                txtAudio.setText(selectedAudioPath);
                if(selectedAudioPath != null)
                    prePareForMuxing();
//                    selectVideo();

            }if (requestCode == REQUEST_VIDEO) {
                Uri selectedImageUri = data.getData();

                // OI FILE Manager
                filemanagerstring = selectedImageUri.getPath();

                // MEDIA GALLERY
                selectedVideoPath = Utility.getPath(this, selectedImageUri);
                if (selectedVideoPath != null) {
                    txtVideo.setText(selectedVideoPath);

                }
            }
        }

    }

    /**
     * open video selector dialog
     */
    private void selectVideo(){
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Video"),REQUEST_VIDEO);
    }


    /**
     * listing assets files
     * @param path
     * @return
     */
    private boolean listAssetFiles(String path) {

        String [] list;
        try {
            list = getAssets().list(path);
            if (list.length > 0) {
                // This is a folder
                for (String file : list) {
                    if (!listAssetFiles(path + "/" + file))
                        return false;
                }
            } else {
                // This is a file
                // TODO: add file name to an array list
            }
        } catch (IOException e) {
            return false;
        }

        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                        prePareForMuxing();
                } else {

                    Toast.makeText(this,"permission denied ",Toast.LENGTH_LONG).show();

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}

