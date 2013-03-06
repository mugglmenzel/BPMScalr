/**
 * 
 */
package de.eorganization.go8.bpmscalr;

import java.util.logging.Logger;

import javax.xml.ws.Endpoint;

import de.eorganization.go8.bpmscalr.BPMScalrWebservice;

/**
 * @author mugglmenzel
 * 
 */
public class BPMScalrBooter {

	private static Endpoint endpoint;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Logger log = Logger.getLogger(BPMScalrBooter.class.getName());

		String host = "localhost";
		setEndpoint(Endpoint.create(new BPMScalrWebservice()));
		int port = 8880;
		while (!getEndpoint().isPublished()) {
			try {
				getEndpoint().publish(
						"http://" + host + ":" + port
								+ "/BPMScalrWS");
			} catch (Exception e) {
				log.severe("Endpoint " + "http://" + host + ":" + port
						+ "/BPMScalrWS"
						+ " could not be published.");
			}
			port++;
		}
		log.info("Webservice registered at " + "http://" + host + ":" + --port
				+ "/BPMScalrWS");
		//new BPMScalrWebservice().benchmark(1000000);
	}

	/**
	 * @return the endpoint
	 */
	public static Endpoint getEndpoint() {
		return endpoint;
	}

	/**
	 * @param endpoint
	 *            the endpoint to set
	 */
	public static void setEndpoint(Endpoint endpoint) {
		BPMScalrBooter.endpoint = endpoint;
	}

}
