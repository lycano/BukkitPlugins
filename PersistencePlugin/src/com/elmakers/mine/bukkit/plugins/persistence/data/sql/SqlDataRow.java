package com.elmakers.mine.bukkit.plugins.persistence.data.sql;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import com.elmakers.mine.bukkit.plugins.persistence.data.DataField;
import com.elmakers.mine.bukkit.plugins.persistence.data.DataRow;
import com.elmakers.mine.bukkit.plugins.persistence.data.DataTable;
import com.elmakers.mine.bukkit.plugins.persistence.data.DataType;

public class SqlDataRow extends DataRow
{
	/**
	 * Create a DataRow based on a SQL ResultSet
	 * 
	 * This constructor is used to create a DataRow for reading from a store.
	 * 
	 * @param row The SQL ResultSet to read from
	 */
	public SqlDataRow(DataTable table, ResultSet row)
	{
		super(table);
		
		try
		{
			ResultSetMetaData rowInfo = row.getMetaData();
			int columnCount = rowInfo.getColumnCount();
			for (int i = 1; i <= columnCount; i++)
			{
				int sqlType = rowInfo.getColumnType(i);
				String columnName = rowInfo.getColumnName(i);
				Object data = row.getObject(i);
				DataType dataType = getTypeFromSqlType(sqlType);
				DataField field = new DataField(columnName, dataType, data);
				add(field);
			}
		}
		catch (SQLException ex)
		{
			log.warning("Persistence: error creating SQLDataRow: " + ex.getMessage());
		}
		
	}
	
	/**
	 * Convert a SQL data type to a DataType.
	 * 
	 * Only converts supported DataTypes.
	 * 
	 * @param sqlType The java.sql.Type value to convert
	 * @return A DataType value, or DataType.NULL if invalid
	 */
	public static DataType getTypeFromSqlType(int sqlType)
	{
		DataType dataType = DataType.NULL;
		
		switch(sqlType)
		{
			case java.sql.Types.BOOLEAN:
				dataType = DataType.BOOLEAN;
				break;
			case java.sql.Types.BIT:
				dataType = DataType.BOOLEAN;
				break;	
			case java.sql.Types.DATE:
				dataType = DataType.DATE;
				break;
			case java.sql.Types.DECIMAL:
				dataType = DataType.DOUBLE;
				break;	
			case java.sql.Types.DOUBLE:
				dataType = DataType.DOUBLE;
				break;	
			case java.sql.Types.FLOAT:
				dataType = DataType.DOUBLE;
				break;	
			case java.sql.Types.INTEGER:
				dataType = DataType.INTEGER;
				break;	
			case java.sql.Types.JAVA_OBJECT:
				dataType = DataType.OBJECT;
				break;	
			case java.sql.Types.LONGNVARCHAR:
				dataType = DataType.STRING;
				break;	
			case java.sql.Types.LONGVARCHAR:
				dataType = DataType.STRING;
				break;	
			case java.sql.Types.NCHAR:
				dataType = DataType.STRING;
				break;	
			case java.sql.Types.NULL:
				dataType = DataType.NULL;
				break;	
			case java.sql.Types.NUMERIC:
				dataType = DataType.INTEGER;
				break;	
			case java.sql.Types.REAL:
				dataType = DataType.DOUBLE;
				break;	
			case java.sql.Types.ROWID:
				dataType = DataType.INTEGER;
				break;	
			case java.sql.Types.SMALLINT:
				dataType = DataType.INTEGER;
				break;	
			case java.sql.Types.TIME:
				dataType = DataType.DATE;
				break;	
			case java.sql.Types.TIMESTAMP:
				dataType = DataType.DATE;
				break;	
			case java.sql.Types.TINYINT:
				dataType = DataType.INTEGER;
				break;	
			case java.sql.Types.VARCHAR:
				dataType = DataType.STRING;
				break;	
		}
		
		return dataType;
	}
	
}
