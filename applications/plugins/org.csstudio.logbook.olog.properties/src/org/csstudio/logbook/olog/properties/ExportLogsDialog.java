package org.csstudio.logbook.olog.properties;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.csstudio.logbook.LogEntry;
import org.csstudio.logbook.LogEntryBuilder;
import org.csstudio.logbook.Logbook;
import org.csstudio.logbook.Tag;
import org.csstudio.ui.util.dialogs.StringListSelectionDialog;
import org.csstudio.ui.util.widgets.ErrorBar;
import org.csstudio.ui.util.widgets.MultipleSelectionCombo;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;
import com.google.common.base.Joiner;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;


public class ExportLogsDialog extends Dialog {

    private ErrorBar errorBar;
    private final Collection<LogEntryBuilder> data;
	private Button btnAddFields;
	private Button btnAddPath;
	private Text filePath;
	private MultipleSelectionCombo<String> fieldsText;
	private Map<String, Integer> fieldPositionMap = new HashMap<String,Integer>();
	private final List<String> fields = Arrays.asList("id", "date", "description", "owner", "logbooks", "tags", "level");
	private final String separator = "\t";
 
    protected ExportLogsDialog(Shell parentShell, Collection<LogEntryBuilder> data) {
		super(parentShell);
		setBlockOnOpen(false);
		setShellStyle(SWT.RESIZE | SWT.DIALOG_TRIM);
		this.data = data;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
		getShell().setText("Export Log Entries");
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) container.getLayout();
		gridLayout.marginWidth = 2;
		gridLayout.marginHeight = 2;
		errorBar = new ErrorBar(container, SWT.NONE);
		errorBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		container.setLayout(new GridLayout(2, false));
	    filePath =  new Text(container, SWT.BORDER);
	    filePath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	    btnAddPath = new Button(container, SWT.PUSH);
	    btnAddPath.addSelectionListener(new SelectionAdapter() {
			@Override
		    public void widgetSelected(SelectionEvent e) {
				final FileDialog dlg = new FileDialog(getShell(), SWT.SAVE);
				dlg.setFilterExtensions(new String[] {"*.csv",  "*.txt"});
				final String filename = dlg.open();
				if (filename != null) {
					filePath.setText(filename);
				}
		    }
		});
		btnAddPath.setText("Select Destination");
	    btnAddPath.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false,1, 1));
	    fieldsText = new MultipleSelectionCombo<String>(container, SWT.NONE);
	    fieldsText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		fieldsText.setItems(fields);
	    btnAddFields = new Button(container, SWT.PUSH);
		btnAddFields.addSelectionListener(new SelectionAdapter() {
		    @Override
		    public void widgetSelected(SelectionEvent e) {
			// Open a dialog which allows users to select fields
		    	StringListSelectionDialog dialog = new StringListSelectionDialog(
						getShell(), fields, fieldsText.getSelection(), "Add Fields");
				if (dialog.open() == IDialogConstants.OK_ID) {
			    	fieldsText.setSelection(Joiner.on(",").join(dialog.getSelectedValues()));
				}
		    }
		});
		
		btnAddFields.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false,1, 1));
		btnAddFields.setText("Select Fields to Export");
		return container;
    }
	
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		createButton(parent, IDialogConstants.OK_ID, "Submit", true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }
	
    @Override
    protected void okPressed() {
		Cursor originalCursor = getShell().getCursor();
		try {	
	        final BufferedWriter bw = new BufferedWriter(new FileWriter(new File(filePath.getText())));
	        try {
	        	bw.append(Joiner.on(separator).join(getHeader()));
				for (LogEntryBuilder log : data) {
					bw.newLine();
					bw.append(Joiner.on(separator).join(getLine(log.build())));	
				}
				getShell().setCursor(originalCursor);
				setReturnCode(OK);
				close();  
	        } finally {
				bw.close();
	        }
		} catch (Exception ex) {
		    getShell().setCursor(originalCursor);
		    errorBar.setException(ex);
		} 
    }  
    
    private String[] getHeader() {
    	List<String> fields = fieldsText.getSelection();
    	int i = 0;
    	List<String> header = new LinkedList<String>();
    	for (String f : fields) {
    		if (!fieldPositionMap.containsKey(f)) {
    				fieldPositionMap.put(f, i++);
    				header.add(f);
    		}
    	}
    	return header.toArray(new String[fieldPositionMap.size()]);    	
    }
    
    private String[] getLine(final LogEntry log) {
    	String[] line = new String[fieldPositionMap.size()];
    	for (String field : fieldPositionMap.keySet()) {
    		switch (field) {
    			case "id" :
    				line[fieldPositionMap.get(field)] = String.valueOf(log.getId()); 
    				 break;
    			case "owner":
    				line[fieldPositionMap.get(field)] = log.getOwner();
    				 break;
    			case "date":
    				line[fieldPositionMap.get(field)] = log.getCreateDate().toString();
    				 break;
    			case "description":
    				line[fieldPositionMap.get(field)] = log.getText().replaceAll("\n", " ").replaceAll("\t", " ");
    				 break;
    			case "logbooks":
    				StringBuilder logbooks = new StringBuilder();
    				for (Logbook logbook : log.getLogbooks()) {
    				    logbooks.append(logbook.getName() + "/");
    				}
    				
    				line[fieldPositionMap.get(field)] = logbooks.substring(0, logbooks.length() - 1);
    				 break;
    			case "tags":
    				StringBuilder tags = new StringBuilder();
    				for (Tag tag : log.getTags()) {
    				    tags.append(tag.getName() + "/");
    				}
    				line[fieldPositionMap.get(field)] = tags.length() == 0 ? "" : tags.substring(0, tags.length() - 1);
    				 break;
    			case "level":
    				line[fieldPositionMap.get(field)] = log.getLevel();    		
    				 break;
    		} 
    	}
    	return line;
    }
    
}
