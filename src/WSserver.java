import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.apache.catalina.websocket.WsOutbound;

public class WSserver extends WebSocketServlet
{
	 	private final static Set<EchoMessageInbound> connections = new CopyOnWriteArraySet<EchoMessageInbound>();
	 	static HashMap<String, StreamInbound> allConnections = new HashMap<String, StreamInbound>(); //для поиска соединения по логину игрока
	 	
	    @Override
	    protected StreamInbound createWebSocketInbound(String subProtocol,
	            HttpServletRequest request) {
	        return new EchoMessageInbound();
	    }

	    private static final class EchoMessageInbound extends MessageInbound {
	    	 
	    	StreamInbound currentConnect; //Для запоминания текущего соединения
	    	
	    	
	    	@Override
	        protected void onOpen(WsOutbound outbound) {
	            connections.add(this);
	            currentConnect = this;
	        }

	        @Override
	        protected void onClose(int status) {
	        	connections.remove(this);
	         }
	        @Override
	        protected void onBinaryMessage(ByteBuffer message) throws IOException {
	        	
	            getWsOutbound().writeBinaryMessage(message);
	            
	        }

	        @Override
	        protected void onTextMessage(CharBuffer message) throws IOException {
	        	
	        	String[] result = message.toString().split(",");
	        	
	        	if( result[0].equals("login") ) //регистрация логина и соединения в хэшмапе
	        	{
	        		try
	        		{
	        			allConnections.put(result[1], currentConnect); //записываем текущее соединение под индексом логина
	        			currentConnect.getWsOutbound().writeTextMessage(CharBuffer.wrap("Server -- Connection Accept.")); //сообщаем что все хорошо
	        		}
	        		catch(  NullPointerException ex )
	        		{ ex.printStackTrace(); }
	        		
	        	}
	        	else if( result[0].equals("toPlayer") ) //отправка сообщения определенному соединению по логину
	        	{
	        		CharBuffer toPlayer = CharBuffer.wrap(result[2]);
	        		
	        		try
	        		{
	        			StreamInbound tempory = allConnections.get(result[1]); //получаем соединение по логину
	        			
	        			if ( tempory != null )
	        			{
	        				tempory.getWsOutbound().writeTextMessage(toPlayer); //отправляем сообщение если соединение найдено
	        			}
	        			else
	        			{
	        				currentConnect.getWsOutbound().writeTextMessage(CharBuffer.wrap("This user is Offline."));
	        				System.out.println("error, tempory is NULL");
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
