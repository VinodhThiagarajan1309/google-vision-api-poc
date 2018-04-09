package com.vinodh;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.NonNull;
/**
 * Created by vthiagarajan on 4/5/18.
 */
@Data
@RequiredArgsConstructor(staticName = "of")
@NoArgsConstructor
public class ImageAndScore {

    @NonNull
    private String imgUrl;
    @NonNull
    private float score;
}
