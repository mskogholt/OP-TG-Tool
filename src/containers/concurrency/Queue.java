/**
 * 
 */
package containers.concurrency;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Martin
 *
 */
public class Queue {

	private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	private final Lock readLock = readWriteLock.readLock();
	private final Lock writeLock = readWriteLock.writeLock();
	private ArrayList<String> links;

	public Queue(){
		links = new ArrayList<String>();
	}

	public void addToQueue(String link){
		writeLock.lock();
		try {
			links.add(link);
		} finally {
			writeLock.unlock();
		}
	}
	
	public String getNext(){
		writeLock.lock();
		try {
			ArrayList<String> tempLinks = new ArrayList<String>();
			String toReturn = links.get(0);

			for(int i=1; i<links.size(); i++){
				tempLinks.add(links.get(i));
			}

			links.clear();
			links.addAll(tempLinks);
			
			return toReturn;
		} finally {
			writeLock.unlock();
		}
	}

	public int size(){
		readLock.lock();
		try {
			return links.size();
		} finally {
			readLock.unlock();
		}
	}
}
