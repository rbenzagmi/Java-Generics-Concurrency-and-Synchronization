package bgu.spl.mics.application;


import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.passiveObjects.Diary;
import bgu.spl.mics.application.services.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * This is the Main class of the application. You should parse the input file,
 * create the different components of the application, and run the system.
 * In the end, you should output a JSON.
 */
public class Main {

	//set the gson
	public static JsonClass setGson(String input) throws IOException {
		Gson g = new Gson();
		Reader reader = new FileReader(input);
		JsonClass gAccess = g.fromJson(reader, JsonClass.class);
		return gAccess;
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		JsonClass gsonAccess = setGson(args[0]);
		Future<CountDownLatch> cdl_in_a_box = new Future<>();
		Diary d = Diary.getInstance();

		//list of all the microservices
		List<MicroService> ms = new LinkedList<>();
		ms.add(new C3POMicroservice(cdl_in_a_box, gsonAccess.Ewoks));
		ms.add(new HanSoloMicroservice(cdl_in_a_box));
		ms.add(new R2D2Microservice(cdl_in_a_box, gsonAccess.R2D2));
		ms.add(new LandoMicroservice(cdl_in_a_box, gsonAccess.Lando));
		ms.add(new LeiaMicroservice(cdl_in_a_box, gsonAccess.attacks));

		cdl_in_a_box.resolve(new CountDownLatch(ms.size() - 1));

		//list of threads to each microservice
		List<Thread> threads = new LinkedList<>();
		for (MicroService m : ms)
			threads.add(new Thread(m));

		//start the threads and join (waiting until they finish)
		for (Thread thread : threads)
			thread.start();
		for (Thread thread : threads)
			thread.join();

		//create the output gson file
		Gson gWriter = new GsonBuilder().setPrettyPrinting().create();
		FileWriter writer = new FileWriter(args[1]);
		gWriter.toJson(d, writer);
		writer.flush();
		writer.close();
	}
}
