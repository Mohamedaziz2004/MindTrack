package dao;

import java.sql.SQLException;
import java.util.List;

public interface IDAO<T, ID> {
    T create(T entity) throws SQLException;
    T read(ID id) throws SQLException;
    boolean update(T entity) throws SQLException;
    boolean delete(ID id) throws SQLException;
    List<T> findAll() throws SQLException;
    long count() throws SQLException;
}