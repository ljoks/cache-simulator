public class PLRUSet implements CacheSet {
    int assoc;
    int blocksize;
    Block[] blocks;
    // need something to represent the tree
    private byte[] plru_tree;

    public PLRUSet(int assoc, int blocksize) {
        this.assoc = assoc;
        this.blocksize = blocksize;
        blocks = new Block[assoc];
        for(int i = 0; i < blocks.length; i++) {
            blocks[i] = new Block(blocksize);
        }

        this.plru_tree = new byte[assoc-1];
        for(int i = 0; i < this.plru_tree.length; i++) {
            this.plru_tree[i] = 0;
        }
    }
    
    public void write(Block block, Long tag) {
        // replace block
        block.readOrWritePLRU(tag);

        // update plru tree path (if assoc > 1)
        if(this.plru_tree.length > 0) {
            for(int i = 0; i < blocks.length; i++) {
                if(tag.equals(blocks[i].tag)) {
                    updatePathOnAccess((i + assoc -2)/2, (i + assoc - 1)%2);
                }
            }
        }

    }

    public void read(Block block, Long tag) {
        // replace block
        block.readOrWritePLRU(tag);

        // update plru tree path (if assoc > 1)
        if(this.plru_tree.length > 0) {
            for(int i = 0; i < blocks.length; i++) {
                if(tag.equals(blocks[i].tag)) {
                    updatePathOnAccess((i + assoc -2)/2, (i + assoc - 1)%2);
                }
            }
        }
       
    }

    
    public Block checkForHit(Long tag) {
        for (int i = 0; i < blocks.length; i++) {
            if(tag.equals(blocks[i].tag) && blocks[i].valid) {
                // hit. update plru tree (if assoc > 1)
                if(this.plru_tree.length > 0) {
                    updatePathOnAccess((i + assoc -2)/2, (i + assoc - 1)%2);
                }
                   
                return blocks[i];
            } 
        }
        return null;
    }

    
    public Block selectVictim() {
        int index = 0;

        if(this.plru_tree.length > 0) {
            // find the victim
            index = traverseTree(0);
            // update tree as that index is now the most recently accessed
            // updatePathOnAccess((index + assoc -2)/2, (index + assoc - 1)%2);
        }
        
        // return the victim
        return blocks[index];
    }

    // returns the index of the block in the set which will be the victim
    private int traverseTree(int index) {
        // have reached leaf node
        if(2*index + 1 > this.plru_tree.length - 1) {
            // if left:
            if(this.plru_tree[index] == 1) 
                return index * 2 + 1 - (assoc - 1);
            // else if right
            else return index * 2 + 2 - (assoc - 1);
        }
        else {
            // if left
            if(this.plru_tree[index] == 1) return traverseTree(2*index + 1);
            else return traverseTree(2*index + 2);
        }
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
    
    // recursively update plru tree path
    // dir: 1 = left, 0 = right
    // update this.plru_tree[index]
    private void updatePathOnAccess(int index, int dir) {
        // if left set node to 0, if right set node to 1
        this.plru_tree[index] = dir == 1 ? 0 : (byte) 1;

        if(index > 0) {
            updatePathOnAccess((index-1)/2, index%2);
        }
        
    }
}
