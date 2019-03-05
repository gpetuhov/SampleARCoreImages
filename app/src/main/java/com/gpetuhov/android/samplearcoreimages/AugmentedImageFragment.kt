package com.gpetuhov.android.samplearcoreimages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.ar.sceneform.ux.ArFragment

class AugmentedImageFragment : ArFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        // Turn off the plane discovery since we're only looking for images
        planeDiscoveryController.hide()
        planeDiscoveryController.setInstructionView(null)
        arSceneView.planeRenderer.isEnabled = false
        return view
    }
}