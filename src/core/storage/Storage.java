package core.storage;

public interface Storage<T>{
    boolean add(T item);
    T get(String id);
}