package com.miguel_lm.socialmedia.ui.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import com.miguel_lm.socialmedia.R;
import com.miguel_lm.socialmedia.model.Post;
import com.miguel_lm.socialmedia.provaiders.AuthProvider;
import com.miguel_lm.socialmedia.provaiders.ImageProvider;
import com.miguel_lm.socialmedia.provaiders.PostProvider;
import com.miguel_lm.socialmedia.utils.FileUtil;
import com.miguel_lm.socialmedia.utils.ViewedMessageHelper;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;


public class PostActivity extends AppCompatActivity {

    ImageView iv_subirImagen, iv_subirImagen2, iv_pc, iv_ps4, iv_xbox, iv_nintendo;
    File mImageFile;
    File mImageFile2;
    Button btn_publicar;
    ImageProvider imageProvider;
    TextInputEditText ed_name_game, ed_description;
    String mCategory = "", title = "", description = "";
    PostProvider postProvider;
    AuthProvider mAuth;
    TextView tv_category;
    AlertDialog mDialog;
    CircleImageView circleImageBack;
    AlertDialog.Builder mBuilderSelector;
    CharSequence[] options;

    //FOTO 1
    String absolutePhotoPath;
    String mPhotoPath;
    File mPhotoFile;

    //FOTO 2
    String absolutePhotoPath2;
    String mPhotoPath2;
    File mPhotoFile2;


    private final int GALLERY_REQUEST_CODE = 1;
    private final int GALLERY_REQUEST_CODE_2 = 2;
    private static final int PHOTO_REQUEST_CODE = 3;
    private static final int PHOTO_REQUEST_CODE_2 = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        init();
        events();
    }

    @Override
    public void onStart() {
        super.onStart();
        ViewedMessageHelper.updateOnline(true, PostActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ViewedMessageHelper.updateOnline(false, PostActivity.this);
    }

    private void init() {

        iv_subirImagen = findViewById(R.id.iv_subirImagen);
        iv_subirImagen2 = findViewById(R.id.iv_subirImagen2);
        btn_publicar = findViewById(R.id.btn_publicar);
        ed_name_game = findViewById(R.id.ed_name_game);
        ed_description = findViewById(R.id.ed_description);
        tv_category = findViewById(R.id.tv_category);
        iv_pc = findViewById(R.id.iv_pc);
        iv_ps4 = findViewById(R.id.iv_ps4);
        iv_xbox = findViewById(R.id.iv_xbox);
        iv_nintendo = findViewById(R.id.iv_nintendo);
        circleImageBack = findViewById(R.id.circleImageBackPost);

        mDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Espere un momento")
                .setCancelable(false).build();

        mBuilderSelector = new AlertDialog.Builder(this);
        mBuilderSelector.setTitle("Selecciona una opción");
        options = new CharSequence[]{"Imagen de galeria", "Tomar una foto"};

        imageProvider = new ImageProvider();
        postProvider = new PostProvider();
        mAuth = new AuthProvider();
    }

    @SuppressLint("SetTextI18n")
    private void events(){

        iv_subirImagen.setOnClickListener(v -> selectOptionImage(1));
        iv_subirImagen2.setOnClickListener(v -> selectOptionImage(2));
        btn_publicar.setOnClickListener(v -> clickPost());
        iv_pc.setOnClickListener(v -> changeNameCategory("PC"));
        iv_ps4.setOnClickListener(v -> changeNameCategory("PS4"));
        iv_xbox.setOnClickListener(v -> changeNameCategory("XBOX"));
        iv_nintendo.setOnClickListener(v -> changeNameCategory("NINTENDO"));
        circleImageBack.setOnClickListener(v -> onBackPressed());
    }

    private void selectOptionImage(final int numberImage) {

        mBuilderSelector.setItems(options, (dialog, which) -> {
            if(which == 0){
                if(numberImage == 1){
                    openGallery(GALLERY_REQUEST_CODE);

                } else if(numberImage == 2){
                    openGallery(GALLERY_REQUEST_CODE_2);
                }

            } else if(which == 1) {
                if(numberImage == 1){
                    takePhoto(PHOTO_REQUEST_CODE);

                } else if(numberImage == 2){
                    takePhoto(PHOTO_REQUEST_CODE_2);
                }
            }
        });

        mBuilderSelector.show();
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void takePhoto(int requestCode) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(getPackageManager()) != null){
            File photoFile = null;
            try{
                photoFile = createPhotoFile(requestCode);

            } catch(Exception e){
                Toast.makeText(PostActivity.this,
                        "Hubo un error con el archivo" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            if(photoFile != null){
                Uri photoUri = FileProvider.getUriForFile(PostActivity.this, "com.miguel_lm.socialmedia", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri);
                startActivityForResult(takePictureIntent, requestCode);
            }
        }
    }

    private File createPhotoFile(int requestCode) throws IOException {

       File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
       File photoFile = File.createTempFile(new Date() + "_photo", ".jpg", storageDir);

       if(requestCode == PHOTO_REQUEST_CODE){
           mPhotoPath = "file:" + photoFile.getAbsolutePath();
           absolutePhotoPath = photoFile.getAbsolutePath();

       } else if(requestCode == PHOTO_REQUEST_CODE_2){
           mPhotoPath2 = "file:" + photoFile.getAbsolutePath();
           absolutePhotoPath2 = photoFile.getAbsolutePath();
       }

        return photoFile;
    }

    private void changeNameCategory(String category){

        mCategory = category;
        tv_category.setText(category);
    }

    private void clickPost() {

        title = Objects.requireNonNull(ed_name_game.getText()).toString();
        description = Objects.requireNonNull(ed_description.getText()).toString();

        if(!title.isEmpty() && !description.isEmpty() && !mCategory.isEmpty()){
            //SELECCION DE AMABAS FOTOS DE LA GALERIA
            if(mImageFile != null && mImageFile2 != null){
                saveImage(mImageFile, mImageFile2);
            }

            //SE TOMARON AMABAS FOTOS DESDE LA CAMARA
            else if(mPhotoFile != null && mPhotoFile2 != null){
                saveImage(mPhotoFile, mPhotoFile2);
            }

            //SELECCION DE LA FOTO 1 EN LA GALERIA Y SE TOMA CON LA CAMARA LA FOTO 2
            else if(mImageFile != null && mPhotoFile2 != null){
                saveImage(mImageFile, mPhotoFile2);
            }

            //SE TOMA CON LA CAMARA LA FOTO 1 Y SE SELECCIONA EN LA GALERIA LA FOTO 2
            else if(mPhotoFile != null && mImageFile2 != null){
                saveImage(mPhotoFile, mImageFile2);

            } else {
                Toast.makeText(PostActivity.this, "Debes seleccionar una imagen", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(PostActivity.this, "Completa los campos para publicar", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImage(File imageFile, final File imageFile2) {

        mDialog.show();
        imageProvider.save(PostActivity.this, imageFile).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                imageProvider.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                    final String url = uri.toString();
                    imageProvider.save(PostActivity.this, imageFile2).addOnCompleteListener(taskSave2 -> {
                        if(taskSave2.isSuccessful()){
                            imageProvider.getStorage().getDownloadUrl().addOnSuccessListener(uri2 -> {
                                String url2 = uri2.toString();
                                Post post = new Post();
                                post.setImage1(url);
                                post.setImage2(url2);
                                post.setTitle(title.toLowerCase());
                                post.setDescription(description);
                                post.setCategory(mCategory);
                                post.setIdUser(mAuth.getUid());
                                post.setTimestamp(new Date().getTime());
                                postProvider.save(post).addOnCompleteListener(taskSave -> {
                                    mDialog.dismiss();
                                    if(taskSave.isSuccessful()){
                                        clearForm();
                                        Toast.makeText(PostActivity.this,
                                                "La información se almacenó correctamente", Toast.LENGTH_SHORT).show();

                                    } else {
                                        Toast.makeText(PostActivity.this,
                                                "No se ha podido almacenar la información de la imagen 1", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            });

                        } else {
                            mDialog.dismiss();
                            Toast.makeText(PostActivity.this,
                                    "No se ha podido almacenar la información de la imagen 2", Toast.LENGTH_SHORT).show();
                        }
                    });
                });

            } else {
                mDialog.dismiss();
                Toast.makeText(PostActivity.this, "La imagen no se ha podido almacenar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void clearForm() {

        ed_name_game.setText("");
        ed_description.setText("");
        tv_category.setText("CATEGORIA");
        iv_subirImagen.setImageResource(R.drawable.upload_image);
        iv_subirImagen2.setImageResource(R.drawable.upload_image);
        title = "";
        description = "";
        mImageFile = null;
        mImageFile2 = null;
    }

    private void openGallery(int requestCode) {

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, requestCode);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /*
         *  SELECCIÓN DE LA IMAGEN DESDE LA GALERIA
        */
        if(requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK){
            try{
                mPhotoFile = null;
                mImageFile = FileUtil.from(this, Objects.requireNonNull(data).getData());
                iv_subirImagen.setImageBitmap(BitmapFactory.decodeFile(mImageFile.getAbsolutePath()));

            } catch(Exception e){
                Log.d("ERROR", "Se produjo un error" + e.getMessage());
                Toast.makeText(PostActivity.this, "Se produjo un error" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        if(requestCode == GALLERY_REQUEST_CODE_2 && resultCode == RESULT_OK){
            try{
                mPhotoFile2 = null;
                mImageFile2 = FileUtil.from(this, Objects.requireNonNull(data).getData());
                iv_subirImagen2.setImageBitmap(BitmapFactory.decodeFile(mImageFile2.getAbsolutePath()));

            } catch(Exception e){
                Log.d("ERROR", "Se produjo un error" + e.getMessage());
                Toast.makeText(PostActivity.this, "Se produjo un error" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        /*
         *  SELECCIÓN DE LA IMAGEN DESDE LA GALERIA
        */
        if(requestCode == PHOTO_REQUEST_CODE && resultCode == RESULT_OK){
            mImageFile = null;
            mPhotoFile = new File(absolutePhotoPath);
            Picasso.with(PostActivity.this).load(mPhotoPath).into(iv_subirImagen);
        }

        if(requestCode == PHOTO_REQUEST_CODE_2 && resultCode == RESULT_OK){
            mImageFile2 = null;
            mPhotoFile2 = new File(absolutePhotoPath2);
            Picasso.with(PostActivity.this).load(mPhotoPath2).into(iv_subirImagen2);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}