package com.example.artbookfragment.view

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.room.Room
import com.example.artbookfragment.R
import com.example.artbookfragment.databinding.FragmentDetailsBinding
import com.example.artbookfragment.model.Art
import com.example.artbookfragment.roomdb.ArtDao
import com.example.artbookfragment.roomdb.ArtDatabase
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream

class DetailsFragment : Fragment() {

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var selectedBitmap: Bitmap
    private lateinit var artDao: ArtDao
    private lateinit var db: ArtDatabase
    private lateinit var compositeDisposable: CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        compositeDisposable= CompositeDisposable()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //viewBinding
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        val view = binding.root

        // Inflate the layout for this fragment
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //register
        registerLauncher()

        arguments?.let {
            //arguments
            val info=DetailsFragmentArgs.fromBundle(it).info
            val selectedArt=DetailsFragmentArgs.fromBundle(it).selectedArt

            //scene check
            if(info.equals("new")){
                //ADD NEW ART

                //clear
                binding.saveButton.visibility=View.VISIBLE
                binding.titleText.setText("")
                binding.dateText.setText("")
                binding.imageView.setImageResource(R.drawable.ic_launcher_background)

                //buttons
                binding.imageView.setOnClickListener {
                    selectImageFun()
                }
                binding.saveButton.setOnClickListener {
                    saveFun()
                }

            } else {
                //ART OF RECYCLERVIEW

                binding.saveButton.visibility=View.INVISIBLE

                selectedArt?.let {
                    //decoder
                    val byteArray=it.image
                    val bitmap=BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)

                    binding.imageView.setImageBitmap(bitmap)
                    binding.titleText.setText(it.title)
                    binding.dateText.setText(it.date)
                }

            }

        }

        //database
        db= Room.databaseBuilder(requireContext(),ArtDatabase::class.java,"Arts").build()
        artDao=db.artDao()

    }

    private fun saveFun(){

        val title=binding.titleText.text.toString()
        val date=binding.dateText.text.toString()

        if(selectedBitmap!=null){
            //do small picture and bitmap
            val smallBitmap=makeSmaller(selectedBitmap,300)
            val outputStream=ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteArray=outputStream.toByteArray()

            val art= Art(title,date,byteArray)

            //database insert
            compositeDisposable.add(
                artDao.insert(art)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponse)
            )

        }

    }

    private fun handleResponse(){
        val action= DetailsFragmentDirections.actionDetailsFragmentToMainFragment()
        Navigation.findNavController(requireView()).navigate(action)
    }

    private fun selectImageFun() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            //Android 33+ --> Read_Media_Images

            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        android.Manifest.permission.READ_MEDIA_IMAGES
                    )
                ) {
                    //rationale
                    Snackbar.make(
                        requireView(),
                        "Permission need for gallery",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction("Give Permission") {
                        //request permission
                        permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                    }.show()
                } else {
                    //request permission
                    permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                }
            } else {
                //permission granted
                val intenToGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intenToGallery)
            }

        } else {
            //Android 32- --> Read_External_Storage

            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {
                    //rationale
                    Snackbar.make(
                        requireView(),
                        "Permission need for gallery",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction("Give Permission") {
                        //request permission
                        permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    }.show()
                } else {
                    //request permission
                    permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            } else {
                //permission granted
                val intenToGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intenToGallery)

            }

        }
    }

    private fun registerLauncher() {

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == RESULT_OK) {

                val intentFromResult = result.data
                if (intentFromResult != null) {

                    val imageData = intentFromResult.data
                    if (imageData != null) {

                        try {

                            if (Build.VERSION.SDK_INT >= 28) {
                                val source = ImageDecoder.createSource(requireActivity().contentResolver, imageData)
                                selectedBitmap = ImageDecoder.decodeBitmap(source)
                                binding.imageView.setImageBitmap(selectedBitmap)
                            } else {
                                selectedBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, imageData)
                                binding.imageView.setImageBitmap(selectedBitmap)
                            }

                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }

                    }

                }

            }

        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
                //permission grandted
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            } else {
                //permission denied
                Toast.makeText(requireContext(), "Permission needed", Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun makeSmaller(image: Bitmap,maxSize: Int) : Bitmap{

        var width=image.width
        var height=image.height
        var bitmapRatio: Double= width.toDouble()/height.toDouble()

        if(bitmapRatio>1){
            //landscape
            width=maxSize
            val scaledHeight=width/bitmapRatio
            height=scaledHeight.toInt()
        } else {
            //portrait
            height=maxSize
            val scaledWidth=height/bitmapRatio
            width=scaledWidth.toInt()
        }

        return Bitmap.createScaledBitmap(image,width,height,true)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        compositeDisposable.clear()
    }

}
