package services;

import java.sql.SQLException;
import java.util.List;

public interface IService<T, ID> {
    T ajouter(T entity) throws SQLException;
    T getById(ID id) throws SQLException;
    List<T> getAll() throws SQLException;
    boolean modifier(T entity) throws SQLException;
    boolean supprimer(ID id) throws SQLException;
    boolean valider(T entity);
}