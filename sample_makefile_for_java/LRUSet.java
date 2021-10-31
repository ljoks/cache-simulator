public class LRUSet implements CacheSet {
    int assoc;
    int blocksize;
    Block[] blocks;
    int setLRUcounter;

    public LRUSet(int assoc, int blocksize){
        this.assoc = assoc;
        this.blocksize = blocksize;
        blocks = new Block[assoc];
        for(int i = 0; i < blocks.length; i++) {
            blocks[i] = new Block(blocksize);
        }
        setLRUcounter = 0;
    }
    
    public void write(Block block, Long tag) {
        block.writeLRU(tag, ++setLRUcounter);
    }

    public void read(Block block, Long tag) {
        block.readLRU(tag, ++setLRUcounter);
        
    }


    public Block checkForHit(Long tag) {
        for (int i = 0; i < blocks.length; i++) {
            if(tag.equals(blocks[i].tag) && blocks[i].valid) {
                // hit. update lru for that block
                blocks[i].LRU_count = ++setLRUcounter;
                return blocks[i];
            } 
        }
        return null;
    }


    public Block selectVictim() {
        Block victim = blocks[0];

        for(int i = 1; i < blocks.length; i++) {
            if(blocks[i].LRU_count < victim.LRU_count) {
                victim = blocks[i];
            }
        }

        return victim;
    }

    public Block findInvalidBlock() {
        // step one: search for invalid block
        for(Block block : blocks) {
            if(block.valid == false) return block;
        }

        return null;
    }

    public Block[] getBlocks() {
        return blocks;
    }

    
    
}
