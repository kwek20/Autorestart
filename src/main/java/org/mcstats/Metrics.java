// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Metrics.java

package org.mcstats;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.zip.GZIPOutputStream;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.scheduler.BukkitTask;

public class Metrics {
	public static class Graph {

		public String getName() {
			return name;
		}

		public void addPlotter(Plotter plotter) {
			plotters.add(plotter);
		}

		public void removePlotter(Plotter plotter) {
			plotters.remove(plotter);
		}

		public Set<Plotter> getPlotters() {
			return Collections.unmodifiableSet(plotters);
		}

		public int hashCode() {
			return name.hashCode();
		}

		public boolean equals(Object object) {
			if (!(object instanceof Graph)) {
				return false;
			} else {
				Graph graph = (Graph) object;
				return graph.name.equals(name);
			}
		}

		protected void onOptOut() {
		}

		private final String name;
		private final Set<Plotter> plotters;

		private Graph(String name) {
			plotters = new LinkedHashSet<Plotter>();
			this.name = name;
		}

		Graph(String s, Graph graph) {
			this(s);
		}
	}

	public static abstract class Plotter {

		public abstract int getValue();

		public String getColumnName() {
			return name;
		}

		public void reset() {
		}

		public int hashCode() {
			return getColumnName().hashCode();
		}

		public boolean equals(Object object) {
			if (!(object instanceof Plotter))
				return false;
			Plotter plotter = (Plotter) object;
			return plotter.name.equals(name) && plotter.getValue() == getValue();
		}

		private final String name;

		public Plotter() {
			this("Default");
		}

		public Plotter(String name) {
			this.name = name;
		}
	}

	public Metrics(Plugin plugin) throws IOException {
		task = null;
		if (plugin == null)
			throw new IllegalArgumentException("Plugin cannot be null");
		this.plugin = plugin;
		configurationFile = getConfigFile();
		configuration = YamlConfiguration.loadConfiguration(configurationFile);
		configuration.addDefault("opt-out", Boolean.valueOf(false));
		configuration.addDefault("guid", UUID.randomUUID().toString());
		configuration.addDefault("debug", Boolean.valueOf(false));
		if (configuration.get("guid", null) == null) {
			configuration.options().header("http://mcstats.org").copyDefaults(true);
			configuration.save(configurationFile);
		}
		guid = configuration.getString("guid");
		debug = configuration.getBoolean("debug", false);
	}

	public Graph createGraph(String name) {
		if (name == null) {
			throw new IllegalArgumentException("Graph name cannot be null");
		} else {
			Graph graph = new Graph(name, null);
			graphs.add(graph);
			return graph;
		}
	}

	public void addGraph(Graph graph) {
		if (graph == null) {
			throw new IllegalArgumentException("Graph cannot be null");
		} else {
			graphs.add(graph);
			return;
		}
	}

	public boolean start(){
		label0:
        {
            synchronized(optOutLock)
            {
                if(!isOptOut())
                    break label0;
            }
            return false;
        }
	
        if(task == null){
	        task = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
	        	private boolean firstPost = true;
	        	
	        	public void run(){
	                try {
	                    synchronized(optOutLock){
	                        if(isOptOut() && task != null){
	                            task.cancel();
	                            task = null;
	                            Graph graph;
	                            for(Iterator<Graph> iterator = graphs.iterator(); iterator.hasNext(); graph.onOptOut()){
	                                graph = (Graph)iterator.next();
	                            }
	                        }
	                    }
	                    postPlugin(!firstPost);
	                    firstPost = false;
	                } catch(IOException e) {
	                    if(debug){
	                        Bukkit.getLogger().log(Level.INFO, (new StringBuilder("[Metrics] ")).append(e.getMessage()).toString());
	                    }
	                }
	            }
	        }, 0L, 18000L);
        }
        return true;
    }

	public boolean isOptOut(){
        try {
        	configuration.load(getConfigFile());
        } catch (IOException | InvalidConfigurationException ex){
        	if(debug)
                Bukkit.getLogger().log(Level.INFO, (new StringBuilder("[Metrics] ")).append(ex.getMessage()).toString());
            return true;
        }
        
        return configuration.getBoolean("opt-out", false);
	}

	public void enable() throws IOException {
		synchronized (optOutLock) {
			if (isOptOut()) {
				configuration.set("opt-out", Boolean.valueOf(false));
				configuration.save(configurationFile);
			}
			if (task == null)
				start();
		}
	}

	public void disable() throws IOException {
		synchronized (optOutLock) {
			if (!isOptOut()) {
				configuration.set("opt-out", Boolean.valueOf(true));
				configuration.save(configurationFile);
			}
			if (task != null) {
				task.cancel();
				task = null;
			}
		}
	}

	public File getConfigFile() {
		File pluginsFolder = plugin.getDataFolder().getParentFile();
		return new File(new File(pluginsFolder, "PluginMetrics"), "config.yml");
	}

	private void postPlugin(boolean isPing) throws IOException {
		PluginDescriptionFile description = plugin.getDescription();
		String pluginName = description.getName();
		boolean onlineMode = Bukkit.getServer().getOnlineMode();
		String pluginVersion = description.getVersion();
		String serverVersion = Bukkit.getVersion();
		int playersOnline = Bukkit.getServer().getOnlinePlayers().size();
		StringBuilder json = new StringBuilder(1024);
		json.append('{');
		appendJSONPair(json, "guid", guid);
		appendJSONPair(json, "plugin_version", pluginVersion);
		appendJSONPair(json, "server_version", serverVersion);
		appendJSONPair(json, "players_online", Integer.toString(playersOnline));
		String osname = System.getProperty("os.name");
		String osarch = System.getProperty("os.arch");
		String osversion = System.getProperty("os.version");
		String java_version = System.getProperty("java.version");
		int coreCount = Runtime.getRuntime().availableProcessors();
		if (osarch.equals("amd64"))
			osarch = "x86_64";
		appendJSONPair(json, "osname", osname);
		appendJSONPair(json, "osarch", osarch);
		appendJSONPair(json, "osversion", osversion);
		appendJSONPair(json, "cores", Integer.toString(coreCount));
		appendJSONPair(json, "auth_mode", onlineMode ? "1" : "0");
		appendJSONPair(json, "java_version", java_version);
		if (isPing)
			appendJSONPair(json, "ping", "1");
		if (graphs.size() > 0)
			synchronized (graphs) {
				json.append(',');
				json.append('"');
				json.append("graphs");
				json.append('"');
				json.append(':');
				json.append('{');
				boolean firstGraph = true;
				for (Iterator<Graph> iter = graphs.iterator(); iter.hasNext();) {
					Graph graph = (Graph) iter.next();
					StringBuilder graphJson = new StringBuilder();
					graphJson.append('{');
					Plotter plotter;
					for (Iterator<Plotter> iterator = graph.getPlotters().iterator(); iterator.hasNext(); appendJSONPair(graphJson, plotter.getColumnName(), Integer.toString(plotter.getValue())))
						plotter = (Plotter) iterator.next();

					graphJson.append('}');
					if (!firstGraph)
						json.append(',');
					json.append(escapeJSON(graph.getName()));
					json.append(':');
					json.append(graphJson);
					firstGraph = false;
				}

				json.append('}');
			}
		json.append('}');
		URL url = new URL((new StringBuilder("http://report.mcstats.org")).append(String.format("/plugin/%s", new Object[] { urlEncode(pluginName) })).toString());
		URLConnection connection;
		if (isMineshafterPresent())
			connection = url.openConnection(Proxy.NO_PROXY);
		else
			connection = url.openConnection();
		byte uncompressed[] = json.toString().getBytes();
		byte compressed[] = gzip(json.toString());
		connection.addRequestProperty("User-Agent", "MCStats/7");
		connection.addRequestProperty("Content-Type", "application/json");
		connection.addRequestProperty("Content-Encoding", "gzip");
		connection.addRequestProperty("Content-Length", Integer.toString(compressed.length));
		connection.addRequestProperty("Accept", "application/json");
		connection.addRequestProperty("Connection", "close");
		connection.setDoOutput(true);
		if (debug)
			System.out.println((new StringBuilder("[Metrics] Prepared request for ")).append(pluginName).append(" uncompressed=").append(uncompressed.length).append(" compressed=")
					.append(compressed.length).toString());
		OutputStream os = connection.getOutputStream();
		os.write(compressed);
		os.flush();
		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String response = reader.readLine();
		os.close();
		reader.close();
		if (response == null || response.startsWith("ERR") || response.startsWith("7")) {
			if (response == null)
				response = "null";
			else if (response.startsWith("7"))
				response = response.substring(response.startsWith("7,") ? 2 : 1);
			throw new IOException(response);
		}
		if (response.equals("1") || response.contains("This is your first update this hour"))
			synchronized (graphs) {
				for (Iterator<Graph> iter = graphs.iterator(); iter.hasNext();) {
					Graph graph = (Graph) iter.next();
					Plotter plotter;
					for (Iterator<Plotter> iterator1 = graph.getPlotters().iterator(); iterator1.hasNext(); plotter.reset())
						plotter = (Plotter) iterator1.next();

				}

			}
	}

	public static byte[] gzip(String input) {
        ByteArrayOutputStream baos;
        GZIPOutputStream gzos;
        baos = new ByteArrayOutputStream();
        gzos = null;
        
        try {
            gzos = new GZIPOutputStream(baos);
            gzos.write(input.getBytes("UTF-8"));
            
        } catch(IOException e) {
            e.printStackTrace();
        }
        
        if(gzos != null){
            try {
                gzos.close();
            } catch(IOException ioexception) { }
        }
        
        return baos.toByteArray();
    }

	private boolean isMineshafterPresent() {
		try {
			Class.forName("mineshafter.MineServer");
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private static void appendJSONPair(StringBuilder json, String key, String value) throws UnsupportedEncodingException {
		boolean isValueNumeric = false;
		try {
			if (value.equals("0") || !value.endsWith("0")) {
				Double.parseDouble(value);
				isValueNumeric = true;
			}
		} catch (NumberFormatException e) {
			isValueNumeric = false;
		}
		if (json.charAt(json.length() - 1) != '{')
			json.append(',');
		json.append(escapeJSON(key));
		json.append(':');
		if (isValueNumeric)
			json.append(value);
		else
			json.append(escapeJSON(value));
	}

	private static String escapeJSON(String text) {
		StringBuilder builder = new StringBuilder();
		builder.append('"');
		for (int index = 0; index < text.length(); index++) {
			char chr = text.charAt(index);
			switch (chr) {
			case 34: // '"'
			case 92: // '\\'
				builder.append('\\');
				builder.append(chr);
				break;

			case 8: // '\b'
				builder.append("\\b");
				break;

			case 9: // '\t'
				builder.append("\\t");
				break;

			case 10: // '\n'
				builder.append("\\n");
				break;

			case 13: // '\r'
				builder.append("\\r");
				break;

			default:
				if (chr < ' ') {
					String t = (new StringBuilder("000")).append(Integer.toHexString(chr)).toString();
					builder.append((new StringBuilder("\\u")).append(t.substring(t.length() - 4)).toString());
				} else {
					builder.append(chr);
				}
				break;
			}
		}

		builder.append('"');
		return builder.toString();
	}

	private static String urlEncode(String text) throws UnsupportedEncodingException {
		return URLEncoder.encode(text, "UTF-8");
	}

	private static final int REVISION = 7;
	private static final String BASE_URL = "http://report.mcstats.org";
	private static final String REPORT_URL = "/plugin/%s";
	private static final int PING_INTERVAL = 15;
	
	private final Plugin plugin;
	private final Set<Graph> graphs = Collections.synchronizedSet(new HashSet<Graph>());
	private final YamlConfiguration configuration;
	private final File configurationFile;
	private final String guid;
	private final boolean debug;
	private final Object optOutLock = new Object();
	private volatile BukkitTask task;

}
