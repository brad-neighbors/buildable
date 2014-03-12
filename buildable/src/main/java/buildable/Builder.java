package buildable;

/**
 * Specifies a common interface to build an instance of a specified type.
 *
 * @param <T> The type of class that the Builder can build.
 */
public interface Builder<T> {

    /**
     * Builds the type specified.
     *
     * @return An instance of the type T.
     */
    T build();
}
