package org.csstudio.utility.channelfinder;

import gov.bnl.channelfinder.api.Channel;
import gov.bnl.channelfinder.api.ChannelFinderClient;
import gov.bnl.channelfinder.api.ChannelFinderException;

import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class ChannelQuery {

	// private final Executor executor;
	private final ChannelFinderClient client;
	private final String query;
	private final Executor queryExecutor;

	private AtomicReference<Exception> lastException = new AtomicReference<Exception>();	

	private volatile Collection<Channel> lastValidResult;
	private volatile Collection<Channel> result;

	private List<ChannelQueryListener> listeners = new CopyOnWriteArrayList<ChannelQueryListener>();

	public static class Builder {
		private String query = null;
		private ChannelFinderClient client = null;
		private Executor queryExecutor = Executors.newSingleThreadExecutor();

		private Builder(String query) {
			this.query = query;
		}

		public static Builder query(String query) {
			return new Builder(query);
		}

		public Builder using(ChannelFinderClient client) {
			this.client = client;
			return this;
		}

		public Builder on(Executor executor) {
			this.queryExecutor = executor;
			return this;
		}

		public ChannelQuery create() {
			if (this.query == null)
				throw new IllegalArgumentException(
						"query string cannot be null");
			if (this.client == null)
				this.client = CFClientManager.getClient();
			return new ChannelQuery(this.query, this.client, this.queryExecutor);
		}
	}

	private ChannelQuery(String query, ChannelFinderClient client, Executor queryExecutor) {
		super();
		this.query = query;
		this.client = client;
		this.queryExecutor = queryExecutor;
	}

	public void addChannelQueryListener(ChannelQueryListener listener) {
		this.listeners.add(listener);
	}

	public void removeChannelQueryListener(ChannelQueryListener listener) {
		this.listeners.remove(listener);
	}

	private void fireGetQueryResult() {
		for (ChannelQueryListener listener : this.listeners) {
			listener.getQueryResult();
		}
	}

	public Collection<Channel> getResult() {
		return this.result;
	}

	public Collection<Channel> getLastValidResult() {
		return this.lastValidResult;
	}

	public Exception getLastException() {
		return this.lastException.getAndSet(null);
	}

	public void execute() {
		queryExecutor.execute(new Runnable() {

			@Override
			public void run() {
				try {
					result = client.find(buildSearchMap(query));
					lastValidResult = result;
				} catch (ChannelFinderException e) {
					result = null;
					lastException.set(e);
				} catch (Exception e) {
					result = null;
					lastException.set(e);
				} finally {
					fireGetQueryResult();
				}
			}
		});
	}

	static Map<String, String> buildSearchMap(String searchPattern) {
		// FIXME handle space in comma separated values Tags=tag1, tag2
		Hashtable<String, String> map = new Hashtable<String, String>();
		String[] words = searchPattern.split("\\s");
		if (words.length <= 0) {
			// ERROR
		}
		for (int index = 0; index < words.length; index++) {
			if (!words[index].contains("=")) {
				// this is a name value
				map.put("~name", words[index]);
			} else {
				// this is a property or tag
				String key = words[index].split("=")[0];
				String values = words[index].split("=")[1];
				if (key.equalsIgnoreCase("Tags")) {
					map.put("~tag", values.replace("||", ","));
					// for (int i = 0; i < values.length; i++)
					// map.put("~tag", values[i]);
				} else {
					map.put(key, values.replace("||", ","));
				}
			}
		}
		return map;
	}

}
