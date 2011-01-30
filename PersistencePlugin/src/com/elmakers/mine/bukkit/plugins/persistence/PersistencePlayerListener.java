package com.elmakers.mine.bukkit.plugins.persistence;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerListener;

public class PersistencePlayerListener extends PlayerListener
{

	@Override
	public void onPlayerCommand(PlayerChatEvent event)
	{
		Persistence persistence = Persistence.getInstance();
		Player player = event.getPlayer();
		
		// Currently only ops can use Persistence commands.
		if (!player.isOp()) return;
		
		String[] split = event.getMessage().split(" ");
    	String commandString = split[0];
    	
    	if (!commandString.equalsIgnoreCase("/persist")) return;
    	
    	event.setCancelled(true);
    	
    	if (split.length == 1)
    	{
    		player.sendMessage("Use \"/persist help\" for help.");
    		return;
    	}
    	
    	String command = split[1];
    	
    	if (command.equalsIgnoreCase("help"))
    	{
    		printHelp(player);
    		return;
    	}

    	if (command.equalsIgnoreCase("save"))
    	{
    		persistence.save();
    		player.sendMessage("Data saved");
    		return;
    	}
    	
    	if (command.equalsIgnoreCase("reload"))
    	{
    		if (split.length < 3)
    		{
    			player.sendMessage("Use: \"/persist reload <schema>.<entity>");
    			return;
    		}
    		String[] entityPath = split[2].split("\\.");
    		if (entityPath.length < 2)
    		{
    			player.sendMessage("Use: \"/persist reload <schema>.<entity>");
    			return;
    		}
    		String schemaName = entityPath[0];
    		String entityName = entityPath[1];	
    		reloadEntity(player, schemaName, entityName);
    		return;
    	}
    	
       	if (command.equals("RESET"))
    	{
    		if (split.length < 3)
    		{
    			player.sendMessage("Use: \"/perist RESET <schema>.<entity>");
    			return;
    		}
    		String[] entityPath = split[2].split("\\.");
    		if (entityPath.length < 2)
    		{
    			player.sendMessage("Use: \"/persist RESET <schema>.<entity>");
    			return;
    		}
    		String schemaName = entityPath[0];
    		String entityName = entityPath[1];	
    		resetEntity(player, schemaName, entityName);
    		return;
    	}
       	
       	if (command.equalsIgnoreCase("reset"))
       	{
       		player.sendMessage("Use: \"/persist RESET <schema>.<entity>");
       		player.sendMessage("Be VERY sure!");
       		return;
       	}
    	
    	if (command.equalsIgnoreCase("describe"))
    	{
    		if (split.length < 3)
    		{
    			listSchemas(player);
    			return;
    		}
    		
    		String[] entityPath = split[2].split("\\.");
    		if (entityPath.length == 1)
    		{
    			describeSchema(player, entityPath[0]);
    		}
    		else
    		{
    			describeEntity(player, entityPath[0], entityPath[1]);
    		}
    		return;
    	}
    	
    	if (command.equalsIgnoreCase("list"))
    	{
    		if (split.length < 3)
    		{
    			player.sendMessage("Use: \"/persist list <schema>.<entity>[.<id>]");
    			return;
    		}
    		
    		String[] entityPath = split[2].split("\\.");
    		if (entityPath.length < 2)
    		{
    			player.sendMessage("Use: \"/persist list <schema>.<entity>[.<id>]");
    			return;
    		}
    		
    		String schemaName = entityPath[0];
    		String entityName = entityPath[1];
    		
    		if (entityPath.length == 2)
    		{
    			listEntityIds(player, schemaName, entityName);
    			return;
    		}
    		
    		String id = entityPath[2];
    		listEntity(player, schemaName, entityName, id);
    		
    		return;
    	}
    	
    	player.sendMessage("Unknown /persist command. Type \"/persist help\" for help.");
	}
	
	protected void listEntity(Player player, String schemaName, String entityName, String id)
	{
		PersistedClass persisted = getEntity(player, schemaName, entityName);
		if (persisted == null) return;
		
		Object instance = persisted.get(id);
		
		if (instance == null)
		{
			player.sendMessage("Can't find entity " + schemaName + "." + entityName + ", " + persisted.getIdField().getName() + "=" + id);
			return;
		}
		
		List<String> rows = new ArrayList<String>();
		for (PersistedField field : persisted.getPersistedFields())
		{
			String fieldName = field.getName();
			fieldName = padColumn(fieldName);
			
			
			Object data = field.get(instance);
			String dataField = "null";
			if (data != null)
			{
				dataField = data.toString();
			}
			
			String row = fieldName + " = " + dataField;
			rows.add(row);
		}
		
		player.sendMessage("Entity " + schemaName + "." + entityName + ":");
		for (String row : rows)
		{
			player.sendMessage(row);
		}
	}
	
	protected void listEntities(Player player, String schemaName, String entityName)
	{
		PersistedClass persisted = getEntity(player, schemaName, entityName);
		if (persisted == null) return;
		
		String heading = "";
		List<String> rows = new ArrayList<String>();
		for (PersistedField field : persisted.getPersistedFields())
		{
			String fieldName = field.getName();
			fieldName = padColumn(fieldName);
			
			String newHeading = heading +  fieldName + " | ";
			if (newHeading.length() > maxLineLength)
			{
				break;
			}
			heading = newHeading;
		}
		
		int lineCount = 0;
		List<Object> entities = new ArrayList<Object>();
		persisted.getAll(entities);
		
		for (Object entity : entities)
		{
			String row = "";
			for (PersistedField field : persisted.getPersistedFields())
			{
				Object data = field.get(entity);
				String dataField = "null";
				if (data != null)
				{
					dataField = data.toString();
				}
				dataField = padColumn(dataField);
				String newRow = row + dataField + " | ";
				if (newRow.length() > maxLineLength)
				{
					break;
				}
				row = newRow;
			}
			
			rows.add(row);
			
			if (lineCount >= maxLineCount)
			{
				break;
			}
		}
		
		player.sendMessage(heading);
		for (String row : rows)
		{
			player.sendMessage(row);
		}
		
	}
	
	protected String padColumn(String column)
	{
		return String.format("%1$-" + maxColumnWidth + "s", column);
	}
	
	protected void listEntityIds(Player player, String schemaName, String entityName)
	{
		PersistedClass persisted = getEntity(player, schemaName, entityName);
		if (persisted == null) return;
		
		List<Object> entities = new ArrayList<Object>();
		persisted.getAll(entities);
		
		player.sendMessage(schemaName + "." + entityName + ", " + entities.size() + " entities:");
		
		int idCount = 0;
		List<String> idLines = new ArrayList<String>();
		String currentLine = "";
		for (Object entity : entities)
		{
			boolean firstInLine = currentLine.length() == 0;
			
			if (!firstInLine)
			{
				currentLine += ", ";
			}
			else
			{
				currentLine = " ";
			}
			String thisId = persisted.getId(entity).toString();
			String newLine = currentLine + thisId;
			if (newLine.length() > maxLineLength)
			{
				idLines.add(currentLine);
				currentLine = " " + thisId;
			}
			else
			{
				currentLine = newLine;
			}
			idCount++;
			if (idCount >= maxIdCount)
			{
				currentLine += "...";
			}
		}
		if (currentLine.length() > 0)
		{
			idLines.add(currentLine);
		}
		for (String idLine : idLines)
		{
			player.sendMessage(idLine);
		}
	}
	
	protected void listSchemas(Player player)
	{
		Persistence persistence = Persistence.getInstance();
		player.sendMessage("Schemas:");
		List<Schema> schemas = persistence.getSchemaList();
		for (Schema schema : schemas)
		{
			String schemaMessage = " " + schema.getName() + " [" + schema.getPersistedClasses().size() + "]";
			player.sendMessage(schemaMessage);
		}
	}
	
	protected PersistedClass getEntity(Player player, String schemaName, String entityName)
	{
		Persistence persistence = Persistence.getInstance();
		Schema schema = persistence.getSchema(schemaName);
		if (schema == null)
		{
			player.sendMessage("Unknown schema: " + schemaName);
			return null;
		}
		PersistedClass persisted = schema.getPersistedClass(entityName);
		if (persisted == null)
		{
			player.sendMessage("Unknown entity: " + schemaName + "." + entityName);
			return null;
		}
		return persisted;
	}
	
	protected void describeSchema(Player player, String schemaName)
	{
		Persistence persistence = Persistence.getInstance();
		Schema schema = persistence.getSchema(schemaName);
		if (schema == null)
		{
			player.sendMessage("Unknown schema: " + schemaName);
			return;
		}		
		player.sendMessage("Schema " + schemaName + ":");
		for (PersistedClass persisted : schema.getPersistedClasses())
		{
			String schemaMessage = " " + persisted.getTableName();
			player.sendMessage(schemaMessage);
		}
	}

	protected void describeEntity(Player player, String schemaName, String entityName)
	{
		PersistedClass persisted = getEntity(player, schemaName, entityName);
		if (persisted == null) return;
		
		player.sendMessage("Entity " + schemaName + "." + entityName + ":");
		for (PersistedField field : persisted.getPersistedFields())
		{
			String entityMessage = " " + field.getName() + " : " + field.getColumnType();
			player.sendMessage(entityMessage);
		}
	}
	
	protected void reloadEntity(Player player, String schemaName, String entityName)
	{
		PersistedClass persisted = getEntity(player, schemaName, entityName);
		if (persisted == null) return;
		
		player.sendMessage("Reloading entity: " + schemaName + "." + entityName);
		persisted.clear();
	}
	
	protected void resetEntity(Player player, String schemaName, String entityName)
	{
		PersistedClass persisted = getEntity(player, schemaName, entityName);
		if (persisted == null) return;
		
		player.sendMessage("RESETTING entity: " + schemaName + "." + entityName);
		persisted.reset();
	}
	
	protected void printHelp(Player player)
	{
		String helpMessage = 
			"Persistence:\r"
		+	"/persist save : Save cached entities\r"
		+	"/persist describe : List all schemas\r"
		+	"/persist describe <schema> : List entities in a schema\r"
		+	"/persist describe <schema>.<entity> : Describe an entity\r"
		+	"/persist list <schema>.<entity> : List all entity ids\r"
		+	"/persist list <schema>.<entity>.<id> : List an entity\r"
		+	"/persist reload <schema>.<entity> : Reload entities\r"
		+	"/persist RESET <schema>.<entity> : DROP entity table!";
		
		String[] lines = helpMessage.split("\r");
		for (String line : lines)
		{
			player.sendMessage(line);
		}
	}
	
	private int maxLineCount = 10;
	private int maxColumnWidth = 10;
	private int maxLineLength = 50;
	private int maxIdCount = 50;

}