package org.csstudio.utility.pv.simu;

import java.util.HashMap;
import java.util.Map;

import org.csstudio.utility.pv.IPVFactory;
import org.csstudio.utility.pv.PV;

/** PV Factory for simulated PVs
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class SimulatedPVFactory implements IPVFactory
{
    /** PV type prefix */
    public static final String PREFIX = "sim";

    /** All the 'simulated' PVs, mapped by name */
    private static Map<String, DynamicValue> values =
        new HashMap<String, DynamicValue>();

    /** @return Number of values */
    public static int getValueCount()
    {
        return values.size();
    }

    /** Create a 'dynamic' PV.
     *  @param name Name of the PV
     */
    public PV createPV(final String name)
    {
        DynamicValue value = values.get(name);
        if (value == null)
        {
            if (name.startsWith("sine"))
                value = new SineValue(name);
            else if (name.startsWith("ramp"))
            	value = new RampValue(name);
            else if (name.startsWith("noiseWaveform"))
            	value = new NoiseWaveformValue(name);
            else
                value = new NoiseValue(name);
            values.put(name, value);
        }else
        	value.update();
        return new SimulatedPV(PREFIX, value);
    }
}
