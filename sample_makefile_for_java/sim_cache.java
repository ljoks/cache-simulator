import java.io.File;
import java.io.FileNotFoundException;
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
		L1_assoc = args[2].equals("full") ? L1_size/blocksize : Integer.parseInt(args[2]);
		L2_size = Integer.parseInt(args[3]);
		L2_assoc = Integer.parseInt(args[4]);
		replacement_policy = Integer.parseInt(args[5]);
		inclusion_property = Integer.parseInt(args[6]);
	

		Cache L2 = null;
		// L2_size greater than 0 indicates there is an L2 cache
		if(L2_size > 0) {
			L2 = new Cache(L2_size, L2_assoc, blocksize, replacement_policy, inclusion_property, null);
		}

		Cache L1 = new Cache(L1_size, L1_assoc, blocksize, replacement_policy, inclusion_property, L2);

		if(inclusion_property == 1) {
			L2.parent = L1;
		}

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

					// get the tags, index, and write/read for L1
					int index = (int) (dec / blocksize) % L1.sets.length;
					Long tag = (dec / (L1.sets.length * blocksize));
					Boolean write = tokens[0].equals("w");

					// insert it into the access stream for the appropriate set
					((OptimalSet) L1.sets[index]).addToAccessStream(write, tag);
				}
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

			// if replacement policy is optimal, we need to go through
			// accesses for the preprocessed L2 streams that were
			// written back from L1
			if(replacement_policy == 2 && L2_size > 0) {
				// loop through each set
				for(CacheSet set: L2.sets) {
					
					// perform the necessary accesses
					for(int i = 0; i < ((OptimalSet) set).accessStream.size(); i++) {

						// if it's a write
						if(((OptimalSet) set).accessStreamWrite.get(i) == true) {
							L2.write(((OptimalSet) set).accessStream.get(i));
						}
						// else read
						else{
							L2.read(((OptimalSet) set).accessStream.get(i));
						}
					}
				}
			}
			
			// print our output

			// generate replacement policy string
			String rp = "LRU";
			if(replacement_policy == 1) rp = "Pseudo-LRU";
			else if(replacement_policy == 2) rp = "Optimal";

			
			System.out.print("===== Simulator configuration =====\n");
			System.out.print("BLOCKSIZE:\t\t" + blocksize + "\n");
			System.out.print("L1_SIZE:\t\t" + L1_size + "\n");
			System.out.print("L1_ASSOC:\t\t" + L1_assoc + "\n");
			System.out.print("L2_SIZE:\t\t" + L2_size + "\n");
			System.out.print("L2_ASSOC:\t\t" + L2_assoc + "\n");
			System.out.print("REPLACEMENT POLICY:\t\t" + rp + "\n");
			System.out.print("INCLUSION PROPERTY:\t" + (inclusion_property == 1 ? "inclusive" : "non-inclusive") + "\n");
			// remove the directories from the filepath string
			System.out.print("trace_file:\t" + args[7].substring(args[7].lastIndexOf('\\') + 1) + "\n");
			System.out.print("===== L1 contents =====\n");
			for(int i = 0; i < L1.sets.length; i++) {
				System.out.print("Set\t" + i + ": ");
				for(int j = 0; j < L1.sets[i].getBlocks().length; j++) {
					if(L1.sets[i].getBlocks()[j].tag != null)
						System.out.print(Long.toHexString(L1.sets[i].getBlocks()[j].tag) + " " + (L1.sets[i].getBlocks()[j].dirty ? "D" : " ") + "\t");
					else System.out.print("null\t");
				}
				System.out.print("\n");
			}

			// write L2 contents if it exists
			if(L2_size > 0) {
				System.out.print("===== L2 contents =====\n");
				for(int i = 0; i < L2.sets.length; i++) {
					System.out.print("Set\t" + i + ": ");
					for(int j = 0; j < L2.sets[i].getBlocks().length; j++) {
						if(L2.sets[i].getBlocks()[j].tag != null)
						System.out.print(Long.toHexString(L2.sets[i].getBlocks()[j].tag) + " " + (L2.sets[i].getBlocks()[j].dirty ? "D" : " ") + "\t");
					else System.out.print("null\t");
					}
					System.out.print("\n");
				}
			} else {
				// prevent null pointer errors by instantiating dummy cache, just for output purposes
				L2 = new Cache(1,1,1,0,0,null);
			}

			// calculate total memory traffic 
			int memtraffic = 0;
			if(L2_size > 0) memtraffic = L2.readmiss + L2.writemiss + L2.writebacks;
			else memtraffic = L1.readmiss + L1.writemiss + L1.writebacks;
			if(inclusion_property == 1){
				memtraffic += L1.writebacksToMainMem;
				System.out.println("Writebacks to main mem: " + L1.writebacksToMainMem);
			} 


			// prevent NaN and divide by zero errors
			String L1_missrate = "0", L2_missrate = "0";
			if(L1.readmiss + L1.writemiss > 0 && L1.reads + L1.writes > 0) {
				L1_missrate = String.format("%.6f", (L1.readmiss + L1.writemiss)/((double)L1.reads + (double) L1.writes));
			}
			if(L2.readmiss > 0 && L2.reads > 0) {
				L2_missrate = String.format("%.6f", L2.readmiss/(double)L2.reads);
			}



			System.out.print("===== Simulation results (raw) =====\n");
			System.out.print("a. number of L1 reads:\t\t" + L1.reads + "\n");
			System.out.print("b. number of L1 read misses:\t" + L1.readmiss + "\n");
			System.out.print("c. number of L1 writes: \t" + L1.writes + "\n");
			System.out.print("d. number of L1 write misses:\t" + L1.writemiss + "\n");
			System.out.print("e. L1 miss rate:\t\t" + L1_missrate + "\n");
			System.out.print("f. number of L1 writebacks:\t" + L1.writebacks + "\n");
			System.out.print("g. number of L2 reads:\t\t" + L2.reads + "\n");
			System.out.print("h. number of L2 read misses:\t" + L2.readmiss + "\n");
			System.out.print("i. number of L2 writes:\t\t" + L2.writes + "\n");
			System.out.print("j. number of L2 write misses:\t" + L2.writemiss + "\n");
			System.out.print("k. L2 miss rate:\t\t" +L2_missrate + "\n");
			System.out.print("l. number of L2 writebacks:\t" + L2.writebacks + "\n");
			System.out.print("m. total memory traffic:\t" + memtraffic);

			
			
		} catch(FileNotFoundException e) {
			System.out.println("An error reading the file occured");
			e.printStackTrace();
		}
		
	}
}
