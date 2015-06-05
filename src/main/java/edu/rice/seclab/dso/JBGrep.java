package edu.rice.seclab.dso;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;

import com.google.common.primitives.UnsignedLong;



@SuppressWarnings("deprecation")
public class JBGrep {
	ArrayList<File> myTargetFiles = null;
	public static final String BINARY_FILE = "binaryFile";
	public static final String LIVE_UPDATE = "liveUpdate";
	public static final String GREP_OUTPUT = "grepOutput";
	public static final String BY_FILENAME_OUTPUT = "byFilenameOutput";
	
	private static final String START_OFFSET = "startOffset";

	public static final String NUM_THREADS = "numThreads";

	public static final String BINARY_STRING_FILE = "binaryStrings";
	public static final String HELP_ME = "help";

	
	static Option myHelpOption = new Option(HELP_ME, "print the help message" );
	static Option myLiveUpdateOption = new Option(LIVE_UPDATE, "produce a live update on each hit" );

	
	@SuppressWarnings("static-access")
	static Option myBinaryStringFileOption = OptionBuilder.withArgName( "file" )
    .hasArg()
    .withDescription(  "use given file for log" )
    .create( BINARY_STRING_FILE );
	
	@SuppressWarnings("static-access")
	static Option myNumThreadsOption = OptionBuilder.withArgName( "num_threads" )
		    .hasArg()
		    .withDescription(  "number of threads for scanning" )
		    .create( NUM_THREADS );
	
	@SuppressWarnings("static-access")
	static Option myMemDumpFileOption = OptionBuilder.withArgName( "file" )
		    .hasArg()
		    .withDescription(  "binary memory dump file" )
		    .create( BINARY_FILE );
	
	@SuppressWarnings("static-access")
	static Option myGrepableOutput = OptionBuilder.withArgName( "file" )
		    .hasArg()
		    .withDescription(  "grepable output file" )
		    .create( GREP_OUTPUT );
	
	@SuppressWarnings("static-access")
	static Option myOffsetOption = OptionBuilder.withArgName( "offset" )
		    .hasArg()
		    .withDescription(  "start at given offset" )
		    .create( START_OFFSET );
	
	@SuppressWarnings("static-access")
	static Option myByFilenameOption = OptionBuilder.withArgName( "file" )
		    .hasArg()
		    .withDescription(  "listing key-filename hits on a single line" )
		    .create( BY_FILENAME_OUTPUT);
	
	public static Options myOptions = new Options().addOption(myBinaryStringFileOption)
			.addOption(myNumThreadsOption).addOption(myOffsetOption).addOption(myMemDumpFileOption)
			.addOption(myHelpOption).addOption(myLiveUpdateOption).addOption(myGrepableOutput)
			.addOption(myByFilenameOption);
	
	// Hash Table mapping hash values to key bytes
	HashMap<Long, BinaryStringInfo> myBinaryStringInfoMap = new HashMap<Long, BinaryStringInfo>();
	HashMap<String, ArrayList<BinaryStringInfo>> msToKeys = new HashMap<String, ArrayList<BinaryStringInfo>>();

	HashMap<String, ArrayList<String>> keysToFileOffset = new HashMap<String, ArrayList<String>>();
	
	Integer myNumThreads = 1;
	Long myStartOffset;
	private String myMemDump;
	private File myBinaryStringsFile = null;
	
	ExecutorService myExecutor = null;
	
	ArrayList<Future<?>> myThreadFutures = new ArrayList<Future<?>>();
	private boolean myLiveUpdate = false;
	
	public JBGrep(List<String> binary_strings,
			String memory_dump_file, Long offset, Integer numThreads, boolean liveUpdate) {
		
		myMemDump = memory_dump_file;
		myNumThreads = numThreads;
		myStartOffset = offset;
		for (String s : binary_strings) {
			addBinaryString(s);
		}
		myExecutor = Executors.newFixedThreadPool(numThreads);
		myTargetFiles = Utils.readDirectoryFilenames(myMemDump);
		myLiveUpdate = liveUpdate;
	}
	
	public JBGrep(String binary_strings_file,
			String memory_dump_file, Long offset, Integer numThreads, boolean liveUpdate) throws FileNotFoundException {
		myBinaryStringsFile = new File(binary_strings_file);
		myMemDump = memory_dump_file;
		myNumThreads = numThreads;
		myStartOffset = offset;
		readBinaryStringsFromFile();
		myExecutor = Executors.newFixedThreadPool(numThreads);
		myTargetFiles = Utils.readDirectoryFilenames(myMemDump);
		myLiveUpdate = liveUpdate;
	}
	
	void liveUpdate(String filename, long offset, String key) {
		String offset_str = UnsignedLong.valueOf(offset).toString(16);
		System.out.println(String.format("%s %s %s", key, offset_str, filename));
	}
	
	void addBinaryString(String key) {
		try{
			BinaryStringInfo bsi = new BinaryStringInfo(key, Utils.DefaultHasher());
			synchronized (myBinaryStringInfoMap) {
				myBinaryStringInfoMap.put(bsi.getHash(), bsi);
			}			
		} catch (Exception e) {
			e.printStackTrace();			
		}	
	}
	
	
	void executeFileScans() {
		System.out.println("Starting the binary string search");
		for (File file : myTargetFiles) {
			performFileScan(file);
		}
		
		while(!myThreadFutures.isEmpty()) {
			Future<?> p = myThreadFutures.get(0);
			if (p.isDone()) {
				myThreadFutures.remove(0);
			} else {
				//System.out.println(String.format("Waiting on %d threads to complete.", myThreadFutures.size()));
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// Dont know if i care about this
					e.printStackTrace();
				}
			}
		}
		int totalHits = 0, uniqueKeys = 0;
		HashSet<String> containedFiles = new HashSet<String>();
		
		for (BinaryStringInfo bsi : myBinaryStringInfoMap.values()) {
			HashSet<String> fileHits = bsi.getFileHits();
			containedFiles.addAll(fileHits);
			int t = fileHits.size();
			if (t > 0) uniqueKeys++;
			totalHits += bsi.numLocationsHits();
			
		}
		long totalFileHits = containedFiles.size();
		System.out.println(String.format("File contains %d hits in %d files and %d unique keys", totalHits, totalFileHits, uniqueKeys));
		myExecutor.shutdown();
		while (!myExecutor.isTerminated()) {}
		
	}
	
	public String getGreppableOutput() {
		ArrayList<String> output = new ArrayList<String>();
		for (BinaryStringInfo bsi : myBinaryStringInfoMap.values()) {
			String s = bsi.toGreppableString();
			if (s.length() > 0) output.add(s);
		}
		return StringUtils.join(output, "\n")+"\n";
	}
	
	public String getByFilenameOutput() {
		ArrayList<String> output = new ArrayList<String>();
		for (BinaryStringInfo bsi : myBinaryStringInfoMap.values()) {
			String s = bsi.toByFilenameString();
			if (s.length() > 0) output.add(s);
		}
		return StringUtils.join(output, "\n")+"\n";
	}
	
	void performFileScan(File file) {
		
		if (myBinaryStringInfoMap.isEmpty() || 
			!file.exists() || 
			!file.isFile()){
			// nothing to do there there are no patterns
			// or this is not a valid file
			return;
		}
		
		long fileSize = file.length();
		long chunkSz = fileSize/myNumThreads;
		for (long offset = myStartOffset; offset < fileSize; offset += chunkSz) {
			Runnable cp = null;
			if (offset + chunkSz > fileSize) {
				cp = new ChunkProcessor(file, offset, fileSize - offset, myBinaryStringInfoMap, Utils.DefaultHasher(), myLiveUpdate);
			} else {
				cp = new ChunkProcessor(file, offset, chunkSz, myBinaryStringInfoMap, Utils.DefaultHasher(), myLiveUpdate);
			}
			
			Future<?> p = myExecutor.submit(cp);
			myThreadFutures.add(p);
				
		}
	}

	
	public static Options getOptions() {
		return myOptions;
	}
	
	void readBinaryStringsFromFile () {
		if (myBinaryStringsFile.exists()) {
			BufferedReader br;
			try {
				br = new BufferedReader(new FileReader(myBinaryStringsFile));
			    for(String line; (line = br.readLine()) != null; ) {
			        // process the line.
			    	addBinaryString(line);
			    }
			} catch (FileNotFoundException e) {
				// this should not happen unless the file is deleted when we start
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}

	}
	
	public static void main(String[] args) throws FileNotFoundException {
		JBGrep fbs = null;		
		CommandLineParser parser = new DefaultParser();
		CommandLine cli;
		String binary_strings_file = null,
			   memory_dump_file = null, 
			   num_scanning_threads = "1",
			   offset_start = "0";
		boolean liveUpdate = false;
		try {
			cli = parser.parse(JBGrep.getOptions(), args);
			binary_strings_file = cli.hasOption(BINARY_STRING_FILE) ? cli.getOptionValue(BINARY_STRING_FILE) : null;
			memory_dump_file = cli.hasOption(BINARY_FILE) ? cli.getOptionValue(BINARY_FILE) : null;
			if (cli.hasOption(NUM_THREADS)) num_scanning_threads =  cli.getOptionValue(NUM_THREADS);
			if (cli.hasOption(START_OFFSET)) offset_start = cli.getOptionValue(START_OFFSET);
			if (cli.hasOption(LIVE_UPDATE)) liveUpdate = true;

		} catch (ParseException e) {
			
			e.printStackTrace();
			return;
		}
		
		if (cli.hasOption(HELP_ME)) {
			HelpFormatter hf = new HelpFormatter();
			hf.printHelp("jbgrep", JBGrep.getOptions());
			return;
		} else if (!cli.hasOption(BINARY_FILE)) {
			System.err.println(String.format("jbgrep error: %s is required.", BINARY_FILE));
			return;
		}
		
		Integer numThreads = Utils.tryParseHexNumber(num_scanning_threads);
		Long offset = Utils.tryParseHexLongNumber(offset_start);

		if (binary_strings_file != null) {
			fbs = new JBGrep(binary_strings_file,
					memory_dump_file, offset, numThreads, liveUpdate);			
		}else if (!cli.getArgList().isEmpty()){
			fbs = new JBGrep(cli.getArgList(),
					memory_dump_file, offset, numThreads, liveUpdate);
		}
		
		if (fbs != null) {
			fbs.executeFileScans();
			if (cli.hasOption(GREP_OUTPUT)) {
				File f = new File (cli.getOptionValue(GREP_OUTPUT));
				Utils.writeOutputFile(f, fbs.getGreppableOutput());
			}
			if (cli.hasOption(BY_FILENAME_OUTPUT)) {
				File f = new File (cli.getOptionValue(BY_FILENAME_OUTPUT));
				Utils.writeOutputFile(f, fbs.getByFilenameOutput());
			}
		}
	}

	

}
