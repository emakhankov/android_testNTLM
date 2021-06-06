package ru.emakhankov.testhttpok;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.netcosports.ntlm.NTLMAuthenticator;

import java.io.IOException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


//https://github.com/netcosports/Android_Ntlm/blob/master/sample/src/main/java/com/netcosports/ntlm/sample/MainActivity.java
//https://github.com/netcosports/Android_Ntlm/blob/master/library/src/main/java/com/netcosports/ntlm/NTLMAuthenticator.java

public class MainActivity extends AppCompatActivity {

    Button bHttp1;
    Button bHttp2;
    Button bHttp2_2;
    Button bHttp2_3;
    TextView textResult;

    EditText tLogin;
    EditText tPassword;
    EditText tDomain;

    public final String url1 = "https://www.rbc.ru";
    public final String url2 = "https://ias.vgtrk.com/WebWorkFlow/api/RTR/Person/CheckIAmUser";
    public final String url2_2 = "http://ias.vgtrk.com/WebWorkFlow/api/RTR/Person/CheckIAmUser";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bHttp1 = findViewById(R.id.button_download_any);
        bHttp2 = findViewById(R.id.button_download_ias);
        bHttp2_2 = findViewById(R.id.button_download_ias_2);
        bHttp2_3 = findViewById(R.id.button_download_ias_3);


        textResult = findViewById(R.id.text_result_download);

        tLogin = findViewById(R.id.txt_Login);
        tPassword = findViewById(R.id.txt_Password);
        tDomain = findViewById(R.id.txt_Domain);

        bHttp1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SynchronousGetRequest okHttpHandler = new SynchronousGetRequest();
                UrlData data = new UrlData(url1, "", "", "", false);
                okHttpHandler.execute(data);


            }
        });




        bHttp2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SynchronousGetRequest okHttpHandler = new SynchronousGetRequest();
                UrlData data = new UrlData(url2, tLogin.getText().toString(), tPassword.getText().toString(), tDomain.getText().toString(), false);
                okHttpHandler.execute(data);
            }
        });

        bHttp2_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SynchronousGetRequest okHttpHandler = new SynchronousGetRequest();
                UrlData data = new UrlData(url2_2, tLogin.getText().toString(), tPassword.getText().toString(), tDomain.getText().toString(), false);
                okHttpHandler.execute(data);
            }
        });

        bHttp2_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SynchronousGetRequest okHttpHandler = new SynchronousGetRequest();
                UrlData data = new UrlData(url2, tLogin.getText().toString(), tPassword.getText().toString(), tDomain.getText().toString(), true);
                okHttpHandler.execute(data);
            }
        });

    }


    public class SynchronousGetRequest extends AsyncTask<UrlData, Void, String> {

        //OkHttpClient client = new OkHttpClient();

        @Override
        protected String doInBackground(UrlData... params) {

            UrlData data = params[0];
            NTLMAuthenticator auth = new NTLMAuthenticator(data.login,data.password, data.domain);

            OkHttpClient client;
            if (data.unsafe) {
                client = getUnsafeOkHttpClient(data).build();
            } else {
                client = new OkHttpClient.Builder()
                        .authenticator(auth)
                .followRedirects(false)
                .followSslRedirects(true)
                        .build();
            }

            Request.Builder builder = new Request.Builder();
            builder.url(data.url);

            Request request = builder.build();
            try {
                Response response = client.newCall(request).execute();
                return response.body().string();
            } catch (Exception e) {
                e.printStackTrace();
                return e.toString();
            }
            //return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            textResult.setText(s);
        }
    }

    public static OkHttpClient.Builder getUnsafeOkHttpClient(UrlData data) {

        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            NTLMAuthenticator auth = new NTLMAuthenticator(data.login, data.password, data.domain);

            OkHttpClient.Builder builder = new OkHttpClient.Builder();

            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            builder.authenticator(auth);
            builder.followSslRedirects(false);
            builder.followRedirects(false);
            return builder;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}