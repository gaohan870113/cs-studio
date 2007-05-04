package org.csstudio.util.time.swt;

import java.util.ArrayList;

import org.csstudio.util.time.RelativeTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

/** Widget for displaying and selecting a relative date and time.
 *  @author Kay Kasemir
 */
public class RelativeTimeWidget extends Composite
{
    /** Widgets for date pieces. */
    private Spinner year, month, day;
    /** Widgets for time pieces. */
    private Spinner hour, minute, second;
    
    /** The relative time pieces for year, month, day, hour, minute, second. */
    private RelativeTime relative_time;
    
    /** Used to prevent recursion when the widget updates the GUI,
     *  which in turn fires listener notifications...
     */
    private boolean in_GUI_update = false;
    
    private ArrayList<RelativeTimeWidgetListener> listeners
       = new ArrayList<RelativeTimeWidgetListener>();

    /** Construct widget, initialized to zero offsets.
     *  @param parent Widget parent.
     *  @param flags SWT widget flags.
     */
    public RelativeTimeWidget(Composite parent, int flags)
    {
        this(parent, flags, new RelativeTime());
    }
        
    /** Construct widget, initialized to given time.
     *  @param parent Widget parent.
     *  @param flags SWT widget flags.
     */
    public RelativeTimeWidget(Composite parent, int flags, RelativeTime relative_time)
    {
        super(parent, flags);
        GridLayout layout = new GridLayout();
        layout.numColumns = 6;
        setLayout(layout);
        GridData gd;
        
        // Date: (year)+- / (month)+-  / (day)+-
        // Time: (hour)+- : (minute)+- : (second)+-
        //                                    [now]

        // New row
        Label l = new Label(this, SWT.NONE);
        l.setText(Messages.Time_Date);
        gd = new GridData();
        l.setLayoutData(gd);

        year = new Spinner(this, SWT.BORDER | SWT.WRAP);
        year.setToolTipText(Messages.Time_SelectYear);
        gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = SWT.FILL;
        year.setLayoutData(gd);
        year.setMinimum(-19);
        year.setMaximum(+10);
        year.setIncrement(1);
        year.setPageIncrement(5);
        l = new Label(this, SWT.NONE);
        l.setText(Messages.Date_Sep);
        gd = new GridData();
        l.setLayoutData(gd);

        month = new Spinner(this, SWT.BORDER | SWT.WRAP);
        month.setToolTipText(Messages.Time_SelectMonth);
        gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = SWT.FILL;
        month.setLayoutData(gd);
        month.setMinimum(-12);
        month.setMaximum(+12);
        month.setIncrement(1);
        month.setPageIncrement(3);
        l = new Label(this, SWT.NONE);
        l.setText(Messages.Date_Sep);
        gd = new GridData();
        l.setLayoutData(gd);
        
        day = new Spinner(this, SWT.BORDER | SWT.WRAP);
        day.setToolTipText(Messages.Time_SelectDay);
        gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = SWT.FILL;
        day.setLayoutData(gd);
        day.setMinimum(-31);
        day.setMaximum(+31);
        day.setIncrement(1);
        day.setPageIncrement(10);
        
        // New row
        l = new Label(this, SWT.NONE);
        l.setText(Messages.Time_Time);
        gd = new GridData();
        l.setLayoutData(gd);

        hour = new Spinner(this, SWT.BORDER | SWT.WRAP);
        hour.setToolTipText(Messages.Time_SelectHour);
        gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = SWT.FILL;
        hour.setLayoutData(gd);
        hour.setMinimum(-23);
        hour.setMaximum(+23);
        hour.setIncrement(1);
        hour.setPageIncrement(6);
        l = new Label(this, SWT.NONE);
        l.setText(Messages.Time_Sep);
        gd = new GridData();
        l.setLayoutData(gd);

        minute = new Spinner(this, SWT.BORDER | SWT.WRAP);
        minute.setToolTipText(Messages.Time_SelectMinute);
        gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = SWT.FILL;
        minute.setLayoutData(gd);
        minute.setMinimum(-59);
        minute.setMaximum(+59);
        minute.setIncrement(1);
        minute.setPageIncrement(10);

        l = new Label(this, SWT.NONE);
        l.setText(Messages.Time_Sep);
        gd = new GridData();
        l.setLayoutData(gd);

        second = new Spinner(this, SWT.BORDER | SWT.WRAP);
        second.setToolTipText(Messages.Time_SelectSeconds);
        gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = SWT.FILL;
        second.setLayoutData(gd);
        second.setMinimum(0);
        second.setMaximum(59);
        second.setIncrement(1);
        second.setPageIncrement(10);
        
        // New row        
        Button now = new Button(this, SWT.PUSH);
        now.setText(Messages.Time_Now);
        now.setToolTipText(Messages.Time_Now_TT);
        gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalSpan = layout.numColumns - 1;
        gd.horizontalAlignment = SWT.RIGHT;
        now.setLayoutData(gd);
        
        // Initialize to given relative time pieces
        setRelativeTime(relative_time);
        
        now.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                if (!in_GUI_update)
                {
                    year.setSelection(0);
                    month.setSelection(0);
                    day.setSelection(0);
                    hour.setSelection(0);
                    minute.setSelection(0);
                    second.setSelection(0);
                    // TODO: Write 'now' to <whereever>
                    updateDataFromGUI();
                }
            }
        });
        SelectionAdapter update = new SelectionAdapter() 
        {
            public void widgetSelected(SelectionEvent e)
            {
                if (!in_GUI_update)
                    updateDataFromGUI();
            }
        };
        year.addSelectionListener(update);
        month.addSelectionListener(update);
        day.addSelectionListener(update);
        hour.addSelectionListener(update);
        minute.addSelectionListener(update);
        second.addSelectionListener(update);
    }
    
    /** Add given listener. */
    public void addListener(RelativeTimeWidgetListener listener)
    {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }
    
    /** Remove given listener. */
    public void removeListener(RelativeTimeWidgetListener listener)
    {
        listeners.remove(listener);
    }
    
    /** Set the widget to display the given time.
     *  @see #setNow()
     */
    public void setRelativeTime(RelativeTime relative_time)
    {
        this.relative_time = relative_time;
        updateGUIfromData();
    }

    /** @return Returns the currently selected time. */
    public RelativeTime getRelativeTime()
    {
        return (RelativeTime) relative_time.clone();
    }
    
    /** Update the data from the interactive GUI elements. */
    private void updateDataFromGUI()
    {
        relative_time.set(RelativeTime.YEARS, year.getSelection());
        relative_time.set(RelativeTime.MONTHS, month.getSelection());
        relative_time.set(RelativeTime.DAYS, day.getSelection());
        relative_time.set(RelativeTime.HOURS, hour.getSelection());
        relative_time.set(RelativeTime.MINUTES, minute.getSelection());
        relative_time.set(RelativeTime.SECONDS, second.getSelection());
        updateGUIfromData();
    }

    /** Display the current value of the data on the GUI. */
    private void updateGUIfromData()
    {
        in_GUI_update = true;
        year.setSelection(relative_time.get(RelativeTime.YEARS));
        month.setSelection(relative_time.get(RelativeTime.MONTHS));
        day.setSelection(relative_time.get(RelativeTime.DAYS));
        hour.setSelection(relative_time.get(RelativeTime.HOURS));
        minute.setSelection(relative_time.get(RelativeTime.MINUTES));
        second.setSelection(relative_time.get(RelativeTime.SECONDS));
        in_GUI_update = false;
        // fireUpdatedTimestamp
        for (RelativeTimeWidgetListener l : listeners)
            l.updatedTime(this, relative_time);
    }
}
