package ru.andreev_av.user.net;

import android.content.Context;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.File;

import ru.andreev_av.user.api.AmazonawsApi;

public class AvatarHttpRequestAmazonaws implements IAvatarHttpRequest {

    private static AmazonS3Client s3Client;
    private static CognitoCachingCredentialsProvider credentialsProvider;
    private static TransferUtility transferUtility;

    private Context context;

    public AvatarHttpRequestAmazonaws(Context context) {
        this.context = context;
    }

    @Override
    public void addUserAvatar(final String avatarFileName) {

        if (credentialsProvider == null) {
            credentialsProvider = new CognitoCachingCredentialsProvider(context.getApplicationContext()
                    , AmazonawsApi.COGNITO_POOL_ID, AmazonawsApi.COGNITO_POOL_REGION
            );
        }

        if (s3Client == null) {
            s3Client = new AmazonS3Client(credentialsProvider);
        }

        if (transferUtility == null) {
            transferUtility = new TransferUtility(s3Client, context.getApplicationContext());
        }

        final File avatarFile = new File(context.getCacheDir() + avatarFileName);
        // TODO 1. По хорошему следует повесить слушать observer.setTransferListener(new TransferListener())
        // и отслеживать изменения(успешно ли загрузилась картинка на сервер или возникла ошибка) (в данной задаче, думаю не критично)
        transferUtility.upload(AmazonawsApi.BUCKET_NAME, avatarFileName + ".png", avatarFile);
    }
}