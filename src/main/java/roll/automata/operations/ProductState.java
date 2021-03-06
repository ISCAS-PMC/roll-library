package roll.automata.operations;

public class ProductState {
	
    int fstState;
    int sndState;
    public int resState;
    final int n;
    
    public ProductState(int fstState, int sndState, int n) {
        this.fstState = fstState;
        this.sndState = sndState;
        this.n = n;
    }
    
    @Override
    public int hashCode() {
        return fstState * n + sndState;
    }
    
    public int getFirst() {
    	return fstState;
    }
    
    public int getSecond() {
    	return sndState;
    }
    
    @Override
    public boolean equals(Object obj) {
        if(obj == null) return false;
        if(obj == this) return true;
        if(obj instanceof ProductState) {
            ProductState other = (ProductState)obj;
            return fstState == other.fstState
                && sndState == other.sndState;
        }
        return false;
    }
        
    @Override
    public String toString() {
        return resState + ":(" + fstState + "," + sndState + ")";
    }

}
