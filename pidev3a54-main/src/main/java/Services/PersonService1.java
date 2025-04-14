package Services;

import Models.Person;
import Utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersonService1 implements IService<Person> {
    Connection con;

    PersonService1() {
        con = DataSource.getDataSource().getConnection();

    }

    @Override
    public void add(Person person) throws SQLException {
        String query = "INSERT INTO person (FirstName,lastName,age) VALUES (?,?,?)";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, person.getFirstName());
        ps.setString(2, person.getLastName());
        ps.setInt(3, person.getAge());
        ps.executeUpdate();
    }

    @Override
    public void update(Person person) throws SQLException {
        String query = "UPDATE person SET FirstName = ?, lastName = ?, age = ? WHERE id = ?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, person.getFirstName());
        ps.setString(2, person.getLastName());
        ps.setInt(3, person.getAge());
        ps.setInt(4, person.getId());
        ps.executeUpdate();
    }

    @Override
    public void delete(Person person) throws SQLException {


        String query = "DELETE FROM person WHERE id = ?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, person.getId());
        ps.executeUpdate();
    }

    @Override
    public List<Person> getAll() throws SQLException {
        List<Person> persons = new ArrayList<>();
        String query = "SELECT * FROM person";
        PreparedStatement ps = con.prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Person person = new Person();
            person.setId(rs.getInt("id"));
            person.setFirstName(rs.getString("FirstName"));
            person.setLastName(rs.getString("lastName"));
            person.setAge(rs.getInt("age"));
            persons.add(person);
        }
        return persons;
    }
}
