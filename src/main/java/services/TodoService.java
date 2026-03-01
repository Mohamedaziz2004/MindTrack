package services;

import entities.Todo;
import dao.TodoDAO;
import utils.ValidationUtils;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

public class TodoService implements IService<Todo, Integer> {

    private final TodoDAO todoDAO;

    public TodoService() {
        this.todoDAO = new TodoDAO();
    }

    @Override
    public Todo ajouter(Todo todo) throws SQLException {
        if (!valider(todo)) {
            throw new IllegalArgumentException("Les données du todo sont invalides");
        }

        if (todo.getDateCreation() == null) {
            todo.setDateCreation(LocalDateTime.now());
        }

        if (todo.getProgression() == null) {
            todo.setProgression(0);
        }

        return todoDAO.create(todo);
    }

    @Override
    public Todo getById(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID invalide");
        }
        return todoDAO.read(id);
    }

    @Override
    public List<Todo> getAll() throws SQLException {
        return todoDAO.findAll();
    }

    @Override
    public boolean modifier(Todo todo) throws SQLException {
        if (todo == null || todo.getIdTodo() <= 0) {
            throw new IllegalArgumentException("Todo invalide ou ID manquant");
        }
        if (!valider(todo)) {
            throw new IllegalArgumentException("Les données du todo sont invalides");
        }
        return todoDAO.update(todo);
    }

    @Override
    public boolean supprimer(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID invalide");
        }
        return todoDAO.delete(id);
    }

    @Override
    public boolean valider(Todo todo) {
        if (todo == null) return false;

        if (!ValidationUtils.isValidName(todo.getTitre())) {
            System.err.println("Erreur: Titre invalide");
            return false;
        }

        return true;
    }

    // Méthodes métier
    public Todo creerTodo(String titre, String description, String priorite, LocalDate echeance, Integer idExercice) throws SQLException {
        Todo todo = new Todo(titre);
        todo.setDescription(description);
        todo.setPriorite(priorite);
        todo.setDateEcheance(echeance);
        todo.setIdExercice(idExercice);

        return ajouter(todo);
    }

    public boolean changerStatut(int idTodo, String nouveauStatut) throws SQLException {
        Todo todo = getById(idTodo);
        if (todo == null) return false;

        todo.setStatut(nouveauStatut);

        if ("DONE".equals(nouveauStatut)) {
            todo.setDateCompletion(LocalDateTime.now());
            todo.setProgression(100);
        } else if ("EN_COURS".equals(nouveauStatut) && todo.getProgression() == 0) {
            todo.setProgression(25);
        }

        return modifier(todo);
    }

    public boolean mettreAJourProgression(int idTodo, int progression) throws SQLException {
        Todo todo = getById(idTodo);
        if (todo == null) return false;

        todo.setProgression(progression);

        if (progression >= 100) {
            todo.setStatut("DONE");
            todo.setDateCompletion(LocalDateTime.now());
        } else if (progression > 0 && todo.isTodo()) {
            todo.setStatut("EN_COURS");
        }

        return modifier(todo);
    }

    public boolean marquerEnCours(int idTodo) throws SQLException {
        return changerStatut(idTodo, "EN_COURS");
    }

    public boolean marquerTermine(int idTodo) throws SQLException {
        return changerStatut(idTodo, "DONE");
    }

    public List<Todo> getTodosByStatut(String statut) throws SQLException {
        return todoDAO.findByStatut(statut);
    }

    public Map<String, List<Todo>> getTodosOrganises() throws SQLException {
        List<Todo> todos = getAll();

        Map<String, List<Todo>> organised = new HashMap<>();
        organised.put("TODO", todos.stream().filter(Todo::isTodo).collect(Collectors.toList()));
        organised.put("EN_COURS", todos.stream().filter(Todo::isEnCours).collect(Collectors.toList()));
        organised.put("DONE", todos.stream().filter(Todo::isDone).collect(Collectors.toList()));

        return organised;
    }

    public Map<String, Long> getStatistiques() throws SQLException {
        List<Todo> todos = getAll();

        Map<String, Long> stats = new HashMap<>();
        stats.put("total", (long) todos.size());
        stats.put("todo", todos.stream().filter(Todo::isTodo).count());
        stats.put("enCours", todos.stream().filter(Todo::isEnCours).count());
        stats.put("done", todos.stream().filter(Todo::isDone).count());
        stats.put("enRetard", todos.stream().filter(Todo::estEnRetard).count());

        return stats;
    }

    public List<Todo> getTodosEnRetard() throws SQLException {
        return getAll().stream()
                .filter(Todo::estEnRetard)
                .collect(Collectors.toList());
    }
}