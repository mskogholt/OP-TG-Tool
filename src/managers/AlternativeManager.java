/**
 * 
 */
package managers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import containers.HashList;
import containers.Query;
import containers.Result;
import containers.Task;
import containers.concurrency.Queue;
import containers.concurrency.RandomGenerator;
import graphicalinterfaces.InputPanel;
import graphicalinterfaces.MessagePanel;
import io.Writer;
import models.TerminationLimit;
import workers.CompleteWorker;
import workers.links.LinkWorker;
import workers.links.LinkWorkerFactory;

/**
 * @author martin.skogholt
 *
 */
public class AlternativeManager implements Callable<Boolean>{

	private InputPanel input;
	private MessagePanel messenger;
	private ArrayList<String> proxies;
	private ArrayList<Query> queries;
	public static final int maxAttempts = 10;
	private int threads = 4;
	private String id;
	private BufferedWriter writer;

	/**
	 * @param input
	 * @param messenger
	 */
	public AlternativeManager(InputPanel input, MessagePanel messenger, ArrayList<Query> queries, String id, BufferedWriter writer) {
		this.input = input;
		this.messenger = messenger;
		this.proxies = fillProxies();
		this.queries = queries;
		this.id = id;
		this.writer = writer;
		this.threads = input.getThreads();
	}

	@Override
	public Boolean call() throws Exception {

		RandomGenerator gen = new RandomGenerator(proxies.size());

		HashMap<String, Query> queryMapper = new HashMap<String, Query>(); 
		ArrayList<String> links = getLinks(queries, input.getSite(), proxies, gen, input.getTerminationLimit(), queryMapper);

		messenger.setMessage(links.size()+"\t"+"links collected");
		messenger.setProgress(0);

		HashList<Result> results =  getResults(links, input.getSite(), proxies, gen, input.getTerminationLimit(), queryMapper);

		messenger.setProgress(100);
		messenger.setStatus("Collected a total of " + results.size() + " leads. Writing results...");

		if(input.isSeparateFiles()) {
			Writer.savePerQuery(results, input.getSaveLocation());
		} else {
			Writer.saveResults(results, "Run " + id, input.getSaveLocation());
		}

		return true;
	}

	public HashList<Result> getResults(ArrayList<String> links, String site, ArrayList<String> proxies, 
			RandomGenerator gen, TerminationLimit termination, HashMap<String, Query> queryMapper) throws Exception {

		long start = System.currentTimeMillis();
		ExecutorService executor = Executors.newFixedThreadPool(threads);
		ArrayList<Task<HashList<Result>>> tasks = new ArrayList<Task<HashList<Result>>>();

		Queue queue = new Queue();
		messenger.setStatus("Scheduling workers...");
		for(String link : links) {
			CompleteWorker worker = new CompleteWorker(link, proxies, gen, queue, messenger);
			Future<HashList<Result>> future = executor.submit(worker);

			Task<HashList<Result>> task = new Task<HashList<Result>>(future, link);
			tasks.add(task);
		}		

		ArrayList<Task<HashList<Result>>> left = new ArrayList<Task<HashList<Result>>>();
		int size = tasks.size();
		int counter = 0;

		HashList<Result> finalResults = new HashList<Result>();

		while(tasks.size()>0) {
			for(Task<HashList<Result>> task : tasks) {

				if(task.isDone()) {

					try {
						HashList<Result> results = task.get();
						if(queryMapper.containsKey(task.getLink())) {
							for(int i=0; i<results.size(); i++){
								Result result = results.get(i);
								result.setWhat(queryMapper.get(task.getLink()).getWhat());
								result.setWhere(queryMapper.get(task.getLink()).getWhere());
							}
						} else {
							System.out.println("Link is er niet!");
						}

						for(int i=0; i<results.size();i++){
							finalResults.add(results.get(i));
						}

						if(writer!=null) {
							for(int i=0; i<results.size();i++){
								writer.write(results.get(i).serialize());
								writer.newLine();
							}
						}

						counter++;

					} catch (Exception e) {
						if(task.getAttempts()<maxAttempts) {
							CompleteWorker worker = new CompleteWorker(task.getLink(), proxies, gen, queue, messenger);
							Future<HashList<Result>> future = executor.submit(worker);

							Task<HashList<Result>> newTask = new Task<HashList<Result>>(future, task.getLink());
							newTask.setAttempts((task.getAttempts()+1));
							left.add(newTask);
						}
						size++;
						System.err.println(e);
					}
					messenger.setStatus("Collected Results for link: " + counter + " out of " + size);
					double prog = ((double) counter)/((double) size);
					prog = prog *100;
					messenger.setProgress((int) prog);
				}else {
					left.add(task);
				}
			}

			while(queue.size()>0){
				String queueLink = queue.getNext();
				CompleteWorker worker = new CompleteWorker(queueLink, proxies, gen, queue, messenger);
				Future<HashList<Result>> future = executor.submit(worker);
				Task<HashList<Result>> newTask = new Task<HashList<Result>>(future, queueLink);

				left.add(newTask);
			}

			tasks.clear();
			tasks.addAll(left);
			left.clear();

			long duration = System.currentTimeMillis()-start;
			double mins = duration/1000;
			mins = mins/60;
			if (termination.getType().equals("Time Limit")) {
				if(termination.terminate((int) mins)) {
					executor.shutdown();
					messenger.setStatus("Shutting down, time limit is reached.");
				}
			}
		}

		if(writer!=null) {
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		executor.shutdown();
		while(!executor.isTerminated()) {
			Thread.sleep(100);
		}
		return finalResults;
	}

	public ArrayList<String> getLinks(ArrayList<Query> queries, String site, ArrayList<String> proxies, 
			RandomGenerator gen, TerminationLimit termination, HashMap<String, Query> queryMapper){

		ExecutorService executor = Executors.newFixedThreadPool(threads);
		ArrayList<Task<ArrayList<String>>> tasks = new ArrayList<Task<ArrayList<String>>>();

		messenger.setStatus("Collecting queries...");
		for(Query query : queries) {
			LinkWorker worker = LinkWorkerFactory.getLinkWorker(query, site, proxies, gen, termination, messenger, true);
			Future<ArrayList<String>> future = executor.submit(worker);

			Task<ArrayList<String>> task = new Task<ArrayList<String>>(future, query);
			tasks.add(task);
		}

		ArrayList<Task<ArrayList<String>>> left = new ArrayList<Task<ArrayList<String>>>();
		int size = tasks.size();
		int counter = 0;

		HashSet<String> links = new HashSet<String>();

		while(tasks.size()>0) {
			for(Task<ArrayList<String>> task : tasks) {
				if(task.isDone()) {
					try {

						ArrayList<String> results = task.get();
						for(String link : results) {
							queryMapper.put(link, task.getQuery());
						}
						links.addAll(results);
						counter++;

					} catch (Exception e) {

						if(task.getAttempts()<maxAttempts) {
							LinkWorker worker = LinkWorkerFactory.getLinkWorker(task.getQuery(), site, proxies, gen, termination, messenger, true);
							Future<ArrayList<String>> future = executor.submit(worker);

							Task<ArrayList<String>> newTask = new Task<ArrayList<String>>(future, task.getQuery());
							newTask.setAttempts((task.getAttempts()+1));
							left.add(newTask);
						}

						size++;
						System.err.println(e);
					}
					messenger.setStatus("Collected Links for query: " + counter + " out of " + size);
					double prog = ((double) counter)/((double) size);
					prog = prog *100;
					messenger.setProgress((int) prog);
				}else {
					left.add(task);
				}
			}

			tasks.clear();
			tasks.addAll(left);
			left.clear();
		}

		ArrayList<String> listLinks = new ArrayList<String>();
		listLinks.addAll(links);
		executor.shutdown();
		while(!executor.isTerminated()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return listLinks;
	}

	private ArrayList<String> fillProxies() {
		ArrayList<String> links = new ArrayList<String>();
		for(int l=0; l<10; l++){
			links.add("http://fishproxy.com/"); // One
		}	
		for(int l=0; l<10; l++){
			links.add("http://xitenow.com/"); // One
		}
		for(int l=0; l<10; l++){
			links.add("http://webanonymizer.org/");
		}
		for(int k=0; k<10; k++){
			links.add("https://www.proxfree.com/");
		}
		for(int i=0; i<10; i++){
			links.add("https://www.filterbypass.me/"); // 1
		}
		for(int i=0; i<10; i++){
			links.add("http://www.unblockmyweb.com/"); // 1
		}
		for(int i=0; i<25; i++){
			links.add("https://www.proxfree.com/"); // 1
		}
		for(int i=0; i<10; i++){
			links.add("http://newproxy.ninja/"); // 1
		}
		for(int i=0; i<10; i++){
			links.add("http://www.theproxy.link/"); // 1
		}
		for(int i=0; i<10; i++){
			links.add("http://www.hollandproxy.eu/"); // 1
		}
		for(int i=0; i<10; i++){
			links.add("http://www.shinyproxy.eu/"); // 1
		}
		for(int i=0; i<10; i++){
			links.add("http://applepieproxy.xyz"); // 1
		}
		for(int i=0; i<10; i++){
			links.add("http://xtcsoul.net/"); // 1
		}
		for(int i=0; i<10; i++){
			links.add("http://www.bramka-proxy.pl/"); // 1
		}
		return links;
	}
}
