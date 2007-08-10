/**
 * 
 */
package org.csstudio.platform.ui.dnd.rfc;

import org.csstudio.platform.model.rfc.IProcessVariableAdress;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.TransferData;

public class ProcessVariableAdressDropTargetAdapter extends DropTargetAdapter {
	private IProcessVariableAdressReceiver _receiver;

	ProcessVariableAdressDropTargetAdapter(IProcessVariableAdressReceiver pvCallback) {
		_receiver = pvCallback;
	}

	@Override
	public void drop(final DropTargetEvent event) {
		IProcessVariableAdress[] pvs = new IProcessVariableAdress[0];

		if (PVTransfer.getInstance().isSupportedType(event.currentDataType)) {
			// get the layer that was moved
			pvs = (IProcessVariableAdress[]) PVTransfer.getInstance()
					.nativeToJava(event.currentDataType);
		} else if (TextTransfer.getInstance().isSupportedType(
				event.currentDataType)) {
			String rawName = (String) TextTransfer.getInstance()
					.nativeToJava(event.currentDataType);

			IProcessVariableAdress pv = ProcessVariableExchangeUtil.checkRawInput(rawName);
			
			pvs = new IProcessVariableAdress[] { pv };
		}

		if (pvs.length > 0) {
			_receiver.receive(pvs, event);
		}
	}

	@Override
	public void dropAccept(final DropTargetEvent event) {
		if (!isSupportedType(event)) {
			event.detail = DND.DROP_NONE;
		} else {
			event.detail = DND.DROP_COPY;
		}
	}

	@Override
	public void dragEnter(final DropTargetEvent event) {
		if (!isSupportedType(event)) {
			event.detail = DND.DROP_NONE;
		} else {
			event.detail = DND.DROP_COPY;
		}
	}

	@Override
	public void dragOver(final DropTargetEvent event) {
		if (!isSupportedType(event)) {
			event.detail = DND.DROP_NONE;
		} else {
			event.detail = DND.DROP_COPY;
		}
	}

	private boolean isSupportedType(DropTargetEvent event) {
		boolean supported = (PVTransfer.getInstance().isSupportedType(
				event.currentDataType) || TextTransfer.getInstance()
				.isSupportedType(event.currentDataType));
		
		return supported;
	}
}