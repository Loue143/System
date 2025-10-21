package Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {


    public static Connection connectDB() {
        Connection con = null;
        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:Tasl_re.db"); 
            
        } catch (Exception e) {
            System.out.println("Connection Failed: " + e.getMessage());
        }
        return con;
    }

 
    public void addRecord(String sql, Object... values) {
        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

        
            for (int i = 0; i < values.length; i++) {
                Object val = values[i];

                if (val instanceof Integer) {
                    pstmt.setInt(i + 1, (Integer) val);
                } else if (val instanceof Double) {
                    pstmt.setDouble(i + 1, (Double) val);
                } else if (val instanceof Float) {
                    pstmt.setFloat(i + 1, (Float) val);
                } else if (val instanceof Long) {
                    pstmt.setLong(i + 1, (Long) val);
                } else if (val instanceof Boolean) {
                    pstmt.setBoolean(i + 1, (Boolean) val);
                } else if (val instanceof java.util.Date) {
                    pstmt.setDate(i + 1, new java.sql.Date(((java.util.Date) val).getTime()));
                } else if (val instanceof java.sql.Date) {
                    pstmt.setDate(i + 1, (java.sql.Date) val);
                } else if (val instanceof java.sql.Timestamp) {
                    pstmt.setTimestamp(i + 1, (java.sql.Timestamp) val);
                } else {
                    pstmt.setString(i + 1, val.toString());
                }
            }

            pstmt.executeUpdate();
            System.out.println("Record added successfully!");

        } catch (SQLException e) {
            System.out.println("Error adding record: " + e.getMessage());
        }
    }
  
    public void viewRecords(String sqlQuery, String[] columnHeaders, String[] columnNames) {
       
        if (columnHeaders.length != columnNames.length) {
            System.out.println("Error: Mismatch between column headers and column names.");
            return;
        }

        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sqlQuery);
             ResultSet rs = pstmt.executeQuery()) {

   
            StringBuilder headerLine = new StringBuilder();
            headerLine.append("--------------------------------------------------------------------------------\n| ");
            for (String header : columnHeaders) {
                headerLine.append(String.format("%-20s | ", header));
            }
            headerLine.append("\n--------------------------------------------------------------------------------");

            System.out.println(headerLine.toString());

 
            while (rs.next()) {
                StringBuilder row = new StringBuilder("| ");
                for (String colName : columnNames) {
                    String value = rs.getString(colName);
                    row.append(String.format("%-20s | ", value != null ? value : ""));
                }
                System.out.println(row.toString());
            }
            System.out.println("--------------------------------------------------------------------------------");

        } catch (SQLException e) {
            System.out.println("Error retrieving records: " + e.getMessage());
        }
    }

    
    public void updateRecord(String sql, Object... values) {
        try (Connection conn = connectDB(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

   
            for (int i = 0; i < values.length; i++) {
                if (values[i] instanceof Integer) {
                    pstmt.setInt(i + 1, (Integer) values[i]);
                } else if (values[i] instanceof Double) {
                    pstmt.setDouble(i + 1, (Double) values[i]); 
                } else if (values[i] instanceof Float) {
                    pstmt.setFloat(i + 1, (Float) values[i]); 
                } else if (values[i] instanceof Long) {
                    pstmt.setLong(i + 1, (Long) values[i]); 
                } else if (values[i] instanceof Boolean) {
                    pstmt.setBoolean(i + 1, (Boolean) values[i]); 
                } else if (values[i] instanceof java.util.Date) {
                    pstmt.setDate(i + 1, new java.sql.Date(((java.util.Date) values[i]).getTime())); 
                } else if (values[i] instanceof java.sql.Date) {
                    pstmt.setDate(i + 1, (java.sql.Date) values[i]); 
                } else if (values[i] instanceof java.sql.Timestamp) {
                    pstmt.setTimestamp(i + 1, (java.sql.Timestamp) values[i]); 
                } else {
                    pstmt.setString(i + 1, values[i].toString()); 
                }
            }

            pstmt.executeUpdate();
            System.out.println("Record updated successfully!");
        } catch (SQLException e) {
            System.out.println("Error updating record: " + e.getMessage());
        }
    }


    public void deleteRecord(String sql, Object... values) {
        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

           
            for (int i = 0; i < values.length; i++) {
                if (values[i] instanceof Integer) {
                    pstmt.setInt(i + 1, (Integer) values[i]);
                } else {
                    pstmt.setString(i + 1, values[i].toString()); 
                }
            }

            pstmt.executeUpdate();
            System.out.println("Record deleted successfully!");
        } catch (SQLException e) {
            System.out.println("Error deleting record: " + e.getMessage());
        }
    }

    
    public java.util.List<java.util.Map<String, Object>> fetchRecords(String sqlQuery, Object... values) {
    java.util.List<java.util.Map<String, Object>> records = new java.util.ArrayList<>();

    try (Connection conn = this.connectDB();
         PreparedStatement pstmt = conn.prepareStatement(sqlQuery)) {

        for (int i = 0; i < values.length; i++) {
            pstmt.setObject(i + 1, values[i]);
        }

        ResultSet rs = pstmt.executeQuery();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (rs.next()) {
            java.util.Map<String, Object> row = new java.util.HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                row.put(metaData.getColumnName(i), rs.getObject(i));
            }
            records.add(row);
        }

    } catch (SQLException e) {
        System.out.println("Error fetching records: " + e.getMessage());
    }

    return records;
    }
}
