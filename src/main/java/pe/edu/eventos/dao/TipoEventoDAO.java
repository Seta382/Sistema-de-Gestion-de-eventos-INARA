package pe.edu.eventos.dao;

import pe.edu.eventos.model.TipoEvento;
import java.util.List;

public interface TipoEventoDAO {
    List<TipoEvento> listarTodos();
    TipoEvento buscarPorId(int idTipoEvento);
}
