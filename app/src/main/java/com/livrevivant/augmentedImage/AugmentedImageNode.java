package com.livrevivant.augmentedImage;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.view.Gravity;
import android.widget.Toast;

import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Pose;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.ExternalTexture;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.livrevivant.R;

public class AugmentedImageNode extends AnchorNode {

    private static final String TAG = "AugmentedImageNode";

    private static ModelRenderable videoRenderable;
    private ExternalTexture texture;
    private Context context;
    private MediaPlayer mediaPlayer = null;

    // The color to filter out of the video.
    private static final Color CHROMA_KEY_COLOR = new Color(0.1843f, 1.0f, 0.098f);

    private static final boolean DISABLE_CHROMA_KEY = true;

    public AugmentedImageNode(Context context) {
        this.context = context;
        texture = new ExternalTexture();
        ModelRenderable.builder()
                .setSource(context, R.raw.chroma_key_video)
                .build()
                .thenAccept(
                        renderable -> {
                            videoRenderable = renderable;
                            renderable.getMaterial().setExternalTexture("videoTexture", texture);
                            renderable.getMaterial().setFloat4("keyColor", CHROMA_KEY_COLOR);
                            renderable.getMaterial().setBoolean("disableChromaKey", DISABLE_CHROMA_KEY);
                        })
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(context, "Unable to load video renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });
    }

    /**
     * Called when the AugmentedImage is detected and should be rendered. A Sceneform node tree is
     * created based on an Anchor created from the image. The corners are then positioned based on the
     * extents of the image. There is no need to worry about world coordinates since everything is
     * relative to the center of the image, which is the parent node of the corners.
     */
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    public void setImageWithVideo(AugmentedImage image, MediaPlayer video) {
        this.mediaPlayer = video;

        mediaPlayer.setSurface(texture.getSurface());
        mediaPlayer.setLooping(true);

        Pose pose = image.getCenterPose();
        Pose rotate = Pose.makeRotation(1,0,0,-1);
        Pose translate = Pose.makeTranslation(0,-image.getExtentZ() / 4, 0);

        pose = pose.compose(rotate).compose(translate);

        // Set the anchor based on the center of the image.
        setAnchor(image.createAnchor(pose));

        // Create a node to render the video and add it to the anchor.
        Node videoNode = new Node();
        videoNode.setParent(this);

        videoNode.setLocalScale( new Vector3(
                        image.getExtentX() , image.getExtentZ(),  0.5f));

        // Start playing the video when the first node is placed.
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();

            // Wait to set the renderable until the first frame of the  video becomes available.
            // This prevents the renderable from briefly appearing as a black quad before the video
            // plays.
            texture
                    .getSurfaceTexture()
                    .setOnFrameAvailableListener(
                            (SurfaceTexture surfaceTexture) -> {
                                videoNode.setRenderable(videoRenderable);
                                texture.getSurfaceTexture().setOnFrameAvailableListener(null);
                            });
        } else {
            videoNode.setRenderable(videoRenderable);
        }
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }
}
