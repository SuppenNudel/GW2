package de.rohmio.gw2.tools.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import de.rohmio.gw2.tools.model.RequestProgress.RequestType;
import me.xhsun.guildwars2wrapper.GuildWars2;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class Util {

	public static File getImage(String url) throws IOException {
		String fileName = String.format("data/img/%s", new File(url).getName());
		final File file = new File(fileName);
		if (!file.exists()) {
			Request request = new Request.Builder().url(url).build();
			OkHttpClient client = ClientFactory.getClient();
			okhttp3.Response response = client.newCall(request).execute();
			file.getParentFile().mkdirs();
			file.createNewFile();
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			fileOutputStream.write(response.body().bytes());
			fileOutputStream.close();
		}
		return file;
	}

	private static File getFilePath(RequestType type, int id) {
		File file = null;
		switch (type) {
		case ITEM:
			file = new File(String.format("data/cache/%s/%s/%d.json", type.getPath(), GuildWars2.getLanguage().getValue(), id));
			break;
		case RECIPE:
			file = new File(String.format("data/cache/%s/%d.json", type.getPath(), id));
			break;
		default:
			break;
		}
		return file;
	}

	public static <T> T getCache(RequestType type, int id, Type clazz) {
		Gson gson = new Gson();
		File file = getFilePath(type, id);
		T object = null;
		if (file.exists()) {
			try {
				object = gson.fromJson(new FileReader(file), clazz);
			} catch (JsonSyntaxException e) {
				e.printStackTrace();
			} catch (JsonIOException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		return object;
	}

	public static void writeCache(RequestType type, int id, Object object) {
		Gson gson = new Gson();
		File file = getFilePath(type, id);
		file.getParentFile().mkdirs();
		try {
			file.createNewFile();
			String json = gson.toJson(object);
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write(json);
			fileWriter.close();
		} catch (JsonIOException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static List<int[]> chunkUp(int chunkSize, int[] array) {
		List<int[]> list = new ArrayList<>();
		for (int i = 0; i < array.length; i += chunkSize) {
			int[] chunkArray = Arrays.copyOfRange(array, i, Math.min(array.length, i + chunkSize));
			list.add(chunkArray);
		}
		return list;
	}

}
