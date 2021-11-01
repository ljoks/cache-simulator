import java.util.ArrayList;

public class OptimalSet extends CacheSet {

    ArrayList<Long> accessStream;
    int accessIndex;

    public OptimalSet(int assoc, int blocksize) {
        this.assoc = assoc;
        this.blocksize = blocksize;
        blocks = new Block[assoc];
        for(int i = 0; i < blocks.length; i++) {
            blocks[i] = new Block(blocksize);
        }

        accessStream = new ArrayList<Long>();
        accessIndex = 0;
    }

    
    public void write(Block block, Long tag) {
        block.readOrWrite(tag);  
        accessIndex++;  
    }

    
    public void read(Block block, Long tag) {
        block.readOrWrite(tag);
        accessIndex++;
    }

    
    public Block checkForHit(Long tag) {
        
        for (int i = 0; i < blocks.length; i++) {
            if(tag.equals(blocks[i].tag) && blocks[i].valid) {
                accessIndex++;
                return blocks[i];
            } 
        }
        return null;
    }

    
    public Block selectVictim() {
        int farthest = accessIndex;
        int victimIndex = -1;

        // go through each block in the set
        for(int i = 0; i < blocks.length; i++) {
            int j;
            // loop thru remaining accesses that will occur
            for(j = accessIndex + 1; j < accessStream.size(); j++) {
                // if the tags are the same
                if(blocks[i].tag.equals(accessStream.get(j))) {
                    // if this access is farther than our farthest access
                    // set this block to be victim
                    if(j > farthest) {
                        farthest = j;
                        victimIndex = i;
                    }
                    break;
                }
            }

            // if that tag never gets referenced again, return this block
            if(j == accessStream.size()) {
                return blocks[i];
            }
        }

        // if all of the blocks were not in the future, just return the first one
        return victimIndex == -1 ? blocks[0] : blocks[victimIndex];
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

    public void addToAccessStream(Long tag) {
        accessStream.add(tag);
    }
    
}
