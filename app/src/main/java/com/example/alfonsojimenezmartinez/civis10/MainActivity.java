package com.example.alfonsojimenezmartinez.civis10;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    static final int REQUEST_TAKE_PHOTO = 1;

    static final int REQUEST_GALLERY = 0;


    String ConvertImage = "0";

    Bitmap bitmap;

    boolean check = true;

    Button SelectImageGallery, UploadImageServer, SelectCamera;

    ImageView imageView;

    EditText imageName, title;

    ProgressDialog progressDialog ;

    String GetImageNameEditText, GetTitleEditText, GetCategory,  GetDateTime, mCurrentPhotoPath;

    String ImageName = "user_id" ;

    String Title = "title";

    String Category = "category";

    String ImagePath = "image_path" ;

    String DateTime = "datetime" ;


    String ServerUploadPath ="https://civismadrid.000webhostapp.com/report_upload_to_server.php" ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Spinner categorySpinner = (Spinner) findViewById(R.id.category_spinner);
        final Button buttonUpload = (Button) findViewById(R.id.buttonUpload);

        String [] valores = getResources().getStringArray(R.array.category_array);

        ArrayAdapter<String> adaptador = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,valores);


        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                GetCategory = categorySpinner.getSelectedItem().toString();
                Log.d(TAG, "onItemSelected");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        buttonUpload.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                try{
                    if(categorySpinner.isSelected()){


                    }else{
                        Toast toast = Toast.makeText(getBaseContext(), "Seleccione la categoría", Toast.LENGTH_SHORT);
                        toast.show();
                    }

                }catch(Exception e){}
            }
        });


        // Does your device have a camera?
        if (hasCamera()) {
        }

        // Do you have Camera Apps?
        if (hasDefualtCameraApp(MediaStore.ACTION_IMAGE_CAPTURE)) {
        }

        imageView = (ImageView)findViewById(R.id.imageView);

        imageName = (EditText)findViewById(R.id.editTextImageName);

        title = (EditText)findViewById(R.id.editTextTitle);

        SelectImageGallery = (Button)findViewById(R.id.buttonSelectGallery);

        UploadImageServer = (Button)findViewById(R.id.buttonUpload);

        SelectCamera = (Button) findViewById(R.id.buttonSelectCamera);

        SelectImageGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();

                intent.setType("image/*");

                intent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(intent, "Select Image From Gallery"), REQUEST_GALLERY);

            }
        });

        SelectCamera.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

                dispatchTakePictureIntent();
            }
        });

        UploadImageServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                GetImageNameEditText = imageName.getText().toString();

                GetTitleEditText = title.getText().toString();

                GetDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

                ImageUploadToServerFunction();



            }
        });


    }

    // method to check you have a Camera
    private boolean hasCamera() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    // method to check you have Camera Apps
    private boolean hasDefualtCameraApp(String action) {
        final PackageManager packageManager = getPackageManager();
        final Intent intent = new Intent(action);
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

        return list.size() > 0;

    }

    private void dispatchTakePictureIntent() {


        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            // Create the File where the photo should go
            File photoFile = null;

            try {

                photoFile = createImageFile();

            } catch (IOException e) {
                // Error occurred while creating the File
                Log.e(TAG, e.getMessage());
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                takePictureIntent.putExtra("mCurrentPhotoPath",mCurrentPhotoPath);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);

            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            switch(requestCode ){
                case(REQUEST_TAKE_PHOTO):{

                    if (resultCode == Activity.RESULT_OK) {

                        bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);

                        imageView.setImageBitmap(bitmap);

                    }else { // Activity.RESULT_CANCELLED
                        Log.i(TAG, "onActivityResult nada seleccionado");
                        Toast.makeText(getBaseContext(),
                                "Cancelado", Toast.LENGTH_LONG)
                                .show();
                    }
                }
                case(REQUEST_GALLERY):{

                    if (resultCode == Activity.RESULT_OK){

                        Uri uri = data.getData();

                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

                        imageView.setImageBitmap(bitmap);

                    }else { // Activity.RESULT_CANCELLED
                        Log.i(TAG, "onActivityResult nada seleccionado");
                        Toast.makeText(getBaseContext(),
                                "Cancelado", Toast.LENGTH_LONG)
                                .show();
                    }
                }
            }

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG)
                    .show();
        } // catch
    } // onActivityResult
    /*@Override
    protected void onActivityResult(int RC, int RQC, Intent I) {

        super.onActivityResult(RC, RQC, I);

        if (RC == 1 && RQC == RESULT_OK && I != null && I.getData() != null) {

            Uri uri = I.getData();

            try {

                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

                imageView.setImageBitmap(bitmap);

            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }*/
//*********************************************************UPLOAD*********************************************************************
    public void ImageUploadToServerFunction(){

        //Creamos el bitmap a partir de la foto que hemos tomado con la camara

        ByteArrayOutputStream byteArrayOutputStreamObject ;

        byteArrayOutputStreamObject = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStreamObject);

        byte[] byteArrayVar = byteArrayOutputStreamObject.toByteArray();

        ConvertImage = Base64.encodeToString(byteArrayVar, Base64.DEFAULT);

        AsyncTaskUploadClass AsyncTaskUploadClassOBJ = new AsyncTaskUploadClass();

        AsyncTaskUploadClassOBJ.execute();



    }

    public class ImageProcessClass{

        public String ImageHttpRequest(String requestURL,HashMap<String, String> PData) {

            StringBuilder stringBuilder = new StringBuilder();

            try {

                URL url;
                HttpURLConnection httpURLConnectionObject ;
                OutputStream OutPutStream;
                BufferedWriter bufferedWriterObject ;
                BufferedReader bufferedReaderObject ;
                int RC ;

                url = new URL(requestURL);

                httpURLConnectionObject = (HttpURLConnection) url.openConnection();

                httpURLConnectionObject.setReadTimeout(19000);

                httpURLConnectionObject.setConnectTimeout(19000);

                httpURLConnectionObject.setRequestMethod("POST");

                httpURLConnectionObject.setDoInput(true);

                httpURLConnectionObject.setDoOutput(true);

                OutPutStream = httpURLConnectionObject.getOutputStream();

                bufferedWriterObject = new BufferedWriter(

                        new OutputStreamWriter(OutPutStream, "UTF-8"));

                bufferedWriterObject.write(bufferedWriterDataFN(PData));

                bufferedWriterObject.flush();

                bufferedWriterObject.close();

                OutPutStream.close();

                RC = httpURLConnectionObject.getResponseCode();

                if (RC == HttpsURLConnection.HTTP_OK) {

                    bufferedReaderObject = new BufferedReader(new InputStreamReader(httpURLConnectionObject.getInputStream()));

                    stringBuilder = new StringBuilder();

                    String RC2;

                    while ((RC2 = bufferedReaderObject.readLine()) != null){

                        stringBuilder.append(RC2);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return stringBuilder.toString();
        }

        private String bufferedWriterDataFN(HashMap<String, String> HashMapParams) throws UnsupportedEncodingException {

            StringBuilder stringBuilderObject;

            stringBuilderObject = new StringBuilder();

            for (Map.Entry<String, String> KEY : HashMapParams.entrySet()) {

                if (check)

                    check = false;
                else
                    stringBuilderObject.append("&");

                stringBuilderObject.append(URLEncoder.encode(KEY.getKey(), "UTF-8"));

                stringBuilderObject.append("=");

                stringBuilderObject.append(URLEncoder.encode(KEY.getValue(), "UTF-8"));
            }

            return stringBuilderObject.toString();
        }

    }
    class AsyncTaskUploadClass extends AsyncTask<Void,Void,String> {

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            progressDialog = ProgressDialog.show(MainActivity.this,"Image is Uploading","Please Wait",false,false);
        }

        @Override
        protected void onPostExecute(String string1) {

            super.onPostExecute(string1);

            // Dismiss the progress dialog after done uploading.
            progressDialog.dismiss();

            // Printing uploading success message coming from server on android app.
            Toast.makeText(MainActivity.this,string1,Toast.LENGTH_LONG).show();

            // Setting image as transparent after done uploading.
            imageView.setImageResource(android.R.color.transparent);


        }

        @Override
        protected String doInBackground(Void... params) {

            ImageProcessClass imageProcessClass = new ImageProcessClass();

            HashMap<String,String> HashMapParams = new HashMap<String,String>();

            HashMapParams.put(ImageName, GetImageNameEditText);

            HashMapParams.put(Title, GetTitleEditText);

            HashMapParams.put(Category, GetCategory);

            HashMapParams.put(ImagePath, ConvertImage);

            HashMapParams.put(DateTime, GetDateTime);




            String FinalData = imageProcessClass.ImageHttpRequest(ServerUploadPath, HashMapParams);

            return FinalData;
        }
    }

}