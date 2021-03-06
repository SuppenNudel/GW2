package de.rohmio.gw2.tools.main;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;

public class ClientFactory {
	
	public static OkHttpClient getClient(ProxySettings proxySettings) {
		if(proxySettings == null) {
			return new OkHttpClient();
		}
		OkHttpClient client;
		if(proxySettings.getHost() == null) {
			client = new OkHttpClient();
		} else {
			Authenticator proxyAuthenticator = (route, response) -> {
				String credential = Credentials.basic(proxySettings.getUser(), proxySettings.getPassword());
				return response.request().newBuilder()
						.header("Proxy-Authorization", credential)
						.build();
			};
			
			client = new OkHttpClient.Builder()
					.connectTimeout(60, TimeUnit.SECONDS)
					.writeTimeout(60, TimeUnit.SECONDS)
					.readTimeout(60, TimeUnit.SECONDS)
					.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxySettings.getHost(), proxySettings.getPort())))
					.proxyAuthenticator(proxyAuthenticator)
					.sslSocketFactory(trustAllSslSocketFactory, (X509TrustManager)trustAllCerts[0])
					.build();
		}
		return client;
	}
	
	private static final TrustManager[] trustAllCerts = new TrustManager[] {
		    new X509TrustManager() {
		        @Override
		        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
		        }

		        @Override
		        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
		        }

		        @Override
		        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		          return new java.security.cert.X509Certificate[]{};
		        }
		    }
		};
	
	private static final SSLContext trustAllSslContext;
	static {
	    try {
	        trustAllSslContext = SSLContext.getInstance("SSL");
	        trustAllSslContext.init(null, trustAllCerts, new java.security.SecureRandom());
	    } catch (NoSuchAlgorithmException | KeyManagementException e) {
	        throw new RuntimeException(e);
	    }
	}
	private static final SSLSocketFactory trustAllSslSocketFactory = trustAllSslContext.getSocketFactory();

}
