package funny.co.design;

public interface Originator<S> {
    S save();

    void restore(S s);
}
