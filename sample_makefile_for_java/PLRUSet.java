public class PLRUSet implements CacheSet {
    int assoc;
    int blocksize;
    Block[] blocks;
    // need something to represent the tree

    public PLRUSet(int assoc, int blocksize) {
        this.assoc = assoc;
        this.blocksize = blocksize;
        blocks = new Block[assoc];
        for(int i = 0; i < blocks.length; i++) {
            blocks[i] = new Block(blocksize);
        }
    }
    
    public void write(Block block, Long tag) {
        // TODO Auto-generated method stub
        
    }

    
    public void read(Block block, Long tag) {
        // TODO Auto-generated method stub
        
    }

    
    public Block checkForHit(Long tag) {
        // TODO Auto-generated method stub
        return null;
    }

    
    public Block selectVictim() {
        // TODO Auto-generated method stub
        return null;
    }

    
    public Block findInvalidBlock() {
        // TODO Auto-generated method stub
        return null;
    }

    
    public Block[] getBlocks() {
        // TODO Auto-generated method stub
        return null;
    }
    
}
