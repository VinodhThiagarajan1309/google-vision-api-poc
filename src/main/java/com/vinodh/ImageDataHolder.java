package com.vinodh;

import java.util.List;

import lombok.Data;

/**
 * Created by vthiagarajan on 4/5/18.
 */
@Data
public class ImageDataHolder {

    private long hostId;

    private long propertyId;

    private String imageUrl;

    private List<ImageAndScore> matchingImageUrls;

    private List<ImageAndScore> partiallyMatchingImageUrls;

    private List<ImageAndScore> fullyMatchingImageUrls;

    private List<ImageAndScore> visuallySimilarImageUrls;


}
