package com.example.RecieptScanner;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.websitebeaver.documentscanner.DocumentScanner;
import com.websitebeaver.documentscanner.constants.ImageProvider;
import com.websitebeaver.documentscanner.utils.ImageUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProcessReciept extends AppCompatActivity {

    MyDatabaseHelper myDB;
    int calculate_discounts = 0;
    Uri imageUri;
    Bitmap imageBitmap;
    TextRecognizer textRecognizer;

    ImageView cropped_image_view;

    List<String> product_holder, price_holder;

    String[] limiters = {
            "Suma",
            "Suma PLN",
            "Suma:"
    };

    ImageUtil imageUtil = new ImageUtil();

    Button cancel_btn, process_btn;

    DocumentScanner documentScanner = new DocumentScanner(
            this,
            (croppedImageResults) -> {
                imageBitmap = imageUtil.readBitmapFromFileUriString(
                        croppedImageResults.get(0),
                        getContentResolver()
                );

                cropped_image_view.setImageBitmap(imageBitmap);

                recognizeText();
                return null;
            },
            (errorMessage) -> {
                // an error happened
                Log.v("documentscannerlogs", errorMessage);
                return null;
            },
            () -> {
                // user canceled document scan
                Log.v("documentscannerlogs", "User canceled document scan");
                return null;
            },
            null,
            null,
            null, null, ImageProvider.CAMERA

    );

    DocumentScanner documentScanner2 = new DocumentScanner(
            this,
            (croppedImageResults) -> {

                imageBitmap = imageUtil.readBitmapFromFileUriString(
                        croppedImageResults.get(0),
                        getContentResolver()
                );

                cropped_image_view.setImageBitmap(imageBitmap);
                recognizeText();
                return null;
            },
            (errorMessage) -> {
                // an error happened
                Log.v("documentscannerlogs", errorMessage);
                return null;
            },
            () -> {
                // user canceled document scan
                Log.v("documentscannerlogs", "User canceled document scan");
                return null;
            },
            null,
            null,
            null, null, ImageProvider.GALLERY


    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.processreciept_layout);
        cropped_image_view = findViewById(R.id.image_view);
        cancel_btn = findViewById(R.id.cancel_btn);
        process_btn= findViewById(R.id.process_button);
        myDB = new MyDatabaseHelper(ProcessReciept.this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String value = extras.getString("key");
            if(value.equals("camera")) {
                Intent intent = new Intent(documentScanner.createDocumentScanIntent());
                someActivityResultLauncher.launch(intent);
            }
            if(value.equals("gallery")) {
                Intent intent = new Intent(documentScanner2.createDocumentScanIntent());
                someActivityResultLauncher.launch(intent);
            }
        }

        process_btn.setEnabled(false);
        process_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent add= new Intent(getApplicationContext(), AddProducts.class);
                add.putStringArrayListExtra("product_array", (ArrayList<String>) product_holder);
                add.putStringArrayListExtra("price_array", (ArrayList<String>) price_holder);
                startActivity(add);
                finish();
            }
        });

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Here, no request code
                        Intent data = result.getData();
                        documentScanner.handleDocumentScanIntentResult(result);

                    }
                }
            });

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK){
            if(data!=null){
                imageUri = data.getData();
            }
        }
        else {
            Toast.makeText(this, "Nie wybrano obrazu", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void recognizeText() {

        if (imageBitmap!=null){
                InputImage inputImage = InputImage.fromBitmap(imageBitmap,0);
                Task<Text> result = textRecognizer.process(inputImage)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text text) {
                                String recognizedText = text.getText();
                                if(text.getText()!="") {
                                    prepareTextRecognitionResult(text);
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ProcessReciept.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });
        }

    }

    private void prepareTextRecognitionResult(Text text) {

        String processedText = "";
        List<String> preparedText_array = new ArrayList<String>();
        List<Text.TextBlock> blocks = text.getTextBlocks();
        Point[] upperBorderCorners = {};
        float text_orientation = 0;
        Point[] lowerBorderCorners = {};
        int lowerBorderFinalCorner = 10000;
        int upperBorderFinalCorner = -1;
        Point[] currentLineCorners = {};

        Log.d("documentscannerlogs", String.valueOf(text.getTextBlocks().size()));

        for (int k = 0; k < text.getTextBlocks().size(); k++ ){
            for (int i = 0; i < blocks.get(k).getLines().size(); i++){
                Log.d("qwe",blocks.get(k).getLines().get(i).getText()+"\n");
            }
        }

        if(text.getTextBlocks().size()==0){
            Toast.makeText(this, "Coś poszło nie tak", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int k = 0; k < text.getTextBlocks().size(); k++ ){
            for (int i = 0; i < blocks.get(k).getLines().size(); i++){
                String compare_upper = blocks.get(k).getLines().get(i).getText();
                if(compare_upper.equalsIgnoreCase("PARAGON FISKALNY") || LevenshteinDistance(compare_upper, "PARAGON FISKALNY")<1 || compare_upper.equalsIgnoreCase("PARAG0N FISKALNY") || LevenshteinDistance(compare_upper, "PARAG0N FISKALNY")<1 || compare_upper.equalsIgnoreCase("PARAGON")){
                    upperBorderCorners = blocks.get(k).getLines().get(i).getCornerPoints();
                    text_orientation = blocks.get(k).getLines().get(i).getAngle();

                    Log.d("rotation", String.valueOf(blocks.get(k).getLines().get(i).getAngle()));
                    if (upperBorderCorners != null) {
                        upperBorderFinalCorner = upperBorderCorners[0].y;
                    } else{
                        return;
                    }

                }
                for (int j = 0; j < limiters.length; j++) {
                    String compare_lower = blocks.get(k).getLines().get(i).getText();
                    if (compare_lower.equalsIgnoreCase(limiters[j]) || LevenshteinDistance(compare_lower, limiters[j])<1) {

                            lowerBorderCorners = blocks.get(k).getLines().get(i).getCornerPoints();
                            if((lowerBorderCorners != null ? lowerBorderCorners[0].y : 0) <lowerBorderFinalCorner){
                                assert lowerBorderCorners != null;
                                lowerBorderFinalCorner = lowerBorderCorners[0].y;
                            }

                    }
                }

            }

        }

        if(upperBorderFinalCorner<0){
            Toast.makeText(this, "Coś poszło nie tak", Toast.LENGTH_SHORT).show();
            return;
        }

        if( text_orientation>10 || text_orientation<-10){
            Toast.makeText(this, "Nieprawidłowa orientacja paragonu", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int k = 0; k < text.getTextBlocks().size(); k++ ){
            for (int i = 0; i < blocks.get(k).getLines().size(); i++) {
                currentLineCorners = blocks.get(k).getLines().get(i).getCornerPoints();
                if (currentLineCorners != null && currentLineCorners[0].y > upperBorderFinalCorner && currentLineCorners[2].y < lowerBorderFinalCorner) {
                    processedText = processedText + blocks.get(k).getLines().get(i).getText() + "\n";
                    preparedText_array.add(blocks.get(k).getLines().get(i).getText());
                }

            }
        }
        calculate_discounts = myDB.getIncludeDiscoutsValue();

        if(calculate_discounts == 0) {
            process_result(preparedText_array);
        } else {
            process_result_including_discounts(preparedText_array);
        }

    }
    private void process_result_including_discounts(List<String> input){
        List<String> product= new ArrayList<String>();
        List<String> price= new ArrayList<String>();

        Pattern p1;
        Matcher m1;

        for(int i = 0; i < input.size();i++){
            input.set(i, input.get(i).replaceAll("([A-Za-z][ ]+(\\d+[,.]\\d{3}))",""));

            input.set(i, input.get(i).replaceAll("[,]", "."));
            input.set(i, input.get(i).replaceAll("[.][.]", "."));
            input.set(i, input.get(i).replaceAll("[ ][.]", "."));
            input.set(i, input.get(i).replaceAll("[.][ ]", "."));
            input.set(i, input.get(i).replaceAll("[ô]","6"));
            input.set(i, input.get(i).replaceAll("[ớ]","6"));
            input.set(i, input.get(i).replaceAll("[ồ]","6"));
            input.set(i, input.get(i).replaceAll("[ố]","6"));
            input.set(i, input.get(i).replaceAll("[ổ]","6"));
            input.set(i, input.get(i).replaceAll("[ỗ]","6"));
            input.set(i, input.get(i).replaceAll("(\\d+[xX]\\d+[,.]\\d{1,5})","")); //{1,2}
            input.set(i, input.get(i).replaceAll("(\\d+[ ][xX]\\d+[,.]\\d{1,5})","")); //{1,2}
            input.set(i, input.get(i).replaceAll("(\\d+[xX][ ]\\d+[,.]\\d{1,5})","")); //{1,2}
            input.set(i, input.get(i).replaceAll("(\\d+[ ][xX][ ]\\d+[,.]\\d{1,5})","")); //{1,2}
            input.set(i, input.get(i).replaceAll("(\\d+[xX]\\d+[,.]\\d{1,5})","")); //{1,2}
            input.set(i, input.get(i).replaceAll("(\\d+[xX][ ]\\d+[,.]\\d{1,5})",""));
            input.set(i, input.get(i).replaceAll("([xX]\\d+[,.]\\d{1,5})","")); //{1,2}
            input.set(i, input.get(i).replaceAll("([xX][ ]\\d+[,.]\\d{1,5})","")); //{1,2}
            input.set(i, input.get(i).replaceAll("[0][.]$",""));
            input.set(i, input.get(i).replaceAll("[0][ ][.]$",""));
            input.set(i, input.get(i).replaceAll("[ ][ ]", " "));
            input.set(i, input.get(i).replaceAll("^[ \\t]", ""));
            input.set(i, input.get(i).trim());

            //Usunięcie błędów rozpoznania oznaczenia podatkowego przy cenie produktu
            p1 = Pattern.compile("(\\d+[,.]\\d{3}$)");
            m1 = p1.matcher(input.get(i));

            while (m1.find()) {
                input.set(i, input.get(i).replaceAll("[8]$", "B"));
                input.set(i, input.get(i).replaceAll("[0]$", "D"));
                input.set(i, input.get(i).replaceAll("[6]$", "B"));
                Log.d("TAG1", m1.group());

            }
        }

        for(int i = 0; i < input.size();i++) {
            Log.d("before", input.get(i));

        }

        //Przygotuj elementy zawierające tylko spacje
        for(int i = 0; i < input.size();i++) {
            if(input.get(i).length()== 1) {
                input.set(i, " ");
            }
            if(input.get(i).trim().isEmpty()) {
                input.set(i, input.get(i).replaceAll("[ ]", ""));
            }

        }

        //Usuniecie zbednych pustych linijek
        ListIterator<String> iter_input = input.listIterator();
        while(iter_input.hasNext()){
            if(iter_input.next().length() == 0){
                iter_input.remove();
            }
        }

        for(int i = 0; i < input.size();i++) {
            Log.d("after", input.get(i));
        }

        Pattern p2;
        Matcher m2;
        Pattern p3;
        Matcher m3;
        Pattern p3_rabaty;
        Matcher m3_rabaty;
        Pattern p4_rabaty;
        Matcher m4_rabaty;
        for(int i = 0; i < input.size();i++){
            boolean found = false;

            p2 = Pattern.compile("((^|\\s)\\d+[,.]\\d{2}[ABCcDEFG]$)");
            m2 = p2.matcher(input.get(i));

            while(m2.find()) {
                found = true;
                //price.add(m2.group());
                if (input.get(i).matches("((^|\\s)\\d+[,.]\\d{2}[ABCcDEFG])$")) {
                    price.add(input.get(i).trim());
                } else {
                    price.add(m2.group().trim());
                    product.add(input.get(i).replaceAll("(\\d+[,.]\\d{2}[ABCcDEFG]$)", "").trim());
                }
            }

                if (!found){
                    p3 = Pattern.compile("((^|\\s)\\d+[,.]\\d{2}[ ][ABCcDEFG]$)");
                    m3 = p3.matcher(input.get(i));
                    while(m3.find()) {
                        found = true;
                        //price.add(m2.group());
                        if(input.get(i).matches("((^|\\s)\\d+[,.]\\d{2}[ ][ABCcDEFG])$"))
                        {
                            price.add(input.get(i).trim());
                        } else{
                            price.add(m3.group().trim());
                            product.add(input.get(i).replaceAll("(\\d+[,.]\\d{2}[ ][ABCcDEFG]$)","").trim());
                        }
                }

            }

            p3_rabaty = Pattern.compile("([-]\\d+[,.]\\d{2}[ABCcDEFG]$)");
            m3_rabaty = p3_rabaty.matcher(input.get(i));
            while(m3_rabaty.find()) {
                if(input.get(i).matches("([-]\\d+[,.]\\d{2}[ABCcDEFG]$)"))
                {
                    price.add(input.get(i).trim());
                }

            }

            p4_rabaty = Pattern.compile("([-]\\d+[,.]\\d{2}$)");
            m4_rabaty = p4_rabaty.matcher(input.get(i));
            while(m4_rabaty.find()) {
                if(input.get(i).matches("([-]\\d+[,.]\\d{2}$)"))
                {
                    price.add(input.get(i).trim());
                }

            }

            if(!found){
                product.add(input.get(i));
            }
        }

        for(int i = 0; i < product.size();i++) {
            if(product.get(i).length() < 3) {
                product.set(i, "");
            }

            if(!product.get(i).trim().matches(".*[a-zA-Z]+.*")) {
                product.set(i, "");
            }
            if(product.get(i).trim().isEmpty()) {
                product.set(i, product.get(i).replaceAll("[ ]", ""));
            }
            if(product.get(i).trim().matches("([-]\\d+[,.]\\d{2}[ABCcDEFG])")) {
                product.set(i, "");
            }
            if(product.get(i).trim().matches("([-]\\d+[,.]\\d{2})")) {
                product.set(i, "");
            }

        }

        ListIterator<String> iter_product = product.listIterator();
        while(iter_product.hasNext()){
            if(iter_product.next().length()==0){
                iter_product.remove();
            }
        }

        for(int i = 0; i<price.size(); i++){
            Log.d("price", price.get(i));
        }

        Pattern p_price_rabaty_case1 = Pattern.compile("([-]+\\d+[,.]\\d{2}[ABCcDEFG])$");
        Matcher m_price_rabaty_case1;
        Pattern p_price_rabaty_case2 = Pattern.compile("([-]+\\d+[,.]\\d{2})$");
        Matcher m_price_rabaty_case2;
        int liczba_rabaty_case2 = 0;

        for(int i = 0; i < price.size();i++) {
            m_price_rabaty_case1 = p_price_rabaty_case1.matcher(price.get(i));

            while(m_price_rabaty_case1.find()) {
                try {
                    double price_after_rabat_case1 = Double.parseDouble(price.get(i-1).replaceAll("[ABCcDEFG]",""))+Double.parseDouble(price.get(i).replaceAll("[ABCcDEFG]",""));
                    price.set(i-1, price_after_rabat_case1+"A");
                    price.set(i,"");

                    if(i<product.size()) {
                        product.set(i, "");
                    }

                } catch (Exception e){
                    break;
                }

            }

            m_price_rabaty_case2 = p_price_rabaty_case2.matcher(price.get(i));
            while(m_price_rabaty_case2.find()) {
                try {
                    if(i!=0) {
                        double price_after_rabat_case2 = Double.parseDouble(price.get(i - 1).replaceAll("[ABCcDEFG]", "")) + Double.parseDouble(price.get(i).replaceAll("[ABCcDEFG]", ""));
                        price.set(i - 1, price_after_rabat_case2 + "A");
                        price.set(i, "");
                    }
                    if(i-liczba_rabaty_case2<product.size()) {
                        product.set(i - liczba_rabaty_case2, "");
                    }
                    liczba_rabaty_case2++;

                    if(i+1< price.size()){
                        price.set(i+1,"");
                    }

                } catch (Exception e)
                {
                    break;
                }

            }
        }

        ListIterator<String> iter_price = price.listIterator();
        while(iter_price.hasNext()){
            if(iter_price.next().length()==0){
                iter_price.remove();
            }
        }

        ListIterator<String> iter_product2 = product.listIterator();
        while(iter_product2.hasNext()){
            if(iter_product2.next().length()==0){
                iter_product2.remove();
            }
        }

        product_holder = product;
        price_holder = price;
        process_btn.setEnabled(true);

    }


    private void process_result(List<String> input){
        List<String> product= new ArrayList<String>();
        List<String> price= new ArrayList<String>();

        Pattern p1;
        Matcher m1;

        for(int i = 0; i < input.size();i++){
            input.set(i, input.get(i).replaceAll("([A-Za-z][ ]+(\\d+[,.]\\d{3}))",""));

            input.set(i, input.get(i).replaceAll("[,]", "."));
            input.set(i, input.get(i).replaceAll("[.][.]", "."));
            input.set(i, input.get(i).replaceAll("[ ][.]", "."));
            input.set(i, input.get(i).replaceAll("[.][ ]", "."));
            input.set(i, input.get(i).replaceAll("[ô]","6"));
            input.set(i, input.get(i).replaceAll("[ớ]","6"));
            input.set(i, input.get(i).replaceAll("[ồ]","6"));
            input.set(i, input.get(i).replaceAll("[ố]","6"));
            input.set(i, input.get(i).replaceAll("[ổ]","6"));
            input.set(i, input.get(i).replaceAll("[ỗ]","6"));
            input.set(i, input.get(i).replaceAll("(\\d+[xX]\\d+[,.]\\d{1,5})","")); //{1,2}
            input.set(i, input.get(i).replaceAll("(\\d+[ ][xX]\\d+[,.]\\d{1,5})","")); //{1,2}
            input.set(i, input.get(i).replaceAll("(\\d+[xX][ ]\\d+[,.]\\d{1,5})","")); //{1,2}
            input.set(i, input.get(i).replaceAll("(\\d+[ ][xX][ ]\\d+[,.]\\d{1,5})","")); //{1,2}
            input.set(i, input.get(i).replaceAll("(\\d+[xX]\\d+[,.]\\d{1,5})","")); //{1,2}
            input.set(i, input.get(i).replaceAll("(\\d+[xX][ ]\\d+[,.]\\d{1,5})",""));
            input.set(i, input.get(i).replaceAll("([xX]\\d+[,.]\\d{1,5})","")); //{1,2}
            input.set(i, input.get(i).replaceAll("([xX][ ]\\d+[,.]\\d{1,5})","")); //{1,2}
            input.set(i, input.get(i).replaceAll("[0][.]$",""));
            input.set(i, input.get(i).replaceAll("[0][ ][.]$",""));
            input.set(i, input.get(i).replaceAll("[ ][ ]", " "));
            input.set(i, input.get(i).replaceAll("^[ \\t]", ""));
            input.set(i, input.get(i).trim());

            //Usunięcie błędów rozpoznania oznaczenia podatkowego przy cenie produktu
            p1 = Pattern.compile("(\\d+[,.]\\d{3}$)");
            m1 = p1.matcher(input.get(i));

            while (m1.find()) {
                //if(input.get(i).endsWith("0"))
                input.set(i, input.get(i).replaceAll("[8]$", "B"));
                input.set(i, input.get(i).replaceAll("[0]$", "D"));
                input.set(i, input.get(i).replaceAll("[6]$", "B"));
                Log.d("TAG1", m1.group());

            }
        }

        for(int i = 0; i < input.size();i++) {
            Log.d("before", input.get(i));

        }

        //Przygotuj elementy zawierające tylko spacje
        for(int i = 0; i < input.size();i++) {
            if(input.get(i).length()== 1) {
                input.set(i, " ");
            }
            if(input.get(i).trim().isEmpty()) {
                input.set(i, input.get(i).replaceAll("[ ]", ""));
            }

        }

        //Usuniecie zbednych pustych linijek
        ListIterator<String> iter_input = input.listIterator();
        while(iter_input.hasNext()){
            if(iter_input.next().length() == 0){
                iter_input.remove();
            }
        }

        for(int i = 0; i < input.size();i++) {
            Log.d("after", input.get(i));

        }

        Pattern p2;
        Matcher m2;
        Pattern p3;
        Matcher m3;

        for(int i = 0; i < input.size();i++){
            boolean found = false;

            p2 = Pattern.compile("(\\d+[,.]\\d{2}[ABCcDEFG]$)");
            m2 = p2.matcher(input.get(i));

            while(m2.find()) {
                found = true;
                if (input.get(i).matches("(\\d+[,.]\\d{2}[ABCcDEFG])$")) {
                    price.add(input.get(i).trim());
                } else if (input.get(i).matches("([-]+\\d+[,.]\\d{2}[ABCcDEFG])$")) {
                    price.add("-"+ m2.group().trim());
                    product.add(input.get(i).replaceAll("([-]+\\d+[,.]\\d{2}[ABCcDEFG]$)", "").trim());
                } else {
                    price.add(m2.group().trim());
                    product.add(input.get(i).replaceAll("(\\d+[,.]\\d{2}[ABCcDEFG]$)", "").trim());
                }
            }

            if (!found){
                p3 = Pattern.compile("(\\d+[,.]\\d{2}[ ][ABCcDEFG]$)");
                m3 = p3.matcher(input.get(i));
                while(m3.find()) {
                    found = true;
                    if(input.get(i).matches("(\\d+[,.]\\d{2}[ ][ABCcDEFG])$"))
                    {
                        price.add(input.get(i).trim());
                    } else{
                        price.add(m3.group().trim());
                        product.add(input.get(i).replaceAll("(\\d+[,.]\\d{2}[ ][ABCcDEFG]$)","").trim());
                    }

                }

            }

            if(!found){
                product.add(input.get(i));
            }
        }

        String[] oznaczenia_rabat = {
                "Rabat",
                "Opust",
                "0pust",
                "0p0st",
                "Op0st"
        };

        for(int i = 0; i < product.size();i++) {
            if(product.get(i).length() < 3) {
                product.set(i, "");
            }

            if(!product.get(i).trim().matches(".*[a-zA-Z]+.*")) {
                product.set(i, "");
            }
            if(product.get(i).trim().isEmpty()) {
                product.set(i, product.get(i).replaceAll("[ ]", ""));
            }
            if(product.get(i).trim().matches("([-]\\d+[,.]\\d{2}[ABCcDEFG])")) {
                product.set(i, "");
            }
            if(product.get(i).trim().matches("([-]\\d+[,.]\\d{2})")) {
                product.set(i, "");
            }

        }

        ListIterator<String> iter_product = product.listIterator();
        while(iter_product.hasNext()){
            if(iter_product.next().length()==0){
                iter_product.remove();
            }
        }

        ListIterator<String> iter_price = price.listIterator();
        while(iter_price.hasNext()){
            if(iter_price.next().length()==0){
                iter_price.remove();
            }
        }

        product_holder = product;
        price_holder = price;
        process_btn.setEnabled(true);

    }

    public static int LevenshteinDistance(String s0, String s1) {
        int len0 = s0.length() + 1;
        int len1 = s1.length() + 1;

        // the array of distances
        int[] cost = new int[len0];
        int[] newcost = new int[len0];

        // initial cost of skipping prefix in String s0
        for (int i = 0; i < len0; i++)
            cost[i] = i;

        // dynamicaly computing the array of distances

        // transformation cost for each letter in s1
        for (int j = 1; j < len1; j++) {

            // initial cost of skipping prefix in String s1
            newcost[0] = j - 1;

            // transformation cost for each letter in s0
            for (int i = 1; i < len0; i++) {

                // matching current letters in both strings
                int match = (s0.charAt(i - 1) == s1.charAt(j - 1)) ? 0 : 1;

                // computing cost for each transformation
                int cost_replace = cost[i - 1] + match;
                int cost_insert = cost[i] + 1;
                int cost_delete = newcost[i - 1] + 1;

                // keep minimum cost
                newcost[i] = Math.min(Math.min(cost_insert, cost_delete),
                        cost_replace);
            }

            // swap cost/newcost arrays
            int[] swap = cost;
            cost = newcost;
            newcost = swap;
        }

        // the distance is the cost for transforming all letters in both strings
        return cost[len0 - 1];
    }



}




