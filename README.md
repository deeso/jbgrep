#jbgrep
Java Program that takes a binary string list, a memory dump, and scans the dump for those strings.

## Build instructions
0) Builds with Java 8 and Eclipse Luna and Ubuntu 15.04, all others YMMV.
1) Install gradle 2.3, 2.4 or Eclipse with gradle plugins
2) On Linux CLI, cd into the project directory and execute ```gradle clean build fatJar```, and fini
3) In Eclipse, right-click on the project, ```Grade->Tasks Quick Launcher```, type in ```fatJar```, and fini.

Overall, this utility will take a list of binary strings from a file
```
java -jar build/libs/jbgrep-all-1.0.jar -help
usage: jbgrep
 -binaryFile <file>          binary memory dump file
 -binaryStrings <file>       use given file for log
 -byFilenameOutput <file>    listing key-filename hits on a single line
 -grepOutput <file>          grepable output file
 -help                       print the help message
 -liveUpdate                 produce a live update on each hit
 -numThreads <num_threads>   number of threads for scanning
 -startOffset <offset>       start at given offset
```

Example file listing:
```
# tail jbgrep_strings.txt
75C529B8BD97DD6B25B177EE0ADE24D4
631D10989CAB515177CE2AFCD33AB09D4AA88FCFA89D2B48C42289C42598138A4CC74A5AA9FC2B72034F959269610803
6FD1358F3D2564DBF4E3958C328B5EA5A6B68E5DE20ECD9C08D451A48501E567
B4BC0B52F231FB8DC58BFF4649851703
8E3CA492F7979653318759F76F4E106B94929B1AEE6829E6376AFE8EA7805C05E9A801C93C0E77949484D92480FA5D81
EB1D2D81346364577AD9EA4221A899FFDEB35D53AB0C958FFC0BB81DEFD4AD81
A956613429733EC3265E3186E9DB258A
EC9F2208424835069EE855E9838C15E454D41A5B02BE17B5B16B4A8FFE96D40F71108A137FC47A7DD2BAFFF822B7A0D5
0888BDC0A34B9E86D951AA4E07F0C152F0046B43621D5F461BC18BA34C5A9891
```

Example usages:
Scan a binary file for strings found in *jbgrep_strings.txt* using 21 threads in *java-work.dump*.
```
# java -jar jbgrep-all-1.0.jar -binaryFile java-work.dump -numThreads 17 -binaryStrings jbgrep_strings.txt
File contains 1061 hits in 1 files and 623 unique keys
```
Scan a binary file for the string specified on the *CLI* using 21 threads in *java-work.dump*
```
# java -jar jbgrep-all-1.0.jar -binaryFile java-work.dump -numThreads 17 5257B22F18BC472CCF0C5D2C706E9E9227B3FA8AAB3DB7A2C4A7A9B44CD1EF6702DF4AC72BD9A0447571B45A0756603A
File contains 1 hits in 1 files and 1 unique keys
```

Thats fine, but I am sure you want output, right?  Well using `-liveUpdate`, `-grepOutput <file>`, and/or `-byFilenameOutput <file>` will give output in the following manner:

```
# java -jar jbgrep-all-1.0.jar -binaryFile java-work.dump -numThreads 17 -binaryStrings jbgrep_strings.txt -grepOutput /tmp/test.grep -byFilenameOutput /tmp/test.fnames

# tail /tmp/test.grep 
java-work.dump: 2f3990ac 1150103B27895827F81B694B3D3F3EE7D933C7EDA052C86E25019E17ED9F530D5B0E5A7568DE0491B3A0ACE44C3818F4
java-work.dump: 2f3980fc 1150103B27895827F81B694B3D3F3EE7D933C7EDA052C86E25019E17ED9F530D5B0E5A7568DE0491B3A0ACE44C3818F4
java-work.dump: 16147d7c 9B4D6A3705E8CA4B7A6C92FA6DCCF787D372D2EB91BECD46B868DDDF81699CC606EC759EA9B30684F51A904454066AAE
java-work.dump: 21d6f42c 313ABC0B0F6DFD6848FFBAF41BC92191EA577F35BDF58D6EBA7F44287B190C11758C3C73417B98A320D0120FFE73FE0C
java-work.dump: 184fdac4 5AF91014DC2E698C6FFBFEA70A7068A43CF52FFB59382C2A756C8EFC0B35A30D7D0A7EDC23552C1CB0B29B0FC1381470
java-work.dump: 24925174 5AF91014DC2E698C6FFBFEA70A7068A43CF52FFB59382C2A756C8EFC0B35A30D7D0A7EDC23552C1CB0B29B0FC1381470
java-work.dump: 38a8d9ec 0B49F107EEECEB00CB7D160BE891F4EB706F2B6648801855EEF00559F8FD4A64C73E01C7C05BED2EA1D897D95AA448A0
java-work.dump: 2bfd6c04 A43DE33A8158B98CDDBA974AE90BB973
java-work.dump: 2bfd697c A43DE33A8158B98CDDBA974AE90BB973

# tail /tmp/test.fnames 
A80FE1A8E70290D0894B4D8B263408877264F2834BFC21321EBFF6961622B1F191B90E82C28E768134EC8896BE88ED16 java-work.dump: "237f01c4, 19f6fb34"
479D8F40D41E626A2AB1239D422A929B104D6413B89DDF512DE79727B90A0DD9C7F2489B14AA5D1BA71087DF2F85BA14 java-work.dump: "1e540bbc"
FDE679C3C11CFB845C65206BD337972D92A14DA9FF89A7B89104116065C20D5BBA139BB7C8910CC105E304D0A0753E3C java-work.dump: "1ae3349c"
1150103B27895827F81B694B3D3F3EE7D933C7EDA052C86E25019E17ED9F530D5B0E5A7568DE0491B3A0ACE44C3818F4 java-work.dump: "2f3980fc, 2f3990ac, 2f397cc4, 2f39e284"
9B4D6A3705E8CA4B7A6C92FA6DCCF787D372D2EB91BECD46B868DDDF81699CC606EC759EA9B30684F51A904454066AAE java-work.dump: "16147d7c"
313ABC0B0F6DFD6848FFBAF41BC92191EA577F35BDF58D6EBA7F44287B190C11758C3C73417B98A320D0120FFE73FE0C java-work.dump: "21d6f42c"
5AF91014DC2E698C6FFBFEA70A7068A43CF52FFB59382C2A756C8EFC0B35A30D7D0A7EDC23552C1CB0B29B0FC1381470 java-work.dump: "24925174, 184fdac4"
0B49F107EEECEB00CB7D160BE891F4EB706F2B6648801855EEF00559F8FD4A64C73E01C7C05BED2EA1D897D95AA448A0 java-work.dump: "38a8d9ec"
A43DE33A8158B98CDDBA974AE90BB973 java-work.dump: "2bfd6c04, 2bfd697c"
5257B22F18BC472CCF0C5D2C706E9E9227B3FA8AAB3DB7A2C4A7A9B44CD1EF6702DF4AC72BD9A0447571B45A0756603A java-work.dump: "2338d09c"
```

The live update can be used to print results as they are found as follows:

```
# java -jar jbgrep-all-1.0.jar -binaryFile java-work.dump -numThreads 17 -liveUpdate 1150103B27895827F81B694B3D3F3EE7D933C7EDA052C86E25019E17ED9F530D5B0E5A7568DE0491B3A0ACE44C3818F4

1150103B27895827F81B694B3D3F3EE7D933C7EDA052C86E25019E17ED9F530D5B0E5A7568DE0491B3A0ACE44C3818F4: java-work.dump 2f397cc4
1150103B27895827F81B694B3D3F3EE7D933C7EDA052C86E25019E17ED9F530D5B0E5A7568DE0491B3A0ACE44C3818F4: java-work.dump 2f3980fc
1150103B27895827F81B694B3D3F3EE7D933C7EDA052C86E25019E17ED9F530D5B0E5A7568DE0491B3A0ACE44C3818F4: java-work.dump 2f3990ac
1150103B27895827F81B694B3D3F3EE7D933C7EDA052C86E25019E17ED9F530D5B0E5A7568DE0491B3A0ACE44C3818F4: java-work.dump 2f39e284
```
