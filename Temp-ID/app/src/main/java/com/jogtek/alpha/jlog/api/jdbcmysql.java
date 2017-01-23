package com.jogtek.alpha.jlog.api;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class jdbcmysql {
	private Connection con = null; // Database objects
	// �s��object
	private Statement stat = null;
	// ����,�ǤJ��sql������r��
	private ResultSet rs = null;
	// ���G��
	private PreparedStatement pst = null;
	// ����,�ǤJ��sql���w�x���r��,�ݭn�ǤJ�ܼƤ���m
	// ��Q��?�Ӱ��Х�

	private String dropdbSQL = "DROP TABLE User ";

	private String createdbSQL = "CREATE TABLE User (" + "    id     INTEGER "
			+ "  , name    VARCHAR(20) " + "  , passwd  VARCHAR(20))";

	private String insertdbSQL = "insert into User(id,name,passwd) "
			+ "select ifNULL(max(id),0)+1,?,? FROM User";

	private String selectSQL = "select * from User ";

	public jdbcmysql() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			// ��Udriver
			con = DriverManager.getConnection(
							"jdbc:mysql://59.124.222.153:3306/pxmart",
							"pxmart", "pxmart");
			// ��oconnection//3306//?autoReconnect=true
//?useUnicode=true&characterEncoding=Big5
			// jdbc:mysql://localhost/test?useUnicode=true&characterEncoding=Big5
			// localhost�O�D���W,test�Odatabase�W
			// useUnicode=true&characterEncoding=Big5�ϥΪ��s�X
		} catch (ClassNotFoundException e) {
			System.out.println("DriverClassNotFound :" + e.toString());
		}// ���i��|����sqlexception
		catch (SQLException x) {
			System.out.println("Exception :" + x.toString());
		}
	}

	// �إ�table���覡
	// �i�H�ݬ�Statement���ϥΤ覡
	public void createTable() {
		try {
			stat = con.createStatement();
			stat.executeUpdate(createdbSQL);
		} catch (SQLException e) {
			System.out.println("CreateDB Exception :" + e.toString());
		} finally {
			Close();
		}
	}

	// �s�W���
	// �i�H�ݬ�PrepareStatement���ϥΤ覡
	public void insertTable(String name, String passwd) {
		try {
			pst = con.prepareStatement(insertdbSQL);

			pst.setString(1, name);
			pst.setString(2, passwd);
			pst.executeUpdate();
		} catch (SQLException e) {
			System.out.println("InsertDB Exception :" + e.toString());
		} finally {
			Close();
		}
	}

	public void insertTable(String id, String time, String[] value) {
		try {
			pst = con.prepareStatement(insertdbSQL);

			pst.setString(1, id);
			pst.setString(2, time);
			for (int i = 0; i < value.length; i++) {
				pst.setString(3 + i, value[i]);
			}
			pst.executeUpdate();
		} catch (SQLException e) {
			System.out.println("InsertDB Exception :" + e.toString());
		} finally {
			Close();
		}
	}

	// �R��Table,
	// ��إ�table�ܹ�
	public void dropTable() {
		try {
			stat = con.createStatement();
			stat.executeUpdate(dropdbSQL);
		} catch (SQLException e) {
			System.out.println("DropDB Exception :" + e.toString());
		} finally {
			Close();
		}
	}

	// �d�߸��
	// �i�H�ݬݦ^�ǵ��G���Ψ�o��Ƥ覡
	public void SelectTable() {
		try {
			stat = con.createStatement();
			rs = stat.executeQuery(selectSQL);
			System.out.println("ID\t\tName\t\tPASSWORD");
			while (rs.next()) {
				System.out.println(rs.getInt("id") + "\t\t"
						+ rs.getString("name") + "\t\t"
						+ rs.getString("passwd"));
			}
		} catch (SQLException e) {
			System.out.println("DropDB Exception :" + e.toString());
		} finally {
			Close();
		}
	}

	// ����ϥΧ���Ʈw��,�O�o�n�����Ҧ�Object
	// �_�h�b����Timeout��,�i��|��Connection poor�����p
	private void Close() {
		try {
			if (rs != null) {
				rs.close();
				rs = null;
			}
			if (stat != null) {
				stat.close();
				stat = null;
			}
			if (pst != null) {
				pst.close();
				pst = null;
			}
		} catch (SQLException e) {
			System.out.println("Close Exception :" + e.toString());
		}
	}

	public static void upload(String id, String time, String[] args) {
		// ��ݬݬO�_���`
		jdbcmysql test = new jdbcmysql();
		test.insertTable(id,time, args);
	}
}
