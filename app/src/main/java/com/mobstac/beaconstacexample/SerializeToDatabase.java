package com.mobstac.beaconstacexample;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

/**
 * Created by admin on 11/13/2015.
 */
public class SerializeToDatabase {

    private static final String SQL_SERIALIZE_OBJECT = "INSERT INTO serialized_java_objects(object_name, serialized_object) VALUES (?, ?)";
    private static final String SQL_DESERIALIZE_OBJECT = "SELECT serialized_object FROM serialized_java_objects WHERE serialized_id = ?";

    public static long serializeJavaObjectToDB(Connection connection, Object objectToSerialize) throws SQLException {

        PreparedStatement pstmt = connection.prepareStatement(SQL_SERIALIZE_OBJECT);

        // just setting the class name
        pstmt.setString(1, objectToSerialize.getClass().getName());
        pstmt.setObject(2, objectToSerialize);
        pstmt.executeUpdate();
        ResultSet rs = pstmt.getGeneratedKeys();
        int serialized_id = -1;
        if (rs.next()) {
            serialized_id = rs.getInt(1);
        }
        rs.close();
        pstmt.close();
        System.out.println("Java object serialized to database. Object: " + objectToSerialize);
        return serialized_id;
    }

    /**
     * To de-serialize a java object from database
     *
     * @throws SQLException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object deSerializeJavaObjectFromDB(Connection connection, long serialized_id) throws SQLException, IOException, ClassNotFoundException {
        PreparedStatement pstmt = connection.prepareStatement(SQL_DESERIALIZE_OBJECT);
        pstmt.setLong(1, serialized_id);
        ResultSet rs = pstmt.executeQuery();
        rs.next();

        // Object object = rs.getObject(1);

        byte[] buf = rs.getBytes(1);
        ObjectInputStream objectIn = null;
        if (buf != null)
            objectIn = new ObjectInputStream(new ByteArrayInputStream(buf));

        Object deSerializedObject = objectIn.readObject();

        rs.close();
        pstmt.close();

        System.out.println("Java object de-serialized from database. Object: "
                + deSerializedObject + " Classname: "
                + deSerializedObject.getClass().getName());
        return deSerializedObject;
    }

    /**
     * Serialization and de-serialization of java object from mysql
     *
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws IOException
     */
    public static void main(String args[]) throws ClassNotFoundException, SQLException, IOException {
        Connection connection = null;

        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://localhost/javaserialization";
        String username = "root";
        String password = "admin";
        Class.forName(driver);
        connection = DriverManager.getConnection(url, username, password);

        // a sample java object to serialize
        Vector obj = new Vector();
        obj.add("java");
        obj.add("papers");

        // serializing java object to mysql database
        long serialized_id = serializeJavaObjectToDB(connection, obj);

        // de-serializing java object from mysql database
        Vector objFromDatabase = (Vector) deSerializeJavaObjectFromDB(connection, serialized_id);

        connection.close();
    }
}
