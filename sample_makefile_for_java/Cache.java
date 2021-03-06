public class Cache {
    int size;
    int assoc;
    int blocksize;
    int num_sets;
    CacheSet[] sets;
    long index_bits;
    long offset_bits;
    long tag_bits;
    int reads;
    int readmiss;
    int writes; 
    int writemiss;
    int writebacks;
    int writebacksToMainMem;
    int repl_policy;
    int incl_policy;
    Cache child; // basically next level in heirarchy
    Cache parent; // level above in heirarchy, necessary for inclusion policy

    public Cache(int size, int assoc, int blocksize, int repl_policy, int incl_policy, Cache child) {
        this.size = size;
        this.assoc = assoc;
        this.blocksize = blocksize;

        num_sets = size/((assoc * blocksize));
        switch(repl_policy) {
            case 0: // LRU
                this.sets = new LRUSet[num_sets];
                break;
            case 1: // PLRU
                this.sets = new PLRUSet[num_sets];
                break;
            case 2: // optimal
                this.sets = new OptimalSet[num_sets];
                break;
            default: // this shouldn't happen
                break;
        }
        
        for(int i = 0; i < sets.length; i++) {
            switch(repl_policy) {
                case 0: // LRU
                    sets[i] = new LRUSet(assoc, blocksize);
                    break;
                case 1: // PLRU
                    sets[i] = new PLRUSet(assoc, blocksize);
                    break;
                case 2: // Optimal
                    sets[i] = new OptimalSet(assoc, blocksize);
                    break;
                default: // this shouldn't happen
                    break;
            }
            
        }

        index_bits = log2(num_sets);
        offset_bits = log2(blocksize);
        tag_bits = 32 - index_bits - offset_bits;
        reads = readmiss = writes = writemiss = writebacks = writebacksToMainMem = 0;
        this.repl_policy = repl_policy;
        this.incl_policy = incl_policy;

        if(child != null) {
            this.child = child;
        } else this.child = null;

    }

    // attempt to write to cache
    public void write(Long address) {
        // increment write counter i THINK? or should it be only if there's a hit
        writes++;
        // is the block in the cache

        //get index
        int index = (int) (address / blocksize ) % sets.length;

        // get tag
        Long tag =  ( address / (sets.length * blocksize));
        
        // check if the tag matches a tag in the set
        Block block = sets[index].checkForHit(tag);

        // hit
        if(block != null) {
            // set dirty bit of that block to 1
            block.dirty = true;
            
        }
        // miss
        else {
            //increment write miss counter
            writemiss++;

            // we may need to evict a block

            // 2 step process: 
            // 1. is there an invalid block in the set? we don't have to evict
            Block victimBlock = null;
            victimBlock = sets[index].findInvalidBlock();

            // there were no invalid blocks. we must select a victim based
            // on the replacement policy
            if(victimBlock == null) {
                victimBlock = sets[index].selectVictim();

                // if that block is dirty, we need to write it back
                // to the next level of the cache
                if(victimBlock.dirty) {
                    writebacks++;
                    if(child != null) {
                        Long victimAddress = (victimBlock.tag * sets.length + index) * blocksize;
                        child.write(victimAddress);
                        
                    }
                    victimBlock.dirty = false;
                    victimBlock.valid = false;
                }
                // not dirty. we don't need to write back. just invalidate it
                else {
                    victimBlock.valid = false;
                }

                // if inclusive policy and L2, we need to also invalidate the
                // corresponding block in L1. If that L1 block is dirty,
                // it will be written directly to main memory
                if(incl_policy == 1 && child == null) {
                    // if a block in L1 was invalidated,
                    // increment writebacks because it was 
                    // written directly back to main memory
                    Long victimAddress = (victimBlock.tag * sets.length + index) * blocksize;
                    parent.invalidate(victimAddress);
                }
            }   

            // 2. bring in requested block from child by issuing read request
            if(child != null) {
                // if optimal policy, we have to write back the tag to an
                // access stream for preprocessing
                // then we will do the read later
                if(repl_policy == 2) {
                    // figure out the index for the child cache
                    int childIndex = (int) (address / blocksize) % child.sets.length;
                    // Long childTag = address / (child.sets.length * blocksize);

                    ((OptimalSet) child.sets[childIndex]).addToAccessStream(true, address);

                }
                else{
                    child.read(address);
                }
                
            }
            // either way fill yourself and "return to level above"
            sets[index].write(victimBlock, tag);
            victimBlock.dirty = true;
        }
    }

    public void read(Long address) {
        // increment number of reads
        reads++;

        // get index and tag
        int index = (int) (address / blocksize ) % sets.length;
        Long tag =  ( address / (sets.length * blocksize));

        // check if the tag matches a tag in the set
        Block block = sets[index].checkForHit(tag);

        // miss (nothing to do at cache level on a hit)
        if(block == null)  {
            readmiss++;

            // 2 step process: 
            // 1. is there an invalid block in the set? we don't have to evict
            Block victimBlock = null;
            victimBlock = sets[index].findInvalidBlock();

            // there were no invalid blocks. we must select a victim based
            // on the replacement policy
            if(victimBlock == null) {
                victimBlock = sets[index].selectVictim();

                // if that block is dirty, we need to write it back
                // to the next level of the cache
                if(victimBlock.dirty) {
                    writebacks++;
                    if(child != null) {
                        Long victimAddress = (victimBlock.tag * sets.length + index) * blocksize;
                        child.write(victimAddress);
                        
                    }
                    victimBlock.dirty = false;
                    victimBlock.valid = false;
                }
                // not dirty. we don't need to write back. just invalidate it
                else {
                    victimBlock.valid = false;
                }    

                // if inclusive policy and L2, we need to also invalidate the
                // corresponding block in L1. If that L1 block is dirty,
                // it will be written directly to main memory
                if(incl_policy == 1 && child == null) {
                    // System.out.println("Invalidating the corresponding block in L1 (read)");
                    // if a block in L1 was invalidated,
                    // increment writebacks because it was 
                    // written directly back to main memory
                    Long victimAddress = (victimBlock.tag * sets.length + index) * blocksize;
                    parent.invalidate(victimAddress);
                }
            }   

            // 2. bring in requested block from child by issuing read request
            if(child != null) {
                // if optimal policy, we have to write back the tag to an
                // access stream for preprocessing
                // then we will do the read later.
                if(repl_policy == 2) {
                    // figure out the index for the child cache
                    int childIndex = (int) (address / blocksize) % child.sets.length;
                    // Long childTag = address / (child.sets.length * blocksize);

                    ((OptimalSet) child.sets[childIndex]).addToAccessStream(false, address);

                }
                else{
                    child.read(address);
                }
            }
            // otherwise fill yourself and "return to level above"
            sets[index].read(victimBlock, tag);
        }
    }

    public void invalidate(Long address) {
        int index = (int) (address / blocksize ) % sets.length;
        Long tag =  ( address / (sets.length * blocksize));

        if(sets[index].invalidate(tag)) {
            writebacksToMainMem++;
        }
    }

    static int log2(int n) {
        return (int)(Math.log(n)/Math.log(2));
    }
}
