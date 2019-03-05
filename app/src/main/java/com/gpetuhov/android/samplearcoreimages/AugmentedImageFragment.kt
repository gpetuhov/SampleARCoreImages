package com.gpetuhov.android.samplearcoreimages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.sceneform.ux.ArFragment
import com.pawegio.kandroid.toast
import java.lang.Exception

class AugmentedImageFragment : ArFragment() {

    companion object {
        const val IMAGE_DATABASE_NAME = "myimages.imgdb"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        // Turn off the plane discovery since we're only looking for images
        planeDiscoveryController.hide()
        planeDiscoveryController.setInstructionView(null)
        arSceneView.planeRenderer.isEnabled = false
        return view
    }

    override fun getSessionConfiguration(session: Session?): Config {
        val config = super.getSessionConfiguration(session)

        if (!setupAugmentedImageDatabase(config, session)) {
            toast("Could not setup augmented image database")
        }

        return config
    }

    private fun setupAugmentedImageDatabase(config: Config, session: Session?): Boolean {
        // In this example we load pre-existing augmented image database.
        // Database is created with arcoreimg tool, that can be found here:
        // https://github.com/google-ar/arcore-android-sdk/tree/master/tools/arcoreimg
        // Instructions on using the tool can be found here:
        // https://developers.google.com/ar/develop/java/augmented-images/arcoreimg

        // We don't have to include the image itself into the project
        // (in this example the image is included for convenience: to open on screen in order to test the app).
        return try {
            val inputStream = context?.assets?.open(IMAGE_DATABASE_NAME)
            val augmentedImageDatabase = AugmentedImageDatabase.deserialize(session, inputStream)
            config.augmentedImageDatabase = augmentedImageDatabase
            true
        } catch (e: Exception) {
            false
        }
    }
}