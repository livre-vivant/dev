package com.livrevivant;

import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Frame;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.ux.ArFragment;
import com.livrevivant.augmentedImage.AugmentedImageList;
import com.livrevivant.augmentedImage.AugmentedImageNode;
import com.livrevivant.augmentedImage.AugmentedImageVideo;
import com.livrevivant.common.helpers.SnackbarHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ArFragment arFragment;
    private ImageView fitToScanView;

    private AugmentedImageList imageList = new AugmentedImageList();
    private Map<String, Integer> videos = new HashMap<>();

    private final float SECONDS_STOP_VIDEO = 0.5f;
    private float timeCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        fitToScanView = findViewById(R.id.image_view_fit_to_scan);

        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdateFrame);

        initVideos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (imageList.isEmpty()) {
            fitToScanView.setVisibility(View.VISIBLE);
        }
    }

    private void initVideos() {
        videos.put("image1.jpg", R.raw.small);
        videos.put("default.jpg", R.raw.sample);
    }

    /**
     * Registered with the Sceneform Scene object, this method is called at the start of each frame.
     *
     * @param frameTime - time since last frame.
     */

    private void onUpdateFrame(FrameTime frameTime) {
        Frame frame = arFragment.getArSceneView().getArFrame();
        timeCount += frameTime.getDeltaSeconds();

        // If there is no frame or ARCore is not tracking yet, just return.
        if (frame == null || frame.getCamera().getTrackingState() != TrackingState.TRACKING) {
            return;
        }

        Collection<AugmentedImage> updatedAugmentedImages =
                frame.getUpdatedTrackables(AugmentedImage.class);

        for (AugmentedImage augmentedImage : updatedAugmentedImages) {
            switch (augmentedImage.getTrackingState()) {
                case PAUSED:
                    // When an image is in PAUSED state, but the camera is not PAUSED, it has been detected,
                    // but not yet tracked.
                    String text = "Detected Image " + augmentedImage.getIndex();
                    SnackbarHelper.getInstance().showMessage(this, text);
                    break;

                case TRACKING:
                    // Have to switch to UI Thread to update View.
                    fitToScanView.setVisibility(View.GONE);

                    // Create a new anchor for newly found images.
                    if (!imageList.contains(augmentedImage)) {
                        AugmentedImageNode node = new AugmentedImageNode(this);
                        node.setImageWithVideo(augmentedImage, MediaPlayer.create(this, videos.get(augmentedImage.getName())));

                        imageList.add(new AugmentedImageVideo(augmentedImage, node));
                        arFragment.getArSceneView().getScene().addChild(node);
                    }

                    imageList.get(augmentedImage).setCameraTracking(true);
                    break;

                case STOPPED:
                    if (imageList.contains(augmentedImage)) {
                        imageList.remove(augmentedImage);
                    }

                    break;
            }
        }

        if (timeCount > SECONDS_STOP_VIDEO) {
            imageList.removeIfNotTracking();
            timeCount = 0;

            imageList.setAllCameraTracking(false);
        }
    }
}
