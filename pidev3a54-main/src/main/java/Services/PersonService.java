package Services;

import Models.Person;
import Utils.DataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PersonService implements IService<Person> {
    Connection con;

    public PersonService() {
        con = DataSource.getDataSource().getConnection();
    }

    @Override
    public void add(Person person) throws SQLException {
        String query = "INSERT INTO person (FirstName,lastName,age) VALUES ('" + person.getFirstName() + "', '" + person.getLastName() + "','" + person.getAge() + "')";
        Statement stmt = con.createStatement();
        stmt.executeUpdate(query);
    }

    @Override
    public void update(Person person) throws SQLException {
        String query = "UPDATE person SET firstName = '" + person.getFirstName() + "',lastName = '" + person.getLastName() + "', age = '" + person.getAge() + "' WHERE id = '" + person.getId() + "'";
        Statement stmt = con.createStatement();
        stmt.executeUpdate(query);

    }

    @Override
    public void delete(Person person) throws SQLException {
        String query = "DELETE FROM person WHERE id = '" + person.getId() + "'";
        Statement statement = con.createStatement();
        statement.executeUpdate(query);
    }

    @Override
    public List<Person> getAll() throws SQLException {
        String query = "SELECT * FROM person";
        Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery(query);
        List<Person> persons = new ArrayList<>();
        while (rs.next()) {
            Person person = new Person();
            person.setId(rs.getInt(1));
            person.setFirstName(rs.getString("firstName"));
            person.setLastName(rs.getString("lastName"));
            person.setAge(rs.getInt("age"));
            persons.add(person);
        }

        return persons;
    }
}
