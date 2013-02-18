package mysql;


import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONException;
import org.json.JSONObject;


public class MysqlLib extends HttpServlet {
	
	
	Connection con = null;
	Statement st = null;

	PreparedStatement insertReg = null;
	PreparedStatement insertData = null;
	PreparedStatement selectData = null;
	PreparedStatement updateData = null;
	PreparedStatement setUnicod1 = null;
	PreparedStatement setUnicod2 = null;

	String url = "jdbc:mysql://127.0.0.1:3306/MyGame?useUnicode=true&amp;characterEncoding=utf-8";
	String user = "MyGame";
	String password = "MyGame";

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		
		JSONObject json = new JSONObject();
		
		//Авторизация
		
		if ( request.getParameter("action").equals("auth") )
		{	
			try {
					
					Class.forName("com.mysql.jdbc.Driver");
					con = DriverManager.getConnection(url, user, password);
					setUnicod1 = con.prepareStatement("set character set utf8");
					setUnicod2 = con.prepareStatement("set names utf8");
					setUnicod1.execute();
					setUnicod2.execute();
		
					selectData = con.prepareStatement("SELECT `login`,`password` FROM `users` WHERE `login` = ? AND `password` = ? ");
					selectData.setString(1, request.getParameter("login"));
					selectData.setString(2, request.getParameter("password"));
					ResultSet rs = selectData.executeQuery();
					rs.next();
					json.put("result", rs.getRow());
					out.print( request.getParameter("callback") + "(" + json.toString() + ")" );	
					con.close();
					
					HttpSession session = request.getSession(true);
					session.setAttribute("login", request.getParameter("login"));
				} 
			catch(SQLException | ClassNotFoundException | JSONException e){ e.printStackTrace(); }
		}			
		else if( request.getParameter("action").equals("reg") )  //регистрация
		{
			try {
				
				Class.forName("com.mysql.jdbc.Driver");
				con = DriverManager.getConnection(url, user, password);
				setUnicod1 = con.prepareStatement("set character set utf8");
				setUnicod2 = con.prepareStatement("set names utf8");
				setUnicod1.execute();
				setUnicod2.execute();
				
				selectData = con.prepareStatement("SELECT `login` FROM `users` WHERE `login` = ? "); //Check the existing login in base
				selectData.setString(1, request.getParameter("login"));
				ResultSet rs = selectData.executeQuery();
				rs.next();
				if ( rs.getRow() == 0 ) //если такого логина в базе не найдено - регистрируем
				{
					insertReg = con.prepareStatement("INSERT INTO `users` (`login`, `password`) VALUES ( ?, ? )");
					insertReg.setString(1, request.getParameter("login"));
					insertReg.setString(2, request.getParameter("password"));

					con.close();
					
					HttpSession session = request.getSession(true);
					session.setAttribute("login", request.getParameter("login"));
				}
				else
				{
					json.put("result", "false");
					out.print( request.getParameter("callback") + "(" + json.toString() + ")" );	
				}
			} 
			catch(SQLException | ClassNotFoundException | JSONException e){ e.printStackTrace(); }
		}
		else if( request.getParameter("action").equals("checkSession") ) //проверяем есть ли логин в сессийной переменной ( авторизован ли )
		{
			HttpSession session = request.getSession(true);
			
			if ( session.getAttribute("login") != null )
			{
				try {
					
					json.put("result", session.getAttribute("login")); //отправляем логин из сессионной переменной
					out.print( request.getParameter("callback") + "(" + json.toString() + ")" );
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else
			{
				
				response.sendRedirect(response.encodeRedirectURL("http://427044.dyn.ufanet.ru/MyGame/index.html"));
			
			}
				
		}
		else if( request.getParameter("action").equals("newChar") ) //создаем персонажа
		{
			HttpSession session = request.getSession(true);
			
			if ( session.getAttribute("login") != null )
			{
				try { 
					System.out.println(session.getAttribute("login").toString());
					
					Class.forName("com.mysql.jdbc.Driver");
					con = DriverManager.getConnection(url, user, password);
					setUnicod1 = con.prepareStatement("set character set utf8");
					setUnicod2 = con.prepareStatement("set names utf8");
					setUnicod1.execute();
					setUnicod2.execute();
					
					selectData = con.prepareStatement("SELECT `name` FROM `heroes` WHERE `name` = ? "); //Проверяем наличие персонажа с таким именем
					selectData.setString(1, request.getParameter("name"));
					ResultSet rs = selectData.executeQuery();
					rs.next();
					if ( rs.getRow() == 0 ) //если такого имени героя в базе не найдено - регистрируем
					{	
	
						insertReg = con.prepareStatement("INSERT INTO `heroes` (`login`, `name`, `role`) VALUES ( ?, ?, ? )");
						insertReg.setString(1, session.getAttribute("login").toString());
						insertReg.setString(2, request.getParameter("name"));
						insertReg.setString(3, request.getParameter("role"));
						
						insertReg.execute();
						con.close();
						
						json.put("result", "ok");
						out.print( request.getParameter("callback") + "(" + json.toString() + ")" );		
					}
					else
					{
	
						json.put("result", "false");
						out.print( request.getParameter("callback") + "(" + json.toString() + ")" );	
					}
				} 
				catch(SQLException | ClassNotFoundException | JSONException e){ e.printStackTrace(); }
			}
		}
		else if( request.getParameter("action").equals("getCharacters") ) //берем персонажей
		{
			HttpSession session = request.getSession(true);
			
			if ( session.getAttribute("login") != null )
			{
				try {
					
					Class.forName("com.mysql.jdbc.Driver");
					con = DriverManager.getConnection(url, user, password);
					setUnicod1 = con.prepareStatement("set character set utf8");
					setUnicod2 = con.prepareStatement("set names utf8");
					setUnicod1.execute();
					setUnicod2.execute();
		
					selectData = con.prepareStatement("SELECT COUNT(`name`) FROM `heroes` WHERE `login` = ?"); //получаем кол-во строк
					selectData.setString(1, session.getAttribute("login").toString() );
					ResultSet countResult = selectData.executeQuery();
					countResult.next();
					int count = countResult.getInt(1);//Значение будет только одной строкой, эту строку и выбираем
					String[] character = new String[count]; //создаем массив численностью в кол-во строк что получили из базы
					
					selectData = con.prepareStatement("SELECT `name`,`role`,`lvl`,`exp` FROM `heroes` WHERE `login` = ?"); //получаем строки 
					selectData.setString(1, session.getAttribute("login").toString() );
					ResultSet rs = selectData.executeQuery();
					
					for(int i = 0; i < count; i++) //кладем в массим в удобной форме, чтобы прочитать на клиентской стороне
					{	
						rs.next();
						character[i] = rs.getString(1) + "," + rs.getString(2) + "," + rs.getString(3) + "," + rs.getString(4);						
					}
					json.put( "result", character);
					out.print( request.getParameter("callback") + "(" + json.toString() + ")" );	
					con.close();
	
				} 
				catch(SQLException | ClassNotFoundException | JSONException e){ e.printStackTrace(); }
			}
		}
		else if( request.getParameter("action").equals("newGame") ) //добавление новой игры в БД
		{
			HttpSession session = request.getSession(true);
			
			if ( session.getAttribute("login") != null )
			{
				try
				{
					Class.forName("com.mysql.jdbc.Driver");
					con = DriverManager.getConnection(url, user, password);
					setUnicod1 = con.prepareStatement("set character set utf8");
					setUnicod2 = con.prepareStatement("set names utf8");
					setUnicod1.execute();
					setUnicod2.execute();
					
					selectData = con.prepareStatement("SELECT `login` FROM `games` WHERE `login` = ? "); //Проверяем наличие уже созданной игры по логину
					selectData.setString(1, session.getAttribute("login").toString());
					ResultSet rs = selectData.executeQuery();
					rs.next();
					if ( rs.getRow() == 0 ) //если такой игры в базе не найдено - регистрируем
					{	
						String role = null;
	
						//Назначаем ячейку таблицы согласно полученному Id персонажа
		
						if ( session.getAttribute("role").equals("1") )
						{
							role = "knight";
						}
						else if( session.getAttribute("role").equals("2") )
						{
							role = "mage";
						}
						else if( session.getAttribute("role").equals("3") )
						{
							role = "archer";
						}
						
						if( role != null )
						{
							
							insertReg = con.prepareStatement("INSERT INTO `games` (`login`, `title`, `password`, "+ role +") VALUES ( ?, ?, ?, ? )");
							insertReg.setString(1, session.getAttribute("login").toString());
							insertReg.setString(2, request.getParameter("title"));
							insertReg.setString(3, request.getParameter("password"));
							insertReg.setString(4, session.getAttribute("login").toString());
							
							insertReg.execute();
							con.close();
							
							json.put("result", "ok");
							out.print( request.getParameter("callback") + "(" + json.toString() + ")" );	
						}
					}
					else
					{
		
						json.put("result", "false");
						out.print( request.getParameter("callback") + "(" + json.toString() + ")" );	
					}
				} 
				catch(SQLException | ClassNotFoundException | JSONException e){ e.printStackTrace(); }
			}
		}
		else if( request.getParameter("action").equals("showGames"))
		{
			HttpSession session = request.getSession(true);
			
			if ( session.getAttribute("login") != null )
			{
				try {
					
					Class.forName("com.mysql.jdbc.Driver");
					con = DriverManager.getConnection(url, user, password);
					setUnicod1 = con.prepareStatement("set character set utf8");
					setUnicod2 = con.prepareStatement("set names utf8");
					setUnicod1.execute();
					setUnicod2.execute();
		
					selectData = con.prepareStatement("SELECT COUNT(`login`) FROM `games` WHERE `login` = ?"); //получаем кол-во строк
					selectData.setString(1, session.getAttribute("login").toString() );
					ResultSet countResult = selectData.executeQuery();
					countResult.next();
					int count = countResult.getInt(1);//Значение будет только одной строкой, эту строку и выбираем
					String[] games = new String[count]; //создаем массив численностью в кол-во строк что получили из базы
					
					selectData = con.prepareStatement("SELECT `login`,`title`,`knight`,`mage`,`archer` FROM `games` WHERE `login` = ?"); //получаем строки 
					selectData.setString(1, session.getAttribute("login").toString() );
					ResultSet rs = selectData.executeQuery();
					
					for(int i = 0; i < count; i++) //кладем в массим в удобной форме, чтобы прочитать на клиентской стороне
					{	
						rs.next();
						games[i] = rs.getString(1) + "," + rs.getString(2) + "," + rs.getString(3) + "," + rs.getString(4) + "," + rs.getString(5);						
					}
					json.put( "result", games);
					out.print( request.getParameter("callback") + "(" + json.toString() + ")" );	
					con.close();
	
				} 
				catch(SQLException | ClassNotFoundException | JSONException e){ e.printStackTrace(); }
			}
		}
		else if( request.getParameter("action").equals("choiseChar")) //передаем игроку его роль
		{
			HttpSession session = request.getSession(true);
			
			try {
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection(url, user, password);
			setUnicod1 = con.prepareStatement("set character set utf8");
			setUnicod2 = con.prepareStatement("set names utf8");
			setUnicod1.execute();
			setUnicod2.execute();
			
			selectData = con.prepareStatement("SELECT `name`,`role`,`login` FROM `heroes` WHERE `name` = ? "); //Берем из базы ник и роль игрока
			selectData.setString(1, request.getParameter("name"));
			ResultSet rs = selectData.executeQuery();
			rs.next();
			
			//пишем в сессионные переменные
			session.setAttribute("name", rs.getString(1));
			session.setAttribute("role", rs.getString(2));
			
			if ( session.getAttribute("login").toString().equals(rs.getString(3))) //проверяем чтобы логин сессии соответствовал логину выбранного персонажа
			{
				json.put( "result", "Вы выбрали " + session.getAttribute("name"));
				out.print( request.getParameter("callback") + "(" + json.toString() + ")" );	
				con.close();
			}
			else
			{
				json.put( "result", "Ошибка, пожалуйста перейдите на страницу авторизации.");
				out.print( request.getParameter("callback") + "(" + json.toString() + ")" );	
				con.close();
			}
			
			} catch (ClassNotFoundException | SQLException | JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		else
		{
			
			response.sendRedirect(response.encodeRedirectURL("http://427044.dyn.ufanet.ru/MyGame/index.html"));
		}
	}	
}