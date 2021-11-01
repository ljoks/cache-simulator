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
}
