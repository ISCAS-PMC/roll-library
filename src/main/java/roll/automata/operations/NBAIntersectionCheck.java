/* Copyright (c) 2016, 2017                                               */
/*       Institute of Software, Chinese Academy of Sciences               */
/* This file is part of ROLL, a Regular Omega Language Learning library.  */
/* ROLL is free software: you can redistribute it and/or modify           */
/* it under the terms of the GNU General Public License as published by   */
/* the Free Software Foundation, either version 3 of the License, or      */
/* (at your option) any later version.                                    */

/* This program is distributed in the hope that it will be useful,        */
/* but WITHOUT ANY WARRANTY; without even the implied warranty of         */
/* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          */
/* GNU General Public License for more details.                           */

/* You should have received a copy of the GNU General Public License      */
/* along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

package roll.automata.operations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import roll.automata.NBA;
import roll.util.Pair;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;
import roll.words.Alphabet;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class NBAIntersectionCheck {
    
    private NBA fstOp;
    private NBA sndOp;
    private boolean empty = true;
    private NBA result;
    private ISet fstAcc;
    private ISet sndAcc;
    private boolean needCE;
    private int numStates;
    private NBAEmptinessCheck checker;
    
    public NBAIntersectionCheck(NBA fstOp, NBA sndOp) {
        this(fstOp, sndOp, false);
    }
    
    public NBAIntersectionCheck(NBA fstOp, NBA sndOp, boolean needCE) {
        assert fstOp != null && sndOp != null;
        this.needCE = needCE;
        this.fstOp = fstOp;
        this.sndOp = sndOp;
        this.numStates = 0;
        if(needCE) {
            this.result = new NBA(fstOp.getAlphabet());
            this.fstAcc = UtilISet.newISet();
            this.sndAcc = UtilISet.newISet();
        }
        new AsccExplore();
        if(needCE) {
            checker = new NBAEmptinessCheck(result, fstAcc, sndAcc);
            if(!empty) { // is not empty
                checker.isEmpty(); // find accepting loop
            }
        }
    }
    
    public void computePath() {
        if(!needCE || fstAcc == null || sndAcc == null) {
            throw new UnsupportedOperationException("No accepting loop");
        }
        
        if(fstAcc.isEmpty() || sndAcc.isEmpty()) {
            throw new UnsupportedOperationException("No accepting loop");
        }
        checker.findpath();
    }
    
    public Pair<Word, Word> getCounterexample() {
        return checker.getCounterexample();
    }
    
    public boolean isEmpty() {
        return empty;
    }
    
    private class Elem {
        ProductState state;
        byte label;
        Elem(ProductState s, byte l) {
            state = s;
            label = l;
        }
    }
    
    private class ProductState {
        int fstState;
        int sndState;
        int resState;
        ProductState(int fstState, int sndState) {
            this.fstState = fstState;
            this.sndState = sndState;
        }
        
        @Override
        public int hashCode() {
            return fstState * sndOp.getStateSize() + sndState;
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
    
    
    public class AsccExplore {
        
        private int depth;
        private final Stack<Elem> sccs;           // C99 's root stack
        private final Stack<Integer> act;            // tarjan's stack
        private final TIntIntMap dfsNum;        
        private final Map<ProductState, ProductState> map; 
        
        public AsccExplore() {
            this.sccs = new Stack<>();
            this.act = new Stack<>();
            this.dfsNum = new TIntIntHashMap();
            this.depth = 0;
            this.map = new HashMap<>();
            strongConnect(initialize());
        }
        
        ProductState getOrAddState(int fst, int snd) {
            ProductState prod = new ProductState(fst, snd);
            if(map.containsKey(prod)) {
                return map.get(prod);
            }
            prod.resState = numStates;
            map.put(prod, prod);
            ++ numStates;
            if(needCE) {
                result.createState();
                assert numStates == result.getStateSize();
                if(fstOp.isFinal(fst)) {
                    fstAcc.set(prod.resState);
                }
                if(sndOp.isFinal(snd)) {
                    sndAcc.set(prod.resState);
                }
            }
            
            return prod;
        }
        
        private ProductState initialize() {
            ProductState prod = getOrAddState(fstOp.getInitialState(), sndOp.getInitialState());
            if(needCE) result.setInitial(prod.resState);
            return prod;
        }

        byte getLabel(ProductState prod) {
            byte label = 0;
            if(fstOp.isFinal(prod.fstState)) {
                label |= 1;
            }
            if(sndOp.isFinal(prod.sndState)) {
                label |= 2;
            }
            return label;
        }

        void strongConnect(ProductState prod) {
            ++ depth;
            dfsNum.put(prod.resState, depth);
            sccs.push(new Elem(prod, getLabel(prod)));
            act.push(prod.resState);
            
            Alphabet alphabet = fstOp.getAlphabet();
            for (int letter = 0; letter < alphabet.getLetterSize(); letter ++) {
                for(int sndSucc : sndOp.getSuccessors(prod.sndState, letter)) {
                    for(int fstSucc : fstOp.getSuccessors(prod.fstState, letter)) {
                        ProductState succ = getOrAddState(fstSucc, sndSucc);
                        if(needCE) result.getState(prod.resState).addTransition(letter, succ.resState);
                        if (!dfsNum.containsKey(succ.resState)) {
                            strongConnect(succ);
                            if(!empty) return ;
                        } else if (act.contains(succ.resState)) {
                            // we have already seen it before, there is a loop
                            // probably there is one final state without self-loop
                            byte B = 0;
                            ProductState u;
                            do {
                                Elem p = sccs.pop();
                                u = p.state;
                                B |= p.label;
                                if(B == 3) {
                                    empty = false;
                                    return;
                                }
                            }while(dfsNum.get(u.resState) > dfsNum.get(succ.resState));
                            sccs.push(new Elem(u, B));
                        }
                    }
                }
            }
            
            // if current number is done, then we should remove all 
            // active states in the same scc
            if(sccs.peek().state.resState == prod.resState) {
                sccs.pop();
                int u = 0;
                do {
                    assert !act.isEmpty() : "Act empty";
                    u = act.pop();
                }while(u != prod.resState);
            }
        }
        
        void strongConnectNonrecursive(ProductState prod) {
        	
        	TIntObjectMap<ProductState[]> prodSuccMap = new TIntObjectHashMap<>();
        	Stack<ProductState> prodStateStack = new Stack<>(); // stack for return
        	Stack<Integer> prodSuccStack = new Stack<>(); // next successor
        	
        	ProductState tjCurr = prod;
            ++ depth;
            dfsNum.put(tjCurr.resState, depth);
            sccs.push(new Elem(tjCurr, getLabel(tjCurr)));
            act.push(tjCurr.resState);
            
            Alphabet alphabet = fstOp.getAlphabet();
            prodStateStack.push(tjCurr);
            
            int tjSuccIter = 0;
            int tjCallStackIndex = 0;
            
            while(tjCallStackIndex >= 0) {
            	// current stack
            	if(! prodSuccMap.containsKey(tjCurr.resState)) {
            		HashSet<ProductState> succs = new HashSet<>();
                    for (int letter = 0; letter < alphabet.getLetterSize(); letter ++) {
                        for(int sndSucc : sndOp.getSuccessors(tjCurr.sndState, letter)) {
                            for(int fstSucc : fstOp.getSuccessors(tjCurr.fstState, letter)) {
                            	ProductState succ = getOrAddState(fstSucc, sndSucc);
                            	succs.add(succ);
                            }
                        }
                    }
                    ProductState[] arrSuccs = new ProductState[succs.size()];
                    int i = 0 ;
                    for(ProductState succ : succs) {
                    	arrSuccs[i] = succ;
                    	i ++;
                    }
                    prodSuccMap.put(tjCurr.resState, arrSuccs);
            	}
            	// now turn to the successor
            	int numSucc = prodSuccMap.get(tjCurr.resState).length;
            	if(tjSuccIter < numSucc) {
            		ProductState tjSucc = prodSuccMap.get(tjCurr.resState)[tjSuccIter];
            		tjSuccIter ++ ; // move to next successor
            		if (! dfsNum.containsKey(tjSucc.resState)) {
                        // store current node and next iterator
            			prodStateStack.push(tjCurr); // current product state
            			prodSuccStack.push(tjSuccIter);// cursor to next successor
            			tjCallStackIndex ++;
            			// now do things for curr
            			tjCurr = tjSucc;
            			tjSuccIter = 0;
                        ++ depth;
                        dfsNum.put(tjCurr.resState, depth);
                        sccs.push(new Elem(tjCurr, getLabel(tjCurr)));
                        act.push(tjCurr.resState);
                    } else if (act.contains(tjSucc.resState)) {
                        // we have already seen it before, there is a loop
                        // probably there is one final state without self-loop
                        byte B = 0;
                        ProductState u;
                        do {
                            Elem p = sccs.pop();
                            u = p.state;
                            B |= p.label;
                            if(B == 3) {
                                empty = false;
                                return;
                            }
                        }while(dfsNum.get(u.resState) > dfsNum.get(tjSucc.resState));
                        sccs.push(new Elem(u, B));
                    }
            	}else {
            		// else we have visited 
            		// if current number is done, then we should remove all 
                    // active states in the same scc
                    if(sccs.peek().state.resState == tjCurr.resState) {
                        sccs.pop();
                        int u = 0;
                        do {
                            assert !act.isEmpty() : "Act empty";
                            u = act.pop();
                        }while(u != tjCurr.resState);
                    }
                    tjCallStackIndex --;
                    // now return to upper level
                    if(tjCallStackIndex >= 0) {
                    	//ProductState succ = tjCurr;
                    	tjCurr = prodStateStack.pop();
                    	tjSuccIter = prodSuccStack.pop();
                    	if(! empty) return ;
                    }
            	}
            }
        }
    }

}
