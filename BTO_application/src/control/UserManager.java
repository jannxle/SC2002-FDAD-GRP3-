package control;

import entities.User;
import java.util.List;

public interface UserManager<T extends User> {

    void loadUsers();

    void saveUsers();

    List<T> getUsers();

    T findByNRIC(String nric);

    boolean changePassword(String nric, String newPassword);
}