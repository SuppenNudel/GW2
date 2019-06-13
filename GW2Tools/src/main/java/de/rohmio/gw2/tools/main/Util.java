package de.rohmio.gw2.tools.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import de.rohmio.gw2.tools.model.RequestType;
import me.xhsun.guildwars2wrapper.GuildWars2;
import me.xhsun.guildwars2wrapper.model.identifiable.IdentifiableInt;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class Util {

	/**
	 * location where all app data is stored
	 */
	public static File DOCS = new File(System.getProperty("user.home") + "/AppData/Roaming/GW2 Tools");

	private static Gson gson = new Gson();

	public static File getImage(String url) throws IOException {
		String fileName = String.format("data/img/%s", new File(url).getName());
		final File file = new File(DOCS, fileName);
		if (!file.exists()) {
			Request request = new Request.Builder().url(url).build();
			OkHttpClient client = ClientFactory.getClient();
			okhttp3.Response response = client.newCall(request).execute();
			FileUtils.writeByteArrayToFile(file, response.body().bytes());
		}
		return file;
	}

	public static File getFilePath(RequestType type) {
		String fileName = null;
		switch (type) {
		case ITEM:
			fileName = String.format("data/cache/%s_%s.json", type.getPath(), GuildWars2.getLanguage().getValue());
			break;
		case RECIPE:
			fileName = String.format("data/cache/%s.json", type.getPath());
			break;
		default:
			break;
		}
		return new File(DOCS, fileName);
	}

	public static File getFilePath(RequestType type, int id) {
		String fileName = null;
		switch (type) {
		case ITEM:
			fileName = String.format("data/cache/%s/%s/%d.json", type.getPath(), GuildWars2.getLanguage().getValue(), id);
			break;
		case RECIPE:
			fileName = String.format("data/cache/%s/%s.json", type.getPath(), id);
			break;
		default:
			break;
		}
		return new File(DOCS, fileName);
	}

	public static <T> T getCache(RequestType type, int id) {
		File file = getFilePath(type, id);
		return readFile(file, type.getClazz());
	}

	public static <T> T getCache(RequestType type) {
		File file = getFilePath(type);
		T readFile = readFile(file, type.getArrClazz());
		return readFile;
	}

	public static <T> T readFile(File file, Type clazz) {
		T object = null;
		if (file.exists()) {
			try {
				String json = FileUtils.readFileToString(file, "UTF-8");
				object = gson.fromJson(json, clazz);
			} catch (JsonSyntaxException e) {
				e.printStackTrace();
			} catch (JsonIOException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return object;
	}

	public static void writeCache(RequestType type, List<? extends IdentifiableInt> object) throws Exception {
		//		if(!object.get(0).getClass().isAssignableFrom(type.getClazz())) {
		//			throw new Exception(object.getClass()+" not assignable from "+type.getClazz());
		//		}
		File file = getFilePath(type);
		System.out.println("Writing cache "+file);
		writeFile(file, object);
	}

	public static void writeCache(RequestType type, IdentifiableInt object) throws Exception {
		if(!object.getClass().isAssignableFrom(type.getClazz())) {
			throw new Exception(object.getClass()+" not assignable from "+type.getClazz());
		}
		File file = getFilePath(type, object.getId());
		System.out.println("Writing cache "+file);
		writeFile(file, object);
	}

	public static void writeFile(File file, Object object) {
		try {
			String json = gson.toJson(object);
			FileUtils.writeStringToFile(file, json, "UTF-8");
		} catch (JsonIOException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static List<int[]> chunkUp(int chunkSize, List<Integer> integers) {
		int[] allIdArray = integers.stream().mapToInt(i -> i).toArray();
		return chunkUp(chunkSize, allIdArray);
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
