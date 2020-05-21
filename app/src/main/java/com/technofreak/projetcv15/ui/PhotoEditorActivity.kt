package com.technofreak.projetcv15.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.textfield.TextInputLayout
import com.rtugeek.android.colorseekbar.ColorSeekBar.OnColorChangeListener
import com.technofreak.projetcv15.R
import com.technofreak.projetcv15.adapter.*
import com.technofreak.projetcv15.model.PhotoEntity
import com.technofreak.projetcv15.utils.OnSwipeTouchListener
import com.technofreak.projetcv15.utils.SpaceItemDecoration
import com.technofreak.projetcv15.utils.focusAndShowKeyboard
import com.technofreak.projetcv15.utils.hideKeyboard
import com.technofreak.projetcv15.viewmodel.PhotoEditorViewModel
import com.theartofdev.edmodo.cropper.CropImageView
import es.dmoral.toasty.Toasty
import id.zelory.compressor.Compressor
import ja.burhanrashid52.photoeditor.*
import ja.burhanrashid52.photoeditor.PhotoEditor.OnSaveListener
import kotlinx.android.synthetic.main.activity_photo_editor.*
import kotlinx.android.synthetic.main.image_input_dialog.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class PhotoEditorActivity : AppCompatActivity() {
    private val viewModel: PhotoEditorViewModel by viewModels()
    private lateinit var mPhotoEditor: PhotoEditor
    private lateinit var rbutton_entry:Animation
    private lateinit var lbutton_entry:Animation
    private lateinit var cropTool: CropImageView
    var PICK_IMAGE_MULTIPLE = 100


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_editor)

        rbutton_entry = AnimationUtils.loadAnimation(            this,            R.anim.rbutton_entry        )
        lbutton_entry = AnimationUtils.loadAnimation(            this,            R.anim.lbutton_entry        )
        val photoPath = intent.getStringExtra("uri")
        if (photoPath != null) {

          //  cropTool.setImageBitmap(bitmap)
            viewModel.isCaptured=true
            viewModel.photoFile = File(photoPath)
            val bmOptions = BitmapFactory.Options()
            var bitmap = BitmapFactory.decodeFile(viewModel.photoFile.absolutePath, bmOptions)
            bitmap = Bitmap.createBitmap(bitmap!!)
            viewModel.fileUri= bitmap
            viewModel.multiFileList.add(bitmap)
            photo_editor_view.source.setImageBitmap(bitmap)
            initializeEditor()
        } else {
            select_image.setOnClickListener { selectImage() }
        }

        sticker_emoji_view.addItemDecoration(
            SpaceItemDecoration(
                60, 20, 20, 20
            )
        )

        viewModel.stickersItem.observe(this, Observer{ images ->
            viewModel.stickers=images
        })
        addGestures()
    }

    private fun addGestures() {
        photo_editor_view.setOnTouchListener( object :OnSwipeTouchListener(this) {
            override fun onSwipeTop() {
                addFilters()
            }

            override fun onSwipeRight() {
              //  Toast.makeText(applicationContext, "right", Toast.LENGTH_SHORT).show()
            }

            override fun onSwipeLeft() {
             //   Toast.makeText(applicationContext, "left", Toast.LENGTH_SHORT).show()
            }

            override fun onSwipeBottom() {
                  closeFilters()
            }
        })
    }

    private fun selectImage() {
        select_image.setOnClickListener{null}
        val intent = Intent()
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_MULTIPLE)
    }

    private fun initializeEditor() {
        viewModel.OnTop=0
        intro.visibility = View.GONE
        filter_text.visibility=View.VISIBLE
        viewButtons()
        viewModel.textFont=ResourcesCompat.getFont(this, R.font.carter_one)
        mPhotoEditor = PhotoEditor.Builder(this, photo_editor_view)
            .setPinchTextScalable(true)
            .build()
        cropTool=crop_Image_View
        draw_tool.setOnClickListener { draw() }
        add_Text.setOnClickListener { addText() }
        add_Emoji.setOnClickListener { addons() }
        //undo_tool.setOnClickListener { mPhotoEditor.undo() }
         // redo_tool.setOnClickListener { mPhotoEditor.redo() }
        clear_button.setOnClickListener { clearAll()      }
        save.setOnClickListener { savePhoto() }
      //  delete.setOnClickListener { deletePhoto() }
        crop_tool.setOnClickListener    {    cropPhoto()  }
        initFilterandMulltiselection()

    }

    private fun initFilterandMulltiselection() {

        if(viewModel.isMultiPhoto){

            viewModel.multiPhotoSelectionAdapter=
                MultiPhotoSelectionAdapter(viewModel.multiFileList,object:ImageSelectListner{
                    override fun onImageSelected(pos: Int) {
                        changePhoto(pos)
                    }
                })
            bottom_recyclerView.visibility=View.VISIBLE
            bottom_recyclerView.adapter=viewModel.multiPhotoSelectionAdapter
        }

        viewModel.filterAdapter = FilterAdapter(viewModel.filterPair, object : FilterListener {
            override fun onFilterSelected(photoFilter: PhotoFilter?) {
                mPhotoEditor.setFilterEffect(photoFilter)
                viewModel.prevFilter=photoFilter!!

            }
        }
        )
    }

    private fun cropPhoto() {
        viewModel.OnTop=6
        crop_container.visibility=View.VISIBLE
        left_tools_container.visibility = View.GONE
        right_tools_container.visibility = View.GONE
        bottom_recyclerView.visibility=View.GONE
        filter_text.visibility=View.GONE

        cropTool.setImageBitmap(viewModel.fileUri)

        rotate_crop.setOnClickListener{     cropTool.rotateImage(90)    }
        save_crop.setOnClickListener{  saveCrop()        }
    }

    private fun saveCrop() {
        val cropped = cropTool.croppedImage
        crop_container.visibility=View.GONE
        bottom_recyclerView.visibility=View.VISIBLE
        filter_text.visibility=View.VISIBLE
        viewButtons()
        photo_editor_view.source.setImageBitmap(cropped)
        mPhotoEditor.setFilterEffect(viewModel.prevFilter)
        viewModel.OnTop=0
        cropTool.clearImage()
    }

    private fun deletePhoto() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Do you want to Exit eithout Saving")
        builder.setPositiveButton(android.R.string.yes) { _, _ ->
            if (viewModel.isCaptured)
                viewModel.photoFile.delete()
            photo_editor_view.source.setImageURI(null)
            goToGallery()
        }
        builder.setNegativeButton(android.R.string.no) { _, _ ->
        }
        builder.show()
    }

    private fun savePhoto() {

        left_tools_container.visibility = View.GONE
        right_tools_container.visibility = View.GONE
        val customLayout = LayoutInflater.from(this).inflate(R.layout.image_input_dialog, null)
        val image_title: TextInputLayout = customLayout.title
        val image_tags: TextInputLayout = customLayout.tags
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(customLayout)
            .setPositiveButton("Submit") { dialogInterface, _ ->
                dialogInterface.dismiss()
                saveAllImages( image_title.editText!!.text.toString(), image_tags.editText!!.text.toString())

            }
            .setNegativeButton("Cancle") { dialogInterface, _ ->
                viewButtons()
                dialogInterface.cancel()
            }
        builder.show()

    }


    private fun saveAllImages(title:String, tags:String)
    {
        progressBar.visibility = View.VISIBLE
        Toasty.info(this, "Saving", Toast.LENGTH_SHORT ).show()

        if (!viewModel.isMultiPhoto)   {
            val saveSettings = SaveSettings.Builder().build()
            val editedfilepath=externalCacheDir!!.absolutePath+System.currentTimeMillis()+"_Edited.jpg"
            mPhotoEditor.saveAsFile(
                editedfilepath,
                saveSettings,
                object : OnSaveListener {
                    override fun onSuccess(imagePath: String) {
                        val editedFile=File(imagePath)
                        viewModel.compressAndSave(editedFile,title,tags)
                        photo_editor_view.source.setImageURI(null)
                        goToGallery()
                        return
                    }
                    override fun onFailure(exception: Exception) {
                        Toast.makeText(applicationContext,"FAILED",Toast.LENGTH_SHORT).show()
                    }
                })
        }
        else {
            mPhotoEditor.saveAsBitmap(
                object : OnSaveBitmap {
                    override fun onBitmapReady(saveBitmap: Bitmap?) {
                                viewModel.editedPhotosList[viewModel.currentPos].recycle()
                                viewModel.editedPhotosList[viewModel.currentPos] = saveBitmap!!
                    }
                    override fun onFailure(exception: Exception) {
                        Toast.makeText(applicationContext, "FAILED", Toast.LENGTH_SHORT).show()
                        return
                    }
                })

            var editedFile:File
            for (i in viewModel.editedPhotosList)
            {
               editedFile=viewModel.bitmapToFile(i)
               viewModel.compressAndSave(editedFile,title,tags)
                photo_editor_view.source.setImageURI(null)
                i.recycle()
            }
        }
        goToGallery()
        return
    }

    private fun addFilters() {
        mPhotoEditor.clearHelperBox()
        stopDrawingMode()
        viewModel.OnTop = 5
        filter_text.text="Swipe down to close"
        bottom_recyclerView.visibility = View.VISIBLE
        left_tools_container.visibility = View.GONE
        right_tools_container.visibility = View.GONE
        bottom_recyclerView.adapter = viewModel.filterAdapter
    }

    private fun closeFilters(){
        viewModel.OnTop = 0
        viewButtons()
        filter_text.text="^ Swipe up to add filters ^"
        if(!viewModel.isMultiPhoto){
                bottom_recyclerView.visibility=View.GONE
            }
        bottom_recyclerView.adapter=viewModel.multiPhotoSelectionAdapter

    }

    private fun draw() {
        colorSlider.visibility=View.VISIBLE
        draw_tool.setColorFilter(colorSlider.getColor())
        mPhotoEditor.clearHelperBox()
        viewModel.OnTop = 1
        mPhotoEditor.setBrushDrawingMode(true)
        mPhotoEditor.setBrushColor(getColor(R.color.colorPrimary))
        colorSlider.setOnColorChangeListener(OnColorChangeListener { colorBarPosition, alphaBarPosition, color ->
            draw_tool.setColorFilter(color)
            mPhotoEditor.brushColor=color
        })
    }
    private fun stopDrawingMode(){
        colorSlider.visibility=View.GONE
        draw_tool.setColorFilter(resources.getColor(R.color.white))
        mPhotoEditor.setBrushDrawingMode(false)
        mPhotoEditor.clearHelperBox()

    }


    private fun addons(){
        stopDrawingMode()
        viewModel.OnTop = 3
        sticker_emoji_container.visibility=View.VISIBLE
        val emojiAdapter = EmojiAdapter(viewModel.emoji)
        emojiAdapter.setOnClickListener {
            viewModel.OnTop = 0
            sticker_emoji_container.visibility = View.GONE
            mPhotoEditor.addEmoji(it)
        }
        addEmoji(emojiAdapter)
        val stickerAdapter=StickerAdapter()
        stickerAdapter.setOnClickListener {
            viewModel.OnTop = 0
            sticker_emoji_container.visibility = View.GONE
            mPhotoEditor.addImage(it)
        }
        stickerAdapter.submitList(viewModel.stickers)
        emoji_tab.setOnClickListener{          addEmoji(emojiAdapter)       }
        sticker_tab.setOnClickListener{          addSticker(stickerAdapter)        }
    }

    private fun addEmoji(emojiAdapter:EmojiAdapter){
        sticker_emoji_view.layoutManager = GridLayoutManager(this, 5)
        sticker_emoji_view.adapter=emojiAdapter
    }

    private fun addSticker(stickerAdapter: StickerAdapter){
        sticker_emoji_view.layoutManager = GridLayoutManager(this, 3)
        sticker_emoji_view.adapter = stickerAdapter
    }

    private fun addText() {
        left_tools_container.visibility=View.GONE
        right_tools_container.visibility=View.GONE
        input_text_container.visibility=View.VISIBLE
        bottom_recyclerView.visibility=View.GONE
        filter_text.visibility=View.GONE
        stopDrawingMode()
        colorSlider.visibility=View.VISIBLE
        viewModel.OnTop = 2
        input_text.text = null
        focusAndShowKeyboard(this, input_text)
        input_text_container.setOnClickListener {
            addingTextToPhoto()
        }
        colorSlider.setOnColorChangeListener( { colorBarPosition, alphaBarPosition, color ->
            input_text.setTextColor(color)
            viewModel.colorCode = color
        })
    }
    private fun addingTextToPhoto(){
        viewButtons()
        viewModel.OnTop = 0
        hideKeyboard(this)
        colorSlider.visibility=View.GONE
        input_text_container.visibility = View.GONE
        bottom_recyclerView.visibility=View.VISIBLE
        filter_text.visibility=View.VISIBLE
        hideKeyboard(this)
        viewModel.currentText = input_text.text.toString()
        if (viewModel.currentText != "") {
            val textStyle = TextStyleBuilder()
            textStyle.withTextColor(viewModel.colorCode)
            textStyle.withTextFont(viewModel.textFont!!)
            textStyle.withBackgroundColor(viewModel.bgcolorCode)
            mPhotoEditor.addText("  " + viewModel.currentText + "  ", textStyle)
        }
    }



    private fun clearAll()
    {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Do you want to remove all the changes made ?")
        builder.setPositiveButton(android.R.string.yes) { _, _ ->
            mPhotoEditor.clearAllViews()
            photo_editor_view.source.setImageBitmap(viewModel.multiFileList[viewModel.currentPos])
            viewModel.prevFilter=PhotoFilter.NONE
            mPhotoEditor.setFilterEffect(PhotoFilter.NONE)
        }
        builder.setNegativeButton(android.R.string.no) { _, _ ->
        }
        builder.show()
    }



    private fun goToGallery() {
        intent = Intent(this, DHGalleryActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        finish()
        startActivity(intent)
    }


    override fun onBackPressed() {
        when (viewModel.OnTop) {

            -1 ->goToGallery()
            0 -> deletePhoto()
            1 -> stopDrawingMode()
            2 -> addingTextToPhoto()
            3 ->  sticker_emoji_container.visibility = View.GONE
            5 ->  closeFilters()
            6  ->{
                crop_container.visibility=View.GONE
                cropTool.clearImage()
                viewButtons()
            }
        }
        viewModel.OnTop=0
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_MULTIPLE) {
            select_image.setOnClickListener{    selectImage()   }
            if (data != null && data.getClipData() != null) {
                val count = data.getClipData()!!.getItemCount(); //evaluate the count before the for loop --- otherwise, the count is evaluated every loop.
                  var tempBitmap:Bitmap
                for(i in 0..count-1) {
                    tempBitmap  =  MediaStore.Images.Media.getBitmap(this.contentResolver, data!!.getClipData()!!.getItemAt(i).getUri())
                    viewModel.multiFileList.add( tempBitmap)
                    viewModel.editedPhotosList.add( tempBitmap.copy(tempBitmap.getConfig(), true))
                    viewModel.isMultiPhoto=true
                    Log.i("DDDD","--TO---"+i+"  "+viewModel.multiFileList[i])
                    Log.i("DDDD","--TO---"+i+"  "+viewModel.editedPhotosList[i])

                }
                  viewModel.fileUri=viewModel.editedPhotosList[0]

            }else if (data != null && data.getData() != null) {
                viewModel.fileUri =  MediaStore.Images.Media.getBitmap(this.contentResolver, data!!.data!!)
                viewModel.multiFileList.add(viewModel.fileUri)
            }

            photo_editor_view.source.setImageBitmap(viewModel.fileUri)
            initializeEditor()
        }
    }


    private fun viewButtons()
    {
        left_tools_container.visibility = View.VISIBLE
        left_tools_container.startAnimation(lbutton_entry)
        right_tools_container.visibility = View.VISIBLE
        right_tools_container.startAnimation(rbutton_entry)
    }

    private fun changePhoto(pos:Int) {

        if(viewModel.currentPos==pos)
            return
        viewModel.prevFilter=PhotoFilter.NONE
        mPhotoEditor.saveAsBitmap(
            object : OnSaveBitmap {
                override fun onBitmapReady(saveBitmap: Bitmap?) {
                    if (saveBitmap != null) {
                        viewModel.editedPhotosList[viewModel.currentPos].recycle()
                        viewModel.editedPhotosList[viewModel.currentPos] = saveBitmap
                        photo_editor_view.source.setImageBitmap(viewModel.editedPhotosList[pos])
                        viewModel.currentPos = pos
                        viewModel.fileUri = viewModel.editedPhotosList[pos]

                    }
                    else
                      Toasty.error(applicationContext,"Sorry Couldnt load",Toasty.LENGTH_SHORT,true).show()
                     }
                override fun onFailure(exception: Exception) {
                    Toast.makeText(applicationContext, "FAILED", Toast.LENGTH_SHORT).show()
                }
            })
    }
}




