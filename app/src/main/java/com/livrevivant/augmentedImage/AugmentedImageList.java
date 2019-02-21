package com.livrevivant.augmentedImage;

import android.app.Activity;
import android.content.Context;

import com.google.ar.core.AugmentedImage;
import com.livrevivant.common.helpers.SnackbarHelper;

import java.util.ArrayList;
import java.util.List;

public class AugmentedImageList {
    private List<AugmentedImageVideo> images = new ArrayList<>();

    public void add(AugmentedImageVideo image) {
        images.add(image);
    }

    public void remove(AugmentedImage image) {
        this.remove(get(image));
    }

    public void remove(AugmentedImageVideo image) {
        image.getNode().getAnchor().detach();
        image.getNode().setEnabled(false);
        image.getNode().getMediaPlayer().reset();
        images.remove(image);
    }

    public AugmentedImageVideo get(AugmentedImage image) {
        for (AugmentedImageVideo aiv : images) {
            if (aiv.getImage().getName().equals(image.getName())) {
                return aiv;
            }
        }

        return null;
    }

    public boolean contains(AugmentedImage image) {
        return get(image) != null;
    }

    public boolean isEmpty() {
        return images.isEmpty();
    }

    public void setAllCameraTracking(boolean val) {
        for (AugmentedImageVideo image : images) {
            image.setCameraTracking(val);
        }
    }

    public void removeIfNotTracking() {
        for (AugmentedImageVideo image : images) {
            if (!image.isCameraTracking()) {
                this.remove(image);
            }
        }
    }
}
