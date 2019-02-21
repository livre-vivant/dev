package com.livrevivant.augmentedImage;

import com.google.ar.core.AugmentedImage;

public class AugmentedImageVideo {
    private AugmentedImage image;
    private boolean isCameraTracking = false;
    private AugmentedImageNode node;

    public AugmentedImageVideo(AugmentedImage image, AugmentedImageNode node) {
        this.image = image;
        this.node = node;
    }

    public AugmentedImage getImage() {
        return image;
    }

    public AugmentedImageNode getNode() {
        return node;
    }

    public boolean isCameraTracking() {
        return isCameraTracking;
    }

    public void setCameraTracking(boolean val) {
        isCameraTracking = val;
    }
}
