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
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import com.miguel_lm.socialmedia.R;
import com.miguel_lm.socialmedia.model.User;
import com.miguel_lm.socialmedia.provaiders.AuthProvider;
import com.miguel_lm.socialmedia.provaiders.ImageProvider;
import com.miguel_lm.socialmedia.provaiders.UserProvider;
import com.miguel_lm.socialmedia.utils.FileUtil;
import com.miguel_lm.socialmedia.utils.ViewedMessageHelper;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;


public class EditProfileActivity extends AppCompatActivity {

    CircleImageView mCircleImageView, circle_image_profile;
    ImageView iv_cover_image;
    TextInputEditText ed_name_profile, ed_phone_profile;
    Button btn_edit_profile;
    AlertDialog mDialog;
    AlertDialog.Builder mBuilderSelector;
    CharSequence[] options;
    File mImageFile;
    File mImageFile2;
    String username = "";
    String phone = "";
    String imageProfile = "";
    String imageCover = "";
    ImageProvider imageProvider;
    UserProvider userProvider;
    AuthProvider authProvider;

    //FOTO 1
    String absolutePhotoPath;
    String mPhotoPath;
    File mPhotoFile;

    //FOTO 2
    String absolutePhotoPath2;
    String mPhotoPath2;
    File mPhotoFile2;

    private final int GALLERY_REQUEST_CODE_PROFILE = 1;
    private final int GALLERY_REQUEST_CODE_COVER = 2;
    private static final int PHOTO_REQUEST_CODE_PROFILE = 3;
    private static final int PHOTO_REQUEST_CODE_COVER = 4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        init();
        events();
        getUser();
    }

    @Override
    public void onStart() {
        super.onStart();
        ViewedMessageHelper.updateOnline(true, EditProfileActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ViewedMessageHelper.updateOnline(false, EditProfileActivity.this);
    }

    private void init(){

        mCircleImageView = findViewById(R.id.circleImageArrowBack);
        circle_image_profile = findViewById(R.id.circle_image_profile);
        iv_cover_image = findViewById(R.id.iv_cover_image);
        ed_name_profile = findViewById(R.id.ed_name_profile);
        ed_phone_profile = findViewById(R.id.ed_phone_profile);
        btn_edit_profile = findViewById(R.id.btn_edit_profile);

        mBuilderSelector = new AlertDialog.Builder(this);
        mBuilderSelector.setTitle("Selecciona una opción");
        options = new CharSequence[]{"Imagen de galeria", "Tomar una foto"};

        imageProvider = new ImageProvider();
        userProvider = new UserProvider();
        authProvider = new AuthProvider();

        mDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Espere un momento")
                .setCancelable(false).build();
    }

    private void events(){

        mCircleImageView.setOnClickListener(v -> onBackPressed());
        circle_image_profile.setOnClickListener(v -> selectOptionImage(1));
        iv_cover_image.setOnClickListener(v -> selectOptionImage(2));
        btn_edit_profile.setOnClickListener(v -> clickEditProfile());
    }

    private void getUser(){

        userProvider.getUser(authProvider.getUid()).addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot.exists()){
                if(documentSnapshot.contains("username")){
                    username = documentSnapshot.getString("username");
                    ed_name_profile.setText(username);
                }
                if(documentSnapshot.contains("phone")){
                    phone = documentSnapshot.getString("phone");
                    ed_phone_profile.setText(phone);
                }
                if(documentSnapshot.contains("image_profile")){
                    imageProfile = documentSnapshot.getString("image_profile");
                    if(imageProfile != null){
                        if(!imageProfile.isEmpty()){
                            Picasso.with(EditProfileActivity.this).load(imageProfile).into(circle_image_profile);
                        }
                    }
                }
                if(documentSnapshot.contains("image_cover")){
                    imageCover = documentSnapshot.getString("image_cover");
                    if(imageCover != null){
                        if(!imageCover.isEmpty()){
                            Picasso.with(EditProfileActivity.this).load(imageCover).into(iv_cover_image);
                        }
                    }
                }
            }
        });
    }

    private void clickEditProfile() {

        username = Objects.requireNonNull(ed_name_profile.getText()).toString();
        phone = Objects.requireNonNull(ed_phone_profile.getText()).toString();

        if(!username.isEmpty() && !phone.isEmpty()){
            //comprobacionDatosIntroducidos(username, phone);

            //SELECCION DE AMABAS FOTOS DE LA GALERIA
            if(mImageFile != null && mImageFile2 != null){
                saveImageCoverAndProfile(mImageFile, mImageFile2);
            }

            //SE TOMARON AMABAS FOTOS DESDE LA CAMARA
            else if(mPhotoFile != null && mPhotoFile2 != null){
                saveImageCoverAndProfile(mPhotoFile, mPhotoFile2);
            }

            //SELECCION DE LA FOTO 1 EN LA GALERIA Y SE TOMA CON LA CAMARA LA FOTO 2
            else if(mImageFile != null && mPhotoFile2 != null){
                saveImageCoverAndProfile(mImageFile, mPhotoFile2);
            }

            //SE TOMA CON LA CAMARA LA FOTO 1 Y SE SELECCIONA EN LA GALERIA LA FOTO 2
            else if(mPhotoFile != null && mImageFile2 != null){
                saveImageCoverAndProfile(mPhotoFile, mImageFile2);

            } else if(mPhotoFile != null){
                saveImage(mPhotoFile, true);

            } else if(mPhotoFile2 != null){
                saveImage(mPhotoFile2, false);

            } else if(mImageFile != null){
                saveImage(mImageFile, true);

            } else if(mImageFile2 != null){
                saveImage(mImageFile2, false);

            } else {
                User user = new User();
                user.setUsername(username);
                user.setPhone(phone);
                user.setId(authProvider.getUid());
                updateInfo(user);
            }

        } else {
            Toast.makeText(EditProfileActivity.this,
                    "Ingresar nombre de usuario y teléfono", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImageCoverAndProfile(File imageFile, final File imageFile2) {

        mDialog.show();
        imageProvider.save(EditProfileActivity.this, imageFile).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                imageProvider.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                    final String urlProfile = uri.toString();
                    imageProvider.save(EditProfileActivity.this, imageFile2).addOnCompleteListener(taskSave2 -> {
                        if(taskSave2.isSuccessful()){
                            imageProvider.getStorage().getDownloadUrl().addOnSuccessListener(uri2 -> {
                                String urlCover = uri2.toString();
                                User user = new User();
                                user.setUsername(username);
                                user.setPhone(phone);
                                user.setImageProfile(urlProfile);
                                user.setImageCover(urlCover);
                                user.setId(authProvider.getUid());
                                updateInfo(user);
                            });

                        } else {
                            mDialog.dismiss();
                            Toast.makeText(EditProfileActivity.this,
                                    "No se ha podido almacenar la información de la imagen 2", Toast.LENGTH_SHORT).show();
                        }
                    });
                });

            } else {
                mDialog.dismiss();
                Toast.makeText(EditProfileActivity.this, "La imagen no se ha podido almacenar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveImage(File image, boolean isProfileImage){

        mDialog.show();
        imageProvider.save(EditProfileActivity.this, image).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                imageProvider.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                    final String url = uri.toString();
                    User user = new User();
                    user.setUsername(username);
                    user.setPhone(phone);

                    if(isProfileImage){
                        user.setImageProfile(url);
                        user.setImageCover(imageCover);

                    } else {
                        user.setImageCover(url);
                        user.setImageProfile(imageProfile);
                    }

                    user.setId(authProvider.getUid());
                    updateInfo(user);
                });

            } else {
                mDialog.dismiss();
                Toast.makeText(EditProfileActivity.this, "La imagen no se ha podido almacenar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateInfo(User user){

        if(mDialog.isShowing()){
            mDialog.show();
        }

        userProvider.update(user).addOnCompleteListener(taskUpdateInfo -> {

            if(taskUpdateInfo.isSuccessful()){
                mDialog.dismiss();
                Toast.makeText(EditProfileActivity.this,
                        "La información se actualizó correctamente", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(EditProfileActivity.this,
                        "La información no se pudo actualizar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectOptionImage(final int numberImage) {

        mBuilderSelector.setItems(options, (dialog, which) -> {
            if(which == 0){
                if(numberImage == 1){
                    openGallery(GALLERY_REQUEST_CODE_PROFILE);

                } else if(numberImage == 2){
                    openGallery(GALLERY_REQUEST_CODE_COVER);
                }

            } else if(which == 1) {
                if(numberImage == 1){
                    takePhoto(PHOTO_REQUEST_CODE_PROFILE);

                } else if(numberImage == 2){
                    takePhoto(PHOTO_REQUEST_CODE_COVER);
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
                Toast.makeText(EditProfileActivity.this,
                        "Hubo un error con el archivo" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            if(photoFile != null){
                Uri photoUri = FileProvider.getUriForFile(EditProfileActivity.this, "com.miguel_lm.socialmedia", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri);
                startActivityForResult(takePictureIntent, requestCode);
            }
        }
    }

    private File createPhotoFile(int requestCode) throws IOException {

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File photoFile = File.createTempFile(new Date() + "_photo", ".jpg", storageDir);

        if(requestCode == PHOTO_REQUEST_CODE_PROFILE){
            mPhotoPath = "file:" + photoFile.getAbsolutePath();
            absolutePhotoPath = photoFile.getAbsolutePath();

        } else if(requestCode == PHOTO_REQUEST_CODE_COVER){
            mPhotoPath2 = "file:" + photoFile.getAbsolutePath();
            absolutePhotoPath2 = photoFile.getAbsolutePath();
        }

        return photoFile;
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
        if(requestCode == GALLERY_REQUEST_CODE_PROFILE && resultCode == RESULT_OK){
            try{
                mPhotoFile = null;
                mImageFile = FileUtil.from(this, Objects.requireNonNull(data).getData());
                circle_image_profile.setImageBitmap(BitmapFactory.decodeFile(mImageFile.getAbsolutePath()));

            } catch(Exception e){
                Log.d("ERROR", "Se produjo un error" + e.getMessage());
                Toast.makeText(EditProfileActivity.this, "Se produjo un error" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        if(requestCode == GALLERY_REQUEST_CODE_COVER && resultCode == RESULT_OK){
            try{
                mPhotoFile2 = null;
                mImageFile2 = FileUtil.from(this, Objects.requireNonNull(data).getData());
                iv_cover_image.setImageBitmap(BitmapFactory.decodeFile(mImageFile2.getAbsolutePath()));

            } catch(Exception e){
                Log.d("ERROR", "Se produjo un error" + e.getMessage());
                Toast.makeText(EditProfileActivity.this, "Se produjo un error" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        /*
         *  SELECCIÓN DE LA IMAGEN DESDE LA GALERIA
         */
        if(requestCode == PHOTO_REQUEST_CODE_PROFILE && resultCode == RESULT_OK){
            mImageFile = null;
            mPhotoFile = new File(absolutePhotoPath);
            Picasso.with(EditProfileActivity.this).load(mPhotoPath).into(circle_image_profile);
        }

        if(requestCode == PHOTO_REQUEST_CODE_COVER && resultCode == RESULT_OK){
            mImageFile2 = null;
            mPhotoFile2 = new File(absolutePhotoPath2);
            Picasso.with(EditProfileActivity.this).load(mPhotoPath2).into(iv_cover_image);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}