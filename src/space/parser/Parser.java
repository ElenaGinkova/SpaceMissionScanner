package space.parser;

public interface Parser<T> {
    T deserialize(String line);
}
