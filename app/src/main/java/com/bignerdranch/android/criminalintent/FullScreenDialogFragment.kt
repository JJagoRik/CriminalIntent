package com.bignerdranch.android.criminalintent

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import androidx.core.view.doOnLayout
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import com.bignerdranch.android.criminalintent.databinding.FragmentCrimeListBinding
import java.io.File

class FullScreenDialogFragment : DialogFragment() {

    private val args: FullScreenDialogFragmentArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireActivity(), android.R.style.Theme_Black_NoTitleBar_Fullscreen).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_fullscreen_image, container, false)
        val imageView: ImageView = view.findViewById(R.id.fullscreen_image)

        val photoFileName: String = args.crimePhotoId

        val photoFile = photoFileName?.let {
            File(requireContext().applicationContext.filesDir, it)
        }

        if (photoFile?.exists() == true) {
            imageView.doOnLayout { measuredView ->
                val scaledBitmap = getScaleBitmap(
                    photoFile.path,
                    measuredView.width,
                    measuredView.height
                )
                imageView.setImageBitmap(scaledBitmap)
                imageView.tag = photoFileName
            }
        } else {
            imageView.setImageBitmap(null)
            imageView.tag = null
        }
        return view
    }
}