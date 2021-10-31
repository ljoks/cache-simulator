public class Set {
    int assoc;
    int blocksize;
    Block[] blocks;
    int setLRUcounter;

    public Set(int assoc, int blocksize){
        this.assoc = assoc;
        this.blocksize = blocksize;
        blocks = new Block[assoc];
        for(int i = 0; i < blocks.length; i++) {
            blocks[i] = new Block(blocksize);
        }
        setLRUcounter = 0;
    }

    public Block findValidBlock(Long tag) {
        for (int i = 0; i < blocks.length; i++) {
            if(tag.equals(blocks[i].tag) && blocks[i].valid) return blocks[i];
        }
        return null;
    }

    public void write(Block block, Long tag) {
        block.writeLRU(tag, ++setLRUcounter);
    }

    public void read(Block block, Long tag) {
        block.readLRU(tag, ++setLRUcounter);
    }

    public void updateBlockLRU(Block block) {
        block.LRU_count = ++setLRUcounter;
    }

    public Block findInvalidBlock() {
        // step one: search for invalid block
        for(Block block : blocks) {
            if(block.valid == false) return block;
        }

        return null;
    }

    public Block selectVictim(int policy) {
        Block victim = blocks[0];

        // LRU
        if(policy == 0) {
            for(int i = 1; i < blocks.length; i++) {
                if(blocks[i].LRU_count < victim.LRU_count) {
                    victim = blocks[i];
                }
            }

            return victim;
        }

        else {
            // this shouldn't happen yet, we haven't implemented other policies
            return null;
        }
    }
}
