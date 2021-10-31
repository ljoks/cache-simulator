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

    public void write(Long tag, int setLRUcounter) {
        this.tag = tag;
        valid = true;
        LRU_count = setLRUcounter;
    }

    public void read(Long tag, int setLRUcounter) {
        this.tag = tag;
        valid = true;
        LRU_count = setLRUcounter;
    }
}
