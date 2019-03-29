package de.rohmio.gw2.tools.main;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;

public class ClientFactory {

	public static OkHttpClient getClient() {
		Authenticator proxyAuthenticator = (route, response) -> {
			String credential = Credentials.basic(ProxyCredentials.username, ProxyCredentials.password);
			return response.request().newBuilder().header("Proxy-Authorization", credential).build();
		};

		OkHttpClient client = new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS)
				.writeTimeout(60, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS)
				.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ProxyCredentials.proxyHost, ProxyCredentials.proxyPort)))
				.proxyAuthenticator(proxyAuthenticator).build();

		return client;
	}

}
