/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * This code has been modified. Portions copyright 2018 Amazon.com, Inc. or its affiliates. Please see LICENSE.txt for applicable license terms and NOTICE.txt for applicable notices.
 */

package com.amazon.sumerianarcorestarter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.exceptions.CameraNotAvailableException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends AppCompatActivity implements GLSurfaceView.Renderer {
    private static final String TAG = MainActivity.class.getSimpleName();
    // Image Detection
     private static final String SCENE_URL = "https://us-east-1.sumerian.aws/ada393a64c8d45bdb6c9f26a4e9372e0.scene/?arMode=true";
     private static final String QR_IMAGE_URL = "https://s3.amazonaws.com/aws-sumerian-ar/LinkQRImages.txt";
    // Drone Detection
    //private static final String SCENE_URL = "https://us-east-1.sumerian.aws/c9c5b3bbed914c27bc1b912d3cbdb5b0.scene/?arMode=true";

    private static final String IMAGE_FILENAME = "SumerianAnchorImage.png";
    private static final String IMAGE_BIKE_FILENAME = "bike.jpg";
    private static final float IMAGE_WIDTH_IN_METERS = (float)0.1;
    //private static String images[] = {"label_01.jpg", "label_02.jpg", "label_03.jpg", "label_04.jpg"};


    private GLSurfaceView mSurfaceView;
    private Session mSession;
    private SumerianConnector mSumerianConnector;

    // Set to true ensures requestInstall() triggers installation if necessary.
    private boolean mUserRequestedInstall = true;

    private final BackgroundRenderer mBackgroundRenderer = new BackgroundRenderer();
    private ArrayList<String> imageList = new ArrayList<String>();


    private ArrayList<Bitmap> getImageUrls(String url){
        ArrayList<String> uilList = new ArrayList<String>();
        ArrayList<Bitmap> bitmapList = new ArrayList<Bitmap>();
        try {
            // Create a URL for the desired page
            URL urlLink = new URL(url); // text file location
            //First open the connection
            HttpURLConnection conn=(HttpURLConnection) urlLink.openConnection();
            conn.setConnectTimeout(60000); // timing out in a minute
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String str;
            // Read URL of QR Images
            while ((str = in.readLine()) != null) {
                uilList.add(str);
            }
            in.close();


            for (int i =0 ; i< uilList.size(); i++) {
              String item =  uilList.get(i);
                urlLink = new URL(item);
                conn = (HttpURLConnection) urlLink.openConnection();
                conn.setConnectTimeout(60000);

            }
        } catch (Exception e) {
            Log.d(TAG,e.toString());
        }
        return bitmapList;
    }

    private AugmentedImageDatabase createImageDatabase(Session mSession) {
        AugmentedImageDatabase imageDatabase = new AugmentedImageDatabase(mSession);
        Bitmap bitmap = null;
        try {
            String imList[] = getAssets().list("awsimages");
            for (int i =0 ; i<imList.length; i++) {
                String im = imList[i];
                try (InputStream inputStream = getAssets().open("awsimages/" + im)) {
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    int ind = imageDatabase.addImage(im.split("\\.")[0], bitmap, IMAGE_WIDTH_IN_METERS);
                    Log.d("AWS", String.valueOf(ind));
                } catch (IOException e) {
                    Log.e(TAG, "I/O exception loading augmented image bitmap.", e);
                }
            }
        }
        catch (IOException e) {
            Log.e(TAG, "I/O exception loading augmented image bitmap.", e);
        }
        return imageDatabase;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSurfaceView = findViewById(R.id.gl_surface_view);

        // Set up renderer.
        mSurfaceView.setPreserveEGLContextOnPause(true);
        mSurfaceView.setEGLContextClientVersion(2);
        mSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0); // Alpha used for plane blending.
        mSurfaceView.setRenderer(this);
        mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // Standard Android full-screen functionality.
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mSession == null) {
            // ARCore requires camera permissions to operate. If we did not yet obtain runtime
            // permission on Android M and above, now is a good time to ask the user for it.
            if (!CameraPermissionHelper.hasCameraPermission(this)) {
                CameraPermissionHelper.requestCameraPermission(this);
                return;
            }

            try {
                switch (ArCoreApk.getInstance().requestInstall(this, mUserRequestedInstall)) {
                    case INSTALLED:
                        // Success, create the AR session.
                        mSession = new Session(this);
                        break;
                    case INSTALL_REQUESTED:
                        // Ensures next invocation of requestInstall() will either return
                        // INSTALLED or throw an exception.
                        mUserRequestedInstall = false;
                        return;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            final WebView webView = findViewById(R.id.activity_main_webview);
            mSumerianConnector = new SumerianConnector(webView, mSession, mSurfaceView);

            // Create config and check if camera access that is not blocking is supported.
            Config config = new Config(mSession);
            config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
            if (!mSession.isSupported(config)) {
                throw new RuntimeException("This device does not support AR");
            }

            config.setAugmentedImageDatabase(createImageDatabase(mSession));
            mSession.configure(config);
            mSumerianConnector.loadUrl(SCENE_URL);
        }

        try {
            mSession.resume();
        } catch (CameraNotAvailableException e) {
            e.printStackTrace();
        }
        mSurfaceView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Note that the order matters - GLSurfaceView is paused first so that it does not try
        // to query the session. If Session is paused before GLSurfaceView, GLSurfaceView may
        // still call mSession.update() and get a SessionPausedException.
        mSurfaceView.onPause();
        if (mSession != null) {
            mSession.pause();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(this,
                    "Camera permission is needed to run this application", Toast.LENGTH_LONG).show();
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(this);
            }
            finish();
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

        // Create the texture and pass it to ARCore session to be filled during update().
        mBackgroundRenderer.createOnGlThread(/*context=*/this);
        mSession.setCameraTextureName(mBackgroundRenderer.getTextureId());
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        mSession.setDisplayGeometry(getSystemService(WindowManager.class).getDefaultDisplay().getRotation(), width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // Clear screen to notify driver it should not load any pixels from previous frame.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (mSession == null) {
            return;
        }

        try {
            // Obtain the current frame from ARSession. When the configuration is set to
            // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
            // camera framerate.
            final Frame frame = mSession.update();

            // Draw background.
            mBackgroundRenderer.draw(frame);
            mSumerianConnector.update();
        } catch (Throwable t) {
            // Avoid crashing the application due to unhandled exceptions.
            Log.e(TAG, "Exception on the OpenGL thread", t);
        }
    }
}
