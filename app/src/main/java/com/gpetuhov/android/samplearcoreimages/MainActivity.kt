package com.gpetuhov.android.samplearcoreimages

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import com.google.ar.core.AugmentedImage
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.pawegio.kandroid.toast

class MainActivity : AppCompatActivity() {

    companion object {
        const val MIN_OPENGL_VERSION = 3.0
    }

    private var arFragment: ArFragment? = null
    private var fitToScanView: ImageView? = null

    // Augmented image and its associated center pose anchor,
    // keyed by the augmented image in the database.
    private val augmentedImageMap = mutableMapOf<AugmentedImage, AugmentedImageNode>()

    private lateinit var node: AugmentedImageNode

    private var modelRenderable: ModelRenderable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!checkIsSupportedDeviceOrFinish(this)) {
            return
        }

        setContentView(R.layout.activity_main)

        arFragment = supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment
        fitToScanView = findViewById(R.id.image_view_fit_to_scan)

        arFragment?.arSceneView?.scene?.addOnUpdateListener(::onUpdateFrame)

        node = AugmentedImageNode(this)

        ModelRenderable.builder()
            // R.raw.model_name is created by Sceneform plugin (see build.gradle for details)
            .setSource(this, Uri.parse("file:///android_asset/models/earth_obj.sfb"))
            .build()
            .thenAccept { renderable -> modelRenderable = renderable }
            .exceptionally { throwable ->
                toast("Unable to load renderable")
                null
            }

        // This is needed to place our model on the detected plane,
        // at the place of the user's tap.
        arFragment?.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, motionEvent: MotionEvent ->
            if (modelRenderable == null) {
                return@setOnTapArPlaneListener
            }

            // Create the Anchor at the place of the tap.
            val anchor = hitResult.createAnchor()
            val anchorNode = AnchorNode(anchor)
            anchorNode.setParent(arFragment?.arSceneView?.scene)

            // Create the transformable model and add it to the anchor.
            val model = TransformableNode(arFragment?.transformationSystem)
            model.setParent(anchorNode)
            model.renderable = modelRenderable
            model.select()
        }
    }

    /**
     * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
     * on this device.
     *
     * Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
     *
     * Finishes the activity if Sceneform can not run
     */
    private fun checkIsSupportedDeviceOrFinish(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            toast("Sceneform requires Android N or later")
            activity.finish()
            return false
        }

        val openGlVersionString = (activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
            .deviceConfigurationInfo
            .glEsVersion

        if (openGlVersionString.toDouble() < MIN_OPENGL_VERSION) {
            toast("Sceneform requires OpenGL ES 3.0 or later")
            activity.finish()
            return false
        }

        return true
    }

    /**
     * Registered with the Sceneform Scene object, this method is called at the start of each frame.
     *
     * @param frameTime - time since last frame.
     */
    private fun onUpdateFrame(frameTime: FrameTime) {
        val frame = arFragment?.arSceneView?.arFrame

        // If there is no frame or ARCore is not tracking yet, just return.
        if (frame == null || frame.camera.trackingState != TrackingState.TRACKING) {
            return
        }

        val updatedAugmentedImages = frame.getUpdatedTrackables(AugmentedImage::class.java)

        for (augmentedImage in updatedAugmentedImages) {
            val trackingState = augmentedImage.trackingState

            trackingState ?: continue

            when (trackingState) {
                TrackingState.PAUSED -> {
                    // When an image is in PAUSED state, but the camera is not PAUSED, it has been detected,
                    // but not yet tracked.
                    // Do nothing
                }

                TrackingState.TRACKING -> {
                    fitToScanView?.visibility = View.GONE

                    // Create a new anchor for newly found images.
                    if (!augmentedImageMap.containsKey(augmentedImage)) {
                        node.setImage(augmentedImage)
                        augmentedImageMap[augmentedImage] = node
                        arFragment?.arSceneView?.scene?.addChild(node)
                    }
                }

                TrackingState.STOPPED -> {
                    augmentedImageMap.remove(augmentedImage)
                }
            }
        }
    }
}
