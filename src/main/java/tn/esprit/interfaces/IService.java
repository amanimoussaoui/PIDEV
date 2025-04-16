package tn.esprit.interfaces;
import java.util.List;

public interface IService<T>{

    default void add(T t) {}

    void update (T t) ;

    void delete (int id );

    List<T> display ();

    T get (int id);

}

