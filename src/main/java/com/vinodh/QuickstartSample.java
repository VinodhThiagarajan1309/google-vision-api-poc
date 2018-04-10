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
import com.google.gson.Gson;
import com.google.protobuf.ByteString;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class QuickstartSample {
    public static void main(String... args) throws Exception {

        try {

            File f = new File("./src/main/resources/part-00000.txt");

            System.out.println("Reading files using Apache IO:");

            List<String> lines = FileUtils.readLines(f, "UTF-8");
int iCount = 0 ;
            QuickstartSample obj = new QuickstartSample();
            for (String line : lines) {
                //System.out.println(line);
                obj.detectWebDetectionsGcs(line.split("\\,"));
                System.out.println(iCount++);
            }
            File f2 = new File("./src/main/resources/output.txt");
            FileUtils.writeStringToFile(f2, "VINODH TOOK THE BLOW" +"\n", true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public  void detectWebDetectionsGcs(String[] record) throws Exception,
        IOException {

        // Parse incoming requests
        long hostId = Long.valueOf(record[0].trim().toString());
        long property_id = Long.valueOf(record[1].trim().toString());
        String filePath = record[2].trim().toString();

        // Build Image from URI
        ImageSource imgSource = ImageSource.newBuilder().setImageUri(filePath).build();
        Image img = Image.newBuilder().setSource(imgSource).build();

        // Build Google Vision API request
        Feature feat = Feature.newBuilder().setType(Type.WEB_DETECTION).build();
        AnnotateImageRequest request =
            AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();

        // Collect batch of requests
        List<AnnotateImageRequest> requests = new ArrayList();
        requests.add(request);

        try  {
            // Create client
            ImageAnnotatorClient client = ImageAnnotatorClient.create();

            // Submit Requests
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);

            // Collect Responses
            List<AnnotateImageResponse> responses = response.getResponsesList();

            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    File f = new File("./src/main/resources/error.txt");
                    FileUtils.writeStringToFile(f, hostId + " " + property_id + " " + filePath + "\n", true);
                    System.out.println(res.getError().getMessage());
                    return;
                }

                // Prep Java Collectors
                ImageDataHolder imageDataHolder = new ImageDataHolder();
                imageDataHolder.setPropertyId(property_id);
                imageDataHolder.setHostId(hostId);
                imageDataHolder.setImageUrl(filePath);
                WebDetection annotation = res.getWebDetection();
                List<ImageAndScore> matchList = new ArrayList<ImageAndScore>();
                List<ImageAndScore> partialMatchList = new ArrayList<ImageAndScore>();
                List<ImageAndScore> fullMatchList = new ArrayList<ImageAndScore>();
                List<ImageAndScore> visMatchList = new ArrayList<ImageAndScore>();

                // Parse response and set into Java objects
                ImageAndScore imageAndScore;
                for (WebDetection.WebPage page : annotation.getPagesWithMatchingImagesList()) {
                     imageAndScore = ImageAndScore.of(page.getUrl() , page.getScore());
                    matchList.add(imageAndScore);
                }
                for (WebDetection.WebImage  image : annotation.getPartialMatchingImagesList()) {
                     imageAndScore = ImageAndScore.of(image.getUrl() , image.getScore());
                    partialMatchList.add(imageAndScore);
                }
                for (WebDetection.WebImage image : annotation.getFullMatchingImagesList()) {
                     imageAndScore = ImageAndScore.of(image.getUrl() , image.getScore());
                    fullMatchList.add(imageAndScore);
                }
                for (WebDetection.WebImage image : annotation.getVisuallySimilarImagesList()) {
                     imageAndScore = ImageAndScore.of(image.getUrl(), image.getScore());
                    visMatchList.add(imageAndScore);
                }

                imageDataHolder.setMatchingImageUrls(matchList);
                imageDataHolder.setPartiallyMatchingImageUrls(partialMatchList);
                imageDataHolder.setFullyMatchingImageUrls(fullMatchList);
                imageDataHolder.setVisuallySimilarImageUrls(visMatchList);

                // Convert Object to String
                Gson gson = new Gson();
                String jsonInString = gson.toJson(imageDataHolder);

                // Write the deserialized object into output
                File f = new File("./src/main/resources/output.txt");
                FileUtils.writeStringToFile(f, jsonInString+"\n", true);
            }
        } catch ( Exception e) {
            System.out.println("Google Try Catch");
            e.printStackTrace();

        }

    }

    public List<ImageAndScore> trySeparateMethod(List<WebDetection.WebImage> lst) {
        List<ImageAndScore> visMatchList = new ArrayList<ImageAndScore>();
        //System.out.println("\nPages with fully Visually Similar Images: Score\n==");
        for (WebDetection.WebImage image : lst) {
            ImageAndScore imageAndScore = ImageAndScore.of(image.getUrl(), image.getScore());
            //imageAndScore = null;
            // System.out.println(image.getUrl() + " : " + image.getScore());
            visMatchList.add(imageAndScore);
            imageAndScore =null;
        }
        return visMatchList;
    }

    public static void detectWebDetectionsGcsFromDownloadedImage(String[] record, int iCount) throws Exception,
        IOException {
        try {
            List<ImageDataHolder> imageDataHolders = new ArrayList();
            long hostId = Long.valueOf(record[0].trim().toString());
            long property_id = Long.valueOf(record[1].trim().toString());
            String filePath = record[2].trim().toString();
            List<AnnotateImageRequest> requests = new ArrayList();


            URL url = new URL(filePath);
            File localFilePath = new File("./src/main/resources/downloaded/" + iCount + ".jpg");
            FileUtils.copyURLToFile(url, localFilePath);

            ByteString imgBytes = ByteString.readFrom(new FileInputStream(localFilePath));

            //ImageSource imgSource = ImageSource.newBuilder().setImageUri(filePath).build();

            Image img = Image.newBuilder().setContent(imgBytes).build();
            //Image img = Image.newBuilder().setSource(imgSource).build();

            Feature feat = Feature.newBuilder().setType(Type.WEB_DETECTION).build();
            AnnotateImageRequest request =
                AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
            requests.add(request);

            try {
                ImageAnnotatorClient client = ImageAnnotatorClient.create();
                BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
                List<AnnotateImageResponse> responses = response.getResponsesList();

                for (AnnotateImageResponse res : responses) {
                    if (res.hasError()) {
                        File f1 = new File("/Users/vthiagarajan/Documents/WorkSpaces/HomeawayMavenProjects/google-vision-api-poc/src/main/resources/error.txt");
                        FileUtils.writeStringToFile(f1, hostId + " " + property_id + " " + filePath + "\n", true);
                        System.out.println(res.getError().getMessage());
                        return;
                    }

                    ImageDataHolder imageDataHolder = new ImageDataHolder();
                    imageDataHolder.setPropertyId(property_id);
                    imageDataHolder.setHostId(hostId);
                    imageDataHolder.setImageUrl(filePath);
                    // Search the web for usages of the image. You could use these signals later
                    // for user input moderation or linking external references.
                    // For a full list of available annotations, see http://g.co/cloud/vision/docs
                    WebDetection annotation = res.getWebDetection();
                    //System.out.println("Entity:Id:Score");
                    //System.out.println("===============");
                    List<ImageAndScore> matchList = new ArrayList<ImageAndScore>();
                    List<ImageAndScore> partialMatchList = new ArrayList<ImageAndScore>();
                    List<ImageAndScore> fullMatchList = new ArrayList<ImageAndScore>();
                    List<ImageAndScore> visMatchList = new ArrayList<ImageAndScore>();
                /*for (WebDetection.WebEntity entity : annotation.getWebEntitiesList()) {
                    System.out.println(entity.getDescription() + " : " + entity.getEntityId() + " : "
                        + entity.getScore());
                }*/

                    ///  System.out.println("\nPages with matching images: Score\n==");
                    for (WebDetection.WebPage page : annotation.getPagesWithMatchingImagesList()) {
                        ImageAndScore imageAndScore = ImageAndScore.of(page.getUrl(), page.getScore());
                        // System.out.println(page.getUrl() + " : " + page.getScore());
                        matchList.add(imageAndScore);
                    }
                    // System.out.println("\nPages with partially matching images: Score\n==");
                    for (WebDetection.WebImage image : annotation.getPartialMatchingImagesList()) {
                        ImageAndScore imageAndScore = ImageAndScore.of(image.getUrl(), image.getScore());
                        // System.out.println(image.getUrl() + " : " + image.getScore());
                        partialMatchList.add(imageAndScore);
                    }
                    // System.out.println("\nPages with fully matching images: Score\n==");
                    for (WebDetection.WebImage image : annotation.getFullMatchingImagesList()) {
                        ImageAndScore imageAndScore = ImageAndScore.of(image.getUrl(), image.getScore());
                        // System.out.println(image.getUrl() + " : " + image.getScore());
                        fullMatchList.add(imageAndScore);
                    }

                    //System.out.println("\nPages with fully Visually Similar Images: Score\n==");
                    for (WebDetection.WebImage image : annotation.getVisuallySimilarImagesList()) {
                        ImageAndScore imageAndScore = ImageAndScore.of(image.getUrl(), image.getScore());
                        // System.out.println(image.getUrl() + " : " + image.getScore());
                        visMatchList.add(imageAndScore);
                    }

                    imageDataHolder.setMatchingImageUrls(matchList);
                    imageDataHolder.setPartiallyMatchingImageUrls(partialMatchList);
                    imageDataHolder.setFullyMatchingImageUrls(fullMatchList);
                    imageDataHolder.setVisuallySimilarImageUrls(visMatchList);

                    Gson gson = new Gson();
              /*  Staff obj = new Staff();

                // 1. Java object to JSON, and save into a file
                gson.toJson(obj, new FileWriter("D:\\file.json"));
*/
                    // 2. Java object to JSON, and assign to a String
                    String jsonInString = gson.toJson(imageDataHolder);

                    // System.out.println(jsonInString);
                    File f = new File("/Users/vthiagarajan/Documents/WorkSpaces/HomeawayMavenProjects/google-vision-api-poc/src/main/resources/output.txt");
                    FileUtils.writeStringToFile(f, jsonInString + "\n", true);

                    imageDataHolders.add(imageDataHolder);
                }
            } catch (Exception e) {
                System.out.println("Google Try Catch");
                e.printStackTrace();

            }
        } catch (Exception e) {
            System.out.println(record.toString());
            e.printStackTrace();
        }

    }
}