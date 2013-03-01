package websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.apache.catalina.websocket.WsOutbound;


public class WSserver extends WebSocketServlet
{		
		//Mysql
		static Connection con = null;
		Statement st = null;	
		static PreparedStatement selectData = null;
		static PreparedStatement selectData2 = null;
		static PreparedStatement setUnicod1 = null;
		static PreparedStatement setUnicod2 = null;

		static String url = "jdbc:mysql://127.0.0.1:3306/MyGame?useUnicode=true&amp;characterEncoding=utf-8";
		static String user = "MyGame";
		static String password = "MyGame";
		//Mysql END
	
	 	private final static Set<EchoMessageInbound> connections = new CopyOnWriteArraySet<EchoMessageInbound>(); //все соединения
	 	static HashMap<String, StreamInbound> allConnections = new HashMap<String, StreamInbound>(); //для поиска соединения по логину игрока	
	 	
	 	static String player1; //3 переменных в которые положим логины игроков
		static String player2;
		static String player3;
		static String currentPlayer;
	 	
	    @Override
	    protected StreamInbound createWebSocketInbound(String subProtocol, HttpServletRequest request) 
	    {
	        return new EchoMessageInbound();
	    }

	    private static final class EchoMessageInbound extends MessageInbound 
	    {
	    	 
	    	StreamInbound currentConnect; //Для запоминания текущего соединения
	    	
	    	
	    	@Override
	        protected void onOpen(WsOutbound outbound) {
	            connections.add(this);
	            currentConnect = this;
	        }

	        @Override
	        protected void onClose(int status) {
	        	connections.remove(this);
	        	allConnections.remove(currentPlayer);
	         }
	        @Override
	        protected void onBinaryMessage(ByteBuffer message) throws IOException {
	        	
	            getWsOutbound().writeBinaryMessage(message);   
	        }

	        @Override
	        protected void onTextMessage(CharBuffer message) throws IOException {
	        	
	        	String[] result = message.toString().split(",");
	        	
	        	if ( result[0].equals("id")) //при соединении с игрой проверка на наличии логина в игровой сессии по ее id
	        	{	        	
	        		
	        		try {
		        		Class.forName("com.mysql.jdbc.Driver");
						con = DriverManager.getConnection(url, user, password);
						setUnicod1 = con.prepareStatement("set character set utf8");
						setUnicod2 = con.prepareStatement("set names utf8");
						setUnicod1.execute();
						setUnicod2.execute();
			
						selectData = con.prepareStatement("SELECT * FROM `heroes` WHERE `login` = ? AND `name` = ?"); //проверяем соответствует ли персонаж логину
						selectData.setString(1, result[2]);
						selectData.setString(2, result[3]);
						ResultSet rs = selectData.executeQuery();
						rs.next();
						if ( rs.getRow() != 0 ) 
						{					
							selectData = con.prepareStatement("SELECT `knight`,`mage`,`archer` FROM `games` WHERE `id` = ?"); //проверяем есть ли доступ к этой игровой сессии
							selectData.setString(1, result[1]);
							rs = selectData.executeQuery();
							rs.next();
							if ( rs.getString(1).equals(result[2]) || rs.getString(2).equals(result[2]) || rs.getString(3).equals(result[2]) )
							{
								allConnections.put(result[2], currentConnect);
								currentConnect.getWsOutbound().writeTextMessage(CharBuffer.wrap("Welcome " + result[2]));
								
								//пишем логины игроков данной сессии в переменные
								currentPlayer = result[2];
								player1 = rs.getString(1);
								player2 = rs.getString(2);
								player3 = rs.getString(3);
								
							}
						}
	        		} catch (SQLException | ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        	}
	        	else if( result[0].equals("toMembersOfGame"))
	        	{
	        		for ( int i = 2; i < result.length; i++ ) //цикл для перебора всех сообщений и записи с запятими в переменную для отправки клиенту
	        		{
	        			result[1] = result[1] + "," + result[i];	
	        		}
	        		
	        		try
	        		{
	        			StreamInbound temporary1 = allConnections.get(player1); //получаем соединение по логину
	        			StreamInbound temporary2 = allConnections.get(player2); //получаем соединение по логину
	        			StreamInbound temporary3 = allConnections.get(player3); //получаем соединение по логину
	        			
	        			
	        			if ( temporary1 != null )
	        			{
	        				CharBuffer toPlayers = CharBuffer.wrap(result[1]);
	        				temporary1.getWsOutbound().writeTextMessage(toPlayers); //отправляем сообщение если соединение найдено
	        			}
	        			if( temporary2 != null )
	        			{
	        				CharBuffer toPlayers = CharBuffer.wrap(result[1]);
	        				temporary2.getWsOutbound().writeTextMessage(toPlayers); //отправляем сообщение если соединение найдено
	        			}
	        			if( temporary3 != null )
	        			{
	        				CharBuffer toPlayers = CharBuffer.wrap(result[1]);
	        				temporary3.getWsOutbound().writeTextMessage(toPlayers); //отправляем сообщение если соединение найдено
	        			}
	        			
	        		}
	        		catch( NullPointerException ex )
	        		{ ex.printStackTrace(); }
	        	}
	        	else
	        	{
	        		currentConnect.getWsOutbound().writeTextMessage(CharBuffer.wrap("Error, unknown query =)"));
	        	}

	        }        
	        private void broadcast( String messageAll ) {
	            for (EchoMessageInbound connection : connections) {
	                try {
	                    CharBuffer buffer = CharBuffer.wrap(messageAll);
	                    
	                    if(connection != currentConnect ) //отсылаем всем кроме самого себя
	                    {
	                    	connection.getWsOutbound().writeTextMessage(buffer);
	                    }
	                    
	                } catch (IOException ex) {
	                    
	                }
	            }
	        }
	    }
	    
}
