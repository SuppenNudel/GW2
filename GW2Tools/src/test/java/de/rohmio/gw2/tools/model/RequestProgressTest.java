package de.rohmio.gw2.tools.model;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.rohmio.gw2.tools.model.request.RequestProgress;
import de.rohmio.gw2.tools.model.request.RequestType;
import me.xhsun.guildwars2wrapper.model.v2.Recipe;

public class RequestProgressTest {

	@Test
	public void test() throws InterruptedException {
		RequestProgress<Recipe> requestProgress = new RequestProgress<>(RequestType.RECIPE);
		requestProgress.getProgress().addListener((obj, newV, oldV) -> {
			System.out.println(newV);
		});
		List<Integer> ids = requestProgress.getIds();
		System.out.println(ids);
		List<Thread> threads = new ArrayList<>();
		for(int i=1; i<=10; ++i) {
			int idx = i;
			Thread thread = new Thread(() -> {
				requestProgress.getById(idx);
				System.out.println(requestProgress.getValues());
			});
			thread.start();
			threads.add(thread);
		}
		for(Thread thread : threads) {
			thread.join();
		}
	}

}
