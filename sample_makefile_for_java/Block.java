public class Block {
    int size;
    Long tag;
    int LRU_count;
    boolean dirty;
    boolean valid;


    public Block(int blocksize) {
        size = blocksize;
        LRU_count = 0;
        dirty = false;
        valid = false;
    }

    public void writeLRU(Long tag, int setLRUcounter) {
        this.tag = tag;
        valid = true;
        LRU_count = setLRUcounter;
    }

    public void readLRU(Long tag, int setLRUcounter) {
        this.tag = tag;
        valid = true;
        LRU_count = setLRUcounter;
    }

    public void readOrWrite(Long tag) {
        this.tag = tag;
        valid = true;
    }

    public boolean invalidate() {
        this.valid = false;
        boolean ret = this.dirty;
        this.dirty = false;
        return ret;
    }
}
