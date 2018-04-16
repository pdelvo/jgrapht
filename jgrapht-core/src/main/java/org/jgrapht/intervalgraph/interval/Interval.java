package org.jgrapht.intervalgraph.interval;

public class Interval<T extends Comparable<T>> implements Comparable<Interval<T>> {
    private T start;
    private T end;

    public Interval(T start, T end)  {
        this.start = start;
        this.end = end;
        if (!isValid())
            throw new RuntimeException();
    }

    public T getStart() {
        return start;
    }
    public T getEnd() {
        return end;
    }

    public boolean isIntersecting(Interval<T> other) {
        return other.getStart().compareTo(this.getEnd()) <= 0 || this.getStart().compareTo(other.getEnd()) <= 0;
    }

    public boolean contains(T point) {
        boolean result = point.compareTo(getStart()) >= 0 && point.compareTo(getEnd()) <= 0;
        assert result == (relativeDistance(point) == 0);
        return result;
    }


    public int relativeDistance(T o) {
        int relativeStart = getStart().compareTo(o);
        int relativeEnd = getEnd().compareTo(o);

        if (relativeStart <= 0 && relativeEnd >= 0) {
            return 0;
        } else if (relativeStart > 0) {
            return relativeStart;
        } else {
            return relativeEnd;
        }
    }


    @Override
    public int compareTo(Interval<T> o) {
        int isLeft = getEnd().compareTo(o.getStart()); // < 0 if this ends before other starts
        int isRight = getStart().compareTo(o.getEnd()); // > 0 if this starts before other ends

        if (isLeft >= 0 && isRight <= 0) {
            return 0;
        } else if (isLeft < 0) {
            return isLeft;
        } else {
            return isRight;
        }
    }

    public boolean isValid() {
        return getStart().compareTo(getEnd()) <= 0;
    }


}
