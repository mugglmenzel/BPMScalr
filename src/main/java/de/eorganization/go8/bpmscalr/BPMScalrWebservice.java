/**
 * 
 */
package de.eorganization.go8.bpmscalr;

import java.util.Date;
import java.util.logging.Logger;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

/**
 * @author mugglmenzel
 * 
 */
@WebService
@SOAPBinding(style = Style.RPC)
public class BPMScalrWebservice {

	@WebMethod(operationName = "benchmark")
	public long benchmark(long maxValue) {
		Logger log = Logger.getLogger(BPMScalrWebservice.class.getName());

		long start = new Date().getTime();
		for (long i = 1; i <= maxValue; i++) {
			long fact = 1;
			for (long j = 1; j <= i; j++)
				fact *= j;
		}
		long end = new Date().getTime();
		log.info("benchmarked with n=" + maxValue + " in " + (end - start)
				+ " msec.");

		return end - start;
	}

}
