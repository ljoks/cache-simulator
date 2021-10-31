public interface CacheSet {

    public void write(Block block, Long tag);
    public void read(Block block, Long tag);
    public Block checkForHit(Long tag);
    public Block selectVictim();
    public Block findInvalidBlock();
    public Block[] getBlocks();
}
