public abstract class CacheSet {
    int assoc;
    int blocksize;
    Block[] blocks;

    abstract void write(Block block, Long tag);
    abstract void read(Block block, Long tag);
    abstract Block checkForHit(Long tag);
    abstract Block selectVictim();
    abstract Block findInvalidBlock();
    abstract Block[] getBlocks();

    // return true if the block to invalidate was found and was dirty
    // otherwise return false
    public Boolean invalidate(Long tag) {
        for(int i = 0; i < blocks.length; i++) {
            if(tag.equals(blocks[i].tag) && blocks[i].valid) {
                // block was dirty. return true to write it back to main memory
                return blocks[i].invalidate();
            }
        }

        // block was not found
        return false;
    }
}
