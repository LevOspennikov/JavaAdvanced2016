import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Consumer;

/**
 * Created by ospen_000 on 28.02.2016.
 */
public class ArraySet<E> extends AbstractSet<E> implements NavigableSet<E> {
    private class Pair{
        int first;
        int second;
        Pair(int f, int s){
            first = f;
            second = s;
        }
    }

    private class MyIterator implements Iterator<E>  {
        boolean isReversed;
        ListIterator<E> iter;
        int begin, end;

        MyIterator(ListIterator<E> it, int begin, int end){
            iter = it;
            isReversed = false;
            this.begin = begin;
            this.end = end;
        }


        @Override
        public boolean hasNext() {
            if (!isReversed) {
                return  iter.nextIndex() <= end && iter.nextIndex() >= begin;
            }
            else {
                return iter.previousIndex() >= begin ;
            }
        }

        private boolean hasPrevious(){
            return false;
        }
        @Override
        public E next() {
            if (this.hasNext()) {
                return iter.next();
            } else return null;
        }
    }

    private ArrayList<E> skeletonList;

    private Comparator<E> comparator;
    private boolean haveComp;
    private int begin, end;



    public ArraySet(){
        skeletonList = new ArrayList();
        begin = 0;
        end = -1;
        haveComp = false;
    }

    private ArraySet(ArraySet<E> set, int begin, int end){
        this.skeletonList = set.skeletonList;
        this.begin = begin;
        this.end = end ;
        this.comparator = set.comparator;
        this.haveComp = haveComp;

    }

    public ArraySet(Collection<E> collection, Comparator<E> comp ){

        skeletonList = new ArrayList<E>();
        Iterator it = collection.iterator();
        begin = 0;
        comparator = comp;
        haveComp = true;
        while (it.hasNext()) {
            E element = (E) it.next();
            if (!this.contains(element)) {
                skeletonList.add(element);
            }
        }
        end = skeletonList.size() - 1;
        skeletonList.sort(comparator);
    }

    public ArraySet(Collection<E> collection) {
        this(collection, new Comparator<E>() {
            @Override
            public int compare(E o1, E o2) {
                return ((Comparable<E>)o1).compareTo((E) o2);
            }
        });
        haveComp = false;
    }


    private Pair findElem(E o){
        if (size() == 0){
            return (null);
        }
        int i = Collections.binarySearch(skeletonList, o, comparator);
        if (i >= 0){
            if (i < begin){
                return new Pair(begin - 1, begin);
            } else {
                if (i > end) {
                    return new Pair (end, end + 1);
                }
            }
            return new Pair(i, i);
        } else {
            int p = -(i + 1);
            if (p < begin) {
                return new Pair(begin - 1, begin);
            } else {
                if ( p > end){
                    return new Pair(end, end + 1);
                }
            }
            return new Pair(p - 1, p);
        }
    }


    private E lowerWithPair(Pair pair){
        if (pair != null && pair.second != 0){
            return skeletonList.get(pair.second - 1);
        } else {
            return null;
        }
    }

    @Override
    public boolean contains(Object o){
        if (this.size() == 0){
            return false;
        }
        Pair p =  findElem((E) o);
        return (p != null && p.first == p.second );
    }

    @Override
    public E lower(E o) {
        Pair pair = findElem(o);
        if (pair != null ) {
            return lowerWithPair(pair);
        } else {
            return null;
        }
    }

    @Override
    public E floor(E o) {
        Pair pair = findElem(o);
        if ( pair != null && pair.second == pair.first){
            return skeletonList.get(pair.second);
        } else {
            return lowerWithPair(pair);
        }
    }
    public E higherWithPair(Pair pair){
        if (pair != null && pair.first != skeletonList.size() - 1){
            return skeletonList.get(pair.first + 1);
        } else {
            return null;
        }
    }

    @Override
    public E ceiling(E o) {
        Pair pair = findElem(o);

        if ( pair != null && pair.second == pair.first){
            return skeletonList.get(pair.second);
        } else {
            return higherWithPair(pair);
        }
    }

    @Override
    public E higher(E o) {
        Pair pair = findElem(o);
        if (pair != null ) {
            return higherWithPair(pair);
        } else {
            return null;
        }
    }

    @Override
    public E pollFirst() {
        return (E)skeletonList.get(begin);
    }

    @Override
    public E pollLast() {
        return (E)skeletonList.get(end);
    }

    @Override
    public Iterator<E> iterator() {
        return new MyIterator(skeletonList.listIterator(begin), begin, end);
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return null;
    }

    @Override
    public Iterator<E> descendingIterator() {
        return null;
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        return this.tailSet(fromElement, fromInclusive).headSet(toElement, toInclusive);


    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        Pair p = findElem(toElement);
        if (p != null) {
            int second = p.second;
            if (!inclusive || p.second != p.first){
                second--;
            }
            return  new ArraySet<E>(this, begin, second);
        }
        return new ArraySet<E>();
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        Pair p = findElem(fromElement);
        if (p != null) {
            int first = p.first;
            if (!inclusive || p.first != p.second) {
                first++;
            }
            return  new ArraySet<E>(this, first, end);
        }
        return new ArraySet<E>();
    }

    @Override
    public Comparator<E> comparator() {
        if (haveComp) {
            return comparator;
        }
        else {
            return null;
        }
    }

    @Override
    public SortedSet<E> subSet(Object fromElement, Object toElement) {
        return this.subSet((E)fromElement, true, (E)toElement, false);
    }

    @Override
    public SortedSet<E> headSet(Object toElement) {
        return this.headSet((E)toElement, false);
    }

    @Override
    public SortedSet<E> tailSet(Object fromElement) {
        return this.tailSet((E)fromElement, true);
    }

    @Override
    public E first() {
        if (skeletonList.size() > 0){
            return skeletonList.get(begin);
        } else{
            throw new NoSuchElementException();
        }
    }

    @Override
    public E last() {
        if (skeletonList.size() > 0){
            return skeletonList.get(end);
        } else{
            throw new NoSuchElementException();
        }
    }

    @Override
    public int size() {
        if (end - begin + 1 <= 0) {
            return 0;
        } else {
            return end - begin + 1;
        }
    }
}
