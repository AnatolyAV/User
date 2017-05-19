package ru.andreev_av.user.api;

import com.amazonaws.regions.Regions;

public interface AmazonawsApi {

    String BASE_URL = "https://s3-us-west-2.amazonaws.com";
    String BUCKET_NAME = "binaryblitzaavbucketwest";
    String URL_WITH_BUCKET = BASE_URL + "/" + BUCKET_NAME + "/";
    String COGNITO_POOL_ID = "us-west-2:83ec8c07-c79f-4052-a277-88178a2549b8";
    Regions COGNITO_POOL_REGION = Regions.US_WEST_2;
}