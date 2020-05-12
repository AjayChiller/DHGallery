package com.technofreak.projetcv15.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.textfield.TextInputLayout
import com.technofreak.projetcv15.R
import com.technofreak.projetcv15.adapter.EmojiAdapter
import com.technofreak.projetcv15.adapter.FilterAdapter
import com.technofreak.projetcv15.adapter.FilterListener
import com.technofreak.projetcv15.adapter.StickerAdapter
import com.technofreak.projetcv15.model.PhotoEntity
import com.technofreak.projetcv15.utils.SpaceItemDecoration
import com.technofreak.projetcv15.utils.focusAndShowKeyboard
import com.technofreak.projetcv15.utils.hideKeyboard
import com.technofreak.projetcv15.viewmodel.PhotoEditorViewModel
import com.theartofdev.edmodo.cropper.CropImageView
import es.dmoral.toasty.Toasty
import id.zelory.compressor.Compressor
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.PhotoEditor.OnSaveListener
import ja.burhanrashid52.photoeditor.PhotoFilter
import ja.burhanrashid52.photoeditor.SaveSettings
import ja.burhanrashid52.photoeditor.TextStyleBuilder
import kotlinx.android.synthetic.main.activity_photo_editor.*
import kotlinx.android.synthetic.main.image_input_dialog.view.*
import petrov.kristiyan.colorpicker.ColorPicker
import petrov.kristiyan.colorpicker.ColorPicker.OnChooseColorListener
import java.io.File
import java.io.IOException

private const val PICK_REQUEST = 100

class PhotoEditorActivity : AppCompatActivity() {
    private val viewModel: PhotoEditorViewModel by viewModels()
    private lateinit var mPhotoEditor: PhotoEditor
    private lateinit var rbutton_entry:Animation
    private lateinit var lbutton_entry:Animation
    private lateinit var cropTool: CropImageView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_editor)

        rbutton_entry = AnimationUtils.loadAnimation(            this,            R.anim.rbutton_entry        )
        lbutton_entry = AnimationUtils.loadAnimation(            this,            R.anim.lbutton_entry        )
        val photoPath = intent.getStringExtra("uri")
        if (photoPath != null) {
            viewModel.isCaptured=true
            viewModel.photoFile = File(photoPath)
            viewModel.fileUri=Uri.parse(photoPath)
            photo_editor_view.source.setImageURI(viewModel.fileUri)
            initializeEditor()
        } else {
            select_image.setOnClickListener { selectImage() }
        }
    }

    private fun selectImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Select Picture"),
            PICK_REQUEST
        )
    }

    private fun initializeEditor() {
        viewModel.OnTop=0
        intro.visibility = View.GONE
        viewButtons()
       viewModel.textFont=ResourcesCompat.getFont(this, R.font.carter_one)
        mPhotoEditor = PhotoEditor.Builder(this, photo_editor_view)
            .setPinchTextScalable(true)
            .setDefaultTextTypeface(viewModel.textFont)
            .build()

        cropTool=crop_Image_View

        photo_editor_view.setOnClickListener{    mPhotoEditor.clearHelperBox()    }
        draw.setOnClickListener { draw() }
        add_Text.setOnClickListener { addText() }
        add_Emoji.setOnClickListener { addEmoji() }
        add_Sticker.setOnClickListener { addSricker() }
        undo_tool.setOnClickListener { mPhotoEditor.undo() }
        redo_tool.setOnClickListener { mPhotoEditor.redo() }
        filter_tool.setOnClickListener { addFilters() }
        cancle.setOnClickListener { clearAll()      }
        save.setOnClickListener { savePhoto() }
        delete.setOnClickListener { deletePhoto() }
        crop_tool.setOnClickListener    {       cropPhoto()  }
    }

    private fun cropPhoto() {
        viewModel.OnTop=6
        crop_container.visibility=View.VISIBLE
        left_tools_container.visibility = View.GONE
        right_tools_container.visibility = View.GONE
        if(viewModel.isCaptured) {
            val bmOptions = BitmapFactory.Options()
            var bitmap = BitmapFactory.decodeFile(viewModel.photoFile.absolutePath, bmOptions)
            bitmap = Bitmap.createBitmap(bitmap!!)
            cropTool.setImageBitmap(bitmap)
            cropTool.rotateImage(90)
        }
        else
        {
            cropTool.setImageUriAsync(viewModel.fileUri)
        }
        rotate_crop.setOnClickListener{     cropTool.rotateImage(90)    }
        save_crop.setOnClickListener{  saveCrop()        }
    }

    private fun saveCrop() {
        val cropped = cropTool.croppedImage
        crop_container.visibility=View.GONE
        viewButtons()
        photo_editor_view.source.setImageBitmap(cropped)
        mPhotoEditor.setFilterEffect(viewModel.prevFilter)
        viewModel.OnTop=0
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
                progressBar.visibility = View.VISIBLE
                Toasty.info(this, "Saving", Toast.LENGTH_SHORT ).show();

                val saveSettings = SaveSettings.Builder().build()
                val editedfilepath=externalCacheDir!!.absolutePath+System.currentTimeMillis()+".jpg"
                mPhotoEditor.saveAsFile(
                    editedfilepath,
                    saveSettings,
                    object : OnSaveListener {
                        override fun onSuccess(imagePath: String) {
                            val editedFile=File(imagePath)
                            val compressor =
                                Compressor(applicationContext).setDestinationDirectoryPath(externalMediaDirs.first().absolutePath)
                            val compressedFile = compressor.compressToFile(editedFile)
                            val photoEntity = PhotoEntity(
                                0,
                                image_title.editText!!.text.toString(),
                                compressedFile.lastModified(),
                                compressedFile.absolutePath,
                                image_tags.editText!!.text.toString()
                            )
                            viewModel.insert(photoEntity)
                            editedFile.delete()
                            if(viewModel.isCaptured)
                                viewModel.photoFile.delete()
                            photo_editor_view.source.setImageURI(null)
                            goToGallery()
                        }

                        override fun onFailure(exception: Exception) {
                            Toast.makeText(applicationContext,"FAILED",Toast.LENGTH_SHORT).show()
                        }
                    })
            }
            .setNegativeButton("Cancle") { dialogInterface, _ ->
                viewButtons()
                dialogInterface.cancel()
            }
        builder.show()

    }

    private fun addFilters() {
        mPhotoEditor.clearHelperBox()
        viewModel.OnTop = 5
        filter_container.visibility = View.VISIBLE
        left_tools_container.visibility = View.GONE
        right_tools_container.visibility = View.GONE

        val filterAdapter = FilterAdapter(viewModel.filterPair, object : FilterListener {
            override fun onFilterSelected(photoFilter: PhotoFilter?) {
                mPhotoEditor.setFilterEffect(photoFilter)
                viewModel.prevFilter=photoFilter!!
            }
        }
        )
        filter_view.adapter = filterAdapter
        filter_container.setOnClickListener {
            viewModel.OnTop = 0
            filter_container.visibility = View.GONE
             viewButtons()
        }
    }

    private fun draw() {
        mPhotoEditor.clearHelperBox()
        viewModel.OnTop = 1
        draw_container.visibility = View.VISIBLE
        left_tools_container.visibility = View.GONE
        right_tools_container.visibility = View.GONE
        mPhotoEditor.setBrushDrawingMode(true)
        mPhotoEditor.setBrushColor(getColor(R.color.colorPrimary))
        brush_size.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                mPhotoEditor.setBrushSize(seekBar!!.progress.toFloat())
            }
        })
        opacity.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                mPhotoEditor.setOpacity(seekBar!!.progress)
            }
        })
        eraser.setOnClickListener {
            mPhotoEditor.brushEraser()
        }
        close_drawing.setOnClickListener {
            viewModel.OnTop = 0
            draw_container.visibility = View.GONE
           viewButtons()
            mPhotoEditor.setBrushDrawingMode(false)
        }
        brush_color_picker.setOnClickListener {
            val colorPicker = ColorPicker(this)
            colorPicker.show()
            colorPicker.setOnChooseColorListener(object : OnChooseColorListener {
                override fun onChooseColor(position: Int, color: Int) {
                    mPhotoEditor.brushColor = color
                }
                override fun onCancel() {}
            })
        }


    }

    private fun addEmoji() {
        mPhotoEditor.clearHelperBox()
        viewModel.OnTop = 4
        val emojiAdapter = EmojiAdapter(viewModel.emoji)
        emoji_container.visibility = View.VISIBLE
        emoji_container.layoutManager = GridLayoutManager(this, 5)
        emoji_container.adapter = emojiAdapter
        emojiAdapter.setOnClickListener {
            viewModel.OnTop = 0
            emoji_container.visibility = View.GONE
            mPhotoEditor.addEmoji(it)
        }

    }

    private fun addSricker() {
        mPhotoEditor.clearHelperBox()
        viewModel.OnTop = 3
        val stickerAdapter = StickerAdapter(viewModel.stickers)
        sticker_container.visibility = View.VISIBLE
        sticker_container.layoutManager = GridLayoutManager(this, 3)
        sticker_container.adapter = stickerAdapter
        sticker_container.addItemDecoration(
            SpaceItemDecoration(
                60, 20, 20, 20
            )
        )
        stickerAdapter.setOnClickListener {
            viewModel.OnTop = 0
            sticker_container.visibility = View.GONE
            mPhotoEditor.addImage(it)
        }

    }

    private fun addText() {
        mPhotoEditor.clearHelperBox()
        viewModel.OnTop = 2
        input_text.text=null
        focusAndShowKeyboard(this, input_text)
        add_txt_container.visibility = View.VISIBLE
        add_txt_container.setOnClickListener {
            viewModel.OnTop = 0
            hideKeyboard(this)
            viewModel.currentText = input_text.text.toString()
            add_txt_container.visibility = View.GONE
            if (viewModel.currentText != "") {
                val textStyle = TextStyleBuilder()
                textStyle.withTextColor(viewModel.colorCode)
                textStyle.withTextFont(viewModel.textFont!!)
                textStyle.withBackgroundColor(viewModel.bgcolorCode)
                mPhotoEditor.addText("  "+viewModel.currentText+"  ", textStyle)
            }
        }

        color_picker.setOnClickListener {
            val colorPicker = ColorPicker(this)
            colorPicker.show()
            colorPicker.setOnChooseColorListener(object : OnChooseColorListener {
                override fun onChooseColor(position: Int, color: Int) {
                    input_text.setTextColor(color)
                    viewModel.colorCode = color
                }
                override fun onCancel() {}
            })
        }
        bg_color_picker.setOnClickListener {
            val colorPicker = ColorPicker(this)
            colorPicker.show()
            colorPicker.setOnChooseColorListener(object : OnChooseColorListener {
                override fun onChooseColor(position: Int, color: Int) {
                    input_text.setBackgroundColor(color)
                    viewModel.bgcolorCode = color
                }
                override fun onCancel() {}
            })
        }
    }

    private fun clearAll()
    {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Do you want to remove all the changes made ?")
        builder.setPositiveButton(android.R.string.yes) { _, _ ->
            mPhotoEditor.clearAllViews()
            viewModel.prevFilter=PhotoFilter.NONE

            mPhotoEditor.setFilterEffect(PhotoFilter.NONE)
            photo_editor_view.source.setImageURI(viewModel.fileUri)
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
            1 -> {
                draw_container.visibility = View.GONE
                viewButtons()
                mPhotoEditor.setBrushDrawingMode(false)
            }
            2 -> {
                viewModel.currentText = input_text.text.toString()
                add_txt_container.visibility = View.GONE
                if (viewModel.currentText != "") {
                    val textStyle = TextStyleBuilder()
                    textStyle.withTextColor(viewModel.colorCode)
                    textStyle.withTextFont(viewModel.textFont!!)
                    textStyle.withBackgroundColor(viewModel.bgcolorCode)
                    mPhotoEditor.addText("  "+viewModel.currentText+"  ", textStyle)
                }

            }
            3 -> {
                sticker_container.visibility = View.GONE
            }
            4 -> {
                emoji_container.visibility = View.GONE
            }
            5 -> {
                viewModel.OnTop = 0
                filter_container.visibility = View.GONE
                viewButtons()
            }
            6  ->{
                crop_container.visibility=View.GONE
                viewButtons()
            }
        }
        viewModel.OnTop=0
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_REQUEST) {
                try {
                    viewModel.fileUri = data!!.data!!
                    photo_editor_view.source.setImageURI(viewModel.fileUri)
                    initializeEditor()
                } catch (e: IOException) {
                    Log.i("DDDD", "tempp FILE " + e)
                    e.printStackTrace()
                }
            }
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun viewButtons()
    {
        left_tools_container.visibility = View.VISIBLE
        left_tools_container.startAnimation(lbutton_entry)
        right_tools_container.visibility = View.VISIBLE
        right_tools_container.startAnimation(rbutton_entry)
    }

}




