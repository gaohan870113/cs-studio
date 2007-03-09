/**
 * 
 */
package org.epics.css.dal.epics.demo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.epics.css.dal.DoubleProperty;
import org.epics.css.dal.DynamicValueAdapter;
import org.epics.css.dal.DynamicValueEvent;
import org.epics.css.dal.DynamicValueListener;
import org.epics.css.dal.epics.EPICSApplicationContext;
import org.epics.css.dal.epics.EPICSPlug;
import org.epics.css.dal.simulation.PropertyProxyImpl;
import org.epics.css.dal.simulation.RandomNoiseValueProvider;
import org.epics.css.dal.simulation.SimulatorPlug;
import org.epics.css.dal.spi.DefaultPropertyFactoryService;
import org.epics.css.dal.spi.LinkPolicy;
import org.epics.css.dal.spi.PropertyFactory;

/**
 * @author ikriznar
 *
 */
public class CSum implements Runnable {

	PropertyFactory factoryA;
	PropertyFactory factoryB;
	EPICSApplicationContext ctx;
	DoubleProperty propA;
	DoubleProperty propB;
	BufferedReader in;
	SimpleDateFormat f= new SimpleDateFormat("yyyy-mm-dd'T'HH:mm:ss.SSS");
	
	double valA=0.0;
	double valB=0.0;
	
	long timeA;
	long timeB;
	long correlation=500;
	
	DynamicValueListener<Double,DoubleProperty> listenerA= new DynamicValueAdapter<Double,DoubleProperty>() {
	
		@Override
		public void valueChanged(DynamicValueEvent<Double,DoubleProperty> event) {
			timeA=System.currentTimeMillis();
			valA=event.getValue();
			//out("Update from A: "+f.format(new Date(timeA))+" "+valA);
			doSum();
		}
		@Override
		public void valueUpdated(DynamicValueEvent<Double,DoubleProperty> event) {
			timeA=System.currentTimeMillis();
			valA=event.getValue();
			//out("Update from A: "+f.format(new Date(timeA))+" "+valA);
			doSum();
		}
	
	};
	DynamicValueListener<Double,DoubleProperty> listenerB= new DynamicValueAdapter<Double,DoubleProperty>() {
		
		@Override
		public void valueChanged(DynamicValueEvent<Double,DoubleProperty> event) {
			timeB=System.currentTimeMillis();
			valB=event.getValue();
			//out("Update from B: "+f.format(new Date(timeB))+" "+valB);
			doSum();
		}
		@Override
		public void valueUpdated(DynamicValueEvent<Double,DoubleProperty> event) {
			timeB=System.currentTimeMillis();
			valB=event.getValue();
			//out("Update from B: "+f.format(new Date(timeB))+" "+valB);
			doSum();
		}
	
	};
	

	public static void main(String[] args) {
		CSum ex= new CSum();
		ex.run();
		System.exit(0);
	}
	
	/**
	 * 
	 */
	public CSum() {
		super();
		in= new BufferedReader(new InputStreamReader(System.in),128);
	}
	
	private void doSum() {
		//System.out.println("DIFF "+(Math.abs(timeA-timeB)));
		if (Math.abs(timeA-timeB)>correlation) {
			return;
		}
		
		out("Corelated event: "+f.format(new Date((timeA+timeB)/2)));
		out("channel A:   "+valA);
		out("channel B:   "+valB);
		out("channel A+B: "+(valA+valB));
	}
	
	private void out(String s) {
		System.out.print("< ");
		System.out.println(s);
	}
	
	private String read() {
		try {
			return in.readLine();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		ctx= new EPICSApplicationContext("ChannelExplorer");
		factoryA= DefaultPropertyFactoryService.getPropertyFactoryService().getPropertyFactory(ctx,LinkPolicy.SYNC_LINK_POLICY,SimulatorPlug.PLUG_TYPE);
		factoryB= DefaultPropertyFactoryService.getPropertyFactoryService().getPropertyFactory(ctx,LinkPolicy.SYNC_LINK_POLICY,EPICSPlug.PLUG_TYPE);
		
		
		out("This is simple demo which sums two channls from two independent protocols underneath DAL.");
		out("So far we can use Simulator and EPICS.");
		out("Channel A: put in name for Simulator channel (any name will be accepted):");
		
		String nameA= read();
		
		out("Channel B: put in name for EPICS double channel (must be running):");
		
		String nameB= read();
		
		out("Correlation event window in milliseconds (default 500):");
		
		String time= read();

		try {
			if (time!=null && time.length()>0) {
				correlation=Long.parseLong(time);
			}
			
			propA= factoryA.getProperty(nameA,DoubleProperty.class,null);
			propB= factoryB.getProperty(nameB,DoubleProperty.class,null);

			// a little trick to make simulation more interesting.
			PropertyProxyImpl<Double> sim= (PropertyProxyImpl<Double>)SimulatorPlug.getInstance().getPropertyProxy(nameA);
			sim.setValueSync(10.0);
			sim.setValueProvider(new RandomNoiseValueProvider<Double>(10.0,0.5));
			SimulatorPlug.getInstance().releaseProxy(sim);

			
			
			out("Channels are connected, type <enter> key to exit.");

			Thread.sleep(1000);

			propA.addDynamicValueListener(listenerA);
			propB.addDynamicValueListener(listenerB);
			
			read();
			
			propA.removeDynamicValueListener(listenerA);
			propB.removeDynamicValueListener(listenerB);
			
			out("Closed.");
			
		} catch (Exception e) {
			out("Failed.");
			e.printStackTrace();
		}

	}
	
}
