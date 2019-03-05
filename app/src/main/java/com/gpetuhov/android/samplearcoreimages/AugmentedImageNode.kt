package com.gpetuhov.android.samplearcoreimages

import android.content.Context
import android.net.Uri
import com.google.ar.core.AugmentedImage
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.rendering.ModelRenderable
import java.util.concurrent.CompletableFuture

class AugmentedImageNode(context: Context) : AnchorNode() {

    private var earth = ModelRenderable.builder()
        .setSource(context, Uri.parse("file:///android_asset/models/earth_obj.sfb"))
        .build()

    fun setImage(image: AugmentedImage) {
        // If model is not loaded, then recurse when loaded.
        if (!earth.isDone) {
            CompletableFuture.allOf(earth)
                .thenAccept { setImage(image) }
                .exceptionally { null }
        }

        // Set the anchor based on the center of the image.
        anchor = image.createAnchor(image.centerPose)

        val node = Node()
        node.setParent(this)
        localPosition.set(0.0f, 0.0f, 0.0f)
        node.localPosition = localPosition
        node.renderable = earth.getNow(null)
    }
}