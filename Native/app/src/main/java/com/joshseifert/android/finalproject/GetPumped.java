package com.joshseifert.android.finalproject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by Josh on 12/1/2015.
 */
public class GetPumped extends Activity {

    MediaPlayer mySound;
    ImageView userImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_pumped_layout);

        userImage=(ImageView)findViewById(R.id.userImage);

        Intent takePhotoIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePhotoIntent, 0);

        mySound = MediaPlayer.create(this, R.raw.eyeofthetiger);
        mySound.start();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bitmap bp = (Bitmap) data.getExtras().get("data");
        userImage.setImageBitmap(bp);
    }


    public void stopMusic(View view){ mySound.stop(); }

}