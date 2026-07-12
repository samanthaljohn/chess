package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClearServiceTest {

    @Test
    void clear() throws DataAccessException {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        ClearService clearService = new ClearService(dataAccess);

        UserData user = new UserData("newUsername", "password", "myemail@gmail.com");
        dataAccess.createUser(user);
        dataAccess.createGame("newGame");
        AuthData auth = new AuthData("token123", "someUser");
        dataAccess.createAuth(auth);

        clearService.clear();

        assertNull(dataAccess.getUser("newUsername"));
        assertTrue(dataAccess.listGames().isEmpty());
        assertNull(dataAccess.getAuth("token123"));
    }
}