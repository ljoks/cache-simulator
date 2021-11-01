import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

class sim_cache {

	static final int LRU = 0;	

	public static void main(String[] args) {
		// instantiate command line arguments as ints
		int blocksize, L1_size, L1_assoc, L2_size, L2_assoc;
		int replacement_policy, inclusion_property;

		// get arguments from cmdline
		blocksize = Integer.parseInt(args[0]);
		L1_size = Integer.parseInt(args[1]);
		L1_assoc = Integer.parseInt(args[2]);
		L2_size = Integer.parseInt(args[3]);
		L2_assoc = Integer.parseInt(args[4]);
		replacement_policy = Integer.parseInt(args[5]);
		inclusion_property = Integer.parseInt(args[6]);
	

		Cache L2 = null;
		// L2_size greater than 0 indicates there is an L2 cache
		if(L2_size > 0) {
			L2 = new Cache(L2_size, L2_assoc, blocksize, replacement_policy, null);
		}

		Cache L1 = new Cache(L1_size, L1_assoc, blocksize, replacement_policy, L2);

		// read input file line by line
		try {
			File trace = new File(args[7]);
			Scanner scan = new Scanner(trace);	

			// if replacement policy is optimal, we first preprocess addresses
			if(replacement_policy == 2) {
				while(scan.hasNextLine()) {

					String instr = scan.nextLine();
					String[] tokens = instr.split(" ");
	
					// convert hex to decimal
					Long dec = Long.decode("0x" + tokens[1]);

					int index = (int) (dec / blocksize) % L1.sets.length;
					Long tag = (dec / (L1.sets.length * blocksize));

					((OptimalSet) L1.sets[index]).addToAccessStream(tag);
				}

				((OptimalSet) L1.sets[20]).accessStream.forEach((tag) -> System.out.print(tag + " "));
				System.out.println();
				System.out.println(((OptimalSet) L1.sets[20]).accessStream.size());
			}
			
			scan = new Scanner(trace);
			while(scan.hasNextLine()) {

				String instr = scan.nextLine();
				String[] tokens = instr.split(" ");

				// convert hex to decimal
				Long dec = Long.decode("0x" + tokens[1]);
				// write to L1
				if(tokens[0].equals("w")) {
					L1.write(dec);
				}
				// read to L1
				else {
					L1.read(dec);
				}

				
			}
			
			// write our output to file output.txt
			try {
				// generate replacement policy string
				String rp = "LRU";
				if(replacement_policy == 1) rp = "Pseudo-LRU";
				else if(replacement_policy == 2) rp = "Optimal";

				FileWriter w = new FileWriter("output.txt");
				w.write("===== Simulator configuration =====\n");
				w.write("BLOCKSIZE:\t\t\t\t" + blocksize + "\n");
				w.write("L1_SIZE:\t\t\t\t" + L1_size + "\n");
				w.write("L1_ASSOC:\t\t\t\t" + L1_assoc + "\n");
				w.write("L2_SIZE:\t\t\t\t" + L2_size + "\n");
				w.write("L2_ASSOC:\t\t\t\t" + L2_assoc + "\n");
				w.write("REPLACEMENT POLICY:\t\t" + rp + "\n");
				w.write("INCLUSION PROPERTY:\t\t" + (inclusion_property == 1 ? "inclusive" : "non-inclusive") + "\n");
				// remove the directories from the filepath string
				w.write("trace_file:\t\t\t\t" + args[7].substring(args[7].lastIndexOf('\\') + 1) + "\n");
				w.write("===== L1 contents =====\n");
				for(int i = 0; i < L1.sets.length; i++) {
					w.write("Set\t\t" + i + ":\t");
					for(int j = 0; j < L1.sets[i].getBlocks().length; j++) {
						if(L1.sets[i].getBlocks()[j].tag != null)
							w.write(Long.toHexString(L1.sets[i].getBlocks()[j].tag) + " " + (L1.sets[i].getBlocks()[j].dirty ? "D" : " ") + "\t");
						else w.write("null\t");
					}
					w.write("\n");
				}

				// write L2 contents if it exists
				if(L2_size > 0) {
					w.write("===== L2 contents =====\n");
					for(int i = 0; i < L2.sets.length; i++) {
						w.write("Set\t\t" + i + ":\t");
						for(int j = 0; j < L2.sets[i].getBlocks().length; j++) {
							if(L2.sets[i].getBlocks()[j].tag != null)
							w.write(Long.toHexString(L2.sets[i].getBlocks()[j].tag) + " " + (L2.sets[i].getBlocks()[j].dirty ? "D" : " ") + "\t");
						else w.write("null\t");
						}
						w.write("\n");
					}
				} else {
					// prevent null pointer errors by instantiating dummy cache, just for output purposes
					L2 = new Cache(1,1,1,0,null);
				}

				// calculate total memory traffic 
				int memtraffic = 0;
				if(L2_size > 0) memtraffic = L2.readmiss + L2.writemiss + L2.writebacks;
				else memtraffic = L1.readmiss + L1.writemiss + L1.writebacks;

				// prevent NaN and divide by zero errors
				String L1_missrate = "0", L2_missrate = "0";
				if(L1.readmiss + L1.writemiss > 0 && L1.reads + L1.writes > 0) {
					L1_missrate = String.format("%.6f", (L1.readmiss + L1.writemiss)/((double)L1.reads + (double) L1.writes));
				}
				if(L2.readmiss > 0 && L2.reads > 0) {
					L2_missrate = String.format("%.6f", L2.readmiss/(double)L2.reads);
				}



				w.write("===== Simulation results (raw) =====\n");
				w.write("a. number of L1 reads:\t\t\t" + L1.reads + "\n");
				w.write("b. number of L1 read misses:\t" + L1.readmiss + "\n");
				w.write("c. number of L1 writes: \t\t" + L1.writes + "\n");
				w.write("d. number of L1 write misses:\t" + L1.writemiss + "\n");
				w.write("e. L1 miss rate:\t\t\t\t" + L1_missrate + "\n");
				w.write("f. number of L1 writebacks:\t\t" + L1.writebacks + "\n");
				w.write("g. number of L2 reads:\t\t\t" + L2.reads + "\n");
				w.write("h. number of L2 read misses:\t" + L2.readmiss + "\n");
				w.write("i. number of L2 writes:\t\t\t" + L2.writes + "\n");
				w.write("j. number of L2 write misses:\t" + L2.writemiss + "\n");
				w.write("k. L2 miss rate:\t\t\t\t" +L2_missrate + "\n");
				w.write("l. number of L2 writebacks:\t\t" + L2.writebacks + "\n");
				w.write("m. total memory traffic:\t\t" + memtraffic);


				w.close();

			} catch(IOException e) {
				System.err.println("an error occurred writed to the file");
				e.printStackTrace();
			}
			
		} catch(FileNotFoundException e) {
			System.out.println("An error reading the file occured");
			e.printStackTrace();
		}
		

	}
}
