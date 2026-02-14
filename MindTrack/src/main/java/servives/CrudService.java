package servives;

import entities.humeur;
import entities.JournalEmotionnel;
import java.util.List;

public interface CrudService<T> {

    void create(T entity);

    T read(int id);

    List<T> readAll();

    void update(T entity);

    void delete(int id);
}
