package com.vinodh;

// Imports the Google Cloud client library

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageSource;
import com.google.cloud.vision.v1.WebDetection;
import com.google.protobuf.ByteString;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class QuickstartSample {
    public static void main(String... args) throws Exception {
        detectWebDetectionsGcs("/Users/vthiagarajan/Downloads/IMG_0371.JPG");
    }

    public static void detectWebDetectionsGcs(String filePath) throws Exception,
        IOException {
        List<AnnotateImageRequest> requests = new ArrayList();

        ByteString imgBytes = ByteString.readFrom(new FileInputStream(filePath));

        ImageSource imgSource = ImageSource.newBuilder().setImageUri("https://odis.homeaway.com/odis/listing/3ff2f148-7dcc-4a54-a5d4-29ec8f1c2f85.c10.jpg").build();

       // Image img = Image.newBuilder().setContent(imgBytes).build();
        Image img = Image.newBuilder().setSource(imgSource).build();

        Feature feat = Feature.newBuilder().setType(Type.WEB_DETECTION).build();
        AnnotateImageRequest request =
            AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
        requests.add(request);

        try  {
            ImageAnnotatorClient client = ImageAnnotatorClient.create();
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();

            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    System.out.println("Error: %s\n" +  res.getError().getMessage());
                    return;
                }

                // Search the web for usages of the image. You could use these signals later
                // for user input moderation or linking external references.
                // For a full list of available annotations, see http://g.co/cloud/vision/docs
                WebDetection annotation = res.getWebDetection();
                System.out.println("Entity:Id:Score");
                System.out.println("===============");
                for (WebDetection.WebEntity entity : annotation.getWebEntitiesList()) {
                    System.out.println(entity.getDescription() + " : " + entity.getEntityId() + " : "
                        + entity.getScore());
                }
                System.out.println("\nPages with matching images: Score\n==");
                for (WebDetection.WebPage page : annotation.getPagesWithMatchingImagesList()) {
                    System.out.println(page.getUrl() + " : " + page.getScore());
                }
                System.out.println("\nPages with partially matching images: Score\n==");
                for (WebDetection.WebImage  image : annotation.getPartialMatchingImagesList()) {
                    System.out.println(image.getUrl() + " : " + image.getScore());
                }
                System.out.println("\nPages with fully matching images: Score\n==");
                for (WebDetection.WebImage image : annotation.getFullMatchingImagesList()) {
                    System.out.println(image.getUrl() + " : " + image.getScore());
                }
            }
        } catch ( Exception e) {

        }
    }
}