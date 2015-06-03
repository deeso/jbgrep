package edu.rice.seclab.dso;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

@SuppressWarnings("deprecation")
public class FindBinaryStrings {
	public static final String BINARY_FILE = "memoryDumpFile";

	private static final String START_OFFSET = "startOffset";

	public static final String NUM_THREADS = "numThreads";

	public static final String BINARY_STRING_FILE = "binaryStringFile";

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
		    .withDescription(  "binary memory dump file" ).isRequired(true)
		    .create( BINARY_FILE );
	
	@SuppressWarnings("static-access")
	static Option myOffsetOption = OptionBuilder.withArgName( "offset" )
		    .hasArg()
		    .withDescription(  "start at given offset" )
		    .create( START_OFFSET );
	
	public static Options myOptions = new Options().addOption(myBinaryStringFileOption)
			.addOption(myNumThreadsOption).addOption(myOffsetOption).addOption(myMemDumpFileOption);
	
	// Hash Table mapping hash values to key bytes
	HashMap<Long, BinaryStringInfo> hashToKeys = new HashMap<Long, BinaryStringInfo>();
	HashMap<String, ArrayList<BinaryStringInfo>> msToKeys = new HashMap<String, ArrayList<BinaryStringInfo>>();

	HashMap<String, ArrayList<String>> keysToFileOffset = new HashMap<String, ArrayList<String>>();
	
	Integer myNumThreads = 1;
	Long myOffsetStart;
	private String myMemDump;
	private String myBinaryStringsFile;
	private boolean myUsingFile;
	
	public FindBinaryStrings(List<String> binary_strings,
			String memory_dump_file, Long offset, Integer numThreads) {
		
		myBinaryStringsFile =  null;
		myMemDump = memory_dump_file;
		myNumThreads = numThreads;
		myOffsetStart = offset;
		myUsingFile = false;
		for (String s : binary_strings) {
			addBinaryString(s);
		}
	}
	
	public FindBinaryStrings(String binary_strings_file,
			String memory_dump_file, Long offset, Integer numThreads) {
		myBinaryStringsFile = binary_strings_file;
		myMemDump = memory_dump_file;
		myNumThreads = numThreads;
		myOffsetStart = offset;
		myUsingFile = true;
		readStrings();
	}

	void addBinaryString(String key) {
		try{
			BinaryStringInfo bsi = new BinaryStringInfo(key);
			synchronized (hashToKeys) {
				hashToKeys.put(bsi.getHash(), bsi);
			}			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		}
		
	}

	public static Options getOptions() {
		return myOptions;
	}

	
	void readStrings () {
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(myBinaryStringsFile));
		    for(String line; (line = br.readLine()) != null; ) {
		        // process the line.
		    	addBinaryString(line);
		    }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		FindBinaryStrings fbs = null;		
		CommandLineParser parser = new DefaultParser();
		CommandLine cli;
		String binary_strings_file = null,
			   memory_dump_file = null, 
			   num_scanning_threads = "1",
			   offset_start = "0";
		try {
			cli = parser.parse(FindBinaryStrings.getOptions(), args);
			binary_strings_file = cli.hasOption(BINARY_STRING_FILE) ? cli.getOptionValue(BINARY_STRING_FILE) : null;
			memory_dump_file = cli.hasOption(BINARY_FILE) ? cli.getOptionValue(BINARY_FILE) : null;
			if (cli.hasOption(NUM_THREADS)) num_scanning_threads =  cli.getOptionValue(NUM_THREADS);
			if (cli.hasOption(START_OFFSET)) offset_start = cli.getOptionValue(START_OFFSET);

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		
		Integer numThreads = Utils.tryParseHexNumber(num_scanning_threads);
		Long offset = Utils.tryParseHexLongNumber(offset_start);

		if (binary_strings_file != null) {
			fbs = new FindBinaryStrings(binary_strings_file,
					memory_dump_file, offset, numThreads);			
		}else if (!cli.getArgList().isEmpty()){
			fbs = new FindBinaryStrings(cli.getArgList(),
					memory_dump_file, offset, numThreads);
		}
		
		if (fbs == null) {
			
		}
	}

}
